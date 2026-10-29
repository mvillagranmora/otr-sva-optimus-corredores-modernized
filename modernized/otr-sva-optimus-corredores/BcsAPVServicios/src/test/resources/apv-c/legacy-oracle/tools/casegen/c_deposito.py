"""Family C cases: CrearRecaudacionDeposito (APV-C-070 .. 089)."""
import copy

from fix import (T_REC, T_TRASPASO, T_DOC, banco, caja, cfg, cuenta_banco, empresa, msg, res_proceso, salida_ing_egreso, salida_liquidar)
from lib import L, caso, ins, seed, tabla

F = 'CrearRecaudacionDeposito.java'
SEQ = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacion'
TREC = tabla(T_REC, '"IdRecaudacion"')
ING = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'


def dep(**kw):
    d = {'IdInterno': 1, 'IdRecaudacion': 0, 'FolioCartola': 1, 'LineaCartola': 1, 'Sucursal': 'SUCUR', 'FechaDeposito': '2025-03-31', 'IdCuentaBanco': 122,
         'IdBanco': 12, 'CuentaCorriente': '13602227', 'MontoDeposito': 1900, 'Observacion': 'OBS', 'NumeroDocumento': 1, 'ConceptoRecaudacion': '',
         'IdTipoDeposito': 'TRA', 'IdCarga': 0, 'IdEstado': 'ING', 'CodigoArchivo': 'A1'}
    d.update(kw)
    return d


def ent(tipo='I', origen='CARGA', params=None, **kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Tipo': tipo, 'p_Origen': origen, 'p_Parametros': params if params is not None else [dep()]}
    e.update(kw)
    return e


BASE = [empresa(), caja(), banco()]
TESO_OK = {'CrearIngresoEgreso': {'salida': salida_ing_egreso(generados=[{'NumeroReferencia': 1, 'IdIngresoEgreso': 7001, 'IdTipoMovimiento': 'MOVING'}],
                                                              resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1)])}}

caso(70, 'CrearRecaudacionDeposito', 'ingreso-un-deposito-con-cuenta-informada', 'type I: one deposit is inserted, OK message, Tesoreria movement built (no settlement)',
     L(F, 'objIngresoEgreso.IdConcepto = "TRASPA"', 'ABNAPV'), ent(), seed(BASE), secuencias={SEQ: 963}, colaboradores=TESO_OK, tablas=[TREC])

caso(71, 'CrearRecaudacionDeposito', 'cuenta-no-informada-se-resuelve-por-numero', 'IdCuentaBanco 0: the corredora account is found by bank + number without leading zeros',
     L(F, 'lstNumeroCuentaCorredora.add'),
     ent(params=[dep(IdCuentaBanco=0, CuentaCorriente='0013602227')]), seed(BASE, cuenta_banco(122, 12, '13602227')), secuencias={SEQ: 970}, colaboradores=TESO_OK,
     tablas=[TREC])

caso(72, 'CrearRecaudacionDeposito', 'cuenta-no-informada-y-no-existe', 'IdCuentaBanco 0 and no such account: UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS, the deposit row was already inserted',
     L(F, 'UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS'),
     ent(params=[dep(IdCuentaBanco=0, CuentaCorriente='999')]), seed(BASE, cuenta_banco(122, 12, '13602227')), secuencias={SEQ: 980}, tablas=[TREC])

caso(73, 'CrearRecaudacionDeposito', 'modificacion-actualiza-con-coalesce', 'type M: the deposit is updated (COALESCE keeps the columns sent as null), OK message UI_MODIFICACION_DEPOSITO_OK, no Tesoreria',
     L(F, 'UI_MODIFICACION_DEPOSITO_OK'),
     ent(tipo='M', params=[dep(IdRecaudacion=500, MontoDeposito=2500, Observacion=None, Sucursal=None)]),
     seed(BASE, ins(T_REC, IdRecaudacion=500, IdEmpresa=1, CodigoArchivo='A1', FolioCartola=1, LineaCartola=1, Sucursal='VIEJA', FechaDeposito='2025-03-01',
                    CuentaCorriente='13602227', MontoDeposito=1900, ConceptoRecaudacion='', Origen='CARGA', Observacion='ANTIGUA', NumeroDocumento=1,
                    IdTipoDeposito='TRA', IdBanco=12, IdRegimenTributario=0, IdCarga=0, IdEstado='ING')),
     secuencias={SEQ: 990}, tablas=[TREC])

