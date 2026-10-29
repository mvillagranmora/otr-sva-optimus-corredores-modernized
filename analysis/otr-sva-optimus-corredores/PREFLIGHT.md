# Preflight — otr-sva-optimus-corredores

**Source location:** `legacy/otr-sva-optimus-corredores` is a real directory in this workspace, not a symlink.
**Re-run:** 2026-10-23, superseding the 2026-10-15 preflight. It was run because on 2026-10-22 (11:12–11:17) a
`git stash` and checkouts (`master` → `feat/modernizacion-acn` → `feat/acn-update-cicd`) in the legacy repository
removed the applied security patch from the working tree and rewrote the modification time of 12,499 files, so the
verify "Source untouched" check failed. On 2026-10-23 the patch was restored at the user's request with
`git -c core.fileMode=false stash apply 1c574a43` (stash kept, not popped). The working tree now matches that stash
again, except the `gradlew` execute bit that NTFS does not store. Later the same day, at the user's decision, the
user reverted **only SEC-012 (BCrypt)** with `git restore --source=HEAD` on `CrearSesion.java` and
`ActualizarUsuario.java` (the only two files that used BCrypt); the rest of the patch stays applied, and the stash is
kept if BCrypt is wanted later. This preflight was then run again (second run of 2026-10-23). It wrote nothing under
`legacy/`: every smoke build ran on `git archive HEAD` copies in scratch.

**Third run (2026-10-23, 18:5x).** The second baseline broke within minutes: the VS Code Java extension (Eclipse
JDT) had `legacy/` imported as a Gradle project and recompiled it into `legacy/.../bin/main` (19,677 `.class` files,
last write 18:34:53) after the two files were reverted; the same extension ran the Gradle 6.3 import at 10:28.
Fix applied: `.vscode/settings.json` at the workspace root now has `"java.import.exclusions": [..., "**/legacy/**"]`,
and the user ran **Java: Clean Java Language Server Workspace**. Checked before writing this file: no `java`
process running, no file under `legacy/` written after 18:34:53, `git status` unchanged (20 tracked changes). The
`bin/` and `build/` folders are untracked IDE/Gradle output; they are part of the baseline as they stand.

> **New baseline.** This file's modification time is what `/code-modernization:modernize-verify` uses for its "Source
> untouched" check. Everything under `legacy/` as it stands today is the reference from now on: branch
> `feat/acn-update-cicd`, commit `944f9b9d5`, plus the uncommitted security patch **without SEC-012** (15 files:
> 13 sources, `build.gradle`, `JOBMAN.gradle`, `Jobmanager.xml`, the deletion of `.claude/settings.json` and Gradle
> cache files). The tree now builds (Check 3b). Any `git checkout`, `stash`, `reset` or Gradle run inside `legacy/` from
> now on breaks the baseline again.

---

## Answers

> Collected via AskUserQuestion pop-ups on 2026-10-23 (asked three times that day, once per preflight run; the same
> answers every time). The answers appear verbatim (the option the person chose).

### 1. Scope
Is this code the complete system, or one slice of a larger codebase? If a slice: what outside it depends on code inside it, and is breaking those consumers acceptable?

**Answer:** "Una parte"

> Open item (the human must fill in): the answer does not say *what* outside depends on this code, nor whether
> breaking it is acceptable. The 2026-10-15 answer said: runtime consumers of the REST/SOAP surface that must not
> break. Known from the project notes: `optimus-servicios` (`modernized/otr-sva-optimus-servicios`, a LoopBack API)
> calls `/BSCL/Plataforma/GWJAVA/PLA/CrearSesion`, `/call?ch=WS&t={servicio}`, `ProcesarIdTransaccion` and
> `AuditoriaWS`; Tesorería's SOAP services `SvcRecaudaInqV1`/`SvcRecaudaTrnV1` have external consumers. Confirm the
> full list and whether breaking any of them is acceptable.

