"""Step 5 FRESH inputs for CrearSolicitudAporteRescateNormal: FRESH-NOR-nn written under fresh/cases/ (case.json + seed.sql). Independent of the development
cases (none of these inputs was used by them). Only VALID records (well formed JSON, known field names and types): malformed records are left out.
The expected outputs are NOT written here: legacy-oracle/record.sh runs the UNMODIFIED legacy and the new Subject on these same directories.
  py -3 make_fresh.py
"""
import cases_nor_lib as L
from cases_nor_b import PRECIOS2  # noqa: F401
from cases_nor_lib import APORTE, RESCATE, SALDO_OK, caso, ins, param, sol, upd

SERIE, FONDO, REGIMEN = L.SERIE, L.FONDO, L.REGIMEN


def precios(n):
    """EntregarPreciosTasas double with n price rows (the legacy divides by the price of the row of each request: fewer rows than requests = division by zero)."""
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [
        {"IdRegistro": i, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": 900, "Nemotecnico": "FM001A", "Precio": 1500.1234,
         "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": "CLP"} for i in range(1, n + 1)]}}}


# The oracle's CalcularParidad double is scripted (MontoDestino 15001 for the first job), so the cash-flow check would compare 15001, not the amount of the request:
# this double answers MontoDestino = 100000 (the amount of the aporte) so that the SaldoFlujoCaja limit cases probe the real boundary (total > saldo).
PAR100K = {"CalcularParidad": {"respuestas": [
    {"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": [{"IdTrabajo": 1, "IdInterno": 1, "FactorCambioLocal": 1, "FactorOrigenDestino": 1, "MontoDestino": 100000}]}},
    {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": [{"IdTrabajo": 2, "IdInterno": 6, "MontoDestino": 38.5},
                                                                           {"IdTrabajo": 2, "IdInterno": 7, "FactorOrigenDestino": 39000, "MontoDestino": 39000}]}}],
    "defecto": {"p_out_CalculosSalida": []}}}


