Feature: Import Excel file to a new Deck.
  Import Excel file to Deck.

  Scenario: IE_01_01 The spreadsheet does not include a header. 2 columns. Empty lines.
    Given The following Excel file:
      | |                   | |                 |
      | | Sample question 1 | | Sample answer 1 |
      | |                   | |                 |
      | | Sample question 2 | | Sample answer 2 |
      | |                   | |                 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |



  Scenario: IE_01_02 The spreadsheet does not include a header. 2 columns. Empty columns.
    Given The following Excel file:
      | Sample question 1 |                 |
      |                   | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 |                 |
      |                   | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |



  Scenario: IE_01_03 The spreadsheet does not include a header. 2 columns. Empty columns.
    Given The following Excel file:
      |                   | Sample answer 1 |
      | Sample question 2 |                 |
      | Sample question 3 | Sample answer 3 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      |                   | Sample answer 1 |
      | Sample question 2 |                 |
      | Sample question 3 | Sample answer 3 |



  Scenario: IE_02 The spreadsheet does not include a header. Only questions.
    Given The following Excel file:
      | Sample question 1 |
      | Sample question 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | |
      | Sample question 2 | |



  Scenario: IE_03_01 The spreadsheet includes the header with the Question and Answer.
    Given The following Excel file:
      | AnsWers         | QuesTIons         |
      | Sample answer 1 | Sample question 1 |
      | Sample answer 2 | Sample question 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |



  Scenario: IE_03_02 The spreadsheet includes the header in the middle with the Question and Answer.
    Given The following Excel file:
      | Sample question 1 | Sample answer 1 |
      | Question          | Answer          |
      | Sample question 2 | Sample answer 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | Sample answer 1 |
      | Question          | Answer          |
      | Sample question 2 | Sample answer 2 |



  Scenario: IE_04 The spreadsheet includes the header with the Question.
    Given The following Excel file:
      |                   |
      | Question          |
      | Sample question 1 |
      | Sample question 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | |
      | Sample question 2 | |



  Scenario: IE_05 The spreadsheet includes the header with the Answer.
    Given The following Excel file:
      |                 |
      | Answer          |
      | Sample answer 1 |
      | Sample answer 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | | Sample answer 1 |
      | | Sample answer 2 |



  Scenario: IE_06 The spreadsheet has 2 columns, but only the header for the Question.
    Given The following Excel file:
      |                 | QuesTIon          |
      | Sample answer 1 | Sample question 1 |
      | Sample answer 2 | Sample question 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |



  Scenario: IE_07 The spreadsheet has 2 columns, but only the header for the Answer.
    Given The following Excel file:
      | AnsWer          |                   |
      | Sample answer 1 | Sample question 1 |
      | Sample answer 2 | Sample question 2 |
      | Sample answer 3 | Sample question 3 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample question 1 | Sample answer 1 |
      | Sample question 2 | Sample answer 2 |
      | Sample question 3 | Sample answer 3 |



#  Scenario Outline: Stress test. Import a lot of cards.
#    Given Generate <repeat> rows.
#    Given The following Excel file:
#      | Sample answer {$i} | Sample question {$i} |
#    When Import the Excel file into the deck.
#
#    Examples:
#      | repeat  |
#      | 100000 |
