---
name: github
description: The TeeTimeCaddie repo's GitHub conventions — the epic/story branch hierarchy, opening a PR against the right base, and triaging PR review comments. Use when starting work on a TTC Jira issue (story, defect, hotfix) and a branch is needed, when code is finished and a PR should be opened, or when asked to address feedback, comments, or a review on a PR.
---

# Working with GitHub on TeeTimeCaddie

Repo `https://github.com/Bradleycorn/TeeTimeCaddie.git`. `gh` is installed and authenticated.

This skill is GitHub only. It assumes you already have the issue's key, summary, and parent epic
key from Jira.

## Which part do you need

| You are about to | Read |
|---|---|
| Start work on an issue — find or create the branch | `references/branching.md` |
| Push finished code and open the PR | `references/pull-requests.md` |
| Address review comments or feedback on a PR | `references/pr-feedback.md` |

Read one. They cover different moments and don't overlap.

## Rules that always apply

- **Commit messages:** Commit frequently (once per Jira sub-task) rather than in one lump at the end.
- **A story PR never targets `main`.** It targets the branch the story was cut from.
- **Never create an epic branch without asking**, and never assume its base.
- **Never force-push.**
- **Query `gh` with `--json` and explicit fields.** Default human-readable output varies by `gh`
  version and can fail silently; `--json` and `gh api` do not.
- **Stop at "PR created."** Merging a PR and deleting the branch are the user's, not yours.
- `main` is always stable.
