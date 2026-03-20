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
