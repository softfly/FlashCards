Feature: Sync the Excel file with the Deck. Add new cards and reorder.


  Scenario: SE_RO_AD_D_01 The order from the file. A new card was added from the deck at the beginning.
    Given Add the following cards into the deck:
      | New card 1        | New card 1      | 2 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | New card 1        | New card 1      |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_D_02 The order from the file. 2 new cards were added from the deck at the beginning.
    Given Add the following cards into the deck:
      | New card 1        | New card 1      | 2 |
      | New card 2        | New card 2      | 2 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_D_03 The order from the file. A new card was added from the deck in the middle.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | New card 1        | New card 1      | 2 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_D_04 The order from the file. 2 new cards were added from the deck in the middle.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | New card 1        | New card 1      | 2 |
      | New card 2        | New card 2      | 2 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_D_05 The order from the file. A new card was added from the deck at the end.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
      | New card 1        | New card 1      | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_D_06 The order from the file. 2 new cards were added from the deck in the end.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
      | New card 1        | New card 1      | 2 |
      | New card 2        | New card 2      | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_F_01 The order from the deck. A new card was added from the deck at the beginning.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
    Given Add the following cards into the file:
      | New card 1        | New card 1      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | New card 1        | New card 1      |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_AD_F_02 The order from the deck. 2 new cards were added from the deck at the beginning.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
    Given Add the following cards into the file:
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_F_03 The order from the deck. A new card was added from the deck in the middle.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_F_04 The order from the deck. 2 new cards were added from the deck in the middle.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 1 | Sample answer 1 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_F_05 The order from the deck. A new card was added from the deck at the end.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |
    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_F_06 The order from the deck. 2 new cards were added from the deck in the end.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 2 |
      | Sample question 2 | Sample answer 2 | 2 |
      | Sample question 3 | Sample answer 3 | 2 |

    Given Add the following cards into the file:
      | Sample question 3 | Sample answer 3 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_01 The order from the file. 2 new cards were added at the beginning.
    A new card from the deck was added.
    A new card from the imported file was added.
    Given Add the following cards into the deck:
      | Deck 1            | Deck 1          | 2 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | File 1            | File 1          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | File 1            | File 1          |
      | Deck 1            | Deck 1          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.



  Scenario: SE_RO_AD_02 The order from the file. 4 new cards were added at the beginning.
    At the beginning, 2 new card from the deck were added.
    At the beginning, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Deck 1            | Deck 1          | 2 |
      | Deck 2            | Deck 2          | 2 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Deck 1            | Deck 1          |
      | Deck 2            | Deck 2          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_03 The order from the file. 2 new cards was added in the middle.
    In the middle, a new card from the deck was added.
    In the middle, a new card from the imported file was added.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Deck 1            | Deck 1          | 2 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | File 1            | File 1          |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Deck 1            | Deck 1          |
      | File 1            | File 1          |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_04 The order from the file. 4 new cards were added in the middle.
    In the middle, 2 new card from the deck were added.
    In the middle, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Deck 1            | Deck 1          | 2 |
      | Deck 2            | Deck 2          | 2 |
      | Sample question 1 | Sample answer 1 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Deck 1            | Deck 1          |
      | Deck 2            | Deck 2          |
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_05 The order from the file. 2 new card were added at the end.
    At the end, a new card from the deck was added.
    At the end, a new card from the imported file was added.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
      | Deck 1            | Deck 1          | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | File 1            | File 1          |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Deck 1            | Deck 1          |
      | File 1            | File 1          |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_06 The order from the file. 4 new cards were added in the end.
    At the end, 2 new card from the deck were added.
    At the end, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
      | Deck 1            | Deck 1          | 2 |
      | Deck 2            | Deck 2          | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Deck 1            | Deck 1          |
      | Deck 2            | Deck 2          |
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.

  Scenario: SE_RO_AD_07 The order from the file. 2 new card were added at the end.
  At the end, a new card from the deck was added.
  At the end, a new card from the imported file was added.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
      | Deck 1            | Deck 1          | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | File 1            | File 1          |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Deck 1            | Deck 1          |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | File 1            | File 1          |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_RO_AD_08 The order from the file. 4 new cards were added in the end.
  At the end, 2 new card from the deck were added.
  At the end, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 1 | Sample answer 1 | 0 |
      | Deck 1            | Deck 1          | 2 |
      | Deck 2            | Deck 2          | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | File 1            | File 1          |
      | File 2            | File 2          |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Deck 1            | Deck 1          |
      | Deck 2            | Deck 2          |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | File 1            | File 1          |
      | File 2            | File 2          |
    Then Check the deck with cards.
    Then Check the Excel file.

