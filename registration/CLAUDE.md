# Registration Service — Implementatiegids

## Wat dit is

De Registration bounded context verwerkt inschrijvingen voor events. Het is het transactionele hart van het systeem: hier zit de capaciteitscheck, de idempotentie, en de SAGA-logica.

**Communicatiestijl**: RPC/command+result over `POST /registrations` (synchronous, want de gebruiker wacht in de UI op een antwoord). Daarna worden events asynchroon gepubliceerd via de Transactional Outbox.

---

## Te implementeren stappen

### 1. Database-schema (Flyway)

Maak twee migratiescripts:

**`V1__create_registrations.sql`**
- Tabel `event_capacity`: kolommen `event_id (UUID PK)`, `capacity (int)`, `open (boolean)`.
- Tabel `registrations`: kolommen `id (UUID PK)`, `event_id`, `user_id`, `status (VARCHAR)`, `plus_ones (int)`, `notes`, `channel (VARCHAR)`, `idempotency_key (VARCHAR UNIQUE NOT NULL)`, `created_at`, `updated_at`, `version (BIGINT)`.
- Partial unique index op `(event_id, user_id) WHERE status NOT IN ('CANCELLED', 'NO_SHOW')` — dit borgt de invariant "max 1 actieve registratie per (Event, User)".
- Index op `(event_id, status)` voor de capaciteitsquery.

**`V2__create_outbox.sql`**
- Tabel `outbox_messages`: kolommen `id (UUID PK)`, `routing_key (VARCHAR)`, `payload (TEXT)`, `status (VARCHAR DEFAULT 'PENDING')`, `created_at`, `published_at`.
- Index op `(status, created_at) WHERE status = 'PENDING'`.

---

### 2. Domain

**`RegistrationStatus` (enum)**
States: `REQUESTED`, `RESERVED`, `CONFIRMED`, `WAITLISTED`, `CANCELLED`, `NO_SHOW`

Transitions:
- REQUESTED → RESERVED (ruimte beschikbaar)
- REQUESTED → WAITLISTED (vol)
- RESERVED → CONFIRMED (betaling ontvangen, of direct bij gratis event)
- CONFIRMED of RESERVED → CANCELLED (gast annuleert, of compenserende SAGA-actie)
- WAITLISTED → RESERVED (promotie na annulering, FIFO)
- CONFIRMED → NO_SHOW (na event.end, door organisator)

**`Registration` (aggregate root / JPA entity)**

Vereisten:
- `@Version` veld voor optimistic locking (secundaire bescherming).
- `idempotency_key` kolom met `UNIQUE` constraint.
- Factory-methode `Registration.create(...)` in plaats van public constructor.
- Methoden: `reserve()`, `confirm()`, `waitlist()`, `cancel()`, `markNoShow()` — elk updaten `status` en `updatedAt`.

**`EventCapacity` (JPA entity)**

Lokaal read-model van event-capaciteit (gevoed door Event BC events).
- `event_id (UUID PK)`, `capacity (int)`, `open (boolean)`.
- Methode `hasRoom(int takenSeats, int plusOnes): boolean`.

---

### 3. Repositories

**`RegistrationRepository` (JpaRepository)**
- `findByIdempotencyKey(String key): Optional<Registration>` — voor de idempotentiecheck.
- `countByEventIdAndStatusIn(UUID eventId, RegistrationStatus... statuses): long` — voor capaciteitscheck.
- `findNextWaitlisted(UUID eventId): Optional<Registration>` — voor FIFO-promotie (JPQL: ORDER BY created_at ASC LIMIT 1).

**`EventCapacityRepository` (JpaRepository)**
- `lockByEventId(UUID eventId): Optional<EventCapacity>` — native query met `SELECT ... FOR UPDATE`. Vereist een actieve `@Transactional` context.

**`OutboxRepository` (JpaRepository)**
- `findPending(): List<OutboxMessage>` — JPQL, max 50, ORDER BY created_at ASC.

---

### 4. Application: ReserveRegistrationHandler

Dit is de kern van de SAGA (Data-distributie §5.2.2).

Stappen binnen één `@Transactional`:
1. **Idempotentiecheck**: zoek op `idempotency_key` — als gevonden, return bestaand resultaat.
2. **Lock capaciteitsrij**: `eventCapacityRepository.lockByEventId(eventId)` — gooit exception als event niet bestaat of niet open is.
3. **Tel bezette plaatsen**: `countByEventIdAndStatusIn(eventId, RESERVED, CONFIRMED)`.
4. **Bepaal status**: als `takenSeats + plusOnes + 1 <= capacity` → RESERVED, anders → WAITLISTED.
5. **Sla Registration op**.
6. **Schrijf OutboxMessage** (zelfde transactie!) met routing key `registration.reserved.v1` of `registration.waitlisted.v1` en JSON-payload.
7. Return `RegistrationResult(registrationId, status, position, createdAt)`.

