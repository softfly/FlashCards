package pl.softfly.flashcards;

import android.content.Context;

import java.time.LocalDateTime;

import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;

/**
 * @author Grzegorz Ziemski
 */
public class CreateSampleDeck {

    private static final String DECK_NAME = "Sample Deck";

    private static final String[][] SAMPLE_DECK = {
            {
                    "What has 13 hearts, but no other organs?",
                    "A deck of cards."
            },
            {
                    "What building has the most stories?",
                    "The library."
            },
            {
                    "Why is Rudolph so good at answering trivia questions?",
                    "He nose a lot."
            },
            {
                    "How many letters are there in the English alphabet?",
                    "18. Three in \"the,\" seven in \"English,\" and eight in \"alphabet\"."
            },
            {
                    "What is the capital in France?",
                    "The letter F is the only capital letter in France."
            },
            {
                    "What goes up but never comes back down?",
                    "Your age."
            },
            {
                    "What does nobody want, yet nobody wants to lose?",
                    "Work."
            },
            {
                    "Why did the spider get a job in I.T.?",
                    "He was a great web designer."
            },
            {
                    "There are two monkeys on a tree and one jumps off. Why does the other monkey jump too?",
                    "Monkey see monkey do."
            },
            {
                    "What can you catch, but not throw?",
                    "A cold."
            },
            {
                    "I follow you all the time and copy your every move, but you can’t touch me or catch me. What am I?",
                    "Your shadow."
            },
            {
                    "What do you get when you cross a snowman and a vampire?",
                    "Frostbite."
            },
            {
                    "Why are teddy bears never hungry?",
                    "Because they are always stuffed."
            },
            {
                    "Which fish costs the most?",
                    "A goldfish!"
            },
            {
                    "What did the triangle say to the circle?",
                    "You are pointless."
            },
            {
                    "Give me food and I will live, Give me water, and I will die.",
                    "Fire."
            },
            {
                    "What do you call a super hero who has lost his powers?",
                    "A super-zero."
            },
            {
                    "I'm taller when I'm young, shorter when I'm old. What am I?",
                    "A candle."
            },
            {
                    "What is easier to get into than out of?",
                    "Trouble."
            },
            {
                    "What is always right in front of you, yet you cannot see it?",
                    "The future. "
            },
            {
                    "How did Darth Vader know what Luke Skywalker was getting for Christmas?",
                    "Because he felt his presents."
            },
            {
                    "Where does Dracula keep his money?",
                    "A blood bank."
            },
            {
                    "If I have it, I don’t share it.  If I share it, I don’t have it.  What is it?",
                    "A secret."
            },
            {
                    "What was the largest island in the world before Australia was discovered?",
                    "Australia - it was still there just not discovered."
            },
            {
                    "Why did the tortilla chip start dancing?",
                    "It put on the salsa."
            },
            {
                    "Which fruit is always sad?",
                    "A blueberry."
            },
            {
                    "What has no beginning, end, or middle?",
                    "A circle."
            },
            {
                    "What type of music do rabbits like?",
                    "Hip Hop!"
            },
            {
                    "Where were Fizzy drinks invented?",
                    "The inventor Joseph Priestley is thought to have created carbonated water by accident in 1767 at a brewery in Leeds."
            },
            {
                    "What do Australians call the textile department?",
                    "If you go to Australia and are looking for any kind of textile like bed sheets or similar items, you will be sent to the “Manchester department” of the store."
            }
    };

    public void create(Context context, Action doOnComplete) {
        if (!AppDatabaseUtil
                .getInstance(context)
                .getStorageDb()
                .exists(DECK_NAME)) {

            AppDatabaseUtil
                    .getInstance(context)
                    .getStorageDb()
                    .removeDatabase(DECK_NAME);

            DeckDatabase deckDB = AppDatabaseUtil
                    .getInstance(context)
                    .getDeckDatabase(DECK_NAME);

            Card[] cards = new Card[SAMPLE_DECK.length];
            LocalDateTime modifiedAt = LocalDateTime.now();
            for (int i = 0; i < SAMPLE_DECK.length; i++) {
                String[] sampleCard = SAMPLE_DECK[i];
                Card card = new Card();
                card.setOrdinal(i + 1);
                card.setTerm(sampleCard[0]);
                card.setDefinition(sampleCard[1]);
                card.setModifiedAt(modifiedAt);
                cards[i] = card;
            }

            deckDB.cardDaoAsync().insertAll(cards)
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnComplete(doOnComplete)
                    .subscribe();
        }
    }

}
