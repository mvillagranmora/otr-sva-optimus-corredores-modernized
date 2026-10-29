"""CrearSolicitudAGF cases (FM-NOR-51, FM-NOR-52)."""
from cases3b_lib import caso, set_param, del_param, dicc
from cases3b_rows import fila, fecha, SOLICITUD, VISTA_SOLICITUD, INTERFAZ_AGF
import cases3b_rows as R

S = "CrearSolicitudAGF"
TABLAS = [(INTERFAZ_AGF, '"IdInterfazAgf"')]
DIC = [dicc("NO_SE_INGRESARON_SOLICITUDES", "No se ingresaron solicitudes"), dicc("SOLICITUDES_REPETIDAS", "Solicitudes repetidas"),
       dicc("NO_SE_ENCONTRO_SOLICITUD", "No se encontro la solicitud"),
       dicc("VALOR_CUOTA_VACIO", "Valor cuota vacio ::IdSerieFondo:: ::DocumentoIdentidad:: ::TipoContrato:: ::FolioOperacion::")]
S_ON = set_param("UsaReplicaSigaFondoMutuo", "S")
# HANA defaults of the NOT NULL columns that the two INSERT leave out are not in the exported catalog: the H2 fixture gives them neutral defaults.
OMITIDAS = ["FechaLiquidacion", "FechaAceptacion", "TipoSolicitud", "IdEstadoSolicitud", "IdSerieFondo", "IdUsuario", "NumeroCuotas", "ValorCuota", "Monto",
            "CalculoPorMontoCuota", "IdMoneda", "PropositoSerie", "IdSerieFondoDestino", "IdEstadoSerieFondo", "IndicadorMLT", "IdSolicitudAporteRescate",
            "IdSolicitudAporteRescateRelacionado", "IdCliente", "IdPortafolio"]


def defaults():
    R._load()
    tipos = dict(R._tables[INTERFAZ_AGF])
    out = []
    for c in OMITIDAS:
        out.append('ALTER TABLE "%s" ALTER COLUMN "%s" SET DEFAULT %s;' % (INTERFAZ_AGF, c, R._default(R._types[tipos[c]])))
    return out


def sol_base(i, **ov):
    d = dict(IdSolicitudAporteRescate=i, IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdEmpresa=1, Estado="ING")
    d.update(ov)
    return fila(SOLICITUD, **d)


def vista(i, **ov):
    d = dict(IdSolicitudAporteRescate=i, DocumentoIdentidad="11111111-1", CodigoPortafolio="PF500", FechaLiquidacion=fecha("2026-03-04"),
             FechaAceptacion=fecha("2026-03-02"), TipoSolicitud="APO", EstadoSolicitud="ING", IdSerieFondo="FM001A", IdUsuario=7, NumeroCuotas=10,
             ValorCuotaSolicitud=1500.1234, Monto=15001.234, CalculoPorMontoCuota="C", IdMoneda="CLP", IdRegimenTributario="GEN", IdEstadoSerieFondo="ACT",
             IndicadorMLT="N", NombreCompleto="Cliente Ficticio Uno", IdTipoMovimientoFondoMutuo="APONORM", FolioContratoOperacion="0", TipoContratoOperacion="")
    d.update(ov)
    return fila(VISTA_SOLICITUD, **d)


