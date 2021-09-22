package pl.softfly.flashcards.ui.deck;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.tasks.Task;

/**
 * https://developer.android.com/training/data-storage/shared/documents-files
 */
public class ImportExcelToDeckTask implements Callable<Object>, Task<Object> {

    protected static final int CARDS_TO_SAVE_SIZE = 100;

    public static final String TYPE_XLS = "application/vnd.ms-excel";

    public static final String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ListDecksActivity listDecksActivity;

    private final Uri uri;

    private DeckDatabase deckDb;

    private int questionIndex = -1;

    private int answerIndex = -1;

    private int skipHeaderRows = -1;

    private String fileName;

    public ImportExcelToDeckTask(ListDecksActivity listDecksActivity, Uri uri) {
        this.listDecksActivity = listDecksActivity;
        this.uri = uri;
    }

    @Override
    public Object call() {
        askPermissions(uri);
        String fileName = null;
        String type = null;
        try (Cursor cursor = listDecksActivity.getContentResolver().query(uri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                type = cursor.getString(cursor.getColumnIndex("mime_type")).toLowerCase();
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        String deckName = findFreeDeckName(fileName.substring(0, fileName.lastIndexOf('.')));
        try {
            InputStream is = listDecksActivity.getContentResolver().openInputStream(uri);
            importExcelFile(type.equals(TYPE_XLS) ? new HSSFWorkbook(is) : new XSSFWorkbook(is), deckName);
            this.listDecksActivity.runOnUiThread(() -> {
                Toast.makeText(listDecksActivity, String.format("The new deck %s has been imported from an Excel file.", deckName), Toast.LENGTH_SHORT).show();
                listDecksActivity.loadDecks();
            });
        } catch (IOException e) {
            e.printStackTrace();
            this.listDecksActivity.runOnUiThread(() -> {
                Toast.makeText(listDecksActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            });
        }
        return true;
    }

    protected void askPermissions(Uri uri) {
        Intent intent = listDecksActivity.getIntent();
        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        listDecksActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    protected String findFreeDeckName(String fileName) {
        String deckName = fileName;
        for (int i = 1; i <= 100; i++) {
            if (!DeckDatabase.exists(deckName)) {
                return deckName;
            }
            deckName = fileName + "-" + i;
        }
        return null;
    }

    public void importExcelFile(Workbook workbook, String deckName) {
        Sheet datatypeSheet = workbook.getSheetAt(0);
        findColumnIndexes(datatypeSheet);
        importCards(datatypeSheet, deckName, questionIndex, answerIndex, skipHeaderRows);
    }

    /**
     * Determines the column indexes for questions and answers.
     * Possible scenarios:
     * <p>
     * 1. The spreadsheet does not include a header.
     * IE_01 2 columns - Questions and Answers
     * IE_02 1 column - Questions
     * <p>
     * 2. The spreadsheet includes the header with:
     * IE_03 2 columns - Question and Answer
     * IE_04 1 column - Question
     * IE_05 1 column - Answer
     * <p>
     * 3. The spreadsheet has 2 columns, but only the header for:
     * IE_06 Question
     * IE_07 Answer
     */
    protected void findColumnIndexes(Sheet datatypeSheet) {
        Iterator<Row> rowIt = datatypeSheet.iterator();
        int questionIndex = -1;
        // IE_03 IE_04 IE_05 Check the header only on the first no empty row.
        boolean lookForHeader = true;
        // IE_04
        int onlyQuestionRowIndex = -1;
        int onlyQuestionColIndex = -1;
        // IE_05
        int onlyAnswerRowIndex = -1;
        int onlyAnswerColIndex = -1;
        // IE_06 Question header from the previous row.
        int pQuestionHeaderIndex = -1;
        // IE_07 Answer header from the previous row.
        int pAnswerHeaderIndex = -1;

        for (int rowNum = 0; rowIt.hasNext(); rowNum++) {
            Row currentRow = rowIt.next();
            Iterator<Cell> cellIt = currentRow.iterator();
            int cQuestionHeaderIndex = -1;
            int cAnswerHeaderIndex = -1;
            int cQuestionIndex = -1;
            if (lookForHeader) {
                lookForHeader = questionIndex == -1;
            }

            for (int colNum = 0; cellIt.hasNext(); colNum++) {
                Cell currentCell = cellIt.next();
                String value = currentCell.getStringCellValue().trim();
                if (nonEmpty(value)) {
                    if (lookForHeader && value.toLowerCase().startsWith("question")) {
                        cQuestionHeaderIndex = colNum;
                        onlyQuestionRowIndex = rowNum;
                        onlyQuestionColIndex = colNum;
                        if (cQuestionHeaderIndex != -1 && cAnswerHeaderIndex != -1) {
                            // IE_01 2 columns - Questions and Answers
                            this.questionIndex = cQuestionHeaderIndex;
                            this.answerIndex = cAnswerHeaderIndex;
                            skipHeaderRows = rowNum;
                            return;
                        }
                    } else if (lookForHeader && value.toLowerCase().startsWith("answer")) {
                        cAnswerHeaderIndex = colNum;
                        onlyAnswerRowIndex = rowNum;
                        onlyAnswerColIndex = colNum;
                        if (cQuestionHeaderIndex != -1 && cAnswerHeaderIndex != -1) {
                            // IE_01 2 columns - Questions and Answers
                            this.questionIndex = cQuestionHeaderIndex;
                            this.answerIndex = cAnswerHeaderIndex;
                            skipHeaderRows = rowNum;
                            return;
                        }
                    } else if (pQuestionHeaderIndex != -1 && pQuestionHeaderIndex != colNum) {
                        // IE_06 The spreadsheet has 2 columns, but only the Question in the header.
                        this.questionIndex = pQuestionHeaderIndex;
                        this.answerIndex = colNum;
                        skipHeaderRows = rowNum - 1;
                        return;
                    } else if (pAnswerHeaderIndex != -1 && pAnswerHeaderIndex != colNum) {
                        // IE_07 The spreadsheet has 2 columns, but only the Answer in the header.
                        this.questionIndex = colNum;
                        this.answerIndex = pAnswerHeaderIndex;
                        skipHeaderRows = rowNum - 1;
                        return;
                    } else if (cQuestionIndex == -1) {
                        // IE_01 IE_02 IE_04 IE_05 1 column
                        // The first column is a question if a header is missing.
                        cQuestionIndex = colNum;
                        questionIndex = colNum;
                    } else {
                        // IE_01 IE_02 The spreadsheet does not include a header.
                        // The second column is an answer if a header is missing.
                        this.questionIndex = cQuestionIndex;
                        this.answerIndex = colNum;
                        return;
                    }
                }
            }
            pQuestionHeaderIndex = cQuestionHeaderIndex;
            pAnswerHeaderIndex = cAnswerHeaderIndex;
        }

        if (onlyQuestionColIndex != -1) {
            // IE_04 1 column - Question
            this.questionIndex = questionIndex;
            skipHeaderRows = onlyQuestionRowIndex;
        } else if (onlyAnswerColIndex != -1) {
            // IE_05 Only 1 column - Answer
            this.questionIndex = -1;
            this.answerIndex = onlyAnswerColIndex;
            skipHeaderRows = onlyAnswerRowIndex;
        } else {
            // IE_02 1 column - Questions
            this.questionIndex = questionIndex;
        }
    }

    protected void importCards(Sheet datatypeSheet, String deckName, int questionPosition, int answerPosition, int skipFirstRows) {
        if (questionPosition == -1 && answerPosition == -1) return;

        Iterator<Row> rowIt = datatypeSheet.iterator();
        List<Card> cardsList = new LinkedList<>();
        for (int rowNum = 0; rowIt.hasNext(); rowNum++) {
            Row currentRow = rowIt.next();
            if (rowNum <= skipFirstRows) {
                continue;
            }
            Card card = new Card();

            if (questionPosition > -1) {
                Cell currentCell = currentRow.getCell(questionPosition);
                String value = currentCell.getStringCellValue().trim();
                card.setQuestion(value);
            }
            if (answerPosition > -1) {
                Cell currentCell = currentRow.getCell(answerPosition);
                String value = currentCell.getStringCellValue().trim();
                card.setAnswer(value);
            }

            if (nonEmpty(card.getQuestion()) || nonEmpty(card.getAnswer())) {
                if (empty(card.getQuestion())) {
                    card.setQuestion(null);
                }
                if (empty(card.getAnswer())) {
                    card.setAnswer(null);
                }
                if (deckDb == null) deckDb = createDeck(deckName);
                cardsList.add(card);
                if (rowNum > 0 && (rowNum % CARDS_TO_SAVE_SIZE == 0)) {
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

    protected boolean nonEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    protected boolean empty(String str) {
        return str == null || str.isEmpty();
    }

    protected DeckDatabase createDeck(String deckName) {
        return AppDatabaseUtil.getInstance().getDeckDatabase(listDecksActivity.getBaseContext(), deckName);
    }

    protected void insertAll(List<Card> cards) {
        deckDb.cardDao().insertAll(cards)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void timeout() {
        listDecksActivity.runOnUiThread(() -> {
            Toast.makeText(listDecksActivity, MessageFormat.format("Timeout. The {0} file could not be imported.", fileName), Toast.LENGTH_SHORT).show();
        });
    }

    public void error() {
        listDecksActivity.runOnUiThread(() -> {
            Toast.makeText(listDecksActivity, MessageFormat.format("Error. The {0} file could not be imported.", fileName), Toast.LENGTH_SHORT).show();
        });
    }
}
