package cl.bcs.plataforma.internal;

import cl.bcs.plataforma.service.InternalJavaCallRouter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** APV owner strategy: legacy class -> typed REST route of apv-service (registry of bcs-apv-javacall-adapter). */
class ApvRestEstrategiaTest {

    private Falso apv;
    private Falso tomee;

    @BeforeEach
    void arrancar() throws Exception {
        apv = new Falso();
        tomee = new Falso();
        tomee.respuesta = "{\"desde\":\"tomee\"}";
    }

    @AfterEach
    void parar() {
        apv.close();
        tomee.close();
    }

    private InternalJavaCallRouter router() {
        return Montaje.router("cl.bcs.APV.*", "cl.bcs.APV.=APV_REST@" + apv.url(), tomee.url(), RegistroServicios.vacio(), 500);
    }

    private static SolicitudJavaCall sol(String clase, String params) {
        return Montaje.solicitud("{\"class\":\"" + clase + "\",\"idServicio\":9,\"params\":" + params + "}");
    }

    @Test
    void post_enviaElRecordTipadoAlaRutaYDevuelveElJsonLegacy() {
        apv.respuesta = "{\"mensajes\":[],\"contadorErrores\":0,\"contadorAdvertencias\":2}";
        Respuesta r = router().enrutar(sol("cl.bcs.APV.CambiarEstadoContratosSAAPV",
                "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"REV\",\"p_DesdePac\":\"S\"}"), null);
        Falso.Pedido p = apv.pedidos.get(0);
        assertThat(p.metodo()).isEqualTo("POST");
        assertThat(p.uri()).isEqualTo("/apv/contratos/cambio-estado");
        assertThat(p.cabecera("X-Internal-Token")).isEqualTo(Montaje.TOKEN);
        assertThat(p.cuerpo()).contains("\"idEmpresa\":1", "\"idUsuario\":2", "\"opcion\":\"REV\"", "\"desdePac\":\"S\"");
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cuerpo()).contains("\"P_OUT_ERRORESCONTADOR\":0");
        assertThat(tomee.pedidos).isEmpty();
    }

    @Test
    void get_enviaEscalaresPlanosPorQueryString() {
        apv.respuesta = "{\"mensajes\":[],\"contadorErrores\":0,\"preferenciaPagoPortafolio\":[]}";
        Respuesta r = router().enrutar(sol("cl.bcs.APV.ConsultarPreferenciaPagoPortafolio",
                "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_IdCliente\":33}"), null);
        Falso.Pedido p = apv.pedidos.get(0);
        assertThat(p.metodo()).isEqualTo("GET");
        assertThat(p.uri()).startsWith("/apv/portafolios/preferencia-pago?").contains("idEmpresa=1", "idCliente=33", "idModulo=APV");
        assertThat(p.cabecera("X-Internal-Token")).isEqualTo(Montaje.TOKEN);
        assertThat(r.cuerpo()).contains("P_OUT_PREFERENCIA_PAGO_PORTAFOLIO");
    }

    @Test
    void rutaTipadaResponde500_sePropagaSinTomee() {
        apv.status = 500;
        apv.respuesta = "{\"error\":\"Internal error\",\"correlationId\":\"c1\"}";
        Respuesta r = router().enrutar(sol("cl.bcs.APV.CambiarEstadoContratosSAAPV", "{}"), null);
        assertThat(r.status()).isEqualTo(500);
        assertThat(r.cuerpo()).isEqualTo("{\"error\":\"Internal error\",\"correlationId\":\"c1\"}");
        assertThat(tomee.pedidos).isEmpty();
    }

    @Test
    void rutaTipada400_sePropagaTalCual() {
        apv.status = 400;
        apv.respuesta = "{\"error\":\"Invalid request\"}";
        Respuesta r = router().enrutar(sol("cl.bcs.APV.CambiarEstadoContratosSAAPV", "{}"), null);
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.cuerpo()).isEqualTo("{\"error\":\"Invalid request\"}");
    }

    @Test
    void servicioApv503_reintentaEnTomee() {
        apv.status = 503;
        Respuesta r = router().enrutar(sol("cl.bcs.APV.CambiarEstadoContratosSAAPV", "{}"), null);
        assertThat(r.cuerpo()).isEqualTo("{\"desde\":\"tomee\"}");
        assertThat(tomee.pedidos.get(0).uri()).isEqualTo("/call?t=9");
    }

    @Test
    void claseSinRutaTipada_marcadorNoRuteableAun_vaATomee_sinLlamarAApv() {
        for (String c : new String[] { "cl.bcs.APV.ValidarMandatosPAC", "cl.bcs.APV.ValidarBancoPrevired", "cl.bcs.APV.NoExiste" }) {
            Respuesta r = router().enrutar(sol(c, "{}"), null);
            assertThat(r.cuerpo()).as(c).isEqualTo("{\"desde\":\"tomee\"}");
        }
        assertThat(apv.pedidos).isEmpty();
    }

    @Test
    void operacionLogicaSoloEnProceso_seRechaza() {
        var ex = org.assertj.core.api.Assertions.catchThrowableOfType(SolicitudJavaCall.Invalida.class,
                () -> router().enrutar(sol("cl.bcs.APV.ValidarMandatosPAC.logica", "{}"), null));
        assertThat(ex.status()).isEqualTo(422);
        assertThat(apv.pedidos).isEmpty();
        assertThat(tomee.pedidos).isEmpty();
    }
}
