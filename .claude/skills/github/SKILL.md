---
name: github
description: The TeeTimeCaddie repo's GitHub conventions — the epic/story branch hierarchy, stacked sub-task PRs (gh stack), opening a PR against the right base, and triaging PR review comments. Use when starting work on a TTC Jira issue (story, defect, hotfix) and a branch is needed, when a sub-task is finished and its stacked PR should be opened, when a story PR should be opened, or when asked to address feedback, comments, or a review on a PR.
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
| Open the story PR (draft or ready), update its description, or mark it ready | `references/pull-requests.md` |
| Create a sub-task's branch and stacked PR, push a fix up the stack, or catch up after merges | `references/stacked-prs.md` |
| Address review comments or feedback on a PR | `references/pr-feedback.md` |

Read one. They cover different moments and don't overlap.

## Rules that always apply

- **Commit cadence:** at least one commit per Jira sub-task, and no commit spans two sub-tasks.
  PR-feedback fixes are separate follow-up commits, keyed to the sub-task that owns the changed
  code.
- **Commit message format:** follow the commit-message convention in CLAUDE.md.
- **Stage only files you changed.** Never `git add -A` or `git add .` — name the files. If
  `git status` shows changes you didn't make, stop and ask; they are the user's work in progress.
- **A story PR never targets `main`.** It targets the branch the story was cut from.
- **A sub-task PR targets the layer below it** in the story's stack, and the bottom layer targets
  the story branch. `gh stack submit` sets these bases.
- **Never create an epic branch without asking**, and never assume its base.
- **Never force-push by hand.** The one exception is `gh stack` (`rebase`, `push`, `sync`), which
  rebases sub-task layer branches and pushes them with `--force-with-lease`. Story, epic, and
  `main` branches are never rebased or force-pushed.
- **Query `gh` with `--json` and explicit fields.** Default human-readable output varies by `gh`
  version and can fail silently; `--json` and `gh api` do not.
- **Stop at "PR created."** That includes each sub-task PR in a stack. Merging a PR and deleting
  the branch are the user's, not yours.
- `main` is always stable.
