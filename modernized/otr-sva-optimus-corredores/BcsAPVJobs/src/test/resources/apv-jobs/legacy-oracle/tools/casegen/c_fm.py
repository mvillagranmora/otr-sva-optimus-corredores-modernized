"""IngresarOrdenFMBonificacionFiscal cases, part 1 (APVJ-FMB-001..): the amounts. Legacy: .../IngresarOrdenFMBonificacionFiscal.java"""
from lib import caso, seed, programa, parametros
from fmlib import *

JOB = 'IngresarOrdenFMBonificacionFiscal'
F = 'IngresarOrdenFMBonificacionFiscal.java'
CR = 'CrearSolicitudAporteRescateNormal'
OK = juntar(empresa(), crear(('INFORMACION', 'ok'), resultado=[9001]))


def c(n, slug, titulo, pina, sol=(1,), colab=None, rule=None, seed_sql=None, **kw):
    s = seed_sql if seed_sql is not None else seed(base(solicitudes=sol, **{k: v for k, v in kw.items() if k in ('calculo', 'monto', 'cuotas', 'mov', 'tipo', 'serie')}))
    resto = {k: v for k, v in kw.items() if k not in ('calculo', 'monto', 'cuotas', 'mov', 'tipo', 'serie')}
    return caso('FMB', n, JOB, slug, titulo, pina, seed_sql=s, colaboradores=colab if colab is not None else OK, tablas=TABLAS_FM, rule=rule, **resto)


c(1, 'por-monto-aplica-el-porcentaje', 'CalculoPorMontoCuota M: Monto*15/100, NumeroCuotas 0, EsPorMontoLiquido always N, flujo called, request created, two commits',
  F + ':223-226')
c(2, 'por-monto-sin-redondeo-conserva-decimales', 'Monto 33.33333333 * 15 / 100 is NOT rounded (divide without RoundingMode)', F + ':223-226', monto='33.33333333')
c(3, 'por-cuota-aplica-el-porcentaje-a-las-cuotas', 'CalculoPorMontoCuota C: Monto 0, NumeroCuotas*15/100', F + ':227-230', calculo='C', monto='500', cuotas='10')
c(4, 'por-total-usa-la-rama-else-con-ambos', 'CalculoPorMontoCuota T: both Monto and NumeroCuotas are scaled', F + ':231-234', calculo='T', monto='1000', cuotas='10')
c(5, 'por-porcentaje-tambien-rama-else', 'CalculoPorMontoCuota P: the else branch too', F + ':231-234', calculo='P', monto='200', cuotas='4')
c(6, 'rama-else-con-cuotas-nulas-lanza-npe', 'else branch with NumeroCuotas null: NullPointerException, nothing created', F + ':231-234',
  calculo='T', monto='1000', cuotas=None)
c(7, 'rama-monto-con-monto-nulo-lanza-npe', 'M branch with Monto null: NullPointerException', F + ':224', monto=None)
c(8, 'rescate-por-cuota-tope-por-disponible', 'RESNORM C: 100 cuotas * 15% = 15 > available 10: NumeroCuotas = 10, still C', F + ':236-246',
  calculo='C', monto='0', cuotas='100', colab=juntar(OK, flujo((2000, 'FONDO-A', '10'))))
c(9, 'rescate-por-cuota-disponible-igual-no-topa', 'available equals the requested cuotas: not capped (compareTo == -1 only)', F + ':241',
  calculo='C', monto='0', cuotas='100', colab=juntar(OK, flujo((2000, 'FONDO-A', '15'))))
c(10, 'rescate-por-cuota-disponible-mayor-no-topa', 'available greater: not capped', F + ':241', calculo='C', monto='0', cuotas='100',
  colab=juntar(OK, flujo((2000, 'FONDO-A', '500'))))
c(11, 'rescate-total-toma-todo-lo-disponible', 'RESNORM T: NumeroCuotas = available, CalculoPorMontoCuota stays T', F + ':244-246', calculo='T', monto='0', cuotas='100',
  colab=juntar(OK, flujo((2000, 'FONDO-A', '42.5'))))
