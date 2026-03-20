Feature: JWT-token valideren (AUTH-05)
  Als systeem wil ik bij elke request het JWT-token valideren
  zodat alleen geauthenticeerde gebruikers toegang krijgen tot beveiligde endpoints.

  Scenario: Geldig token
    Given een gebruiker stuurt een request met een geldig JWT-token
    When het systeem het token valideert
    Then wordt de request doorgelaten op basis van de rol in het token

  Scenario: Verlopen token
    Given een gebruiker stuurt een request met een verlopen JWT-token
    When het systeem het token valideert
    Then wordt de request geweigerd met een 401 Unauthorized foutmelding
