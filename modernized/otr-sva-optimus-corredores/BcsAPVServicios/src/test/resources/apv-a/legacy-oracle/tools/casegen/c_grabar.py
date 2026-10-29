"""Cases of GrabarContratoSAAPV (ids 380-449)."""
import copy

from lib import *  # noqa: F401,F403

G = 'GrabarContratoSAAPV'
GF = 'GrabarContratoSAAPV.java'

T_TIPOIND = 'BSCL.Plataforma.BD::IND.TipoIndicador'
V_DIRFIS = 'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica'
T_DIRFIS = 'BSCL.SocioNegocio.BD::PER.DireccionFisica'
V_REL = 'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion'
V_REGIM = 'BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario'

SEQ = {'BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV': 9500, 'BSCL.APV.BD.Secuencias::TRA.IdTraspaso': 6000,
       'BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso': 6500}
SP_PORTAFOLIO = 'BSCL.Portafolio.BD.Servicios::CrearPortafolio'
SP_RELACION = 'BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona'
SP_INDICADOR = 'BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador'
# the wall-clock dates the legacy puts in the enrolment objects and in the relation call (System.currentTimeMillis): masked in the call log
MASCARA = ['FechaCreacion', 'FechaModificacion', 'EMP_FechaInicioCargo', 'EMP_FechaTerminoCargo', 'FechaInicioRelacion', 'FechaNacimiento']


def sp_ok(ref=0):
    return {'p_out_RefId': ref, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': 0, 'p_out_ErroresDetalle': [], 'p_out_AdvertenciasDetalle': []}


def par(**kw):
    d = {'IdEmpresa': 1, 'FolioContratoSAAPV': 'F-G1', 'IdPlan': 1, 'NombrePlan': 'ABIERTO', 'FechaSuscripcion': '2025-03-01', 'FechaCreacion': '2025-03-01',
         'FechaAprobacion': '0001-01-01', 'FechaNotificacion': '0001-01-01', 'FechaRecepcion': '0001-01-01', 'Folio': 'F-G1', 'IdPersona': 501,
         'IdEmpleador': 0, 'IdCliente': 701, 'IdCategoriaInversionista': 'MODERADO', 'IdPerfilComercial': 'PERFIL BASE', 'NombreCliente': 'ANA',
         'ApellidoPaternoCliente': 'PEREZ', 'ApellidoMaternoCliente': 'SOTO', 'DocumentoIdentidadCliente': '11111111-1', 'FechaNacimientoCliente': '1980-05-10',
         'EstadoCivil': 'SO', 'IdTipoGenero': 'F', 'IdPaisResidenciaCliente': 44, 'IdCiudadCliente': 10, 'IdComunaCliente': 20, 'DireccionCliente': 'CALLE UNO',
         'NumeroDireccionCliente': '123', 'IdTipoCargoPersona': 1, 'TelefonoFijo': '', 'TelefonoMovil': '+56911112222', 'TelefonoLaboral': '',
         'EmailCliente': 'ana@example.invalid', 'RazonSocialEmpleador': '', 'IdMedioRecaudacionTrabajador': 1, 'IdEntidadRecaudadora': 1,
         'DocumentoIdentidadEmpleador': '', 'DireccionComercialEmpleador': '', 'NumeroDireccionEmpleador': '', 'IdCiudadEmpleador': 0, 'IdComunaEmpleador': 0,
         'TelefonoEmpleador': '', 'NombreEncargado': '', 'EmailEncargado': '', 'TelefonoEncargado': '', 'CodigoPrevision': 1, 'Pensionado': 'NO',
         'Trabajador': 'INDEPENDIENTE', 'FormaDePago': 'DIRECTA', 'IdMedioRecaudacionFormaPago': 1, 'DiaDescuento': 0, 'Institucion': '1',
         'ComisionTransferencia': 0, 'CodigoTipoSolicitud': '', 'TipoMovimiento': '', 'RazonRetiro': '', 'NombreEntidadOrigen': '', 'RutEntidadOrigen': '',
         'IdPersonaOrigen': 0, 'Descripcion': '', 'TipoDeAhorro': '', 'FondoDeOrigen': '', 'TipoTraspaso': '', 'MontoTraspaso': 0, 'UFTraspaso': 0,
         'PorcentajeTraspaso': 0, 'IdEstadoEnvioNotificacion': 'NEV', 'IdEstadoSolicitud': '', 'IdEstado': 'PDI', 'IdEmpleado': 335,
         'IdPropositoPortafolio': 'PROPOSITOAPV', 'IdMatrizAgrupadorAPV': 1, 'CodigoAgrupador': 'GRUPO 1'}
    d.update(kw)
    return {k: v for k, v in d.items() if v is not _NULO}


_NULO = object()


def pm(**kw):
    d = {'CodigoAgrupador': 'GRUPO 1', 'IdMatrizAgrupador': 1, 'OrigenAhorro': 'APV', 'IdPortafolio': 0, 'IdPropositoPortafolio': 'PROPOSITOAPV', 'MesInicio': '2025-03',
         'MesTermino': '9999-01', 'MontoPesos': 100000, 'MontoUF': 0, 'Pago': 'IND', 'PagoNombre': 'INDEFINIDO', 'PorcentajeRegimenImponible': 0,
         'Producto': 'APV FONDOS MUTUOS', 'Regimen': 'APV-A', 'IdInterno': 0}
    d.update(kw)
    return d


def di(**kw):
    d = {'IdContratoSAAPV': 0, 'FolioContratoSAAPV': 'F-G1', 'IdInstrumento': 1021327, 'Nemotecnico': '00391CCLP', 'MontoPesos': 100000, 'Porcentaje': 100,
         'IdEstado': 'VIG', 'IdInterno': 0}
    d.update(kw)
    return d


def dot(**kw):
    d = {'IdDetalleOrigenTraspaso': 0, 'IdTraspaso': 0, 'CodigoTipoAhorro': 'APV', 'IdEntidadRecaudadora': 831, 'NombreEntidad': 'AFP UNO', 'DocumentoEntidad': '76240079-0',
         'Fondo': 'A', 'MontoTraspaso': 5000000, 'UFTraspaso': 0, 'PorcentajeTraspaso': 100, 'IdEstado': 'VIG', 'IdInstrumento': 0, 'IdPortafolio': 0,
         'CodigoRegimenTributario': '0', 'IdInterno': 0}
    d.update(kw)
    return d


def ent(parametros, pms=None, dis=None, dots=None, tipo='I'):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV', 'p_Tipo': tipo, 'p_Parametros': parametros,
            'p_DistribucionInversion': [di()] if dis is None else dis, 'p_PortafoliosModificados': [pm()] if pms is None else pms,
            'p_DetalleOrigenTraspaso': dots or []}


