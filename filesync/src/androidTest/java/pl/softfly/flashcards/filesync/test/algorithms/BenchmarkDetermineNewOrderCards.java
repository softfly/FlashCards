package pl.softfly.flashcards.filesync.test.algorithms;

import android.util.Log;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import pl.softfly.flashcards.filesync.algorithms.DetermineNewOrderCards;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;

/**
 * @author Grzegorz Ziemski
 */
public class BenchmarkDetermineNewOrderCards extends DetermineNewOrderCards {

    public static final String TAG = "BenchmarkDetermineNewOrderCards";

    @Override
    public void determineNewOrderCards(@NonNull FileSyncDeckDatabase deckDb, @NonNull long fileLastSyncAt) {
        long start = Instant.now().toEpochMilli();
        super.determineNewOrderCards(deckDb, fileLastSyncAt);

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "determineNewOrderCards= " + time.toString());
    }

    @Override
    protected boolean createCardEdgesForDeckCards() {
        long start = Instant.now().toEpochMilli();
        boolean r = super.createCardEdgesForDeckCards();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "createCardEdgesForDeckCards= " + time.toString());
        return r;
    }

    @Override
    protected boolean createCardEdgesForImportedFile() {
        long start = Instant.now().toEpochMilli();
        boolean r = super.createCardEdgesForImportedFile();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "createCardEdgesForImportedFile= " + time.toString());
        return r;
    }

    @Override
    protected void mergeCardsIntoGraphsByEdges(String[] statuses) {
        long start = Instant.now().toEpochMilli();
        super.mergeCardsIntoGraphsByEdges(statuses);

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "mergeCardsIntoGraphsByEdges= " + time.toString());
    }

    @Override
    protected void mergeGraphsWithNewCards() {
        long start = Instant.now().toEpochMilli();
        super.mergeGraphsWithNewCards();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "mergeGraphsWithNewCards= " + time.toString());
    }

    //@ todo remove
    @Override
    protected void createTrivialOneCardsGraphs() {
        long start = Instant.now().toEpochMilli();
        super.createTrivialOneCardsGraphs();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "createTrivialOneCardsGraphs= " + time.toString());
    }

    @Override
    protected void mergeGraphsByStrongestRelation() {
        long start = Instant.now().toEpochMilli();
        super.mergeGraphsByStrongestRelation();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "mergeGraphsByStrongestRelation= " + time.toString());
    }

    @Override
    protected void mergeGraphsWithoutRelation() {
        long start = Instant.now().toEpochMilli();
        super.mergeGraphsWithoutRelation();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "mergeGraphsWithoutRelation= " + time.toString());
    }

    @Override
    protected void determineNewOrdinals() {
        long start = Instant.now().toEpochMilli();
        super.determineNewOrdinals();

        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        Log.i(TAG, "determineNewOrdinals= " + time.toString());
    }

}
