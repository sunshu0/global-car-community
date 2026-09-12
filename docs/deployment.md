# Railway deployment

World Garage deploys as one Docker service. The Docker build compiles Vue, copies
the generated static files into the Spring Boot application, and packages one
executable JAR. Keeping the browser and API on one origin avoids cross-origin
session-cookie and CSRF configuration.

The production Spring profile uses Railway PostgreSQL through private-network
`PG*` variables. Flyway creates and versions the schema. Uploaded images are
written below the attached Railway volume mount path.

## 1. Push the repository

Railway builds the root [Dockerfile](../Dockerfile) from the GitHub repository.
Deploy only a commit whose backend tests, frontend tests, and frontend production
build pass.

## 2. Create the Railway project

1. Create a Railway project from the GitHub repository.
2. Add a PostgreSQL service to the same project.
3. Generate a public Railway domain for the World Garage application service.

## 3. Reference PostgreSQL variables

Add these variables to the application service. If the database service has a
different name, replace `Postgres` in the references.

```text
PGHOST=${{Postgres.PGHOST}}
PGPORT=${{Postgres.PGPORT}}
PGDATABASE=${{Postgres.PGDATABASE}}
PGUSER=${{Postgres.PGUSER}}
PGPASSWORD=${{Postgres.PGPASSWORD}}
```

Do not expose PostgreSQL publicly. Both services communicate over Railway's
private network.

## 4. Configure the administrator

Add secret application variables:

```text
WORLD_GARAGE_ADMIN_EMAIL=<your-admin-email>
WORLD_GARAGE_ADMIN_PASSWORD=<a-unique-strong-password>
WORLD_GARAGE_ADMIN_DISPLAY_NAME=World Garage Admin
```

The bootstrap password creates the first administrator. On later restarts, an
existing account is promoted without having its password reset.

## 5. Attach persistent image storage

Attach a Railway volume to the application service with mount path `/data`.
Railway supplies `RAILWAY_VOLUME_MOUNT_PATH` automatically, and the production
profile writes images to `${RAILWAY_VOLUME_MOUNT_PATH}/uploads`.

Without the volume, uploads disappear on redeploy. PostgreSQL data uses the
database service's own storage and does not belong on this application volume.

## 6. Deployment settings

- Dockerfile: `/Dockerfile`
- Healthcheck path: `/api/health`
- Start command: leave empty; the Docker image defines it
- Watch paths: `backend/**`, `frontend/**`, `Dockerfile`, `.dockerignore`

The application reads Railway's `PORT` automatically. HTTPS terminates at
Railway's proxy, while forwarded-header support and secure session cookies are
enabled by the production profile.

## 7. Acceptance checks

After deployment:

1. Open `/` and navigate directly to `/cars/1` to verify SPA route fallback.
2. Confirm `/api/health` returns `{"status":"UP"}`.
3. Register and sign in as a normal user.
4. Upload an image and submit a vehicle; confirm it remains pending and private.
5. Sign in as the configured administrator and approve the vehicle.
6. Sign out and confirm the vehicle and image are publicly visible.
7. Restart/redeploy the application and confirm PostgreSQL records and uploaded
   images remain available.

Set a Railway usage limit or alert before leaving the service running. The Hobby
subscription includes a usage allowance, but usage above that allowance can be
billed separately.
