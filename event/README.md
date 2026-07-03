# Event Module — Team Rocket

Spring Boot 3.3 / Java 21 implementatie van de **Event** bounded context uit het
Event-Planner project. Deze repo is opgezet conform het patroon van Joshua's
`user_profile` repo: één Spring Boot deployable per BC, package onder
`nl.teamrocket.core.<module>`. Bij later samenvoegen in de Core Modulaire Monoliet
(architectuurdoc §3.1.1) hoeft de package-structuur niet te wijzigen.

## Verantwoordelijkheid

Het Event-aggregate beheert de levenscyclus van events: aanmaken, bewerken,
publiceren, annuleren en automatische status-promotie. Het aggregate bewaakt zelf
zijn invariants (`endsAt > startsAt`, `capacity > 0`, geldige status-transities).

Niet de verantwoordelijkheid van deze module:
- Tellen van bevestigde registraties → Registration BC.
- Reserveren van een venue-tijdslot → synchrone call naar Venue BC vanuit de
  orchestration-saga (data-distributiedoc §5.2.3).

## Architectuur — Hexagonal (Ports & Adapters)

Architectuurdoc §4.1 (Hexagonal + DDD). Layering:

```
adapter/                    ← secondary/primary adapters (REST, JPA, publisher)
└── rest/                   primary adapter: HTTP endpoints
└── jpa/                    secondary adapter: PostgreSQL/H2 persistence
└── publisher/              secondary adapter: domain-event publisher

application/                ← use-cases + ports
├── port/inbound/           EventCommandPort, EventQueryPort
├── port/outbound/          EventRepository, DomainEventPublisher
├── command/                CreateEventCommand, UpdateEventCommand, …
├── query/                  GetEventQuery, ListEventsQuery
└── service/                EventCommandService, EventQueryService

domain/                     ← pure domeinlaag, geen frameworks
├── model/                  Event (aggregate root), EventStatus, EventMetadata,
│                           EventTimeSlot, Visibility
├── service/                EventStatusPolicy (auto-status)
├── event/                  EventCreated, EventPublished, EventUpdated, EventCancelled
└── exception/              EventNotFoundException, InvalidEventStatusTransition…
```

**Afhankelijkheidsrichting**: `adapter → application → domain`. Het domein kent
geen Spring of JPA. Application kent geen JPA — alleen de outbound port.

## Wat is gerealiseerd

| Onderdeel | Status | Bron in ontwerp |
|---|---|---|
| Event aggregate (DRAFT/PLANNED/CANCELLED/COMPLETED) | ✅ | Architectuurdoc §5.4.1 |
| Invariants: tijdslot, capaciteit, status-transities | ✅ | Architectuurdoc §5.1 / §5.4 |
| `@Version` optimistic locking | ✅ | Data-distributiedoc §2.3.1 |
| Hexagonale layering met ports/adapters | ✅ | Architectuurdoc §4.1 / §4.2 |
| EventStatusPolicy domain service | ✅ | Architectuurdoc §4.2 |
| Domain events (EventCreated/Published/Updated/Cancelled) | ✅ | Communicatiedoc §3.2.1 |
| REST controller met Bean Validation | ✅ | Architectuurdoc §4.2 / §5.1 |
| GlobalExceptionHandler met juiste HTTP-codes | ✅ | Kwaliteitseisen §1 |
| JPA-adapter (PostgreSQL prod / H2 dev) | ✅ | Data-distributiedoc §2.3.1 |
| Domain → entity mapper (geen JPA in domein) | ✅ | Architectuurdoc §4.2 |
| Unit tests + Spring context test | ✅ (12/12) | Kwaliteitseisen "Testdekking" |

## Afwijkingen t.o.v. het ontwerp

Conform de opdrachtbeschrijving "afwijken van het ontwerp mag, mits onderbouwd".
Deze leveringsfase richt zich op de **kern**: aggregate + hexagonal + JPA + REST
(scope-keuze met opdrachtgever afgestemd). De onderstaande onderdelen uit het
ontwerp zijn bewust uitgesteld:

### 1. RabbitMQ topic exchange + transactional outbox
**Ontwerp** (communicatiedoc §6.1 / §6.4, data-distributiedoc §5.3): events worden
in de outbox-tabel weggeschreven en door een scheduler naar RabbitMQ gepushed.

**Realisatie**: `LoggingDomainEventPublisher` als tijdelijke adapter voor de
outbound port `DomainEventPublisher`. Events worden gelogd in plaats van
gepubliceerd. Domein- en applicationcode kennen alleen de port, dus deze
adapter is 1:1 vervangbaar door een `RabbitOutboxPublisher` zonder verdere
wijzigingen — dat is precies waarvoor de port bestaat.

**Waarom**: zonder draaiende RabbitMQ + outbox-tabel + DLQ + idempotency-gate
is dit veel infrastructuur voor weinig demonstreerbare meerwaarde tijdens de
huidige fase. Het architecturale patroon (port + adapter) is wel aanwezig
en getest.

