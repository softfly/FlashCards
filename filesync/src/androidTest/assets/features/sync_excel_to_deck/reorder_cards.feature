Feature: Sync the Excel file with the Deck. Reorder the cards in the deck.

  Scenario: SE_RO_01 No changes.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 0 |
      | Sample Term 2 | Sample Definition 2 | 0 |
      | Sample Term 3 | Sample Definition 3 | 0 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_02 The imported file is newer.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 0 |
      | Sample Term 2 | Sample Definition 2 | 0 |
      | Sample Term 3 | Sample Definition 3 | 0 |
    Given Add the following cards into the file:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_03 The deck is newer.
    Given Add the following cards into the deck:
      | Sample Term 3 | Sample Definition 3 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 1 | Sample Definition 1 | 2 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_04 2 cards swapped places in the deck, 2 cards swapped places changed in the imported file.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 4 | Sample Definition 4 | 0 |
      | Sample Term 3 | Sample Definition 3 | 0 |
    Given Add the following cards into the file:
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_05 2 cards swapped places in the deck, 2 cards swapped places changed in the imported file, 2 cards moved to the beginning in the deck.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 4 | Sample Definition 4 | 0 |
      | Sample Term 3 | Sample Definition 3 | 0 |
    Given Add the following cards into the file:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_06 3 cards swapped places in the deck, 3 cards swapped places in the imported file.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 3 | Sample Definition 3 | 2 |
      | Sample Term 6 | Sample Definition 6 | 0 |
      | Sample Term 5 | Sample Definition 5 | 0 |
      | Sample Term 4 | Sample Definition 4 | 0 |
    Given Add the following cards into the file:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 5 | Sample Definition 5 |
      | Sample Term 6 | Sample Definition 6 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 5 | Sample Definition 5 |
      | Sample Term 6 | Sample Definition 6 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_07 3 cards swapped places in the deck, 3 cards swapped places in the imported file, 3 cards moved to the beginning in the deck.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 3 | Sample Definition 3 | 2 |
      | Sample Term 6 | Sample Definition 6 | 0 |
      | Sample Term 5 | Sample Definition 5 | 0 |
      | Sample Term 4 | Sample Definition 4 | 0 |
    Given Add the following cards into the file:
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 5 | Sample Definition 5 |
      | Sample Term 6 | Sample Definition 6 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
      | Sample Term 5 | Sample Definition 5 |
      | Sample Term 6 | Sample Definition 6 |
    Then Check the deck with cards.
    Then Check the Excel file.