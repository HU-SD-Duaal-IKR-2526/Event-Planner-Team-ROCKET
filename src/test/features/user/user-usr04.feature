Feature: Notificatie-instellingen aanpassen (USR-04)
  Als gebruiker wil ik mijn notificatie-instellingen kunnen aanpassen
  zodat ik alleen relevante meldingen ontvang.

  Scenario: E-mailnotificaties uitschakelen
    Given ik ben ingelogd als gebruiker
    When ik een PUT-verzoek stuur naar /users/42/notifications met e-mail ingesteld op false
    Then worden mijn notificatie-instellingen bijgewerkt
    And ontvang ik geen e-mailnotificaties meer

  Scenario: Instellingen opvragen
    Given ik ben ingelogd als gebruiker
    When ik een GET-verzoek stuur naar /users/42/notifications
    Then ontvang ik mijn huidige notificatie-instellingen per categorie
