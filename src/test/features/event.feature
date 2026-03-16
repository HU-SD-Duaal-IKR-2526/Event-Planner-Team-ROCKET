Feature: Event aanmaken (EVT-01)
  Als OWNER wil ik een nieuw event aanmaken
  zodat gebruikers zich kunnen aanmelden.

  Scenario: Event succesvol aanmaken
    Given ik ben ingelogd als OWNER
    When ik een POST-verzoek stuur naar /events met naam "TechTalk", datum "2026-06-01" en locatie "Brussel"
    Then wordt het event aangemaakt met status PLANNED
    And ontvang ik de event-details inclusief het gegenereerde id

  Scenario: Event aanmaken zonder verplichte velden
    Given ik ben ingelogd als OWNER
    When ik een POST-verzoek stuur naar /events zonder een datum
    Then ontvang ik een 400 Bad Request foutmelding

Feature: Event bewerken (EVT-02)
  Als OWNER wil ik een bestaand event kunnen bewerken
  zodat ik wijzigingen in datum, locatie of beschrijving kan doorvoeren.

  Scenario: Event succesvol bewerken
    Given ik ben ingelogd als OWNER van event 7
    When ik een PUT-verzoek stuur naar /events/7 met een nieuwe locatie "Amsterdam"
    Then worden de wijzigingen opgeslagen
    And geeft de response de bijgewerkte event-details terug

  Scenario: Event bewerken van een ander OWNER
    Given ik ben ingelogd als OWNER maar niet de eigenaar van event 7
    When ik een PUT-verzoek stuur naar /events/7
    Then ontvang ik een 403 Forbidden foutmelding

Feature: Event bekijken (EVT-03)
  Als gebruiker wil ik alle beschikbare events kunnen bekijken
  zodat ik kan beslissen voor welk event ik mij wil aanmelden.

  Scenario: Lijst van geplande events opvragen
    Given ik ben ingelogd als USER
    When ik een GET-verzoek stuur naar /events
    Then ontvang ik een lijst van alle events met status PLANNED

  Scenario: Lege lijst wanneer er geen events zijn
    Given er zijn geen events aangemaakt
    When ik een GET-verzoek stuur naar /events
    Then ontvang ik een lege lijst met statuscode 200

Feature: Event detail opvragen (EVT-04)
  Als gebruiker wil ik de details van een specifiek event kunnen opvragen
  zodat ik volledig geïnformeerd ben.

  Scenario: Event detail succesvol opvragen
    Given er bestaat een event met id 5
    When ik een GET-verzoek stuur naar /events/5
    Then ontvang ik de volledige event-details inclusief naam, datum, locatie en beschrijving

  Scenario: Niet-bestaand event opvragen
    Given er bestaat geen event met id 9999
    When ik een GET-verzoek stuur naar /events/9999
    Then ontvang ik een 404 Not Found foutmelding

Feature: Event annuleren (EVT-05)
  Als OWNER wil ik een event kunnen annuleren
  zodat deelnemers tijdig worden geïnformeerd.

  Scenario: Event succesvol annuleren
    Given ik ben ingelogd als OWNER van event 7
    And event 7 heeft status PLANNED
    When ik een PUT-verzoek stuur naar /events/cancel/7
    Then krijgt het event status CANCELLED
    And ontvangen alle aangemelde deelnemers een notificatie

  Scenario: Reeds geannuleerd event annuleren
    Given event 7 heeft al status CANCELLED
    When ik een PUT-verzoek stuur naar /events/cancel/7
    Then ontvang ik een 409 Conflict foutmelding

Feature: Event verwijderen (EVT-06)
  Als ADM wil ik een event kunnen verwijderen
  zodat verouderde of ongepaste events worden opgeruimd.

  Scenario: Event succesvol verwijderen
    Given ik ben ingelogd als ADM
    And er bestaat een event met id 7
    When ik een DELETE-verzoek stuur naar /events/7
    Then wordt het event inclusief alle gekoppelde registraties verwijderd
    And ontvang ik een 204 No Content response

  Scenario: Niet-bestaand event verwijderen
    Given ik ben ingelogd als ADM
    When ik een DELETE-verzoek stuur naar /events/9999
    Then ontvang ik een 404 Not Found foutmelding

Feature: Automatische statusupdate (EVT-07)
  Als systeem wil ik de evenementstatus automatisch bijwerken op basis van de datum
  zodat voltooide events correct worden gemarkeerd.

  Scenario: Event is verstreken
    Given er bestaat een event met id 3 met datum "2026-01-01" en status PLANNED
    When de autostatusupdate wordt uitgevoerd via PUT /events/autostatusupdate/3
    Then krijgt het event status COMPLETED

  Scenario: Toekomstig event blijft PLANNED
    Given er bestaat een event met datum "2027-01-01" en status PLANNED
    When de autostatusupdate wordt uitgevoerd
    Then blijft de status PLANNED ongewijzigd
