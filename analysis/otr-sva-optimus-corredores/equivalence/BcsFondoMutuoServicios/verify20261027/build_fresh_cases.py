"""verify 2026-10-27 (independent verifier): FRESH inputs for FSA / VDO / FMR, written to verify20261027/fresh/<front>/cases/VF-<FSA|VDO|FMR>-nn/{case.json,seed.sql}.
Same shape as the development cases (same esquemasExtra, same tables, same doubles), different values/limits/orders/combinations. Reproducible: random.Random(20261027).
Only case INPUTS are written; the legacy output is recorded by the legacy-oracle record_<x>.sh and the new output by Volcar27 (dir mode).

  set PYTHONDONTWRITEBYTECODE=1 & python build_fresh_cases.py
"""
import json
import pathlib
import random
import sys

HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
sys.dont_write_bytecode = True
import cases3a_lib as C3  # noqa: E402
import cases_fsa_lib as F  # noqa: E402
import cases_vdo_lib as V  # noqa: E402
import cases_fmr_lib as M  # noqa: E402
import cases_lect_lib as L  # noqa: E402

SEED = 20261027
C3._contador["n"] = 700   # IdFeriado of the extra holidays: clear of the ids of seed/base-fsa.sql


def escribir(front, prefijo, n, servicio, esquema, ref, ent, seed, tablas, extra=None, colaboradores=None):
    cid = "VF-%s-%02d" % (prefijo, n)
    doc = {"servicio": servicio, "ref": ref}
    doc.update(extra or {})
    doc.update({"esquemasExtra": [esquema], "entrada": ent, "tablas": tablas})
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = HERE / "fresh" / front / "cases" / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text("-- case seed on top of seed/base-fmm.sql + %s (fake data; verify 2026-10-27)\n" % esquema + ("\n".join(seed) if seed else "SELECT 1;") + "\n",
                                encoding="utf-8", newline="\n")
    return cid


