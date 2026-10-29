package cl.bcs.contabilidad.servicios;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Each legacy service loads its statements with loadResourceToString("cl/bcs/contabilidad/sql/&lt;file&gt;")
 * (e.g. AnularCentralizacion.java:27-32). The modern services load the same resource names, and fall back to ""
 * when the resource is missing. These cases require the module to ship every statement with the legacy text.
 * ConsultarConceptoNormativo is not listed: its modern SQL is inlined and pinned by CONT-G-360.
 */
class SqlResourcesTest {

    static Stream<Arguments> services() {
        String g = "GeneraPreComprobanteMultiSeleccion_";
        return Stream.of(
                arguments("CONT-G-001", "GeneraPreComprobanteMultiSeleccion", new String[]{g + "SelectCentralizacion.sql", g + "SelectTipoContabilidad.sql",
                        g + "SelectPrecomprobanteUltimo.sql", g + "SelectCondicionesAsiento.sql", g + "SelectMovimientosDatos.sql", g + "SelectMovimientos.sql",
                        g + "SelectEventos.sql", g + "SelectDefinicionTipoGlosa.sql", g + "SelectCondicionesAsientoPrecomprobante.sql",
                        g + "SelectTipoContabilidadTipoCentralizacion.sql", g + "SelectAsiento.sql", g + "SelectEventosAsiento.sql",
                        g + "InsertPrecomprobantes.sql", g + "InsertPrecomprobantesAsientos.sql", g + "InsertEventoPrecomprobantesAsientos.sql"}),
                arguments("CONT-G-002", "CuadraturaCierreDiario", files("CuadraturaCierreDiario_", "SelectCuadraturaDiariaOperaciones", "SelectContabilidad",
                        "SelectComprobantes", "DeleteCuadraturaDiariaOperaciones", "InsertCuadraturaDiariaOperaciones", "SelectJSONCuadraturaDiariaOperaciones")),
                arguments("CONT-G-003", "AnularCentralizacion", files("AnularCentralizacion_", "SelectCentralizacion", "SelectComprobantes",
                        "UpdateEvento", "UpdateMovimiento", "UpdateMovimientoDato", "UpdateCentralizacion")),
                arguments("CONT-G-004", "EnviarPreComprobante", files("EnviarPreComprobante_", "SelectPreComprobantes", "UpdatePreComprobantes")),
                arguments("CONT-G-005", "HomologarCuentasContables", files("HomologarCuentasContables_", "SelectCuentasContables", "DeleteCuentasContables", "InsertCuentasContables")),
                arguments("CONT-G-006", "CargarCabeceraComprobante", files("CargarCabeceraComprobante_", "InsertCombrobante", "SelectIdCombrobante", "InsertCentralizaciones", "InsertGlosa")),
                arguments("CONT-G-007", "CargarAsientoComprobante", files("CargarAsientoComprobante_", "SelectCombrobante", "SelectIdCuentaContable", "SelectMetadata",
                        "InsertContabilidadDefinicionAsiento", "InsertContabilidadDefinicionTipoEvento", "InsertContabilidadTipoAsientoCondicion")),
                arguments("CONT-G-008", "CargarDeterminanteComprobante", files("CargarDeterminanteComprobante_", "InsertDeterminante", "SelectCombrobante")),
                arguments("CONT-G-009", "AgregarCuentasPorMetadata", files("AgregarCuentasPorMetadata_", "SelectCuentaFiltros", "SelectIdCuentaContable", "SelectUltimoOrden",
                        "InsertContabilidadDefinicionAsiento", "InsertContabilidadDefinicionTipoEvento", "InsertContabilidadTipoAsientoCondicion")),
                arguments("CONT-G-010", "AgregarMetadata", files("AgregarMetadata_", "SelectMetadataCuentaContable", "InsertMetadataCuentaContable", "InsertMetadataFiltros")),
                arguments("CONT-G-011", "EditarMetadata", files("EditarMetadata_", "UpdateMetadataCuentaContable", "InsertMetadataFiltros", "SelectMetadataFiltros",
                        "BorrarMetadataFiltros", "SelectComprobante", "SelectEventoComprobante")),
                arguments("CONT-G-012", "EliminarMetadata", files("EliminarMetadata_", "SelectMetadataCuentaContable", "DeleteMetadataCuentaContable", "DeleteMetadataFiltros")),
                arguments("CONT-G-013", "ConfigurarConceptoNormativo", files("ConfigurarConceptoNormativo_", "Select_CondicionConceptoExistentes", "SelectConceptoNormativo",
                        "SelectTipoDatoEvento", "SelectTipoDatoValorValidos", "InsCondicionConcepto", "UpdateCondicionConcepto", "DeleteCondicionConcepto")),
                arguments("CONT-G-014", "ConsultarConfiguracionesConceptoNormativo", files("ConsultarConfiguracionesConceptoNormativo_", "SelectConceptosConfiguraciones")),
                arguments("CONT-G-015", "EditarCuentaBasilea", files("EditarCuentaBasilea_", "SelectCuentaContableCliente", "SelectCuentaContableClienteActual", "UpdateCuentaBasilea")),
                arguments("CONT-G-016", "FiltrarCuentas", files("FiltrarCuentas_", "SelectCuentaContable")),
                arguments("CONT-G-017", "CentralizaGeneraCabecera", files("CentralizaGenerico_", "SelectTipoCentralizacion", "InsertCentralizacion")),
                arguments("CONT-G-018", "CentralizaMultiSeleccion", new String[]{"Centraliza_SelectInformesValorizados.sql",
                        "CentralizaMultiSeleccion_SelectConfiguraciones.sql", "CentralizaMultiSeleccion_CallCentralizacion.sql",
                        "CentralizaMultiSeleccion_SelectMensajes.sql", "CentralizaMultiSeleccion_SelectCentralizacionesVigentes.sql",
                        "CentralizaMultiSeleccion_DeleteDatosMovimientos.sql", "CentralizaMultiSeleccion_SelectRutaInformes.sql"}));
    }

    static String[] files(String prefix, String... names) {
        return Stream.of(names).map(n -> prefix + n + ".sql").toArray(String[]::new);
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("services")
    void contG001_legacySqlResourcesShipped(String id, String service, String[] files) {
        // legacy: every <Service>.java field "SQL_* = loadResourceToString("cl/bcs/contabilidad/sql/<file>")"
        assertThat(LegacySql.missingOrDifferent(files)).as("%s %s: statements missing or different on classpath cl/bcs/contabilidad/sql/", id, service).isEmpty();
    }
}
