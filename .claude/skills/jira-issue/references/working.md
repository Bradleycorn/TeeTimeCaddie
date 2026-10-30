# Working the sub-tasks

Each sub-task is a layer in the story's PR stack: its own branch and PR, stacked on the story
branch. The github skill's `references/stacked-prs.md` has the commands.

An issue with only one sub-task that produces code skips the stack — see "Single-sub-task issues"
at the end.

## Catch up first

Every time you start or resume, before touching the next sub-task:

1. Check every sub-task PR in the stack for new review comments and approvals (github
   `references/stacked-prs.md`, "Checking the stack"), and tick approved ones in the draft story
   PR's checklist. Unresolved feedback on any layer comes first — see "Review comments and
   rework".
2. If the user's own work landed on the epic branch, merge the epic into the story branch, push,
   and `gh stack sync`.

Approved sub-tasks stay IN REVIEW. Nothing merges, and nothing goes to Done, until the user merges
the whole stack.

## Each sub-task

1. `Atlassian:transitionJiraIssue` → In Progress.
2. Add its layer on top of the stack.
3. Do the work. See "Comments that point at later work".
4. Search the code for `TODO(<this sub-task's key>)` and resolve every match. Fix any older
   comment this work made untrue, whichever ticket wrote it.
5. Commit, following the repo's commit conventions.
6. Submit the stack so the sub-task's PR opens ready for review; set its title and body.
7. → IN REVIEW.
8. **Stop.** Report the PR URL and the next sub-task. Wait to be told to continue — approval is
   not a cue to merge or to start the next one.

## Comments that point at later work

Code comments describe the code as it is now. Never narrate another ticket's work in a comment
("TTC-100 opens the contacts flow from here", "responses arrive with TTC-70").

When a stub must point at later work, mark it `TODO(<key>): <what to do>` in the file's comment
syntax, with the key of the sub-task or story that will do it. Then comment on that issue in
Jira, naming the file, so whoever picks it up knows to clear it. This is the only form a future
ticket key takes in code.

## The final sub-task

In step 4, also clear every `TODO(<story key>)`. After step 7, also move the story → IN REVIEW,
then stop.

When every sub-task PR is approved, the user merges the stack into the story branch. When resumed
after that: `gh stack sync --prune`, move every sub-task → Done, rewrite the story PR description
to describe what was built, and mark it ready (github `references/pull-requests.md`).

## User-owned sub-tasks

Move it to In Progress, stop, and say exactly what's needed. Resume only when told. It gets no
layer or PR of its own. Move it to Done when the user says it's finished, then catch up — their
work may have landed on the epic branch.

## Review comments and rework

Handle review comments using the repo's PR-feedback conventions. Each fix belongs to the
sub-task that owns the changed code:

1. Reopen that sub-task (In Progress), even if its PR is approved.
2. Commit the fix on its layer, under its key.
3. `gh stack rebase --upstack`, then `gh stack push`.
4. → IN REVIEW.

Fixes to an earlier layer are the normal case, not an exception. Never create a new sub-task or
fold the work into another one. After the stack is merged, fixes from the story PR's review go on
the story branch under the owning sub-task's key; that sub-task stays Done.

## Jira comments

- Post verification results on the relevant sub-task with `Atlassian:addCommentToJiraIssue`.
- When later work supersedes a comment, add a new comment saying so. Never edit or delete the
  old one.

## Single-sub-task issues

No stack, no `subtask/` branch, no `gh stack`.

1. Sub-task and story → In Progress.
2. Do the work on the story branch, committing under the sub-task's key. Before pushing, search
   the code for `TODO(<sub-task key>)` and `TODO(<story key>)` and resolve every match.
3. Push and open the story PR, ready for review, against the story's base (github
   `references/pull-requests.md`).
4. Sub-task and story → IN REVIEW.
5. **Stop.** Report the PR URL.

Review fixes are committed on the story branch under the sub-task's key and pushed; the sub-task
goes back to In Progress, then IN REVIEW. When resumed after the user has merged the PR, move the
sub-task to Done. The story stays where it is; closing it is the user's.
