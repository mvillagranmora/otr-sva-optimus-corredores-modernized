"""IngresarOrdenRVBonificacionFiscal cases, part 2: selection, parameters, messages, failures (APVJ-RVB-026..)."""
from lib import caso, seed, ins, programa, parametros
from fmlib import juntar, portafolio, regimen
from rvlib import *

JOB = 'IngresarOrdenRVBonificacionFiscal'
F = 'IngresarOrdenRVBonificacionFiscal.java'
OKC = juntar(precios((31, '1000')), flujo((2000, 'CLP', '1000000')), calculo((1, '15')), creadas())


def c(n, slug, titulo, pina, ordenes=None, colab=None, seed_sql=None, extra=(), **kw):
    s = seed_sql if seed_sql is not None else seed(base(ordenes=ordenes), *extra)
    return caso('RVB', n, JOB, slug, titulo, pina, seed_sql=s, colaboradores=colab if colab is not None else OKC, tablas=TABLAS_RV, **kw)


def lanza(srv, clase='RuntimeException', msg='x'):
    return {srv: {'_lanza': {'clase': clase, 'mensaje': msg}}}


def solo(*pares, filas=()):
    return seed(programa(777), parametros(777, *pares), regimen(), tipos_operacion(), list(filas))


c(26, 'sin-ordenes-en-los-parametros', 'no IdOrden parameter: log "No cuentan con portafolio BF", returns true before any collaborator', F + ':258-261',
  seed_sql=solo(('IdEmpresa', '1'), ('IdUsuario', '21')), colab={})
c(27, 'cliente-sin-regimen-bf', 'the client regime is not BF: nothing selected, the same log', F + ':253-261', seed_sql=seed(base(bf=False)), colab={})
c(28, 'portafolio-sin-proposito-apv', 'TienePropositoAPV N: not selected', F + ':253-261',
  seed_sql=seed(base(portafolios=[portafolio(100, 2000, aplica='N')])), colab={})
c(29, 'dos-portafolios-apv-duplican-la-orden', 'two APV portfolios for the client: the join returns the order twice', F + ':253',
  seed_sql=seed(base(portafolios=[portafolio(100, 2000), portafolio(100, 2002)])), colab=juntar(OKC, calculo((1, '15'), (2, '15'))))
c(30, 'tres-ordenes-van-por-idorden-descendente', 'three orders are processed by IdOrden DESC with correlative IdInterno', F + ':253',
  ordenes=[orden(1, cliente=100), orden(2, cliente=101), orden(3, cliente=102)],
  colab=juntar(OKC, flujo((2000, 'CLP', '1000000'), (2001, 'CLP', '1000000'), (2002, 'CLP', '1000000')), calculo((1, '5'), (2, '6'), (3, '7'))))
c(31, 'calculo-pisa-la-cantidad-solo-donde-coincide-el-id', 'CalcularMonto answers IdInterno 1 and 9: only the order with IdInterno 1 gets CantidadOperacion', F + ':577-586',
  colab=juntar(OKC, calculo((1, '15'), (9, '77'))))
c(32, 'calculo-sin-resultado-deja-la-cantidad', 'CalcularMonto answers nothing: Cantidad stays 0', F + ':577-586', colab=juntar(OKC, calculo()))
c(33, 'mensajes-de-crear-ordenes-se-registran-todos', 'every CrearOrdenes message (any type) becomes "ADVERTENCIAS RV : ..."', F + ':600-602',
  colab=juntar(OKC, creadas(('ERROR', 'e'), ('INFORMACION', 'i'), ('ADVERTENCIA', 'a'))))
c(34, 'crear-ordenes-lanza', 'CrearOrdenes throws: the exception is rethrown, ERROR, rollback, the temporary table is dropped', F + ':598-621',
  colab=juntar(OKC, lanza('CrearOrdenes', 'Exception', 'sin cupo')), pin_mensaje=True)
