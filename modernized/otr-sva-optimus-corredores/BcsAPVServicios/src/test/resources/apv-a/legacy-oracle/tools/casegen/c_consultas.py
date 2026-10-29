"""Cases of ConsultarClienteContratoSAAPV (ids 150-179), ConsultarCustodiaPortafolioAPV (180-209) and CargaMasivaArchivo (360-379)."""
from lib import *  # noqa: F401,F403
from c_grabar import par, pm, di, ent as ent_grabar, SEQ, MASCARA, sp_ok, SP_PORTAFOLIO, PARTES as PARTES_G  # noqa: F401

BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}
T_EVENTO = 'BSCL.Plataforma.BD::AUT.Evento'
T_PLAEST = 'BSCL.Plataforma.BD::PLA.Estado'
T_ENTREC = 'BSCL.APV.BD::SCS.EntidadRecaudadora'


def e(**kw):
    d = dict(BASE)
    d.update(kw)
    return d


# ===================================================================================================== ConsultarClienteContratoSAAPV
Q = 'ConsultarClienteContratoSAAPV'
QF = 'ConsultarClienteContratoSAAPV.java'
PERS = [persona(501, '11111111-1', idc=701), persona(502, '22222222-2', idc=702), persona(801, '76000000-0', nombres='', paterno='', materno='', tipo='J', razon='EMPRESA UNO'),
        portafolio(901, 701, matriz=1), portafolio(902, 701, matriz=2), portafolio(903, 702, matriz=1)]


def vig(idc, folio, idp=501, idcli=701, idpo=901, **kw):
    return [contrato(idc, folio, IdEstado='VIG', IdPlan=1, IdPersona=idp, IdCliente=idcli, **kw), porcon(8000 + idc, idpo, idc, origen='APV', monto=100000, pago='IND', ini='2025-03', fin='9999-01', pct=100)]


def pp(idp=501, idpo=901):
    return {k: v for k, v in {'IdPersona': idp, 'IdPortafolio': idpo}.items() if v is not None}


LG = L(QF, r'if \("GRILLA".equals\(in.p_Consulta\)\)', r'out.P_OUT_CLIENTECONTRATOSAAPV.add\(clienteContratoSAAPV\)')
caso(150, Q, 'grilla-sin-filtro-devuelve-un-registro-por-persona-y-portafolio', 'GRILLA without person filter: one row per (person, portfolio) of VIG contracts of plan 1, with name, RUT and product', LG,
     e(p_Consulta='GRILLA'), seed(PERS, vig(9001, 'F-Q1'), vig(9002, 'F-Q2', idp=502, idcli=702, idpo=903)), sin_orden=['P_OUT_CLIENTECONTRATOSAAPV'])
caso(151, Q, 'grilla-con-filtro-de-persona', 'GRILLA with a person filter: only that person', L(QF, r'idPersonaList = in.p_PersonaPortafolio.stream\(\)'),
     e(p_Consulta='GRILLA', p_PersonaPortafolio=[pp(502, None)]), seed(PERS, vig(9001, 'F-Q1'), vig(9002, 'F-Q2', idp=502, idcli=702, idpo=903)), sin_orden=['P_OUT_CLIENTECONTRATOSAAPV'])
caso(152, Q, 'grilla-quita-duplicados-persona-portafolio', 'two contracts of the same person and portfolio: a single row (the hash by IdPersona_IdPortafolio)', L(QF, r'clienteContratoSAAPVHash.containsKey'),
     e(p_Consulta='GRILLA'), seed(PERS, vig(9001, 'F-Q1'), vig(9002, 'F-Q2')), sin_orden=['P_OUT_CLIENTECONTRATOSAAPV'])
caso(153, Q, 'grilla-excluye-no-vigentes-portafolio-inactivo-y-otro-plan', 'a contract that is not VIG, a contract of plan 2 and one on an INA portfolio are excluded', LG,
     e(p_Consulta='GRILLA'), seed(PERS[:-3], portafolio(901, 701, matriz=1), portafolio(902, 701, matriz=2, estado='INA'), portafolio(903, 702, matriz=1), vig(9001, 'F-Q1'),
                                  contrato(9002, 'F-Q2', IdEstado='REV', IdPlan=1), porcon(8802, 901, 9002), contrato(9003, 'F-Q3', IdEstado='VIG', IdPlan=2), porcon(8803, 903, 9003),
                                  contrato(9004, 'F-Q4', IdEstado='VIG', IdPlan=1), porcon(8804, 902, 9004)),
     sin_orden=['P_OUT_CLIENTECONTRATOSAAPV'])
