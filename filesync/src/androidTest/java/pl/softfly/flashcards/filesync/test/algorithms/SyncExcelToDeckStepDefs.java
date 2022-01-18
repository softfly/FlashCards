package pl.softfly.flashcards.filesync.test.algorithms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import io.cucumber.core.api.Scenario;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.algorithms.ExportExcelToDeck;
import pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck;
import pl.softfly.flashcards.filesync.db.FileSyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.FileSynced;

/**
 * @author Grzegorz Ziemski
 */
public class SyncExcelToDeckStepDefs {

    public static final int DB_ENTITIES_POOL = 1000;

    private static final String TAG = "SyncExcelToDeckStepDefs";

    private static final int COLUMN_TEST_INDEX_QUESTION = 0;
    private static final int COLUMN_TEST_INDEX_ANSWER = 1;
    private static final int COLUMN_TEST_INDEX_MODIFIED_AT = 2;

    private final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    /* -----------------------------------------------------------------------------------------
     * Database Properties
     * ----------------------------------------------------------------------------------------- */
    private String deckName;

    @Nullable
    private FileSyncDeckDatabase deckDb;

    private int currentCardId = 1;

    /* -----------------------------------------------------------------------------------------
     * Excel Properties
     * ----------------------------------------------------------------------------------------- */
    private String testDirPath;
    private String excelFilePath;
    private Workbook excelWorkbook;
    private Sheet excelSheet;
    private CreationHelper creationHelper;
    private final long excelLastModifiedAt = 1*60*60;
    private int currentRowNum;

    /* -----------------------------------------------------------------------------------------
     * Others
     * ----------------------------------------------------------------------------------------- */
    private DataTable expectedDeck;

    @Before
    public void before(@NonNull Scenario scenario) {
        deckName = scenario.getName().replaceAll("(\\.)*$", "");
        initDB();
        initTestDir();
        initExcelFile();
    }

