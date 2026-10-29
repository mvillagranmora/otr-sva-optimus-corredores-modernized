"""Cases of ValidaContratoSAAPV (ids 210-249)."""
from lib import *  # noqa: F401,F403

V = 'ValidaContratoSAAPV'
VF = 'ValidaContratoSAAPV.java'
BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}


def e(**kw):
    d = dict(BASE)
    d.update(kw)
    return d


def er(**kw):
    """entrada of part 2: an empty contract list (the constructor default [null] would throw before reaching the recaudaciones)."""
    return e(p_ContratosSAAPV=[], **kw)


V_REC = 'BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido'
V_CLI = 'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente'
V_GEST = 'BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV'
V_DIS = 'BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion'
V_DET = 'BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV'
T_SERIE = 'BSCL.FondoMutuo.BD::FMM.SerieDeFondo'

# parties: persona 501 (cliente 701, doc 11111111-1, one APV-A portfolio 901), employer 801 (doc 76000000-0, tipo J)
PARTES = [persona(501, '11111111-1', idc=701), persona(801, '76000000-0', nombres='', paterno='', materno='', tipo='J', razon='EMPRESA UNO'),
          portafolio(901, 701)]
TC = [tabla(T_CONTRATO, '"IdContratoSAAPV"', ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdPersona', 'IdCliente', 'IdEmpleador', 'IdPortafolio'])]


def c(folio='F-V1', **kw):
    d = {'FolioContratoSAAPV': folio, 'DocumentoIdentidad': '11111111-1', 'DocumentoIdentidadEmpleador': '', 'Codigo': 'APV-A',
         'NombrePlan': 'ABIERTO', 'CodigoAgrupador': 'GRUPO 1'}
    d.update(kw)
    return d


# ------------------------------------------------------------------------------------------------------ part 1: ids of the contracts
caso(210, V, 'ingreso-resuelve-persona-cliente-y-portafolio', 'option I: person, client and portfolio are resolved from the document; a free folio is answered as empty',
     L(VF, 'if\\(fila.IdPersona == null\\)', 'out.P_IDSCONTRATO.add\\(mod\\)'),
     e(p_Opcion='I', p_ContratosSAAPV=[c()]), seed(PARTES), tablas=TC)
caso(211, V, 'ingreso-folio-existente-se-devuelve', 'option I: a folio that already exists is echoed (the caller reads it as FOLIO_CONTRATO_EXISTE)',
     L(VF, 'existeFolioContrato'), e(p_Opcion='I', p_ContratosSAAPV=[c('F-V1')]), seed(PARTES, contrato(9001, 'F-V1')), tablas=TC)
caso(212, V, 'ingreso-persona-inexistente', 'option I: unknown document: no person, no client, no portfolio',
     L(VF, 'if\\(existePersona\\)'), e(p_Opcion='I', p_ContratosSAAPV=[c(DocumentoIdentidad='99999999-9')]), seed(PARTES), tablas=TC)
caso(213, V, 'ids-informados-no-se-consultan-ni-se-devuelven', 'ids already informed are not queried and employer/client/portfolio are NOT echoed (only IdPersona is)',
     L(VF, 'Ids.IdPersona = fila.IdPersona'),
     e(p_Opcion='I', p_ContratosSAAPV=[c(IdPersona=501, IdEmpleador=801, IdCliente=701, IdPortafolio=901)]), seed(PARTES), tablas=TC)
caso(214, V, 'empleador-por-documento', 'employer resolved by document (tipo J) when IdEmpleador is null and the document is not empty',
     L(VF, 'params.put\\("TipoPersona", "J"\\)'), e(p_Opcion='I', p_ContratosSAAPV=[c(DocumentoIdentidadEmpleador='76000000-0')]), seed(PARTES), tablas=TC)
caso(215, V, 'empleador-con-documento-vacio-no-se-busca', 'employer document "" skips the lookup (a null document would query and find nothing)',
     L(VF, 'fila.IdEmpleador == null &&'), e(p_Opcion='I', p_ContratosSAAPV=[c(DocumentoIdentidadEmpleador='')]), seed(PARTES), tablas=TC)
caso(216, V, 'portafolio-duplicado-descripcion', 'two matching portfolios: no IdPortafolio and the description "Existe mas de un Portafolio con proposito APV."',
     L(VF, 'Existe mas de un Portafolio'), e(p_Opcion='I', p_ContratosSAAPV=[c()]), seed(PARTES, portafolio(902, 701)), tablas=TC)
caso(217, V, 'portafolio-inexistente', 'no portfolio of that regimen/plan/agrupador: IdPortafolio and Codigo stay null',
     L(VF, 'existePortafolio && contadorPortafolio.size'), e(p_Opcion='I', p_ContratosSAAPV=[c(Codigo='APV-B')]), seed(PARTES), tablas=TC)
caso(218, V, 'modificar-actualiza-ids-del-contrato', 'option M: the resolved ids are written to the contract of that folio (the batch re-applies the accumulated list)',
     L(VF, 'in.p_Opcion.compareTo\\("M"\\)'), e(p_Opcion='M', p_ContratosSAAPV=[c('F-V1'), c('F-V2')]),
     seed(PARTES, contrato(9001, 'F-V1', IdPersona=None, IdCliente=None, IdEmpleador=None), contrato(9002, 'F-V2', IdPersona=None, IdCliente=None, IdEmpleador=None)),
     tablas=TC)
caso(219, V, 'modificar-sin-ids-conserva-los-valores', 'option M with nothing resolved: the CASE keeps the stored ids',
     L(VF, 'in.p_Opcion.compareTo\\("M"\\)'), e(p_Opcion='M', p_ContratosSAAPV=[c('F-V1', DocumentoIdentidad='99999999-9')]),
     seed(PARTES, contrato(9001, 'F-V1', IdPersona=555, IdCliente=666, IdEmpleador=777)), tablas=TC)
caso(220, V, 'ingreso-con-folio-nulo-lanza-NPE', 'option I with a null folio: fila.FolioContratoSAAPV.equals("") throws NullPointerException (legacy behaviour)',
     L(VF, 'fila.FolioContratoSAAPV != null \\|\\|'), e(p_Opcion='I', p_ContratosSAAPV=[{'DocumentoIdentidad': '11111111-1', 'Codigo': 'APV-A', 'NombrePlan': 'ABIERTO', 'CodigoAgrupador': 'GRUPO 1'}]),
     seed(PARTES), tablas=TC)
caso(221, V, 'ingreso-con-folio-vacio', 'option I with folio "": the existence query ignores empty folios, the answer is an empty folio',
     L(VF, 'existeFolioContrato'), e(p_Opcion='I', p_ContratosSAAPV=[c('')]), seed(PARTES, contrato(9001, '')), tablas=TC)
caso(222, V, 'reapertura-tambien-valida-el-folio', 'option R behaves as I for the folio check', L(VF, 'in.p_Opcion.equals\\("I"\\) \\|\\| in.p_Opcion.equals\\("R"\\)'),
     e(p_Opcion='R', p_ContratosSAAPV=[c('F-V1')]), seed(PARTES, contrato(9001, 'F-V1')), tablas=TC)
caso(223, V, 'otra-opcion-no-toca-el-folio', 'any other option (here C) answers the folio as sent and writes nothing',
     L(VF, 'Ids.FolioContratoSAAPV = fila.FolioContratoSAAPV'), e(p_Opcion='C', p_ContratosSAAPV=[c('F-V1')]), seed(PARTES, contrato(9001, 'F-V1')), tablas=TC)
caso(224, V, 'opcion-nula-lanza-NPE', 'null option: in.p_Opcion.equals throws NullPointerException after the lookups', L(VF, 'in.p_Opcion.equals\\("I"\\)'),
     e(p_ContratosSAAPV=[c()]), seed(PARTES), tablas=TC)
caso(225, V, 'lista-de-contratos-ausente-lanza-NPE', 'p_ContratosSAAPV not informed: the constructor default is [null] and fila.FolioContratoSAAPV throws NullPointerException',
     L(VF, 'p_ContratosSAAPV.add\\(null\\)'), e(p_Opcion='I'), seed(PARTES), tablas=TC)
caso(226, V, 'lista-de-contratos-vacia', 'empty contract list and no recaudacion: empty answers', L(VF, 'in.p_ContratosSAAPV.size\\(\\) > 0'),
     e(p_Opcion='I', p_ContratosSAAPV=[]), seed(PARTES), tablas=TC)


# ------------------------------------------------------------------------------------------------------ part 2: validation of the recaudaciones
def rec(idrc=11, folio=1, idre=0, rut_emp='', monto=100000, regimen='APV-A', tipo='APV', idcli=701):
    return ins(V_REC, FolioRecaudacion=folio, IdRecaudacionCliente=idrc, IdRecaudacionEmpleador=idre, RutEmpleador=rut_emp, RecMonto=monto,
               TipoRegimen=tipo, IdCliente=idcli, Regimen=regimen, RutCliente='11111111-1', CodigoEstado='REZ', FechaDeposito='2025-03-15',
               MontoRecaudacion=monto)


def gest(idc=9001, idpo=901, medio=1, rut_emp='', pago='IND', termino='2025-12', monto=100000, origen='APV', regimen='APV-A', est_po='ACT',
         est_con='VIG', forma='DIRECTA'):
    return ins(V_GEST, IdPortafolio=idpo, IdContratoSAAPV=idc, IdMedioRecaudacionFormaPago=medio, RutEmpleador=rut_emp, Pago=pago,
               PeriodoTermino=termino, MontoPesos=monto, OrigenAhorro=origen, FormaDePago=forma, RegimenTributario=regimen,
               IdEstadoPortafolio=est_po, IdEstadoNuevo=est_con)


def asig(idrc=11, idc=9001):
    return ins(T_ASIGN, IdRecaudacionCliente=idrc, IdContratoSAAPV=idc, FechaCreacion='2025-03-16 10:00:00', FechaModificacion='2025-03-16 10:00:00')


def cli_est(idcli=701, est='ACT'):
    return ins(V_CLI, IdCliente=idcli, IdEstado=est)


def rc(idrc=11, idr=1, **kw):
    d = {'IdRecaudacion': idr, 'IdRecaudacionEmpleador': 0, 'IdRecaudacionCliente': idrc, 'Monto': 100000, 'IdEstadoRecaudacionCliente': 'REZ', 'Pago': 'IND'}
    d.update(kw)
    return d


OKRC = [rec(), gest(), asig(), cli_est()]
LREC = L(VF, 'if\\(in.p_RecaudacionCliente != null && in.p_RecaudacionCliente.size', 'out.P_IDSRECAUDACION.add\\(_detalleValSAAPV\\)')

caso(230, V, 'recaudacion-valida-ok', 'a recaudacion whose single contract passes every check: ValidacionOK with portfolio, contract, regimen and pago', LREC,
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed(OKRC))
caso(231, V, 'sin-detalle-cliente', 'IdRecaudacionCliente 0 and no rows: "No tiene detalle cliente asociado."', L(VF, 'No tiene detalle cliente asociado'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc(idrc=0)]), seed(OKRC))
caso(232, V, 'sin-contratos-vigentes', 'a recaudacion without rows: "No tiene contratos vigentes."', L(VF, 'No tiene contratos vigentes'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc(idrc=12)]), seed(OKRC))
caso(233, V, 'empleador-no-coincide-y-configuracion-N', 'medio 1 (direct) with an employer RUT in the recaudacion and REC.ValidacionSAAPV_Empleador=N: message "no es por empleador"',
     L(VF, 'la recaudacion no es por empleador'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(rut_emp='76000000-0'), gest(), asig(), cli_est()]), config={'APV/REC.ValidacionSAAPV_Empleador': 'N'})