def ent(*ids):
    return {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_EsManual": "N", "p_Solicitudes": [{"IdSolicitudAporteRescate": i} for i in ids]}


def cases():
    base = DIC + defaults()
    yield caso("NOR-51-01_replica-no-hace-nada", S, "FM-NOR-51: UsaReplicaSigaFondoMutuo=N (valor sembrado): no hace nada",
               ent(1), base + [sol_base(1), vista(1)], tablas=TABLAS)
    yield caso("NOR-51-02_parametro-ausente-es-N", S, "FM-NOR-51: sin parametro => N", ent(1), base + [del_param("UsaReplicaSigaFondoMutuo"), sol_base(1), vista(1)],
               tablas=TABLAS)
    yield caso("NOR-51-03_lista-vacia", S, "FM-NOR-51: lista vacia => contador 1 NO_SE_INGRESARON_SOLICITUDES", ent(), base + [S_ON], tablas=TABLAS)
    yield caso("NOR-51-04_repetidas", S, "FM-NOR-51: id repetido => contador 1 SOLICITUDES_REPETIDAS", ent(1, 2, 1), base + [S_ON, sol_base(1), vista(1)], tablas=TABLAS)
    yield caso("NOR-51-05_no-encontrada", S, "FM-NOR-51: solicitud sin fila en la vista => NO_SE_ENCONTRO_SOLICITUD", ent(77), base + [S_ON], tablas=TABLAS)
    yield caso("NOR-51-06_valor-cuota-cero", S, "FM-NOR-51: ValorCuota 0 => VALOR_CUOTA_VACIO y no se inserta la solicitud",
               ent(1), base + [S_ON, sol_base(1), vista(1, ValorCuotaSolicitud=0)], tablas=TABLAS)
    yield caso("NOR-51-07_cuotas-cero", S, "FM-NOR-51: NumeroCuotas 0 => VALOR_CUOTA_VACIO", ent(1), base + [S_ON, sol_base(1), vista(1, NumeroCuotas=0)], tablas=TABLAS)
    yield caso("NOR-52-01_inserta-beneficiario-y-solicitud", S, "FM-NOR-52: participante nuevo + solicitud AR; resultado con los campos de la interfaz",
               ent(1), base + [S_ON, sol_base(1), vista(1)], tablas=TABLAS)
    yield caso("NOR-52-02_traspaso-tipo-TR", S, "FM-NOR-52: RESTRANSF y RESTRAPOR => TipoRegistro TR; otro => AR",
               ent(1, 2, 3), base + [S_ON, sol_base(1), sol_base(2), sol_base(3), vista(1, IdTipoMovimientoFondoMutuo="RESTRANSF"),
                                      vista(2, IdTipoMovimientoFondoMutuo="restrapor"), vista(3, IdTipoMovimientoFondoMutuo="RESNORM", TipoSolicitud="RES")], tablas=TABLAS)
    yield caso("NOR-52-03_participante-ya-existe", S, "FM-NOR-52: el participante ya esta en la interfaz (documento + codigo portafolio): no se vuelve a insertar",
               ent(1), base + [S_ON, sol_base(1), vista(1),
                               fila(INTERFAZ_AGF, IdInterfazAgf=50, DocumentoIdentidad="11111111-1", CodigoPortafolio="PF500", NombreCompleto="x", IdEmpresa=1)],
               tablas=TABLAS)
    yield caso("NOR-52-04_mezcla-ok-faltante-y-sin-cuota", S, "FM-NOR-52: tres ids: ok, inexistente y sin cuota; contador suma por mensaje y se inserta solo la ok",
               ent(1, 77, 3), base + [S_ON, sol_base(1), sol_base(3), vista(1), vista(3, ValorCuotaSolicitud=0)], tablas=TABLAS)
    yield caso("NOR-52-05_sin-fecha-sistema", S, "FM-NOR-52: falta PLA.FechaSistema => excepcion",
               ent(1), base + [S_ON, 'DELETE FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = \'PLA.FechaSistema\';'], tablas=TABLAS)
    yield caso("NOR-52-06_redondeo-a-16-decimales", S, "FM-NOR-52: los BigDecimal se redondean a escala 16 HALF_DOWN antes de insertar (la columna guarda 8)",
               ent(1), base + [S_ON, sol_base(1), vista(1, NumeroCuotas=3.123456785, Monto=0.000000005)], tablas=TABLAS)
    yield caso("NOR-52-07_dos-solicitudes-mismo-participante", S, "FM-NOR-52: dos solicitudes del mismo portafolio: un participante (GROUP BY) y dos solicitudes",
               ent(1, 2), base + [S_ON, sol_base(1), sol_base(2), vista(1), vista(2, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")], tablas=TABLAS)
    yield caso("NOR-52-08_id-nulo", S, "FM-NOR-52: IdSolicitudAporteRescate ausente: se trata como id repetido solo si hay otro nulo; uno solo cae en no encontrada",
               {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_EsManual": "N", "p_Solicitudes": [{}]}, base + [S_ON], tablas=TABLAS)
