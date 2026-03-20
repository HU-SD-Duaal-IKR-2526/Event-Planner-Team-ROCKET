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
