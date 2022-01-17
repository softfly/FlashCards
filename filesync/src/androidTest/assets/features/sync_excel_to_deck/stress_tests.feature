Feature: Sync the Excel file with the Deck. Find similar cards. Stress tests.


  @disabled
  Scenario Outline: ST_01 No changes <repeat> cards.
    Given Generate cards <repeat> times into the deck:
      | Sample question {i} | Sample answer {i} | 0 |
    Given Generate cards <repeat> times into the file:
      | Sample question {i} | Sample answer {i} |
    When Synchronize the Excel file with the deck.
    Then <repeat> cards in the imported file.
    Then The expected deck with cards:
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    Then Check the deck with cards.
    Then Check the Excel file.
    Examples:
      | repeat |
      | 2000   |


  @disabled
  Scenario Outline: ST_02 Similar <repeat> cards.
    Given Generate cards <repeat> times into the deck:
      | Sample question deck {i}  | Sample answer deck {i}  | 0 |
    Given Generate cards <repeat> times into the file:
      | Sample question excel {i} | Sample answer excel {i} |
    When Synchronize the Excel file with the deck.
    Then <repeat> cards in the deck.
    Then <repeat> cards in the imported file.
    Then The expected deck with cards:
      | Sample question excel 1    | Sample answer excel 1      |
      | Sample question excel 2    | Sample answer excel 2      |
      | Sample question excel 3    | Sample answer excel 3      |
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
      | Common card   1  | Common card   1  |
      | Deck Deck 1   1  | Deck Deck 1   1  |
      | Deck Deck 2   1  | Deck Deck 2   1  |
      | Deck Deck 3   1  | Deck Deck 3   1  |
      | Excel Excel 1 1  | Excel Excel 1 1  |
      | Excel Excel 2 1  | Excel Excel 2 1  |
      | Excel Excel 3 1  | Excel Excel 3 1  |
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
      | Swap places 1       | Swap places 1     | 2 |
      | Swap places 2       | Swap places 2     | 2 |
    Given Generate cards <repeat> times into the deck:
      | Sample question {i} | Sample answer {i} | 0 |
    Given Add the following cards into the file:
      | Swap places 2       | Swap places 2     |
      | Swap places 1       | Swap places 1     |
    Given Generate cards <repeat> times into the file:
      | Sample question {i} | Sample answer {i} |
    When Synchronize the Excel file with the deck.
    Then <expected> cards in the deck.
    Then <expected> cards in the imported file.
    Examples:
      | repeat | expected |
      | 1000   | 1002     |