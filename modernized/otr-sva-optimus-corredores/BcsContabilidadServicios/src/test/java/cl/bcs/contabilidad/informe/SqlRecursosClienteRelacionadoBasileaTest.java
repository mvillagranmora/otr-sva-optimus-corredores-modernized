package cl.bcs.contabilidad.informe;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Pins the query filters of WP-D. The legacy loads these files with {@code loadResourceToString(...)}; the modern
 * module must ship them under {@code src/main/resources/cl/bcs/contabilidad/sql/}. A missing resource FAILS (the
 * existing services' {@code sql()} helper silently returns "" for a missing file - that must not hide here).
 *
 * <p>Comparison ignores whitespace and case, so formatting may change but no filter may be dropped.
 * Legacy originals: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/
 * cl/bcs/contabilidad/sql/*.sql (+ BSCL/Custodia/.../cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultarCtaPropia.sql).
 */
class SqlRecursosClienteRelacionadoBasileaTest {

    private static final String BASE = "cl/bcs/contabilidad/sql/";

    static Stream<Arguments> recursos() {
        return Stream.of(
            // CONT-D-90 ConsultarOperacionesClienteRelacionado.java:29-35
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectComprobantes.sql", List.of(
                "tind.\"CodigoTipoIndicador\" = 'RELCORRPER'", "ind.\"ValorIndicador\" = 'S'",
                "COM.\"IdClasificacionTipoInstrumento\" IN ('RV','RF', 'IF') OR COM.\"IdClasificacionTipoInstrumento\" IS null",
                "com.\"IdEstadoComprobante\" != 'ANU'",
                "TO_VARCHAR(COM.\"FechaComprobante\",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin",
                "ABS(COALESCE(COM.\"MontoComprobante\" , 0)) AS \"MontoOperado\"",
                "ORDER BY com.\"IdComprobante\" desc")),
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectOperacionesSpot.sql", List.of(
                "'RELCORRPER'", "COM.\"IdClasificacionTipoInstrumento\" = 'SP'", "!= 'ANU'",
                "CASE COM.\"IdTipoOperacion\" WHEN 'ANCSPT' THEN 0 ELSE COM.\"Monto\" END AS \"MontoCompra\"",
                "CASE COM.\"IdTipoOperacion\" WHEN 'ANVSPT' THEN 0 ELSE COM.\"Monto\" END AS \"MontoVenta\"",
                "BETWEEN :FechaInicio AND :FechaFin")),
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectOperacionesForward.sql", List.of(
                "'RELCORRPER'", "\"IdTipoInstrumento\" = 'FWD'", "CAR.\"IdEstado\" in ('VIG', 'VCD')",
                "TO_VARCHAR(CAR.\"FechaOperacion\",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin")),
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectOperacionesPacto.sql", List.of(
                "'RELCORRPER'", "OPE.\"IdTipoOperacion\" IN ('COMPAC', 'VENPAC')", "OPE.\"IdEstado\" IN ('LIQ', 'VIG', 'VCD')",
                "TO_VARCHAR(ope.\"FechaLiquidacion\",'YYYY-MM-DD') AS \"FechaInicio\"",
                "BETWEEN :FechaInicio AND :FechaFin")),
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectOperacionesTermino.sql", List.of(
                "'RELCORRPER'", "OPE.\"IdTipoOperacion\" IN ('COMTER', 'VENTER')", "OPE.\"IdEstado\" IN ('LIQ', 'VIG', 'VCD')",
                "TO_VARCHAR(ope.\"FechaVencimiento\",'YYYY-MM-DD') AS \"FechaInicio\"")),
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectOperacionesTerminoCartera.sql", List.of(
                "'RELCORRPER'", "OPE.\"IdTipoOperacion\" IN ('COMTER', 'VENTER')",
                "res.\"IdClasificacionTipoInstrumento\" IN ('RF', 'IF')", "res.\"MontoValorizado\" AS \"MontoInicial\"")),
            Arguments.of("CONT-D-90", "ConsultarOperacionesClienteRelacionado_SelectPrestamos.sql", List.of(
                "CAR.\"IdTipoInstrumento\" IN ('PRT', 'PRF', 'EMP', 'ERF')", "'RELCORRPER'",
                "car.\"IdEstado\" IN ('VIG', 'LIQ', 'VCD')", "CAR.\"MontoCompra\" AS \"MontoOperado\"")),
            // ConsultarCondicionesConceptosNormativos.java:18
            Arguments.of("CONT-D-90", "ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql", List.of(
                "\"BSCL.Contabilidad.BD::ITZ.ConceptoNormativo\"",
                "LEFT JOIN \"BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo\"")),
            // CONT-D-91 GenerarInformeClienteRelacionado.java:38 (inline in legacy, externalized in modern)
            Arguments.of("CONT-D-91", "GenerarInformeClienteRelacionado_SelectLogoCorredora.sql", List.of(
                "\"BSCL.Plataforma.BD::PLA.Imagenes\"", "\"NombreImagen\" = 'LOGO_CORREDOR'",
                "\"IdEmpresa\" = :IdEmpresa", "ORDER BY Imagen.\"Version\" DESC")),
            // CONT-D-92 ProcesarInterfazBasilea.java:33-41
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectCuentasBasilea.sql", List.of(
                "\"BSCL.Contabilidad.BD::ITZ.InterfazCuentasBasilea\"", "WHERE \"IdEstado\" = 'ACT'")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectCuentaPropia.sql", List.of(
                "SOC.\"IdCliente\" AS \"IdClienteCtaPropia\"", "IDENT.\"DocumentoIdentidad\" AS \"DocumentoIdentidadCtaPropia\"",
                "WHERE EMP.\"IdEmpresa\" = :IdEmpresa")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectAcciones.sql", List.of(
                "\"IdCliente\" = :IdCliente", "'ACN' AS \"Grupo\"", "'ACCIONES' AS \"Producto\"",
                "VP.\"Monto\" * -1 AS \"Monto\"", "DA.\"Nemotecnico\" AS \"NemoBasilea\"",
                "ROUND(SUM(DA.\"Monto\"),0) AS \"Monto\"", "HAVING SUM(DA.\"Monto\") > 0")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectRfIf.sql", List.of(
                "'LIBRE' AS \"Grupo\"", "'GARANTIZADA' AS \"Grupo\"", "'RF-IF' AS \"Producto\"",
                "CAR.\"INGFechaOperacion\" <= :FechaSistema", "CAR.\"TipoCarteraCorredor\" <> 'CC'",
                "WHEN CAR.\"IdEmisor\" IN ('CENTRAL', 'TESORERIA', 'ESTADO', 'BBSCOTIABA', 'DESARROLLO', 'BBVA', 'BHIF')",
                "WHEN CAR.\"Nemotecnico\" IN ('PDBC', 'PRBC', 'PDT', 'PRT')")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectVentasCortas.sql", List.of(
                "\"EsVentaCorta\" = 'S'", "CMB.\"IdCliente\" = :IdCliente",
                "YEAR(CAR.\"FechaOperacion\") = YEAR(:FechaSistema)", "MONTH(CAR.\"FechaOperacion\") = MONTH(:FechaSistema)",
                "CAR.\"IdTipoInstrumento\" IN ('PRT', 'PRF')", "HAVING SUM(CMB.\"Monto\") > 0")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectPactosVigentes.sql", List.of(
                "'PACTEADA' AS \"Grupo\"", "OP.\"IdTipoOperacion\" = 'VENPAC'", "SUB.\"IdEstado\" IN('VIG', 'RPA')",
                "HAVING SUM(re.\"MontoValorizado\") > 0")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_SelectForward.sql", List.of(
                "'NOCIONAL' AS \"Grupo\"", "'OBLIG' AS \"Grupo\"", "'DERECHO' AS \"Grupo\"", "'FORWARD' AS \"Producto\"",
                "OPE.\"IdTipoProducto\" = 'MEFW'", "CIC.\"CodigoTipoIndicador\" = 'RELCORR'",
                "CAR.\"INGMontoValorRazonablePasivo\" AS \"Monto\"")),
            Arguments.of("CONT-D-92", "ProcesarInterfazBasilea_InsertInterfazBasilea.sql", List.of(
                "INSERT INTO \"BSCL.Contabilidad.BD::ITZ.InterfazBasilea\"",
                "\"BSCL.Contabilidad.BD.Secuencias::InterfazBasilea\".NEXTVAL", "CURRENT_UTCTIMESTAMP",
                ":FechaEnvio", ":RutCliente", ":DigitoVerificador", ":Filler"))
        );
    }

    private static String squash(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }

    @ParameterizedTest(name = "{0}: {1} existe y conserva sus filtros")
    @MethodSource("recursos")
    void contD90_92_sqlPresenteConFiltrosDelLegacy(String id, String recurso, List<String> fragmentos) throws Exception {
        String sql;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(BASE + recurso)) {
            if (in == null) fail("Recurso SQL ausente en el classpath: " + BASE + recurso);
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertThat(sql).as(recurso).isNotBlank();
        String s = squash(sql);
        for (String f : fragmentos) {
            assertThat(s).as("%s debe contener: %s", recurso, f).contains(squash(f));
        }
    }
}
