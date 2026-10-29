// Firestore security rules tests for the `courses` and `games` collections (TTC-68).
//
// Run from firebase/:
//   firebase emulators:exec --only firestore "npm --prefix rules-tests test"
//
// Each test publishes the way the app does — the course and the game in one atomic write — and
// then breaks exactly one thing, so a denial can only be caused by that one thing.

import { readFileSync } from 'node:fs';
import { after, before, beforeEach, describe, test } from 'node:test';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  limit,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
  writeBatch,
} from 'firebase/firestore';

const BRAD = 'brad-uid';
const DANA = 'dana-uid';

let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-ttc-rules',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});

after(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, 'players', BRAD), {
      name: 'Brad Ball', email: 'brad@golf.app', phone: '5025551234',
    });
    await setDoc(doc(db, 'players', DANA), {
      name: 'Dana Park', email: 'dana@golf.app', phone: '5025552345',
    });
    await setDoc(doc(db, 'courses', 'pebble beach'), {
      name: 'Pebble Beach', nameKey: 'pebble beach',
    });
  });
});

const signedIn = (uid) => env.authenticatedContext(uid).firestore();
const signedOut = () => env.unauthenticatedContext().firestore();

function newCourse(overrides = {}) {
  return { name: 'Bethpage Black', nameKey: 'bethpage black', ...overrides };
}

function newGame(overrides = {}, teeTimeOverrides = {}, playerOverrides = {}) {
  return {
    organizerId: BRAD,
    courseId: 'pebble beach',
    courseName: 'Pebble Beach',
    date: '2026-06-13',
    teeTimes: [{
      time: '09:00',
      slots: 4,
      players: [{ playerId: BRAD, name: 'Brad Ball', ...playerOverrides }],
      ...teeTimeOverrides,
    }],
    playerIds: [BRAD],
    invitees: [],
    invitedPhones: [],
    createdAt: serverTimestamp(),
    ...overrides,
  };
}

/** Publish the way the app does: an optional new course and the game, atomically. */
function publish(db, game, { course, gameId = 'game-1' } = {}) {
  const batch = writeBatch(db);
  if (course) batch.set(doc(db, 'courses', course.nameKey), course);
  batch.set(doc(db, 'games', gameId), game);
  return batch.commit();
}

/** Seed a published game without going through the rules. */
async function seedGame(gameId = 'game-1', overrides = {}) {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), 'games', gameId), newGame(overrides));
  });
}

const MATT = { name: 'Matt Chen', phone: '4155550188', playerId: null, status: 'PENDING' };
const JEFF = { name: 'Jeff Morrison', phone: '4155550142', playerId: DANA, status: 'PENDING' };

/** A new game inviting [invitees], with invitedPhones kept in step. */
function invitingGame(invitees, overrides = {}) {
  return newGame({ invitees, invitedPhones: invitees.map((i) => i.phone), ...overrides });
}

describe('publishing a game with invitees', () => {
  test('allows app-user and managed invitees', async () => {
    await assertSucceeds(publish(signedIn(BRAD), invitingGame([MATT, JEFF])));
  });

  test('allows more invitees than slots', async () => {
    const invitees = [0, 1, 2, 3, 4].map((n) => ({ ...MATT, phone: `415555010${n}` }));
    await assertSucceeds(publish(signedIn(BRAD), invitingGame(invitees, {}), { gameId: 'many' }));
  });

  test('denies a game without the invitee fields', async () => {
    const game = newGame();
    delete game.invitees;
    delete game.invitedPhones;
    await assertFails(publish(signedIn(BRAD), game));
  });

  test('denies invitedPhones that do not pair with the invitees', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ invitees: [MATT], invitedPhones: [] })));
    await assertFails(
      publish(signedIn(BRAD), newGame({ invitees: [MATT], invitedPhones: ['4155550142'] }))
    );
  });

  test('denies a formatted phone number', async () => {
    await assertFails(publish(signedIn(BRAD), invitingGame([{ ...MATT, phone: '(415) 555-0188' }])));
  });

  test('denies an invitee who has already responded', async () => {
    await assertFails(publish(signedIn(BRAD), invitingGame([{ ...MATT, status: 'ACCEPTED' }])));
  });

  test('denies an invitee with an extra field', async () => {
    await assertFails(publish(signedIn(BRAD), invitingGame([{ ...MATT, extra: 'nope' }])));
  });

  test('denies a blank invitee name', async () => {
    await assertFails(publish(signedIn(BRAD), invitingGame([{ ...MATT, name: '' }])));
  });

  test('denies more than 50 invitees', async () => {
    const invitees = Array.from({ length: 51 }, (_, n) => ({ ...MATT, phone: `41555${String(n).padStart(5, '0')}` }));
    await assertFails(publish(signedIn(BRAD), invitingGame(invitees)));
  });

  test('denies putting invitees in playerIds', async () => {
    await assertFails(publish(signedIn(BRAD), invitingGame([JEFF], { playerIds: [BRAD, DANA] })));
  });
});

