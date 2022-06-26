package pl.softfly.flashcards.filesync.test.algorithms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.Manifest;
import android.content.Context;
import android.os.Build;
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
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.filesync.algorithms.ExportExcelToDeck;
import pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck;
import pl.softfly.flashcards.filesync.db.FileSyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;
import pl.softfly.flashcards.entity.filesync.FileSynced;

/**
 * @author Grzegorz Ziemski
 */
public class SyncExcelToDeckStepDefs {

    public static final int DB_ENTITIES_POOL = 1000;

    private static final String TAG = "SyncExcelToDeckStepDefs";

    private static final int COLUMN_TEST_INDEX_TERM = 0;
    private static final int COLUMN_TEST_INDEX_DEFINITION = 1;
    private static final int COLUMN_TEST_INDEX_MODIFIED_AT = 2;

    private final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private long excelLastModifiedAt = 1;
    /* -----------------------------------------------------------------------------------------
     * Database Properties
     * ----------------------------------------------------------------------------------------- */
    private String deckName;
    private String deckDbPath;
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
    private int currentRowNum;

    /**
     * If true, before the next sync simulation the Time will go +1
     */
    private boolean fileUpdatedAfterLastSync;

    /* -----------------------------------------------------------------------------------------
     * Others
     * ----------------------------------------------------------------------------------------- */
    private DataTable expectedDeck;

    /**
     * Helps compare dates more easily while debugging instead of comparing long numbers.
     */
    private long simulationTime = 3;

    @Before
    public void before(@NonNull Scenario scenario) {
        deckName = scenario.getName().replaceAll("(\\.)*$", "");
        initDB();
        initTestDir();
        initExcelFile();
    }

    protected void initDB() {
        GrantPermissionRule.grant(Manifest.permission.MANAGE_EXTERNAL_STORAGE);

        deckDbPath = FileSyncDatabaseUtil
                .getInstance(appContext)
                .getStorageDb().getDbFolder() + "/Tests/" + deckName;

        FileSyncDatabaseUtil
                .getInstance(appContext)
                .getStorageDb()
                .removeDatabase(deckDbPath);

        deckDb = FileSyncDatabaseUtil
                .getInstance(appContext)
                .createDatabase(deckDbPath);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void initTestDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
        } else {
            testDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + appContext.getResources().getString(R.string.app_name) + "/tests/";
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

    @Given("Clear file.")
    public void clear_file() {
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
            card.setTerm(rowIn.get(COLUMN_TEST_INDEX_TERM));
            card.setDefinition(rowIn.get(COLUMN_TEST_INDEX_DEFINITION));
            card.setModifiedAt(Long.parseLong(rowIn.get(COLUMN_TEST_INDEX_MODIFIED_AT)));
            if (card.getModifiedAt() < 0) {
                card.setDeletedAt(TimeUtil.getNowEpochSec());
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
                card.setTerm(
                        rowIn.get(COLUMN_TEST_INDEX_TERM)
                                .replace("{i}", Integer.toString(i))
                );
                card.setDefinition(
                        rowIn.get(COLUMN_TEST_INDEX_DEFINITION)
                                .replace("{i}", Integer.toString(i))
                );
                card.setModifiedAt(Long.parseLong(rowIn.get(COLUMN_TEST_INDEX_MODIFIED_AT)));
                if (card.getModifiedAt() < 0) {
                    card.setDeletedAt(TimeUtil.getNowEpochSec());
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
        fileUpdatedAfterLastSync = true;
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
        fileUpdatedAfterLastSync = true;
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

    @Given("Update the cards in the deck:")
    public void update_the_cards_in_the_deck(DataTable dataTable) {
        long currentTime = TimeUtil.getNowEpochSec();
        List<Card> cards = deckDb.cardDao().getCards();
        for (List<String> row : dataTable.asLists()) {
            Card card = cards.remove(0);
            String term = row.get(COLUMN_TEST_INDEX_TERM);
            String definition = row.get(COLUMN_TEST_INDEX_DEFINITION);
            if (!term.equals(card.getTerm())) {
                card.setTerm(term);
                card.setModifiedAt(currentTime);
            }
            if (!definition.equals(card.getDefinition())) {
                card.setDefinition(definition);
                card.setModifiedAt(currentTime);
            }
            deckDb.cardDao().updateAll(card);
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
        } else if (fileUpdatedAfterLastSync) {
            //excelLastModifiedAt = fileSynced.getLastSyncAt() + 1;
            excelLastModifiedAt = ++simulationTime;
            ++simulationTime;
        }
        fileUpdatedAfterLastSync = false;

        SyncExcelToDeck syncExcelToDeck = new MockSyncExcelToDeck(
                appContext, new
                BenchmarkDetermineNewOrderCards(),
                simulationTime
        );
        syncExcelToDeck.syncExcelFile(
                deckDbPath,
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
                deckDbPath,
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
            int ordinal = 1;
            for (List<String> row : expectedDeck.asLists()) {
                Card card = it.next();
                org.junit.Assert.assertEquals(ordinal++, card.getOrdinal().intValue());
                assertThat(
                        new String[]{row.get(0), row.get(1)},
                        is(new String[]{card.getTerm(), card.getDefinition()})
                );
            }
        } catch (@NonNull AssertionError | Exception e) {
            List<Card> cards = deckDb.cardDao().getCardsOrderByOrdinalAsc();
            throw new AssertionError(printCards(cards), e);
        }
    }

    @NonNull
    protected String printCards(@NonNull List<Card> cards) {
        int maxLengthTerm = cards.stream()
                .flatMapToInt(card -> IntStream.of(card.getTerm().length()))
                .max()
                .getAsInt();
        int maxLengthDefinition = cards.stream()
                .flatMapToInt(card -> IntStream.of(card.getDefinition().length()))
                .max()
                .getAsInt();

        StringBuilder sb = new StringBuilder("\nCards in the deck:");
        deckDb.cardDao().getCardsOrderByOrdinalAsc()
                .forEach(card -> sb.append("\n")
                        .append("| ")
                        .append(String.format("%-" + maxLengthTerm + "." + maxLengthTerm + "s", card.getTerm()))
                        .append(" | ")
                        .append(String.format("%-" + maxLengthDefinition + "." + maxLengthDefinition + "s", card.getDefinition()))
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
                                excelRow.getCell(COLUMN_TEST_INDEX_TERM).getStringCellValue(),
                                excelRow.getCell(COLUMN_TEST_INDEX_DEFINITION).getStringCellValue()
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
        int maxLengthTerm = 0;
        int maxLengthDefinition = 0;
        for (Row row : datatypeSheet) {
            maxLengthTerm = Math.max(
                    maxLengthTerm,
                    row.getCell(COLUMN_TEST_INDEX_TERM).getStringCellValue().length()
            );
            maxLengthDefinition = Math.max(
                    maxLengthDefinition,
                    row.getCell(COLUMN_TEST_INDEX_DEFINITION).getStringCellValue().length()
            );
        }

        StringBuilder sb = new StringBuilder("\nCards in the file:");
        for (Row row : datatypeSheet) {
            sb.append("\n")
                    .append("| ")
                    .append(String.format(
                            "%-" + maxLengthTerm + "." + maxLengthTerm + "s",
                            row.getCell(COLUMN_TEST_INDEX_TERM).getStringCellValue()
                    ))
                    .append(" | ")
                    .append(String.format(
                            "%-" + maxLengthDefinition + "." + maxLengthDefinition + "s",
                            row.getCell(COLUMN_TEST_INDEX_DEFINITION).getStringCellValue()
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
