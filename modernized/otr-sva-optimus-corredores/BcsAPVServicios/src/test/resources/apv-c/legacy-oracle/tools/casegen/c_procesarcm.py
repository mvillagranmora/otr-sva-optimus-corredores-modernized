"""Family C cases: ProcesarCargaMasivaRecaudacion (APV-C-050 .. 069)."""
from fix import (cuenta_banco, T_CARGA, T_CARGADET, T_RCLI, T_REC, T_REMP, T_V_CLIENTE, T_V_CTAS, T_V_GESTION, T_V_PJ, T_V_REC, banco, caja, empresa, msg, res_proceso,
                 salida_ing_egreso)
from lib import L, caso, ins, seed, tabla

F = 'ProcesarCargaMasivaRecaudacion.java'
ING = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'
SEQD = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacion'
SEQE = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador'
SEQC = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente'
SEQS = {SEQD: 900, SEQE: 600, SEQC: 500}
TESO = {'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1)])}}

TDET = tabla(T_CARGADET, '"FolioCartola", "FolioLineaCartola", "RUTCliente"', columnas=['IdCargaMasiva', 'FolioCartola', 'FolioLineaCartola', 'RUTEmpleador', 'RUTCliente', 'MensajeValidacion', 'EstadoRendicion', 'EstadoProceso'])
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"', columnas=['IdCargaMasiva', 'IdEstado'])
TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'IdEmpresa', 'FolioCartola', 'LineaCartola', 'CuentaCorriente', 'MontoDeposito', 'IdTipoDeposito', 'IdBanco', 'Origen', 'IdEstado'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'IdPersona', 'PeriodoPago', 'TipoAfiliado', 'TipoPago', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'Origen', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdCliente', 'TipoAfiliado', 'IdRegimenTriburario',
                                                         'MontoDepositoConvenido', 'MontoDepositoAPV', 'Origen', 'IdEstado'])


def det(folio, linea, carga=77, cli='9499210-9', emp='76129263-3', monto=1500, afiliado='DEP', regimen='APV-B', tipo_pago='NORMAL', periodo='2021-07', estado='', banco_='CHILE',
        cuenta='111111', msg_val=''):
    return ins(T_CARGADET, IdCargaMasiva=carga, Fecha='2021-12-07', TipoDeposito='TRA', FolioCartola=folio, FolioLineaCartola=linea, Banco=banco_, Cuenta=cuenta, Monto=monto,
               PeriodoPago=periodo, TipoPago=tipo_pago, RUTEmpleador=emp, RUTCliente=cli, TipoDeAfiliado=afiliado, Regimen=regimen, MensajeValidacion=msg_val, EstadoRendicion='',
               EstadoProceso=estado)


def pj(doc='76129263-3', idpersona=4400, estado='ACT'):
    return ins(T_V_PJ, IdPersona=idpersona, PersonaBusqueda=doc + ' RAZON SOCIAL', DocumentoIdentidad=doc, IdEstado=estado)


def vc(doc, idcliente):
    return ins(T_V_CLIENTE, IdCliente=idcliente, DocumentoIdentidad=doc)


def cta(nombre='CHILE', numero='111111', idbanco=12):
    return ins(T_V_CTAS, IdBanco=idbanco, NombreBanco=nombre, NumeroCuentaBancaria=numero, IdCuentaBanco=122)


def vrec(idrec, folio, linea, monto=62500):
    return ins(T_V_REC, IdRecaudacion=idrec, IdEmpresa=1, FolioCartola=folio, LineaCartola=linea, MontoDeposito=monto, IdEstado='ING')


def contrato(rut, regimen='APV-B', idcontrato=31, folio='F31'):
    return ins(T_V_GESTION, RutCliente=rut, RegimenTributario=regimen, IdContratoSAAPV=idcontrato, FolioContratoSAAPV=folio, IdEstado='VIG')


def ent(accion, carga=77):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 70830, 'p_IdModulo': 'APV', 'p_accion': accion, 'p_IdCargaMasiva': carga}


