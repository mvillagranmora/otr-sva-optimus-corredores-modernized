"""F2 W1 front 2, plan 7 "entradas frescas": 40 random but REPRODUCIBLE (seed 20261024) FMM-RRE inputs, written AFTER the new code was finished and never
used to develop it, into fresh-rre/cases/FRESH-RRE-nn/. Both sides run them: the unmodified legacy (record_rre.sh with ORACLE_CASES / ORACLE_OUT, dumps in
fresh-rre/legacy/) and the new code (module test RreFrescasTest over the copies synced by sync_fresh_rre.py).

  py -3 -I make_fresh_rre.py
"""
import json
import pathlib
import random
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_rre_lib as L  # noqa: E402
from cases_rre_b import BATCH_FECHAS  # noqa: E402
from cases_rre_c import APV  # noqa: E402

OUT = L.BASE / "fresh-rre" / "cases"
DESTINOS = {"FM001A": ("AGF1", "CLP"), "FM002A": ("AGF2", "CLP"), "FMX01U": ("EXT1", "USD"), "FMX02U": ("EXT1", "USD"), "FMX03U": ("EXT2", "USD")}


def uno(rnd, n):
    pares = rnd.randint(1, 4)
    grupos = rnd.choice(["cero", "distintos", "uno"])
    rescates, aportes, precios, ids = [], [], {}, []
    custodia = {}
    for i in range(1, pares + 1):
        sr = rnd.choice(["FM001A", "FM001A", "FMX01U", "FM002A"])
        sa = rnd.choice([s for s in DESTINOS if s != sr] + [sr] * (1 if rnd.random() < 0.1 else 0))
        ag = 0 if grupos == "cero" else (i if grupos == "distintos" else 5)
        calc = rnd.choice(["C", "C", "M"])
        cuotas = round(rnd.uniform(0.0001, 1500), rnd.choice([0, 2, 4]))
        monto = round(rnd.uniform(1, 2000000), rnd.choice([0, 2]))
        mon_r = L.SERIES[sr][2]
        rescates.append(L.sol(L.RESCATE, IdInterno=i, IdSerieFondo=sr, IdMoneda=mon_r, CalculoPorMontoCuota=calc, NumeroCuotas=cuotas,
                              Monto=monto if calc == "M" else 0, IdAgrupador=ag, FolioContrato=rnd.choice([0, 0, 900 + i])))
        aportes.append(L.sol(L.APORTE, IdInterno=i, IdSerieFondo=sa, IdEmisor=DESTINOS[sa][0], IdMoneda=rnd.choice([DESTINOS[sa][1], mon_r]),
                             IdAgrupador=ag, Porcentaje=rnd.choice([100, 50, 33.33333333, 25]), PorcentajeReal=round(rnd.uniform(0, 100), 8),
                             IndicadorMLT=rnd.choice(["N", "N", "N", "S"])))
        precios[i] = sr
        precios[-i] = sa
        ids += [i, -i]
        ins = L.SERIES[sr][0]
        custodia[ins] = max(custodia.get(ins, 0), rnd.choice([cuotas, cuotas + 1, max(cuotas - 1, 0), 100000]))
    seed = []
    if rnd.random() < 0.3:
        seed += BATCH_FECHAS
        for r in rescates:
            r["SolicitudBatch"] = rnd.choice(["S", "N"])
    if rnd.random() < 0.25:
        seed += APV
    if rnd.random() < 0.2:
        seed.append(L.upd(L.SERIE, {"MontoMinimo": rnd.choice([1000, 100000, 5000000])}, "\"IdSerieFondo\" = '%s'" % aportes[0]["IdSerieFondo"]))
    if rnd.random() < 0.15:
        seed.append(L.upd(L.SERIE, {"PatrimonioMinimo": rnd.choice([1, 999999999])}, "\"IdSerieFondo\" = '%s'" % aportes[-1]["IdSerieFondo"]))
    if rnd.random() < 0.15:
        seed.append(L.upd(L.SERIE, {"PatrimonioAltos": "S"}, "\"IdSerieFondo\" = '%s'" % aportes[0]["IdSerieFondo"]))
    if rnd.random() < 0.2:
        seed.append(L.param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S"))
    cab = {"p_IdTipoCanalIngreso": rnd.choice(["AGE", "WEB", "EJE", None, "OWP"]), "p_SimularOperacion": rnd.choice(["N", "N", "S", None])}
    if rnd.random() < 0.3:
        cab["p_IdOrigenProceso"] = "CAR"
    col = L.colab(L.precios(precios), L.custodia(*[(k, "GEN", v) for k, v in custodia.items()]), L.paridad(ids))
    cid = "FRESH-RRE-%02d" % n
    doc = {"servicio": "CrearSolicitudRescateReinversion", "ref": "fresh input (seed 20261024)", "rescateReinversionReal": True,
           "esquemasExtra": ["schema-rre.sql", "seed/base-rre.sql"], "tablas": L.TABLAS, "colaboradores": col,
           "entrada": dict({"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_UsaReglasNegocio": "S", "p_SolicitudesRescate": rescates,
                            "p_SolicitudesAporte": aportes}, **cab)}
    if rnd.random() < 0.25:   # the same case through the legacy Manejador
        e = doc["entrada"]
        doc["servicio"] = "ManejadorSolicitudAporteRescate"
        doc["entrada"] = {k: v for k, v in e.items() if k not in ("p_SolicitudesRescate", "p_SolicitudesAporte", "p_IdOrigenProceso", "p_SimularOperacion")}
        doc["entrada"]["p_SolicitudRescateReinversionRescate"] = rescates
        doc["entrada"]["p_SolicitudRescateReinversionAporte"] = aportes
    d = OUT / cid
    d.mkdir(parents=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text("-- fresh seed\n" + ("\n".join(seed) if seed else "SELECT 1;") + "\n", encoding="utf-8", newline="\n")


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    rnd = random.Random(20261024)
    for n in range(1, 41):
        uno(rnd, n)
    print("FRESH-RRE inputs:", 40)


if __name__ == "__main__":
    main()
