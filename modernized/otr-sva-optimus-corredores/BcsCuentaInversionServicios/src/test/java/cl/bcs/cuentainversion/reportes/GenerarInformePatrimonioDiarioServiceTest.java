package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarInformePatrimonioDiarioService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarInformePatrimonioDiario} (legacy/.../cuentaInversion/GenerarInformePatrimonioDiario.java) against the modern {@code GenerarInformePatrimonioDiarioService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarInformePatrimonioDiarioServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-157_informe-patrimonio-diario-xlsx-y-log",
            "CTA-B-158_informe-patrimonio-diario-sin-datos-solo-cabecera",
            "CTA-B-159_informe-patrimonio-diario-filtra-por-portafolio-y-estado",
            "CTA-B-160_informe-patrimonio-diario-ruta-inexistente-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GenerarInformePatrimonioDiarioService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (GenerarInformePatrimonioDiario): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-157: xlsx and log file")
    void ctaB157_informePatrimonioDiarioXlsxYLog() {
        // GenerarInformePatrimonioDiario.java:86-341 (XSSFWorkbook, log writer :341)
        // Writes <p_Ruta>/InformePatrimonioDiario_ddMMyyyy.xlsx (date = SYSTEM date 14032025, not the query range) with sheet
        // 'Patrimonio Diario', 12 columns (date as yyyyMMdd, RUT, portfolio, market, nemo, quantities, price, valuation,
        // commission factor, nemo factor, accumulated commission) from custody and cash patrimony, and a .log with the
        // dictionary message ARCHIVO_GENERADO_EXITOSAMENTE.
        Contexto x = ejecutar("CTA-B-157_informe-patrimonio-diario-xlsx-y-log");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/InformePatrimonioDiario_14032025.log", "CTA/InformePatrimonioDiario_14032025.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-158: no data: header only")
    void ctaB158_informePatrimonioDiarioSinDatosSoloCabecera() {
        // GenerarInformePatrimonioDiario.java:122-335
        // No patrimony in the range: the workbook only has the header row; the process still reports success.
        Contexto x = ejecutar("CTA-B-158_informe-patrimonio-diario-sin-datos-solo-cabecera");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/InformePatrimonioDiario_14032025.log", "CTA/InformePatrimonioDiario_14032025.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-159: filters by portfolio and state")
    void ctaB159_informePatrimonioDiarioFiltraPorPortafolioYEstado() {
        // GenerarInformePatrimonioDiario.java:86-122
        // Filters by portfolio and by state INA (the only way to see the INA rows).
        Contexto x = ejecutar("CTA-B-159_informe-patrimonio-diario-filtra-por-portafolio-y-estado");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/InformePatrimonioDiario_14032025.log", "CTA/InformePatrimonioDiario_14032025.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-160: missing directory throws (defect: unprotected log write)")
    void ctaB160_informePatrimonioDiarioRutaInexistenteLanzaExcepcion() {
        // GenerarInformePatrimonioDiario.java:121, :341
        // LEGACY DEFECT: the workbook write failure is caught and only counted/commented, but the log file is then opened in the
        // same missing directory outside any protection, so the call ends in FileNotFoundException. Only the exception class is
        // pinned.
        Contexto x = ejecutar("CTA-B-160_informe-patrimonio-diario-ruta-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.io.FileNotFoundException").isEqualTo("java.io.FileNotFoundException");
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
