"""Family C cases: CargaMasivaPAC (APV-C-160 .. 179)."""
from fix import (T_CARGA, T_RCLI, T_REC, T_V_CLIENTE, T_V_GESTION, banco, caja, cuenta_banco, empresa, ins, msg, res_proceso, salida_ing_egreso)
from lib import L, caso, seed, tabla

F = 'CargaMasivaPAC.java'
T_SUC = 'BSCL.Tesoreria.BD::TES.SucursalesBancarias'
T_V_BAN = 'BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco'
T_V_CTAS = 'BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias'
T_PPC = 'BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor'
T_V_PCB = 'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias'
SEQM = 'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva'
SEQD = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacion'
SEQC = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente'
SEQF = 'BSCL.FondoMutuo.BD.Secuencias::FolioContrato'
SEQS = {SEQM: 100, SEQD: 960, SEQC: 520, SEQF: 5000}
NOMBRE = 'ARCHIVO_CARGO_10_4_2018_133504.txt'
ING = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'
TESO = {'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1)])}}

TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'CodigoArchivo', 'FolioCartola', 'Sucursal', 'CuentaCorriente', 'MontoDeposito', 'Origen', 'IdBanco', 'IdCarga', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdCliente', 'TipoAfiliado', 'IdRegimenTriburario', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'Origen', 'IdEstado'])
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"')


def total(codigo='016', monto='600000'):
    return {'CodigoBanco': codigo, 'CodigoEmpresa': '006', 'CodigoConvenio': '023', 'TipoRegistro': 'T', 'MontoTotalCargo': monto, 'NumeroRegistros': '000001'}


def det(servicio='000022222233400160    ', monto=600000):
    return {'CodigoBanco': '016', 'CodigoEmpresa': '006', 'CodigoConvenio': '023', 'TipoRegistro': 'D', 'IdServicioEmpresa': servicio, 'InformacionAdicional': '          ',
            'MontoCargo': monto, 'FechaFacturacion': '20180508', 'FechaVencimiento': '20180508'}


def ent(totales=None, dets=None, nombre=NOMBRE, archivos=None):
    arch = archivos if archivos is not None else [{'IdEmpresa': 1, 'IdCargaMasiva': 0, 'NombreCargaMasiva': nombre, 'CantidadRegistros': 1, 'IdUsuario': 17, 'IdTipoCarga': 1,
                                                  'Descripcion': 'Carga PAC', 'FechaCarga': '2018-05-20T04:00:00.000Z', 'IdEstado': 'VIG'}]
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 17, 'p_IdModulo': 'APV', 'p_NombreCarga': nombre, 'p_Origen': 'PAC', 'p_IdEmpleado': 7312, 'p_Archivo': arch,
            'p_ParametrosArchivo': [det()] if dets is None else dets, 'p_ParametrosTotalArchivo': [total()] if totales is None else totales}


SUC = ins(T_SUC, IdSucursal=7, NombreSucursal='BANCA EN LINEA', IdBanco=16, IdEstado='ACT')
# the corredora account for the bank code 016: BAN.ConsultarBanco x ConsultarCuentasBancarias x PreferenciaPagoCorredor (APVPAC / MOVING)
CTA_CORREDORA = [ins(T_V_BAN, IdBanco=16, CodigoBanco='016', NombreBanco='BANCO PAC'), ins(T_V_CTAS, IdBanco=16, CodigoBanco='016', NumeroCuentaBancaria='7001', IdCuentaBanco=122, IdMoneda='CLP', NombreBanco='BANCO PAC'),
                 ins(T_PPC, IdConcepto='APVPAC', IdTipoMovimiento='MOVING', IdCuentaBanco=122)]
# the client of the PAC account 00002222223340 at bank 016
CLIENTE_PAC = [ins(T_V_PCB, IdBanco=16, IdPersona=501, NumeroCuentaBancaria='00002222223340'), ins(T_V_GESTION, IdPersona=501, IdEstado='VIG', RutCliente='11111111-1', IdContratoSAAPV=31),
               ins(T_V_CLIENTE, IdCliente=701, IdPersona=501, DocumentoIdentidad='11111111-1')]
BASE = [empresa(), caja(), banco(16, 3016), cuenta_banco(122, 16, '7001'), SUC, CTA_CORREDORA]

caso(160, 'CargaMasivaPAC', 'nombre-con-pocas-partes', 'file name with 5 or fewer "_" parts: "Formato Nombre de Archivo Incorrecto"; the sequence block IS requested before the check', L(F, 'if\\(CodigoArchivoExtencion.length > 5\\)'),
     ent(nombre='ARCHIVO_CARGO_10_4.txt'), seed(BASE), secuencias=SEQS, tablas=[TREC, TCLI, TCARGA])
caso(161, 'CargaMasivaPAC', 'archivo-ya-cargado', 'a deposit already exists with the file code: "El archivo ya fue cargado previamente."', L(F, 'El archivo ya fue cargado previamente'),
     ent(), seed(BASE, ins(T_REC, IdRecaudacion=5, IdEmpresa=1, CodigoArchivo='133504', FolioCartola=0, LineaCartola=0, MontoDeposito=1, IdEstado='ING')), secuencias=SEQS, tablas=[TREC, TCLI, TCARGA])
