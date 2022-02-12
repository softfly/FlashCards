Feature: Sync the Excel file with the Deck. Find similar cards.

  @disabled
  Scenario: SE_SI_01 Update the deck cards with changes from the Excel file.
    Given Add the following cards into the deck:
      | Sample Term 1  | Sample Definition 1  | 0 |
      | Sample Term 2  | Sample Definition 2  | 0 |
    Given Create an Excel file with the last modification on 1.
    Given Add the following cards into the file:
      | Sample Term 11 | Sample Definition 11 |
      | Sample Term 22 | Sample Definition 22 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 11 | Sample Definition 11 |
      | Sample Term 22 | Sample Definition 22 |
    Then Check the deck with cards.
    Then Check the Excel file.

  @disabled
  Scenario Outline: SE_SI_02 Multithreading, Prevent the same card from being matched multiple times.
    Given The deck of <num> generated cards.
      | Sample Term deck duplicated  | Sample Definition deck duplicated  | 0 |
    Given The deck of <num> generated cards.
      | Sample Term deck {i}         | Sample Definition deck {i}         | 0 |
    Given The Excel file of <num> generated cards with last modified 1.
      | Sample Term excel duplicated | Sample Definition excel duplicated |
    Given The Excel file of <num> generated cards with last modified 1.
      | Sample Term excel {i}        | Sample Definition excel {i}        |
    When Synchronize the Excel file with the deck.
    Then 1002 cards in the deck.
    Then 1002 cards in the imported file.
    Examples:
      | num |
      | 501 |