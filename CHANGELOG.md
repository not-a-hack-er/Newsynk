# Changelog

## 2.3.0 - 2026-10-03

- Adopted the supplied Newsynk logo for the launcher, sign-in, Home header, and story-card sharing.
- Reduced feed jank by removing whole-list crossfades and per-card image shimmer, bounding image decode size, and avoiding repeated interaction fetches during scroll.
- Added immediate cached-feed display while fresh news loads, moved story grouping and digest scheduling off the main thread, and avoided duplicate search requests.

## 2.2.0 - 2026-10-03

- Preserved the original launcher icon and reused its existing mark on sign-in and the Home header for consistent branding.
- Added NewsData.io as a fourth optional news provider in debug builds and the App Check-protected backend.
- Fixed the feed merge dropping articles when providers return uneven result counts.
- Corrected publisher badges for Guardian sections and category-only articles, and accepted the different timestamp formats used by the providers.
- Corrected the About screen's outdated NewsAPI.org attribution and documented provider setup and free-tier caveats.
- Added NewsData.io mapping tests.

## 2.1.0 - 2026-10-03

- Replaced the unusable Guardian `test` key path with a public BBC/Guardian RSS fallback for headlines, categories, and recent-story search.
- Improved feed retry and empty states, search readability, category chips, bottom navigation, and app colors.
- Refreshed sign-in, registration, and password-reset screens with a cohesive editorial palette and clearer hierarchy.
- Added RSS parser and search-normalization tests.
- Fixed Saved-to-reader navigation so it opens the selected story, improved the Saved empty/error states, and standardized profile and article icons.

## 2.0.2 - 2026-10-03

- Fixed Google sign-in by using the web OAuth client generated from the Firebase configuration when no local override is provided.
- Shared one authentication state across sign-in, registration, password reset, and the rest of the app.
- Improved form validation, error messages, and authentication screen clarity.

## 2.0.1 - 2026-10-03

- Fixed an app-start crash caused by the missing Firebase Crashlytics Gradle plugin.
- Enabled Firebase Performance Monitoring bytecode instrumentation.
- Verified clean-install startup on an Android emulator with no fatal exception.

All notable changes to Newsynk will be documented here.

## 2.0.0 - Unreleased

- Added multi-provider aggregation using The Guardian, GNews, and Currents.
- Added category browsing, search, pagination, and pull-to-refresh.
- Added Google sign-in and password reset.
- Added an expanded article reader, executive takeaways, reading-time estimates, and story-card sharing.
- Added notification categories, quiet hours, daily digests, and notification deep links.
- Added a personalized For You feed, topic onboarding, recent searches, and a configurable briefing time.
- Added Room-powered offline feeds with seven-day cache retention.
- Added story clustering and a multi-source Compare Coverage experience.
- Added Home, Explore, Saved, and Profile bottom navigation.
- Added reader palettes, typography controls, text-to-speech, translation handoff, and source transparency.
- Added comment reporting, local user blocking, input limits, and posting cooldowns.
- Added an App Check-protected Firebase Functions proxy so release APKs contain no provider credentials.
- Added privacy opt-in analytics, Crashlytics, Performance Monitoring, and Play Integrity App Check.
- Added Firestore emulator rules tests, Compose UI tests, notification deep-link tests, a baseline-profile generator, and a cold-start macrobenchmark.
- Added Hilt dependency injection and a standalone Gradle project configuration.
- Added automated unit tests, CI builds, and tagged-release automation.
- Removed unrelated leftover test and theme artifacts.
