package cl.bcs.fondomutuo.adapter.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.ServerSocket;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ServidorRouterSimulado;
import cl.bcs.fondomutuo.ServidorRouterSimulado.Respuesta;
import cl.bcs.fondomutuo.kit.ColaboradorErrorException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/** FM-KIT-90..104: {@link RouterClient} and the H ports against a SIMULATED router (no real call). Mapping of 200, 503, refused, 500, timeout, bad body. */
class ColaboradoresEnrutamientoTest {

    static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    private static RouterClient cliente(String url, String token) {
        return new RouterClient(url, Duration.ofMillis(500), Duration.ofMillis(600), token);
    }

    @Test
    void fmKit90_200_enviaClaseYParamsDelLegacy_conElToken_yDevuelveElJsonTalCual() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.CALCULAR_PARIDAD, Respuesta.ok("{\"p_out_Paridad\":1.25,\"P_OUT_ERRORESCONTADOR\":0}"))) {
            ColaboradoresHttp http = new ColaboradoresHttp(cliente(s.url(), "tok"));
            JsonObject salida = http.calcular(json("{\"p_IdMonedaOrigen\":\"USD\",\"p_IdMonedaDestino\":\"CLP\"}"));
            assertThat(salida.toString()).isEqualTo("{\"p_out_Paridad\":1.25,\"P_OUT_ERRORESCONTADOR\":0}");
            assertThat(s.recibidas()).hasSize(1);
            var rq = s.recibidas().get(0);
            assertThat(rq.metodo()).isEqualTo("POST");
            assertThat(rq.ruta()).isEqualTo("/internal/java-call");
            assertThat(rq.token()).isEqualTo("tok");
            assertThat(rq.clase()).isEqualTo("cl.bcs.plataforma.mer.CalcularParidad");
            assertThat(rq.params().toString()).isEqualTo("{\"p_IdMonedaOrigen\":\"USD\",\"p_IdMonedaDestino\":\"CLP\"}");
        }
    }

    @Test
    void fmKit91_cadaPuertoVaALaClaseLegacyDeSuColaborador() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado()) {
            ColaboradoresHttp.CLASES.values().forEach(c -> s.responde(c, Respuesta.ok("{\"k\":1}")));
            ColaboradoresHttp h = new ColaboradoresHttp(cliente(s.url(), ""));
            JsonObject e = new JsonObject();
            h.calcular(e);
            h.entregar(e);
            h.saldo(e);
            h.consultarFlujo(e);
            h.agregar(e);
            h.inscribir(e);
            h.consultar(e);
            h.notificar(e);
            assertThat(s.recibidas()).extracting(ServidorRouterSimulado.Peticion::clase).containsExactly(
                    "cl.bcs.plataforma.mer.CalcularParidad", "cl.bcs.plataforma.tas.EntregarPreciosTasas", "cl.bcs.plataforma.pla.SaldoFlujoCaja",
                    "cl.bcs.custodia.ConsultarFlujoCustodia", "cl.bcs.plataforma.pla.AgregarLog", "cl.bcs.plataforma.bat.InscribirProcesoBatch",
                    "cl.bcs.plataforma.com.ConsultarProductosPortafolio", "cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP");
            assertThat(ColaboradoresHttp.CLASES).hasSize(8);
            assertThat(s.recibidas()).allSatisfy(r -> assertThat(r.token()).isNull());   // blank token: header not sent
        }
    }

    @Test
    void fmKit92_503DelRouter_esServicioNoServido_G5() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.AGREGAR_LOG, Respuesta.estado(503))) {
            assertThatThrownBy(() -> new ColaboradoresHttp(cliente(s.url(), "t")).agregar(new JsonObject()))
                    .isInstanceOf(ServicioNoServidoException.class).extracting(e -> ((ServicioNoServidoException) e).compuerta()).isEqualTo("ROUTER");
        }
    }

    @Test
    void fmKit93_conexionRechazada_esServicioNoServido_nadaSeEnvio() throws Exception {
        int libre;
        try (ServerSocket ss = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            libre = ss.getLocalPort();
        }
        RouterClient c = cliente("http://127.0.0.1:" + libre, "t");
        assertThatThrownBy(() -> new ColaboradoresHttp(c).inscribir(new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
    }

    @Test
    void fmKit94_500_esErrorDeColaborador_nuncaUn503_podriaHaberConfirmado() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.INSCRIBIR_PROCESO_BATCH, Respuesta.estado(500))) {
            assertThatThrownBy(() -> new ColaboradoresHttp(cliente(s.url(), "t")).inscribir(new JsonObject()))
                    .isInstanceOf(ColaboradorErrorException.class).isNotInstanceOf(ServicioNoServidoException.class);
        }
    }

    @Test
    void fmKit95_401y404_tambienSonErrorDeColaborador() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.SALDO_FLUJO_CAJA, Respuesta.estado(401))
                .responde(ColaboradoresHttp.ENTREGAR_PRECIOS_TASAS, Respuesta.estado(404))) {
            ColaboradoresHttp h = new ColaboradoresHttp(cliente(s.url(), "t"));
            assertThatThrownBy(() -> h.saldo(new JsonObject())).isInstanceOf(ColaboradorErrorException.class);
            assertThatThrownBy(() -> h.entregar(new JsonObject())).isInstanceOf(ColaboradorErrorException.class);
        }
    }

    @Test
    void fmKit96_timeoutDeLectura_esErrorDeColaborador_laPeticionSeEnvio_noSeRepiteEnTomee() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.AGREGAR_LOG,
                new Respuesta(200, "{\"a\":1}", 3000))) {
            assertThatThrownBy(() -> new ColaboradoresHttp(cliente(s.url(), "t")).agregar(new JsonObject()))
                    .isInstanceOf(ColaboradorErrorException.class).isNotInstanceOf(ServicioNoServidoException.class);
            assertThat(s.recibidas()).hasSize(1);
        }
    }

    @Test
    void fmKit97_cuerpoQueNoEsObjetoJson_esErrorDeColaborador() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.CALCULAR_PARIDAD, Respuesta.ok("[1,2]"))
                .responde(ColaboradoresHttp.CONSULTAR_FLUJO_CUSTODIA, Respuesta.ok("no es json"))) {
            ColaboradoresHttp h = new ColaboradoresHttp(cliente(s.url(), "t"));
            assertThatThrownBy(() -> h.calcular(new JsonObject())).isInstanceOf(ColaboradorErrorException.class);
            assertThatThrownBy(() -> h.consultarFlujo(new JsonObject())).isInstanceOf(ColaboradorErrorException.class);
        }
    }

    @Test
    void fmKit98_sinParams_seEnviaUnObjetoVacio_noNull() {
        try (ServidorRouterSimulado s = new ServidorRouterSimulado().responde(ColaboradoresHttp.CALCULAR_PARIDAD, Respuesta.ok("{}"))) {
            cliente(s.url(), "t").invocar(ColaboradoresHttp.CALCULAR_PARIDAD, null);
            assertThat(s.recibidas().get(0).params().toString()).isEqualTo("{}");
        }
    }
}
