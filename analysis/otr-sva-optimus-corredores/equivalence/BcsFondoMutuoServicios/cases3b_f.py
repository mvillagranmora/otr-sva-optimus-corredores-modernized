"""CrearSolicitudRescateComisionNN cases (FM-COM / FM-NOR-46). CalcularFechaSolicitudAporteRescate and CalcularMontoNumeroCuota (step 3A) are scripted."""
from cases3b_lib import caso, dicc, set_param, sql
from cases3b_seeds import regimen

RawTs = "2020-01-01 00:00:00"

S = "CrearSolicitudRescateComisionNN"
SOL_TABLA = ("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate", '"IdSolicitudAporteRescate"')
TIPO = ('INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo", "Descripcion", "IndicadorAporteRescate", '
        '"IdTipoOperacion", "TieneCarga", "TieneIngresoManual", "TieneCertificacion", "IdEstado", "GeneraOmnibus") VALUES '
        "('RESCNN', 'RESCNN', 'RES', 'RESCFM', '', '', '', 'ACT', '');")
DIC = [dicc("NO_SE_INGRESARON_SOLICITUDES_RESCATE", "No se ingresaron solicitudes de rescate"), dicc("VALOR_CUOTA_INCORRECTO", "Valor cuota incorrecto"),
       dicc("MONTO_VALIDO", "Monto invalido"), dicc("FONDO_BLOQUEADO_REQ_APO_RES", "Fondo ::IdInstrumento:: bloqueado ::DocumentoIdentidad::"),
       dicc("FONDO_NO_DIFERIDO_REQ_APO_RES", "Fondo no diferido ::DocumentoIdentidad::")]
BASE = DIC + [TIPO]


def fechas(*items):
    return {"CalcularFechaSolicitudAporteRescate": {"defecto": {"P_FECHASAPORTERESCATE": [
        {"IdInterno": i, "IdSerieFondo": s, "FechaEmision": "2026-03-02", "FechaDigitacion": "2026-03-02", "FechaAceptacion": "2026-03-02",
         "FechaValorCuota": "2026-03-02", "FechaCertificacion": "2026-03-03", "FechaLiquidacion": "2026-03-04"} for (i, s) in items]}}}


def cuotas(*items):
    return {"CalcularMontoNumeroCuota": {"defecto": {"P_OUT_RESULTADO": [
        {"IdInterno": i, "TipoSolicitud": "RES", "Monto": m, "NumeroCuotas": n, "ValorCuota": v, "MontoDestino": m, "ValorParidad": 1,
         "ValorFactorAjuste": 30000, "CostoEnFactorAjuste": 0.5, "IdMonedaSistema": "CLP", "MontoMonedaSistema": m, "Resultado": "OK"}
        for (i, m, n, v) in items]}}}


def merge(*ds):
    out = {}
    for d in ds:
        out.update(d)
    return out


def sol(i, **ov):
    d = {"IdInterno": i, "IdCliente": 100, "IdPortafolio": 500, "IdSerieFondo": "FM001A", "Monto": 0, "NumeroCuotas": 10, "IdMoneda": "CLP",
         "FolioContrato": 0, "IdRegimenTributario": "GEN", "FolioCartera": 900 + i, "IdMonedaPago": "CLP"}
    d.update(ov)
    return d


def ent(*sols, simular="N", canal=None, usuario=7):
    e = {"p_IdEmpresa": 1, "p_IdUsuario": usuario, "p_IdModulo": "FMM", "p_SimularOperacion": simular, "p_UsaReglasNegocio": "S", "p_SolicitudesRescate": list(sols)}
    if canal:
        e["p_IdTipoCanalIngreso"] = canal
    return e


def ok(*ids):
    return merge(fechas(*[(i, "FM001A") for i in ids]), cuotas(*[(i, 15001.23, 10, 1500.123) for i in ids]))