    protected void initDB() {
        GrantPermissionRule.grant(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        FileSyncDatabaseUtil
                .getInstance(appContext)
                .getStorageDb()
                .removeDatabase(deckName);

        deckDb = FileSyncDatabaseUtil
                .getInstance(appContext)
                .getDeckDatabase(deckName);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void initTestDir() {
        if (Environment.isExternalStorageManager()) {
            testDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + appContext.getResources().getString(R.string.app_name) + "/tests/";
        } else {
            // @todo use getContext
            testDirPath = InstrumentationRegistry
                    .getInstrumentation()
                    .getTargetContext()
                    .getFilesDir()
                    .getPath() + "/tests/";
        }
        new File(testDirPath).mkdirs();
        Log.i(TAG, "testDirPath=" + testDirPath);
    }

    protected void initExcelFile() {
        excelFilePath = testDirPath + deckName + ".xlsx";
        excelWorkbook = new XSSFWorkbook();
        creationHelper = excelWorkbook.getCreationHelper();
        excelSheet = excelWorkbook.createSheet(WorkbookUtil.createSafeSheetName(deckName));
        ZipSecureFile.setMinInflateRatio(0.001);
    }

    @Given("Create a new file.")
    public void create_a_new_file() {
        excelFilePath = testDirPath + deckName + ".xlsx";
        (new File(excelFilePath)).delete();
        excelWorkbook = new XSSFWorkbook();
        creationHelper = excelWorkbook.getCreationHelper();
        excelSheet = excelWorkbook.createSheet(WorkbookUtil.createSafeSheetName(deckName));
        currentRowNum = 0;
    }

    @Given("Create a new {word} file.")
    public void create_a_new_file(String fileName) {
        excelFilePath = testDirPath + fileName + ".xlsx";
        (new File(excelFilePath)).delete();
        excelWorkbook = new XSSFWorkbook();
        creationHelper = excelWorkbook.getCreationHelper();
        excelSheet = excelWorkbook.createSheet(WorkbookUtil.createSafeSheetName(deckName));
        currentRowNum = 0;
    }

    @Given(value = "Add the following cards into the deck:")
    public void add_the_following_cards_into_the_deck(@NonNull DataTable dataTable) {
        List<Card> cardsToInsert = new LinkedList<>();
        for (List<String> rowIn : dataTable.asLists()) {
            Card card = new Card();
            card.setId(currentCardId);
            card.setOrdinal(currentCardId);
            card.setQuestion(rowIn.get(COLUMN_TEST_INDEX_QUESTION));
            card.setAnswer(rowIn.get(COLUMN_TEST_INDEX_ANSWER));
            long modifiedAt = Long.parseLong(rowIn.get(COLUMN_TEST_INDEX_MODIFIED_AT));
            card.setModifiedAt(Converters.fromTimestampToLocalDateTime(modifiedAt * 60 * 60));
            if (modifiedAt < 0) {
                card.setDeletedAt(LocalDateTime.now());
            }
            cardsToInsert.add(card);
            currentCardId++;
        }
        deckDb.cardDao().insertAll(cardsToInsert);
    }

    @Given(value = "Generate cards {int} times into the deck:", timeout = 2 * 1000)
    public void generate_cards_times_into_the_deck(Integer cardsNum, @NonNull DataTable dataTable) {
        List<Card> cardsToInsert = new LinkedList<>();
        for (int i = 1; i <= cardsNum; i++) {
            for (List<String> rowIn : dataTable.asLists()) {
                Card card = new Card();
                card.setId(currentCardId);
                card.setOrdinal(currentCardId);
                card.setQuestion(
                        rowIn.get(COLUMN_TEST_INDEX_QUESTION)
                                .replace("{i}", Integer.toString(i))
                );
                card.setAnswer(
                        rowIn.get(COLUMN_TEST_INDEX_ANSWER)
                                .replace("{i}", Integer.toString(i))
                );
                long modifiedAt = Long.parseLong(rowIn.get(COLUMN_TEST_INDEX_MODIFIED_AT));
                card.setModifiedAt(Converters.fromTimestampToLocalDateTime(modifiedAt * 60 * 60));
                if (modifiedAt < 0) {
                    card.setDeletedAt(LocalDateTime.now());
                }
                cardsToInsert.add(card);
                if (i % DB_ENTITIES_POOL == 0) {
                    deckDb.cardDao().insertAll(cardsToInsert);
                    cardsToInsert.clear();
                    Log.i(TAG, i + " cards have been added to the deck.");
                }
                currentCardId++;
            }
        }
        if (!cardsToInsert.isEmpty()) {
            deckDb.cardDao().insertAll(cardsToInsert);
        }
    }

    @Given(value = "Add the following cards into the file:", timeout = 2 * 1000)
    public void add_the_following_cards_into_the_file(@NonNull DataTable dataTable) {
        for (List<String> rowIn : dataTable.asLists()) {
            Row rowOut = excelSheet.createRow(currentRowNum);
            int colNum = 0;
            for (String colIn : rowIn) {
                Cell cell = rowOut.createCell(colNum);
                cell.setCellValue(creationHelper.createRichTextString(colIn));
                colNum++;
            }
            currentRowNum++;
        }
    }

    @Given(value = "Generate cards {int} times into the file:", timeout = 5 * 60 * 1000)
    public void generate_cards_times_into_the_file(
            Integer cardsNum, @NonNull DataTable dataTable) {
        for (int i = 1; i <= cardsNum; i++) {
            for (List<String> rowIn : dataTable.asLists()) {
                Row rowOut = excelSheet.createRow(currentRowNum);
                int colNum = 0;
                for (String colIn : rowIn) {
                    Cell cell = rowOut.createCell(colNum);
                    cell.setCellValue(creationHelper.createRichTextString(colIn.replace("{i}", Integer.toString(i))));
                    colNum++;
                }
                currentRowNum++;
            }
        }
    }

    @When(value = "Synchronize the Excel file with the deck.", timeout = 5 * 60 * 1000)
    public void synchronize_the_Excel_file_with_the_deck() throws Exception {
        OutputStream fileOut = new FileOutputStream(excelFilePath);
        excelWorkbook.write(fileOut);
        fileOut.close();

        FileSynced fileSynced = deckDb.fileSyncedDao().findByUri(excelFilePath);
        if (fileSynced == null) {
            fileSynced = new FileSynced();
            fileSynced.setUri(excelFilePath);
        }

        SyncExcelToDeck syncExcelToDeck = new BenchmarkSyncExcelToDeck(
                appContext, new
                BenchmarkDetermineNewOrderCards()
        );
        syncExcelToDeck.syncExcelFile(
                deckName,
                fileSynced,
                new FileInputStream(excelFilePath),
                TYPE_XLSX,
                excelLastModifiedAt
        );
        syncExcelToDeck.commitChanges(fileSynced, new FileOutputStream(excelFilePath));
    }

    @When("Export an Excel file from the deck.")
    public void export_the_Excel_file_from_the_deck() throws Exception {
        OutputStream fileOut = new FileOutputStream(excelFilePath);
        excelWorkbook.write(fileOut);
        fileOut.close();

        FileSynced fileSynced = deckDb.fileSyncedDao().findByUri(excelFilePath);
        if (fileSynced == null) {
            fileSynced = new FileSynced();
            fileSynced.setUri(excelFilePath);
        }

        SyncExcelToDeck syncExcelToDeck = new ExportExcelToDeck(
                appContext,
                new BenchmarkDetermineNewOrderCards()
        );
        syncExcelToDeck.syncExcelFile(
                deckName,
                fileSynced,
                new FileInputStream(excelFilePath),
                TYPE_XLSX,
                excelLastModifiedAt
        );
        syncExcelToDeck.commitChanges(fileSynced, new FileOutputStream(excelFilePath));
    }

    @Then(value = "The expected deck with cards:", timeout = 1000)
    public void the_expected_deck_with_cards(DataTable dataTable) {
        this.expectedDeck = dataTable;
    }

    @Then(value = "Check the deck with cards.", timeout = 1000)
    public void check_the_deck_with_cards() {
        try {
            List<Card> cardList = deckDb.cardDao().getCardsOrderByOrdinalAsc();
            Iterator<Card> it = cardList.iterator();
            expectedDeck.asLists().forEach(row -> {
                Card card = it.next();
                assertThat(
                        new String[]{row.get(0), row.get(1)},
                        is(new String[]{card.getQuestion(), card.getAnswer()})
                );
            });
        } catch (@NonNull AssertionError | Exception e) {
            List<Card> cards = deckDb.cardDao().getCardsOrderByOrdinalAsc();
            throw new AssertionError(printCards(cards), e);
        }
    }

    @NonNull
    protected String printCards(@NonNull List<Card> cards) {
        int maxLengthQuestion = cards.stream()
                .flatMapToInt(card -> IntStream.of(card.getQuestion().length()))
                .max()
                .getAsInt();
        int maxLengthAnswer = cards.stream()
                .flatMapToInt(card -> IntStream.of(card.getAnswer().length()))
                .max()
                .getAsInt();

        StringBuilder sb = new StringBuilder("\nCards in the deck:");
        deckDb.cardDao().getCardsOrderByOrdinalAsc()
                .forEach(card -> sb.append("\n")
                        .append("| ")
                        .append(String.format("%-" + maxLengthQuestion + "." + maxLengthQuestion + "s", card.getQuestion()))
                        .append(" | ")
                        .append(String.format("%-" + maxLengthAnswer + "." + maxLengthAnswer + "s", card.getAnswer()))
                        .append(" |")
                );
        return sb.toString();
    }

    @Then(value = "Check the Excel file.", timeout = 5 * 1000)
    public void check_the_excel_file() throws IOException {
        InputStream is = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(is);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Iterator<Row> it = datatypeSheet.iterator();
        try {
            expectedDeck.asLists().forEach(row -> {
                Row excelRow = it.next();
                assertThat(
                        new String[]{row.get(0), row.get(1)},
                        is(new String[]{
                                excelRow.getCell(COLUMN_TEST_INDEX_QUESTION).getStringCellValue(),
                                excelRow.getCell(COLUMN_TEST_INDEX_ANSWER).getStringCellValue()
                        })
                );
            });
        } catch (AssertionError e) {
            throw new AssertionError(printCards(datatypeSheet), e);
        }
    }

    @Then("{int} cards in the deck.")
    public void cards_in_the_deck(@NonNull Integer cardsNum) {
        Assert.assertEquals(cardsNum.longValue(), deckDb.cardDao().count());
    }

    @Then("{int} cards in the imported file.")
    public void cards_in_the_imported_file(@NonNull Integer cardsNum) throws IOException {
        InputStream is = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(is);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Assert.assertEquals(cardsNum.longValue(), Integer.valueOf(datatypeSheet.getLastRowNum() + 1).longValue());
    }

    @NonNull
    protected String printCards(@NonNull Sheet datatypeSheet) {
        int maxLengthQuestion = 0;
        int maxLengthAnswer = 0;
        for (Row row : datatypeSheet) {
            maxLengthQuestion = Math.max(
                    maxLengthQuestion,
                    row.getCell(COLUMN_TEST_INDEX_QUESTION).getStringCellValue().length()
            );
            maxLengthAnswer = Math.max(
                    maxLengthAnswer,
                    row.getCell(COLUMN_TEST_INDEX_ANSWER).getStringCellValue().length()
            );
        }

        StringBuilder sb = new StringBuilder("\nCards in the file:");
        for (Row row : datatypeSheet) {
            sb.append("\n")
                    .append("| ")
                    .append(String.format(
                            "%-" + maxLengthQuestion + "." + maxLengthQuestion + "s",
                            row.getCell(COLUMN_TEST_INDEX_QUESTION).getStringCellValue()
                    ))
                    .append(" | ")
                    .append(String.format(
                            "%-" + maxLengthAnswer + "." + maxLengthAnswer + "s",
                            row.getCell(COLUMN_TEST_INDEX_ANSWER).getStringCellValue()
                    ))
                    .append(" |");
        }

        return sb.toString();
    }

    @Then("Updated {long} rows in the file.")
    public void updated_rows_in_the_file(@NonNull Long expected) {
        Assert.assertEquals(
                expected.longValue(),
                Integer.valueOf(deckDb.cardImportedDao().countByOrderChangedTrue()).longValue()
        );
    }
}
