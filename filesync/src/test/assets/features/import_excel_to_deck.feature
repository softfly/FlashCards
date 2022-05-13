Feature: Import Excel file to a new Deck.
  Import Excel file to Deck.

  Scenario: IE_01_01 The spreadsheet does not include a header. 2 columns. Empty lines.
    Given The following Excel file:
      |  |               |  |                     |
      |  | Sample term 1 |  | Sample definition 1 |
      |  |               |  |                     |
      |  | Sample term 2 |  | Sample definition 2 |
      |  |               |  |                     |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 | Sample definition 1 |
      | Sample term 2 | Sample definition 2 |


  Scenario: IE_01_02 The spreadsheet does not include a header. 2 columns. Empty columns.
    Given The following Excel file:
      | Sample term 1 |                     |
      |               | Sample definition 2 |
      | Sample term 3 | Sample definition 3 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 |                     |
      |               | Sample definition 2 |
      | Sample term 3 | Sample definition 3 |


  Scenario: IE_01_03 The spreadsheet does not include a header. 2 columns. Empty columns.
    Given The following Excel file:
      |               | Sample definition 1 |
      | Sample term 2 |                     |
      | Sample term 3 | Sample definition 3 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      |               | Sample definition 1 |
      | Sample term 2 |                     |
      | Sample term 3 | Sample definition 3 |


  Scenario: IE_02 The spreadsheet does not include a header. Only terms.
    Given The following Excel file:
      | Sample term 1 |
      | Sample term 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 |  |
      | Sample term 2 |  |


  Scenario: IE_03_01 The spreadsheet includes the header with the Term and Definition.
    Given The following Excel file:
      | AnsWers             | QuesTIons     |
      | Sample definition 1 | Sample term 1 |
      | Sample definition 2 | Sample term 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 | Sample definition 1 |
      | Sample term 2 | Sample definition 2 |


  Scenario: IE_03_02 The spreadsheet includes the header in the middle with the Term and Definition.
    Given The following Excel file:
      | Sample term 1 | Sample definition 1 |
      | Term          | Definition          |
      | Sample term 2 | Sample definition 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 | Sample definition 1 |
      | Term          | Definition          |
      | Sample term 2 | Sample definition 2 |


  Scenario: IE_04 The spreadsheet includes the header with the Term.
    Given The following Excel file:
      |               |
      | Term          |
      | Sample term 1 |
      | Sample term 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 |  |
      | Sample term 2 |  |


  Scenario: IE_05 The spreadsheet includes the header with the Definition.
    Given The following Excel file:
      |                     |
      | Definition          |
      | Sample definition 1 |
      | Sample definition 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      |  | Sample definition 1 |
      |  | Sample definition 2 |


  Scenario: IE_06 The spreadsheet has 2 columns, but only the header for the Term.
    Given The following Excel file:
      |                     | QuesTIon      |
      | Sample definition 1 | Sample term 1 |
      | Sample definition 2 | Sample term 2 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 | Sample definition 1 |
      | Sample term 2 | Sample definition 2 |


  Scenario: IE_07 The spreadsheet has 2 columns, but only the header for the Definition.
    Given The following Excel file:
      | AnsWer              |               |
      | Sample definition 1 | Sample term 1 |
      | Sample definition 2 | Sample term 2 |
      | Sample definition 3 | Sample term 3 |
    When Import the Excel file into the deck.
    Then A new deck with the following cards imported.
      | Sample term 1 | Sample definition 1 |
      | Sample term 2 | Sample definition 2 |
      | Sample term 3 | Sample definition 3 |



#  Scenario Outline: Stress test. Import a lot of cards.
#    Given Generate <repeat> rows.
#    Given The following Excel file:
#      | Sample definition {$i} | Sample term {$i} |
#    When Import the Excel file into the deck.
#
#    Examples:
#      | repeat  |
#      | 100000 |
