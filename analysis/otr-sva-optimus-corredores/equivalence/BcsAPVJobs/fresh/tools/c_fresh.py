"""FRESH inputs (verification step 4.2) for BcsAPVJobs: ids 101.. per job, none equal to a development case. Inputs only; expected is recorded by the legacy oracle."""
import sys
sys.path.insert(0, sys.argv[2])
import lib
from lib import caso, seed, programa, parametros, ins
from fmlib import *
import fmlib
import rvlib
import c_carga as CG  # reuse entrada/par/msg/sal helpers of the dev generator (inputs only)

lib.CASOS.clear()
F = 'fresh'


def cmp_(n, slug, titulo, **kw):
    kw.setdefault('carpetas', ['APV'])
    kw.setdefault('seed_sql', seed(programa(777)))
    return caso('CMP', n, 'BcsJobCargaMasivaPrevired', slug, titulo, F, **kw)


def fila_prv(i, monto, msgv=None):
    return {'fila1': i, 'Folio': 900 + i, 'Tipo_Pag': 1, 'FechaOperacion': 20241231, 'HoraOperacion': '23:59', 'INS_APV': 3 + i,
            'Rut_Empleador': '99999999-9', 'Rut_Trabajador': '2222222%d-K' % i, 'Monto_APV': monto, 'Monto_Total': monto,
            'Periodo_Pago': '122024', 'NroRegistro': i, 'MENSAJEVALIDACION': msgv}


MIX = CG.sal(CG.msg(1, 'ERROR', 'E-01', 'Fila 3; monto "negativo"'), CG.msg(2, 'AUTORIZACION', 'A-07', 'requiere visto bueno'),
             CG.msg(3, 'INFORMACION', 'I-00', 'x' * 400), CG.msg(0, 'ADVERTENCIA', 'W-99', 'ultima'), err=1)
cmp_(101, 'mensajes-mixtos-descripcion-larga-usuario-long', 'four message types, a 400 char description, quotes and semicolon, p_IdUsuario above int range',
     parametros=CG.par(CG.entrada(p_IdUsuario=9999999999, p_IdEmpresa=3, p_Origen='MAN', p_NombreCarga='APV_PREVIRED_ZZ9.txt')), colaboradores=MIX)
cmp_(102, 'cinco-filas-previred-montos-extremos', 'five Previred rows with amount 0 and 9999999999999, three file rows, null and text validation messages',
     parametros=CG.par(CG.entrada(p_Parametros=[fila_prv(1, 0), fila_prv(2, 9999999999999, 'Rut invalido'), fila_prv(3, 1), fila_prv(4, 15000, None),
                                                 fila_prv(5, 99999999)],
                                  p_Archivo=[{'IdEmpresa': 1, 'IdCargaMasiva': 0, 'NombreCargaMasiva': 'A.txt', 'CantidadRegistros': 0, 'IdUsuario': 17,
                                              'IdTipoCarga': 1, 'Descripcion': '', 'FechaCarga': '2024-12-31', 'IdEstado': 'ERR'}] * 3)),
     colaboradores=CG.sal(CG.msg(7, 'INFORMACION', 'OK', 'cinco filas')))
cmp_(103, 'ruta-base-con-subcarpeta-y-programa-grande', 'base folder with a sub folder with a space, IdPrograma 123456789, a long previous RutaSalida',
     parametros=CG.par(CG.entrada(p_IdUsuario=1)), colaboradores=CG.sal(*[CG.msg(i, 'ADVERTENCIA', 'W%d' % i, 'Advertencia %d' % i) for i in range(1, 13)], err=12),
     config={'PLATAFORMA/PLA.RutaBaseArchivos': '<BASE>/sub dir'}, carpetas=['sub dir/APV'], idp=123456789,
     seed_sql=seed(programa(123456789, 'APV/APV_PREVIRED_ANTIGUO_20200101000000_99999.txt')))
