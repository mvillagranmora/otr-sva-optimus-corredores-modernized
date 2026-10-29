"""verify 2026-10-28 (v28): FRESH inputs for ConsultarCustodiaPortafolioFFMM (FFMM alone), seed 20261028. Writes ONLY under verify20261027/fresh/ffmm/cases/VF-FFMM-nn/{case.json,seed.sql}
(same shape as the development cases: schema-ffmm.sql + seed/base-ffmm.sql through "esquemasExtra"). Re-uses (read only) the helpers of cases_ffmm_lib; never touches cases-ffmm/ nor a golden.
Values are drawn from random.Random(20261028) so that no output repeats a development output. Run: python -I v28-gen_fresh_ffmm.py
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
import cases_ffmm_lib as L  # noqa: E402
from cases_ffmm_lib import (cartera, garantia, his, hgar, solicitud, socio, cliente, ins, config, entrada, empresa, N, D, TS, SOCV, REL, relacion, fecha)  # noqa: E402

SEED = 20261028
R = random.Random(SEED)
OUT = HERE / "fresh" / "ffmm" / "cases"
_n = {"n": 0}
INST = L.INSTRUMENTOS
CERR = dict(EGRFechaLiquidacion=D("2026-03-02"), EGRFechaOperacion=D("2026-02-27"))


def P(inst, **kw):
    emi, nemo = INST[inst]
    d = dict(IdInstrumento=inst, Nemotecnico=nemo, IdEmisor=emi)
    d.update(kw)
    return d


def q(lo=1, hi=900000, dec=None):
    """a random Decimal literal (RawSql) with 0..4 decimals"""
    d = R.randint(0, 4) if dec is None else dec
    return N(str((Decimal(R.randint(lo * 10 ** d, hi * 10 ** d)) / Decimal(10 ** d)).quantize(Decimal(1).scaleb(-d)) if d else Decimal(R.randint(lo, hi))))


def pos(i, inst=900, **kw):
    """a custody row with random quantity, value, rate amount and price"""
    c = q(1, 5000)
    d = dict(INGCantidad=c, INGMontoCompraActualMonedaInstrumento=q(1000, 90000000, 2), INGMontoATasaCompraActual=q(1000, 90000000, 2), PrecioCompra=q(900, 40000, 4))
    d.update(P(inst))
    d.update(kw)
    return cartera(i, **d)


def caso(slug, ref, ent, seed=(), colaboradores=None):
    datos = []
    for x in seed:
        datos += x if isinstance(x, (list, tuple)) else [x]
    _n["n"] += 1
    cid = "VF-FFMM-%02d" % _n["n"]
    doc = {"servicio": L.SERVICIO, "ref": "%s: %s" % (slug, ref), "esquemasExtra": L.ESQUEMAS, "entrada": ent, "tablas": L.TABLAS}
    col = dict(colaboradores or {})
    if "ConsultarEmpresa" not in col:
        col.update(empresa())
    doc["colaboradores"] = col
    d = OUT / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text("-- fresh case (verify 2026-10-28, seed %d) on top of seed/base-fmm.sql + schema-ffmm.sql + seed/base-ffmm.sql (fake data)\n" % SEED
                                + ("\n".join(datos) if datos else "SELECT 1;") + "\n", encoding="utf-8", newline="\n")
    return cid


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    RESP9 = TS("2026-03-09 00:00:00")

    # 01 vigente con fechas limite contra la fecha del sistema (2026-03-10): ingresa hoy, egresa hoy (excluida), egresa manana, ingresa manana (aun no)
    caso("vigente-limites", "p_FechaConsulta = fecha del sistema: ingreso el mismo dia (sale), egreso el mismo dia (no), egreso al dia siguiente (sale), ingreso al dia siguiente (no)", entrada(),
         [pos(1, 900, INGFechaLiquidacion=D("2026-03-10")), pos(2, 901, EGRFechaLiquidacion=D("2026-03-10"), EGRFechaOperacion=D("2026-03-09")),
          pos(3, 902, EGRFechaLiquidacion=D("2026-03-11"), EGRFechaOperacion=D("2026-03-10")), pos(4, 903, INGFechaLiquidacion=D("2026-03-11"), INGFechaOperacion=D("2026-03-10")),
          pos(5, 900, IdCliente=101, IdPortafolio=502, INGCantidad=q(1, 99, 3))])

    # 02 historica un dia antes del sistema con varias versiones del mismo folio, un respaldo de otro dia, una garantia historica
    caso("historica-un-dia-antes", "p_FechaConsulta 2026-03-09 (un dia antes del sistema): fuente historica; folio 31 en dos versiones (gana el IdCartera mayor), un respaldo del 03-08, una garantia historica",
         entrada(p_FechaConsulta="2026-03-09"),
         [his(11, FolioCartera=31, **P(900, INGCantidad=q(1, 900))), his(12, FolioCartera=31, **P(900, INGCantidad=q(1, 900))), his(13, FolioCartera=32, **P(901, INGCantidad=q(1, 900))),
          his(14, FolioCartera=33, **P(902, INGCantidad=q(1, 900)), FechaRespaldo=TS("2026-03-08 00:00:00")), his(15, FolioCartera=34, **P(903, INGCantidad=q(1, 900), IdBoveda="DCV")),
          hgar(origen=13, **P(901, INGCantidad=q(1, 99, 2)))])

    # 03 fecha por defecto (p_FechaConsulta ausente => 0001-01-01) con posiciones vigentes "desde el origen" y una normal
    caso("fecha-defecto-desde-el-origen", "sin p_FechaConsulta ('0001-01-01' crudo): dos posiciones y una garantia vigentes desde 0001-01-01 salen; la normal (ingreso 2026-01-12) no", entrada(p_FechaConsulta="ABSENT"),
         [pos(1, 900, INGFechaLiquidacion=D("0001-01-01"), INGFechaOperacion=D("0001-01-01")), pos(2, 905, INGFechaLiquidacion=D("0001-01-01"), INGFechaOperacion=D("0001-01-01"), IdBoveda="DCV"),
          garantia(origen=2, **P(905, INGCantidad=q(1, 40, 1), INGFechaLiquidacion=D("0001-01-01"))), pos(3, 901)])

    # 04 ADQ: limites sobre fechas de operacion
    caso("adq-limites", "p_TipoFechaConsulta ADQ: ingreso por operacion el dia de la consulta / despues, egreso por operacion el mismo dia / despues", entrada(p_TipoFechaConsulta="ADQ"),
         [pos(1, 900, INGFechaOperacion=D("2026-03-10"), EGRFechaOperacion=D("2026-03-10"), EGRFechaLiquidacion=D("2026-03-12")),
          pos(2, 901, INGFechaOperacion=D("2026-03-09"), EGRFechaOperacion=D("2026-03-11"), EGRFechaLiquidacion=D("2026-03-12")),
          pos(3, 902, INGFechaOperacion=D("2026-03-11"), EGRFechaOperacion=D("2026-03-12"), EGRFechaLiquidacion=D("2026-03-13")),
          pos(4, 903, INGFechaOperacion=D("2026-01-02"), EGRFechaOperacion=D("2026-02-02"), EGRFechaLiquidacion=D("2026-02-03"))])

    # 05 tipo de fecha distinto de LIQ / ADQ (solo solicitudes) con custodia que NO la excluye
    caso("tipo-fecha-otro", "p_TipoFechaConsulta 'FEC' (ni LIQ ni ADQ): la custodia no sale por fecha; las solicitudes si, y no excluyen nada; con garantia", entrada(p_TipoFechaConsulta="FEC"),
         [pos(1, 900), garantia(origen=1, **P(900, INGCantidad=q(1, 60, 1))), solicitud(1, IdSerieFondo="FM001A", NumeroCuotas=round(R.uniform(1, 90), 2), Monto=R.randint(1000, 900000)),
          solicitud(2, IdSerieFondo="FM004C", IdCliente=101, IdPortafolio=502, Monto=R.randint(1000, 900000), ValorCuota=R.randint(900, 4000))])

    # 06 filtros por cliente + portafolio + instrumento a la vez
    caso("filtro-cliente-portafolio-instrumento", "p_IdCliente 101, p_IdPortafolio 502, p_IdInstrumento 901: solo la posicion que cumple los tres; una solicitud ajena de otro instrumento aparece igual (D-F3C-06)",
         entrada(p_IdCliente=101, p_IdPortafolio=502, p_IdInstrumento=901),
         [pos(1, 901, IdCliente=101, IdPortafolio=502), pos(2, 901, IdCliente=100, IdPortafolio=500), pos(3, 900, IdCliente=101, IdPortafolio=502), pos(4, 901, IdCliente=101, IdPortafolio=503),
          garantia(origen=1, IdCliente=101, IdPortafolio=502, **P(901, INGCantidad=q(1, 30, 2))),
          solicitud(1, IdSerieFondo="FM005D", IdCliente=101, IdPortafolio=502, Monto=R.randint(1000, 900000))])

    # 07 filtros por emisor y familia sobre seis instrumentos
    caso("filtro-emisor-familia", "p_IdEmisor AGF2 y p_IdFamiliaFondo FAM2 sobre seis instrumentos (FAM2 = 901 y 904; AGF2 = 901 y 904): una garantia de otro emisor; solicitud ajena de FM001A",
         entrada(p_IdEmisor="AGF2", p_IdFamiliaFondo="FAM2"),
         [pos(i + 1, 900 + i) for i in range(6)] + [garantia(origen=2, **P(901, INGCantidad=q(1, 30, 2))), garantia(origen=1, **dict(P(900), IdEmisor="AGF2", INGCantidad=q(1, 30, 2))),
                                                      solicitud(1, IdSerieFondo="FM001A", Monto=R.randint(1000, 90000))])

    # 08 agrupa por boveda con tres bovedas, garantia con origen en otra boveda y solicitud
    caso("agrupa-boveda-tres", "p_AgrupaPorBoveda S: bovedas COR / DCV / EXT del mismo instrumento (tres grupos), garantia con origen en DCV, solicitud (boveda '')", entrada(p_AgrupaPorBoveda="S"),
         [pos(1, 900, IdBoveda="COR"), pos(2, 900, IdBoveda="DCV"), pos(3, 900, IdBoveda="EXT"), pos(4, 901, IdBoveda="DCV"), garantia(origen=2, **P(900, INGCantidad=q(1, 70, 3))),
          solicitud(1, IdSerieFondo="FM003U", Monto=R.randint(1000, 90000))])

    # 09 agrupa por relacion: corredora (99), cliente relacionado activo (101), inactivo (102), tercero (100)
    caso("agrupa-relacion", "p_AgrupaPorRelacion S: cliente 99 (corredora), 101 (persona relacionada ACT), 102 (relacion INA) y 100 (tercero) del mismo instrumento, con garantia del 101 y solicitud del 99",
         entrada(p_AgrupaPorRelacion="S"),
         [pos(1, 900, IdCliente=99, IdPortafolio=504), pos(2, 900, IdCliente=101, IdPortafolio=502), pos(3, 900, IdCliente=102, IdPortafolio=505), pos(4, 900, IdCliente=100, IdPortafolio=500),
          garantia(origen=2, IdCliente=101, IdPortafolio=502, **P(900, INGCantidad=q(1, 70, 3))), solicitud(1, IdSerieFondo="FM002B", IdCliente=99, IdPortafolio=504, Monto=R.randint(1000, 90000))])

    # 10 regimenes nulos (IdRegimenFondoMutuo es NULLABLE en HANA)
    caso("regimenes-nulos", "CUS.Cartera.IdRegimenFondoMutuo NULL en dos posiciones del mismo instrumento (se suman en un grupo 'null') y en otro instrumento, mas 18Q y una garantia cuyo origen tiene regimen nulo",
         entrada(),
         [pos(1, 900, IdRegimenFondoMutuo=None), pos(2, 900, IdRegimenFondoMutuo=None), pos(3, 900, IdRegimenFondoMutuo="18Q"), pos(4, 901, IdRegimenFondoMutuo=None),
          garantia(origen=4, **P(901, INGCantidad=q(1, 70, 3))), pos(5, 902, IdRegimenFondoMutuo="18T", **CERR), garantia(origen=5, **P(902, INGCantidad=q(1, 70, 3)))])

    # 10b regimen nulo en el respaldo historico y solicitud sin regimen tributario
    caso("regimen-nulo-historico-y-solicitud", "respaldo 2026-03-09 con IdRegimenFondoMutuo NULL y una solicitud (consulta del dia del sistema) con IdRegimenTributario vacio (NULL no entra: la columna de la solicitud es NOT NULL) junto a la custodia vigente con regimen nulo",
         entrada(), [pos(1, 900, IdRegimenFondoMutuo=None), his(2, **P(901, INGCantidad=q(1, 900), IdRegimenFondoMutuo=None)), solicitud(1, IdSerieFondo="FM002B", IdRegimenTributario="", Monto=R.randint(1000, 90000))])

    # 11 persona con DOS socios de negocio (caso real de QA): las cantidades se duplican
    caso("dos-socios-de-negocio", "persona 7103 con DOS filas de SocioDeNegocio (tabla y vista): custodia, garantia y solicitud del cliente 103 salen duplicadas; el cliente 100 tiene un segundo socio solo en la tabla",
         entrada(),
         [cliente(103, 7103, "55555555-5", "Cliente Ficticio Cuatro"), socio(1103, 7103, 103), socio(2103, 7103, 103), ins(SOCV, IdSocioDeNegocio=1103, IdPersona=7103, IdCliente=103, IdEmpresa=1),
          ins(SOCV, IdSocioDeNegocio=2103, IdPersona=7103, IdCliente=103, IdEmpresa=1), socio(2100, 7100, 100),
          pos(1, 900, IdCliente=103, IdPortafolio=500), garantia(origen=1, IdCliente=103, IdPortafolio=500, **P(900, INGCantidad=q(1, 70, 3))), pos(2, 901, IdCliente=100),
          solicitud(1, IdSerieFondo="FM002B", IdCliente=103, IdPortafolio=500, Monto=R.randint(1000, 90000))])

    # 12 HashMap de seis claves insertadas en orden aleatorio
    seis = [pos(i + 1, 900 + i) for i in range(6)]
    R.shuffle(seis)
    caso("orden-seis-claves-aleatorias", "seis grupos (un instrumento cada uno) con cantidades aleatorias insertados en orden aleatorio: el orden de p_out_tbl es el del HashMap", entrada(), seis)

    # 13 HashMap de 17 claves: 6 instrumentos x portafolios x regimenes, con garantias solo-garantia
    muchas = []
    i = 0
    for inst in range(900, 906):
        for port, cli in ((500, 100), (501, 100), (502, 101)):
            i += 1
            if i > 15:
                break
            muchas.append(pos(i, inst, IdPortafolio=port, IdCliente=cli, IdRegimenFondoMutuo=["GEN", "BF", None, "18Q", "57BIS"][i % 5]))
    muchas.append(pos(30, 904, IdRegimenFondoMutuo="18T", **CERR))
    muchas.append(garantia(origen=30, **P(904, INGCantidad=q(1, 70, 3))))
    muchas.append(pos(31, 905, IdBoveda="DCV", IdPortafolio=505, IdCliente=102))
    R.shuffle(muchas)
    caso("orden-diecisiete-claves", "17 grupos (instrumento x portafolio x regimen, uno con regimen nulo) mas un grupo solo-garantia: el HashMap se redimensiona (>12)", entrada(), muchas)

    # 14 PPP 0 y nulo, valor 0.0 vs 0
    caso("ppp-cero-y-nulo", "PrecioCompra 0 con cantidad; cantidad 0 con valor 0.0 y garantia (PPP nulo); valor y monto 0 con cantidad (PPP 0); garantia sobre custodia con monto 0.0", entrada(),
         [pos(1, 900, PrecioCompra=N("0")), pos(2, 901, INGCantidad=N("0"), INGMontoCompraActualMonedaInstrumento=N("0.0"), INGMontoATasaCompraActual=N("0.0"), PrecioCompra=N("0")),
          garantia(origen=2, **P(901, INGCantidad=q(1, 70, 3))), pos(3, 902, INGMontoCompraActualMonedaInstrumento=N("0"), INGMontoATasaCompraActual=N("0")),
          pos(4, 903, INGMontoCompraActualMonedaInstrumento=N("0.0"), INGMontoATasaCompraActual=N("0.00")), garantia(origen=4, **P(903, INGCantidad=q(1, 70, 3)))])

    # 15 solicitudes con monto 0.0 vs 0 y valor de cuota 0
    caso("solicitud-monto-cero", "solicitudes en transito con Monto 0 / 0.0 y ValorCuota 0 / 0.0 sobre series sin cartera y con cartera: las sumas de ValorCompra / MontoCompra con escalas distintas", entrada(),
         [solicitud(1, IdSerieFondo="FM001A", Monto=0, ValorCuota=0), solicitud(2, IdSerieFondo="FM002B", Monto=0.0, ValorCuota=0.0), solicitud(3, IdSerieFondo="FM002B", Monto=round(R.randint(100, 9000) / 7, 2), ValorCuota=1000.5),
          solicitud(4, IdSerieFondo="FM004C", Monto=0.0, ValorCuota=2500), pos(1, 903)])

    # 16 historica con solicitud y emisor informado
    caso("historica-solicitud-con-emisor", "consulta 2026-03-04 (historica) con p_IdEmisor AGF2: la rama de solicitudes historica solo existe con emisor informado; solicitud aceptada el 03-01 y otra el 03-05 (posterior)",
         entrada(p_FechaConsulta="2026-03-04", p_IdEmisor="AGF2"),
         [his(1, FechaRespaldo=TS("2026-03-04 00:00:00"), **P(901, INGCantidad=q(1, 900))), his(2, FechaRespaldo=TS("2026-03-04 00:00:00"), **P(900, INGCantidad=q(1, 900))),
          solicitud(1, IdSerieFondo="FM002B", FechaAceptacion=fecha("2026-03-01"), Monto=R.randint(1000, 90000)), solicitud(2, IdSerieFondo="FM005D", FechaAceptacion=fecha("2026-03-05"), Monto=R.randint(1000, 90000))])

    # 17 garantias vigentes con fechas limite
    caso("garantias-limites", "garantias: ingreso el dia de la consulta (sale), egreso el dia de la consulta (no), egreso al dia siguiente (sale), estado LIQ (sale), estado ANU (no), de otra empresa (no)", entrada(),
         [pos(1, 900), garantia(origen=1, INGFechaLiquidacion=D("2026-03-10"), **P(900, INGCantidad=q(1, 50, 2))), garantia(origen=1, EGRFechaLiquidacion=D("2026-03-10"), **P(900, INGCantidad=q(1, 50, 2))),
          garantia(origen=1, EGRFechaLiquidacion=D("2026-03-11"), **P(900, INGCantidad=q(1, 50, 2))), garantia(origen=1, IdEstado="LIQ", **P(900, INGCantidad=q(1, 50, 2))),
          garantia(origen=1, IdEstado="ANU", **P(900, INGCantidad=q(1, 50, 2))), garantia(origen=1, IdEmpresa=2, **P(900, INGCantidad=q(1, 50, 2)))])

    # 18 fecha futura con egresos proximos
    caso("fecha-futura-egresos", "consulta 2026-04-15 (posterior al sistema: vigente): egresos el 04-15 (no), 04-16 (si), 03-20 (no) y una posicion que ingresa el 04-16 (no)", entrada(p_FechaConsulta="2026-04-15"),
         [pos(1, 900, EGRFechaLiquidacion=D("2026-04-15"), EGRFechaOperacion=D("2026-04-14")), pos(2, 901, EGRFechaLiquidacion=D("2026-04-16"), EGRFechaOperacion=D("2026-04-15")),
          pos(3, 902, EGRFechaLiquidacion=D("2026-03-20"), EGRFechaOperacion=D("2026-03-19")), pos(4, 903, INGFechaLiquidacion=D("2026-04-16")), pos(5, 904)])

    # 19 cliente como numero decimal exacto en el JSON (100.0) y p_IdPortafolio 500.0
    caso("filtro-numerico-decimal-exacto", "p_IdCliente 100.0 y p_IdPortafolio 501.0 (numeros con parte decimal cero en el JSON; Gson los acepta como long) y p_IdInstrumento 0.0", entrada(p_IdCliente=100.0, p_IdPortafolio=501.0, p_IdInstrumento=0.0),
         [pos(1, 900, IdPortafolio=501), pos(2, 901, IdPortafolio=500), pos(3, 902, IdPortafolio=501, IdCliente=101)])

    # 20 fecha del sistema cambiada por parametro y posicion con ingreso entre ambas
    caso("sistema-movido", "PLA.FechaSistema 2026-03-20 y consulta 2026-03-19 (un dia antes: historica) con respaldo 03-19, mas la misma consulta en vigente no existe", entrada(p_FechaConsulta="2026-03-19"),
         [config("PLA.FechaSistema", "2026-03-20"), his(1, FechaRespaldo=TS("2026-03-19 00:00:00"), **P(900, INGCantidad=q(1, 900))), pos(2, 901)])

    print("fresh FFMM inputs:", _n["n"])


if __name__ == "__main__":
    main()
