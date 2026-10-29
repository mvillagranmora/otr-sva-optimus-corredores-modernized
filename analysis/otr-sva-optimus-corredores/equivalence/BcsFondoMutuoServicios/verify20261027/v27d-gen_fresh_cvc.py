"""verify 2026-10-27 (v27d): FRESH inputs for CargarValorCuota (CVC, W2, WRITES), seed 20261027. Writes ONLY under verify20261027/fresh/cvc/cases/VF-CVC-nn/{case.json,seed.sql}
(same shape as the development cases: schema-cvc.sql + seed/base-cvc.sql are loaded through "esquemasExtra"; the real PK of FMM.ValorCuota, one row per serie, is in the schema).
Re-uses the helpers (read only) of cases_cvc_lib; never touches cases-cvc/ nor any golden. Run: python -I v27d-gen_fresh_cvc.py
"""
import datetime
import json
import pathlib
import random
import shutil
import sys

sys.dont_write_bytecode = True
HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
import cases_cvc_lib as L  # noqa: E402
from cases_cvc_lib import ABSENT, Num, entrada, vc  # noqa: E402

SEED = 20261027
R = random.Random(SEED)
OUT = HERE / "fresh" / "cvc" / "cases"
HOY = datetime.date(2026, 3, 2)
_n = {"n": 0}
_h = {"id": 0}

BASE = [("FM001A", "CLP", 4, "CL0000000001"), ("FM001B", "CLP", 2, "CL0000000002"), ("FM001C", "CLP", 6, "CL0000000002"), ("FM002A", "USD", 6, "US0000000003"),
        ("FM003A", "CLP", 0, "CL0000000004"), ("FM004A", "CLP", 8, "")]


def extra(n):
    """n extra series FX01..FXnn (fondo 10 CLP the even ones, fondo 20 USD the odd ones): (id, moneda, decimales, isin, seed lines)"""
    res = []
    for k in range(1, n + 1):
        ids = "FX%02d" % k
        usd = k % 2 == 1
        dec = [0, 2, 4, 6, 8][k % 5]
        isin = "CLX%09d" % k
        s = [L.clonar(L.SERIE, IdSerieFondo=ids, IdInstrumento=1000 + k, IdFondo="20" if usd else "10", NombreSerieFondo="Serie " + ids, CodigoSerie="X", DecimalesValorCuota=dec),
             'UPDATE "%s" SET "ISIN" = %s WHERE "IdSerieFondo" = %s;' % (L.SERIE, L.q(isin), L.q(ids))]
        res.append((ids, "USD" if usd else "CLP", dec, isin, s))
    return res


def fec(dias_atras):
    return (HOY - datetime.timedelta(days=dias_atras)).isoformat()


def hid():
    _h["id"] += 1
    return _h["id"]


