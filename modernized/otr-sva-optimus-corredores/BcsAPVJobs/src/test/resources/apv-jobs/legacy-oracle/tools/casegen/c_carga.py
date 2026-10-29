"""BcsJobCargaMasivaPrevired cases (APVJ-CMP-NNN). Legacy: APV/JAVA/BcsAPVJobs/src/cl/bcs/APV/jobs/BcsJobCargaMasivaPrevired.java"""
import json
from lib import caso, seed, programa, TABLAS_BASE

JOB = 'BcsJobCargaMasivaPrevired'
F = 'BcsJobCargaMasivaPrevired.java'
CB = 'CargaMasivaPrevired'


def entrada(**kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 17, 'p_IdModulo': 'APV', 'p_NombreCarga': 'APV_PREVIRED_ABC123.txt', 'p_Origen': 'PRV',
         'p_IdEmpleado': 5, 'p_Archivo': [], 'p_Parametros': []}
    e.update(kw)
    return e


def par(e):
    return {'p_parametros': json.dumps({'p_parametros': e})}


def msg(i, tipo, cod, desc):
    return {'id': i, 'tipo': tipo, 'codigo': cod, 'descripcion': desc}


def sal(*m, err=0):
    return {CB: {'P_OUT_MENSAJES': list(m), 'P_OUT_ERRORESCONTADOR': err}}


def c(n, slug, titulo, pina, **kw):
    kw.setdefault('carpetas', ['APV'])
    kw.setdefault('seed_sql', seed(programa(777)))
    return caso('CMP', n, JOB, slug, titulo, pina, **kw)


OK2 = sal(msg(1, 'INFORMACION', 'OK', 'Archivo cargado'), msg(2, 'ADVERTENCIA', 'ADV01', 'Rut duplicado'))
c(1, 'dos-mensajes-escribe-log-y-ruta-salida', 'two messages: log file written, RutaSalida updated, collaborator called with the entrada', F + ':67-114',
  parametros=par(entrada()), colaboradores=OK2)
c(2, 'sin-mensajes-solo-encabezado-y-fin', 'no messages: the file holds the header lines and FIN RESULTADO', F + ':93-110',
  parametros=par(entrada()), colaboradores=sal())
c(3, 'entrada-completa-llega-al-servicio', 'p_Archivo and p_Parametros rows reach the service with every field', F + ':132-139',
  parametros=par(entrada(p_Archivo=[{'IdEmpresa': 1, 'IdCargaMasiva': 9, 'NombreCargaMasiva': 'APV_PREVIRED_ABC123.txt', 'CantidadRegistros': 2,
                                     'IdUsuario': 17, 'IdTipoCarga': 3, 'Descripcion': 'carga', 'FechaCarga': '2025-04-01', 'IdEstado': 'ACT'}],
                         p_Parametros=[{'fila1': 1, 'Folio': 100, 'Tipo_Pag': 2, 'FechaOperacion': 20250401, 'HoraOperacion': '10:00', 'INS_APV': 7,
                                        'Rut_Empleador': '76123456-7', 'Rut_Trabajador': '11111111-1', 'Monto_APV': 15000, 'Monto_Total': 15000,
                                        'Periodo_Pago': '032025', 'NroRegistro': 1, 'MENSAJEVALIDACION': None}])),
  colaboradores=OK2)
c(4, 'mensaje-con-campos-nulos-imprime-null', 'a message with null fields is written as the text null', F + ':104-108',
  parametros=par(entrada()), colaboradores=sal({'id': None, 'tipo': None, 'codigo': None, 'descripcion': None}))
c(5, 'descripcion-con-tildes-y-enie', 'accents and enie in a description are written (UTF-8)', F + ':104-108',
  parametros=par(entrada()), colaboradores=sal(msg(1, 'ERROR', 'E1', 'Año inválido: señor Núñez, país Perú')), )
c(6, 'treinta-mensajes-en-orden', 'thirty messages keep their order', F + ':102-108',
  parametros=par(entrada()), colaboradores=sal(*[msg(i, 'INFORMACION', 'OK%d' % i, 'linea %d' % i) for i in range(1, 31)]))
c(7, 'idusuario-nulo-da-null-en-el-nombre', 'p_IdUsuario null: the file name carries the text null', F + ':73-75',
  parametros=par(entrada(p_IdUsuario=None)), colaboradores=OK2)
c(8, 'idusuario-cero', 'p_IdUsuario 0 in the file name', F + ':73-75', parametros=par(entrada(p_IdUsuario=0)), colaboradores=OK2)
c(9, 'ruta-base-con-barra-final', 'base folder configured with a trailing slash: the file is still written, RutaSalida is relative', F + ':73-80',
  parametros=par(entrada()), colaboradores=OK2, config={'PLATAFORMA/PLA.RutaBaseArchivos': '<BASE>/'})
