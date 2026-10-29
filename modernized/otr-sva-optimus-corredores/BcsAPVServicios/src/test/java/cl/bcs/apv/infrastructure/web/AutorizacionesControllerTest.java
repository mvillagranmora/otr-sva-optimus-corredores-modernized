package cl.bcs.apv.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.MensajeMapeo;
import cl.bcs.apv.adapter.javacall.Sabor;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.application.autorizaciones.AutorizacionCambioPlanActivo;
import cl.bcs.apv.application.autorizaciones.EntradaAutorizacion;
import cl.bcs.apv.application.autorizaciones.EventoAutorizacion;
import cl.bcs.apv.application.autorizaciones.ResultadoAutorizacion;
import cl.bcs.apv.application.autorizaciones.SalidaAutorizacion;

/**
 * The REST entry point of the authorization hook of the plan change: protected by the token filter like every other path (no allow-list change: only the
 * health probes are public), legacy JSON body in (EntradaAutorizacion), legacy JSON answer out (SalidaAutorizacion), and a failure of the hook (the legacy throws NullPointerException, NumberFormatException,
 * IndexOutOfBoundsException...) is the generic 500 of {@code ApvExceptionHandler}, never a leak. The behaviour of the hook itself is pinned by
 * {@code AutorizacionCambiarAPlanActivoContratoSAAPVCasosTest} against the recorded legacy.
 */
class AutorizacionesControllerTest {

    private static final String TOKEN = "token-de-prueba";
    private static final String AUTORIZACION = "/apv/autorizaciones/cambio-plan-activo/autorizacion";
    private static final String RECHAZO = "/apv/autorizaciones/cambio-plan-activo/rechazo";
    private static final String CUERPO = """
            {"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"APV","p_UsaReglasNegocio":"N",
             "p_Lista":[{"IdEvento":5001,"IdTipoEvento":"MOPLAC","IdEmpresa":1,"IdModulo":"APV","IdUsuario":21,"Fecha":"Oct 20, 2026","IdCliente":1,"IdCanal":"OPT"}]}""";

