#!/usr/bin/env python3
"""
Topology extractor for otr-sva-optimus-corredores.
Inputs:
  - jdeps DOT output (package-level dependency graph)
  - Java source tree under BSCL/
  - web.xml (Plataforma.xml), @WebServlet annotations, build.gradle
Outputs:
  - analysis/otr-sva-optimus-corredores/map/<domain>.json  (per-domain chunk)
  - analysis/otr-sva-optimus-corredores/topology.json      (merged)
Re-run: skips chunks whose file already exists.
"""
import json, os, re, sys
from pathlib import Path
from collections import defaultdict

REPO = Path("/home/mvillagran/develop/nuam/claude-modernizer/legacy/otr-sva-optimus-corredores")
ANALYSIS = Path("/home/mvillagran/develop/nuam/claude-modernizer/analysis/otr-sva-optimus-corredores")
MAP_DIR = ANALYSIS / "map"
DOT_FILE = Path("/tmp/claude-1000/-home-mvillagran-develop-nuam-claude-modernizer/7cd6149a-f6ab-4cf4-9723-11ae77bfc61d/scratchpad/jdeps_dot/PLUGIN_OPTIMUS.jar.dot")
MAP_DIR.mkdir(exist_ok=True)

# ─── Domain → module directories ─────────────────────────────────────────────
DOMAINS = {
    "Plataforma": [
        "Plataforma/JAVA/BcsPlataformaWeb",
        "Plataforma/JAVA/BcsPlataformaServicios",
        "Plataforma/JAVA/BcsJobManager",
        "Plataforma/JAVA/BcsPlataformaJobs",
        "Plataforma/JAVA/BcsUtils",
        "Plataforma/JAVA/BcsFilters",
        "Plataforma/JAVA/BcsPlataformaReporteApi",
        "Plataforma/JAVA/BcsValo2",
        "Plataforma/JAVA/BcsDeploy",
        "Plataforma/JAVA/BcsCargasPeru",
        "Plataforma/JAVA/BcsPlataformaServlets",
        "Plataforma/JAVA/BcsPortalConfirmacionWeb",
    ],
    "SocioNegocio": [
        "SocioNegocio/JAVA/BcsSocioNegocioServicios",
        "SocioNegocio/JAVA/BcsSocioNegocioJobs",
    ],
    "Custodia": [
        "Custodia/JAVA/BcsCustodiaServicios",
        "Custodia/JAVA/BcsCustodiaJobs",
    ],
    "Orden": [
        "Orden/JAVA/BcsOrdenServicios",
        "Orden/JAVA/BcsOrdenJobs",
        "Orden/JAVA/BcsJobOrders",
    ],
    "Mesa": [
        "Mesa/JAVA/BcsMesaServicios",
        "Mesa/JAVA/BcsMesaJobs",
    ],
    "FondoMutuo": [
        "FondoMutuo/JAVA/BcsFondoMutuoServicios",
        "FondoMutuo/JAVA/BcsJobsFondoMutuo",
    ],
    "Tesoreria": [
        "Tesoreria/JAVA/BcsTesoreriaServicios",
        "Tesoreria/JAVA/BcsTesoreriaJobs",
    ],
    "Facturacion": [
        "Facturacion/JAVA/BcsFacturacionServicios",
        "Facturacion/JAVA/BcsFacturacionJobs",
    ],
    "Contabilidad": [
        "Contabilidad/JAVA/BcsContabilidadServicios",
        "Contabilidad/JAVA/BcsContabilidadJobs",
    ],
    "Normativa": [
        "Normativa/Java/BcsNormativaServicios",
        "Normativa/Java/BcsNormativaJobs",
        "Normativa/Java/BcsNormativaCore",
        "Normativa/Java/BcsCartolas",
        "Normativa/Java/BcsCartolasJobs",
        "Normativa/Java/BcsCartolasMaster",
        "Normativa/Java/BcsTributarioJobs",
    ],
    "APV": [
        "APV/JAVA/BcsAPVServicios",
        "APV/JAVA/BcsAPVJobs",
    ],
    "CuentaInversion": [
        "CuentaInversion/JAVA/BcsCuentaInversionServicios",
        "CuentaInversion/JAVA/BcsCuentaInversionJobs",
    ],
    "Portafolio": [
        "Portafolio/JAVA/BcsPortafolioServicios",
    ],
    "CanalAgente": [
        "CanalAgente/JAVA/BcsCanalAgenteServicios",
    ],
    "Corredor": [
        "Corredor/BECB",
        "Corredor/SURA",
    ],
    "Replica": [
        "Replica/JAVA/BcsReplica",
    ],
}

