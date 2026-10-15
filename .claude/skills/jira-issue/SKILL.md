---
name: jira-issue
description: Implements a TeeTimeCaddie (TTC) Jira story or defect end to end — planning it, splitting it into sub-tasks, tracking status in Jira, and handing it off for review. Use when asked to implement, start, continue, or resume a TTC issue, or to pick up its next sub-task.
---

# Implementing a TTC story

Defects follow the same steps. Field, user, issue-type, and transition ids are in CLAUDE.md.

- Acceptance Criteria and Development Notes are **custom fields**. Read with
  `fields: ["*all"]` or they look empty.
- Transition by id. **Never move the story to Done.**

| You are | Read |
|---|---|
| Starting the story — no sub-tasks yet | `references/planning.md` |
| Working sub-tasks, opening the draft PR, or handling review | `references/working.md` |

Copy this checklist and track progress:

```
- [ ] Read the story (custom fields included) and its epic
- [ ] Plan: every AC covered, branches named; propose sub-tasks
- [ ] After approval: create and assign sub-tasks; story → In Progress
- [ ] Set up the story's branches
- [ ] Each sub-task: In Progress → work → commit → Done
- [ ] Open the story PR as a draft after the first meaningful chunk
- [ ] All sub-tasks Done and pushed; rewrite the PR description, mark it ready,
      story → IN REVIEW
```

Stop there. Merging the PR and closing the story are the user's.
