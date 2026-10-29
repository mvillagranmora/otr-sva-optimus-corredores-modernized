"""Cases of CrearContratoSAAPV (ids 440-499)."""
from lib import *  # noqa: F401,F403
from c_grabar import MASCARA, SEQ, sp_ok, SP_PORTAFOLIO, PARTES as PARTES_G, err  # noqa: F401

C = 'CrearContratoSAAPV'
CF = 'CrearContratoSAAPV.java'

V_DIRFIS = 'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica'
T_DIRFIS = 'BSCL.SocioNegocio.BD::PER.DireccionFisica'
T_DIVPAIS = 'BSCL.Plataforma.BD::REG.DivisionPais'
T_CLASIF = 'BSCL.Plataforma.BD::REG.ClasificacionDivisionPais'
T_PAIS = 'BSCL.Plataforma.BD::REG.Pais'
T_CIUDAD = 'BSCL.Plataforma.BD::REG.Ciudad'
T_TEL = 'BSCL.SocioNegocio.BD::PER.Telefono'
T_EMAIL = 'BSCL.SocioNegocio.BD::PER.DireccionElectronica'
V_PERFIL = 'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase'
V_ENTIDAD = 'BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora'
T_INS = 'BSCL.Plataforma.BD::INS.Instrumento'
T_TINS = 'BSCL.Plataforma.BD::INS.TipoInstrumento'

# client 701 / person 501 with the whole "ficha" graph (address, phone, email, country, city, executive)
FICHA = [persona(501, '11111111-1', idc=701, ejecutivo=335),
         ins(T_DIRFIS, Calle='CALLE UNO', Numero='123', IdDireccionFisica=6501, IdDivisionPais=20, IdCiudad=10, IdPersona=501, CodigoProposito='PART', IdEstado='ACT', Predeterminado='S'),
         ins(V_DIRFIS, IdDireccionFisica=6501, IdPersona=501, IdEstado='ACT', IdCiudad=10, IdPais=44, IdDivisionPais=20, NombreCiudad='SANTIAGO', NombrePais='CHILE',
             PaisNombreDivisionPais='PROVIDENCIA'),
         ins(T_DIVPAIS, IdDivisionPais=20, NombreDivisionPais='PROVIDENCIA', IdClasificacion1=30),
         ins(T_CLASIF, IdClasificacionDivisionPais=30, NombreClasificacionDivisionPais='METROPOLITANA', IdPais=44),
         ins(T_PAIS, IdPais=44, NombrePais='CHILE'), ins(T_CIUDAD, IdCiudad=10, NombreCiudad='SANTIAGO'),
         ins(T_TEL, IdTelefono=7501, IdPersona=501, IdTipoTelefono='MOVIL', CodigoProposito='PART', Borrado='N', Numero='+56911112222'),
         ins(T_EMAIL, IdPersona=501, CodigoProposito='PART', Direccion='ana@example.invalid'),
         portafolio(901, 701, matriz=1),
         ins(T_INS, IdInstrumento=1021327, Nemotecnico='00391CCLP', IdTipoInstrumento='FFM'), ins(T_TINS, IdTipoInstrumento='FFM', IdClasificacionTipoInstrumento='FM')]
ENTIDAD = ins(V_ENTIDAD, IdEntidadRecaudadora=831, DocumentoIdentidad='76240079-0', Nombre='AFP UNO', IdPersona=61704)
COLC = {'SalidaProcedimiento': {SP_PORTAFOLIO: sp_ok(901)}}
COLS_C = ['IdContratoSAAPV', 'FolioContratoSAAPV', 'Folio', 'IdPersona', 'IdCliente', 'IdEmpleador', 'IdEstado', 'IdEstadoSolicitud', 'IdEstadoEnvioNotificacion', 'IdPlan',
          'NombrePlan', 'FormaDePago', 'IdMedioRecaudacionFormaPago', 'IdMedioRecaudacionTrabajador', 'Trabajador', 'Pensionado', 'CodigoTipoSolicitud', 'FechaSuscripcion',
          'FechaNacimientoCliente', 'NombreCliente', 'ApellidoPaternoCliente', 'DireccionCliente', 'TelefonoCelularCliente', 'EmailCliente', 'Institucion', 'IdPerfilComercial',
          'IdCategoriaInversionista', 'DocumentoIdentidadEmpleador', 'RazonSocialEmpleador', 'TipoTraspaso', 'TipoDeAhorro', 'RutEntidadOrigen']
