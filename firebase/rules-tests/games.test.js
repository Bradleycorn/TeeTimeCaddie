// Firestore security rules tests for the `courses` and `games` collections, and each game's
// `invitees` subcollection.
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
const SAM = 'sam-uid';
const DANA_PHONE = '5025552345';
const BRAD_PHONE = '5025551234';

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
      name: 'Brad Ball', email: 'brad@golf.app', phone: BRAD_PHONE,
    });
    await setDoc(doc(db, 'players', DANA), {
      name: 'Dana Park', email: 'dana@golf.app', phone: DANA_PHONE,
    });
    await setDoc(doc(db, 'players', SAM), {
      name: 'Sam Ortiz', email: 'sam@golf.app', phone: '5025553456',
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
      players: [{ playerId: BRAD, name: 'Brad Ball', phone: BRAD_PHONE, ...playerOverrides }],
      ...teeTimeOverrides,
    }],
    playerIds: [BRAD],
    invitedPhones: [],
    createdAt: serverTimestamp(),
    ...overrides,
  };
}

/**
 * Publish the way the app does: an optional new course, the game and one document per invitee,
 * atomically. [invitees] are the invitee documents to write, keyed by phone.
 */
function publish(db, game, { course, gameId = 'game-1', invitees = {} } = {}) {
  const batch = writeBatch(db);
  if (course) batch.set(doc(db, 'courses', course.nameKey), course);
  batch.set(doc(db, 'games', gameId), game);
  for (const [phone, invitee] of Object.entries(invitees)) {
    batch.set(doc(db, 'games', gameId, 'invitees', phone), invitee);
  }
  return batch.commit();
}

/** Seed a published game without going through the rules. */
async function seedGame(gameId = 'game-1', overrides = {}) {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), 'games', gameId), newGame(overrides));
  });
}

// Invitees as the app holds them: a phone number and the document stored under it.
const MATT = { phone: '4155550188', name: 'Matt Chen', playerId: null, status: 'PENDING' };
const JEFF = { phone: '4155550142', name: 'Jeff Morrison', playerId: SAM, status: 'PENDING' };
const DANA_INVITE = { phone: DANA_PHONE, name: 'Dana', playerId: null, status: 'PENDING' };

/** The invitee documents for [invitees], keyed by phone, each carrying its order. */
function inviteeDocs(invitees) {
  return Object.fromEntries(
    invitees.map(({ phone, ...invitee }, order) => [phone, { ...invitee, order }])
  );
}

/** Publish a game inviting [invitees], with invitedPhones kept in step. */
function publishInviting(db, invitees, { overrides = {}, docs = inviteeDocs(invitees), ...options } = {}) {
  const game = newGame({ invitedPhones: invitees.map((i) => i.phone), ...overrides });
  return publish(db, game, { invitees: docs, ...options });
}

