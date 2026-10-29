package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.envio.support.CasoEnvio;
import cl.bcs.contabilidad.envio.support.Fakes;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import cl.bcs.contabilidad.service.EnvioContabilidadService;
import cl.bcs.contabilidad.service.ReversaPreComprobanteService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.contabilidad.ReversaPreComprobante} (ReversaPreComprobante.java:134-258)
 * as {@link ReversaPreComprobanteService#execute(JsonObject)}: copies a pre-voucher with debit/credit swapped
 * (the swap is done by ReversaPreComprobante_SelectPrecomprobanteAsiento.sql), inserts header, lines and events,
 * then runs EnvioContabilidad on the new ids with p_UsaReglasNegocio=S, p_Destino=B and merges its counter/messages.
 *
 * <p>The REAL EnvioContabilidadService is wired in (as legacy calls EnvioContabilidad.logica directly), so the
 * goldens also pin the staging rows produced for the reversal. Sequences and clock are deterministic fakes that
 * mirror the oracle shims (IdPreComprobante from 5000, IdPreComprobanteAsiento from 9000, 2025-04-01 10:15:30).
 */
class ReversaPreComprobanteServiceTest {

    static final List<String> CASOS = List.of(
            "CONT-B-30_reversa-nueva-secuencia",
            "CONT-B-31_reversa-ya-reversada-e-id-explicito",
            "CONT-B-32_reversa-montos-ambos-lados",
            "CONT-B-33_reversa-inexistente",
            "CONT-B-34_reversa-vacia",
            "CONT-B-35_reversa-duplicada");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    private static final String[] TABLAS = {
            "PreComprobante", "PreComprobanteAsiento", "EventoPreComprobanteAsiento",
            "PreComprobanteEnvioContabilidad", "PreComprobanteAsientoEnvioContabilidad", "PreComprobanteCentralizacionTipo"};

    record Resultado(JsonObject salida, JsonObject tablas, Fakes.Secuencias secuencias, CasoEnvio caso) {
    }

    static Resultado ejecutar(String nombre) {
        CasoEnvio caso = CasoEnvio.cargar(nombre);
        EnvioContabilidadService envio = new EnvioContabilidadService(caso.jdbc(), Fakes.planCuentas(caso.planCuentas()),
                Fakes.plataforma(caso.config()), Fakes.diccionario());
        Fakes.Secuencias secuencias = new Fakes.Secuencias();
        ReversaPreComprobanteService service = new ReversaPreComprobanteService(caso.jdbc(), secuencias, Fakes.reloj(),
                Fakes.diccionario(), envio);

        JsonObject salida = service.execute(caso.entrada().deepCopy());

        JsonObject tablas = caso.tablasEnvio();
        tablas.add("PreComprobante", caso.tabla("BSCL.Contabilidad.BD::ITZ.PreComprobante", Set.of(), "\"IdPreComprobante\""));
        tablas.add("PreComprobanteAsiento", caso.tabla("BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento", Set.of(), "\"IdPreComprobanteAsiento\""));
        tablas.add("EventoPreComprobanteAsiento", caso.tabla("BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento", Set.of(),
                "\"IdPreComprobanteAsiento\", \"IdEvento\""));
        return new Resultado(salida, tablas, secuencias, caso);
    }

    static Resultado ejecutarYComparar(String nombre) {
        Resultado r = ejecutar(nombre);
        JsonCanon.assertEquivalente(nombre + " salida", r.caso().esperado().get("salida"), r.salida());
        compararTablas(r);
        EJECUTADOS.incrementAndGet();
        return r;
    }

    static void compararTablas(Resultado r) {
        JsonObject esperadas = r.caso().esperado().getAsJsonObject("tablas");
        for (String t : TABLAS) {
            assertThat(filas(r.tablas().getAsJsonArray(t))).as(r.caso().nombre() + " rows of " + t)
                    .isEqualTo(filas(esperadas.getAsJsonArray(t)));
        }
    }

    private static List<String> filas(JsonArray rows) {
        List<String> out = new ArrayList<>();
        rows.forEach(x -> out.add(JsonCanon.canon(x, Set.of()).toString()));
        out.sort(String::compareTo);
        return out;
    }

    private static JsonObject fila(JsonArray rows, String col, long valor) {
        for (JsonElement e : rows) {
            if (e.getAsJsonObject().get(col).getAsLong() == valor) {
                return e.getAsJsonObject();
            }
        }
        throw new AssertionError("no row " + col + "=" + valor + " in " + rows);
    }

    @Test
    @DisplayName("CONT-B-30: IdPreComprobante=0 -> new id from sequence; header copied with Glosa 'REVERSA PRE-COMPROBANTE N° <ant>', IdReversa, Estado V, new FechaContable/FechaProceso/HoraProceso/CodigoUsuario; lines swapped; events copied; then staged")
    void contB30_reversaConNuevaSecuencia() {
        // ReversaPreComprobante.java:183-184 (sequences), :191-209 (header), :211-220 (lines, Glosa ' CUENTA N° '),
        // :221-228 (events, Operador NOT flipped), :233-235 (inserts), :237-252 (EnvioContabilidad S/B, counters merged).
        Resultado r = ejecutarYComparar("CONT-B-30_reversa-nueva-secuencia");
        JsonObject cab = fila(r.tablas().getAsJsonArray("PreComprobante"), "IdPreComprobante", 5000);
        assertThat(cab.get("Glosa").getAsString()).isEqualTo("REVERSA PRE-COMPROBANTE N° 100");
        assertThat(cab.get("IdReversa").getAsLong()).isEqualTo(100L);
        assertThat(cab.get("Estado").getAsString()).isEqualTo("V");
        assertThat(cab.get("FechaContable").getAsString()).isEqualTo("2025-04-01");
        assertThat(cab.get("FechaProceso").getAsString()).isEqualTo("2025-04-01");
        assertThat(cab.get("HoraProceso").getAsString()).isEqualTo("10:15:30");
        assertThat(cab.get("CodigoUsuario").getAsString()).isEqualTo("usr.fake");
        assertThat(cab.get("FolioContabilidad").getAsLong()).as("folio of the ORIGINAL is copied").isEqualTo(55L);

        JsonObject l1 = fila(r.tablas().getAsJsonArray("PreComprobanteAsiento"), "IdPreComprobanteAsiento", 9000);
        assertThat(l1.get("MontoDebe").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(l1.get("MontoHaber").getAsBigDecimal()).isEqualByComparingTo("1500.50");
        assertThat(l1.get("Glosa").getAsString()).isEqualTo("REVERSA PRE-COMPROBANTE N° 100 CUENTA N° 1201");
        assertThat(l1.get("IdPreComprobante").getAsLong()).isEqualTo(5000L);

        JsonObject ev = fila(r.tablas().getAsJsonArray("EventoPreComprobanteAsiento"), "IdPreComprobanteAsiento", 9000);
        assertThat(ev.get("Operador").getAsString()).isEqualTo("+");
        assertThat(ev.get("IdEvento").getAsLong()).isEqualTo(501L);

        assertThat(r.tablas().getAsJsonArray("PreComprobanteAsientoEnvioContabilidad")).hasSize(2);
        assertThat(r.secuencias().llamadas()).containsExactly(Fakes.SEQ_PRECOMPROBANTE + "#1", Fakes.SEQ_PRECOMPROBANTE_ASIENTO + "#2");
    }

    @Test
    @DisplayName("CONT-B-31: an already-reversed id -> ERROR PRECOMPROBANTE_YA_ESTA_REVERSADO {IdPreComprobanteAnt} but P_OUT_ERRORESCONTADOR stays 0; explicit IdPreComprobante is used as-is")
    void contB31_yaReversada_eIdExplicito() {
        // ReversaPreComprobante.java:149-167 (only the EnvioContabilidad counter is added at :250), :195-200.
        Resultado r = ejecutarYComparar("CONT-B-31_reversa-ya-reversada-e-id-explicito");
        assertThat(r.salida().get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        JsonObject m = r.salida().getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject();
        assertThat(m.get("tipo").getAsString()).isEqualTo("ERROR");
        assertThat(m.get("codigo").getAsString()).isEqualTo("PRECOMPROBANTE_YA_ESTA_REVERSADO");
        assertThat(m.get("id").getAsLong()).isEqualTo(100L);
        assertThat(m.get("descripcion").getAsString()).isEqualTo("[PRECOMPROBANTE_YA_ESTA_REVERSADO]{IdPreComprobanteAnt=100}");
        assertThat(fila(r.tablas().getAsJsonArray("PreComprobante"), "IdPreComprobante", 6100).get("IdReversa").getAsLong()).isEqualTo(101L);
        assertThat(r.secuencias().llamadas()).doesNotContain(Fakes.SEQ_PRECOMPROBANTE + "#1");
    }

    @Test
    @DisplayName("CONT-B-32: swap SQL - Debe>0 & Haber>0 becomes 0/0, 0/0 stays 0/0, 0/7.5 becomes 7.5/0; only the non-zero reversed line is staged")
    void contB32_swapConAmbosLados() {
        // ReversaPreComprobante_SelectPrecomprobanteAsiento.sql:8-15; SelectAsientoProcesoResumido.sql:18-19.
        Resultado r = ejecutarYComparar("CONT-B-32_reversa-montos-ambos-lados");
        JsonArray pca = r.tablas().getAsJsonArray("PreComprobanteAsiento");
        assertThat(fila(pca, "IdPreComprobanteAsiento", 9000).get("MontoDebe").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(fila(pca, "IdPreComprobanteAsiento", 9000).get("MontoHaber").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(fila(pca, "IdPreComprobanteAsiento", 9002).get("MontoDebe").getAsBigDecimal()).isEqualByComparingTo("7.5");
        assertThat(r.tablas().getAsJsonArray("PreComprobanteAsientoEnvioContabilidad")).hasSize(1);
    }

    @Test
    @DisplayName("CONT-B-33: unknown IdPreComprobanteAnt -> no insert, no message, EnvioContabilidad not run, counter 0")
    void contB33_inexistente() {
        // ReversaPreComprobante.java:191 iterates DB rows only; :244 skips EnvioContabilidad when nothing was inserted.
        Resultado r = ejecutarYComparar("CONT-B-33_reversa-inexistente");
        assertThat(r.salida().getAsJsonArray("P_OUT_MENSAJES")).isEmpty();
        assertThat(r.tablas().getAsJsonArray("PreComprobante")).isEmpty();
    }

    @Test
    @DisplayName("CONT-B-34: empty p_IdPreComprobantes -> {P_OUT_ERRORESCONTADOR:0, P_OUT_MENSAJES:[]}, nothing written")
    void contB34_vacia() {
        Resultado r = ejecutarYComparar("CONT-B-34_reversa-vacia");
        assertThat(r.salida().get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    @DisplayName("CONT-B-35: same IdPreComprobanteAnt twice -> legacy throws 'key field is not unique' before any insert; modern must write nothing and report ERROR with counter 1")
    void contB35_duplicadaNoInsertaNada() {
        // ReversaPreComprobante.java:181 Utils.List2Hash throws on duplicate keys (BcsUtils Utils.java:777-779).
        Resultado r = ejecutar("CONT-B-35_reversa-duplicada");
        assertThat(r.caso().esperado().get("excepcion").getAsString()).contains("key field is not unique");
        compararTablas(r);
        assertThat(r.salida().get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(r.salida().getAsJsonArray("P_OUT_MENSAJES")).isNotEmpty();
        assertThat(r.salida().getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("tipo").getAsString()).isEqualTo("ERROR");
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void reportarEjecutados() {
        System.out.println("equivalence cases executed (ReversaPreComprobante): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed; zero means NOT PROVEN").isPositive();
    }
}
