"""verify 2026-10-27 (v27c): FRESH inputs for ConsultarCustodiaPortafolioFondoMutuo (CPF), seed 20261027. Writes ONLY under verify20261027/fresh/cpf/cases/VF-CPF-nn/{case.json,seed.sql}.
Re-uses the helpers (read only) of cases_cpf_lib / cases_cpf_a; never touches cases-cpf/ nor any golden. Run: python v27c-gen_fresh_cpf.py
"""
import json
import pathlib
import random
import shutil
import sys

sys.dont_write_bytecode = True
HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
import cases_cpf_lib as L  # noqa: E402
import cases_cpf_a as A  # noqa: E402
from cases3a_lib import update  # noqa: E402

SEED = 20261027
R = random.Random(SEED)
OUT = HERE / "fresh" / "cpf" / "cases"
_n = {"n": 0}


def caso(ref, ent, filas=(), seed=(), colaboradores=None, **extra):
    datos = []
    for x in list(seed) + list(filas):
        datos += x if isinstance(x, (list, tuple)) else [x]
    _n["n"] += 1
    cid = "VF-CPF-%02d" % _n["n"]
    doc = {"servicio": L.SERVICIO, "ref": ref, "esquemasExtra": ["schema-cpf.sql", "seed/base-cpf.sql"], "entrada": ent, "tablas": L.TABLAS}
    doc.update(extra)
    col = dict(colaboradores or {})
    for k, v in (("ConsultarCustodiaPortafolioFFMM", L.sin_custodia()), ("EntregarPreciosTasas", L.precios()), ("CalcularParidad", L.paridad(final=1))):
        if k not in col:
            col.update(v)
    doc["colaboradores"] = col
    d = OUT / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text("-- fresh case (verify 2026-10-27, seed %d) on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql\n" % SEED + ("\n".join(datos) if datos else "SELECT 1;") + "\n",
                                encoding="utf-8", newline="\n")
    return cid


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    ent, cus, sol, rescate, solm, conj, custodias, precios, paridad = L.entrada, A.cus, L.sol, L.rescate, A.solm, L.conjunto, L.custodias, L.precios, L.paridad
    reg = lambda serie, r, estado="ACT": L.reg_serie(serie, r, estado)  # noqa: E731
    cal = L.fin_de_semana(56) + [L.feriado("2026-03-09", 56), L.feriado("2026-03-10", 56)]
    dia_s = L.config("PLA.CalculaValorCuotaDiaSiguiente", "S")

    # 1 custodia + aporte en transito por cuotas con precio y cantidades al azar
    p = round(R.uniform(900, 3900), 4); q = R.randint(40, 4000); c = round(R.uniform(1, 300), 4)
    caso("custodia %d + aporte en transito %s cuotas, precio %s (saldo valorizado con transito)" % (q, c, p), ent(), [sol(1, NumeroCuotas=c)],
         colaboradores=conj(custodias(cus(cantidad=q, ppp=round(R.uniform(900, 3900), 4))), precios(precio=p)))

    # 2 rescate en transito por monto con cuotas + aporte en otra serie sin custodia (dos saldos)
    p = round(R.uniform(900, 3900), 4)
    caso("rescate por monto con cuotas (M, 4 cuotas, monto 61234) FM001A + aporte por cuotas 12,75 en FM002B; custodia FM001A", ent(),
         [rescate(1, CalculoPorMontoCuota="M", Monto=61234, NumeroCuotas=4, MontoMonedaSistema=71111), sol(2, IdSerieFondo="FM002B", NumeroCuotas="12.75")],
         colaboradores=conj(custodias(cus(cantidad=55)), precios(precio=p)))

    # 3 saldo sin valorizar (sin precio) junto a saldo valorizado: el precio solo viene para el instrumento 900
    caso("dos saldos de custodia: FM001A (instrumento 900, con precio) y FM002B (instrumento 901) con una respuesta de precios que solo trae el instrumento 900 y solo IdRegistro 1",
         ent(), colaboradores=conj(custodias(cus(cantidad=21), cus("FM002B", cantidad=34)), {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [
             {"IdRegistro": 1, "IdClasificacionTipoInstrumento": "FM", "Precio": 1777.7777, "IdInstrumento": 900, "FechaFuente": L.FUENTE}]}}}))

    # 4 dia siguiente 'S' con FechaValorCuota sabado y dos feriados consecutivos (lunes 09 y martes 10): el siguiente habil es miercoles 11
    caso("dia siguiente 'S': FechaValorCuota sabado 2026-03-07 y feriados lunes 09 y martes 10 => miercoles 11", ent(),
         colaboradores=conj(custodias(cus(cantidad=17)), precios(fuente="2026-03-07 00:00:00.000")), seed=dia_s + cal)

    # 5 dia siguiente 'S' con fecha de consulta explicita de fin de ano (jueves 2026-12-31), feriado 2027-01-01
    caso("dia siguiente 'S' con p_FechaConsulta 2026-12-31 y feriado 2027-01-01 (viernes): salta a lunes 2027-01-04", ent(p_FechaConsulta="2026-12-31"),
         [sol(1, NumeroCuotas=9)], colaboradores=conj(custodias(cus(cantidad=8)), precios(fuente="2026-12-31 00:00:00.000")),
         seed=dia_s + L.fin_de_semana(56) + [L.feriado("2027-01-01", 56)])

    # 6 'N' con fuente de fecha futura y fondo en dolares con aporte PEN
    caso("dia siguiente 'N', fondo USD FM003U: aporte PEN 5,5 cuotas en USD y custodia 12 cuotas", ent(),
         [sol(1, IdSerieFondo="FM003U", IdMoneda="USD", Estado="PEN", NumeroCuotas="5.5")],
         colaboradores=conj(custodias(cus("FM003U", cantidad=12)), precios(precio=31.25, instrumento=902, fuente="2026-03-04 00:00:00.000"),
                            paridad(final=913.4, cuota=L.uf(2, 0.8, 39000, sistema=4500))))

    # 7 regimenes distintos: dos saldos, BuscaMLT S, regimen inactivo y activo en la misma serie
    caso("BuscaMLT 'S': FM002B (18Q INA, 18T ACT) y FM005D (57BIS ACT, 18Q ACT): el primer regimen ACT del SQL decide", ent(p_BuscaMLT="S"),
         colaboradores=custodias(cus("FM002B", cantidad=3), cus("FM005D", cantidad=4, cliente=101, portafolio=502, codigo="PF502")),
         seed=[reg("FM002B", "18Q", "INA"), reg("FM002B", "18T"), reg("FM005D", "57BIS"), reg("FM005D", "18Q")])

    # 8 regimenes distintos en la custodia (mismo instrumento): GEN / BF / 57BIS => tres saldos
    caso("tres saldos de FM001A en regimenes GEN, BF y 57BIS (misma serie, mismo cliente/portafolio): tres claves distintas", ent(p_BuscaMLT="S"),
         colaboradores=custodias(cus(cantidad=11), cus(cantidad=22, regimen="BF", rt="Beneficio Fiscal"), cus(cantidad=33, regimen="57BIS", rt="Articulo 57 bis")),
         seed=[reg("FM001A", "BF"), reg("FM001A", "57BIS")])

    # 9 orden 6 claves al azar
    series = list(A.SERIES_ID)
    seis = []
    for k in range(6):
        seis.append(cus(R.choice(series), cantidad=R.randint(1, 900), cliente=R.randint(100, 130), portafolio=R.choice([500, 501, 502, 503]), regimen=R.choice(["GEN", "BF", "18Q", "18T", "57BIS"])))
    seis = list({(f["IdCliente"], f["IdPortafolio"], f["IdInstrumento"], f["IdRegimenFondoMutuo"]): f for f in seis}.values())
    while len(seis) < 6:
        seis.append(cus(R.choice(series), cantidad=R.randint(1, 900), cliente=R.randint(140, 190), portafolio=500, regimen="GEN"))
    caso("orden con %d saldos de claves al azar (semilla %d)" % (len(seis), SEED), ent(), colaboradores=conj(custodias(*seis), paridad(final=1, n=len(seis))))

    # 10 orden 14 saldos (primera redimension) con claves de cliente grandes
    catorce = [cus(series[i % 6], cantidad=2 * i + 1, cliente=1000 + 37 * i, portafolio=500 + (i % 4), regimen="GEN") for i in range(14)]
    caso("orden con 14 saldos de clientes 1000+37i (HashMap redimensionado a 32)", ent(), colaboradores=conj(custodias(*catorce), precios(k=15), paridad(final=1, n=14)))

    # 11 orden 25 saldos (segundo umbral: 24 => redimensiona a 64) con solicitudes en transito primero
    veinticinco = [cus(series[i % 6], cantidad=3 * i + 2, cliente=200 + i // 6, portafolio=500 + (i % 3), regimen=["GEN", "BF", "18Q"][i % 3]) for i in range(25)]
    caso("orden con 25 saldos (segunda redimension del HashMap) y dos aportes por monto sin cuotas delante", ent(),
         [solm(1, 4321, IdSerieFondo="FM004C"), solm(2, 8765, IdSerieFondo="FM006E", IdCliente=101, IdPortafolio=502)],
         colaboradores=conj(custodias(*veinticinco), precios(k=26), paridad(final=1, n=26)))

    # 12 rescate total T con garantia y custodia, mas rescate normal en transito
    caso("custodia 70 con garantia 5,5 + 'T' ING + rescate en transito de 9 cuotas por cuotas: transito del rescate = 70 + 9", ent(),
         [rescate(1, CalculoPorMontoCuota="T", NumeroCuotas=3), rescate(2, NumeroCuotas=9, IdTipoMovimientoFondoMutuo="RESNORM")],
         colaboradores=conj(custodias(cus(cantidad=70, garantia=5.5)), precios(precio=1888.8888)))

    # 13 custodia con cantidad 0 y garantia > 0 y ppp / ing nulos
    caso("custodia cantidad 0 con garantia 12,25 y los precios promedio nulos", ent(), colaboradores=conj(custodias(cus(cantidad=0, garantia=12.25, ppp=None, ing_ppc=None, ing_monto=None, ing_moneda=None))))

    # 14 entrada con tipos como texto (Gson coacciona) y filtros combinados que si dejan filas
    caso("entrada con p_IdCliente '100', p_IdPortafolio '500', p_IdInstrumento '900' como texto, p_IdEmisor 'AGF1' y p_IdFamiliaFondo 'FAM1'",
         ent(p_IdCliente="100", p_IdPortafolio="500", p_IdInstrumento="900", p_IdEmisor="AGF1", p_IdFamiliaFondo="FAM1"), [sol(1, NumeroCuotas=6), sol(2, IdSerieFondo="FM002B", NumeroCuotas=6)],
         colaboradores=custodias(cus(cantidad=15)))

    # 15 solicitud automatica con el parametro en 'S' (filtro de no automaticas) y rescate total
    caso("WS_SOLICITUDES... 'S': aporte automatico (excluido) y rescate manual 'T' + aporte manual por cuotas 2,5", ent(),
         [sol(1, IngresoAutomatico="S", NumeroCuotas=40), sol(2, NumeroCuotas="2.5"), rescate(3, CalculoPorMontoCuota="T", NumeroCuotas=1)],
         colaboradores=custodias(cus(cantidad=30)), seed=L.fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S"))

    # 16 paridad con fraccion exacta en la mitad y moneda sistema con formato de 2 decimales
    caso("moneda sistema CLP con 2 decimales: 33,3333 cuotas x 1234,5678 de paridad por cuota", ent(), colaboradores=conj(custodias(cus(cantidad=33.3333)), paridad(final=1234.5678)),
         seed=[update('"BSCL.Plataforma.BD::PLA.MonedaFormato"', {"MontoDecimales": 2}, "\"IdMoneda\" = 'CLP'")])
    print("fresh CPF cases:", _n["n"])


if __name__ == "__main__":
    main()
