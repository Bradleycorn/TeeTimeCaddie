# Working the sub-tasks

Each sub-task is a layer in the story's PR stack: its own branch and PR, stacked on the story
branch. The github skill's `references/stacked-prs.md` has the commands.

An issue with only one sub-task that produces code skips the stack — see "Single-sub-task issues"
at the end.

## Catch up first

Every time you start or resume, before touching the next sub-task:

1. Find which sub-task PRs the user has merged since you last stopped, and `gh stack sync --prune`.
2. Move each merged sub-task to Done.
3. If no story PR exists yet and a sub-task PR has merged into the story branch, open the story PR
   as a draft (github `references/pull-requests.md`). Otherwise update its checklist.
4. If the user's own work landed on the epic branch, merge the epic into the story branch, push,
   and `gh stack sync`.

## Each sub-task

1. `Atlassian:transitionJiraIssue` → In Progress.
2. Add its layer on top of the stack.
3. Do the work.
4. Commit, following the repo's commit conventions.
5. Submit the stack so the sub-task's PR opens ready for review; set its title and body.
6. → IN REVIEW.
7. **Stop.** Report the PR URL and the next sub-task. Wait to be told to continue.

## The final sub-task

After step 6, also move the story → IN REVIEW, then stop.

When resumed after the user has merged every sub-task PR into the story branch: catch up (all
sub-tasks → Done), rewrite the story PR description to describe what was built, and mark it ready.

## User-owned sub-tasks

Move it to In Progress, stop, and say exactly what's needed. Resume only when told. It gets no
layer or PR of its own. Move it to Done when the user says it's finished, then catch up — their
work may have landed on the epic branch.

## Review comments and rework

Handle review comments using the repo's PR-feedback conventions. Each fix belongs to the
sub-task that owns the changed code:

- Reopen that sub-task (In Progress) and commit the fix under its key.
- **Its PR is still open:** commit on its layer and push the stack up from there. → IN REVIEW.
- **Its PR is already merged:** commit on the story branch, push, and `gh stack sync`. → Done.
- Never create a new sub-task or fold the work into another one.

The same applies to any rework after a sub-task is IN REVIEW or Done.

## Jira comments

- Post verification results on the relevant sub-task with `Atlassian:addCommentToJiraIssue`.
- When later work supersedes a comment, add a new comment saying so. Never edit or delete the
  old one.

## Single-sub-task issues

No stack, no `subtask/` branch, no `gh stack`.

1. Sub-task and story → In Progress.
2. Do the work on the story branch, committing under the sub-task's key.
3. Push and open the story PR, ready for review, against the story's base (github
   `references/pull-requests.md`).
4. Sub-task and story → IN REVIEW.
5. **Stop.** Report the PR URL.

Review fixes are committed on the story branch under the sub-task's key and pushed; the sub-task
goes back to In Progress, then IN REVIEW. When resumed after the user has merged the PR, move the
sub-task to Done. The story stays where it is; closing it is the user's.
