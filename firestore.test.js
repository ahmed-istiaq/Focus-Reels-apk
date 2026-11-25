const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_focus_123";
const BOB_UID = "bob_focus_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function pastDate(offsetMs = 1000) {
  return new Date(Date.now() - offsetMs);
}

test("Unauthenticated user cannot read or write any Focus Reels documents", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauth.collection("users").doc(ALICE_UID).collection("sources").get()
  );
  await assertFails(
    unauth.collection("users").doc(ALICE_UID).collection("savedVideos").get()
  );
  await assertFails(
    unauth
      .collection("users")
      .doc(ALICE_UID)
      .collection("sources")
      .doc("src_1")
      .set({
        userId: ALICE_UID,
        name: "Channel",
        handleOrUrl: "https://youtube.com/@channel",
        platform: "YOUTUBE",
        category: "MOTIVATION",
        active: true,
        gradientIndex: 0,
        createdAt: pastDate(),
        updatedAt: pastDate(),
      })
  );
});

test("UserProfile create, partial update, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      language: "en",
      darkTheme: true,
      onboardingCompleted: true,
      dailyLimitMinutes: 20,
      endOfFeedVideoLimit: 10,
      pauseDuringPrayer: true,
      cityName: "Dhaka",
      latitude: 23.8103,
      longitude: 90.4125,
      selectedInterests: ["SELF_BUILDING", "ISLAMIC"],
      streakDays: 3,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update of mutable settings succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      language: "bn",
      dailyLimitMinutes: 25,
      darkTheme: false,
      updatedAt: pastDate(500),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: pastDate(200),
      updatedAt: pastDate(200),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastDate(200),
    })
  );

  // Reject cross-user read or write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      dailyLimitMinutes: 5,
      updatedAt: pastDate(200),
    })
  );
});

test("VideoSource create, pause toggle, list, and @DocumentId rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sources")
      .doc("src_1")
      .set({
        userId: ALICE_UID,
        name: "Yaqeen Institute",
        handleOrUrl: "https://youtube.com/@yaqeeninstitute",
        platform: "YOUTUBE",
        category: "ISLAMIC",
        active: true,
        gradientIndex: 2,
        createdAt: created,
        updatedAt: created,
      })
  );

  // List subcollection succeeds for owner
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sources").get()
  );

  // Toggle pause succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sources")
      .doc("src_1")
      .update({
        active: false,
        updatedAt: pastDate(500),
      })
  );

  // Reject extra `id` field (mirrors @DocumentId exclusion)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sources")
      .doc("src_2")
      .set({
        id: "src_2",
        userId: ALICE_UID,
        name: "Invalid",
        handleOrUrl: "@invalid",
        platform: "YOUTUBE",
        category: "OTHER",
        active: true,
        gradientIndex: 1,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sources")
      .doc("src_3")
      .set({
        userId: ALICE_UID,
        name: "Future",
        handleOrUrl: "@future",
        platform: "YOUTUBE",
        category: "OTHER",
        active: true,
        gradientIndex: 1,
        createdAt: new Date(Date.now() + 60000),
        updatedAt: new Date(Date.now() + 60000),
      })
  );

  // Cross-user list denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("sources").get()
  );
});

test("SavedVideo create (with and without optional youtubeVideoId) and note update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = pastDate(5000);

  // Without optional youtubeVideoId
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedVideos")
      .doc("vid_1")
      .set({
        userId: ALICE_UID,
        videoId: "vid_1",
        title: "Consistency Over Intensity",
        channelName: "Deep Focus Lab",
        platform: "YOUTUBE",
        category: "SELF_BUILDING",
        videoUrl: "https://youtube.com/@deepfocuslab",
        viewsCount: 45200,
        likesCount: 3800,
        durationSeconds: 75,
        note: "",
        gradientIndex: 0,
        createdAt: created,
        updatedAt: created,
      })
  );

  // With optional youtubeVideoId
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedVideos")
      .doc("vid_2")
      .set({
        userId: ALICE_UID,
        videoId: "vid_2",
        title: "Pasted Real Clip",
        channelName: "User Link",
        platform: "YOUTUBE",
        category: "MOTIVATION",
        youtubeVideoId: "dQw4w9WgXcQ",
        videoUrl: "https://youtu.be/dQw4w9WgXcQ",
        viewsCount: 1000,
        likesCount: 120,
        durationSeconds: 60,
        note: "Key takeaway",
        gradientIndex: 3,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Update note and category folder
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedVideos")
      .doc("vid_1")
      .update({
        note: "Start with 10 focused minutes every Fajr.",
        category: "ISLAMIC",
        updatedAt: pastDate(500),
      })
  );
});

test("WatchHistory and DailyLog create, update, and strict dateKey validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = pastDate(5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchHistory")
      .doc("vid_1")
      .set({
        userId: ALICE_UID,
        videoId: "vid_1",
        watchedAtMillis: Date.now(),
        notInterested: false,
        reported: false,
        createdAt: created,
        updatedAt: created,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dailyLogs")
      .doc("2026-11-25")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-25",
        watchedSeconds: 180,
        videosWatchedCount: 3,
        quranDone: true,
        prayerDone: true,
        learningDone: false,
        exerciseDone: false,
        createdAt: created,
        updatedAt: created,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dailyLogs")
      .doc("2026-11-25")
      .update({
        learningDone: true,
        watchedSeconds: 240,
        updatedAt: pastDate(500),
      })
  );

  // Invalid dateKey format rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dailyLogs")
      .doc("bad-date")
      .set({
        userId: ALICE_UID,
        dateKey: "bad-date",
        watchedSeconds: 0,
        videosWatchedCount: 0,
        quranDone: false,
        prayerDone: false,
        learningDone: false,
        exerciseDone: false,
        createdAt: created,
        updatedAt: created,
      })
  );
});