caso(234, V, 'empleador-no-coincide-y-configuracion-S-no-avisa', 'same data with the parameter at S: the NOK is ignored',
     L(VF, 'valorValEMPLEADOR.compareTo\\("N"\\)'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(rut_emp='76000000-0'), gest(), asig(), cli_est()]))
caso(235, V, 'periodo-termino-futuro-no-avisa-vencido-defecto', 'LEGACY DEFECT: Pago TEM with PeriodoTermino after the system period is NOK in the SQL, but the Java compares the period TEXT with "NOK" (never equal): no "vencido" message',
     L(VF, 'El contrato se encuentra vencido'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(), gest(pago='TEM', termino='2025-12'), asig(), cli_est()]))
caso(236, V, 'periodo-termino-pasado-ok', 'Pago TEM with PeriodoTermino <= system period 2025-04: OK', L(VF, 'valorValPERIODO_TERMINO.compareTo\\("S"\\) == 0\\)\\{'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(), gest(pago='TEM', termino='2025-04'), asig(), cli_est()]))
caso(237, V, 'cliente-bloqueado', 'client state different from ACT: "Cliente bloqueado."', L(VF, 'Cliente bloqueado'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(), gest(), asig(), cli_est(est='BLO')]))
caso(238, V, 'portafolio-bloqueado', 'portfolio state different from ACT: "Portafolio bloqueado."', L(VF, 'Portafolio bloqueado'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(), gest(est_po='BLO'), asig(), cli_est()]))
caso(239, V, 'fondo-restringido', 'the distribution has a fund that restricts contributions (RestringeAporte S): "Fondo restringido para aportes. " (with the trailing space)',
     L(VF, 'Fondo restringido para aportes'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]),
     seed([rec(), gest(), asig(), cli_est(), ins(V_DIS, IdContratoSAAPV=9001, Nemotecnico='FFMM-A'), ins(T_SERIE, IdSerieFondo='FFMM-A', RestringeAporte='S')]))