describe('publishing a game with invitees', () => {
  test('allows app-user and managed invitees', async () => {
    await assertSucceeds(publishInviting(signedIn(BRAD), [MATT, JEFF]));
  });

  test('allows more invitees than slots', async () => {
    const invitees = [0, 1, 2, 3, 4].map((n) => ({ ...MATT, phone: `415555010${n}` }));
    await assertSucceeds(publishInviting(signedIn(BRAD), invitees));
  });

  test('allows 50 invitees in one write', async () => {
    const invitees = Array.from({ length: 50 }, (_, n) => ({ ...MATT, phone: `41555${String(n).padStart(5, '0')}` }));
    await assertSucceeds(publishInviting(signedIn(BRAD), invitees));
  });

  test('denies a game without invitedPhones', async () => {
    const game = newGame();
    delete game.invitedPhones;
    await assertFails(publish(signedIn(BRAD), game));
  });

  test('denies a game that still holds an invitees map', async () => {
    const docs = inviteeDocs([MATT]);
    const game = newGame({ invitedPhones: [MATT.phone], invitees: docs });
    await assertFails(publish(signedIn(BRAD), game, { invitees: docs }));
  });

  test('denies invitedPhones without their invitee documents', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [MATT], { docs: {} }));
  });

  test('denies an invitee document for a number not in invitedPhones', async () => {
    const docs = inviteeDocs([MATT, JEFF]);
    await assertFails(publishInviting(signedIn(BRAD), [MATT], { docs }));
  });

  test('denies a number invited twice', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [MATT, MATT], { docs: inviteeDocs([MATT]) }));
  });

  test('denies a formatted phone number', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [{ ...MATT, phone: '(415) 555-0188' }]));
  });

  test('denies a formatted phone number after the first invitee', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [MATT, { ...JEFF, phone: '415-555-0142' }]));
  });

  test('denies an invitee without an order', async () => {
    const docs = inviteeDocs([MATT]);
    delete docs[MATT.phone].order;
    await assertFails(publishInviting(signedIn(BRAD), [MATT], { docs }));
  });

  test('denies an invitee who has already responded', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [{ ...MATT, status: 'ACCEPTED' }]));
  });

  test('denies an invalid invitee after the first', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [MATT, { ...JEFF, status: 'DECLINED' }]));
    await assertFails(publishInviting(signedIn(BRAD), [MATT, { ...JEFF, name: '' }]));
  });

  test('denies an invitee with an extra field', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [{ ...MATT, extra: 'nope' }]));
  });

  test('denies a blank invitee name', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [{ ...MATT, name: '' }]));
  });

  test('denies more than 50 invitees', async () => {
    const invitees = Array.from({ length: 51 }, (_, n) => ({ ...MATT, phone: `41555${String(n).padStart(5, '0')}` }));
    await assertFails(publishInviting(signedIn(BRAD), invitees));
  });

  test('denies putting invitees in playerIds', async () => {
    await assertFails(publishInviting(signedIn(BRAD), [JEFF], { overrides: { playerIds: [BRAD, SAM] } }));
  });

  test('denies adding an invitee to a published game', async () => {
    await seedGame('game-1', { invitedPhones: [MATT.phone] });
    const { phone, ...invitee } = { ...MATT, order: 0 };
    await assertFails(setDoc(doc(signedIn(BRAD), 'games', 'game-1', 'invitees', phone), invitee));
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

  test("denies an organizer seat without their profile's number", async () => {
    await assertFails(publish(signedIn(BRAD), newGame({}, {}, { phone: DANA_PHONE })));
    const noPhone = newGame();
    delete noPhone.teeTimes[0].players[0].phone;
    await assertFails(publish(signedIn(BRAD), noPhone));
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

/**
 * Seed a published game inviting Dana and Matt, without going through the rules. [invitedPhones]
 * defaults to the invitees' numbers.
 */
async function seedInvitingGame(
  teeTimeOverrides = {},
  invitees = [DANA_INVITE, MATT],
  invitedPhones = invitees.map((i) => i.phone),
) {
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    const game = newGame({ invitedPhones });
    game.teeTimes[0] = { ...game.teeTimes[0], ...teeTimeOverrides };
    await setDoc(doc(db, 'games', 'game-1'), game);
    for (const [phone, invitee] of Object.entries(inviteeDocs(invitees))) {
      await setDoc(doc(db, 'games', 'game-1', 'invitees', phone), invitee);
    }
  });
}

const BRAD_SLOT = { playerId: BRAD, name: 'Brad Ball', phone: BRAD_PHONE };
const DANA_SLOT = { playerId: DANA, name: 'Dana Park', phone: DANA_PHONE };
const SEATED = {
  teeTimes: [{ time: '09:00', slots: 4, players: [BRAD_SLOT, DANA_SLOT] }],
  playerIds: [BRAD, DANA],
};

const inviteeRef = (db, phone = DANA_PHONE) => doc(db, 'games', 'game-1', 'invitees', phone);

/**
 * An answer as the app writes it, signed in as [uid]: the invitee document under [phone], plus
 * [game] changes to the game in the same write (a seat, for a Yes).
 */
function answer(status, {
  uid = DANA,
  phone = DANA_PHONE,
  invitee = {},
  game = status === 'ACCEPTED' ? SEATED : null,
} = {}) {
  const db = signedIn(uid);
  const batch = writeBatch(db);
  batch.update(inviteeRef(db, phone), { status, playerId: uid, ...invitee });
  if (game) batch.update(doc(db, 'games', 'game-1'), game);
  return batch.commit();
}

describe('reading games as an invitee', () => {
  test('allows an invitee to read the game', async () => {
    await seedInvitingGame();
    await assertSucceeds(getDoc(doc(signedIn(DANA), 'games', 'game-1')));
  });

  test("allows the Games tab's invitation query for your own number", async () => {
    await seedInvitingGame();
    const invitations = query(
      collection(signedIn(DANA), 'games'),
      where('invitedPhones', 'array-contains', DANA_PHONE),
      where('date', '>=', '2026-06-12'),
      orderBy('date'),
    );
    await assertSucceeds(getDocs(invitations));
  });

  test("denies the invitation query for someone else's number", async () => {
    await seedInvitingGame();
    const invitations = query(
      collection(signedIn(SAM), 'games'),
      where('invitedPhones', 'array-contains', DANA_PHONE),
      where('date', '>=', '2026-06-12'),
      orderBy('date'),
    );
    await assertFails(getDocs(invitations));
  });

  test('denies someone who was not invited', async () => {
    await seedInvitingGame();
    await assertFails(getDoc(doc(signedIn(SAM), 'games', 'game-1')));
  });

  test('denies a signed-in user with no profile', async () => {
    await seedInvitingGame();
    await assertFails(getDoc(doc(signedIn('nobody-uid'), 'games', 'game-1')));
  });
});

describe('reading invitees', () => {
  const invitees = (db) => collection(db, 'games', 'game-1', 'invitees');

  test('allows a player in the game to read and list them', async () => {
    await seedInvitingGame();
    await assertSucceeds(getDoc(inviteeRef(signedIn(BRAD), MATT.phone)));
    await assertSucceeds(getDocs(invitees(signedIn(BRAD))));
  });

  test('allows an invitee to read and list all of them', async () => {
    await seedInvitingGame();
    await assertSucceeds(getDoc(inviteeRef(signedIn(DANA), MATT.phone)));
    await assertSucceeds(getDocs(invitees(signedIn(DANA))));
  });

  test('denies someone who cannot see the game', async () => {
    await seedInvitingGame();
    await assertFails(getDoc(inviteeRef(signedIn(SAM))));
    await assertFails(getDocs(invitees(signedIn(SAM))));
  });

  test('denies a signed-out read', async () => {
    await seedInvitingGame();
    await assertFails(getDoc(inviteeRef(signedOut())));
  });
});

describe('answering an invitation', () => {
  test('allows a Yes that takes the next seat', async () => {
    await seedInvitingGame();
    await assertSucceeds(answer('ACCEPTED'));
  });

  test('allows a No that changes only your invitation', async () => {
    await seedInvitingGame();
    await assertSucceeds(answer('DECLINED'));
  });

  test('denies a Yes when every slot is taken', async () => {
    await seedInvitingGame({ slots: 1 });
    const game = { ...SEATED, teeTimes: [{ ...SEATED.teeTimes[0], slots: 1 }] };
    await assertFails(answer('ACCEPTED', { game }));
  });

  test('denies a Yes that does not take a seat', async () => {
    await seedInvitingGame();
    await assertFails(answer('ACCEPTED', { game: null }));
  });

  test('denies taking a seat without a Yes', async () => {
    await seedInvitingGame();
    await assertFails(updateDoc(doc(signedIn(DANA), 'games', 'game-1'), SEATED));
  });

  test('denies a No that takes a seat', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { game: SEATED }));
  });

  test('denies a seat under a name other than your profile', async () => {
    await seedInvitingGame();
    const game = {
      ...SEATED,
      teeTimes: [{ time: '09:00', slots: 4, players: [BRAD_SLOT, { ...DANA_SLOT, name: 'Tiger' }] }],
    };
    await assertFails(answer('ACCEPTED', { game }));
  });

  test("denies a seat under a number other than your profile's", async () => {
    await seedInvitingGame();
    const game = {
      ...SEATED,
      teeTimes: [{ time: '09:00', slots: 4, players: [BRAD_SLOT, { ...DANA_SLOT, phone: MATT.phone }] }],
    };
    await assertFails(answer('ACCEPTED', { game }));
  });

  test('denies a Yes that also removes another slot holder', async () => {
    await seedInvitingGame();
    const game = { teeTimes: [{ time: '09:00', slots: 4, players: [DANA_SLOT] }], playerIds: [DANA] };
    await assertFails(answer('ACCEPTED', { game }));
  });

  test('denies an answer that is not Yes or No', async () => {
    await seedInvitingGame();
    await assertFails(answer('MAYBE', { game: null }));
    await assertFails(answer('MAYBE', { game: SEATED }));
  });

  test('denies an answer that does not record your uid', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { invitee: { playerId: null } }));
    await assertFails(answer('DECLINED', { invitee: { playerId: SAM } }));
  });

  test('denies changing anything else in your invitation', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { invitee: { name: 'Dana Park' } }));
    await assertFails(answer('DECLINED', { invitee: { order: 5 } }));
  });

  test('denies answering No for another invitee', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { phone: MATT.phone }));
  });

  test("denies taking a seat on another invitee's invitation", async () => {
    await seedInvitingGame();
    await assertFails(answer('ACCEPTED', { phone: MATT.phone }));
  });

  test('denies an answer that also changes the game', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { game: { date: '2026-06-14' } }));
    await assertFails(answer('DECLINED', { game: { invitedPhones: [DANA_PHONE] } }));
  });

  test('denies a Yes that also changes the game', async () => {
    await seedInvitingGame();
    await assertFails(answer('ACCEPTED', { game: { ...SEATED, date: '2026-06-14' } }));
  });

  test('denies answering under a number that is not in invitedPhones', async () => {
    // A game only the publish rules keep from existing: an invitee document whose number is not
    // in invitedPhones.
    await seedInvitingGame({}, [DANA_INVITE, MATT], [MATT.phone]);
    await assertFails(answer('DECLINED'));
  });

  test('denies changing an answer once given', async () => {
    await seedInvitingGame({}, [{ ...DANA_INVITE, status: 'DECLINED', playerId: DANA }, MATT]);
    await assertFails(answer('ACCEPTED'));
    await assertFails(answer('DECLINED'));
  });

  test('denies someone who was not invited answering', async () => {
    await seedInvitingGame({}, [MATT]);
    const { status, ...invitee } = { ...DANA_INVITE, status: 'DECLINED', playerId: DANA, order: 1 };
    await assertFails(setDoc(inviteeRef(signedIn(DANA)), { ...invitee, status }));
  });

  test('denies a signed-out answer', async () => {
    await seedInvitingGame();
    await assertFails(updateDoc(inviteeRef(signedOut()), { status: 'DECLINED' }));
  });
});

