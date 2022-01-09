package pl.softfly.flashcards.filesync.algorithms;

import static pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck.ENTITIES_TO_UPDATE_POOL_MAX;
import static pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck.MULTIPLE_ORDINAL;


import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.InvalidAlgorithmException;
import pl.softfly.flashcards.filesync.db.SyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.CardEdge;
import pl.softfly.flashcards.filesync.entity.CardImported;
import pl.softfly.flashcards.filesync.view.GraphEdge;
import pl.softfly.flashcards.filesync.view.GraphEdgeOnlyNewCards;

/**
 * 5. Determine the new order of the cards after merging.
 *
 * @author Grzegorz Ziemski
 */
public class DetermineNewOrderCards {

    private SyncDeckDatabase deckDb;

    private LocalDateTime fileLastSyncAt;

    public void determineNewOrderCards(
            @NonNull SyncDeckDatabase deckDb,
            @NonNull LocalDateTime fileLastSyncAt
    ) {
        this.deckDb = deckDb;
        this.fileLastSyncAt = fileLastSyncAt;

        refreshCardOrdinal();

        // 5.1. Create an edge between adjacent cards in the deck.
        boolean isEdgesCreated = createCardEdgesForDeckCards();
        // 5.2. Create an edge between adjacent cards from the imported file.
        isEdgesCreated = isEdgesCreated | createCardEdgesForImportedFile();
        if (!isEdgesCreated) return; //No cards

        // 5.3. Merge cards that relations are very certain into graphs.
        mergeCardsIntoGraphsByEdges(new String[]{
                // Weight: 1
                CardEdge.STATUS_UNCHANGED,
                // Weight: 5
                CardEdge.STATUS_DECK_BOTH_NEW,
                CardEdge.STATUS_IMPORTED_BOTH_NEW,
        });
        // 5.4. Merge the graphs with new cards if they have the same parent.
        mergeGraphsWithNewCards();
        // 5.5. Merge cards with the rest relations into graphs.
        mergeCardsIntoGraphsByEdges(new String[]{
                // Weight: 4
                CardEdge.STATUS_IMPORTED_SECOND_NEW,
                CardEdge.STATUS_DECK_SECOND_NEW,

                // Weight:3
                CardEdge.STATUS_DECK_BOTH_NEWER,
                CardEdge.STATUS_IMPORTED_BOTH_NEWER,

                // Weight: 2
                CardEdge.STATUS_IMPORTED_FIRST_NEW,
                CardEdge.STATUS_DECK_FIRST_NEW,
                //CardEdge.STATUS_IMPORTED_BOTH_OLDER,
                //CardEdge.STATUS_DECK_BOTH_OLDER,
                //CardEdge.STATUS_IMPORTED_ONE_NEWER,
                //CardEdge.STATUS_DECK_ONE_NEWER,
                //@todo check why not used
        });
        // 5.6. Create trivial one-card graphs if the card does not belong to any graph.
        createTrivialOneCardsGraphs();
        // 5.7. Merge the graphs with the strongest relation
        // - the summed weight of the card edges between the graphs.
        mergeGraphsByStrongestRelation();
        // 5.8. Merge consecutive graphs without any relation.
        mergeGraphsWithoutRelation();
        // 5.9. Enrich {@link CardImported#newOrdinal}
        // passing through all cards using {@link CardImported#newNextCardImportedId}
        determineNewOrdinals();
    }

    // @todo move to create card
    protected void refreshCardOrdinal() {
        List<Integer> cardIds = deckDb.cardDao().getCardIdsOrderByOrdinalAsc();
        for (int i = MULTIPLE_ORDINAL; !cardIds.isEmpty(); i += MULTIPLE_ORDINAL) {
            deckDb.cardDao().updateOrdinal(cardIds.remove(0), i);
        }
    }