caso(240, V, 'varios-motivos-se-concatenan', 'blocked client + blocked portfolio + restricted fund: the messages are concatenated in the legacy order',
     L(VF, 'Cliente bloqueado'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]),
     seed([rec(), gest(est_po='BLO'), asig(), cli_est(est='BLO'), ins(V_DIS, IdContratoSAAPV=9001, Nemotecnico='FFMM-A'), ins(T_SERIE, IdSerieFondo='FFMM-A', RestringeAporte='S')]))
caso(241, V, 'dos-recaudaciones-una-valida-y-una-sin-contrato', 'two recaudacion clients in one call: one OK, one without rows', LREC,
     er(p_Opcion='V', p_RecaudacionCliente=[rc(idrc=11, idr=1), rc(idrc=12, idr=2)]), seed(OKRC))
caso(242, V, 'varias-filas-de-distribucion-no-cambian-el-resultado', 'two distribution rows of the same contract: DISTINCT keeps both, the contract is still the single Asignado=OK one',
     L(VF, 'Asignado.compareTo\\("OK"\\)'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]),
     seed(OKRC, ins(V_DIS, IdContratoSAAPV=9001, Nemotecnico='FFMM-A'), ins(V_DIS, IdContratoSAAPV=9001, Nemotecnico='FFMM-B'),
          ins(T_SERIE, IdSerieFondo='FFMM-A', RestringeAporte='N'), ins(T_SERIE, IdSerieFondo='FFMM-B', RestringeAporte='S')))
