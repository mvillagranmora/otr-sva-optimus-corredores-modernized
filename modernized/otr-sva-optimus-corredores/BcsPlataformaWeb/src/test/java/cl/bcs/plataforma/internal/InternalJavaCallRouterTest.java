package cl.bcs.plataforma.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Routing branches of the internal router against local fake servers: owner (JAVACALL strategy) and TomEE. */
class InternalJavaCallRouterTest {

    private Falso dueno;
    private Falso tomee;
    private static final String CLASE = "cl.bcs.tesoreria.CrearIngresoEgreso";
    private static final String CUERPO = "{\"class\":\"" + CLASE + "\",\"idServicio\":77,\"params\":{\"IdEmpresa\":5,\"p_IdUsuario\":9,\"X\":\"y\"}}";

    @BeforeEach
    void arrancar() throws Exception {
        dueno = new Falso();
        tomee = new Falso();
        dueno.respuesta = "{\"P_OUT_ERRORESCONTADOR\":0}";
        tomee.respuesta = "{\"desde\":\"tomee\"}";
    }

    @AfterEach
    void parar() {
        dueno.close();
        tomee.close();
    }

    private cl.bcs.plataforma.service.InternalJavaCallRouter router(String habilitadas, String urlDueno) {
        return Montaje.router(habilitadas, "cl.bcs.tesoreria.=JAVACALL@" + urlDueno, tomee.url(), RegistroServicios.vacio(), 400);
    }

    @Test
    void claseNoHabilitada_vaATomee_sinTocarAlDueno_yConIdServicioSinTokenInterno() {
        Respuesta r = router("", dueno.url()).enrutar(Montaje.solicitud(CUERPO), "sesion-1");
        assertThat(r.cuerpo()).isEqualTo("{\"desde\":\"tomee\"}");
        assertThat(dueno.pedidos).isEmpty();
        Falso.Pedido p = tomee.pedidos.get(0);
        assertThat(p.uri()).isEqualTo("/call?t=77");
        assertThat(p.cabecera("X-Internal-Token")).isNull();
        assertThat(p.cabecera("X-SESSION-TOKEN")).isEqualTo("sesion-1");
        assertThat(p.cuerpo()).isEqualTo("{\"IdEmpresa\":5,\"p_IdUsuario\":9,\"X\":\"y\"}"); // no identity override
        assertThat(p.cabeceras().keySet()).noneMatch(k -> k.toLowerCase().contains("idempot"));
    }

    @Test
    void claseHabilitada_vaAlDueno_conTokenInterno_paramsIntactos_yRespuestaSinAlterar() {
        dueno.status = 200;
        Respuesta r = router(CLASE, dueno.url()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cuerpo()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0}");
        assertThat(tomee.pedidos).isEmpty();
        Falso.Pedido p = dueno.pedidos.get(0);
        assertThat(p.metodo()).isEqualTo("POST");
        assertThat(p.uri()).isEqualTo("/internal/java-call");
        assertThat(p.cabecera("X-Internal-Token")).isEqualTo(Montaje.TOKEN);
        assertThat(p.cabecera("X-SESSION-TOKEN")).isNull();
        assertThat(p.cabeceras().keySet()).noneMatch(k -> k.toLowerCase().contains("idempot"));
        assertThat(p.cuerpo()).isEqualTo("{\"class\":\"" + CLASE + "\",\"params\":{\"IdEmpresa\":5,\"p_IdUsuario\":9,\"X\":\"y\"}}");
    }

