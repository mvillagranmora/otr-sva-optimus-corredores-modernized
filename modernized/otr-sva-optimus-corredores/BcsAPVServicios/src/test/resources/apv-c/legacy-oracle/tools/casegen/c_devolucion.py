"""Family C cases: DevolucionPagoRezagos (APV-C-240 .. 259) and DevolucionBF_EntidadRecaudadora (APV-C-260 .. 269)."""
from fix import T_INVBON, T_RCLI, T_REC, T_REMP, T_TRASPASO, empresa, ins, msg, rcli, rec, res_proceso, salida_ing_egreso
from lib import L, caso, seed, tabla
from pd import CRE, crd, sc_traspaso_ok, transiciones

T_CTA = 'BSCL.SocioNegocio.BD::PER.CuentaBancaria'
T_TIPOCTA = 'BSCL.Plataforma.BD::BAN.TipoCuentaBancaria'
T_DET = 'BSCL.APV.BD::TRA.DetalleOrigenTraspaso'
T_V_ENT = 'BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora'
MASK = ['*IdTrabajo']

# ------------------------------------------------------------------------------------------------ DevolucionPagoRezagos
F = 'DevolucionPagoRezagos.java'
TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdEstado'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'IdEstado'])
TABLAS = [TREC, TEMP, TCLI]


def ent(ids=(11,), banco=300, medio='TRA', persona=900):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_DevolucionTipoPersona': 'OTROS', 'p_DevolucionIdPersona': persona, 'p_DevolucionIdPortafolio': 100027,
            'p_DevolucionIdMedioPago': medio, 'p_DevolucionIdBanco': banco, 'p_DevolucionIdCuentaCorriente': 0, 'p_Parametros': [{'IdRecaudacionCliente': i} for i in ids]}


def cuenta(idc=300):
    return [ins(T_CTA, IdCuentaBancaria=idc, IdBanco=19, IdTipoCuentaBancaria='CC', IdMoneda='CLP', NumeroCuentaBancaria='9876'),
            ins(T_TIPOCTA, IdTipoCuentaBancaria='CC', NombreTipoCuentaBancaria='Cuenta Corriente')]


def detalle(idrc=11, folio=10, idre=0, monto=100000, estado='REZ', idcli=701, idpersona=501):
    return [crd(idrc=idrc, folio=folio, idre=idre, idcli=idcli, idpersona=idpersona, monto=monto, estado=estado),
            rec(folio, estado, monto=monto, banco_=17, cuenta='111222'), rcli(idrc, folio, None, estado, apv=monto, idcliente=idcli)]


BASE = [empresa(), transiciones(rez=False, dis=False, dev=True), cuenta()]
OK = sc_traspaso_ok(2)
ERR_TES = {'P_OUT_MENSAJES': [msg(1, 'ERROR', 'SIN_SALDO', 'sin saldo')], 'P_OUT_INGRESOEGRESOGENERADO': [], 'P_OUT_RESULTADOPROCESO': []}
ING_OK = OK['CrearIngresoEgreso']['salida']
COMP_OK = OK['CrearComprobante']['salida']
ERR_COMP = {'P_OUT_MENSAJES': [msg(1, 'ERROR', 'COMPROBANTE_NO_CREADO', 'comprobante no creado')], 'P_OUT_RESULTADOPROCESO': []}