describe('publishing a game', () => {
  test('allows the organizer to publish at an existing course', async () => {
    await assertSucceeds(publish(signedIn(BRAD), newGame()));
  });

  test('allows creating the course in the same write', async () => {
    const course = newCourse();
    const game = newGame({ courseId: course.nameKey, courseName: course.name });

    await assertSucceeds(publish(signedIn(BRAD), game, { course }));
  });

  test('allows every slot count from 1 to 4', async () => {
    for (const slots of [1, 4]) {
      await assertSucceeds(publish(signedIn(BRAD), newGame({}, { slots }), { gameId: `g${slots}` }));
    }
  });

  test('denies a signed-out publish', async () => {
    await assertFails(publish(signedOut(), newGame()));
  });

  test('denies publishing as someone else', async () => {
    await assertFails(publish(signedIn(DANA), newGame()));
  });

  test('denies an extra field', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ extra: 'nope' })));
  });

  test('denies a missing field', async () => {
    const game = newGame();
    delete game.date;
    await assertFails(publish(signedIn(BRAD), game));
  });

  test('denies an oversize course name', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ courseName: 'x'.repeat(101) })));
  });

  test('denies slot counts of 0 and 5', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({}, { slots: 0 })));
    await assertFails(publish(signedIn(BRAD), newGame({}, { slots: 5 })));
  });

  test('denies a slot count that is not a number', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({}, { slots: '4' })));
  });

  test('denies an organizer name that differs from their profile', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({}, {}, { name: 'Tiger Woods' })));
  });

  test('denies putting someone else in the first slot', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({}, {}, { playerId: DANA, name: 'Dana Park' })));
  });

  test('denies adding other players to playerIds', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ playerIds: [BRAD, DANA] })));
  });

  test('denies more than one tee time', async () => {
    const game = newGame();
    game.teeTimes = [game.teeTimes[0], { ...game.teeTimes[0], time: '09:10' }];
    await assertFails(publish(signedIn(BRAD), game));
  });

  test('denies a course that does not exist', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ courseId: 'augusta', courseName: 'Augusta' })));
  });

  test('denies a course name that differs from the stored spelling', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ courseName: 'PEBBLE BEACH' })));
  });

  test('denies a malformed date', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ date: 'June 13' })));
  });

  test('denies a client-chosen createdAt', async () => {
    await assertFails(publish(signedIn(BRAD), newGame({ createdAt: new Date(2020, 0, 1) })));
  });
});

describe('courses', () => {
  test('denies a course whose id is not its key', async () => {
    const db = signedIn(BRAD);
    await assertFails(setDoc(doc(db, 'courses', 'augusta'), newCourse()));
  });

  test('denies a course whose key does not match its name', async () => {
    const db = signedIn(BRAD);
    const course = newCourse({ name: 'Spam', nameKey: 'bethpage black' });
    await assertFails(setDoc(doc(db, 'courses', 'bethpage black'), course));
  });

  test('allows reading a course by key', async () => {
    await assertSucceeds(getDoc(doc(signedIn(DANA), 'courses', 'pebble beach')));
  });

  test('denies listing courses', async () => {
    await assertFails(getDocs(query(collection(signedIn(BRAD), 'courses'), limit(1))));
  });

  test('denies renaming or deleting a course', async () => {
    const db = signedIn(BRAD);
    await assertFails(updateDoc(doc(db, 'courses', 'pebble beach'), { name: 'Pebble' }));
    await assertFails(deleteDoc(doc(db, 'courses', 'pebble beach')));
  });
});

describe('reading games', () => {
  test('allows a member to read the game', async () => {
    await seedGame();
    await assertSucceeds(getDoc(doc(signedIn(BRAD), 'games', 'game-1')));
  });

  test('denies a non-member reading the game', async () => {
    await seedGame();
    await assertFails(getDoc(doc(signedIn(DANA), 'games', 'game-1')));
  });

  test('denies a signed-out read', async () => {
    await seedGame();
    await assertFails(getDoc(doc(signedOut(), 'games', 'game-1')));
  });

  test("allows the Games tab's membership query", async () => {
    await seedGame();
    const games = query(
      collection(signedIn(BRAD), 'games'),
      where('playerIds', 'array-contains', BRAD),
      where('date', '>=', '2026-06-12'),
      orderBy('date'),
    );
    await assertSucceeds(getDocs(games));
  });

  test('denies an unfiltered games query', async () => {
    await seedGame();
    await assertFails(getDocs(collection(signedIn(BRAD), 'games')));
  });
});

describe('changing games', () => {
  test('denies the organizer updating or deleting a game', async () => {
    await seedGame();
    const db = signedIn(BRAD);
    await assertFails(updateDoc(doc(db, 'games', 'game-1'), { date: '2026-06-14' }));
    await assertFails(deleteDoc(doc(db, 'games', 'game-1')));
  });
});
