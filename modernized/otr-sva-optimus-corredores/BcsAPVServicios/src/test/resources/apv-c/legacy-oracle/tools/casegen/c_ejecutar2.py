"""Family C cases: EjecutarBonificacion, second part (APV-C-301 .. 339); the scenario builders live in c_ejecutar.py."""
from c_ejecutar import (BASE, F, MASK, OK_FFM, SEQ, TABLAS, T_CFGAPV, T_FON, T_POR, T_REGFMM, T_REGSERIE, T_SERIE, T_V_POR, TESO_DEP, bonificacion, custodia, ent, fondo, odt_bon, portafolios,
                        sc_cargo_abono, sc_custodia, sc_custodia_por_llamada, sc_portafolio_bf)
from fix import banco, caja, cuenta_banco, empresa, ins, msg
from lib import L, caso, seed
from pd import sc_ordenes, sc_precio, sc_solicitud, sol_resultado


def con(**kw):
    """A copy of OK_FFM with the given collaborator scripts replaced."""
    d = dict(OK_FFM)
    d.update(kw)
    return d


def base(bon=None, port=None, extra=(), fondo_=None, odt=True, emp=True):
    return [empresa() if emp else None, caja(), banco(18, 3018), cuenta_banco(137, 18, '333444'), odt_bon(odt), port if port is not None else portafolios(bf=950),
            bon if bon is not None else bonificacion(), fondo_ if fondo_ is not None else fondo() + fondo('CHILE', 'CLP', idfondo=3, idins=5001, con_regimen_fmm=False), list(extra)]


RV = custodia('CHILE', 100000, 'ACN', 'RV', 'CLP', 5001)
PRECIO = {'IdInstrumento': 5001, 'PrecioCierre': 2500, 'IdBolsa': 'BCS'}
CALC = {'IdInterno': 1, 'CantidadOperacion': 42.5}
RV_SCRIPTS = dict(sc_precio(PRECIO), **sc_ordenes(calculadas=[CALC]))
RV_VACIO = dict(sc_precio(PRECIO), **sc_ordenes(calculadas=[]))
NUEVO_BF = ins(T_POR, IdPortafolio=950, IdCliente=701, IdMatrizAgrupadorAPV=2, IdEstado='ACT', CodigoPortafolio='NUEVO')

caso(301, 'EjecutarBonificacion', 'rv-con-calculo-de-orden-falla-por-id-interno-nulo', 'defect: the order built for an RV custody never gets IdInterno, so when the quantity calculation answers an order the match loop throws NullPointerException; the inner catch answers ERROR_CREAR_CLIENTE_APV and no state change happens',
     L(F, r'Orden.IdTipoOperacion = "COMACN"'), ent(), seed(base()), secuencias=SEQ, colaboradores=dict(sc_custodia(RV), **sc_cargo_abono(), **TESO_DEP, **RV_SCRIPTS), tablas=TABLAS, enmascarar=MASK)
caso(302, 'EjecutarBonificacion', 'sin-portafolio-bf-lo-crea-por-procedimiento', 'no BF portfolio yet: the CrearPortafolio procedure is called, the new portfolio is renamed BF and the INFORMACION message CREACION_PORTAFOLIO_BF_EXITOSO (id = IdInterno) is added',
     L(F, r'msg.codigo = "CREACION_PORTAFOLIO_BF_EXITOSO"'), ent(), seed(base(port=portafolios(), extra=[NUEVO_BF])), secuencias=SEQ, colaboradores=con(**sc_portafolio_bf(950)), tablas=TABLAS, enmascarar=MASK)
caso(303, 'EjecutarBonificacion', 'error-al-crear-portafolio-bf-se-ignora', 'defect: the CrearPortafolio procedure answers errors; the error details are read but never reported, the process goes on with portfolio 0 and still ends in state EJE',
     L(F, r'for\(ErrorDetalleCall errorDetalle'), ent(), seed(base(port=portafolios())), secuencias=SEQ,
     colaboradores=con(**sc_portafolio_bf(0, errores=1, detalle=[{'Codigo': 'PORTAFOLIO_DUPLICADO', 'Descripcion': 'portafolio duplicado'}])), tablas=TABLAS, enmascarar=MASK)
