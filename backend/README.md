# BhoomiSetu Backend

Node.js + Express. Two storage systems, each doing a different job:

- **`users.json`** (a flat file, no database) — accounts and login. Simple on purpose; fine for development, not meant to scale.
- **MongoDB** — survey requests and chat messages. This is the part that genuinely needs a real, shared database: a request created on one phone has to be visible on a completely different phone, which local-only storage can never do.

No cloud account or credentials needed for any of this — everything below runs entirely on your own computer.

## Endpoints

**Auth** (`users.json`)
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/users/me`

**Survey requests** (MongoDB — all require a Bearer token from login/register)
- `POST /api/v1/requests` — create a request
- `GET /api/v1/requests?district=&mandal=&status=OPEN` — browse open requests
- `GET /api/v1/requests/mine` — your own submitted requests
- `GET /api/v1/requests/assigned` — your accepted jobs (surveyor)
- `POST /api/v1/requests/:id/accept`
- `POST /api/v1/requests/:id/complete`
- `GET /api/v1/requests/:id/messages`
- `POST /api/v1/requests/:id/messages`

Plus a Socket.io connection on the same port for live updates (new requests, request status changes, new chat messages) -- see `server.js` for the `join_surveyor_feed` / `join_request_room` events.

## Step 1: Install and start MongoDB locally

You need `mongod` running before starting the backend, or every request-related call will fail (auth still works fine without it, since that part uses `users.json`).

- **Windows**: install MongoDB Community Server from mongodb.com, it installs as a service and starts automatically.
- **Mac**: `brew tap mongodb/brew && brew install mongodb-community && brew services start mongodb-community`
- **Linux**: install via your package manager, then `sudo systemctl start mongod`

Verify it's running: `mongosh` should connect without an error (Ctrl+D to exit).

## Step 2: Install and run the backend

```bash
cd backend
npm install
npm start
```

You should see:
```
MongoDB connected: mongodb://127.0.0.1:27017/bhoomisetu
BhoomiSetu Backend running at http://0.0.0.0:3000/api/v1
```

If you see a MongoDB connection error instead, `mongod` isn't running -- go back to Step 1.

## Step 3: Verify it works, before touching the Android app at all

Register a test user with `curl` (or Postman, or any HTTP client):

```bash
curl -X POST http://localhost:3000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","phone":"9999999999","email":"test@test.com","password":"test123","role":"USER"}'
```

You should get back a JSON response with `accessToken`, `refreshToken`, and a `user` object. Copy the `accessToken` value, then try creating a survey request:

```bash
curl -X POST http://localhost:3000/api/v1/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer PASTE_ACCESS_TOKEN_HERE" \
  -d '{"requesterName":"Test User","requesterPhone":"9999999999","district":"Guntur","mandal":"Guntur West","village":"","surveyNumber":"103","description":"Test request"}'
```

You should get back the saved request with a real MongoDB `id`. If both of those work, the backend is fully confirmed working end to end, independent of anything on the Android side.

## Connecting from the Android app

Same as before: `Constants.BASE_URL` in the app points at this server. For a single emulator via ADB reverse, `http://127.0.0.1:3000/api/v1/` works as-is. For two real/separate devices (e.g. testing user + surveyor roles on different phones), both need to reach your computer's actual LAN IP instead -- `http://<your-computer's-IP>:3000/api/v1/` -- since ADB reverse only maps one connected device's localhost, not a second physical phone's.
