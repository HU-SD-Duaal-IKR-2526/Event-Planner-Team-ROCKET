Feature: Profiel bekijken (USR-01)
  Als gebruiker wil ik mijn eigen profiel kunnen bekijken
  zodat ik mijn geregistreerde gegevens kan inzien.

  Scenario: Eigen profiel opvragen
    Given ik ben ingelogd als gebruiker met id 42
    When ik een GET-verzoek stuur naar /users/42
    Then ontvang ik mijn profielgegevens inclusief gebruikersnaam en rol
    And staat het wachtwoord niet in de response

  Scenario: Profiel van andere gebruiker opvragen zonder rechten
    Given ik ben ingelogd als USER met id 42
    When ik een GET-verzoek stuur naar /users/99
    Then ontvang ik een 403 Forbidden foutmelding

Feature: Profiel bewerken (USR-02)
  Als gebruiker wil ik mijn profielgegevens kunnen bewerken
  zodat mijn profiel up-to-date blijft.

  Scenario: Naam succesvol bijwerken
    Given ik ben ingelogd als gebruiker met id 42
    When ik een PUT-verzoek stuur naar /users/42 met een nieuwe naam
    Then worden de bijgewerkte gegevens opgeslagen
    And geeft de response de nieuwe profielinformatie terug

  Scenario: Ander profiel bewerken zonder rechten
    Given ik ben ingelogd als USER
    When ik een PUT-verzoek stuur naar /users/99
    Then ontvang ik een 403 Forbidden foutmelding

Feature: Profielfoto uploaden (USR-03)
  Als gebruiker wil ik een profielfoto kunnen uploaden
  zodat andere gebruikers mij kunnen herkennen.

  Scenario: Geldige foto uploaden
    Given ik ben ingelogd als USER
    When ik een POST-verzoek stuur naar /users/42/avatar met een PNG-bestand van 2 MB
    Then wordt de foto opgeslagen en gekoppeld aan mijn profiel

  Scenario: Bestand te groot
    Given ik ben ingelogd als USER
    When ik een foto probeer te uploaden van 10 MB
    Then ontvang ik een 413 Payload Too Large foutmelding

  Scenario: Ongeldig bestandsformaat
    Given ik ben ingelogd als USER
    When ik een PDF-bestand probeer te uploaden als profielfoto
    Then ontvang ik een 415 Unsupported Media Type foutmelding

Feature: Notificatie-instellingen aanpassen (USR-04)
  Als gebruiker wil ik mijn notificatie-instellingen kunnen aanpassen
  zodat ik alleen relevante meldingen ontvang.

  Scenario: E-mailnotificaties uitschakelen
    Given ik ben ingelogd als USER
    When ik een PUT-verzoek stuur naar /users/42/notifications met e-mail ingesteld op false
    Then worden mijn notificatie-instellingen bijgewerkt
    And ontvang ik geen e-mailnotificaties meer

  Scenario: Instellingen opvragen
    Given ik ben ingelogd als USER
    When ik een GET-verzoek stuur naar /users/42/notifications
    Then ontvang ik mijn huidige notificatie-instellingen per categorie

Feature: Alle gebruikers opvragen (USR-05)
  Als ADM wil ik alle gebruikers kunnen opvragen
  zodat ik overzicht heb over de gebruikersbase.

  Scenario: Gebruikerslijst opvragen als ADM
    Given ik ben ingelogd als ADM
    When ik een GET-verzoek stuur naar /users
    Then ontvang ik een lijst van alle gebruikers gesorteerd op aanmaakdatum

  Scenario: Gebruikerslijst opvragen als USER
    Given ik ben ingelogd als USER
    When ik een GET-verzoek stuur naar /users
    Then ontvang ik een 403 Forbidden foutmelding

Feature: Gebruiker verwijderen (USR-06)
  Als ADM wil ik een gebruiker kunnen verwijderen
  zodat inactieve of problematische accounts worden opgeruimd.

  Scenario: Gebruiker succesvol verwijderen
    Given ik ben ingelogd als ADM
    And er bestaat een gebruiker met id 15
    When ik een DELETE-verzoek stuur naar /users/15
    Then wordt de gebruiker verwijderd
    And ontvang ik een 204 No Content response

  Scenario: Niet-bestaande gebruiker verwijderen
    Given ik ben ingelogd als ADM
    When ik een DELETE-verzoek stuur naar /users/9999
    Then ontvang ik een 404 Not Found foutmelding
