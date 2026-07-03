# Event Planner — Team ROCKET

Event Planner is een event-planning platform, opgebouwd uit zeven zelfstandige
Spring Boot services die communiceren via RabbitMQ (domain events) en JWT/JWKS
(authenticatie).

## Services

| Service | Map | Host-poort | Database | Beschrijving |
|---|---|---|---|---|
| User Profile | [userprofile](userprofile) | 8080 | PostgreSQL + MongoDB + MinIO | Profielen, avatars; maakt profiel aan bij `account.registered.v1` |
| Identity | [identity_service](identity_service) | 8081 | PostgreSQL | Registratie/login, JWT-uitgifte, JWKS op `/.well-known/jwks.json` |
| Event | [event](event) | 8082 | PostgreSQL | Event bounded context; publiceert `event.*` domain events |
| Venue | [venue](venue) | 8083 | PostgreSQL | Locaties, geospatiale zoekfunctie (Haversine) |
| Registration | [registration](registration) | 8084 | PostgreSQL | Inschrijvingen; outbox → `registration.events` |
| Schedule | [schedule](schedule) | 8085 | PostgreSQL | Read-model / tijdlijn-projecties |
| Notification | [notification](notification) | 8086 | MongoDB + Redis | Notificaties; idempotent via Redis |

## Alles starten met Docker

Vereist alleen Docker (Desktop). Elke image bouwt zichzelf (multi-stage Maven):

```bash
docker compose up --build -d
docker compose ps          # wacht tot alles healthy is
```

Infra-endpoints:

- RabbitMQ management: http://localhost:15672 (guest/guest)
- MinIO console: http://localhost:9001 (minioadmin/minioadmin)
- Postgres: 5433 (identity), 5434 (core), 5435 (event), 5436 (venue), 5437 (registration), 5438 (schedule)
- MongoDB: 27017 (userprofile), 27018 (notification) — Redis: 6379

## Event-flows tussen de services

```
identity ──account.registered.v1──▶ ep.events ──▶ userprofile (profiel aanmaken)
identity ──email.*.v1────────────▶ ep.events      (e-mail BC, nog niet gebouwd)

event ──event.published/updated/cancelled.v1──▶ event.events ──▶ registration (capacity sync)
                                                              ├─▶ schedule (tijdlijn)
                                                              └─▶ notification (updated/cancelled)

registration ──registration.*.v1 (outbox)──▶ registration.events ──▶ schedule (headcount)
                                                                  └─▶ notification (confirmed)
```

## Lokaal bouwen en testen

```bash
./mvnw verify            # bouwt alle modules en draait alle tests
./mvnw -pl event verify  # één module
```

Elke module is ook los te draaien vanuit de eigen map / IDE; zie de README's
per module. De `event`- en `venue`-modules draaien standaard met het
`dev`-profiel (H2 in-memory); in Docker draaien ze met `prod` (PostgreSQL).

## Bekende beperkingen

- **Notification bij event-updates/annuleringen:** de Event BC stuurt geen
  ontvanger (userId) mee — welke gebruikers een notificatie moeten krijgen
  vereist een fan-out over de inschrijvingen (Registration BC). Berichten
  zonder userId worden nu ontvangen en gelogd, maar er wordt geen notificatie
  opgeslagen.
- **Event service publiceert direct** naar RabbitMQ (geen transactional
  outbox zoals het communicatie-ontwerp §6.4 voorschrijft); Registration
  heeft die outbox wél.
- **Checkstyle** (gedeelde `checkstyle.xml`) is in CI advisory totdat de
  bestaande violations per module zijn weggewerkt.

## License

MIT — zie [LICENSE](LICENSE).