LS = L(QF, r'if \("SUBGRILLA".equals\(in.p_Consulta\)\) \{', r'sort\(Comparator.comparing\(\(ContratoSAAPV')
FULL = dict(FechaSuscripcion='2025-03-01', FechaCreacion='2025-03-02', FechaAprobacion='2025-03-03', FechaNotificacion='2025-03-04', FechaRecepcion='2025-03-05',
            NombreEncargado='JUAN ENCARGADO', IdEmpleador=801, DocumentoIdentidadEmpleador='76000000-0', FormaDePago='DIRECTA', IdMedioRecaudacionFormaPago=3, DiaDescuento=5,
            Institucion='831', ComisionTransferencia=1.5, CodigoTipoSolicitud='TRA', RutEntidadOrigen='76240079-0', NombreEntidadOrigen='AFP UNO', TipoDeAhorro='APV', FondoDeOrigen='A',
            TipoTraspaso='TOT', MontoTraspaso=100000, UFTraspaso=2.5, PorcentajeTraspaso=100, IdEstadoEnvioNotificacion='ENV')
caso(154, Q, 'subgrilla-directa-pac-conserva-dia-de-descuento', 'SUBGRILLA of a DIRECTA contract with medio PAC: NombreMedioRecaudacion and DiaDescuento are kept, Institucion and ComisionTransferencia are nulled, EstadoNuevo is the APV state name',
     LS, e(p_Consulta='SUBGRILLA', p_PersonaPortafolio=[pp(501, 901)]), seed(PERS, vig(9001, 'F-Q1', **FULL), ins(T_ENTREC, Codigo=831, Nombre='AFP UNO')))
caso(155, Q, 'subgrilla-indirecta-conserva-institucion-y-comision', 'SUBGRILLA of an INDIRECTA contract: Institucion (entity name) and ComisionTransferencia are kept, medio and day are nulled', L(QF, r'"INDIRECTA".equals\(contrato.FormaDePago\) \? contrato.Institucion'),
     e(p_Consulta='SUBGRILLA', p_PersonaPortafolio=[pp(501, 901)]), seed(PERS, vig(9001, 'F-Q1', **dict(FULL, FormaDePago='INDIRECTA')), ins(T_ENTREC, Codigo=831, Nombre='AFP UNO')))
caso(156, Q, 'subgrilla-estado-nuevo-segun-el-evento-de-autorizacion', 'EstadoNuevo: event state VCD/VIG -> PENDIENTE DE APROBACION, RZD -> RECHAZADO, any other (or none) -> the APV state name; three contracts show the three outcomes',
     L(QF, r'"PENDIENTE DE APROBACION"'),
     e(p_Consulta='SUBGRILLA', p_PersonaPortafolio=[pp(501, 901)]),
     seed(PERS, vig(9001, 'F-Q1'), vig(9002, 'F-Q2'), vig(9003, 'F-Q3'),
          ins(T_EVENTO, IdEstado='VCD', NumeroOperacionConcepto='F-Q1', IdCliente=701, IdModulo='APV', FechaHoraRegistro='2025-03-10 10:00:00'),
          ins(T_EVENTO, IdEstado='RZD', NumeroOperacionConcepto='F-Q2', IdCliente=701, IdModulo='APV', FechaHoraRegistro='2025-03-10 10:00:00')))
caso(157, Q, 'subgrilla-usa-el-ultimo-evento-del-contrato', 'two events for the same folio: only the one with the latest FechaHoraRegistro counts', L(QF, r'contrato.EstadoNuevo = "VCD"'),
     e(p_Consulta='SUBGRILLA', p_PersonaPortafolio=[pp(501, 901)]),
     seed(PERS, vig(9001, 'F-Q1'), ins(T_EVENTO, IdEstado='RZD', NumeroOperacionConcepto='F-Q1', IdCliente=701, IdModulo='APV', FechaHoraRegistro='2025-03-10 10:00:00'),
          ins(T_EVENTO, IdEstado='VCD', NumeroOperacionConcepto='F-Q1', IdCliente=701, IdModulo='APV', FechaHoraRegistro='2025-03-11 10:00:00')))
