# Schedule Service — Implementatiegids

## Wat dit is

De Schedule bounded context is een **read-geoptimaliseerd projectiemodel** (CQRS read-side) dat wordt opgebouwd uit events van andere contexten. Het beheert ook de sessie-planning van een event inclusief overlap-detectie.

**Communicatiestijl**: Open Host Service (OHS) — andere contexten bevragen Schedule via HTTP GET. Schedule zelf wordt nooit synchroon aangesproken om te schrijven; alles loopt via events.

---

## Architectuurpatroon: CQRS

```
Write-pad:  events (RabbitMQ) → ScheduleProjectionListener → Schedule aggregate → PostgreSQL
Read-pad:   HTTP GET /schedule/... → ScheduleController → PostgreSQL read-model
```

De twee paden zijn onafhankelijk. De read-side mag een fractie achterlopen (eventual consistency). Elke event-verwerking is een lokale ACID-transactie.

---

## Te implementeren stappen

### 1. Database-schema (Flyway)

**`V1__create_schedules.sql`**

- Tabel `schedules`: kolommen `id (UUID PK)`, `event_id (UUID UNIQUE NOT NULL)`, `event_title (VARCHAR)`, `event_starts_at (TIMESTAMPTZ)`, `event_ends_at (TIMESTAMPTZ)`, `headcount (int DEFAULT 0)`, `last_updated (TIMESTAMPTZ)`.
- Tabel `sessions`: kolommen `id (UUID PK)`, `schedule_id (UUID FK → schedules)`, `title`, `room_id (UUID)`, `speaker_id (UUID)`, `slot_start (TIMESTAMPTZ)`, `slot_end (TIMESTAMPTZ)`, constraint `slot_start < slot_end`.
- Index op `sessions(room_id, slot_start, slot_end)` en `sessions(speaker_id, slot_start, slot_end)` — voor overlap-queries.

**`V2__create_user_upcoming.sql`** (later)

- Tabel `user_upcoming_events`: `user_id`, `event_id`, `title`, `starts_at`, PK op `(user_id, event_id)`.
- Gevoed door `registration.reserved.v1` events.

---

### 2. Domain

**`EventRef` (Value Object)**

Cross-aggregate ID-referentie naar Event BC. Bevat alleen `eventId (UUID)`. Geen JPA-join, geen lookup naar Event service.

**`TimeSlot` (Value Object)**

Velden: `start (Instant)`, `end (Instant)`.
- Valideer `start < end` in constructor.
- Methode `overlapsWith(TimeSlot other): boolean` — logica: `this.start < other.end && other.start < this.end`.

**`Session` (Entity — onderdeel van Schedule aggregate)**

Velden: `id`, `title`, `roomId (UUID)`, `speakerId (UUID)`, `slotStart`, `slotEnd`.
- Methoden: `roomOverlapsWith(Session other)` en `speakerOverlapsWith(Session other)`.
- Geen directe JPA-relatie naar rooms of speakers, alleen ID-referenties.

**`Schedule` (Aggregate Root)**

Velden: `id`, `eventId (UNIQUE)`, `eventTitle`, `eventStartsAt`, `eventEndsAt`, `headcount`, `lastUpdated`, `sessions (List<Session>)`.

Methoden:
- `Schedule.create(eventId, title, startsAt, endsAt)` — factory.
- `addSession(title, roomId, speakerId, TimeSlot)` — controleert overlap vóór toevoegen; gooit exception bij room-overlap of speaker-overlap (hard block). Updateert `lastUpdated`.
- `applyHeadcountDelta(int delta)` — verhoog of verlaag headcount, minimaal 0. Updateert `lastUpdated`.
- `setEventTitle(String title)` — voor `event.updated` events.

**Aggregate-grens**: Schedule is per Event. Dit beperkt lock-contention; twee gelijktijdige sessie-toevoegingen voor *verschillende* events conflicteren niet.

**Cross-aggregate invariant** (TimeSlot binnen Event.DateRange):
Wordt **niet** in de aggregate afgedwongen — afgedwongen in de applicatielaag via een ACL-aanroep naar de Event BC vóór `addSession()`.

---

### 3. Repository

**`ScheduleRepository` (JpaRepository<Schedule, UUID>)**
- `findByEventId(UUID eventId): Optional<Schedule>`.

---

### 4. Application: ScheduleProjectionListener

`@Component` met twee `@RabbitListener` methoden.

**`onRegistrationEvent(String message)` — queue: `q.schedule.registrations`**

