Feature: Sync the Excel file with the Deck. Reorder the cards in the deck.

  Scenario: SE_RO_01 No changes.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_02 The imported file is newer.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_03 The deck is newer.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 1 | Sample answer 1 | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_04 2 cards swapped places in the deck, 2 cards swapped places changed in the imported file.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 4 | Sample answer 4 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
    Given Add the following cards into the file:
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_05 2 cards swapped places in the deck, 2 cards swapped places changed in the imported file, 2 cards moved to the beginning in the deck.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 4 | Sample answer 4 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_06 3 cards swapped places in the deck, 3 cards swapped places in the imported file.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
      | Sample question 6 | Sample answer 6 | 0 |
      | Sample question 5 | Sample answer 5 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
      | Sample question 4 | Sample answer 4 |
      | Sample question 5 | Sample answer 5 |
      | Sample question 6 | Sample answer 6 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
      | Sample question 5 | Sample answer 5 |
      | Sample question 6 | Sample answer 6 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_07 3 cards swapped places in the deck, 3 cards swapped places in the imported file, 3 cards moved to the beginning in the deck.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
      | Sample question 6 | Sample answer 6 | 0 |
      | Sample question 5 | Sample answer 5 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 4 | Sample answer 4 |
      | Sample question 5 | Sample answer 5 |
      | Sample question 6 | Sample answer 6 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
      | Sample question 5 | Sample answer 5 |
      | Sample question 6 | Sample answer 6 |
    Then Check the deck with cards.
    Then Check the Excel file.