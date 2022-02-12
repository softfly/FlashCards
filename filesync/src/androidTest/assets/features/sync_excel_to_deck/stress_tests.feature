Feature: Sync the Excel file with the Deck. Find similar cards. Stress tests.


  @disabled
  Scenario Outline: ST_01 No changes <repeat> cards.
    Given Generate cards <repeat> times into the deck:
      | Sample Term {i} | Sample Definition {i} | 0 |
    Given Generate cards <repeat> times into the file:
      | Sample Term {i} | Sample Definition {i} |
    When Synchronize the Excel file with the deck.
    Then <repeat> cards in the imported file.
    Then The expected deck with cards:
      | Sample Term 1 | Sample Definition 1 |
      | Sample Term 2 | Sample Definition 2 |
      | Sample Term 3 | Sample Definition 3 |
    Then Check the deck with cards.
    Then Check the Excel file.
    Examples:
      | repeat |
      | 2000   |


  @disabled
  Scenario Outline: ST_02 Similar <repeat> cards.
    Given Generate cards <repeat> times into the deck:
      | Sample Term deck {i}  | Sample Definition deck {i}  | 0 |
    Given Generate cards <repeat> times into the file:
      | Sample Term excel {i} | Sample Definition excel {i} |
    When Synchronize the Excel file with the deck.
    Then <repeat> cards in the deck.
    Then <repeat> cards in the imported file.
    Then The expected deck with cards:
      | Sample Term excel 1 | Sample Definition excel 1      |
      | Sample Term excel 2 | Sample Definition excel 2      |
      | Sample Term excel 3 | Sample Definition excel 3      |
    Then Check the deck with cards.
    Then Check the Excel file.
    Examples:
      | repeat |
      | 2000   |


  @disabled
  Scenario Outline: ST_03 Merging new cards.
    Given Generate cards <repeat> times into the deck:
      | Common card   {i} | Common card   {i} | 0 |
      | Deck Deck 1   {i} | Deck Deck 1   {i} | 2 |
      | Deck Deck 2   {i} | Deck Deck 2   {i} | 2 |
      | Deck Deck 3   {i} | Deck Deck 3   {i} | 2 |
    Given Generate cards <repeat> times into the file:
      | Common card   {i} | Common card   {i} |
      | Excel Excel 1 {i} | Excel Excel 1 {i} |
      | Excel Excel 2 {i} | Excel Excel 2 {i} |
      | Excel Excel 3 {i} | Excel Excel 3 {i} |
    When Synchronize the Excel file with the deck.
    Then <expected> cards in the deck.
    Then <expected> cards in the imported file.
    Then The expected deck with cards:
      | Common card   1 | Common card   1  |
      | Deck Deck 1   1 | Deck Deck 1   1  |
      | Deck Deck 2   1 | Deck Deck 2   1  |
      | Deck Deck 3   1 | Deck Deck 3   1  |
      | Excel Excel 1 1 | Excel Excel 1 1  |
      | Excel Excel 2 1 | Excel Excel 2 1  |
      | Excel Excel 3 1 | Excel Excel 3 1  |
    Then Check the deck with cards.
    Then Check the Excel file.
    Examples:
      | repeat | expected |
      | 200    | 1400     |


  @disabled
  Scenario Outline: Check overwriting only changed rows in the exported file.
    If only all records are saved to the file:
    UpdateExcelFile=00:00:00.325
    If only changed records are saved to the file:
    UpdateExcelFile=00:00:00.095
    Given Add the following cards into the deck:
      | Swap places 1   | Swap places 1     | 2 |
      | Swap places 2   | Swap places 2     | 2 |
    Given Generate cards <repeat> times into the deck:
      | Sample Term {i} | Sample Definition {i} | 0 |
    Given Add the following cards into the file:
      | Swap places 2   | Swap places 2     |
      | Swap places 1   | Swap places 1     |
    Given Generate cards <repeat> times into the file:
      | Sample Term {i} | Sample Definition {i} |
    When Synchronize the Excel file with the deck.
    Then <expected> cards in the deck.
    Then <expected> cards in the imported file.
    Examples:
      | repeat | expected |
      | 1000   | 1002     |