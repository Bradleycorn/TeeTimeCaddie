# Writing the Acceptance Criteria and Development Notes fields

The field ids, the Jira routing, and the general rules for these custom fields (reads omit them,
writes take ADF only) are in CLAUDE.md. This file covers only what adding design criteria needs on
top of that.

Two reminders, because getting them wrong is silent:

- **Read with `fields: ["*all"]` or the ACs look empty.**
- **An edit replaces the whole field — losing an AC is the worst outcome.**

## Contents
- The ADF payload — which nodes work, and continuing the numbering
- Shape of the finished AC field
- Rebuilding the field — verbatim ACs, and the placeholder to delete
- Verifying the write — structural check script

## The ADF payload

Pass a full ADF document inline in `fields`, keyed by the Acceptance Criteria field id:

```json
{
  "<AC field id>": {
    "type": "doc",
    "version": 1,
    "content": [ ... ]
  }
}
```

### Nodes that work here

- `paragraph`, `orderedList` / `bulletList` / `listItem`, and `heading` with
  `attrs: {level: 3}` (or 4). Headings do render inside these fields.
- Marks: `strong`, `em`, `code`, and `link` with `attrs: {href}`.
- `orderedList` accepts `attrs: {order: N}` — **use this** to continue numbering across
  intervening sub-headings, so the criteria read 1 through 18 rather than restarting at each
  group.
- Nest a `bulletList` inside a `listItem`, after that item's `paragraph`, for sub-points.

## Shape of the finished AC field

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

## Rebuilding the field

Whatever you send becomes the entire field value, so the existing functional AC items must be
reproduced **verbatim** in the payload — read them first, copy the text exactly, and check them
after writing. Losing or paraphrasing an AC while adding design criteria is the worst available
outcome here.

While you're rebuilding the field, delete the placeholder paragraph that reads:

> *Functional ACs only. Per the project Definition of Done, every criterion must hold on both iOS
> and Android (behavioral parity). Design and design-conformance criteria are added before this
> story is pulled.*

Its whole purpose was to mark the work this skill does.

Write the two fields in **separate `Atlassian:editJiraIssue` calls** — the payloads are large, and a
failure then tells you which one to fix.

## Verifying the write

Read the fields back and check the *structure*, not just that prose appears. Headings that
silently became literal `###` text, or numbering that restarted, are easy to miss by eye.

Save the read-back response to a file, then pass the Acceptance Criteria field id as the argument
and the file on stdin:

```bash
python3 -c "
import json, sys
doc = json.load(sys.stdin)['fields'][sys.argv[1]]
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
" <AC field id> <saved-response.json
```

Confirm: the original functional items are intact and unaltered, new ones appended, placeholder
gone, `H3`/`H4` are real headings, `order` attributes continue the numbering, and nested bullet
lists survived.