### 2. Build & test locally
Can this environment restore, build, and run the tests? Roughly how long does the full CI pipeline take?

**Answer:** "Sí, compila aquí"

> Open item (the human must fill in): the duration of the full CI pipeline was not given. Check 3b confirms the
> answer: clean `HEAD` and the current working tree both build here.

### 3. Bespoke build infrastructure
Is there organization-specific build or dependency machinery (an internal package feed, a code generator, a wrapper around the standard build tool) a newcomer would not guess? Where is it documented?

**Answer:** "No"

> Note: CI goes through `@Library('jenkins-shared-library') gradlePipeline(...)` (`Jenkinsfile:3,15`), which lives
> outside the tree. Dependencies are Maven Central plus 26 JARs committed in `libs-private/`.

### 4. Prior attempts
Has anyone tried to modernize this before? What went wrong?

**Answer:** "No"

### 5. Off limits
Is anything in this code not allowed to change in this pass (a component another team owns, a frozen branch, generated code)?

**Answer:** "Nada"

---

## Check 6 — Scope boundary (lead finding)

`legacy/otr-sva-optimus-corredores` **is a standalone git repository** (`.git/` at its root, 13,897 commits, last
2026-09-16; the workspace root is not a git repository). Every `srcDir` in `build.gradle` resolves inside the tree.

- **Outbound:** none. No source or build reference reaches outside `legacy/otr-sva-optimus-corredores`.
- **Inbound:** no code-level consumer (no sibling manifests reference it). Answer 1 says it is a slice: the consumers
  are at runtime, through the REST/SOAP/JavaCall surface (see the open item). Each migrated endpoint needs an explicit
  transition decision in the brief (§7b): keep the contract for old and new consumers, a compatibility shim in the API
  Gateway, or a scheduled break.

**Verdict:** no code-level blast radius; the runtime API consumers need the transition plan the brief still lists as
open.

---

## Check 1 — Stack

| Dimension | Finding |
|---|---|
| Tracked files | 20,599 (`git ls-files`) |
| Java | 3,622 `.java` (about 1.02 M lines; 765,652 in the 42 in-scope modules, `LOC_LEGACY.md`) |
| SQL / HANA | 12,333 tracked `.sql`, 156 `.calculationview`, `BSCL.hdbschema` |
| Reports | 160 tracked Crystal Reports `.rpt` (binary), 131 JasperReports `.jrxml` |
| Front end | 1,753 `.js`, 1,574 `.html`, 55 `.css` |
| Build | Gradle 6.3 wrapper (`gradle/wrapper/gradle-wrapper.properties`), 5 `.gradle` files |
| Runtime | Apache TomEE 8.0.16 (Java EE 8), Java 8 bytecode built with JDK 11 (`Jenkinsfile:12`) |
| Deployment | `Dockerfile` + Jenkins → AWS ECR/EKS; artifacts `PLUGIN_OPTIMUS.jar` and `PLA.war` |
| Frameworks | Apache CXF 3.2.0 (SOAP), Spring 4.3.10, sql2o, DBCP2, JasperReports 6.3.0, Crystal Reports (proprietary) |

---

## Check 2 — Analysis tooling

| Tool | Status | Version | Without it |
|---|---|---|---|
| `scc` | ❌ missing | — | LOC and complexity fall back to `find` + `wc`; the COCOMO index is coarser |
| `cloc` | ❌ missing | — | same fallback |
| Python 3 | ✅ `python` and `py -3` (the `python3` on PATH is the Microsoft Store stub and does not run) | 3.14.4 | — |
| `lizard` | ✅ installed since 2026-10-15 | 1.24.2 | — |

Install (Windows): `winget install --id AlDanial.Cloc` (or scc from `github.com/boyter/scc/releases`). Use
`PYTHONIOENCODING=utf-8 python` wherever a command says `python3`.

---

## Check 3 — Build toolchain