cmp_(104, 'idusuario-negativo-y-lista-vacia', 'p_IdUsuario -5 in the name, both lists empty, only the FIN lines, error counter 0 but one ERROR message',
     parametros=CG.par(CG.entrada(p_IdUsuario=-5)), colaboradores=CG.sal(CG.msg(1, 'ERROR', 'E', 'Sin filas')))

OB = 'IngresarOrdenBonificacionFiscal'


def obf(n, slug, titulo, idp=777, **kw):
    kw.setdefault('seed_sql', seed(programa(idp)))
    return caso('OBF', n, OB, slug, titulo, F, idp=idp, **kw)


obf(101, 'tres-mensajes-y-dos-resultados', 'CrearOrdenes answers 3 mixed messages and 2 result rows: all ignored, IdPrograma 424242', idp=424242,
    colaboradores={'CrearOrdenes': {'P_OUT_RESULTADO': [{'IdInterno': 1, 'IdOrden': 5}, {'IdInterno': 2, 'IdOrden': 6}], 'P_OUT_ERRORESCONTADOR': 2,
                                    'P_OUT_MENSAJES': [{'id': 1, 'tipo': 'ERROR', 'codigo': 'E', 'descripcion': 'a'}, {'id': 2, 'tipo': 'ADVERTENCIA', 'codigo': 'W', 'descripcion': 'b'},
                                                       {'id': 3, 'tipo': 'INFORMACION', 'codigo': 'I', 'descripcion': 'c'}]}})
obf(102, 'idprograma-int-max-mas-uno-lista-vacia', 'IdPrograma 2147483648 (int max + 1), empty message list', idp=2147483648,
    colaboradores={'CrearOrdenes': {'P_OUT_MENSAJES': [], 'P_OUT_RESULTADO': []}})

JF = 'IngresarOrdenFMBonificacionFiscal'
OKF = juntar(empresa(), crear(('INFORMACION', 'ok'), resultado=[9001]))


def fmb(n, slug, titulo, seed_sql, colab=None, **kw):
    return caso('FMB', n, JF, slug, titulo, F, seed_sql=seed_sql, colaboradores=colab if colab is not None else OKF, tablas=TABLAS_FM, **kw)


def sol_seed(sols, extra=(), series=None):
    """sols: list of dict(kw) for solicitud ids 1..n (clients 100.., portfolios 2000..)."""
    pares = [('IdEmpresa', '1'), ('IdUsuario', '21')] + [('IdSolicitudAporteRescate', str(i + 1)) for i in range(len(sols))]
    filas = [programa(777), parametros(777, *pares), regimen('BF'), series if series else serie(), conf_pais(), list(extra)]
    for k, kw in enumerate(sols):
        filas += [solicitud(k + 1, cliente=100 + k, **kw), portafolio(cliente=100 + k, idp=2000 + k)]
    return seed(filas)


fmb(101, 'porcentaje-uno-por-monto-999-99', 'pct 1: 999.99 * 1 / 100 = 9.9999 exact', sol_seed([dict(monto='999.99')]), config={'APV/APV.PorcentajeBonificacionFiscal': '1'})
fmb(102, 'porcentaje-99-monto-un-centavo', 'pct 99: 0.01 * 99 / 100 = 0.0099 exact', sol_seed([dict(monto='0.01')]), config={'APV/APV.PorcentajeBonificacionFiscal': '99'})
fmb(103, 'porcentaje-100-limite-de-custodia-mas-menos', 'pct 100 RESNORM by quota 12.3456: available 12.3456 (equal, no cap) and 12.3455 (below, capped)',
    sol_seed([dict(calculo='C', monto='0', cuotas='12.3456'), dict(calculo='C', monto='0', cuotas='12.3456')]),
    colab=juntar(OKF, flujo((2000, 'FONDO-A', '12.3456'), (2001, 'FONDO-A', '12.3455'))), config={'APV/APV.PorcentajeBonificacionFiscal': '100'})