    private final List<String> llamadas = new ArrayList<>();
    private final List<EntradaAutorizacion> entradas = new ArrayList<>();
    private RuntimeException falla;
    private SalidaAutorizacion salida;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        AutorizacionCambioPlanActivo servicio = new AutorizacionCambioPlanActivo() {
            @Override
            public SalidaAutorizacion autorizarEvento(EntradaAutorizacion entrada) {
                llamadas.add("autorizar");
                entradas.add(entrada);
                if (falla != null) {
                    throw falla;
                }
                return salida != null ? salida : new SalidaAutorizacion(List.of(new Mensaje(0L, "INFORMACION", "OPERACION_AUTORIZADA", "ok")), 0L,
                        List.of(new ResultadoAutorizacion(5001L, ResultadoAutorizacion.EXITO)));
            }

            @Override
            public SalidaAutorizacion rechazarEvento(EntradaAutorizacion entrada) {
                llamadas.add("rechazar");
                entradas.add(entrada);
                return salida != null ? salida : new SalidaAutorizacion(new ArrayList<>(), 0L, new ArrayList<>());
            }
        };
        mvc = MockMvcBuilders.standaloneSetup(new AutorizacionesController(servicio)).setControllerAdvice(new ApvExceptionHandler())
                .addFilters(new TokenInternoFilter(TOKEN)).build();
    }

    @Test
    void sinTokenOConTokenIncorrectoEsUn401YElHookNoSeEjecuta() throws Exception {
        mvc.perform(post(AUTORIZACION).contentType(MediaType.APPLICATION_JSON).content(CUERPO)).andExpect(status().isUnauthorized());
        mvc.perform(post(RECHAZO).contentType(MediaType.APPLICATION_JSON).content(CUERPO).header(TokenInternoFilter.CABECERA, "otro"))
                .andExpect(status().isUnauthorized());
        // the allow-list normalizes path parameters and repeated slashes: the variants are protected too
        mvc.perform(post("/apv/autorizaciones;x=1/cambio-plan-activo/autorizacion").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("//apv/autorizaciones/cambio-plan-activo/autorizacion").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
        assertThat(llamadas).isEmpty();
    }

    @Test
    void autorizarLeeElCuerpoLegacyIgnoraLosCamposDelEventoQueNoUsaYResponderConLosNombresLegacy() throws Exception {
        mvc.perform(post(AUTORIZACION).contentType(MediaType.APPLICATION_JSON).content(CUERPO).header(TokenInternoFilter.CABECERA, TOKEN))
                .andExpect(status().isOk()).andExpect(content().json("""
                        {"P_OUT_MENSAJES":[{"id":0,"tipo":"INFORMACION","codigo":"OPERACION_AUTORIZADA","descripcion":"ok"}],
                         "P_OUT_ERRORESCONTADOR":0,"P_OUT_RESULTADO":[{"evento":5001,"resultado":"EXITO"}]}""", true));
        assertThat(llamadas).containsExactly("autorizar");
        EntradaAutorizacion e = entradas.get(0);
        assertThat(e.idEmpresa()).isEqualTo(1L);
        assertThat(e.idUsuario()).isEqualTo(21L);
        assertThat(e.idModulo()).isEqualTo("APV");
        assertThat(e.usaReglasNegocio()).isEqualTo("N");
        assertThat(e.lista()).hasSize(1);
        assertThat(e.lista().get(0)).isEqualTo(new EventoAutorizacion(5001L, 1L, "APV", 21L, 1L));
    }

    @Test
    void losMensajesDeErrorSeSerializanComoLosDeLasOtrasOperacionesYUnaListaAusenteEsVacia() throws Exception {
        salida = new SalidaAutorizacion(List.of(new Mensaje(1L, "ERROR", "NO_SE_INGRESARON_CONTRATOS", "sin contratos"), new Mensaje(2L, "OK", null, null)),
                2L, List.of(new ResultadoAutorizacion(5001L, ResultadoAutorizacion.ERROR)));
        mvc.perform(post(AUTORIZACION).contentType(MediaType.APPLICATION_JSON).content("{\"p_IdEmpresa\":1}").header(TokenInternoFilter.CABECERA, TOKEN))
                .andExpect(status().isOk()).andExpect(content().json("""
                        {"P_OUT_MENSAJES":[{"id":1,"tipo":"ERROR","codigo":"NO_SE_INGRESARON_CONTRATOS","descripcion":"sin contratos"},{"id":2,"tipo":"OK"}],
                         "P_OUT_ERRORESCONTADOR":2,"P_OUT_RESULTADO":[{"evento":5001,"resultado":"ERROR"}]}""", true));
        assertThat(entradas.get(0).lista()).isEmpty();
        assertThat(entradas.get(0).idUsuario()).isNull();
    }

    /** Round trip against the recorded legacy: golden entrada -> typed call -> typed salida of the golden -> JSON equal to the golden salida. */
    @ParameterizedTest
    @ValueSource(strings = { "APV-A-526_exito-cuando-el-cliente-del-evento-es-el-numero-de-portafolios",
            "APV-A-527_cliente-real-no-coincide-y-devuelve-los-mensajes-del-cambio-de-plan", "APV-A-528_dos-detalles-del-evento-dos-contratos-y-exito-con-cliente-2",
            "APV-A-529_evento-sin-detalle-el-contador-queda-en-cero-con-un-error", "APV-A-530_error-al-modificar-portafolio-se-copia-con-su-contador",
            "APV-A-531_exito-oculta-los-errores-del-cambio-de-plan", "APV-A-532_cliente-nulo-se-compara-con-el-id-cero",
            "APV-A-538_el-cambio-de-plan-lanza-excepcion-y-queda-contador-uno", "APV-A-539_rechazar-no-hace-nada" })
    void laSalidaEsIgualALaDelLegacyGrabadaEnElCaso(String caso) throws Exception {
        JsonObject c = leer("apv-a/cases/" + caso + "/case.json");
        JsonObject golden = leer("apv-a/cases/" + caso + "/expected.json").getAsJsonObject("salida");
        salida = salidaTipada(golden);
        boolean rechazo = "rechazarEvento".equals(c.get("metodo").getAsString());
        String ruta = rechazo ? RECHAZO : AUTORIZACION;
        String respuesta = mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(c.getAsJsonObject("entrada").toString())
                .header(TokenInternoFilter.CABECERA, TOKEN)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(JsonParser.parseString(respuesta)).isEqualTo(golden);
        assertThat(llamadas).containsExactly(rechazo ? "rechazar" : "autorizar");
        JsonObject entradaGolden = c.getAsJsonObject("entrada");
        EntradaAutorizacion e = entradas.get(0);
        assertThat(e.idEmpresa()).isEqualTo(entradaGolden.get("p_IdEmpresa").getAsLong());
        assertThat(e.lista()).hasSize(entradaGolden.getAsJsonArray("p_Lista").size());
        assertThat(e.lista().get(0).idEvento()).isEqualTo(entradaGolden.getAsJsonArray("p_Lista").get(0).getAsJsonObject().get("IdEvento").getAsLong());
    }

    private static JsonObject leer(String recurso) throws Exception {
        try (var in = AutorizacionesControllerTest.class.getClassLoader().getResourceAsStream(recurso)) {
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static SalidaAutorizacion salidaTipada(JsonObject g) {
        List<Mensaje> mensajes = new ArrayList<>();
        for (var m : g.getAsJsonArray("P_OUT_MENSAJES")) {
            mensajes.add(MensajeMapeo.desdeJson(Sabor.PLANO, m.getAsJsonObject()));
        }
        List<ResultadoAutorizacion> resultados = new ArrayList<>();
        for (var r : g.getAsJsonArray("P_OUT_RESULTADO")) {
            resultados.add(new ResultadoAutorizacion(r.getAsJsonObject().get("evento").getAsLong(), r.getAsJsonObject().get("resultado").getAsString()));
        }
        return new SalidaAutorizacion(mensajes, g.get("P_OUT_ERRORESCONTADOR").getAsLong(), resultados);
    }

    @Test
    void rechazarEsOtraRutaYDevuelveUnaSalidaVacia() throws Exception {
        mvc.perform(post(RECHAZO).contentType(MediaType.APPLICATION_JSON).content(CUERPO).header(TokenInternoFilter.CABECERA, TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.P_OUT_ERRORESCONTADOR").value(0)).andExpect(jsonPath("$.P_OUT_MENSAJES").isEmpty())
                .andExpect(jsonPath("$.P_OUT_RESULTADO").isEmpty());
        assertThat(llamadas).containsExactly("rechazar");
    }

    @Test
    void unaFallaDelHookEsElErrorGenericoSinFiltrarElDetalle() throws Exception {
        falla = new NullPointerException("detalle interno que no debe salir");
        mvc.perform(post(AUTORIZACION).contentType(MediaType.APPLICATION_JSON).content(CUERPO).header(TokenInternoFilter.CABECERA, TOKEN))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.error").value("Internal error"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void unCuerpoQueNoEsJsonEsUn400() throws Exception {
        mvc.perform(post(AUTORIZACION).contentType(MediaType.APPLICATION_JSON).content("no es json").header(TokenInternoFilter.CABECERA, TOKEN))
                .andExpect(status().isBadRequest());
        assertThat(llamadas).isEmpty();
    }
}
