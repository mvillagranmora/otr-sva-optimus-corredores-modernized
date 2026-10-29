"""Scenario builder shared by the ProcesarDistribucion / CargaMasivaPAC / devolucion cases: one corredora, one deposit with one client detail,
one contract with its distribution, the bank accounts of the traspaso, and the scripts of the collaborators outside APV (all synthetic)."""
from fix import (T_CLI, T_CONTRATO, T_DISTR, T_INS, T_ODT, T_ASIGN, T_V_CLIENTE, T_V_CRD, T_V_FONDO, T_V_GESTION, T_CFGAPV, T_PORCON, cuenta_banco, empresa, ins, msg, rcli, rec,
                 remp, res_proceso, salida_ing_egreso)

T_V_DIS = 'BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion'
T_V_CTAS = 'BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias'
T_V_PCB = 'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias'
T_SERIE = 'BSCL.FondoMutuo.BD::FMM.SerieDeFondo'
CRE = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'


def crd(idrc=11, folio=10, idre=0, idcli=701, idpersona=501, monto=100000, estado='CON', regimen='APV-A', tipo='APV', rut_emp='', banco=17, cuenta='111222', rutc='11111111-1'):
    """A row of the HANA view REC.ConsultarRecaudadoDistribuido (read by ConsultarDetalleRecaudacionADistribuir and by ValidacionSAAPV_validaRecaudacion)."""
    return ins(T_V_CRD, FolioRecaudacion=folio, IdRecaudacionCliente=idrc, IdRecaudacionEmpleador=idre, IdCliente=idcli, IdPersonaCliente=idpersona, RutEmpleador=rut_emp,
               FechaDeposito='2025-03-15', RecTotal=monto, RecMonto=monto, MontoRecaudacion=monto, IdBanco=banco, IdTipoDeposito='TRA', NombreTipoDeposito='TRANSFERENCIA',
               CuentaCorriente=cuenta, CodigoEstado=estado, NombreEstado=estado, Regimen=regimen, TipoRegimen=tipo, RutCliente=rutc)


def gest(idc=9001, idpo=901, medio=1, rut_emp='', pago='IND', termino='2025-12', monto=100000, origen='APV', regimen='APV-A', est_po='ACT', est_con='VIG', forma='DIRECTA', idpersona=501):
    return ins(T_V_GESTION, IdPortafolio=idpo, IdContratoSAAPV=idc, IdMedioRecaudacionFormaPago=medio, RutEmpleador=rut_emp, Pago=pago, PeriodoTermino=termino, MontoPesos=monto,
               OrigenAhorro=origen, FormaDePago=forma, RegimenTributario=regimen, IdEstadoPortafolio=est_po, IdEstadoNuevo=est_con, IdPersona=idpersona, IdEstado='VIG', RutCliente='11111111-1')


def asig(idrc=11, idc=9001):
    return ins(T_ASIGN, IdRecaudacionCliente=idrc, IdContratoSAAPV=idc, FechaCreacion='2025-03-16 10:00:00', FechaModificacion='2025-03-16 10:00:00')


def cliente_sn(idcliente=701, idpersona=501, estado='ACT'):
    """CLI.Cliente (physical, persona of the client) and the CLI.ConsultarCliente view row (state of the client)."""
    return [ins(T_CLI, IdPersona=idpersona, IdCliente=idcliente), ins(T_V_CLIENTE, IdCliente=idcliente, IdPersona=idpersona, IdEstado=estado, DocumentoIdentidad='11111111-1')]


def transiciones(rez=True, dis=True, dbf=False, dev=False):
    """Bank accounts of the corredora and of the socio, and the APV origin/destination rules of the traspaso (REZ / DIS / DBF / DEV): origin 136 -> destination 137."""
    out = [ins(T_V_CTAS, IdCuentaBanco=136, IdBanco=17, IdTipoCuentaBancaria='CC', NombreTipoCuentaBancaria='Cuenta Corriente', NumeroCuentaBancaria='111222', IdMoneda='CLP', CodigoBanco='017'),
           ins(T_V_CTAS, IdCuentaBanco=137, IdBanco=18, IdTipoCuentaBancaria='CV', NombreTipoCuentaBancaria='Cuenta Vista', NumeroCuentaBancaria='333444', IdMoneda='CLP', CodigoBanco='018'),
           ins(T_V_PCB, IdCuentaBancaria=100, IdBanco=19, IdTipoCuentaBancaria='CUCO', NumeroCuentaBancaria='555000111')]
    i = 1
    for op, activo in (('REZ', rez), ('DIS', dis), ('DBF', dbf), ('DEV', dev)):
        if activo:
            out.append(ins(T_ODT, IdOrigenDestinoTransicion=i, IdEmpresa=1, Operacion=op, OrigenCtaBco=136, SocioNegocioCtaBco=100, DestinoCtaBco=137, MueveCuentaCorriente='S',
                           MueveCuentaInversion='S', IdEstado='ACT'))
            i += 1
    return out