caso(74, 'CrearRecaudacionDeposito', 'modificacion-de-deposito-inexistente', 'type M of a deposit that does not exist: nothing updated, no message, no P_OUT_IDREC',
     L(F, 'if \\(infoRecaudacion != null\\)'),
     ent(tipo='M', params=[dep(IdRecaudacion=501)]), seed(BASE), secuencias={SEQ: 991}, tablas=[TREC])

caso(75, 'CrearRecaudacionDeposito', 'origen-traspa-usa-persona-de-la-entidad', 'origin TRASPA: concept ATRAPV and the title person comes from the recaudadora entity of the contract (NumeroDocumento)',
     L(F, 'objIngresoEgreso.IdConcepto = "TRASPA"'),
     ent(origen='TRASPA', params=[dep(NumeroDocumento=31)]),
     seed(BASE, ins(T_TRASPASO, IdTraspaso=1, IdContratoSAAPV=31, IdEmpresa=1, DocumentoEntidad='96000000-1'),
          ins(T_DOC, DocumentoIdentidad='96000000-1', IdDocumentoIdentidad=1, IdPersona=4400, Predeterminado='S')),
     secuencias={SEQ: 1000}, colaboradores=TESO_OK, tablas=[TREC])

caso(76, 'CrearRecaudacionDeposito', 'origen-bonif-concepto-ingbon', 'origin BONIF: concept INGBON and IdComprobante 0',
     L(F, 'p_Origen.equals\\("BONIF"\\)'),
     ent(origen='BONIF'), seed(BASE), secuencias={SEQ: 1010}, colaboradores=TESO_OK, tablas=[TREC])

caso(77, 'CrearRecaudacionDeposito', 'empresa-sin-configuracion', 'no corredora data (PLA.Empresa empty): NO_SE_PUEDE_OBTENER_CONNFIGURACION_EMPRESA and nothing written',
     L(F, 'NO_SE_PUEDE_OBTENER_CONNFIGURACION_EMPRESA'),
     ent(), seed(caja(), banco()), secuencias={SEQ: 1020}, tablas=[TREC])

caso(78, 'CrearRecaudacionDeposito', 'caja-pac-cerrada', 'the PAC cash box is closed (CER): CAJA_RECAUDACION_CERRADA_O_INEXISTENTE',
     L(F, 'CAJA_RECAUDACION_CERRADA_O_INEXISTENTE'),
     ent(), seed(empresa(), caja(estado='CER'), banco()), secuencias={SEQ: 1030}, tablas=[TREC])

caso(79, 'CrearRecaudacionDeposito', 'tesoreria-responde-error', 'Tesoreria answers an ERROR: its messages are added, the counter counts them and the call returns (deposit stays inserted)',
     L(F, 'getNumeroErrores\\(OutTesoreria.P_OUT_MENSAJES\\) > 0'),
     ent(), seed(BASE), secuencias={SEQ: 1040},
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(mensajes=[msg(1, 'ERROR', 'CAJA_SIN_SALDO', 'sin saldo')])}}, tablas=[TREC])

caso(80, 'CrearRecaudacionDeposito', 'liquidacion-automatica-liquida-comprobantes', 'REC.LiquidacionAutomatica S and a comprobante created: LiquidarComprobante is called and its messages added',
     L(F, 'valorParametroLiquidacion.equals\\("S"\\)'),
     ent(), seed(BASE), secuencias={SEQ: 1050}, config=cfg(**{'APV/REC.LiquidacionAutomatica': 'S'}),
     colaboradores=dict(TESO_OK, LiquidarComprobante={'salida': salida_liquidar(mensajes=[msg(8001, 'OK', 'LIQUIDADO', 'liquidado')])}), tablas=[TREC])

