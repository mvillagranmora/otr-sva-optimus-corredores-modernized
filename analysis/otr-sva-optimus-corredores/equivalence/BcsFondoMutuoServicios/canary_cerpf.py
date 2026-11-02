"""F3 W3 T6-A: GOLDEN-MUTATION check of the goldens of CertificarPorFiltrosSolicitudAporteRescate (plan F3 section 4: break the code in one small way that matters, confirm that at least one test goes
red, restore).

There is no production code of T6-A yet (red before code), so the mutants are of the ORACLE: copies of the legacy source `CertificarPorFiltrosSolicitudAporteRescate.java` with ONE line changed, or
copies of its SQL `CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql` with ONE clause changed (a LEGACY_OVERLAY / LEGACY_OVERLAY_RES directory under the temp dir; `legacy/` is only read and
NEVER written). Each mutant is compiled (Java) and run by `legacy-oracle/record_cerpf.sh` over the whole matrix (ORACLE_OUT = a temp dir, ONE run per case: the goldens are never overwritten) and every
case is compared with its golden (the key `_provenance` is ignored). A mutant is KILLED when at least one case differs: the matrix pins that line. Controls: (1) the unmodified legacy re-recorded the same
way must differ in NO case (the harness and the determinism are sound); (2) the neutrality of the observation line of the class (`ORACLE_PF_SPY=0` compiles the source as it is) must give the same output,
tables and collaborator calls as the goldens once the records of the spies are expanded in place.

  python -X utf8 -I canary_cerpf.py [text of the mutation name]
Writes golden-mutation-cerpf/GOLDEN_MUTATION_T6A.json (mutant, killed, number of cases that differ, the first cases) and prints the summary. Needs JDK 25 (JAVA_HOME) and the local Maven repository
jars the record scripts use. The SQL mutants reuse the classes compiled by the control run; each Java mutant is compiled (about 1-2 minutes). Exit 1 if a control differs or a mutant survives.
"""
import json
import os
import pathlib
import shutil
import subprocess
import sys
import tempfile

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
FUENTE = WS / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo"
JAVA = FUENTE / "CertificarPorFiltrosSolicitudAporteRescate.java"
SQL = FUENTE / "sql" / "CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql"
SALIDA = BASE / "golden-mutation-cerpf"
BASH = os.environ.get("BASH_EXE") or (next((c for c in ("C:/Program Files/Git/bin/bash.exe", "C:/Program Files/Git/usr/bin/bash.exe") if os.path.exists(c)), None) or "bash")   # NOT the WSL bash of System32
REGISTROS = ("CertificarPorFiltros.certificar", "CertificarSolicitudAporteRescate.hijo")

