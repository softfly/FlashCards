Feature: Show success notification message

  @single
  Scenario: Show success notification message
    Given Statistical data after synchronization:
      | 0 | 0 | 0 | 0 | 0 | 0 |
      | 1 | 1 | 1 | 0 | 0 | 0 |
      | 2 | 0 | 0 | 0 | 0 | 0 |
      | 0 | 2 | 0 | 0 | 0 | 0 |
      | 0 | 0 | 2 | 0 | 0 | 0 |
      | 3 | 3 | 0 | 0 | 0 | 0 |
      | 3 | 0 | 3 | 0 | 0 | 0 |
      | 0 | 4 | 4 | 0 | 0 | 0 |
    Then The expected messages:
      | Deck: 0 updated\nFile: 0 updated |
      | Deck: 1 added and 1 updated and 1 deleted\nFile: 0 updated |
      | Deck: 2 added\nFile: 0 updated |
      | Deck: 2 updated\nFile: 0 updated |
      | Deck: 2 deleted\nFile: 0 updated |
      | Deck: 3 added and 3 updated\nFile: 0 updated |
      | Deck: 3 added and 3 deleted\nFile: 0 updated |
      | Deck: 4 updated and 4 deleted\nFile: 0 updated |