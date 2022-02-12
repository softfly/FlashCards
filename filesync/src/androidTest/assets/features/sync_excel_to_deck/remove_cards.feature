Feature: Sync the Excel file with the Deck. Remove cards.


  Scenario: SE_RE_D_01 Remove all cards in the deck.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | -1 |
      | Sample Term 2 | Sample Definition 2 | -1 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
    When Synchronize the Excel file with the deck.
    Then 0 cards in the imported file.


  Scenario: SE_RE_D_02 Remove cards in the deck file. Do not delete the same card if it has been added to the same file twice.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 |  0 |
      | Sample Term 2 | Sample Definition 2 | -1 |
      | Sample Term 3 | Sample Definition 3 | -1 |
      | Sample Term 4 | Sample Definition 4 |  0 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    When Synchronize the Excel file with the deck.
    Then 2 cards in the imported file.
    Given Create a new file.
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    When Synchronize the Excel file with the deck.
    Then 4 cards in the imported file.


  Scenario: SE_RE_D_03 Remove cards in the deck file. Delete from each synced file.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 |  0 |
      | Sample Term 2 | Sample Definition 2 | -1 |
      | Sample Term 3 | Sample Definition 3 | -1 |
      | Sample Term 4 | Sample Definition 4 |  0 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    When Synchronize the Excel file with the deck.
    Then 2 cards in the imported file.
    Given Create a new B file.
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 4 | Sample Definition 4 |
    When Synchronize the Excel file with the deck.
    Then 2 cards in the imported file.