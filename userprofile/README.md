# Core Monolith — Team Rocket Event Planner

Single Spring Boot deployable containing all Core bounded contexts.  
Architecture doc §3.1.1.

## Modules in this deployable
| Module | Owner | Database | Status |
|---|---|---|---|
| **userprofile** | Joshua | MongoDB | ✅ Implemented |
| event | Wessel | PostgreSQL | 🔲 To be added |
| registration | Johan | PostgreSQL | 🔲 To be added |
| venue | Wessel | PostgreSQL + PostGIS | 🔲 To be added |
| schedule | Johan | PostgreSQL | 🔲 To be added |

## Key Architecture Decisions

### Why a Monolith (not microservices) for these 5 contexts?
These contexts have **strong transactional cohesion**: an RSVP must validate event status,
venue capacity, and registration in one consistent operation. They share master data
(userId, eventId, venueId) and are deployed together daily.

### Module boundaries enforced by ArchUnit
`ModuleBoundaryTest` prevents modules from directly accessing each other's domain layers.
Cross-module communication via explicit ports or in-process domain events only.

### JWT validation
Validates RS256 JWTs issued by Identity service.
Fetches public key from `JWKS_URI` (Identity's `/.well-known/jwks.json`) at startup.
No per-request call to Identity.

## User/Profile Module

### Design decisions
- MongoDB for flexible, evolving profile schema (data distribution doc §2.2.2)
- `dietaryPreferences` and `accessibilityNeeds` published in `ProfileUpdated` event
  for Registration BC's guest-list read model (communication doc §3.1.2)
- Avatar stored in S3-compatible Object Storage (context map §6.3)
- Majority write concern for read-your-writes causal consistency

### ProfileUpdated event contract (Published Language)
| Field | Type | Consumed by |
|---|---|---|
| `userId` | UUID | Registration |
| `displayName` | String | Registration, Messaging |
| `dietaryPreferences` | String[] | Registration guest list |
| `accessibilityNeeds` | String | Registration guest list |
| `preferredLanguage` | ISO 639-1 | Notification |
| `updatedAt` | Instant | All consumers |
| `version` | Long | Late-arrival filtering |

### Endpoints
| Method | Path | Notes |
|---|---|---|
| GET | `/profiles/me` | own profile |
| PUT | `/profiles/me` | update (incl. dietary/accessibility) |
| PUT | `/profiles/me/avatar` | set avatar (upload to S3 first) |
| DELETE | `/profiles/me/avatar` | remove avatar |
| GET | `/profiles/{accountId}` | any profile |
| PUT | `/profiles/{accountId}` | admin update |
| GET | `/users/{id}/notification-prefs` | for Notification BC |

## Running locally
```bash
docker-compose up -d   # starts PostgreSQL, MongoDB, RabbitMQ, MinIO
./mvnw spring-boot:run
```

## Ubiquitous Language (Living Glossary)
See Event Storming document §5.2 for UserProfile terms with invariants and "EXPLICITLY NOT" rules.
