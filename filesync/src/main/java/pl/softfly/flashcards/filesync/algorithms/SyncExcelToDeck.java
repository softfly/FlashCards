package pl.softfly.flashcards.filesync.algorithms;

import static pl.softfly.flashcards.filesync.FileSyncConstants.TYPE_XLS;

import android.content.Context;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
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

import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.db.SyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.SyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.CardImported;
import pl.softfly.flashcards.tasks.LongTasksExecutor;
import pl.softfly.flashcards.ui.card.ListCardsActivity;

/**
 * Sync changes between the current deck and the file.
 * Based on the current deck and file changes,
 * a new version of the deck is created with newest changes.
 *
 * @author Grzegorz Ziemski
 */
public class SyncExcelToDeck extends AbstractReadExcel {
    public static final int MULTIPLE_ORDINAL = 100;
    public static final int ENTITIES_TO_UPDATE_POOL_MAX = 100;
    protected static final int PAGE_LIMIT = 100;

    private final Context appContext;
    private SyncDeckDatabase deckDb;
    private DetermineNewOrderCards determineNewOrderCards = new DetermineNewOrderCards();
    private ListCardsActivity listCardsActivity;

    private Long lastModifiedAtImportedFile;
    private InputStream isImportedFile;
    private Workbook workbook;
    private Sheet sheet;

    //Only for tests
    public SyncExcelToDeck(Context appContext, DetermineNewOrderCards determineNewOrderCards) {
        this.appContext = appContext;
        this.determineNewOrderCards = determineNewOrderCards;
    }

    public SyncExcelToDeck(ListCardsActivity listCardsActivity) {
        this.listCardsActivity = listCardsActivity;
        this.appContext = listCardsActivity.getApplicationContext();
    }

    public void syncExcelFile(
            String deckName,
            InputStream inputStream,
            String typeFile,
            Long lastModifiedAtImportedFile
    ) throws Exception {
        this.isImportedFile = inputStream;
        this.deckDb = getDeckDB(deckName);
        this.workbook = typeFile.equals(TYPE_XLS) ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
        this.sheet = workbook.getSheetAt(0);
        this.lastModifiedAtImportedFile = lastModifiedAtImportedFile;

        findColumnIndexes(sheet);
        if (getQuestionIndex() == -1 && getAnswerIndex() == -1)
            throw new Exception("No cards in the file.");

        // 1. Purge sync entities in the database before starting.
        deckDb.cardEdgeDao().forceDeleteAll();
        deckDb.cardImportedDao().deleteAll();

        // 2. Import cards {@link CardImported} and merge to the same deck card {@link Card}.
        importAndMatchCardsFromImportedFile();
        isImportedFile.close();

        // 3. Find similar cards, that is, find which cards are updated.
        matchSimilarCards();

        // 4. Find added/removed cards between deck and imported file.
        // 4.1. Find added/removed cards between deck and imported file.
        processDeckCard();
        // 4.2. Find new cards in the imported file.
        findNewCardsInImportedFile();
        // 4.3. Enrich {@link CardImported#nextCardId} and {@link CardImported#previousCardId}.
        processCardFromFile();

        // 5. Determine the new order of the cards after merging.
        determineNewOrderCards.determineNewOrderCards(deckDb, lastModifiedAtImportedFile);
    }

    /**
     * 6. Apply changes to the deck and file.
     */
    public void commitChanges(OutputStream os) throws IOException {
        updateDeckCards();
        updateExcelFile(os);
        if (listCardsActivity != null) listCardsActivity.onRestart();
    }

    /**
     * 2. Import cards {@link CardImported} and merge to the same deck card {@link Card}.
     */
    protected void importAndMatchCardsFromImportedFile() {
        int questionPosition = getQuestionIndex();
        int answerPosition = getAnswerIndex();
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

            String question = getStringCellValue(currentRow, questionPosition);
            String answer = getStringCellValue(currentRow, answerPosition);

            if (nonEmpty(question) || nonEmpty(answer)) {
                CardImported cardImported = new CardImported();

                if (!findSameCardAndConnect(cardImported, question, answer, cardIdsConnected)) {
                    if (!empty(question)) cardImported.setQuestion(question);
                    if (!empty(answer)) cardImported.setAnswer(answer);
                }

                // @todo remove, only for debugging
                if (!empty(question)) cardImported.setQuestion(question);
                if (!empty(answer)) cardImported.setAnswer(answer);

                cardImported.setId(id);
                if (id - 1 > 0) cardImported.setPreviousId(id - 1);
                if (rowIt.hasNext()) cardImported.setNextId(id + 1);
                cardImported.setOrdinal(ordinal);
                ordinal+=MULTIPLE_ORDINAL;
                id++;
                cardsToSaveList.add(cardImported);
                if (cardsToSaveList.size() % ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                    deckDb.cardImportedDao().insertAll(cardsToSaveList);
                    cardsToSaveList.clear();
                    cardIdsConnected.clear();
                }
            }
        }

