"""Family C cases: CargaMasivaBanco (APV-C-180 .. 199)."""
from fix import (T_CARGA, T_REC, T_V_PJ, banco, caja, cuenta_banco, empresa, ins, msg, rec, res_proceso, salida_ing_egreso)
from lib import L, caso, seed, tabla

F = 'CargaMasivaBanco.java'
T_SUC = 'BSCL.Tesoreria.BD::TES.SucursalesBancarias'
T_V_BP = 'BSCL.APV.BD.Vistas::REC.ConsultarBancoPrevired'
SEQM = 'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva'
SEQD = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacion'
SEQS = {SEQM: 90, SEQD: 950}
NOMBRE = 'Banco-AC07BAA_20191212_1648Z.TXT'
ING = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'
TESO = {'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1)])}}

TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'CodigoArchivo', 'FolioCartola', 'LineaCartola', 'Sucursal', 'CuentaCorriente', 'MontoDeposito', 'Origen', 'IdBanco', 'IdCarga', 'IdEstado'])
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"')


def fila(folio=2069201912003132, rut='0763090973', banco_='Banco Security  ', monto='000000841456', cuenta='000004360602                  ', valida='OK', abono='20191212'):
    return {'TipoFila': 2, 'Folio': folio, 'FechaOperacion': '20191010', 'HoraOperacion': '123036', 'IdBanco': banco_, 'TipoInstitucion': 'IAAPV', 'Institucion': 'Corredores de Bolsa SURA S.A.',
            'FechaAbono': abono, 'HoraAbono': '090000', 'PeriodoPago': '201912', 'RutPagador': rut, 'CuentaCorrientePagador': '165369501', 'NumeroAfiliado': '00001', 'NumeroContrato': '12',
            'MontoAbono': monto, 'CuentaCorrienteInstitucion': cuenta, 'MENSAJEVALIDACION': valida}


def ent(filas, nombre=NOMBRE, archivos=None):
    arch = archivos if archivos is not None else [{'IdEmpresa': 1, 'IdCargaMasiva': 0, 'NombreCargaMasiva': nombre, 'CantidadRegistros': len(filas), 'IdUsuario': 15, 'IdTipoCarga': 3,
                                                  'Descripcion': 'Carga Banco', 'FechaCarga': '2019-12-16T03:00:00.000Z', 'IdEstado': 'VIG'}]
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 15, 'p_IdModulo': 'APV', 'p_NombreCarga': nombre, 'p_Origen': 'BANCO', 'p_Archivo': arch, 'p_Parametros': filas}


def pj(doc='76309097-3', idpersona=4400):
    return ins(T_V_PJ, IdPersona=idpersona, PersonaBusqueda=doc + ' EMPRESA', DocumentoIdentidad=doc, IdEstado='ACT')


SUC = ins(T_SUC, IdSucursal=7, NombreSucursal='BANCA EN LINEA', IdBanco=12, IdEstado='ACT')
BP = ins(T_V_BP, IdBanco=12, CodigoBancoPrevired='Banco Security')
BASE = [empresa(), caja(), banco(12, 3012), cuenta_banco(122, 12, '4360602'), SUC, BP, pj()]

caso(180, 'CargaMasivaBanco', 'carga-una-fila-crea-deposito-y-registra-carga', 'one valid row: the deposit is created (Sucursal 7, account without leading zeros, amount parsed) and the CargaMasiva row is written',
     L(F, 'sql.batch\\(con, SQL_INSERT_CARGA_ARCHIVO, in.p_Archivo'), ent([fila()]), seed(BASE), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCARGA])
caso(181, 'CargaMasivaBanco', 'nombre-invalido', 'file name that does not match the pattern: FORMATO_NOMBRE_INVALIDO (the counter is NOT incremented) and nothing else runs', L(F, 'FORMATO_NOMBRE_INVALIDO'),
     ent([fila()], nombre='Banco_AC07BAA.TXT'), seed(BASE), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(182, 'CargaMasivaBanco', 'archivo-ya-cargado', 'a deposit already linked to a CargaMasiva of the same file name: "El archivo X ya fue cargado previamente." (counter 0)', L(F, 'ya fue cargado previamente'),
     ent([fila()]), seed(BASE, ins(T_CARGA, IdEmpresa=1, IdCargaMasiva=70, NombreCargaArchivo=NOMBRE, CantidadRegistros=1, IdUsuario=1, IdTipoCarga=3, Descripcion='Carga Banco', FechaCarga='2019-12-01', IdEstado='VIG'),
                        rec(5, 'ING', carga=70)), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(183, 'CargaMasivaBanco', 'sin-sucursal-banca-en-linea', 'TES.SucursalesBancarias without IdSucursal 7: SUCURSAL_ERROR', L(F, 'SUCURSAL_ERROR'),
     ent([fila()]), seed(empresa(), caja(), banco(12, 3012), cuenta_banco(122, 12, '4360602'), BP, pj()), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(184, 'CargaMasivaBanco', 'ningun-empleador-existe', 'none of the employers exists: error "Los Empleadores del archivo no existen en el sistema." (id 0) and the counter is 1', L(F, 'Los Empleadores del archivo no existen'),
     ent([fila()]), seed(empresa(), caja(), banco(12, 3012), cuenta_banco(122, 12, '4360602'), SUC, BP), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(185, 'CargaMasivaBanco', 'falta-un-empleador', 'two employers, one missing: ERROR "El Empleador RUT : X no existe en el sistema." (id 1) and the counter is 1', L(F, 'no existe en el sistema'),
     ent([fila(), fila(folio=2, rut='0088506100')]), seed(BASE), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(186, 'CargaMasivaBanco', 'mismo-empleador-dos-filas-sigue', 'two rows of the same employer: the size differs from the distinct employers but none is missing, so it continues and creates two deposits', L(F, 'empleadoresExistentes.size\\(\\) != rutsEmpleadores.size\\(\\)'),
     ent([fila(), fila(folio=2069201912003133)]), seed(BASE), secuencias=SEQS,
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1), res_proceso('IdComprobante', 8002, ING, 'ING', 2)])}},
     tablas=[TREC, TCARGA])
