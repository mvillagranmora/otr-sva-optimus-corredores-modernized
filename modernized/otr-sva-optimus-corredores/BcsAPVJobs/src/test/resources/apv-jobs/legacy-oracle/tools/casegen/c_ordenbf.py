"""IngresarOrdenBonificacionFiscal cases (APVJ-OBF-NNN). Legacy: APV/JAVA/BcsAPVJobs/src/cl/bcs/APV/jobs/IngresarOrdenBonificacionFiscal.java"""
from lib import caso, seed, programa

JOB = 'IngresarOrdenBonificacionFiscal'
F = 'IngresarOrdenBonificacionFiscal.java'
CO = 'CrearOrdenes'


def c(n, slug, titulo, pina, **kw):
    kw.setdefault('seed_sql', seed(programa(kw.get('idp', 777))))
    return caso('OBF', n, JOB, slug, titulo, pina, **kw)


def lanza(clase, msg):
    return {CO: {'_lanza': {'clase': clase, 'mensaje': msg}}}


c(1, 'orden-fija-de-prueba-se-crea-y-confirma', 'the hard-coded demo order is sent to CrearOrdenes, the log row is written, three commits (job, job, runner)',
  F + ':47-142')
c(2, 'mensajes-de-error-del-servicio-se-ignoran', 'error messages in the CrearOrdenes output are ignored: still commits', F + ':129-131',
  colaboradores={CO: {'P_OUT_MENSAJES': [{'id': 1, 'tipo': 'ERROR', 'codigo': 'E1', 'descripcion': 'cupo'}], 'P_OUT_ERRORESCONTADOR': 1}})
c(3, 'servicio-lanza-exception-rollback-borra-el-log', 'CrearOrdenes throws Exception: rollback (the log row is lost), then commit twice, result COMMIT',
  F + ':127-136', colaboradores=lanza('Exception', 'falla orden'), pin_mensaje=True)
c(4, 'servicio-lanza-runtime-igual', 'CrearOrdenes throws a RuntimeException: swallowed the same way', F + ':127-136', colaboradores=lanza('RuntimeException', 'rt'))
c(5, 'servicio-lanza-sqlexception-igual', 'CrearOrdenes throws SQLException: swallowed', F + ':127-136', colaboradores=lanza('SQLException', 'sql'))
c(6, 'servicio-lanza-error-no-se-captura', 'CrearOrdenes throws an Error: not caught, no rollback of the job, ERROR status', F + ':127-136',
  colaboradores=lanza('AssertionError', 'fatal'), pin_mensaje=True)
c(7, 'otro-idprograma-en-el-log', 'the log row carries the IdPrograma of the run', F + ':47-58', idp=5, seed_sql=seed(programa(5)))
c(8, 'idprograma-grande', 'a 10-digit IdPrograma', F + ':47-58', idp=9999999999, seed_sql=seed(programa(9999999999)))
c(9, 'sin-fila-de-programa-no-falla', 'no BAT.Programa row: nothing needs it, the job runs', F + ':47-58', seed_sql='')
c(10, 'salida-con-resultado-no-se-usa', 'a scripted output with results is printed only', F + ':130',
  colaboradores={CO: {'P_OUT_RESULTADO': [{'IdInterno': 1, 'IdOrden': 99}], 'P_OUT_ERRORESCONTADOR': 0}})
