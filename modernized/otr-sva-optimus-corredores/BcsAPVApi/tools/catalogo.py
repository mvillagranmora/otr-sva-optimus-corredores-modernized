# -*- coding: utf-8 -*-
"""Curated (human) decisions of the design: legacy class -> family, use case name, REST route, verb.

Everything else (DTO fields, defaults, collaborators) is derived mechanically from the legacy sources.
Routes are PROPOSALS (open question for the human: naming).  Verb rule: GET only for Consultar*/Obtener* whose Entrada is flat scalars
and has no side effect (ObtenerFolioSuscripcion is POST: it calls ActualizarFolioSuscripcion); Modificar* is PUT; the rest POST.
A "None" use case = the legacy JavaCall class whose call() returns null (not an invocable operation: helper of other services).
"""

FAMILIAS = {
    'A': ('contratos', 'Contratos SAAPV (crear, grabar, modificar, estados, validacion, folios, perfiles, busqueda y asignacion a recaudacion, carga masiva de contratos)'),
    'B': ('traspasos', 'Traspasos (contrato de traspaso, portafolio, tesoreria, desinversion, reapertura, cuentas bancarias, retencion, recaudacion de traspaso)'),
    'C': ('recaudacion', 'Recaudacion / PAC / bonificacion / cargas masivas / distribucion / bancos Previred / replica de portafolio'),
}