caso(158, Q, 'subgrilla-con-mandato-pac-informa-su-estado', 'the contract folio has a PAC mandate with a state of the platform table: EstadoPAC is its name', L(QF, r'contrato.NombreMedioRecaudacion = "DIRECTA"'),
     e(p_Consulta='SUBGRILLA', p_PersonaPortafolio=[pp(501, 901)]),
     seed(PERS, vig(9001, 'F-Q1'), ins(T_MANDATO, IdMandatoPAC=300, Estado='ACT', FolioSAAPV='F-Q1'), ins(T_PLAEST, IdEstado='ACT', Nombre='ACTIVO')))
caso(159, Q, 'subgrilla-ordena-por-id-de-contrato', 'two contracts of the same person: the result is sorted by IdContratoSAAPV', L(QF, r'sort\(Comparator.comparing\(\(ContratoSAAPV'),
     e(p_Consulta='SUBGRILLA', p_PersonaPortafolio=[pp(501, None)]), seed(PERS, vig(9005, 'F-Q5'), vig(9002, 'F-Q2')))
caso(160, Q, 'modal-devuelve-contratos-con-persona-y-portafolio', 'MODAL: person, RUT, name, portfolio, product, contract id and folio, sorted by contract id', L(QF, r'else \{\s*$'),
     e(p_Consulta='MODAL', p_PersonaPortafolio=[pp(501, 901)]), seed(PERS, vig(9005, 'F-Q5'), vig(9002, 'F-Q2')))
caso(161, Q, 'modal-sin-filtro-de-portafolio-devuelve-todos', 'MODAL with only a person: every portfolio of that person', L(QF, r'filtroIdPortafolio = idPortafolioList.size\(\)'),
     e(p_Consulta='MODAL', p_PersonaPortafolio=[pp(501, None)]), seed(PERS, vig(9001, 'F-Q1'), vig(9002, 'F-Q2', idpo=902)))
caso(162, Q, 'consulta-desconocida-no-devuelve-nada', 'p_Consulta with another value: all lists empty', L(QF, r'else if \("SUBGRILLA".equals'), e(p_Consulta='OTRA'), seed(PERS, vig(9001, 'F-Q1')))
caso(163, Q, 'consulta-nula-no-devuelve-nada', 'p_Consulta not informed: all lists empty (the comparisons are written constant.equals(value))', L(QF, r'if \("GRILLA".equals\(in.p_Consulta\)\)'), e(), seed(PERS, vig(9001, 'F-Q1')))
caso(164, Q, 'filtro-con-persona-nula-se-ignora', 'a PersonaPortafolio entry without IdPersona is dropped from the filter (no filter at all)', L(QF, r'filter\(pp -> pp.IdPersona != null\)'),
     e(p_Consulta='GRILLA', p_PersonaPortafolio=[{'IdPortafolio': 901}]), seed(PERS, vig(9001, 'F-Q1'), vig(9002, 'F-Q2', idp=502, idcli=702, idpo=903)), sin_orden=['P_OUT_CLIENTECONTRATOSAAPV'])

# ===================================================================================================== ConsultarCustodiaPortafolioAPV
K = 'ConsultarCustodiaPortafolioAPV'
KF = 'ConsultarCustodiaPortafolioAPV.java'
COLK_BASE = [persona(501, '11111111-1', idc=701), portafolio(901, 701, matriz=1), portafolio(902, 701, matriz=2)]
T_DETORG = tabla(T_DETORIGEN, '"IdDetalleOrigenTraspaso"')


def tbl(idpo=901, idi=1021327, nemo='00391CCLP', cant=100, monto=1000, regimen='APV-A', clas='FM', tipo='FFM', producto='APV FONDOS MUTUOS', tipo_ahorro='APV', cod='APV-A', **kw):
    d = {'IdPortafolio': idpo, 'IdInstrumento': idi, 'Nemotecnico': nemo, 'CantidadCustodia': cant, 'MontoMercadoCustodia': monto, 'IdRegimenTributario': regimen,
         'IdClasificacionTipoInstrumento': clas, 'IdTipoInstrumento': tipo, 'Producto': producto, 'CodigoRegimenTipoAhorro': tipo_ahorro, 'CodigoPortafolio': cod,
         'PPPCustodia': 10, 'ValorPPPCustodia': 10, 'MontoPPPCustodia': 1000, 'IdEmisor': 'EMI1', 'NombreCliente': 'ANA PEREZ SOTO', 'DocumentoIdentidad': '11111111-1'}
    d.update(kw)
    return d


def custo(*filas, **kw):
    return {'ConsultarCustodiaPortafolio': {'P_OUT_TBL': list(filas), 'P_OUT_ERRORESCONTADOR': 0}}