CARGA = ins(T_CARGA, IdEmpresa=1, IdCargaMasiva=77, NombreCargaArchivo='Carga_4.txt', CantidadRegistros=3, IdUsuario=21, IdTipoCarga=2, Descripcion='Carga', FechaCarga='2021-12-07', IdEstado='VIG')
TRES = ['IdCargaMasiva', 'FolioCartola', 'FolioLineaCartola', 'RUTEmpleador', 'RUTCliente', 'MensajeValidacion', 'EstadoRendicion', 'EstadoProceso']

# ------------------------------------------------------------------------------------------------ PRE (validation of the loaded file)
caso(50, 'ProcesarCargaMasivaRecaudacion', 'pre-todo-valido-queda-crt', 'PRE with valid client and employer: every detail becomes OK/CRT and the single message is PROCESA_CARGA_RECAUDACION_OK',
     L(F, 'PROCESA_CARGA_RECAUDACION_OK'), ent('PRE'),
     seed(CARGA, det(1011, 115, cli='9499210-9'), det(1011, 116, cli='12969673-7'), pj(), vc('9499210-9', 7001), vc('12969673-7', 7002), contrato('9499210-9')), tablas=[TDET])
caso(51, 'ProcesarCargaMasivaRecaudacion', 'pre-cliente-inexistente', 'PRE with an unknown client: PROCESA_CARGA_RECAUDACION_CLIENTE_NO_VALIDO, EstadoProceso ADV, rendicion ADV, validation text appended',
     L(F, 'PROCESA_CARGA_RECAUDACION_CLIENTE_NO_VALIDO'), ent('PRE'), seed(CARGA, det(1011, 115, cli='1-9'), pj()), tablas=[TDET])
caso(52, 'ProcesarCargaMasivaRecaudacion', 'pre-empleador-inexistente', 'PRE with an unknown employer (type DEP): EMPLEADOR_NO_VALIDO, state NOK', L(F, 'PROCESA_CARGA_RECAUDACION_EMPLEADOR_NO_VALIDO'),
     ent('PRE'), seed(CARGA, det(1011, 115, emp='99999999-9'), vc('9499210-9', 7001)), tablas=[TDET])
caso(53, 'ProcesarCargaMasivaRecaudacion', 'pre-cliente-y-empleador-invalidos', 'both invalid: two messages, the employer check runs last so EstadoProceso ends NOK, validation text has both phrases',
     L(F, 'detalle.EstadoProceso="NOK"'), ent('PRE'), seed(CARGA, det(1011, 115, cli='1-9', emp='99999999-9')), tablas=[TDET])
caso(54, 'ProcesarCargaMasivaRecaudacion', 'pre-tipo-pago-invalido-deja-estado-vacio', 'invalid TipoPago with valid client and employer: message TIPO_PAGO_NO_VALIDO, EstadoRendicion and EstadoProceso end as the old (empty) EstadoProceso',
     L(F, 'PROCESA_CARGA_RECAUDACION_TIPO_PAGO_NO_VALIDO'), ent('PRE'), seed(CARGA, det(1011, 115, tipo_pago='ESPECIAL'), pj(), vc('9499210-9', 7001)), tablas=[TDET])
caso(55, 'ProcesarCargaMasivaRecaudacion', 'pre-individual-empleador-igual-al-cliente', 'type IND: valid when the employer RUT equals the client RUT (no employer lookup needed)', L(F, 'if\\("IND".equals\\(tipoAfiliado\\)\\)'),
     ent('PRE'), seed(CARGA, det(1011, 115, cli='9499210-9', emp='9499210-9', afiliado='IND'), vc('9499210-9', 7001)), tablas=[TDET])
caso(56, 'ProcesarCargaMasivaRecaudacion', 'pre-individual-empleador-distinto-invalido', 'type IND with a different employer RUT: EMPLEADOR_NO_VALIDO even if the employer exists', L(F, 'empleadorArchivo.equals\\(clienteArchivo\\)'),
     ent('PRE'), seed(CARGA, det(1011, 115, cli='9499210-9', emp='76129263-3', afiliado='IND'), vc('9499210-9', 7001), pj()), tablas=[TDET])