def contrato_y_distribucion(idc=9001, porcentaje=100, tipo='FFM', moneda='CLP', canal='OPT', restringe='N', instrumento=421060, nemo='FFMM-A', pago='IND'):
    """Contract row, its distribution line, the instrument and (for funds) the fund view row."""
    out = [ins(T_CONTRATO, IdContratoSAAPV=idc, IdEmpresa=1, FolioContratoSAAPV='F%d' % idc, IdEstado='VIG', Canal=canal),
           ins(T_PORCON, IdPortafolioContratoSAAPV=8000 + idc, IdPortafolio=901, IdContratoSAAPV=idc, IdEstado='VIG', OrigenAhorro='APV', MontoPesos=100000, MontoUF=0,
               PorcentajeRegimenImponible=0, Pago=pago, PeriodoInicio='2025-03', PeriodoTermino='9999-01'),
           ins(T_DISTR, IdDistribucionInversion=1, IdContratoSAAPV=idc, IdInstrumento=instrumento, Nemotecnico=nemo, MontoPesos=0, Porcentaje=porcentaje, IdEstado='VIG'),
           ins(T_INS, IdInstrumento=instrumento, IdTipoInstrumento=tipo, IdMoneda=moneda, Nemotecnico=nemo),
           ins(T_V_DIS, IdContratoSAAPV=idc, Nemotecnico=nemo)]
    if tipo == 'FFM':
        out.append(ins(T_V_FONDO, IdInstrumento=instrumento, IdSerieFondo=nemo, IdMoneda=moneda, RestringeAporte=restringe))
        out.append(ins(T_SERIE, IdSerieFondo=nemo, RestringeAporte=restringe))
    return out


def escenario_ok(monto=100000, estado='CON', regimen='APV-A', pago='IND', dist=None, empresa_=True, trans=True, con_asig=True, extra=()):
    """One deposit (id 10) with one client detail (11, client 701) in state `estado`, assigned to contract 9001 which passes every validation."""
    return [empresa() if empresa_ else None, rec(10, estado, monto=monto, banco_=17, cuenta='111222'), rcli(11, 10, None, estado, apv=monto, idcliente=701),
            cliente_sn(), crd(monto=monto, estado=estado, regimen=regimen), asig() if con_asig else None, gest(monto=monto, pago=pago, regimen=regimen),
            dist if dist is not None else contrato_y_distribucion(pago=pago), transiciones() if trans else None, list(extra)]


# ------------------------------------------------------------------------------------------------ scripts of the collaborators outside APV
def sc_calcular_cuotas(*resultados):
    return {'CalcularMontoNumeroCuota': {'salida': {'P_OUT_RESULTADO': list(resultados)}}}


def cuota(idinterno=1, monto=100000, cuotas=98.5):
    return {'IdInterno': idinterno, 'Monto': monto, 'NumeroCuotas': cuotas}


def sc_solicitud(resultados=(), mensajes=(), errores=0):
    return {'CrearSolicitudAporteRescateNormal': {'salida': {'P_OUT_ERRORESCONTADOR': errores, 'P_OUT_MENSAJES': list(mensajes), 'P_OUT_RESULTADO': list(resultados)}}}


def sol_resultado(idinterno=1, idsolicitud=3001, folio=5000):
    return {'IdInterno': idinterno, 'IdSolicitudAporteRescate': idsolicitud, 'FolioContrato': folio}


def sc_cargo_abono(mensajes=()):
    return {'CrearCargoAbono': {'salida': {'P_OUT_MENSAJES': list(mensajes), 'P_OUT_CARGOABONOGENERADO': []}}}


def sc_traspaso_ok(n=2):
    """Tesoreria answers for TraspasoCuentasBancarias (n movements: egreso + ingreso): movements generated and comprobantes created."""
    gen = [{'NumeroReferencia': i + 1, 'IdIngresoEgreso': 7001 + i, 'IdTipoMovimiento': 'MOVEGR' if i % 2 == 0 else 'MOVING'} for i in range(n)]
    res = [res_proceso('IdComprobante', 8001 + i, CRE, 'ING', i + 1) for i in range(n)]
    return {'CrearIngresoEgreso': {'salida': salida_ing_egreso(generados=gen)}, 'CrearComprobante': {'salida': {'P_OUT_MENSAJES': [], 'P_OUT_RESULTADOPROCESO': res}}}


def sc_precio(*instrumentos):
    return {'ConsultarInstrumentoPrecioBolsa': {'salida': {'P_OUT_MENSAJES': [], 'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_INSTRUMENTO': list(instrumentos)}}}


def sc_ordenes(calculadas=(), mensajes=(), errores=0):
    return {'CalcularMontoYCobrosOrden': {'salida': {'p_out_Ordenes': list(calculadas)}},
            'CrearOrdenes': {'salida': {'P_OUT_MENSAJES': list(mensajes), 'P_OUT_ERRORESCONTADOR': errores, 'P_OUT_RESULTADO': []}}}
