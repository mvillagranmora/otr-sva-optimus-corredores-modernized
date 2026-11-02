"""FMM-CSR (B): type of reinversion and its valutas, switch, quota and amount, inheritance of CostoUF (cards FM-REI-47..50, 52; defects D-SQL-1, D-F2-20, D-F2-4)."""
import cases_csr_lib as L
from cases_csr_lib import APORTE, KEY_AUSENTE, sol

USD = dict(IdMoneda="USD", Monto=1000, MontoOrigen=1000)


def generar():
    c = L.caso
    bc = L.base_colab

    def una(slug, ref, **kw):
        a = [sol(APORTE, **kw.pop("campos", {}))]
        return c(slug, ref, a, colaboradores=bc(a), **kw)

    # ---- the type of reinversion shows through the valutas of the dates (retention 2/4/serie, valuta cuota 1/serie, certification 2/serie, curse 0/serie)
    una("tipo-switch-mismo-emisor", "FM-REI-47/48/C66: origen FMX01U destino FMX02U (extranjeros, mismo emisor EXT1): SWITCH: retencion 2, valuta cuota 1, certificacion 2, curse 0",
        campos=dict(IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX02U", **USD))
    una("tipo-internacional-otro-emisor", "FM-REI-47/48/C66: origen FMX01U destino FMX03U (extranjeros, otro emisor): INTERNACIONAL: retencion 4, valuta cuota 1, certificacion 2, curse 0",
        campos=dict(IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX03U", **USD))
    una("tipo-nacional-extranjero-a-nacional", "FM-REI-47: origen extranjero (FMX01U) destino nacional en pesos (FM002A): NACIONAL: dias de la serie destino",
        campos=dict(IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FM002A"))
    una("tipo-nacional-nacional-a-extranjero", "FM-REI-47: origen nacional (FM001A) destino extranjero (FMX01U): NACIONAL (origen N gana sobre las reglas de extranjero)",
        campos=dict(IdSerieFondo="FMX01U", IdMoneda="USD", Monto=1000, MontoOrigen=1000))
    una("tipo-valutasnacusd-tercera-rama", "FM-REI-47/C65: origen extranjero en USD (EmisorFondoInternacionalDolar S) destino nacional en USD (EmisorFondoNacionalDolar S): VALUTASNACUSD: curse de la serie",
        campos=dict(IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMN01U", **USD))
    una("tipo-valutasnacusd-rama-muerta-pesos-a-dolar", "FM-REI-47/C65, D-SQL-1: origen nacional en pesos destino nacional en USD: la segunda regla VALUTASNACUSD exige EmisorFondoNacionalPesos S, que el SQL nunca da: sigue NACIONAL",
        campos=dict(IdSerieFondo="FMN01U", **USD))
    una("tipo-valutasnacusd-rama-muerta-dolar-a-pesos", "FM-REI-47/C65, D-SQL-1: origen nacional en USD destino nacional en pesos: la primera regla exige EmisorFondoNacionalPesos S del destino: sigue NACIONAL",
        campos=dict(IdSerieFondoOrigen="FMN01U", IdEmisorOrigen="AGF3", IdSerieFondo="FM002A"))
    una("tipo-nacional-dolar-a-dolar", "FM-REI-47: origen FMN01U destino FMN02U (ambos nacionales en USD): NACIONAL (origen no extranjero)",
        campos=dict(IdSerieFondoOrigen="FMN01U", IdEmisorOrigen="AGF3", IdSerieFondo="FMN02U", **USD))
    una("origen-sin-serie-npe", "FM-REI-47/67: IdSerieFondoOrigen sin serie (\"\", como el main del legacy) -> NullPointerException al clasificar", campos=dict(IdSerieFondoOrigen=""))
    una("origen-desconocido-npe", "FM-REI-47/67: IdSerieFondoOrigen inexistente -> NullPointerException", campos=dict(IdSerieFondoOrigen="NOEXISTE"))
    una("destino-sin-serie-npe", "FM-REI-47/67: IdSerieFondo inexistente -> NullPointerException al clasificar", campos=dict(IdSerieFondo="NOEXISTE"))
    mix = [sol(APORTE, IdInterno=1, IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX02U", **USD),
           sol(APORTE, IdInterno=2, IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX03U", **USD),
           sol(APORTE, IdInterno=3)]
    c("lote-mixto-de-tipos", "FM-REI-43/47/48: tres aportes (SWITCH, INTERNACIONAL, NACIONAL) en un lote: cada uno con sus valutas; folios y secuencias en un solo pedido", mix, colaboradores=bc(mix))
    # ---- switch of the input
    una("switch-s-con-fechas-de-origen", "FM-REI-48/52/C84: Switch S y fechas de origen (certificacion/liquidacion/valor cuota del rescate): EstadoLiquidacion LIQ; las fechas salen del calculo de switch",
        campos=dict(Switch="S", FechaCertificacionRescateOrigen="2026-03-04", FechaLiquidacionRescateOrigen="2026-03-04", FechaValorCuotaRescateOrigen="2026-03-03",
                    IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX02U", **USD))
    una("switch-s-sin-fechas-de-origen", "D-CSR-03 FM-REI-48/52: Switch S con las fechas de origen por defecto (0001-01-01): lo que el calculo de switch hace con ellas", campos=dict(Switch="S"))
    una("switch-minuscula", "D-CSR-03 FM-REI-48/52: Switch 's': EstadoLiquidacion LIQ (equalsIgnoreCase) y el calculo de fechas lo trata segun su propia comparacion",
        campos=dict(Switch="s", FechaCertificacionRescateOrigen="2026-03-04", FechaLiquidacionRescateOrigen="2026-03-04", FechaValorCuotaRescateOrigen="2026-03-03"))
    una("switch-n-explicito", "FM-REI-52: Switch N: EstadoLiquidacion ING", campos=dict(Switch="N"))
    una("switch-nulo-npe", "FM-REI-52/67: Switch nulo explicito -> NullPointerException (solicitud.Switch.equalsIgnoreCase)", campos=dict(Switch=None))
    una("switch-s-en-batch", "FM-REI-48: Switch S con SolicitudBatch S (rama de carga masiva): las fechas de origen y el switch no se pasan al calculo; EstadoLiquidacion igual LIQ",
        campos=dict(Switch="S", SolicitudBatch="S"))
    # ---- quota and amount (49)
    una("monto-cero", "FM-REI-49: Monto 0: cuotas 0 y montos 0; la solicitud sigue (la validacion de monto minimo del cierre decide)", campos=dict(Monto=0, MontoOrigen=0))
    una("monto-con-decimales", "FM-REI-49: Monto 123456.789 en CLP: el formato de moneda del calculo de cuotas", campos=dict(Monto=123456.789, MontoOrigen=123456.789))
    una("monto-usd-en-serie-usd", "FM-REI-49/51: aporte USD en serie USD (FMX01U): sin Spot, IdMonedaPago = moneda de la serie, IdMoneda = la del aporte",
        campos=dict(IdSerieFondoOrigen="FMX02U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX01U", **USD))
    una("numerocuotas-informado", "FM-REI-49: NumeroCuotas informado: el calculo es siempre por monto (CalculoPorMontoCuota M)", campos=dict(NumeroCuotas=77.5))
    a = [sol(APORTE)]
    c("sin-valor-cuota", "D-CSR-04 FM-REI-49, D-CUO: EntregarPreciosTasas sin precio para la serie: el calculo no trae resultado (el AR ignora los mensajes de CalcularMontoNumeroCuota): montos 0 y la solicitud sigue",
      a, colaboradores=L.colab(bc(a), {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": []}}}))
    c("precios-con-error", "D-CSR-04 FM-REI-49: EntregarPreciosTasas con P_OUT_ERRORESCONTADOR 1 y sin precios: el AR sigue igual", a,
      colaboradores=L.colab(bc(a), {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 1, "P_OUT_PRECIOSTASAS": []}}}))
    c("paridad-trabajo-1-vacia", "FM-REI-46/56: CalcularParidad (trabajo 1) sin fila para el IdInterno: ValidaMontoInversion recibe Monto 0", a,
      colaboradores=L.colab(bc(a), {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": L.paridad([1])["CalcularParidad"]["respuestas"][1]["respuesta"]["p_out_CalculosSalida"]}}],
                                                         "defecto": {"p_out_CalculosSalida": []}}}))
    c("paridad-monto-total-de-inversion", "FM-REI-46: MontoTotalInversion distinto de 0: la paridad (trabajo 1) se calcula sobre ese monto y no sobre Monto", [sol(APORTE, MontoTotalInversion=250000)],
      colaboradores=bc(a))
    c("paridad-moneda-usd", "FM-REI-46: aporte USD: CalcularParidad origen USD destino CLP (MonedaSistema)", [sol(APORTE, IdSerieFondoOrigen="FMX02U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX01U", **USD)],
      colaboradores=bc([sol(APORTE, IdSerieFondo="FMX01U")]))
    # ---- inheritance of CostoUF (50, D-F2-20): CostoUF of the input 0.5; the calculated one is 38.5 (UF amount of the double)
    for pid, cod, slug in ((500, "PF500", "portafolio-normal-calcula"), (501, "APV-A", "portafolio-apv-a-hereda"), (502, "BF", "portafolio-bf-hereda"), (503, "DC-DL", "portafolio-dc-dl-hereda"),
                           (504, "DC-AL", "portafolio-dc-al-hereda"), (505, "DC-R", "portafolio-dc-r-hereda"), (506, "apv-a", "portafolio-apv-a-minuscula-hereda")):
        una("costouf-" + slug, "FM-REI-50/C64: portafolio %s: %s el CostoUF de la entrada" % (cod, "hereda" if pid != 500 else "calcula (vc.CostoEnFactorAjuste)"), campos=dict(IdPortafolio=pid))
    una("costouf-mlt-s-todo-18q-hereda", "FM-REI-50/C64: MLT S, regimen del aporte 18Q y regimen del rescate relacionado (9002) 18Q: hereda", campos=dict(IndicadorMLT="S", IdRegimenTributario="18Q",
                                                                                                                                                     IdSolicitudAporteRescateRelacionado=9002))
    una("costouf-mlt-s-rescate-gen-calcula", "FM-REI-50/C64: MLT S, aporte 18Q pero el rescate relacionado (9001) es GEN: calcula", campos=dict(IndicadorMLT="S", IdRegimenTributario="18Q"))
    una("costouf-mlt-n-18q-calcula", "FM-REI-50/C64: MLT N con todo 18Q: calcula", campos=dict(IndicadorMLT="N", IdRegimenTributario="18Q", IdSolicitudAporteRescateRelacionado=9002))
    una("costouf-mlt-minuscula", "FM-REI-50: IndicadorMLT 's' (equalsIgnoreCase) con todo 18Q: hereda", campos=dict(IndicadorMLT="s", IdRegimenTributario="18Q", IdSolicitudAporteRescateRelacionado=9002))
    una("costouf-mlt-s-sin-rescate-hereda-vacio", "FM-REI-50, D-F2-20: MLT S, aporte 18Q, rescate relacionado 0 (sin filas): allMatch de lista vacia verdadero: hereda",
        campos=dict(IndicadorMLT="S", IdRegimenTributario="18Q", IdSolicitudAporteRescateRelacionado=0))
    mix18 = [sol(APORTE, IdInterno=1, IndicadorMLT="S", IdRegimenTributario="18Q", IdSolicitudAporteRescateRelacionado=9002), sol(APORTE, IdInterno=2, IndicadorMLT="S", IdSolicitudAporteRescateRelacionado=9002)]
    c("costouf-mlt-lote-con-regimen-distinto-calcula", "FM-REI-50: dos aportes MLT, uno 18Q y otro GEN: la lista de regimenes del LOTE no es toda 18Q: ambos calculan", mix18, colaboradores=bc(mix18))
    una("costouf-cliente-nulo-npe", "FM-REI-50/53/67: portafolio inexistente (sin cliente): NullPointerException (cliente.CodigoPortafolio / codigoPortafolio.CodigoPortafolio)", campos=dict(IdPortafolio=999))
    ap = [sol(APORTE, IdPortafolio=501)]
    c("costouf-sin-calculo-vc-nulo", "FM-REI-50: sin valor cuota (vc nulo) no se decide la herencia: CostoUF queda 0 aunque el portafolio sea APV-A", ap,
      colaboradores=L.colab(bc(ap), {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": []}}}))
