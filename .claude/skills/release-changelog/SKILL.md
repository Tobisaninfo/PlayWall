---
name: release-changelog
description: Add a new release version to CHANGELOG_de.md and CHANGELOG.md using the tickets from YouTrack (project PW). Use when the user asks to extend/update the changelog for a version (e.g. "Changelog für 8.4.0").
argument-hint: <version, e.g. 8.4.0>
---

# Release changelog

Adds a new version section to `CHANGELOG_de.md` (German, written first) and `CHANGELOG.md` (English).
The version comes from the argument; the release date is **today's date** (`YYYY-MM-DD`) unless the user says otherwise.

## 1. Collect tickets (YouTrack MCP, project `PW`)

The YouTrack MCP tools are deferred: load them first with
`ToolSearch("select:mcp__youtrack__search_issues,mcp__youtrack__get_issue")`.

1. Search: `project: PW Lösungsversion: <version>` with
   `customFieldsToReturn: ["Ticket Type", "Stage", "Lösungsversion"]` (limit 20, page with `offset` if `hasNextPage`).
   If the version does not exist as a `Lösungsversion` value, stop and tell the user.
2. For every ticket with `Ticket Type: Epic`, run `subtask of: <EPIC-ID>`. **Use only the Epic; omit all its subtasks**
   (even if they are not listed in the first search).
3. Ignore tickets with Stage `Verworfen`.
4. Tickets that are not finished (Stage `Backlog`, `In Progress`, `Review`) are still included, but
   mention them to the user in the final reply so they can check.

## 2. Classify

- `Ticket Type: Bug` → `### Bugfixes`
- Everything else (Story, Aufgabe, Epic) → `### Features`
- Sort each list by ticket number ascending. Omit an empty section.

## 3. Write the entries

Format (one line per ticket, no trailing period):

```markdown
## <version> - <YYYY-MM-DD>

### Features

- PW-123 - Title

### Bugfixes

- PW-124 - Title
```

- Insert the section directly below `# Changelog`, above the previous version, followed by a blank line.
- **German file first** (`CHANGELOG_de.md`): use the YouTrack summary, fixing obvious typos (e.g. "Betriebsystem" →
  "Betriebssystem") and using the term "Kachel" for pads.
- **English file** (`CHANGELOG.md`): translate the same entries; "Kachel" → "pad". Same tickets, same order.
- Look at the previous section in each file and match its style.

## 4. Finish

- Do not commit unless asked.
- Reply with the list of included tickets, which Epics replaced subtasks (and which subtasks were left out),
  any tickets that are not yet done, and any titles you changed.
