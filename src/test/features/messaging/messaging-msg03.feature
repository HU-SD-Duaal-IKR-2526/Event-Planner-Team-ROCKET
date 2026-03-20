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
