# World Garage

World Garage is a full-stack automotive community MVP where owners submit their
cars and administrators review them before they appear in a public, magazine-style
garage.

![Blue Nissan 370Z featured by World Garage](frontend/src/assets/world-garage-hero.jpg)

## MVP features

- Public, searchable garage of approved vehicles
- Individual vehicle pages and public owner profiles
- Account registration, password login, session restoration, and logout
- Authenticated vehicle submissions with optional JPEG, PNG, or WebP uploads
- Personal garage showing pending, approved, and rejected submissions
- Owner-only deletion of vehicle submissions
- Administrator review queue with approve and reject actions
- Persistent local H2 data and protected local image storage
- Backend integration/unit tests and frontend component tests

At the deployment-ready MVP checkpoint, the complete suites contain 72 backend tests and 28
frontend tests. Deployment, email verification, password recovery, and social
features are intentionally listed as future work rather than presented as
finished functionality.

## Technology

| Layer | Technology |
| --- | --- |
| Frontend | Vue 3, TypeScript, Vite, Vue Router, Vitest |
| Backend | Java 21, Spring Boot, Spring MVC, Spring Data JPA, Spring Security |
| Data | H2 for local development; PostgreSQL + Flyway in production |
| Testing | JUnit 5, Mockito, MockMvc, Vitest, Vue Test Utils |

The frontend calls the backend through `/api`. During development, Vite proxies
those requests to `http://localhost:8080`, so session cookies and CSRF protection
work as same-origin requests from the browser.

## Run locally

Requirements:

- Java 21
- Node.js 24 and npm

Start the backend from PowerShell:

```powershell
cd backend
./mvnw.cmd spring-boot:run
```

In a second terminal, start the frontend:

```powershell
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. Use the same hostname consistently; browser
cookies for `localhost` and `127.0.0.1` are separate.

Local data is stored under `backend/data`, and uploaded files default to
`backend/uploads`. Both are excluded from Git.

## Create the first administrator

Public registration always creates a normal `USER`. On a fresh database, set
the bootstrap variables before starting the backend:

```powershell
$env:WORLD_GARAGE_ADMIN_EMAIL = "admin@example.com"
$env:WORLD_GARAGE_ADMIN_PASSWORD = "replace-with-a-strong-password"
$env:WORLD_GARAGE_ADMIN_DISPLAY_NAME = "World Garage Admin"
./mvnw.cmd spring-boot:run
```

The password is encoded before storage. If the email already belongs to an
account, that account is promoted without resetting its existing password.
Never commit real credentials; [.env.example](.env.example) documents the
available settings with placeholder values.

## Main API flows

| Method and path | Access | Purpose |
| --- | --- | --- |
| `GET /api/cars` | Public | List approved cars |
| `GET /api/cars/{id}` | Public | Read an approved car and safe owner summary |
| `POST /api/auth/register` | Public + CSRF | Register a normal user |
| `POST /api/auth/login` | Public + CSRF | Start an authenticated session |
| `GET /api/cars/mine` | User | List the signed-in owner's submissions |
| `POST /api/uploads` | User + CSRF | Upload a vehicle image |
| `POST /api/cars` | User + CSRF | Create a pending vehicle submission |
| `DELETE /api/cars/{id}` | Owner + CSRF | Delete the owner's own submission |
| `GET /api/admin/cars` | Admin | List pending submissions |
| `PATCH /api/admin/cars/{id}/approve` | Admin + CSRF | Publish a submission |
| `PATCH /api/admin/cars/{id}/reject` | Admin + CSRF | Reject a submission |

Passwords and password hashes are never returned by an API. Pending images are
visible only to their owner and administrators; anonymous visitors can access an
uploaded image only after its vehicle is approved.

## Verification

```powershell
cd backend
./mvnw.cmd clean test
```

```powershell
cd frontend
npm ci
npm run test:unit -- --run
npm run build
```

GitHub Actions runs the same backend test, frontend test, and production build
checks for pushes and pull requests, and verifies that the deployment image builds.

## Deployment

The repository includes a multi-stage Docker build and a `prod` Spring profile
for Railway. The deployed container serves both the Vue application and API,
uses PostgreSQL through Railway's private network, and stores uploaded files on
an attached persistent volume.

See [docs/deployment.md](docs/deployment.md) for the required PostgreSQL variable
references, administrator secrets, `/data` volume, healthcheck, and acceptance
checklist. Deployment configuration is ready; a public service is not claimed as
live until those Railway resources have been created and verified.

## Project structure

```text
backend/   Spring Boot API, persistence, security, uploads, and JUnit tests
frontend/  Vue application, API clients, routed pages, and component tests
docs/      Focused technical notes
```

## Deliberate MVP boundaries

Email verification, password reset, user moderation, richer owner profiles,
multiple vehicle photos, likes, and comments remain on the roadmap.
