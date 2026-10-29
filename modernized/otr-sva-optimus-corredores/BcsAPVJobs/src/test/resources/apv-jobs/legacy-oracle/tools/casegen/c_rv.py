"""IngresarOrdenRVBonificacionFiscal cases, part 1 (APVJ-RVB-001..): COMPRA and VENTA amounts. Legacy: .../IngresarOrdenRVBonificacionFiscal.java"""
from lib import caso, seed, ins
from fmlib import juntar
from rvlib import *

JOB = 'IngresarOrdenRVBonificacionFiscal'
F = 'IngresarOrdenRVBonificacionFiscal.java'
OKC = juntar(precios((31, '1000')), flujo((2000, 'CLP', '1000000')), calculo((1, '15')), creadas())
OKV = juntar(precios((31, '1000')), custodia('1000'), calculo((1, '15')), creadas())


def c(n, slug, titulo, pina, ordenes=None, colab=None, seed_sql=None, extra=(), **kw):
    s = seed_sql if seed_sql is not None else seed(base(ordenes=ordenes), *extra)
    return caso('RVB', n, JOB, slug, titulo, pina, seed_sql=s, colaboradores=colab if colab is not None else OKC, tablas=TABLAS_RV, **kw)


c(1, 'compra-por-monto-con-saldo-suficiente', 'COMPRA: MontoTotal = MontoNeto*15/100 UP, saldo ok, CalcularMonto and CrearOrdenes receive the order, no log', F + ':425-431')
c(2, 'compra-redondeo-up-a-escala-8', 'COMPRA 0.00000011 * 10 / 100 rounds UP (not HALF_UP) at the scale of the amount', F + ':426',
  ordenes=[orden(1, monto='0.00000011')], config={'APV/APV.PorcentajeBonificacionFiscal': '10'})
c(3, 'compra-saldo-menor-que-monto-pero-mayor-que-precio', 'saldo < monto but saldo >= price of ONE share: the order is created for the whole saldo (defect)', F + ':430-437',
  colab=juntar(OKC, flujo((2000, 'CLP', '5000'))))
c(4, 'compra-saldo-igual-al-precio-usa-el-saldo', 'saldo equal to the price (not less): the saldo becomes the amount', F + ':429',
  colab=juntar(OKC, flujo((2000, 'CLP', '1000'))))
c(5, 'compra-saldo-menor-que-el-precio-termina-con-advertencia', 'saldo < price: log "Saldo Caja Insuficiente", AgregarLog, the job returns true without creating orders', F + ':430-481',
  colab=juntar(OKC, flujo((2000, 'CLP', '999'))))
c(6, 'compra-saldo-igual-al-monto-no-cambia', 'saldo equal to the amount: amount unchanged', F + ':430', colab=juntar(OKC, flujo((2000, 'CLP', '15000'))))
c(7, 'compra-sin-flujo-de-caja-es-saldo-cero', 'no cash flow for the portfolio: saldo 0, insufficient', F + ':383-390', colab=juntar(OKC, flujo()))
c(8, 'compra-flujo-en-otra-moneda-se-ignora', 'a cash flow in another currency is filtered out', F + ':371-380', colab=juntar(OKC, flujo((2000, 'USD', '1000000'))))
c(9, 'compra-flujo-de-otra-fecha-se-ignora', 'a cash flow dated another day is filtered out', F + ':371-380',
  colab=juntar(OKC, {'ConsultarFlujoCaja': {'P_OUT_FLUJOCAJA': [{'FechaSaldo': '2025-03-31', 'IdPortafolio': 2000, 'IdMoneda': 'CLP', 'Saldo': '1000000'}]}}))
c(10, 'compra-segunda-orden-sin-flujo-hereda-el-saldo-anterior', 'the saldo variable is not reset per order: an order without cash flow reuses the previous order saldo (defect)',
  F + ':247-390', ordenes=[orden(1, cliente=100), orden(2, cliente=101)], colab=juntar(OKC, flujo((2001, 'CLP', '1000000')), calculo((1, '15'), (2, '15'))))
