# Core Identity

Identity and Access Management for Core Platform.

## Architecture

Four sub-projects with clear boundaries:

```
core-identity/
├── core-identity-backend/       # Identity core service (sole data owner)
├── core-identity-web/           # User self-service portal (Vue 3)
├── core-identity-admin-backend/ # Management BFF (calls Identity via Internal API)
├── core-identity-admin-web/     # Admin console (Vue 3, dark theme)
├── contracts/                   # OpenAPI specifications
├── docs/                        # Design documents
└── scripts/                     # Build scripts
```

## Responsibilities

- **core-identity-backend**: Users, organizations, authentication, authorization. **Only service with database access.**
- **core-identity-web**: Registration, login, account management for end users and organization members.
- **core-identity-admin-backend**: Platform admin operations — **BFF pattern, no direct database access.**
- **core-identity-admin-web**: Admin console for platform super admins, security, audit.

## Quick Start

```bash
# Build all
scripts/build-all.bat    # Windows
scripts/build-all.sh     # Linux/macOS

# Start Identity Backend (port 8101)
cd core-identity-backend
mvn spring-boot:run

# Start Admin Backend (port 8102)
cd core-identity-admin-backend
mvn spring-boot:run

# Start User Web (port 5173)
cd core-identity-web
npm install && npm run dev

# Start Admin Web (port 5174)
cd core-identity-admin-web
npm install && npm run dev
```

## API Endpoints

### Public API (8101)
- `GET /api/v1/identity/meta` — Service metadata
- `GET /api/v1/identity/capabilities` — Available capabilities

### Internal API (8101, service auth required)
- `POST /internal/v1/identity/service-tokens` — Issue service token
- `GET /internal/v1/identity/system/info` — System information
- `GET /internal/v1/identity/system/health` — Health check
- `POST /internal/v1/identity/audit-events` — Record audit event

### Admin API (8102)
- `GET /admin-api/v1/identity/bootstrap` — Initial load
- `GET /admin-api/v1/identity/system/overview` — Aggregated status
- `GET /admin-api/v1/identity/system/health` — Health aggregation
- `GET /admin-api/v1/identity/system/version` — Version info
- `GET /admin-api/v1/identity/system/contracts` — Contract compatibility

## Configuration

### Identity Backend
```yaml
server.port: 8101
core.identity.instance-name: Core Identity
core.internal-auth.token-ttl-seconds: 600
```

### Admin Backend
```yaml
server.port: 8102
core.identity.base-url: http://localhost:8101
core.admin.development-access: true  # localhost-only in production
```

## Database

Default: SQLite (`./data/core-identity.db`). Production: MySQL.

Tables (P0 technical foundation):
- `identity_instance_metadata`
- `identity_internal_client`
- `identity_audit_event`
- `identity_outbox_event`
- `identity_idempotency_record`

Migrations: Flyway (SQLite + MySQL dual profiles).

## Current Status

**P0 Complete**: Engineering foundation with four sub-projects, internal service auth, audit, outbox, idempotency.

P1 will add: user registration, login, organizations, RBAC.

## License

MIT