"""Seed and script helpers of the RV (renta variable) bonificacion cases."""
from lib import ins, seed, programa, parametros, tabla, T_PROGRAMA, T_LOG
from fmlib import portafolio, regimen, juntar

T_ORDEN = 'BSCL.Orden.BD::ORD.Orden'
T_TIPOOP = 'BSCL.Plataforma.BD::INS.TipoOperacion'
T_FORMATO = 'BSCL.Plataforma.BD::PLA.MonedaFormato'
SP_PRECIO = 'BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa'


def orden(ido, cliente=100, tipo='COMACN', cantidad='100', monto='100000', precio='1000', instrumento=31, moneda='CLP', **kw):
    f = dict(IdOrden=ido, IdCliente=cliente, FechaRecepcion='2025-03-31 10:00:00', FechaVigencia='2025-04-30', FechaEmision='2025-03-31',
             FechaOperacion='2025-03-31 10:00:00', IdCondicionDeLiquidacion='T2', IdTipoOperacion=tipo, IdTipoVigenciaOrden='SH', Glosa='orden %d' % ido,
             Cantidad=cantidad, MontoOrden=monto, ValorRescate='0', IdBoveda='DCV', IdFormaCustodia='CUS', PrecioOrden=precio, IdTipoPrecio='M',
             IdTipoCanalIngreso='AGE', IdTipoReajuste='', IdEmisor='AGUAS', IdBolsa='BCS', IdPorcentajeVisibilidad=1, EspecificaBolsa='S',
             IdMonedaLiquidacionOrden=moneda, TipoCambio='1', IdInstrumento=instrumento, IdInstrumentoSubyacente=0, TasaVentaCorta='0',
             PlazoVentaCorta=0, EsVentaCorta='N', TasaSimultanea='0', PlazoSimultanea=0, PlazoIF=0, TasaRFIF='0', CodigoClaseOrden='',
             EsFungible='N', IdCuentaCustodioPortafolio=None, EsPorMonto='S', IdCorredorCorresponsal=None, IdPersonaAutorizadoDarOrdenes=None,
             IdTipoCanalDistribucion='MAN', EsOrdenEspecial='N', IdPortafolioCliente=900, BaseTasa=0, TipoInteres='', IdTipoInstrumento='ACN')
    f.update(kw)
    return ins(T_ORDEN, **f)


def tipos_operacion():
    return [ins(T_TIPOOP, IdTipoOperacion='COMACN', AccionOrden='COMPRA'), ins(T_TIPOOP, IdTipoOperacion='VENACN', AccionOrden='VENTA')]


def base(idp=777, ordenes=None, extra_params=(), bf=True, portafolios=None):
    """Orders (default one COMACN) of BF clients 100.. with their portfolios 2000.., and the job parameters (IdOrden per order)."""
    ordenes = ordenes if ordenes is not None else [orden(1)]
    ids = [int(o.split('VALUES (')[1].split(',')[0]) for o in ordenes]
    pares = [('IdEmpresa', '1'), ('IdUsuario', '21')] + [('IdOrden', str(i)) for i in ids] + list(extra_params)
    pf = portafolios if portafolios is not None else [portafolio(100 + k, 2000 + k) for k in range(len(ordenes))]
    return [programa(idp), parametros(idp, *pares), regimen('BF' if bf else 'OT'), tipos_operacion(), ordenes, pf]


def precios(*pares):
    return {'SalidaProcedimiento': {SP_PRECIO: {'p_out_Instrumento': [{'IdInstrumento': i, 'PrecioCierre': p} for i, p in pares]}}}


def flujo(*lineas):
    return {'ConsultarFlujoCaja': {'P_OUT_FLUJOCAJA': [{'FechaSaldo': '2025-04-01', 'IdPortafolio': p, 'IdMoneda': m, 'Saldo': s} for p, m, s in lineas]}}


def custodia(*totales):
    return {'ConsultarCustodiaPortafolio': {'P_OUT_TBL': [{'CantidadTotal': t} for t in totales]}}


def calculo(*filas):
    return {'CalcularMontoYCobrosOrden': {'p_out_Ordenes': [{'IdInterno': i, 'CantidadOperacion': c} for i, c in filas]}}


def creadas(*msgs):
    return {'CrearOrdenes': {'P_OUT_MENSAJES': [{'id': i + 1, 'tipo': t, 'codigo': 'C', 'descripcion': d} for i, (t, d) in enumerate(msgs)]}}


def formato(ido, moneda, tipoinst, tipoop, dec, tr):
    return ins(T_FORMATO, IdMonedaFormato=ido, IdMoneda=moneda, IdUsoMonedaFormato='GEN', IdTipoInstrumento=tipoinst, IdTipoOperacion=tipoop,
               CantidadDecimales=dec, CantidadTrucaRedondea=tr, MontoDecimales=0, MontoTrucaRedondea='T')


TABLAS_RV = [tabla(T_PROGRAMA, '"IdPrograma"', ['IdPrograma', 'RutaSalida']),
             tabla(T_LOG, '"IdPrograma", "Mensaje"', ['IdTipoPrograma', 'IdPrograma', 'Mensaje'])]
