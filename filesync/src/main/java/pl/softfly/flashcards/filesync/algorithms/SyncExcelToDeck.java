package pl.softfly.flashcards.filesync.algorithms;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.softfly.flashcards.CardUtil;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.filesync.dao.CardImportedDao;
import pl.softfly.flashcards.filesync.db.FileSyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;
import pl.softfly.flashcards.entity.filesync.CardImported;
import pl.softfly.flashcards.entity.filesync.FileSynced;

/**
 * Sync changes between the current deck and the file.
 * Based on the current deck and file changes,
 * a new version of the deck is created with newest changes.
 *
 * @author Grzegorz Ziemski
 */
public class SyncExcelToDeck extends AbstractReadExcel {

    public static final int MULTIPLE_ORDINAL = 1;
    public static final int ENTITIES_TO_UPDATE_POOL_MAX = 100;

    protected static final int PAGE_LIMIT = 100;
    private static final int THREAD_TIMEOUT_SECONDS = 60;

    protected final Context appContext;
    private final CardUtil cardUtil = CardUtil.getInstance();
    @Nullable
    protected FileSyncDeckDatabase fsDeckDb;
    protected DetermineNewOrderCards determineNewOrderCards = new DetermineNewOrderCards();
    protected FileSynced fileSynced;
    protected String deckDbPath;
    @Nullable
    protected long newLastSyncAt = TimeUtil.getNowEpochSec();
    protected long fileModifiedAt;
    protected InputStream isImportedFile;
    protected Workbook workbook;
    protected Sheet sheet;

    protected int deckAdded;
    protected int deckUpdated;
    protected int deckDeleted;
    protected int fileAdded;
    protected int fileUpdated;
    protected int fileDeleted;

    //Only for tests
    public SyncExcelToDeck(Context appContext, DetermineNewOrderCards determineNewOrderCards) {
        this.appContext = appContext;
        this.determineNewOrderCards = determineNewOrderCards;
    }

    public SyncExcelToDeck(@NonNull Context appContext) {
        this.appContext = appContext;
    }

    public void syncExcelFile(
            @NonNull String deckDbPath,
            @NonNull FileSynced fileSynced,
            @NonNull InputStream inputStream,
            @NonNull String typeFile,
            @NonNull long fileModifiedAt
    ) throws Exception {
        this.deckDbPath = deckDbPath;
        this.isImportedFile = inputStream;
        this.fsDeckDb = getFsDeckDb(deckDbPath);
        this.workbook = typeFile.equals(TYPE_XLS)
                ? new HSSFWorkbook(inputStream)
                : new XSSFWorkbook(inputStream);
        this.sheet = workbook.getSheetAt(0);

        if (fileModifiedAt > TimeUtil.getNowEpochSec()) {
            throw new Exception("The synchronized file has been modified in the future.");
        } else {
            this.fileModifiedAt = fileModifiedAt;
        }

        findColumnIndexes(sheet);
        if (getTermIndex() == -1 && getDefinitionIndex() == -1)
            throw new Exception("No cards in the file.");

        // @todo desc lastSync
        if (fileSynced.getLastSyncAt() == null) {
            fileSynced.setLastSyncAt(0l);
        }
        if (fileSynced.getId() == null) {
            fileSynced.setId(Long.valueOf(fsDeckDb.fileSyncedDao().insert(fileSynced)).intValue());
        }
        this.fileSynced = fileSynced;

        // 1. Purge sync entities in the database before starting.
        fsDeckDb.cardEdgeDao().forceDeleteAll();
        fsDeckDb.cardImportedDao().deleteAll();

        // 2. Import cards {@link CardImported} and merge to the same deck card {@link Card}.
        importAndMatchCardsFromImportedFile();
        isImportedFile.close();

        // 3. Find similar cards, that is, find which cards are updated.
        matchSimilarCards();

        // 4. Find added/removed cards between deck and imported file.
        // 4.1. Remove the cards in the file, removed by the deck.
        removeCardsByDeck();
        // 4.2. Find added/removed cards between deck and imported file.
        processDeckCard();
        // 4.3. Find new cards in the imported file.
        findNewCardsInImportedFile();
        // 4.4. Enrich {@link CardImported#nextCardId} and {@link CardImported#previousCardId}.
        processCardFromFile();

        // 5. Determine the new order of the cards after merging.
        determineNewOrderCards.determineNewOrderCards(fsDeckDb, fileModifiedAt);

        // 6. Count the statistics of how many cards have been changed.
        CardImportedDao cardImportedDao = fsDeckDb.cardImportedDao();
        deckAdded = cardImportedDao.countByContentStatus(CardImported.STATUS_INSERT_BY_FILE);
        deckUpdated = cardImportedDao.countByContentStatus(CardImported.STATUS_UPDATE_BY_FILE);
        deckDeleted = cardImportedDao.countByContentStatus(CardImported.STATUS_DELETE_BY_FILE);
        fileAdded = cardImportedDao.countByContentStatus(CardImported.STATUS_INSERT_BY_DECK);
        fileUpdated = cardImportedDao.countByContentStatus(CardImported.STATUS_UPDATE_BY_DECK);
        fileDeleted = cardImportedDao.countByContentStatus(CardImported.STATUS_DELETE_BY_DECK);
    }

