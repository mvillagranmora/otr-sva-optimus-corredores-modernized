"""FM-NOR cases, part A: calculation modes, flows of money, origins."""
from cases_nor_lib import *


def lock_row(s):
    ident = "%s_%s_%s_%s_%s_%s_2026-03-02" % (s["IdPortafolio"], s["IdSerieFondo"], s["IdRegimenTributario"], s["TipoSolicitud"], s["Monto"], s["NumeroCuotas"])
    return ins(LOCK, {"IdBloqueoSolicitudAporteRescate": ident, "IdPortafolio": s["IdPortafolio"], "IdSerieFondo": s["IdSerieFondo"],
                      "IdRegimenTributario": s["IdRegimenTributario"], "TipoSolicitud": s["TipoSolicitud"], "Monto": s["Monto"],
                      "NumeroCuotas": s["NumeroCuotas"], "FechaSistema": "2026-03-02"})


def generar():
    R, A = RESCATE, APORTE
    caso("FMM-NOR-02_rescate-por-monto", "FM-NOR-02: rescate por monto (M): numero de cuotas por el valor cuota con el redondeo del legacy",
         [sol(R, CalculoPorMontoCuota="M", Monto=150000, NumeroCuotas=0)])
    caso("FMM-NOR-03_rescate-total", "FM-NOR-03: rescate total (T) de la custodia completa",
         [sol(R, CalculoPorMontoCuota="T", NumeroCuotas=1000)])
    caso("FMM-NOR-04_aporte-cin-manual-saldo-ok", "FM-NOR-04: aporte por monto con medio CIN y origen manual: SaldoFlujoCaja suficiente", [sol(A)], colab=SALDO_OK)
    caso("FMM-NOR-05_aporte-cin-saldo-insuficiente", "FM-NOR-05: aporte CIN manual con saldo insuficiente (UI_SALDO_INSUFICIENTE_FLUJO_CAJA)", [sol(A)], colab=SALDO_BAJO)
    caso("FMM-NOR-31_aporte-cin-opera-saldo-negativo", "FM-NOR-05 CELLINCECT-763: el portafolio opera con saldo negativo (S) y el ingreso viene de CORREDORES: no se rechaza",
         [sol(A, IngresoDesde="CORREDORES")],
         seed=[ins('"BSCL.Plataforma.BD::IND.TipoIndicador"', {"IdTipoIndicador": 1, "IdTipoEntidad": 1, "CodigoTipoIndicador": "GEN_OCSNEG", "IdEstado": "ACT"}),
               ins('"BSCL.Plataforma.BD::IND.Indicador"', {"IdEntidad": 500, "IdTipoIndicador": 1, "IdTipoEntidad": 1, "ValorIndicador": "S"})], colab=SALDO_BAJO)
    caso("FMM-NOR-06_aporte-medio-no-cin-tesoreria", "FM-NOR-06: aporte con medio TRA: ProcesarIngresoEgresoTesoreria -> CrearIngresoEgreso (doble); con la compuerta G1 cerrada el modulo nuevo responde 503 (DEV-F4)",
         [sol(A, IdMedioDePago="TRA")],
         colab={"CrearIngresoEgreso": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_RESULTADOPROCESO": [], "P_OUT_INGRESOEGRESOGENERADO": []}}})
    caso("FMM-NOR-07_aporte-usd-genera-spot", "FM-NOR-07: aporte en USD con pago CLP: GeneraSpot y ProcesarIngresoOperacionSpot (dobles Spot); G2 cerrada => 503 (DEV-F4)",
         [sol(A, IdMoneda="USD", IdMonedaPago="CLP", Monto=100)],
         seed=[upd(SERIE, {"Diferido": "S"})], colab=SALDO_OK, p_FormaPagoSpot=[{"IdInternoPagoOperacion": 1, "IdComprobantePagoOperacion": 1, "IdTipoMovimiento": "PAGO",
              "IdFormaDePago": "TRA", "IdMoneda": "CLP", "IdMonedaLiquidacion": "CLP", "IdOperacion": 1, "RecibePagaPortafolio": "R", "MontoFormaDePago": 90000, "AfectarTesoreria": "S"}])
    caso("FMM-NOR-08_perfil-comercial-fondo-cerrado", "FM-NOR-08: el estado de la serie depende del perfil: rescate cerrado para el perfil comercial (usuario 7)",
         [sol(R)], seed=[upd(FONDO, {"EstadoRescateComercial": "C"})])
    caso("FMM-NOR-32_perfil-backoffice-fondo-abierto", "FM-NOR-08: mismo fondo (cerrado para comercial) con un usuario de back office: se acepta",
         [sol(R)], p_IdUsuario=8,
         seed=[upd(FONDO, {"EstadoRescateComercial": "C"}), ins('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"', {"Key": "PERFIL_BACKOFFICE", "Value": "6"}),
               ins('"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"', {"IdPerfil": 6, "IdUsuario": 8}),
               ins('"BSCL.Plataforma.BD::ACC.Usuario"', {"IdUsuario": 8, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 70})])
    caso("FMM-NOR-09_usuario-sin-perfil", "FM-NOR-09: usuario sin perfil de fondos mutuos: PERFIL_INCORRECTO, salida temprana",
         [sol(R)], p_IdUsuario=9,
         seed=[ins('"BSCL.Plataforma.BD::ACC.Usuario"', {"IdUsuario": 9, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 70})])
    pat = [upd(SERIE, {"PatrimonioMinimo": 5000000})]
    caso("FMM-NOR-10_pac-patrimonio-minimo-d-nor-4", "FM-NOR-10 D-NOR-4: origen PAC (sin bloqueo): por JSON la comparacion de referencia es siempre verdadera y el patrimonio minimo se exige",
         [sol(A)], seed=pat, p_IdOrigenProceso="PAC")
    caso("FMM-NOR-33_bonif-exenta-patrimonio-minimo", "FM-NOR-10: origen BONIF queda exento del patrimonio minimo (equalsIgnoreCase)", [sol(A)], seed=pat, p_IdOrigenProceso="BONIF")
    caso("FMM-NOR-34_rec-patrimonio-minimo", "FM-NOR-10: origen REC no esta exento", [sol(A)], seed=pat, p_IdOrigenProceso="REC")
    caso("FMM-NOR-11_origen-car-sin-bloqueo", "FM-NOR-11: origen CAR: sin bloqueo anti duplicados", [sol(R)], p_IdOrigenProceso="CAR")
    caso("FMM-NOR-35_origen-csc-sin-bloqueo", "FM-NOR-11: origen CSC: sin bloqueo anti duplicados", [sol(R)], p_IdOrigenProceso="CSC")
    caso("FMM-NOR-12_batch-sin-respetar-valutas", "FM-NOR-12: SolicitudBatch=S con RespetaValutasCargaMasiva N: fechas sin valutas", [sol(R, SolicitudBatch="S")])
    caso("FMM-NOR-36_batch-respeta-valutas", "FM-NOR-12: SolicitudBatch=S con RespetaValutasCargaMasiva S", [sol(R, SolicitudBatch="S")],
         seed=[param("RespetaValutasCargaMasiva", "S")])
    caso("FMM-NOR-13_traspaso-apv", "FM-NOR-13: rescate por traspaso APV (FolioContrato en TRA.Traspaso, EsTraspaso)", [sol(R, EsTraspaso="S", FolioContrato=700)],
         seed=[ins('"BSCL.APV.BD::TRA.Traspaso"', {"IdTraspaso": 700, "RazonRetiro": "TRA", "IdPersona": 1000})])
    caso("FMM-NOR-14_patrimonio-minimo-insuficiente", "FM-NOR-14: patrimonio minimo de la serie insuficiente (aporte CIN manual)", [sol(A)],
         seed=[upd(SERIE, {"PatrimonioMinimo": 99999999})], colab=SALDO_OK)
    caso("FMM-NOR-15_rescate-mayor-a-custodia", "FM-NOR-15: rescate mayor a la custodia: UI_ERROR_FM_CLIENTE_SIN_CUSTODIA", [sol(R, NumeroCuotas=5000)])
    caso("FMM-NOR-18_multi-solicitud-mixta", "FM-NOR-18: tres solicitudes, una rechazada: el contador suma por mensaje",
         [sol(R, IdInterno=1), sol(R, IdInterno=2, NumeroCuotas=5000), sol(R, IdInterno=3, NumeroCuotas=20)])
    caso("FMM-NOR-19_simulacion", "FM-NOR-19: p_SimularOperacion=S: sin INSERT, sin bloqueo, sin colaboradores de escritura", [sol(R)], p_SimularOperacion="S")
    caso("FMM-NOR-20_bloqueo-duplicado", "FM-NOR-20: la solicitud identica ya esta bloqueada: UI_ERROR_BLOQUEO_SOLICITUD", [sol(R)], seed=[lock_row(R)])
    caso("FMM-NOR-37_bloqueo-lote-parcial", "FM-NOR-20 D-NOR-2: lote con una solicitud bloqueada y otra no: no se bloquea",
         [sol(R, IdInterno=1), sol(R, IdInterno=2, NumeroCuotas=20)], seed=[lock_row(R)])
