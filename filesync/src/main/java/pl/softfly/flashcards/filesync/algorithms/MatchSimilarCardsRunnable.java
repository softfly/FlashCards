package pl.softfly.flashcards.filesync.algorithms;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.CardImported;
import pl.softfly.flashcards.filesync.entity.FileSynced;

/**
 * Compare all cards {@link Card} with all imported cards {@link CardImported}.
 * Calculate the similarity of those cards.
 * Connect the most similar card with imported card.
 *
 * @author Grzegorz Ziemski
 * @todo discard cards of very different length.
 */
public class MatchSimilarCardsRunnable implements Runnable {

    public static final String TAG = "MatchSimilarCardsRunnable";
    private final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
    private final FileSyncDeckDatabase deckDb;
    private final FileSynced fileSynced;
    private final List<CardImported> cardImportedList;

    public MatchSimilarCardsRunnable(
            FileSyncDeckDatabase deckDb,
            FileSynced fileSynced,
            List<CardImported> cardImportedList
    ) {
        this.deckDb = deckDb;
        this.fileSynced = fileSynced;
        this.cardImportedList = cardImportedList;
    }

    @Override
    public void run() {
        while (!cardImportedList.isEmpty()) {
            //long start = Instant.now().toEpochMilli();
            CardImported cardImported = cardImportedList.remove(0);
            double maxSimilarity = 0;
            Card mostSimilarCard = null;

            //@todo optimization of using less memory, could be shared between threads
            List<Card> cardList = deckDb.cardDao().findByCardImportedNullOrderById(0, fileSynced.getId());
            while (!cardList.isEmpty()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Card card = cardList.remove(0);

                double similarity = calcSimilarity(card.getTerm(), cardImported.getTerm());
                similarity += calcSimilarity(card.getDefinition(), cardImported.getDefinition());
                if (similarity > 1.5 & similarity > maxSimilarity) {
                    mostSimilarCard = card;
                    maxSimilarity = similarity;
                }

                if (cardList.isEmpty()) {
                    cardList = deckDb.cardDao().findByCardImportedNullOrderById(card.getId(), fileSynced.getId());
                }
            }
            if (mostSimilarCard != null) linkCards(cardImported, mostSimilarCard);
            //displayLog(cardImported, start);
        }
    }

    protected void displayLog(@NonNull CardImported cardImported, long start) {
        LocalTime time = Instant.ofEpochMilli(Instant.now().toEpochMilli() - start)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
        try {
            Runtime info = Runtime.getRuntime();
            long freeSize = info.freeMemory();
            long totalSize = info.totalMemory();
            long usedSize = totalSize - freeSize;
            Log.i(TAG, String.format(
                    "Similarity has been checked. [ThreadId=%s, CardImported=%5s, freeSize=%5d, totalSize=%5d, usedSize=%5d, time=%s]",
                    Thread.currentThread().getId(),
                    cardImported.getId(),
                    freeSize / 0x100000L,
                    totalSize / 0x100000L,
                    usedSize / 0x100000L,
                    time.toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected double calcSimilarity(@Nullable String str1, @Nullable String str2) {
        double similarity = 0;
        if (str1 == null && str2 == null) {
            similarity += 1;
        } else if (str1 == null) {
            similarity += jaroWinklerSimilarity.apply("", str2);
        } else if (str2 == null) {
            similarity += jaroWinklerSimilarity.apply(str1, "");
        } else {
            similarity += jaroWinklerSimilarity.apply(str1, str2);
        }
        return similarity;
    }

    protected void linkCards(@NonNull CardImported cardImported, @NonNull Card card) {
        cardImported.setCardId(card.getId());
        cardImported.setContentStatus(
                isImportedFileNewer(card) ?
                        CardImported.STATUS_UPDATE_BY_FILE :
                        CardImported.STATUS_UPDATE_BY_DECK
        );
        cardImported.setPositionStatus(
                isImportedFileNewer(card) ?
                        CardImported.POSITION_STATUS_BY_FILE :
                        CardImported.POSITION_STATUS_BY_DECK
        );
        deckDb.cardImportedDao().updateAll(cardImported);
    }

    protected boolean isImportedFileNewer(@NonNull Card card) {
        return fileSynced.getLastSyncAt() > card.getModifiedAt();
    }
}