describe('changing games', () => {
  test('denies the organizer updating or deleting a game', async () => {
    await seedGame();
    const db = signedIn(BRAD);
    await assertFails(updateDoc(doc(db, 'games', 'game-1'), { date: '2026-06-14' }));
    await assertFails(deleteDoc(doc(db, 'games', 'game-1')));
  });

  test('denies the organizer removing an invitee', async () => {
    await seedInvitingGame();
    await assertFails(deleteDoc(inviteeRef(signedIn(BRAD))));
  });
});

/** The seat the organizer records for an invitee: their invited name and number. */
function recordedSeat(invitee, playerId = null) {
  return { playerId, name: invitee.name, phone: invitee.phone };
}

/** The game with [seat] appended to Brad's tee time and [playerIds] holding its slot holders. */
function seatedWith(seat, { slots = 4, playerIds = [BRAD] } = {}) {
  return { teeTimes: [{ time: '09:00', slots, players: [BRAD_SLOT, seat] }], playerIds };
}

/**
 * An answer the organizer records, as the app writes it: the status on the invitee document
 * under [phone], plus [game] changes to the game in the same write (a seat, for a Yes).
 */
function record(status, { uid = BRAD, phone = MATT.phone, invitee = {}, game = null } = {}) {
  const db = signedIn(uid);
  const batch = writeBatch(db);
  batch.update(inviteeRef(db, phone), { status, ...invitee });
  if (game) batch.update(doc(db, 'games', 'game-1'), game);
  return batch.commit();
}

