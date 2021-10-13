package pl.softfly.flashcards.filesync.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import pl.softfly.flashcards.entity.Card;

/**
 * A card representing a card from a synchronized / imported / exported file.
 *
 * @author Grzegorz Ziemski
 */
@Entity(tableName = "FileSync_CardImported")
public class CardImported {

    /** -----------------------------------------------------------------------------------------
     * {@link #status}
     * ----------------------------------------------------------------------------------------- */
    /**
     * {@link SyncExcelToDeckTask#importAndMatchCardsFromImportedFile()}
     */
    public static final String STATUS_UNCHANGED = "UNCHANGED";

    /**
     * {@link SyncExcelToDeckTask#matchSimilarCards()}
     */
    public static final String STATUS_UPDATE_BY_DECK = "UPDATE_BY_DECK";

    /**
     * {@link SyncExcelToDeckTask#matchSimilarCards()}
     */
    public static final String STATUS_UPDATE_BY_FILE = "UPDATE_BY_FILE";

    /**
     * {@link SyncExcelToDeckTask#processDeckCard()}
     */
    public static final String STATUS_INSERT_BY_DECK = "INSERT_BY_DECK";

    /**
     * {@link SyncExcelToDeckTask#addCardsFromImportedFile()}
     */
    public static final String STATUS_INSERT_BY_FILE = "INSERT_BY_FILE";

    /**
     * @todo This case does not occur.
     * The entire deck would need to be newer than the imported file.
     */
    public static final String STATUS_DELETE_BY_DECK = "DELETE_BY_DECK";

    /**
     * {@link SyncExcelToDeckTask#processDeckCard()}
     */
    public static final String STATUS_DELETE_BY_FILE = "DELETE_BY_FILE";

    /** -----------------------------------------------------------------------------------------
     * {@link #positionStatus}
     * ----------------------------------------------------------------------------------------- */
    /**
     * {@link SyncExcelToDeckTask#checkIfPositionUnchanged}
     */
    public static final String POSITION_STATUS_UNCHANGED = "UNCHANGED";

    /**
     * {@link SyncExcelToDeckTask#importAndMatchCardsFromImportedFile()}
     * {@link SyncExcelToDeckTask#matchSimilarCards()}
     */
    public static final String POSITION_STATUS_BY_DECK = "BY_DECK";

    /**
     * {@link SyncExcelToDeckTask#importAndMatchCardsFromImportedFile()}
     * {@link SyncExcelToDeckTask#matchSimilarCards()}
     * {@link SyncExcelToDeckTask#addCardsFromImportedFile()}
     */
    public static final String POSITION_STATUS_BY_FILE = "BY_FILE";

    @PrimaryKey
    private Integer id;

    private Integer ordinal;

    /**
     * The id of {@link Card}.
     */
    private Integer cardId;

    private String contentStatus;

    private String positionStatus;

    /**
     * The value is blank if the value in the card is the same.
     * Optimization for storing redundant text data.
     * {@link CardImported#STATUS_UNCHANGED}
     */
    private String question;

    /**
     * The value is blank if the value in the card is the same.
     * Optimization for storing redundant text data.
     * {@link CardImported#STATUS_UNCHANGED}
     */
    private String answer;

    /**
     * Previous {@link CardImported}.
     * Represents the order of cards from the file before synchronization.
     * {@link SyncExcelToDeckTask#importAndMatchCardsFromImportedFile()}
     */
    private Integer previousId;

    /**
     * Next {@link CardImported}.
     * Represents the order of cards from the file before synchronization.
     * {@link SyncExcelToDeckTask#importAndMatchCardsFromImportedFile()}
     */
    private Integer nextId;

    /**
     * Previous {@link Card}.
     * Represents the order of cards from the file before synchronization.
     * {@link SyncExcelToDeckTask#processDeckCard()}
     */
    private Integer previousCardDeckId;

    /**
     * Next {@link Card}.
     * Represents the order of cards from the file before synchronization.
     * {@link SyncExcelToDeckTask#processDeckCard()}
     */
    private Integer nextCardDeckId;

    /**
     * Previous {@link Card}.
     * Represents the order of cards from the file before synchronization.
     * {@link SyncExcelToDeckTask#processCardFromFile()}
     */
    private Integer previousCardId;

