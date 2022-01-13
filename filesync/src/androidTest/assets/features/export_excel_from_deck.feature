Feature: Export the Excel file from the Deck.

  @single
  Scenario: EX_01 No changes.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
    When Export an Excel file from the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.
