package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.GrabadorPdf;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarArchivoCuentasPorCobrarAnexo16Service;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarArchivoCuentasPorCobrarAnexo16} (legacy/.../cuentaInversion/GenerarArchivoCuentasPorCobrarAnexo16.java) against the modern {@code GenerarArchivoCuentasPorCobrarAnexo16Service}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarArchivoCuentasPorCobrarAnexo16ServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-201_anexo16-zip-con-xls-y-pdf-agrupado-por-cliente",
            "CTA-B-202_anexo16-sin-saldos-negativos",
            "CTA-B-203_anexo16-solo-positivos-o-cpro-genera-reporte-vacio",
            "CTA-B-204_anexo16-ruta-inexistente-lanza-excepcion",
            "CTA-B-205_anexo16-sin-logo-lanza-excepcion",
            "CTA-B-206_anexo16-logo-que-no-es-imagen-lanza-excepcion",
            "CTA-B-207_anexo16-saldo-inicial-negativo-deuda-mas-antigua-de-30-dias",
            "CTA-B-208_anexo16-pago-total-y-nueva-deuda",
            "CTA-B-209_anexo16-movimiento-de-otro-tipo-no-mueve-el-saldo",
            "CTA-B-210_anexo16-cargo-de-cuenta-sin-cuenta-por-cobrar-lanza-excepcion",
            "CTA-B-211_anexo16-paridad-no-disponible-deja-la-deuda-en-cero",
            "CTA-B-212_anexo16-fecha-invalida-usa-fecha-segura-1900",
            "CTA-B-225_anexo16-limites-de-los-tramos-por-dias-sin-movimientos");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, GrabadorPdf pdf, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        GrabadorPdf pdf = new GrabadorPdf();
        Resultado r = caso.ejecutar(new GenerarArchivoCuentasPorCobrarAnexo16Service(caso.jdbc(), colab, pdf)::execute);
        EJECUTADOS.incrementAndGet();
        return new Contexto(caso, colab, pdf, r);
    }

    private static void verificar(Contexto x) {
        x.caso().assertEquivalente(x.resultado());
        PendienteSupport.assertLlamadas(x.colaboradores(), x.caso());
        PendienteSupport.assertArchivos(x.caso());
        assertPdfTextos(x);
    }

    /**
     * The texts the PDF shows (recorded "pdfTextos" of the golden): table shape, cells and page header/footer; the only masked value is
     * the wall clock of the header cell ("Fecha: dd-MM-yyyy / Hora: HH:mm").
     */
    private static void assertPdfTextos(Contexto x) {
        List<String> esperado = new java.util.ArrayList<>();
        if (x.caso().esperado().has("pdfTextos")) {
            x.caso().esperado().getAsJsonArray("pdfTextos").forEach(e -> esperado.add(enmascarar(e.getAsString())));
        }
        List<String> actual = x.pdf().textos().stream().map(GenerarArchivoCuentasPorCobrarAnexo16ServiceTest::enmascarar).toList();
        assertThat(actual).as(x.caso().nombre() + " pdfTextos").isEqualTo(esperado);
    }

    private static String enmascarar(String linea) {
        return linea.replaceAll("(?s)(: Fecha: )\\d{2}-\\d{2}-\\d{4}\\nHora: \\d{2}:\\d{2}", "$1<FECHA>");
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
        System.out.println("equivalence cases executed (GenerarArchivoCuentasPorCobrarAnexo16): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-201: zip with xls and pdf, debts aged by tranche and grouped by client")
    void ctaB201_anexo16ZipConXlsYPdfAgrupadoPorCliente() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:261-335 (logica), :487-611 (clasificarTramosDeuda), :816-861 (agruparCuentasPorCliente)
        // Negative balances (ConsultarSaldo, p_SoloSaldoNegativo=S) are aged by effective date of the movements of the last 30
        // days: <=2 days vigente, 3-10 segundo tramo, 11-30 tercer tramo, >30 cuarto tramo. 4001: charge 1500 (03-05) then
        // payment 500 (03-13) -> 1000 in segundo tramo (payments consume the oldest debt first); 4002 USD -50 (no movements) is
        // converted with CalcularParidad (USD>CLP 950) per tramo; 4003 (last movement 41 days ago) goes to cuarto tramo; amounts
        // are grouped by client (absolute values) and the related-party flag/description comes from ConsultarRelacionCorredora.
        // Output: zip <RutaBase><RutaCta>Anexo16_<ts>.zip with reporteAnexo16_<date>.xls and .pdf; P_OUT_RUTAARCHIVO = RutaCta +
        // name. The PDF is a shim: only the texts and the table structure are pinned (pdfTextos). The page event runs once
        // (single page assumed).
        Contexto x = ejecutar("CTA-B-201_anexo16-zip-con-xls-y-pdf-agrupado-por-cliente");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-202: no negative balances")
    void ctaB202_anexo16SinSaldosNegativos() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:407
        // ConsultarSaldo answers no balance -> NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO and nothing else is queried or
        // written.
        Contexto x = ejecutar("CTA-B-202_anexo16-sin-saldos-negativos");
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
    @DisplayName("CTA-B-203: only positive or CPRO rows: an empty report is generated (quirk)")
    void ctaB203_anexo16SoloPositivosOCproGeneraReporteVacio() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:417
        // LEGACY QUIRK: rows with balance >= 0 or Relacion CPRO are skipped; when none is left the report is NOT rejected: an
        // xls with header only and a pdf with an empty table are zipped and returned.
        Contexto x = ejecutar("CTA-B-203_anexo16-solo-positivos-o-cpro-genera-reporte-vacio");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-204: unwritable directory throws NoSuchFileException")
    void ctaB204_anexo16RutaInexistenteLanzaExcepcion() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:1101
        // The target directory does not exist: Files.write throws NoSuchFileException, nothing catches it. (The legacy
        // FALTA_CONFIGURACION_RUTA_ARCHIVO branch can never fire: the configuration query uses MAX() and always answers one row;
        // with no configuration at all the zip would be written to the working directory.)
        Contexto x = ejecutar("CTA-B-204_anexo16-ruta-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.nio.file.NoSuchFileException").isEqualTo("java.nio.file.NoSuchFileException");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-205: missing logo throws IndexOutOfBounds (defect)")
    void ctaB205_anexo16SinLogoLanzaExcepcion() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:1114
        // LEGACY DEFECT: obtenerLogoCorredora tests 'imagenes != null || !imagenes.isEmpty()' (always true) and calls get(0) ->
        // IndexOutOfBoundsException when there is no LOGO_CORREDOR image. The xls was already built in memory; nothing is
        // written.
        Contexto x = ejecutar("CTA-B-205_anexo16-sin-logo-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.IndexOutOfBoundsException").isEqualTo("java.lang.IndexOutOfBoundsException");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-206: logo that is not an image throws IOException")
    void ctaB206_anexo16LogoQueNoEsImagenLanzaExcepcion() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:1116
        // The logo bytes are not an image: Image.getInstance throws IOException ('The byte array is not a recognized
        // imageformat.' in iText 5; the shim reproduces the check on the magic number only).
        Contexto x = ejecutar("CTA-B-206_anexo16-logo-que-no-es-imagen-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.io.IOException").isEqualTo("java.io.IOException");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-207: opening negative balance becomes a debt older than 30 days")
    void ctaB207_anexo16SaldoInicialNegativoDeudaMasAntiguaDe30Dias() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:527-534
        // Opening balance -700 before the 30 day window becomes a debt dated 40 days back (cuarto tramo); the payment of 200 on
        // 03-12 consumes it: 500 stays in cuarto tramo.
        Contexto x = ejecutar("CTA-B-207_anexo16-saldo-inicial-negativo-deuda-mas-antigua-de-30-dias");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(2);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-208: full payment then a new charge")
    void ctaB208_anexo16PagoTotalYNuevaDeuda() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:538-563
        // Charge 100 (03-02), payment 100 (03-04) clears it, new charge 40 (03-12, 2 days -> vigente): only 40 vigente remains.
        Contexto x = ejecutar("CTA-B-208_anexo16-pago-total-y-nueva-deuda");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(2);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-209: unknown movement type has delta 0")
    void ctaB209_anexo16MovimientoDeOtroTipoNoMueveElSaldo() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:1155-1160
        // clasificaCargoAbono only knows MOVABN (+) and MOVCAR (-); any other type has delta 0: 300 stays in segundo tramo.
        Contexto x = ejecutar("CTA-B-209_anexo16-movimiento-de-otro-tipo-no-mueve-el-saldo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(2);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-210: movements of an account without a receivable throw NullPointerException (defect)")
    void ctaB210_anexo16CargoDeCuentaSinCuentaPorCobrarLanzaExcepcion() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:693
        // LEGACY DEFECT: accounts are matched by IdCliente_IdCuentaInversion_IdMoneda; when the currency of the movements view
        // differs from the one of the balance row, the movement account has no counterpart and the merge dereferences null ->
        // NullPointerException.
        Contexto x = ejecutar("CTA-B-210_anexo16-cargo-de-cuenta-sin-cuenta-por-cobrar-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-211: no rate: the foreign debt becomes zero")
    void ctaB211_anexo16ParidadNoDisponibleDejaLaDeudaEnCero() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:792
        // No USD>CLP rate: CalcularParidad answers MontoLocal null and the legacy replaces the tramo amount by ZERO: the USD
        // debt of 4002 disappears from client 1001 (only 4001 remains).
        Contexto x = ejecutar("CTA-B-211_anexo16-paridad-no-disponible-deja-la-deuda-en-cero");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-212: invalid date falls back to 1900-01-01")
    void ctaB212_anexo16FechaInvalidaUsaFechaSegura1900() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:1194-1198
        // LEGACY QUIRK: an unparseable p_FechaHasta becomes 1900-01-01 (convertirFecha); the zip entries are named
        // reporteAnexo16_1900-01-01 and every date difference is negative (clamped to 0): everything is vigente.
        Contexto x = ejecutar("CTA-B-212_anexo16-fecha-invalida-usa-fecha-segura-1900");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(6);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-225: boundaries of the vigente / 3-10 / 11-30 / >30 day tranches without movements")
    void ctaB225_anexo16LimitesDeLosTramosPorDiasSinMovimientos() {
        // GenerarArchivoCuentasPorCobrarAnexo16.java:613-637 (clasificarTramosDeudas), :639-707
        // Boundaries of the tranches for accounts without movements in the last 30 days (days between p_FechaHasta and the last
        // movement): 2 days -> vigente, 3 -> segundo tramo, 10 -> segundo, 11 -> tercer tramo, 30 -> tercer, 31 -> cuarto tramo.
        // Client 1001: 100 vigente + 200 segundo; 1002: 300 segundo + 400 tercer (USD at rate 1); 1003: 500 tercer + 600 cuarto.
        Contexto x = ejecutar("CTA-B-225_anexo16-limites-de-los-tramos-por-dias-sin-movimientos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso")).as("rows of GenerarArchivoCuentasPorCobrarAnexo16_Paso").hasSize(11);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/Anexo16_<AHORA>.zip");
        verificar(x);
    }
}
