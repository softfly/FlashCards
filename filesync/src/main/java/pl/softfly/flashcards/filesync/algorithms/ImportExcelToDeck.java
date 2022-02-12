package pl.softfly.flashcards.filesync.algorithms;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;

/**
 * Creates a new deck with cards from a file.
 *
 * @author Grzegorz Ziemski
 */
public class ImportExcelToDeck extends AbstractReadExcel {

    public static final int ENTITIES_TO_UPDATE_POOL_MAX = 100;

    private Context appContext;

    @Nullable
    private DeckDatabase deckDb;

    //For testing, mocking
    public ImportExcelToDeck() {}

    public ImportExcelToDeck(Context appContext) {
        this.appContext = appContext;
    }

    public void importExcelFile(
            String deckName,
            @NonNull InputStream inputStream,
            @NonNull String typeFile,
            Long lastModifiedAtFile
    ) throws IOException {
        deckName = findFreeDeckName(deckName);
        Workbook workbook = typeFile.equals(TYPE_XLS)
                ? new HSSFWorkbook(inputStream)
                : new XSSFWorkbook(inputStream);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        findColumnIndexes(datatypeSheet);
        importCards(
                datatypeSheet,
                deckName,
                getTermIndex(),
                getDefinitionIndex(),
                getSkipHeaderRows(),
                lastModifiedAtFile
        );
    }

    protected void importCards(
            @NonNull Sheet datatypeSheet,
            @NonNull String deckName,
            int termPosition,
            int definitionPosition,
            int skipFirstRows,
            Long lastModifiedAtFile
    ) {
        if (termPosition == -1 && definitionPosition == -1) return;

        Iterator<Row> rowIt = datatypeSheet.iterator();
        List<Card> cardsList = new LinkedList<>();
        int ordinal = SyncExcelToDeck.MULTIPLE_ORDINAL;
        LocalDateTime createdAt = Converters.fromTimestampToLocalDateTime(TimeUnit.MILLISECONDS.toSeconds(lastModifiedAtFile));
        for (int rowNum = 0; rowIt.hasNext(); rowNum++) {
            Row currentRow = rowIt.next();
            if (rowNum <= skipFirstRows) {
                continue;
            }
            Card card = new Card();
            card.setOrdinal(ordinal);
            ordinal+=SyncExcelToDeck.MULTIPLE_ORDINAL;
            card.setModifiedAt(createdAt);

            if (termPosition > -1) {
                Cell currentCell = currentRow.getCell(termPosition);
                String value = currentCell.getStringCellValue().trim();
                card.setTerm(value);
            }
            if (definitionPosition > -1) {
                Cell currentCell = currentRow.getCell(definitionPosition);
                String value = currentCell.getStringCellValue().trim();
                card.setDefinition(value);
            }

            if (nonEmpty(card.getTerm()) || nonEmpty(card.getDefinition())) {
                if (empty(card.getTerm())) {
                    card.setTerm(null);
                }
                if (empty(card.getDefinition())) {
                    card.setDefinition(null);
                }
                if (deckDb == null) deckDb = getDeckDatabase(deckName);
                cardsList.add(card);
                if (rowNum > 0 && (rowNum % ENTITIES_TO_UPDATE_POOL_MAX == 0)) {
                    insertAll(new ArrayList<>(cardsList));
                    cardsList.clear();
                }
            }
        }

        if (!cardsList.isEmpty()) {
            insertAll(cardsList);
            cardsList.clear();
        }
    }

    //@todo Public for mocking
    //@todo try DI instead of ServiceLocator
    public String findFreeDeckName(@NonNull String deckName) {
        return AppDatabaseUtil
                .getInstance(appContext)
                .getStorageDb()
                .findFreeDeckName(deckName.substring(0, deckName.lastIndexOf('.')));
    }

    //@todo Public for mocking
    public void insertAll(List<Card> cards) {
        deckDb.cardDao().insertAll(cards);
    }

    //@todo Public for mocking
    @Nullable
    public DeckDatabase getDeckDatabase(@NonNull String deckName) {
        return AppDatabaseUtil.getInstance(appContext).getDeckDatabase(deckName);
    }
}
