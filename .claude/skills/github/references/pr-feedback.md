# Addressing PR comments and reviews

Read this when asked to handle feedback on a PR.

## 1. Find every comment — there are three separate sources

Missing comments is the main failure mode here, because feedback lives in three places and no
single command shows all of them. `gh pr view` shows **none** of the inline review comments on any
version, and its default non-`--json` form can fail silently on some versions (see
`pull-requests.md`). The REST calls below behave the same across versions — use them.

```bash
# a) Review summaries — the APPROVED / CHANGES_REQUESTED / COMMENTED verdict and its body
gh api repos/{owner}/{repo}/pulls/<n>/reviews \
  --jq '.[] | "\(.id)\t\(.state)\t\(.user.login)\t\(.body)"'

# b) Inline comments on code — the bulk of real feedback. Note the review id to map each
#    comment back to the review whose state decides whether it is mandatory.
gh api repos/{owner}/{repo}/pulls/<n>/comments --paginate \
  --jq '.[] | "\(.id)\t\(.pull_request_review_id)\t\(.path):\(.line)\n\(.body)\n"'

# c) Top-level conversation comments, which are issue comments, not review comments
gh api repos/{owner}/{repo}/issues/<n>/comments \
  --jq '.[] | "\(.id)\t\(.user.login)\n\(.body)\n"'
```

**Resolved state is GraphQL-only** — the REST payloads above do not carry it, so this is the only
way to tell what is still outstanding:

```bash
gh api graphql -f owner=Bradleycorn -f repo=TeeTimeCaddie -F pr=<n> -f query='
query($owner:String!, $repo:String!, $pr:Int!) {
  repository(owner:$owner, name:$repo) {
    pullRequest(number:$pr) {
      reviewThreads(first:100) { nodes {
        isResolved isOutdated path
        comments(first:100) { nodes { databaseId author{login} body } }
      } }
    }
  }
}' --jq '.data.repository.pullRequest.reviewThreads.nodes[]
         | select(.isResolved == false)
         | "\(.path)  id=\(.comments.nodes[0].databaseId)\n\(.comments.nodes[0].body)\n"'
```

Work from the unresolved threads. Account for every one of them — fixed, replied to, or
deliberately skipped.

## 2. Decide what to act on

- **`@claude` mentions win.** If *any* comment on the PR mentions `@claude`, address only those.
- Otherwise, address **all** comments in **"Request Changes"** reviews.
- `@claude don't fix this` (or similar) skips a comment even inside a Request Changes review.
- **"Request Changes"** = must fix. **"Comment"** = discussion; use judgment.

For "Comment" reviews, read the intent:

| Definite change | Question / discussion | Suggestion |
|---|---|---|
| Bug reports | "Why…" | "Could this be simplified…" |
| Architecture violations | "Have you considered…" | "Might be clearer if…" |
| Missing error handling, null checks | "What happens if…" | "Nit: …" |

**When uncertain, ask.** Git makes rollback cheap; guessing wrong wastes a review cycle.

## 3. Discussion comments get a reply, not a commit

When a comment is marked "For discussion:", "Let's discuss:", "Question:", or is otherwise
exploratory — **do not write code**. Reply with your analysis and wait for the user.

Post it as a **threaded reply to that comment**, not a new top-level comment, so the discussion
stays readable and on record. Start the reply with `From Claude: `.

```bash
gh api --method POST repos/{owner}/{repo}/pulls/<n>/comments/<comment_id>/replies \
  -f body='From Claude: ...'
```

`<comment_id>` is the `id` from the inline-comments listing, or `databaseId` from the GraphQL
query. `gh pr comment` posts to the main conversation instead and breaks the thread — use it only
for genuinely PR-wide remarks.

## 4. Close the loop

Commit and push the fixes to the same branch, then reply on each comment you acted on saying what
changed. Leaving a thread silently fixed makes the reviewer re-read the whole diff.
