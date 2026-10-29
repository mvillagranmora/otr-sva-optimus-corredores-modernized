"""Shared fixtures of the family C cases: table names, seed rows of the corredora, collaborator scripts (all values synthetic)."""
from lib import ins

# ------------------------------------------------------------------------------------------------ catalog names
T_EMPRESA = 'BSCL.Plataforma.BD::PLA.Empresa'
T_SOC = 'BSCL.SocioNegocio.BD::SOC.SocioDeNegocio'
T_CLI = 'BSCL.SocioNegocio.BD::CLI.Cliente'
T_V_EMPRESA = 'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa'
T_V_CLIENTE = 'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente'
T_CAJA = 'BSCL.Tesoreria.BD::TES.Caja'
T_CTABCO = 'BSCL.Tesoreria.BD::TES.CuentaBanco'
T_BANCO = 'BSCL.Plataforma.BD::BAN.Banco'
T_REC = 'BSCL.APV.BD::REC.Recaudacion'
T_RCLI = 'BSCL.APV.BD::REC.Cliente'
T_REMP = 'BSCL.APV.BD::REC.Empleador'
T_TRASPASO = 'BSCL.APV.BD::TRA.Traspaso'
T_DOC = 'BSCL.SocioNegocio.BD::PER.DocumentoIdentidad'
T_CARGA = 'BSCL.APV.BD::APV.CargaMasiva'
T_CARGADET = 'BSCL.APV.BD::APV.CargaMasivaDetalleRecaudacion'
T_ESTTRANS = 'BSCL.APV.BD::APV.EstadoTransicion'
T_CONTRATO = 'BSCL.APV.BD::SCS.ContratoSAAPV'
T_DISTR = 'BSCL.APV.BD::SCS.DistribucionInversion'
T_PORCON = 'BSCL.APV.BD::SCS.PortafolioContratoSAAPV'
T_ODT = 'BSCL.APV.BD::APV.OrigenDestinoTransicion'
T_BONIF = 'BSCL.APV.BD::BON.Bonificacion'
T_ANOT = 'BSCL.APV.BD::REC.Anotacion'
T_V_CRD = 'BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido'
T_V_REC = 'BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion'
T_V_PJ = 'BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica'
T_V_CTAS = 'BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias'
T_V_GESTION = 'BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV'
T_V_FONDO = 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo'
T_INS = 'BSCL.Plataforma.BD::INS.Instrumento'
T_CFGAPV = 'BSCL.APV.BD::PLA.ConfiguracionAPV'
T_SOLAPO = 'BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate'
T_INVBON = 'BSCL.APV.BD::INV.Bonificacion'


# ------------------------------------------------------------------------------------------------ the corredora (empresa 1)
def empresa(idpersona=900, portafolio=777, idsocio=50, idcliente=9000):
    """PLA.Empresa -> SOC.SocioDeNegocio -> CLI.Cliente (IdPortafolioDefecto), the physical tables read by ProcesarDistribucion/CrearRecaudacionDeposito, and
    the two views read by the devolucion/bonificacion classes (modelled as tables with the same rows)."""
    return [ins(T_EMPRESA, IdEmpresa=1, IdSocioDeNegocio=idsocio),
            ins(T_SOC, IdSocioDeNegocio=idsocio, IdPersona=idpersona),
            ins(T_CLI, IdPersona=idpersona, IdPortafolioDefecto=portafolio, IdCliente=idcliente, IdSocioDeNegocio=idsocio),
            ins(T_V_EMPRESA, IdEmpresa=1, IdPersona=idpersona, IdSocioDeNegocio=idsocio),
            ins(T_V_CLIENTE, IdSocioDeNegocio=idsocio, IdPortafolioDefecto=portafolio, IdCliente=idcliente, DocumentoIdentidad='76000000-0')]


def caja(estado='ABT', codigo='PAC', idcaja=5):
    return ins(T_CAJA, IdCaja=idcaja, Codigo=codigo, Nombre='CAJA PAC', IdEstado=estado, IdEmpresa=1)


def banco(idbanco=12, idpersona=3012):
    return ins(T_BANCO, IdBanco=idbanco, IdPersona=idpersona)


def cuenta_banco(idcuenta=122, idbanco=12, numero='13602227'):
    return ins(T_CTABCO, IdCuentaBanco=idcuenta, IdBanco=idbanco, NumeroCuentaBancaria=numero)


# ------------------------------------------------------------------------------------------------ scripted collaborators outside APV
def msg(i, tipo, codigo, descripcion):
    return {'id': i, 'tipo': tipo, 'codigo': codigo, 'descripcion': descripcion}


