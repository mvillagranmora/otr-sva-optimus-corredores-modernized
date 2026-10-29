"""Family C cases: EjecutarBonificacion (APV-C-300 .. 339)."""
from fix import T_INVBON, T_ODT, T_REC, T_RCLI, T_V_FONDO, banco, caja, cuenta_banco, empresa, ins, msg, res_proceso, salida_ing_egreso
from lib import L, caso, seed, tabla
from pd import T_V_CTAS, T_V_PCB, sc_ordenes, sc_precio, sc_solicitud, sol_resultado, transiciones

F = 'EjecutarBonificacion.java'
T_V_BON = 'BSCL.APV.BD.Vistas::INV.ConsultarBonificacion'
T_V_POR = 'BSCL.Portafolio.BD.Vistas::ConsultarPortafolio'
T_V_REGTRI = 'BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario'
T_REGTRI = 'BSCL.APV.BD::REC.RegimenTributario'
T_MATRIZ = 'BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV'
T_POR = 'BSCL.Portafolio.BD::POR.Portafolio'
T_PROD = 'BSCL.APV.BD::APV.ProductoAgrupador'
T_FON = 'BSCL.FondoMutuo.BD::FMM.Fondo'
T_SERIE = 'BSCL.FondoMutuo.BD::FMM.SerieDeFondo'
T_REGFMM = 'BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo'
T_REGSERIE = 'BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo'
T_CFGAPV = 'BSCL.APV.BD::PLA.ConfiguracionAPV'
ING = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'
SP_CREAR = 'BSCL.Portafolio.BD.Servicios::CrearPortafolio'
SEQ = {'BSCL.APV.BD.Secuencia::REC.IdRecaudacion': 960, 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente': 520}
MASK = ['*IdTrabajo']

TBON = tabla(T_INVBON, '"IdBonificacion"', columnas=['IdBonificacion', 'IdCliente', 'IdEstado', 'MontoCalculado'])
TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'IdEmpresa', 'MontoDeposito', 'Origen', 'IdBanco', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdCliente', 'IdRegimenTriburario', 'MontoDepositoAPV', 'Origen', 'IdEstado'])
TPOR = tabla(T_POR, '"IdPortafolio"', columnas=['IdPortafolio', 'CodigoPortafolio', 'IdCliente'])
TABLAS = [TBON, TREC, TCLI, TPOR]


def ent(ids=(1,)):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': [{'IdBonificacion': i} for i in ids]}


def custodia(nemo='FFMM-A', monto=100000, tipo='FFM', clasif='FM', moneda='CLP', idins=421060, idpor=901):
    return {'IdPortafolio': idpor, 'IdInstrumento': idins, 'Nemotecnico': nemo, 'CantidadCustodia': 100, 'MontoMercadoCustodia': monto, 'IdRegimenTributario': 'APV-A',
            'IdClasificacionTipoInstrumento': clasif, 'IdTipoInstrumento': tipo, 'Producto': 'APV FONDOS MUTUOS', 'CodigoRegimenTipoAhorro': 'APV', 'CodigoPortafolio': 'APV-A',
            'PPPCustodia': 10, 'ValorPPPCustodia': 10, 'MontoPPPCustodia': monto, 'IdEmisor': 'EMI1', 'IdMonedaInstrReajuste': moneda, 'NombreCliente': 'ANA PEREZ SOTO',
            'DocumentoIdentidad': '11111111-1'}


def sc_custodia_por_llamada(*llamadas):
    """Like sc_custodia but the k-th call to the custody service answers llamadas[k] (a list of custody rows)."""
    d = sc_custodia()
    d['ConsultarCustodiaPortafolio'] = {'salidas': [{'P_OUT_TBL': list(l), 'P_OUT_MENSAJES': [], 'P_OUT_ERRORESCONTADOR': 0} for l in llamadas]}
    return d


def sc_custodia(*custodias, ffmm=()):
    """The three collaborators of ConsultarCustodiaPortafolioAPV that live outside APV: custody, fund custody and cash balance."""
    return {'ConsultarCustodiaPortafolio': {'salida': {'P_OUT_TBL': list(custodias), 'P_OUT_MENSAJES': [], 'P_OUT_ERRORESCONTADOR': 0}},
            'ConsultarCustodiaPortafolioFondoMutuo': {'salida': {'P_OUT_TBL': list(ffmm)}},
            'ConsultarSaldoCuentaInversion': {'salida': {'P_OUT_SALDOINICIALFINAL': [], 'P_OUT_SALDOPORFECHAEFECTIVA': [{'IdPortafolio': 901, 'IdMoneda': 'CLP', 'Saldo': 5000}],
                                                'P_OUT_SALDOPORFECHAMOVIMIENTO': [], 'P_OUT_INFORMACIONCONTADOR': 0, 'P_OUT_INFORMACIONDETALLE': [], 'P_OUT_ADVERTENCIASCONTADOR': 0,
                                                'P_OUT_ADVERTENCIASDETALLE': [], 'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_ERRORESDETALLE': []}}}


def sc_cargo_abono(mensajes=(), errores=0):
    return {'CrearCargoAbono': {'salida': {'P_OUT_MENSAJES': list(mensajes), 'P_OUT_CARGOABONOGENERADO': [], 'P_OUT_ERRORESCONTADOR': errores}}}


def sc_portafolio_bf(refid=950, errores=0, detalle=()):
    return {'StoredProcedure': {SP_CREAR: {'salida': {'p_out_RefId': refid, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': errores, 'p_out_ErroresDetalle': list(detalle),
                                                      'p_out_AdvertenciasDetalle': []}}}}


TESO_DEP = {'CrearIngresoEgreso': {'salida': salida_ing_egreso(generados=[{'NumeroReferencia': 1, 'IdIngresoEgreso': 7001, 'IdTipoMovimiento': 'MOVING'}],
                                                              resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1)])}}


