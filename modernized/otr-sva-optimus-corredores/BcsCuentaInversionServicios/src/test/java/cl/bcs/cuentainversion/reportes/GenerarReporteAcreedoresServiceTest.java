package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarReporteAcreedoresService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarReporteAcreedores} (legacy/.../cuentaInversion/GenerarReporteAcreedores.java) against the modern {@code GenerarReporteAcreedoresService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarReporteAcreedoresServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-137_reporte-acreedores-xlsx-por-cuenta",
            "CTA-B-138_reporte-acreedores-xlsx-consolidado-sin-columna-de-portafolio",
            "CTA-B-139_reporte-acreedores-sin-configuracion-de-ruta",
            "CTA-B-140_reporte-acreedores-sin-codigo-de-corredora",
            "CTA-B-141_reporte-acreedores-codigo-de-corredora-de-4-digitos-no-es-valido",
            "CTA-B-142_reporte-acreedores-codigo-de-corredora-de-3-digitos-se-mantiene",
            "CTA-B-143_reporte-acreedores-sin-formato-de-archivo",
            "CTA-B-144_reporte-acreedores-error-de-la-consulta-genera-igual-un-archivo-con-solo-cabecera");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GenerarReporteAcreedoresService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (GenerarReporteAcreedores): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-137: xlsx by account")
    void ctaB137_reporteAcreedoresXlsxPorCuenta() {
        // GenerarReporteAcreedores.java:192-330 (XSSFWorkbook, cabecera/cuerpo)
        // Consolidar=N: one xlsx (<RutaBase><RutaCta><name>.xlsx), sheet '<codigo corredora>_ddMMyyyy_cuentas_por_cobrar' (code
        // 012 = 2-digit code padded with 0), header from the PLA.Formato layout (position and Spanish title per CodigoCampo),
        // one row per report row with the amounts as TEXT (toPlainString) and the portfolio code column; result carries the
        // module path and CODIGOCORREDOR_012.
        Contexto x = ejecutar("CTA-B-137_reporte-acreedores-xlsx-por-cuenta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAcreedores_N.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-138: xlsx consolidated without the portfolio column")
    void ctaB138_reporteAcreedoresXlsxConsolidadoSinColumnaDePortafolio() {
        // GenerarReporteAcreedores.java:282-330
        // Consolidar=S: the layout query drops RACREEDORES_CODIGOPORTAFOLIO and shifts the following positions by one; the
        // column widths depend on the shifted positions.
        Contexto x = ejecutar("CTA-B-138_reporte-acreedores-xlsx-consolidado-sin-columna-de-portafolio");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAcreedores_S.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-139: missing path configuration")
    void ctaB139_reporteAcreedoresSinConfiguracionDeRuta() {
        // GenerarReporteAcreedores.java:117 (FALTA_CONFIGURACION_RUTA_ARCHIVO)
        // No RutaArchivosCta / RutaBase -> FALTA_CONFIGURACION_RUTA_ARCHIVO; nothing else is attempted.
        Contexto x = ejecutar("CTA-B-139_reporte-acreedores-sin-configuracion-de-ruta");
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
    @DisplayName("CTA-B-140: missing broker code")
    void ctaB140_reporteAcreedoresSinCodigoDeCorredora() {
        // GenerarReporteAcreedores.java:125 (CODIGO_CORREDOR_NO_EXISTE)
        // No broker code row -> CODIGO_CORREDOR_NO_EXISTE.
        Contexto x = ejecutar("CTA-B-140_reporte-acreedores-sin-codigo-de-corredora");
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
    @DisplayName("CTA-B-141: 4 digit broker code is not valid")
    void ctaB141_reporteAcreedoresCodigoDeCorredoraDe4DigitosNoEsValido() {
        // GenerarReporteAcreedores.java:125-139
        // Broker codes of length 2 are zero padded, length 3 are kept, any other length becomes empty ->
        // CODIGO_CORREDOR_NO_EXISTE.
        Contexto x = ejecutar("CTA-B-141_reporte-acreedores-codigo-de-corredora-de-4-digitos-no-es-valido");
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
    @DisplayName("CTA-B-142: 3 digit broker code is kept")
    void ctaB142_reporteAcreedoresCodigoDeCorredoraDe3DigitosSeMantiene() {
        // GenerarReporteAcreedores.java:139 (CODIGOCORREDOR_ + code)
        // A 3 digit broker code (set to 123 by the seed) is used as is in the sheet name and in P_OUT_CODIGOCORREDOR.
        Contexto x = ejecutar("CTA-B-142_reporte-acreedores-codigo-de-corredora-de-3-digitos-se-mantiene");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAcreedores_3.xlsx");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-143: missing file format")
    void ctaB143_reporteAcreedoresSinFormatoDeArchivo() {
        // GenerarReporteAcreedores.java:187 (FALTA_CONFIGURACION_ARCHIVO_FORMATO)
        // No layout defined for REPORTE_ACREEDORES -> FALTA_CONFIGURACION_ARCHIVO_FORMATO and no file.
        Contexto x = ejecutar("CTA-B-143_reporte-acreedores-sin-formato-de-archivo");
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
    @DisplayName("CTA-B-144: query error still writes a header only workbook")
    void ctaB144_reporteAcreedoresErrorDeLaConsultaGeneraIgualUnArchivoConSoloCabecera() {
        // GenerarReporteAcreedores.java:134, :192
        // LEGACY QUIRK: when the underlying report returns its error (SIN_CUENTAS_DE_INVERSION_SALDO_POSITIVO) the messages are
        // copied to the output but the workbook is still written with only the header and P_OUT_RUTAARCHIVO /
        // P_OUT_CODIGOCORREDOR are filled in.
        Contexto x = ejecutar("CTA-B-144_reporte-acreedores-error-de-la-consulta-genera-igual-un-archivo-con-solo-cabecera");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("SIN_CUENTAS_DE_INVERSION_SALDO_POSITIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ReporteAcreedores_E.xlsx");
        verificar(x);
    }
}