caso(227, V, 'dos-contratos-uno-asignado-gana-el-asignado', 'two candidate contracts for one recaudacion, the second one is the assigned one (Asignado OK): the loop keeps the assigned contract (9002) only',
     L(VF, 'Asignado.compareTo\("OK"\)'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]),
     seed([rec(), gest(idc=9001, idpo=901), gest(idc=9002, idpo=902), asig(idc=9002), cli_est()]))
caso(228, V, 'dos-contratos-sin-asignar-no-son-candidatos', 'two contracts of the client but none assigned to the recaudacion: the SQL returns no row for it, so "No tiene contratos vigentes." (the size>=2 branch is not reached)',
     L(VF, 'contadorOK= contadorOK\+1'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]),
     seed([rec(), gest(idc=9001, idpo=901), gest(idc=9002, idpo=902), cli_est()]))
caso(229, V, 'dos-contratos-sin-asignar-con-portafolio-bloqueado', 'same without assignment and one blocked portfolio: still no candidate, same message',
     L(VF, 'IdEstadoPortafolio.compareTo\("NOK"\)'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]),
     seed([rec(), gest(idc=9001, idpo=901, est_po='BLO'), gest(idc=9002, idpo=902), cli_est()]))
caso(243, V, 'recaudacion-sin-id-lanza-NPE', 'a recaudacion without IdRecaudacion: fila.IdRecaudacion.toString() throws NullPointerException', L(VF, 'A VALIDAR FOLIO_RECAUDACION'),
     er(p_Opcion='V', p_RecaudacionCliente=[{'IdRecaudacionCliente': 11, 'Monto': 100000}]), seed(OKRC))