# ------------------------------------------------------------------------------------------------ FSA
def fsa():
    rng = random.Random(SEED)
    n = [0]
    SER = ["FSN01", "FSN02", "FSX01", "FSX02", "FSX03", "FSD01", "FSX04", "FSX05", "FSX06", "FSX07"]

    def caso(ref, ent, seed=None, colaboradores=None):
        n[0] += 1
        escribir("fsa", "FSA", n[0], "ConsultarFechaSolicitudAporteRescate", "seed/base-fsa.sql", ref, ent, seed, F.TABLAS, {"consultarFechaReal": True}, colaboradores)

    S, ent = F.sol, F.entrada
    caso("APO comercial FSN02 emitido el viernes 2026-04-03 (fin de semana de por medio)", ent([S(11, "FSN02", "APO", emision="2026-04-03")]))
    caso("RES backoffice FSX03 emitido el ultimo dia del anio 2026-12-31", ent([S(12, "FSX03", "RES", emision="2026-12-31")], usuario=8))
    caso("RES backoffice FSX02 el 2028-02-28 (anio bisiesto: el 29 existe)", ent([S(13, "FSX02", "RES", emision="2028-02-28")], usuario=8))
    caso("reinversion de 3 RES extranjeras de 3 emisores (EXT1, EXT1, EXT2) comercial, emision 2026-03-13",
         ent([S(21, "FSX02", "RES", emision="2026-03-13", moneda="USD"), S(22, "FSX03", "RES", emision="2026-03-13", moneda="USD"), S(23, "FSX01", "RES", emision="2026-03-13", moneda="USD")]))
    caso("reinversion de 2 RES extranjeras EXT3/EXT4 (emisores distintos) backoffice", ent([S(31, "FSX05", "RES", moneda="USD"), S(32, "FSX07", "RES", moneda="USD")], usuario=8))
    caso("4 solicitudes: 2 RES extranjeras + 2 APO, monedas en mayuscula/minuscula, ids desordenados",
         ent([S(405, "FSX01", "RES", emision="2026-03-09", moneda="usd", pago="USD"), S(17, "FSX02", "APO", emision="2026-03-10", moneda="USD"),
              S(250, "FSX02", "RES", emision="2026-03-11", moneda="USD"), S(3, "FSX01", "APO", emision="2026-03-12", moneda="Usd")]))
    caso("feriado 2026-04-06 de los paises 56 y 840: RES FSX01 emitido el viernes 2026-04-03 cruza fin de semana y feriado",
         ent([S(41, "FSX01", "RES", emision="2026-04-03", moneda="USD", pago="CLP")]),
         seed=[C3.feriado("2026-04-06", 56), C3.feriado("2026-04-06", 840)])
    caso("serie FSN01 con retencion de rescate 0 y certificacion 9 dias: RES comercial",
         ent([S(51, "FSN01", "RES", emision="2026-03-18")]),
         seed=[C3.update(F.SERIE, {"DiasRetencionesRescate": 0, "DiasValutaCertificacionRescate": 9}, "\"IdSerieFondo\" = 'FSN01'")])
    caso("serie FSN02 con 15 dias de valuta en aporte, backoffice, cruza de mes (2026-03-25)",
         ent([S(61, "FSN02", "APO", emision="2026-03-25")], usuario=8),
         seed=[C3.update(F.SERIE, {"DiasValutaValorCuotaAporte": 15, "DiasValutaCertificacionAporte": 15, "DiasValutaCurseAporte": 3}, "\"IdSerieFondo\" = 'FSN02'")])
    largo = []
    for i in range(40):
        largo.append(S(rng.randint(1, 99999), rng.choice(SER), rng.choice(["APO", "RES"]), emision="2026-%02d-%02d" % (rng.randint(3, 6), rng.randint(1, 28)),
                       moneda=rng.choice(["USD", "CLP", "usd"])))
    caso("40 solicitudes aleatorias (semilla 20261027): 10 series, APO/RES, fechas de marzo a junio, monedas mezcladas", ent(largo))
    caso("lista vacia con perfil backoffice (usuario 8)", ent([], usuario=8))
    caso("usuario 10 (perfil comercial y backoffice): 2 APO extranjeras mismo emisor", ent([S(71, "FSX01", "APO", moneda="USD"), S(72, "FSX02", "APO", moneda="USD")], usuario=10))
    caso("IdInterno en el limite de Long (9223372036854775807) con RES FSN01", ent([S(9223372036854775807, "FSN01", "RES")]))
    caso("FechaEmision con espacio final '2026-03-02 ' (formato no estricto)", ent([S(81, "FSN01", "RES", emision="2026-03-02 ")]))
    caso("ConsultarEmpresa devuelve IdPais 152 (pais sin calendario sembrado): RES FSX01 y APO FSN02",
         ent([S(91, "FSX01", "RES", emision="2026-03-16"), S(92, "FSN02", "APO", emision="2026-03-16")]), colaboradores=F.empresa(IdPais=152))
    caso("2 RES: FSD01 (nacional-dolar, moneda 'usd', pago CLP) con FSN01; emision lunes 2026-06-15",
         ent([S(101, "FSD01", "RES", emision="2026-06-15", moneda="usd", pago="CLP"), S(102, "FSN01", "RES", emision="2026-06-15", moneda="CLP")]))
    caso("3 APO extranjeras con p_SimularOperacion S y p_UsaReglasNegocio N, backoffice, emision en sabado 2026-03-14",
         ent([S(111, "FSX01", "APO", emision="2026-03-14"), S(112, "FSX02", "APO", emision="2026-03-14"), S(113, "FSX03", "APO", emision="2026-03-14")],
             usuario=8, p_SimularOperacion="S", p_UsaReglasNegocio="N"))
    caso("RES FSX06 (pais fondo 840 / emisor 56) y FSX05 (56/840) con emision 1 de enero 2027 y feriado sembrado en 56",
         ent([S(121, "FSX06", "RES", emision="2027-01-01", moneda="USD"), S(122, "FSX05", "RES", emision="2027-01-01", moneda="USD")]),
         seed=[C3.feriado("2027-01-01", 56)])
    caso("p_IdModulo en minuscula 'fmm' y usuario 8 (el modulo se valida)", ent([S(131, "FSN01", "APO")], usuario=8, p_IdModulo="fmm"))
    caso("serie en minuscula 'fsn01' (comparacion de texto exacta) en una lista de 2 con FSN02", ent([S(141, "fsn01", "RES"), S(142, "FSN02", "RES")]))