PARTES = [persona(501, '11111111-1', idc=701), portafolio(901, 701), ins(T_TIPOIND, IdTipoIndicador=11, CodigoTipoIndicador='NOMBRE_ENCARGADO_RRHH', IdTipoEntidad='PERSONA',
                                                                       GrupoIndicador='DATOS EMPLEADOR'),
          ins(T_TIPOIND, IdTipoIndicador=12, CodigoTipoIndicador='MAIL_ENCARGADO_RRHH', IdTipoEntidad='PERSONA', GrupoIndicador='DATOS EMPLEADOR'),
          ins(T_TIPOIND, IdTipoIndicador=13, CodigoTipoIndicador='TELEFONO_ENCARGADO_RRHH', IdTipoEntidad='PERSONA', GrupoIndicador='DATOS EMPLEADOR')]
COLS_C = ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdPersona', 'IdCliente', 'IdEmpleador', 'IdEstado', 'IdEstadoSolicitud', 'IdEstadoEnvioNotificacion', 'IdPlan',
          'NombrePlan', 'FormaDePago', 'IdMedioRecaudacionFormaPago', 'Trabajador', 'CodigoTipoSolicitud', 'Canal', 'IdCargaMasiva', 'FechaSuscripcion',
          'TipoTraspaso', 'Institucion', 'DocumentoIdentidadCliente', 'IdCategoriaInversionista', 'IdPerfilComercial', 'IdTipoCargoPersona']
TG = [tabla(T_CONTRATO, '"IdContratoSAAPV"', COLS_C),
      tabla(T_PORCON, '"IdPortafolioContratoSAAPV"'),
      tabla(T_DISTR, '"IdDistribucionInversion"', excluir=['IdDistribucionInversion']),
      tabla(T_TRASPASO, '"IdTraspaso"'),
      tabla(T_DETORIGEN, '"IdDetalleOrigenTraspaso"')]
TG_CONTRATO = [TG[0]]
NUEVA = {'DocumentoIdentidadCliente': '33333333-3'}          # a document no seeded person has: ValidaContratoSAAPV finds nothing and the person is created
SIN_CLIENTE = {'DocumentoIdentidadCliente': '44444444-4'}   # a seeded person (503) that has no client
CPER_OK = {'P_OUT_REFID': 502, 'P_OUT_ERRORESCONTADOR': 0}
CCLI_OK = {'P_OUT_REFIDCLIENTE': 703, 'P_OUT_ERRORESCONTADOR': 0}


def col(**kw):
    d = {'SalidaProcedimiento': {SP_PORTAFOLIO: sp_ok(901)}}
    d.update(kw)
    return d


def err(codigo, desc='descripcion del error'):
    return {'Codigo': codigo, 'Descripcion': desc}


# =================================================================================================== client state, folio
caso(380, G, 'cliente-bloqueado-en-traspaso-de-ingreso', 'TRA INGRESO for a client in state BLO: CLIENTE_BLOQUEADO error with the document, the row is skipped',
     L(GF, r'String codMsg = "CLIENTE_BLOQUEADO"'),
     ent([par(CodigoTipoSolicitud='TRA', TipoMovimiento='INGRESO')]), seed(persona(501, '11111111-1', idc=701, estado='BLO'), portafolio(901, 701)),
     secuencias=SEQ, tablas=TG)
caso(381, G, 'cliente-bloqueado-sin-traspaso-no-bloquea', 'a BLO client in a normal contract (no TRA/INGRESO) is not blocked by this check', L(GF, r'"TRA".equalsIgnoreCase\(fila.CodigoTipoSolicitud\)'),
     ent([par()]), seed(persona(501, '11111111-1', idc=701, estado='BLO'), portafolio(901, 701), PARTES[2:]), config={}, colaboradores=col(), secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)
