package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.EditarCobroComisionOpcionesCobroService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.EditarCobroComisionOpcionesCobro} (legacy/.../cuentaInversion/EditarCobroComisionOpcionesCobro.java) against the modern
 * {@code EditarCobroComisionOpcionesCobroService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class EditarCobroComisionOpcionesCobroServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-08_editar-opcion-cobro-existente",
            "CTA-B-09_editar-opcion-cobro-no-encontrada",
            "CTA-B-10_editar-colision-de-clave-unica-es-advertencia",
            "CTA-B-11_editar-opcion-cobro-porcentaje-invalido",
            "CTA-B-12_editar-opcion-cobro-sin-opciones");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new EditarCobroComisionOpcionesCobroService(caso.dataSource())::execute);
        EJECUTADOS.incrementAndGet();
        return r;
    }

    private static void verificar(String nombre, Resultado r) {
        CASOS_CARGADOS.get(nombre).assertEquivalente(r);
    }

    private static JsonObject salida(Resultado r) {
        assertThat(r.excepcion()).as("modern implementation threw").isNull();
        return r.salida();
    }

    private static long contador(JsonObject s, String campo) {
        return s.get(campo).getAsLong();
    }

    @AfterAll
    static void ningunCasoOmitido() {
        System.out.println("equivalence cases executed (EditarCobroComisionOpcionesCobro): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-08: existing option is updated in place")
    void ctaB08_editarOpcionCobroExistente() {
        // EditarCobroComisionOpcionesCobro.java:156-168 (UPDATE of existing options)
        // Existing option updated in place; FechaModificacion = CURRENT_UTCTIMESTAMP (masked as <NOW>).
        Resultado r = ejecutar("CTA-B-08_editar-opcion-cobro-existente");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro")).as("rows of CobroComisionOpcionesCobro").hasSize(4);
        verificar("CTA-B-08_editar-opcion-cobro-existente", r);
    }

    @Test
    @DisplayName("CTA-B-09: option found neither by id nor by key is an error")
    void ctaB09_editarOpcionCobroNoEncontrada() {
        // EditarCobroComisionOpcionesCobro.java:139-153 (UI_ERROR_OPCION_NO_ENCONTRADA)
        // Neither the id nor the (OpcionCobro, APV, Mercado) key exists -> UI_ERROR_OPCION_NO_ENCONTRADA (message parameter
        // IdOpcion is 0).
        Resultado r = ejecutar("CTA-B-09_editar-opcion-cobro-no-encontrada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_OPCION_NO_ENCONTRADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-09_editar-opcion-cobro-no-encontrada", r);
    }

    @Test
    @DisplayName("CTA-B-10: unique-key collision on update is only a warning")
    void ctaB10_editarColisionDeClaveUnicaEsAdvertencia() {
        // EditarCobroComisionOpcionesCobro.java:158-167 (catch Exception -> ADVERTENCIA UI_ADVERTENCIA_OPCION_DUPLICADA)
        // Moving 7001 onto the key of 7002 violates the unique key: the UPDATE exception is swallowed and reported as
        // ADVERTENCIA UI_ADVERTENCIA_OPCION_DUPLICADA (ERRORESCONTADOR stays 0, ADVERTENCIASCONTADOR 1). Depends on the
        // unique-key assumption of schema-reportes.sql.
        Resultado r = ejecutar("CTA-B-10_editar-colision-de-clave-unica-es-advertencia");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ADVERTENCIA_OPCION_DUPLICADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro")).as("rows of CobroComisionOpcionesCobro").hasSize(4);
        verificar("CTA-B-10_editar-colision-de-clave-unica-es-advertencia", r);
    }

    @Test
    @DisplayName("CTA-B-11: percentage above 1.0 is rejected")
    void ctaB11_editarOpcionCobroPorcentajeInvalido() {
        // EditarCobroComisionOpcionesCobro.java:115-131
        // PorcentajeCobro 2.0 -> UI_ERROR_PORCENTAJE_NO_VALIDO, no update.
        Resultado r = ejecutar("CTA-B-11_editar-opcion-cobro-porcentaje-invalido");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_PORCENTAJE_NO_VALIDO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-11_editar-opcion-cobro-porcentaje-invalido", r);
    }

    @Test
    @DisplayName("CTA-B-12: empty request is rejected")
    void ctaB12_editarOpcionCobroSinOpciones() {
        // EditarCobroComisionOpcionesCobro.java:108-112
        // Empty list -> UI_ERROR_ENTRADA_SIN_CONCEPTOS.
        Resultado r = ejecutar("CTA-B-12_editar-opcion-cobro-sin-opciones");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_CONCEPTOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-12_editar-opcion-cobro-sin-opciones", r);
    }
}
