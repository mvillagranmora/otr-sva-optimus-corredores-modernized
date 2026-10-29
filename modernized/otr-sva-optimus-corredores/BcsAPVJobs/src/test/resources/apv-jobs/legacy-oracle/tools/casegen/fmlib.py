"""Seed and script helpers of the FM (fondos mutuos) bonificacion cases."""
from lib import ins, seed, programa, parametros, tabla, T_PROGRAMA, T_LOG

T_SOL = 'BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate'
T_PORT = 'BSCL.Portafolio.BD.Vistas::ConsultarPortafolio'
T_MAT = 'BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV'
T_REG = 'BSCL.APV.BD::REC.RegimenTributario'
T_SERIE = 'BSCL.FondoMutuo.BD::FMM.SerieDeFondo'
T_FONDO = 'BSCL.FondoMutuo.BD::FMM.Fondo'
T_CONF = 'BSCL.Plataforma.BD::PLA.Configuracion'


def solicitud(ids, cliente=100, tipo='RES', serie='FONDO-A', calculo='M', monto='1000', cuotas='0', mov='RESNORM', **kw):
    f = dict(IdSolicitudAporteRescate=ids, IdCliente=cliente, TipoSolicitud=tipo, IdSerieFondo=serie, CalculoPorMontoCuota=calculo, Monto=monto,
             NumeroCuotas=cuotas, IdMoneda='CLP', IdMedioDePago='CTA', IdPreferenciaPagoPortafolio=900, Visado='S', SolicitudBatch='N',
             OrigenOperacion='N', IdRegimenTributario=10, IndicadorMLT='N', IdSerieFondoDestino='', IdEmisorDestino='', IdAutorizadoOrigen=0,
             Porcentaje='0', IdMonedaPago='CLP', IdTipoMovimientoFondoMutuo=mov, FolioContrato=555, TipoContratoOperacion='CON',
             FolioContratoOperacion='F-1', Glosa='glosa ' + str(ids))
    f.update(kw)
    return ins(T_SOL, **f)


def portafolio(cliente=100, idp=2000, aplica='S', estado='ACT', matriz=1):
    return ins(T_PORT, IdCliente=cliente, IdPortafolio=idp, TienePropositoAPV=aplica, IdEstado=estado, IdMatrizAgrupadorAPV=matriz)


def regimen(codigo='BF'):
    return [ins(T_MAT, IdMatrizAgrupadorAPV=1, IdRegimenTributario=10), ins(T_REG, IdRegimenTributario=10, Codigo=codigo, IdEstado='ACT')]


def serie(ids='FONDO-A', instrumento=77, fondo='F1'):
    return [ins(T_SERIE, IdSerieFondo=ids, IdInstrumento=instrumento, IdFondo=fondo, IdEmpresa=1),
            ins(T_FONDO, IdFondo=fondo, IdEmpresa=1, IdPaisOrigen=152, IdPaisEmisor=152, IdMoneda='CLP')]


def conf_pais():
    return [ins(T_CONF, IdEmpresa=1, CodigoParametro='PLA.PaisSistema', Valor='152')]


def base(idp=777, solicitudes=(1,), extra_params=(), bf=True, **kw):
    """One BF client with one solicitud per id (clients 100, 101, ...), the serie FONDO-A and the job parameters."""
    pares = [('IdEmpresa', '1'), ('IdUsuario', '21')] + [('IdSolicitudAporteRescate', str(i)) for i in solicitudes] + list(extra_params)
    filas = [programa(idp), parametros(idp, *pares), regimen('BF' if bf else 'OT'), serie(), conf_pais()]
    for k, i in enumerate(solicitudes):
        filas += [solicitud(i, cliente=100 + k, **kw), portafolio(cliente=100 + k, idp=2000 + k)]
    return filas


def empresa():
    return {'ConsultarEmpresa': {'IdEmpresa': 1, 'IdPais': 44}}


def flujo(*lineas):
    return {'ConsultarFlujoCustodiaFondoMutuo': {'P_OUT_FLUJOFONDOMUTUOTOTALIZADO': [
        {'IdPortafolio': p, 'Nemotecnico': n, 'CantidadDisponible': d} for p, n, d in lineas]}}


def crear(*mensajes, resultado=()):
    return {'CrearSolicitudAporteRescateNormal': {
        'P_OUT_RESULTADO': [{'IdSolicitudAporteRescate': r} for r in resultado],
        'P_OUT_MENSAJES': [{'id': i + 1, 'tipo': t, 'codigo': 'C', 'descripcion': d} for i, (t, d) in enumerate(mensajes)]}}


def juntar(*ds):
    r = {}
    for d in ds:
        r.update(d)
    return r


TABLAS_FM = [tabla(T_PROGRAMA, '"IdPrograma"', ['IdPrograma', 'RutaSalida']),
             tabla(T_LOG, '"IdPrograma", "Mensaje"', ['IdTipoPrograma', 'IdPrograma', 'Mensaje'])]
