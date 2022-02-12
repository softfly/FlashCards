Feature: Sync the Excel file with the Deck.


  Scenario: Check overwriting only changed rows in the file.
    Given Add the following cards into the deck:
      | Swap places 1 | Swap places 1       | 2 |
      | Swap places 2 | Swap places 2       | 2 |
      | Sample Term 3 | Sample Definition 3 | 0 |
      | Sample Term 4 | Sample Definition 4 | 0 |
      | Sample Term 5 | Sample Definition 5 | 0 |
    Given Add the following cards into the file:
      | Swap places 2 | Swap places 2       |
      | Swap places 1 | Swap places 1       |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 5 | Sample Definition 5 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Swap places 1 | Swap places 1       |
      | Swap places 2 | Swap places 2       |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 5 | Sample Definition 5 |
    Then Check the deck with cards.
    Then Check the Excel file.
    Then Updated 2 rows in the file.