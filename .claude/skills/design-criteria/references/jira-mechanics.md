# Reading and writing TTC issue fields

## Contents
- Routing — which Jira site and cloudId
- The two custom fields — ids, and why reads omit them
- Writing: ADF only — the markdown rejection, which nodes work, field shape
- An edit replaces the whole field — and the placeholder to delete
- Verifying the write — structural check script

## Routing

TTC lives on the **personal** Jira site — use the **`Atlassian`** MCP server, not the TwinSpires
Rovo one:

- site `bradleycorn.atlassian.net`
- `cloudId` `5f936620-68a5-4bc4-93c9-2554f20f6511`

This site rejects unbounded JQL, so always constrain a search by project or date:
`project = TTC AND parent = TTC-66 ORDER BY key ASC`.

## The two custom fields

| Field | Id | Type |
|---|---|---|
| Acceptance Criteria | `customfield_10130` | rich textarea (ADF) |
| Development Notes | `customfield_10131` | rich textarea (ADF) |

`Atlassian:getJiraIssue` without an explicit `fields` argument returns only the default set and **silently
omits both**, which makes a story look like it has no ACs at all. Always read with
`fields: ["*all"]` (add `expand: "names"` if you want the custom-field ids mapped to labels).

## Writing: ADF only

`Atlassian:editJiraIssue` with `contentFormat: "markdown"` **is rejected** for these fields:

```
Operation value must be an Atlassian Document (see the Atlassian Document Format)
```

That parameter only converts for `description` and comments, not custom fields. Pass a full ADF
document inline in `fields`:

```json
{
  "customfield_10130": {
    "type": "doc",
    "version": 1,
    "content": [ ... ]
  }
}
```

The failure is clean — nothing is written — so a markdown attempt costs only a round trip. But
there's no reason to make it.

### Nodes that work here

- `paragraph`, `orderedList` / `bulletList` / `listItem`, and `heading` with
  `attrs: {level: 3}` (or 4). Headings do render inside these fields.
- Marks: `strong`, `em`, `code`, and `link` with `attrs: {href}`.
- `orderedList` accepts `attrs: {order: N}` — **use this** to continue numbering across
  intervening sub-headings, so the criteria read 1 through 18 rather than restarting at each
  group.
- Nest a `bulletList` inside a `listItem`, after that item's `paragraph`, for sub-points.

### Shape of the finished AC field

```
orderedList                        ← the functional ACs, existing items carried over verbatim
heading level 3  "Design Criteria"
paragraph                          ← design project link
paragraph                          ← which sections to open, prototype fixtures
heading level 4  "<first screen>"
orderedList order=1
heading level 4  "<second screen>"
orderedList order=7
heading level 4  "In the design, but deliberately not in this story"
orderedList order=12
heading level 4  "Everywhere in the flow"
orderedList order=14
heading level 4  "Where the two platforms diverge on purpose"
orderedList order=17
heading level 4  "Copy that isn't readable off the frames"
orderedList order=18
```

### An edit replaces the whole field

There is no append. Whatever you send becomes the entire field value, so the existing functional
AC items must be reproduced **verbatim** in the payload — read them first, copy the text exactly,
and check them after writing. Losing or paraphrasing an AC while adding design criteria is the
worst available outcome here.

While you're rebuilding the field, delete the placeholder paragraph that reads:

> *Functional ACs only. Per the project Definition of Done, every criterion must hold on both iOS
> and Android (behavioral parity). Design and design-conformance criteria are added before this
> story is pulled.*

Its whole purpose was to mark the work this skill does.

Write the two fields in **separate `Atlassian:editJiraIssue` calls** — the payloads are large, and a
failure then tells you which one to fix.

## Verifying the write

Read the fields back and check the *structure*, not just that prose appears. Headings that
silently became literal `###` text, or numbering that restarted, are easy to miss by eye:

```bash
python3 -c "
import json, sys
doc = json.load(open(sys.argv[1]))['fields']['customfield_10130']
def flat(n):
    if n.get('type') == 'text': return n['text']
    return ''.join(flat(c) for c in n.get('content', []))
for node in doc['content']:
    t = node['type']
    if t == 'heading':
        print(f\"[H{node['attrs']['level']}] {flat(node)}\")
    elif t == 'orderedList':
        print(f\"[list order={node.get('attrs', {}).get('order', '-')}] {len(node['content'])} items\")
        for li in node['content']:
            nested = sum(1 for c in li.get('content', []) if c['type'].endswith('List'))
            print(f\"   - {flat(li)[:70]}{' (+nested)' if nested else ''}\")
    else:
        print(f'[{t}] {flat(node)[:80]}')
print('placeholder gone:', 'Functional ACs only' not in json.dumps(doc))
" <saved-response.json
```

Confirm: the original functional items are intact and unaltered, new ones appended, placeholder
gone, `H3`/`H4` are real headings, `order` attributes continue the numbering, and nested bullet
lists survived.