def ffmm(*filas):
    return {'ConsultarCustodiaPortafolioFondoMutuo': {'P_OUT_TBL': list(filas)}}


def saldo(*filas):
    return {'ConsultarSaldoCuentaInversion': {'P_OUT_SALDOPORFECHAEFECTIVA': list(filas)}}


def merge(*ds):
    out = {}
    for d in ds:
        out.update(d)
    return out


def ek(**kw):
    d = dict(BASE)
    d['p_IdCliente'] = 701
    d.update(kw)
    return d


LK = L(KF, r'String fechaConsulta = obtenerFechaConsulta', r'out.P_OUT_TOTALFFMM')
caso(180, K, 'una-custodia-con-saldo-de-cuenta', 'one FM custody of portfolio 901: product and plan from the matrix, balances CLP/USD/EUR from the account balance, totals by instrument type', LK,
     ek(), seed(COLK_BASE), colaboradores=merge(custo(tbl()), ffmm(), saldo({'IdPortafolio': 901, 'IdMoneda': 'CLP', 'Saldo': 5000}, {'IdPortafolio': 901, 'IdMoneda': 'USD', 'Saldo': 12.5})))
caso(181, K, 'totales-por-tipo-de-instrumento', 'an ACN (RV) row and an FFM row: TOTALRV and TOTALFFM add the MontoMercadoCustodia of their type, TOTALCUSTODIA of all', L(KF, r'obtenerTotal\(out.P_OUT_CUSTODIA, "ACN"\)'),
     ek(), seed(COLK_BASE),
     colaboradores=merge(custo(tbl(), tbl(idi=1021400, nemo='ACCION1', clas='RV', tipo='ACN', monto=2500.5)), ffmm(), saldo()))
caso(182, K, 'filtra-otros-no-apv-clasificacion-y-cantidad-cero', 'rows of product "Otros (No APV)", of another classification (RF) or with quantity 0 are dropped', L(KF, r'!"Otros \(No APV\)".equals\(custodia.Producto\)'),
     ek(), seed(COLK_BASE),
     colaboradores=merge(custo(tbl(), tbl(idi=2, producto='Otros (No APV)'), tbl(idi=3, clas='RF', tipo='BON'), tbl(idi=4, cant=0)), ffmm(), saldo()))
caso(183, K, 'solicitudes-de-rescate-restan-cantidad', 'FFMM requests with a pending rescue (matching instrument, portfolio and regimen) subtract from the custody quantity; a fully rescued row disappears', L(KF, r'custodia.CantidadCustodia.subtract\(solicitud.CantidadTransSolicitudesRescate\)'),
     ek(), seed(COLK_BASE),
     colaboradores=merge(custo(tbl(cant=100), tbl(idi=2, nemo='OTRO', cant=30)),
                         ffmm({'IdInstrumento': 1021327, 'IdPortafolio': 901, 'IdRegimenFondoMutuo': 'APV-A', 'CantidadCustodia': 100, 'CantidadTransSolicitudesRescate': 40, 'IdEmpresa': 1, 'Nemotecnico': '00391CCLP'},
                              {'IdInstrumento': 2, 'IdPortafolio': 901, 'IdRegimenFondoMutuo': 'APV-A', 'CantidadCustodia': 30, 'CantidadTransSolicitudesRescate': 30, 'IdEmpresa': 1, 'Nemotecnico': 'OTRO'}),
                         saldo()))
caso(184, K, 'fondo-sin-custodia-agrega-error-por-cliente-sin-custodia', 'an FFMM row with CantidadCustodia exactly 0: UI_ERROR_CLIENTE_SIN_CUSTODIA error with the nemotecnico (message id = instrument) and the error counter 1', L(KF, r'UI_ERROR_CLIENTE_SIN_CUSTODIA'),
     ek(), seed(COLK_BASE),
     colaboradores=merge(custo(tbl()), ffmm({'IdInstrumento': 7, 'IdPortafolio': 901, 'CantidadCustodia': 0, 'CantidadTransSolicitudesRescate': 0, 'IdEmpresa': 1, 'Nemotecnico': 'FONDO7'}), saldo()))
caso(185, K, 'portafolio-desconocido-lanza-NPE', 'a custody of a portfolio the information query does not return: informacionPortafolio.Regimen throws NullPointerException', L(KF, r'\("BF"\).equals\(informacionPortafolio.Regimen\)'),
     ek(), seed(COLK_BASE), colaboradores=merge(custo(tbl(idpo=999)), ffmm(), saldo()))
