package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.EditarCobroComisionRegistrosService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.EditarCobroComisionRegistros} (legacy/.../cuentaInversion/EditarCobroComisionRegistros.java) against the modern
 * {@code EditarCobroComisionRegistrosService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class EditarCobroComisionRegistrosServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-32_editar-registro-crea-editado",
            "CTA-B-33_editar-registro-modifica-editado-existente",
            "CTA-B-34_editar-registro-reactiva-inactivo",
            "CTA-B-35_editar-registro-procesado-no-editable",
            "CTA-B-36_editar-registro-fuera-de-periodo",
            "CTA-B-37_editar-registro-periodo-inexistente",
            "CTA-B-38_editar-registro-estado-distinto-vig",
            "CTA-B-39_editar-registro-periodo-termina-el-dia-del-sistema",
            "CTA-B-45_editar-registro-periodo-termino-el-dia-anterior");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new EditarCobroComisionRegistrosService(caso.dataSource())::execute);
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
        System.out.println("equivalence cases executed (EditarCobroComisionRegistros): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-32: no edited row yet: a new one is inserted")
    void ctaB32_editarRegistroCreaEditado() {
        // EditarCobroComisionRegistros.java:141-160 (sequence + insert, UI_REGISTRO_EDITADO_CORRECTAMENTE)
        // No edited row for (portfolio, period): a new one is inserted with sequence id, FechaModificacion = system date.
        Resultado r = ejecutar("CTA-B-32_editar-registro-crea-editado");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTRO_EDITADO_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(4);
        verificar("CTA-B-32_editar-registro-crea-editado", r);
    }

    @Test
    @DisplayName("CTA-B-33: existing VIG edited row gets the new amount")
    void ctaB33_editarRegistroModificaEditadoExistente() {
        // EditarCobroComisionRegistros.java:184-196 (MODIFICACION_EDITADO)
        // Existing VIG edited row gets the new amount (state forced to VIG).
        Resultado r = ejecutar("CTA-B-33_editar-registro-modifica-editado-existente");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTRO_EDITADO_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(3);
        verificar("CTA-B-33_editar-registro-modifica-editado-existente", r);
    }

    @Test
    @DisplayName("CTA-B-34: INA edited row is reactivated")
    void ctaB34_editarRegistroReactivaInactivo() {
        // EditarCobroComisionRegistros.java:172-186
        // An INA edited row (previously deleted) is reactivated with the new amount.
        Resultado r = ejecutar("CTA-B-34_editar-registro-reactiva-inactivo");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTRO_EDITADO_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(3);
        verificar("CTA-B-34_editar-registro-reactiva-inactivo", r);
    }

    @Test
    @DisplayName("CTA-B-35: PRO edited row cannot be edited")
    void ctaB35_editarRegistroProcesadoNoEditable() {
        // EditarCobroComisionRegistros.java:166-171 (UI_REGISTRO_EDITADO_PROCESADO)
        // PRO edited row -> UI_REGISTRO_EDITADO_PROCESADO, no change.
        Resultado r = ejecutar("CTA-B-35_editar-registro-procesado-no-editable");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTRO_EDITADO_PROCESADO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-35_editar-registro-procesado-no-editable", r);
    }

    @Test
    @DisplayName("CTA-B-36: period not yet over is out of period")
    void ctaB36_editarRegistroFueraDePeriodo() {
        // EditarCobroComisionRegistros.java:105-115 (UI_FUERA_DE_PERIODO)
        // Period 3 ends 2025-03-31, system date is 2025-03-14 (not after the period end) -> UI_FUERA_DE_PERIODO.
        Resultado r = ejecutar("CTA-B-36_editar-registro-fuera-de-periodo");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_FUERA_DE_PERIODO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-36_editar-registro-fuera-de-periodo", r);
    }

    @Test
    @DisplayName("CTA-B-37: unknown period")
    void ctaB37_editarRegistroPeriodoInexistente() {
        // EditarCobroComisionRegistros.java:115-121 (UI_NO_EXISTE_PERIODO)
        // Unknown period -> UI_NO_EXISTE_PERIODO.
        Resultado r = ejecutar("CTA-B-37_editar-registro-periodo-inexistente");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_NO_EXISTE_PERIODO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-37_editar-registro-periodo-inexistente", r);
    }

    @Test
    @DisplayName("CTA-B-38: only state VIG may be edited")
    void ctaB38_editarRegistroEstadoDistintoVig() {
        // EditarCobroComisionRegistros.java:125-130 (UI_REGISTRO_DISTINTO_VIG)
        // Only VIG records can be edited -> UI_REGISTRO_DISTINTO_VIG.
        Resultado r = ejecutar("CTA-B-38_editar-registro-estado-distinto-vig");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTRO_DISTINTO_VIG");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-38_editar-registro-estado-distinto-vig", r);
    }

    @Test
    @DisplayName("CTA-B-39: period ending ON the system date is out of period (boundary)")
    void ctaB39_editarRegistroPeriodoTerminaElDiaDelSistema() {
        // EditarCobroComisionRegistros.java:109 (!fechaSIS.after(fechaHasta))
        // Boundary: the period ends ON the system date (2025-03-14): fechaSistema.after(fechaHasta) is false ->
        // UI_FUERA_DE_PERIODO.
        Resultado r = ejecutar("CTA-B-39_editar-registro-periodo-termina-el-dia-del-sistema");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_FUERA_DE_PERIODO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-39_editar-registro-periodo-termina-el-dia-del-sistema", r);
    }

    @Test
    @DisplayName("CTA-B-45: period that ended the day before is accepted (boundary)")
    void ctaB45_editarRegistroPeriodoTerminoElDiaAnterior() {
        // EditarCobroComisionRegistros.java:109
        // Boundary: the period ended the day before the system date -> accepted (new edited row created).
        Resultado r = ejecutar("CTA-B-45_editar-registro-periodo-termino-el-dia-anterior");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTRO_EDITADO_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(4);
        verificar("CTA-B-45_editar-registro-periodo-termino-el-dia-anterior", r);
    }
}
