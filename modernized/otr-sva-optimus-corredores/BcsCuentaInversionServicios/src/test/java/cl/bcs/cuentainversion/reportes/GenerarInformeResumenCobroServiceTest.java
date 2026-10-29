package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarInformeResumenCobroService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarInformeResumenCobro} (legacy/.../cuentaInversion/GenerarInformeResumenCobro.java) against the modern {@code GenerarInformeResumenCobroService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarInformeResumenCobroServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-161_informe-resumen-cobro-xls-y-log",
            "CTA-B-162_informe-resumen-cobro-sin-datos-solo-cabecera",
            "CTA-B-163_informe-resumen-cobro-filtra-por-cliente",
            "CTA-B-164_informe-resumen-cobro-ruta-inexistente-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GenerarInformeResumenCobroService(caso.jdbc())::execute);
        EJECUTADOS.incrementAndGet();
        return new Contexto(caso, colab, r);
    }

    private static void verificar(Contexto x) {
        x.caso().assertEquivalente(x.resultado());
        PendienteSupport.assertLlamadas(x.colaboradores(), x.caso());
        PendienteSupport.assertArchivos(x.caso());
    }

    private static List<String> servicios(FakeColaboradores f) {
        return f.llamadas().stream().map(o -> o.get("servicio").getAsString()).toList();
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
        System.out.println("equivalence cases executed (GenerarInformeResumenCobro): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-161: xls and log file")
    void ctaB161_informeResumenCobroXlsYLog() {
        // GenerarInformeResumenCobro.java:82-235 (HSSFWorkbook, log writer :235)
        // Writes <p_Ruta>/InformeResumenCobro_ddMMyyyy.xls (HSSF, system date 14032025), sheet 'Resumen Cobro': RUT, portfolio,
        // daily accumulated commission, client name, from ConsultarCobroComisionRegistros (module forced to CUS,
        // UsaReglasNegocio N); log with ARCHIVO_GENERADO_EXITOSAMENTE.
        Contexto x = ejecutar("CTA-B-161_informe-resumen-cobro-xls-y-log");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/InformeResumenCobro_14032025.log", "CTA/InformeResumenCobro_14032025.xls");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-162: no data: header only")
    void ctaB162_informeResumenCobroSinDatosSoloCabecera() {
        // GenerarInformeResumenCobro.java:120-229
        // No commission rows: header only.
        Contexto x = ejecutar("CTA-B-162_informe-resumen-cobro-sin-datos-solo-cabecera");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/InformeResumenCobro_14032025.log", "CTA/InformeResumenCobro_14032025.xls");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-163: filters by client")
    void ctaB163_informeResumenCobroFiltraPorCliente() {
        // GenerarInformeResumenCobro.java:82-120
        // Filter by client 1002: only its portfolio row.
        Contexto x = ejecutar("CTA-B-163_informe-resumen-cobro-filtra-por-cliente");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/InformeResumenCobro_14032025.log", "CTA/InformeResumenCobro_14032025.xls");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-164: missing directory throws (defect: unprotected log write)")
    void ctaB164_informeResumenCobroRutaInexistenteLanzaExcepcion() {
        // GenerarInformeResumenCobro.java:119, :235
        // LEGACY DEFECT (same as the daily patrimony report): the unprotected log write in a missing directory throws
        // FileNotFoundException.
        Contexto x = ejecutar("CTA-B-164_informe-resumen-cobro-ruta-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.io.FileNotFoundException").isEqualTo("java.io.FileNotFoundException");
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