# ─── Build module info from source tree ───────────────────────────────────────
def get_module_name(rel_dir):
    return rel_dir.split("/")[-1]

def count_loc_and_packages(rel_dir):
    src_dir = REPO / "BSCL" / rel_dir / "src"
    if not src_dir.exists():
        # Try without /src
        alt = REPO / "BSCL" / rel_dir
        if alt.exists():
            src_dir = alt
        else:
            return 0, set()
    loc = 0
    packages = set()
    for f in src_dir.rglob("*.java"):
        try:
            content = f.read_text(errors="ignore")
            loc += len(content.splitlines())
            m = re.search(r"^\s*package\s+([\w.]+)\s*;", content, re.MULTILINE)
            if m:
                packages.add(m.group(1))
        except Exception:
            pass
    return loc, packages

print("Building module list...")
modules = {}  # id -> {name, domain, rel_dir, loc, packages, file}
pkg_to_mod = {}  # package prefix -> module id

for domain, dirs in DOMAINS.items():
    for rel_dir in dirs:
        mod_name = get_module_name(rel_dir)
        mod_id = f"mod:{mod_name}"
        loc, pkgs = count_loc_and_packages(rel_dir)
        modules[mod_id] = {
            "id": mod_id,
            "name": mod_name,
            "kind": "module",
            "domain": domain,
            "rel_dir": rel_dir,
            "loc": loc,
            "file": f"BSCL/{rel_dir}",
            "packages": list(pkgs),
        }
        for pkg in pkgs:
            # Longest match wins — store all, resolve by longest prefix when querying
            pkg_to_mod[pkg] = mod_id
        print(f"  {mod_id}: {loc} LOC, {len(pkgs)} packages")

def resolve_pkg(pkg):
    """Find the module id for a package by longest-prefix match."""
    pkg = pkg.strip().strip('"')
    # Remove jdeps annotation like "(PLUGIN_OPTIMUS.jar)"
    pkg = re.sub(r"\s*\([^)]+\)", "", pkg).strip()
    if pkg in pkg_to_mod:
        return pkg_to_mod[pkg]
    # Try progressively shorter prefixes
    parts = pkg.split(".")
    for i in range(len(parts)-1, 0, -1):
        prefix = ".".join(parts[:i])
        if prefix in pkg_to_mod:
            return pkg_to_mod[prefix]
    return None

# ─── Parse jdeps DOT ──────────────────────────────────────────────────────────
print("\nParsing jdeps DOT output...")
raw_edges = []
if DOT_FILE.exists():
    dot_content = DOT_FILE.read_text()
    for m in re.finditer(r'"([^"]+)"\s*->\s*"([^"]+)"', dot_content):
        src_pkg, tgt_pkg = m.group(1).strip(), m.group(2).strip()
        # Skip external dependencies (not found in PLUGIN_OPTIMUS.jar)
        if "(not found)" in tgt_pkg or "(java." in tgt_pkg or "(jdk." in tgt_pkg:
            continue
        src_mod = resolve_pkg(src_pkg)
        tgt_mod = resolve_pkg(tgt_pkg)
        if src_mod and tgt_mod and src_mod != tgt_mod:
            raw_edges.append((src_mod, tgt_mod))

# Deduplicate edges
edge_set = set(raw_edges)
edges = [{"source": s, "target": t, "kind": "call"} for s, t in sorted(edge_set)]
print(f"  {len(edge_set)} unique module-level call edges from jdeps")

