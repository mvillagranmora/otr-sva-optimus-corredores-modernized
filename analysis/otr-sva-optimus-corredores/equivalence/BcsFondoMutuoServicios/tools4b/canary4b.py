#!/usr/bin/env python3
"""Step 4B canary: breaks ONE thing in the NEW Manejador inside the ISOLATED scratch copy (tools4b/sync4b.sh; the module sources are never touched),
runs the Manejador tests (expected RED), restores the file and runs again (expected GREEN). One log per mutation.
  py -3 -I canary4b.py <scratchModuleDir> <outDir> [mutationLabel]
"""
import os
import subprocess
import sys

SCRATCH, OUT = sys.argv[1], sys.argv[2]
ONLY = sys.argv[3] if len(sys.argv) > 3 else None
SRC = os.path.join(SCRATCH, "src", "main", "java", "cl", "bcs", "fondomutuo", "manejador", "ManejadorSolicitudAporteRescateService.java")
NORMAL = "            if (entrada.p_SolicitudesAporteRescateNormal.size() > 0) pasoNormal(entrada, out);\n"
G0 = "        g0.accept(conFilas(entrada));\n"
RET = "            return out;\n"
M = {
    "1-g0-despues-de-la-primera-escritura": [(G0, ""), (NORMAL, NORMAL + "            g0.accept(conFilas(entrada));\n")],
    "2-orden-de-listas-normal-al-final": [(NORMAL, ""), (RET, NORMAL + RET)],
    "3-contador-de-la-simulacion-se-suma": [("        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);\n        HashMap<Long, ResultadoSolicitud> resultado = ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);\n        List<NormalContrato.InSolicitud>",
                                           "        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);\n        out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;\n        HashMap<Long, ResultadoSolicitud> resultado = ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);\n        List<NormalContrato.InSolicitud>")],
    "4-par-aporte-y-rescate-con-or": [("if (aportesBuenos.size() > 0 && rescatesBuenos.size() > 0)", "if (aportesBuenos.size() > 0 || rescatesBuenos.size() > 0)")],
    "5-mensajes-de-la-pasada-real-se-conservan": [("            salida = servicio.logica(in);\n            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;\n            out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);\n        }\n    }\n\n    /** CambioSerie",
                                                   "            salida = servicio.logica(in);\n            out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);\n            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;\n            out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);\n        }\n    }\n\n    /** CambioSerie")],
    "6-filtro-de-buenas-invertido": [("filter(s -> resultado.get(s.IdInterno) != null)", "filter(s -> resultado.get(s.IdInterno) == null)")],
    "7-forma-pago-spot-ausente-tolerada": [("in.p_FormaPagoSpot.stream().filter(f ->", "(in.p_FormaPagoSpot == null ? new java.util.ArrayList<NormalContrato.InFormaPagoSpot>() : in.p_FormaPagoSpot).stream().filter(f ->")],
    "8-canal-nulo-no-pisa-el-por-defecto": [("        in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;   // null overwrites",
                                              "        if (entrada.p_IdTipoCanalIngreso != null) in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;   // null overwrites")],
}


def mvn(log):
    env = dict(os.environ, JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot")
    r = subprocess.run(["mvn", "-o", "-f", os.path.join(SCRATCH, "pom.xml"), "test", "-Dtest=cl.bcs.fondomutuo.manejador.*Test", "-DfailIfNoTests=false",
                        "-Dsurefire.failIfNoSpecifiedTests=false", "-Dfile.encoding=UTF-8"], capture_output=True, text=True, encoding="utf-8", errors="replace",
                       shell=True, env=env)
    lines = [l[:240] for l in r.stdout.splitlines() if "Tests run:" in l or "BUILD" in l or "FAILURE!" in l or "COMPILATION" in l]
    log.write("\n".join(lines[:40]) + "\n")
    return "BUILD SUCCESS" in r.stdout


os.makedirs(OUT, exist_ok=True)
orig = open(SRC, encoding="utf-8", newline="").read()
for label, cambios in M.items():
    if ONLY and ONLY != label:
        continue
    text = orig
    for a, b in cambios:
        assert text.count(a) == 1, (label, "pattern count", text.count(a), a[:60])
        text = text.replace(a, b)
    with open(os.path.join(OUT, label + ".log"), "w", encoding="utf-8", newline="\n") as log:
        log.write("CANARY %s  [isolated scratch copy; the module sources are never touched]\nmutated: manejador/ManejadorSolicitudAporteRescateService.java\n" % label)
        for a, b in cambios:
            log.write("---- replaced ----\n%s---- by ----\n%s" % (a, b))
        open(SRC, "w", encoding="utf-8", newline="").write(text)
        log.write("---- expected: tests RED ----\n")
        green = mvn(log)
        log.write("RESULT mutated: %s\n" % ("GREEN (MUTANT SURVIVED)" if green else "RED (mutant killed)"))
        open(SRC, "w", encoding="utf-8", newline="").write(orig)
        os.utime(SRC, None)
        log.write("---- restored: expected GREEN ----\n")
        green = mvn(log)
        log.write("RESULT restored: %s\nCANARYDONE\n" % ("GREEN" if green else "RED (PROBLEM)"))
    print(label, "done")
