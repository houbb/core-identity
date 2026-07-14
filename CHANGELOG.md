# Changelog

## 0.1.0 (2026-07-15)

### P0 - Engineering Foundation

- **core-identity-backend**: Identity core service with 5 technical tables
  - Flyway migrations (SQLite + MySQL)
  - Three-layer architecture (api/application/infrastructure)
  - Public API: meta, capabilities
  - Internal API: service tokens, system info, health, audit events
  - Service-to-service authentication (JWT-based internal tokens)
  - Request ID filter, unified error model (RFC 7807)
  - Audit foundation, outbox event pattern, idempotency records

- **core-identity-admin-backend**: Management BFF
  - IdentityInternalClient for calling Identity Backend
  - Service token caching and refresh
  - Admin API: bootstrap, overview, health, version, contracts
  - Development access protection (localhost-only in production)
  - Request ID pass-through

- **core-identity-web**: User self-service portal (Vue 3 + Vite)
  - Welcome page with version check
  - System unavailable page with diagnostics
  - Version incompatibility page
  - 404 page
  - Placeholder login/account pages

- **core-identity-admin-web**: Admin console (Vue 3 + Vite, dark theme)
  - System overview dashboard with status cards
  - Service health page
  - Contract compatibility page
  - Sidebar layout with navigation
  - Detail drawer for service diagnostics

- **Contracts**: OpenAPI 3.0 YAML specifications
  - Public API, Internal API, Admin API, Events, Error Codes

- **Build**: Maven parent POM, npm scripts, build-all scripts (Windows + Linux)

- **Tests**: Unit tests for all services, architecture boundary tests (ArchUnit)