def saldo(valor):
    s = {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": valor}]}}}
    return dict(s, **PAR100K)


SALDO_99999 = saldo(99999)
SALDO_EXACTO = saldo(100000)
CUSTODIA = {"ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoDetallado": [], "p_out_FlujoTotalizado": [
    {"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "IdRegimenFondoMutuo": "GEN", "Cantidad": 1000, "MontoAMercadoMonedaInstrumento": 1500123}]}}}
LOCK = L.LOCK


def lock_con_monto(monto_txt, cuotas="0"):
    ident = "500_FM001A_GEN_APO_%s_%s_2026-03-02" % (monto_txt, cuotas)
    return ins(LOCK, {"IdBloqueoSolicitudAporteRescate": ident, "IdPortafolio": 500, "IdSerieFondo": "FM001A", "IdRegimenTributario": "GEN",
                      "TipoSolicitud": "APO", "Monto": 100000, "NumeroCuotas": 0, "FechaSistema": "2026-03-02"})


def generar():
    R, A = RESCATE, APORTE
    n = "FRESH-NOR-%02d_"
    caso(n % 1 + "monto-igual-al-minimo-de-la-serie", "limite: aporte = MontoMinimo exacto (100000)", [sol(A)], seed=[upd(SERIE, {"MontoMinimo": 100000})], colab=SALDO_OK)
    caso(n % 2 + "monto-minimo-menos-uno", "limite: aporte = MontoMinimo - 1", [sol(A, Monto=99999)], seed=[upd(SERIE, {"MontoMinimo": 100000})], colab=SALDO_OK)
    caso(n % 3 + "monto-igual-al-maximo-de-la-serie", "limite: aporte = MontoMaximo exacto", [sol(A)], seed=[upd(SERIE, {"MontoMaximo": 100000})], colab=SALDO_OK)
    caso(n % 4 + "monto-maximo-mas-uno", "limite: aporte = MontoMaximo + 1", [sol(A, Monto=100001)], seed=[upd(SERIE, {"MontoMaximo": 100000})], colab=SALDO_OK)
    caso(n % 5 + "rescate-igual-a-la-custodia", "limite: NumeroCuotas = custodia (1000)", [sol(R, NumeroCuotas=1000)])
    caso(n % 6 + "rescate-custodia-mas-una-cuota", "limite: NumeroCuotas = custodia + 1", [sol(R, NumeroCuotas=1001)])
    caso(n % 7 + "rescate-fraccion-de-cuota", "cuotas con decimales (10.5) en un rescate por cuotas", [sol(R, NumeroCuotas=10.5)])
    caso(n % 8 + "rescate-por-monto-minimo", "rescate por monto con Monto=1 (menos de una cuota)", [sol(R, CalculoPorMontoCuota="M", Monto=1, NumeroCuotas=0)])
    caso(n % 9 + "rescate-por-monto-enorme", "rescate por monto 999999999999 (mas que la custodia)", [sol(R, CalculoPorMontoCuota="M", Monto=999999999999, NumeroCuotas=0)])
    caso(n % 10 + "rescate-total-con-cuotas-cero", "rescate total (T) con NumeroCuotas 0", [sol(R, CalculoPorMontoCuota="T", NumeroCuotas=0)])
    caso(n % 11 + "saldo-igual-al-aporte", "limite: SaldoFlujoCaja = monto del aporte CIN manual", [sol(A)], colab=SALDO_EXACTO)
    caso(n % 12 + "saldo-un-peso-menos", "limite: SaldoFlujoCaja = monto - 1", [sol(A)], colab=SALDO_99999)
    pat = [upd(SERIE, {"PatrimonioMinimo": 100000})]
    caso(n % 13 + "patrimonio-minimo-igual-al-monto", "limite: PatrimonioMinimo = monto (origen REC no exento)", [sol(A)], seed=pat, p_IdOrigenProceso="REC", colab=SALDO_OK)
    caso(n % 14 + "patrimonio-minimo-monto-menos-uno", "limite: monto = PatrimonioMinimo - 1 (REC)", [sol(A, Monto=99999)], seed=pat, p_IdOrigenProceso="REC", colab=SALDO_OK)
    caso(n % 15 + "origen-bonif-rescate", "origen BONIF con un rescate (sin bloqueo)", [sol(R)], p_IdOrigenProceso="BONIF")
    caso(n % 16 + "origen-vacio-aporte", "origen de proceso vacio (aplica bloqueo) con aporte CIN", [sol(A)], p_IdOrigenProceso="", colab=SALDO_OK)
    caso(n % 17 + "canal-web", "canal WEB: el doble de productos solo habilita OPT", [sol(R)], p_IdTipoCanalIngreso="WEB")
    caso(n % 18 + "regimen-no-permite-aporte", "el regimen GEN no permite aportes", [sol(A)], seed=[upd(REGIMEN, {"PermiteAporte": "N"})], colab=SALDO_OK)
    caso(n % 19 + "aporte-cerrado-backoffice", "serie con aporte cerrado para backoffice y comercial", [sol(A)],
         seed=[upd(FONDO, {"EstadoAporteComercial": "C", "EstadoAporteBackoffice": "C"})], colab=SALDO_OK)
    caso(n % 20 + "fondo-con-bloqueo-de-aporte", "fondo con BloqueoAporte=S", [sol(A)], seed=[upd(FONDO, {"BloqueoAporte": "S"})], colab=SALDO_OK)
    caso(n % 21 + "bloqueo-con-escala-distinta", "D-NOR-2: bloqueo existente con id 100000 y solicitud 100000.0 (la escala cambia el id)", [sol(A, Monto=100000.0)],
         seed=[lock_con_monto("100000")], colab=SALDO_OK)
    caso(n % 22 + "bloqueo-identico", "el bloqueo identico (id 100000) ya existe", [sol(A)], seed=[lock_con_monto("100000")], colab=SALDO_OK)
    caso(n % 23 + "seis-solicitudes-orden-inusual", "6 solicitudes, IdInterno descendente, aportes y rescates mezclados",
         [sol(R, IdInterno=6, NumeroCuotas=1), sol(A, IdInterno=5, Monto=150000), sol(R, IdInterno=4, NumeroCuotas=2), sol(A, IdInterno=3, Monto=200000),
          sol(R, IdInterno=2, NumeroCuotas=3), sol(A, IdInterno=1, Monto=250000)], colab=dict(precios(6), **SALDO_OK))
    caso(n % 24 + "cuarenta-rescates", "lista grande: 40 rescates con 1..40 cuotas (ids de bloqueo distintos)", [sol(R, IdInterno=i, NumeroCuotas=i) for i in range(1, 41)])
    caso(n % 25 + "simulacion-con-una-rechazada", "p_SimularOperacion=S con 3 solicitudes (la 2da excede la custodia)",
         [sol(R, IdInterno=1), sol(R, IdInterno=2, NumeroCuotas=2000), sol(R, IdInterno=3)], p_SimularOperacion="S")
    caso(n % 26 + "simulacion-minuscula", "p_SimularOperacion en minuscula 's' (no es 'S')", [sol(R)], p_SimularOperacion="s")
    caso(n % 27 + "visado-s", "Visado=S en un rescate", [sol(R, Visado="S")])
    caso(n % 28 + "fecha-operacion-sabado", "FechaOperacion explicita un sabado (2026-02-28)", [sol(A, FechaOperacion="2026-02-28")], colab=SALDO_OK)
    caso(n % 29 + "usuario-inexistente", "p_IdUsuario 999 sin fila de usuario", [sol(R)], p_IdUsuario=999)
    caso(n % 30 + "empresa-inexistente", "p_IdEmpresa 2 (sin datos)", [sol(R)], p_IdEmpresa=2)
    caso(n % 31 + "tipo-solicitud-minuscula", "TipoSolicitud 'res' en minuscula", [sol(R, TipoSolicitud="res")])
    caso(n % 32 + "moneda-eur-no-existe", "IdMoneda EUR (sin fila en PLA.Moneda) en un rescate", [sol(R, IdMoneda="EUR", IdMonedaPago="EUR")])
    caso(n % 33 + "rescate-usd-pago-usd", "rescate en USD pagado en USD", [sol(R, IdMoneda="USD", IdMonedaPago="USD")])
    caso(n % 34 + "mlt-porcentaje-folio", "IndicadorMLT=S, Porcentaje 50 y FolioContrato 12345", [sol(R, IndicadorMLT="S", Porcentaje=50, FolioContrato=12345)])
    caso(n % 35 + "medio-pago-vacio", "IdMedioDePago vacio en un aporte", [sol(A, IdMedioDePago="")], colab=SALDO_OK)
    caso(n % 36 + "tra-en-el-medio-de-la-lista", "aportes CIN, TRA, CIN: Tesoreria necesaria (G1 en el modulo nuevo)",
         [sol(A, IdInterno=1), sol(A, IdInterno=2, IdMedioDePago="TRA", Monto=120000), sol(A, IdInterno=3, Monto=130000)], colab=dict(precios(6), **SALDO_OK))
    caso(n % 37 + "sin-bloqueo-doble-lista", "dos solicitudes identicas con IdInterno distinto (el bloqueo compara listas)",
         [sol(R, IdInterno=1), sol(R, IdInterno=2)])
    caso(n % 38 + "parametros-de-monto-total-inversion", "MontoMinimoTotalInversion=200000 y aporte de 100000", [sol(A)],
         seed=[param("MontoMinimoTotalInversion", "200000")], colab=SALDO_OK)
    grande = {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 900000000000}]}}}
    caso(n % 39 + "aporte-monto-doce-digitos", "precision: aporte CIN de 123456789012 pesos (cuotas, comisiones y pagos con muchos digitos)", [sol(A, Monto=123456789012)], colab=grande)
    caso(n % 40 + "aporte-monto-con-decimales", "precision: aporte CLP de 100000.55 (monto con decimales)", [sol(A, Monto=100000.55)], colab=SALDO_OK)
    for k, valor in ((41, 66), (42, 99000), (43, 100001)):
        caso(n % k + "saldo-%d" % valor, "limite del saldo de flujo de caja: SaldoFlujoCaja = %d para un aporte CIN manual de 100000 (paridad guionada a 100000)" % valor,
             [sol(A)], colab=saldo(valor))