TC = [tabla(T_CONTRATO, '"IdContratoSAAPV"', COLS_C), tabla(T_PORCON, '"IdPortafolioContratoSAAPV"'),
      tabla(T_DISTR, '"IdDistribucionInversion"', excluir=['IdDistribucionInversion']), tabla(T_TRASPASO, '"IdTraspaso"'), tabla(T_DETORIGEN, '"IdDetalleOrigenTraspaso"')]


def reg(**kw):
    d = {'IdPortafolio': 0, 'IdPropositoPortafolio': 'PROPOSITOAPV', 'IdMatrizAgrupador': 1, 'CodigoAgrupador': 'GRUPO 1', 'OrigenAhorro': 'APV', 'MesInicio': '2025-03', 'MesTermino': '',
         'Pago': 'IND', 'TipoRecaudacion': 'INDEFINIDO', 'PorcentajeRegimenImponible': 0, 'Producto': 'APV FONDOS MUTUOS', 'IdRegimen': 1, 'Regimen': 'APV-A', 'Cuenta': '',
         'MontoTotal': 100000, 'Moneda': 'CLP'}
    d.update(kw)
    return d


def dis(**kw):
    d = {'IdSerieFondo': '00391CCLP', 'MontoParcial': 100000, 'PorcentajeParcial': 100, 'IdEstado': 'VIG'}
    d.update(kw)
    return d


def con(**kw):
    d = {'Estado': 'PDI', 'FolioContratoSAAPV': '1000123', 'IdContratoSAAPV': 0, 'IdCliente': 701, 'IdPersona': 501, 'Rut': '11111111-1', 'Nombres': 'ANA', 'ApellidoPaterno': 'PEREZ',
         'ApellidoMaterno': 'SOTO', 'Direccion': 'CALLE UNO', 'NumeroDireccion': '123', 'TelefonoMovil': '+56911112222', 'Email': 'ana@example.invalid', 'IdPais': 44, 'IdCiudad': 10,
         'IdComuna': 20, 'FechaNacimiento': '1980-05-10', 'IdEstadoCivil': 'SO', 'IdGenero': 'F', 'IdCargoPersona': 1, 'IdPlan': 1, 'NombrePlan': 'ABIERTO', 'FechaSuscripcion': '2025-03-01',
         'FechaCreacion': '2025-03-01', 'IdCategoriaInversionista': 'MODERADO', 'IdPerfilComercial': 'PERFIL BASE', 'IdMedioRecaudacionTrabajador': 1, 'IdEntidadRecaudadora': 1,
         'CodigoPrevision': 1, 'Pensionado': 'NO', 'Trabajador': 'INDEPENDIENTE', 'FormaDePago': 'DIRECTA', 'IdMedioRecaudacionFormaPago': 1, 'CodigoTipoSolicitud': 'INGRESO',
         'IdEstadoEnvioNotificacion': 'NEV', 'IdEstadoSolicitud': '', 'IdPropositoPortafolio': '', 'UsarDatosFichaCliente': '', 'Regimen': [reg()], 'Distribucion': [dis()]}
    d.update(kw)
    return d


def ec(contratos, **kw):
    d = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_Contrato': contratos}
    d.update(kw)
    return d