### 3a — CI / build definition
- **CI:** `Jenkinsfile` → `@Library('jenkins-shared-library') _` + `gradlePipeline(...)`; requires JDK 11 and the
  Jenkins "Gradle" tool (`Jenkinsfile:12`). Tasks: `:pluginJar :plaWar :collectRuntimeLibs`.
- **Pinned toolchain:** Gradle 6.3 (wrapper), JDK 11, `sourceCompatibility/targetCompatibility = 1.8`.
- **Dependency sources:** Maven Central plus 26 committed JARs in `libs-private/` (Crystal Reports, SAP HANA JDBC
  `ngdbc-2.8.12`, internal `storedprocedure-2.0-SNAPSHOT`, `kaiser-odata`); no private feed.
- **Bespoke:** the Jenkins shared library is outside the repo, so the full pipeline cannot be reproduced from the tree.

### 3b — Smoke test (legacy), JDK 11.0.32, Gradle 6.3 wrapper, `--offline`, on scratch copies

| Level | Tree | Result |
|---|---|---|
| 2 — `:pluginJar` (whole project) | **Clean `HEAD`** (`git archive HEAD`) | ✅ builds offline; `PLUGIN_OPTIMUS.jar` 21 MB. (The 2026-10-15 offline failure on `commons-fileupload:1.3.3` is gone: it is now in the Gradle cache.) |
| 2 — `:pluginJar :plaWar` | **Current working tree** (`HEAD` + `git diff HEAD`: the patch without SEC-012, 15 files) | ✅ builds offline: `PLUGIN_OPTIMUS.jar` 21 MB and `PLA.war` 6.5 MB |
| 1 — single classes | Current tree, module legacy classes | ✅ on 2026-10-23 the verify oracles compiled the unmodified legacy classes of Contabilidad, Tesorería, APV, Cuenta Inversión and Plataforma (`ConsultarEstadoModulo`) from `legacy/` into scratch and ran them |

**History of the former ❌ (2026-10-15 and the first run of 2026-10-23):** `security_remediation.patch` was applied
without its SEC-012 prerequisite (`security_remediation.patch:8-10`: `jbcrypt` dependency and imports), so
`CrearSesion.java:280` and `ActualizarUsuario.java:382` failed with `cannot find symbol BCrypt`. **Resolved** by
reverting SEC-012 only (those two files back to `HEAD`: SHA-256 password hashing as in production). SEC-012 stays an
open security finding; the stash `1c574a43` still holds it. The remaining patch is still uncommitted: the owner
should commit it on a branch of the legacy repository (that changes `legacy/` mtimes and requires another preflight).

### 3c — Target stack
Proven today on this code: JDK 25.0.4 (Temurin) + Maven 3.10.0 (`mvn -o`). The 2026-10-23 verify ran 10 modules
`clean test` from clean (6,627 tests, 0 failures). The default `JAVA_HOME` is JDK 11: export the JDK 25 path for
`modernized/`.

---

## Check 4 — Source completeness

| Category | Status | Detail |
|---|---|---|
| Java sources and build dirs | ✅ | All `srcDirs` exist; `HEAD` and the current tree compile (3b) |
| Private JARs | ✅ | 26 present in `libs-private/` |
| Web and scheduler descriptors | ✅ | `ANT/config/Plataforma.xml` (becomes `web.xml`), `Jobmanager.xml`, `BcsJobManagerWeb.xml`, `BcsPortalConfirmacionWeb.xml` |
| Data definitions | ⚠️ | 174 SQL scripts with `CREATE TABLE`, `BSCL.hdbschema`, 156 calculation views. Completeness against the live HANA schema is not verified |
| Binary-only artifacts | ⚠️ | 160 tracked `.rpt` Crystal Reports and the proprietary JARs have no source |
| Infra manifests | ⚠️ | No Kubernetes or Helm files in the tree (they live with the Jenkins shared library / infra repo) |

---

## Check 5 — Optional context

