# Working the sub-tasks

## Each sub-task

1. `Atlassian:transitionJiraIssue` → In Progress.
2. Do the work.
3. Commit, following the repo's commit conventions.
4. → Done.

## User-owned sub-tasks

Move it to In Progress, stop, and say exactly what's needed. Resume only when told. The user's
work may have landed on the story's base branch, so merge the base into the story branch before
continuing.

## Draft PR

Open the story PR as a draft after the first meaningful chunk of work is committed. Keep its
sub-task checklist current as sub-tasks finish.

## Review comments and rework

Handle review comments using the repo's PR-feedback conventions. Each fix belongs to the
sub-task that owns the changed code:

- Reopen that sub-task (In Progress), commit the fix under its key, then → Done.
- Never create a new sub-task or fold the work into another one.

The same applies to any rework after a sub-task is Done.

## Jira comments

- Post verification results on the relevant sub-task with `Atlassian:addCommentToJiraIssue`.
- When later work supersedes a comment, add a new comment saying so. Never edit or delete the
  old one.
