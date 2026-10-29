# BcsDeploy Deletion Plan

**Date:** 2026-10-02  
**Status:** Safe to delete — pending manual execution (Edit(legacy/**) denied in project settings)

## What it is

`legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsDeploy/` — 2 files, ~2,800 LOC.

`Deploy.java` is a standalone DEV→PROD deployment tool: reads `config.properties` at runtime for HANA DEV and PRO connection strings, user/password, SVN paths, web host credentials, Fiori host credentials. It synchronizes HANA stored procedures and master tables from development to production.

`Tools.java` — utility helpers used only by Deploy.java.

## Why it must be deleted

- The `config.properties` file it reads contains **production HANA credentials** (DatabasePRO, UserPRO, PassPRO) and web admin credentials. These represent the same credential family as RULE-076/077.
- The tool is a dead-end in a Spring Boot world: HANA schema migrations in Phase 1+ are handled by Liquibase/Flyway or direct DBA scripts, not a bespoke Java tool.
- The brief (§2 mapping table) explicitly marks it: **Delete — dead code with credentials (RULE-076)**.

## Safety verification

```bash
# No inbound references from any other module:
grep -r "BcsDeploy" legacy/otr-sva-optimus-corredores \
  --include="*.java" --include="*.xml" -l | grep -v "BcsDeploy/"
# → (no output) — confirmed no other module depends on it
```

## Deletion command (run manually as project owner)

```bash
rm -rf legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsDeploy/
```

If the legacy source is under version control, use:
```bash
git -C legacy/otr-sva-optimus-corredores rm -r BSCL/Plataforma/JAVA/BcsDeploy/
git -C legacy/otr-sva-optimus-corredores commit -m "Remove BcsDeploy — dead deployment tool with embedded credential paths (RULE-076)"
```

## Phase 1 entry criteria update

Once deleted, tick this box in `MODERNIZATION_BRIEF.md`:
```
- [x] BcsDeploy module confirmed safe to delete and removed from source
```