| Item | Status | Detail |
|---|---|---|
| Production telemetry | ⚠️ | No APM MCP server connected (Datadog is used at runtime only) |
| Version control | ✅ | Real history: 13,897 commits, last 2026-09-16; branch `feat/acn-update-cicd` |

---

## Check 7 — Source protection

✅ **`.claude/settings.json` (project) denies `Edit(/legacy/**)`.** `.claude/settings.local.json` and the user
`~/.claude/settings.json` have no `Edit` deny rule. `legacy/otr-sva-optimus-corredores` is not a symlink, so this rule
is enough. Managed (organization) settings are not read here.

A permission rule covers Claude's file tools and the shell commands it recognizes (`sed`, `tee`, `> file`), not a
script that opens files itself, nor a `git` or Gradle command run inside `legacy/`, nor an IDE (the 2026-10-22
stash/checkout, and the VS Code Java extension's Gradle import at 10:28 and rebuild into `bin/main` on 2026-10-23,
all wrote there). The workspace `.vscode/settings.json` now excludes `**/legacy/**` from the Java import; keep it. The hard guarantee is the operating system: a read-only
mount, or a sandbox that blocks shell writes.

---

## Summary

| # | Check | Status | Finding | Fix |
|---|---|---|---|---|
| 0 | Human answers | ✅ | All 5 answered (2026-10-23): a slice; builds here; no bespoke infra; no prior attempt; nothing off limits | Open: which consumers and whether breaking them is acceptable; CI duration |
| 6 | Scope boundary | ✅ | Standalone repo; no code-level inbound or outbound references; runtime API consumers | Transition plan for the runtime API (brief §7b) |
| 1 | Stack | ✅ | Java 8/JDK 11, Gradle 6.3, TomEE 8, CXF, Spring 4.3, HANA, Crystal and Jasper | — |
| 2 | Analysis tooling | ⚠️ | Python 3.14 and `lizard` 1.24.2 work; `scc`/`cloc` missing | `winget install --id AlDanial.Cloc` |
| 3 | Build toolchain | ✅ | Clean `HEAD` and the current tree (patch without SEC-012) build offline (JDK 11, Gradle 6.3). Target stack (JDK 25 + Maven) proven | Commit the remaining patch in the legacy repo when convenient (then preflight again) |
| 4 | Source completeness | ⚠️ | 174 DDL scripts; Crystal binaries; infra outside the repo | Validate DDL against HANA |
| 5 | Optional context | ⚠️ | No APM; git history present | — |
| 7 | Source protection | ✅ | `Edit(/legacy/**)` in the project settings; VS Code Java import excludes `**/legacy/**` | Do not run git, Gradle or an IDE build inside `legacy/` |

## Command readiness

| Command | Verdict | Gap |
|---|---|---|
| `assess`, `map`, `extract-rules` | Ready-with-gaps | No `scc`/`cloc` (coarser LOC); discovery artifacts already exist |
| `brief` | Ready | API consumer list and acceptability still open |
| `transform`, `reimagine` | Ready-with-gaps | Target stack ✅; legacy builds. No local TomEE: equivalence is oracle-based (module legacy-oracles on H2) |
| `harden` | Ready-with-gaps | No SAST tooling beyond the scripts; SEC-012 (BCrypt) reverted, so it is open again; the rest of the patch is uncommitted |
| `verify` | Ready | "Source untouched" dates from this file; keep `legacy/` unchanged from here on |
| `uplift` | N/A | No version-bump target |

## Most important fix

Nothing is red. The most important rule from here: **do not change anything under `legacy/`** (no git checkout,
stash, reset, restore or Gradle run inside it). When the owner commits the remaining security patch, run this
preflight once more.

## Next step

```
/code-modernization:modernize-assess otr-sva-optimus-corredores
```

The discovery artifacts already exist. For the current work, the useful step is to re-run only the proof pack
(`python ".../proof_pack.py" otr-sva-optimus-corredores`): the 2026-10-23 evidence is current and the baseline is now this file.
