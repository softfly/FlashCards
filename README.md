# FlashCards
Application for learning, studying and memorizing using the so-called flashcards.

The inspiration for the application was independence from storing cards in commercial products. Instead, the cards are kept on your own memory that you always have access to. Cards can be edited in an Excel spreadsheet, such as on a laptop, which is faster and more convenient than editing on a smartphone.
- Sheets can be automatically synced with the app when you open and close the deck.
- Sheets can be stored in cloud storage.

## Features

### Decks

- D_C_01 Create a deck.
- D_D_02 Remove the deck.
- D_U_03 Rename the deck. - TODO
- D_04 Export database.
- D_05 Import database.

### Cards
- C_R_01 Study the cards.
- C_R_02 Display all cards.
  - C_U_03 Edit the card.
- C_02 Display all cards.
  - C_02_01 When no card is selected and tap on the card, show the popup menu.
  - C_02_02 When no card is selected and long pressing on the card, select the card.
  - C_02_03 When any card is selected and tap on the card, select or unselect the card.
  - C_02_04 When any card is selected and long pressing on the card, show the selected popup menu.

  - C_D_04 Delete the card by clicking in the popup menu.
    - C_U_05 Undo remove card.
  - C_D_06 Delete the card by swiping.
    - C_U_07 Undo remove card(s).
  - C_U_08 Dragging the card to another position.
  - C_09 Select (cut) / deselect the card by clicking.
  - C_10 Select / deselect the card by clicking in the context menu.
  - After selecting the card(s)
    - C_D_11 Delete selected card(s)
      - C_D_12 Undo remove selected card(s)
    - C_U_13 Paste the card(s)
    - C_14 Deselect all cards
  - C_C_15 Add a new card at the end of the deck.
  - C_C_16 Add a new card after the selected card.
  - C_FS_17 Show / hide recently synced cards.
- C_U_18 Edit the card.

### File Sync

- FS_I Import the Excel file as a new deck.
- FS_S Synchronize the deck with an Excel file.
- FS_E Export the deck to a new Excel file.
- FS_A Automatically sync with Excel file when deck is opened or closed.