# ─── Entry points: web.xml + @WebServlet ──────────────────────────────────────
print("\nExtracting entry points...")
entry_points = set()
web_xml = REPO / "BSCL/Plataforma/JAVA/ANT/config/Plataforma.xml"
plataforma_web_id = "mod:BcsPlataformaWeb"

if web_xml.exists():
    content = web_xml.read_text(errors="ignore")
    servlets = re.findall(r"<servlet-class>([^<]+)</servlet-class>", content)
    for sc in servlets:
        pkg = ".".join(sc.strip().split(".")[:-1])
        mod = resolve_pkg(sc.strip()) or resolve_pkg(pkg)
        if mod:
            entry_points.add(mod)

# @WebServlet annotations
for mod_id, info in modules.items():
    src_dir = REPO / "BSCL" / info["rel_dir"] / "src"
    if not src_dir.exists():
        continue
    for f in src_dir.rglob("*.java"):
        try:
            content = f.read_text(errors="ignore")
            if "@WebServlet" in content:
                entry_points.add(mod_id)
                break
        except Exception:
            pass

# Job classes (extend BcsJob or BcsTareaServicio)
for mod_id, info in modules.items():
    if "Job" in info["name"] or "Jobs" in info["name"]:
        entry_points.add(mod_id)

# SOAP endpoints (@WebService)
for mod_id, info in modules.items():
    src_dir = REPO / "BSCL" / info["rel_dir"] / "src"
    if not src_dir.exists():
        continue
    for f in src_dir.rglob("*.java"):
        try:
            content = f.read_text(errors="ignore")
            if "@WebService" in content:
                entry_points.add(mod_id)
                break
        except Exception:
            pass

print(f"  {len(entry_points)} entry point modules")

# ─── Data stores ──────────────────────────────────────────────────────────────
print("\nExtracting data store references...")

# Known stores from connections.properties and architecture
datastores = {
    "ds:HANA_BSCL": {"id": "ds:HANA_BSCL", "name": "SAP HANA (BSCL schema)", "kind": "datastore",
                     "description": "Primary SAP HANA database. Contains 156 calculation views, the PLA.Servicio service registry (1,817+ JavaCall class names driving runtime reflection dispatch), and all business domain tables across Plataforma, Orden, Custodia, Normativa, and related schemas."},
    "ds:HANA_PLA_Servicio": {"id": "ds:HANA_PLA_Servicio", "name": "PLA.Servicio registry (HANA)", "kind": "datastore",
                              "description": "SAP HANA table holding 1,817+ service definitions: class name, type (J/P/Q/X/O), and route. CallServlet reads this table on every request to resolve the target JavaCall class via Class.forName(). The entire service dispatch plane depends on the integrity of this registry."},
    "ds:SQL_Server": {"id": "ds:SQL_Server", "name": "SQL Server (jtds/mssql)", "kind": "datastore",
                      "description": "Secondary relational database accessed via jtds 1.3.1 and mssql-jdbc 7.2.1. Used by regulatory reporting (Normativa) and historical data modules."},
    "ds:PostgreSQL": {"id": "ds:PostgreSQL", "name": "PostgreSQL 42.7.7", "kind": "datastore",
                      "description": "Tertiary relational database. PostgreSQL 42.7.7 driver present in build. Used for specific domain data distinct from HANA."},
    "ds:SFTP": {"id": "ds:SFTP", "name": "SFTP (file transfer)", "kind": "datastore",
                "description": "External file exchange via JSch SFTP. ExtractorArchivosJob transfers regulatory files to/from settlement counterparties."},
    "ds:DTE_SII": {"id": "ds:DTE_SII", "name": "DTE/SII (e-invoicing API)", "kind": "datastore",
                   "description": "Chile SII electronic invoice API. Facturacion sends electronic tax documents and receives acknowledgements via REST calls to the DTE provider."},
    "ds:DCV_SOAP": {"id": "ds:DCV_SOAP", "name": "DCV Settlement (SOAP)", "kind": "datastore",
                    "description": "DCV (Depósito Central de Valores) securities settlement system, accessed via generated SOAP stubs in Custodia. Handles custody transfers, capital events, and settlement instructions."},
    "ds:BECB_SOAP": {"id": "ds:BECB_SOAP", "name": "BECB Broker (SOAP)", "kind": "datastore",
                     "description": "BECB electronic brokerage channel. Orden sends trade confirmations and order reports via AdditionalTradeInfo SOAP calls."},
    "ds:SMTP": {"id": "ds:SMTP", "name": "SMTP (email)", "kind": "datastore",
                "description": "Outbound email relay. MailChecker and notification services use javax.mail to send alerts and regulatory notifications."},
    "ds:JasperReports": {"id": "ds:JasperReports", "name": "JasperReports (131 .jrxml)", "kind": "datastore",
                          "description": "JasperReports 6.3.0 template engine. 131 .jrxml templates drive regulatory cartolas, tax certificates, and portfolio statements. BcsCartolas and BcsNormativaJobs are the primary consumers."},
    "ds:CrystalReports": {"id": "ds:CrystalReports", "name": "Crystal Reports (182 .rpt)", "kind": "datastore",
                           "description": "SAP Crystal Reports runtime (proprietary binary). 182 .rpt template files drive broker confirmation reports and institutional statements. Binary-only; no source representation."},
}