    /**
     * 6. Apply changes to the deck and file.
     */
    public void commitChanges(
            @NonNull FileSynced fileSynced,
            @NonNull OutputStream os
    ) throws IOException {
        updateDeckCards();
        updateExcelFile(os);

        fileSynced.setLastSyncAt(newLastSyncAt);
        fsDeckDb.fileSyncedDao().updateAll(fileSynced);
        if (fileSynced.isAutoSync()) {
            fsDeckDb.fileSyncedDao().disableAutoSyncByIdNot(fileSynced.getId());
        }
    }

    /**
     * 2. Import cards {@link CardImported} and merge to the same deck card {@link Card}.
     */
    protected void importAndMatchCardsFromImportedFile() {
        int termPosition = getTermIndex();
        int definitionPosition = getDefinitionIndex();
        int skipHeaderRows = getSkipHeaderRows();

        List<CardImported> cardsToSaveList = new LinkedList<>();
        List<Integer> cardIdsConnected = new LinkedList<>();

        int id = 1;
        int ordinal = MULTIPLE_ORDINAL;
        Iterator<Row> rowIt = sheet.iterator();
        for (int rowNum = 0; rowIt.hasNext(); rowNum++) {
            // Skip the first record if no changes.
            Row currentRow = rowIt.next();
            if (rowNum <= skipHeaderRows) {
                continue;
            }

            String term = getStringCellValue(currentRow, termPosition);
            String definition = getStringCellValue(currentRow, definitionPosition);

            if (nonEmpty(term) || nonEmpty(definition)) {
                CardImported cardImported = new CardImported();

                if (!findSameCardAndConnect(
                        cardImported,
                        term,
                        definition,
                        cardIdsConnected,
                        fileSynced.getId())
                ) {
                    cardImported.setTerm(term);
                    cardImported.setDefinition(definition);
                }

                // @todo remove, only for debugging
                //if (!empty(term)) cardImported.setTerm(term);
                //if (!empty(definition)) cardImported.setDefinition(definition);

                cardImported.setId(id);
                if (id - 1 > 0) cardImported.setPreviousId(id - 1);
                if (rowIt.hasNext()) cardImported.setNextId(id + 1);
                cardImported.setOrdinal(ordinal);
                ordinal += MULTIPLE_ORDINAL;
                id++;
                cardsToSaveList.add(cardImported);
                if (cardsToSaveList.size() % ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                    fsDeckDb.cardImportedDao().insertAll(cardsToSaveList);
                    cardsToSaveList.clear();
                    cardIdsConnected.clear();
                }
            }
        }

        if (!cardsToSaveList.isEmpty()) {
            fsDeckDb.cardImportedDao().insertAll(cardsToSaveList);
        }
    }

