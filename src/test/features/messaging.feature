Feature: Bericht sturen (MSG-01)
  Als gebruiker wil ik een bericht kunnen sturen naar een andere deelnemer
  zodat ik contact kan opnemen zonder externe communicatiekanalen.

  Scenario: Bericht sturen naar mededeelnemer
    Given ik ben ingelogd als USER en deelnemer van event 5
    And gebruiker "lisa" is ook deelnemer van event 5
    When ik een POST-verzoek stuur naar /messages met ontvanger "lisa" en een berichttekst
    Then wordt het bericht opgeslagen en afgeleverd bij "lisa"

  Scenario: Bericht sturen naar niet-deelnemer
    Given gebruiker "piet" is geen deelnemer van event 5
    When ik een bericht probeer te sturen naar "piet" in de context van event 5
    Then ontvang ik een 403 Forbidden foutmelding

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

Feature: Chatgeschiedenis bekijken (MSG-03)
  Als gebruiker wil ik mijn chatgeschiedenis kunnen bekijken
  zodat ik eerdere gesprekken kan terugvinden.

  Scenario: Gesprekken ophalen
    Given ik ben ingelogd als USER
    When ik een GET-verzoek stuur naar /messages/conversations
    Then ontvang ik een lijst van al mijn gesprekken inclusief het laatste bericht

  Scenario: Specifiek gesprek opvragen
    Given ik heb een gesprek met gebruiker "lisa"
    When ik een GET-verzoek stuur naar /messages/conversations/lisa
    Then ontvang ik de volledige berichtenhistorie van dat gesprek chronologisch gesorteerd

Feature: Bericht verwijderen (MSG-04)
  Als OWNER of ADM wil ik ongepaste berichten kunnen verwijderen
  zodat de communicatie binnen het platform veilig en respectvol blijft.

  Scenario: Bericht verwijderen als ADM
    Given ik ben ingelogd als ADM
    And er bestaat een bericht met id 12
    When ik een DELETE-verzoek stuur naar /messages/12
    Then wordt het bericht verwijderd en is niet meer zichtbaar voor beide partijen
    And wordt de verwijdering gelogd in het Audit-domein

  Scenario: Eigen bericht verwijderen als USER
    Given ik ben ingelogd als USER en heb bericht 12 verstuurd
    When ik een DELETE-verzoek stuur naar /messages/12
    Then wordt het bericht verwijderd

  Scenario: Bericht van iemand anders verwijderen als USER
    Given ik ben ingelogd als USER en bericht 12 is van een andere gebruiker
    When ik een DELETE-verzoek stuur naar /messages/12
    Then ontvang ik een 403 Forbidden foutmelding
