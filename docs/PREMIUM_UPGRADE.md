# Premium upgrade implementation

## Implemented in the Android app

- Four-destination navigation: Home, Explore, Saved, and Profile.
- Topic onboarding and a persisted personalized For You feed.
- Recent search suggestions and editable followed topics.
- Deterministic multi-source story clustering and a Compare Coverage screen.
- Room-backed seven-day feed cache with offline article indicators.
- Reader font-size, line-spacing, system/light/dark/sepia controls.
- Android text-to-speech, translation handoff, reading progress, takeaways, and source transparency.
- Configurable personalized daily briefing and quiet hours.
- Comment reporting, blocking, 1,000-character limits, and a posting cooldown.
- Analytics consent disabled by default.
- Firebase App Check, Crashlytics, Performance Monitoring, and a protected Functions news proxy.

## Secure backend

The `backend` Firebase Functions project aggregates Guardian, GNews, and Currents on the server. Provider credentials are Firebase secrets. The endpoint requires Firebase App Check outside the emulator and returns a normalized Guardian-compatible payload to the Android client.

The Android release build overwrites direct provider keys with empty strings. Set `NEWSYNK_BACKEND_URL` for production builds.

## Deliberate boundaries

The takeaways are extractive summaries of publisher-provided text, not generative AI. This avoids presenting unsupported claims as sourced facts. A future generative implementation should run on the server, preserve sentence-level citations, and clearly label generated content.

Comment reports are write-only for clients. A trusted moderation dashboard or Admin SDK worker is required to review them. Server-enforced rate limiting should be added at that layer if comments move behind an API.
