package cl.bcs.sura;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * SQL oracle checks. The legacy SQL files are the oracle: if the legacy directory cannot be found the tests FAIL
 * (never skip). Override the location with -Dsura.legacy.dir=... when running from another checkout.
 *
 * B-SURA-20 (parameterized SQL, RULE-068 pattern: only :Named placeholders, no concatenation)
 * B-SURA-21 (migrated SQL equals legacy SQL; semantic pins that H2 cannot execute).
 *
 * H2 vs HANA: none of these statements can run on H2 (HANA column views "BSCL...::X", UPDATE ... FROM ... JOIN,
 * NEXTVAL on a sequence, CURRENT_UTCTIMESTAMP, local temporary tables). They are checked as text.
 */
class SuraSqlOracleTest {

    static final AtomicInteger EXECUTED = new AtomicInteger();

    static final String UTILS = "ModificarPerfilesPortafolioUtils_";
    static final String WS = "ModificarClienteModificarPerfilesPortafolioWS_";

    /** file -> expected named parameters (derived from the Java call sites of the legacy classes). */
    static final Map<String, Set<String>> PARAMS = Map.ofEntries(
            Map.entry(UTILS + "ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql", Set.of("DocumentoIdentidad")),
            Map.entry(UTILS + "ConsultarPerfilesComercialesPorDocumentosIdentidad.sql", Set.of("DocumentoIdentidad")),
            Map.entry(UTILS + "ModificarPerfilesPortafolio.sql", Set.of("IdPerfilComercial", "IdPerfilProductos", "IdPerfilCobros", "IdPortafolio")),
            Map.entry(UTILS + "InsertIndicadorCobroCustodia.sql", Set.of("IdEntidad", "ValorIndicador")),
            Map.entry(UTILS + "UpdateIndicadorCobroCustodia.sql", Set.of("IdEntidad", "ValorIndicador")),
            Map.entry(UTILS + "InsertDataTablaTemporalClientesFormasCobro.sql", Set.of("IdCliente", "IdFormaCobro", "DocumentoIdentidad", "IdPortafolio")),
            Map.entry(UTILS + "ConsultarPerfilesComercialesFiltradosPorTablaTemporal.sql", Set.of()),
            Map.entry(UTILS + "BorrarDataTablaTemporalClientesFormasCobro.sql", Set.of()),
            Map.entry(UTILS + "CrearTablaTemporalClientesFormasCobro.sql", Set.of()),
            Map.entry(WS + "InsertIndicadorCobroCustodia.sql", Set.of("IdEntidad", "ValorIndicador")),
            Map.entry(WS + "UpdateIndicadorCobroCustodia.sql", Set.of("IdEntidad", "ValorIndicador")));

    /** Legacy files that no legacy Java class loads (dead resources); main must not depend on them. */
    static final Set<String> DEAD = Set.of(
            WS + "ConsultarIdIndicadorFormaCobroPorIdPortafolio.sql",
            WS + "ConsultarIdIndicadorIdTipoIndicadorFormaCobroPorIdPortafolio.sql",
            WS + "ConsultarIdPortafolioIdClientePorDocumentoIdentidad.sql",
            WS + "ConsultarPerfilPorIdFormaCobroIdCategoriaIdPerfilComercial.sql",
            WS + "ConsultarPerfilesPorIdClienteIdFormaCobro.sql",
            WS + "ModificarPerfilesPortafolio.sql",
            WS + "SelectIdClientePorDocumentoIdentidad.sql");

    static Stream<String> sqlFiles() { return PARAMS.keySet().stream().sorted(); }

    static Path legacyDir() {
        String p = System.getProperty("sura.legacy.dir");
        Path dir = p != null ? Paths.get(p)
                : Paths.get("").toAbsolutePath().resolve("../../../legacy/otr-sva-optimus-corredores/BSCL/Corredor/SURA/JAVA/Servicios/src/cl/bcs/sura/socionegocio").normalize();
        if (!Files.isDirectory(dir.resolve("sql"))) {
            fail("ORACLE MISSING: legacy SURA directory not found at " + dir + " (set -Dsura.legacy.dir). A comparison that cannot run is a failure.");
        }
        return dir;
    }

