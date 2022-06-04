Feature: Export the Excel file from the Deck.

  Scenario: EX_01 Export the Excel file from the Deck.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 3 | Sample Definition 3 | 2 |
    When Export an Excel file from the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then The expected deck with cards:
      | Term          | Definition          |
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the Excel file.