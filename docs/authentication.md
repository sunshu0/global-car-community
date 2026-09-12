# Session authentication

## Scope

Implemented: registration, password login, current-user lookup, logout, frontend
forms, session restoration on page reload, and CSRF protection for writes.
Registration creates a USER; it does not log the user in automatically. Vehicle
ownership and the role-protected administrator review UI use this authentication
layer but are documented at project level in the root README.

## Request flow

1. GET /api/auth/csrf returns a token and headerName and establishes a session cookie.
2. POST /api/auth/register sends JSON plus that CSRF header. Success: 201.
3. POST /api/auth/login sends email/password plus the CSRF header. Success: 200.
   Authentication checks the stored password hash. Session ID and CSRF secret rotate.
4. GET /api/auth/me uses the session cookie. Success: id, email, displayName, role.
   Anonymous requests receive 401, without a browser Basic-auth challenge.
5. Fetch a fresh CSRF token, then POST /api/auth/logout. Success: 204.
   Spring Security invalidates the session and deletes JSESSIONID.

The current user's email comes from Authentication, not a client-supplied user ID.
Passwords/hashes are never part of /me responses. No passwords or auth tokens are
stored in localStorage. Auth requests use same-origin cookies through Vite's proxy.
Wrong credentials return 401, missing/invalid CSRF returns 403, and duplicate email
returns 409. Validation failures return 400 with field messages.

## Local development

Start the backend from backend with ./mvnw.cmd spring-boot:run, then start the
frontend from frontend with npm run dev. Visit http://localhost:5173/account.
Use one hostname consistently; localhost and 127.0.0.1 do not share cookies.
The account and vehicle submission forms are separate routed pages.

H2 stores user records in backend/data. Sessions are in memory: restarting the
backend signs users out but does not delete their accounts.
Session idle timeout is 30 minutes. Cookies are HttpOnly and SameSite=Lax.
For production HTTPS, set SESSION_COOKIE_SECURE=true and configure the HTTPS
reverse proxy correctly. This local configuration is not a complete deployment.

## Verification

- backend: ./mvnw.cmd clean test
- frontend: npm run test:unit -- --run
- frontend: npm run build

SessionFlowTest uses real HTTP CSRF tokens (not the csrf() helper) to verify login,
session ID rotation, identity on the next request, stale-token rejection and logout.
AuthPanel.spec.ts covers UI success and failure paths with mocked network responses.

Optional real-browser smoke test:

1. Start an isolated backend:
   ./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080 --spring.datasource.url=jdbc:h2:mem:auth-smoke --spring.jpa.hibernate.ddl-auto=create-drop"
2. From frontend run node scripts/auth-smoke.mjs.
   Requires Playwright and Edge; PLAYWRIGHT_MODULE_DIR can point to a directory
   containing the playwright package, and BROWSER_CHANNEL selects another channel.
   The script starts/stops its own Vite server on 15173. It registers a temporary
   account, logs in, reloads, checks cookies/mobile overflow, logs out, and tries
   a wrong password. Screenshots go to backend/target.
3. Stop the isolated backend. Its in-memory accounts are discarded.

## Remaining boundaries

- No email verification, password reset, rate limiting, or multi-instance session store.
- Vehicle submissions require an authenticated database user and a fresh CSRF
  token. New cars store that user as owner; public vehicle GET requests remain
  anonymous. Existing seeded cars are allowed to have no owner.
- HTTP Basic credentials remain accepted for existing API clients; browser APIs
  intentionally do not send WWW-Authenticate challenges.
