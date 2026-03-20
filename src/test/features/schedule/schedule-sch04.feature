Feature: Parallelle activiteiten inzien (SCH-04)
  Als gebruiker wil ik parallelle activiteiten kunnen inzien
  zodat ik een weloverwogen keuze kan maken welke sessie ik bijwoon.

  Scenario: Parallelle sessies opvragen
    Given event 5 heeft twee sessies tegelijk van 10:00 tot 11:00
    When ik een GET-verzoek stuur naar /events/5/schedule
    Then worden de twee parallelle sessies naast elkaar weergegeven in de tijdlijn

  Scenario: Geen parallelle sessies aanwezig
    Given event 5 heeft alleen opeenvolgende sessies
    When ik het schema opvraag
    Then worden alle sessies chronologisch weergegeven zonder parallelle weergave
