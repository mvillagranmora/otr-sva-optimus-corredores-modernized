"""Family C cases: CargaMasivaSAAPVPAC (APV-C-200 .. 209). The creation path runs the real CargaMasivaArchivo -> GrabarContratoSAAPV (shapes copied from family A's fixtures, fake data)."""
from fix import T_CARGA, T_CONTRATO, T_PORCON, T_PORTAFOLIO, T_CLIENTE_SN, T_DOC, T_PERSONA, T_V_CLIENTE, ins
from lib import L, caso, seed, tabla

F = 'CargaMasivaSAAPVPAC.java'
T_BANCO = 'BSCL.Plataforma.BD::BAN.Banco'
T_CTA = 'BSCL.SocioNegocio.BD::PER.CuentaBancaria'
T_TIPOIND = 'BSCL.Plataforma.BD::IND.TipoIndicador'
SEQ = {'BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV': 9500, 'BSCL.APV.BD.Secuencias::TRA.IdTraspaso': 6000, 'BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso': 6500,
       'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva': 300}
SP = {'StoredProcedure': {'BSCL.Portafolio.BD.Servicios::CrearPortafolio': {'salida': {'p_out_RefId': 901, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': 0, 'p_out_ErroresDetalle': [],
                                                                                        'p_out_AdvertenciasDetalle': []}}}}
CTA_NUEVA = {'CrearPersonaCuentaBancaria': {'salida': {'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_MENSAJES': [], 'P_OUT_REFID': [9100], 'P_OUT_CUENTABANCARIA': [{'IdInterno': 7, 'IdCuentaBancaria': 9100}]}}}
MANDATO = {'IngresarMandatoPAC': {'salida': {'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_MENSAJES': [], 'P_OUT_RESULTADO': [{'IdMandatoPAC': 8800}]}}}
CFG = {'APV/APV.ApruebaTraspasoAutomatico': 'S', 'APV/APV.CambioPlanRequiereAutorizacion': 'N', 'PLATAFORMA/APV.Habilitado': 'N', 'PLATAFORMA/APV.ContratoCLI_IdTipoClienteDefecto': 'NATURAL',
       'PLATAFORMA/APV.ContratoFechaFinRelacionDefecto': '2099-12-31'}
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"')
TCON = tabla(T_CONTRATO, '"IdContratoSAAPV"', columnas=['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdEstado', 'IdPersona', 'IdCliente'])
TCTA = tabla(T_CTA, '"IdCuentaBancaria"')
MASK = ['*IdTrabajo']


def archivo(n=1):
    return [{'IdEmpresa': 1, 'NombreCargaMasiva': 'carga-pac-ficticia.csv', 'CantidadRegistros': n, 'IdUsuario': 21, 'IdTipoCarga': 1, 'Descripcion': 'carga de prueba', 'IdEstado': 'ING',
             'FechaCarga': '2025-03-31'}]