caso(57, 'ProcesarCargaMasivaRecaudacion', 'pre-tipo-pago-sin-distinguir-mayusculas', 'TipoPago ATRASADO / normal are accepted ignoring case', L(F, 'equalsIgnoreCase\\(detalle.TipoPago\\)'),
     ent('PRE'), seed(CARGA, det(1, 1, tipo_pago='atrasado'), det(1, 2, tipo_pago='normal', cli='12969673-7'), pj(), vc('9499210-9', 7001), vc('12969673-7', 7002)), tablas=[TDET])
caso(58, 'ProcesarCargaMasivaRecaudacion', 'pre-carga-sin-detalle', 'PRE for a load without detail rows: no messages from the rules, so the OK message; the empty update batch runs', L(F, 'listaDetalleRecaudacion = obtenerDetalleRecaudacion'),
     ent('PRE', carga=999), seed(CARGA), tablas=[TDET])
caso(59, 'ProcesarCargaMasivaRecaudacion', 'pre-cliente-duplicado-en-vista-lanza', 'the client view returns the same DocumentoIdentidad twice: Collectors.toMap throws IllegalStateException', L(F, 'Collectors.toMap\\(cliente -> cliente.DocumentoIdentidad'),
     ent('PRE'), seed(CARGA, det(1011, 115), pj(), vc('9499210-9', 7001), vc('9499210-9', 7009)), tablas=[TDET])

# ------------------------------------------------------------------------------------------------ PRO (creates deposit, employer and client details)
BASE_PRO = [empresa(), caja(), banco(12, 3012), cta(), cuenta_banco(122, 12, '111111')]
TRES3 = [det(1011, 115, cli='9499210-9', monto=1500, estado='CRT'), det(1011, 115, cli='12969673-7', monto=52500, estado='CRT'), det(1011, 115, cli='20231097-4', monto=8500, estado='CRT')]
CLIS3 = [vc('9499210-9', 7001), vc('12969673-7', 7002), vc('20231097-4', 7003)]

caso(60, 'ProcesarCargaMasivaRecaudacion', 'pro-un-deposito-tres-clientes', 'PRO: three CRT rows of one Folio/Linea become ONE deposit (amounts summed), one employer detail (summed) and three client details; the load goes to PRO',
     L(F, 'this.actualizaCargaMasiva\\(conn, in.p_IdCargaMasiva\\)'), ent('PRO'),
     seed(BASE_PRO, CARGA, TRES3, pj(), CLIS3, vrec(900, 1011, 115)), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TEMP, TCLI, TCARGA, TDET])
