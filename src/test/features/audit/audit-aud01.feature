Feature: Systeemacties loggen (AUD-01)
  Als systeem wil ik alle CRUD-acties op events en gebruikers automatisch loggen
  zodat er een volledige audittrail is.

  Scenario: Event aanmaken wordt gelogd
    Given een OWNER maakt een nieuw event aan
    When de POST /events request succesvol is verwerkt
    Then wordt een logentry aangemaakt met tijdstip, actor "OWNER" en actie "EVENT_CREATED"

  Scenario: Gebruiker verwijderen wordt gelogd
    Given een ADM verwijdert gebruiker met id 15
    When de DELETE /users/15 request succesvol is verwerkt
    Then wordt een logentry aangemaakt met tijdstip, actor "ADM" en actie "USER_DELETED"
