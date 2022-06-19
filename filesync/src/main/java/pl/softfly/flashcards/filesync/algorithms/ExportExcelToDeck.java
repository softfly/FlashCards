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

import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.entity.filesync.FileSynced;

/**
 * @author Grzegorz Ziemski
 */
public class ExportExcelToDeck extends SyncExcelToDeck {

    //Only for tests
    public ExportExcelToDeck(Context appContext, DetermineNewOrderCards determineNewOrderCards) {
        super(appContext, determineNewOrderCards);
    }

    public ExportExcelToDeck(@NonNull Context appContext) {
        super(appContext);
    }

    public void syncExcelFile(
            @NonNull String deckDbPath,
            @NonNull FileSynced fileSynced,
            @NonNull InputStream inputStream,
            @NonNull String typeFile
    ) {
        syncExcelFile(deckDbPath, fileSynced, inputStream, typeFile, 0);
    }

    @Override
    public void syncExcelFile(
            @NonNull String deckDbPath,
            @NonNull FileSynced fileSynced,
            @NonNull InputStream inputStream,
            @NonNull String typeFile,
            long lastModifiedAtFile
    ) {
        this.isImportedFile = inputStream;
        this.fsDeckDb = getFsDeckDb(deckDbPath);
        this.workbook = TYPE_XLS.equals(typeFile)
                ? new HSSFWorkbook()
                : new XSSFWorkbook();
        this.sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(getDeckName(deckDbPath)));
        // The file must be very old to get changes only from deck.
        this.newLastSyncAt = 0;

        setTermIndex(0);
        setDefinitionIndex(1);
        setSkipHeaderRows(0);

        fileSynced.setLastSyncAt(this.newLastSyncAt);
        if (fileSynced.getId() == null) {
            fileSynced.setId(Long.valueOf(fsDeckDb.fileSyncedDao().insert(fileSynced)).intValue());
        }
        this.fileSynced = fileSynced;

        // 1. Purge sync entities in the database before starting.
        fsDeckDb.cardEdgeDao().forceDeleteAll();
        fsDeckDb.cardImportedDao().deleteAll();

        // 4. Find added/removed cards between deck and imported file.
        // 4.2. Find added/removed cards between deck and imported file.
        processDeckCard();
        // 4.4. Enrich {@link CardImported#nextCardId} and {@link CardImported#previousCardId}.
        processCardFromFile();

        // 5. Determine the new order of the cards after merging.
        determineNewOrderCards.determineNewOrderCards(fsDeckDb, fileSynced.getLastSyncAt());
    }

    @Override
    public void commitChanges(
            @NonNull FileSynced fileSynced,
            @NonNull OutputStream os
    ) throws IOException {
        createHeader();
        newLastSyncAt = TimeUtil.getNowEpochSec();
        super.commitChanges(fileSynced, os);
    }

    protected void createHeader() {
        sheet.setColumnWidth(0, 10000);
        sheet.setColumnWidth(1, 10000);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFont(font);

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(getTermIndex());
        cell.setCellValue("Term");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(getDefinitionIndex());
        cell.setCellValue("Definition");
        cell.setCellStyle(cellStyle);
    }

    @NonNull
    protected String getDeckName(@NonNull String deckDbPath) {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1);
    }
}