c(12, 'rescate-por-monto-no-se-topa-con-la-custodia', 'RESNORM M: the custody cap compares cuotas only, so a by-amount rescate is never capped (defect)', F + ':236-246',
  calculo='M', monto='9999999', colab=juntar(OK, flujo((2000, 'FONDO-A', '0'))))
c(13, 'dos-lineas-de-flujo-se-aplican-en-secuencia', 'two custody lines of the same portfolio and series: applied one after the other', F + ':236-248',
  calculo='C', monto='0', cuotas='100', colab=juntar(OK, flujo((2000, 'FONDO-A', '10'), (2000, 'FONDO-A', '4'))))
c(14, 'flujo-de-otro-portafolio-se-ignora', 'a custody line of another portfolio does not cap', F + ':238', calculo='C', monto='0', cuotas='100',
  colab=juntar(OK, flujo((2999, 'FONDO-A', '1'))))
c(15, 'flujo-de-otra-serie-se-ignora', 'a custody line whose Nemotecnico differs from the series does not cap', F + ':238', calculo='C', monto='0', cuotas='100',
  colab=juntar(OK, flujo((2000, 'OTRA', '1'))))
c(16, 'rescate-copia-medio-y-moneda-de-pago', 'RESNORM keeps IdMonedaPago and IdMedioDePago of the request', F + ':248-250')
c(17, 'no-rescate-usa-cuenta-de-inversion', 'APONORM: IdMedioDePago = CIN (ConstantesFondoMutuo.CuentaInversion), IdMonedaPago not set', F + ':251-253', mov='APONORM', tipo='APO')
c(18, 'disponible-nulo-lanza-npe', 'a custody line with CantidadDisponible null: NullPointerException', F + ':241', calculo='C', monto='0', cuotas='100',
  colab=juntar(OK, {'ConsultarFlujoCustodiaFondoMutuo': {'P_OUT_FLUJOFONDOMUTUOTOTALIZADO': [{'IdPortafolio': 2000, 'Nemotecnico': 'FONDO-A'}]}}))
c(19, 'tres-solicitudes-id-interno-correlativo', 'three requests: IdInterno 1,2,3 in query order, three carteras', F + ':211-271', sol=(1, 2, 3))
c(20, 'dos-solicitudes-de-la-misma-serie-comparten-instrumento', 'the instrument object is reused: both entries show the last IdInstrumento (aliasing)', F + ':204-208',
  seed_sql=seed(base(solicitudes=(1, 2)), [ins('BSCL.FondoMutuo.BD::FMM.SerieDeFondo', IdSerieFondo='FONDO-A', IdInstrumento=88, IdFondo='F2', IdEmpresa=1),
                                            ins('BSCL.FondoMutuo.BD::FMM.Fondo', IdFondo='F2', IdEmpresa=1, IdPaisOrigen=152, IdPaisEmisor=152, IdMoneda='CLP')]))
c(21, 'folio-y-tipo-contrato-nulos-se-normalizan', 'COALESCE in the query: FolioContrato 0, TipoContratoOperacion and FolioContratoOperacion empty', F + ':167',
  seed_sql=seed(base(solicitudes=(1,), FolioContrato=None, TipoContratoOperacion=None, FolioContratoOperacion=None)))
c(22, 'porcentaje-cero', 'APV.PorcentajeBonificacionFiscal 0: every amount 0', F + ':224', config={'APV/APV.PorcentajeBonificacionFiscal': '0'})
c(23, 'porcentaje-cien', 'APV.PorcentajeBonificacionFiscal 100', F + ':224', config={'APV/APV.PorcentajeBonificacionFiscal': '100'})
c(24, 'porcentaje-no-numerico', 'APV.PorcentajeBonificacionFiscal "abc": NumberFormatException', F + ':164', config={'APV/APV.PorcentajeBonificacionFiscal': 'abc'})
c(25, 'porcentaje-no-configurado', 'APV.PorcentajeBonificacionFiscal missing: Exception "Parametro ... no encontrado"', F + ':164',
  config={'APV/APV.PorcentajeBonificacionFiscal': None}, pin_mensaje=True)
c(26, 'fecha-sistema-no-configurada', 'PLA.FechaSistema missing: Exception', F + ':165', config={'PLATAFORMA/PLA.FechaSistema': None}, pin_mensaje=True)
