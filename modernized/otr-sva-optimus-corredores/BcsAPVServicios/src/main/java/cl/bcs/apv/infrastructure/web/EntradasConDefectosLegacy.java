package cl.bcs.apv.infrastructure.web;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import cl.bcs.apv.api.CasoUso;
import cl.bcs.apv.api.casouso.CasosUso;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ObjectNode;

/**
 * Makes the REST input equal to the one the JavaCall adapter builds from the same (possibly partial) data (finding H3, user decision: the new service
 * ALWAYS replicates the legacy). The adapter ({@code MapeoJson}) gives a MISSING key the legacy default ({@code X.porDefecto()}: {@code 0L}, {@code ""},
 * {@code "N"}, {@code "LIQ"}, empty lists...), a JSON null stays null (a Java primitive component takes the default) and a present key its value. Plain
 * Jackson would give null to every missing component, hence NPE/500 or different SQL. Here the SAME rule is applied to every Entrada record of the 64
 * use cases and to every nested record (collected by reflection from {@code CasosUso.TODOS}, so a new operation is covered without editing this class):
 * <ul>
 *   <li>body (POST/PUT, {@code @RequestBody}): a Jackson deserializer per record type, registered on the message converter;</li>
 *   <li>query string (GET): {@link ResolverQuery} builds the object of the provided parameters and runs it through the SAME deserializers.</li>
 * </ul>
 */
final class EntradasConDefectosLegacy {

    private static final String PAQUETE_DTO = "cl.bcs.apv.api.dto.";

    private EntradasConDefectosLegacy() {
    }

    /** Entrada records of all use cases plus every record reachable from their components (nested types, list elements) having {@code porDefecto()}. */
    static Set<Class<?>> tiposConDefecto() {
        Set<Class<?>> tipos = new LinkedHashSet<>();
        for (Class<? extends CasoUso<?, ?>> caso : CasosUso.TODOS) {
            Class<?> entrada = ResolvableType.forClass(caso).as(CasoUso.class).getGeneric(0).resolve();
            recoger(entrada, tipos);
        }
        return tipos;
    }

    private static void recoger(Type tipo, Set<Class<?>> tipos) {
        ResolvableType rt = ResolvableType.forType(tipo);
        Class<?> clase = rt.resolve();
        if (clase == null) {
            return;
        }
        for (ResolvableType g : rt.getGenerics()) {
            recoger(g.getType(), tipos);
        }
        if (clase.isArray()) {
            recoger(clase.getComponentType(), tipos);
        }
        if (!clase.isRecord() || !clase.getName().startsWith(PAQUETE_DTO) || !tipos.add(clase)) {
            return;
        }
        try {
            clase.getMethod("porDefecto");
        } catch (NoSuchMethodException e) {
            tipos.remove(clase);
            return;
        }
        for (RecordComponent c : clase.getRecordComponents()) {
            recoger(c.getGenericType(), tipos);
        }
    }

