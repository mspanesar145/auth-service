# Auth Service

Starter authentication microservice for the Event Management platform.

## Stack
- Java 21, Spring Boot 3.5
- PostgreSQL + Flyway
- Spring Security resource-server JWT validation
- RS256 signed access tokens and a JWKS endpoint
- Opaque refresh tokens stored only as SHA-256 hashes
- OTP challenge persistence with expiry, resend cooldown and attempt limits

## Responsibilities and boundaries
Auth Service owns identity credentials, phone verification, login, access/refresh tokens and sessions. User Service owns profile data (name, email, picture, timezone, preferences). Calendar Service owns Google/Microsoft calendar OAuth tokens and synchronization. Google sign-in (identity login) is distinct from Google Calendar authorization.

## Run locally
Requirements: JDK 21, Maven 3.9+, Docker.

```bash
docker compose up -d
mvn spring-boot:run
```

Service runs at `http://localhost:8081`.

## API
All phone numbers must be E.164 format (example `+919876543210`).

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/otp/request` | Request signup/login OTP |
| POST | `/api/v1/auth/otp/verify` | Verify OTP; create identity on first successful verification; return tokens |
| POST | `/api/v1/auth/token/refresh` | Rotate refresh token and issue access token |
| POST | `/api/v1/auth/logout` | Revoke one refresh session |
| POST | `/api/v1/auth/logout-all` | Revoke all refresh sessions for authenticated user |
| GET | `/.well-known/jwks.json` | Public signing keys for downstream JWT validation |

### Request examples

Request OTP:
```json
{"phoneNumber":"+919876543210"}
```

Verify OTP:
```json
{"phoneNumber":"+919876543210","otp":"123456","deviceName":"iPhone 15"}
```

Refresh:
```json
{"refreshToken":"<opaque-refresh-token>"}
```

## Development OTP
With the default profile, OTP is printed to application logs for local development only. Do not use this delivery implementation in production. Activate `prod` and configure `SMS_GATEWAY_URL` and `SMS_API_KEY` for the generic SMS gateway adapter, or replace it with your provider SDK (Twilio, MSG91, AWS SNS, etc.).

## JWT key management
The included RSA key pair is development-only and is embedded in `application.yml` for a self-contained starter. Generate a unique key pair and inject it from a secret manager/Kubernetes Secret in every non-local environment. Keep the private key secret; distribute only JWKS public keys. Implement key rotation with overlapping `kid` values before production.

JWT claims include `iss`, `sub` (the stable auth user UUID), `iat`, `exp`, and `token_type=access`. Access token default lifetime is 15 minutes. Downstream services should validate signature, issuer, expiry and audience; add an explicit audience claim and audience validation before production rollout.

## Integration with User Service
After first successful phone verification, Auth Service creates an identity and returns its UUID as `userId`/JWT `sub`. The application should provision the corresponding User Service profile using an authenticated internal API or, preferably, a transactional outbox + `identity.user-created.v1` event. That provisioning workflow is not wired in this starter yet. User Service remains the owner of name/email/profile fields.

## Important production work still required
- Add a transactional outbox for identity-created, phone-verified, session-revoked and account-state events; publish to Kafka with retries and idempotent consumers.
- Add OTP provider delivery receipts, per-IP/device/phone rate limiting, abuse detection, CAPTCHA/risk checks and secure observability. Do not log OTPs or phone numbers in production.
- Add database row locking / concurrency protection for OTP verification and refresh-token rotation to prevent concurrent reuse races.
- Add refresh-token reuse detection and token-family revocation.
- Add account disable/deletion state synchronization from User Service; reject tokens for disabled/deleted accounts using short token TTL plus event-driven revocation/authorization strategy.
- Add Google identity login (OIDC) only if required; do not reuse calendar OAuth scopes/tokens for login.
- Add password credentials only if product requires them; phone OTP is the primary login flow in this starter.
- Add automated unit/integration tests, migrations review, secrets management, TLS, CORS policy, and API gateway rate limits.

This is a starter implementation, not a production-certified identity provider. For a security-critical production system, evaluate Keycloak or a managed OIDC provider rather than owning password, MFA, recovery, federation and key lifecycle code yourself.
