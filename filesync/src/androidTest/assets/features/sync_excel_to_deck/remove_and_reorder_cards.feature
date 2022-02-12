Feature: Sync the Excel file with the Deck. Delete cards and reorder.


  Scenario: SE_RO_DE_D_01 The order from the file. The card was removed by the deck at the beginning.
    Given Add the following cards into the deck:
      | Delete card 1 | Delete card 1       | -1 |
      | Sample Term 3 | Sample Definition 3 |  0 |
      | Sample Term 2 | Sample Definition 2 |  0 |
      | Sample Term 1 | Sample Definition 1 |  0 |
    Given Add the following cards into the file:
      | Delete card 1 | Delete card 1       |
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



  Scenario: SE_RO_DE_D_02 The order from the file. The card was removed by the deck in the middle.
    Given Add the following cards into the deck:
      | Sample Term 3 | Sample Definition 3 |  0 |
      | Sample Term 2 | Sample Definition 2 |  0 |
      | Delete card 1 | Delete card 1       | -1 |
      | Sample Term 1 | Sample Definition 1 |  0 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Delete card 1 | Delete card 1       |
      | Sample Term 3 | Sample Definition 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_DE_F_01 The order from the deck. The card was removed by the file at the beginning.
    Given Add the following cards into the deck:
      | Delete card 1 | Delete card 1       | 0 |
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 3 | Sample Definition 3 | 2 |
    Given Add the following cards into the file:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_RE_F_02 The order from the deck. The card was removed by the file in the middle.
    Given Add the following cards into the deck:
      | Sample Term 1 | Sample Definition 1 | 2 |
      | Delete card 1 | Delete card 1       | 0 |
      | Sample Term 2 | Sample Definition 2 | 2 |
      | Sample Term 3 | Sample Definition 3 | 2 |
    Given Add the following cards into the file:
      | Sample Term 3 | Sample Definition 3 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 1 | Sample Definition 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_DE_01 The order from the file. 2 cards were removed at the beginning.
    The card was removed by the deck.
    The card was removed by the file.
    Given Add the following cards into the deck:
      | Delete deck 1 | Delete deck 1       |  0 |
      | Delete file 1 | Delete file 1       | -1 |
      | Sample Term 3 | Sample Definition 3 |  0 |
      | Sample Term 2 | Sample Definition 2 |  0 |
      | Sample Term 1 | Sample Definition 1 |  0 |
    Given Add the following cards into the file:
      | Delete file 1 | Delete file 1       |
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



  Scenario: SE_RO_DE_02 The order from the file. 2 cards were removed in the middle.
    In the middle, the card was removed by the deck.
    In the middle, the card was removed by the file.
    Given Add the following cards into the deck:
      | Sample Term 3 | Sample Definition 3 |  0 |
      | Sample Term 2 | Sample Definition 2 |  0 |
      | Delete deck 1 | Delete deck 1       | -1 |
      | Sample Term 1 | Sample Definition 1 |  0 |
    Given Add the following cards into the file:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Delete file 1 | Delete file 1       |
      | Sample Term 3 | Sample Definition 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then Check the Excel file.