caso(304, 'EjecutarBonificacion', 'sin-custodia-advierte-y-sigue', 'the client has no custody: ADVERTENCIA CLIENTE_NO_TIENE_CUSTODIA (id = IdInterno); no order and no FFMM request, but the deposit, the client detail, the cargo/abono and state EJE are still done',
     L(F, r'msg.codigo = "CLIENTE_NO_TIENE_CUSTODIA"'), ent(), seed(base()), secuencias=SEQ, colaboradores=con(**sc_custodia()), tablas=TABLAS, enmascarar=MASK)
caso(305, 'EjecutarBonificacion', 'custodia-en-dolares-se-excluye', 'a custody whose IdMonedaInstrReajuste is USD is removed before the distribution: only the CLP fund is requested',
     L(F, r'IdMonedaInstrReajuste.equals\("USD"\)'), ent(), seed(base(fondo_=fondo() + fondo('FFMM-B', 'USD', idfondo=2, idins=421061, con_regimen_fmm=False))), secuencias=SEQ,
     colaboradores=con(**sc_custodia(custodia(), custodia('FFMM-B', 50000, moneda='USD', idins=421061))), tablas=TABLAS, enmascarar=MASK)
caso(306, 'EjecutarBonificacion', 'fondo-sin-regimen-bf-no-se-solicita', 'the fund series has no BF regimen row: r == null, nothing is requested for it (no message)',
     L(F, r'if \(r!= null\)'), ent(), seed(base(fondo_=fondo(regimen=None))), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(307, 'EjecutarBonificacion', 'reparto-con-redondeo-half-down-ffm-y-half-up-rv', 'FFM 100 + RV 700 (total 800): 12.5% rounds to 12 for the fund (HALF_DOWN, scale 0) and 87.5% to 88 for the order (HALF_UP); amounts are the bonificacion 50000 times the integer percentage / 100',
     L(F, r'montoApagar2.divide\(TotalAPagar,RoundingMode.HALF_DOWN\)'), ent(), seed(base()), secuencias=SEQ,
     colaboradores=dict(sc_custodia(custodia(monto=100), custodia('CHILE', 700, 'ACN', 'RV', 'CLP', 5001)), **sc_cargo_abono(), **TESO_DEP, **sc_solicitud(resultados=[sol_resultado(1, 3001, 0)]), **RV_VACIO),
     tablas=TABLAS, enmascarar=MASK)
caso(308, 'EjecutarBonificacion', 'orden-rv-con-monto-cero-se-omite', 'an RV custody with MontoPPPCustodia 0 (not counted in the total) creates no order, while the fund keeps 100%',
     L(F, r'if\(filaresultadoCustodia.MontoPPPCustodia.compareTo\(BigDecimal.ZERO\)!=0\) \{', r'ConsultarInstrumentoPrecioBolsa consultarInstrumentoPrecioBolsa'), ent(), seed(base()), secuencias=SEQ,
     colaboradores=dict(sc_custodia(custodia(), custodia('CHILE', 0, 'ACN', 'RV', 'CLP', 5001)), **sc_cargo_abono(), **TESO_DEP, **sc_solicitud(resultados=[sol_resultado(1, 3001, 0)]), **RV_SCRIPTS),
     tablas=TABLAS, enmascarar=MASK)
caso(309, 'EjecutarBonificacion', 'sin-origen-destino-bon-el-deposito-falla', 'no BON transition: the deposit goes with bank/account 0 and the deposit service reports an error that is copied; the process goes on',
     L(F, r'msgREC.codigo = fila.codigo'), ent(), seed(base(odt=False)), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(310, 'EjecutarBonificacion', 'sin-empresa-lanza-IndexOutOfBounds', 'the company has no default portfolio row: _IdPortafolioDefecto.get(0) throws IndexOutOfBoundsException before any processing',
     L(F, r'_IdPortafolioDefecto.get\(0\).IdPortafolioDefecto'), ent(), seed(base(emp=False)), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(311, 'EjecutarBonificacion', 'bonificacion-inexistente-no-hace-nada', 'the id is not in the bonificacion view: nothing to process, empty output (no state change, no deposit)',
     L(F, r'if\(ListParamsRecaudacion.size\(\)==0\) return salida;'), ent((99,)), seed(base()), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(312, 'EjecutarBonificacion', 'cliente-sin-portafolio-apv-a-no-se-selecciona', 'the client has no active APV-A portfolio with APV purpose: the inner join drops the bonificacion, empty output',
     L(F, r'SQL_SELECT_ConsultarBonificacion, parametrosConsultaBonificacion'), ent(), seed(base(port=portafolios(idcli=999, idpor=909))), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(313, 'EjecutarBonificacion', 'dos-bonificaciones-se-procesan-en-orden-de-id', 'two bonificaciones requested as (2, 1): processed ascending by id; two deposits, two client details, cargo/abono numbering 1..4, both in state EJE',
     L(F, r'contadorId = contadorId\+1'), ent((2, 1)),
     seed(base(bon=bonificacion(1, 701) + bonificacion(2, 702, 40000, 60000, 10000), port=portafolios(bf=950) + portafolios(idcli=702, idpor=902, bf=951)[7:])), secuencias=SEQ,
     colaboradores=con(**sc_custodia_por_llamada([custodia()], [custodia(monto=200000, idpor=902)])), tablas=TABLAS, enmascarar=MASK)
caso(314, 'EjecutarBonificacion', 'error-del-aporte-ffmm-se-copia-por-solicitud', 'the FFMM contribution service answers an error message: it is copied once per request whose Glosa matches (id = IdInterno) and the counter adds the service errors',
     L(F, r'salida.P_OUT_ERRORESCONTADOR \+= rpt.P_OUT_ERRORESCONTADOR'), ent(), seed(base()), secuencias=SEQ,
     colaboradores=con(**sc_solicitud(mensajes=[msg(1, 'ERROR', 'FONDO_CERRADO', 'fondo cerrado')], errores=1)), tablas=TABLAS, enmascarar=MASK)
caso(315, 'EjecutarBonificacion', 'excepcion-del-aporte-ffmm-se-traga', 'defect: an exception of the FFMM contribution service is swallowed (stack trace only); the bonificacion still ends in EJE and the output is empty',
     L(F, r'catch \(Exception er1\)'), ent(), seed(base()), secuencias=SEQ, colaboradores=con(CrearSolicitudAporteRescateNormal={'lanza': 'servicio ffmm no disponible'}), tablas=TABLAS, enmascarar=MASK)
caso(316, 'EjecutarBonificacion', 'errores-de-ordenes-se-copian-tal-cual', 'the order service answers an error: its messages are copied with their own ids and the counter adds its errors',
     L(F, r'salida.P_OUT_MENSAJES.addAll\(OutOrdenes.P_OUT_MENSAJES\)'), ent(), seed(base()), secuencias=SEQ,
     colaboradores=dict(sc_custodia(RV), **sc_cargo_abono(), **TESO_DEP, **sc_precio(PRECIO), **sc_ordenes(calculadas=[], mensajes=[msg(1, 'ERROR', 'ORDEN_INVALIDA', 'orden invalida')], errores=1)),
     tablas=TABLAS, enmascarar=MASK)
caso(317, 'EjecutarBonificacion', 'excepcion-de-ordenes-se-traga', 'defect: an exception of the order service is swallowed; the bonificacion still ends in EJE',
     L(F, r'CrearOrdenes.CrearOrdenesSalida OutOrdenes = procesoOrdenes.logica'), ent(), seed(base()), secuencias=SEQ,
     colaboradores=dict(dict(sc_custodia(RV), **sc_cargo_abono(), **TESO_DEP, **sc_precio(PRECIO), **sc_ordenes(calculadas=[])), CrearOrdenes={'lanza': 'ordenes no disponible'}), tablas=TABLAS, enmascarar=MASK)
caso(318, 'EjecutarBonificacion', 'errores-de-cuenta-inversion-suman-pero-no-se-informan', 'defect: the cargo/abono errors are added to the counter but the translated message is built and never added to the output',
     L(F, r'salida.P_OUT_ERRORESCONTADOR \+= OutCuentaInversion.P_OUT_ERRORESCONTADOR'), ent(), seed(base()), secuencias=SEQ,
     colaboradores=con(**sc_cargo_abono(mensajes=[msg(1, 'ERROR', 'SIN_SALDO', 'sin saldo')], errores=1)), tablas=TABLAS, enmascarar=MASK)
caso(319, 'EjecutarBonificacion', 'excepcion-en-cuenta-inversion-informa-error-cliente', 'the cargo/abono service throws: the inner catch adds ERROR_CREAR_CLIENTE_APV (id 1, "Error al crear cliente"); the deposit and client detail already written stay, no state change',
     L(F, r'msg1.codigo = "ERROR_CREAR_CLIENTE_APV"'), ent(), seed(base()), secuencias=SEQ, colaboradores=con(CrearCargoAbono={'lanza': 'cuenta inversion no disponible'}), tablas=TABLAS, enmascarar=MASK)
caso(320, 'EjecutarBonificacion', 'excepcion-al-crear-deposito-hace-rollback-e-informa', 'the Tesoreria call of the deposit creation throws: the outer catch rolls back, adds ERROR_CREAR_RECAUDACION_APV (id 1) and returns',
     L(F, r'msg1.codigo = "ERROR_CREAR_RECAUDACION_APV"'), ent(), seed(base()), secuencias=SEQ, colaboradores=con(CrearIngresoEgreso={'lanza': 'tesoreria no disponible'}), tablas=TABLAS, enmascarar=MASK)
caso(321, 'EjecutarBonificacion', 'custodia-ffm-total-cero-lanza-ArithmeticException', 'a single fund custody of amount 0: TotalAPagar is 0 and the percentage division throws ArithmeticException (not caught): nothing is persisted',
     L(F, r'montoApagar2.divide\(TotalAPagar,RoundingMode.HALF_DOWN\)'), ent(), seed(base()), secuencias=SEQ, colaboradores=con(**sc_custodia(custodia(monto=0))), tablas=TABLAS, enmascarar=MASK)
caso(322, 'EjecutarBonificacion', 'custodia-sin-moneda-lanza-NullPointerException', 'IdMonedaInstrReajuste null in the custody: the USD filter calls equals on null and throws NullPointerException',
     L(F, r'IdMonedaInstrReajuste.equals\("USD"\)'), ent(), seed(base()), secuencias=SEQ, colaboradores=con(**sc_custodia(dict(custodia(), IdMonedaInstrReajuste=None))), tablas=TABLAS, enmascarar=MASK)
caso(323, 'EjecutarBonificacion', 'fondo-sin-moneda-lanza-IndexOutOfBounds', 'the fund series is in the BF regimen but not in the fund currency view: _IdMonedaFondo.get(0) throws IndexOutOfBoundsException',
     L(F, r'_IdMonedaFondo.get\(0\).IdMoneda'), ent(), seed(base(fondo_=[ins(T_FON, IdFondo=1, FondoExtranjero='N'), ins(T_SERIE, IdSerieFondo='FFMM-A', IdFondo=1),
                                                                       ins(T_REGFMM, IdRegimenFondoMutuo='BF', IdEstado='ACT', RescateConMandato='N'),
                                                                       ins(T_REGSERIE, IdRegimenFondoMutuo='BF', IdSerieFondo='FFMM-A', IdEmpresa=1, IdEstado='ACT')])), secuencias=SEQ,
     colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(324, 'EjecutarBonificacion', 'dos-filas-de-tolerancia-duplican-la-bonificacion', 'defect: the LEFT JOIN to PLA.ConfiguracionAPV (APV.ToleranciaReajuste) multiplies the bonificacion rows when the parameter has two rows: processed twice (two deposits)',
     L(F, r'SQL_SELECT_ConsultarBonificacion, parametrosConsultaBonificacion'),
     ent(), seed(base(extra=[ins(T_CFGAPV, CodigoParametro='APV.ToleranciaReajuste', ValorParametro='1'), ins(T_CFGAPV, CodigoParametro='APV.ToleranciaReajuste', ValorParametro='2')])),
     secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(325, 'EjecutarBonificacion', 'sin-parametros-lanza-NullPointerException', 'p_Parametros missing: the for over a null list throws NullPointerException after the transition query',
     L(F, r'for\(ParametrosBonificacion fila : in.p_Parametros\)'), {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV'}, seed(base()), secuencias=SEQ, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(326, 'EjecutarBonificacion', 'sin-fecha-de-sistema-lanza', 'PLATAFORMA/PLA.FechaSistema missing: the configuration read throws', L(F, r'ConfiguracionSistema.getParametroConfiguracionString'),
     ent(), seed(base()), secuencias=SEQ, config={'PLATAFORMA/PLA.FechaSistema': None}, colaboradores=OK_FFM, tablas=TABLAS, enmascarar=MASK)
caso(327, 'EjecutarBonificacion', 'portafolio-bf-inactivo-no-cuenta-como-existente', 'the client only has an inactive BF portfolio in the view: it is not found, so the CrearPortafolio procedure is called',
     L(F, r'if\(fila.IdPortafolioBonificacion == 0\)'),
     ent(), seed(base(port=portafolios(), extra=[ins(T_POR, IdPortafolio=940, IdCliente=701, IdMatrizAgrupadorAPV=2, IdEstado='INA', CodigoPortafolio='BF'),
                                                  ins(T_V_POR, IdPortafolio=940, IdCliente=701, IdMatrizAgrupadorAPV=2, IdEstado='INA', TienePropositoAPV='S'), NUEVO_BF])),
     secuencias=SEQ, colaboradores=con(**sc_portafolio_bf(950)), tablas=TABLAS, enmascarar=MASK)
caso(328, 'EjecutarBonificacion', 'dos-fondos-numeran-las-solicitudes', 'two fund custodies of 100000 and 300000: two FFMM requests numbered 1, 2 (IdInterno) with 25% and 75% of the bonificacion',
     L(F, r'AporteRescateFFMM.IdInterno = contadorFFMM'), ent(), seed(base(fondo_=fondo() + fondo('FFMM-B', 'CLP', idfondo=2, idins=421061, con_regimen_fmm=False))), secuencias=SEQ,
     colaboradores=dict(sc_custodia(custodia(), custodia('FFMM-B', 300000, idins=421061)), **sc_cargo_abono(), **TESO_DEP, **sc_solicitud(resultados=[sol_resultado(1, 3001, 0), sol_resultado(2, 3002, 0)])),
     tablas=TABLAS, enmascarar=MASK)
caso(329, 'EjecutarBonificacion', 'rv-sin-calculo-de-cantidad-crea-la-orden', 'one RV custody and the quantity calculation answering no order: the purchase order is created with Cantidad 0 (COMACN, bolsa BCS, amount = the whole bonificacion), no FFMM request',
     L(F, r'CrearOrdenes.CrearOrdenesEntrada paramsOrdenesEntrada'), ent(), seed(base()), secuencias=SEQ, colaboradores=dict(sc_custodia(RV), **sc_cargo_abono(), **TESO_DEP, **RV_VACIO), tablas=TABLAS, enmascarar=MASK)