    /**
     * 5.1. Create an edge between adjacent cards in the deck.
     * @return True if any edge has been created.
     */
    protected boolean createCardEdgesForDeckCards() {
        List<CardImported> cardImportedList = deckDb.cardImportedDao()
                .findByStatusNotOrderByCardOrdinalAsc(new String[]{
                        CardImported.STATUS_INSERT_BY_FILE,
                        CardImported.STATUS_DELETE_BY_FILE,
                        CardImported.STATUS_DELETE_BY_DECK
                }, 0);
        if (cardImportedList.isEmpty()) return false;
        List<CardEdge> cardEdgesListToInsert = new LinkedList<>();

        // Prepare for the first iteration
        CardImported currentCardImported = cardImportedList.remove(0);
        Card currentCard;

        boolean currentIsNew =
                CardImported.STATUS_INSERT_BY_DECK.equals(currentCardImported.getContentStatus());
        boolean currentIsNewerThanImported =
                CardImported.POSITION_STATUS_BY_DECK.equals(currentCardImported.getPositionStatus());

        while (!cardImportedList.isEmpty()) {
            // Prepare for the iteration
            CardImported nextCardImported = cardImportedList.remove(0);
            Card nextCard = deckDb.cardDao().findById(nextCardImported.getCardId());

            boolean nextIsNew =
                    CardImported.STATUS_INSERT_BY_DECK.equals(nextCardImported.getContentStatus());
            boolean nextIsNewerThanImported =
                    CardImported.POSITION_STATUS_BY_DECK.equals(nextCardImported.getPositionStatus());

            // Create CardEdge
            CardEdge cardEdge = new CardEdge();
            cardEdge.setFromCardImportedId(currentCardImported.getId());
            cardEdge.setToCardImportedId(nextCardImported.getId());
            if (currentIsNew && nextIsNew) {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_BOTH_NEW);
            } else if (currentIsNew) {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_FIRST_NEW);
            } else if (nextIsNew) {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_SECOND_NEW);
            } else if (currentIsNewerThanImported && nextIsNewerThanImported) {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_BOTH_NEWER);
            } else if (currentIsNewerThanImported || nextIsNewerThanImported) {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_ONE_NEWER);
            } else {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_BOTH_OLDER);
            }

            // Saving
            cardEdgesListToInsert.add(cardEdge);
            if (cardEdgesListToInsert.size() % SyncExcelToDeck.ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                deckDb.cardEdgeDao().insertAll(cardEdgesListToInsert);
                cardEdgesListToInsert.clear();
            }

            // Prepare for the next iteration
            currentCard = nextCard;
            currentCardImported = nextCardImported;
            currentIsNew = nextIsNew;
            currentIsNewerThanImported = nextIsNewerThanImported;
            if (cardImportedList.isEmpty()) {
                cardImportedList = deckDb.cardImportedDao()
                        .findByStatusNotOrderByCardOrdinalAsc(new String[]{
                                CardImported.STATUS_INSERT_BY_FILE,
                                CardImported.STATUS_DELETE_BY_FILE
                        }, currentCard.getOrdinal());
            }
        }

        // Final saving
        if (!cardEdgesListToInsert.isEmpty()) {
            deckDb.cardEdgeDao().insertAll(cardEdgesListToInsert);
        }
        return true;
    }

    /**
     * 5.2. Create an edge between adjacent cards from the imported file.
     * @return True if any edge has been created.
     */
    protected boolean createCardEdgesForImportedFile() {
        List<CardEdge> cardEdgeListToInsert = new LinkedList<>();
        List<CardEdge> cardEdgeListToUpdate = new LinkedList<>();

        List<CardImported> cardImportedList = deckDb.cardImportedDao()
                .findByStatusNotOrderByOrdinalAsc(new String[]{
                        CardImported.STATUS_INSERT_BY_DECK,
                        CardImported.STATUS_DELETE_BY_DECK,
                        CardImported.STATUS_DELETE_BY_FILE
                }, 0);
        if (cardImportedList.isEmpty()) return false;
        CardImported currentCardImported = cardImportedList.remove(0);

        while (!cardImportedList.isEmpty()) {
            CardImported nextCardImported = cardImportedList.remove(0);

            CardEdge cardEdge = deckDb.cardEdgeDao()
                    .findByVertices(currentCardImported.getId(), nextCardImported.getId());
            if (cardEdge != null) {
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_UNCHANGED);
                cardEdgeListToUpdate.add(cardEdge);
            } else {
                cardEdge = new CardEdge();
                cardEdge.setFromCardImportedId(currentCardImported.getId());
                cardEdge.setToCardImportedId(nextCardImported.getId());

                boolean currentIsNew =
                        CardImported.STATUS_INSERT_BY_FILE.equals(currentCardImported.getContentStatus());
                boolean nextIsNew =
                        CardImported.STATUS_INSERT_BY_FILE.equals(nextCardImported.getContentStatus());
                if (currentIsNew && nextIsNew) {
                    setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_BOTH_NEW);
                    cardEdgeListToInsert.add(cardEdge);
                } else if (currentIsNew) {
                    setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_FIRST_NEW);
                    cardEdgeListToInsert.add(cardEdge);
                } else if (nextIsNew) {
                    setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_SECOND_NEW);
                    cardEdgeListToInsert.add(cardEdge);
                } else {
                    boolean currentIsNewerThanDeck =
                            CardImported.POSITION_STATUS_BY_FILE.equals(currentCardImported.getPositionStatus());
                    boolean nextIsNewerThanDeck =
                            CardImported.POSITION_STATUS_BY_FILE.equals(nextCardImported.getPositionStatus());
                    if (currentIsNewerThanDeck && nextIsNewerThanDeck) {
                        setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_BOTH_NEWER);
                        cardEdgeListToInsert.add(cardEdge);
                    } else if (currentIsNewerThanDeck || nextIsNewerThanDeck) {
                        setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_ONE_NEWER);
                        cardEdgeListToInsert.add(cardEdge);
                    } else {
                        setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_BOTH_OLDER);
                        cardEdgeListToInsert.add(cardEdge);
                    }
                }
            }

            // Saving
            if ((cardEdgeListToInsert.size() + cardEdgeListToUpdate.size()) >= ENTITIES_TO_UPDATE_POOL_MAX) {
                deckDb.cardEdgeDao().insertAll(cardEdgeListToInsert);
                cardEdgeListToInsert.clear();
                deckDb.cardEdgeDao().updateAll(cardEdgeListToUpdate);
                cardEdgeListToUpdate.clear();
            }

            // Prepare for the next iteration
            if (cardImportedList.isEmpty()) {
                cardImportedList = deckDb.cardImportedDao()
                        .findByStatusNotOrderByOrdinalAsc(new String[]{
                                CardImported.STATUS_INSERT_BY_DECK,
                                CardImported.STATUS_DELETE_BY_DECK,
                                CardImported.STATUS_DELETE_BY_FILE
                        }, nextCardImported.getOrdinal());
            }
            currentCardImported = nextCardImported;
        }

        // Final saving
        if (!cardEdgeListToInsert.isEmpty()) {
            deckDb.cardEdgeDao().insertAll(cardEdgeListToInsert);
        }
        if (!cardEdgeListToUpdate.isEmpty()) {
            deckDb.cardEdgeDao().updateAll(cardEdgeListToUpdate);
        }
        return true;
    }

    protected void setCardEdgeStatusAndWeight(CardEdge cardEdge, String status) {
        cardEdge.setStatus(status);
        switch (status) {
            case CardEdge.STATUS_UNCHANGED:
                cardEdge.setWeight(1);
            case CardEdge.STATUS_IMPORTED_BOTH_OLDER:
            case CardEdge.STATUS_DECK_BOTH_OLDER:
            case CardEdge.STATUS_IMPORTED_ONE_NEWER:
            case CardEdge.STATUS_DECK_ONE_NEWER:
            case CardEdge.STATUS_DECK_FIRST_NEW:
            case CardEdge.STATUS_IMPORTED_FIRST_NEW:
                cardEdge.setWeight(2);
                break;
            case CardEdge.STATUS_IMPORTED_BOTH_NEWER:
            case CardEdge.STATUS_DECK_BOTH_NEWER:
                cardEdge.setWeight(3);
                break;
            case CardEdge.STATUS_DECK_SECOND_NEW:
            case CardEdge.STATUS_IMPORTED_SECOND_NEW:
                cardEdge.setWeight(4);
            case CardEdge.STATUS_DECK_BOTH_NEW:
            case CardEdge.STATUS_IMPORTED_BOTH_NEW:
                cardEdge.setWeight(5);
                break;
            default:
                throw new InvalidAlgorithmException("Unknown CardEdge status: " + status);
        }
    }

    /**
     * 5.3. Connect cards that relationships are very certain into Graphs.
     */
    protected void mergeCardsIntoGraphsByEdges(String[] statuses) {
        CardEdge cardEdge = deckDb.cardEdgeDao().findByStatusOrderByWeightDesc(statuses);
        while (cardEdge != null) {
            CardImported fromCardImported = deckDb.cardImportedDao().findById(cardEdge.getFromCardImportedId());
            CardImported toCardImported = deckDb.cardImportedDao().findById(cardEdge.getToCardImportedId());
            if (!detectCycleInGraphIfMerged(fromCardImported, toCardImported)) {
                fromCardImported.setDebugCardEdgeId(cardEdge.getId());
                toCardImported.setDebugCardEdgeId(cardEdge.getId());
                mergeCardsIntoGraph(fromCardImported, toCardImported);
                deckDb.cardEdgeDao()
                        .deleteByFromOrTo(cardEdge.getFromCardImportedId(), cardEdge.getToCardImportedId());
            } else {
                deckDb.cardEdgeDao().delete(cardEdge.getId());
            }
            cardEdge = deckDb.cardEdgeDao().findByStatusOrderByWeightDesc(statuses);
        }
    }

    /**
     * Detect cycle in graph if cards would be merged.
     */
    protected boolean detectCycleInGraphIfMerged(CardImported fromCardImported, CardImported toCardImported) {
        Integer fromGraph = fromCardImported.getGraph();
        Integer toGraph = toCardImported.getGraph();
        if (toGraph != null &&
                deckDb.cardImportedDao().countByIdAndGraph(fromCardImported.getId(), toGraph) > 0)
            return true;
        return fromGraph != null &&
                deckDb.cardImportedDao().countByIdAndGraph(toCardImported.getId(), fromGraph) > 0;
    }

    protected void mergeCardsIntoGraph(CardImported fromCardImported, CardImported toCardImported) {
        if (fromCardImported.getNewNextCardImportedId() != null)
            throw new InvalidAlgorithmException("The card already has the next card.");
        if (toCardImported.getNewPreviousCardImportedId() != null)
            throw new InvalidAlgorithmException("The card already has the previous card.");

        Integer fromGraph = fromCardImported.getGraph();
        Integer toGraph = toCardImported.getGraph();

        if (fromGraph == null && toGraph == null) {
            createGraph(fromCardImported, toCardImported);
        } else if (fromGraph != null && toGraph != null) {
            mergeGraphs(fromCardImported, toCardImported);
        } else if (fromGraph != null) {
            addCardIntoEndGraph(fromCardImported, toCardImported);
        } else {
            addCardIntoBeginningGraph(fromCardImported, toCardImported);
        }

        if (fromCardImported.getDebugFirstGraph() == null)
            fromCardImported.setDebugFirstGraph(fromCardImported.getGraph());

        if (toCardImported.getDebugFirstGraph() == null)
            toCardImported.setDebugFirstGraph(toCardImported.getGraph());

        deckDb.cardImportedDao().updateAll(fromCardImported, toCardImported);
    }

    protected void createGraph(CardImported fromCardImported, CardImported toCardImported) {
        if (fromCardImported.getGraph() != null)
            throw new InvalidAlgorithmException("The card already belongs to the graph.");// @todo remove
        if (toCardImported.getGraph() != null)
            throw new InvalidAlgorithmException("The card already belongs to the graph.");// @todo remove

        Integer lastGraphNum = deckDb.cardImportedDao().maxGraph();
        int newGraph = lastGraphNum != null ? lastGraphNum + 1 : 1;

        fromCardImported.setNewNextCardImportedId(toCardImported.getId());
        fromCardImported.setGraph(newGraph);
        toCardImported.setNewPreviousCardImportedId(fromCardImported.getId());
        toCardImported.setGraph(newGraph);
    }

    protected void mergeGraphs(CardImported fromCardImported, CardImported toCardImported) {
        deckDb.cardImportedDao().updateGraphByGraph(toCardImported.getGraph(), fromCardImported.getGraph());
        fromCardImported.setNewNextCardImportedId(toCardImported.getId());
        toCardImported.setNewPreviousCardImportedId(fromCardImported.getId());
        toCardImported.setGraph(fromCardImported.getGraph());
    }

    protected void addCardIntoBeginningGraph(CardImported fromCardImported, CardImported toCardImported) {
        if (fromCardImported.getGraph() != null)
            throw new InvalidAlgorithmException("The card already belongs to the graph.");// @todo remove

        fromCardImported.setNewNextCardImportedId(toCardImported.getId());
        fromCardImported.setGraph(toCardImported.getGraph());
        toCardImported.setNewPreviousCardImportedId(fromCardImported.getId());
    }

    protected void addCardIntoEndGraph(CardImported fromCardImported, CardImported toCardImported) {
        if (toCardImported.getGraph() != null)
            throw new InvalidAlgorithmException("The card already belongs to the graph.");// @todo remove

        fromCardImported.setNewNextCardImportedId(toCardImported.getId());
        toCardImported.setNewPreviousCardImportedId(fromCardImported.getId());
        toCardImported.setGraph(fromCardImported.getGraph());
    }

    /**
     * 5.4. Merge Graphs with new cards if they have the same parent.
     * <p>
     * {@link CardEdge#STATUS_DECK_FIRST_NEW}
     * {@link CardEdge#STATUS_DECK_SECOND_NEW}
     * {@link CardEdge#STATUS_IMPORTED_FIRST_NEW}
     * {@link CardEdge#STATUS_IMPORTED_SECOND_NEW}
     */
    protected void mergeGraphsWithNewCards() {
        createFirstVertexToMergeFirstTwoNewCard();
        createTrivialGraphsForNewCardsEdges();
        GraphEdgeOnlyNewCards root = deckDb.graphEdgeOnlyNewCardsDao().findForNewCards();
        while (root != null) {
            int fromGraph = root.getFromGraph();
            List<GraphEdgeOnlyNewCards> leaves
                    = deckDb.graphEdgeOnlyNewCardsDao().findForNewCardsByFrom(fromGraph);
            while (!leaves.isEmpty()) {
                GraphEdgeOnlyNewCards leaf = leaves.remove(0);
                mergeGraphs(fromGraph, leaf.getToGraph());
                deckDb.cardEdgeDao().deleteInsideTheSameGraph(fromGraph);
                deckDb.cardEdgeDao().deleteToMiddleGraph(fromGraph);
            }
            root = deckDb.graphEdgeOnlyNewCardsDao().findForNewCards();
        }
    }

    protected void createFirstVertexToMergeFirstTwoNewCard() {
        CardImported firstCardImportedFromDeck = deckDb.cardImportedDao()
                .findByCardId(deckDb.cardDao().getFirst().getId());
        CardImported firstCardImported = deckDb.cardImportedDao().getFirst();

        if (CardImported.STATUS_INSERT_BY_FILE.equals(firstCardImported.getContentStatus())) {
            CardImported firstVertex = new CardImported();
            firstVertex.setId(0);
            firstVertex.setQuestion("The first vertex of the graph.");
            firstVertex.setOrdinal(0);
            firstVertex.setNextId(firstCardImported.getId());
            firstVertex.setContentStatus(CardImported.STATUS_UNCHANGED);
            firstVertex.setPositionStatus(CardImported.STATUS_UNCHANGED);
            deckDb.cardImportedDao().insertAll(firstVertex);

            firstCardImported.setPreviousId(0);
            deckDb.cardImportedDao().updateAll(firstCardImported);

            CardEdge cardEdge = new CardEdge();
            cardEdge.setFromCardImportedId(0);
            cardEdge.setToCardImportedId(firstCardImported.getId());
            setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_IMPORTED_SECOND_NEW);
            deckDb.cardEdgeDao().insertAll(cardEdge);

            if (CardImported.STATUS_INSERT_BY_DECK.equals(firstCardImportedFromDeck.getContentStatus())) {
                firstCardImportedFromDeck.setPreviousId(0);
                deckDb.cardImportedDao().updateAll(firstCardImported);

                cardEdge = new CardEdge();
                cardEdge.setFromCardImportedId(0);
                cardEdge.setToCardImportedId(firstCardImportedFromDeck.getId());
                setCardEdgeStatusAndWeight(cardEdge, CardEdge.STATUS_DECK_SECOND_NEW);
                deckDb.cardEdgeDao().insertAll(cardEdge);
            }
        }
    }

    /**
     * 5.6. Create trivial one-card graphs if the card does not belong to any graph.
     * A consecutive number is assigned to the Graph field.
     */
    protected void createTrivialOneCardsGraphs() {
        Integer lastGraphNum = deckDb.cardImportedDao().maxGraph();
        if (lastGraphNum == null) {
            lastGraphNum = 0;
        }
        List<CardImported> cardImportedList = deckDb.cardImportedDao().findByStatusNotAndGraphNull(
                new String[]{
                        CardImported.STATUS_DELETE_BY_DECK,
                        CardImported.STATUS_DELETE_BY_FILE
                });//todo limit, dao to queries
        while (!cardImportedList.isEmpty()) {
            deckDb.cardImportedDao().updateGraphById(cardImportedList.remove(0).getId(), ++lastGraphNum);
        }
    }

    /**
     * Create trivial one-card graphs for new card edges
     * if the card does not belong to any graph.
     * A consecutive number is assigned to the Graph field.
     */
    protected void createTrivialGraphsForNewCardsEdges() {
        Integer lastGraphNum = deckDb.cardImportedDao().maxGraph();
        if (lastGraphNum == null) {
            lastGraphNum = 0;
        }
        List<Integer> cardImportedIdList = deckDb.cardImportedDao().findByCardEdgeStatusNew();
        while (!cardImportedIdList.isEmpty()) {
            deckDb.cardImportedDao().updateGraphById(
                    cardImportedIdList.remove(0),
                    ++lastGraphNum
            );
        }
    }

    /**
     * 5.7. Merge the graphs with the strongest relation
     * - the summed weight of the card edges between the graphs.
     * <p>
     * Solves a problem where the first and last cards of graph have no edge
     * with the first or last card of another graph.
     */
    protected void mergeGraphsByStrongestRelation() {
        GraphEdge graphEdge = deckDb.graphEdgeDao().getFirstOrderByWeightDesc();
        while (graphEdge != null) {
            mergeGraphs(graphEdge.getFromGraph(), graphEdge.getToGraph());
            graphEdge = deckDb.graphEdgeDao().getFirstOrderByWeightDesc();
        }
    }

    /**
     * 5.8. Merge consecutive graphs without any relation.
     * Solves a problem if cards in a graph do not have card edges relative to cards in another graph.
     */
    protected void mergeGraphsWithoutRelation() {
        CardImported firstCard = findNewestFirstCard();
        Integer firstGraph = firstCard.getGraph();

        List<Integer> graphs = deckDb.cardImportedDao().getGraphs();
        while (!graphs.isEmpty()) {
            Integer toGraph = graphs.remove(0);
            if (!firstGraph.equals(toGraph))
                mergeGraphs(firstGraph, toGraph);
        }
    }

    protected CardImported findNewestFirstCard() {
        Card firstCard = deckDb.cardDao().getFirst();
        if (isImportedFileNewer(firstCard)) {
            return deckDb.cardImportedDao().getFirst();
        } else {
            return deckDb.cardImportedDao().findByCardId(firstCard.getId());
        }
    }

    protected void mergeGraphs(int fromGraph, int toGraph) {
        int previousCardImportedId = deckDb.cardImportedDao()
                .findLastVertex(fromGraph);

        int nextCardImportedId = deckDb.cardImportedDao()
                .findFirstVertex(toGraph);

        deckDb.cardImportedDao().updateNewNextCardImportedIdById(
                previousCardImportedId,
                nextCardImportedId
        );
        deckDb.cardImportedDao().updateNewPreviousCardImportedIdById(
                nextCardImportedId,
                previousCardImportedId
        );
        deckDb.cardImportedDao().updateGraphByGraph(
                toGraph,
                fromGraph
        );
    }

    /**
     * 5.9. Enrich {@link CardImported#newOrdinal}
     * passing through all cards using {@link CardImported#newNextCardImportedId}
     */
    protected void determineNewOrdinals() {
        Integer ordinal = MULTIPLE_ORDINAL;
        List<CardImported> cardImportedListToUpdate = new LinkedList<>();

        // Check the correctness of the last card.
        List<CardImported> cardImportedList = deckDb.cardImportedDao()
                .findByStatusNotDeletedAndNewNextCardImportedIdIsNull();
        if (cardImportedList.size() > 1) {
            throw new InvalidAlgorithmException("The deck must only have 1 last card!!");
        } else if (cardImportedList.size() == 0) {
            throw new InvalidAlgorithmException("The deck has no last card!");
        }

        // Check the correctness of the first card.
        cardImportedList = deckDb.cardImportedDao()
                .findByStatusNotDeletedAndNewPreviousCardImportedIdIsNull();
        if (cardImportedList.size() > 1) {
            throw new InvalidAlgorithmException("The deck must only have 1 first card!!");
        } else if (cardImportedList.size() == 0) {
            throw new InvalidAlgorithmException("The deck has no first card!");
        }

        CardImported cardImported = cardImportedList.remove(0);
        while (true) {
            if (cardImported.getNewOrdinal() != null) {
                throw new InvalidAlgorithmException("The card already has a new ordinal number!");
            }
            if (!ordinal.equals(cardImported.getOrdinal())) {
                cardImported.setOrderChanged(true);
            }
            cardImported.setNewOrdinal(ordinal);
            cardImportedListToUpdate.add(cardImported);
            ordinal += MULTIPLE_ORDINAL;

            if (cardImported.getNewNextCardImportedId() == null) {
                break;
            } else {
                cardImported = deckDb.cardImportedDao().findById(cardImported.getNewNextCardImportedId());
            }
            if (cardImportedListToUpdate.size() % ENTITIES_TO_UPDATE_POOL_MAX == 0) {
                deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
                cardImportedListToUpdate.clear();
            }
        }

        if (!cardImportedListToUpdate.isEmpty()) {
            deckDb.cardImportedDao().updateAll(cardImportedListToUpdate);
        }
        if (deckDb.cardImportedDao().countByNewOrdinalNull() > 0) {
            throw new InvalidAlgorithmException("Not all cards have been assigned a new ordinal number!");
        }
    }

    protected boolean isImportedFileNewer(Card card) {
        return fileLastSyncAt.isAfter(card.getModifiedAt());
    }
}