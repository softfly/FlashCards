# FlashCards
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
    - C_D_04 Delete the card by clicking in the context menu.
        - C_U_06 Undo remove card.
    - C_D_05 Delete the card by swiping.
        - C_U_06 Undo remove card.
    - C_U_07 Dragging the card to another position.
    - C_08 Select (cut) / deselect the card by clicking.
    - C_09 Select / deselect the card by clicking in the context menu.
    - After selecting the card(s)
        - C_D_10 Delete selected card(s)
            - C_D_11 Undo remove selected card(s)
        - C_U_12 Paste the card(s)
        - C_13 Deselect all cards
    - C_C_14 Add a new card at the end of the deck.
    - C_C_15 Add a new card after the selected card.
    - C_FS_16 Show / hide recently synced cards.

### File Sync

- FS_I Import the Excel file as a new deck.
- FS_S Synchronize the deck with an Excel file.
- FS_E Export the deck to a new Excel file.
- FS_A Automatically sync with Excel file when deck is opened or closed.