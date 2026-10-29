"""Family C cases: CargaMasivaPrevired (APV-C-110 .. 139)."""
from fix import (T_CARGA, T_RCLI, T_REC, T_REMP, T_V_CLIENTE, T_V_PJ, ins, rec, remp)
from lib import L, caso, seed, tabla

F = 'CargaMasivaPrevired.java'
T_V_PER = 'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona'
SEQE = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador'
SEQC = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente'
SEQM = 'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva'
SEQS = {SEQE: 600, SEQC: 500, SEQM: 80}
NOMBRE = 'PL03BCH_20231002_O01I.txt'
FOLIO = 2069202305000228

TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'CodigoArchivo', 'FolioCartola', 'IdEstado'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'CodigoArchivo', 'IdPersona', 'PeriodoPago', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'TipoAfiliado', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdCliente', 'IdRegimenTriburario', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'IdEstado'])
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"')


def fila(emp='0076129263', cli='0111849315', folio=FOLIO, apv=20000, dc=0, periodo='202002', razon='CLAUDIA REYES', total_apv=None, total_dc=None):
    return {'fila1': 1, 'Folio': folio, 'Tipo_Pag': 1, 'FechaOperacion': 20200330, 'HoraOperacion': '194151', 'INS_APV': 227, 'Rut_Empleador': emp, 'Tipo_Empleador': '3', 'RazonSocial_Empleador': razon,
            'Rut_Trabajador': cli, 'Ape_Pat_Trabajador': 'REYES', 'Ape_Mat_Trabajador': 'MURARO', 'Nom_Trabajador': 'CLAUDIA', 'Sexo_Trabajador': 'F', 'Renta_Imponible': 301000, 'Monto_APV': apv,
            'Monto_DC': dc, 'Monto_Total': apv + dc, 'Periodo_Pago': periodo, 'Monto_Total_APV': apv if total_apv is None else total_apv, 'Monto_Total_DC': dc if total_dc is None else total_dc,
            'Monto_Total1': apv + dc, 'Periodo_Pago1': int(periodo), 'Num_Afiliados': 1, 'NroRegistro': 1, 'MENSAJEVALIDACION': 'OK'}


def ent(filas, nombre=NOMBRE, archivos=None):
    arch = archivos if archivos is not None else [{'IdEmpresa': 1, 'IdCargaMasiva': 0, 'NombreCargaMasiva': nombre, 'CantidadRegistros': len(filas), 'IdUsuario': 1006, 'IdTipoCarga': 1,
                                                  'Descripcion': 'Carga Previred', 'FechaCarga': '2020-09-30T03:00:00.000Z', 'IdEstado': 'VIG'}]
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 74352, 'p_IdModulo': 'APV', 'p_NombreCarga': nombre, 'p_Origen': 'PREVRED', 'p_IdEmpleado': 845, 'p_Archivo': arch, 'p_Parametros': filas}


def pj(doc='7612926-3', idpersona=4400, busqueda=None):
    return ins(T_V_PJ, IdPersona=idpersona, PersonaBusqueda=busqueda or (doc + ' CLAUDIA REYES'), DocumentoIdentidad=doc, IdEstado='ACT')


def vcli(doc='11184931-5', idcliente=7001):
    return ins(T_V_CLIENTE, IdCliente=idcliente, DocumentoIdentidad=doc)


def deposito(idrec=10, folio=FOLIO, codigo='O01I'):
    return rec(idrec, 'ING', monto=100000, folio=folio, codigo_archivo=codigo)


BASE = [deposito(), pj(), vcli()]

