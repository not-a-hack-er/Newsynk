# Firebase security review

Review date: 28 September 2026

## API keys

The Google Cloud credential inventory contains the Firebase-created Android and browser keys. Both keys have API allowlists limited to 25 Firebase-related APIs. The Generative Language API is not in either allowlist.

Neither key currently has an application restriction. Firebase API keys identify the project and are public by design; access to Firestore data is controlled by Security Rules, Authentication, IAM, and App Check. Application restrictions may still be added after testing against both debug and release certificate fingerprints.

## Firestore

The rules that were live at the start of the review correctly denied unmatched collections and scoped bookmarks to their owner. They did not prevent an authenticated user from replacing another user's vote or modifying another user's comment.

The repository's `firestore.rules` closes those gaps:

- Bookmarks remain owner-only.
- Each user can change only their own entry in an article's voter map.
- New comments must identify the authenticated user and start with no votes.
- Comment updates can change only the authenticated user's vote entry.
- Only a comment's owner can delete it.
- Everything else is denied.

These hardened rules were published to the `newsapp-4078f` Firebase project on 28 September 2026 at 12:28 pm IST. Firebase Console shows that version as the current starred ruleset.

The current local rules also add authenticated, create-only `comment_reports` writes for the new moderation flow. Those additions pass the emulator test suite, but they have not yet been published to the live project. Until they are deployed, the in-app report action will be rejected by the live rules; existing comment, vote, and bookmark behavior remains protected by the published ruleset above.

Deploy future reviewed rule and index changes with:

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

Test rule changes against a non-production Firebase project or the Firebase Emulator Suite before future deployments.