    @Test
    void prefijoEnLaListaDeHabilitadas() {
        router("cl.bcs.tesoreria.*", dueno.url()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(dueno.pedidos).hasSize(1);
    }

    @Test
    void habilitadaPeroSinDestinoConfigurado_vaATomee() {
        var r = Montaje.router(CLASE, "", tomee.url(), RegistroServicios.vacio(), 400);
        assertThat(r.enrutar(Montaje.solicitud(CUERPO), null).cuerpo()).contains("tomee");
        assertThat(dueno.pedidos).isEmpty();
    }

    @Test
    void duenoResponde503_reintentaEnTomee() {
        dueno.status = 503;
        dueno.respuesta = "{\"error\":\"not yet migrated\"}";
        Respuesta r = router(CLASE, dueno.url()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(r.cuerpo()).isEqualTo("{\"desde\":\"tomee\"}");
        assertThat(dueno.pedidos).hasSize(1);
        assertThat(tomee.pedidos).hasSize(1);
    }

    @Test
    void conexionRechazada_reintentaEnTomee() throws Exception {
        Respuesta r = router(CLASE, Falso.urlCerrada()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(r.cuerpo()).isEqualTo("{\"desde\":\"tomee\"}");
        assertThat(tomee.pedidos).hasSize(1);
    }

    @Test
    void error500_sePropagaTalCual_sinTomee() {
        dueno.status = 500;
        dueno.respuesta = "{\"error\":\"boom\",\"correlationId\":\"abc\"}";
        Respuesta r = router(CLASE, dueno.url()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(r.status()).isEqualTo(500);
        assertThat(r.cuerpo()).isEqualTo("{\"error\":\"boom\",\"correlationId\":\"abc\"}");
        assertThat(tomee.pedidos).isEmpty();
    }

    @Test
    void error4xx_sePropagaTalCual() {
        dueno.status = 400;
        dueno.respuesta = "{\"error\":\"Invalid JSON body\"}";
        Respuesta r = router(CLASE, dueno.url()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.cuerpo()).isEqualTo("{\"error\":\"Invalid JSON body\"}");
        assertThat(tomee.pedidos).isEmpty();
    }

    @Test
    void timeoutDeLectura_sePropaga_sinTomee() {
        dueno.demoraMs = 1500;
        assertThatThrownBy(() -> router(CLASE, dueno.url()).enrutar(Montaje.solicitud(CUERPO), null))
                .isInstanceOf(ResourceAccessException.class).hasCauseInstanceOf(SocketTimeoutException.class);
        assertThat(tomee.pedidos).isEmpty();
    }

    @Test
    void tomeeRespondeError_sePropagaSinReintento() {
        tomee.status = 503;
        tomee.respuesta = "{\"e\":1}";
        Respuesta r = router("", dueno.url()).enrutar(Montaje.solicitud(CUERPO), null);
        assertThat(r.status()).isEqualTo(503);
        assertThat(r.cuerpo()).isEqualTo("{\"e\":1}");
        assertThat(tomee.pedidos).hasSize(1);
    }

    @Test
    void operacionSoloEnProceso_seRechaza_aunqueNoEsteHabilitada() {
        String c = "{\"class\":\"cl.bcs.APV.ValidarMandatosPAC.logica\",\"params\":{}}";
        for (String hab : new String[] { "", "cl.bcs.APV.*" }) {
            assertThatThrownBy(() -> router(hab, dueno.url()).enrutar(Montaje.solicitud(c), null))
                    .isInstanceOf(SolicitudJavaCall.Invalida.class).hasMessageContaining("in-process only")
                    .extracting(e -> ((SolicitudJavaCall.Invalida) e).status()).isEqualTo(422);
        }
        assertThat(dueno.pedidos).isEmpty();
        assertThat(tomee.pedidos).isEmpty();
    }

    @Test
    void sinIdServicioParaTomee_422() {
        String c = "{\"class\":\"" + CLASE + "\",\"params\":{}}";
        assertThatThrownBy(() -> router("", dueno.url()).enrutar(Montaje.solicitud(c), null))
                .isInstanceOf(SolicitudJavaCall.Invalida.class).extracting(e -> ((SolicitudJavaCall.Invalida) e).status()).isEqualTo(422);
    }

    @Test
    void idServicioDesdeElRegistroExportado() throws Exception {
        var in = new java.io.ByteArrayInputStream("IdServicio,RutaServicio,TipoServicio\n5,PLUGIN_TES.jar@cl.bcs.tesoreria.CrearIngresoEgreso,J\n".getBytes());
        var r = Montaje.router("", "", tomee.url(), RegistroServicios.desde(in), 400);
        r.enrutar(Montaje.solicitud("{\"class\":\"" + CLASE + "\",\"params\":{},\"skip\":2,\"take\":3}"), null);
        assertThat(tomee.pedidos.get(0).uri()).isEqualTo("/call?t=5&skip=2&take=3");
    }

    @Test
    void prefijoDelPluginSeQuita() {
        router(CLASE, dueno.url()).enrutar(Montaje.solicitud("{\"class\":\"PLUGIN_TES.jar@" + CLASE + "\",\"params\":{}}"), null);
        assertThat(dueno.pedidos.get(0).cuerpo()).contains("\"class\":\"" + CLASE + "\"");
    }
}