def contrato(folio='1001', idinterno=1):
    return {'IdInterno': idinterno, 'IdEmpresa': 1, 'FolioContratoSAAPV': folio, 'IdPlan': 1, 'NombrePlan': 'ABIERTO', 'FechaSuscripcion': '2025-03-01', 'FechaCreacion': '2025-03-01',
            'FechaAprobacion': '0001-01-01', 'FechaNotificacion': '0001-01-01', 'FechaRecepcion': '0001-01-01', 'Folio': folio, 'IdPersona': 501, 'IdEmpleador': 0, 'IdCliente': 701,
            'IdCategoriaInversionista': 'MODERADO', 'IdPerfilComercial': 'PERFIL BASE', 'NombreCliente': 'ANA', 'ApellidoPaternoCliente': 'PEREZ', 'ApellidoMaternoCliente': 'SOTO',
            'DocumentoIdentidadCliente': '11111111-1', 'FechaNacimientoCliente': '1980-05-10', 'EstadoCivil': 'SO', 'IdTipoGenero': 'F', 'IdPaisResidenciaCliente': 44, 'IdCiudadCliente': 10,
            'IdComunaCliente': 20, 'DireccionCliente': 'CALLE UNO', 'NumeroDireccionCliente': '123', 'IdTipoCargoPersona': 1, 'TelefonoFijo': '', 'TelefonoMovil': '+56911112222',
            'TelefonoLaboral': '', 'EmailCliente': 'ana@example.invalid', 'RazonSocialEmpleador': '', 'IdMedioRecaudacionTrabajador': 1, 'IdEntidadRecaudadora': 1,
            'DocumentoIdentidadEmpleador': '', 'DireccionComercialEmpleador': '', 'NumeroDireccionEmpleador': '', 'IdCiudadEmpleador': 0, 'IdComunaEmpleador': 0, 'TelefonoEmpleador': '',
            'NombreEncargado': '', 'EmailEncargado': '', 'TelefonoEncargado': '', 'CodigoPrevision': 1, 'Pensionado': 'NO', 'Trabajador': 'INDEPENDIENTE', 'FormaDePago': 'DIRECTA',
            'IdMedioRecaudacionFormaPago': 1, 'DiaDescuento': 0, 'Institucion': '1', 'ComisionTransferencia': 0, 'CodigoTipoSolicitud': '', 'TipoMovimiento': '', 'RazonRetiro': '',
            'NombreEntidadOrigen': '', 'RutEntidadOrigen': '', 'IdPersonaOrigen': 0, 'Descripcion': '', 'TipoDeAhorro': '', 'FondoDeOrigen': '', 'TipoTraspaso': '', 'MontoTraspaso': 0,
            'UFTraspaso': 0, 'PorcentajeTraspaso': 0, 'IdEstadoEnvioNotificacion': 'NEV', 'IdEstadoSolicitud': '', 'IdEstado': 'PDI', 'IdEmpleado': 335, 'IdPropositoPortafolio': 'PROPOSITOAPV',
            'IdMatrizAgrupadorAPV': 1, 'CodigoAgrupador': 'GRUPO 1'}


def distribucion(folio='1001', idinterno=1):
    return {'IdContratoSAAPV': 0, 'FolioContratoSAAPV': folio, 'IdInstrumento': 1021327, 'Nemotecnico': '00391CCLP', 'MontoPesos': 100000, 'Porcentaje': 100, 'IdEstado': 'VIG', 'IdInterno': idinterno}


def portafolio_mod(idinterno=1):
    return {'CodigoAgrupador': 'GRUPO 1', 'IdMatrizAgrupador': 1, 'OrigenAhorro': 'APV', 'IdPortafolio': 0, 'IdPropositoPortafolio': 'PROPOSITOAPV', 'MesInicio': '2025-03', 'MesTermino': '9999-01',
            'MontoPesos': 100000, 'MontoUF': 0, 'Pago': 'IND', 'PagoNombre': 'INDEFINIDO', 'PorcentajeRegimenImponible': 0, 'Producto': 'APV FONDOS MUTUOS', 'Regimen': 'APV-A', 'IdInterno': idinterno}


def pac(folio=1001, idinterno=1, cuenta='0012345678', banco='016', folio_pac=7):
    return {'NumeroCuentaBancaria': cuenta, 'CodigoBanco': banco, 'FolioSAAPV': folio, 'FolioPAC': folio_pac, 'Receptor': 'SOCIO', 'NumeroMandato': 55, 'Firma': 'S', 'IdentServicio': 'SERV1',
           'IdInterno': idinterno}


def ent(archivos=None, contratos=None, dist=None, pmod=None, pacs=None):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV', 'p_NombreCarga': 'carga-pac-ficticia.csv', 'p_Origen': 'PAC', 'p_IdEmpleado': 7312,
            'p_Archivo': archivo() if archivos is None else archivos, 'p_ParametrosContratos': [] if contratos is None else contratos, 'p_DistribucionInversion': [] if dist is None else dist,
            'p_PortafoliosModificados': [] if pmod is None else pmod, 'p_DetalleOrigenTraspaso': [], 'p_InContratoPAC': [] if pacs is None else pacs}


