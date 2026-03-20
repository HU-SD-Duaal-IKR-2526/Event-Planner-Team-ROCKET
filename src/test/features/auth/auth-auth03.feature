Feature: Wachtwoord resetten (AUTH-03)
  Als gebruiker wil ik mijn wachtwoord kunnen resetten via e-mail
  zodat ik weer toegang krijg als ik mijn wachtwoord ben vergeten.

  Scenario: Reset-link aanvragen
    Given ik ben een geregistreerde gebruiker met e-mailadres "jan@example.com"
    When ik een POST-verzoek stuur naar /auth/reset-password met mijn e-mailadres
    Then ontvang ik een e-mail met een reset-link
    And is de reset-link 1 uur geldig

  Scenario: Reset-link gebruiken na verlopen
    Given ik heb een reset-link ontvangen die meer dan 1 uur geleden is aangemaakt
    When ik de verlopen reset-link gebruik
    Then ontvang ik een 400 Bad Request foutmelding