def salida_ing_egreso(mensajes=(), generados=(), resultados=()):
    """Complete answer of the Tesoreria CrearIngresoEgreso (every non-null field of the legacy class listed)."""
    return {'P_OUT_MENSAJES': list(mensajes), 'P_OUT_INGRESOEGRESOGENERADO': list(generados), 'P_OUT_RESULTADOPROCESO': list(resultados)}


def salida_liquidar(mensajes=(), resultados=()):
    return {'P_OUT_MENSAJES': list(mensajes), 'P_OUT_RESULTADOPROCESO': list(resultados)}


def salida_comprobante(mensajes=(), resultados=()):
    return {'P_OUT_MENSAJES': list(mensajes), 'P_OUT_RESULTADOPROCESO': list(resultados)}


def res_proceso(campo, id_, servicio, resultado, numero_ref=0):
    return {'Campo': campo, 'Id': id_, 'Servicio': servicio, 'Resultado': resultado, 'NumeroReferencia': numero_ref}


def cfg(**kw):
    """config overrides: cfg(**{'APV/REC.LiquidacionAutomatica': 'S'})"""
    return dict(kw)


# ------------------------------------------------------------------------------------------------ recaudacion rows (REC.Recaudacion / Empleador / Cliente)
def rec(idrec, estado='ING', monto=1900, folio=1, linea=1, idempresa=1, banco_=12, carga=0, origen='CARGA', fecha='2025-03-31', cuenta='13602227', tipo='TRA',
        codigo_archivo='A1'):
    return ins(T_REC, IdRecaudacion=idrec, IdEmpresa=idempresa, CodigoArchivo=codigo_archivo, FolioCartola=folio, LineaCartola=linea, Sucursal='SUCUR',
               FechaDeposito=fecha, CuentaCorriente=cuenta, MontoDeposito=monto, ConceptoRecaudacion='', Origen=origen, Observacion='', NumeroDocumento=1,
               IdTipoDeposito=tipo, IdBanco=banco_, IdRegimenTributario=0, IdCarga=carga, IdEstado=estado)


def remp(idemp, idrec, estado='ING', convenido=0, apv=1900, persona=4400, periodo='2025-03', idempresa=1):
    return ins(T_REMP, IdRecaudacionEmpleador=idemp, IdRecaudacion=idrec, CodigoArchivo='A1', IdEmpresa=idempresa, IdPersona=persona, FechaIngreso='2025-03-31',
               PeriodoPago=periodo, NumeroHojas=1, TipoAfiliado='DEP', TipoPago='Normal', Insistencias='', MontoDepositoConvenido=convenido,
               MontoDepositoAPV=apv, Reajustes=0, Intereses=0, Origen='CARGA', Observaciones='', IdEstado=estado)


def rcli(idcli, idrec, idemp=None, estado='ING', convenido=0, apv=1900, idcliente=7001, regimen='APV', afiliado='DEP', idempresa=1, contrato=None):
    return ins(T_RCLI, IdRecaudacionCliente=idcli, IdRecaudacion=idrec, IdRecaudacionEmpleador=idemp, IdEmpresa=idempresa, IdCliente=idcliente,
               FechaIngreso='2025-03-31', TipoAfiliado=afiliado, IdRegimenTriburario=regimen, MontoDepositoConvenido=convenido, MontoDepositoAPV=apv,
               Origen='CARGA', IdEstado=estado, IdContratoDistribucion=contrato)


# ------------------------------------------------------------------------------------------------ contracts / portfolios (copied in shape from the family A fixtures; fake data)
T_PERSONA = 'BSCL.SocioNegocio.BD::PER.Persona'
T_CLIENTE_SN = 'BSCL.SocioNegocio.BD::CLI.Cliente'
T_PORTAFOLIO = 'BSCL.Portafolio.BD::POR.Portafolio'
T_ASIGN = 'BSCL.APV.BD::REC.AsignacionContratoRecaudacion'


def contrato(idc, folio, **kw):
    d = dict(IdContratoSAAPV=idc, IdEmpresa=1, FolioContratoSAAPV=folio, IdPlan=1, NombrePlan='ABIERTO', FechaSuscripcion='2025-03-01', FechaCreacion='2025-03-01',
             IdPersona=501, IdEmpleador=0, IdCliente=701, IdMedioRecaudacionFormaPago=1, IdEstadoSolicitud='PDA', IdEstado='VIG', Canal='OPT', IdPortafolio=None)
    d.update(kw)
    return ins(T_CONTRATO, **d)


def persona(idp, doc, nombres='ANA', paterno='PEREZ', materno='SOTO', idc=None):
    out = [ins(T_PERSONA, IdPersona=idp, Nombres=nombres, ApellidoPaterno=paterno, ApellidoMaterno=materno, RazonSocial='', Tipo='N', IdDocumentoIdentidad=idp + 5000),
           ins(T_DOC, DocumentoIdentidad=doc, IdDocumentoIdentidad=idp + 5000, IdPersona=idp, Predeterminado='S')]
    if idc is not None:
        out.append(ins(T_CLIENTE_SN, IdCategoriaInversionista='MODERADO', IdPersona=idp, IdCliente=idc, IdEstado='ACT', Borrado='N'))
    return out