caso(186, K, 'filtra-por-regimen-producto-plan-y-portafolio', 'p_IdRegimen 1, p_IdProducto 1, p_IdPlan 1 and p_IdPortafolio 901 keep only the rows that match all four; a row of portfolio 902 (matrix 2: regimen 2) is dropped', L(KF, r'entrada.p_IdRegimen.equals\(0L\) \|\| entrada.p_IdRegimen.equals\(custodia.IdRegimenTributarioMatriz\)'),
     ek(p_IdRegimen=1, p_IdProducto=1, p_IdPlan=1, p_IdPortafolio=901), seed(COLK_BASE), colaboradores=merge(custo(tbl(), tbl(idpo=902, regimen='APV-B', cod='APV-B', idi=2)), ffmm(), saldo()))
caso(187, K, 'regimen-BF-marca-carga-extra-para-APV-A', 'a portfolio whose regimen is BF with CodigoRegimenTipoAhorro BF: CargaExtraParaAPVA S and it stays in the list when asking for regimen 0', L(KF, r'CargaExtraParaAPVA = \("BF"\).equals\(custodia.Regimen\)'),
     ek(), seed(COLK_BASE[:1], portafolio(904, 701, matriz=4), ins('BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV', IdMatrizAgrupadorAPV=4, IdPropositoPortafolio='PROPOSITOAPV', IdPlanTipoInversion=1, IdProductoAgrupador=1,
                                                                    IdRegimenTributario=4, CodigoAgrupador='GRUPO 1', IdEstado='VIG'),
          ins('BSCL.APV.BD::REC.RegimenTributario', IdRegimenTributario=4, Codigo='BF', IdEstado='VIG')),
     colaboradores=merge(custo(tbl(idpo=904, regimen='BF', cod='BF', tipo_ahorro='BF')), ffmm(), saldo()))
caso(188, K, 'tipo-de-ahorro-APV-incluye-la-carga-extra', 'p_TipoAhorro APV with regimen 0: rows whose CodigoRegimenTipoAhorro is APV, plus the BF rows flagged CargaExtraParaAPVA; a row of DC is dropped', L(KF, r'!\("0"\).equals\(entrada.p_TipoAhorro\)'),
     ek(p_TipoAhorro='APV'), seed(COLK_BASE), colaboradores=merge(custo(tbl(), tbl(idi=2, tipo_ahorro='DC')), ffmm(), saldo()))
caso(189, K, 'traspaso-filtra-por-instrumento-portafolio-y-regimen', 'p_IdTraspaso informed: only the custody rows covered by a DetalleOrigenTraspaso of that traspaso (same instrument, same portfolio or portfolio 0, same regimen text)', L(KF, r'obtenerInformacionTraspaso\(con,custodias,entrada.p_IdTraspaso\)'),
     ek(p_IdTraspaso=5001), seed(COLK_BASE, ins(T_DETORIGEN, IdDetalleOrigenTraspaso=1, IdTraspaso=5001, IdInstrumento=1021327, IdPortafolio=0, CodigoRegimenTributario='APV-A'),
                                 ins(T_DETORIGEN, IdDetalleOrigenTraspaso=2, IdTraspaso=5002, IdInstrumento=2, IdPortafolio=901, CodigoRegimenTributario='APV-A')),
     colaboradores=merge(custo(tbl(), tbl(idi=2, nemo='OTRO')), ffmm(), saldo()))
caso(190, K, 'fecha-explicita-se-pasa-a-los-colaboradores', 'an explicit p_FechaConsulta is passed as is to the three collaborators (the default 0001-01-01 is replaced by the system date: case 192)', L(KF, r'"0001-01-01".equals\(entrada.p_FechaConsulta\)'),
     ek(p_FechaConsulta='2025-02-14'), seed(COLK_BASE), colaboradores=merge(custo(tbl()), ffmm(), saldo()))
caso(191, K, 'sin-custodia-devuelve-totales-en-cero', 'no custody at all: empty list and zero totals', LK, ek(), seed(COLK_BASE), colaboradores=merge(custo(), ffmm(), saldo()))
caso(192, K, 'entrada-por-defecto-pasa-los-valores-del-constructor', 'only the mandatory data: the constructor defaults (IdCliente 0, TipoFechaConsulta LIQ, TipoAhorro "0", ...) travel in the collaborator inputs', L(KF, r'this.p_TipoAhorro = "0"'),
     dict(BASE), seed(COLK_BASE), colaboradores=merge(custo(tbl()), ffmm(), saldo()))