    static String norm(String s) {
        return s.replace("\r\n", "\n").lines().map(String::stripTrailing).collect(Collectors.joining("\n")).strip();
    }

    static String main(String file) throws IOException {
        try (InputStream is = SuraSqlOracleTest.class.getClassLoader().getResourceAsStream("cl/bcs/sura/socionegocio/sql/" + file)) {
            if (is == null) fail("main resource missing: " + file);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static String flat(String s) { return norm(s).replaceAll("\\s+", " "); }

    @AfterAll
    static void report() { System.out.println("equivalence cases executed: " + EXECUTED.get()); }

    @ParameterizedTest(name = "bSura21 identico al legacy: {0}")
    @MethodSource("sqlFiles")
    void bSura21_recursoMigradoEsIgualAlLegacy(String file) throws IOException {
        String legacy = Files.readString(legacyDir().resolve("sql").resolve(file), StandardCharsets.UTF_8);
        assertThat(norm(main(file))).isEqualTo(norm(legacy));
        EXECUTED.incrementAndGet();
    }

    @ParameterizedTest(name = "bSura20 solo parametros nombrados esperados: {0}")
    @MethodSource("sqlFiles")
    void bSura20_soloPlaceholdersNombradosEsperados_sinConcatenacion(String file) throws IOException {
        String sql = main(file);
        Matcher m = Pattern.compile("(?<!:):([A-Za-z_]\\w*)").matcher(sql);
        Set<String> found = new LinkedHashSet<>();
        while (m.find()) found.add(m.group(1));
        assertThat(found).containsExactlyInAnyOrderElementsOf(PARAMS.get(file));
        assertThat(sql).doesNotContain("%s").doesNotContain("\" +").doesNotContain("${");
        EXECUTED.incrementAndGet();
    }

    @Test
    void bSura21_recursosMuertosDelLegacy_nadieLosCarga_yMainNoLosNecesita() throws IOException {
        Path dir = legacyDir();
        String allJava;
        try (Stream<Path> s = Files.list(dir)) {
            allJava = s.filter(p -> p.toString().endsWith(".java")).map(p -> {
                try { return Files.readString(p, StandardCharsets.UTF_8); } catch (IOException e) { throw new IllegalStateException(e); }
            }).collect(Collectors.joining("\n"));
        }
        for (String dead : DEAD) {
            assertThat(Files.exists(dir.resolve("sql").resolve(dead))).as("legacy file " + dead).isTrue();
            assertThat(allJava).as("referenced by legacy java: " + dead).doesNotContain(dead);
            EXECUTED.incrementAndGet();
        }
        // the temp-table DDL is also never loaded by legacy java (the DML uses the table without '#')
        assertThat(allJava).doesNotContain(UTILS + "CrearTablaTemporalClientesFormasCobro.sql");
        // every other legacy file is loaded
        for (String used : PARAMS.keySet()) {
            if (used.equals(UTILS + "CrearTablaTemporalClientesFormasCobro.sql")) continue;
            assertThat(allJava).as("loaded by legacy java: " + used).contains(used);
            EXECUTED.incrementAndGet();
        }
    }

    // ---------------------------------------------------------------- semantic pins (cannot be executed on H2)

    @Test
    void bSura21_updateIndicador_utilsUsaUPPERenAmbosLados_perolaVersionWSsoloEnLaColumna() throws IOException {
        assertThat(flat(main(UTILS + "UpdateIndicadorCobroCustodia.sql")))
                .contains("ON UPPER(DET.\"DetalleIndicador\") = UPPER(:ValorIndicador)");
        // legacy quirk kept on purpose: the WS variant does NOT upper-case the parameter, so a lower-case value never matches
        String ws = flat(main(WS + "UpdateIndicadorCobroCustodia.sql"));
        assertThat(ws).contains("ON UPPER(DET.\"DetalleIndicador\") = :ValorIndicador");
        assertThat(ws).doesNotContain("UPPER(:ValorIndicador)");
        // the indicator id arrives in :IdEntidad
        assertThat(ws).contains("WHERE IND.\"IdIndicador\" = :IdEntidad");
        EXECUTED.incrementAndGet();
    }

    @Test
    void bSura21_insertIndicador_valoresFijos_ycatalogoCaseInsensitive() throws IOException {
        for (String f : new String[]{UTILS + "InsertIndicadorCobroCustodia.sql", WS + "InsertIndicadorCobroCustodia.sql"}) {
            String s = flat(main(f));
            assertThat(s).contains("'ING' AS \"IdEstadoAutorizacion\"")
                    .contains("'0001-01-01 00:00:01'")
                    .contains("TI.\"CodigoTipoIndicador\" = 'COBROCUST' AND TI.\"IdTipoEntidad\" = 'PORTAFOLIO'")
                    .contains("UPPER(DET.\"DetalleIndicador\") = UPPER(:ValorIndicador)")
                    .contains("PORT.\"IdPortafolio\" = :IdEntidad")
                    .contains("\"BSCL.Plataforma.BD.Secuencias::Indicador\".NEXTVAL");
        }
        EXECUTED.incrementAndGet();
    }

    @Test
    void bSura21_consultas_apvSeResuelveContraElCodigoPortafolioAPV_yJoinsInnerVsLeft() throws IOException {
        String apv = "PERF.\"CodigoPortafolio\" = ( CASE WHEN POR.\"Proposito\" = 'PROPOSITOAPV' THEN 'APV' ELSE POR.\"CodigoPortafolio\" END )";
        String ids = flat(main(UTILS + "ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql"));
        String perfiles = flat(main(UTILS + "ConsultarPerfilesComercialesPorDocumentosIdentidad.sql"));
        String filtrados = flat(main(UTILS + "ConsultarPerfilesComercialesFiltradosPorTablaTemporal.sql"));
        assertThat(ids).startsWith("SELECT DISTINCT POR.\"IdPortafolio\" , POR.\"IdCliente\" , DOCI.\"DocumentoIdentidad\" , INDPORT.\"IdIndicador\" , POR.\"Proposito\"")
                .contains(apv)
                .contains("INNER JOIN \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio\" AS INDPORT");   // portfolios WITHOUT a COBROCUST indicator are excluded
        assertThat(perfiles).contains(apv)
                .contains("INNER JOIN \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio\" AS IND")
                .contains("UPPER(PERF.\"IdFormaCobro\") = UPPER(IND.\"NombreValorIndicador\")")
                .contains("COALESCE(PERF.\"IdFormaCobro\", '') AS \"IdFormaCobro\"")
                .contains("COALESCE(IND.\"IdIndicador\", 0) AS \"IdIndicador\"");
        assertThat(filtrados).contains(apv)
                .contains("LEFT JOIN \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio\" AS IND")
                .contains("COALESCE(PERF.\"IdFormaCobro\", TEMP.\"IdFormaCobro\", '') AS \"IdFormaCobro\"")
                .contains("COALESCE(PERF.\"IdPerfilComercialBase\", '') AS \"IdPerfilComercial\"")
                .doesNotContain("WHERE");  // the temp table is NOT filtered by document: the whole table is read
        EXECUTED.incrementAndGet();
    }

    @Test
    void bSura21_updatePerfiles_tresColumnas_yBorradoTotalDeLaTemporal() throws IOException {
        String upd = flat(main(UTILS + "ModificarPerfilesPortafolio.sql"));
        assertThat(upd).isEqualTo("UPDATE PORT SET PORT.\"IdPerfilComercialBaseOrigen\" = :IdPerfilComercial , PORT.\"IdPerfilProductos\" = :IdPerfilProductos , "
                + "PORT.\"IdPerfilCobros\" = :IdPerfilCobros FROM \"BSCL.Portafolio.BD::POR.Portafolio\" PORT WHERE PORT.\"IdPortafolio\" = :IdPortafolio;");
        assertThat(flat(main(UTILS + "BorrarDataTablaTemporalClientesFormasCobro.sql")))
                .isEqualTo("DELETE FROM \"ModificarPerfilesPortafolioUtils_ClientesFormasCobro\";");
        EXECUTED.incrementAndGet();
    }
}
