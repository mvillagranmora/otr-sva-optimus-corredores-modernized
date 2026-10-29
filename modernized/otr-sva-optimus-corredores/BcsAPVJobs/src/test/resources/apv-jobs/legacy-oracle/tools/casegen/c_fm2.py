"""IngresarOrdenFMBonificacionFiscal cases, part 2: parameters, selection, messages and failures (APVJ-FMB-027..)."""
from lib import caso, seed, programa, parametros
from fmlib import *

JOB = 'IngresarOrdenFMBonificacionFiscal'
F = 'IngresarOrdenFMBonificacionFiscal.java'
CR = 'CrearSolicitudAporteRescateNormal'
OK = juntar(empresa(), crear(('INFORMACION', 'ok'), resultado=[9001]))
SOLO_BASE = [serie(), conf_pais(), regimen()]


def c(n, slug, titulo, pina, seed_sql=None, colab=None, **kw):
    s = seed_sql if seed_sql is not None else seed(base())
    return caso('FMB', n, JOB, slug, titulo, pina, seed_sql=s, colaboradores=colab if colab is not None else OK, tablas=TABLAS_FM, **kw)


def con_params(*pares, filas=None):
    f = filas if filas is not None else [solicitud(1), portafolio(100, 2000)]
    return seed(programa(777), parametros(777, *pares), SOLO_BASE, f)


c(27, 'sin-solicitudes-en-los-parametros', 'no IdSolicitudAporteRescate parameter: IN (NULL) finds nothing, the flujo is still called with no carteras, log NO CUENTA CON PORTAFOLIO BF',
  F + ':310', seed_sql=con_params(('IdEmpresa', '1'), ('IdUsuario', '21')))
c(28, 'cliente-sin-regimen-bf', 'the client regime is not BF: no row, log NO CUENTA CON PORTAFOLIO BF', F + ':167', seed_sql=seed(base(bf=False)))
c(29, 'portafolio-inactivo-no-califica', 'the APV portfolio is not ACT: no row', F + ':167', seed_sql=con_params(('IdEmpresa', '1'), ('IdUsuario', '21'),
  ('IdSolicitudAporteRescate', '1'), filas=[solicitud(1), portafolio(100, 2000, estado='INA')]))
c(30, 'portafolio-sin-proposito-apv-no-califica', 'TienePropositoAPV N: no row', F + ':167', seed_sql=con_params(('IdEmpresa', '1'), ('IdUsuario', '21'),
  ('IdSolicitudAporteRescate', '1'), filas=[solicitud(1), portafolio(100, 2000, aplica='N')]))
c(31, 'solicitud-fuera-de-los-parametros-se-ignora', 'a request not listed in the parameters is not processed', F + ':152',
  seed_sql=con_params(('IdEmpresa', '1'), ('IdUsuario', '21'), ('IdSolicitudAporteRescate', '1'),
                      filas=[solicitud(1), portafolio(100, 2000), solicitud(2, cliente=101), portafolio(101, 2001)]))
c(32, 'idempresa-repetido-gana-el-ultimo', 'IdEmpresa given twice: the last one (by IdSecuencia) wins', F + ':150',
  seed_sql=con_params(('IdEmpresa', '9'), ('IdUsuario', '21'), ('IdSolicitudAporteRescate', '1'), ('IdEmpresa', '1')))
c(33, 'sin-idusuario-es-cero', 'no IdUsuario parameter: 0 goes to the collaborators', F + ':151',
  seed_sql=con_params(('IdEmpresa', '1'), ('IdSolicitudAporteRescate', '1')))
c(34, 'sin-idempresa-es-cero', 'no IdEmpresa parameter: 0 is used (ConsultarEmpresa and the configuration read are asked for company 0)', F + ':150',
  seed_sql=con_params(('IdUsuario', '21'), ('IdSolicitudAporteRescate', '1')))
c(35, 'idusuario-no-numerico', 'IdUsuario "abc": NumberFormatException', F + ':151', seed_sql=con_params(('IdEmpresa', '1'), ('IdUsuario', 'abc'), ('IdSolicitudAporteRescate', '1')))
c(36, 'valor-nulo-en-un-parametro', 'a NULL Valor for IdUsuario: NumberFormatException', F + ':151',
  seed_sql=seed(programa(777), ins('BSCL.Plataforma.BD::BAT.ParametrosPrograma', IdPrograma=777, IdSecuencia=1, NombreParametro='IdEmpresa', Valor='1'),
                ins('BSCL.Plataforma.BD::BAT.ParametrosPrograma', IdPrograma=777, IdSecuencia=2, NombreParametro='IdUsuario', Valor=None), SOLO_BASE))
