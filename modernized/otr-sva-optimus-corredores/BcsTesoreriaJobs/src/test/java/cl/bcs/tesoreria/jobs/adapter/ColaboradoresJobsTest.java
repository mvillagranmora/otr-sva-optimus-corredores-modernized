package cl.bcs.tesoreria.jobs.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Decision 3: the four collaborators that only the jobs call (ValidarMandatosPAC, ValidaEjecucionProcesoRecPAC, ConsultarEstadoModulo,
 * ObtenerDiaAnterior) reach the platform JavaCall router with their legacy class, the entry as {@code params} and the internal token;
 * every other name is the service adapter's.
 */
class ColaboradoresJobsTest {

    private static final String ROUTER = "http://router.test";

    private static final Map<String, String> CLASES = Map.of(
            ServiciosExternos.VALIDAR_MANDATOS_PAC, "cl.bcs.APV.ValidarMandatosPAC",
            ServiciosExternos.VALIDA_EJECUCION_PROCESO_REC_PAC, "cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC",
            ServiciosExternos.CONSULTAR_ESTADO_MODULO, "cl.bcs.plataforma.cre.ConsultarEstadoModulo",
            ServiciosExternos.OBTENER_DIA_ANTERIOR, "cl.bcs.plataforma.pla.ObtenerDiaAnterior");

    private record Montaje(ColaboradoresJobs colaboradores, MockRestServiceServer servidor, Colaboradores servicio) {
    }

    private Montaje montar(String token) {
        RestClient.Builder builder = RestClient.builder().baseUrl(ROUTER);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        Colaboradores servicio = mock(Colaboradores.class);
        return new Montaje(new ColaboradoresJobs(servicio, builder.build(), token), servidor, servicio);
    }

    @ParameterizedTest
    @ValueSource(strings = {ServiciosExternos.VALIDAR_MANDATOS_PAC, ServiciosExternos.VALIDA_EJECUCION_PROCESO_REC_PAC,
            ServiciosExternos.CONSULTAR_ESTADO_MODULO, ServiciosExternos.OBTENER_DIA_ANTERIOR})
    void losCuatroNombresVanAlRouterConSuClaseLegacyYElToken(String nombre) {
        Montaje m = montar("secreto-de-prueba");
        m.servidor().expect(requestTo(ROUTER + "/internal/java-call")).andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Token", "secreto-de-prueba"))
                .andExpect(jsonPath("$.class").value(CLASES.get(nombre)))
                .andExpect(jsonPath("$.params.p_IdEmpresa").value(1))
                .andRespond(withSuccess("{\"P_OUT_ERRORESCONTADOR\":0}", MediaType.APPLICATION_JSON));
        JsonObject entrada = JsonParser.parseString("{\"p_IdEmpresa\":1}").getAsJsonObject();

        JsonObject salida = m.colaboradores().invocar(nombre, entrada);

        assertThat(salida.get("P_OUT_ERRORESCONTADOR").getAsInt()).isZero();
        m.servidor().verify();
        verifyNoInteractions(m.servicio());
    }

    @Test
    void sinTokenConfiguradoNoSeEnviaLaCabecera() {
        Montaje m = montar("");
        m.servidor().expect(requestTo(ROUTER + "/internal/java-call")).andExpect(headerDoesNotExist("X-Internal-Token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        m.colaboradores().invocar(ServiciosExternos.OBTENER_DIA_ANTERIOR, new JsonObject());
        m.servidor().verify();
    }

    @Test
    void unaRespuestaNulaDelRouterEsNula() {
        Montaje m = montar("t");
        m.servidor().expect(requestTo(ROUTER + "/internal/java-call")).andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        assertThat(m.colaboradores().invocar(ServiciosExternos.CONSULTAR_ESTADO_MODULO, new JsonObject())).isNull();
    }

    @Test
    void cualquierOtroNombreEsDelAdaptadorDelServicio() {
        Montaje m = montar("t");
        JsonObject entrada = new JsonObject();
        JsonObject respuesta = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0}").getAsJsonObject();
        when(m.servicio().invocar(ServiciosExternos.AGREGAR_LOG, entrada)).thenReturn(respuesta);

        assertThat(m.colaboradores().invocar(ServiciosExternos.AGREGAR_LOG, entrada)).isSameAs(respuesta);
        verify(m.servicio()).invocar(ServiciosExternos.AGREGAR_LOG, entrada);
        m.servidor().verify();
    }
}