# legacy simple name -> (familia, caso de uso, ruta, verbo o None=regla)
OPS = {
    # ---- family A: contratos SAAPV
    'CrearContratoSAAPV': ('A', 'CrearContrato', '/apv/contratos', 'POST'),
    'GrabarContratoSAAPV': ('A', 'GrabarContratos', '/apv/contratos/grabacion', 'POST'),
    'ModificarContratoSAAPV': ('A', 'ModificarContratos', '/apv/contratos', 'PUT'),
    'CambiarEstadoContratosSAAPV': ('A', 'CambiarEstadoContratos', '/apv/contratos/cambio-estado', 'POST'),
    'CambiarEstadoEnvioNotificacionContratoSAAPV': ('A', 'CambiarEstadoNotificacionContratos', '/apv/contratos/notificacion/cambio-estado', 'POST'),
    'CambiarEstadoRevocacionSAAPV': ('A', 'RevocarContratos', '/apv/contratos/revocacion', 'POST'),
    'CambiarAPlanActivoContratoSAAPV': ('A', 'CambiarContratosAPlanActivo', '/apv/contratos/plan-activo', 'POST'),
    'CambiarAPlanActivoContratoSAAPVWrapper': ('A', 'SolicitarCambioAPlanActivo', '/apv/contratos/plan-activo/solicitud', 'POST'),
    'ValidaContratoSAAPV': ('A', 'ValidarContrato', '/apv/contratos/validacion', 'POST'),
    'ConsultarClienteContratoSAAPV': ('A', 'ConsultarClienteContrato', '/apv/contratos/cliente', None),
    'ObtenerFolioSuscripcion': ('A', 'ObtenerFolioSuscripcion', '/apv/contratos/folios-suscripcion', 'POST'),
    'AsociarPerfilesAPV': ('A', 'AsociarPerfiles', '/apv/perfiles/asociacion', 'POST'),
    'ModificarRelacionLaboral': ('A', 'ModificarRelacionLaboral', '/apv/contratos/relacion-laboral', 'PUT'),
    'BusquedaContratoSAAPVRecaudacion': ('A', 'BuscarContratosParaRecaudacion', '/apv/contratos/busqueda-recaudacion', 'POST'),
    'AsignarContratoARecaudacion': ('A', 'AsignarContratoARecaudacion', '/apv/recaudaciones/asignacion-contrato', 'POST'),
    'ValidarYAsignarContratosARecaudaciones': ('A', 'ValidarYAsignarContratosARecaudaciones', '/apv/recaudaciones/validacion-asignacion-contratos', 'POST'),
    'CargaMasivaArchivo': ('A', 'CargarContratosMasivo', '/apv/contratos/carga-masiva', 'POST'),
    'ConsultarCustodiaPortafolioAPV': ('A', 'ConsultarCustodiaPortafolio', '/apv/portafolios/custodia', None),
    'ConsultarPreferenciaPagoPortafolio': ('A', 'ConsultarPreferenciaPagoPortafolio', '/apv/portafolios/preferencia-pago', None),
    # ---- family B: traspasos
    'GrabarContratoTraspasoSAAPV': ('B', 'GrabarContratosTraspaso', '/apv/traspasos/contratos', 'POST'),
    'ModificarPortafolioTraspaso': ('B', 'ModificarPortafoliosTraspaso', '/apv/traspasos/portafolios', 'PUT'),
    'CrearTraspasoTesoreria': ('B', 'CrearTraspasoTesoreria', '/apv/traspasos/tesoreria', 'POST'),
    'GrabarTraspasos': ('B', 'GrabarTraspasos', '/apv/traspasos', 'POST'),
    'CalculoMontosDesinversion': ('B', 'CalcularMontosDesinversion', '/apv/traspasos/calculo-desinversion', 'POST'),
    'ReaperturarTraspaso': ('B', 'ReaperturarTraspasos', '/apv/traspasos/reapertura', 'POST'),
    'TraspasoCuentasBancarias': ('B', 'TraspasarEntreCuentasBancarias', '/apv/traspasos/cuentas-bancarias', 'POST'),
    'CrearTraspaso': ('B', 'CrearTraspaso', '/apv/traspasos/creacion', 'POST'),
    'AnularTraspasoEgreso': ('B', 'AnularTraspasoEgreso', '/apv/traspasos/anulacion', 'POST'),
    'CalculoRetencionTraspasos': ('B', 'CalcularRetencionTraspasos', '/apv/traspasos/calculo-retencion', 'POST'),
    'GrabarRecaudacionTraspaso': ('B', 'GrabarRecaudacionTraspaso', '/apv/traspasos/recaudaciones', 'POST'),
    'IngresoRecaudacionTraspaso': ('B', 'IngresarRecaudacionTraspaso', '/apv/traspasos/recaudaciones/ingreso', 'POST'),
    'CambiarEstadoTraspaso': ('B', 'CambiarEstadoTraspasos', '/apv/traspasos/cambio-estado', 'POST'),
    'CrearOrdenTraspaso': ('B', 'CrearOrdenesTraspaso', '/apv/traspasos/ordenes', 'POST'),
    'CargaRegistroHistoricoTraspaso': ('B', 'CargarHistoricoTraspasos', '/apv/traspasos/historico', 'POST'),
    'ModificarOrigenDestino': ('B', 'ModificarOrigenDestinoTransicion', '/apv/traspasos/origen-destino', 'PUT'),
    # ---- family C: recaudacion / PAC / bonificacion / cargas / distribucion
    'ProcesarDistribucion': ('C', 'ProcesarDistribucion', '/apv/recaudaciones/distribucion', 'POST'),
    'ProcesarCargaMasivaRecaudacion': ('C', 'ProcesarCargaMasivaRecaudacion', '/apv/recaudaciones/carga-masiva/proceso', 'POST'),
    'CargaMasivaPrevired': ('C', 'CargarArchivoPrevired', '/apv/recaudaciones/carga-masiva/previred', 'POST'),
    'CargaMasivaRecaudacion': ('C', 'CargarMasivaRecaudacion', '/apv/recaudaciones/carga-masiva', 'POST'),
    'CargaRecaudacion': ('C', 'CargarRecaudacion', '/apv/recaudaciones/carga', 'POST'),
    'CrearRecaudacionCliente': ('C', 'CrearRecaudacionCliente', '/apv/recaudaciones/clientes', 'POST'),
    'CrearRecaudacionDeposito': ('C', 'CrearRecaudacionDeposito', '/apv/recaudaciones/depositos', 'POST'),
    'CrearRecaudacionEmpleador': ('C', 'CrearRecaudacionEmpleador', '/apv/recaudaciones/empleadores', 'POST'),
    'ConciliarDeposito': ('C', 'ConciliarDeposito', '/apv/recaudaciones/depositos/conciliacion', 'POST'),
    'DevolucionPagoRezagos': ('C', 'DevolverPagoRezagos', '/apv/recaudaciones/devoluciones/rezagos', 'POST'),
    'DevolucionBF_EntidadRecaudadora': ('C', 'DevolverBonificacionEntidadRecaudadora', '/apv/bonificaciones/devolucion-entidad-recaudadora', 'POST'),
    'CargaBonificacionTGR': ('C', 'CargarBonificacionTGR', '/apv/bonificaciones/carga-tgr', 'POST'),
    'EjecutarBonificacion': ('C', 'EjecutarBonificacion', '/apv/bonificaciones/ejecucion', 'POST'),
    'ProcesarBonificacion': ('C', 'ProcesarBonificacion', '/apv/bonificaciones/proceso', 'POST'),
    'ModificarBonificacion': ('C', 'ModificarBonificacion', '/apv/bonificaciones', 'PUT'),
    'CargaMasivaPAC': ('C', 'CargarPAC', '/apv/pac/carga-masiva', 'POST'),
    'CargaMasivaBanco': ('C', 'CargarArchivoBanco', '/apv/recaudaciones/carga-masiva/banco', 'POST'),
    'CargaMasivaSAAPVPAC': ('C', 'CargarContratosPAC', '/apv/pac/contratos/carga-masiva', 'POST'),
    'ReplicaPortafolioAPV': ('C', 'ReplicarPortafolio', '/apv/portafolios/replica', 'POST'),
    'CrearBancoPrevired': ('C', 'CrearBancoPrevired', '/apv/bancos-previred', 'POST'),
    'EliminarBancoPrevired': ('C', 'EliminarBancoPrevired', '/apv/bancos-previred/eliminacion', 'POST'),
    'ModificarBancoPrevired': ('C', 'ModificarBancoPrevired', '/apv/bancos-previred', 'PUT'),
    'ValidarBancoPrevired': ('C', None, None, None),
    'ValidarMandatosPAC': ('C', None, None, None),
    'CambiarEstadoRecaudacion': ('C', 'CambiarEstadoRecaudacion', '/apv/recaudaciones/cambio-estado', 'POST'),
    'CrearAnotacionRecaudacion': ('C', 'CrearAnotacionRecaudacion', '/apv/recaudaciones/anotaciones', 'POST'),
    'ModificarAnotacionRecaudacion': ('C', 'ModificarAnotacionRecaudacion', '/apv/recaudaciones/anotaciones', 'PUT'),
    'EliminarRegistrosRecaudacion': ('C', 'EliminarRegistrosRecaudacion', '/apv/recaudaciones/registros/eliminacion', 'POST'),
    'AsignarDepositoEmpleador': ('C', 'AsignarDepositoEmpleador', '/apv/recaudaciones/depositos/asignacion-empleador', 'POST'),
    'ConsultarTotalesDistribucionRecaudacion': ('C', 'ConsultarTotalesDistribucion', '/apv/recaudaciones/distribucion/totales', None),
    'ConsultarTotalesOrigenPorDeposito': ('C', 'ConsultarTotalesOrigenPorDeposito', '/apv/recaudaciones/depositos/totales-origen', None),
}

