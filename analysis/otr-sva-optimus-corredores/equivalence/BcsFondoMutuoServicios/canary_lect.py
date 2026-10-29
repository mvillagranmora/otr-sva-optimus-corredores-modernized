"""F3 W1 fronts 2-4 canary of the goldens (plan F2 section 7, "mutation of goldens"): one-line mutations of a COPY of each legacy class (LEGACY_OVERLAY + ORACLE_OUT;
nothing under legacy/ is touched, the goldens are not rewritten) are run through the oracle and the dumps are compared with the recorded goldens. A mutation that
changes no golden is an EQUIVALENT mutant or a hole of the case set. Prints "<class> <mutation>: <n> golden(s) changed".

  py -3 -I canary_lect.py [fmr|ifm|vdo] [text of the mutation name]
"""
import json
import pathlib
import shutil
import subprocess
import sys
import tempfile

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
LEG = WS / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo"
BASH = shutil.which("bash") or "C:/Program Files/Git/usr/bin/bash.exe"

CLASES = {
    "fmr": ("ConsultarFondoMutuoReinversion", "FMM-FMR"),
    "ifm": ("ConsultarInstrumentosFondosMutuos", "FMM-IFM"),
    "vdo": ("ConsultarValorDolarFondoMutuo", "FMM-VDO"),
}

MUTACIONES = {
    "fmr": [
        ("MontoMinimo copiado de MontoPatrimonioEmisor", "aux.MontoMinimo = item.MontoMinimo;", "aux.MontoMinimo = item.MontoPatrimonioEmisor;"),
        ("MLT copiado de IdMoneda", "aux.MLT = item.MLT;", "aux.MLT = item.IdMoneda;"),
        ("se pierde el IdRegimenFondoMutuo", "aux.IdRegimenFondoMutuo = item.IdRegimenFondoMutuo;", ""),
        ("p_IdEstado se envia fijo ACT", 'params_query.put("IdEstado", in.p_IdEstado);', 'params_query.put("IdEstado", "ACT");'),
        ("p_IdEmpresa se envia fijo 1", 'params_query.put("IdEmpresa", in.p_IdEmpresa);', 'params_query.put("IdEmpresa", 1L);'),
        ("la serie de origen no se envia", 'params_query.put("IdSerieFondoOrigen", in.p_IdSerieFondoOrigen);', 'params_query.put("IdSerieFondoOrigen", "-");'),
    ],
    "ifm": [
        ("OcultarBloqueados equalsIgnoreCase -> equals", '"S".equalsIgnoreCase(in.p_EntradaFondosMutuos.OcultarBloqueados)', '"S".equals(in.p_EntradaFondosMutuos.OcultarBloqueados)'),
        ("umbral de FiltroRegimen > 0 -> > 1", "IdRegimenFondoMutuo.size() > 0 ? 1L : 0L", "IdRegimenFondoMutuo.size() > 1 ? 1L : 0L"),
        ("la rama normal tambien con origen nulo", '"".equals(in.p_EntradaFondosMutuos.IdSerieFondoOrigen)', '(in.p_EntradaFondosMutuos.IdSerieFondoOrigen == null || "".equals(in.p_EntradaFondosMutuos.IdSerieFondoOrigen))'),
        ("ContadorErrores 1 -> 2", "out.ContadorErrores = 1L;", "out.ContadorErrores = 2L;"),
        ("filtro de regimenes por serie invertido", "regimen.IdSerieFondo.equals(salida.IdSerieFondo)", "!regimen.IdSerieFondo.equals(salida.IdSerieFondo)"),
        ("MostrarBloqueados N/S intercambiados", 'MostrarBloqueados="N";\n\t\t\t}else {\n\t\t\t\tMostrarBloqueados="S";', 'MostrarBloqueados="S";\n\t\t\t}else {\n\t\t\t\tMostrarBloqueados="N";'),
    ],
    "vdo": [
        ("APO usa el dolar de rescate", "calcularParidadSalida.ValorDolarAporte : calcularParidadSalida.ValorDolarRescate", "calcularParidadSalida.ValorDolarRescate : calcularParidadSalida.ValorDolarAporte"),
        ("no se deduplican las fechas", "if(fechasRepetidas.get(serie.Fecha) != null) {", "if(false) {"),
        ("UsaDolarObservado equals -> equalsIgnoreCase", "_c.SI.equals(usaDolarObservado)", "_c.SI.equalsIgnoreCase(usaDolarObservado)"),
        ("moneda origen USD -> EUR", 'calculoEntrada.IdMonedaOrigen = "USD";', 'calculoEntrada.IdMonedaOrigen = "EUR";'),
        ("MontoOrigen 1 -> 10", "calculoEntrada.MontoOrigen = BigDecimal.ONE;", "calculoEntrada.MontoOrigen = BigDecimal.TEN;"),
        ("sin paridad: 0 -> null (rama observada)", "(calcularParidadSalida != null) ? calcularParidadSalida.MontoDestino : BigDecimal.ZERO;", "(calcularParidadSalida != null) ? calcularParidadSalida.MontoDestino : null;"),
        ("IdInterno de la entrada +1 (la busqueda sigue con 0)", "calculoEntrada.IdInterno = i;", "calculoEntrada.IdInterno = i + 1;"),
    ],
}