# persona 501 / cliente 701 / portafolio 901 of the contracts, the indicator types GrabarContratoSAAPV reads and the bank (shapes of family A's fixtures)
PERSONA = [ins(T_PERSONA, IdPersona=501, Nombres='ANA', ApellidoPaterno='PEREZ', ApellidoMaterno='SOTO', FechaNacimiento='1980-05-10', EstadoCivil='SO', Sexo='F', RazonSocial='',
               IdPaisResidencia=44, IdDocumentoIdentidad=5501, IdDireccionFisicaPrincipal=6501, IdPersonaEmpleador=None, IdTelefonoPrincipal=7501, Tipo='N'),
           ins(T_DOC, DocumentoIdentidad='11111111-1', IdDocumentoIdentidad=5501, IdPersona=501, Predeterminado='S'),
           ins(T_CLIENTE_SN, IdCategoriaInversionista='MODERADO', IdPersona=501, IdCliente=701, IdEstado='ACT', Borrado='N', IdPersonaEjecutivo=None),
           ins(T_PORTAFOLIO, CodigoPortafolio='APV-A', IdMatrizAgrupadorAPV=1, IdPortafolio=901, IdEstado='ACT', IdEmpleadoEjecutivo=335, IdPerfilComercialBaseOrigen='PERFIL BASE', IdCliente=701,
               Proposito='PROPOSITOAPV', IdContratoSAAPV=None),
           ins(T_TIPOIND, IdTipoIndicador=11, CodigoTipoIndicador='NOMBRE_ENCARGADO_RRHH', IdTipoEntidad='PERSONA', GrupoIndicador='DATOS EMPLEADOR'),
           ins(T_TIPOIND, IdTipoIndicador=12, CodigoTipoIndicador='MAIL_ENCARGADO_RRHH', IdTipoEntidad='PERSONA', GrupoIndicador='DATOS EMPLEADOR'),
           ins(T_TIPOIND, IdTipoIndicador=13, CodigoTipoIndicador='TELEFONO_ENCARGADO_RRHH', IdTipoEntidad='PERSONA', GrupoIndicador='DATOS EMPLEADOR')]
BANCO = ins(T_BANCO, IdBanco=16, CodigoBanco='016', IdEmpresa=1, IdEstado='ACT', IdPersona=3016)
EXISTENTE = [ins(T_CONTRATO, IdContratoSAAPV=9001, IdEmpresa=1, FolioContratoSAAPV='2002', IdPersona=501, IdCliente=701, IdEstado='VIG'),
             ins(T_PORCON, IdPortafolioContratoSAAPV=8001, IdPortafolio=901, IdContratoSAAPV=9001, IdEstado='VIG', OrigenAhorro='APV', MontoPesos=1, MontoUF=0, PorcentajeRegimenImponible=0,
                 Pago='IND', PeriodoInicio='2025-03', PeriodoTermino='9999-01')]

caso(200, 'CargaMasivaSAAPVPAC', 'sin-archivo-es-error', 'empty p_Archivo: CARGA_CONTRATOS_ERROR "Carga archivo sin datos." (id 1) and nothing else', L(F, r'Carga archivo sin datos'),
     ent(archivos=[]), seed(PERSONA, BANCO), secuencias=SEQ, tablas=[TCARGA, TCON])
caso(201, 'CargaMasivaSAAPVPAC', 'sin-contratos-ni-pac-solo-consulta', 'a file but no contract and no PAC entry: both lookups run on empty lists, nothing is created and the output is empty',
     L(F, r'if \(in.p_ParametrosContratos.size\(\) > 0\)'), ent(), seed(PERSONA, BANCO), secuencias=SEQ, tablas=[TCARGA, TCON])
caso(202, 'CargaMasivaSAAPVPAC', 'contrato-ya-existente-advierte-y-lo-quita', 'the contract folio already exists: ADVERTENCIA FOLIO_CONTRATO_EXISTE (id = the folio) counted in the error counter, the contract is removed from the load and the result carries its IdContratoSAAPV',
     L(F, r'FOLIO_CONTRATO_EXISTE'), ent(contratos=[contrato('2002')], dist=[distribucion('2002')], pmod=[portafolio_mod()], pacs=[pac(2002)]),
     seed(PERSONA, BANCO, EXISTENTE), secuencias=SEQ, tablas=[TCARGA, TCON])
caso(203, 'CargaMasivaSAAPVPAC', 'folio-existente-sin-id-interno-lanza', 'defect: the removal of the existing contract compares IdInterno of the load rows with a null primitive unboxing: a contract whose IdInterno is missing throws NullPointerException; the catch adds CARGA_CONTRATOS_ERROR and rethrows',
     L(F, r'in.p_ParametrosContratos.removeIf'), ent(contratos=[dict(contrato('2002'), IdInterno=None)], pacs=[pac(2002)]), seed(PERSONA, BANCO, EXISTENTE), secuencias=SEQ, tablas=[TCARGA, TCON])