# Scan for SQL table patterns to build read/write edges
print("  Scanning for SQL table/schema references...")
ds_edges = []

HANA_MODS = set()
SQL_SERVER_MODS = set()
POSTGRE_MODS = set()
JASPER_MODS = set()
CRYSTAL_MODS = set()
SFTP_MODS = set()
DTE_MODS = set()
DCV_MODS = set()
BECB_MODS = set()

for mod_id, info in modules.items():
    src_dir = REPO / "BSCL" / info["rel_dir"] / "src"
    if not src_dir.exists():
        continue
    has_hana = has_sql = has_pg = has_jasper = has_crystal = has_sftp = has_dte = has_dcv = has_becb = False
    for f in src_dir.rglob("*.java"):
        try:
            content = f.read_text(errors="ignore")
            if "jdbc:sap://" in content or "ngdbc" in content or "sap.db.jdbc" in content or "BSCL.Plataforma" in content or "sql2o" in content.lower() or "DataSource" in content:
                has_hana = True
            if "jtds" in content or "sqlserver" in content.lower() or "mssql" in content.lower():
                has_sql = True
            if "postgresql" in content.lower() or "jdbc:postgresql" in content:
                has_pg = True
            if "jasper" in content.lower() or "JasperReport" in content or ".jrxml" in content:
                has_jasper = True
            if "crystal" in content.lower() or "ReportClientDocument" in content or ".rpt" in content:
                has_crystal = True
            if "sftp" in content.lower() or "jsch" in content.lower() or "SftpClient" in content:
                has_sftp = True
            if "dte" in content.lower() and ("authenticate" in content or "DTE" in content):
                has_dte = True
            if "dcv" in content.lower() or "DCV" in content or "Custodia" in content and "SOAP" in content:
                has_dcv = True
            if "becb" in content.lower() or "AdditionalTrade" in content or "BECB" in content:
                has_becb = True
        except Exception:
            pass
    if has_hana:
        HANA_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:HANA_BSCL", "kind": "write"})
    if has_sql:
        SQL_SERVER_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:SQL_Server", "kind": "read"})
    if has_pg:
        POSTGRE_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:PostgreSQL", "kind": "read"})
    if has_jasper:
        JASPER_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:JasperReports", "kind": "read"})
    if has_crystal:
        CRYSTAL_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:CrystalReports", "kind": "read"})
    if has_sftp:
        SFTP_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:SFTP", "kind": "write"})
    if has_dte:
        DTE_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:DTE_SII", "kind": "write"})
    if has_dcv and info["domain"] == "Custodia":
        DCV_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:DCV_SOAP", "kind": "write"})
    if has_becb and info["domain"] in ("Orden", "Corredor"):
        BECB_MODS.add(mod_id)
        ds_edges.append({"source": mod_id, "target": "ds:BECB_SOAP", "kind": "write"})