    @Nullable
    protected String getStringCellValue(@NonNull Row currentRow, int position) {
        if (position > -1) {
            Cell currentCell = currentRow.getCell(position);
            if (currentCell != null) {
                String value = getStringCellValue(currentCell);
                if (value != null) {
                    return value.trim();
                }
            }
        }
        return "";
    }

    protected String getStringCellValue(Cell cell) {
        if (cell.getCellType().equals(CellType.NUMERIC)) {
            return Double.toString(cell.getNumericCellValue());
        } else {
            return cell.getStringCellValue().trim();
        }
    }

    protected boolean findSameCardAndConnect(
            @NonNull CardImported cardImported,
            String term,
            String definition,
            @NonNull List<Integer> cardsConnected,
            int fileSyncedId
    ) {
        Card card = fsDeckDb.cardDao().findByTermLikeAndDefinitionLikeAndCardNull(
                term, definition, cardsConnected, fileSyncedId
        );
        if (card != null) {
            cardsConnected.add(card.getId());
            cardImported.setCardId(card.getId());
            cardImported.setContentStatus(CardImported.STATUS_UNCHANGED);
            // It will be checked and updated by {@link #checkIfPositionUnchanged}
            cardImported.setPositionStatus(
                    isImportedFileNewer(card) ?
                            CardImported.POSITION_STATUS_BY_FILE :
                            CardImported.POSITION_STATUS_BY_DECK
            );
            // @todo remove, only for debugging
            //cardImported.setTerm(term);
            //cardImported.setDefinition(definition);
            return true;
        }
        return false;
    }

