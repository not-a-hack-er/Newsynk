# Security policy

## Reporting a vulnerability

Please report security issues privately to `letnewsynk@gmail.com`. Do not open a public issue containing credentials, personal information, or an exploitable vulnerability.

Include the affected version, reproduction steps, impact, and any suggested mitigation. Reports will normally receive an initial response within 48 hours.

## Credential handling

The repository intentionally excludes:

- `local.properties`
- `google-services.json`
- Android signing keystores
- API keys and signing passwords

Use `local.properties.example` and `google-services.json.example` as templates only. Store release keystores outside the checkout and use GitHub Actions secrets for automated releases.

If a real credential has ever been committed, removing the file in a later commit is not sufficient. Rotate the credential, restrict the replacement, and consider purging the old value from Git history.

## Firebase and Google Cloud checklist

1. Restrict the Android/Firebase API key to the Newsynk package (`com.abpvt.newsapp`) and the expected certificate fingerprints where the API supports application restrictions.
2. Restrict the key to only the Google APIs required by Newsynk.
3. Enable App Check where practical.
4. Require authenticated access in Firestore rules and validate document ownership server-side.
5. Review Authentication, Firestore, and Cloud Messaging usage for unexpected activity.
6. Keep debug and production Firebase projects separate when possible.
