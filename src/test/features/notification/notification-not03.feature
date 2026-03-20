Feature: Notificatie markeren als gelezen (NOT-03)
  Als gebruiker wil ik notificaties kunnen markeren als gelezen
  zodat mijn berichtencentrum overzichtelijk blijft.

  Scenario: Notificatie succesvol markeren als gelezen
    Given ik heb een ongelezen notificatie met id 9
    When ik een PUT-verzoek stuur naar /notifications/9/read
    Then krijgt de notificatie de status "gelezen"
    And wordt de teller van ongelezen notificaties verlaagd

  Scenario: Alle notificaties in één keer markeren als gelezen
    Given ik heb 5 ongelezen notificaties
    When ik een PUT-verzoek stuur naar /notifications/read-all
    Then worden alle notificaties als gelezen gemarkeerd
    And is de teller van ongelezen notificaties 0