caso(61, 'ProcesarCargaMasivaRecaudacion', 'pro-fila-no-crt-con-otra-llave-lanza-NullPointerException', 'defect: the employer step walks ALL rows (also the non-CRT ones): a DEP row with another Folio/Linea has no deposit in the map: NullPointerException (rolled back)',
     L(F, 'empleador.IdRecaudacion           =mapRecaudacion.get\\(key\\).IdRecaudacion'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1011, 115, estado='CRT'), det(2000, 1, cli='12969673-7', estado='ADV'), pj(), vc('9499210-9', 7001), vc('12969673-7', 7002), vrec(900, 1011, 115)),
     secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(62, 'ProcesarCargaMasivaRecaudacion', 'pro-error-al-crear-deposito-detiene', 'the deposit creation reports an error (no PAC cash box): its message is returned and the counter is set, no employer/client created, the load stays VIG',
     L(F, 'if\\(salida.P_OUT_ERRORESCONTADOR.compareTo\\(0L\\)==0 && mapEmpleadores.size\\(\\)>0\\)'), ent('PRO'),
     seed(empresa(), banco(12, 3012), cta(), CARGA, det(1011, 115, estado='CRT'), pj(), vc('9499210-9', 7001)), secuencias=SEQS, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(63, 'ProcesarCargaMasivaRecaudacion', 'pro-individuales-sin-empleador', 'PRO of IND rows whose employer RUT is not a legal person: no employer detail is created, the client detail has no employer id',
     L(F, 'mapEmpleadores.size\\(\\)>0'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1011, 115, cli='9499210-9', emp='9499210-9', afiliado='IND', estado='CRT'), vc('9499210-9', 7001), vrec(900, 1011, 115)),
     secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(64, 'ProcesarCargaMasivaRecaudacion', 'pro-dos-depositos-orden-hashmap', 'two different Folio/Linea: the deposits are created in HashMap order of the key text ("1011115", "118119"): ids follow that order (ORDER-SENSITIVE for a modern implementation)',
     L(F, 'for\\(Map.Entry<String, DetalleRecaudacion> obj : mapIngreso.entrySet\\(\\)\\)'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1011, 115, cli='9499210-9', estado='CRT'), det(118, 119, cli='12969673-7', estado='CRT', monto=21500), pj(), vc('9499210-9', 7001), vc('12969673-7', 7002),
          vrec(900, 1011, 115, 1500), vrec(901, 118, 119, 21500)),
     secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(65, 'ProcesarCargaMasivaRecaudacion', 'pro-llaves-que-colisionan-se-suman', 'defect: the key is FolioCartola+FolioLineaCartola as text, so (1,23) and (12,3) both give "123": two deposits become one with the summed amount',
     L(F, 'String key = detalle.FolioCartola.toString\\(\\)\\+detalle.FolioLineaCartola.toString\\(\\)'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1, 23, cli='9499210-9', estado='CRT', monto=1000), det(12, 3, cli='12969673-7', estado='CRT', monto=2000), pj(), vc('9499210-9', 7001), vc('12969673-7', 7002),
          vrec(900, 1, 23, 3000)),
     secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(66, 'ProcesarCargaMasivaRecaudacion', 'pro-banco-no-encontrado-usa-cero', 'bank/account not found in the view: IdBanco 0 in the deposit', L(F, 'banco !=null\\? banco.IdBanco:0L'), ent('PRO'),
     seed(empresa(), caja(), banco(12, 3012), cuenta_banco(122, 0, '111111'), CARGA, det(1011, 115, estado='CRT'), pj(), vc('9499210-9', 7001), vrec(900, 1011, 115, 1500)), secuencias=SEQS, colaboradores=TESO,
     tablas=[TREC, TEMP, TCLI, TCARGA])
caso(67, 'ProcesarCargaMasivaRecaudacion', 'pro-empleador-fuera-de-la-vista-lanza-NullPointerException', 'DEP row whose employer exists for another row but not for this one: mapEmpleadores.get(..).IdPersona NullPointerException',
     L(F, 'empleador.IdPersona               =mapEmpleadores.get'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1011, 115, estado='CRT'), det(1011, 115, cli='12969673-7', emp='88888888-8', estado='CRT'), pj(), vc('9499210-9', 7001), vc('12969673-7', 7002), vrec(900, 1011, 115)),
     secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(68, 'ProcesarCargaMasivaRecaudacion', 'pro-cliente-fuera-de-la-vista-lanza-NullPointerException', 'a client RUT missing from the client view: NullPointerException when building the client detail (deposit and employer rolled back)',
     L(F, 'cliente.IdCliente                  =mapCliente.get'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1011, 115, cli='55555555-5', estado='CRT'), pj(), vc('9499210-9', 7001), vrec(900, 1011, 115)), secuencias=SEQS, colaboradores=TESO,
     tablas=[TREC, TEMP, TCLI, TCARGA])
caso(69, 'ProcesarCargaMasivaRecaudacion', 'pro-tesoreria-con-error-cuenta-errores', 'Tesoreria answers an error while the deposit is created: the error message is returned and the counter set; nothing else is created',
     L(F, 'out.P_OUT_ERRORESCONTADOR=salidaDepositos.P_OUT_ERRORESCONTADOR'), ent('PRO'),
     seed(BASE_PRO, CARGA, det(1011, 115, estado='CRT'), pj(), vc('9499210-9', 7001), vrec(900, 1011, 115)), secuencias=SEQS,
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(mensajes=[msg(1, 'ERROR', 'CAJA_SIN_SALDO', 'sin saldo')])}}, tablas=[TREC, TEMP, TCLI, TCARGA])