**Compenserende actie** (`compensate(UUID registrationId)`):
- In eigen `@Transactional`.
- Zet status op CANCELLED.
- Schrijf OutboxMessage met `registration.cancelled.v1`.
- Wordt aangeroepen als downstream SAGA-stap onherstelbaar faalt.

---

### 5. Application: ConfirmRegistrationHandler

Voor betaalde events: RESERVED → CONFIRMED na betaling.

Stappen:
1. Zoek Registration op id.
2. Valideer status == RESERVED.
3. Roep `registration.confirm()` aan.
4. Schrijf OutboxMessage met `registration.confirmed.v1`, payload bevat `guestCount`.
5. Sla op.

---

### 6. Application: PromoteFromWaitlistHandler

Wordt getriggerd nadat een CONFIRMED of RESERVED registratie gecanceld is (policy, geen menselijke actie).

Stappen:
1. Zoek volgende wachtende: `findNextWaitlisted(eventId)` (FIFO op `created_at`).
2. Als gevonden: zet op RESERVED.
3. Schrijf OutboxMessage `registration.reserved.v1`.
4. Sla op.

---

### 7. Infrastructure: OutboxPublisher

`@Component` met `@Scheduled(fixedDelay = 500)`.

- Haal `findPending()` op uit OutboxRepository.
- Per bericht: `rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload)`.
- Bij succes: markeer als PUBLISHED, sla op.
- Bij fout: laat PENDING staan (retry bij volgende poll).
- Exchange: `registration.events` (topic exchange, durable).

---

### 8. Infrastructure: EventSyncListener

`@RabbitListener(queues = "q.registration.event-sync")` — ontvangt events van Wessel's Event BC.

- `event.published` → INSERT INTO `event_capacity` (eventId, capacity, open=true).
- `event.updated` → UPDATE capacity.
- `event.cancelled` → SET open=false.

---

### 9. REST Controller

`POST /registrations`

Request headers:
- `X-Idempotency-Key` (verplicht, client-gegenereerde UUID)
- `X-User-Id` (verplicht, UUID — gezet door API Gateway na JWT-validatie)

Request body:
```
{
  "eventId": UUID,
  "plusOnes": int (default 0),
  "notes": string (optioneel),
  "channel": "WEB" | "MOBILE" | "API" (default WEB)
}
```

Response body (RegistrationResult):
```
{
  "registrationId": UUID,
  "status": "RESERVED" | "WAITLISTED",
  "position": int | null,
  "createdAt": Instant
}
```

HTTP 201 bij nieuwe registratie, HTTP 200 bij idempotente herhaling.

---

### 10. RabbitMQ configuratie

Exchange: `registration.events` (topic, durable) — gepubliceerd door deze service.

Queue voor inkomende events (van Event BC):
- `q.registration.event-sync` gebonden aan `event.events` exchange met routing key `event.#`.

---

## Invarianten & constraints (uit ontwerpdocumenten)

- Nooit meer CONFIRMED+RESERVED registraties dan `event_capacity.capacity` — afgedwongen via `SELECT FOR UPDATE`.
- Max 1 actieve registratie per `(event_id, user_id)` — afgedwongen via partial unique index.
- Inschrijven alleen als event status = Gepubliceerd en `open = true` — check in EventCapacity.
- Als capacity 0 of vol → automatisch WAITLISTED (geen fout, policy).
- Betaald event zonder betaling blijft op REQUESTED/RESERVED, nooit CONFIRMED.
- State-overgang + outbox-schrijf in dezelfde transactie (at-least-once delivery).
- Consumers zijn idempotent — dedupliceer op `registrationId` (at-least-once RabbitMQ).

---

## Communicatiematrix (uit Communicatie-ontwerpdocument §4)

| # | Flow | Stijl | Protocol | Sync |
|---|------|-------|----------|------|
| 8 | Registration intake | Command+Result | RPC | HTTP REST | Ja |
| 9 | Registration → Messaging | Event | Messaging | RabbitMQ topic | Nee |
| 10 | Registration → Notification | Event | Messaging | RabbitMQ topic | Nee |
| 11 | Registration → Schedule | Event | Messaging | RabbitMQ topic | Nee |
| 12 | Registration → Audit | Event | Messaging | RabbitMQ topic | Nee |

---

## Event payloads

**`registration.reserved.v1`**
```json
{ "registrationId": UUID, "eventId": UUID, "userId": UUID,
  "status": "RESERVED", "guestCount": int, "reservedAt": Instant }
```

**`registration.confirmed.v1`**
```json
{ "registrationId": UUID, "eventId": UUID, "userId": UUID,
  "confirmedAt": Instant, "guestCount": int, "channel": "WEB|MOBILE|API" }
```

**`registration.waitlisted.v1`**
```json
{ "registrationId": UUID, "eventId": UUID, "userId": UUID,
  "status": "WAITLISTED", "position": int, "waitlistedAt": Instant }
```

**`registration.cancelled.v1`**
```json
{ "registrationId": UUID, "eventId": UUID, "userId": UUID,
  "status": "CANCELLED", "cancelledAt": Instant }
```