# Service registry dispatch edge — all JavaCall modules are "dispatched to" from BcsPlataformaWeb
dispatch_edges = []
javacall_mods = set()
for mod_id, info in modules.items():
    if info["domain"] in ("SocioNegocio", "Custodia", "Orden", "Mesa", "FondoMutuo",
                           "Tesoreria", "Facturacion", "Contabilidad", "Normativa",
                           "APV", "CuentaInversion", "Portafolio", "CanalAgente", "Corredor"):
        dispatch_edges.append({"source": "mod:BcsPlataformaWeb", "target": mod_id, "kind": "dispatch"})
        javacall_mods.add(mod_id)

# Service registry read
dispatch_edges.append({"source": "mod:BcsPlataformaWeb", "target": "ds:HANA_PLA_Servicio", "kind": "read"})

print(f"  {len(ds_edges)} data store edges")
print(f"  {len(dispatch_edges)} runtime dispatch edges")

# ─── Dead-end candidates ───────────────────────────────────────────────────────
all_target_mods = {e["target"] for e in edges + ds_edges + dispatch_edges if not e["target"].startswith("ds:")}
dead_ends = [mid for mid in modules if mid not in all_target_mods and mid not in entry_points]

# ─── Observations ─────────────────────────────────────────────────────────────
observations = [
    "Runtime reflection dispatch: CallServlet (BcsPlataformaWeb) reads 1,817+ service class names from the PLA.Servicio HANA table and resolves them via Class.forName() at request time. Static analysis cannot enumerate the full service surface without DB access; all 14 business domains are potential targets of unresolvable dispatch calls.",
    "Single point of failure: BcsPlataformaWeb is both the authentication gateway and the service dispatcher. A crash or misconfiguration here takes down all 1,817+ services across all 14 domains simultaneously.",
    "Normativa is the most complex domain (603 Java files across 7 modules) with the broadest outbound dependency set: it reads from Custodia, CuentaInversion, SocioNegocio, and Portafolio, and writes to JasperReports (131 templates) for regulatory cartolas. Any schema change in those domains requires Normativa regression testing.",
    "BcsJobManager (batch scheduler, 153+ jobs) and BcsPlataformaServlets (missing servlet implementations) are NOT compiled by the main Gradle build. They are built by a separate ANT process. Their absence from PLUGIN_OPTIMUS.jar means jdeps cannot trace their dependencies, leaving their edges unresolved in this map.",
    "HANA calculation views (156 views) and PLA.Servicio registry are shared read infrastructure. BcsPlataformaWeb, BcsCustodiaServicios, BcsOrdenServicios, BcsNormativaServicios, and BcsContabilidadServicios all depend on the same HANA schema. A HANA schema migration requires coordinating all 14 domains simultaneously.",
    "Crystal Reports (182 .rpt binary files) has no source representation and no path to static analysis. Modules that render .rpt files (BcsNormativaServicios, BcsFacturacionServicios) cannot be refactored without a parallel Crystal→JasperReports migration plan.",
    "BcsValo2 (portfolio valuation) is a high-coupling node: BcsPortafolioServicios, BcsSocioNegocioServicios, and BcsCustodiaServicios all depend on it. It reads from HANA and writes valuation results consumed by Normativa regulatory reports. It is an extraction candidate for an independent pricing microservice.",
]