caso(110, 'CargaMasivaPrevired', 'nombre-con-menos-de-tres-partes', 'file name without three "_" parts: "Formato Nombre de Archivo Incorrecto", counter 1, returns before the log', L(F, 'if\\(CodigoArchivoExtencion.length > 2\\)'),
     ent([fila()], nombre='Previred.txt'), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(111, 'CargaMasivaPrevired', 'tercera-parte-solo-punto', 'third part "." splits into an empty array: same format error', L(F, 'if\\(CodigoArchivoList.length > 0\\)'),
     ent([fila()], nombre='a_b_.'), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(112, 'CargaMasivaPrevired', 'sin-depositos-del-archivo-banco', 'no deposit with that file code: "Antes de cargar el Detalle debe realizar la carga del archivo Banco.", counter 1, the log IS written',
     L(F, 'Antes de cargar el Detalle'), ent([fila()]), seed(pj(), vcli()), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(113, 'CargaMasivaPrevired', 'archivo-ya-cargado', 'a CargaMasiva with the same file name and description "Carga Previred" exists: "El archivo ya fue cargado previamente."', L(F, 'El archivo ya fue cargado previamente'),
     ent([fila()]), seed(BASE, ins(T_CARGA, IdEmpresa=1, IdCargaMasiva=70, NombreCargaArchivo=NOMBRE, CantidadRegistros=1, IdUsuario=1, IdTipoCarga=1, Descripcion='Carga Previred', FechaCarga='2020-01-01', IdEstado='VIG')),
     secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(114, 'CargaMasivaPrevired', 'carga-un-empleador-y-un-cliente-apv', 'happy path: one employer detail, one client detail (APV only), the CargaMasiva row and the two total messages', L(F, 'Total Detalle Empleadores'),
     ent([fila()]), seed(BASE), secuencias=SEQS, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(115, 'CargaMasivaPrevired', 'cliente-con-apv-y-dc-crea-dos-detalles', 'worker with APV and DC amounts both non-zero: two client details (APV and DC regimes)', L(F, 'CLI2.IdRegimenTriburario = "DC"'),
     ent([fila(apv=20000, dc=5000)]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(116, 'CargaMasivaPrevired', 'cliente-solo-dc', 'worker with only a DC amount: one client detail with regimen DC', L(F, 'CLI.IdRegimenTriburario = "DC"'),
     ent([fila(apv=0, dc=7000)]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(117, 'CargaMasivaPrevired', 'cliente-sin-montos-regimen-vacio', 'worker with zero APV and zero DC: one client detail with an empty regimen', L(F, 'CLI.IdRegimenTriburario = ""'),
     ent([fila(apv=0, dc=0)]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(118, 'CargaMasivaPrevired', 'empleador-inexistente-advertencia-y-sin-cliente', 'employer not a legal person in the view: ADVERTENCIA, no employer and no client for that row, counted as rejected', L(F, 'no es un Empleador valido'),
     ent([fila()]), seed(deposito(), vcli()), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(119, 'CargaMasivaPrevired', 'mismo-empleador-y-folio-reutiliza-el-detalle', 'two workers of the same employer and folio: ONE employer detail, two client details pointing to it', L(F, 'IdRecaudacionEmpleador = IdRecaudacionEmpleadorAnterior'),
     ent([fila(), fila(cli='0087654321')]), seed(BASE, vcli('8765432-1', 7002)), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(120, 'CargaMasivaPrevired', 'mismo-empleador-otro-folio-crea-otro-detalle', 'same employer, another deposit folio: a second employer detail is created with the previous IdPersona', L(F, 'EMP.IdPersona = IdPersonaEmpleadorAnterior'),
     ent([fila(), fila(cli='0087654321', folio=FOLIO + 1)]), seed(BASE, deposito(11, FOLIO + 1), vcli('8765432-1', 7002)), secuencias=SEQS, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(121, 'CargaMasivaPrevired', 'dos-empleadores-distintos', 'two different employers: two employer details and two clients', L(F, 'if\\(DocumentoIdentidadAnterior.equals\\(rutPer\\) == false\\)'),
     ent([fila(), fila(emp='0099887766', cli='0087654321', razon='OTRA EMPRESA')]), seed(BASE, pj('9988776-6', 4401), vcli('8765432-1', 7002)), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(122, 'CargaMasivaPrevired', 'empleador-inexistente-segunda-fila-repite-advertencia', 'two rows of an unknown employer: the second row hits the previous-employer-0 branch: another ADVERTENCIA, client rejected', L(F, 'if\\(IdRecaudacionEmpleadorAnterior == 0\\)'),
     ent([fila(), fila(cli='0087654321')]), seed(deposito(), vcli(), vcli('8765432-1', 7002)), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(123, 'CargaMasivaPrevired', 'cliente-inexistente-error', 'worker not found in the client view: ERROR "El Cliente con Documento Identidad: X no existe." (the counter is not incremented)', L(F, 'no existe\\."'),
     ent([fila(cli='0055555555')]), seed(deposito(), pj()), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(124, 'CargaMasivaPrevired', 'varios-empleadores-se-elige-por-razon-social', 'two legal persons with the same document: the one whose PersonaBusqueda equals "<rut> <razon social>" (case-insensitive) is chosen', L(F, 'emp.PersonaBusqueda.equalsIgnoreCase'),
     ent([fila(razon='claudia reyes')]), seed(deposito(), vcli(), pj('7612926-3', 4400, '7612926-3 OTRA RAZON'), pj('7612926-3', 4402, '7612926-3 CLAUDIA REYES')), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(125, 'CargaMasivaPrevired', 'empleador-con-detalle-previo-usa-su-persona', 'the employer already has a detail in this empresa: the IdPersona of that detail is used', L(F, 'ExisteDetalleEmpleador.size\\(\\) == 0 \\? IdPersona'),
     ent([fila()]), seed(BASE, remp(300, 10, 'ING', persona=4999, apv=1), ins(T_V_PER, DocumentoIdentidad='7612926-3', IdPersona=4999)), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(126, 'CargaMasivaPrevired', 'periodo-corto-lanza-StringIndexOutOfBounds', 'Periodo_Pago shorter than 6 characters: StringIndexOutOfBoundsException', L(F, 'fila.Periodo_Pago.substring\\(0, 4\\)'),
     ent([dict(fila(), Periodo_Pago='2020')]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(127, 'CargaMasivaPrevired', 'rut-empleador-nulo-lanza-NullPointerException', 'Rut_Empleador missing: NullPointerException', L(F, 'Long rutLong = Long.parseLong'),
     ent([{k: v for k, v in fila().items() if k != 'Rut_Empleador'}]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(128, 'CargaMasivaPrevired', 'sin-archivo-lanza-IndexOutOfBounds', 'empty p_Archivo: after creating employers and clients, in.p_Archivo.get(0) throws IndexOutOfBoundsException (everything rolled back)', L(F, 'in.p_Archivo.get\\(0\\).IdCargaMasiva = idsCargaArchivo.get\\(0\\)'),
     ent([fila()], archivos=[]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(129, 'CargaMasivaPrevired', 'folio-sin-deposito-usa-cero', 'no deposit with the folio of the row: the employer detail is created with IdRecaudacion 0', L(F, 'private Long obtenerIdRecaudacionDeposito'),
     ent([fila(folio=999)]), seed(BASE), secuencias=SEQS, tablas=[TREC, TEMP, TCLI, TCARGA])
caso(130, 'CargaMasivaPrevired', 'sin-filas-solo-archivo', 'empty p_Parametros: nothing created, the CargaMasiva row is written with zero totals', L(F, 'CrearRecaudacionCliente.ParametrosSalidaServicio ObjOutCLi = crc.logica'),
     ent([]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
caso(131, 'CargaMasivaPrevired', 'dos-clientes-del-mismo-empleador-uno-inexistente', 'two workers, one unknown: one client created, one ERROR message, totals count the rejected one', L(F, 'CliRechazados\\+\\+;\\s*$'),
     ent([fila(), fila(cli='0055555555')]), seed(BASE), secuencias=SEQS, tablas=[TEMP, TCLI, TCARGA])
