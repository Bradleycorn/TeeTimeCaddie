# Opening the PR

Read this when it's time to open a story (or hotfix) PR.

Sub-task PRs are different: they are stacked on the story branch and covered in `stacked-prs.md`.
A story worked in sub-tasks gets its story PR, ready for review, after the user merges the whole
approved stack into the story branch. Before that the story branch has no commits of its own, and
GitHub refuses a PR with no diff. Review status lives on the sub-task PRs until then.

A one-sub-task issue has no stack. Its story PR is its only PR: open it **ready, not as a draft**,
once the sub-task's work is committed on the story branch. Title it with the story key
(`TTC-101: Login screen`) and link both the story and the sub-task in the body.

## 1. Confirm the base branch

The PR targets **the branch the story was cut from** — the epic branch if there is one, otherwise
whatever base was agreed when the branch was created. `main` only if that was the agreed base.

If you no longer have it from `branching.md`, recover it rather than guessing:

```bash
git log --oneline --decorate -1 $(git merge-base HEAD origin/main)   # rough starting point
gh pr list --state all --limit 20 --json number,headRefName,baseRefName   # what past PRs targeted
```

Ask the user if it is still ambiguous. A story PR merged into the wrong base is painful to undo.

## 2. Push

```bash
git push -u origin story/TTC-101-login-screen
```

## 3. Create the PR

```bash
gh pr create --base epic/TTC-100-user-management --title "TTC-101: Login screen" --body "$(cat <<'BODY'
...
BODY
)"
```

The body covers:

- **Jira:** link to the issue
- **Summary:** what changed and why
- **Test plan:** what you ran, or what the reviewer should exercise
- **Breaking changes / migration notes**, when there are any
- **Screenshots** for any UI change — both platforms if both changed
- **Sub-task PRs**, for a stacked story: each one's link, already reviewed and merged

## 4. Stop

Report the PR URL and the base branch it targets. **Do not merge it and do not delete the
branch** — that is the user's call, and they will do it themselves after review.

Once they do merge, the story branch is gone; further work on that issue needs a fresh branch off
the updated base.

## Updating the PR body

Don't use `gh pr edit`. It can hit the same Projects (classic) deprecation error described below
and exit 0 without changing anything. Patch the PR over REST instead, then re-read it to confirm:

```bash
gh api --method PATCH repos/{owner}/{repo}/pulls/<n> -F body=@pr-body.md
gh pr view <n> --json body --jq .body
```

Write the body to a scratch file first; `-F body=@file` reads it verbatim, which avoids shell
quoting problems with long markdown.

## Follow-ups that are not merges

```bash
gh pr view <n> --json number,title,state,baseRefName,headRefName,reviewDecision,url
gh pr checks <n>
gh pr diff <n>
```

Without `--json`, some `gh` versions print only a `Projects (classic) is being deprecated` error
**and exit 0**, so the PR looks empty. Reissue with `--json`; if that fails too, use
`gh api repos/{owner}/{repo}/pulls/<n>`.

To add to an open PR, commit and push to the same branch; the PR updates itself.
