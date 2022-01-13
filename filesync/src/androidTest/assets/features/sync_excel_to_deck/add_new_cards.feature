Feature: Sync the Excel file with the Deck. Add new cards.


  Scenario: SE_AD_01 2 new cards.
    A new card from the deck was added.
    A new card from the imported file was added.
    Given Add the following cards into the deck:
      | Deck 1            | Deck 1          | 2 |
    Given Add the following cards into the file:
      | File 2            | File 2          |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | File 2            | File 2          |
      | Deck 1            | Deck 1          |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_02 2 new cards at the beginning.
    At the beginning, a new card from the deck was added.
    At the beginning, a new card from the imported file was added.
    Given Add the following cards into the deck:
      | Deck 1            | Deck 1          | 2 |
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
    Given Add the following cards into the file:
      | File 1            | File 1          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | File 1            | File 1          |
      | Deck 1            | Deck 1          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_03 4 new cards at the beginning.
    At the beginning, 2 new card from the deck were added.
    At the beginning, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Deck 1            | Deck 1          | 2 |
      | Deck 2            | Deck 2          | 2 |
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
    Given Add the following cards into the file:
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | File 1            | File 1          |
      | File 2            | File 2          |
      | Deck 1            | Deck 1          |
      | Deck 2            | Deck 2          |
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_04 2 new cards in the middle.
    In the middle, a new card from the deck was added.
    In the middle, a new card from the imported file was added.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Deck 3            | Deck 3          | 2 |
      | Sample question 2 | Sample answer 2 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | File 4            | File 4          |
      | Sample question 2 | Sample answer 2 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Deck 3            | Deck 3          |
      | File 4            | File 4          |
      | Sample question 2 | Sample answer 2 |
    Then Check the deck with cards.
    Then Check the Excel file.

  Scenario: SE_AD_05 4 new cards in the middle.
      In the middle, 2 new card from the deck were added.
      In the middle, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Deck 3            | Deck 3          | 2 |
      | Deck 4            | Deck 4          | 2 |
      | Sample question 2 | Sample answer 2 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | File 5            | File 5          |
      | File 6            | File 6          |
      | Sample question 2 | Sample answer 2 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Deck 3            | Deck 3          |
      | Deck 4            | Deck 4          |
      | File 5            | File 5          |
      | File 6            | File 6          |
      | Sample question 2 | Sample answer 2 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_06 2 new cards at the end.
    At the end, a new card from the deck was added.
    At the end, a new card from the imported file was added.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Deck 3            | Deck 3          | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | File 4            | File 4          |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Deck 3            | Deck 3          |
      | File 4            | File 4          |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_07 4 new cards at the end.
    At the end, 2 new card from the deck were added.
    At the end, 2 new card from the imported file were added.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Deck 3            | Deck 3          | 2 |
      | Deck 4            | Deck 4          | 2 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | File 5            | File 5          |
      | File 6            | File 6          |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Deck 3            | Deck 3          |
      | Deck 4            | Deck 4          |
      | File 5            | File 5          |
      | File 6            | File 6          |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_F_01 A new card was added from the imported file.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_F_02 2 new cards were added from the Excel file.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_F_03 3 new cards were added from the Excel file.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | New card 3        | New card 3      |
      | Sample question 2 | Sample answer 2 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | New card 3        | New card 3      |
      | Sample question 2 | Sample answer 2 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_D_01 A new card was added from the deck.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | New card 1        | New card 1      | 2 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_D_02 2 new cards were added from the deck.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | Sample question 2 | Sample answer 2 | 0 |
      | New card 1        | New card 1      | 2 |
      | New card 2        | New card 2      | 2 |
      | Sample question 3 | Sample answer 3 | 0 |
      | Sample question 4 | Sample answer 4 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | Sample question 3 | Sample answer 3 |
      | Sample question 4 | Sample answer 4 |
    Then Check the deck with cards.
    Then Check the Excel file.


  Scenario: SE_AD_D_03 3 new cards were added from the deck.
    Given Add the following cards into the deck:
      | Sample question 1 | Sample answer 1 | 0 |
      | New card 1        | New card 1      | 2 |
      | New card 2        | New card 2      | 2 |
      | New card 3        | New card 3      | 2 |
      | Sample question 2 | Sample answer 2 | 0 |
    Given Add the following cards into the file:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
    When Synchronize the Excel file with the deck.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | New card 1        | New card 1      |
      | New card 2        | New card 2      |
      | New card 3        | New card 3      |
      | Sample question 2 | Sample answer 2 |
    Then Check the deck with cards.
    Then Check the Excel file.