c(37, 'nombre-de-parametro-nulo', 'a NULL NombreParametro: NullPointerException', F + ':150',
  seed_sql=seed(programa(777), ins('BSCL.Plataforma.BD::BAT.ParametrosPrograma', IdPrograma=777, IdSecuencia=1, NombreParametro=None, Valor='1'), SOLO_BASE))
c(38, 'nombres-con-otra-capitalizacion-se-ignoran', 'parameter names are case sensitive: idempresa is ignored (company 0)', F + ':150',
  seed_sql=con_params(('idempresa', '1'), ('IdUsuario', '21'), ('IdSolicitudAporteRescate', '1')))
c(39, 'empresa-no-existe', 'ConsultarEmpresa has no answer: Exception "Empresa no existe"', F + ':155', colab=crear(('INFORMACION', 'ok')), pin_mensaje=True)
c(40, 'empresa-lanza', 'ConsultarEmpresa throws SQLException', F + ':155', colab=juntar(crear(), {'ConsultarEmpresa': {'_lanza': {'clase': 'SQLException', 'mensaje': 'x'}}}),
  pin_mensaje=True)
c(41, 'flujo-lanza', 'ConsultarFlujoCustodiaFondoMutuo throws: ERROR, rollback', F + ':208',
  colab=juntar(OK, {'ConsultarFlujoCustodiaFondoMutuo': {'_lanza': {'clase': 'RuntimeException', 'mensaje': 'flujo'}}}), pin_mensaje=True)
c(42, 'crear-solicitud-lanza', 'CrearSolicitudAporteRescateNormal throws: ERROR, rollback, no commit', F + ':291',
  colab=juntar(empresa(), {CR: {'_lanza': {'clase': 'Exception', 'mensaje': 'sin cupo'}}}), pin_mensaje=True)
c(43, 'mensaje-advertencia-se-registra', 'an ADVERTENCIA message is logged as "ADVERTENCIAS FFMM : ..."', F + ':302-304',
  colab=juntar(empresa(), crear(('ADVERTENCIA', 'saldo bajo'))))
c(44, 'mensaje-error-se-registra-y-no-falla', 'an ERROR message is logged as "ERRORES FFMM : ..." and the job still commits', F + ':305-307',
  colab=juntar(empresa(), crear(('ERROR', 'serie cerrada'))))
c(45, 'mensajes-de-otro-tipo-no-se-registran', 'INFORMACION and AUTORIZACION messages are not logged', F + ':301-308',
  colab=juntar(empresa(), crear(('INFORMACION', 'i'), ('AUTORIZACION', 'a'))))
c(46, 'varios-mensajes-en-orden', 'ERROR, ADVERTENCIA, ERROR: three log rows in order', F + ':301-308',
  colab=juntar(empresa(), crear(('ERROR', 'e1'), ('ADVERTENCIA', 'a1'), ('ERROR', 'e2'))))
c(47, 'mensajes-nulos-lanza-npe', 'P_OUT_MENSAJES null: NullPointerException after the request was created', F + ':301',
  colab=juntar(empresa(), {CR: {'P_OUT_MENSAJES': None, 'P_OUT_RESULTADO': []}}))
c(48, 'resultado-nulo-lanza-npe', 'P_OUT_RESULTADO null: NullPointerException', F + ':296-299',
  colab=juntar(empresa(), {CR: {'P_OUT_RESULTADO': None, 'P_OUT_MENSAJES': []}}))
c(49, 'sin-fila-de-programa', 'no BAT.Programa row: the log rows still reference the IdPrograma', F + ':310',
  seed_sql=seed(parametros(777, ('IdEmpresa', '1'), ('IdUsuario', '21')), SOLO_BASE))
c(50, 'otro-idprograma', 'IdPrograma 31 in the log rows', F + ':310', idp=31,
  seed_sql=seed(programa(31), parametros(31, ('IdEmpresa', '1'), ('IdUsuario', '21')), SOLO_BASE))
