"""FM-NOR cases, part B: early exits, requirements, equity, APV, OWP, failures."""
from cases_nor_lib import *

PRECIOS2 = {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [
    {"IdRegistro": i, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": 900, "Nemotecnico": "FM001A", "Precio": 1500.1234,
     "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": "CLP"} for i in (1, 2)]}}}
REG_APV = [ins('"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"', {"IdPortafolio": 500, "IdMatrizAgrupadorAPV": 7}),
           ins('"BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"', {"IdMatrizAgrupadorAPV": 7, "IdRegimenTributario": 3}),
           ins('"BSCL.APV.BD::REC.RegimenTributario"', {"IdRegimenTributario": 3, "Codigo": "APV-A"})]


def generar():
    R, A = RESCATE, APORTE
    caso("FMM-NOR-21_lista-vacia", "FM-NOR-21: lista vacia: NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE", [])
    caso("FMM-NOR-22_id-interno-cero", "FM-NOR-22: IdInterno 0: ID_INTERNO_NO_CERO (el bloqueo ya creado se elimina)", [sol(R, IdInterno=0)])
    caso("FMM-NOR-38_id-interno-repetido", "FM-NOR-22: IdInterno repetido: CORRELATIVO_INCORRECTO", [sol(R, IdInterno=1), sol(R, IdInterno=1, NumeroCuotas=20)])
    caso("FMM-NOR-23_modulo-invalido", "FM-NOR-23: requisitos basicos: modulo inexistente", [sol(R)], p_IdModulo="ZZZ")
    caso("FMM-NOR-24_serie-bloqueada-rescate", "FM-NOR-24: serie con el rescate bloqueado", [sol(R)], seed=[upd(FONDO, {"BloqueoRescate": "S"})])
    caso("FMM-NOR-39_serie-inactiva", "FM-NOR-24: serie inactiva", [sol(R)], seed=[upd(SERIE, {"IdEstado": "INA"})])
    caso("FMM-NOR-25_monto-minimo-serie", "FM-NOR-25: monto minimo de la serie (aporte CIN manual)", [sol(A)], seed=[upd(SERIE, {"MontoMinimo": 500000})],
         colab={"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 50000000}]}}})
    caso("FMM-NOR-40_monto-maximo-serie", "FM-NOR-25: monto maximo de la serie menor al aporte", [sol(A)], seed=[upd(SERIE, {"MontoMaximo": 1})],
         colab={"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 50000000}]}}})
    caso("FMM-NOR-26_regimen-sin-relacion-serie", "FM-NOR-26: regimen sin relacion con la serie", [sol(R, IdRegimenTributario="XXX")])
    caso("FMM-NOR-41_regimen-no-permite-rescate", "FM-NOR-26: el regimen no permite rescates", [sol(R)], seed=[upd(REGIMEN, {"PermiteRescate": "N"})])
    caso("FMM-NOR-27_patrimonio-emisor", "FM-NOR-27: serie con patrimonio alto por emisor (ValidaPatrimonioEmisor, tabla de paso de la sesion)", [sol(A)],
         seed=[upd(SERIE, {"PatrimonioAltos": "S", "MontoPatrimonioEmisor": 1000})],
         colab={"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 50000000}]}}})
    caso("FMM-NOR-28_rescate-apv-sin-traspaso", "FM-NOR-28: rescate de portafolio APV sin traspaso: InscribirProcesoBatch (doble)", [sol(R)], seed=REG_APV)
    caso("FMM-NOR-29_serie-inexistente", "FM-NOR-29 D-NOR-9: serie inexistente: NullPointerException del legacy (ROLLBACK_HTTP500 y el bloqueo queda liberado)",
         [sol(R, IdSerieFondo="NOEXISTE")])
    caso("FMM-NOR-30_fallo-agregar-log", "FM-NOR-30: AgregarLog falla despues del INSERT: excepcion y rollback total", [sol(R)],
         colab={"AgregarLog": {"respuestas": [{"error": "log caido"}]}})
    caso("FMM-NOR-17_canal-owp-notifica", "FM-NOR-17: canal OWP con PLA.NotificarEstadoOrdenOWP=S: NotificarEstadoOrdenesOWP (doble); G4 cerrada => 503 (DEV-F4)",
         [sol(R)], p_IdTipoCanalIngreso="OWP", seed=[upd(PLACONF, {"Valor": "S"}, "\"CodigoParametro\" = 'PLA.NotificarEstadoOrdenOWP'")],
         colab={"NotificarEstadoOrdenesOWP": {"defecto": {}}})
    caso("FMM-NOR-42_canal-owp-sin-parametro", "FM-NOR-17: canal OWP con el parametro en N: no se notifica", [sol(R)], p_IdTipoCanalIngreso="OWP")
    caso("FMM-NOR-43_dos-aportes-cin-mismo-portafolio", "FM-NOR-04/18: dos aportes CIN del mismo portafolio: el flujo de caja se acumula por portafolio y moneda",
         [sol(A, IdInterno=1), sol(A, IdInterno=2, Monto=200000)],
         colab=dict(PRECIOS2, **{"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 250000}]}}}))
    caso("FMM-NOR-52_dos-aportes-tra-tesoreria", "FM-NOR-06/D-NOR-5: dos aportes con medio TRA: ProcesarIngresoEgresoTesoreria (G1 cerrada => 503 en el modulo nuevo; con la compuerta abierta el test fija D-NOR-5)",
         [sol(A, IdInterno=1, IdMedioDePago="TRA"), sol(A, IdInterno=2, IdMedioDePago="TRA", Monto=200000)], colab=dict(PRECIOS2))
    caso("FMM-NOR-44_origen-vacio-saawv", "FM-NOR-10 D-NOR-4: aporte con TipoContratoOperacion SAAPV: la comparacion de referencia no lo exime por JSON",
         [sol(A, TipoContratoOperacion="SAAPV")], seed=[upd(SERIE, {"PatrimonioMinimo": 5000000})], p_IdOrigenProceso="REC")


def estado_define_fechas():
    """FM-NOR-08/45: the state of the fund for the profile of the user (EstadoFondo) selects the date configuration row: the certification date shows it."""
    cfg = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"'
    fila = ins(cfg, {"IdEmpresa": 1, "IdModalidad": "UNITARIO", "IdTipoFecha": "CERTIF", "EstadoFondo": "C", "Descripcion": "CERTIF", "CantidadDias": 3, "EsHabil": "S",
                     "IdTipoFechaOrigen": "CURSE", "DebeSerHabilFechaOrigen": "N", "ValutaCurse": "N", "ValutaValorCuota": "N", "ValutaCertificacion": "N",
                     "ValutaRetencion": "N", "CalendarioLocal": "N", "CalendarioEmisor": "N", "CalendarioFondo": "N", "CalendarioMoneda": "N", "IdEstado": "ACT"})
    base = [upd(cfg, {"EstadoFondo": "A"}, "\"IdTipoFecha\" = 'CERTIF'"), fila, upd(FONDO, {"EstadoRescateComercial": "C"})]
    caso("FMM-NOR-45_estado-del-fondo-define-las-fechas", "FM-NOR-08: perfil comercial con el rescate cerrado: la configuracion de fechas del estado C (certificacion a 3 dias)", [sol(RESCATE)], seed=base)
    caso("FMM-NOR-46_estado-abierto-backoffice-fechas", "FM-NOR-08: el usuario de back office ve el rescate abierto: la configuracion del estado A (certificacion a 1 dia)", [sol(RESCATE)], p_IdUsuario=8,
         seed=base + [ins('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"', {"Key": "PERFIL_BACKOFFICE", "Value": "6"}),
                      ins('"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"', {"IdPerfil": 6, "IdUsuario": 8}),
                      ins('"BSCL.Plataforma.BD::ACC.Usuario"', {"IdUsuario": 8, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 70})])


def variantes_de_fechas_y_regimen():
    """FM-NOR-53..60: date of the operation (default / explicit / quota date given), unknown request type, spot rescue in USD, empty regime, fund states of a contribution."""
    A, R = APORTE, RESCATE
    saldo = SALDO_OK
    caso("FMM-NOR-53_aporte-fondo-cerrado-comercial-fecha-defecto", "FM-NOR-08: aporte con FechaOperacion por defecto y el aporte cerrado para el perfil comercial: la emision es la fecha de sistema",
         [sol(A)], seed=[upd(FONDO, {"EstadoAporteComercial": "C"})], colab=saldo)
    caso("FMM-NOR-54_aporte-fecha-operacion-explicita", "FM-NOR-12: FechaOperacion explicita (se usa tal cual como fecha de emision)", [sol(A, FechaOperacion="2026-03-03")], colab=saldo)
    caso("FMM-NOR-55_rescate-fecha-valor-cuota-dada", "FM-NOR-02: FechaValorCuota dada por el llamador (formato Gson sql.Date): se usa para el valor cuota y la solicitud",
         [sol(R, FechaValorCuota="Mar 3, 2026", IdMonedaPago="")])
    caso("FMM-NOR-56_rescate-usd-genera-spot-certificacion", "FM-NOR-07: rescate en USD pagado en CLP con RESCATE_GENERA_VENTA_SPOT_DOLAR=S: la certificacion genera Spot (fechas)",
         [sol(R, IdMoneda="USD", IdMonedaPago="CLP", NumeroCuotas=10)], seed=[param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S"), upd(SERIE, {"Diferido": "S"})])
    caso("FMM-NOR-57_tipo-de-solicitud-desconocido", "FM-NOR-26: TipoSolicitud distinto de APO/RES: el fondo se trata como cerrado y sin valutas", [sol(R, TipoSolicitud="XXX")])
    caso("FMM-NOR-58_rescate-regimen-vacio", "FM-NOR-26: IdRegimenTributario vacio: la clave del flujo de custodia es id+null y toma el primer flujo (D-REQ-9)", [sol(R, IdRegimenTributario="")])
    caso("FMM-NOR-59_aporte-moneda-pago-distinta-sin-spot", "FM-NOR-07: aporte en CLP con pago USD (GeneraSpot=S, sin formas de pago): sin compuerta porque no hay Spot que hacer en simulacion", [sol(A, IdMonedaPago="USD")],
         p_SimularOperacion="S", colab=saldo)
    caso("FMM-NOR-60_aporte-sin-saldo-registrado", "FM-NOR-05: SaldoFlujoCaja no devuelve saldo para la fecha: saldo 0 y el aporte CIN manual se rechaza", [sol(A)],
         colab={"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": []}}})


def tributacion_y_traspaso_total():
    """FM-NOR-47..51: the taxation of the row by regime group (APV, ART108, ART107, 57BIS) and a total rescue of a DC custody without regime relation."""
    for n, grupo in ((47, "APV"), (48, "ART108"), (49, "ART107"), (50, "57BIS")):
        caso("FMM-NOR-%d_tributacion-%s" % (n, grupo.lower()), "FM-NOR-26: el grupo de regimen %s fija la tributacion de la solicitud" % grupo, [sol(RESCATE)],
             seed=[upd(REGIMEN, {"GrupoRegimen": grupo})])
    cartera = ins('"BSCL.Custodia.BD::CUS.Cartera"', {"IdPortafolio": 500, "IdInstrumento": 900, "IdRegimenFondoMutuo": "GEN", "IdEstado": "VIG", "IdCliente": 100,
                                                        "IdEmisor": "AGF1", "IdMonedaInstrumento": "CLP", "INGCantidad": 100, "INGMontoMercadoActualMonedaInstrumento": 150000,
                                                        "Nemotecnico": "FM001A", "FechaLiquidacion": "2026-03-01"})
    caso("FMM-NOR-51_rescate-total-sin-regimen-con-custodia-dc", "FM-NOR-26/13: rescate total sin relacion de regimen con la serie y custodia DC vigente: no permite rescate",
         [sol(RESCATE, CalculoPorMontoCuota="T", IdRegimenTributario="XXX")], seed=[cartera])


def comision_trimestral():
    import cases3a_lib as L3
    tramos = L3.tramo(10, "FM001A", "PER", 0, 9999, 1.2) + [L3.comision_maxima("FM001A")]
    seed = [param("CALCULAR_COMISION_CERTIFICACION", "S")] + tramos
    lote = {"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "Cantidad": 100, "PrecioCompra": 1500, "IdConcepto": "APORFM",
            "FechaOrigenCompra": "2026-02-10 00:00:00.000", "FechaOperacion": "2026-02-09 00:00:00.000"}
    flujo = {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoDetallado": [lote],
             "p_out_FlujoTotalizado": [{"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "IdRegimenFondoMutuo": "GEN", "Cantidad": 1000,
                                        "MontoAMercadoMonedaInstrumento": 1500123}]}
    caso("FMM-NOR-16_comision-trimestral-nn", "FM-NOR-16 D-NOR-6: CALCULAR_COMISION_CERTIFICACION=S con comision trimestral PER: se crea el rescate de comision NN",
         [sol(RESCATE, NumeroCuotas=10)], seed=seed, colab={"ConsultarFlujoCustodia": {"defecto": flujo}})