def cases():
    yield caso("COM-NN-01_inserta-y-audita", S, "FM-NOR-46: una comision NN valida: se inserta la solicitud (RESCNN, PEN->ING), AgregarLog y resultado",
               ent(sol(1)), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-02_simulacion-no-inserta", S, "FM-NOR-46: p_SimularOperacion=S: resultado sin insertar ni auditar",
               ent(sol(1), simular="S"), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-03_requisitos-basicos", S, "FM-NOR-01: usuario inexistente => contador 1 y los mensajes de RevisarRequisitosBasicos",
               ent(sol(1), usuario=999), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-04_lista-vacia", S, "FM-NOR-02: sin solicitudes => NO_SE_INGRESARON_SOLICITUDES_RESCATE", ent(), BASE, tablas=[SOL_TABLA])
    yield caso("COM-NN-05_id-interno-cero", S, "FM-NOR-02: IdInterno 0 => ID_INTERNO_NO_CERO", ent(sol(0)), BASE, tablas=[SOL_TABLA])
    yield caso("COM-NN-06_id-interno-repetido", S, "FM-NOR-02: IdInterno repetido => CORRELATIVO_INCORRECTO", ent(sol(1), sol(1)), BASE, tablas=[SOL_TABLA])
    yield caso("COM-NN-07_respeta-valutas", S, "FM-COM: RespetaValutasCargaMasiva=S usa los dias de la serie (no 0) y SolicitudBatch N hacia las fechas",
               ent(sol(1)), BASE + [set_param("RespetaValutasCargaMasiva", "S"),
                                    'UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "DiasRetencionesRescate" = 2, "DiasValutaValorCuotaRescate" = 1, '
                                    '"DiasValutaCertificacionRescate" = 3, "DiasValutaCurseRescate" = 4 WHERE "IdSerieFondo" = \'FM001A\';'],
               colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-08_serie-inexistente", S, "FM-COM: serie inexistente => NullPointerException (IdEmisorOrigen)",
               ent(sol(1, IdSerieFondo="NOEXISTE")), BASE, colaboradores=cuotas((1, 5, 1, 5)), tablas=[SOL_TABLA])
    yield caso("COM-NN-09_sin-fechas-ni-cuotas", S, "FM-COM: los colaboradores no devuelven la solicitud: montos 0 => rechazada (ADVERTENCIA con el folio de cartera), sin resultado",
               ent(sol(1)), BASE, colaboradores=merge(fechas(), cuotas()), tablas=[SOL_TABLA])
    yield caso("COM-NN-10_una-rechazada-una-ok", S, "FM-NOR-29: dos solicitudes, la segunda sin cuota (monto 0): solo la primera se inserta; contador de advertencias",
               ent(sol(1), sol(2)), BASE, colaboradores=merge(fechas((1, "FM001A"), (2, "FM001A")), cuotas((1, 15001.23, 10, 1500.123), (2, 0, 10, 0))),
               tablas=[SOL_TABLA])
    yield caso("COM-NN-11_regimen-apv-tributacion", S, "FM-COM: regimen APV de la serie => Tributacion ModeloRetiroAPV",
               ent(sol(1, IdRegimenTributario="APVX")), BASE + [
                   'INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" ("IdRegimenFondoMutuo", "GrupoRegimen", "PropositoSerieRelacionado", '
                   '"PermiteOtrosGrupos", "ValidaPortafolioCliente", "ValidaPortafolioAGF", "PermiteRescate", "TipoCarteraNormativa", "PermiteCambioGrupo", '
                   '"RescateConMandato", "AceptaMandatoEnReinversion", "PermiteAporte", "PermiteTraspaso", "ValidaMontoMinimoInstrumento", "IdEstado") '
                   'SELECT \'APVX\', \'APV\', "PropositoSerieRelacionado", '
                   '"PermiteOtrosGrupos", "ValidaPortafolioCliente", "ValidaPortafolioAGF", "PermiteRescate", "TipoCarteraNormativa", "PermiteCambioGrupo", '
                   '"RescateConMandato", "AceptaMandatoEnReinversion", "PermiteAporte", "PermiteTraspaso", "ValidaMontoMinimoInstrumento", "IdEstado" '
                   'FROM "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" WHERE "IdRegimenFondoMutuo" = \'GEN\';',
                   sql("BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo", IdSerieFondo="FM001A", IdRegimenFondoMutuo="APVX", IdEmpresa=1, IdEstado="ACT")],
               colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-12_regimen-inexistente", S, "FM-REQ-17: la serie no tiene el regimen pedido => EsRegimenValido N => rechazada",
               ent(sol(1, IdRegimenTributario="ZZZ")), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-13_fecha-valor-cuota-informada", S, "FM-COM: FechaValorCuota de la entrada manda sobre la calculada (fecha de la valorizacion y de la solicitud)",
               ent(sol(1, FechaValorCuota="Mar 5, 2026")), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-14_canal-informado", S, "FM-COM: p_IdTipoCanalIngreso WEB se copia a la solicitud (vacio si no viene)",
               ent(sol(1), canal="WEB"), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-15_auditoria-falla", S, "FM-COM: AgregarLog falla despues del INSERT: la excepcion sale (la transaccion se revierte)",
               ent(sol(1)), BASE, colaboradores=merge(ok(1), {"AgregarLog": {"respuestas": [{"error": "log caido"}]}}), tablas=[SOL_TABLA])
    yield caso("COM-NN-16_cuotas-falla", S, "FM-COM: CalcularMontoNumeroCuota falla: excepcion, sin secuencias consumidas",
               ent(sol(1)), BASE, colaboradores=merge(fechas((1, "FM001A")), {"CalcularMontoNumeroCuota": {"respuestas": [{"error": "cuotas caido"}]}}),
               tablas=[SOL_TABLA])
    yield caso("COM-NN-17_tres-solicitudes-secuencia", S, "FM-COM: tres solicitudes: una secuencia de 3 numeros asignados en orden",
               ent(sol(1), sol(2, IdPortafolio=500), sol(3)), BASE, colaboradores=ok(1, 2, 3), tablas=[SOL_TABLA])
    yield caso("COM-NN-18_fondo-bloqueado-y-no-diferido", S, "FM-REQ-07/10: BloqueoAporte distinto de N y moneda distinta en fondo no diferido => rechazada",
               ent(sol(1, IdMoneda="USD")), BASE + [
                   'UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "BloqueoAporte" = \'S\' WHERE "IdFondo" = \'10\';'],
               colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-19_producto-no-operable", S, "FM-REQ-19: el portafolio no opera INFM => rechazada (ConsultarProductosPortafolio vacio)",
               ent(sol(1)), BASE, colaboradores=merge(ok(1), {"ConsultarProductosPortafolio": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_Mensajes": [],
                                                                                                           "p_out_PortafolioProductos": []}}}),
               tablas=[SOL_TABLA])
    yield caso("COM-NN-20_fechas-repetidas", S, "FM-COM: CalcularFecha devuelve dos filas del mismo IdInterno => Exception key field is not unique",
               ent(sol(1)), BASE, colaboradores=merge(fechas((1, "FM001A"), (1, "FM001A")), cuotas((1, 5, 1, 5))), tablas=[SOL_TABLA])
    yield caso("COM-NN-21_cliente-sin-fila", S, "FM-COM: el portafolio no existe: sin fila de cliente => NullPointerException en los requisitos",
               ent(sol(1, IdPortafolio=999)), BASE, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-22_folio-contrato-y-cartera", S, "FM-COM: FolioContrato positivo y FolioCartera/IdCartera de la entrada pasan a la solicitud",
               ent(sol(1, FolioContrato=555, IdCartera=77, IdSolicitudAporteRescateRelacionado=42, IngresoAutomatico="S")), BASE,
               colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-23_regimen-sin-grupo", S, "FM-COM: el regimen de la serie no esta activo (sin GrupoRegimen): NullPointerException del switch",
               ent(sol(1, IdRegimenTributario="GENX")), BASE + [sql("BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo", IdSerieFondo="FM001A",
                                                                   IdRegimenFondoMutuo="GENX", IdEmpresa=1, IdEstado="ACT")],
               colaboradores=ok(1), tablas=[SOL_TABLA])
    for n, grupo in [(26, "ART108"), (27, "ART107"), (28, "57BIS")]:
        yield caso("COM-NN-%d_regimen-%s" % (n, grupo.lower()), S, "FM-COM: GrupoRegimen %s => Tributacion del grupo" % grupo,
                   ent(sol(1, IdRegimenTributario="R" + grupo)), BASE + regimen("R" + grupo, grupo), colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-25_cliente-sin-ejecutivo", S, "FM-COM: el ejecutivo del cliente no es empleado => IdEmpleadoEjecutivo 0 en la solicitud",
               ent(sol(1, IdCliente=103, IdPortafolio=503)), BASE + [
                   sql("BSCL.SocioNegocio.BD::CLI.Cliente", IdCliente=103, IdPersona=1000, FechaCreacion=RawTs, IdPersonaEjecutivo=9999),
                   sql("BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado", IdCliente=103, IdEstado="ACT"),
                   sql("BSCL.Portafolio.BD::POR.Portafolio", IdPortafolio=503, CodigoPortafolio="PF503", IdCliente=103, IdEstado="ACT", Proposito="PROPGEN")],
               colaboradores=merge(ok(1), {"ConsultarProductosPortafolio": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_Mensajes": [], "p_out_PortafolioProductos": [
                   {"IdPortafolio": 503, "IdEstadoPerfilProducto": "ACT", "IdTipoProducto": "INFM", "IdTipoOperacion": "0"}]}}}), tablas=[SOL_TABLA])
    yield caso("COM-NN-29_sin-tipo-movimiento", S, "FM-REQ-05: no existe el tipo RESCNN en FMM.TipoMovimientoFondoMutuo => tipo '' => rechazada",
               ent(sol(1)), DIC, colaboradores=ok(1), tablas=[SOL_TABLA])
    yield caso("COM-NN-24_simulacion-con-todas-rechazadas", S, "FM-COM: simulando y con todas rechazadas: igual que sin simular, sin resultado",
               ent(sol(1), simular="S"), BASE, colaboradores=merge(fechas(), cuotas()), tablas=[SOL_TABLA])
