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
```

**Remember the base branch.** The PR must target it, and re-deriving it later is how a story PR
ends up pointed at `main` by mistake. State it back to the user when you report the new branch.