# Gson flavour label: how the legacy JavaCall.call() builds its Gson (inbound AND outbound)
SABOR_TXT = {
    'plain': 'new Gson() (nulls omitted on output, default date formats)',
    'builder': 'Utils.getGsonBuilder() (serializeNulls, date "yyyy-MM-dd HH:mm:ss.SSS")',
}

# human notes on known legacy quirks and where they will live (domain outcome vs adapter), keyed by legacy class
NOTAS = {
    'CambiarEstadoRevocacionSAAPV': 'Salida has TWO error counters (P_OUT_ERRORESCONTADOR and ContadorErrores): both kept (typed: contadorErrores and contadorErroresLegacy); the domain fills them as legacy does.',
    'ValidarBancoPrevired': 'call() returns null: not an invocable operation; used by CargaMasivaBanco as an internal helper (domain collaborator, no route).',
    'ValidarMandatosPAC': 'call() returns null: not an invocable operation; logica(SalidaRecaudacionTraspaso,...) is an internal helper of the traspaso recaudacion (domain collaborator, no route).',
    'ModificarOrigenDestino': 'Its Entrada class is (mis)named ModificarBonificacion_Entrada in the legacy source (copy/paste); typed record is ModificarOrigenDestinoTransicionEntrada.',
    'ObtenerFolioSuscripcion': 'Side effect through the stored procedure BSCL.APV.BD.Servicios::ActualizarFolioSuscripcion (the only real stored procedure of the module).',
    'ConsultarTotalesOrigenPorDeposito': 'Reads through the stored procedure BSCL.APV.BD.Servicios::APV.ConsultarTotalesOrigenPorDeposito (SQL.call): on SP errors the legacy REUSES one MensajeSalida instance for every error and warning (all P_OUT_MENSAJES end up identical), and P_OUT_ERRORESCONTADOR is never set (null, omitted by plain Gson). Kept in the domain outcome.',
}


def verbo_de(nombre, op_entrada_plana):
    fam, caso, ruta, verbo = OPS[nombre]
    if caso is None:
        return None
    if verbo:
        return verbo
    return 'GET' if op_entrada_plana else 'POST'