# ─── Business flows ───────────────────────────────────────────────────────────
flows = [
    {
        "name": "Broker executes a securities order",
        "persona": "Broker (corredor) trading desk operator",
        "description": "A broker logs in, selects a client account, enters a buy/sell order, and receives CMF-compliant confirmation and settlement instruction.",
        "steps": [
            {"label": "Authenticate via session token", "nodes": ["mod:BcsPlataformaWeb"]},
            {"label": "Session filter validates role and routes request", "nodes": ["mod:BcsFilters", "mod:BcsPlataformaWeb"]},
            {"label": "Order creation validates client and account", "nodes": ["mod:BcsOrdenServicios", "mod:BcsSocioNegocioServicios", "mod:BcsCuentaInversionServicios"]},
            {"label": "Order booked in HANA and sent to BECB", "nodes": ["mod:BcsOrdenServicios", "ds:HANA_BSCL", "ds:BECB_SOAP"]},
            {"label": "Custody record updated at DCV settlement", "nodes": ["mod:BcsCustodiaServicios", "ds:DCV_SOAP"]},
            {"label": "Accounting journal entry created", "nodes": ["mod:BcsContabilidadServicios", "ds:HANA_BSCL"]},
            {"label": "Confirmation report generated (Crystal Reports)", "nodes": ["mod:BcsFacturacionServicios", "ds:CrystalReports"]},
        ],
    },
    {
        "name": "Client views portfolio statement",
        "persona": "Investment client (cliente)",
        "description": "A client logs in via the web portal and downloads their monthly portfolio statement with valuation, positions, and performance.",
        "steps": [
            {"label": "Client authenticates (SSO or local)", "nodes": ["mod:BcsPlataformaWeb"]},
            {"label": "Portfolio positions read from HANA", "nodes": ["mod:BcsPortafolioServicios", "ds:HANA_BSCL"]},
            {"label": "Positions valued using BcsValo2 pricing engine", "nodes": ["mod:BcsValo2", "ds:HANA_BSCL"]},
            {"label": "Statement compiled from Normativa templates", "nodes": ["mod:BcsCartolas", "ds:JasperReports"]},
            {"label": "Document served via FileDownload servlet", "nodes": ["mod:BcsPlataformaWeb"]},
        ],
    },
    {
        "name": "Regulatory report submitted to CMF",
        "persona": "Compliance officer (normativa team)",
        "description": "An automated batch job compiles trade and position data across all domains and submits a CMF regulatory report on schedule.",
        "steps": [
            {"label": "BcsTributarioJobs scheduler triggers report job", "nodes": ["mod:BcsTributarioJobs"]},
            {"label": "Trade and position data aggregated from HANA", "nodes": ["mod:BcsNormativaJobs", "mod:BcsCustodiaServicios", "ds:HANA_BSCL"]},
            {"label": "Client identity resolved from SocioNegocio", "nodes": ["mod:BcsNormativaServicios", "mod:BcsSocioNegocioServicios"]},
            {"label": "Report rendered from JasperReports templates", "nodes": ["mod:BcsNormativaServicios", "ds:JasperReports"]},
            {"label": "Report emailed to CMF (SMTP)", "nodes": ["mod:BcsNormativaJobs", "ds:SMTP"]},
        ],
    },
    {
        "name": "Mutual fund subscription processed",
        "persona": "Fund administrator (APV/FondoMutuo team)",
        "description": "A client subscribes to a mutual fund; the system validates quotas, books the purchase, updates custody, and replicates the portfolio.",
        "steps": [
            {"label": "Subscription request received via CallServlet dispatch", "nodes": ["mod:BcsPlataformaWeb", "ds:HANA_PLA_Servicio"]},
            {"label": "Fund quota and availability validated", "nodes": ["mod:BcsFondoMutuoServicios", "ds:HANA_BSCL"]},
            {"label": "Client account debited via CuentaInversion", "nodes": ["mod:BcsCuentaInversionServicios", "ds:HANA_BSCL"]},
            {"label": "Custody record updated", "nodes": ["mod:BcsCustodiaServicios", "ds:HANA_BSCL"]},
            {"label": "Portfolio replica updated with new position", "nodes": ["mod:BcsPortafolioServicios", "mod:BcsValo2"]},
            {"label": "APV tax treatment applied if applicable", "nodes": ["mod:BcsAPVServicios", "ds:HANA_BSCL"]},
        ],
    },
]

# ─── Build root tree ───────────────────────────────────────────────────────────
domain_nodes = {}
for domain, dirs in DOMAINS.items():
    children = []
    for rel_dir in dirs:
        mod_name = get_module_name(rel_dir)
        mod_id = f"mod:{mod_name}"
        if mod_id in modules:
            children.append({
                "id": mod_id,
                "name": mod_name,
                "kind": "module",
                "loc": modules[mod_id]["loc"],
                "file": f"BSCL/{rel_dir}",
            })
    domain_nodes[domain] = {
        "id": f"dom:{domain}",
        "name": domain,
        "kind": "domain",
        "children": children,
    }