caso(204, 'CargaMasivaSAAPVPAC', 'sin-moneda-del-sistema-lanza', 'PLATAFORMA/PLA.MonedaSistema missing: the configuration read throws and the catch rethrows (the output message is lost)',
     L(F, r'PLA.MonedaSistema'), ent(), seed(PERSONA, BANCO), secuencias=SEQ, config={'PLATAFORMA/PLA.MonedaSistema': None}, tablas=[TCARGA, TCON])
caso(205, 'CargaMasivaSAAPVPAC', 'crea-contrato-cuenta-nueva-y-mandato', 'a new contract is loaded through CargaMasivaArchivo (real): the person has no account at bank 016, so CrearPersonaCuentaBancaria is called (type CUCO) and the PAC mandate is entered with the new account',
     L(F, r'IngresarMandatoPAC ingresarMandatoPac = new'),
     ent(contratos=[contrato()], dist=[distribucion()], pmod=[portafolio_mod()], pacs=[pac()]), seed(PERSONA, BANCO), secuencias=SEQ, config=CFG, colaboradores=dict(SP, **CTA_NUEVA, **MANDATO),
     tablas=[TCARGA, TCON], enmascarar=MASK)
caso(206, 'CargaMasivaSAAPVPAC', 'crea-contrato-con-cuenta-existente-no-la-crea', 'the person already has the account at bank 016: no CrearPersonaCuentaBancaria call; the mandate is entered with the existing account id',
     L(F, r'if \(inCuentaBancariaList.size\(\) > 0\)'),
     ent(contratos=[contrato()], dist=[distribucion()], pmod=[portafolio_mod()], pacs=[pac()]),
     seed(PERSONA, BANCO, ins(T_CTA, IdCuentaBancaria=9050, IdBanco=16, IdPersona=501, NumeroCuentaBancaria='0012345678', IdEstado='ACT', Borrado='N')), secuencias=SEQ, config=CFG,
     colaboradores=dict(SP, **MANDATO), tablas=[TCARGA, TCON, TCTA], enmascarar=MASK)
caso(207, 'CargaMasivaSAAPVPAC', 'banco-inexistente-informa-y-corta-el-ciclo', 'the bank code of the PAC entry is not in BAN.Banco: SIN_CONFIGURAR_INFORMACION_BANCO ("Cuenta Bancaria: ...") is added and the loop is cut with break; the mandate is still entered',
     L(F, r'SIN_CONFIGURAR_INFORMACION_BANCO'),
     ent(contratos=[contrato()], dist=[distribucion()], pmod=[portafolio_mod()], pacs=[pac(banco='999')]), seed(PERSONA, BANCO), secuencias=SEQ, config=CFG, colaboradores=dict(SP, **MANDATO),
     tablas=[TCARGA, TCON], enmascarar=MASK)
caso(208, 'CargaMasivaSAAPVPAC', 'errores-del-mandato-pac-se-suman', 'the PAC mandate service answers an error: its messages are appended and counted',
     L(F, r'salidaCrearIngresoPAC.P_OUT_MENSAJES'),
     ent(contratos=[contrato()], dist=[distribucion()], pmod=[portafolio_mod()], pacs=[pac()]), seed(PERSONA, BANCO), secuencias=SEQ, config=CFG,
     colaboradores=dict(SP, **CTA_NUEVA, IngresarMandatoPAC={'salida': {'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_MENSAJES': [{'id': 1, 'tipo': 'ERROR', 'codigo': 'MANDATO_DUPLICADO', 'descripcion': 'mandato duplicado'}],
                                                                         'P_OUT_RESULTADO': []}}), tablas=[TCARGA, TCON], enmascarar=MASK)
caso(209, 'CargaMasivaSAAPVPAC', 'contrato-sin-pac-no-ingresa-mandato', 'a contract is loaded but the file has no PAC entry for its folio: no account lookup match and no mandate call',
     L(F, r'if \(inIngresoPACList.size\(\) > 0\)'),
     ent(contratos=[contrato()], dist=[distribucion()], pmod=[portafolio_mod()]), seed(PERSONA, BANCO), secuencias=SEQ, config=CFG, colaboradores=dict(SP), tablas=[TCARGA, TCON], enmascarar=MASK)