caso(81, 'CrearRecaudacionDeposito', 'liquidacion-con-error-cuenta-errores', 'settlement answers an ERROR: messages added, counter = errors, returns',
     L(F, 'getNumeroErrores\\(OutLiquidar.P_OUT_MENSAJES\\) > 0'),
     ent(), seed(BASE), secuencias={SEQ: 1060}, config=cfg(**{'APV/REC.LiquidacionAutomatica': 'S'}),
     colaboradores=dict(TESO_OK, LiquidarComprobante={'salida': salida_liquidar(mensajes=[msg(8001, 'ERROR', 'NO_LIQUIDA', 'no liquida')])}), tablas=[TREC])

caso(82, 'CrearRecaudacionDeposito', 'resultado-sin-comprobante-no-liquida', 'Tesoreria result without a CrearComprobante IdComprobante row: nothing to settle even with automatic settlement',
     L(F, 'outResultado.Campo.equals\\("IdComprobante"\\)'),
     ent(), seed(BASE), secuencias={SEQ: 1070}, config=cfg(**{'APV/REC.LiquidacionAutomatica': 'S'}),
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdIngresoEgreso', 7001, 'BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso', 'ING', 1)])}},
     tablas=[TREC])

caso(83, 'CrearRecaudacionDeposito', 'dos-depositos-referencias-correlativas', 'two deposits: ids from the sequence block, NumeroReferencia 1 and 2, one OK message each, P_OUT_IDREC is the last',
     L(F, 'numeroReferencia\\+\\+'),
     ent(params=[dep(IdInterno=1, FolioCartola=1), dep(IdInterno=2, FolioCartola=2, MontoDeposito=700)]), seed(BASE), secuencias={SEQ: 1100},
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1), res_proceso('IdComprobante', 8002, ING, 'ING', 2)])}},
     tablas=[TREC])

caso(84, 'CrearRecaudacionDeposito', 'lista-vacia-no-inserta', 'empty p_Parametros: no insert, no Tesoreria, empty answer (the settlement parameter is still read)',
     L(F, 'if \\(!lstRecaudacion.isEmpty\\(\\)\\)'),
     ent(params=[]), seed(BASE), secuencias={SEQ: 1110}, tablas=[TREC])

caso(85, 'CrearRecaudacionDeposito', 'lista-vacia-sin-parametro-liquidacion-lanza', 'empty list and the APV/REC.LiquidacionAutomatica parameter missing: Exception from ConfiguracionSistema',
     L(F, 'valorParametroLiquidacion = ConfiguracionSistema'),
     ent(params=[]), seed(BASE), secuencias={SEQ: 1120}, config={'APV/REC.LiquidacionAutomatica': None}, tablas=[TREC])

caso(86, 'CrearRecaudacionDeposito', 'tipo-nulo-lanza-NullPointerException', 'p_Tipo missing: NullPointerException at the first p_Tipo.compareTo',
     L(F, 'if \\(!\\(in.p_Tipo.compareTo'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Origen': 'CARGA', 'p_Parametros': [dep()]}, seed(BASE), secuencias={SEQ: 1130}, tablas=[TREC])

caso(87, 'CrearRecaudacionDeposito', 'cuenta-corriente-nula-lanza-NullPointerException', 'type I, IdCuentaBanco 0 and CuentaCorriente missing: NullPointerException (trim of null)',
     L(F, 'fila.CuentaCorriente.trim\\(\\).replaceFirst'),
     ent(params=[dep(IdCuentaBanco=0, CuentaCorriente=None)]), seed(BASE), secuencias={SEQ: 1140}, tablas=[TREC])

caso(88, 'CrearRecaudacionDeposito', 'fecha-invalida-lanza-SQLException', 'FechaDeposito that is not a date: the insert fails with SQLException and everything is rolled back',
     L(F, 'sql.batch\\(con, in.p_Tipo.compareTo'),
     ent(params=[dep(FechaDeposito='no-es-fecha')]), seed(BASE), secuencias={SEQ: 1150}, tablas=[TREC])

caso(89, 'CrearRecaudacionDeposito', 'sin-persona-del-banco-deja-titular-nulo', 'deposit bank without a BAN.Banco row: IdPersonaTitularPago is not set',
     L(F, 'PersonaBanco.get\\(i\\).IdBanco.compareTo'),
     ent(params=[dep(IdBanco=99)]), seed(empresa(), caja(), banco(12, 3012)), secuencias={SEQ: 1160}, colaboradores=TESO_OK, tablas=[TREC])
