"""verify 2026-10-29 (v29): FRESH inputs VF-NOR-nn for CrearSolicitudAporteRescateNormal, seed 20261029 (random.Random: reproducible), written ONLY under
verify20261027/fresh/nor/cases/VF-NOR-nn/{case.json,seed.sql} (same shape as the development cases). They reuse the helper module cases_nor_lib (read only: its CASES global is
re-pointed to the verifier's directory) and the same schema and base seed. The values (amounts, quantities, balances) are drawn from the seed so that none repeats a development input.
Expected outputs are NOT written here: the unmodified legacy and the new code run these directories (v29-run_oracle_nor.sh, v29-Volcar29.java).

  python -I v29-gen_fresh_nor.py
"""
import json
import pathlib
import random
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
sys.path.insert(0, str(EQ))
sys.dont_write_bytecode = True
import cases_nor_lib as L  # noqa: E402
from cases_nor_lib import APORTE, RESCATE, SERIE, FONDO, REGIMEN, ins, param, sol, upd  # noqa: E402

OUT = HERE / "fresh" / "nor" / "cases"
L.CASES = OUT
RNG = random.Random(20261029)

PRECIO = {"IdRegistro": 1, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": 900, "Nemotecnico": "FM001A", "Precio": 1500.1234,
          "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": "CLP"}
USUARIO = '"BSCL.Plataforma.BD::ACC.Usuario"'
ROLES = '"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"'
CONF = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"'
TRASPASO = '"BSCL.APV.BD::TRA.Traspaso"'


def precios(n):
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [dict(PRECIO, IdRegistro=i) for i in range(1, n + 1)]}}}


