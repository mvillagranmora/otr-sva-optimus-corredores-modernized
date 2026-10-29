package cl.bcs.apv.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;
import cl.bcs.apv.api.CasoUso;
import cl.bcs.apv.api.casouso.CasosUso;
import cl.bcs.apv.api.casouso.ConsultarCustodiaPortafolio;
import cl.bcs.apv.api.casouso.CrearContrato;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioEntrada;
import cl.bcs.apv.api.dto.crearcontrato.CrearContratoEntrada;

/**
 * Finding H3: through REST a missing field/parameter must give the use case EXACTLY the input the JavaCall adapter builds from the same data (legacy
 * defaults: {@code 0L}, {@code ""}, {@code "N"}, {@code "LIQ"}, empty lists...). For ALL registered operations: empty body (POST/PUT) or no query
 * parameters (GET) reaches the use case as {@code equals} to {@code mapeo.entradaDesdeJson({})}; plus the custodia GET example and partial/nested bodies.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEntradaConDefectosLegacyTest.Config.class)
class RestEntradaConDefectosLegacyTest {

    @Configuration
    @EnableWebMvc
    @Import({ ContratosController.class, TraspasosController.class, RecaudacionController.class, ApvExceptionHandler.class,
            EntradasConDefectosLegacyConfig.class })
    static class Config {

        @Bean
        static org.springframework.beans.factory.config.BeanFactoryPostProcessor casosDeUsoFalsos() {
            return bf -> {
                for (Class<? extends CasoUso<?, ?>> caso : CasosUso.TODOS) {
                    registrar((org.springframework.beans.factory.support.BeanDefinitionRegistry) bf, caso);
                }
            };
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static void registrar(org.springframework.beans.factory.support.BeanDefinitionRegistry registro, Class caso) {
            registro.registerBeanDefinition("mock" + caso.getSimpleName(),
                    org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition(caso, () -> mock(caso)).getBeanDefinition());
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    static Stream<OperacionRegistrada> operaciones() {
        return new ApvJavaCallRegistro().todas().stream().filter(OperacionRegistrada::invocable);
    }

    @Test
    void sonLas64Operaciones() {
        assertThat(operaciones().count()).isEqualTo(64);
    }

    @SuppressWarnings("unchecked")
    private Object entradaRecibida(Class<?> interfaz, MockHttpServletRequestBuilder peticion) throws Exception {
        CasoUso<Object, Object> caso = (CasoUso<Object, Object>) contexto.getBean(interfaz);
        org.mockito.Mockito.clearInvocations(caso);
        int estado = mvc().perform(peticion).andReturn().getResponse().getStatus();
        assertThat(estado).as(interfaz.getSimpleName()).isEqualTo(200);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        org.mockito.Mockito.verify(caso).ejecutar(captor.capture());
        return captor.getValue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operaciones")
    void sinDatosElCasoDeUsoRecibeLaMismaEntradaQueElAdaptadorConstruyeDeVacio(OperacionRegistrada op) throws Exception {
        MockHttpServletRequestBuilder peticion = switch (op.verbo()) {
            case "GET" -> get(op.ruta());
            case "PUT" -> put(op.ruta()).contentType(MediaType.APPLICATION_JSON).content("{}");
            default -> post(op.ruta()).contentType(MediaType.APPLICATION_JSON).content("{}");
        };
        Object esperada = op.mapeo().entradaDesdeJson(new JsonObject());
        assertThat(esperada).isNotNull();
        assertThat(entradaRecibida(op.interfaz(), peticion)).as(op.casoUso()).isEqualTo(esperada);
    }

    @Test
    void custodiaSinIdClienteRecibeLosDefectosLegacy() throws Exception {
        Object recibida = entradaRecibida(ConsultarCustodiaPortafolio.class, get("/apv/portafolios/custodia").param("idEmpresa", "7"));
        JsonObject legacy = JsonParser.parseString("{\"p_IdEmpresa\":7}").getAsJsonObject();
        assertThat(recibida).isEqualTo(
                cl.bcs.apv.adapter.javacall.mapeo.ConsultarCustodiaPortafolioMapeo.INSTANCIA.entradaDesdeJson(legacy));
        ConsultarCustodiaPortafolioEntrada e = (ConsultarCustodiaPortafolioEntrada) recibida;
        assertThat(e.idEmpresa()).isEqualTo(7L);
        assertThat(e.idCliente()).isEqualTo(0L);
        assertThat(e.idPortafolio()).isEqualTo(0L);
        assertThat(e.agrupaPorBoveda()).isEqualTo("N");
        assertThat(e.tipoFechaConsulta()).isEqualTo("LIQ");
        assertThat(e.fechaConsulta()).isEqualTo("0001-01-01");
    }

    @Test
    void custodiaConParametrosLosRespetaYLosDemasTomanElDefecto() throws Exception {
        Object recibida = entradaRecibida(ConsultarCustodiaPortafolio.class,
                get("/apv/portafolios/custodia").param("idCliente", "55").param("agrupaPorBoveda", "S"));
        ConsultarCustodiaPortafolioEntrada e = (ConsultarCustodiaPortafolioEntrada) recibida;
        assertThat(e.idCliente()).isEqualTo(55L);
        assertThat(e.agrupaPorBoveda()).isEqualTo("S");
        assertThat(e.agrupaPorRelacion()).isEqualTo("N");
    }

    @Test
    void cuerpoParcialConAnidadosCompletaCadaNivelConSuDefectoLegacy() throws Exception {
        Object recibida = entradaRecibida(CrearContrato.class, post("/apv/contratos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idEmpresa\":5,\"contrato\":[{\"rut\":\"1-9\"},{\"idCliente\":3}]}"));
        JsonObject legacy = JsonParser
                .parseString("{\"p_IdEmpresa\":5,\"p_Contrato\":[{\"Rut\":\"1-9\"},{\"IdCliente\":3}]}").getAsJsonObject();
        assertThat(recibida).isEqualTo(cl.bcs.apv.adapter.javacall.mapeo.CrearContratoMapeo.INSTANCIA.entradaDesdeJson(legacy));
        CrearContratoEntrada e = (CrearContratoEntrada) recibida;
        assertThat(e.contrato()).hasSize(2);
        assertThat(e.contrato().get(0).idContratoSAAPV()).isEqualTo(0L);
        assertThat(e.contrato().get(0).estado()).isEqualTo("");
        assertThat(e.contrato().get(1).idCliente()).isEqualTo(3L);
    }

    @Test
    void sinListaElDefectoEsLaListaVacia() throws Exception {
        CrearContratoEntrada e = (CrearContratoEntrada) entradaRecibida(CrearContrato.class,
                post("/apv/contratos").contentType(MediaType.APPLICATION_JSON).content("{\"idEmpresa\":5}"));
        assertThat(e.contrato()).isEqualTo(List.of());
    }
}