def portafolio(idpo, idcliente, matriz=1, estado='ACT', proposito='PROPOSITOAPV'):
    return ins(T_PORTAFOLIO, CodigoPortafolio='APV-A', IdMatrizAgrupadorAPV=matriz, IdPortafolio=idpo, IdEstado=estado, IdCliente=idcliente, Proposito=proposito)


def porcon(idpc, idpo, idc, origen='APV', estado='VIG', monto=100000, pago='IND', ini='2025-03', fin='9999-01'):
    return ins(T_PORCON, IdPortafolioContratoSAAPV=idpc, IdPortafolio=idpo, IdContratoSAAPV=idc, IdEstado=estado, OrigenAhorro=origen, MontoPesos=monto, MontoUF=0,
               PorcentajeRegimenImponible=0, Pago=pago, PeriodoInicio=ini, PeriodoTermino=fin)


def vigente(idc=9001, idcli=701, medio=1, idpo=901, origen='APV', estado='VIG'):
    return [contrato(idc, 'F-B%d' % idc, IdEstado=estado, IdCliente=idcli, IdMedioRecaudacionFormaPago=medio), porcon(8000 + idc, idpo, idc, origen=origen)]


def refdata_apv():
    """Catalogue rows the contract search/validation needs (plans, products, regimes, matrix, medios de recaudacion, contract states): copied in shape from family A."""
    return [
        'INSERT INTO "BSCL.APV.BD::SCS.PlanTipoInversion" ("IdPlanTipoInversion", "NombrePlanTipoInversion", "IdEstado") VALUES (1, \'ABIERTO\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD::SCS.PlanTipoInversion" ("IdPlanTipoInversion", "NombrePlanTipoInversion", "IdEstado") VALUES (2, \'ACTIVO\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador", "NombreProductoAgrupador", "IdEstado") VALUES (1, \'APV FONDOS MUTUOS\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador", "NombreProductoAgrupador", "IdEstado") VALUES (2, \'APV ACCIONES\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (1, \'APV-A\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (2, \'APV-B\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (3, \'DC-R\', \'VIG\');',
        'INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (1, \'PROPOSITOAPV\', 1, 1, 1, \'GRUPO 1\', \'VIG\');',
        'INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (2, \'PROPOSITOAPV\', 1, 1, 2, \'GRUPO 1\', \'VIG\');',
        'INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (3, \'PROPOSITOAPV\', 1, 1, 3, \'GRUPO 1\', \'VIG\');',
        'INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (7, \'PROPOSITOAPV\', 2, 1, 1, \'GRUPO 1\', \'VIG\');',
        'INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("Codigo", "IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "NombreAgrupador", "CodigoAgrupador", "NombrePlan") VALUES (\'APV-A\', 1, \'PROPOSITOAPV\', \'APV FONDOS MUTUOS\', \'GRUPO 1\', \'ABIERTO\');',
        'INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("Codigo", "IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "NombreAgrupador", "CodigoAgrupador", "NombrePlan") VALUES (\'APV-B\', 2, \'PROPOSITOAPV\', \'APV FONDOS MUTUOS\', \'GRUPO 1\', \'ABIERTO\');',
        'INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("Codigo", "IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "NombreAgrupador", "CodigoAgrupador", "NombrePlan") VALUES (\'DC-R\', 3, \'PROPOSITOAPV\', \'APV FONDOS MUTUOS\', \'GRUPO 1\', \'ABIERTO\');',
        'INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion", "CodigoMedioRecaudacion", "TipoMedioRecaudacion", "NombreMedioRecaudacion") VALUES (1, \'DIREMP\', \'DIRECTA\', \'DEPOSITO DIRECTO\');',
        'INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion", "CodigoMedioRecaudacion", "TipoMedioRecaudacion", "NombreMedioRecaudacion") VALUES (2, \'ATREMP\', \'INDIRECTA\', \'DEPOSITO POR EMPLEADOR\');',
        'INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion", "CodigoMedioRecaudacion", "TipoMedioRecaudacion", "NombreMedioRecaudacion") VALUES (3, \'PAC\', \'DIRECTA\', \'PAC\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'VIG\', \'VIGENTE\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'PDI\', \'PENDIENTE DE INGRESO\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'REV\', \'REVOCADO\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'CER\', \'CERRADO\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'ANU\', \'ANULADO\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'NEV\', \'NO ENVIADO\');',
        'INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES (\'ENV\', \'ENVIADO\');',
    ]