caso(244, V, 'recaudacion-ya-asignada-a-otro-regimen', 'the contract regimen differs from the recaudacion regimen: the join finds no contract row',
     L(VF, 'No tiene contratos vigentes'),
     er(p_Opcion='V', p_RecaudacionCliente=[rc()]), seed([rec(regimen='APV-B'), gest(), asig(), cli_est()]))

# ------------------------------------------------------------------------------------------------------ RECTRA (recaudacion de traspaso)
LRT = L(VF, 'INICIA VALIDACION RECAUDACION TRASPASO', 'FIN VALIDACION RECAUDACION TRASPASO')
caso(245, V, 'rectra-contrato-con-monto-ok', 'option RECTRA: a contract row with Validacion_Monto OK produces one detail OK per portfolio', LRT,
     er(p_Opcion='RECTRA', p_RecaudacionCliente=[rc()]), seed(OKRC))
caso(246, V, 'rectra-monto-distinto-no-genera-detalle', 'RECTRA: Validacion_Monto NOK (amounts differ): no detail', LRT,
     er(p_Opcion='RECTRA', p_RecaudacionCliente=[rc()]), seed([rec(monto=100000), gest(monto=90000), asig(), cli_est()]))
caso(247, V, 'rectra-mismo-portafolio-solo-una-vez', 'RECTRA: two contract rows of the same portfolio: the second is skipped (IdPortafolioRepetido)', LRT,
     er(p_Opcion='RECTRA', p_RecaudacionCliente=[rc()]),
     seed([rec(), gest(), asig(), cli_est(), ins(V_DIS, IdContratoSAAPV=9001, Nemotecnico='FFMM-A'), ins(V_DIS, IdContratoSAAPV=9001, Nemotecnico='FFMM-B')]))
caso(248, V, 'rectra-reapertura-usa-el-detalle-de-traspaso', 'RECTRA with a COM contract and detail ids: portfolio, contract and regimen come from the traspaso view',
     L(VF, 'Caso Reapertura Traspaso'),
     er(p_Opcion='RECTRA', p_RecaudacionCliente=[rc()], p_IdsDetalleTraspasoReapertura=[501]),
     seed([rec(), gest(est_con='COM'), asig(), cli_est(), ins(V_DET, IdPortafolio=902, IdContratoSAAPV=9100, Codigo='APV-B', EsReapertura='S', IdDetalleTraspaso=501)]))
caso(249, V, 'rectra-reapertura-detalle-inexistente-lanza-IndexOutOfBounds', 'RECTRA reapertura with a detail id the view does not know: detalleTraspaso.get(0) throws IndexOutOfBoundsException',
     L(VF, 'detallesTraspasoReapertura.add\\(detalleTraspaso.get\\(0\\)\\)'),
     er(p_Opcion='RECTRA', p_RecaudacionCliente=[rc()], p_IdsDetalleTraspasoReapertura=[999]), seed(OKRC))
