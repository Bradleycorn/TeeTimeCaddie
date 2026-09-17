# Opening the PR

Read this when the code is written and committed.

## 1. Confirm the base branch

The PR targets **the branch the story was cut from** — the epic branch if there is one, otherwise
whatever base was agreed when the branch was created. Never `main` for a story branch.

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

## 4. Stop

Report the PR URL and the base branch it targets. **Do not merge it and do not delete the
branch** — that is the user's call, and they will do it themselves after review.

Once they do merge, the story branch is gone; further work on that issue needs a fresh branch off
the updated base.

## Follow-ups that are not merges

```bash
gh pr view <n> --json number,title,state,baseRefName,headRefName,reviewDecision,url
gh pr checks <n>
gh pr diff <n>
```

**Always pass `--json` with explicit fields.** The default human-readable view asks GitHub for a
broad bundle of PR metadata, and on some `gh` versions part of that bundle is a GraphQL field
GitHub has since deprecated. The command then prints nothing but an error — e.g.
`Projects (classic) is being deprecated ... (repository.pullRequest.projectCards)` — **and still
exits 0**, so an empty result looks like an empty PR. `--json` requests exactly the fields you
name, so it is unaffected by whatever the default view happens to include. If you hit an error
like that, you are not missing data: reissue with `--json`. If `--json` itself fails, fall back to
`gh api repos/{owner}/{repo}/pulls/<n>`, which is plain REST.

To add to an open PR, commit and push to the same branch; the PR updates itself.