# ===================================================================================================== CargaMasivaArchivo
Z = 'CargaMasivaArchivo'
ZF = 'CargaMasivaArchivo.java'
SEQZ = dict(SEQ, **{'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva': 300})
TZ = [tabla(T_CARGA, '"IdCargaMasiva"'), tabla(T_CONTRATO, '"IdContratoSAAPV"', ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdCargaMasiva', 'IdEstado', 'IdPersona', 'IdCliente'])]
COLZ = {'SalidaProcedimiento': {SP_PORTAFOLIO: sp_ok(901)}}


def arch(nombre='carga-ficticia.csv', n=1, tipo=1):
    return {'IdEmpresa': 1, 'NombreCargaMasiva': nombre, 'CantidadRegistros': n, 'IdUsuario': 21, 'IdTipoCarga': tipo, 'Descripcion': 'carga de prueba', 'IdEstado': 'ING'}


def ez(archivos, parametros=None, **kw):
    d = dict(BASE)
    d['p_Archivo'] = archivos
    d['p_ParametrosContratos'] = parametros if parametros is not None else []
    d['p_DistribucionInversion'] = kw.pop('dis', [di()])
    d['p_PortafoliosModificados'] = kw.pop('pms', [pm()])
    d['p_DetalleOrigenTraspaso'] = []
    d.update(kw)
    return d


caso(360, Z, 'archivo-con-contrato-graba-con-el-id-de-carga', 'a file with one contract: the load row is inserted (id from the sequence, FechaCarga = system date), the contract is written through GrabarContratoSAAPV with IdCargaMasiva = that id, and its messages are copied',
     L(ZF, r'List<Long> idsCargaArchivo = Utils.getSecuencia', r'out.P_OUT_MENSAJES.add\(msg\);\s*$'),
     ez([arch()], [par()]), seed(PARTES_G), colaboradores=COLZ, secuencias=SEQZ, tablas=TZ, enmascarar_colaboradores=MASCARA)
caso(361, Z, 'archivo-sin-contratos-es-error', 'a file but an empty contract table: the load row is inserted and CARGA_CONTRATO_ERROR "La tabla de contrato viene vacia." is reported', L(ZF, r'La tabla de contrato viene vacia'),
     ez([arch()]), seed(PARTES_G), colaboradores=COLZ, secuencias=SEQZ, tablas=TZ)
caso(362, Z, 'sin-archivo-no-hace-nada', 'an empty p_Archivo: nothing inserted, no messages', L(ZF, r'if\(in.p_Archivo.size\(\) > 0\)\{'), ez([]), seed(PARTES_G), colaboradores=COLZ, secuencias=SEQZ, tablas=TZ)
caso(363, Z, 'dos-archivos-solo-el-primero-recibe-id', 'two file rows: the sequence gives two values but only the first row gets IdCargaMasiva and FechaCarga (the second is inserted with nulls) and only the first id is returned', L(ZF, r'in.p_Archivo.get\(0\).IdCargaMasiva = idsCargaArchivo.get\(0\)'),
     ez([arch('a.csv'), arch('b.csv', 2)], [par()]), seed(PARTES_G), colaboradores=COLZ, secuencias=SEQZ, tablas=TZ, enmascarar_colaboradores=MASCARA)
caso(364, Z, 'grabar-con-errores-copia-los-mensajes-uno-a-uno', 'the contract already exists: the FOLIO_CONTRATO_EXISTE message of GrabarContratoSAAPV is copied (type ADVERTENCIA because IdCargaMasiva is now set)', L(ZF, r'msg.descripcion = mensaje.descripcion'),
     ez([arch()], [par()]), seed(PARTES_G, contrato(9001, 'F-G1')), colaboradores=COLZ, secuencias=SEQZ, tablas=TZ, enmascarar_colaboradores=MASCARA)
caso(365, Z, 'contrato-sin-tipo-de-solicitud-lanza-error-capturado', 'a contract row without CodigoTipoSolicitud: GrabarContratoSAAPV throws NullPointerException inside the try, CARGA_CONTRATOS_ERROR is reported with the exception text and the load row stays (the service returns normally, the platform commits)',
     L(ZF, r'CARGA_CONTRATOS_ERROR'), ez([arch()], [{k: v for k, v in par().items() if k != 'CodigoTipoSolicitud'}]), seed(PARTES_G), colaboradores=COLZ, secuencias=SEQZ, tablas=TZ[:1], enmascarar_colaboradores=MASCARA)