# (name, legacy text, replacement, occurrence 1-based). `P` = the Java source of the class, `S` = its SQL.
MUT_JAVA = [
    ("P01 the select does not get the company", 'params.put("IdEmpresa", entrada.p_IdEmpresa);', '/* params.put("IdEmpresa", entrada.p_IdEmpresa); */', 1),
    ("P02 only the first filter is queried", "for(InFiltros filtro : entrada.p_Filtros){", "for(InFiltros filtro : entrada.p_Filtros.subList(0, Math.min(1, entrada.p_Filtros.size()))){", 1),
    ("P03 the ids are de-duplicated", "entradaCertificarSolicitudAporteRescate.p_Solicitudes.addAll(sql.query(con, SQL_SELECT_SOLICITUDES, params, CertificarSolicitudAporteRescate.InSolicitudes.class));",
     "for (CertificarSolicitudAporteRescate.InSolicitudes nuevo : sql.query(con, SQL_SELECT_SOLICITUDES, params, CertificarSolicitudAporteRescate.InSolicitudes.class)) { if (entradaCertificarSolicitudAporteRescate.p_Solicitudes.stream().noneMatch(x -> x.IdSolicitudAporteRescate.equals(nuevo.IdSolicitudAporteRescate))) entradaCertificarSolicitudAporteRescate.p_Solicitudes.add(nuevo); }", 1),
    ("P04 Nacional gets the money-market flag", 'params.put("Nacional", filtro.Nacional);', 'params.put("Nacional", filtro.MoneyMarket);', 1),
    ("P05 the filters are queried in reverse order", "for(InFiltros filtro : entrada.p_Filtros){",
     "for(InFiltros filtro : ((java.util.function.Supplier<List<InFiltros>>)() -> { List<InFiltros> r = new ArrayList<InFiltros>(entrada.p_Filtros); java.util.Collections.reverse(r); return r; }).get()){", 1),
    ("P06 the canal is not copied", "entradaCertificarSolicitudAporteRescate.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;", 'entradaCertificarSolicitudAporteRescate.p_IdTipoCanalIngreso = "";', 1),
    ("P07 the user is not copied", "entradaCertificarSolicitudAporteRescate.p_IdUsuario = entrada.p_IdUsuario;", "entradaCertificarSolicitudAporteRescate.p_IdUsuario = 7L;", 1),
    ("P08 the simulation is not copied", "entradaCertificarSolicitudAporteRescate.p_SimularOperacion = entrada.p_SimularOperacion;", 'entradaCertificarSolicitudAporteRescate.p_SimularOperacion = "N";', 1),
    ("P09 the date is not copied to Certificar", "entradaCertificarSolicitudAporteRescate.p_FechaCertificacion = entrada.p_FechaCertificacion;", 'entradaCertificarSolicitudAporteRescate.p_FechaCertificacion = "2026-03-02";', 1),
    ("P10 the business-rules flag is not copied", "entradaCertificarSolicitudAporteRescate.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;", 'entradaCertificarSolicitudAporteRescate.p_UsaReglasNegocio = "N";', 1),
    ("P11 the module is not copied", "entradaCertificarSolicitudAporteRescate.p_IdModulo = entrada.p_IdModulo;", 'entradaCertificarSolicitudAporteRescate.p_IdModulo = "FMM";', 1),
    ("P12 the company is not copied", "entradaCertificarSolicitudAporteRescate.p_IdEmpresa = entrada.p_IdEmpresa;", "entradaCertificarSolicitudAporteRescate.p_IdEmpresa = 1L;", 1),
    ("P13 the notification of OWP is switched off", "//GENERALMENTE ES UN SOLO FILTRO", 'entradaCertificarSolicitudAporteRescate.p_NotificaOWP = "N";', 1),
    ("P14 the counter is not copied", "out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;", "out.P_OUT_ERRORESCONTADOR = 0L;", 1),
    ("P15 the messages are not copied", "out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;", "out.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();", 1),
    ("P16 the results are not copied", "out.P_OUT_RESULTADO = salida.P_OUT_RESULTADO;", "out.P_OUT_RESULTADO = new ArrayList<ResultadoCertificacion>();", 1),
    ("P17 the load id is not passed", 'params.put("IdCarga", filtro.IdCarga);', 'params.put("IdCarga", Long.valueOf(0L));', 1),
    ("P18 the type of request is not passed", 'params.put("TipoSolicitud", filtro.TipoSolicitud);', 'params.put("TipoSolicitud", "");', 1),
    ("P19 the currency is not passed", 'params.put("IdMoneda", filtro.IdMoneda);', 'params.put("IdMoneda", "");', 1),
    ("P20 the issuer is not passed", 'params.put("IdEmisor", filtro.IdEmisor);', 'params.put("IdEmisor", "");', 1),
    ("P21 the movement type is not passed", 'params.put("IdTipoMovimientoFondoMutuo", filtro.IdTipoMovimientoFondoMutuo);', 'params.put("IdTipoMovimientoFondoMutuo", "");', 1),
    ("P22 the series is not passed", 'params.put("IdSerieFondo", filtro.IdSerieFondo);', 'params.put("IdSerieFondo", "");', 1),
    ("P23 the money market flag is not passed", 'params.put("MoneyMarket", filtro.MoneyMarket);', 'params.put("MoneyMarket", "N");', 1),
    ("P24 the date of the select is not the one of the request", 'params.put("FechaCertificacion", entrada.p_FechaCertificacion);', 'params.put("FechaCertificacion", "2026-03-02");', 1),
]
MUT_SQL = [
    ("S01 the select does not filter by company", '\n AND "Solicitudes"."IdEmpresa" = :IdEmpresa', "", 1),
    ("S02 only ING is selected, not ACP", "IN ('ING','ACP')", "IN ('ING')", 1),
    ("S03 the detail row must be PRO, not PEN", "\"Detalle\".\"EstadoProceso\" = 'PEN'", "\"Detalle\".\"EstadoProceso\" = 'PRO'", 1),
    ("S04 Nacional null means S, not N", "COALESCE(:Nacional,'N') = 'N'", "COALESCE(:Nacional,'S') = 'N'", 1),
    ("S05 no DISTINCT", "SELECT DISTINCT", "SELECT", 1),
    ("S06 the date of certification is not filtered", '\n AND "Solicitudes"."FechaCertificacion" = :FechaCertificacion', "", 1),
    ("S07 MoneyMarket null means S, not N", "COALESCE(:MoneyMarket,'N') = 'N'", "COALESCE(:MoneyMarket,'S') = 'N'", 1),
    ("S08 an empty or null issuer is not 'no filter'", "COALESCE(:IdEmisor,'') = ''", "COALESCE(:IdEmisor,'x') = ''", 1),
    ("S09 CRT is selected too", "IN ('ING','ACP')", "IN ('ING','ACP','CRT')", 1),
    ("S10 the load needs the first detail row only (AND becomes OR)", "(\"Detalle\".\"IdCargaSolicitudesAporteRescate\" = :IdCarga AND", "(\"Detalle\".\"IdCargaSolicitudesAporteRescate\" = :IdCarga OR", 1),
    ("S11 IdCarga 0 is not 'no load' (it asks for the detail)", "(:IdCarga = 0 OR", "(:IdCarga = -1 OR", 1),
    ("S12 the type of request compares another column", ':TipoSolicitud = "Solicitudes"."TipoSolicitud"', ':TipoSolicitud = "Solicitudes"."IdTipoMovimientoFondoMutuo"', 1),
    ("S13 the currency compares another column", ':IdMoneda = "Solicitudes"."IdMoneda"', ':IdMoneda = "Solicitudes"."IdMonedaPago"', 1),
    ("S14 the series is not filtered", ':IdSerieFondo = "Solicitudes"."IdSerieFondo"', ":IdSerieFondo = :IdSerieFondo", 1),
    ("S15 the movement type is not filtered", ':IdTipoMovimientoFondoMutuo = "Solicitudes"."IdTipoMovimientoFondoMutuo"', ":IdTipoMovimientoFondoMutuo = :IdTipoMovimientoFondoMutuo", 1),
    ("S16 Nacional compares the money-market column", ':Nacional = "Solicitudes"."EsFondoNacional"', ':Nacional = "Solicitudes"."MoneyMarket"', 1),
]


