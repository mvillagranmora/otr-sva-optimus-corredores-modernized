package cl.bcs.contabilidad.jobs.adapter;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort.ClearingFinancieroDetalle;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEePlataformaClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Step 5, no Spring: {@link PlataformaEnProceso} answers Tesoreria's {@code ConsultarClearingFinanciero} through the in-process entry point (no HTTP) and
 * hands every other port method to the existing port. The rows are the real ones of golden TES-B-23 of BcsTesoreriaServicios (copied byte for byte).
 */
class PlataformaEnProcesoTest {

    static final String CLEARING = PlataformaEnProceso.CLEARING_FINANCIERO;

    /** The HTTP port that must stay untouched: records every method called. */
    static final class HttpRegistrador implements PlataformaPort {
        final List<String> llamadas = new CopyOnWriteArrayList<>();

        @Override
        public EmpresaInfo consultarEmpresa(Long idEmpresa) {
            llamadas.add("consultarEmpresa");
            return null;
        }

        @Override
        public String obtenerDiaAnterior(Long idPais, String fechaContable) {
            llamadas.add("obtenerDiaAnterior");
            return "2025-03-31";
        }

        @Override
        public String obtenerFechaSistema(Long idEmpresa) {
            llamadas.add("obtenerFechaSistema");
            return "2025-04-01";
        }

        @Override
        public String obtenerLocalizacionSistema(Long idEmpresa) {
            llamadas.add("obtenerLocalizacionSistema");
            return "CL";
        }

        @Override
        public String obtenerMonedaSistema(Long idEmpresa) {
            llamadas.add("obtenerMonedaSistema");
            return "CLP";
        }

        @Override
        public String obtenerParametroConfiguracion(Long idEmpresa, String modulo, String parametro, String valorDefault) {
            llamadas.add("obtenerParametroConfiguracion");
            return valorDefault;
        }

        @Override
        public List<ClearingFinancieroDetalle> consultarClearingFinanciero(Long a, Long b, String c, String d) {
            llamadas.add("consultarClearingFinanciero");
            return List.of();
        }
    }

