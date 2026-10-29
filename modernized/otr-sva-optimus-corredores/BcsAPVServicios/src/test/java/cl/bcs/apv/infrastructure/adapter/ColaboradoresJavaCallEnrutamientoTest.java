package cl.bcs.apv.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

/**
 * Architecture review H1 of Tesoreria applied from the start: EVERY collaborator name the code passes to {@code invocar(...)} resolves explicitly to
 * the legacy fully qualified class, and an unknown name fails instead of travelling bare to the router. The other APV classes are NOT collaborators by
 * name (typed use cases), so none of them is in the table. The expected table below is written from the imports of the legacy sources
 * (legacy/.../BSCL/APV/JAVA/BcsAPVServicios), independently of the production map.
 */
class ColaboradoresJavaCallEnrutamientoTest {

    /** name -> legacy FQN of every class outside APV that the three families call. */
    private static final Map<String, String> REMOTOS = new TreeMap<>(Map.ofEntries(
            Map.entry("AgregarLog", "cl.bcs.plataforma.pla.AgregarLog"),
            Map.entry("RevisarRequisitosBasicos", "cl.bcs.plataforma.pla.RevisarRequisitosBasicos"),
            Map.entry("ConsultarEmpresa", "cl.bcs.plataforma.pla.utils.ConsultarEmpresa"),
            Map.entry("CrearEventoAutorizacionMasivo", "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo"),
            Map.entry("ConsultarInstrumentoPrecioBolsa", "cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa"),
            Map.entry("EntregarPreciosTasas", "cl.bcs.plataforma.tas.EntregarPreciosTasas"),
            Map.entry("CalcularParidad", "cl.bcs.plataforma.mer.CalcularParidad"),
            Map.entry("CrearIngresoEgreso", "cl.bcs.tesoreria.CrearIngresoEgreso"),
            Map.entry("LiquidarComprobante", "cl.bcs.tesoreria.LiquidarComprobante"),
            Map.entry("CrearComprobante", "cl.bcs.tesoreria.CrearComprobante"),
            Map.entry("AnulacionMandatoPAC", "cl.bcs.tesoreria.AnulacionMandatoPAC"),
            Map.entry("RevocacionMandatoPAC", "cl.bcs.tesoreria.RevocacionMandatoPAC"),
            Map.entry("IngresarMandatoPAC", "cl.bcs.tesoreria.IngresarMandatoPAC"),
            Map.entry("CrearCargoAbono", "cl.bcs.cuentaInversion.CrearCargoAbono"),
            Map.entry("ConsultarSaldoCuentaInversion", "cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion"),
            Map.entry("CrearSolicitudAporteRescateNormal", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal"),
            Map.entry("CalcularMontoNumeroCuota", "cl.bcs.fondomutuo.CalcularMontoNumeroCuota"),
            Map.entry("ConsultarCustodiaPortafolioFondoMutuo", "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo"),
            Map.entry("ConsultarCustodiaPortafolio", "cl.bcs.custodia.ConsultarCustodiaPortafolio"),
            Map.entry("CalcularMontoYCobrosOrden", "cl.bcs.ordenes.CalcularMontoYCobrosOrden"),
            Map.entry("CrearOrdenes", "cl.bcs.ordenes.CrearOrdenes"),
            Map.entry("ModificarPortafolio", "cl.bcs.portafolio.ModificarPortafolio"),
            Map.entry("CrearCliente", "cl.bcs.socionegocio.cli.CrearCliente"),
            Map.entry("CrearPersona", "cl.bcs.socionegocio.per.CrearPersona"),
            Map.entry("CrearTelefono", "cl.bcs.socionegocio.per.CrearTelefono"),
            Map.entry("CrearDireccionElectronica", "cl.bcs.socionegocio.per.CrearDireccionElectronica"),
            Map.entry("CrearPersonaCuentaBancaria", "cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria")));

    /** The package prefixes the platform router maps (JavaCallRouterService.packageUrlMap, first three segments): a remote FQN must start with one of them. */
    private static final List<String> PREFIJOS_DEL_ROUTER = List.of("cl.bcs.plataforma", "cl.bcs.socionegocio", "cl.bcs.ordenes", "cl.bcs.fondomutuo",
            "cl.bcs.custodia", "cl.bcs.portafolio", "cl.bcs.tesoreria", "cl.bcs.cuentaInversion");

    private HttpServer servidor;
    private final List<JsonObject> recibidos = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> tokenRecibido = new AtomicReference<>();
    private ProcedimientoAlmacenado procedimientos;
    private ColaboradoresJavaCall colaboradores;

    @BeforeEach
    void arranca() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            tokenRecibido.set(exchange.getRequestHeaders().getFirst("X-Internal-Token"));
            recibidos.add(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
            byte[] r = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, r.length);
            exchange.getResponseBody().write(r);
            exchange.close();
        });
        servidor.start();
        procedimientos = mock(ProcedimientoAlmacenado.class);
        colaboradores = new ColaboradoresJavaCall(procedimientos, "http://127.0.0.1:" + servidor.getAddress().getPort(), Duration.ofSeconds(2),
                Duration.ofSeconds(5), "tok");
    }

    @AfterEach
    void para() {
        servidor.stop(0);
    }

    @Test
    void todoNombreLiteralDeInvocarDeLosServiciosSeResuelveYLaTablaEsperadaNoTieneSobrantes() {
        Set<String> literales = NombresDeColaborador.literalesDeInvocar();
        assertThat(ColaboradoresJavaCall.nombresConocidos()).containsAll(literales);
        for (String nombre : literales) {
            if (nombre.equals("StoredProcedure")) {
                continue;
            }
            assertThat(REMOTOS).as("expected table covers " + nombre).containsKey(nombre);
            assertThat(ColaboradoresJavaCall.clase(nombre)).isEqualTo(REMOTOS.get(nombre)).contains(".").startsWith("cl.bcs.");
        }
        // the production map and the expected table agree name for name (a name nobody calls is allowed to stay registered, but never a different class)
        for (Map.Entry<String, String> e : REMOTOS.entrySet()) {
            assertThat(ColaboradoresJavaCall.clase(e.getKey())).as(e.getKey()).isEqualTo(e.getValue());
        }
        Set<String> sobrantes = new TreeSet<>(ColaboradoresJavaCall.nombresConocidos());
        sobrantes.removeAll(REMOTOS.keySet());
        assertThat(sobrantes).containsExactly("StoredProcedure");
    }

    @Test
    void ningunaLlamadaUsaUnNombreQueNoSeaUnLiteral() {
        // a name built at run time (a constant, a variable) could not be checked by the scan above: every call site must pass a literal
        assertThat(NombresDeColaborador.llamadasConNombreNoLiteral()).isEmpty();
    }

    @Test
    void todoNombreVaAlRouterConSuNombreCompletoYConElToken() {
        List<String> nombres = new ArrayList<>(REMOTOS.keySet());
        for (String nombre : nombres) {
            JsonObject params = new JsonObject();
            params.addProperty("marca", nombre);
            colaboradores.invocar(nombre, params);
            assertThat(tokenRecibido.get()).as("token sent to the router for " + nombre).isEqualTo("tok");
        }
        assertThat(recibidos).hasSize(nombres.size());
        for (int i = 0; i < nombres.size(); i++) {
            JsonObject peticion = recibidos.get(i);
            String clase = peticion.get("class").getAsString();
            assertThat(clase).isEqualTo(REMOTOS.get(nombres.get(i)));
            assertThat(PREFIJOS_DEL_ROUTER.stream().anyMatch(clase::startsWith)).as("router maps the prefix of " + clase).isTrue();
            assertThat(peticion.getAsJsonObject("params").get("marca").getAsString()).isEqualTo(nombres.get(i));
        }
    }

    @Test
    void unNombreDesconocidoLanzaIllegalArgumentYNoViajaAlRouter() {
        assertThatThrownBy(() -> colaboradores.invocar("NoExisteEsteColaborador", new JsonObject())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NoExisteEsteColaborador");
        // another APV use case is never a collaborator by name
        assertThatThrownBy(() -> colaboradores.invocar("CambiarEstadoContratosSAAPV", new JsonObject())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> colaboradores.invocar("cl.bcs.plataforma.pla.AgregarLog", new JsonObject())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> colaboradores.invocar(null, new JsonObject())).isInstanceOf(IllegalArgumentException.class);
        assertThat(recibidos).isEmpty();
    }

    @Test
    void storedProcedureVaALaBaseYNoAlRouter() {
        JsonObject entrada = new JsonObject();
        JsonObject salida = new JsonObject();
        when(procedimientos.ejecutar(any(JsonObject.class))).thenReturn(salida);
        assertThat(colaboradores.invocar("StoredProcedure", entrada)).isSameAs(salida);
        assertThat(recibidos).isEmpty();
        verify(procedimientos).ejecutar(entrada);
    }

    @Test
    void sinTokenConfiguradoNoSeEnviaLaCabecera() {
        ColaboradoresJavaCall sinToken = new ColaboradoresJavaCall(procedimientos, "http://127.0.0.1:" + servidor.getAddress().getPort(), Duration.ofSeconds(2),
                Duration.ofSeconds(5), "");
        sinToken.invocar("AgregarLog", new JsonObject());
        assertThat(recibidos).hasSize(1);
        assertThat(tokenRecibido.get()).isNull();
    }
}