# Data stores domain
ds_children = list(datastores.values())
# Remove description from tree node (goes in full topology)
for ds in ds_children:
    ds_node = {k: v for k, v in ds.items() if k != "description"}

domain_nodes["Data"] = {
    "id": "dom:Data",
    "name": "Data stores",
    "kind": "domain",
    "children": [{k: v for k, v in ds.items() if k != "description"} for ds in datastores.values()],
}

root = {
    "id": "sys",
    "name": "otr-sva-optimus-corredores",
    "kind": "system",
    "children": list(domain_nodes.values()),
}

# ─── Write domain chunks ───────────────────────────────────────────────────────
all_edges = edges + ds_edges + dispatch_edges
for domain in DOMAINS:
    chunk_file = MAP_DIR / f"{domain.lower()}.json"
    if chunk_file.exists():
        print(f"  Skipping {chunk_file.name} (already exists)")
        continue
    domain_mods = {f"mod:{get_module_name(d)}" for d in DOMAINS[domain]}
    chunk_edges = [e for e in all_edges if e["source"] in domain_mods]
    chunk = {
        "domain": domain,
        "modules": [modules[m] for m in domain_mods if m in modules],
        "edges": chunk_edges,
    }
    chunk_file.write_text(json.dumps(chunk, indent=2))
    print(f"  Wrote {chunk_file.name} ({len(chunk['modules'])} modules, {len(chunk_edges)} edges)")

# Data chunk
data_chunk_file = MAP_DIR / "data.json"
if not data_chunk_file.exists():
    data_chunk_file.write_text(json.dumps({"domain": "Data", "datastores": list(datastores.values()), "edges": ds_edges}, indent=2))
    print(f"  Wrote data.json ({len(datastores)} stores, {len(ds_edges)} edges)")

# ─── Merge into topology.json ──────────────────────────────────────────────────
# Add descriptions to leaf nodes
for child in root["children"]:
    if child["id"].startswith("dom:"):
        for leaf in child.get("children", []):
            mod_id = leaf["id"]
            if mod_id in modules:
                leaf["language"] = "java"
                leaf["loc"] = modules[mod_id]["loc"]
            elif mod_id.startswith("ds:") and mod_id in datastores:
                leaf["description"] = datastores[mod_id].get("description", "")

topology = {
    "system": "otr-sva-optimus-corredores",
    "root": root,
    "edges": all_edges,
    "entryPoints": sorted(list(entry_points)),
    "deadEnds": dead_ends,
    "observations": observations,
    "flows": flows,
}

topology_file = ANALYSIS / "topology.json"
topology_file.write_text(json.dumps(topology, indent=2))
print(f"\nWrote topology.json ({len(modules)} modules, {len(all_edges)} edges total)")

# Summary
print(f"\n=== SUMMARY ===")
print(f"Modules:       {len(modules)}")
print(f"Domains:       {len(DOMAINS)}")
print(f"Call edges:    {len(edges)} (jdeps)")
print(f"Data edges:    {len(ds_edges)}")
print(f"Dispatch edges:{len(dispatch_edges)}")
print(f"Entry points:  {len(entry_points)}")
print(f"Dead ends:     {len(dead_ends)}")
print(f"Datastores:    {len(datastores)}")
print(f"\nTop 10 modules by LOC:")
top = sorted(modules.values(), key=lambda m: m["loc"], reverse=True)[:10]
for m in top:
    print(f"  {m['name']:45s} {m['loc']:6d} LOC")
print(f"\nTop 5 most-called modules (inbound call edges):")
inbound = defaultdict(int)
for e in edges:
    inbound[e["target"]] += 1
for mod_id, cnt in sorted(inbound.items(), key=lambda x: -x[1])[:5]:
    name = modules.get(mod_id, {}).get("name", mod_id)
    print(f"  {name:45s} {cnt:4d} inbound")
