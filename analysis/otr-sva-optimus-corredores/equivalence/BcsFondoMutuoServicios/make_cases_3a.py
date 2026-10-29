"""Generates the step-3A case INPUTS (cases3a/<case>/case.json + seed.sql) for the pure closure classes: CalcularFechaSolicitudAporteRescate (+ the three
ObtenerDia* helpers), CalcularMontoNumeroCuota, CalcularComisiones and ObtenerPerfilFondoMutuo. Deterministic. The EXPECTED values are not produced here:
legacy-oracle/record3a.sh executes the unmodified legacy classes and records cases3a/<case>/expected.json.

    py -3 -I make_cases_3a.py        then   legacy-oracle/record3a.sh   then   legacy-oracle/sync3a.py
"""
import importlib
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
MODULOS = ["cases3a_fec1", "cases3a_fec2", "cases3a_fec3", "cases3a_fec4", "cases3a_cuo1", "cases3a_cuo2", "cases3a_com1", "cases3a_com2", "cases3a_perfil", "cases3a_extra"]

if __name__ == "__main__":
    import cases3a_lib
    for nombre in MODULOS:
        try:
            m = importlib.import_module(nombre)
        except ModuleNotFoundError as e:
            if e.name != nombre:
                raise
            print("(skipped %s: not written yet)" % nombre)
            continue
        m.generar()
    n = sum(1 for d in cases3a_lib.OUT.iterdir() if (d / "case.json").exists())
    print("cases written: %d in %s" % (n, cases3a_lib.OUT))
