Feature: Notificatie ontvangen (NOT-01)
  Als gebruiker wil ik een notificatie ontvangen wanneer een event wordt gewijzigd of geannuleerd
  zodat ik tijdig geïnformeerd ben.

  Scenario: Notificatie bij event-annulering
    Given ik ben aangemeld voor event 5
    And een OWNER annuleert event 5
    When de annulering wordt verwerkt
    Then ontvang ik een notificatie met de melding dat event 5 is geannuleerd

  Scenario: Notificatie bij wijziging event-datum
    Given ik ben aangemeld voor event 5
    When de OWNER de datum van event 5 wijzigt
    Then ontvang ik een notificatie met de nieuwe datum
