"""FMM-CSR (A): contract, early exits, parameters, batching of the dates, profile, channel (rule cards FM-REI-41..44, 48 (batch), 67; defects D-F2-19, D-NOR-9)."""
import cases_csr_lib as L
from cases_csr_lib import APORTE, KEY_AUSENTE, sol

A1 = [sol(APORTE)]


def generar():
    c = L.caso
    bc = L.base_colab

    c("real-un-aporte-nacional-cin", "FM-REI-41/52/62/64/65/C52: real, 1 aporte FM001A->FM002A CIN: INSERT APOREINV, UPDATE FechaFinalReinversion, AGF (sin fila en la vista), AgregarLog", A1,
      colaboradores=bc(A1))
    c("simulacion-un-aporte", "FM-REI-66/C51: simulacion: sin escritura, sin Tesoreria/Spot/AGF/log; SaldoFlujoCaja se llama igual (D-F2-13)", A1, colaboradores=bc(A1), p_SimularOperacion="S")
    c("simulacion-nula-es-real", "FM-REI-66: p_SimularOperacion nulo = pasada real (!\"S\".equalsIgnoreCase(null))", A1, colaboradores=bc(A1), p_SimularOperacion=None)
    c("simulacion-minuscula-simula", "FM-REI-66: 's' minuscula simula (equalsIgnoreCase)", A1, colaboradores=bc(A1), p_SimularOperacion="s")
    c("simulacion-ausente-es-real", "FM-REI-41/66: la clave p_SimularOperacion ausente = nulo = real", A1, colaboradores=bc(A1), p_SimularOperacion=KEY_AUSENTE)
    c("falta-parametro-spot", "FM-REI-42/C60: falta APORTE_GENERA_COMPRA_SPOT_DOLAR: excepcion del parametro ANTES de los requisitos basicos (sin colaboradores, sin escritura)", A1,
      colaboradores=bc(A1), seed=L.param("APORTE_GENERA_COMPRA_SPOT_DOLAR", None))
    c("falta-moneda-sistema", "FM-REI-42: falta PLA.MonedaSistema: excepcion del parametro antes de los requisitos", A1, colaboradores=bc(A1),
      seed=L.config_plataforma("PLA.MonedaSistema", None))
    c("requisitos-basicos-usuario-inexistente", "FM-REI-42/C61: RevisarRequisitosBasicos con error (usuario inexistente): contador 1, mensajes del kit, return sin leer nada mas", A1,
      colaboradores=bc(A1), p_IdUsuario=99)
    c("requisitos-basicos-modulo-inexistente", "FM-REI-42/C61: modulo inexistente: contador 1 y mensaje del kit", A1, colaboradores=bc(A1), p_IdModulo="XXX")
    c("lista-vacia", "FM-REI-42/C61: lista vacia -> NO_SE_INGRESARON_SOLICITUDES_APORTE (id 0), contador 1, return", [], colaboradores=bc(A1))
    c("idinterno-cero", "FM-REI-42/C61: IdInterno 0 -> ID_INTERNO_NO_CERO, contador 1, return", [sol(APORTE, IdInterno=0)], colaboradores=bc(A1))
    c("idinterno-repetido", "FM-REI-42/C61: IdInterno repetido -> CORRELATIVO_INCORRECTO, contador 1, return (antes de leer FechaSistema)", [sol(APORTE), sol(APORTE)], colaboradores=bc(A1))
    c("idinterno-nulo-npe", "FM-REI-42/67, D-NOR-9: IdInterno nulo -> NullPointerException (mov.IdInterno.longValue())", [sol(APORTE, IdInterno=None)], colaboradores=bc(A1))
    c("falta-fecha-sistema", "FM-REI-42: falta PLA.FechaSistema (tras validar los IdInterno): excepcion del parametro", A1, colaboradores=bc(A1), seed=L.config_plataforma("PLA.FechaSistema", None))
    c("respeta-valutas-ausente-es-n", "FM-REI-42/48: RespetaValutasCargaMasiva ausente (try/catch, ADVERTENCIA) = \"N\": una solicitud batch usa las valutas de carga masiva (fechas sin valutas)",
      [sol(APORTE, SolicitudBatch="S")], colaboradores=bc(A1), seed=L.param("RespetaValutasCargaMasiva", None))
    c("batch-valutas-cero", "FM-REI-48: SolicitudBatch S y RespetaValutasCargaMasiva N: EstadoFondo/Liquidacion A, valutas 0 (modalidad BATCH), sin AGF", [sol(APORTE, SolicitudBatch="S")], colaboradores=bc(A1))
    c("batch-respeta-valutas", "FM-REI-48: SolicitudBatch S con RespetaValutasCargaMasiva S: sigue la rama normal de valutas (por tipo de reinversion) y luego fuerza A/A (fondo siempre abierto para batch)",
      [sol(APORTE, SolicitudBatch="S")], colaboradores=bc(A1), seed=L.param("RespetaValutasCargaMasiva", "S"))
    c("batch-minuscula-no-es-batch", "D-CSR-07 FM-REI-48/62: SolicitudBatch 's' minuscula: el conteo usa equalsIgnoreCase (batch) pero la rama de fechas usa equals (no batch) y la lista AGF usa equalsIgnoreCase('N')",
      [sol(APORTE, SolicitudBatch="s")], colaboradores=bc(A1))
    c("perfil-ausente-con-fechaemision", "FM-REI-44/C62: usuario sin perfil y FechaEmision informada en la primera fila -> PERFIL_INCORRECTO, contador 1, return true", A1, colaboradores=bc(A1),
      p_IdUsuario=9)
    c("perfil-ausente-sin-fechaemision", "FM-REI-44/C62, D-F2-19: usuario sin perfil pero FechaEmision nula en la primera fila: NO rechaza (condicion extra del legacy)",
      [sol(APORTE, FechaEmision=None)], colaboradores=bc(A1), p_IdUsuario=9)
    c("perfil-ausente-todo-batch", "FM-REI-44: usuario sin perfil con todas las filas batch (countTransaccionesBatch == total): no rechaza", [sol(APORTE, SolicitudBatch="S")], colaboradores=bc(A1),
      p_IdUsuario=9)
    c("perfil-backoffice", "FM-REI-44/48: usuario de back office: estados de aporte/liquidez de back office (fondo cerrado para back office, abierto para comercial)", A1, colaboradores=bc(A1),
      p_IdUsuario=8, seed=[L.upd(L.FONDO, {"EstadoAporteBackoffice": "C", "EstadoLiquidezBackoffice": "C"}, "\"IdFondo\" = '11'")])
    c("perfil-comercial-fondo-cerrado", "FM-REI-44/48: usuario comercial con el fondo cerrado para comercial: estado de aporte C en las fechas y en el validador", A1, colaboradores=bc(A1),
      seed=[L.upd(L.FONDO, {"EstadoAporteComercial": "C", "EstadoLiquidezComercial": "C"}, "\"IdFondo\" = '11'")])
    c("canal-de-la-peticion", "FM-REI-52/C83: sin Canal en el aporte: IdTipoCanalIngreso = p_IdTipoCanalIngreso", A1, colaboradores=bc(A1), p_IdTipoCanalIngreso="OPT")
    c("canal-del-aporte", "FM-REI-52: el Canal del aporte gana sobre p_IdTipoCanalIngreso", [sol(APORTE, Canal="WEB")], colaboradores=bc(A1), p_IdTipoCanalIngreso="OPT")
    c("canal-vacio-del-aporte", "FM-REI-52: Canal \"\" (no nulo) se usa tal cual (sin AGE por defecto)", [sol(APORTE, Canal="")], colaboradores=bc(A1), p_IdTipoCanalIngreso="OPT")
    c("canal-nulo-sin-defecto", "D-CSR-06 FM-REI-52/C83: ni Canal ni p_IdTipoCanalIngreso: IdTipoCanalIngreso nulo (sin AGE por defecto): el INSERT falla (NOT NULL) tras Tesoreria/Spot",
      A1, colaboradores=bc(A1), p_IdTipoCanalIngreso=None)
    c("canal-nulo-simulacion", "FM-REI-52/66: canal nulo en simulacion: no hay INSERT; el resultado trae IdTipoCanalIngreso nulo", A1, colaboradores=bc(A1), p_IdTipoCanalIngreso=None, p_SimularOperacion="S")
    minimo = {k: v for k, v in APORTE.items() if k in ("IdInterno", "IdCliente", "IdPortafolio", "IdSerieFondoOrigen", "IdEmisorOrigen", "FechaOrigenInversion", "MontoOrigen", "IndicadorMLT",
                                                       "IdSerieFondo", "Monto", "NumeroCuotas", "IdMoneda", "IdMedioDePago", "Visado", "SolicitudBatch", "OrigenOperacion", "IdRegimenTributario",
                                                       "Glosa", "IdAutorizadoOrigen", "FolioContrato", "IdSolicitudAporteRescateRelacionado")}
    mn = [dict(minimo)]
    c("defaults-del-contrato", "FM-REI-41: solo los campos obligatorios: Switch N, IdAgrupador 0, Porcentaje 100, GeneraSpot N, MontoPatrimonio 0, IngresoAutomatico N, MontoTotalInversion 0, CostoUF 0, "
      "fechas de origen 0001-01-01; FechaEmision nula -> FechaFondo de la serie", mn, colaboradores=bc(mn))
    c("folio-nulo-npe", "FM-REI-52/67: FolioContrato ausente -> NullPointerException (solicitud.FolioContrato > 0)", [sol(minimo, FolioContrato=KEY_AUSENTE)], colaboradores=bc(A1))
    c("folio-informado", "FM-REI-52: FolioContrato > 0 se respeta (la secuencia se pide igual para todo el lote)", [sol(APORTE, FolioContrato=555)], colaboradores=bc(A1))
    c("relacionado-nulo", "FM-REI-49/52/64: IdSolicitudAporteRescateRelacionado nulo: la solicitud queda con 0; la lista de rescates contiene null (IN (null)); la validacion de saldo corre (relacionado 0)",
      [sol(APORTE, IdSolicitudAporteRescateRelacionado=KEY_AUSENTE)], colaboradores=bc(A1))
    c("fechaorigen-nula-npe", "FM-REI-52/67: FechaOrigenInversion ausente -> excepcion al armar la solicitud (Fecha.toDate(null))", [sol(APORTE, FechaOrigenInversion=KEY_AUSENTE)], colaboradores=bc(A1))
    c("fechaorigen-mal-formada", "FM-REI-52/67: FechaOrigenInversion mal formada -> excepcion del parseo", [sol(APORTE, FechaOrigenInversion="15/01/2026")], colaboradores=bc(A1))
    c("fechavalorcuota-de-la-entrada", "FM-REI-41/49/52/C82: FechaValorCuota (java.sql.Date, Gson plano 'Mar 9, 2026', locale en) de la entrada (distinta de la calculada, 2026-03-04): se usa para el precio y queda en la solicitud",
      [sol(APORTE, FechaValorCuota="Mar 9, 2026")], colaboradores=bc(A1))
    c("fechavalorcuota-iso-falla-gson", "FM-REI-41/C82: FechaValorCuota ISO (2026-03-04): Gson plano no la lee (formato MMM d, yyyy) -> JsonSyntaxException antes de la logica",
      [sol(APORTE, FechaValorCuota="2026-03-04")], colaboradores=bc(A1))
    c("fechaemision-nula-usa-fechafondo", "FM-REI-48: FechaEmision nula: la fecha de emision del calculo es la FechaFondo de la serie (con perfil valido)", [sol(APORTE, FechaEmision=None)], colaboradores=bc(A1))
    c("empresa-otro-pais", "FM-REI-42/48: ConsultarEmpresa responde IdPais 840: p_IdPais de las fechas y de la custodia es el de la empresa", A1,
      colaboradores=L.colab(bc(A1), {"ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "Codigo": "BCS", "Nombre": "Corredora Ficticia", "IdEstado": "ACT", "IdPais": 840, "IdSocioDeNegocio": 10, "IdCliente": 99}}}))
    # ---- null fields of the request (FM-REI-41/67, D-NOR-9): what the legacy does with each absent field
    c("moneda-nula-npe", "FM-REI-41/51/67: IdMoneda ausente -> NullPointerException (solicitud.IdMoneda.equals(...) o la paridad)", [sol(APORTE, IdMoneda=KEY_AUSENTE)], colaboradores=bc(A1))
    c("monto-nulo", "FM-REI-41/46/49: Monto ausente (MontoTotalInversion 0): la paridad y el calculo reciben Monto nulo", [sol(APORTE, Monto=KEY_AUSENTE)], colaboradores=bc(A1))
    c("medio-de-pago-nulo-es-no-cin", "FM-REI-52/53/61: IdMedioDePago ausente: no es CIN (equalsIgnoreCase(null) falso): va a Tesoreria y Visado queda el de la entrada", [sol(APORTE, IdMedioDePago=KEY_AUSENTE)], colaboradores=bc(A1))
    c("regimen-nulo", "FM-REI-45/53: IdRegimenTributario ausente: la lectura de regimenes lleva null, no hay fila para la serie: EsRegimenValido N", [sol(APORTE, IdRegimenTributario=KEY_AUSENTE)], colaboradores=bc(A1))
    c("solicitudbatch-nulo", "FM-REI-48/62: SolicitudBatch ausente: no es batch (equalsIgnoreCase(null) falso) y tampoco entra en AGF (NO.equalsIgnoreCase(null) falso)", [sol(APORTE, SolicitudBatch=KEY_AUSENTE)], colaboradores=bc(A1))
    c("agrupador-nulo-falla-insert", "FM-REI-52/64: IdAgrupador nulo explicito: el INSERT falla (NOT NULL) tras Tesoreria/Spot", [sol(APORTE, IdAgrupador=None)], colaboradores=bc(A1))
    c("serie-origen-igual-a-destino", "FM-REI-47: origen y destino la misma serie (FM001A -> FM001A): sin validacion de cambio de serie en AR; NACIONAL", [sol(APORTE, IdSerieFondo="FM001A")], colaboradores=bc([sol(APORTE, IdSerieFondo="FM001A")]))
    c("perfil-comercial-ignora-backoffice", "FM-REI-44/48: fondo cerrado solo para back office con usuario comercial: abierto, sin advertencias", A1, colaboradores=bc(A1),
      seed=[L.upd(L.FONDO, {"EstadoAporteBackoffice": "C", "EstadoLiquidezBackoffice": "C"}, "\"IdFondo\" = '11'")])