    /**
     * Next {@link Card}.
     * Represents the order of cards from the file before synchronization.
     * {@link SyncExcelToDeckTask#processCardFromFile()}
     */
    private Integer nextCardId;

    /**
     * Previous {@link Card}
     * Represents the order of cards after synchronization.
     * Determined by {@link DetermineNewOrderCards}.
     */
    private Integer newPreviousCardImportedId;

    /**
     * Next {@link Card}.
     * Represents the order of cards after synchronization.
     * Determined by {@link DetermineNewOrderCards}.
     */
    private Integer newNextCardImportedId;

    /**
     * Represents the order of cards after synchronization.
     * Determined by {@link DetermineNewOrderCards#determineNewOrdinals}.
     */
    private Integer newOrdinal;

    /**
     * Cards are merged into graphs.
     * At the end of the {@link DetermineNewOrderCards}, the cards are merged into one graph.
     */
    private Integer graph;

    /**
     * -----------------------------------------------------------------------------------------
     * @todo Remove, fields for debugging.
     * -----------------------------------------------------------------------------------------
     */
    private Integer debugFirstGraph;

    private Integer debugCardEdgeId;

    /**
     * Optimized for not updating all rows in the file, only changed.
     * {@link SyncExcelToDeckTask#updateExcelFile}
     * Determined by {@link DetermineNewOrderCards#determineNewOrdinals}.
     */
    private boolean orderChanged;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getContentStatus() {
        return contentStatus;
    }

    public void setContentStatus(String contentStatus) {
        this.contentStatus = contentStatus;
    }

    public String getPositionStatus() {
        return positionStatus;
    }

    public void setPositionStatus(String positionStatus) {
        this.positionStatus = positionStatus;
    }

    public boolean isOrderChanged() {
        return orderChanged;
    }

    public void setOrderChanged(boolean orderChanged) {
        this.orderChanged = orderChanged;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getPreviousId() {
        return previousId;
    }

    public void setPreviousId(Integer previousId) {
        this.previousId = previousId;
    }

    public Integer getNextId() {
        return nextId;
    }

    public void setNextId(Integer nextId) {
        this.nextId = nextId;
    }

    public Integer getPreviousCardDeckId() {
        return previousCardDeckId;
    }

    public void setPreviousCardDeckId(Integer previousCardDeckId) {
        this.previousCardDeckId = previousCardDeckId;
    }

    public Integer getNextCardDeckId() {
        return nextCardDeckId;
    }

    public void setNextCardDeckId(Integer nextCardDeckId) {
        this.nextCardDeckId = nextCardDeckId;
    }

    public Integer getPreviousCardId() {
        return previousCardId;
    }

    public void setPreviousCardId(Integer previousCardId) {
        this.previousCardId = previousCardId;
    }

    public Integer getNextCardId() {
        return nextCardId;
    }

    public void setNextCardId(Integer nextCardId) {
        this.nextCardId = nextCardId;
    }

    public Integer getNewPreviousCardImportedId() {
        return newPreviousCardImportedId;
    }

    public void setNewPreviousCardImportedId(Integer newPreviousCardImportedId) {
        this.newPreviousCardImportedId = newPreviousCardImportedId;
    }

    public Integer getNewNextCardImportedId() {
        return newNextCardImportedId;
    }

    public void setNewNextCardImportedId(Integer newNextCardImportedId) {
        this.newNextCardImportedId = newNextCardImportedId;
    }

    public Integer getNewOrdinal() {
        return newOrdinal;
    }

    public void setNewOrdinal(Integer newOrdinal) {
        this.newOrdinal = newOrdinal;
    }

    public Integer getGraph() {
        return graph;
    }

    public void setGraph(Integer graph) {
        this.graph = graph;
    }

    public Integer getDebugFirstGraph() {
        return debugFirstGraph;
    }

    public void setDebugFirstGraph(Integer debugFirstGraph) {
        this.debugFirstGraph = debugFirstGraph;
    }

    public Integer getDebugCardEdgeId() {
        return debugCardEdgeId;
    }

    public void setDebugCardEdgeId(Integer debugCardEdgeId) {
        this.debugCardEdgeId = debugCardEdgeId;
    }
}
