package pl.softfly.flashcards.entity.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import pl.softfly.flashcards.entity.deck.CardLearningProgress;
import pl.softfly.flashcards.entity.deck.Card;

/**
 * @author Grzegorz Ziemski
 */
@Entity
public class Deck {

    @PrimaryKey
    private Integer id;

    private String name;

    private String path;

    /**
     * Refreshed after adding / updating a {@link Card} or {@link CardLearningProgress}.
     */
    private long lastUpdatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
