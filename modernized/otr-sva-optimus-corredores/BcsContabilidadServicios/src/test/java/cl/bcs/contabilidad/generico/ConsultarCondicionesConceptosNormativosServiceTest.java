package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.service.ConsultarCondicionesConceptosNormativosService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for legacy cl.bcs.contabilidad.ConsultarCondicionesConceptosNormativos
 * (ConsultarCondicionesConceptosNormativos.java:59-121).
 *
 * Runs the real ported SQL resource against an in-memory H2 database (schema in
 * src/test/resources/generico/condiciones_conceptos_schema.sql). The legacy query has
 * no WHERE clause and the input (p_IdEmpresa, p_IdUsuario, p_IdModulo) is never used.
 * Output is serialised with a plain {@code new Gson()} (nulls omitted).
 */
class ConsultarCondicionesConceptosNormativosServiceTest {

    private EmbeddedDatabase db;
    private JdbcTemplate jdbc;
    private ConsultarCondicionesConceptosNormativosService service;

    @BeforeEach
    void setUp() {
        db = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .addScript("generico/condiciones_conceptos_schema.sql")
                .build();
        jdbc = new JdbcTemplate(db);
        service = new ConsultarCondicionesConceptosNormativosService(new NamedParameterJdbcTemplate(db));
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    private void concepto(long id, String nombre) {
        jdbc.update("INSERT INTO \"BSCL.Contabilidad.BD::ITZ.ConceptoNormativo\" VALUES (?,?,'V')", id, nombre);
    }

    private void condicion(long id, long concepto, String tipoDato, String valor, String operador) {
        jdbc.update("INSERT INTO \"BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo\" VALUES (?,?,?,?,?)",
                id, concepto, tipoDato, valor, operador);
    }

    private static JsonObject in(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static JsonObject conceptoPorId(JsonObject out, long id) {
        return StreamSupport.stream(out.getAsJsonArray("conceptosNormativos").spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .filter(c -> c.get("IdConceptoNormativo").getAsLong() == id)
                .findFirst().orElseThrow(() -> new AssertionError("concepto " + id + " missing in " + out));
    }

    private void datosBase() {
        concepto(1, "Concepto A");
        condicion(10, 1, "Moneda", "CLP", "=");
        condicion(11, 1, "Moneda", "USD", "=");
        condicion(12, 1, "Producto", "PACTOS", "IN");
        concepto(2, "Concepto Sin Condiciones");
    }

    @Test
    @DisplayName("CONT-C-80: success -> P_OUT_ERRORESCONTADOR 0, no P_OUT_MENSAJE key, one entry per concepto")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:72-73, :113; Gson without serializeNulls (:61)
    void contC80_estructuraSalida() {
        datosBase();

        JsonObject out = service.execute(in("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\"}"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(out.has("P_OUT_MENSAJE")).isFalse();
        assertThat(out.getAsJsonArray("conceptosNormativos")).hasSize(2);
        assertThat(conceptoPorId(out, 1).get("ConceptoNormativo").getAsString()).isEqualTo("Concepto A");
    }

    @Test
    @DisplayName("CONT-C-81: conditions grouped per IdTipoDatoEvento; each group holds {ValorVarchar, OperadorFuncion} values")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:94-110
    void contC81_agrupacionPorTipoDato() {
        datosBase();

        JsonObject c1 = conceptoPorId(service.execute(in("{}")), 1);

        JsonArray grupos = c1.getAsJsonArray("condiciones");
        assertThat(grupos).hasSize(2);
        JsonObject moneda = StreamSupport.stream(grupos.spliterator(), false).map(JsonElement::getAsJsonObject)
                .filter(g -> g.get("IdTipoDatoEvento").getAsString().equals("Moneda")).findFirst().orElseThrow();
        assertThat(moneda.get("IdConceptoNormativo").getAsLong()).isEqualTo(1L);
        assertThat(StreamSupport.stream(moneda.getAsJsonArray("valores").spliterator(), false)
                .map(v -> v.getAsJsonObject().get("ValorVarchar").getAsString() + v.getAsJsonObject().get("OperadorFuncion").getAsString())
                .toList()).containsExactlyInAnyOrder("CLP=", "USD=");
    }

    @Test
    @DisplayName("CONT-C-82: group objects never carry IdCondicionConceptoNormativo (never assigned -> null -> omitted by Gson)")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:100-104 (field not set)
    void contC82_grupoSinIdCondicion() {
        datosBase();

        JsonObject c1 = conceptoPorId(service.execute(in("{}")), 1);

        assertThat(c1.getAsJsonArray("condiciones")).allSatisfy(g ->
                assertThat(g.getAsJsonObject().has("IdCondicionConceptoNormativo")).isFalse());
    }

    @Test
    @DisplayName("CONT-C-83: concepto without conditions (LEFT JOIN null) -> present with empty 'condiciones'")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:82-92; SQL LEFT JOIN
    void contC83_conceptoSinCondiciones() {
        datosBase();

        JsonObject c2 = conceptoPorId(service.execute(in("{}")), 2);

        assertThat(c2.getAsJsonArray("condiciones")).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-84: IdTipoDatoEvento grouping is case-insensitive ('moneda' and 'MONEDA' -> one group)")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:95 (equalsIgnoreCase)
    void contC84_agrupacionCaseInsensitive() {
        concepto(3, "Concepto C");
        condicion(20, 3, "moneda", "EUR", "!=");
        condicion(21, 3, "MONEDA", "UF", "=");

        JsonObject c3 = conceptoPorId(service.execute(in("{}")), 3);

        assertThat(c3.getAsJsonArray("condiciones")).hasSize(1);
        JsonObject g = c3.getAsJsonArray("condiciones").get(0).getAsJsonObject();
        assertThat(g.get("IdTipoDatoEvento").getAsString()).isEqualToIgnoringCase("moneda");
        assertThat(g.getAsJsonArray("valores")).hasSize(2);
    }

    @Test
    @DisplayName("CONT-C-85: null ConceptoNormativo name -> key omitted; input parameters do not filter anything")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:77 (empty params), :85; Gson omits nulls
    void contC85_nombreNuloYSinFiltros() {
        datosBase();
        concepto(4, null);

        JsonObject a = service.execute(in("{\"p_IdEmpresa\":1}"));
        JsonObject b = service.execute(in("{\"p_IdEmpresa\":999,\"p_IdModulo\":\"OTRO\"}"));

        assertThat(conceptoPorId(a, 4).has("ConceptoNormativo")).isFalse();
        assertThat(a.getAsJsonArray("conceptosNormativos")).hasSize(3);
        assertThat(b.getAsJsonArray("conceptosNormativos")).hasSize(3);
    }

    @Test
    @DisplayName("CONT-C-86: a group with null IdTipoDatoEvento followed by another row of the same concepto -> NPE caught: errores 1, message set, empty list")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:95 (null.equalsIgnoreCase), :113 not reached, :115-118
    void contC86_npeCapturada() {
        concepto(5, "Concepto E");
        condicion(30, 5, null, "X", "=");
        condicion(31, 5, null, "Y", "=");

        JsonObject out = service.execute(in("{}"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.has("P_OUT_MENSAJE")).isTrue();
        assertThat(out.getAsJsonArray("conceptosNormativos")).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-87: SQL failure (missing table) -> errores 1, P_OUT_MENSAJE = exception message, conceptosNormativos []")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:115-118
    void contC87_errorSql() {
        jdbc.execute("DROP TABLE \"BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo\"");

        JsonObject out = service.execute(in("{}"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.get("P_OUT_MENSAJE").getAsString()).isNotBlank();
        assertThat(out.getAsJsonArray("conceptosNormativos")).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-88: empty tables -> errores 0 and empty conceptosNormativos")
    // Legacy: ConsultarCondicionesConceptosNormativos.java:72-73, :113
    void contC88_vacio() {
        JsonObject out = service.execute(in("{}"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(out.getAsJsonArray("conceptosNormativos")).isEqualTo(new JsonArray());
        assertThat(List.of(out.keySet().toArray())).containsExactlyInAnyOrder("P_OUT_ERRORESCONTADOR", "conceptosNormativos");
    }
}
