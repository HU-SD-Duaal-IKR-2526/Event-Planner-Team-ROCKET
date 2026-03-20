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