describe('the organizer recording an answer', () => {
  // Dana's invitation names her own account, which owns her number. Jeff's names Sam's account,
  // which doesn't own Jeff's number. Matt was invited with no account at all.
  const DANA_CLAIMED = { ...DANA_INVITE, playerId: DANA };
  const seedAll = (teeTimeOverrides = {}) =>
    seedInvitingGame(teeTimeOverrides, [DANA_CLAIMED, MATT, JEFF]);

  test('allows a No that changes only the invitation', async () => {
    await seedAll();
    await assertSucceeds(record('DECLINED'));
  });

  test('allows a Yes for an invitee with no account, seated without a playerId', async () => {
    await seedAll();
    await assertSucceeds(record('ACCEPTED', { game: seatedWith(recordedSeat(MATT)) }));
  });

  test('allows a Yes for an app user, seated under their account', async () => {
    await seedAll();
    const game = seatedWith(recordedSeat(DANA_CLAIMED, DANA), { playerIds: [BRAD, DANA] });
    await assertSucceeds(record('ACCEPTED', { phone: DANA_PHONE, game }));
  });

  test('seats a claimed account only when it owns the number', async () => {
    await seedAll();
    const asSam = seatedWith(recordedSeat(JEFF, SAM), { playerIds: [BRAD, SAM] });
    await assertFails(record('ACCEPTED', { phone: JEFF.phone, game: asSam }));
    await assertSucceeds(record('ACCEPTED', { phone: JEFF.phone, game: seatedWith(recordedSeat(JEFF)) }));
  });

  test("denies seating an app user without their account", async () => {
    await seedAll();
    await assertFails(record('ACCEPTED', { phone: DANA_PHONE, game: seatedWith(recordedSeat(DANA_CLAIMED)) }));
    const notInPlayerIds = seatedWith(recordedSeat(DANA_CLAIMED, DANA));
    await assertFails(record('ACCEPTED', { phone: DANA_PHONE, game: notInPlayerIds }));
  });

  test('denies a Yes when every slot is taken', async () => {
    await seedAll({ slots: 1 });
    await assertFails(record('ACCEPTED', { game: seatedWith(recordedSeat(MATT), { slots: 1 }) }));
  });

  test('denies a Yes that does not take a seat', async () => {
    await seedAll();
    await assertFails(record('ACCEPTED'));
  });

  test('denies taking a seat without a Yes', async () => {
    await seedAll();
    await assertFails(updateDoc(doc(signedIn(BRAD), 'games', 'game-1'), seatedWith(recordedSeat(MATT))));
  });

  test('denies a No that takes a seat', async () => {
    await seedAll();
    await assertFails(record('DECLINED', { game: seatedWith(recordedSeat(MATT)) }));
  });

  test("denies a seat for someone other than the invitee answered for", async () => {
    await seedAll();
    await assertFails(record('ACCEPTED', { game: seatedWith(recordedSeat(JEFF)) }));
  });

  test('denies a seat under a name other than the one invited', async () => {
    await seedAll();
    const game = seatedWith({ ...recordedSeat(MATT), name: 'Tiger' });
    await assertFails(record('ACCEPTED', { game }));
  });

  test('denies a Yes that also changes the game', async () => {
    await seedAll();
    await assertFails(record('ACCEPTED', { game: { ...seatedWith(recordedSeat(MATT)), date: '2026-06-14' } }));
  });

  test('denies a No that also changes the game', async () => {
    await seedAll();
    await assertFails(record('DECLINED', { game: { date: '2026-06-14' } }));
  });

  test("denies changing anything but the invitation's status", async () => {
    await seedAll();
    await assertFails(record('DECLINED', { invitee: { playerId: BRAD } }));
    await assertFails(record('DECLINED', { invitee: { name: 'Matthew Chen' } }));
  });

  test('denies an answer that is not Yes or No', async () => {
    await seedAll();
    await assertFails(record('MAYBE'));
  });

  test('denies changing an answer once given', async () => {
    await seedInvitingGame({}, [{ ...MATT, status: 'DECLINED' }]);
    await assertFails(record('ACCEPTED', { game: seatedWith(recordedSeat(MATT)) }));
    await assertFails(record('DECLINED'));
  });

  test('denies recording for a number that is not in invitedPhones', async () => {
    await seedInvitingGame({}, [DANA_INVITE, MATT], [DANA_PHONE]);
    await assertFails(record('DECLINED'));
  });

  test('denies anyone but the organizer recording an answer', async () => {
    await seedAll();
    await assertFails(record('DECLINED', { uid: SAM }));
    await assertFails(record('ACCEPTED', { uid: DANA, game: seatedWith(recordedSeat(MATT)) }));
  });
});