def sin_procedencia(d):
    d = dict(d)
    d.pop("_provenance", None)
    return d


def main():
    only = sys.argv[1] if len(sys.argv) > 1 else None
    filtro = sys.argv[2] if len(sys.argv) > 2 else None
    total = rojas = 0
    for k, (clase, prefijo) in CLASES.items():
        if only and only != k:
            continue
        original = (LEG / (clase + ".java")).read_text(encoding="utf-8", errors="replace")
        for nombre, viejo, nuevo in MUTACIONES[k]:
            if filtro and filtro not in nombre:
                continue
            if viejo not in original:
                print("%s %s: NOT APPLICABLE (text not found)" % (k, nombre))
                continue
            with tempfile.TemporaryDirectory() as tmp:
                tmp = pathlib.Path(tmp)
                (tmp / "overlay").mkdir()
                (tmp / "overlay" / (clase + ".java")).write_text(original.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
                env = dict(__import__("os").environ, LEGACY_OVERLAY=str(tmp / "overlay"), ORACLE_OUT=str(tmp / "out"), ORACLE_REPEAT="1",
                           ORACLE_REPORT=str(tmp / "rep.txt"), ORACLE_WORK=str(tmp / "work"), PYTHONUTF8="1")
                (tmp / "work").mkdir()
                r = subprocess.run([BASH, str(BASE / "legacy-oracle" / ("record_%s.sh" % k)), prefijo], env=env, capture_output=True, text=True)
                if not (tmp / "out").exists():
                    print("%s %s: ORACLE FAILED (mutant does not compile?)\n%s" % (k, nombre, r.stdout[-400:] + r.stderr[-400:]))
                    continue
                cambiados = 0
                for g in sorted((BASE / ("cases-%s" % k)).glob(prefijo + "-*")):
                    nuevo_dump = tmp / "out" / (g.name + ".json")
                    if not nuevo_dump.exists():
                        cambiados += 1
                        continue
                    a = sin_procedencia(json.loads((g / "expected.json").read_text(encoding="utf-8")))
                    b = sin_procedencia(json.loads(nuevo_dump.read_text(encoding="utf-8")))
                    if a != b:
                        cambiados += 1
                total += 1
                rojas += 1 if cambiados else 0
                print("%s %-48s: %d golden(s) changed" % (k, nombre, cambiados), flush=True)
    print("mutations that change at least one golden: %d of %d" % (rojas, total))


if __name__ == "__main__":
    main()