def saldo(v):
    return {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": v}]}}}


def custodia(*filas, errores=0, mensajes=None):
    d = {"P_OUT_ERRORESCONTADOR": errores, "p_out_FlujoDetallado": [], "p_out_FlujoTotalizado": [
        {"IdCliente": c, "IdPortafolio": p, "IdInstrumento": 900, "IdRegimenFondoMutuo": r, "Cantidad": q, "MontoAMercadoMonedaInstrumento": m} for (c, p, r, q, m) in filas]}
    if mensajes:
        d["p_out_Mensajes"] = mensajes
    return {"ConsultarFlujoCustodia": {"defecto": d}}


def mezcla(*ds):
    r = {}
    for d in ds:
        r.update(d)
    return r


def usuario(i, perfil=None):
    s = [ins(USUARIO, {"IdUsuario": i, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 70})]
    if perfil:
        s.append(ins(ROLES, {"IdPerfil": perfil[0], "IdUsuario": i}))
    return s


BACKOFFICE = [ins(CONF, {"Key": "PERFIL_BACKOFFICE", "Value": "6"})] + usuario(8, (6, 8))


MOD = pathlib.Path(__file__).resolve().parents[5] / "modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/src/test/resources/fondomutuo-nor/cases"


def seed_de(caso_dev):
    """The seed.sql of a development case (read only): the Tesoreria objects (FMM-NOR-96) / the BATCH date modality (FMM-NOR-62), as ONE seed item."""
    t = (next(MOD.glob(caso_dev + "*")) / "seed.sql").read_text(encoding="utf-8")
    return [t.rstrip()]


def generar():
    R, A = RESCATE, APORTE
    n = "VF-NOR-%02d"
    k = [0]

    def c(ref, solicitudes, seed=None, colab=None, **cab):
        k[0] += 1
        L.caso(n % k[0], ref, solicitudes, seed=seed, colab=colab, **cab)
        return n % k[0]

    # --- rescates por cuotas / por monto / total
    cuotas = RNG.randint(11, 499)
    c("rescate por cuotas: %d cuotas de las 1000 de la custodia" % cuotas, [sol(R, NumeroCuotas=cuotas)])
    monto = RNG.randint(50, 900) * 1000 + RNG.randint(1, 999)
    c("rescate por monto (M): Monto %d, NumeroCuotas 0" % monto, [sol(R, CalculoPorMontoCuota="M", Monto=monto, NumeroCuotas=0)])
    c("rescate total (T) con NumeroCuotas informado 5 y Glosa", [sol(R, CalculoPorMontoCuota="T", NumeroCuotas=5, Glosa="rescate total v29")])
    liq = RNG.randint(100, 400) * 1000
    c("rescate por monto liquido (EsPorMontoLiquido=S) de %d" % liq, [sol(R, CalculoPorMontoCuota="M", EsPorMontoLiquido="S", Monto=liq, NumeroCuotas=0)])
    c("rescate con cuotas fraccionarias 12.5 y la custodia del doble en 12.5 (igual)", [sol(R, NumeroCuotas=12.5)], colab=custodia((100, 500, "GEN", 12.5, 18751)))
    # --- aportes CIN y no CIN
    ap = RNG.randint(110, 990) * 1000 + RNG.randint(1, 999)
    c("aporte CIN manual de %d con saldo suficiente" % ap, [sol(A, Monto=ap)], colab=saldo(RNG.randint(60, 90) * 1000000))
    ap2 = RNG.randint(110, 990) * 1000
    c("aporte CIN manual de %d con saldo 20000 (la paridad del oraculo devuelve 15001)" % ap2, [sol(A, Monto=ap2)], colab=saldo(20000))
    ap3 = RNG.randint(100, 800) * 1000
    c("aporte con medio TRA (no CIN) de %d: Tesoreria (doble por defecto)" % ap3, [sol(A, IdMedioDePago="TRA", Monto=ap3)], seed=seed_de("FMM-NOR-96"))
    c("aporte con medio CHE, Tesoreria informa un error (doble) para la solicitud 1", [sol(A, IdMedioDePago="CHE", Monto=ap3 + 7)],
      seed=seed_de("FMM-NOR-96"), colab={"CrearIngresoEgreso": {"defecto": {"P_OUT_ERRORESCONTADOR": 1, "P_OUT_MENSAJES": [{"id": 1, "tipo": "ERROR", "codigo": "ERR_TES", "descripcion": "rechazo v29"}],
                                                 "P_OUT_RESULTADOPROCESO": [], "P_OUT_INGRESOEGRESOGENERADO": []}}})
    c("aporte con medio EFE en simulacion (p_SimularOperacion=S): Tesoreria no se llama", [sol(A, IdMedioDePago="EFE", Monto=ap3 + 11)], p_SimularOperacion="S")
    # --- USD / Spot (compuerta G2 cerrada en el modulo nuevo)
    usd = RNG.randint(50, 900)
    c("aporte en USD (%d) con pago CLP y serie diferida: GeneraSpot y una forma de pago Spot" % usd, [sol(A, IdMoneda="USD", Monto=usd)],
      seed=[upd(SERIE, {"Diferido": "S"})], colab=saldo(60000000),
      p_FormaPagoSpot=[{"IdInternoPagoOperacion": 1, "IdComprobantePagoOperacion": 1, "IdTipoMovimiento": "PAGO", "IdFormaDePago": "TRA", "IdMoneda": "CLP",
                        "IdMonedaLiquidacion": "CLP", "IdOperacion": 1, "RecibePagaPortafolio": "R", "MontoFormaDePago": usd * 900, "AfectarTesoreria": "S"}])
    c("rescate en USD pagado en CLP con RESCATE_GENERA_VENTA_SPOT_DOLAR=S y 33 cuotas", [sol(R, IdMoneda="USD", IdMonedaPago="CLP", NumeroCuotas=33)],
      seed=[upd(L.PARAM, {"ValorParametro": "S"}, "\"NombreTipoParametro\" = 'RESCATE_GENERA_VENTA_SPOT_DOLAR'")])
    # --- traspaso APV
    c("rescate por traspaso APV: Traspaso 701 (RazonRetiro TRA) y 41 cuotas", [sol(R, NumeroCuotas=41, FolioContrato=701, EsTraspaso="S")],
      seed=[ins(TRASPASO, {"IdTraspaso": 701, "RazonRetiro": "TRA", "IdPersona": 1000})])
    c("rescate por traspaso APV con FolioContrato 999 que no existe en TRA.Traspaso", [sol(R, NumeroCuotas=41, FolioContrato=999, EsTraspaso="S")])
    # --- patrimonio minimo
    pm = RNG.randint(200, 900) * 1000000
    c("patrimonio minimo de la serie %d (aporte de 55555, origen REC no exento): custodia 1000 por el doble" % pm, [sol(A, Monto=55555)],
      seed=[upd(SERIE, {"PatrimonioMinimo": pm})], p_IdOrigenProceso="REC", colab=saldo(60000000))
    c("patrimonio minimo 1000 y el doble de custodia con 900 millones a mercado: aporte de 77777 (REC)", [sol(A, Monto=77777)], seed=[upd(SERIE, {"PatrimonioMinimo": 1000})],
      p_IdOrigenProceso="REC", colab=mezcla(saldo(60000000), custodia((100, 500, "GEN", 1000, 900000000))))
    # --- rescate mayor que la custodia / flujo de custodia
    c("rescate de 251 cuotas con la custodia (doble) en 250", [sol(R, NumeroCuotas=251)], colab=custodia((100, 500, "GEN", 250, 375031)))
    c("rescate de 5 cuotas y el doble de custodia sin ninguna fila totalizada", [sol(R, NumeroCuotas=5)], colab=custodia())
    c("rescate de 5 cuotas y el doble de custodia informa un error (P_OUT_ERRORESCONTADOR 1)", [sol(R, NumeroCuotas=5)],
      colab=custodia((100, 500, "GEN", 1000, 1500123), errores=1, mensajes=[{"id": 1, "tipo": "ERROR", "codigo": "CUS_ERR", "descripcion": "custodia v29"}]))
    c("rescate de 300 cuotas y la custodia en tres filas: regimen GEN 400, APV 600 y otro cliente 9999", [sol(R, NumeroCuotas=300)],
      colab=custodia((100, 500, "APV", 600, 900074), (100, 500, "GEN", 400, 600049), (9999, 500, "GEN", 70, 105008)))
    c("rescate de 1000 cuotas: custodia con el regimen en minuscula 'gen'", [sol(R, NumeroCuotas=1000)], colab=custodia((100, 500, "gen", 1000, 1500123)))
    # --- perfiles comercial y backoffice
    c("aporte del perfil comercial (usuario 7) con el aporte cerrado para comercial y abierto para backoffice", [sol(A, Monto=ap)],
      seed=[upd(FONDO, {"EstadoAporteComercial": "C", "EstadoAporteBackoffice": "A"})], colab=saldo(60000000))
    c("aporte del perfil backoffice (usuario 8) con el aporte cerrado para comercial y abierto para backoffice", [sol(A, Monto=ap)],
      seed=BACKOFFICE + [upd(FONDO, {"EstadoAporteComercial": "C", "EstadoAporteBackoffice": "A"})], colab=saldo(60000000), p_IdUsuario=8)
    c("rescate del perfil backoffice (usuario 8) con el rescate cerrado para backoffice y abierto para comercial", [sol(R, NumeroCuotas=22)],
      seed=BACKOFFICE + [upd(FONDO, {"EstadoRescateBackoffice": "C", "EstadoRescateComercial": "A"})], p_IdUsuario=8)
    c("rescate del perfil comercial (usuario 7) con el rescate cerrado para comercial y abierto para backoffice", [sol(R, NumeroCuotas=22)],
      seed=[upd(FONDO, {"EstadoRescateComercial": "C", "EstadoRescateBackoffice": "A"})])
    # --- batch / valutas
    c("aporte batch (SolicitudBatch=S) sin respetar valutas (RespetaValutasCargaMasiva N) con FechaOperacion explicita 2026-03-04", [sol(A, SolicitudBatch="S", Monto=333000, FechaOperacion="2026-03-04")],
      seed=seed_de("FMM-NOR-62"), colab=saldo(60000000))
    # --- usuario / empresa
    c("usuario 12 sin perfil de fondos mutuos (aporte)", [sol(A, Monto=ap)], seed=usuario(12), p_IdUsuario=12, colab=saldo(60000000))
    c("p_IdUsuario 0", [sol(R, NumeroCuotas=3)], p_IdUsuario=0)
    c("p_IdUsuario 2147483647 (maximo de un int) sin fila de usuario", [sol(R, NumeroCuotas=3)], p_IdUsuario=2147483647)
    c("p_IdEmpresa 0", [sol(R, NumeroCuotas=3)], p_IdEmpresa=0)
    c("p_IdEmpresa -1", [sol(A, Monto=ap)], p_IdEmpresa=-1, colab=saldo(60000000))
    # --- lotes de 1 a 5 solicitudes con una mala
    c("lote de 1: IdInterno 0 (aporte)", [sol(A, IdInterno=0, Monto=ap)], colab=mezcla(precios(1), saldo(60000000)))
    c("lote de 2: rescate 10 cuotas y rescate de 5000 cuotas (mayor a la custodia)", [sol(R, IdInterno=1, NumeroCuotas=10), sol(R, IdInterno=2, NumeroCuotas=5000)], colab=precios(2))
    c("lote de 3: aportes CIN de 150000 (bajo el MontoMinimo 200000), 250000 y 300000", [sol(A, IdInterno=1, Monto=150000), sol(A, IdInterno=2, Monto=250000), sol(A, IdInterno=3, Monto=300000)],
      seed=[upd(SERIE, {"MontoMinimo": 200000})], colab=mezcla(precios(3), saldo(900000000)))
    c("lote de 4: rescate, aporte, rescate con IdRegimenTributario XYZ (sin relacion), aporte",
      [sol(R, IdInterno=1, NumeroCuotas=4), sol(A, IdInterno=2, Monto=210000), sol(R, IdInterno=3, NumeroCuotas=6, IdRegimenTributario="XYZ"), sol(A, IdInterno=4, Monto=220000)],
      colab=mezcla(precios(4), saldo(900000000)))
    c("lote de 5 rescates con la 4ta de un cliente inexistente (999)", [sol(R, IdInterno=i, NumeroCuotas=i + 1) for i in (1, 2, 3)] + [sol(R, IdInterno=4, IdCliente=999, NumeroCuotas=2),
                                                                                                                                    sol(R, IdInterno=5, NumeroCuotas=7)],
      colab=precios(5))
    c("lote de 2 en simulacion: un aporte y un rescate (la 2da excede la custodia)", [sol(A, IdInterno=1, Monto=ap), sol(R, IdInterno=2, NumeroCuotas=1500)], p_SimularOperacion="S",
      colab=mezcla(precios(2), saldo(60000000)))
    # --- reloj y reglas de negocio
    c("reloj viernes 2026-03-06 18:45 con un rescate de 9 cuotas", [sol(R, NumeroCuotas=9)], reloj="2026-03-06 18:45:00")
    c("reloj sabado 2026-03-07 10:00 con un aporte CIN", [sol(A, Monto=ap)], reloj="2026-03-07 10:00:00", colab=saldo(60000000))
    c("p_UsaReglasNegocio N con un aporte CIN", [sol(A, Monto=ap)], p_UsaReglasNegocio="N", colab=saldo(60000000))
    c("lote de 3 con Tesoreria: aporte TRA, aporte CIN y aporte CHE (objetos de Tesoreria sembrados)",
      [sol(A, IdInterno=1, IdMedioDePago="TRA", Monto=ap3), sol(A, IdInterno=2, Monto=ap3 + 1000), sol(A, IdInterno=3, IdMedioDePago="CHE", Monto=ap3 + 2000)],
      seed=seed_de("FMM-NOR-96"), colab=mezcla(precios(3), saldo(900000000)))
    c("rescate batch (SolicitudBatch=S) de 17 cuotas con la modalidad BATCH sembrada", [sol(R, SolicitudBatch="S", NumeroCuotas=17)], seed=seed_de("FMM-NOR-62"))
    return k[0]


def main():
    if OUT.exists():
        shutil.rmtree(OUT)   # only the verifier's own directory fresh/nor/cases
    total = generar()
    # "reloj" is a key of case.json, not of the entrada: caso() puts every extra keyword in the entrada, so move it
    for d in sorted(OUT.iterdir()):
        p = d / "case.json"
        doc = json.loads(p.read_text(encoding="utf-8"))
        ent = doc["entrada"]
        if "reloj" in ent:
            doc["reloj"] = ent.pop("reloj")
        # field order as the development cases: servicio, ref, reloj?, entrada, tablas, colaboradores
        out = {"servicio": doc["servicio"], "ref": doc["ref"]}
        if "reloj" in doc:
            out["reloj"] = doc["reloj"]
        out["entrada"] = ent
        out["tablas"] = doc["tablas"]
        if "colaboradores" in doc:
            out["colaboradores"] = doc["colaboradores"]
        p.write_text(json.dumps(out, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print("cases:", total)


if __name__ == "__main__":
    main()