# ------------------------------------------------------------------------------------------------ VDO
def vdo():
    rng = random.Random(SEED + 1)
    n = [0]
    sr, ent, par, pdol = V.sr, V.entrada, V.paridad, V.parametro_dolar
    obs = pdol("S")
    nuevas = (V.clonar_serie("V006", "91", fondo={"NombreFondo": "Fondo Dolar Seis", "Nemotecnico": "FDU06"}, serie={"NombreSerieFondo": "Serie Dolar Seis"}, tipo_cambio=(99999999.99999999, 0.00000001))
              + V.clonar_serie("V007", "92", fondo={"NombreFondo": "Fondo Dolar Siete", "Nemotecnico": "FDU07"}, serie={"NombreSerieFondo": "Serie Dolar Siete"}, tipo_cambio=(-5, -0.5))
              + V.clonar_serie("V008", "93", fondo={"NombreFondo": "Fondo Dolar Ocho", "Nemotecnico": "FDU08"}, serie={"NombreSerieFondo": "Serie Dolar Ocho"}, tipo_cambio=(870.5, 0)))

    def caso(ref, e, seed=None, colaboradores=None):
        n[0] += 1
        escribir("vdo", "VDO", n[0], "ConsultarValorDolarFondoMutuo", "seed/base-vdo.sql", ref, e, seed, V.TABLAS, None, colaboradores)

    caso("tabla: series nuevas con extremos de escala (V006 99999999.99999999 / 0.00000001) APO y RES", ent([sr("V006", "APO"), sr("V006", "RES")]), seed=nuevas)
    caso("tabla: V007 con tipos de cambio negativos y V008 con rescate 0 (aporte 870.5)", ent([sr("V007", "APO"), sr("V007", "RES"), sr("V008", "APO"), sr("V008", "RES")]), seed=nuevas)
    caso("tabla: tipos 'Apo' y 'RES ' (no coinciden con APO exacto) y 'APO' al final, orden invertido de series",
         ent([sr("V005", "Apo"), sr("V001", "RES "), sr("V001", "APO"), sr("FM001A", "APO")]))
    caso("tabla: 100 series aleatorias (semilla 20261027) entre V001, V005, FM001A y desconocidas, APO/RES",
         ent([sr(rng.choice(["V001", "V005", "FM001A", "ZZ%03d" % rng.randint(0, 99), "V002"]), rng.choice(["APO", "RES", None, ""])) for _ in range(100)]))
    caso("tabla: IdSerieFondo en minuscula 'v001' y con espacio 'V001 '", ent([sr("v001", "APO"), sr("V001 ", "APO")]))
    caso("tabla: p_IdEmpresa 0", ent([sr("V001", "APO")], empresa=0))
    caso("tabla: p_IdEmpresa como texto numerico '1'", ent([sr("V001", "RES")], empresa="1"))
    caso("tabla: p_IdEmpresa decimal 1.0", ent([sr("V005", "APO")], empresa=1.0))
    caso("tabla: p_IdEmpresa decimal 1.5 (Long)", ent([sr("V005", "APO")], empresa=1.5))
    caso("observado S, una fecha 2026-12-31, 5 series en distinto orden, paridad 0.5", ent([sr(s, rng.choice(["APO", "RES"]), "2026-12-31") for s in ("V005", "V001", "FM001A", "V006", "NOX")]),
         seed=obs + nuevas, colaboradores=par((0, 0.5)))
    caso("observado S: paridad negativa -3.75 y fecha 2027-02-29 (no existe: texto libre)", ent([sr("V001", "APO", "2027-02-29")]), seed=obs, colaboradores=par((0, -3.75)))
    caso("observado S: 4 fechas distintas (colision de IdInterno 0 en las 4 salidas)", ent([sr("V001", "APO", "2026-01-%02d" % d) for d in (5, 6, 7, 8)]), seed=obs,
         colaboradores=par((0, 1.1), (0, 2.2), (0, 3.3), (0, 4.4)))
    caso("observado S con valor ' S ' (espacios): no es S", ent([sr("V001", "APO", "2026-03-10")]), seed=pdol(" S "), colaboradores=par((0, 940.25)))
    caso("observado S, moneda del sistema EUR, 12 series de una fecha", ent([sr("V%03d" % (1 + i % 8), "APO" if i % 2 else "RES", "2026-05-05") for i in range(12)]),
         seed=obs + V.config_plataforma("PLA.MonedaSistema", "EUR") + nuevas, colaboradores=par((0, 1234.5678901234)))
    caso("observado S: el colaborador devuelve dos salidas, una con IdInterno 0 y otra 7 (se toma la 0), una fecha", ent([sr("V001", "RES", "2026-03-10"), sr("V005", "APO", "2026-03-10")]),
         seed=obs, colaboradores=par((7, 55.5), (0, 66.6)))
    caso("observado S sin ninguna serie desconocida pero con Fecha omitida en la serie (clave ausente)", ent([{"IdSerieFondo": "V001", "IdTipoSolicitud": "APO"}]), seed=obs, colaboradores=par((0, 10.0)))
    caso("tabla: usuario negativo y modulo nulo, serie V001 APO", ent([sr("V001", "APO")], usuario=-1, modulo=None))
    caso("tabla: p_Series con un elemento que trae IdTipoSolicitud numerico 1 (tipo incorrecto)", ent([{"IdSerieFondo": "V001", "IdTipoSolicitud": 1, "Fecha": "2026-03-10"}]))


