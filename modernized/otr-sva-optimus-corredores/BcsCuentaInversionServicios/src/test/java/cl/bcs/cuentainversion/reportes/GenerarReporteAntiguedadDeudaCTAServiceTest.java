package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaCTAService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Activated pending test (see reportes/README.md "Pending tests").
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaCTA} (legacy/.../cuentaInversion/GenerarReporteAntiguedadDeudaCTA.java) against the modern {@code GenerarReporteAntiguedadDeudaCTAService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarReporteAntiguedadDeudaCTAServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-145_reporte-antiguedad-cta-xlsx-separa-intermediacion",
            "CTA-B-146_reporte-antiguedad-cta-xlsx-consolidado",
            "CTA-B-147_reporte-antiguedad-cta-sin-configuracion-de-ruta",
            "CTA-B-148_reporte-antiguedad-cta-sin-codigo-de-corredora",
            "CTA-B-149_reporte-antiguedad-cta-sin-formato-de-archivo",
            "CTA-B-150_reporte-antiguedad-cta-error-de-la-consulta-genera-igual-un-archivo-con-solo-cabecera");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GenerarReporteAntiguedadDeudaCTAService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (GenerarReporteAntiguedadDeudaCTA): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-145: xlsx with intermediation split")
    void ctaB145_reporteAntiguedadCtaXlsxSeparaIntermediacion() {
        // GenerarReporteAntiguedadDeudaCTA.java:230-440 (XSSFWorkbook, FechaDeuda :389, :435)
        // Aging report to xlsx (layout REPORTE_ANTIGUEDAD_DEUDA_CTA, 12 columns incl. FechaDeuda dd-MM-yyyy): general row +
        // intermediation row for the same account, amounts as text; a row whose total is 0 would blank its FechaDeuda.
        Contexto x = ejecutar("CTA-B-145_reporte-antiguedad-cta-xlsx-separa-intermediacion");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAntiguedad_S.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-146: xlsx consolidated")
    void ctaB146_reporteAntiguedadCtaXlsxConsolidado() {
        // GenerarReporteAntiguedadDeudaCTA.java:333-440
        // Consolidar=S: the portfolio-code column is dropped from the layout and positions shift; the row is the client
        // consolidation.
        Contexto x = ejecutar("CTA-B-146_reporte-antiguedad-cta-xlsx-consolidado");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAntiguedad_C.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-147: missing path configuration")
    void ctaB147_reporteAntiguedadCtaSinConfiguracionDeRuta() {
        // GenerarReporteAntiguedadDeudaCTA.java:166
        // No path configuration -> FALTA_CONFIGURACION_RUTA_ARCHIVO.
        Contexto x = ejecutar("CTA-B-147_reporte-antiguedad-cta-sin-configuracion-de-ruta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("FALTA_CONFIGURACION_RUTA_ARCHIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-148: missing broker code")
    void ctaB148_reporteAntiguedadCtaSinCodigoDeCorredora() {
        // GenerarReporteAntiguedadDeudaCTA.java:174
        // No broker code -> CODIGO_CORREDOR_NO_EXISTE.
        Contexto x = ejecutar("CTA-B-148_reporte-antiguedad-cta-sin-codigo-de-corredora");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("CODIGO_CORREDOR_NO_EXISTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-149: missing file format")
    void ctaB149_reporteAntiguedadCtaSinFormatoDeArchivo() {
        // GenerarReporteAntiguedadDeudaCTA.java:237
        // No layout -> FALTA_CONFIGURACION_ARCHIVO_FORMATO.
        Contexto x = ejecutar("CTA-B-149_reporte-antiguedad-cta-sin-formato-de-archivo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("FALTA_CONFIGURACION_ARCHIVO_FORMATO", "UI_ERROR_AL_GENERAR_REPORTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(2L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-150: query error still writes a header only workbook")
    void ctaB150_reporteAntiguedadCtaErrorDeLaConsultaGeneraIgualUnArchivoConSoloCabecera() {
        // GenerarReporteAntiguedadDeudaCTA.java:184, :230
        // Same quirk as the creditors report: the aging query reports SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO, the messages are
        // returned but a header-only workbook is still written and its path returned.
        Contexto x = ejecutar("CTA-B-150_reporte-antiguedad-cta-error-de-la-consulta-genera-igual-un-archivo-con-solo-cabecera");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAntiguedad_E.xlsx");
        verificar(x);
    }
}
