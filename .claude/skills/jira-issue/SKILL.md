---
name: jira-issue
description: Implements a TeeTimeCaddie (TTC) Jira story or defect end to end — planning it, splitting it into sub-tasks, giving each sub-task its own stacked PR (or one PR when there is only one sub-task), tracking status in Jira, and handing it off for review. Use when asked to implement, start, continue, or resume a TTC issue, or to pick up its next sub-task.
---

# Implementing a TTC story

Defects follow the same steps.

- **Never move the story to Done.**
- **One sub-task per turn.** When a sub-task's stacked PR is pushed and the sub-task is IN
  REVIEW, stop and report: the PR URL and which sub-task is next. Do this even when asked to
  implement the whole story. Start the next sub-task only when the user says to.
- **One sub-task, one PR.** An issue with a single sub-task that produces code gets no stack.
  Follow "Single-sub-task issues" in `references/working.md` instead of the checklist below.

| You are | Read |
|---|---|
| Starting the story — no sub-tasks yet | `references/planning.md` |
| Working or resuming sub-tasks, opening the story PR, or handling review | `references/working.md` |

Copy this checklist and track progress (stacked issues):

```
- [ ] Read the story (custom fields included) and its epic
- [ ] Plan: every AC covered, branches named; propose sub-tasks
- [ ] After approval: create and assign sub-tasks; story → In Progress
- [ ] Set up and push the story branch
- [ ] Each sub-task: catch up on merges → In Progress → work → commit → stacked PR
      → IN REVIEW → stop
- [ ] Open the story PR as a draft once the first sub-task PR is merged into the story branch
- [ ] Final sub-task's PR up: story → IN REVIEW, stop
- [ ] Every sub-task PR merged: sub-tasks Done, rewrite the story PR description, mark it ready
```

Stop there. Merging PRs and closing the story are the user's.