    /**
     * 3. Find similar cards, that is, find which cards are updated.
     */
    protected void matchSimilarCards() throws InterruptedException {
        System.gc();

        // Assign cards to threads.
        int countCardImported = fsDeckDb.cardDao().countByCardImportedNull();
        int numCores = 1;
        if (countCardImported > 1000) {
            numCores = determineNumberCoresToUse();
        }
        int perCore = (int) Math.ceil((double) countCardImported / numCores);
        if (perCore < 1) {
            numCores = 1;
            perCore = countCardImported;
        }

        // Run threads.
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                numCores,
                numCores,
                THREAD_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        List<CardImported> cardImportedList = fsDeckDb.cardImportedDao().findByCardNull(0, perCore);
        int lastCardImportedId;
        while (!cardImportedList.isEmpty()) {
            threadPoolExecutor.submit(new MatchSimilarCardsRunnable(fsDeckDb, fileSynced, cardImportedList));
            lastCardImportedId = cardImportedList.get(cardImportedList.size() - 1).getId();
            cardImportedList = fsDeckDb.cardImportedDao().findByCardNull(lastCardImportedId, perCore);
        }
        threadPoolExecutor.shutdown();
        if (!threadPoolExecutor.awaitTermination(THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            threadPoolExecutor.shutdownNow();
        }


        if (numCores > 1) {
            fixCardsMatchedMultiple();
        }
    }

    /**
     * Stress tests have shown that more than 2-3 threads do not help.
     * Probably the bottleneck is access to the database.
     */
    protected int determineNumberCoresToUse() {
        int numCores = Runtime.getRuntime().availableProcessors();
        if (numCores > 3) {
            return 3;
        } else if (numCores > 2) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * SE_SI_02 Prevent the same card from being matched multiple times.
     * <p>
     * The search for similar cards is multi-threaded,
     * the same card may be connected many times.
     */
    protected void fixCardsMatchedMultiple() {
        fsDeckDb.cardImportedDao().clearMultipleAssignedCards();
        int lastCardImportedId;
        List<CardImported> cardImportedList = fsDeckDb.cardImportedDao().findByCardNull(0, PAGE_LIMIT);
        while (!cardImportedList.isEmpty()) {
            lastCardImportedId = cardImportedList.get(cardImportedList.size() - 1).getId();
            (new MatchSimilarCardsRunnable(fsDeckDb, fileSynced, cardImportedList)).run();
            cardImportedList = fsDeckDb.cardImportedDao().findByCardNull(lastCardImportedId, PAGE_LIMIT);
        }
    }

    /**
     * 4.1. Remove the cards in the file, removed by the deck.
     * {@link CardImported#STATUS_DELETE_BY_DECK}
     */
    protected void removeCardsByDeck() {
        List<Integer> cardIds = fsDeckDb.cardDao().findByDeletedAtNotNull();
        for (Integer cardId : cardIds) {
            if (fsDeckDb.cardImportedRemovedDao().count(fileSynced.getId(), cardId) == 0) {
                fsDeckDb.cardImportedRemovedDao().insert(fileSynced.getId(), cardId);
                fsDeckDb.cardImportedDao().setStatusByCardId(CardImported.STATUS_DELETE_BY_DECK, cardId);
            }
        }
    }

    /**
     * 4.2. Find added/removed cards between deck and imported file.
     * <p>
     * 4.2.1. Create {@link CardImported} for missing cards in the imported file:
     * {@link CardImported#STATUS_DELETE_BY_FILE}
     * {@link CardImported#STATUS_INSERT_BY_DECK}
     * {@link #createImportedCard}
     * <p>
     * 4.2.1. Enrich {@link CardImported#nextCardDeckId} and {@link CardImported#previousCardDeckId}.
     * It will be required for {@link #checkIfPositionUnchanged}
     */
    protected void processDeckCard() {
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();
        List<CardImported> cardImportedListToInsert = new LinkedList<>();
        List<Card> cardList = fsDeckDb.cardDao()
                .findByOrdinalGreaterThanOrderByOrdinal(0, fileSynced.getId());
        if (cardList.isEmpty()) return;

        // Prepare for the first iteration
        Card currentCard = cardList.remove(0);
        CardImported currentCardImported = fsDeckDb.cardImportedDao().findByCardId(currentCard.getId());

        // Processing the first card
        if (currentCardImported == null) {
            currentCardImported = createImportedCard(currentCard);
        }

        while (!cardList.isEmpty()) {
            // Prepare for the next iteration
            Card nextCard = cardList.remove(0);
            CardImported nextCardImported = fsDeckDb.cardImportedDao().findByCardId(nextCard.getId());

            // Processing
            if (nextCardImported == null) {
                nextCardImported = createImportedCard(nextCard);
            }
            currentCardImported.setNextCardDeckId(nextCardImported.getCardId());
            nextCardImported.setPreviousCardDeckId(currentCardImported.getCardId());

            // Saving
            if (currentCardImported.getId() == null) {
                cardImportedListToInsert.add(currentCardImported);
            } else {
                cardImportedListToUpdate.add(currentCardImported);
            }
            if ((cardImportedListToInsert.size() + cardImportedListToUpdate.size())
                    >= ENTITIES_TO_UPDATE_POOL_MAX) {
                fsDeckDb.cardImportedDao().insertAll(cardImportedListToInsert);
                cardImportedListToInsert.clear();
                fsDeckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                cardImportedListToUpdate.clear();
            }

            // Prepare for the next iteration
            currentCard = nextCard;
            currentCardImported = nextCardImported;
            if (cardList.isEmpty()) {
                cardList = fsDeckDb.cardDao()
                        .findByOrdinalGreaterThanOrderByOrdinal(
                                currentCard.getOrdinal(),
                                fileSynced.getId()
                        );
            }
        }

        // Final saving
        if (currentCardImported.getId() == null) {
            cardImportedListToInsert.add(currentCardImported);
        } else {
            cardImportedListToUpdate.add(currentCardImported);
        }
        if (!cardImportedListToInsert.isEmpty()) {
            fsDeckDb.cardImportedDao().insertAll(cardImportedListToInsert);
        }
        if (!cardImportedListToUpdate.isEmpty()) {
            fsDeckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
    }

    /**
     * 4.1 Create {@link CardImported} for missing cards in the imported file:
     * {@link CardImported#STATUS_DELETE_BY_FILE}
     * {@link CardImported#STATUS_INSERT_BY_DECK}
     */
    @NonNull
    protected CardImported createImportedCard(@NonNull Card card) {
        CardImported cardImported = new CardImported();
        cardImported.setCardId(card.getId());
        if (isImportedFileNewer(card)) {
            cardImported.setContentStatus(CardImported.STATUS_DELETE_BY_FILE);
        } else {
            cardImported.setContentStatus(CardImported.STATUS_INSERT_BY_DECK);
            cardImported.setPositionStatus(CardImported.POSITION_STATUS_BY_DECK);
            //@todo remove after debugging
            //cardImported.setTerm(card.getTerm());
            //cardImported.setDefinition(card.getDefinition());
        }
        return cardImported;
    }

    /**
     * 4.3. Find new cards in the imported file.
     */
    protected void findNewCardsInImportedFile() {
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();
        List<CardImported> cardImportedList = fsDeckDb.cardImportedDao()
                .findByCardNull(0, PAGE_LIMIT);
        while (!cardImportedList.isEmpty()) {
            // Prepare for the next iteration
            CardImported cardImported = cardImportedList.remove(0);

            // Processing
            if (fileSynced.getLastSyncAt() >= fileModifiedAt) {
                // SE_AD_D_04 The card must have been added in the past and has been modified in the deck.
                cardImported.setContentStatus(CardImported.STATUS_DELETE_BY_DECK);
            } else {
                cardImported.setContentStatus(CardImported.STATUS_INSERT_BY_FILE);
                cardImported.setPositionStatus(CardImported.POSITION_STATUS_BY_FILE);
            }

            // Saving
            cardImportedListToUpdate.add(cardImported);
            if (cardImportedListToUpdate.size() % ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                fsDeckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                cardImportedListToUpdate.clear();
            }

            // Prepare for the next iteration
            if (cardImportedList.isEmpty()) {
                cardImportedList = fsDeckDb.cardImportedDao()
                        .findByCardNull(cardImported.getId(), PAGE_LIMIT);
            }
        }

        // Final saving
        if (!cardImportedListToUpdate.isEmpty()) {
            fsDeckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
    }

    /**
     * 4.4.1. Enrich {@link CardImported#nextCardId} and {@link CardImported#previousCardId}.
     * It is required for {@link #checkIfPositionUnchanged}
     * 4.4.2. Set {@link CardImported#POSITION_STATUS_UNCHANGED}
     * if the card is in the same place in the deck and file.
     */
    protected void processCardFromFile() {
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();
        List<CardImported> cardImportedList = fsDeckDb.cardImportedDao()
                .findByStatusNotOrderByOrdinalAsc(new String[]{
                        CardImported.STATUS_DELETE_BY_FILE,
                        CardImported.STATUS_INSERT_BY_DECK
                }, 0);

        if (!cardImportedList.isEmpty()) {
            CardImported currentCardImported = cardImportedList.remove(0);

            while (!cardImportedList.isEmpty()) {
                // Prepare for the next iteration
                CardImported nextCardImported = cardImportedList.remove(0);

                // Processing
                currentCardImported.setNextCardId(nextCardImported.getCardId());
                nextCardImported.setPreviousCardId(currentCardImported.getCardId());
                checkIfPositionUnchanged(currentCardImported);

                // Saving
                cardImportedListToUpdate.add(currentCardImported);
                if (cardImportedListToUpdate.size() % ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                    fsDeckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                    cardImportedListToUpdate.clear();
                }

                // Prepare for the next iteration
                currentCardImported = nextCardImported;
                if (cardImportedList.isEmpty()) {
                    cardImportedList = fsDeckDb.cardImportedDao()
                            .findByStatusNotOrderByOrdinalAsc(new String[]{
                                    CardImported.STATUS_DELETE_BY_FILE,
                                    CardImported.STATUS_INSERT_BY_DECK
                            }, currentCardImported.getOrdinal());
                }
            }

            // Processing
            checkIfPositionUnchanged(currentCardImported);

            // Saving
            cardImportedListToUpdate.add(currentCardImported);
            fsDeckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
    }

    /**
     * 4.4.2. Set {@link CardImported#POSITION_STATUS_UNCHANGED}
     * if the card is in the same place in the deck and file.
     */
    protected void checkIfPositionUnchanged(@NonNull CardImported cardImported) {
        boolean previousUnchanged = Objects.equals(cardImported.getPreviousCardDeckId(), cardImported.getPreviousCardId());
        boolean nextUnchanged = Objects.equals(cardImported.getNextCardDeckId(), cardImported.getNextCardId());
        if (previousUnchanged && nextUnchanged) {
            cardImported.setPositionStatus(CardImported.POSITION_STATUS_UNCHANGED);
        }
    }

    protected void updateDeckCards() {
        List<Card> cardsListToUpdate = new LinkedList<>();
        List<CardImported> cardsImportedListToUpdate = new LinkedList<>();

        List<CardImported> cardImportedList = fsDeckDb.cardImportedDao().findAll(0);
        while (!cardImportedList.isEmpty()) {
            CardImported cardImported = cardImportedList.remove(0);

            switch (cardImported.getContentStatus()) {
                case CardImported.STATUS_UNCHANGED: {
                    Card card = fsDeckDb.cardDao().findById(cardImported.getCardId());
                    card.setOrdinal(cardImported.getNewOrdinal());
                    cardsListToUpdate.add(card);
                }
                break;
                case CardImported.STATUS_INSERT_BY_DECK: {
                    Card card = fsDeckDb.cardDao().findById(cardImported.getCardId());
                    card.setOrdinal(cardImported.getNewOrdinal());
                    card.setFileSyncCreatedAt(newLastSyncAt);
                    cardsListToUpdate.add(card);
                    fileSynced.setDeckModifiedAt(newLastSyncAt);
                }
                break;
                case CardImported.STATUS_INSERT_BY_FILE: {
                    Card card = new Card();
                    cardUtil.setTerm(card, cardImported.getTerm());
                    cardUtil.setDefinition(card, cardImported.getDefinition());
                    card.setModifiedAt(newLastSyncAt);
                    card.setCreatedAt(newLastSyncAt);
                    card.setOrdinal(cardImported.getNewOrdinal());

                    cardImported.setCardId(Long.valueOf(fsDeckDb.cardDao().insert(card)).intValue());//@todo not needed
                    cardsImportedListToUpdate.add(cardImported);
                    fileSynced.setDeckModifiedAt(newLastSyncAt);
                }
                break;
                case CardImported.STATUS_UPDATE_BY_DECK: {
                    Card card = fsDeckDb.cardDao().findById(cardImported.getCardId());
                    card.setFileSyncModifiedAt(newLastSyncAt);
                    cardsListToUpdate.add(card);
                    fileSynced.setDeckModifiedAt(newLastSyncAt);
                }
                break;
                case CardImported.STATUS_UPDATE_BY_FILE: {
                    Card card = fsDeckDb.cardDao().findById(cardImported.getCardId());
                    cardUtil.setTerm(card, cardImported.getTerm());
                    cardUtil.setDefinition(card, cardImported.getDefinition());
                    card.setModifiedAt(newLastSyncAt);

                    card.setOrdinal(cardImported.getNewOrdinal());
                    cardsListToUpdate.add(card);
                    fileSynced.setDeckModifiedAt(newLastSyncAt);
                }
                break;
                case CardImported.STATUS_DELETE_BY_FILE: {
                    Card card = fsDeckDb.cardDao().findById(cardImported.getCardId());
                    card.setDeletedAt(newLastSyncAt);
                    cardsListToUpdate.add(card);
                }
                break;
            }

            if ((cardsListToUpdate.size() + cardsImportedListToUpdate.size())
                    >= ENTITIES_TO_UPDATE_POOL_MAX) {
                fsDeckDb.cardDao().updateAll(cardsListToUpdate);
                cardsListToUpdate.clear();
                fsDeckDb.cardImportedDao().updateAll(cardsImportedListToUpdate);
                cardsImportedListToUpdate.clear();
            }
            if (cardImportedList.isEmpty()) {
                cardImportedList = fsDeckDb.cardImportedDao().findAll(cardImported.getId());
            }
        }

        if (!cardsListToUpdate.isEmpty()) {
            fsDeckDb.cardDao().updateAll(cardsListToUpdate);
        }
        if (!cardsImportedListToUpdate.isEmpty()) {
            fsDeckDb.cardImportedDao().updateAll(cardsImportedListToUpdate);
        }
    }

    public void updateExcelFile(@NonNull OutputStream os) throws IOException {
        List<CardImported> cardImportedList = fsDeckDb.cardImportedDao()
                .findByStatusNotOrderByCardOrdinalAsc(new String[]{
                        CardImported.STATUS_DELETE_BY_DECK,
                        CardImported.STATUS_DELETE_BY_FILE
                }, 0);

        int rowNum = Math.max(getSkipHeaderRows()+1, 0);
        int lastRowNum = sheet.getLastRowNum();

        for (; !cardImportedList.isEmpty(); rowNum++) {
            CardImported cardImported = cardImportedList.remove(0);
            Card card = fsDeckDb.cardDao().findById(cardImported.getCardId());

            if (CardImported.STATUS_UNCHANGED.equals(cardImported.getContentStatus())) {
                if (cardImported.isOrderChanged()) {
                    if (rowNum > lastRowNum) {
                        createExcelCell(rowNum, card);
                    } else {
                        updateExcelCell(rowNum, card);
                    }
                }
            } else if (rowNum > lastRowNum) {
                createExcelCell(rowNum, card);
            } else {
                updateExcelCell(rowNum, card);
            }

            if (cardImportedList.isEmpty()) {
                cardImportedList = fsDeckDb.cardImportedDao()
                        .findByStatusNotOrderByCardOrdinalAsc(new String[]{
                                CardImported.STATUS_DELETE_BY_DECK,
                                CardImported.STATUS_DELETE_BY_FILE
                        }, card.getOrdinal());
            }
        }
        int countLastRowsToRemove = fsDeckDb.cardImportedDao().countByDeleteByDeck() - fsDeckDb.cardImportedDao().countByInsertByDeck();
        for (int i = countLastRowsToRemove; i > 0; i--) {
            sheet.removeRow(sheet.getRow(rowNum++));
        }

        if (isImportedFile != null) this.isImportedFile.close();
        this.workbook.write(os);
        this.workbook.close();
        os.close();
    }

    protected void createExcelCell(@NonNull int rowNum, @NonNull Card card) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(getTermIndex());
        cell.setCellValue(card.getTerm());
        cell.setCellStyle(cellStyle);

        cell = row.createCell(getDefinitionIndex());
        cell.setCellValue(card.getDefinition());
        cell.setCellStyle(cellStyle);
    }

    protected void updateExcelCell(@NonNull int rowNum, @NonNull Card card) {
        Row row = sheet.getRow(rowNum);
        updateExcelCell(row, getTermIndex(), card.getTerm());
        updateExcelCell(row, getDefinitionIndex(), card.getDefinition());
    }

    protected void updateExcelCell(Row row, int index, String value) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            cell = row.createCell(index);
        } else if (cell.getCellType().equals(CellType.NUMERIC)) {
            cell = row.createCell(index);
        }
        cell.setCellValue(value);
    }

    protected boolean isImportedFileNewer(@NonNull Card card) {
        return fileSynced.getLastSyncAt() < fileModifiedAt
                && fileModifiedAt > card.getModifiedAt();
    }

    //@todo public visibility for testing
    @Nullable
    public FileSyncDeckDatabase getFsDeckDb(@NonNull String deckName) {
        return FileSyncDatabaseUtil.getInstance(appContext).getDeckDatabase(deckName);
    }

    public void setNewLastSyncAt(long newLastSyncAt) {
        this.newLastSyncAt = newLastSyncAt;
    }

    public int getDeckAdded() {
        return deckAdded;
    }

    public int getDeckUpdated() {
        return deckUpdated;
    }

    public int getDeckDeleted() {
        return deckDeleted;
    }

    public int getFileAdded() {
        return fileAdded;
    }

    public int getFileUpdated() {
        return fileUpdated;
    }

    public int getFileDeleted() {
        return fileDeleted;
    }
}