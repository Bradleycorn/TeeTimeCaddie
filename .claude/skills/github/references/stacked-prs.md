# Stacked sub-task PRs

Read this when a sub-task needs its branch and PR, when a fix must reach the layers above it, or
when the user has merged part of a stack.

Each sub-task of a story gets its own branch and PR, linked as a GitHub stack with the `gh stack`
extension. The story branch is the stack's **trunk**:

```
epic/TTC-100-user-management
 └── story/TTC-101-login-screen          trunk; story PR → epic, draft until the stack is merged
      └── subtask/TTC-102-login-model    layer 1; PR → story branch
           └── subtask/TTC-103-login-ui  layer 2; PR → layer 1
```

`gh stack submit` sets every base. Merging is the user's; when a layer merges, GitHub retargets
the PR above it.

## Prerequisites

- `gh stack --help` must work. If it doesn't, stop and ask the user to run
  `gh extension install github/gh-stack`. Don't install it yourself.
- The story branch is pushed to `origin` (see `branching.md`). The bottom PR targets it.
- If `submit` reports that stacked PRs aren't available for the repo, stop and tell the user.
  Don't fall back to plain PRs on your own.

## Naming

`subtask/<SUB-TASK KEY>-<kebab-case-summary>`, for example `subtask/TTC-102-login-model`. Layers
can't live under the story branch's name (`story/TTC-101-login-screen/...`): git can't hold a ref
and a folder with the same name.

## Adding a layer

First sub-task of the story:

```bash
git checkout story/TTC-101-login-screen
git pull
gh stack init -b story/TTC-101-login-screen subtask/TTC-102-login-model
```

Every later sub-task goes on top of the stack:

```bash
gh stack view --json            # find the top layer
gh stack checkout subtask/TTC-102-login-model
gh stack add subtask/TTC-103-login-ui
```

`init` and `add` create and check out the new branch. Commit the sub-task's work there, staging
named files only.

## Publishing the layer

```bash
gh stack submit --auto --open
```

`--auto` skips the interactive editor (it needs a terminal). `--open` makes new PRs ready for
review; without it they are drafts. Sub-task PRs open ready.

`--auto` generates the title and body. Replace both on the new PR:

- **Title:** `TTC-102 <Imperative summary>`, matching the commit convention.
- **Body:** Jira link to the sub-task, summary, test plan, screenshots for UI changes (both
  platforms if both changed). Note the story it belongs to.

Patch over REST, as in `pull-requests.md` ("Updating the PR body"), then confirm:

```bash
gh api --method PATCH repos/{owner}/{repo}/pulls/<n> -f title='TTC-102 Add the login model' -F body=@pr-body.md
gh pr view <n> --json number,title,baseRefName,headRefName,isDraft,url
```

Check `baseRefName` is the layer below (or the story branch for layer 1). Report the PR URL.

## A fix on a lower layer

Review fixes and rework go on the layer that owns the code. The layers above must then be rebased
onto it:

```bash
gh stack checkout subtask/TTC-102-login-model
# commit the fix
gh stack rebase --upstack
gh stack push
```

`gh stack push` uses `--force-with-lease` on the layer branches. That is the only force-push
allowed, and only through `gh stack`. If `rebase` stops on a conflict, resolve it and run
`gh stack rebase --continue`, or `gh stack rebase --abort` and ask the user.

If the owning layer is already merged, the fix goes on the story branch instead (see below).

## When the story branch moves

The story branch moves when the user merges a layer, when the epic is merged into it, or when a
fix lands on it directly. Plain-push anything you committed on it, then:

```bash
gh stack sync
```

`sync` fast-forwards the trunk, rebases the open layers onto it, pushes, and relinks the PRs. If it
reports the stack has diverged, stop and ask; don't force anything.

## After the user merges

Find which sub-task PRs have landed:

```bash
gh pr list --state merged --limit 50 --json number,title,headRefName,baseRefName,mergedAt \
  --jq '.[] | select(.headRefName | startswith("subtask/TTC-"))'
gh stack view --json
```

Then `gh stack sync --prune` to update the local stack and drop branches for merged layers.

## Never

- `gh stack merge`, or merging any layer. The user merges.
- `gh stack modify` (interactive and rewrites the stack) without asking.
- `git push --force` or `--force-with-lease` by hand, on any branch.
- Rebasing the story or epic branch.
