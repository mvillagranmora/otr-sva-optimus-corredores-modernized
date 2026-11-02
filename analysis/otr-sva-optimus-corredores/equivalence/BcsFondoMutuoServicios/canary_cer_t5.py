"""F3 W3 T5: GOLDEN-MUTATION check of the goldens of front J (plan F3 section 4: break the code in one small way that matters, confirm that at least one test goes red, restore).

There is no production code of T5 yet (red before code), so the mutants are of the ORACLE: copies of the legacy source `CertificarSolicitudAporteRescate.java` with ONE line of front J changed
(a LEGACY_OVERLAY directory under the temp dir; `legacy/` is only read and NEVER written). Each mutant is compiled and run by `legacy-oracle/record_cer_t5.sh` over the whole matrix
(ORACLE_OUT = a temp dir, ONE run per case: the goldens are never overwritten) and every case is compared with its golden (the key `_provenance` is ignored). A mutant is KILLED when at least one
case differs: the matrix pins that line. Controls: (1) the unmodified legacy re-recorded the same way must differ in NO case (the harness and the determinism are sound); (2) the neutrality of the
observation line of the recursion (`ORACLE_T4_SPY=0` compiles the source as it is) must give the same calls as the goldens once the child records are expanded in place.

  python -X utf8 -I canary_cer_t5.py [text of the mutation name]
Writes golden-mutation-cer-t5/GOLDEN_MUTATION_T5.json (mutant, killed, number of cases that differ, the first cases) and prints the summary. Needs JDK 25 (JAVA_HOME) and the local Maven
repository jars the record scripts use. Takes about 2-3 minutes per mutant. Exit 1 if a control differs or a mutant survives.
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
LEGACY = WS / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo" / "CertificarSolicitudAporteRescate.java"
SALIDA = BASE / "golden-mutation-cer-t5"
HIJO = "CertificarSolicitudAporteRescate.hijo"
BASH = os.environ.get("BASH_EXE") or (next((c for c in ("C:/Program Files/Git/bin/bash.exe", "C:/Program Files/Git/usr/bin/bash.exe") if os.path.exists(c)), None) or "bash")   # NOT the WSL bash of System32
LECTURAS = set()

# (name, legacy text, replacement, occurrence 1-based): ONE line of front J (C:835-864, 1580-1643, 2738-2880), plus the recursion (C:2764-2775)
MUTACIONES = [
    ("J01 the UPDATE skips the warned solicitudes (ADV)", "List<SolicitudesProcesoCertificacion> solActualizar = filtrarSolicitudesPendientesYAdvertencias(solicitudes).collect(Collectors.toList());",
     "List<SolicitudesProcesoCertificacion> solActualizar = filtrarSolicitudesPendientes(solicitudes).collect(Collectors.toList());", 1),
    ("J02 the UPDATE needs two solicitudes", "if(solActualizar.size() > 0){", "if(solActualizar.size() > 1){", 1),
    ("J03 the asynchronous notification is never sent", "notificar(con, solActualizar, entrada);", "/* notificar(con, solActualizar, entrada); */", 1),
    ("J04 the asynchronous notification carries the module of the request changed", "entradaNotificador.p_IdModulo = entrada.p_IdModulo;", "entradaNotificador.p_IdModulo = \"XXX\";", 1),
    ("J05 OWP needs the parameter OR the entry", "if(_c.SI.equalsIgnoreCase(notificaOWP) && _c.SI.equalsIgnoreCase(entrada.p_NotificaOWP)){", "if(_c.SI.equalsIgnoreCase(notificaOWP) || _c.SI.equalsIgnoreCase(entrada.p_NotificaOWP)){", 1),
    ("J06 OWP puts the updated solicitudes before the switch aportes", "idSolicitudes.addAll(idSolicitudesAportesSwitch);\n\t\tidSolicitudes.addAll(idSolicitudesActualizar);",
     "idSolicitudes.addAll(idSolicitudesActualizar);\n\t\tidSolicitudes.addAll(idSolicitudesAportesSwitch);", 1),
    ("J07 OWP market is not F", "entradaSolicitud.Mercado = \"F\";", "entradaSolicitud.Mercado = \"X\";", 1),
    ("J08 OWP quantity is the quota value", "entradaSolicitud.NTitulos = solicitud.NumeroCuotas;", "entradaSolicitud.NTitulos = solicitud.ValorCuota;", 1),
    ("J09 OWP sends even an empty list", "if(listOrdenNotiOWP.size() > 0){", "if(listOrdenNotiOWP.size() > -1){", 1),
    ("J10 the system log is not written", "agregarLogSistema(con,entrada,fechaSistema);", "/* agregarLogSistema(con,entrada,fechaSistema); */", 1),
    ("J11 the system log has another register state", "log.EstadoRegistro = _c.EstadoCertificado;", "log.EstadoRegistro = _c.EstadoRechazado;", 1),
    ("J12 the approved code is not UI_EXITO", "solicitud.Codigo = \"UI_EXITO\";", "solicitud.Codigo = \"UI_OK\";", 1),
    ("J13 the output prints the id of the state and not its name", "resultadoCertificacion.NombreEstado = estado.Nombre;", "resultadoCertificacion.NombreEstado = estado.IdEstado;", 1),
    ("J14 the simulation does not force the counter", "out.P_OUT_ERRORESCONTADOR=1L;\n\t\t}\n\t\t\n\t\tlogger.info(\"FIN::CertificarSolicitudAporteRescate\");", "out.P_OUT_ERRORESCONTADOR=out.P_OUT_ERRORESCONTADOR;\n\t\t}\n\t\t\n\t\tlogger.info(\"FIN::CertificarSolicitudAporteRescate\");", 1),
    ("J15 the log of certification loses the simulation flag", "l.Simulacion = entrada.p_SimularOperacion;", "l.Simulacion = \"N\";", 1),
    ("J16 the log of certification exchange rate is not the parity", "l.TipoCambio =s.ParidadFechaCertificacion ;", "l.TipoCambio =BigDecimal.ONE;", 1),
    ("J17 the child output does not simulate", "if(_c.SI.equals(entrada.p_CertificacionAporteSwitchSimulado)) {", "if(_c.NO.equals(entrada.p_CertificacionAporteSwitchSimulado)) {", 1),
    ("J18 the child notifies OWP", "entradaSwitch.p_NotificaOWP = \"N\";", "entradaSwitch.p_NotificaOWP = \"S\";", 1),
    ("J19 the child results are not added", "out.P_OUT_RESULTADO.addAll(out_switch.P_OUT_RESULTADO);", "", 1),
    ("J20 the child counter is not added", "out.P_OUT_ERRORESCONTADOR += out_switch.P_OUT_ERRORESCONTADOR;", "", 1),
    ("J21 FM-CER-64 is not written", "cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot(con,solicitudes);", "/* cambiar... */", 1),
    ("J22 the child does not copy the simulation of the request", "p_CertificacionAporteSwitchSimulado = entradaSwitch.p_SimularOperacion;", "p_CertificacionAporteSwitchSimulado = \"N\";", 1),
]


def comparable(texto):
    j = json.loads(texto)
    j.pop("_provenance", None)
    return j


def aplanar(calls, out):
    for c in calls:
        if c["colaborador"] == HIJO:
            aplanar(c.get("internas", []), out)
        else:
            out.append({"colaborador": c["colaborador"], "argumentos": c["argumentos"], "respuesta": c["respuesta"]})


def grabar(extra_env, destino, work):
    env = dict(os.environ)
    env.update({"PYTHON_CMD": "python -X utf8", "ORACLE_WORK": work.as_posix(), "ORACLE_OUT": destino.as_posix(), "ORACLE_REPORT": (destino / "informe.txt").as_posix(), "ORACLE_REPEAT": "1",
                "M2_REPO": os.environ.get("M2_REPO", (pathlib.Path.home() / ".m2" / "repository").as_posix())})
    env.update(extra_env)
    destino.mkdir(parents=True, exist_ok=True)
    r = subprocess.run([BASH, "legacy-oracle/record_cer_t5.sh", "FMM-CER"], env=env, capture_output=True, text=True, encoding="utf-8", cwd=str(BASE), stdin=subprocess.DEVNULL)
    if r.returncode != 0:
        print(r.stdout[-1500:])
        print(r.stderr[-1500:])
        raise SystemExit("record_cer_t5.sh failed (%s)" % r.returncode)


def comparar(destino):
    distintos = []
    for d in sorted((BASE / "cases-cer-t5").glob("FMM-CER-*")):
        g = comparable((d / "expected.json").read_text(encoding="utf-8"))
        n = destino / (d.name + ".json")
        if not n.exists():
            distintos.append(d.name + " (not recorded)")
            continue
        if comparable(n.read_text(encoding="utf-8")) != g:
            distintos.append(d.name)
    return distintos


def main():
    filtro = sys.argv[1] if len(sys.argv) > 1 else ""
    fuente = LEGACY.read_text(encoding="utf-8")
    SALIDA.mkdir(exist_ok=True)
    temp = pathlib.Path(tempfile.mkdtemp(prefix="canary_t5_"))
    work = temp / "work"
    work.mkdir()
    resultado = {"mutants": [], "controls": []}
    malos = []
    # control 1: the unmodified legacy, one run per case: no case may differ from its golden
    if not filtro:
        destino = temp / "control"
        grabar({}, destino, work)
        d = comparar(destino)
        resultado["controls"].append({"name": "C1 unmodified legacy re-recorded (1 run per case)", "differs": len(d), "cases": d[:5]})
        if d:
            malos.append("control C1 differs in %d cases" % len(d))
        print("C1 unmodified: %d cases differ (expected 0)" % len(d))
        # control 2: the observation line of the recursion is neutral
        destino = temp / "sinespia"
        grabar({"ORACLE_T4_SPY": "0"}, destino, work)
        malos_n = []
        con_hijo = 0
        for dd in sorted((BASE / "cases-cer-t5").glob("FMM-CER-*")):
            g = json.loads((dd / "expected.json").read_text(encoding="utf-8"))
            if not any(c["colaborador"] == HIJO for c in json.dumps(g["colaboradores"]) and g["colaboradores"]):
                continue
            con_hijo += 1
            s = json.loads((destino / (dd.name + ".json")).read_text(encoding="utf-8"))
            a, b = [], []
            aplanar(g["colaboradores"], a)
            aplanar(s["colaboradores"], b)
            if json.dumps(a, sort_keys=True) != json.dumps(b, sort_keys=True):
                malos_n.append(dd.name)
        resultado["controls"].append({"name": "C2 the observation line of the recursion is neutral (ORACLE_T4_SPY=0, child records expanded)", "cases": con_hijo, "differs": len(malos_n), "list": malos_n[:5]})
        if malos_n:
            malos.append("control C2 differs in %d cases" % len(malos_n))
        print("C2 neutrality of the observation line: %d cases with a child, %d differ (expected 0)" % (con_hijo, len(malos_n)))
    for nombre, viejo, nuevo, ocurrencia in MUTACIONES:
        if filtro and filtro not in nombre:
            continue
        if fuente.count(viejo) < ocurrencia:
            malos.append("%s: the text to mutate is not in the legacy source" % nombre)
            continue
        partes = fuente.split(viejo)
        mutado = viejo.join(partes[:ocurrencia]) + nuevo + viejo.join(partes[ocurrencia:])
        overlay = temp / "overlay"
        if overlay.exists():
            shutil.rmtree(overlay)
        overlay.mkdir()
        (overlay / LEGACY.name).write_text(mutado, encoding="utf-8", newline="")
        destino = temp / ("m_" + nombre.split(" ")[0])
        grabar({"LEGACY_OVERLAY": overlay.as_posix()}, destino, work)
        d = comparar(destino)
        matado = len(d) > 0
        resultado["mutants"].append({"name": nombre, "killed": matado, "cases_that_differ": len(d), "first_cases": d[:5]})
        print("%s: %s (%d cases differ)" % (nombre, "KILLED" if matado else "SURVIVED", len(d)))
        if not matado:
            malos.append("%s survived: no case pins that line of front J" % nombre)
    if not filtro:
        (SALIDA / "GOLDEN_MUTATION_T5.json").write_text(json.dumps(resultado, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    shutil.rmtree(temp, ignore_errors=True)
    for m in malos:
        print("!!", m)
    print("mutants killed: %d of %d" % (sum(1 for m in resultado["mutants"] if m["killed"]), len(resultado["mutants"])))
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