caso(382, G, 'folio-existente-sin-carga-masiva-es-error', 'the folio already exists, IdCargaMasiva absent: FOLIO_CONTRATO_EXISTE as ERROR, row skipped',
     L(GF, r'msg.tipo = fila.IdCargaMasiva== 0'), ent([par()]), seed(PARTES, contrato(9001, 'F-G1')), colaboradores=col(), secuencias=SEQ, tablas=TG)
caso(383, G, 'folio-existente-con-carga-masiva-es-advertencia', 'the folio already exists with IdCargaMasiva 7: the same message as ADVERTENCIA', L(GF, r'msg.tipo = fila.IdCargaMasiva== 0'),
     ent([par(IdCargaMasiva=7)]), seed(PARTES, contrato(9001, 'F-G1')), colaboradores=col(), secuencias=SEQ, tablas=TG)
caso(384, G, 'folio-existente-con-carga-masiva-cero-es-error', 'the folio already exists with IdCargaMasiva 0: ERROR', L(GF, r'msg.tipo = fila.IdCargaMasiva== 0'),
     ent([par(IdCargaMasiva=0)]), seed(PARTES, contrato(9001, 'F-G1')), colaboradores=col(), secuencias=SEQ, tablas=TG)

# =================================================================================================== the standard creation (everything already exists)
LHAPPY = L(GF, r'res = sql.batch\(con, SQL_CREAR_CONTRATOSAAPV', r'"CREAR_DISTRIBUCION_OK"')
caso(385, G, 'creacion-completa-con-todo-existente', 'a complete contract of an existing person/client/portfolio: contract inserted, portfolio created via the procedure, relation to the portfolio, distribution, and the state goes VIG through CambiarEstadoContratosSAAPV',
     LHAPPY, ent([par()]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(386, G, 'sin-distribucion-queda-con-advertencia-y-corta-el-ciclo', 'no distribution: the contract stays pending, "No posee distribucion inversion." and the loop BREAKS (the second contract is not processed)',
     L(GF, r'No posee distribucion inversion'),
     ent([par(), par(FolioContratoSAAPV='F-G2', Folio='F-G2', IdInterno=1)], pms=[pm(), pm(IdInterno=1)], dis=[]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)
caso(387, G, 'datos-minimos-incompletos-contrato-pendiente', 'missing mandatory data (no address): VALIDA_DATOS_MINIMOS_CONTRATO warnings, the contract stays PDI',
     L(GF, r'Contrato Ingresado con datos Pendientes de completar'),
     ent([par(DireccionCliente='')]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(388, G, 'dependiente-sin-empleador-advierte-trabajador-sin-empleador', 'a DEPENDIENTE worker without employer data: the extra "Trabajador DEPENDIENTE sin informar Empleador." warning',
     L(GF, r"Trabajador 'DEPENDIENTE' sin informar Empleador"),
     ent([par(Trabajador='DEPENDIENTE')]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(389, G, 'portafolio-con-errores-del-procedimiento-conserva-el-encontrado', 'the portfolio procedure answers an error: ERROR message with the procedure code (counted as error), the portfolio found by ValidaContratoSAAPV is still linked',
     L(GF, r'resultSP = sql.call\(con, SQL_CREAR_PORTAFOLIO_APV_Call'),
     ent([par()]), seed(PARTES),
     colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: {'p_out_RefId': 0, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': 1,
                                                          'p_out_ErroresDetalle': [err('PORTAFOLIO_NO_CREADO', 'no se pudo crear')], 'p_out_AdvertenciasDetalle': []}}},
     secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(390, G, 'portafolio-con-errores-en-carga-masiva-es-advertencia', 'the same procedure error with IdCargaMasiva 5: the message type is ADVERTENCIA',
     L(GF, r'if\(fila.IdCargaMasiva != null\) \{\s*$'),
     ent([par(IdCargaMasiva=5)]), seed(PARTES),
     colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: {'p_out_RefId': 0, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': 1,
                                                          'p_out_ErroresDetalle': [err('PORTAFOLIO_NO_CREADO', 'no se pudo crear')], 'p_out_AdvertenciasDetalle': []}}},
     secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(391, G, 'portafolio-con-advertencias-del-procedimiento', 'the portfolio procedure answers a warning only: ADVERTENCIA message, no reference taken',
     L(GF, r'advertenciaDetalle : \(List<AdvertenciaDetalleCall>\) resultSP.get\("p_out_AdvertenciasDetalle"\)\)\{\s*$'),
     ent([par()]), seed(PARTES),
     colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: {'p_out_RefId': 0, 'p_out_AdvertenciasContador': 1, 'p_out_ErroresContador': 0, 'p_out_ErroresDetalle': [],
                                                          'p_out_AdvertenciasDetalle': [{'Codigo': 'PORTAFOLIO_YA_EXISTE', 'RequiereAutorizacion': 'N', 'Descripcion': 'ya existe'}]}}},
     secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(392, G, 'sin-portafolios-no-crea-plan-de-ahorro', 'no portfolio rows: "No es posible Crear Plan de Ahorro (Portafolio)" warning and the contract is still inserted',
     L(GF, r'No es posible Crear Plan de Ahorro'),
     ent([par()], pms=[]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(393, G, 'portafolio-sin-id-no-se-vincula', 'the portfolio of the row has no id after the procedure (reference 0): ERROR_CREAR_PORTAFOLIO_CONTRATO (must match plan and portfolio of the client)',
     L(GF, r'Debe coincidir el plan de ahorro'),
     ent([par(), ], pms=[pm(Regimen='APV-B', CodigoAgrupador='GRUPO 1')]), seed(PARTES), colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: sp_ok(0)}}, secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)
caso(394, G, 'insercion-fallida-devuelve-error-de-contrato', 'the contract insert fails (folio longer than the column): ERROR_CREAR_CONTRATO and the method returns at once',
     L(GF, r'ERROR_CREAR_CONTRATO'),
     ent([par(FolioContratoSAAPV='F' * 120, Folio='F' * 120)]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)

# =================================================================================================== creation of the person
PER_NUEVA = [ins(T_PERSONA, IdPersona=502, Nombres='ANA', ApellidoPaterno='PEREZ', ApellidoMaterno='SOTO', FechaNacimiento='1980-05-10', EstadoCivil='SO', Sexo='F',
                 RazonSocial='', IdPaisResidencia=44, IdDocumentoIdentidad=5502, IdDireccionFisicaPrincipal=6502, IdPersonaEmpleador=None, IdTelefonoPrincipal=7502, Tipo='N',
                 IdProfesion=0),
             ins(V_DIRFIS, IdDireccionFisica=6502, IdPersona=502, IdEstado='ACT'),
             ins(T_DIRFIS, Calle='CALLE UNO', Numero='123', IdDireccionFisica=6502, IdDivisionPais=20, IdCiudad=10, IdPersona=502, CodigoProposito='PART', IdEstado='ACT',
                 Predeterminado='S')]
LPER = L(GF, r'if\(fila.IdPersona == null \|\| fila.IdPersona == 0\)', r'CREACION_PERSONA_OK')
caso(395, G, 'crea-la-persona-con-telefono-email-direccion-y-cargo', 'IdPersona 0: CrearPersona answers REFID 502; the address with purpose PREV is copied, the post is changed, phone (movil) and email are created, CREACION_PERSONA_OK',
     LPER, ent([par(IdPersona=0, IdCliente=0, **NUEVA)]), seed(persona(501, '11111111-1', idc=701), portafolio(901, 701), PER_NUEVA),
     colaboradores=col(CrearPersona=CPER_OK, CrearCliente=CCLI_OK), secuencias=SEQ,
     tablas=TG + [tabla(T_DIRFIS, '"IdDireccionFisica"', ['IdDireccionFisica', 'IdPersona', 'CodigoProposito', 'Calle'])] +
     [tabla(T_PERSONA, '"IdPersona"', ['IdPersona', 'IdProfesion'])], enmascarar_colaboradores=MASCARA)
caso(396, G, 'persona-existe-es-advertencia', 'CrearPersona answers PERSONA_EXISTE: ADVERTENCIA PERSONA_EXISTE (the creation goes on)', L(GF, r'msg.codigo = "PERSONA_EXISTE"'),
     ent([par(IdPersona=0, IdCliente=0, **NUEVA)]), seed(PARTES),
     colaboradores=col(CrearPersona={'P_OUT_REFID': 0, 'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [err('PERSONA_EXISTE')]}, CrearCliente=CCLI_OK), secuencias=SEQ,
     tablas=TG, enmascarar_colaboradores=MASCARA)
caso(397, G, 'persona-otro-error-es-advertencia-error-crear-persona', 'CrearPersona answers another error: ADVERTENCIA ERROR_CREAR_PERSONA', L(GF, r'msg.codigo = "ERROR_CREAR_PERSONA";\s*$'),
     ent([par(IdPersona=0, IdCliente=0, **NUEVA)]), seed(PARTES),
     colaboradores=col(CrearPersona={'P_OUT_REFID': 0, 'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [err('PERSONA_INVALIDA')]}, CrearCliente=CCLI_OK), secuencias=SEQ,
     tablas=TG, enmascarar_colaboradores=MASCARA)
caso(398, G, 'telefonos-y-email-con-error', 'CrearPersona OK but CrearTelefono and CrearDireccionElectronica answer errors: ERROR_CREAR_TELEFONO / ERROR_CREAR_EMAIL (except the UI_ERROR_AL_CREAR_* codes, which are only logged)',
     L(GF, r'ERROR_CREAR_TELEFONO'),
     ent([par(IdPersona=0, IdCliente=0, TelefonoFijo='+56222223333', **NUEVA)]), seed(PARTES, PER_NUEVA),
     colaboradores=col(CrearPersona=CPER_OK, CrearCliente=CCLI_OK,
                       CrearTelefono=[{'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [err('TELEFONO_INVALIDO')]},
                                      {'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [err('UI_ERROR_AL_CREAR_TELEFONO')]}],
                       CrearDireccionElectronica={'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [err('EMAIL_INVALIDO')]}), secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)
caso(399, G, 'sin-cargo-no-actualiza-la-persona', 'IdTipoCargoPersona 0: the post is not changed', L(GF, r'if\(fila.IdTipoCargoPersona != 0\)'),
     ent([par(IdPersona=0, IdCliente=0, IdTipoCargoPersona=0, **NUEVA)]), seed(PARTES, PER_NUEVA), colaboradores=col(CrearPersona=CPER_OK, CrearCliente=CCLI_OK), secuencias=SEQ,
     tablas=TG + [tabla(T_PERSONA, '"IdPersona"', ['IdPersona', 'IdProfesion'])], enmascarar_colaboradores=MASCARA)
caso(400, G, 'cargo-no-actualizable-es-error', 'the post update affects no row (the new person is not in PER.Persona): CAMBIAR_CARGO_PERSONA_ERROR',
     L(GF, r'CAMBIAR_CARGO_PERSONA_ERROR'),
     ent([par(IdPersona=0, IdCliente=0, **NUEVA)]), seed(PARTES), colaboradores=col(CrearPersona={'P_OUT_REFID': 999, 'P_OUT_ERRORESCONTADOR': 0}, CrearCliente=CCLI_OK), secuencias=SEQ,
     tablas=TG, enmascarar_colaboradores=MASCARA)
caso(401, G, 'cargo-nulo-lanza-NPE', 'IdTipoCargoPersona null in the JSON: fila.IdTipoCargoPersona != 0 throws NullPointerException after CrearPersona',
     L(GF, r'if\(fila.IdTipoCargoPersona != 0\)'),
     ent([par(IdPersona=0, IdCliente=0, IdTipoCargoPersona=_NULO, **NUEVA)]), seed(PARTES, PER_NUEVA), colaboradores=col(CrearPersona=CPER_OK, CrearCliente=CCLI_OK), secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)

# =================================================================================================== employer
LEMP = L(GF, r'if\(\(fila.IdEmpleador == null \|\| fila.IdEmpleador == 0\) && !fila.DocumentoIdentidadEmpleador.equals', r'SQL.exec\(con, "DROP TABLE #RegistroAtributo"\)')
EMP = dict(DocumentoIdentidadEmpleador='76000000-0', RazonSocialEmpleador='EMPRESA UNO', DireccionComercialEmpleador='AV UNO', NumeroDireccionEmpleador='10',
           Trabajador='DEPENDIENTE', NombreEncargado='JUAN', EmailEncargado='j@example.invalid', TelefonoEncargado='+56933334444', IdPaisEmpleador=44)
caso(402, G, 'crea-el-empleador-y-sus-indicadores-de-rrhh', 'IdEmpleador 0 with a document: CrearPersona (juridica), then the RRHH indicators procedure with the three attributes, then the person-employer relation',
     LEMP, ent([par(**EMP)]), seed(PARTES),
     colaboradores=col(CrearPersona={'P_OUT_REFID': 802, 'P_OUT_ERRORESCONTADOR': 0},
                       SalidaProcedimiento={SP_PORTAFOLIO: sp_ok(901), SP_INDICADOR: sp_ok(0), SP_RELACION: sp_ok(77)}), secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)
caso(403, G, 'encargado-vacio-escribe-N-en-los-indicadores', 'the three RRHH fields empty: every indicator value is "N"', L(GF, r'indicadoresEmpleadoSonVacio'),
     ent([par(**dict(EMP, NombreEncargado='', EmailEncargado='', TelefonoEncargado=''))]), seed(PARTES),
     colaboradores=col(CrearPersona={'P_OUT_REFID': 802, 'P_OUT_ERRORESCONTADOR': 0},
                       SalidaProcedimiento={SP_PORTAFOLIO: sp_ok(901), SP_INDICADOR: sp_ok(0), SP_RELACION: sp_ok(77)}), secuencias=SEQ, tablas=TG_CONTRATO,
     enmascarar_colaboradores=MASCARA)
caso(404, G, 'indicadores-rrhh-con-errores-y-advertencias', 'the indicators procedure answers an error and a warning: ERROR_ENCARGADO_RRHH and ADVERTENCIA_ENCARGADO_RRHH', L(GF, r'ERROR_ENCARGADO_RRHH'),
     ent([par(**EMP)]), seed(PARTES),
     colaboradores=col(CrearPersona={'P_OUT_REFID': 802, 'P_OUT_ERRORESCONTADOR': 0},
                       SalidaProcedimiento={SP_PORTAFOLIO: sp_ok(901),
                                            SP_INDICADOR: {'p_out_ErroresContador': 1, 'p_out_AdvertenciasContador': 1, 'p_out_ErroresDetalle': [err('IND_ERROR')],
                                                           'p_out_AdvertenciasDetalle': [{'Codigo': 'IND_AVISO', 'RequiereAutorizacion': 'N', 'Descripcion': 'aviso'}]},
                                            SP_RELACION: sp_ok(77)}), secuencias=SEQ, tablas=TG_CONTRATO, enmascarar_colaboradores=MASCARA)
caso(405, G, 'empleador-existe-y-error-de-creacion', 'employer creation answers PERSONA_EXISTE and another error: ADVERTENCIA PERSONA_EMPLEADOR_EXISTE and ERROR ERROR_CREAR_PERSONA', L(GF, r'PERSONA_EMPLEADOR_EXISTE'),
     ent([par(**EMP)]), seed(PARTES),
     colaboradores=col(CrearPersona={'P_OUT_REFID': 0, 'P_OUT_ERRORESCONTADOR': 2, 'P_OUT_ERRORESDETALLE': [err('PERSONA_EXISTE'), err('OTRO')]}), secuencias=SEQ,
     tablas=TG_CONTRATO, enmascarar_colaboradores=MASCARA)

# =================================================================================================== client
LCLI = L(GF, r'if\(fila.IdCliente == null \|\| fila.IdCliente == 0\)\{', r'CLIENTE_NO_PUEDE_CREAR_PORTAFOLIO')
caso(406, G, 'crea-el-cliente', 'IdCliente 0 with the minimum client data: CrearCliente answers 703, CREACION_CLIENTE_OK', LCLI,
     ent([par(IdCliente=0, **SIN_CLIENTE)]), seed(PARTES, persona(503, '44444444-4')), colaboradores=col(CrearCliente=CCLI_OK), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(407, G, 'datos-minimos-de-cliente-incompletos', 'IdCliente 0 but the client data is incomplete (no email): VALIDA_DATOS_MINIMOS_CLIENTE, CrearCliente is not called', L(GF, r'No es posible completar la creacion del cliente por datos faltantes'),
     ent([par(IdCliente=0, EmailCliente='', **SIN_CLIENTE)]), seed(PARTES, persona(503, '44444444-4')), colaboradores=col(CrearCliente=CCLI_OK), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(408, G, 'cliente-errores-de-creacion-por-codigo', 'CrearCliente answers four errors (CLIENTE_EXISTE, PERFIL_COMERCIAL_NO_EXISTE, CLIENTE_NO_PUEDE_SER_ENROLADO, other): the first two and the last are reported, the third builds a message that is never added',
     L(GF, r'lineaError.Codigo.equals\("CLIENTE_EXISTE"\)'),
     ent([par(IdCliente=0, **SIN_CLIENTE)]), seed(PARTES, persona(503, '44444444-4')),
     colaboradores=col(CrearCliente={'P_OUT_REFIDCLIENTE': 0, 'P_OUT_ERRORESCONTADOR': 4,
                                     'P_OUT_ERRORESDETALLE': [err('CLIENTE_EXISTE'), err('PERFIL_COMERCIAL_NO_EXISTE'), err('CLIENTE_NO_PUEDE_SER_ENROLADO'), err('OTRO')]}),
     secuencias=SEQ, tablas=TG_CONTRATO, enmascarar_colaboradores=MASCARA)

# =================================================================================================== relation person-employer
LREL = L(GF, r'Valida la existencia de la relacion de la persona con el empleador', r'SalidaCliente.addAdvertencia\(advertenciaDetalle.Codigo')
caso(409, G, 'crea-la-relacion-persona-empleador', 'an employer is known and the relation does not exist: the CrearRelacionPersona procedure is called (EMPDOR, FechaFin from the default parameter)', LREL,
     ent([par(IdEmpleador=801, DocumentoIdentidadEmpleador='76000000-0', RazonSocialEmpleador='EMPRESA UNO', Trabajador='DEPENDIENTE')]),
     seed(PARTES, persona(801, '76000000-0', nombres='', paterno='', materno='', tipo='J')),
     colaboradores=col(SalidaProcedimiento={SP_PORTAFOLIO: sp_ok(901), SP_RELACION: sp_ok(77)}), secuencias=SEQ, tablas=TG_CONTRATO, enmascarar_colaboradores=MASCARA)
caso(410, G, 'relacion-existente-no-se-vuelve-a-crear', 'the relation already exists in the relation view: the procedure is not called', L(GF, r'existeRelacionPersona = \(ConsultaRelacion.isEmpty'),
     ent([par(IdEmpleador=801, DocumentoIdentidadEmpleador='76000000-0', RazonSocialEmpleador='EMPRESA UNO', Trabajador='DEPENDIENTE')]),
     seed(PARTES, persona(801, '76000000-0', nombres='', paterno='', materno='', tipo='J'), ins(V_REL, IdRelacionPersona=1, IdPersonaOrigen=501, IdPersonaDestino=801)),
     colaboradores=col(SalidaProcedimiento={SP_PORTAFOLIO: sp_ok(901), SP_RELACION: sp_ok(77)}), secuencias=SEQ, tablas=TG_CONTRATO, enmascarar_colaboradores=MASCARA)
caso(411, G, 'relacion-con-errores-y-advertencias', 'the relation procedure answers an error and a warning: ERROR and ADVERTENCIA messages with the procedure codes', L(GF, r'msg.codigo = errorDetalle.Codigo'),
     ent([par(IdEmpleador=801, DocumentoIdentidadEmpleador='76000000-0', RazonSocialEmpleador='EMPRESA UNO', Trabajador='DEPENDIENTE')]),
     seed(PARTES, persona(801, '76000000-0', nombres='', paterno='', materno='', tipo='J')),
     colaboradores=col(SalidaProcedimiento={SP_PORTAFOLIO: sp_ok(901),
                                            SP_RELACION: {'p_out_RefId': 0, 'p_out_ErroresContador': 1, 'p_out_AdvertenciasContador': 1, 'p_out_ErroresDetalle': [err('REL_ERROR', 'error de relacion')],
                                                          'p_out_AdvertenciasDetalle': [{'Codigo': 'REL_AVISO', 'RequiereAutorizacion': 'N', 'Descripcion': 'aviso de relacion'}]}}),
     secuencias=SEQ, tablas=TG_CONTRATO, enmascarar_colaboradores=MASCARA)

# =================================================================================================== closing of duplicate contracts
V_EXISTE = [contrato(9001, 'F-OLD', IdEstado='VIG', FormaDePago='DIRECTA', IdMedioRecaudacionFormaPago=1, IdEmpleador=0)]
LCER = L(GF, r'SQL.exec\(con, SQL_CREAR_ENTRADA_TEMPORAL\)', r'SQL.exec\(con, SQL_ELIMINAR_ENTRADA_TEMPORAL\)')


def viejo(pago, ini='2025-03', idpc=8801, medio=1, forma='DIRECTA', emp=0, porcon_regimen=True):
    out = [contrato(9001, 'F-OLD', IdEstado='VIG', FormaDePago=forma, IdMedioRecaudacionFormaPago=medio, IdEmpleador=emp),
           porcon(idpc, 901, 9001, pago=pago, ini=ini)]
    return out


DIS_VIEJA = [ins(T_DISTR, IdDistribucionInversion=1, IdContratoSAAPV=9001, FolioContratoSAAPV='F-OLD', IdInstrumento=1, Nemotecnico='X', MontoPesos=1, Porcentaje=100, IdEstado='VIG')]
CERRAR_TG = TG[:3]
caso(412, G, 'cierra-el-contrato-indefinido-equivalente', 'an existing VIG contract with Pago IND, same forma de pago/medio/employer and the same portfolios: it is closed (CER) with its distribution',
     LCER, ent([par()]), seed(PARTES, viejo('IND'), DIS_VIEJA, ins('BSCL.APV.BD::APV.ProductoAgrupador', IdProductoAgrupador=1, NombreProductoAgrupador='APV FONDOS MUTUOS', IdEstado='VIG')),
     colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(413, G, 'indefinido-con-otro-empleador-no-se-cierra', 'an existing IND contract with a different employer is not closed', L(GF, r'contratoExistente.IdEmpleador.equals\(fila.IdEmpleador\)'),
     ent([par()]), seed(PARTES, viejo('IND', emp=801)), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(414, G, 'unico-directo-mismo-medio-no-se-cierra', 'an existing UNI contract with the same start month, DIRECTA and the same medio (not PAC): not closed (debeCerrarContratoUnico false)',
     L(GF, r'private Boolean debeCerrarContratoUnico'),
     ent([par(), ], pms=[pm(Pago='UNI', PagoNombre='UNICO')]), seed(PARTES, viejo('UNI')), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(415, G, 'unico-con-otro-medio-se-cierra', 'an existing UNI contract with the same start month but another medio: closed', L(GF, r'"UNI".equalsIgnoreCase\(linea.Pago\) && linea.PeriodoInicio'),
     ent([par(IdMedioRecaudacionFormaPago=2)], pms=[pm(Pago='UNI', PagoNombre='UNICO')]), seed(PARTES, viejo('UNI', medio=2)),
     colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(416, G, 'temporal-igual-forma-de-pago-se-cierra', 'an existing TEM (neither UNI nor IND) contract with the same pago type text as the new one is closed without more checks',
     L(GF, r'!"UNI".equalsIgnoreCase\(linea.Pago\) && !\("IND"\)'),
     ent([par()], pms=[pm(Pago='TEM', PagoNombre='TEMPORAL', MesTermino='2025-12')]), seed(PARTES, viejo('TEM')), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(417, G, 'pago-distinto-no-se-toca', 'an existing contract with another Pago than the new one is left alone', L(GF, r'linea.Pago.equalsIgnoreCase\(in.p_PortafoliosModificados.get\(0\).Pago\)'),
     ent([par()]), seed(PARTES, viejo('TEM')), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)

# =================================================================================================== modification (p_Tipo = M)
LMOD = L(GF, r'if\(in.p_Tipo.compareTo\("M"\) == 0\)', r'else \{\s*$')
caso(418, G, 'modificacion-elimina-el-contrato-pendiente-anterior', 'p_Tipo M: the previous contract (PDI) is deleted with its distribution and a delete log is written; no closing of duplicates happens',
     LMOD, ent([par(IdContratoSAAPV=9001)], tipo='M'),
     seed(PARTES, contrato(9001, 'F-OLD', IdEstado='PDI'), DIS_VIEJA), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(419, G, 'modificacion-conserva-el-contrato-vigente-anterior', 'p_Tipo M: a previous contract that is VIG is not deleted', LMOD, ent([par(IdContratoSAAPV=9001)], tipo='M'),
     seed(PARTES, contrato(9001, 'F-OLD', IdEstado='VIG'), DIS_VIEJA), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)
caso(420, G, 'tipo-nulo-lanza-NPE-despues-del-insert', 'p_Tipo null: after the contract insert in.p_Tipo.compareTo throws NullPointerException (everything is rolled back)', L(GF, r'in.p_Tipo.compareTo\("M"\) == 0'),
     {k: v for k, v in ent([par()]).items() if k != 'p_Tipo'}, seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=CERRAR_TG, enmascarar_colaboradores=MASCARA)

# =================================================================================================== distribution, ids, several contracts
LDIS = L(GF, r'if\(in.p_DistribucionInversion.size\(\) > 0\)\{\s*$', r'ERROR_CREAR_DISTRIBUCION')
caso(421, G, 'distribucion-filtra-por-id-interno-e-instrumento', 'only the distribution rows of the contract (same IdInterno) and with IdInstrumento != 0 are inserted',
     LDIS, ent([par()], dis=[di(), di(IdInstrumento=0, Nemotecnico='CERO'), di(IdInterno=5, Nemotecnico='OTRO')]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG,
     enmascarar_colaboradores=MASCARA)
caso(422, G, 'dos-contratos-usan-ids-consecutivos', 'two contracts in one call: each takes its own value from the sequence, with its own portfolio, distribution and relations',
     L(GF, r'contadorIds \+\+;'),
     ent([par(), par(FolioContratoSAAPV='F-G2', Folio='F-G2', IdInterno=1)], pms=[pm(), pm(IdInterno=1)], dis=[di(), di(IdInterno=1, FolioContratoSAAPV='F-G2')]),
     seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(423, G, 'lista-vacia-no-hace-nada', 'no contracts: empty output, no inserts', L(GF, r'in.p_Parametros.size\(\)\);'),
     ent([]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG)
caso(424, G, 'lista-nula-lanza-NPE', 'p_Parametros null in the JSON: in.p_Parametros.size() throws NullPointerException', L(GF, r'in.p_Parametros.size\(\)\);'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV', 'p_Tipo': 'I', 'p_Parametros': None}, seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG)
caso(425, G, 'tipo-de-solicitud-ausente-lanza-NPE', 'a row without CodigoTipoSolicitud: fila.CodigoTipoSolicitud.equals("") throws NullPointerException',
     L(GF, r'if\(fila.CodigoTipoSolicitud.equals\(""\) \|\| \(fila.CodigoTipoSolicitud.equals\("TRA"\) && fila.TipoMovimiento.equals\("INGRESO"\)\)\)\{\s*$'),
     ent([par(CodigoTipoSolicitud=_NULO)]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)

# =================================================================================================== traspaso (CodigoTipoSolicitud = TRA)
LTRA = L(GF, r'INICIO - CREACION DE TRASPASO', r'FIN - CREACION DE TRASPASO')
TRA_IN = dict(CodigoTipoSolicitud='TRA', TipoMovimiento='INGRESO', TipoTraspaso='TOT', NombreEntidadOrigen='AFP UNO', RutEntidadOrigen='76240079-0', TipoDeAhorro='APV',
              IdPersonaOrigen=61704, IdEstado='PDI')
caso(426, G, 'traspaso-de-ingreso-completo-queda-pendiente-de-ingreso', 'TRA INGRESO with complete client data: contract inserted, traspaso created PDI with its origin detail, no VIG change',
     LTRA, ent([par(**TRA_IN)], pms=[pm()], dis=[], dots=[dot()]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
caso(427, G, 'traspaso-de-ingreso-con-datos-faltantes-queda-TPD', 'TRA INGRESO with missing client data (no address): the traspaso state is TPD', L(GF, r'Validatraspaso.size\(\) == 0 \? "PDI" : "TPD"'),
     ent([par(**dict(TRA_IN, DireccionCliente=''))], dis=[], dots=[dot()]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=TG, enmascarar_colaboradores=MASCARA)
EGR = dict(CodigoTipoSolicitud='TRA', TipoMovimiento='EGRESO', TipoTraspaso='TOT', NombreEntidadOrigen='AFP UNO', RutEntidadOrigen='76240079-0', TipoDeAhorro='APV')
caso(428, G, 'traspaso-de-egreso-sin-aprobacion-automatica-pide-autorizacion', 'TRA EGRESO with APV.ApruebaTraspasoAutomatico N: traspaso PDA, MODIFICA_ESTADO_OK, an authorization event INGTRA is created',
     L(GF, r'ApruebaTraspasoAutomatico.equals\("N"\)'), ent([par(**EGR)], dis=[], pms=[], dots=[dot()]), seed(PARTES),
     config={'APV/APV.ApruebaTraspasoAutomatico': 'N'}, colaboradores=col(), secuencias=SEQ, tablas=[TG[3], TG[4]], enmascarar_colaboradores=MASCARA)
caso(429, G, 'traspaso-de-egreso-con-aprobacion-automatica-queda-ING', 'TRA EGRESO with automatic approval: the traspaso goes to ING, no event, contract id 0 in the traspaso',
     L(GF, r'tm.IdEstado = "ING"'), ent([par(**EGR)], dis=[], pms=[], dots=[dot()]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=[TG[0], TG[3], TG[4]], enmascarar_colaboradores=MASCARA)
caso(430, G, 'traspaso-sin-detalle-con-documento-no-inserta-origen', 'origin rows with an empty DocumentoEntidad are skipped: only the traspaso is created', L(GF, r'!row.DocumentoEntidad.equals\(""\)'),
     ent([par(**EGR)], dis=[], pms=[], dots=[dot(DocumentoEntidad='')]), seed(PARTES), colaboradores=col(), secuencias=SEQ, tablas=[TG[3], TG[4]], enmascarar_colaboradores=MASCARA)
