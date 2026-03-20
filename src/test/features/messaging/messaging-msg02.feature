Feature: Bericht lezen (MSG-02)
  Als gebruiker wil ik mijn berichten kunnen lezen
  zodat ik op de hoogte blijf van inkomende communicatie.

  Scenario: Ongelezen berichten ophalen
    Given ik ben ingelogd als USER en heb 2 ongelezen berichten
    When ik een GET-verzoek stuur naar /messages/inbox
    Then ontvang ik alle berichten gesorteerd op tijdstip
    And zijn ongelezen berichten duidelijk gemarkeerd

  Scenario: Bericht markeren als gelezen
    Given ik heb een bericht met id 12
    When ik een PUT-verzoek stuur naar /messages/12/read
    Then krijgt het bericht de status "gelezen"
