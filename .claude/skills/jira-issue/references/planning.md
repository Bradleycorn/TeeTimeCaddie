# Planning the story

## Read

`Atlassian:getJiraIssue` with `fields: ["*all"]`. Confirm both custom fields — Acceptance
Criteria and Development Notes — have content before planning. Read the parent epic too.

## Plan

- Map each AC to the sub-task that delivers it. Every AC must be covered.
- Name the branches and what each is based on.

## Propose sub-tasks

- 5–10 broad sub-tasks, ordered by dependency. A step list in a description is optional.
- Work the user must do themselves (console settings, adding packages, assets) gets its own
  sub-task.
- Create nothing in Jira until the user approves.

## Create and assign

- `Atlassian:createJiraIssue`, issue type `Subtask`, parented to the story.
- Assign user-owned sub-tasks to the user (Brad Ball). Assign every other sub-task, and the
  story, to Claude Code.