fmb(104, 'cinco-solicitudes-tipos-y-calculos-mezclados', 'pct 7, five requests (M, C, T, P, aporte M) with huge and tiny amounts, exact products',
    sol_seed([dict(calculo='M', monto='1234567890123.45'), dict(calculo='C', monto='0', cuotas='0.00000001'), dict(calculo='T', monto='0.1', cuotas='3.3333'),
              dict(calculo='P', monto='77.7', cuotas='1'), dict(calculo='M', monto='5', tipo='APO', mov='APONORM')]),
    config={'APV/APV.PorcentajeBonificacionFiscal': '7'})
fmb(105, 'porcentaje-cero-por-cuota-con-custodia', 'pct 0 by quota: requested 0, available 5 is greater: no cap; a second request is APONORM', sol_seed(
    [dict(calculo='C', monto='0', cuotas='50'), dict(calculo='C', monto='0', cuotas='50', tipo='APO', mov='APONORM')]),
    colab=juntar(OKF, flujo((2000, 'FONDO-A', '5'), (2001, 'FONDO-A', '5'))), config={'APV/APV.PorcentajeBonificacionFiscal': '0'})
fmb(106, 'moneda-usd-medio-efectivo-dos-series', 'USD request paid in cash (EFE) on series FONDO-B (instrument 78); RESNORM T capped at 0.5; messages WARNING, ERROR, INFO order',
    sol_seed([dict(calculo='T', monto='10', cuotas='100', serie='FONDO-B', IdMoneda='USD', IdMonedaPago='USD', IdMedioDePago='EFE')],
             series=[serie(), serie('FONDO-B', 78, 'F9')]),
    colab=juntar(empresa(), flujo((2000, 'FONDO-B', '0.5')), crear(('ADVERTENCIA', 'w1'), ('ERROR', 'e1'), ('INFORMACION', 'i1'), ('ADVERTENCIA', 'w2'), resultado=[1, 2])))
fmb(107, 'dos-lineas-flujo-misma-serie-por-total', 'pct 33 RESNORM T: two custody lines for the same portfolio (7.75 then 0.125), the last assignment wins',
    sol_seed([dict(calculo='T', monto='0', cuotas='1000')]), colab=juntar(OKF, flujo((2000, 'FONDO-A', '7.75'), (2000, 'FONDO-A', '0.125'))),
    config={'APV/APV.PorcentajeBonificacionFiscal': '33'})

JR = 'IngresarOrdenRVBonificacionFiscal'


def rvb(n, slug, titulo, ordenes, colab, extra=(), **kw):
    return caso('RVB', n, JR, slug, titulo, F, seed_sql=seed(rvlib.base(ordenes=ordenes), *extra), colaboradores=colab, tablas=rvlib.TABLAS_RV, **kw)


def P(**k):
    return {'APV/APV.PorcentajeBonificacionFiscal': str(k['pct'])}


rvb(101, 'compra-porcentaje-uno-redondeo-up-escala-8', 'COMPRA pct 1: 0.99999999 * 1 / 100 = 0.0099999999 rounds UP to 0.01000000 (scale 8)',
    [rvlib.orden(1, monto='0.99999999')], juntar(rvlib.precios((31, '1000')), rvlib.flujo((2000, 'CLP', '0')), rvlib.calculo((1, '1')), rvlib.creadas()),
    config=P(pct=1))
rvb(102, 'compra-saldo-justo-bajo-el-monto-y-sobre-el-precio', 'pct 99, amount 99.00000001 UP, saldo 99.00000000 (one unit below) and price 50: the saldo becomes the amount',
    [rvlib.orden(1, monto='100.00000001')], juntar(rvlib.precios((31, '50')), rvlib.flujo((2000, 'CLP', '99.00000000')), rvlib.calculo((1, '2')), rvlib.creadas()),
    config=P(pct=99))
