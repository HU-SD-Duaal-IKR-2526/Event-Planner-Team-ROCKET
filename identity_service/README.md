# Identity Service — Team Rocket Event Planner

Standalone Spring Boot microservice. Architecture doc §3.1.2.

## Responsibility
Authentication, JWT issuance, token management.
- Account registration, email verification
- Login → RS256 access token (15 min) + opaque refresh token (7 days)
- Token rotation on refresh, blacklisting on logout
- Password reset via event → Email BC

## Key Design Decisions

### RS256 Asymmetric JWT (OHS + Published Language)
Identity holds the **private key** and signs all tokens.  
All other BCs fetch the **public key** from `GET /.well-known/jwks.json` at startup.  
No per-request calls to Identity — validation is fully local.  
Communication doc §3.1.1: "asymmetrisch, met de publieke sleutel via JWK-endpoint."

### Email via RabbitMQ, not SMTP
Identity publishes `email.verification-requested.v1` and `email.password-reset-requested.v1`.  
The **Email BC** (Glenn) subscribes and handles SMTP, templates, retries, bounce handling.  
Communication doc §6.3: "Identity → Email: Upstream via domain events."

### Token Blacklist
On logout, the JWT `jti` claim is stored in `identity.token_blacklist` until natural expiry.  
`TokenBlacklistFilter` rejects blacklisted tokens even if signature is valid.

## JWT Payload (Published Language — do not rename without versioning)
| Field | Type | Description |
|---|---|---|
| `sub` | UUID | AccountId — stable user identifier |
| `iss` | String | `event-planner-identity` |
| `aud` | String[] | `["event-planner"]` |
| `iat` / `exp` | epoch sec | issued-at / expiry (15 min) |
| `jti` | UUID | JWT ID for blacklisting |
| `roles` | String[] | GUEST, ORGANIZER, ADMIN, COMPLIANCE |
| `email` | String | for Audit + Notification |
| `correlation_id` | UUID | request tracing |

## Endpoints
| Method | Path | Auth |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public |
| POST | `/auth/logout` | Bearer JWT |
| POST | `/auth/verify-email` | Public |
| POST | `/auth/resend-verification` | Public |
| POST | `/auth/password-reset/request` | Public |
| POST | `/auth/password-reset/confirm` | Public |
| GET | `/.well-known/jwks.json` | Public |
| GET | `/admin/accounts/{id}` | ROLE_ADMIN |
| POST | `/admin/accounts/{id}/roles` | ROLE_ADMIN |
| DELETE | `/admin/accounts/{id}/roles/{role}` | ROLE_ADMIN |

## Running locally
```bash
docker-compose up -d
./mvnw spring-boot:run
```

## Database
PostgreSQL — `identity` schema  
Flyway migrations in `src/main/resources/db/migration/`

## Ubiquitous Language (Living Glossary)
See Event Storming document §5.1 for full glossary with invariants and "EXPLICITLY NOT" rules.
