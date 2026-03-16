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

Feature: Afmelden voor event (REG-02)
  Als gebruiker wil ik mij kunnen afmelden voor een event
  zodat er ruimte vrijkomt voor andere deelnemers.

  Scenario: Succesvolle afmelding
    Given ik ben ingelogd als USER en aangemeld voor event 5
    When ik een DELETE-verzoek stuur naar /registrations/5
    Then wordt mijn aanmelding verwijderd
    And wordt de beschikbare capaciteit van het event verhoogd

  Scenario: Afmelden voor event waarbij ik niet aangemeld ben
    Given ik ben niet aangemeld voor event 5
    When ik een DELETE-verzoek stuur naar /registrations/5
    Then ontvang ik een 404 Not Found foutmelding

Feature: RSVP-status bekijken (REG-03)
  Als gebruiker wil ik mijn RSVP-status kunnen bekijken
  zodat ik weet of mijn aanmelding succesvol is verwerkt.

  Scenario: RSVP-status opvragen
    Given ik ben ingelogd als USER en aangemeld voor event 5
    When ik een GET-verzoek stuur naar /registrations/me
    Then ontvang ik een overzicht van al mijn aanmeldingen inclusief hun status

  Scenario: Geen aanmeldingen aanwezig
    Given ik ben ingelogd als USER en heb geen aanmeldingen
    When ik een GET-verzoek stuur naar /registrations/me
    Then ontvang ik een lege lijst met statuscode 200

Feature: Deelnemerslijst opvragen (REG-04)
  Als OWNER wil ik de deelnemerslijst van mijn event kunnen opvragen
  zodat ik de aanwezigen kan beheren.

  Scenario: Lijst opvragen als OWNER
    Given ik ben ingelogd als OWNER van event 5
    When ik een GET-verzoek stuur naar /events/5/registrations
    Then ontvang ik een volledige lijst van aangemelde gebruikers

  Scenario: Lijst opvragen als gewone USER
    Given ik ben ingelogd als USER en geen OWNER van event 5
    When ik een GET-verzoek stuur naar /events/5/registrations
    Then ontvang ik een 403 Forbidden foutmelding

Feature: Check-in registreren (REG-05)
  Als OWNER wil ik deelnemers kunnen inchecken op de dag van het event
  zodat de daadwerkelijke aanwezigheid wordt bijgehouden.

  Scenario: Deelnemer succesvol inchecken
    Given ik ben ingelogd als OWNER van event 5
    And gebruiker "jan" is aangemeld voor event 5
    When ik een PUT-verzoek stuur naar /registrations/5/checkin voor gebruiker "jan"
    Then krijgt de registratie van "jan" status CHECKED_IN
    And wordt het tijdstip van inchecken vastgelegd

  Scenario: Inchecken van niet-aangemelde deelnemer
    Given gebruiker "piet" is niet aangemeld voor event 5
    When ik "piet" probeer in te checken voor event 5
    Then ontvang ik een 404 Not Found foutmelding
