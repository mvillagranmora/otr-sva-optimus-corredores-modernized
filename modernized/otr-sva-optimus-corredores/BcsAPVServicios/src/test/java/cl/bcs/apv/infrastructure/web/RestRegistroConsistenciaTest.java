package cl.bcs.apv.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;
import cl.bcs.apv.api.CasoUso;
import cl.bcs.apv.api.casouso.CasosUso;

/**
 * REST / registry CONSISTENCY for all 64 operations: every invocable entry of the JavaCall registry ({@code ApvJavaCallRegistro}, the catalogue the gateway
 * uses) has a REST endpoint with the SAME verb and route, secured by the {@code TokenInternoFilter} allow-list, whose handler takes the typed Entrada and
 * answers the typed Resultado of that use case, and that calls EXACTLY that use case (not a neighbour). Conversely no endpoint of the three typed controllers
 * exists without a registry entry. The use cases are mocks: what is under test is the wiring routes -> controller -> use case interface.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestRegistroConsistenciaTest.Config.class)
class RestRegistroConsistenciaTest {

    private static final String TOKEN = "token-de-prueba";

    @Configuration
    @EnableWebMvc
    @Import({ ContratosController.class, TraspasosController.class, RecaudacionController.class, ApvExceptionHandler.class })
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

        @Bean
        TokenInternoFilter filtro() {
            return new TokenInternoFilter(TOKEN);
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private RequestMappingHandlerMapping mapeos;

    @Autowired
    private TokenInternoFilter filtro;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
    }

    private static List<OperacionRegistrada> operacionesRegistradas() {
        return new ApvJavaCallRegistro().todas().stream().filter(OperacionRegistrada::invocable).toList();
    }

    private static String clave(String verbo, String ruta) {
        return verbo + " " + ruta;
    }

    /** verb + route of every handler of the three typed controllers -> its handler method. */
    private Map<String, HandlerMethod> endpoints() {
        Map<String, HandlerMethod> m = new java.util.TreeMap<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : mapeos.getHandlerMethods().entrySet()) {
            Class<?> tipo = e.getValue().getBeanType();
            if (!tipo.getPackageName().equals("cl.bcs.apv.infrastructure.web")) {
                continue;
            }
            Set<String> rutas = e.getKey().getPathPatternsCondition().getPatternValues();
            for (String ruta : rutas) {
                for (var verbo : e.getKey().getMethodsCondition().getMethods()) {
                    assertThat(m.put(clave(verbo.name(), ruta), e.getValue())).as("duplicate endpoint " + verbo + " " + ruta).isNull();
                }
            }
        }
        return m;
    }

    @Test
    void elRegistroTiene64OperacionesInvocablesYElContratoTodos() {
        assertThat(operacionesRegistradas()).hasSize(64);
        assertThat(CasosUso.TODOS).hasSize(64);
        assertThat(operacionesRegistradas().stream().map(OperacionRegistrada::interfaz).toList()).containsExactlyInAnyOrderElementsOf(CasosUso.TODOS);
    }

    @Test
    void cadaOperacionDelRegistroTieneSuEndpointConElMismoVerboYRuta() {
        Map<String, HandlerMethod> endpoints = endpoints();
        List<String> faltan = new ArrayList<>();
        for (OperacionRegistrada op : operacionesRegistradas()) {
            if (!endpoints.containsKey(clave(op.verbo(), op.ruta()))) {
                faltan.add(clave(op.verbo(), op.ruta()) + " (" + op.casoUso() + ")");
            }
        }
        assertThat(faltan).as("registry operations without a REST endpoint").isEmpty();
    }

    @Test
    void ningunEndpointTipadoExisteSinEntradaEnElRegistro() {
        Set<String> delRegistro = new TreeSet<>();
        operacionesRegistradas().forEach(op -> delRegistro.add(clave(op.verbo(), op.ruta())));
        assertThat(endpoints().keySet()).containsExactlyInAnyOrderElementsOf(delRegistro);
    }

    @Test
    void elHandlerRecibeLaEntradaTipadaYDevuelveElResultadoTipadoDeSuCasoDeUso() {
        Map<String, HandlerMethod> endpoints = endpoints();
        for (OperacionRegistrada op : operacionesRegistradas()) {
            HandlerMethod h = endpoints.get(clave(op.verbo(), op.ruta()));
            ResolvableType casoUso = ResolvableType.forClass(op.interfaz()).as(CasoUso.class);
            Class<?> entrada = casoUso.getGeneric(0).resolve();
            Class<?> resultado = casoUso.getGeneric(1).resolve();
            Method m = h.getMethod();
            assertThat(m.getParameterTypes()).as(op.casoUso() + " parameters").containsExactly(entrada);
            assertThat(m.getReturnType()).as(op.casoUso() + " return").isEqualTo(resultado);
            Class<?> beanType = h.getBeanType();
            boolean inyectaElCaso = java.util.Arrays.stream(beanType.getDeclaredConstructors()).anyMatch(c -> java.util.Arrays.asList(c.getParameterTypes()).contains(op.interfaz()));
            assertThat(inyectaElCaso).as(beanType.getSimpleName() + " injects " + op.interfaz().getSimpleName()).isTrue();
            // the verb annotation is the registry's
            switch (op.verbo()) {
                case "POST" -> assertThat(m.getAnnotation(PostMapping.class)).as(op.casoUso()).isNotNull();
                case "PUT" -> assertThat(m.getAnnotation(PutMapping.class)).as(op.casoUso()).isNotNull();
                case "GET" -> assertThat(m.getAnnotation(GetMapping.class)).as(op.casoUso()).isNotNull();
                default -> throw new AssertionError("verb " + op.verbo());
            }
        }
    }

    @Test
    void todoEndpointExigeElTokenYLlamaExactamenteAlCasoDeUsoDelRegistro() throws Exception {
        for (OperacionRegistrada op : operacionesRegistradas()) {
            Object caso = contexto.getBean(op.interfaz());
            org.mockito.Mockito.clearInvocations(caso);

            // without the token: 401 and the use case is not reached
            status401(peticion(op, false));
            verify((CasoUso<Object, Object>) caso, never()).ejecutar(any());

            // with the token: the controller of THAT use case runs it exactly once
            int estado = mvc().perform(peticion(op, true)).andReturn().getResponse().getStatus();
            assertThat(estado).as(op.verbo() + " " + op.ruta() + " with token").isNotEqualTo(401).isNotEqualTo(404).isNotEqualTo(405);
            verify((CasoUso<Object, Object>) caso).ejecutar(any());
            for (Class<? extends CasoUso<?, ?>> otro : CasosUso.TODOS) {
                if (otro != op.interfaz()) {
                    org.mockito.Mockito.verifyNoInteractions(contexto.getBean(otro));
                }
            }
            for (Class<? extends CasoUso<?, ?>> otro : CasosUso.TODOS) {
                org.mockito.Mockito.clearInvocations(contexto.getBean(otro));
            }
        }
    }

    private void status401(MockHttpServletRequestBuilder b) throws Exception {
        mvc().perform(b).andExpect(status().isUnauthorized());
    }

    private static MockHttpServletRequestBuilder peticion(OperacionRegistrada op, boolean conToken) {
        MockHttpServletRequestBuilder b = switch (op.verbo()) {
            case "GET" -> get(op.ruta());
            case "PUT" -> put(op.ruta()).contentType(MediaType.APPLICATION_JSON).content("{}");
            default -> post(op.ruta()).contentType(MediaType.APPLICATION_JSON).content("{}");
        };
        return conToken ? b.header("X-Internal-Token", TOKEN) : b;
    }
}