caso(240, 'DevolucionPagoRezagos', 'devuelve-un-rezago', 'one REZ client detail: traspaso DEV, own egreso movement + comprobante, the client detail goes to DEV (deposit and employer untouched), a log entry; empty output',
     L(F, 'RecaudacionEstado.IdEstado = "DEV"'), ent(), seed(BASE, detalle()), secuencias={}, colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(241, 'DevolucionPagoRezagos', 'dos-detalles-suman-el-monto-del-traspaso', 'two details (100000 + 50000): one traspaso for the sum, two egreso/forma de pago entries, the log lists the folios sorted ascending',
     L(F, 'sumaMontos = sumaMontos.add\\(fila.RecTotal\\)'), ent(ids=(12, 11)), seed(BASE, detalle(11, 10, monto=100000), detalle(12, 20, monto=50000)), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(242, 'DevolucionPagoRezagos', 'estado-con-tambien-se-devuelve', 'state CON is accepted by the query as well as REZ', L(F, 'SQL_SELECT_Recaudacion'),
     ent(), seed(BASE, detalle(estado='CON')), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(243, 'DevolucionPagoRezagos', 'estado-distribuido-no-se-selecciona-monto-cero', 'a detail already DIS is not selected: the sum is 0 and MONTO_TOTAL_RECAUDADO_IGUAL_CERO is answered',
     L(F, 'MONTO_TOTAL_RECAUDADO_IGUAL_CERO'), ent(), seed(BASE, detalle(estado='DIS')), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(244, 'DevolucionPagoRezagos', 'detalle-con-monto-cero', 'a REZ detail whose total is 0: same MONTO_TOTAL_RECAUDADO_IGUAL_CERO answer even though a row was found',
     L(F, 'sumaMontos.compareTo\\(BigDecimal.ZERO\\)'), ent(), seed(BASE, detalle(monto=0)), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(245, 'DevolucionPagoRezagos', 'sin-parametros-monto-cero', 'empty p_Parametros: the IN list is empty, nothing is found and the zero-amount message is answered',
     L(F, 'for\\(ParametrosRecaudacion fila : in.p_Parametros\\)'), ent(ids=()), seed(BASE, detalle()), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(246, 'DevolucionPagoRezagos', 'sin-origen-destino-dev-lanza-IndexOutOfBounds', 'no OrigenDestinoTransicion with Operacion DEV: _CtaBco.get(0) throws IndexOutOfBoundsException',
     L(F, 'IdCtaBco = _CtaBco.get\\(0\\)'), ent(), seed(empresa(), transiciones(rez=True, dis=False), cuenta(), detalle()), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(247, 'DevolucionPagoRezagos', 'sin-empresa-lanza-IndexOutOfBounds', 'the company is not in the PLA/CLI views: _DatosEmpresa.get(0) throws IndexOutOfBoundsException',
     L(F, '_DatosEmpresa.get\\(0\\).IdPortafolioDefecto'), ent(), seed(transiciones(rez=False, dis=False, dev=True), cuenta(), detalle()), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(248, 'DevolucionPagoRezagos', 'cuenta-bancaria-inexistente-lanza-IndexOutOfBounds', 'defect: p_DevolucionIdBanco is looked up as IdCuentaBancaria; a missing PER.CuentaBancaria row makes DatosBancarios.get(0) throw IndexOutOfBoundsException inside the loop',
     L(F, 'DatosBancarios.get\\(0\\).IdBanco;'), ent(banco=999), seed(BASE, detalle()), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(249, 'DevolucionPagoRezagos', 'error-de-tesoreria-en-el-traspaso-corta', 'the traspaso DEV gets a Tesoreria error: its ERROR messages are copied, the counter is the number of errors and nothing else runs (no own movement, state unchanged)',
     L(F, 'OutTrasCtaBca_Entrada.P_OUT_MENSAJES.parallelStream'), ent(), seed(BASE, detalle()), colaboradores=dict(OK, CrearIngresoEgreso={'salida': ERR_TES}), tablas=TABLAS, enmascarar=MASK)
caso(250, 'DevolucionPagoRezagos', 'error-al-crear-el-egreso-propio-corta', 'the traspaso is fine but the own CrearIngresoEgreso (second call) answers an error: messages copied and the state is not changed',
     L(F, 'OutTesoreria.P_OUT_MENSAJES.parallelStream'), ent(), seed(BASE, detalle()), colaboradores=dict(OK, CrearIngresoEgreso={'salidas': [ING_OK, ERR_TES]}), tablas=TABLAS, enmascarar=MASK)
caso(251, 'DevolucionPagoRezagos', 'error-al-crear-el-comprobante-propio-corta', 'the own CrearComprobante (second call) answers an error: messages copied and the state is not changed',
     L(F, 'OutComprobante.P_OUT_MENSAJES.parallelStream'), ent(), seed(BASE, detalle()), colaboradores=dict(OK, CrearComprobante={'salidas': [COMP_OK, ERR_COMP]}), tablas=TABLAS, enmascarar=MASK)
caso(252, 'DevolucionPagoRezagos', 'detalle-de-empleador-cambia-tambien-el-empleador', 'a detail that hangs from an employer detail (IdRecaudacionEmpleador 31): CambiarEstadoRecaudacion also moves the employer detail to DEV',
     L(F, 'RecaudacionEstado.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador'),
     ent(), seed(BASE, crd(idrc=11, folio=10, idre=31, monto=100000, estado='REZ', rut_emp='76111222-3'), rec(10, 'REZ', monto=100000, banco_=17, cuenta='111222'),
                 ins(T_REMP, IdRecaudacionEmpleador=31, IdRecaudacion=10, IdEmpresa=1, IdEstado='REZ', MontoDepositoAPV=100000, MontoDepositoConvenido=0),
                 rcli(11, 10, 31, 'REZ', apv=100000)), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(253, 'DevolucionPagoRezagos', 'sin-parametros-nulo-lanza-NullPointerException', 'p_Parametros missing: the for over a null list throws NullPointerException',
     L(F, 'for\\(ParametrosRecaudacion fila : in.p_Parametros\\)'), {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_DevolucionIdBanco': 300}, seed(BASE, detalle()), colaboradores=OK, tablas=TABLAS, enmascarar=MASK)
caso(254, 'DevolucionPagoRezagos', 'sin-parametro-de-fecha-lanza', 'PLATAFORMA/PLA.FechaSistema missing: the configuration read throws before any query',
     L(F, 'ConfiguracionSistema.getParametroConfiguracionString'), ent(), seed(BASE, detalle()), config={'PLATAFORMA/PLA.FechaSistema': None}, colaboradores=OK, tablas=TABLAS, enmascarar=MASK)

# ------------------------------------------------------------------------------------------------ DevolucionBF_EntidadRecaudadora
F = 'DevolucionBF_EntidadRecaudadora.java'
TBON = tabla(T_INVBON, '"IdBonificacion"', columnas=['IdBonificacion', 'IdCliente', 'AnoAhorro', 'IdEstado'])


def ent_bf(ids=(61,)):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 39, 'p_IdModulo': 'APV', 'p_Parametros': [{'IdBonificacion': i} for i in ids]}


def bonif(idb=61, idcli=701, ano=2025, empresa_=1, nombre='AFP UNO', idtras=1, monto=80000, fondo='A', persona=8000, tipo='APV', fecha='2025-03-01'):
    return [ins(T_INVBON, IdBonificacion=idb, IdCliente=idcli, AnoAhorro=ano, IdEmpresa=empresa_, IdEstado='PRO'),
            ins(T_TRASPASO, IdTraspaso=idtras, IdCliente=idcli, FechaIngreso=fecha, NombreEntidad=nombre, IdEmpresa=1),
            ins(T_DET, IdTraspaso=idtras, CodigoTipoAhorro=tipo, Fondo=fondo, MontoTraspaso=monto),
            ins(T_V_ENT, Nombre=nombre, IdPersona=persona)]


BASE_BF = [empresa(), transiciones(rez=False, dis=False, dbf=True)]

caso(260, 'DevolucionBF_EntidadRecaudadora', 'devuelve-una-bonificacion', 'one bonificacion with an APV/A traspaso to the entity: traspaso DBF, egreso EGRBON for the entity person, comprobante PAREGR; empty output (liquidation is commented out)',
     L(F, 'IngresoEgreso.IdConcepto = "EGRBON"'), ent_bf(), seed(BASE_BF, bonif()), colaboradores=OK, tablas=[TBON])
caso(261, 'DevolucionBF_EntidadRecaudadora', 'dos-bonificaciones-suman-y-numeran', 'two bonificaciones (80000 + 30000): one traspaso for the sum; the movement reference numbers are the running counter 1, 2',
     L(F, 'IngresoEgreso.NumeroReferencia = contador'), ent_bf((61, 62)), seed(BASE_BF, bonif(), bonif(62, 702, idtras=2, nombre='AFP DOS', monto=30000, persona=8001)), colaboradores=OK, tablas=[TBON])
caso(262, 'DevolucionBF_EntidadRecaudadora', 'sin-origen-destino-dbf-lanza-IndexOutOfBounds', 'no OrigenDestinoTransicion with Operacion DBF: _CtaBco.get(0) throws IndexOutOfBoundsException',
     L(F, 'IdCtaBco = _CtaBco.get\\(0\\)'), ent_bf(), seed(empresa(), transiciones(rez=False, dis=False, dev=True), bonif()), colaboradores=OK, tablas=[TBON])
caso(263, 'DevolucionBF_EntidadRecaudadora', 'sin-empresa-lanza-IndexOutOfBounds', 'the company is not in the views: _DatosEmpresa.get(0) throws IndexOutOfBoundsException',
     L(F, '_DatosEmpresa.get\\(0\\).IdPortafolioDefecto'), ent_bf(), seed(transiciones(rez=False, dis=False, dbf=True), bonif()), colaboradores=OK, tablas=[TBON])
caso(264, 'DevolucionBF_EntidadRecaudadora', 'sin-bonificaciones-igual-ejecuta-con-monto-cero', 'no bonificacion found: no zero-amount guard; the traspaso, the (empty) egreso call and the comprobante are still invoked',
     L(F, 'paramsTrasCtaBca_Entrada.p_Monto = sumaMontos'), ent_bf((99,)), seed(BASE_BF, bonif()), colaboradores=OK, tablas=[TBON])
caso(265, 'DevolucionBF_EntidadRecaudadora', 'errores-de-tesoreria-se-ignoran', 'defect: Tesoreria errors of the traspaso, the egreso and the comprobante are never checked: the call ends with an empty output as if everything went well',
     L(F, 'if \\(MensajeSalida.getNumeroErrores\\(OutComprobante.P_OUT_MENSAJES\\) > 0\\)'), ent_bf(),
     seed(BASE_BF, bonif()), colaboradores=dict(OK, CrearIngresoEgreso={'salida': ERR_TES}, CrearComprobante={'salida': ERR_COMP}), tablas=[TBON])
caso(266, 'DevolucionBF_EntidadRecaudadora', 'fondo-distinto-de-a-monto-nulo-lanza-NPE', 'the traspaso detail is not fund A: the LEFT JOIN gives a NULL sum and sumaMontos.add(null) throws NullPointerException',
     L(F, 'sumaMontos = sumaMontos.add\\(fila.Monto\\)'), ent_bf(), seed(BASE_BF, bonif(fondo='B')), colaboradores=OK, tablas=[TBON])
caso(267, 'DevolucionBF_EntidadRecaudadora', 'bonificacion-de-otra-empresa-no-se-selecciona', 'the bonificacion belongs to another company: not selected, zero amount path',
     L(F, 'ParamsSelectBonificacion.put\\("IdEmpresa"'), ent_bf(), seed(BASE_BF, bonif(empresa_=2)), colaboradores=OK, tablas=[TBON])
caso(268, 'DevolucionBF_EntidadRecaudadora', 'ano-del-traspaso-distinto-no-se-asocia', 'the traspaso entered in another year than AnoAhorro: the inner join drops the bonificacion',
     L(F, 'SQL_SELECT_Bonificacion'), ent_bf(), seed(BASE_BF, bonif(fecha='2024-03-01')), colaboradores=OK, tablas=[TBON])
caso(269, 'DevolucionBF_EntidadRecaudadora', 'excepcion-despues-del-commit-intermedio', 'CrearIngresoEgreso throws after the con.commit() that follows the traspaso: the call ends with the exception (the pipeline rolls back only what was not committed)',
     L(F, '^\\t\\t    con.commit\\(\\);'), ent_bf(), seed(BASE_BF, bonif()), colaboradores=dict(OK, CrearIngresoEgreso={'lanza': 'Tesoreria no disponible'}), tablas=[TBON])
