Feature: Sync the Excel file with the Deck.

  @single
  Scenario: Check overwriting only changed rows in the file.
    Given Add the following cards into the deck:
      | Swap places 1       | Swap places 1   | 2 |
      | Swap places 2       | Swap places 2   | 2 |
      | Sample question 3   | Sample answer 3 | 0 |
      | Sample question 4   | Sample answer 4 | 0 |
      | Sample question 5   | Sample answer 5 | 0 |
    Given Add the following cards into the file:
      | Swap places 2       | Swap places 2   |
      | Swap places 1       | Swap places 1   |
      | Sample question 3   | Sample answer 3 |
      | Sample question 4   | Sample answer 4 |
      | Sample question 5   | Sample answer 5 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Swap places 1       | Swap places 1   |
      | Swap places 2       | Swap places 2   |
      | Sample question 3   | Sample answer 3 |
      | Sample question 4   | Sample answer 4 |
      | Sample question 5   | Sample answer 5 |
    Then Check the deck with cards.
    Then Check the Excel file.
    Then Updated 2 rows in the file.