# ------------------------------------------------------------------------------------------------ FMR
def fmr():
    n = [0]
    rng = random.Random(SEED + 2)
    ent = M.entrada
    ext = []

    def serie(sid, fondo, fo, se, regs, vc):
        s = L.clonar_serie(sid, fondo, fondo=fo, serie=se)
        for r, e in regs:
            s.append(L.reg_serie(sid, r, e, se.get("IdEmpresa", 1)))
        if vc:
            s.append(L.valor_cuota(sid, vc[0], vc[1]))
        return s

    # new universe (case seed): 5 series on top of base-fmr.sql
    nuevas = (serie("N101", "95", L._f("AGF2", "S", "EUR", "CAT1", nombre="Fondo Europa", nemo="FEU95"), L._s("Serie Europa", "E", fam="FAM4", dec=6, minimo=5, patrimonio=777777.777),
                    [("18Q", "ACT"), ("18T", "ACT"), ("57BIS", "ACT")], (0.0001, "2026-03-02"))
              + serie("N102", "96", L._f("AGF1", "N", "CLP", "CAT2", nombre="Fondo Nuevo", nemo="FNU96"), L._s("Serie Nueva A", "A", fam="FAM1", minimo=0, maximo=0, patrimonio=0),
                      [("GEN", "ACT"), ("NINGUNO", "INA")], (99999999.9999, "2026-01-31"))
              + serie("N103", "96", False, L._s("Serie Nueva B", "B", fam="FAM2", estado="ACT", comision="N"), [("18T", "ACT")], None)
              + serie("N104", "97", L._f("EXT1", "S", "USD", "CAT1", nombre="Fondo Ext Cuatro", nemo="FEX97"), L._s("Serie Ext Cuatro", "X", fam="FAM3", estado="INA"), [("18Q", "ACT")], (3.14159, "2026-02-28"))
              + serie("N105", "98", L._f("AGF2", "N", "CLP", "CAT1", empresa=2, nombre="Fondo Emp2", nemo="FE298"), L._s("Serie Empresa Dos", "A", empresa=2), [("18Q", "ACT"), ("GEN", "ACT")], (10, "2026-03-02")))

    def caso(ref, e, seed=None):
        n[0] += 1
        escribir("fmr", "FMR", n[0], "ConsultarFondoMutuoReinversion", "seed/base-fmr.sql", ref, e, seed, M.TABLAS)

    caso("estado '' + moneda EUR + extranjero S con serie nueva N101 (3 regimenes => 3 filas)", ent(estado="", moneda="EUR", extranjero="S"), seed=nuevas)
    caso("estado ACT + familia FAM4 (solo la serie nueva) sin filtro de regimen", ent(estado="ACT", familia="FAM4"), seed=nuevas)
    caso("regimen 57BIS + emisor AGF2 (S003 y N101)", ent(estado="ACT", regimen="57BIS", emisor="AGF2"), seed=nuevas)
    caso("origen S003 + regimen 18Q + estado '' (excluye el origen, mantiene el resto)", ent(estado="", regimen="18Q", origen="S003"), seed=nuevas)
    caso("origen N101 con todos los demas comodin (la serie origen con 3 regimenes se excluye completa)", ent(estado="", origen="N101"), seed=nuevas)
    caso("estado INA + extranjero S: N104 (inactiva) y ninguna otra", ent(estado="INA", extranjero="S"), seed=nuevas)
    caso("estado '' + empresa 2: series de empresa 2 (S011 y N105 con dos regimenes)", ent(estado="", empresa=2), seed=nuevas)
    caso("regimen GEN + origen FM001A (FM001A tiene regimen GEN: queda excluida; N102 y S009 salen)", ent(estado="ACT", regimen="GEN", origen="FM001A"), seed=nuevas)
    caso("regimen NINGUNO: N102 tiene NINGUNO en INA (no cuenta), S012 si", ent(estado="", regimen="NINGUNO"), seed=nuevas)
    caso("moneda USD + familia FAM3 + emisor EXT1 + regimen 18T (combinacion de 4 filtros)", ent(estado="ACT", moneda="USD", familia="FAM3", emisor="EXT1", regimen="18T"), seed=nuevas)
    caso("filtros con caracteres especiales: emisor \"AGF1' OR '1'='1\" y familia '%'", ent(estado="ACT", emisor="AGF1' OR '1'='1", familia="%"))
    caso("familia en minuscula 'fam1' y extranjero 'n' (no coinciden)", ent(estado="", familia="fam1", extranjero="n"), seed=nuevas)
    caso("todo nulo salvo empresa (estado, moneda, extranjero, familia, emisor, regimen, origen = null)", ent(estado=None, moneda=None, extranjero=None, familia=None, emisor=None, regimen=None, origen=None), seed=nuevas)
    caso("empresa 1 con usuario 0, modulo vacio y un campo desconocido numerico; estado ACT + moneda CLP + familia FAM2", ent(estado="ACT", moneda="CLP", familia="FAM2", usuario=0, modulo="", Extra=[1, 2]), seed=nuevas)
    caso("serie N103 (sin cuota) con regimen 18T y serie con 6 decimales: regimen 18T + estado ACT", ent(estado="ACT", regimen="18T"), seed=nuevas)
    caso("emisor AGF3 (sin fila en la vista de emisores) y extranjero 'X'", ent(estado="", emisor="AGF3", extranjero="X"), seed=nuevas)
    # random filter combinations over the fixed domain (reproducible)
    dom = {"estado": ["ACT", "INA", "", None], "moneda": ["-", "CLP", "USD", "EUR"], "extranjero": ["-", "S", "N", "X"], "familia": ["-", "FAM1", "FAM2", "FAM3", "FAM4"],
           "emisor": ["-", "AGF1", "AGF2", "EXT1", "AGF3"], "regimen": ["-", "18Q", "18T", "57BIS", "GEN", "NINGUNO"], "origen": ["-", "S001", "S004", "N101", "FM001A", "N102"]}
    for k in range(2):
        f = {c: rng.choice(v) for c, v in dom.items()}
        caso("combinacion aleatoria (semilla) %s" % json.dumps(f, ensure_ascii=False), ent(**f), seed=nuevas)


def main():
    for d in ("fsa", "vdo", "fmr"):
        base = HERE / "fresh" / d / "cases"
        if base.exists():
            for p in base.iterdir():
                if p.is_dir() and p.name.startswith("VF-"):
                    for f in p.iterdir():
                        f.unlink()
                    p.rmdir()
    fsa()
    vdo()
    fmr()
    for d in ("fsa", "vdo", "fmr"):
        print(d, len(list((HERE / "fresh" / d / "cases").iterdir())))


if __name__ == "__main__":
    main()