    static final DataSourceTransactionManager TRANSACCIONES = new DataSourceTransactionManager(
            new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:mem:s5tm;DB_CLOSE_DELAY=-1", "sa", ""));

    static PlataformaEnProceso nuevo(PlataformaPort http, DespachadorEnProceso despachador) {
        return new PlataformaEnProceso(http, despachador, TRANSACCIONES);
    }

    static JsonObject salidaDorada() {
        try (InputStream in = PlataformaEnProcesoTest.class.getResourceAsStream("/contabilidad-jobs/enproceso/TES-B-23.expected.json")) {
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("salida");
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    static PlataformaEnProceso puerto(HttpRegistrador http, List<JsonObject> recibidas, JsonObject respuesta) {
        EntradaEnProceso tesoreria = EntradaEnProceso.de("tesoreria", CLEARING::equals, (clase, entrada) -> {
            recibidas.add(entrada);
            return respuesta;
        });
        return nuevo(http, new DespachadorEnProceso(List.of(tesoreria)));
    }

    @Test
    @DisplayName("S5-01: the clearing runs in process, with the request the legacy built, and no HTTP method is touched")
    void elClearingSeResuelveEnProcesoSinHttp() {
        HttpRegistrador http = new HttpRegistrador();
        List<JsonObject> recibidas = new ArrayList<>();
        PlataformaEnProceso puerto = puerto(http, recibidas, salidaDorada());

        List<ClearingFinancieroDetalle> filas = puerto.consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01");

        assertThat(http.llamadas).isEmpty();
        assertThat(filas).hasSize(9);
        assertThat(recibidas).hasSize(1);
        JsonObject entrada = recibidas.get(0);
        // legacy CentralizaClearingFinanciero.java:44-50
        assertThat(entrada.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(entrada.get("p_IdUsuario").getAsLong()).isEqualTo(5L);
        assertThat(entrada.get("p_IdModulo").getAsString()).isEqualTo("CON");
        assertThat(entrada.get("p_UsaReglasNegocio").getAsString()).isEqualTo("S");
        assertThat(entrada.get("p_FechaLiquidacion").getAsString()).isEqualTo("2025-04-01");
        List<String> camaras = new ArrayList<>();
        entrada.getAsJsonArray("p_Camaras").forEach(c -> camaras.add(c.getAsJsonObject().get("Camara").getAsString()));
        assertThat(camaras).containsExactly("PM", "RV", "SM", "PH", "HS");
    }

    @Test
    @DisplayName("S5-02: the in-process rows equal the rows the HTTP adapter builds from the same detail (amounts compared by value)")
    void lasFilasEnProcesoEquivalenAlAdaptadorHttp() throws IOException {
        JsonObject salida = salidaDorada();
        JsonArray detalle = salida.getAsJsonArray("P_OUT_CLEARINGFINANCIERODETALLE");
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/api/tesoreria/ConsultarClearingFinanciero", ex -> {
            byte[] cuerpo = detalle.toString().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, cuerpo.length);
            ex.getResponseBody().write(cuerpo);
            ex.close();
        });
        servidor.start();
        try {
            NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(
                    new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:mem:s5equiv", "sa", ""));
            List<ClearingFinancieroDetalle> porHttp = new TomEePlataformaClient("http://127.0.0.1:" + servidor.getAddress().getPort(), jdbc)
                    .consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01");
            List<ClearingFinancieroDetalle> enProceso = puerto(new HttpRegistrador(), new ArrayList<>(), salida)
                    .consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01");

            assertThat(enProceso).hasSameSizeAs(porHttp).hasSize(9);
            for (int i = 0; i < porHttp.size(); i++) {
                ClearingFinancieroDetalle h = porHttp.get(i);
                ClearingFinancieroDetalle p = enProceso.get(i);
                assertThat(p.idTransaccion()).isEqualTo(h.idTransaccion());
                assertThat(p.idComprobanteTesoreria()).isEqualTo(h.idComprobanteTesoreria());
                assertThat(p.folioTransaccion()).isEqualTo(h.folioTransaccion());
                assertThat(p.totalVenta()).isEqualByComparingTo(h.totalVenta());
                assertThat(p.totalCompra()).isEqualByComparingTo(h.totalCompra());
                assertThat(p.idMonedaTransaccion()).isEqualTo(h.idMonedaTransaccion());
                assertThat(p.rutContraparte()).isEqualTo(h.rutContraparte());
                assertThat(p.razonSocialContraparte()).isEqualTo(h.razonSocialContraparte());
                assertThat(p.compraVenta()).isEqualTo(h.compraVenta());
                assertThat(p.idClasificacionTipoInstrumento()).isEqualTo(h.idClasificacionTipoInstrumento());
                assertThat(p.nombreGrupoCamara()).isEqualTo(h.nombreGrupoCamara());
                assertThat(p.idCondicionDeLiquidacion()).isEqualTo(h.idCondicionDeLiquidacion());
                assertThat(p.idBolsa()).isEqualTo(h.idBolsa());
                assertThat(p.fechaLiquidacion()).isEqualTo(h.fechaLiquidacion());
            }
            assertThat(enProceso.get(0).idTransaccion()).isEqualTo(201L);
            assertThat(enProceso.get(0).totalCompra()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(enProceso.get(0).folioTransaccion()).isEqualTo("F-201");
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    @DisplayName("S5-03: null and absent values become 0 / empty text / zero, like the HTTP adapter; no detail means an empty list")
    void nulosYAusentes() {
        JsonObject salida = JsonParser.parseString("{\"P_OUT_CLEARINGFINANCIERODETALLE\":[{\"IdTransaccion\":7,\"IdComprobanteTesoreria\":null,"
                + "\"TotalVenta\":null,\"FolioTransaccion\":null}],\"P_OUT_MENSAJES\":[]}").getAsJsonObject();
        ClearingFinancieroDetalle fila = puerto(new HttpRegistrador(), new ArrayList<>(), salida)
                .consultarClearingFinanciero(1L, 5L, null, "2025-04-01").get(0);
        assertThat(fila.idTransaccion()).isEqualTo(7L);
        assertThat(fila.idComprobanteTesoreria()).isZero();
        assertThat(fila.totalVenta()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fila.totalCompra()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fila.folioTransaccion()).isEmpty();
        assertThat(fila.rutContraparte()).isEmpty();

        assertThat(puerto(new HttpRegistrador(), new ArrayList<>(), new JsonObject())
                .consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01")).isEmpty();
    }

    @Test
    @DisplayName("S5-04: a null module is sent as empty text, as the HTTP adapter does")
    void moduloNuloSeEnviaVacio() {
        List<JsonObject> recibidas = new ArrayList<>();
        puerto(new HttpRegistrador(), recibidas, new JsonObject()).consultarClearingFinanciero(1L, 5L, null, "2025-04-01");
        assertThat(recibidas.get(0).get("p_IdModulo").getAsString()).isEmpty();
    }

    @Test
    @DisplayName("S5-05: every other port method (company, parameters, ObtenerDiaAnterior) goes to the existing port, with the arguments unchanged")
    void losDemasMetodosVanAlPuertoExistente() {
        HttpRegistrador http = new HttpRegistrador();
        PlataformaEnProceso puerto = puerto(http, new ArrayList<>(), new JsonObject());
        assertThat(puerto.obtenerDiaAnterior(56L, "2025-04-01")).isEqualTo("2025-03-31");
        assertThat(puerto.obtenerFechaSistema(1L)).isEqualTo("2025-04-01");
        assertThat(puerto.obtenerLocalizacionSistema(1L)).isEqualTo("CL");
        assertThat(puerto.obtenerMonedaSistema(1L)).isEqualTo("CLP");
        assertThat(puerto.obtenerParametroConfiguracion(1L, "TESORERIA", "X", "dflt")).isEqualTo("dflt");
        assertThat(puerto.consultarEmpresa(1L)).isNull();
        assertThat(http.llamadas).containsExactly("obtenerDiaAnterior", "obtenerFechaSistema", "obtenerLocalizacionSistema",
                "obtenerMonedaSistema", "obtenerParametroConfiguracion", "consultarEmpresa");
    }

    @Test
    @DisplayName("S5-06: when no in-process owner serves the clearing, the existing HTTP port answers it")
    void sinPropietarioVaPorHttp() {
        HttpRegistrador http = new HttpRegistrador();
        PlataformaEnProceso puerto = nuevo(http, new DespachadorEnProceso(List.of(
                EntradaEnProceso.de("otro", c -> false, (c, e) -> new JsonObject()))));
        assertThat(puerto.consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01")).isEmpty();
        assertThat(http.llamadas).containsExactly("consultarClearingFinanciero");
    }

    @Test
    @DisplayName("S5-07: a runtime exception of the service reaches the caller unchanged (the job rolls back); a checked one is wrapped with its cause")
    void excepcionesSePropagan() {
        RuntimeException npe = new NullPointerException("legacy TES-B-45");
        PlataformaEnProceso lanzaRuntime = nuevo(new HttpRegistrador(), new DespachadorEnProceso(List.of(
                EntradaEnProceso.de("tesoreria", CLEARING::equals, (c, e) -> {
                    throw npe;
                }))));
        assertThatThrownBy(() -> lanzaRuntime.consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01")).isSameAs(npe);

        Exception comprobada = new Exception("checked");
        PlataformaEnProceso lanzaComprobada = nuevo(new HttpRegistrador(), new DespachadorEnProceso(List.of(
                EntradaEnProceso.de("tesoreria", CLEARING::equals, (c, e) -> {
                    throw comprobada;
                }))));
        assertThatThrownBy(() -> lanzaComprobada.consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01"))
                .isInstanceOf(IllegalStateException.class).hasCause(comprobada);
    }
}
