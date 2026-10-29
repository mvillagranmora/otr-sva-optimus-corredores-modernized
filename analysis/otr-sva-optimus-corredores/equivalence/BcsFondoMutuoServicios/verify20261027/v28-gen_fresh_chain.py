"""verify 2026-10-28 (v28): FRESH inputs for the CHAIN CPF + FFMM (both legacy classes real), seed 20261028. Writes ONLY under verify20261027/fresh/cpfchain/cases/VF-CHAIN-nn/{case.json,seed.sql}
(same shape as the development cases: schema-cpf.sql, seed/base-cpf.sql, schema-cpfchain.sql, seed/base-cpfchain.sql through "esquemasExtra"). Re-uses (read only) the helpers of cases_cpfchain_lib /
cases_cpf_a; never touches cases-cpfchain/ nor a golden. Random values (random.Random(20261028)) so that no output repeats a development output. Run: python -I v28-gen_fresh_chain.py
"""
import json
import pathlib
import random
import shutil
import sys
from decimal import Decimal

sys.dont_write_bytecode = True
HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
import cases_cpfchain_lib as L  # noqa: E402
from cases_cpfchain_lib import (cartera, garantia, his, hgar, ins, entrada, sol, precios, paridad, config, N, D, TS, SOCT, SOCV, INSTRUMENTOS, empresa)  # noqa: E402
from cases_cpf_a import solm  # noqa: E402
import cases_ffmm_lib as F  # noqa: E402

SEED = 20261028
R = random.Random(SEED)
OUT = HERE / "fresh" / "cpfchain" / "cases"
_n = {"n": 0}
CERR = dict(EGRFechaLiquidacion=D("2026-02-20"), EGRFechaOperacion=D("2026-02-18"))
RESP1 = TS("2026-03-01 00:00:00")      # one day before the system date (2026-03-02)


def P(inst, **kw):
    emi, nemo = INSTRUMENTOS[inst]
    d = dict(IdInstrumento=inst, Nemotecnico=nemo, IdEmisor=emi)
    d.update(kw)
    return d


def q(lo=1, hi=900000, dec=None):
    d = R.randint(0, 4) if dec is None else dec
    return N(str((Decimal(R.randint(lo * 10 ** d, hi * 10 ** d)) / Decimal(10 ** d)).quantize(Decimal(1).scaleb(-d)) if d else Decimal(R.randint(lo, hi))))


def pos(i, inst=900, **kw):
    d = dict(INGCantidad=q(1, 5000), INGMontoCompraActualMonedaInstrumento=q(1000, 90000000, 2), INGMontoATasaCompraActual=q(1000, 90000000, 2), PrecioCompra=q(900, 40000, 4))
    d.update(P(inst))
    d.update(kw)
    return cartera(i, **d)


def H(i, inst=900, **kw):
    d = dict(INGCantidad=q(1, 5000), INGMontoCompraActualMonedaInstrumento=q(1000, 90000000, 2), INGMontoATasaCompraActual=q(1000, 90000000, 2), PrecioCompra=q(900, 40000, 4), FechaRespaldo=RESP1)
    d.update(P(inst))
    d.update(kw)
    return his(i, **d)


def caso(slug, ref, ent, filas=(), seed=(), colaboradores=None):
    datos = []
    for x in list(seed) + list(filas):
        datos += x if isinstance(x, (list, tuple)) else [x]
    _n["n"] += 1
    cid = "VF-CHAIN-%02d" % _n["n"]
    doc = {"servicio": L.SERVICIO, "ref": "%s: %s" % (slug, ref), "esquemasExtra": L.ESQUEMAS, "entrada": ent, "tablas": L.TABLAS}
    col = dict(colaboradores or {})
    for k, v in (("EntregarPreciosTasas", precios()), ("CalcularParidad", paridad(final=1)), ("ConsultarEmpresa", empresa())):
        if k not in col:
            col.update(v)
    doc["colaboradores"] = col
    d = OUT / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text("-- fresh case (verify 2026-10-28, seed %d) on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql + schema-cpfchain.sql + seed/base-cpfchain.sql (fake data)\n" % SEED
                                + ("\n".join(datos) if datos else "SELECT 1;") + "\n", encoding="utf-8", newline="\n")
    return cid


