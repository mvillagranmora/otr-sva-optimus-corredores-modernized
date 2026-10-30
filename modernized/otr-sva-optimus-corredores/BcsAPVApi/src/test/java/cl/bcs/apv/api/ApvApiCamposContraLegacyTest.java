package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Pins the FIELDS of every record against the legacy {@code Entrada*}/{@code Salida*} classes read independently from the sources
 * ({@link LegacyApv}, {@link GrafoApv}): same number of fields, same order (own fields first, inherited after), names (camelCase rule of the
 * contract, or the documented collision variants) and types (mapping documented in {@link GrafoApv}), and the figures of CONTRATO.md / CONTRATO_CAMPOS.md.
 *
 * <p>Rule ids: B-APVAPI-06 (records and fields vs legacy), B-APVAPI-07 (no record without a legacy class and no legacy class without a record),
 * B-APVAPI-08 (names), B-APVAPI-09 (figures 234 records / 2096 fields in the docs), B-APVAPI-10 (annex rows equal the code).
 */
class ApvApiCamposContraLegacyTest {

    private static final int RECORDS = 234;
    private static final int CAMPOS = 2096;

    private static String resumen(List<String> errores) {
        return errores.size() + " difference(s):\n  " + String.join("\n  ", errores.stream().limit(40).toList());
    }

    // B-APVAPI-06
    @Test
    void b_apvapi_06_cada_record_tiene_los_campos_nombres_tipos_y_orden_del_POJO_legacy() {
        GrafoApv g = GrafoApv.get();
        assertTrue(g.errores.isEmpty(), resumen(g.errores));
        assertEquals(RECORDS, g.pares.size(), "records reached from the legacy Entrada/Salida graphs");
        assertEquals(CAMPOS, g.totalCampos(), "fields of those records");
        // the walk really compared something: every record has at least the component count of its legacy class
        for (GrafoApv.Par p : g.pares) {
            assertEquals(p.campos().size(), p.record().getRecordComponents().length, p.record().getName());
        }
        System.out.println("[B-APVAPI-06] records=" + g.pares.size() + " fields=" + g.totalCampos() + " externalTypesAsJsonObject=" + g.externos);
    }

    // B-APVAPI-07
    @Test
    void b_apvapi_07_no_hay_records_sobrantes_ni_clases_legacy_sin_record() throws Exception {
        GrafoApv g = GrafoApv.get();
        Set<String> alcanzados = new TreeSet<>();
        for (Set<Class<?>> s : g.recordsPorCaso.values()) {
            for (Class<?> c : s) {
                alcanzados.add(c.getName());
            }
        }
        List<String> enDisco = GrafoApv.recordsEnDisco();
        assertEquals(new TreeSet<>(enDisco), alcanzados, "records on disk (src/main/java/.../dto) vs records reached from the legacy graphs");
        assertEquals(RECORDS, enDisco.size(), "records on disk");
        assertEquals(64, g.recordsPorCaso.size(), "operations with records");
        for (String nombre : enDisco) {
            assertTrue(Class.forName(nombre).isRecord(), nombre + " must be a record");
        }
    }

    // B-APVAPI-08
    @Test
    void b_apvapi_08_nombres_de_componentes_siguen_la_regla_y_las_variantes_estan_acotadas() {
        GrafoApv g = GrafoApv.get();
        // pure camelCase rule for prefix stripping, checked on well known names
        assertTrue(GrafoApv.nombreCoherente("p_IdEmpresa", "idEmpresa"));
        assertTrue(GrafoApv.nombreCoherente("P_OUT_MENSAJES", "mensajes"));
        assertTrue(GrafoApv.nombreCoherente("P_OUT_ERRORESCONTADOR", "contadorErrores"));
        assertTrue(!GrafoApv.nombreCoherente("p_IdEmpresa", "empresa"), "a different name must be rejected");
        assertTrue(!GrafoApv.nombreCoherente("p_IdEmpresa", "p_IdEmpresa"), "the legacy prefix does not survive in the record");
        // every component of every record is coherent with the legacy field (done in the walk); the collision variants are few
        System.out.println("[B-APVAPI-08] name variants (legacy->record): " + g.nombresConVariante);
        assertTrue(g.nombresConVariante.size() <= 40, "unexpectedly many renamed components: " + g.nombresConVariante.size());
        assertTrue(g.errores.stream().noneMatch(e -> e.contains("component name")), resumen(g.errores));
    }

    // B-APVAPI-09
    @Test
    void b_apvapi_09_las_cifras_de_CONTRATO_md_y_del_anexo_cuadran_con_el_codigo() {
        GrafoApv g = GrafoApv.get();
        String contrato = LegacyApv.leer(LegacyApv.modulo().resolve("CONTRATO.md"));
        Matcher m = Pattern.compile("Reachable JSON POJO classes turned into records: \\*\\*(\\d+)\\*\\*; fields: \\*\\*(\\d+)\\*\\*").matcher(contrato);
        assertTrue(m.find(), "CONTRATO.md must state the figures");
        assertEquals(RECORDS, Integer.parseInt(m.group(1)), "records stated in CONTRATO.md");
        assertEquals(CAMPOS, Integer.parseInt(m.group(2)), "fields stated in CONTRATO.md");
        assertEquals(g.pares.size(), Integer.parseInt(m.group(1)), "records stated vs records found by the independent walk");
        assertEquals(g.totalCampos(), Integer.parseInt(m.group(2)), "fields stated vs fields found by the independent walk");
        assertEquals(RECORDS, g.bloquesAnexo, "tables in CONTRATO_CAMPOS.md");
        assertEquals(CAMPOS, g.filasAnexo, "field rows in CONTRATO_CAMPOS.md");
        assertEquals(g.pares.size(), GrafoApv.recordsEnDisco().size());
    }

    // B-APVAPI-10
    @Test
    void b_apvapi_10_las_filas_del_anexo_coinciden_con_legacy_y_con_los_records() {
        GrafoApv g = GrafoApv.get();
        assertTrue(g.erroresAnexo.isEmpty(), resumen(g.erroresAnexo));
    }
}