caso(162, 'CargaMasivaPAC', 'sin-cuenta-de-banco', 'no corredora account for the bank code: CUENTA_BANCO_ERROR (id = message counter 1)', L(F, 'CUENTA_BANCO_ERROR'),
     ent(totales=[total('999')]), seed(BASE), secuencias=SEQS, tablas=[TREC, TCLI, TCARGA])
caso(163, 'CargaMasivaPAC', 'sin-sucursal-banca-en-linea', 'TES.SucursalesBancarias without IdSucursal 7: SUCURSAL_ERROR', L(F, 'SUCURSAL_ERROR'),
     ent(), seed(empresa(), caja(), banco(16, 3016), cuenta_banco(122, 16, '7001'), CTA_CORREDORA), secuencias=SEQS, tablas=[TREC, TCLI, TCARGA])
caso(164, 'CargaMasivaPAC', 'error-del-deposito-copia-mensajes-y-termina', 'the deposit creation reports an error (no PAC cash box): its messages are copied with id = message counter and the call returns', L(F, 'DepOut.P_OUT_ERRORESCONTADOR == 0'),
     ent(), seed(empresa(), banco(16, 3016), cuenta_banco(122, 16, '7001'), SUC, CTA_CORREDORA), secuencias=SEQS, tablas=[TREC, TCLI, TCARGA])
caso(165, 'CargaMasivaPAC', 'excepcion-del-deposito-se-informa-y-termina', 'an exception in the deposit creation (settlement parameter missing): rollback, an ERROR message with the exception text, returns', L(F, 'msg.descripcion = er.getMessage\\(\\);'),
     ent(), seed(BASE), secuencias=SEQS, config={'APV/REC.LiquidacionAutomatica': None}, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(166, 'CargaMasivaPAC', 'cliente-inexistente-y-deposito-sin-clientes', 'no client for the PAC account: ERROR message, the CargaMasiva row and the totals message are written, then ConciliarDeposito reports a deposit without clients (no distribution)',
     L(F, 'No existe un Cliente con Codigo Banco'), ent(), seed(BASE), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(167, 'CargaMasivaPAC', 'cliente-existe-pero-montos-no-cuadran', 'client found: CREACION_CLIENTE_OK; the client amount (500000) differs from the deposit total (600000): the conciliation reports UI_NO_CUADRA_DEPOSITO_CON_CLIENTE and nothing is distributed',
     L(F, 'CREACION_CLIENTE_OK'), ent(dets=[det(monto=500000)]), seed(BASE, CLIENTE_PAC), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(168, 'CargaMasivaPAC', 'cliente-cuadra-concilia-y-distribuye-sin-registros', 'client amount = deposit total: CONCILIADO_OK, then ProcesarDistribucion (option PAC) runs for real and finds nothing to distribute (empty distribution view)',
     L(F, 'fila.codigo =="CONCILIADO_OK"'), ent(), seed(BASE, CLIENTE_PAC), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(169, 'CargaMasivaPAC', 'monto-total-no-numerico-lanza-NumberFormatException', 'MontoTotalCargo that is not an integer: NumberFormatException', L(F, 'dep.MontoDeposito = BigDecimal.valueOf'),
     ent(totales=[total(monto='600000.5')]), seed(BASE), secuencias=SEQS, tablas=[TREC, TCLI, TCARGA])
caso(170, 'CargaMasivaPAC', 'identificador-corto-lanza-StringIndexOutOfBounds', 'IdServicioEmpresa shorter than 17 characters: StringIndexOutOfBoundsException', L(F, 'fila.IdServicioEmpresa.substring\\(0, 14\\)'),
     ent(dets=[det(servicio='0000222222')]), seed(BASE), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(171, 'CargaMasivaPAC', 'sin-archivo-lanza-IndexOutOfBounds-al-armar-el-deposito', 'empty p_Archivo: the sequence block is empty and dep.IdCarga = idsCargaArchivo.get(0) throws IndexOutOfBoundsException before anything is written',
     L(F, 'dep.IdCarga = idsCargaArchivo.get\(0\)'), ent(archivos=[]), seed(BASE, CLIENTE_PAC), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(172, 'CargaMasivaPAC', 'sin-totales-no-crea-deposito', 'no TotalPAC entries: no deposit; the client rows still try to attach to IdRecaudacion 0 and the CargaMasiva row is written', L(F, 'for \\(TotalPAC fila : in.p_ParametrosTotalArchivo\\)'),
     ent(totales=[]), seed(BASE, CLIENTE_PAC), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
caso(173, 'CargaMasivaPAC', 'sin-totales-ni-archivo-lanza-IndexOutOfBounds-al-final', 'no totals and no file: the client rows are created (and committed inside), then in.p_Archivo.get(0) throws IndexOutOfBoundsException; the rows stay because con.commit() ran inside the loop (partial commit)',
     L(F, 'in.p_Archivo.get\(0\).IdCargaMasiva = idsCargaArchivo.get\(0\)'), ent(totales=[], archivos=[]), seed(BASE, CLIENTE_PAC), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCLI, TCARGA])
