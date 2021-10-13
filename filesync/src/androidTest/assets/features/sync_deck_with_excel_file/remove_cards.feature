Feature: Sync the Excel file with the Deck. Remove cards.

  Scenario: SE_RE_01 Remove cards in the imported file.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 4 | Sample answer 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  # TODO Scenario: SE_RE_02 Remove cards in the Deck.