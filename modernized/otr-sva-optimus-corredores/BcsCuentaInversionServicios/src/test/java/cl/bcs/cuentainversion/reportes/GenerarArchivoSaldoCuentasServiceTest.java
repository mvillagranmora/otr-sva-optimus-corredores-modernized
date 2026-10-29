package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.GrabadorTablaPdf;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarArchivoSaldoCuentasService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarArchivoSaldoCuentas} (legacy/.../cuentaInversion/GenerarArchivoSaldoCuentas.java) against the modern {@code GenerarArchivoSaldoCuentasService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarArchivoSaldoCuentasServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-131_saldo-cuentas-archivo-zip-con-csv-y-pdf-columnas-por-defecto",
            "CTA-B-132_saldo-cuentas-encabezados-a-excluir",
            "CTA-B-133_saldo-cuentas-sin-configuracion-de-ruta",
            "CTA-B-134_saldo-cuentas-sin-movimientos-para-generar-el-archivo",
            "CTA-B-135_saldo-cuentas-error-del-servicio-de-saldos",
            "CTA-B-136_saldo-cuentas-pasa-los-filtros-al-servicio-de-saldos");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado, GrabadorTablaPdf pdf) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        GrabadorTablaPdf pdf = new GrabadorTablaPdf();
        Resultado r = caso.ejecutar(new GenerarArchivoSaldoCuentasService(caso.jdbc(), colab, pdf)::execute);
        EJECUTADOS.incrementAndGet();
        return new Contexto(caso, colab, r, pdf);
    }

    private static void verificar(Contexto x) {
        x.caso().assertEquivalente(x.resultado());
        PendienteSupport.assertLlamadas(x.colaboradores(), x.caso());
        PendienteSupport.assertArchivos(x.caso());
        assertPdfTextos(x);
    }

    /** The texts the PDF shows (recorded "pdfTextos" of the golden; a case without them shows no text). */
    private static void assertPdfTextos(Contexto x) {
        List<String> esperado = new java.util.ArrayList<>();
        if (x.caso().esperado().has("pdfTextos")) {
            x.caso().esperado().getAsJsonArray("pdfTextos").forEach(e -> esperado.add(e.getAsString()));
        }
        assertThat(x.pdf().textos()).as(x.caso().nombre() + " pdfTextos").isEqualTo(esperado);
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
        System.out.println("equivalence cases executed (GenerarArchivoSaldoCuentas): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-131: zip with CSV and PDF, default columns")
    void ctaB131_saldoCuentasArchivoZipConCsvYPdfColumnasPorDefecto() {
        // GenerarArchivoSaldoCuentas.java:335-435 (CSV), :203 logica
        // No p_Encabezados: all 21 columns, header names from the dictionary, one line per balance; amounts through
        // MonedaFormato (CLP truncated to 0 decimals, USD rounded to 2) with decimal COMMA; FechaUltimoMovimiento 0001-01-01 is
        // blank; the CSV (UTF-8 BOM, ';' separator, no quotes) and a PDF go in <name>.zip under RutaBase+RutaCta. The PDF is
        // replaced by a shim: only the texts shown are pinned (pdfTextos, numbers re-formatted as #.##0,##).
        Contexto x = ejecutar("CTA-B-131_saldo-cuentas-archivo-zip-con-csv-y-pdf-columnas-por-defecto");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/SaldoCuentas_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-132: headers to exclude")
    void ctaB132_saldoCuentasEncabezadosAExcluir() {
        // GenerarArchivoSaldoCuentas.java:335, :385
        // p_Encabezados lists the columns to LEAVE OUT (matched upper-case and accent-free; 'codigo portafolio' and
        // 'MoneyMarket' are translated to the dictionary names of Portafolio and Money Market). QUIRK: the remaining header line
        // gets no separator after the last name and the data lines use String.valueOf for amounts before formatting.
        Contexto x = ejecutar("CTA-B-132_saldo-cuentas-encabezados-a-excluir");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/SaldoCuentas_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-133: missing path configuration")
    void ctaB133_saldoCuentasSinConfiguracionDeRuta() {
        // GenerarArchivoSaldoCuentas.java:234 (FALTA_CONFIGURACION_RUTA_ARCHIVO)
        // No RutaArchivosCta / PLA.RutaBaseArchivos configuration -> ERROR FALTA_CONFIGURACION_RUTA_ARCHIVO; no balance request
        // is made.
        Contexto x = ejecutar("CTA-B-133_saldo-cuentas-sin-configuracion-de-ruta");
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
    @DisplayName("CTA-B-134: no movements to generate the file")
    void ctaB134_saldoCuentasSinMovimientosParaGenerarElArchivo() {
        // GenerarArchivoSaldoCuentas.java (NO_EXISTEN_MOVIMIENTOS)
        // ConsultarSaldo returns no rows -> ERROR NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO and no file.
        Contexto x = ejecutar("CTA-B-134_saldo-cuentas-sin-movimientos-para-generar-el-archivo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-135: balance service error")
    void ctaB135_saldoCuentasErrorDelServicioDeSaldos() {
        // GenerarArchivoSaldoCuentas.java (error of ConsultarSaldoCuentaInversion)
        // ConsultarSaldo reports errors -> ERROR_EN_GENERACION_ARCHIVO, no file.
        Contexto x = ejecutar("CTA-B-135_saldo-cuentas-error-del-servicio-de-saldos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_EN_GENERACION_ARCHIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-136: passes the filters to the balance service")
    void ctaB136_saldoCuentasPasaLosFiltrosAlServicioDeSaldos() {
        // GenerarArchivoSaldoCuentas.java:203-335
        // Pins the request sent to ConsultarSaldoCuentaInversion (llamadasColaboradores): negative / available /
        // available-negative filters, APV and other balances flags, executive, currency (also inside CuentaInversionAConsultar),
        // date range, UsaReglasNegocio S, client/portfolio/product null.
        Contexto x = ejecutar("CTA-B-136_saldo-cuentas-pasa-los-filtros-al-servicio-de-saldos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/SaldoCuentas_<AHORA>.zip");
        verificar(x);
    }
}
