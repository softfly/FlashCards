package pl.softfly.flashcards.filesync.algorithms;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;

import android.content.Context;

import androidx.annotation.NonNull;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ExportExcelToDeck extends SyncExcelToDeck {

    //Only for tests
    public ExportExcelToDeck(Context appContext, DetermineNewOrderCards determineNewOrderCards) {
        super(appContext, determineNewOrderCards);
    }

    public ExportExcelToDeck(@NonNull FileSyncListCardsActivity listCardsActivity) {
        super(listCardsActivity);
    }

    public void syncExcelFile(
            @NonNull String deckName,
            @NonNull FileSynced fileSynced,
            @NonNull InputStream inputStream,
            @NonNull String typeFile
    ) {
        syncExcelFile(deckName, fileSynced, inputStream, typeFile, null);
    }

    @Override
    public void syncExcelFile(
            @NonNull String deckName,
            @NonNull FileSynced fileSynced,
            @NonNull InputStream inputStream,
            @NonNull String typeFile,
            Long lastModifiedAtFile
    ) {
        this.isImportedFile = inputStream;
        this.deckDb = getDeckDB(deckName);
        this.workbook = typeFile.equals(TYPE_XLS)
                ? new HSSFWorkbook()
                : new XSSFWorkbook();
        this.sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(deckName));
        // The file must be very old to get changes only from deck.
        this.newLastSyncAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

        setQuestionIndex(0);
        setAnswerIndex(1);
        setSkipHeaderRows(1);

        fileSynced.setLastSyncAt(this.newLastSyncAt);
        fileSynced.setId(Long.valueOf(deckDb.fileSyncedDao().insert(fileSynced)).intValue());
        this.fileSynced = fileSynced;

        // 1. Purge sync entities in the database before starting.
        deckDb.cardEdgeDao().forceDeleteAll();
        deckDb.cardImportedDao().deleteAll();

        lockDeckEditing();

        // 4. Find added/removed cards between deck and imported file.
        // 4.2. Find added/removed cards between deck and imported file.
        processDeckCard();
        // 4.4. Enrich {@link CardImported#nextCardId} and {@link CardImported#previousCardId}.
        processCardFromFile();

        // 5. Determine the new order of the cards after merging.
        determineNewOrderCards.determineNewOrderCards(deckDb, fileSynced.getLastSyncAt());
    }

    @Override
    public void commitChanges(
            @NonNull FileSynced fileSynced,
            @NonNull OutputStream os
    ) throws IOException {
        createHeader();
        super.commitChanges(fileSynced, os);
    }

    protected void createHeader() {
        sheet.setColumnWidth(0,10000);
        sheet.setColumnWidth(1,10000);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFont(font);

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(getQuestionIndex());
        cell.setCellValue("Question");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(getAnswerIndex());
        cell.setCellValue("Answer");
        cell.setCellStyle(cellStyle);
    }

    @Override
    protected void updateExcelCell(@NonNull Row row, @NonNull Card card) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        Cell cell = row.createCell(getQuestionIndex());
        cell.setCellValue(card.getQuestion());
        cell.setCellStyle(cellStyle);

        cell = row.createCell(getAnswerIndex());
        cell.setCellValue(card.getAnswer());
        cell.setCellStyle(cellStyle);
    }
}