Verwerkt `registration.reserved.v1`, `registration.confirmed.v1`, `registration.waitlisted.v1`, `registration.cancelled.v1`.

Stappen:
1. Parse JSON, lees `eventId` en `guestCount`.
2. Bepaal delta: positief voor reserved/confirmed, negatief voor cancelled.
3. `schedules.findByEventId(eventId)` → `schedule.applyHeadcountDelta(delta)` → save.
4. **Idempotentie**: controleer op `messageId` (of `registrationId`) in een `processed_messages` tabel vóór verwerken — voorkomt dubbele headcount-update bij redelivery (RabbitMQ levert at-least-once).

**`onEventLifecycle(String message)` — queue: `q.schedule.events`**

- `event.published` → `Schedule.create(...)` → save.
- `event.updated` → `findByEventId` → `setEventTitle(...)` → save.
- `event.cancelled` → markeer schedule als inactief (of verwijder sessies).

Beide methoden zijn `@Transactional`. Bij parse-fout: log en stuur naar dead-letter queue (niet opnieuw gooien, anders blijft het in een retry-loop).

---

### 5. Application: ScheduleService (write-commando's)

**`addSession(UUID eventId, String title, UUID roomId, UUID speakerId, TimeSlot slot)`**

Stappen:
1. Zoek schedule op `eventId`.
2. **ACL-check** (cross-aggregate invariant): valideer dat `slot` binnen `schedule.eventStartsAt..eventEndsAt` valt. In productie: aanroep naar Event BC; in eerste implementatie: check lokaal op de denormalized velden in Schedule.
3. Delegeer aan `schedule.addSession(...)` — overlap-detectie zit in de aggregate.
4. Save.

---

### 6. REST Controller (OHS)

**`GET /schedule/events/{eventId}`**

Response:
```json
{
  "eventId": UUID,
  "title": string,
  "timeline": [
    { "sessionId": UUID, "title": string, "start": Instant, "end": Instant,
      "roomId": UUID, "speakerId": UUID }
  ],
  "headcount": int,
  "lastUpdated": Instant
}
```

- Sorteer timeline op `start`.
- Cache-headers: `Cache-Control: max-age=30` + `ETag` op basis van `lastUpdated.toEpochMilli()`.

**`GET /schedule/users/{userId}`**

Response:
```json
{
  "userId": UUID,
  "upcoming": [
    { "eventId": UUID, "title": string, "startsAt": Instant }
  ]
}
```

- Bevraagt de `user_upcoming_events` projectie-tabel.
- Ook ETag + Cache-Control: max-age=30.

---

### 7. RabbitMQ configuratie

Queues die deze service declareert:
- `q.schedule.registrations` gebonden aan exchange `registration.events` met routing key `registration.#`.
- `q.schedule.events` gebonden aan exchange `event.events` met routing key `event.#`.

---

## Invarianten & constraints (uit ontwerpdocumenten)

- Eén Schedule hoort bij precies één Event (1:1 via `event_id UNIQUE`).
- Sessies mogen alleen binnen `Event.DateRange` vallen — cross-aggregate invariant, afgedwongen in applicatielaag.
- Overlap op **room**: twee sessies met zelfde `roomId` mogen geen overlappende TimeSlot hebben — hard block, gooit exception.
- Overlap op **speaker**: twee sessies met zelfde `speakerId` mogen niet overlappen — hard block. Cross-event speaker overlap is out-of-scope (warning only).
- Verplaatsen van een sessie triggert opnieuw de overlap-check.
- Race condition bij gelijktijdige sessie-toevoegingen: moeten hard falen (optimistic lock via `@Version` op Schedule, of transactie-isolatie).
- Headcount is eventual consistent (read-model, mag fractie achterlopen).
- Event-consumers zijn idempotent — dedupliceer op `messageId` vóór het updaten van headcount.

---

## Communicatiematrix (uit Communicatie-ontwerpdocument §4)

| # | Flow | Stijl | Protocol | Sync |
|---|------|-------|----------|------|
| 13 | Schedule → UI / overige (query) | Query | RPC | HTTP REST | Ja |

---

## Relatie met Registration service

Schedule is het **downstream read-model** van Registration-events:

```
POST /registrations
  → Registration handler (transactional outbox)
    → RabbitMQ: registration.reserved.v1
      → ScheduleProjectionListener.onRegistrationEvent()
        → schedule.applyHeadcountDelta(+guestCount)
```

Bij annulering:
```
registration.cancelled.v1
  → ScheduleProjectionListener
    → schedule.applyHeadcountDelta(-guestCount)
```