c(10, 'ruta-salida-previa-se-sobrescribe', 'an existing RutaSalida is overwritten', F + ':102',
  parametros=par(entrada()), colaboradores=OK2, seed_sql=seed(programa(777, 'APV/VIEJO.txt')))
c(11, 'sin-fila-de-programa-no-falla', 'no BAT.Programa row: the UPDATE touches nothing, the run commits', F + ':102',
  parametros=par(entrada()), colaboradores=OK2, seed_sql='')
c(12, 'sin-carpeta-apv-falla-el-filewriter', 'the APV folder does not exist: FileWriter fails BEFORE the mkdirs, the job ends in ERROR (legacy defect)',
  F + ':80-85', parametros=par(entrada()), colaboradores=OK2, carpetas=[])
c(13, 'sin-parametro-ruta-base', 'PLA.RutaBaseArchivos not configured: StoredProcedureException, no file', F + ':73',
  parametros=par(entrada()), colaboradores=OK2, config={'PLATAFORMA/PLA.RutaBaseArchivos': None})
c(14, 'sin-parametro-p-parametros', 'no p_parametros param: NullPointerException', F + ':69', parametros={}, colaboradores=OK2)
c(15, 'p-parametros-vacio', 'empty p_parametros text: IllegalStateException (not a JSON object)', F + ':135', parametros={'p_parametros': ''})
c(16, 'json-invalido', 'malformed JSON: JsonSyntaxException', F + ':135', parametros={'p_parametros': '{"p_parametros": '})
c(17, 'json-sin-clave-interna', 'valid JSON without the inner p_parametros key: NullPointerException', F + ':135-136', parametros={'p_parametros': '{}'})
c(18, 'clave-interna-es-texto', 'inner p_parametros is a string: ClassCastException', F + ':135', parametros={'p_parametros': '{"p_parametros": "x"}'})
c(19, 'clave-interna-es-null', 'inner p_parametros is null: ClassCastException', F + ':135', parametros={'p_parametros': '{"p_parametros": null}'})
c(20, 'entrada-vacia-sin-idempresa', 'inner {}: p_IdEmpresa null unboxes to NullPointerException', F + ':73', parametros={'p_parametros': '{"p_parametros": {}}'})
c(21, 'idempresa-no-numerico', 'p_IdEmpresa "abc": JsonSyntaxException', F + ':139', parametros=par(entrada(p_IdEmpresa='abc')))
c(22, 'clave-con-otra-capitalizacion', 'P_IdEmpresa (other case) is ignored: p_IdEmpresa null, NullPointerException', F + ':139',
  parametros={'p_parametros': json.dumps({'p_parametros': {'P_IdEmpresa': 1, 'p_IdUsuario': 17}})})
c(23, 'servicio-lanza-runtime-hace-rollback-y-commit', 'the service throws a RuntimeException: rollback, header-only file, the job still returns true (COMMIT)',
  F + ':115-118', parametros=par(entrada()), colaboradores={CB: {'_lanza': {'clase': 'RuntimeException', 'mensaje': 'boom'}}}, pin_mensaje=True)
c(24, 'servicio-lanza-sqlexception-igual', 'the service throws SQLException: same as a RuntimeException', F + ':115-118',
  parametros=par(entrada()), colaboradores={CB: {'_lanza': {'clase': 'SQLException', 'mensaje': 'sql boom'}}})
c(25, 'servicio-lanza-error-no-se-captura', 'the service throws an Error: not caught by the job, no explicit rollback, ERROR status', F + ':115-121',
  parametros=par(entrada()), colaboradores={CB: {'_lanza': {'clase': 'AssertionError', 'mensaje': 'fatal'}}}, pin_mensaje=True)
c(26, 'mensajes-nulos-rollback-deshace-ruta-salida', 'P_OUT_MENSAJES null: NullPointerException AFTER the RutaSalida update, the rollback undoes it, COMMIT of nothing',
  F + ':102-108', parametros=par(entrada()), colaboradores={CB: {'P_OUT_MENSAJES': None, 'P_OUT_ERRORESCONTADOR': 0}})
c(27, 'claves-desconocidas-se-ignoran', 'unknown keys in the inner object are ignored', F + ':139',
  parametros=par(entrada(extra='x', otro={'a': 1})), colaboradores=OK2)
c(28, 'p-archivo-null-explicito', 'explicit null lists reach the service as null', F + ':139', parametros=par(entrada(p_Archivo=None, p_Parametros=None)),
  colaboradores=OK2)
c(29, 'idusuario-como-texto-numerico', 'p_IdUsuario "17" (text) is read as 17', F + ':139', parametros=par(entrada(p_IdUsuario='17')), colaboradores=OK2)
c(30, 'idusuario-decimal-falla', 'p_IdUsuario 12.5 for a Long: JsonSyntaxException', F + ':139', parametros=par(entrada(p_IdUsuario=12.5)))
