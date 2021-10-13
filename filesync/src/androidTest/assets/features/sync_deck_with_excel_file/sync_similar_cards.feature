Feature: Sync the Excel file with the Deck. Find similar cards.

  @disabled
  Scenario: SE_SI_01 Update the deck cards with changes from the Excel file.
    Given Add the following cards into the deck:
      | Sample question 1  | Sample answer 1  | 0 |
      | Sample question 2  | Sample answer 2  | 0 |
    Given Create an Excel file with the last modification on 1.
    Given Add the following cards into the file:
      | Sample question 11 | Sample answer 11 |
      | Sample question 22 | Sample answer 22 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 11 | Sample answer 11 |
      | Sample question 22 | Sample answer 22 |
    Then Check the deck with cards.
    Then Check the Excel file.

  @disabled
  Scenario Outline: SE_SI_02 Multithreading, Prevent the same card from being matched multiple times.
    Given The deck of <num> generated cards.
      | Sample question deck duplicated  | Sample answer deck duplicated  | 0 |
    Given The deck of <num> generated cards.
      | Sample question deck {i}         | Sample answer deck {i}         | 0 |
    Given The Excel file of <num> generated cards with last modified 1.
      | Sample question excel duplicated | Sample answer excel duplicated |
    Given The Excel file of <num> generated cards with last modified 1.
      | Sample question excel {i}        | Sample answer excel {i}        |
    When Synchronize the Excel file with the deck.
    Then 1002 cards in the deck.
    Then 1002 cards in the imported file.
    Examples:
      | num |
      | 501 |