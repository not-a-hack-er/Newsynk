const {after, before, beforeEach, describe, test} = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require("@firebase/rules-unit-testing");
const {
  doc,
  getDoc,
  setDoc,
  updateDoc,
} = require("firebase/firestore");

let environment;

before(async () => {
  environment = await initializeTestEnvironment({
    projectId: "demo-newsynk",
    firestore: {
      rules: fs.readFileSync(path.resolve(__dirname, "../../firestore.rules"), "utf8"),
    },
  });
});

beforeEach(async () => environment.clearFirestore());
after(async () => environment.cleanup());

describe("Newsynk Firestore rules", () => {
  test("bookmarks are private to their owner", async () => {
    const owner = environment.authenticatedContext("owner").firestore();
    const stranger = environment.authenticatedContext("stranger").firestore();
    const bookmark = doc(owner, "users/owner/bookmarks/story-1");

    await assertSucceeds(setDoc(bookmark, {url: "https://example.com/story"}));
    await assertFails(getDoc(doc(stranger, "users/owner/bookmarks/story-1")));
  });

  test("comments must identify the authenticated creator", async () => {
    const user = environment.authenticatedContext("user-1").firestore();
    const valid = {
      articleId: "story-1",
      userId: "user-1",
      username: "reader",
      content: "Useful context",
      timestamp: Date.now(),
      voters: {},
      parentCommentId: null,
    };

    await assertSucceeds(setDoc(doc(user, "comments/valid"), valid));
    await assertFails(setDoc(doc(user, "comments/forged"), {...valid, userId: "someone-else"}));
  });

  test("a voter can change only their own vote entry", async () => {
    await environment.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), "article_votes/story-1"), {
        voters: {alice: true},
      });
    });
    const bob = environment.authenticatedContext("bob").firestore();

    await assertSucceeds(updateDoc(doc(bob, "article_votes/story-1"), {
      voters: {alice: true, bob: false},
    }));
    await assertFails(updateDoc(doc(bob, "article_votes/story-1"), {
      voters: {alice: false, bob: false},
    }));
  });

  test("reports are write-only and tied to the reporter", async () => {
    const user = environment.authenticatedContext("reporter").firestore();
    const valid = {
      commentId: "comment-1",
      reporterId: "reporter",
      reason: "abuse",
      createdAt: new Date(),
      status: "open",
    };

    await assertSucceeds(setDoc(doc(user, "comment_reports/report-1"), valid));
    await assertFails(getDoc(doc(user, "comment_reports/report-1")));
    await assertFails(setDoc(doc(user, "comment_reports/report-2"), {...valid, reporterId: "other"}));
  });
});