c(35, 'calcular-monto-lanza', 'CalcularMontoYCobrosOrden throws: ERROR', F + ':577', colab=juntar(OKC, lanza('CalcularMontoYCobrosOrden')), pin_mensaje=True)
c(36, 'procedimiento-de-precios-lanza', 'the price procedure throws SQLException: ERROR, the finally drops the temporary table', F + ':294-298',
  colab=juntar(OKC, {'SalidaProcedimiento': {SP_PRECIO: {'_lanza': {'clase': 'SQLException', 'mensaje': 'proc'}}}}), pin_mensaje=True)
c(37, 'custodia-lanza', 'ConsultarCustodiaPortafolio throws on a VENTA: ERROR', F + ':331', ordenes=[orden(1, tipo='VENACN')], colab=juntar(OKC, lanza('ConsultarCustodiaPortafolio')))
c(38, 'flujo-de-caja-lanza', 'ConsultarFlujoCaja throws on a COMPRA: ERROR', F + ':371', colab=juntar(OKC, lanza('ConsultarFlujoCaja')))
c(39, 'error-no-exception-no-se-captura', 'an Error from a collaborator is not an Exception: the catch does not run, the finally drops the table, ERROR status', F + ':606-621',
  colab=juntar(OKC, lanza('CrearOrdenes', 'AssertionError', 'fatal')))
c(40, 'sin-idempresa-es-cero', 'no IdEmpresa parameter: 0 in every collaborator input and in the configuration read', F + ':238',
  seed_sql=solo(('IdUsuario', '21'), ('IdOrden', '1'), filas=[orden(1), portafolio(100, 2000)]))
c(41, 'sin-idusuario-es-cero', 'no IdUsuario parameter: 0 in the inputs', F + ':239',
  seed_sql=solo(('IdEmpresa', '1'), ('IdOrden', '1'), filas=[orden(1), portafolio(100, 2000)]))
c(42, 'idorden-no-numerico', 'IdOrden "x": NumberFormatException', F + ':240', seed_sql=solo(('IdEmpresa', '1'), ('IdOrden', 'x')))
c(43, 'idorden-repetido-no-duplica-filas', 'the same IdOrden twice in the parameters: IN list, the order is still read once', F + ':240',
  seed_sql=solo(('IdEmpresa', '1'), ('IdUsuario', '21'), ('IdOrden', '1'), ('IdOrden', '1'), filas=[orden(1), portafolio(100, 2000)]))
c(44, 'porcentaje-no-configurado', 'APV.PorcentajeBonificacionFiscal missing: Exception', F + ':249', config={'APV/APV.PorcentajeBonificacionFiscal': None}, pin_mensaje=True)
c(45, 'moneda-sistema-no-configurada', 'PLA.MonedaSistema missing: Exception (it is read although never used)', F + ':244', config={'PLATAFORMA/PLA.MonedaSistema': None}, pin_mensaje=True)
c(46, 'fecha-sistema-no-configurada', 'PLA.FechaSistema missing: Exception', F + ':243', config={'PLATAFORMA/PLA.FechaSistema': None}, pin_mensaje=True)
c(47, 'saldo-insuficiente-con-tipo-cambio-nulo-lanza-npe', 'insufficient saldo and TipoCambio NULL: NullPointerException while building the log', F + ':461',
  ordenes=[orden(1, TipoCambio=None)], colab=juntar(OKC, flujo((2000, 'CLP', '1'))))
c(48, 'codigo-clase-orden-define-esod', 'CodigoClaseOrden NULL -> EsOD S, "" -> N, "X" -> S (CASE of the query)', F + ':253',
  ordenes=[orden(1, cliente=100, CodigoClaseOrden=None), orden(2, cliente=101), orden(3, cliente=102, CodigoClaseOrden='X')],
  colab=juntar(OKC, flujo((2000, 'CLP', '1000000'), (2001, 'CLP', '1000000'), (2002, 'CLP', '1000000')), calculo((1, '1'), (2, '1'), (3, '1'))))
c(49, 'compra-con-monto-nulo-lanza-npe', 'COMPRA with MontoOrden NULL: NullPointerException', F + ':426', ordenes=[orden(1, monto=None)])
c(50, 'venta-con-cantidad-nula-lanza-npe', 'VENTA with Cantidad NULL: NullPointerException', F + ':339', ordenes=[orden(1, tipo='VENACN', cantidad=None)],
  colab=juntar(OKC, custodia('1000')))
