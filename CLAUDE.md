# IKR — Johan Hendriks: Registration & Schedule

## Jouw verantwoordelijkheid (uit alle 4 ontwerpdocumenten)

Johan bezit twee bounded contexts: **Registration** en **Schedule**.

Bronnen:
- Architectuur-ontwerpdocument §6: "Johan Hendriks — Registration & Schedule bounded contexts; UML consolidatie (hoofdstuk 5) en sequence diagram."
- Communicatie-ontwerpdocument §1.4: "Johan | Schedule, Registration | Registration-commands en resulterende events; Schedule als OHS richting andere contexten"
- Data-distributie-ontwerpdocument §1.4: "Johan | Schedule, Registration | PostgreSQL read-model (Schedule), PostgreSQL (Registration)"
- Event-Storming §1.1: "Johan Hendriks | Schedule, Registration"

---

## Tech stack

- Java 21, Spring Boot 3.3
- Spring Data JPA + Hibernate + PostgreSQL
- Spring AMQP / RabbitMQ (topic exchange)
- Flyway voor schema-migraties
- Optioneel: Redis voor hot-data caching (Schedule read-model)

---

## Interfaces met teamgenoten

| Richting | Wat | Van/naar |
|----------|-----|---------|
| Registration **ontvangt** | `event.published`, `event.cancelled`, `event.updated` | Wessel (Event BC) |
| Registration **ontvangt** | JWT / userId | Joshua (User/Profile BC via API Gateway) |
| Registration **publiceert** | `registration.reserved.v1`, `registration.confirmed.v1`, `registration.waitlisted.v1`, `registration.cancelled.v1` | Glenn (Messaging, Notification, Audit BC's) |
| Schedule **ontvangt** | `registration.#` events | Eigen Registration service |
| Schedule **ontvangt** | `event.#` events | Wessel (Event BC) |
| Schedule **wordt bevraagd** | `GET /schedule/events/{id}`, `GET /schedule/users/{id}` | UI / BFF |

---

## Zie ook

- `registration-service/CLAUDE.md` — alle details voor de Registration implementatie
- `schedule-service/CLAUDE.md` — alle details voor de Schedule implementatie
