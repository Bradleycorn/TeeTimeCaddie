---
name: github
description: The TeeTimeCaddie repo's GitHub conventions — the epic/story branch hierarchy, stacked sub-task PRs (gh stack) or a single PR for a one-sub-task issue, opening a PR against the right base, and triaging PR review comments. Use when starting work on a TTC Jira issue (story, defect, hotfix) and a branch is needed, when a sub-task is finished and its stacked PR should be opened, when a story PR should be opened, or when asked to address feedback, comments, or a review on a PR.
---

# Working with GitHub on TeeTimeCaddie

`gh` is installed and authenticated, and resolves the repo from the checkout — the `{owner}` and
`{repo}` placeholders below are filled in by `gh api` itself.

This skill is GitHub only. It assumes you already have the issue's key, summary, and parent epic
key from Jira.

## Which part do you need

| You are about to | Read |
|---|---|
| Find or create a branch, update it from its base, or place out-of-scope changes | `references/branching.md` |
| Open the story PR or update its description | `references/pull-requests.md` |
| Create a sub-task's branch and stacked PR, push a fix up the stack, or check the stack's reviews | `references/stacked-prs.md` |
| Address review comments or feedback on a PR | `references/pr-feedback.md` |

## One PR or a stack

An issue with **two or more sub-tasks that produce code** gets a stack: one PR per sub-task,
stacked on the story branch, plus the story PR (`stacked-prs.md`).

An issue with **exactly one** such sub-task (a small defect or story) gets **no stack**: no
`subtask/` branch and no `gh stack`. Commit straight to the story branch and open the story PR,
ready for review, against the story's base (`pull-requests.md`). User-owned sub-tasks don't count;
they never get a branch or PR.

## Rules that always apply

- **Commit cadence:** at least one commit per Jira sub-task, and no commit spans two sub-tasks.
  PR-feedback fixes are separate follow-up commits, keyed to the sub-task that owns the changed
  code.
- **Stage only files you changed.** Never `git add -A` or `git add .` — name the files. If
  `git status` shows changes you didn't make, stop and ask; they are the user's work in progress.
- **A story PR targets the branch the story was cut from** — the epic branch, or `main` only when
  the story was cut from `main`. Never re-derive it as `main` by default.
- **Never create an epic branch without asking**, and never assume its base.
- **Never force-push by hand.** The one exception is `gh stack` (`rebase`, `push`, `sync`), which
  rebases sub-task layer branches and pushes them with `--force-with-lease`. Story, epic, and
  `main` branches are never rebased or force-pushed.
- **Query `gh` with `--json` and explicit fields.** Default human-readable output varies by `gh`
  version and can fail silently; `--json` and `gh api` do not.
- **Stop at "PR created."** That includes each sub-task PR in a stack. Merging a PR and deleting
  the branch are the user's, not yours. A stack is approved layer by layer and merged by the user
  only once every layer is approved; never ask for or offer a merge.