def pr(**kw):
    return round(R.uniform(900, 3900), 4)


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)

    # 01 custodia + garantia + segundo instrumento, precio aleatorio
    caso("custodia-garantia-precio-aleatorio", "custodia de dos instrumentos con cantidades / PPP aleatorios, garantia sobre uno, precio de cuota aleatorio", entrada(),
         seed=[pos(1, 900), garantia(origen=1, **P(900, INGCantidad=q(1, 90, 2))), pos(2, 901)], colaboradores=precios(precio=pr()))

    # 02 historica un dia antes del sistema, dos versiones de folio y una garantia historica
    caso("historica-un-dia-antes", "p_FechaConsulta 2026-03-01 (un dia antes del sistema): respaldo con folio 51 en dos versiones, otro folio y una garantia historica", entrada(p_FechaConsulta="2026-03-01"),
         seed=[H(21, 900, FolioCartera=51), H(22, 900, FolioCartera=51), H(23, 901, FolioCartera=52), hgar(origen=23, FechaRespaldo=RESP1, **P(901, INGCantidad=q(1, 90, 2))),
               pos(24, 902, FolioCartera=53)], colaboradores=precios(precio=pr()))

    # 03 fecha explicita igual a la del sistema con limites
    caso("fecha-sistema-limites", "p_FechaConsulta 2026-03-02 (= sistema): ingreso el mismo dia (sale), egreso el mismo dia (no), egreso al dia siguiente (si), ingreso al dia siguiente (no)", entrada(p_FechaConsulta="2026-03-02"),
         seed=[pos(1, 900, INGFechaLiquidacion=D("2026-03-02")), pos(2, 901, EGRFechaLiquidacion=D("2026-03-02"), EGRFechaOperacion=D("2026-03-01")),
               pos(3, 903, EGRFechaLiquidacion=D("2026-03-03"), EGRFechaOperacion=D("2026-03-02")), pos(4, 904, INGFechaLiquidacion=D("2026-03-03"))], colaboradores=precios(precio=pr()))

    # 04 fecha por defecto 0001-01-01 de CPF
    caso("fecha-defecto-cpf", "p_FechaConsulta '0001-01-01': CPF la reemplaza por el sistema; dos instrumentos con cantidades aleatorias", entrada(p_FechaConsulta="0001-01-01"),
         seed=[pos(1, 900), pos(2, 903, IdBoveda="DCV")], colaboradores=precios(precio=pr()))

    # 05 solicitudes en transito dentro de la custodia: aporte por cuotas, rescate por cuotas, aporte por monto
    caso("transito-dentro-de-custodia", "custodia aleatoria de FM001A con un aporte (cuotas aleatorias), un rescate y un aporte por monto en transito de la misma serie", entrada(),
         [sol(1, NumeroCuotas=round(R.uniform(1, 90), 2)), sol(2, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", NumeroCuotas=round(R.uniform(1, 40), 2)), solm(3, R.randint(100000, 9000000))],
         seed=[pos(1, 900)], colaboradores=precios(precio=pr()))

    # 06 solicitud en otra serie sin custodia mas custodia de otra
    caso("transito-serie-ajena", "custodia de FM001A y solicitudes en transito de FM002B y FM004C (sin cartera): saldos de solo transito", entrada(),
         [sol(1, IdSerieFondo="FM002B", NumeroCuotas=round(R.uniform(1, 90), 2)), sol(2, IdSerieFondo="FM004C", NumeroCuotas=round(R.uniform(1, 90), 2))], seed=[pos(1, 900)],
         colaboradores=precios(precio=pr()))

    # 07 filtros cliente + portafolio
    caso("filtro-cliente-portafolio", "p_IdCliente 101 y p_IdPortafolio 502 con posiciones de varios clientes y una solicitud del cliente 101", entrada(p_IdCliente=101, p_IdPortafolio=502),
         [sol(1, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B", NumeroCuotas=round(R.uniform(1, 90), 2))],
         seed=[pos(1, 900, IdCliente=101, IdPortafolio=502), pos(2, 900), pos(3, 901, IdCliente=101, IdPortafolio=503)], colaboradores=precios(precio=pr()))

    # 08 filtros emisor + familia + instrumento
    caso("filtro-emisor-familia", "p_IdEmisor AGF2 y p_IdFamiliaFondo FAM2 con seis instrumentos y una solicitud de otra familia (la rama de solicitudes de FFMM no filtra)", entrada(p_IdEmisor="AGF2", p_IdFamiliaFondo="FAM2"),
         [sol(1, IdSerieFondo="FM001A", NumeroCuotas=round(R.uniform(1, 90), 2))], seed=[pos(i + 1, 900 + i) for i in range(6)], colaboradores=precios(precio=pr()))
    caso("filtro-instrumento-producto", "p_IdInstrumento 903 y, aparte, p_IdProductoAgrupador 3 (portafolio APV 501) en una sola peticion con posiciones de dos portafolios", entrada(p_IdInstrumento=903, p_IdProductoAgrupador=3),
         seed=[pos(1, 903, IdPortafolio=501), pos(2, 903, IdPortafolio=500), pos(3, 900, IdPortafolio=501)], colaboradores=precios(precio=pr()))

    # 09 regimenes nulos
    caso("regimenes-nulos", "IdRegimenFondoMutuo NULL en dos posiciones del mismo instrumento (un grupo) y en otro, con 18Q y garantia sobre el origen de regimen nulo", entrada(),
         seed=[pos(1, 900, IdRegimenFondoMutuo=None), pos(2, 900, IdRegimenFondoMutuo=None), pos(3, 900, IdRegimenFondoMutuo="18Q"), pos(4, 901, IdRegimenFondoMutuo=None),
               garantia(origen=4, **P(901, INGCantidad=q(1, 90, 2)))], colaboradores=precios(precio=pr()))
    caso("regimen-nulo-con-solicitud", "custodia con regimen nulo y una solicitud con IdRegimenTributario vacio (la columna es NOT NULL) de la misma serie y otra con GEN", entrada(),
         [sol(1, IdRegimenTributario="", NumeroCuotas=round(R.uniform(1, 90), 2)), sol(2, NumeroCuotas=round(R.uniform(1, 90), 2))], seed=[pos(1, 900, IdRegimenFondoMutuo=None)],
         colaboradores=precios(precio=pr()))

    # 10 dos socios de negocio
    caso("dos-socios-de-negocio", "la persona 7100 con DOS socios de negocio (tabla y vista): la custodia y la garantia del cliente 100 se duplican en FFMM; con solicitud", entrada(),
         [sol(1, NumeroCuotas=round(R.uniform(1, 90), 2))],
         seed=[F.socio(2100, 7100, 100), ins(SOCV, IdSocioDeNegocio=2100, IdPersona=7100, IdCliente=100, IdEmpresa=1), pos(1, 900), garantia(origen=1, **P(900, INGCantidad=q(1, 90, 2))), pos(2, 901)],
         colaboradores=precios(precio=pr()))
    caso("dos-socios-solo-tabla", "dos socios de negocio del cliente 101 solo en SOC.SocioDeNegocio (la vista tiene uno): posicion duplicada por el LEFT JOIN de la tabla", entrada(),
         seed=[F.socio(2101, 7101, 101), pos(1, 900, IdCliente=101, IdPortafolio=502), pos(2, 903, IdCliente=101, IdPortafolio=502, IdBoveda="DCV")], colaboradores=precios(precio=pr()))

    # 11 HashMap de seis claves y de 17 claves
    seis = [pos(i + 1, 900 + i) for i in range(6)]
    R.shuffle(seis)
    caso("orden-seis-claves-aleatorias", "seis saldos de un instrumento cada uno con cantidades aleatorias insertados en orden aleatorio (HashMap de FFMM y de CPF)", entrada(), seed=seis,
         colaboradores=dict(precios(precio=pr()), **paridad(final=1, n=16)))
    muchas = []
    i = 0
    for inst in range(900, 906):
        for port, cli in ((500, 100), (501, 100), (502, 101)):
            i += 1
            if i > 17:
                break
            muchas.append(pos(i, inst, IdPortafolio=port, IdCliente=cli, IdRegimenFondoMutuo=["GEN", "BF", None, "18Q", "57BIS"][i % 5]))
    R.shuffle(muchas)
    caso("orden-diecisiete-claves", "17 saldos (instrumento x portafolio x regimen, uno nulo): ambos HashMap se redimensionan", entrada(), seed=muchas,
         colaboradores=dict(precios(precio=pr()), **paridad(final=1, n=60)))

    # 12 PPP 0 y nulo, monto 0.0 vs 0
    caso("ppp-cero-y-nulo", "PrecioCompra 0; cantidad 0 con valor 0.0 y garantia; valor y monto 0; (PPP 0 y nulo hacia CPF)", entrada(),
         seed=[pos(1, 900, PrecioCompra=N("0")), pos(2, 901, INGCantidad=N("0"), INGMontoCompraActualMonedaInstrumento=N("0.0"), INGMontoATasaCompraActual=N("0.0"), PrecioCompra=N("0")),
               garantia(origen=2, **P(901, INGCantidad=q(1, 70, 3))), pos(3, 902, INGMontoCompraActualMonedaInstrumento=N("0"), INGMontoATasaCompraActual=N("0.00")),
               pos(4, 903, INGMontoCompraActualMonedaInstrumento=N("0.0"), INGMontoATasaCompraActual=N("0"))], colaboradores=precios(precio=pr()))
    caso("solicitud-monto-cero", "solicitudes por monto con Monto 0 y 0.0 y una por cuotas con valor de cuota 0.0; sin cartera y con cartera", entrada(),
         [solm(1, 0), solm(2, 0.0, IdSerieFondo="FM002B"), sol(3, IdSerieFondo="FM004C", NumeroCuotas=round(R.uniform(1, 90), 2), Monto=0.0, ValorCuota=0.0), solm(4, round(R.uniform(1000, 90000), 2), IdSerieFondo="FM002B")],
         seed=[pos(1, 900)], colaboradores=precios(precio=pr()))

    # 13 bovedas y corredora
    caso("bovedas-y-corredora", "posiciones en bovedas COR / DCV / EXT de un mismo instrumento (se suman) y del cliente 99 (corredora, portafolio 504)", entrada(),
         seed=[pos(1, 900, IdBoveda="COR"), pos(2, 900, IdBoveda="DCV"), pos(3, 900, IdBoveda="EXT"), pos(4, 901, IdCliente=99, IdPortafolio=504, IdBoveda="DCV")], colaboradores=precios(precio=pr()))

    # 14 historica con solicitud y emisor
    caso("historica-solicitud-emisor", "consulta 2026-02-27 con p_IdEmisor AGF2: respaldo del 02-27 y una solicitud aceptada el 02-25 (CPF la valora) y otra el 03-01", entrada(p_FechaConsulta="2026-02-27", p_IdEmisor="AGF2"),
         [sol(1, IdSerieFondo="FM002B", FechaAceptacion=F.fecha("2026-02-25"), NumeroCuotas=round(R.uniform(1, 90), 2)), sol(2, IdSerieFondo="FM005D", FechaAceptacion=F.fecha("2026-03-01"), NumeroCuotas=round(R.uniform(1, 90), 2))],
         seed=[H(1, 901, FechaRespaldo=TS("2026-02-27 00:00:00")), H(2, 904, FechaRespaldo=TS("2026-02-27 00:00:00"))], colaboradores=precios(precio=pr()))

    # 15 garantias con fechas limite
    caso("garantias-limites", "garantias con ingreso el dia del sistema (sale), egreso el dia del sistema (no), egreso al dia siguiente (si), estado LIQ (si), ANU (no)", entrada(),
         seed=[pos(1, 900), garantia(origen=1, INGFechaLiquidacion=D("2026-03-02"), **P(900, INGCantidad=q(1, 50, 2))), garantia(origen=1, EGRFechaLiquidacion=D("2026-03-02"), **P(900, INGCantidad=q(1, 50, 2))),
               garantia(origen=1, EGRFechaLiquidacion=D("2026-03-03"), **P(900, INGCantidad=q(1, 50, 2))), garantia(origen=1, IdEstado="LIQ", **P(900, INGCantidad=q(1, 50, 2))),
               garantia(origen=1, IdEstado="ANU", **P(900, INGCantidad=q(1, 50, 2)))], colaboradores=precios(precio=pr()))

    # 16 cliente numerico decimal exacto + fondo en dolares
    caso("fondo-usd-y-filtro-decimal", "p_IdCliente 100.0 (decimal exacto) con un fondo USD (instrumento 902) y otro CLP", entrada(p_IdCliente=100.0), seed=[pos(1, 902), pos(2, 900)],
         colaboradores=precios(precio=pr()))

    print("fresh CHAIN inputs:", _n["n"])


if __name__ == "__main__":
    main()