### 2. VenueBcClient (synchroon Venue BC bevragen)
**Ontwerp** (communicatiedoc §3.2.2, data-distributiedoc §5.2.3): `WebClient`
met Resilience4j retry/circuit breaker en ACL-mapper naar `VenueInfo` VO.

**Realisatie**: `venueId` wordt enkel als referentie-ID bewaard in het
aggregate. Er is geen runtime-validatie van bestaan of beschikbaarheid van de
venue (de gebruiker is verantwoordelijk voor het correct meegeven).

**Waarom**: hoort bij de event-creatie-saga (orchestration), die expliciet
buiten scope viel voor deze leveringsfase. Toevoegen blijft mogelijk door
een nieuwe `VenueBcPort` als outbound port + een adapter te introduceren.

### 3. JWT-validatie / OAuth2 resource server
**Ontwerp** (communicatiedoc §3.1.1): alle requests met JWT via API Gateway.

**Realisatie**: geen security configured. Alle endpoints open.

**Waarom**: Identity service draait niet in deze setup. Voor lokaal testen
en demonstratie van het Event-domein is dit voldoende. Bij integratie in de
Core Monoliet wordt de `CoreSecurityConfig` uit `user_profile` toegepast.

### 4. Flyway-migraties
**Ontwerp** (data-distributiedoc §3.2): Flyway voor schema-versiebeheer.

**Realisatie**: Hibernate `ddl-auto=update` (dev) / `validate` (prod). Geen
migrations directory.

**Waarom**: bij één tabel en geen production data is migratie-tooling overkill.
Toevoegen bij eerste schema-wijziging op een omgeving met data.

### 5. EventStatus.DRAFT toegevoegd
**Brownfield**: alleen `PLANNED`, `CANCELLED`, `COMPLETED`.

**Realisatie**: extra status `DRAFT` als initiële status na `create()`. Het
event wordt expliciet `publish()`'ed naar `PLANNED`.

**Waarom**: de orchestration-saga (data-distributiedoc §5.2.3) maakt het event
eerst tentatief aan vóór de externe venue-boeking. Zonder DRAFT zou het
event direct gepubliceerd zijn (en dus aan gasten zichtbaar) vóór de
venue-confirmatie. Dit sluit beter aan op het Event Storming-werk.

## Runnen

### Lokaal (dev profile, H2 in-memory)
```bash
./mvnw spring-boot:run
# of:
mvn spring-boot:run
```

App draait op `http://localhost:8081`. H2 console op `/h2`
(`jdbc:h2:mem:event`, user `sa`).

### Tests
```bash
mvn test
```

Verwachting: **12 tests, 0 failures, BUILD SUCCESS**.

### Productie (PostgreSQL)
```bash
SPRING_PROFILES_ACTIVE=prod \
PG_HOST=localhost PG_DB=event PG_USER=event PG_PASSWORD=event \
mvn spring-boot:run
```

## REST endpoints

| Method | Path | Body | Resultaat |
|---|---|---|---|
| POST | `/events` | `CreateEventRequest` | 201, `EventResponse` (status=DRAFT) |
| GET | `/events?status=&organizerId=` | – | 200, `EventResponse[]` |
| GET | `/events/{id}` | – | 200, `EventResponse` |
| PUT | `/events/{id}` | `UpdateEventRequest` | 200, `EventResponse` |
| POST | `/events/{id}/publish` | – | 200, status → PLANNED |
| POST | `/events/{id}/cancel` | `CancelEventRequest` (optional) | 200, status → CANCELLED |
| POST | `/events/{id}/refresh-status` | – | 200, status → COMPLETED bij verlopen tijdslot |
| DELETE | `/events/{id}` | – | 204 |

Foutgedrag:
- 404 — event niet gevonden
- 400 — validatiefout (Bean Validation), capaciteit ≤ 0, ongeldig tijdslot
- 409 — ongeldige status-transitie of optimistic locking conflict

## Voorbeeld request

```bash
curl -X POST http://localhost:8081/events \
  -H "Content-Type: application/json" \
  -d '{
    "organizerId": "00000000-0000-0000-0000-000000000001",
    "venueId":     "00000000-0000-0000-0000-000000000002",
    "title":       "DevDay 2026",
    "description": "Jaarlijkse meetup",
    "startsAt":    "2026-09-12T09:00:00Z",
    "endsAt":      "2026-09-12T17:00:00Z",
    "capacity":    150,
    "speaker":     "Joshua Larez",
    "visibility":  "PUBLIC"
  }'
```

## Lessen-dekking

Mapping van de cursusonderwerpen (lessen 1–8) naar wat deze repo concreet
bevat. Bedoeld als expliciete onderbouwing van waarom bepaalde onderwerpen
*niet* in code zitten — niet alles wat in de lessen behandeld is, hoort in
deze BC thuis.