        if (!cardsToSaveList.isEmpty()) {
            deckDb.cardImportedDao().insertAll(cardsToSaveList);
        }
    }

    protected String getStringCellValue(Row currentRow, int position) {
        if (position > -1) {
            Cell currentCell = currentRow.getCell(position);
            String value = currentCell.getStringCellValue();
            if (value != null) {
                return value.trim();
            }
        }
        return null;
    }

    protected boolean findSameCardAndConnect(
            CardImported cardImported,
            String question,
            String answer,
            List<Integer> cardsConnected
    ) {
        Card card = deckDb.cardDao().findByQuestionLikeAndAnswerLikeAndCardNull(question, answer, cardsConnected);
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
            cardImported.setQuestion(question);
            cardImported.setAnswer(answer);
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
        int countCardImported = deckDb.cardDao().countByCardImportedNull();
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
                LongTasksExecutor.THREAD_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        List<CardImported> cardImportedList = deckDb.cardImportedDao().findByCardNull(0, perCore);
        int lastCardImportedId;
        while (!cardImportedList.isEmpty()) {
            threadPoolExecutor.submit(new MatchSimilarCardsRunnable(deckDb, lastModifiedAtImportedFile, cardImportedList));
            lastCardImportedId = cardImportedList.get(cardImportedList.size() - 1).getId();
            cardImportedList = deckDb.cardImportedDao().findByCardNull(lastCardImportedId, perCore);
        }
        threadPoolExecutor.shutdown();
        if (!threadPoolExecutor.awaitTermination(LongTasksExecutor.THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
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
        deckDb.cardImportedDao().clearMultipleAssignedCards();
        int lastCardImportedId;
        List<CardImported> cardImportedList = deckDb.cardImportedDao().findByCardNull(0, PAGE_LIMIT);
        while (!cardImportedList.isEmpty()) {
            lastCardImportedId = cardImportedList.get(cardImportedList.size() - 1).getId();
            (new MatchSimilarCardsRunnable(deckDb, lastModifiedAtImportedFile, cardImportedList)).run();
            cardImportedList = deckDb.cardImportedDao().findByCardNull(lastCardImportedId, PAGE_LIMIT);
        }
    }

    /**
     * 4.1. Find added/removed cards between deck and imported file.
     * <p>
     * 4.1.1. Create {@link CardImported} for missing cards in the imported file:
     * {@link CardImported#STATUS_DELETE_BY_FILE}
     * {@link CardImported#STATUS_INSERT_BY_DECK}
     * {@link #createImportedCard}
     * <p>
     * 4.1.1. Enrich {@link CardImported#nextCardDeckId} and {@link CardImported#previousCardDeckId}.
     * It will be required for {@link #checkIfPositionUnchanged}
     */
    protected void processDeckCard() {
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();
        List<CardImported> cardImportedListToInsert = new LinkedList<>();
        List<Card> cardList = deckDb.cardDao()
                .findByOrdinalGreaterThanOrderByOrdinal(0);

        // Prepare for the first iteration
        Card currentCard = cardList.remove(0);
        CardImported currentCardImported = deckDb.cardImportedDao().findByCardId(currentCard.getId());

        // Processing the first card
        if (currentCardImported == null) {
            currentCardImported = createImportedCard(currentCard);
        }

        while (!cardList.isEmpty()) {
            // Prepare for the next iteration
            Card nextCard = cardList.remove(0);
            CardImported nextCardImported = deckDb.cardImportedDao().findByCardId(nextCard.getId());

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
                deckDb.cardImportedDao().insertAll(cardImportedListToInsert);
                cardImportedListToInsert.clear();
                deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                cardImportedListToUpdate.clear();
            }

            // Prepare for the next iteration
            currentCard = nextCard;
            currentCardImported = nextCardImported;
            if (cardList.isEmpty()) {
                cardList = deckDb.cardDao()
                        .findByOrdinalGreaterThanOrderByOrdinal(currentCard.getOrdinal());
            }
        }

        // Final saving
        if (currentCardImported.getId() == null) {
            cardImportedListToInsert.add(currentCardImported);
        } else {
            cardImportedListToUpdate.add(currentCardImported);
        }
        if (!cardImportedListToInsert.isEmpty()) {
            deckDb.cardImportedDao().insertAll(cardImportedListToInsert);
        }
        if (!cardImportedListToUpdate.isEmpty()) {
            deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
    }

    /**
     * 4.1 Create {@link CardImported} for missing cards in the imported file:
     * {@link CardImported#STATUS_DELETE_BY_FILE}
     * {@link CardImported#STATUS_INSERT_BY_DECK}
     */
    protected CardImported createImportedCard(Card card) {
        CardImported cardImported = new CardImported();
        cardImported.setCardId(card.getId());
        if (isImportedFileNewer(card)) {
            cardImported.setContentStatus(CardImported.STATUS_DELETE_BY_FILE);
        } else {
            cardImported.setContentStatus(CardImported.STATUS_INSERT_BY_DECK);
            cardImported.setPositionStatus(CardImported.POSITION_STATUS_BY_DECK);
            //@todo remove after debugging
            cardImported.setQuestion(card.getQuestion());
            cardImported.setAnswer(card.getAnswer());
        }
        return cardImported;
    }

    /**
     * 4.2. Find new cards in the imported file.
     */
    protected void findNewCardsInImportedFile() {
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();
        List<CardImported> cardImportedList = deckDb.cardImportedDao()
                .findByCardNull(0, PAGE_LIMIT);
        while (!cardImportedList.isEmpty()) {
            // Prepare for the next iteration
            CardImported cardImported = cardImportedList.remove(0);

            // Processing
            cardImported.setContentStatus(CardImported.STATUS_INSERT_BY_FILE);
            cardImported.setPositionStatus(CardImported.POSITION_STATUS_BY_FILE);

            // Saving
            cardImportedListToUpdate.add(cardImported);
            if (cardImportedListToUpdate.size() % ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                cardImportedListToUpdate.clear();
            }

            // Prepare for the next iteration
            if (cardImportedList.isEmpty()) {
                cardImportedList = deckDb.cardImportedDao()
                        .findByCardNull(cardImported.getId(), PAGE_LIMIT);
            }
        }

        // Final saving
        if (!cardImportedListToUpdate.isEmpty()) {
            deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
    }

    /**
     * 4.3.1. Enrich {@link CardImported#nextCardId} and {@link CardImported#previousCardId}.
     * It is required for {@link #checkIfPositionUnchanged}
     * 4.3.2. Set {@link CardImported#POSITION_STATUS_UNCHANGED}
     * if the card is in the same place in the deck and file.
     */
    protected void processCardFromFile() {
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();
        List<CardImported> cardImportedList = deckDb.cardImportedDao()
                .findByStatusNotOrderByOrdinalAsc(new String[]{
                        CardImported.STATUS_DELETE_BY_FILE,
                        CardImported.STATUS_INSERT_BY_DECK
                }, 0);
        CardImported currentCardImported = cardImportedList.remove(0);

        if (!cardImportedList.isEmpty()) {
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
                    deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                    cardImportedListToUpdate.clear();
                }

                // Prepare for the next iteration
                currentCardImported = nextCardImported;
                if (cardImportedList.isEmpty()) {
                    cardImportedList = deckDb.cardImportedDao()
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
            deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
    }

    /**
     * 4.3.2. Set {@link CardImported#POSITION_STATUS_UNCHANGED}
     * if the card is in the same place in the deck and file.
     */
    protected void checkIfPositionUnchanged(CardImported cardImported) {
        boolean previousUnchanged = Objects.equals(cardImported.getPreviousCardDeckId(), cardImported.getPreviousCardId());
        boolean nextUnchanged = Objects.equals(cardImported.getNextCardDeckId(), cardImported.getNextCardId());
        if (previousUnchanged && nextUnchanged) {
            cardImported.setPositionStatus(CardImported.POSITION_STATUS_UNCHANGED);
        }
    }

    protected void updateDeckCards() {
        List<Card> cardsListToUpdate = new LinkedList<>();
        List<Card> cardsListToRemove = new LinkedList<>();
        List<CardImported> cardsImportedListToUpdate = new LinkedList<>();

        List<CardImported> cardImportedList = deckDb.cardImportedDao().findAll(0);
        while (!cardImportedList.isEmpty()) {
            CardImported cardImported = cardImportedList.remove(0);

            switch (cardImported.getContentStatus()) {
                case CardImported.STATUS_UNCHANGED:
                case CardImported.STATUS_INSERT_BY_DECK: {
                    Card card = deckDb.cardDao().findById(cardImported.getCardId());
                    card.setOrdinal(cardImported.getNewOrdinal());
                    cardsListToUpdate.add(card);
                }
                break;
                case CardImported.STATUS_INSERT_BY_FILE: {
                    Card card = new Card();

                    card.setQuestion(cardImported.getQuestion());
                    card.setAnswer(cardImported.getAnswer());
                    card.setModifiedAt(lastModifiedAtImportedFile);

                    card.setOrdinal(cardImported.getNewOrdinal());

                    cardImported.setCardId(Long.valueOf(deckDb.cardDao().insert(card)).intValue());//@todo not needed
                    cardsImportedListToUpdate.add(cardImported);
                }
                break;
                case CardImported.STATUS_UPDATE_BY_FILE: {
                    Card card = deckDb.cardDao().findById(cardImported.getCardId());
                    card.setQuestion(cardImported.getQuestion());
                    card.setAnswer(cardImported.getAnswer());
                    card.setModifiedAt(lastModifiedAtImportedFile);

                    card.setOrdinal(cardImported.getNewOrdinal());
                    cardsListToUpdate.add(card);
                }
                break;
                case CardImported.STATUS_DELETE_BY_FILE: {
                    Card card = deckDb.cardDao().findById(cardImported.getCardId());
                    cardsListToRemove.add(card);
                }
                break;
            }

            if ((cardsListToUpdate.size() + cardsListToRemove.size() + cardsImportedListToUpdate.size())
                    >= ENTITIES_TO_UPDATE_POOL_MAX) {
                deckDb.cardDao().updateAll(cardsListToUpdate);
                cardsListToUpdate.clear();
                deckDb.cardDao().deleteAll(cardsListToRemove);
                cardsListToRemove.clear();
                deckDb.cardImportedDao().updateAll(cardsImportedListToUpdate);
                cardsImportedListToUpdate.clear();
            }
            if (cardImportedList.isEmpty()) {
                cardImportedList = deckDb.cardImportedDao().findAll(cardImported.getId());
            }
        }

        if (!cardsListToUpdate.isEmpty()) {
            deckDb.cardDao().updateAll(cardsListToUpdate);
        }
        if (!cardsListToRemove.isEmpty()) {
            deckDb.cardDao().deleteAll(cardsListToRemove);
        }
        if (!cardsImportedListToUpdate.isEmpty()) {
            deckDb.cardImportedDao().updateAll(cardsImportedListToUpdate);
        }
    }

    public void updateExcelFile(OutputStream os) throws IOException {
        int skipHeaderRows = getSkipHeaderRows();
        List<CardImported> cardImportedList = deckDb.cardImportedDao()
                .findByStatusNotOrderByCardOrdinalAsc(new String[]{
                        CardImported.STATUS_DELETE_BY_DECK,
                        CardImported.STATUS_DELETE_BY_FILE
                }, 0);
        try {
            for (int rowNum = Math.max(skipHeaderRows, 0);
                 !cardImportedList.isEmpty();
                 rowNum++
            ) {
                CardImported cardImported = cardImportedList.remove(0);
                Card card = deckDb.cardDao().findById(cardImported.getCardId());

                if (CardImported.STATUS_UNCHANGED.equals(cardImported.getContentStatus())) {
                    if (cardImported.isOrderChanged()) {
                        updateExcelCell(sheet.createRow(rowNum), card);
                    }
                } else {
                    updateExcelCell(sheet.createRow(rowNum), card);
                }

                if (cardImportedList.isEmpty()) {
                    cardImportedList = deckDb.cardImportedDao()
                            .findByStatusNotOrderByCardOrdinalAsc(new String[]{
                                    CardImported.STATUS_DELETE_BY_DECK,
                                    CardImported.STATUS_DELETE_BY_FILE
                            }, card.getOrdinal());
                }

                //@todo add remove last records
            }
            this.isImportedFile.close();
            this.workbook.write(os);
            this.workbook.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateExcelCell(Row row, Card card) {
        Cell cell = row.createCell(getQuestionIndex());
        cell.setCellValue(card.getQuestion());
        cell = row.createCell(getAnswerIndex());
        cell.setCellValue(card.getAnswer());
    }

    protected boolean isImportedFileNewer(Card card) {
        return lastModifiedAtImportedFile.compareTo(card.getModifiedAt()) > 0;
    }

    //@todo public visibility for testing
    public SyncDeckDatabase getDeckDB(String deckName) {
        return SyncDatabaseUtil.getInstance(appContext).getDeckDatabase(deckName);
    }
}