# =================================================================================================================== quick answers
caso(440, C, 'sin-contratos-no-hay-nada-que-ingresar', 'empty list: NO_HAY_CONTRATOS_PARA_INGRESAR with counter 1', L(CF, r'NO_HAY_CONTRATOS_PARA_INGRESAR'), ec([]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(441, C, 'folio-vacio-pide-un-folio', 'a contract with empty FolioContratoSAAPV: DEBE_INGRESAR_UN_FOLIO_CONTRATO, counter 1', L(CF, r'DEBE_INGRESAR_UN_FOLIO_CONTRATO'),
     ec([con(FolioContratoSAAPV='')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)

# =================================================================================================================== folio handling
LF = L(CF, r'Long.parseLong\(contrato.FolioContratoSAAPV.trim\(\)\);', r'NuevoFolioContratoSAAPV = in.p_Contrato.get\(0\).FolioContratoSAAPV')
caso(442, C, 'folio-numerico-se-recorta', 'a numeric folio with spaces is trimmed and used', LF, ec([con(FolioContratoSAAPV=' 1000123 ')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1],
     enmascarar_colaboradores=MASCARA)
caso(443, C, 'folio-de-una-letra-genera-folio-de-suscripcion-nuevo', 'a folio "W" (one letter): ObtenerFolioSuscripcion creates the channel counter and the folio is W1', L(CF, r'esGeneracionFolioAutomatico\(contrato.FolioContratoSAAPV\)'),
     ec([con(FolioContratoSAAPV='W')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1] + [tabla(T_FOLIO, '"Canal"')], enmascarar_colaboradores=MASCARA)
caso(444, C, 'folio-de-una-letra-con-contador-existente', 'a folio "W" with an existing counter 5: the stored procedure is called (recorded) and the folio is W6', L(CF, r'obtenerFolioSuscripcion.logica\(conn, entradaObtenerFolioSuscripcion\)'),
     ec([con(FolioContratoSAAPV='W')]), seed(FICHA, ins(T_FOLIO, Canal='W', Folio=5)), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1] + [tabla(T_FOLIO, '"Canal"')], enmascarar_colaboradores=MASCARA)
caso(445, C, 'folio-alfanumerico-largo-se-usa-tal-cual', 'a non numeric folio of several characters is used as sent (the first contract folio, untrimmed)', L(CF, r'NuevoFolioContratoSAAPV = in.p_Contrato.get\(0\).FolioContratoSAAPV'),
     ec([con(FolioContratoSAAPV='AB-12')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)

# =================================================================================================================== modification checks
LMOD = L(CF, r'contrato.CodigoTipoSolicitud.equals\("MODIFICACION"\) && contrato.IdContratoSAAPV > 0', r'TipoOperacion = "M";')
caso(446, C, 'modificacion-con-folio-y-contrato-inexistente', 'MODIFICACION with the previous Folio but a contract id that does not exist: ERROR_CONTRATO_NO_EXISTE and return', L(CF, r'El valor de IdContratoSAAPV proporcionado no es valido'),
     ec([con(CodigoTipoSolicitud='MODIFICACION', IdContratoSAAPV=9999, Folio='F-OLD')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(447, C, 'modificacion-con-folio-que-no-corresponde', 'MODIFICACION with a previous Folio different from the stored one: ERROR_FOLIO_NO_CORRESPONDE and return', L(CF, r'ERROR_FOLIO_NO_CORRESPONDE'),
     ec([con(CodigoTipoSolicitud='MODIFICACION', IdContratoSAAPV=9001, Folio='OTRO')]), seed(FICHA, contrato(9001, 'F-OLD', IdEstado='VIG', IdCliente=701)), colaboradores=COLC, secuencias=SEQ)
caso(448, C, 'modificacion-cierra-el-contrato-anterior-vigente', 'MODIFICACION of a VIG contract (Folio matches): the new contract is created through GrabarContratoSAAPV (type M) and the previous one is closed (CER) with a log', L(CF, r'validaContrato.get\(0\).IdEstado.equals\("VIG"\)'),
     ec([con(CodigoTipoSolicitud='MODIFICACION', IdContratoSAAPV=9001, Folio='F-OLD', Regimen=[reg(IdPortafolio=901)])]),
     seed(FICHA, contrato(9001, 'F-OLD', IdEstado='VIG', IdCliente=701), porcon(8801, 901, 9001)), colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)
caso(449, C, 'modificacion-de-contrato-no-vigente-devuelve-error-despues-de-grabar', 'MODIFICACION of a contract that is not VIG: the new contract is already written when ERROR_CONTRATO_NO_VIGENTE is returned (legacy defect: the error does not undo it)',
     L(CF, r'ERROR_CONTRATO_NO_VIGENTE'),
     ec([con(CodigoTipoSolicitud='MODIFICACION', IdContratoSAAPV=9001, Folio='F-OLD', Regimen=[reg(IdPortafolio=901)])]), seed(FICHA, contrato(9001, 'F-OLD', IdEstado='PDI', IdCliente=701), porcon(8801, 901, 9001)),
     colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)
caso(450, C, 'contrato-vigente-aprobado-existente-bloquea', 'a VIG contract with IdEstadoSolicitud APR already exists for the client: CONTRATO_VIGENTE_APROBADO with the folio, return', L(CF, r'CONTRATO_VIGENTE_APROBADO'),
     ec([con()]), seed(FICHA, contrato(9001, 'F-EXIST', IdEstado='VIG', IdEstadoSolicitud='APR', IdCliente=701), porcon(8801, 901, 9001)), colaboradores=COLC, secuencias=SEQ)
caso(451, C, 'solo-el-contrato-vigente-aprobado-bloquea', 'an existing contract of the client that is not VIG+APR (here VIG with IdEstadoSolicitud PDA) is not even returned by the query: the creation goes on as a new contract. The loop that would turn matching contracts into a modification is unreachable (every row the query returns is VIG/APR and returns the error first)',
     L(CF, r'Long idProductoAgrupador = Long.parseLong'),
     ec([con(CodigoAgrupador='GRUPO 1', IdMatrizAgrupador=1)]), seed(FICHA, contrato(9001, 'F-EXIST', IdEstado='VIG', IdEstadoSolicitud='PDA', IdCliente=701), porcon(8801, 901, 9001)),
     colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)

# =================================================================================================================== datos APV and portfolio
caso(453, C, 'regimen-sin-datos-apv-en-la-matriz', 'INGRESO with a product that is not in the matrix: DATOS_APV_INCORRECTOS and return', L(CF, r'DATOS_APV_INCORRECTOS'),
     ec([con(Regimen=[reg(Producto='PRODUCTO INEXISTENTE')])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(454, C, 'regimen-vacio-lanza-IndexOutOfBounds-antes-de-validar', 'no regimen rows: the check ERROR_REGIMEN_VACIO is unreachable because listaPortafoliosModificados.get(0) throws IndexOutOfBoundsException first (legacy defect)',
     L(CF, r'listaPortafoliosModificados.get\(0\).IdPortafolio\);'), ec([con(Regimen=[])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(455, C, 'portafolio-sin-existir-toma-el-primero-del-cliente', 'the client has no portfolio of that matrix/regimen: the lowest portfolio id of the client is used', L(CF, r'SelectPrimerIdPortafolio.get\(0\).IdPortafolio'),
     ec([con(Regimen=[reg(IdRegimen=2, Regimen='APV-B', IdMatrizAgrupador=2, Producto='APV FONDOS MUTUOS')])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)
caso(456, C, 'moneda-uf-llena-el-monto-en-uf', 'a regimen in UF (Moneda not CLP) fills MontoUF, not MontoPesos', L(CF, r'portafoliosModificados.MontoUF = regimen.MontoTotal'),
     ec([con(Regimen=[reg(Moneda='UF', MontoTotal=50)], Distribucion=[dis(MontoParcial=50)])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:3], enmascarar_colaboradores=MASCARA)
caso(457, C, 'regimen-sin-nombre-lo-busca-por-id', 'a regimen with empty Regimen text: the code is read from RegimenTributario by IdRegimen', L(CF, r'SQL_SELECT_REGIMEN, EntradaRegimen'),
     ec([con(Regimen=[reg(Regimen='')], CodigoTipoSolicitud='AUTORIZACION')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)

# =================================================================================================================== ficha cliente
caso(458, C, 'sin-ficha-de-cliente-lanza-IndexOutOfBounds', 'the client/portfolio has no ficha row: salidaParametrosFichaCliente.get(0) throws IndexOutOfBoundsException', L(CF, r'salidaParametrosFichaCliente.get\(0\).IdPersona = contrato.IdPersona'),
     ec([con(IdCliente=999)]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(459, C, 'usar-datos-de-ficha-N-pisa-la-ficha-con-los-del-contrato', 'UsarDatosFichaCliente N: the ficha data (name, address, phone, ...) is replaced by the contract data', L(CF, r'UsarDatosFichaCliente.equalsIgnoreCase\("N"\)'),
     ec([con(UsarDatosFichaCliente='N', Nombres='OTRA', Direccion='CALLE DOS', TelefonoMovil='+56999990000', Email='otra@example.invalid')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ,
     tablas=TC[:1], enmascarar_colaboradores=MASCARA)
caso(460, C, 'dependiente-sin-rut-de-empleador-usa-el-de-la-ficha', 'a DEPENDIENTE contract without employer RUT takes the employer data of the ficha', L(CF, r'contrato.RutEmpleador.equals\(""\) && esDependiente'),
     ec([con(Trabajador='DEPENDIENTE')]),
     seed(FICHA, ins(T_PERSONA, IdPersona=801, Nombres='', ApellidoPaterno='', ApellidoMaterno='', RazonSocial='EMPRESA UNO', IdPaisResidencia=44, Tipo='J', IdDocumentoIdentidad=5801,
                     IdDireccionFisicaPrincipal=0, IdTelefonoPrincipal=0),
          ins(T_DOC, DocumentoIdentidad='76000000-0', IdDocumentoIdentidad=5801, IdPersona=801, Predeterminado='S'),
          ins(T_DIRFIS, Calle='AV EMPRESA', Numero='10', IdDireccionFisica=6801, IdDivisionPais=20, IdCiudad=10, IdPersona=801, CodigoProposito='COME', IdEstado='ACT', Predeterminado='S')),
     colaboradores=COLC, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)
caso(461, C, 'pensionado-solo-si-dice-SI', 'Pensionado SI (any case) is stored as SI, anything else as NO; Trabajador other than DEPENDIENTE is INDEPENDIENTE', L(CF, r'"SI".equalsIgnoreCase\(contrato.Pensionado\)'),
     ec([con(Pensionado='si', Trabajador='otro')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)

# =================================================================================================================== perfil comercial APV exento
caso(462, C, 'perfil-exento-sin-parametro-de-configuracion', 'IdPropositoPortafolio PROPOSITOAPV with APV.Habilitado S but no APV.PerfilComercial.<categoria> parameter: PERFIL_COMERCIAL_APV_EXENTO_INVALIDO and return',
     L(CF, r'PERFIL_COMERCIAL_APV_EXENTO_INVALIDO'), ec([con(IdPropositoPortafolio='PROPOSITOAPV')]), seed(FICHA), config={'PLATAFORMA/APV.Habilitado': 'S'}, colaboradores=COLC, secuencias=SEQ)
caso(463, C, 'perfil-exento-configurado-reemplaza-el-perfil-del-contrato', 'APV.Habilitado S and the configured exempt profile exists (view, APV_EXENTO, ACT): the contract takes it as IdPerfilComercial', L(CF, r'perfilComercialPorDefecto = obtenerPerfilComercialPorDefecto'),
     ec([con(IdPropositoPortafolio='PROPOSITOAPV')]), seed(FICHA, ins(V_PERFIL, IdPerfilComercialBase='PERFIL EXENTO', IdPerfilCobros='APV_EXENTO', IdEstado='ACT')),
     config={'PLATAFORMA/APV.Habilitado': 'S', 'PLATAFORMA/APV.PerfilComercial.MODERADO': 'PERFIL EXENTO'}, colaboradores=COLC, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)
caso(464, C, 'perfil-exento-configurado-pero-inactivo', 'the configured profile exists in the config but its view row is INA: the same error', L(CF, r'no existe, est'),
     ec([con(IdPropositoPortafolio='PROPOSITOAPV')]), seed(FICHA, ins(V_PERFIL, IdPerfilComercialBase='PERFIL EXENTO', IdPerfilCobros='APV_EXENTO', IdEstado='INA')),
     config={'PLATAFORMA/APV.Habilitado': 'S', 'PLATAFORMA/APV.PerfilComercial.MODERADO': 'PERFIL EXENTO'}, colaboradores=COLC, secuencias=SEQ)

# =================================================================================================================== distribution
caso(465, C, 'distribucion-sin-monto-ni-porcentaje', 'a distribution row with neither amount nor percentage: ERROR_MONTO_O_PORCENTAJE_VACIO', L(CF, r'ERROR_MONTO_O_PORCENTAJE_VACIO'),
     ec([con(Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': None, 'PorcentajeParcial': None}])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(466, C, 'distribucion-con-porcentaje-cero', 'only a percentage and it is exactly 0: ERROR_PORCENTAJE_CERO', L(CF, r'ERROR_PORCENTAJE_CERO'),
     ec([con(Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': None, 'PorcentajeParcial': 0}])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(467, C, 'distribucion-con-porcentaje-0.0-pasa-la-validacion-de-cero', 'the percentage 0.0 is NOT equal to BigDecimal.ZERO (scale differs): it passes the zero check, the amount is computed as 0 and the failure only comes later as the total check (legacy defect)', L(CF, r'PorcentajeParcial.equals\(BigDecimal.ZERO\)'),
     ec([con(Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': None, 'PorcentajeParcial': 0.0}])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:3],
     enmascarar_colaboradores=MASCARA)
caso(468, C, 'distribucion-solo-porcentaje-calcula-el-monto', 'only a percentage (60) and MontoTotal 100000: the amount 60000 is computed; the total check then fails against the deposit amount (UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO)',
     L(CF, r'contrato.Distribucion.get\(i\).MontoParcial = contrato.Regimen.get\(0\).MontoTotal.multiply'),
     ec([con(Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': None, 'PorcentajeParcial': 60}])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(469, C, 'distribucion-solo-porcentaje-con-monto-total-cero', 'only a percentage and MontoTotal 0: ERROR_MONTO_DEPOSITO_CERO (without IdContratoSAAPV 0 / folio entity values)', L(CF, r'ERROR_MONTO_DEPOSITO_CERO'),
     ec([con(Regimen=[reg(MontoTotal=0)], Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': None, 'PorcentajeParcial': 50}])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(470, C, 'distribucion-solo-monto-cero', 'only an amount and it is 0: ERROR_MONTO_CERO', L(CF, r'ERROR_MONTO_CERO'),
     ec([con(Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': 0, 'PorcentajeParcial': None}])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(471, C, 'distribucion-solo-monto-calcula-el-porcentaje-a-8-decimales', 'two amounts 33333.33 + 66666.67 of 100000: the percentages are computed with scale 8 HALF_UP and the total matches', L(CF, r'new BigDecimal\(100\).multiply\(contrato.Distribucion.get\(i\).MontoParcial\).divide'),
     ec([con(Distribucion=[{'IdSerieFondo': '00391CCLP', 'IdEstado': 'VIG', 'MontoParcial': 33333.33, 'PorcentajeParcial': None},
                           {'IdSerieFondo': '00392CCLP', 'IdEstado': 'VIG', 'MontoParcial': 66666.67, 'PorcentajeParcial': None}])]),
     seed(FICHA, ins(T_INS, IdInstrumento=1021328, Nemotecnico='00392CCLP', IdTipoInstrumento='FFM')), colaboradores=COLC, secuencias=SEQ, tablas=TC[:3], enmascarar_colaboradores=MASCARA)
caso(472, C, 'distribucion-que-no-suma-el-monto-del-deposito', 'the distribution total differs from the deposit: UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO and the loop breaks', L(CF, r'UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO'),
     ec([con(Distribucion=[dis(MontoParcial=90000, PorcentajeParcial=90)])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)
caso(473, C, 'serie-de-fondo-sin-instrumento-lanza-NPE', 'a fund series that is not an FM instrument: the instrument lookup returns null and instrumento.IdInstrumento throws NullPointerException', L(CF, r'distribucionInversion.IdInstrumento = instrumento.IdInstrumento'),
     ec([con(Distribucion=[dis(IdSerieFondo='NO-EXISTE')])]), seed(FICHA), colaboradores=COLC, secuencias=SEQ)

# =================================================================================================================== traspaso details
LDET = L(CF, r'if\(!contrato.DetalleOrigenTraspaso.isEmpty\(\)\) \{', r'listaDetalleOrigenTraspaso.add\(detalleOrigenTraspaso\)')


def det(**kw):
    d = {'CodigoTipoAhorro': 'APV', 'DocumentoEntidad': '76240079-0', 'Fondo': 'A', 'MontoTraspaso': 0, 'UFTraspaso': 0, 'PorcentajeTraspaso': 100}
    d.update(kw)
    return d


caso(474, C, 'traspaso-sin-documento-de-entidad', 'an origin detail with empty DocumentoEntidad: ERROR_ENTIDAD_RECAUDADORA_TRASPASO', LDET, ec([con(TipoTraspaso='TOT', DetalleOrigenTraspaso=[det(DocumentoEntidad='')])]),
     seed(FICHA, ENTIDAD), colaboradores=COLC, secuencias=SEQ)
caso(475, C, 'traspaso-sin-tipo', 'origin details but empty TipoTraspaso: ERROR_TIPO_TRASPASO', L(CF, r'ERROR_TIPO_TRASPASO'), ec([con(TipoTraspaso='', DetalleOrigenTraspaso=[det()])]), seed(FICHA, ENTIDAD),
     colaboradores=COLC, secuencias=SEQ)
caso(476, C, 'traspaso-apv-con-fondo-invalido', 'CodigoTipoAhorro APV with Fondo outside A-E: ERROR_TIPO_AHORRO_FONDO', L(CF, r'ERROR_TIPO_AHORRO_FONDO'), ec([con(TipoTraspaso='TOT', DetalleOrigenTraspaso=[det(Fondo='Z')])]),
     seed(FICHA, ENTIDAD), colaboradores=COLC, secuencias=SEQ)
caso(477, C, 'traspaso-parcial-sin-monto-uf-ni-porcentaje', 'TipoTraspaso PAR with amount, UF and percentage all 0: ERROR_MONTO_UF_PORCENTAJE_TRASPASO', L(CF, r'ERROR_MONTO_UF_PORCENTAJE_TRASPASO'),
     ec([con(TipoTraspaso='PAR', DetalleOrigenTraspaso=[det(PorcentajeTraspaso=0)])]), seed(FICHA, ENTIDAD), colaboradores=COLC, secuencias=SEQ)
caso(478, C, 'traspaso-con-entidad-inexistente', 'the origin entity document is not in the entity view: ERROR_ENTIDAD_RECAUDADORA_TRASPASO', L(CF, r'if \(entidad == null\)'),
     ec([con(TipoTraspaso='TOT', DetalleOrigenTraspaso=[det(DocumentoEntidad='11111111-9')])]), seed(FICHA, ENTIDAD), colaboradores=COLC, secuencias=SEQ)
caso(479, C, 'traspaso-total-completo-crea-contrato-y-traspaso', 'TOT with a valid entity: the origin amount is the regimen total and 100%; GrabarContratoSAAPV creates the contract as TRA INGRESO with traspaso and origin detail', L(CF, r'CodigoTipoSolicitud = listaDetalleOrigenTraspaso.size\(\) > 0 \? "TRA"'),
     ec([con(TipoTraspaso='TOT', DetalleOrigenTraspaso=[det()])]), seed(FICHA, ENTIDAD), colaboradores=COLC, secuencias=SEQ, tablas=TC, enmascarar_colaboradores=MASCARA)
caso(480, C, 'traspaso-parcial-calcula-el-monto-de-los-detalles-con-porcentaje', 'TipoTraspaso PAR: the origin rows with a percentage take the regimen total as amount', L(CF, r'else if\(\("PAR"\).equals\(contrato.TipoTraspaso\)\)'),
     ec([con(TipoTraspaso='PAR', DetalleOrigenTraspaso=[det(PorcentajeTraspaso=40), det(PorcentajeTraspaso=0, MontoTraspaso=5000)])]), seed(FICHA, ENTIDAD), colaboradores=COLC, secuencias=SEQ, tablas=TC[3:],
     enmascarar_colaboradores=MASCARA)

# =================================================================================================================== Grabar messages mapping and several contracts
caso(481, C, 'ingreso-completo-mapea-los-mensajes-de-grabar', 'INGRESO of an existing client/portfolio: the OK messages of GrabarContratoSAAPV become Mensajes (IdInterno 0, TipoMensaje, Codigo, Mensaje); IdContratoSAAPV is the id of the last message; FolioContrato is the new folio',
     L(CF, r'for \(MensajeSalida mensajeSalida : salidaGrabarContratoSAAPV.P_OUT_MENSAJES\)', r'entidadContrato.FolioContrato = NuevoFolioContratoSAAPV'),
     ec([con()]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC, enmascarar_colaboradores=MASCARA)
caso(482, C, 'un-error-de-grabar-corta-la-lista-de-mensajes', 'a message of type ERROR from GrabarContratoSAAPV (portfolio procedure error): the Mensajes list becomes just that error, the counter is 1 and the copy stops',
     L(CF, r'if\(mensajeSalida.tipo.equals\("ERROR"\)\)'),
     ec([con()]), seed(FICHA),
     colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: {'p_out_RefId': 0, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': 1, 'p_out_ErroresDetalle': [err('PORTAFOLIO_NO_CREADO')],
                                                          'p_out_AdvertenciasDetalle': []}}}, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)
caso(483, C, 'solo-se-procesa-el-primer-contrato', 'two contracts: the loop breaks after the first processed one, the second is ignored', L(CF, r'entidadContrato.FolioContrato = NuevoFolioContratoSAAPV;\s*$'),
     ec([con(), con(FolioContratoSAAPV='1000124')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)
caso(484, C, 'un-contrato-sin-folio-y-otro-valido', 'the first contract has no folio (error, loop continues) and the second is processed', L(CF, r'DEBE_INGRESAR_UN_FOLIO_CONTRATO'),
     ec([con(FolioContratoSAAPV=''), con(FolioContratoSAAPV='1000124')]), seed(FICHA), colaboradores=COLC, secuencias=SEQ, tablas=TC[:1], enmascarar_colaboradores=MASCARA)
caso(485, C, 'modificacion-sin-folio-hereda-dia-y-medio-de-pago', 'MODIFICACION with IdContratoSAAPV of an existing VIG contract and an empty Folio (the folio is read from the contract): DiaDescuento 0 and IdMedioRecaudacionFormaPago 0 are replaced by the values of the existing contract (15 and 2) before Grabar',
     L(CF, r'contrato.DiaDescuento = contrato.DiaDescuento.compareTo'),
     ec([con(CodigoTipoSolicitud='MODIFICACION', Folio='', IdContratoSAAPV=9001, IdMedioRecaudacionFormaPago=0, DiaDescuento=0, Regimen=[reg(IdPortafolio=901)])]),
     seed(FICHA, contrato(9001, 'F-OLD', IdEstado='VIG', IdCliente=701, DiaDescuento=15, IdMedioRecaudacionFormaPago=2), porcon(8801, 901, 9001)),
     colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)
caso(486, C, 'modificacion-sin-folio-conserva-dia-y-medio-informados', 'same existing contract but the input carries DiaDescuento 5 and IdMedioRecaudacionFormaPago 3: the informed values are kept',
     L(CF, r'contrato.IdMedioRecaudacionFormaPago = contrato.IdMedioRecaudacionFormaPago.compareTo'),
     ec([con(CodigoTipoSolicitud='MODIFICACION', Folio='', IdContratoSAAPV=9001, IdMedioRecaudacionFormaPago=3, DiaDescuento=5, Regimen=[reg(IdPortafolio=901)])]),
     seed(FICHA, contrato(9001, 'F-OLD', IdEstado='VIG', IdCliente=701, DiaDescuento=15, IdMedioRecaudacionFormaPago=2), porcon(8801, 901, 9001)),
     colaboradores=COLC, secuencias=SEQ, tablas=TC[:2], enmascarar_colaboradores=MASCARA)