def comparable(texto):
    j = json.loads(texto)
    j.pop("_provenance", None)
    return j


def aplanar(calls, out):
    for c in calls:
        if c["colaborador"] in REGISTROS:
            aplanar(c.get("internas", []), out)
        else:
            out.append({"colaborador": c["colaborador"], "argumentos": c["argumentos"], "respuesta": c["respuesta"]})


def grabar(extra_env, destino, work):
    env = dict(os.environ)
    env.update({"PYTHON_CMD": "python -X utf8", "ORACLE_WORK": work.as_posix(), "ORACLE_OUT": destino.as_posix(), "ORACLE_REPORT": (destino / "informe.txt").as_posix(), "ORACLE_REPEAT": "1",
                "M2_REPO": os.environ.get("M2_REPO", (pathlib.Path.home() / ".m2" / "repository").as_posix())})
    env.update(extra_env)
    destino.mkdir(parents=True, exist_ok=True)
    r = subprocess.run([BASH, "legacy-oracle/record_cerpf.sh", "FMM-CERPF"], env=env, capture_output=True, text=True, encoding="utf-8", cwd=str(BASE), stdin=subprocess.DEVNULL)
    if r.returncode != 0:
        print(r.stdout[-1500:])
        print(r.stderr[-1500:])
        raise SystemExit("record_cerpf.sh failed (%s)" % r.returncode)


def comparar(destino):
    distintos = []
    for d in sorted((BASE / "cases-cerpf").glob("FMM-CERPF-*")):
        g = comparable((d / "expected.json").read_text(encoding="utf-8"))
        n = destino / (d.name + ".json")
        if not n.exists():
            distintos.append(d.name + " (not recorded)")
            continue
        if comparable(n.read_text(encoding="utf-8")) != g:
            distintos.append(d.name)
    return distintos


def mutar(fuente, viejo, nuevo, ocurrencia):
    if fuente.count(viejo) < ocurrencia:
        return None
    partes = fuente.split(viejo)
    return viejo.join(partes[:ocurrencia]) + nuevo + viejo.join(partes[ocurrencia:])


