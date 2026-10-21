# Finding or creating the branch

Read this before the first commit of any issue.

## The hierarchy

```
main
 └── epic/TTC-100-user-management        from main, long-lived, merged to main manually
      ├── story/TTC-101-login-screen     from the epic branch, short-lived
      └── story/TTC-102-profile-screen
```

`hotfix/TTC-XXX-short-description` branches from `main` and goes back to `main` directly.

**Naming:** `<type>/<JIRA-KEY>-<kebab-case-summary>`. The description is a shortened form of the
issue summary — a few words, not the whole title. Defects use the `story/` prefix, same as stories.

## Pre-flight, in order

1. **Uncommitted changes?** `git status --porcelain`. If anything is there, **stop and ask the
   user what to do with it.** Do not stash, commit, or discard on your own.
2. `git fetch origin`
3. **Does the story branch already exist?** Check local *and* remote:
   `git branch -a --list '*TTC-XXX*'`. If it exists, check it out and continue there — do not
   create a second branch for the same issue.
4. **Does the epic branch exist?** Same check for the parent epic's key, local and `origin/`.

## If the epic branch does not exist

**Ask before creating it, and ask what it should be based on.** Don't assume `main`.

> This story belongs to Epic TTC-100. Should I create `epic/TTC-100-user-management`, and should
> it come from `main`?

## If the issue has no parent epic

Do not invent an epic branch to satisfy the diagram. Ask what the story branch should be based on,
suggesting `main`.

## Creating the story branch

```bash
git checkout epic/TTC-100-user-management   # or whatever base was agreed
git pull
git checkout -b story/TTC-101-login-screen
git push -u origin story/TTC-101-login-screen
```

Push it right away, empty: it is the trunk of the story's sub-task stack, and the first sub-task
PR targets it. Sub-task branches are created by `gh stack`, not by hand — see `stacked-prs.md`.

For a one-sub-task issue (see SKILL.md, "One PR or a stack") there is no stack: commit the work
directly on the story branch.

**Remember the base branch.** The PR must target it, and re-deriving it later is how a story PR
ends up pointed at `main` by mistake. State it back to the user when you report the new branch.

## Bringing in changes from the base

When the base branch has moved on (for example, the user merged their own work into the epic
branch), **merge** it into the story branch:

```bash
git checkout story/TTC-101-login-screen
git fetch origin
git merge origin/epic/TTC-100-user-management
git push
gh stack sync    # if the story has open sub-task layers: rebases them onto the new story branch
```

Never rebase a branch that has been pushed — that needs a force-push. The only exception is
sub-task layer branches, and only through `gh stack` (see `stacked-prs.md`).

## Changes outside the issue's scope

Tooling, skills, CLAUDE.md, and other changes that aren't part of the issue don't go on the story
branch. Put them on `chore/<short-description>` cut from `main`, with their own PR against `main`.
That keeps the story PR limited to the feature.