    static SimpleModule modulo() {
        SimpleModule m = new SimpleModule("apv-entradas-defectos-legacy");
        for (Class<?> tipo : tiposConDefecto()) {
            registrar(m, tipo);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static <T> void registrar(SimpleModule m, Class<?> tipo) {
        m.addDeserializer((Class<T>) tipo, new DeserializadorConDefectos<>((Class<T>) tipo));
    }

    /** Replaces the JSON converter by one whose mapper carries the module; returns the mapper (also used for the query string). */
    static JsonMapper instalar(List<HttpMessageConverter<?>> convertidores) {
        SimpleModule modulo = modulo();
        JsonMapper elegido = null;
        for (int i = 0; i < convertidores.size(); i++) {
            if (convertidores.get(i) instanceof JacksonJsonHttpMessageConverter c) {
                JsonMapper nuevo = c.getMapper().rebuild().addModule(modulo).build();
                convertidores.set(i, new JacksonJsonHttpMessageConverter(nuevo));
                if (elegido == null) {
                    elegido = nuevo;
                }
            }
        }
        if (elegido == null) {
            elegido = JsonMapper.builder().addModule(modulo).build();
            convertidores.add(0, new JacksonJsonHttpMessageConverter(elegido));
        }
        return elegido;
    }

    /** Missing key = {@code porDefecto()} component; JSON null = null (primitive: the default); present = value converted to the component type. */
    static final class DeserializadorConDefectos<T> extends ValueDeserializer<T> {

        private final Class<T> tipo;
        private final RecordComponent[] componentes;
        private final Constructor<T> constructor;
        private final T defecto;

        DeserializadorConDefectos(Class<T> tipo) {
            this.tipo = tipo;
            this.componentes = tipo.getRecordComponents();
            try {
                Class<?>[] tipos = new Class<?>[componentes.length];
                for (int i = 0; i < tipos.length; i++) {
                    tipos[i] = componentes[i].getType();
                }
                this.constructor = tipo.getDeclaredConstructor(tipos);
                @SuppressWarnings("unchecked")
                T d = (T) tipo.getMethod("porDefecto").invoke(null);
                this.defecto = d;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Record " + tipo.getName() + " without canonical constructor or porDefecto()", e);
            }
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            JsonNode nodo = ctxt.readTree(p);
            if (nodo == null || nodo.isNull()) {
                return null;
            }
            if (!nodo.isObject()) {
                return (T) ctxt.reportInputMismatch(tipo, "Expected a JSON object for %s", tipo.getSimpleName());
            }
            return desdeNodo(nodo, ctxt);
        }

        T desdeNodo(JsonNode nodo, DeserializationContext ctxt) {
            Object[] args = new Object[componentes.length];
            try {
                for (int i = 0; i < args.length; i++) {
                    RecordComponent c = componentes[i];
                    JsonNode v = nodo.get(c.getName());
                    if (v == null) {
                        args[i] = c.getAccessor().invoke(defecto);
                    } else if (v.isNull()) {
                        args[i] = c.getType().isPrimitive() ? c.getAccessor().invoke(defecto) : null;
                    } else {
                        JavaType jt = ctxt.getTypeFactory().constructType(c.getGenericType());
                        args[i] = ctxt.readTreeAsValue(v, jt);
                    }
                }
                return constructor.newInstance(args);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot build " + tipo.getName(), e);
            }
        }
    }

    /** GET: the provided query parameters become the JSON object that {@link DeserializadorConDefectos} completes with the legacy defaults. */
    static final class ResolverQuery implements HandlerMethodArgumentResolver {

        private final Set<Class<?>> entradas = new LinkedHashSet<>();
        private final java.util.function.Supplier<JsonMapper> mapper;

        ResolverQuery(java.util.function.Supplier<JsonMapper> mapper) {
            this.mapper = mapper;
            for (Class<? extends CasoUso<?, ?>> caso : CasosUso.TODOS) {
                entradas.add(ResolvableType.forClass(caso).as(CasoUso.class).getGeneric(0).resolve());
            }
        }

        @Override
        public boolean supportsParameter(MethodParameter parametro) {
            return entradas.contains(parametro.getParameterType()) && !parametro.hasParameterAnnotation(RequestBody.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer contenedor, NativeWebRequest peticion,
                WebDataBinderFactory fabrica) {
            JsonMapper m = mapper.get();
            ObjectNode nodo = m.createObjectNode();
            for (Map.Entry<String, String[]> e : peticion.getParameterMap().entrySet()) {
                String[] valores = e.getValue();
                if (valores.length == 1) {
                    nodo.put(e.getKey(), valores[0]);
                } else {
                    var array = nodo.putArray(e.getKey());
                    for (String v : valores) {
                        array.add(v);
                    }
                }
            }
            try {
                return m.treeToValue(nodo, parametro.getParameterType());
            } catch (JacksonException ex) {
                throw new HttpMessageNotReadableException("Invalid query parameters: " + ex.getOriginalMessage(), ex, null);
            }
        }
    }
}
