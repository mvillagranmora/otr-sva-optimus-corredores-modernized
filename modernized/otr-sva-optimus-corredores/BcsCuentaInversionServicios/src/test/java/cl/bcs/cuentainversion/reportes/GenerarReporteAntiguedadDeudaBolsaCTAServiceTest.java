package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaBolsaCTAService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaBolsaCTA} (legacy/.../cuentaInversion/GenerarReporteAntiguedadDeudaBolsaCTA.java) against the modern {@code GenerarReporteAntiguedadDeudaBolsaCTAService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarReporteAntiguedadDeudaBolsaCTAServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-151_reporte-antiguedad-bolsa-xlsx",
            "CTA-B-152_reporte-antiguedad-bolsa-xlsx-consolidado",
            "CTA-B-153_reporte-antiguedad-bolsa-sin-configuracion-de-ruta",
            "CTA-B-154_reporte-antiguedad-bolsa-sin-codigo-de-corredora",
            "CTA-B-155_reporte-antiguedad-bolsa-sin-formato-de-archivo",
            "CTA-B-156_reporte-antiguedad-bolsa-error-de-la-consulta-genera-igual-un-archivo-con-solo-cabecera");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GenerarReporteAntiguedadDeudaBolsaCTAService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (GenerarReporteAntiguedadDeudaBolsaCTA): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-151: xlsx")
    void ctaB151_reporteAntiguedadBolsaXlsx() {
        // GenerarReporteAntiguedadDeudaBolsaCTA.java:215-310 (XSSFWorkbook)
        // Bolsa aging report to xlsx (layout REPORTE_ANTIGUEDAD_DEUDA_BOLSA_CTA, RADB_ codes): intermediation row then general
        // row, with classification and related columns.
        Contexto x = ejecutar("CTA-B-151_reporte-antiguedad-bolsa-xlsx");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteBolsa_S.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-152: xlsx consolidated")
    void ctaB152_reporteAntiguedadBolsaXlsxConsolidado() {
        // GenerarReporteAntiguedadDeudaBolsaCTA.java:308
        // Consolidar=S drops RADB_CODIGOPORTAFOLIO and shifts positions.
        Contexto x = ejecutar("CTA-B-152_reporte-antiguedad-bolsa-xlsx-consolidado");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(2);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteBolsa_C.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-153: missing path configuration")
    void ctaB153_reporteAntiguedadBolsaSinConfiguracionDeRuta() {
        // GenerarReporteAntiguedadDeudaBolsaCTA.java:144
        // No path configuration -> FALTA_CONFIGURACION_RUTA_ARCHIVO.
        Contexto x = ejecutar("CTA-B-153_reporte-antiguedad-bolsa-sin-configuracion-de-ruta");
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
    @DisplayName("CTA-B-154: missing broker code")
    void ctaB154_reporteAntiguedadBolsaSinCodigoDeCorredora() {
        // GenerarReporteAntiguedadDeudaBolsaCTA.java:151
        // No broker code -> CODIGO_CORREDOR_NO_EXISTE.
        Contexto x = ejecutar("CTA-B-154_reporte-antiguedad-bolsa-sin-codigo-de-corredora");
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
    @DisplayName("CTA-B-155: missing file format")
    void ctaB155_reporteAntiguedadBolsaSinFormatoDeArchivo() {
        // GenerarReporteAntiguedadDeudaBolsaCTA.java:210
        // No layout -> FALTA_CONFIGURACION_ARCHIVO_FORMATO.
        Contexto x = ejecutar("CTA-B-155_reporte-antiguedad-bolsa-sin-formato-de-archivo");
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
    @DisplayName("CTA-B-156: query error still writes a header only workbook")
    void ctaB156_reporteAntiguedadBolsaErrorDeLaConsultaGeneraIgualUnArchivoConSoloCabecera() {
        // GenerarReporteAntiguedadDeudaBolsaCTA.java:160, :215
        // Same quirk: the underlying query returns SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO, messages returned, header-only
        // workbook still written.
        Contexto x = ejecutar("CTA-B-156_reporte-antiguedad-bolsa-error-de-la-consulta-genera-igual-un-archivo-con-solo-cabecera");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteBolsa_E.xlsx");
        verificar(x);
    }
}