c(11, 'precio-sin-resultado-y-saldo-corto-lanza-npe', 'no price row for the instrument and saldo < amount: PrecioOrden null, NullPointerException', F + ':431',
  colab=juntar(OKC, precios(), flujo((2000, 'CLP', '10'))))
c(12, 'precio-sin-resultado-y-saldo-suficiente-sigue', 'no price row but enough saldo: PrecioOrden null is sent to CalcularMonto', F + ':396-400', colab=juntar(OKC, precios()))
c(13, 'precio-repetido-gana-el-ultimo', 'two price rows for the instrument: the last PrecioCierre is used', F + ':396-400', colab=juntar(OKC, precios((31, '900'), (31, '1100'))))
c(14, 'procedimiento-de-precios-sin-salida-lanza-npe', 'the procedure returns no p_out_Instrumento: NullPointerException', F + ':296-298',
  colab={'ConsultarFlujoCaja': OKC['ConsultarFlujoCaja']})
c(15, 'venta-con-custodia-suficiente', 'VENTA: Cantidad = 100*15/100 UP formatted to 2 decimals (truncate), custody enough', F + ':327-345', ordenes=[orden(1, tipo='VENACN')], colab=OKV)
c(16, 'venta-formato-trunca-a-dos-decimales', 'VENTA 0.07 * 15 / 100 = 0.0105 truncated to 0.01 by the currency format', F + ':339', ordenes=[orden(1, tipo='VENACN', cantidad='0.07')], colab=OKV)
c(17, 'venta-formato-redondea-half-up', 'currency USD format rounds HALF_UP to 0 decimals: 10*15% = 1.5 -> 2', F + ':339',
  ordenes=[orden(1, tipo='VENACN', cantidad='10', moneda='USD')], colab=OKV, extra=[formato(10, 'USD', 'ACN', 'VENACN', 0, 'R')])
c(18, 'venta-custodia-menor-se-vende-todo', 'custody < quantity: the whole custody is sold', F + ':338-344', ordenes=[orden(1, tipo='VENACN')], colab=juntar(OKV, custodia('3')))
c(19, 'venta-sin-custodia-termina-sin-ordenes', 'no custody row: the job returns true at once, no order, no price used', F + ':334-348', ordenes=[orden(1, tipo='VENACN')], colab=juntar(OKV, custodia()))
c(20, 'venta-solo-cuenta-la-primera-fila-de-custodia', 'two custody rows: only the first CantidadTotal is read', F + ':335', ordenes=[orden(1, tipo='VENACN')], colab=juntar(OKV, custodia('2', '5000')))
c(21, 'venta-formato-no-encontrado', 'no currency format for EUR: Exception "formato no encontrado ..."', F + ':339', ordenes=[orden(1, tipo='VENACN', moneda='EUR')], colab=OKV, pin_mensaje=True)
c(22, 'venta-sin-custodia-descarta-una-compra-previa', 'orders go by IdOrden DESC: the COMPRA (id 2) is dropped when the VENTA (id 1) has no custody', F + ':334-348',
  ordenes=[orden(1, tipo='VENACN'), orden(2)], colab=juntar(OKV, flujo((2001, 'CLP', '1000000')), custodia()))
c(23, 'accion-en-minuscula-pasa-el-primer-filtro-y-no-el-segundo', 'AccionOrden "compra": the first test ignores case, the later ones do not: no amount is set', F + ':349-515',
  extra=[ins(T_TIPOOP, IdTipoOperacion='COMMIN', AccionOrden='compra')], ordenes=[orden(1, tipo='COMMIN')])
c(24, 'accion-desconocida-no-fija-monto', 'AccionOrden OTRA: neither COMPRA nor VENTA, the order goes with the defaults', F + ':425-515',
  extra=[ins(T_TIPOOP, IdTipoOperacion='OTRO', AccionOrden='OTRA')], ordenes=[orden(1, tipo='OTRO')])
c(25, 'accion-nula-lanza-npe', 'AccionOrden NULL: NullPointerException', F + ':327', extra=[ins(T_TIPOOP, IdTipoOperacion='NULO', AccionOrden=None)], ordenes=[orden(1, tipo='NULO')])