rvb(103, 'venta-pct-100-cantidad-igual-a-custodia', 'VENTA pct 100: 12.345 truncated to 12.34 by the CLP format, custody 12.34 (equal): not replaced',
    [rvlib.orden(1, tipo='VENACN', cantidad='12.345')], juntar(rvlib.precios((31, '10')), rvlib.custodia('12.34'), rvlib.calculo((1, '12.34')), rvlib.creadas()), config=P(pct=100))
rvb(104, 'venta-pct-100-custodia-una-centesima-menor', 'VENTA pct 100: 12.345 -> 12.34, custody 12.33 (one hundredth below): sells 12.33',
    [rvlib.orden(1, tipo='VENACN', cantidad='12.345')], juntar(rvlib.precios((31, '10')), rvlib.custodia('12.33'), rvlib.calculo((1, '12.33')), rvlib.creadas()), config=P(pct=100))
rvb(105, 'venta-usd-half-up-medio-sube', 'VENTA USD pct 50: 1 * 50 / 100 = 0.5 formatted HALF_UP to 0 decimals = 1',
    [rvlib.orden(1, tipo='VENACN', cantidad='1', moneda='USD')], juntar(rvlib.precios((31, '10')), rvlib.custodia('1000'), rvlib.calculo((1, '1')), rvlib.creadas()),
    extra=[rvlib.formato(10, 'USD', 'ACN', 'VENACN', 0, 'R')], config=P(pct=50))
rvb(106, 'venta-usd-half-up-bajo-medio-baja', 'VENTA USD pct 49: 1 * 49 / 100 = 0.49 formatted HALF_UP to 0 decimals = 0, custody 0',
    [rvlib.orden(1, tipo='VENACN', cantidad='1', moneda='USD')], juntar(rvlib.precios((31, '10')), rvlib.custodia('0'), rvlib.calculo((1, '0')), rvlib.creadas()),
    extra=[rvlib.formato(10, 'USD', 'ACN', 'VENACN', 0, 'R')], config=P(pct=49))
rvb(107, 'cuatro-ordenes-mixtas-tres-instrumentos-y-usd', 'COMPRA/VENTA/COMPRA USD/COMPRA over instruments 31, 32, 33 (orders 4,3,2,1 by IdOrden DESC), distinct prices',
    [rvlib.orden(1, cliente=100, instrumento=31), rvlib.orden(2, cliente=101, instrumento=32, moneda='USD', monto='2500.5'),
     rvlib.orden(3, cliente=102, tipo='VENACN', instrumento=33, cantidad='250'), rvlib.orden(4, cliente=103, instrumento=31, monto='1')],
    juntar(rvlib.precios((31, '1234.56'), (32, '0.01'), (33, '99999')), rvlib.flujo((2000, 'CLP', '50000'), (2001, 'USD', '3000'), (2003, 'CLP', '0.5')),
           rvlib.custodia('80'), rvlib.calculo((1, '1'), (2, '2'), (3, '3'), (4, '4')), rvlib.creadas(('ADVERTENCIA', 'x'))), config=P(pct=25))
rvb(108, 'compra-porcentaje-cero', 'COMPRA pct 0: amount 0, saldo 0 is not below 0, the order goes with amount 0',
    [rvlib.orden(1, monto='5000')], juntar(rvlib.precios((31, '1000')), rvlib.flujo((2000, 'CLP', '0')), rvlib.calculo((1, '0')), rvlib.creadas()), config=P(pct=0))
rvb(109, 'ocho-ordenes-ocho-instrumentos', 'eight COMPRA orders over eight instruments (many series), prices and saldos per portfolio, pct 15',
    [rvlib.orden(i, cliente=99 + i, instrumento=30 + i, monto=str(1000 * i + 7)) for i in range(1, 9)],
    juntar(rvlib.precios(*[(30 + i, str(100 + i)) for i in range(1, 9)]), rvlib.flujo(*[(2000 + k, 'CLP', str(10000 * (k + 1))) for k in range(8)]),
           rvlib.calculo(*[(i, str(i)) for i in range(1, 9)]), rvlib.creadas()))

print('fresh cases:', lib.escribir(sys.argv[1]))
