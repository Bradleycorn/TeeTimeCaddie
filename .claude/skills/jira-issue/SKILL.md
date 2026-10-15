---
name: jira-issue
description: The workflow for implementing a TeeTimeCaddie (TTC) Jira story or defect end to end — reading it, planning it, breaking it into sub-tasks, moving statuses, committing per sub-task, the draft PR, and finishing it for review. Use when asked to implement, start, continue, or resume work on a TTC issue, or to pick up a TTC story's next sub-task.
---

# Working a TTC Jira issue

Procedure only. The Jira routing, custom field ids, issue types, assignee account ids, and
transition ids are in CLAUDE.md — use them from there.

Always:

- **Acceptance Criteria and Development Notes are custom fields.** Read with `fields: ["*all"]`,
  or they look empty.
- **Never move the story to Done.** Closing it after the PR merges is the user's job.
- **Transition by id**, not by matching status names.

## 1. Read and plan

- Read the issue including its **custom fields** (`fields: ["*all"]`). The Acceptance Criteria
  and Development Notes are custom fields, not standard ones — confirm you have both before
  planning. If the ACs look empty, the custom fields weren't fetched.
- Read everything else that is populated, and the parent epic.
- The plan must cover every AC. Map each AC to the sub-task that delivers it.
- The plan includes the git setup: which branches, and what each is based on. Set up the story's
  branches now, before any code.

## 2. Propose sub-tasks; create them only after approval

- Prefer **5–10 broad sub-tasks** over 20+ detailed ones. A step list in a description is fine
  but optional. Order them by dependency.
- Work the user must do themselves (console settings, adding packages, assets, and so on) gets its
  own sub-task.
- Present them in the plan. Create nothing in Jira until the user approves.
- Create them as the `Subtask` issue type, parented to the story.

## 3. Assign

- User-owned sub-tasks → Brad.
- All other sub-tasks, and the story itself → the "Claude Code" user.

## 4. Move statuses as work happens

- Story → In Progress when work begins.
- Each sub-task → In Progress before work on it starts, → Done once its work is committed.
- When everything is finished, story → IN REVIEW. Never Done.

## 5. Pause for user-owned sub-tasks

- Move the sub-task to In Progress, stop, and hand back with exactly what's needed. Resume only
  when told.
- The user's work may land outside the story branch, for example merged straight into the epic
  branch. After resuming, bring the story branch up to date by merging its base before
  continuing.

## 6. Commit per sub-task

Commit as each sub-task is completed, following the repo's commit conventions. At least one
commit per sub-task; no commit spans two.

## 7. Open the story PR as a draft

- Open the story PR as a draft once the first meaningful chunk of work is committed — typically
  after the first two sub-tasks. Its description tracks the sub-tasks as a checklist; keep it
  current as they finish.
- When the user leaves review comments, handle them using the repo's PR-feedback conventions.
  A fix belongs to the sub-task that owns the changed code: reopen that sub-task (step 8) and
  key the commit to it.

## 8. Rework after a sub-task is Done

- Reopen that sub-task: In Progress, then Done again once the rework is committed.
- Don't create a new sub-task, and don't fold the work into a different one.

## 9. Jira comments

- Post verification results (what was run, on which platforms, what passed) on the relevant
  sub-task.
- When later work supersedes an earlier comment, add a new comment that says so. Never edit or
  delete the earlier one — the history stays intact.

## 10. Definition of done

- [ ] All sub-tasks are Done.
- [ ] Everything is committed and pushed.
- [ ] The PR description is rewritten to match what was actually built.
- [ ] The PR is moved from draft to ready.
- [ ] The story is in IN REVIEW.

Stop there. Merging the PR and closing the story are the user's job.