Legenda: **in code** = geïmplementeerd in deze repo · **deels** = aanwezig maar gestubd/beperkt · **doc** = behandeld in projectdocumentatie, niet als code-artefact · **niet** = bewust niet geïmplementeerd, met motivatie.

| Les | Onderwerp | Status | Toelichting |
|---|---|---|---|
| 1 | Gedistribueerde systemen / CAP / fallacies | doc | Eigen deployable per BC = bewuste partitionering. CAP-keuze (CP binnen het aggregate via ACID, AP tussen BC's) staat in architectuurdoc §3.1.1 + data-distributiedoc §2.3. |
| 2 | DDD tactical (aggregate, VO, domain events, domain service) | in code | `Event` aggregate, `EventTimeSlot`/`EventMetadata`/`Visibility` VO's, vier domain events, `EventStatusPolicy` domain service. |
| 2 | DDD strategic (bounded context, context map) | doc | Bounded context = Event; context-map relaties (Customer-Supplier met Venue, Publisher naar Registration) in architectuurdoc §3.2. |
| 3 | Event Storming | doc | Workshop-output (events, commands, aggregates) in architectuurdoc §2. Geen apart artefact in deze repo — het resultaat zit verwerkt in het domain-model en de domain events. |
| 4 | Architectuurstijlen (hexagonal, modulaire monoliet) | in code | Strikte `domain → application → adapter` layering, package-structuur klaar voor merge in Core Monoliet zonder code-wijziging. |
| 5 | Communicatie & interactie — REST (RPC-stijl) | in code | `EventRestController` met Bean Validation en `GlobalExceptionHandler`. |
| 5 | Communicatie & interactie — Messaging | deels | Outbound port `DomainEventPublisher` aanwezig, RabbitMQ-adapter gestubd naar logger (zie Afwijking 1). Patroon werkt, alleen het transport ontbreekt. |
| 5 | Coupling (referential/temporal) | in code | Domain events worden gepubliceerd zonder kennis van consumers (referential decoupling). Async port maakt temporal decoupling mogelijk zodra RabbitMQ erbij komt. |
| 6 | RDBMS / ACID / concurrency | in code | PostgreSQL (prod) / H2 (dev), `@Version` optimistic locking, `@Transactional` op application services, expliciete domain↔entity mapper. |
| 6 | NoSQL / NewSQL | niet | Eén relatief klein aggregate met sterke consistency-eisen op status-transities. Het opportunisme van eventual consistency / horizontale sharding heeft hier geen valide use-case. |
| 7 | Idempotency & concurrency control | in code | Optimistic locking voorkomt lost-updates bij parallelle wijziging. Status-machine in `assertTransition` voorkomt ongeldige overgangen. |
| 7 | SAGA / 2PC / CQRS / Event Sourcing | niet | Het Event-aggregate beslaat één lokale transactie — geen cross-aggregate of cross-BC schrijfproces dat een saga rechtvaardigt. De event-creatie-saga (incl. Venue-reservering) viel expliciet buiten scope (zie Afwijking 2). CQRS/ES is mogelijke evolutie, niet nu nodig. |
| 8 | Security (JWT / mTLS) | niet | Identity service draait niet (zie Afwijking 3). Bij integratie in Core Monoliet komt `CoreSecurityConfig` uit `user_profile`. |
| 8 | Resilience (retry / circuit breaker / rate limit) | niet | Deze BC doet geen outgoing calls naar andere services (zie Afwijking 2: VenueBcClient niet aanwezig). Geen kandidaat voor Resilience4j zonder die client. Optimistic locking dekt wel de concurrency-resilience. |
| 8 | Observability | deels | `@Slf4j` op alle services + Spring Boot logging. Geen Prometheus-export, geen distributed tracing — die horen bij het Core-platform, niet bij een enkele BC. |

## Verantwoording per rubric-criterium

| Criterium | Bewijs |
|---|---|
| Uitwerking systeem-architectuur | Eigen Spring Boot deployable conform "modulaire monoliet met selectieve splits" (architectuurdoc §3.1.1). Package-structuur sluit aan op de Core Monoliet zodat samenvoegen triviaal is. |
| Uitwerking applicatie-architectuur | Strikte hexagonale layering domain → application → adapter, geen Spring/JPA in domein, ports/adapters expliciet (architectuurdoc §4.1 / §4.2). |
| Uitwerking communicatie en interactie | Inbound port `EventCommandPort`/`EventQueryPort`, outbound `EventRepository`/`DomainEventPublisher`. REST controller roept enkel ports aan. Domain events via publisher (afwijking RabbitMQ → logger gedocumenteerd hierboven). |
| Uitwerking databases | PostgreSQL (prod) / H2 (dev) via Spring Data JPA, `@Version` voor optimistic locking, expliciete domain↔entity mapping. |
| Uitwerking dataverwerking | Event aggregate met invariants en domain events, `EventStatusPolicy` domain service, application services met `@Transactional`. 12/12 tests groen. |