def bonificacion(idb=1, idcli=701, calculado=50000, reajustado=70000, neto=15000, idpor_bf=None, estado='EJE_PEND'):
    return [ins(T_INVBON, IdBonificacion=idb, IdCliente=idcli, IdEmpresa=1, IdEstado='PRO', MontoCalculado=calculado, MontoReajustadoPagadoAfiliado=reajustado,
                MontoBonificacionAfiliadoNeto=neto, AnoAhorro=2024),
            ins(T_V_BON, IdBonificacion=idb, AnoAhorro=2024, AnoProceso=2025, MontoPagadoNeto=1, MontoSaldoNeto=1, MontoPagadoReajustado=1, MontoReajustadoPagadoAfiliado=reajustado,
                MontoCalculado=calculado, IdPersona=500 + idcli, IdCliente=idcli, MontoBonificacionAfiliadoNeto=neto, PA1=1, PA2=0, PA3=0, MontoAfiliadoNeto=1, IdEstado='PRO')]


def portafolios(idcli=701, idpor=901, bf=None):
    """APV-A portfolio of the client (matrix 1) and, when `bf` is an id, the existing BF portfolio (matrix 2)."""
    out = [ins(T_REGTRI, IdRegimenTributario=1, Codigo='APV-A', IdEstado='VIG'), ins(T_REGTRI, IdRegimenTributario=2, Codigo='BF', IdEstado='VIG'),
           ins(T_MATRIZ, IdMatrizAgrupadorAPV=1, IdRegimenTributario=1, IdProductoAgrupador=1, IdPlanTipoInversion=1),
           ins(T_MATRIZ, IdMatrizAgrupadorAPV=2, IdRegimenTributario=2, IdProductoAgrupador=1, IdPlanTipoInversion=1),
           ins(T_PROD, IdProductoAgrupador=1, NombreProductoAgrupador='APV FONDOS MUTUOS', IdEstado='VIG'),
           ins(T_V_REGTRI, IdMatrizAgrupadorAPV=1, IdRegimenTributario=1, IdPropositoPortafolio='PROPOSITOAPV', NombrePlan='ABIERTO', CodigoAgrupador='APV-A'),
           ins(T_V_REGTRI, IdMatrizAgrupadorAPV=2, IdRegimenTributario=2, IdPropositoPortafolio='PROPOSITOAPV', NombrePlan='ABIERTO', CodigoAgrupador='BF'),
           ins(T_POR, IdPortafolio=idpor, IdCliente=idcli, IdMatrizAgrupadorAPV=1, IdEstado='ACT', CodigoPortafolio='APV-A', Proposito='PROPOSITOAPV'),
           ins(T_V_POR, IdPortafolio=idpor, IdCliente=idcli, IdMatrizAgrupadorAPV=1, IdEstado='ACT', TienePropositoAPV='S', IdPerfilComercialBaseOrigen='PERFIL BASE')]
    if bf:
        out += [ins(T_POR, IdPortafolio=bf, IdCliente=idcli, IdMatrizAgrupadorAPV=2, IdEstado='ACT', CodigoPortafolio='BF', Proposito='PROPOSITOAPV'),
                ins(T_V_POR, IdPortafolio=bf, IdCliente=idcli, IdMatrizAgrupadorAPV=2, IdEstado='ACT', TienePropositoAPV='S', IdPerfilComercialBaseOrigen='PERFIL BASE')]
    return out


def fondo(nemo='FFMM-A', moneda='CLP', regimen='BF', idfondo=1, idins=421060, con_regimen_fmm=True):
    """The series with its BF regimen (FondoMutuo SQL ComunesSolicitudes_SelectRegimenSerieDeFondo; the legacy asks it for every custody, shares too) and the fund
    currency view. The regimen master row is shared by every series: only the first call inserts it."""
    out = [ins(T_FON, IdFondo=idfondo, FondoExtranjero='N'), ins(T_SERIE, IdSerieFondo=nemo, IdFondo=idfondo), ins(T_V_FONDO, IdInstrumento=idins, IdSerieFondo=nemo, IdMoneda=moneda, RestringeAporte='N')]
    if regimen:
        if con_regimen_fmm:
            out.append(ins(T_REGFMM, IdRegimenFondoMutuo=regimen, IdEstado='ACT', RescateConMandato='N'))
        out.append(ins(T_REGSERIE, IdRegimenFondoMutuo=regimen, IdSerieFondo=nemo, IdEmpresa=1, IdEstado='ACT'))
    return out


def odt_bon(con_bon=True):
    out = [transiciones(rez=False, dis=False)]
    if con_bon:
        out.append(ins(T_ODT, IdOrigenDestinoTransicion=5, IdEmpresa=1, Operacion='BON', OrigenCtaBco=136, SocioNegocioCtaBco=100, DestinoCtaBco=137, MueveCuentaCorriente='S',
                       MueveCuentaInversion='S', IdEstado='ACT'))
    return out


BASE = [empresa(), caja(), banco(18, 3018), cuenta_banco(137, 18, '333444'), odt_bon(), portafolios(bf=950), bonificacion(), fondo()]
OK_FFM = dict(sc_custodia(custodia()), **sc_cargo_abono(), **TESO_DEP, **sc_solicitud(resultados=[sol_resultado(1, 3001, 0)]))

caso(300, 'EjecutarBonificacion', 'bonificacion-ffmm-con-portafolio-bf-existente', 'one bonificacion, FFM custody, BF portfolio already exists: BF deposit + client detail, cargo/abono pair, FFMM contribution request, state EJE',
     L(F, 'Bon_update.IdEstado				= "EJE"'), ent(), seed(BASE), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
