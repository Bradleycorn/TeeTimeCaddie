// Firestore security rules tests for the `courses` and `games` collections.
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
      players: [{ playerId: BRAD, name: 'Brad Ball', ...playerOverrides }],
      ...teeTimeOverrides,
    }],
    playerIds: [BRAD],
    invitees: {},
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

// Invitees as the app holds them: a phone number and the entry stored under it.
const MATT = { phone: '4155550188', name: 'Matt Chen', playerId: null, status: 'PENDING' };
const JEFF = { phone: '4155550142', name: 'Jeff Morrison', playerId: SAM, status: 'PENDING' };
const DANA_INVITE = { phone: DANA_PHONE, name: 'Dana', playerId: null, status: 'PENDING' };

/** The stored invitees map for [invitees]: keyed by phone, each entry carrying its order. */
function inviteesMap(invitees) {
  return Object.fromEntries(
    invitees.map(({ phone, ...entry }, order) => [phone, { ...entry, order }])
  );
}

/** A new game inviting [invitees], with invitedPhones kept in step. */
function invitingGame(invitees, overrides = {}) {
  return newGame({
    invitees: inviteesMap(invitees),
    invitedPhones: invitees.map((i) => i.phone),
    ...overrides,
  });
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
    const invitees = inviteesMap([MATT]);
    await assertFails(publish(signedIn(BRAD), newGame({ invitees, invitedPhones: [] })));
    await assertFails(
      publish(signedIn(BRAD), newGame({ invitees, invitedPhones: ['4155550142'] }))
    );
    await assertFails(
      publish(signedIn(BRAD), newGame({ invitees, invitedPhones: [MATT.phone, MATT.phone] }))
    );
  });

  test('denies invitees stored as a list', async () => {
    const game = newGame({ invitees: [MATT], invitedPhones: [MATT.phone] });
    await assertFails(publish(signedIn(BRAD), game));
  });

  test('denies a formatted phone number', async () => {
    await assertFails(publish(signedIn(BRAD), invitingGame([{ ...MATT, phone: '(415) 555-0188' }])));
  });

  test('denies a formatted phone number after the first invitee', async () => {
    await assertFails(
      publish(signedIn(BRAD), invitingGame([MATT, { ...JEFF, phone: '415-555-0142' }]))
    );
  });

  test('denies invitedPhones that name someone other than the invitees', async () => {
    const game = invitingGame([MATT, JEFF], { invitedPhones: [MATT.phone, '4155550199'] });
    await assertFails(publish(signedIn(BRAD), game));
  });

  test('denies an invitee without an order', async () => {
    const game = invitingGame([MATT]);
    delete game.invitees[MATT.phone].order;
    await assertFails(publish(signedIn(BRAD), game));
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
    await assertFails(publish(signedIn(BRAD), invitingGame([JEFF], { playerIds: [BRAD, SAM] })));
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

/** Seed a published game inviting Dana and Matt, without going through the rules. */
async function seedInvitingGame(teeTimeOverrides = {}, invitees = [DANA_INVITE, MATT]) {
  await env.withSecurityRulesDisabled(async (context) => {
    const game = invitingGame(invitees);
    game.teeTimes[0] = { ...game.teeTimes[0], ...teeTimeOverrides };
    await setDoc(doc(context.firestore(), 'games', 'game-1'), game);
  });
}

const BRAD_SLOT = { playerId: BRAD, name: 'Brad Ball' };
const DANA_SLOT = { playerId: DANA, name: 'Dana Park' };
const DANA_ENTRY = { name: 'Dana', playerId: null, status: 'PENDING', order: 0 };

/** Dana's answer as the app writes it: her own entry, plus a seat for a Yes. */
function answer(status, { entry = {}, seat = status === 'ACCEPTED', extra = {} } = {}) {
  const update = {
    [`invitees.${DANA_PHONE}`]: { ...DANA_ENTRY, status, playerId: DANA, ...entry },
    ...extra,
  };
  if (seat) {
    update.teeTimes = [{ time: '09:00', slots: 4, players: [BRAD_SLOT, DANA_SLOT] }];
    update.playerIds = [BRAD, DANA];
  }
  return updateDoc(doc(signedIn(DANA), 'games', 'game-1'), update);
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

describe('answering an invitation', () => {
  test('allows a Yes that takes the next seat', async () => {
    await seedInvitingGame();
    await assertSucceeds(answer('ACCEPTED'));
  });

  test('allows a No that changes nothing else', async () => {
    await seedInvitingGame();
    await assertSucceeds(answer('DECLINED'));
  });

  test('denies a Yes when every slot is taken', async () => {
    await seedInvitingGame({ slots: 1 });
    const update = {
      [`invitees.${DANA_PHONE}`]: { ...DANA_ENTRY, status: 'ACCEPTED', playerId: DANA },
      teeTimes: [{ time: '09:00', slots: 1, players: [BRAD_SLOT, DANA_SLOT] }],
      playerIds: [BRAD, DANA],
    };
    await assertFails(updateDoc(doc(signedIn(DANA), 'games', 'game-1'), update));
  });

  test('denies a Yes that does not take a seat', async () => {
    await seedInvitingGame();
    await assertFails(answer('ACCEPTED', { seat: false }));
  });

  test('denies a No that takes a seat', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { seat: true }));
  });

  test('denies a seat under a name other than your profile', async () => {
    await seedInvitingGame();
    const update = {
      [`invitees.${DANA_PHONE}`]: { ...DANA_ENTRY, status: 'ACCEPTED', playerId: DANA },
      teeTimes: [{ time: '09:00', slots: 4, players: [BRAD_SLOT, { ...DANA_SLOT, name: 'Tiger' }] }],
      playerIds: [BRAD, DANA],
    };
    await assertFails(updateDoc(doc(signedIn(DANA), 'games', 'game-1'), update));
  });

  test('denies a Yes that also removes another slot holder', async () => {
    await seedInvitingGame();
    const update = {
      [`invitees.${DANA_PHONE}`]: { ...DANA_ENTRY, status: 'ACCEPTED', playerId: DANA },
      teeTimes: [{ time: '09:00', slots: 4, players: [DANA_SLOT] }],
      playerIds: [DANA],
    };
    await assertFails(updateDoc(doc(signedIn(DANA), 'games', 'game-1'), update));
  });

  test('denies an answer that is not Yes or No', async () => {
    await seedInvitingGame();
    await assertFails(answer('MAYBE', { seat: false }));
    await assertFails(answer('MAYBE', { seat: true }));
  });

  test('denies an answer that does not record your uid', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { entry: { playerId: null } }));
    await assertFails(answer('DECLINED', { entry: { playerId: SAM } }));
  });

  test('denies changing anything else in your entry', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { entry: { name: 'Dana Park' } }));
    await assertFails(answer('DECLINED', { entry: { order: 5 } }));
  });

  test("denies answering for another invitee", async () => {
    await seedInvitingGame();
    const update = {
      [`invitees.${MATT.phone}`]: { name: 'Matt Chen', playerId: DANA, status: 'DECLINED', order: 1 },
    };
    await assertFails(updateDoc(doc(signedIn(DANA), 'games', 'game-1'), update));
  });

  test("denies changing another invitee's entry alongside your own", async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', {
      extra: { [`invitees.${MATT.phone}.status`]: 'DECLINED' },
    }));
  });

  test('denies an answer that also changes the game', async () => {
    await seedInvitingGame();
    await assertFails(answer('DECLINED', { extra: { date: '2026-06-14' } }));
    await assertFails(answer('DECLINED', { extra: { invitedPhones: [DANA_PHONE] } }));
  });

  test('denies a Yes that also changes the game', async () => {
    await seedInvitingGame();
    await assertFails(answer('ACCEPTED', { extra: { date: '2026-06-14' } }));
  });

  test('denies answering under a number that is not in invitedPhones', async () => {
    // A game only the rules' own pairing check keeps from existing: an invitees key with no
    // matching invitedPhones entry.
    await env.withSecurityRulesDisabled(async (context) => {
      const game = invitingGame([DANA_INVITE, MATT], { invitedPhones: [MATT.phone] });
      await setDoc(doc(context.firestore(), 'games', 'game-1'), game);
    });
    await assertFails(answer('DECLINED'));
  });

  test('denies changing an answer once given', async () => {
    await seedInvitingGame({}, [{ ...DANA_INVITE, status: 'DECLINED' }, MATT]);
    await assertFails(answer('ACCEPTED', { entry: { status: 'ACCEPTED' } }));
  });

  test('denies someone who was not invited answering', async () => {
    await seedInvitingGame({}, [MATT]);
    await assertFails(answer('DECLINED'));
  });

  test('denies a signed-out answer', async () => {
    await seedInvitingGame();
    const update = { [`invitees.${DANA_PHONE}`]: { ...DANA_ENTRY, status: 'DECLINED' } };
    await assertFails(updateDoc(doc(signedOut(), 'games', 'game-1'), update));
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
