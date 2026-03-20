Feature: Automatische statusupdate (EVT-07)
  Als systeem wil ik de evenementstatus automatisch bijwerken op basis van de datum
  zodat voltooide events correct worden gemarkeerd.

  Scenario: Event is verstreken
    Given er bestaat een event met id 3 met datum "2026-01-01" en status PLANNED
    When de autostatusupdate wordt uitgevoerd via PUT /events/autostatusupdate/3
    Then krijgt het event status COMPLETED

  Scenario: Toekomstig event blijft PLANNED
