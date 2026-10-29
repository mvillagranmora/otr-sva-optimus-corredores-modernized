package cl.bcs.tesoreria.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import cl.bcs.tesoreria.javacall.AnularNominasTransferenciasService;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;

/**
 * Architecture review H1: EVERY collaborator name the services pass to {@code invocar(...)} resolves explicitly, either to a class served in process
 * (the REAL {@link JavaCallDispatcher} table) or to a remote entry with its fully qualified legacy class, and an unknown name fails instead of
 * travelling bare to the router. The expected table below is written from the legacy sources (legacy/.../BSCL/**), independently of the production map.
 */
class ColaboradoresJavaCallEnrutamientoTest {

    /** name -> legacy FQN, for the classes this module serves (in process, no network). */
    private static final Map<String, String> EN_PROCESO = Map.of(
            "CrearIngresoEgreso", "cl.bcs.tesoreria.CrearIngresoEgreso",
            "LiquidarComprobante", "cl.bcs.tesoreria.LiquidarComprobante",
            "CrearComprobante", "cl.bcs.tesoreria.CrearComprobante",
            "ActualizarSaldosCuentaBanco", "cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco",
            "AnularNominasTransferencias", "cl.bcs.tesoreria.nor.AnularNominasTransferencias");

    /** name -> legacy FQN, for the classes of other modules (HTTP to the router, which maps them by package prefix). */
    private static final Map<String, String> REMOTOS = new TreeMap<>(Map.ofEntries(
            Map.entry("RevisarRequisitosBasicos", "cl.bcs.plataforma.pla.RevisarRequisitosBasicos"),
            Map.entry("AgregarLog", "cl.bcs.plataforma.pla.AgregarLog"),
            Map.entry("ConsultarFlujoCaja", "cl.bcs.plataforma.pla.ConsultarFlujoCaja"),
            Map.entry("SaldoFlujoCaja", "cl.bcs.plataforma.pla.SaldoFlujoCaja"),
            Map.entry("AgregarPrograma", "cl.bcs.plataforma.bat.AgregarPrograma"),
            Map.entry("CalcularParidad", "cl.bcs.plataforma.mer.CalcularParidad"),
            Map.entry("ConsultarInstrumentoPrecioBolsa", "cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa"),
            Map.entry("CrearEventoAutorizacionMasivo", "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo"),
            Map.entry("ModificarEstadoEventoAutorizacionMasivo", "cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo"),
            Map.entry("CrearCargoAbono", "cl.bcs.cuentaInversion.CrearCargoAbono"),
            Map.entry("AnularCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono"),
            Map.entry("AnulacionContratoCSC", "cl.bcs.fondomutuo.AnulacionContratoCSC"),
            Map.entry("RevocacionContratoCSC", "cl.bcs.fondomutuo.RevocacionContratoCSC"),
            Map.entry("CalcularMontoNumeroCuota", "cl.bcs.fondomutuo.CalcularMontoNumeroCuota"),
            Map.entry("CrearSolicitudAporteRescateNormal", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal"),
            Map.entry("CambiarEstadoContratosSAAPV", "cl.bcs.APV.CambiarEstadoContratosSAAPV"),
            Map.entry("CalcularMontoYCobrosOrden", "cl.bcs.ordenes.CalcularMontoYCobrosOrden"),
            Map.entry("CrearOrdenes", "cl.bcs.ordenes.CrearOrdenes"),
            Map.entry("ObtenerPortafoliosEjecutivo", "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo"),
            Map.entry("ObtenerPortafoliosEjecutivo.restringeCartera", "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.restringeCartera")));

    /** The package prefixes the platform router maps (JavaCallRouterService.packageUrlMap): a remote FQN must start with one of them. */
    private static final List<String> PREFIJOS_DEL_ROUTER = List.of("cl.bcs.plataforma", "cl.bcs.socionegocio", "cl.bcs.ordenes", "cl.bcs.fondomutuo",
            "cl.bcs.APV", "cl.bcs.tesoreria", "cl.bcs.cuentaInversion");

    private HttpServer servidor;
    private final List<JsonObject> recibidos = new CopyOnWriteArrayList<>();
    private ApplicationContext contexto;
    private JavaCallDispatcher local;
    private ProcedimientoAlmacenado procedimientos;
    private ColaboradoresJavaCall colaboradores;

    @BeforeEach
    void arranca() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            recibidos.add(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
            byte[] r = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, r.length);
            exchange.getResponseBody().write(r);
            exchange.close();
        });
        servidor.start();
        // the REAL dispatcher table; the beans behind it are mocks (what is under test is the routing, not the services)
        contexto = mock(ApplicationContext.class);
        when(contexto.getBean(CrearIngresoEgresoService.class)).thenReturn(mock(CrearIngresoEgresoService.class));
        when(contexto.getBean(LiquidarComprobanteService.class)).thenReturn(mock(LiquidarComprobanteService.class));
        when(contexto.getBean(CrearComprobanteService.class)).thenReturn(mock(CrearComprobanteService.class));
        when(contexto.getBean(ActualizarSaldosCuentaBancoService.class)).thenReturn(mock(ActualizarSaldosCuentaBancoService.class));
        when(contexto.getBean(AnularNominasTransferenciasService.class)).thenReturn(mock(AnularNominasTransferenciasService.class));
        local = new JavaCallDispatcher(contexto);
        procedimientos = mock(ProcedimientoAlmacenado.class);
        colaboradores = new ColaboradoresJavaCall(local, procedimientos, "http://127.0.0.1:" + servidor.getAddress().getPort(), Duration.ofSeconds(2),
                Duration.ofSeconds(5), "tok");
    }

    @AfterEach
    void para() {
        servidor.stop(0);
    }

    @Test
    void todoNombreLiteralDeInvocarDeLosServiciosYDeLosContratosSeResuelve() {
        Set<String> literales = NombresDeColaborador.literalesDeInvocar();
        assertThat(literales).as("the scan must find the call sites (a broken regex would make this test vacuous)").hasSizeGreaterThan(20)
                .contains("CrearComprobante", "RevisarRequisitosBasicos", "StoredProcedure", "Cloper", "ObtenerPortafoliosEjecutivo.restringeCartera");
        assertThat(ColaboradoresJavaCall.nombresConocidos()).containsAll(literales);
        for (String nombre : literales) {
            if (nombre.equals("StoredProcedure") || nombre.equals("Cloper")) {
                continue;
            }
            String fqn = ColaboradoresJavaCall.clase(nombre);
            assertThat(fqn).as(nombre).contains(".").startsWith("cl.bcs.");
            assertThat(EN_PROCESO.containsKey(nombre) || REMOTOS.containsKey(nombre)).as("expected table covers " + nombre).isTrue();
        }
        // the expected tables name nothing that the code does not call (a stale entry would hide a removed call site)
        Set<String> esperados = new TreeSet<>(EN_PROCESO.keySet());
        esperados.addAll(REMOTOS.keySet());
        esperados.removeAll(literales);
        assertThat(esperados).isEmpty();
    }

    @Test
    void lasClasesDeEsteModuloSeEjecutanEnProcesoYNuncaSalenALaRed() throws Exception {
        for (Map.Entry<String, String> e : EN_PROCESO.entrySet()) {
            assertThat(ColaboradoresJavaCall.clase(e.getKey())).as(e.getKey()).isEqualTo(e.getValue());
            assertThat(local.maneja(e.getValue())).as("dispatcher serves " + e.getValue()).isTrue();
            colaboradores.invocar(e.getKey(), new JsonObject());
        }
        assertThat(recibidos).as("nothing went over HTTP").isEmpty();
        verify(contexto).getBean(CrearComprobanteService.class);
        verify(contexto).getBean(CrearIngresoEgresoService.class);
        verify(contexto).getBean(LiquidarComprobanteService.class);
        verify(contexto).getBean(ActualizarSaldosCuentaBancoService.class);
        verify(contexto).getBean(AnularNominasTransferenciasService.class);
    }

    @Test
    void lasClasesDeOtrosModulosVanAlRouterConSuNombreCompletoNuncaDesnudo() {
        List<String> nombres = new ArrayList<>(REMOTOS.keySet());
        for (String nombre : nombres) {
            assertThat(local.maneja(REMOTOS.get(nombre))).as(REMOTOS.get(nombre) + " is not served in process").isFalse();
            JsonObject params = new JsonObject();
            params.addProperty("marca", nombre);
            colaboradores.invocar(nombre, params);
        }
        assertThat(recibidos).hasSize(nombres.size());
        for (int i = 0; i < nombres.size(); i++) {
            JsonObject peticion = recibidos.get(i);
            String clase = peticion.get("class").getAsString();
            assertThat(clase).isEqualTo(REMOTOS.get(nombres.get(i)));
            assertThat(clase).as("not a bare name").contains(".");
            assertThat(PREFIJOS_DEL_ROUTER.stream().anyMatch(clase::startsWith)).as("router maps the prefix of " + clase).isTrue();
            assertThat(peticion.getAsJsonObject("params").get("marca").getAsString()).isEqualTo(nombres.get(i));
        }
    }

    @Test
    void unNombreDesconocidoLanzaIllegalArgumentYNoViajaAlRouter() {
        assertThatThrownBy(() -> colaboradores.invocar("NoExisteEsteColaborador", new JsonObject())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NoExisteEsteColaborador");
        assertThatThrownBy(() -> colaboradores.invocar("cl.bcs.plataforma.pla.AgregarLog", new JsonObject())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> colaboradores.invocar(null, new JsonObject())).isInstanceOf(IllegalArgumentException.class);
        assertThat(recibidos).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void storedProcedureVaALaBaseYCloperNoEstaDisponible() {
        JsonObject entrada = new JsonObject();
        JsonObject salida = new JsonObject();
        when(procedimientos.ejecutar(any(JsonObject.class))).thenReturn(salida);
        assertThat(colaboradores.invocar("StoredProcedure", entrada)).isSameAs(salida);
        assertThatThrownBy(() -> colaboradores.invocar("Cloper", new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
        assertThat(recibidos).isEmpty();
        verify(procedimientos).ejecutar(entrada);
        verify(contexto, never()).getBean(any(Class.class));
    }
}
