package pl.softfly.flashcards.filesync.test.algorithms;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import pl.softfly.flashcards.filesync.algorithms.DetermineNewOrderCards;
import pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck;

/**
 * @author Grzegorz Ziemski
 */
public class BenchmarkSyncExcelToDeck extends SyncExcelToDeck {

    public static final String TAG = "BenchmarkSyncExcelToDeck";

    public BenchmarkSyncExcelToDeck(
            Context appContext,
            DetermineNewOrderCards determineNewOrderCards
    ) {
        super(appContext, determineNewOrderCards);
    }

    @Override
    protected void importAndMatchCardsFromImportedFile() {
        long start = Instant.now().toEpochMilli();
        super.importAndMatchCardsFromImportedFile();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "importAndMatchCardsFromImportedFile= " + time.toString());
    }

    @Override
    protected void matchSimilarCards() throws InterruptedException {
        long start = Instant.now().toEpochMilli();
        super.matchSimilarCards();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "matchSimilarCards= " + time.toString());
    }

    @Override
    protected void processDeckCard() {
        long start = Instant.now().toEpochMilli();
        super.processDeckCard();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "processDeckCard= " + time.toString());
    }

    @Override
    protected void findNewCardsInImportedFile() {
        long start = Instant.now().toEpochMilli();
        super.findNewCardsInImportedFile();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "findNewCardsInImportedFile= " + time.toString());
    }

    @Override
    protected void processCardFromFile() {
        long start = Instant.now().toEpochMilli();
        super.processCardFromFile();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "processCardFromFile= " + time.toString());
    }

    @Override
    protected void updateDeckCards() {
        long start = Instant.now().toEpochMilli();
        super.updateDeckCards();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "updateDeckCards= " + time.toString());
    }

    @Override
    public void updateExcelFile(@NonNull OutputStream os) throws IOException {
        long start = Instant.now().toEpochMilli();
        super.updateExcelFile(os);

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "updateExcelFile= " + time.toString());
    }

}
