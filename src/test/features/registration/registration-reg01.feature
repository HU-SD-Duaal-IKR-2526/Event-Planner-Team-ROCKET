Feature: Aanmelden voor event - RSVP (REG-01)
  Als gebruiker wil ik mij kunnen aanmelden voor een event
  zodat mijn aanwezigheid wordt geregistreerd.

  Scenario: Succesvolle aanmelding
    Given ik ben ingelogd als USER
    And event 5 heeft status PLANNED en de capaciteit is niet bereikt
    When ik een POST-verzoek stuur naar /registrations met event-id 5
    Then wordt mijn aanmelding opgeslagen met status AANGEMELD
    And ontvang ik een bevestigingsmail

  Scenario: Aanmelden voor een vol event
    Given event 5 heeft de maximale capaciteit bereikt
    When ik probeer mij aan te melden voor event 5
    Then ontvang ik een 409 Conflict foutmelding met de melding "Capaciteit bereikt"

  Scenario: Dubbele aanmelding
    Given ik ben al aangemeld voor event 5
    When ik nogmaals probeer mij aan te melden voor event 5
    Then ontvang ik een 409 Conflict foutmelding met de melding "Al aangemeld"
