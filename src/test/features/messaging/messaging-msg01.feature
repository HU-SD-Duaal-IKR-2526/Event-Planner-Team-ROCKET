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
