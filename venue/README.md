# Venue Module — Team Rocket

Spring Boot 3.3 / Java 21 implementatie van de **Venue** bounded context uit het
Event-Planner project. Volgt hetzelfde patroon als de
[Event-module](https://github.com/wesselvandenijssel/event-module) en Joshua's
[`user_profile`](https://github.com/Darth-Joshua/user_profile) repo: één Spring
Boot deployable per BC onder `nl.teamrocket.core.venue.*`. Bij samenvoegen in de
Core Modulaire Monoliet (architectuurdoc §3.1.1) hoeft niets aan de packaging te
veranderen.

## Verantwoordelijkheid

Venue is een **lokale read-cache** van het externe venue-systeem (architectuurdoc
§3.2.2 — Conformist-relatie, data-distributiedoc §2.3.2 — eventual consistency).
We kennen elke venue via een externe ID en houden lokaal naam, adres, locatie,
capaciteit en faciliteiten bij. De source of truth blijft het externe systeem;
onze cache mag iets achterlopen.

De module ondersteunt:
- Geospatiaal zoeken ("venues binnen N km") — Haversine in deze fase, PostGIS
  in productie.
- Beschikbaarheidsvraag (`/availability`) doorgezet naar het externe systeem
  via een Anti-Corruption Layer.
- Idempotente cache-refresh op `externalVersion` (geen out-of-order overschrijvingen).

Niet de verantwoordelijkheid van deze module:
- Bookings of bezettingsagenda → blijven bij het externe venue-systeem.
- Tellen van actuele deelnemers → Registration BC.

## Architectuur — Hexagonal (Ports & Adapters)

```
adapter/
├── rest/                   primary adapter: HTTP endpoints
├── jpa/                    secondary adapter: PostgreSQL/H2
└── acl/                    secondary adapter: StubExternalVenueGateway

application/
├── port/inbound/           VenueCommandPort, VenueQueryPort
├── port/outbound/          VenueRepository, ExternalVenueGateway  ← ACL grens
├── command/                Register/Refresh/Remove commands
├── query/                  GetVenue / FindVenuesNearby / CheckAvailability
└── service/                VenueCommandService, VenueQueryService

domain/
├── model/                  Venue (aggregate root), Address, GeoLocation, Amenity
├── service/                DistanceCalculator (Haversine)
└── exception/              VenueNotFoundException, InvalidCapacityException
```

**ACL-patroon**: `ExternalVenueGateway` is een **outbound port** in de application
laag. De application code spreekt enkel domein-types; de adapter
(`StubExternalVenueGateway`, of later een echte HTTP-client) is verantwoordelijk
voor het mappen van/naar het externe model. Wijzigingen in de externe API raken
domein en application niet.

## Wat is gerealiseerd

| Onderdeel | Status | Bron in ontwerp |
|---|---|---|
| Venue aggregate als read-cache met `externalVersion` dedup | ✅ | Architectuurdoc §3.2.2, data-distributiedoc §2.3.2/4.3.2 |
| Address / GeoLocation / Amenity value objects met invariants | ✅ | Architectuurdoc §5.1 |
| Hexagonal layering met ports/adapters | ✅ | Architectuurdoc §4.1 / §4.2 |
| Anti-Corruption Layer (`ExternalVenueGateway` outbound port + stub) | ✅ | Architectuurdoc §3.2.2 |
| Geospatiaal zoeken via Haversine `DistanceCalculator` | ✅ | Data-distributiedoc §2.3.2 (PostGIS-equivalent) |
| REST controller met Bean Validation | ✅ | Communicatiedoc §3.2.2 |
| `GET /venues/{id}/availability` → ACL fail-closed bij externe storing | ✅ | Communicatiedoc §3.2.2, data-distributiedoc §6 |
| JPA-adapter (PostgreSQL prod / H2 dev) + expliciete mapper | ✅ | Data-distributiedoc §2.3.2 |
| Unit tests + Spring context test | ✅ (10/10) | Kwaliteitseisen "Testdekking" |

## Afwijkingen t.o.v. het ontwerp

### 1. PostGIS niet ingezet — Haversine in-memory
**Ontwerp** (data-distributiedoc §2.3.2): geospatiaal zoeken via PostGIS
`ST_DWithin` in dezelfde relationele database, met spatiale index.

**Realisatie**: lat/lon staan als losse `double`-kolommen; afstand wordt in-memory
berekend met de Haversine-formule in `DistanceCalculator`.

**Waarom**: PostGIS vereist een aparte database-extensie en een dedicated
Hibernate Spatial dialect. Voor de huidige dataset (3 seed-venues) is in-memory
ruim voldoende. Bij groei is migratie naar PostGIS lokaal: vervang de
filter-stream in `VenueQueryService.findNearby()` door een JPQL/native query
en voeg een spatiale index toe op de tabel. Domein- en API-contract wijzigen niet.

### 2. ExternalVenueGateway is een in-memory stub
**Ontwerp** (architectuurdoc §3.2.2, communicatiedoc §3.2.2): HTTP-client
(WebClient) naar de externe SaaS, met Resilience4j retry/circuit breaker en
mapper van extern DTO naar onze `VenueInfo` VO.

**Realisatie**: `StubExternalVenueGateway` met drie seed-venues (Hex Factory,
HU Padualaan 99, Jaarbeurs Hal 7) en een eenvoudige availability-regel (12-13 UTC
= bezet) zodat de fail-closed-flow getest kan worden.

**Waarom**: er is geen extern systeem om mee te integreren in deze fase. Door
dezelfde outbound port (`ExternalVenueGateway`) te gebruiken, is de echte
HTTP-adapter later een vervanging van uitsluitend `StubExternalVenueGateway` —
geen wijziging in application of domain.

### 3. Geen JWT-validatie
**Ontwerp** (communicatiedoc §3.1.1): JWT van Identity service via API Gateway.
**Realisatie**: geen security configured. Endpoints open.
**Waarom**: Identity service draait niet in deze setup. Voor lokaal demonstreren
van het Venue-domein is dit voldoende.

### 4. Geen Flyway-migraties
Hibernate `ddl-auto=update` (dev) / `validate` (prod). Toevoegen bij eerste
schema-wijziging op een omgeving met productie-data.

### 5. Geen Venue domain events
**Ontwerp** voorziet geen Venue → consumers events (Venue is een read-cache, geen
event-publisher in de Context Map). Daarom géén `DomainEventPublisher` in deze
module — bewuste keuze, consistent met het ontwerp.

## Runnen

### Lokaal (dev profile, H2 in-memory)
```bash
mvn spring-boot:run
```

App draait op `http://localhost:8082`. H2 console op `/h2`
(`jdbc:h2:mem:venue`, user `sa`).

### Tests
```bash
mvn test
```

Verwachting: **10 tests, 0 failures, BUILD SUCCESS**.

### Productie (PostgreSQL)
```bash
SPRING_PROFILES_ACTIVE=prod \
PG_HOST=localhost PG_DB=venue PG_USER=venue PG_PASSWORD=venue \
mvn spring-boot:run
```

## REST endpoints

| Method | Path | Body / Params | Resultaat |
|---|---|---|---|
| POST | `/venues` | `RegisterVenueRequest` | 201, `VenueResponse` |
| GET | `/venues` | – | 200, `VenueResponse[]` |
| GET | `/venues/{id}` | – | 200, `VenueResponse` |
| DELETE | `/venues/{id}` | – | 204 |
| POST | `/venues/{id}/refresh` | – | 200, `{ "updated": true/false }` |
| GET | `/venues/nearby` | `?lat=&lon=&radiusKm=` (default 10) | 200, `VenueResponse[]` |
| GET | `/venues/{id}/availability` | `?from=&to=` (ISO-8601 UTC) | 200, `AvailabilityResponse` |

Foutgedrag:
- 404 — venue niet gevonden
- 400 — validatiefout (Bean Validation), invalide lat/lon, capaciteit ≤ 0,
  `to` niet na `from`, radius ≤ 0 of > 1000 km

## Voorbeelden

```bash
# Eerste keer cachen vanuit het externe systeem (seeded venue id):
curl -X POST http://localhost:8082/venues/11111111-1111-1111-1111-111111111111/refresh

# Alle venues binnen 5 km van HU Padualaan 99:
curl "http://localhost:8082/venues/nearby?lat=52.0852&lon=5.1740&radiusKm=5"

# Beschikbaarheid (12:00-13:00 UTC = bezet door de stub):
curl "http://localhost:8082/venues/11111111-1111-1111-1111-111111111111/availability?from=2026-06-10T12:30:00Z&to=2026-06-10T13:30:00Z"
```

## Lessen-dekking

Mapping van de cursusonderwerpen (lessen 1–8) naar wat deze repo concreet
bevat. Bedoeld als expliciete onderbouwing van waarom bepaalde onderwerpen
*niet* in code zitten — Venue is een read-cache BC, geen volledig
event-publishing systeem, dus sommige patronen passen hier inhoudelijk niet.

Legenda: **in code** = geïmplementeerd in deze repo · **deels** = aanwezig maar gestubd/beperkt · **doc** = behandeld in projectdocumentatie, niet als code-artefact · **niet** = bewust niet geïmplementeerd, met motivatie.

| Les | Onderwerp | Status | Toelichting |
|---|---|---|---|
| 1 | Gedistribueerde systemen / CAP / fallacies | doc | Venue als read-cache is bewust **AP** t.o.v. extern systeem (eventual consistency, data-distributiedoc §2.3.2). Fail-closed availability-check beschermt tegen network-fallacies. |
| 2 | DDD tactical (aggregate, VO, domain service) | in code | `Venue` aggregate, `Address`/`GeoLocation`/`Amenity` VO's, `DistanceCalculator` domain service. |
| 2 | DDD strategic — Conformist + ACL | in code | `ExternalVenueGateway` outbound port = expliciete Anti-Corruption Layer-grens. Venue is **Conformist** t.o.v. extern model (architectuurdoc §3.2.2). |
| 3 | Event Storming | doc | Workshop-output in architectuurdoc §2. Deze BC heeft geen eigen domain events (geen event-publisher in de Context Map — Venue is consumer/cache, niet origin). |
| 4 | Architectuurstijlen (hexagonal, modulaire monoliet) | in code | Hexagonal layering identiek aan Event-module. Klaar voor merge in Core Monoliet. |
| 5 | Communicatie & interactie — REST (RPC-stijl) | in code | `VenueRestController` voor inkomende calls, ACL port voor uitgaande. |
| 5 | Communicatie & interactie — Messaging | niet | Venue publiceert geen events (read-cache, geen origin). Geen messaging-port = geen incomplete adapter — dit is een ontwerpkeuze, geen omissie (zie Afwijking 5). |
| 5 | Fail-closed bij externe storing | in code | `VenueQueryService.checkAvailability` retourneert `unknown` i.p.v. `available=true` bij exceptions van de gateway. Voorkomt false-positives die tot dubbele boekingen leiden. |
| 6 | RDBMS / ACID | in code | PostgreSQL (prod) / H2 (dev), `@ElementCollection` voor amenities, expliciete mapper. |
| 6 | NoSQL / NewSQL / PostGIS | deels | PostGIS vervangen door Haversine in-memory (Afwijking 1) — gedocumenteerd migratiepad. NoSQL niet relevant: venue-attributen zijn sterk gestructureerd. |
| 7 | Idempotente cache-refresh | in code | `Venue.refreshFromExternal()` controleert `externalVersion` en negeert oudere/gelijke versies. Voorkomt out-of-order overschrijvingen bij at-least-once delivery. |
| 7 | SAGA / 2PC / CQRS / Event Sourcing | niet | Read-cache zonder eigen schrijfworkflow over meerdere services. De availability-check is een doorgeefluik naar het externe systeem — geen lokale state-change die saga-coordinatie vraagt. |
| 8 | Security (JWT / mTLS) | niet | Identity service draait niet (zie Afwijking 3 in README). Bij Core-integratie komt centrale security-config terug. |
| 8 | Resilience (retry / circuit breaker / rate limit) | deels | De architectuurplek (`ExternalVenueGateway`) is voorzien — fail-closed try/catch is al aanwezig in de query-service. Resilience4j retry + circuit breaker zijn logische uitbreidingen zodra `StubExternalVenueGateway` vervangen wordt door een echte HTTP-adapter (Afwijking 2). |
| 8 | Observability | deels | `@Slf4j` met betekenisvolle logging (seed-bevestiging, fail-closed reason). Geen metrics/tracing-export — hoort bij het Core-platform. |

## Verantwoording per rubric-criterium

| Criterium | Bewijs |
|---|---|
| Uitwerking systeem-architectuur | Eigen Spring Boot deployable conform "modulaire monoliet met selectieve splits" (architectuurdoc §3.1.1). Package-structuur identiek aan Event/UserProfile-modules. |
| Uitwerking applicatie-architectuur | Strikte hexagonale layering domain → application → adapter, geen Spring/JPA in domein, ports/adapters expliciet. ACL via outbound port — exact het patroon uit architectuurdoc §3.2.2. |
| Uitwerking communicatie en interactie | Endpoints uit communicatiedoc §3.2.2 (`/venues/{id}`, `/venues/{id}/availability`). ACL fail-closed bij externe storing → voorkomt false-positives die tot dubbele boekingen leiden. |
| Uitwerking databases | PostgreSQL (prod) / H2 (dev) via Spring Data JPA. Address/lat-lon als kolommen; amenities als `@ElementCollection`. PostGIS-afwijking gedocumenteerd. |
| Uitwerking dataverwerking | Venue aggregate met invariants (capacity > 0, lat/lon ranges); idempotente refresh op `externalVersion` (data-distributiedoc §4.3.2). 10/10 tests groen. |