def main():
    filtro = sys.argv[1] if len(sys.argv) > 1 else ""
    java = JAVA.read_text(encoding="utf-8")
    sql = SQL.read_text(encoding="utf-8")
    SALIDA.mkdir(exist_ok=True)
    temp = pathlib.Path(tempfile.mkdtemp(prefix="canary_t6a_"))
    work_sql = temp / "work_sql"        # compiled by the control run, reused by the SQL mutants (ORACLE_SKIP_COMPILE)
    work_sql.mkdir()
    resultado = {"mutants": [], "controls": []}
    malos = []
    if not filtro:
        destino = temp / "control"
        grabar({}, destino, work_sql)
        d = comparar(destino)
        resultado["controls"].append({"name": "C1 unmodified legacy re-recorded (1 run per case)", "differs": len(d), "cases": d[:5]})
        if d:
            malos.append("control C1 differs in %d cases" % len(d))
        print("C1 unmodified: %d cases differ (expected 0)" % len(d))
        # control 2: the observation line of the class is neutral
        destino = temp / "sinespia"
        work_c2 = temp / "work_c2"
        work_c2.mkdir()
        grabar({"ORACLE_PF_SPY": "0"}, destino, work_c2)
        malos_n = []
        n_casos = 0
        for dd in sorted((BASE / "cases-cerpf").glob("FMM-CERPF-*")):
            g = json.loads((dd / "expected.json").read_text(encoding="utf-8"))
            s = json.loads((destino / (dd.name + ".json")).read_text(encoding="utf-8"))
            n_casos += 1
            a, b = [], []
            aplanar(g["colaboradores"], a)
            aplanar(s["colaboradores"], b)
            iguales = json.dumps(a, sort_keys=True) == json.dumps(b, sort_keys=True)
            for k in ("salida", "excepcion", "tablasIniciales", "tablasTrasLaLlamada", "tablas", "decisionServlet", "secuenciasSolicitadas", "sesion"):
                if json.dumps(g.get(k), sort_keys=True) != json.dumps(s.get(k), sort_keys=True):
                    iguales = False
            if not iguales:
                malos_n.append(dd.name)
        resultado["controls"].append({"name": "C2 the observation line of the class is neutral (ORACLE_PF_SPY=0, records expanded)", "cases": n_casos, "differs": len(malos_n), "list": malos_n[:5]})
        if malos_n:
            malos.append("control C2 differs in %d cases" % len(malos_n))
        print("C2 neutrality of the observation line: %d cases, %d differ (expected 0)" % (n_casos, len(malos_n)))
    # SQL mutants first (the classes compiled by the control run are reused)
    if (work_sql / "classes").exists() or filtro:
        pass
    for nombre, viejo, nuevo, ocurrencia in MUT_SQL:
        if filtro and filtro not in nombre:
            continue
        mutado = mutar(sql, viejo, nuevo, ocurrencia)
        if mutado is None:
            malos.append("%s: the text to mutate is not in the legacy SQL" % nombre)
            continue
        overlay = temp / "overlay_res"
        if overlay.exists():
            shutil.rmtree(overlay)
        (overlay / "cl" / "bcs" / "fondomutuo" / "sql").mkdir(parents=True)
        (overlay / "cl" / "bcs" / "fondomutuo" / "sql" / SQL.name).write_text(mutado, encoding="utf-8", newline="")
        destino = temp / ("m_" + nombre.split(" ")[0])
        env = {"LEGACY_OVERLAY_RES": overlay.as_posix()}
        trabajo = work_sql
        if (work_sql / "classes").exists():
            env["ORACLE_SKIP_COMPILE"] = "1"
        else:
            trabajo = temp / "work_filtro"
            trabajo.mkdir(exist_ok=True)
        grabar(env, destino, trabajo)
        d = comparar(destino)
        resultado["mutants"].append({"name": nombre, "killed": len(d) > 0, "cases_that_differ": len(d), "first_cases": d[:5]})
        print("%s: %s (%d cases differ)" % (nombre, "KILLED" if d else "SURVIVED", len(d)))
        if not d:
            malos.append("%s survived: no case pins that clause of the select" % nombre)
    for nombre, viejo, nuevo, ocurrencia in MUT_JAVA:
        if filtro and filtro not in nombre:
            continue
        mutado = mutar(java, viejo, nuevo, ocurrencia)
        if mutado is None:
            malos.append("%s: the text to mutate is not in the legacy source" % nombre)
            continue
        overlay = temp / "overlay"
        if overlay.exists():
            shutil.rmtree(overlay)
        overlay.mkdir()
        (overlay / JAVA.name).write_text(mutado, encoding="utf-8", newline="")
        destino = temp / ("m_" + nombre.split(" ")[0])
        trabajo = temp / "work_java"
        trabajo.mkdir(exist_ok=True)
        grabar({"LEGACY_OVERLAY": overlay.as_posix()}, destino, trabajo)
        d = comparar(destino)
        resultado["mutants"].append({"name": nombre, "killed": len(d) > 0, "cases_that_differ": len(d), "first_cases": d[:5]})
        print("%s: %s (%d cases differ)" % (nombre, "KILLED" if d else "SURVIVED", len(d)))
        if not d:
            malos.append("%s survived: no case pins that line of the class" % nombre)
    if not filtro:
        (SALIDA / "GOLDEN_MUTATION_T6A.json").write_text(json.dumps(resultado, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    shutil.rmtree(temp, ignore_errors=True)
    for m in malos:
        print("!!", m)
    print("mutants killed: %d of %d" % (sum(1 for m in resultado["mutants"] if m["killed"]), len(resultado["mutants"])))
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