def caso(ref, ent, seed=(), colaboradores=None, previas=None):
    _n["n"] += 1
    cid = "VF-CVC-%02d" % _n["n"]
    doc = {"servicio": L.SERVICIO, "ref": [ref], "esquemasExtra": ["schema-cvc.sql", "seed/base-cvc.sql"], "entrada": ent, "tablas": L.TABLAS}
    if previas:
        doc["entradasPrevias"] = list(previas)
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = OUT / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(L.dumps(doc), encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text("-- fresh case (verify 2026-10-27, seed %d) on top of seed/base-fmm.sql + schema-cvc.sql + seed/base-cvc.sql (fake data)\n" % SEED
                                + ("\n".join(seed) if seed else "SELECT 1;") + "\n", encoding="utf-8", newline="\n")
    _h["id"] = 0
    return cid


def precio(dec, escala_extra=0):
    d = min(dec + escala_extra, 6)
    v = round(R.uniform(80, 4800), d)
    return v


def lote(series, n, fechas, obs="Carga fresca", sin_repetir=True, msg="OK"):
    """n rows over the given series ((id, moneda, dec, ...) tuples) and dates (days back); each (serie, fecha) at most once when sin_repetir"""
    pares = [(s, f) for s in series for f in fechas]
    R.shuffle(pares)
    pares = pares[:n]
    return [vc(s[0], fec(f), precio(s[2], R.choice([0, 0, 1])), s[1], obs + " %d" % i, msg) for i, (s, f) in enumerate(pares)]


def seed_prev(series, dias, valores=None, ultima="S", vigente_dias=None):
    """historico rows (and optionally the vigente one) of every serie of `series` at `dias` days back"""
    out = []
    for s in series:
        v = valores.get(s[0]) if valores else None
        if v is None:
            v = round(R.uniform(80, 4800), 2)
        out.append(L.historico(hid(), s[0], fec(dias), v, ultima, moneda=s[1]))
        if vigente_dias is not None:
            out.append(L.vigente(s[0], fec(vigente_dias), v, moneda=s[1]))
    return out


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    ex = extra(16)
    ex_seed = [x for e in ex for x in e[4]]
    ser = [(s[0], s[1], s[2], s[3]) for s in BASE] + [(e[0], e[1], e[2], e[3]) for e in ex]   # 22 series
    ser_sin_6 = [s for s in ser if s[0] != "FM004A"]

    # 01 real, 40 rows: 20 series x 2 dates, history of the previous day for half of them (variation against yesterday), vigente of older days for some
    lt = lote(ser[:20], 40, [0, 1, 3, 5])
    lt.append(vc("FM004A", fec(0), 777.123456, "CLP", "ultima fila fija la fecha anterior"))
    caso("lote real de 41 filas en 20 series y 4 fechas, historia del dia anterior en la mitad", entrada(lt),
         ex_seed + seed_prev(ser[:10], 1, vigente_dias=1) + seed_prev(ser[10:14], 3, vigente_dias=3))

    # 02 simulation, 30 rows with every kind of error mixed
    filas = lote(ser[:12], 18, [0, 2, 4])
    filas += [vc("NOEXISTE%d" % i, fec(0), 10 + i) for i in range(3)]
    filas += [vc(ser[3][0], fec(0), 0), vc(ser[4][0], fec(-3), 55.5), vc(ser[5][0], fec(0), 12.5, "USD"), vc(ser[6][0], fec(1), 9, msg="ERROR"),
              vc(ser[7][0], fec(0), -4.25, ser[7][1]), vc(ser[8][0], fec(0), 31, msg=None), vc(ser[2][0], fec(2), Num("5.10"), "CLP")]
    R.shuffle(filas)
    caso("simulacion de 28 filas con todos los tipos de error mezclados", entrada(filas, sim="S"), ex_seed + seed_prev(ser[:8], 1, vigente_dias=1),
         previas=[entrada(lote(ser[:6], 6, [1]))])

    # 03 repeat a load: previa = same lote twice, final = same series with other values and a new date
    a = lote(ser[:14], 24, [0, 2])
    b = [dict(x, VALORCUOTA=round(x["VALORCUOTA"] * R.uniform(0.9, 1.1), 3)) for x in a[:12]] + lote(ser[14:18], 8, [0, 1])
    caso("repetir una carga de 24 filas dos veces y una tercera con otros valores y series nuevas", entrada(b), ex_seed + seed_prev(ser[:10], 1, vigente_dias=1),
         previas=[entrada(a), entrada(a)])

    # 04 repeat with a failed previa (rollback of its writes) followed by a good one, then simulation
    a = lote(ser[:10], 14, [0, 1])
    mala = a + [vc("NOEXISTE", fec(0), 1)]
    caso("previas: carga con una fila mala (se revierte), la buena, y una simulacion; la peticion repite la buena", entrada(a),
         ex_seed + seed_prev(ser[:10], 2, vigente_dias=2), previas=[entrada(mala), entrada(a), entrada(a, sim="S")])

    # 05 older date vs the vigente one: vigente of today for 12 series, lote of three dates per serie (older and, for some, a newer one)
    v12 = ser[:12]
    filas = []
    for s in v12:
        for d in R.sample([0, 1, 2, 4, 6, 9], 3):
            filas.append(vc(s[0], fec(d), precio(s[2]), s[1], "fecha %d" % d))
    caso("fecha atrasada contra vigente de hoy: 36 filas, tres fechas por serie, vigente de hoy en 12 series", entrada(filas),
         ex_seed + seed_prev(v12, 0, vigente_dias=0) + seed_prev(v12, 1, ultima="S"))

    caso("solo fechas atrasadas (todas anteriores a la vigente) en 8 series, sin tocar otras series", entrada([vc(s[0], fec(R.randint(2, 20)), precio(s[2]), s[1]) for s in ser[:8]]),
         ex_seed + seed_prev(ser[:14], 0, vigente_dias=0))

    # 06 duplicates in the request: pairs, triples, hour variants, a serie twice at two dates
    filas = lote(ser[:10], 14, [0, 1])
    filas += [dict(filas[0]), dict(filas[0], VALORCUOTA=3.5), dict(filas[5]), dict(filas[5], FECHACIERRE=filas[5]["FECHACIERRE"] + " 10:30:00"), dict(filas[7], MENSAJEVALIDACION="ERROR"),
              dict(filas[9]), dict(filas[9]), dict(filas[9])]
    R.shuffle(filas)
    caso("duplicados en la peticion: parejas, trio, con hora y con formato invalido, 22 filas", entrada(filas), ex_seed + seed_prev(ser[:10], 1, vigente_dias=1))

    # 07 limit values that fit the column: zero, negative, scale 9, 17, 24, large, tiny
    lim = [vc(ser[0][0], fec(0), 0), vc(ser[1][0], fec(0), -123.45), vc(ser[2][0], fec(0), Num("0.123456789")), vc(ser[3][0], fec(0), Num("1.12345678901234567"), "USD"),
           vc(ser[4][0], fec(0), Num("2.123456789012345678901234")), vc(ser[5][0], fec(0), Num("99999999999999999.99999999")), vc(ser[6][0], fec(0), Num("123456789012345678")),
           vc(ser[7][0], fec(0), Num("0.000000001"), "USD"), vc(ser[8][0], fec(0), Num("0.00000000500000001")), vc(ser[9][0], fec(0), Num("-0.000000005")),
           vc(ser[10][0], fec(0), Num("7.0000000000000000500000000000"), "USD"), vc(ser[11][0], fec(0), Num("1E+3"), "USD"), vc(ser[12][0], fec(0), Num("12.50")),
           vc(ser[13][0], fec(0), Num("0E-10"))]
    caso("valores limite que caben en la columna: cero, negativos, escala 9/17/24, grandes, minimos, exponente y empates del binder", entrada(lim), ex_seed + seed_prev(ser[:14], 1))

    # 08 value that does not fit (19 integer digits): the write fails after the earlier steps
    caso("un valor de 19 digitos enteros en medio de un lote real de 12 filas: falla la escritura", entrada(lote(ser[:11], 11, [0]) + [vc(ser[11][0], fec(0), Num("1234567890123456789"), ser[11][1])]),
         ex_seed + seed_prev(ser[:12], 1, vigente_dias=1))

    # 09 mixed currencies with p_AllFunds "N", null and "S"-less variants
    mon = ["CLP", "USD", "EUR", "clp", "", "UF"]
    filas = []
    for i, s in enumerate(ser[:18]):
        filas.append(vc(s[0], fec(0), precio(s[2]), R.choice([s[1], s[1], R.choice(mon)])))
    caso("monedas distintas con p_AllFunds N: 18 series, la mitad con moneda distinta a la de la serie", entrada(filas, todos="N"), ex_seed)
    caso("monedas distintas con p_AllFunds nulo explicito: la moneda no se valida y se escribe la de la entrada", entrada([dict(f, IDMONEDA=R.choice(["CLP", "USD", "EUR", "x"])) for f in filas], todos=None),
         ex_seed)
    caso("monedas distintas con p_AllFunds ausente y en minuscula n", entrada([dict(f, IDMONEDA=R.choice(["USD", "JPY", "CLP"])) for f in filas], todos="n"),
         ex_seed + seed_prev(ser[:6], 1, vigente_dias=1))

    # 10 ISIN mode: 18 distinct ISINs (two of them shared by two series: FM001B/FM001C), one unknown, rows carry their own FECHACALCULO
    isins = ["CL0000000001", "CL0000000002", "US0000000003", "CL0000000004"] + [e[3] for e in ex[:12]] + ["XX0000000000", "CLX000000099"]
    filas = []
    for i, ii in enumerate(isins):
        filas.append(vc(ii, fec(R.choice([0, 1, 2])), precio(2), R.choice(["CLP", "USD", "EUR"]), "isin %d" % i, calculo=fec(R.randint(0, 6))))
    caso("modo ISIN real: 18 ISIN, uno compartido por dos series, dos sin serie, monedas mezcladas, FECHACALCULO por fila", entrada(filas, todos="S"),
         ex_seed + seed_prev(ser[:8], 1, vigente_dias=1))
    caso("modo ISIN en simulacion con los mismos ISIN y otros valores", entrada([dict(f, VALORCUOTA=round(f["VALORCUOTA"] + 1.5, 2)) for f in filas], todos="S", sim="S"), ex_seed)

    # 11 ISIN repeated in the request
    filas2 = [dict(f) for f in filas[:10]] + [dict(filas[3], VALORCUOTA=1.1), dict(filas[8], FECHACIERRE=fec(3))]
    caso("modo ISIN con ISIN repetidos en la peticion (dos ISIN repetidos)", entrada(filas2, todos="S"), ex_seed)

    # 12 ISIN mode: no ISIN known / empty list / several series per ISIN with one empty-ISIN serie in the request
    caso("modo ISIN sin ninguna serie conocida (10 ISIN inexistentes)", entrada([vc("ZZ%010d" % i, fec(0), 1 + i, calculo=fec(0)) for i in range(10)], todos="S"),
         ex_seed + seed_prev(ser[:6], 1, vigente_dias=1), previas=[entrada(lote(ser[:5], 5, [1]))])
    caso("modo ISIN con lista vacia y p_IdUsuario 7", entrada([], todos="S"), ex_seed + seed_prev(ser[:5], 1, vigente_dias=1))
    caso("lista vacia en simulacion con pais 56 y una carga previa", entrada([], sim="S", p_IdPais=56), ex_seed, previas=[entrada(lote(ser[:7], 7, [0]))])
    caso("modo ISIN con ISIN vacio y mayusculas mezcladas (serie sin ISIN, FM004A)", entrada([vc("", fec(0), 5, calculo=fec(0)), vc("cl0000000001", fec(0), 6, calculo=fec(0)),
                                                                                           vc("CL0000000004", fec(1), 7, calculo=fec(1))], todos="S"), ex_seed)

    # 13 failing log
    filas = lote(ser[:15], 22, [0, 1])
    caso("log caido (RuntimeException del colaborador) en una carga real de 22 filas", entrada(filas), ex_seed + seed_prev(ser[:6], 1, vigente_dias=1), colaboradores=L.log_cae("fallo del router 502"))
    caso("log caido con texto vacio en un lote mixto OK+ERR", entrada(filas[:10] + [vc("NOEXISTE", fec(0), 1), vc(ser[0][0], fec(0), 0)]), ex_seed, colaboradores=L.log_cae(""),
         previas=[entrada(filas[:4])])
    caso("log caido en la peticion con una carga previa que escribe", entrada(filas[:12]), ex_seed, colaboradores=L.log_cae("timeout"), previas=[entrada(filas[12:18])])

    # 14 variation against yesterday: previous values 0, equal, down, up 10x; plus a non-last historico that does not count
    prev = {}
    filas = []
    for i, s in enumerate(ser[:16]):
        pv = [0, 100, 100, 200, 50, 1000][i % 6]
        prev[s[0]] = pv
        nuevo = [10, 100, 80, 200.5, 500, 1][i % 6]
        filas.append(vc(s[0], fec(0), nuevo, s[1]))
    sd = seed_prev(ser[:16], 1, valores=prev) + [L.historico(hid(), ser[0][0], fec(1), 4321, "N"), L.historico(hid(), ser[1][0], fec(1), 4321, "N")]
    caso("variacion contra el dia anterior: valores anteriores 0, iguales, mas bajos y de 10 veces, mas dos cargas no ultimas", entrada(filas), ex_seed + sd)

    # 15 p_IdPais variants (holidays must not matter, but the country goes to ObtenerDiaAnterior)
    fer = [L.ins(L.FERIADO, IdFeriado=10, Tipo="FERIADO", Fecha=fec(1), Dia=0, DescripcionDia="feriado 152", AgnoCarga=2026, IdPais=152, IdEstado="ACT"),
           L.ins(L.FERIADO, IdFeriado=11, Tipo="INHABIL", Fecha=fec(2), Dia=6, DescripcionDia="inhabil 152", AgnoCarga=2026, IdPais=152, IdEstado="ACT"),
           L.ins(L.FERIADO, IdFeriado=12, Tipo="FERIADO", Fecha=fec(1), Dia=0, DescripcionDia="feriado -1", AgnoCarga=2026, IdPais=-1, IdEstado="ACT")]
    filas = lote(ser[:8], 10, [0, 1])
    for pais in (152, 0, "56", -7):
        caso("p_IdPais %r con feriados de otros paises el dia anterior" % (pais,), entrada(filas, p_IdPais=pais), ex_seed + fer + seed_prev(ser[:8], 1) + seed_prev(ser[:8], 2, valores={}, ultima="S"))

    # 16 big mixed OK+ERR lote (50 rows), random kinds
    filas = []
    for i in range(50):
        s = R.choice(ser)
        k = R.choice(["ok", "ok", "ok", "ok", "noserie", "cero", "moneda", "futura", "formato", "adv"])
        f = fec(R.choice([0, 1, 2, 3]))
        if k == "ok":
            filas.append(vc(s[0], f, precio(s[2]), s[1]))
        elif k == "noserie":
            filas.append(vc("N%s" % s[0], f, 3))
        elif k == "cero":
            filas.append(vc(s[0], f, 0))
        elif k == "moneda":
            filas.append(vc(s[0], f, precio(s[2]), "EUR"))
        elif k == "futura":
            filas.append(vc(s[0], fec(-R.randint(1, 5)), precio(s[2]), s[1]))
        elif k == "formato":
            filas.append(vc(s[0], f, precio(s[2]), s[1], msg=R.choice(["ERROR", "KO", ""])))
        else:
            filas.append(vc(s[0], f, precio(s[2]), s[1]))
    caso("lote mixto de 50 filas con OK, serie inexistente, cero, moneda, fecha futura, formato y repeticiones al azar", entrada(filas), ex_seed + seed_prev(ser[:12], 1, vigente_dias=1))

    # 17 injected faults: CHECK of the stand-in tables violated by one row of the batch
    filas = lote(ser[:12], 12, [0])
    mal = filas[7]["VALORCUOTA"]
    caso("fallo inyectado en la tabla vigente por una fila de un lote de 12 (paso 4)", entrada(filas), ex_seed + seed_prev(ser[:12], 1, vigente_dias=1)
         + [L.falla_en(L.VC, '"ValorCuota" <> %s' % repr(mal))])
    caso("fallo inyectado en el historico por una fila del medio de un lote de 12 (paso 2)", entrada(filas), ex_seed + seed_prev(ser[:12], 1, vigente_dias=1)
         + [L.falla_en(L.VCH, '"ValorCuota" <> %s' % repr(mal) + ' OR "EsUltimaCargaDiaria" = \'N\'')])

    # 18 60 rows real, one date, every series twice? (no: 22 series x 3 dates minus) big lote
    caso("lote real de 60 filas: 22 series en tres fechas", entrada(lote(ser, 60, [0, 1, 2])), ex_seed + seed_prev(ser, 1, vigente_dias=1))

    # 19 different user/empresa shapes with a lote
    caso("empresa y usuario como texto con un lote de 10 filas y modulo en minuscula", entrada(lote(ser[:8], 10, [0, 1]), p_IdEmpresa="1", p_IdUsuario="7", p_IdModulo="fmm"), ex_seed)
    # 20 (appended last so that the earlier cases keep their random draws): repeated ISIN in the request after a previous real load that wrote rows, so that the tables differ
    caso("modo ISIN con ISIN repetidos tras una carga previa real de 6 filas (la excepcion no deshace la previa confirmada)", entrada(filas2, todos="S"), ex_seed,
         previas=[entrada(lote(ser[:6], 6, [0, 1]))])
    print("fresh CVC cases:", _n["n"])


if __name__ == "__main__":
    main()