caso(187, 'CargaMasivaBanco', 'codigo-de-banco-no-registrado', 'a bank code (trimmed) that is not in the Previred bank view: CODIGOBANCO_PREVIRED_ERROR', L(F, 'CODIGOBANCO_PREVIRED_ERROR'),
     ent([fila(banco_='Otro Banco')]), seed(BASE), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(188, 'CargaMasivaBanco', 'fila-no-ok-no-valida-empleador-pero-igual-deposita', 'a row whose MENSAJEVALIDACION is not OK is skipped by the employer check but STILL becomes a deposit (every row is processed)', L(F, 'equalsIgnoreCase\\(fila.MENSAJEVALIDACION\\)'),
     ent([fila(), fila(folio=2, rut='0088506100', valida='NOK')]), seed(BASE), secuencias=SEQS,
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1), res_proceso('IdComprobante', 8002, ING, 'ING', 2)])}},
     tablas=[TREC, TCARGA])
caso(189, 'CargaMasivaBanco', 'todas-las-filas-no-ok-no-hay-empleadores', 'every row NOK: the employer list is empty, so the employer error (id 0) is returned', L(F, 'rutsEmpleadores.size\\(\\) > 0'),
     ent([fila(valida='NOK')]), seed(BASE), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(190, 'CargaMasivaBanco', 'error-al-crear-deposito-detiene-sin-registrar-carga', 'the deposit creation reports an error (no PAC cash box): its messages and counter are returned and the CargaMasiva row is NOT written', L(F, 'if\\(salida.P_OUT_ERRORESCONTADOR > 0\\) return salida'),
     ent([fila()]), seed(empresa(), banco(12, 3012), cuenta_banco(122, 12, '4360602'), SUC, BP, pj()), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(191, 'CargaMasivaBanco', 'excepcion-al-crear-deposito-se-traga-con-rollback', 'an exception while creating the deposit (settlement parameter missing) is swallowed after con.rollback(): no deposit, no message, yet the CargaMasiva row is written and committed',
     L(F, 'con.rollback\\(\\);'), ent([fila()]), seed(BASE), secuencias=SEQS, config={'APV/REC.LiquidacionAutomatica': None}, colaboradores=TESO, tablas=[TREC, TCARGA])
caso(192, 'CargaMasivaBanco', 'monto-no-numerico-lanza-NumberFormatException', 'MontoAbono that is not a number: NumberFormatException', L(F, 'PR.MontoDeposito = BigDecimal.valueOf\\(Long.parseLong'),
     ent([fila(monto='12.5')]), seed(BASE), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(193, 'CargaMasivaBanco', 'nombre-nulo-lanza-NullPointerException', 'p_NombreCarga missing: NullPointerException at the pattern match', L(F, 'in.p_NombreCarga.matches'),
     {'p_IdEmpresa': 1, 'p_Archivo': [], 'p_Parametros': []}, seed(BASE), secuencias=SEQS, tablas=[TREC, TCARGA])
caso(194, 'CargaMasivaBanco', 'dos-filas-dos-depositos-correlativos', 'two rows of two employers: two deposits with consecutive ids and one message each', L(F, 'for \\(CargaMasivaBanco.archivoBanco fila : in.p_Parametros\\) \\{\\s*$'),
     ent([fila(), fila(folio=2, rut='0088506100')]), seed(BASE, pj('8850610-0', 4401)), secuencias=SEQS,
     colaboradores={'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1), res_proceso('IdComprobante', 8002, ING, 'ING', 2)])}},
     tablas=[TREC, TCARGA])
caso(195, 'CargaMasivaBanco', 'cuenta-se-normaliza-ceros-y-espacios', 'the institution account "000004360602   " is stored without leading zeros and trailing blanks', L(F, 'replaceFirst\\("\\^0\\+", ""\\)'),
     ent([fila(cuenta='000004360602   ')]), seed(BASE), secuencias=SEQS, colaboradores=TESO, tablas=[TREC])
caso(196, 'CargaMasivaBanco', 'rut-con-digito-k', 'a RUT with check digit K is rebuilt as "<number>-K"', L(F, 'rutLongEmpleador.toString\\(\\) \\+ "-"'),
     ent([fila(rut='012345678K')]), seed(BASE, pj('12345678-K', 4402)), secuencias=SEQS, colaboradores=TESO, tablas=[TREC])
caso(197, 'CargaMasivaBanco', 'sin-archivo-lanza-IndexOutOfBounds', 'empty p_Archivo: after the deposit step in.p_Archivo.get(0) fails: IndexOutOfBoundsException (rolled back)', L(F, 'in.p_Archivo.get\\(0\\)'),
     ent([fila()], archivos=[]), seed(BASE), secuencias=SEQS, colaboradores=TESO, tablas=[TREC, TCARGA])
