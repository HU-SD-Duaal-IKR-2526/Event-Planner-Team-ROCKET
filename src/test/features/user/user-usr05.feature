Feature: Alle gebruikers opvragen (USR-05)
  Als ADM wil ik alle gebruikers kunnen opvragen
  zodat ik overzicht heb over de gebruikersbase.

  Scenario: Gebruikerslijst opvragen als ADM
    Given ik ben ingelogd als ADM
    When ik een GET-verzoek stuur naar /users
    Then ontvang ik een lijst van alle gebruikers gesorteerd op aanmaakdatum

  Scenario: Gebruikerslijst opvragen als USER
    Given ik ben ingelogd als gebruiker
    When ik een GET-verzoek stuur naar /users
    Then ontvang ik een 403 Forbidden foutmelding
