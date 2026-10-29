package cl.bcs.apv.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.apv.api.CasoUso;
import cl.bcs.apv.api.casouso.CasosUso;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogica;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Composition root of the use cases WITHOUT the web application: builds one instance of every use case implementation, keyed by its use case
 * interface ({@link CasosUso#TODOS}), on the six ports and the {@code NamedParameterJdbcTemplate} it is given. The characterization pipeline
 * ({@code ApvPipelineDeCaso}) and any in-process caller use it. HAND WRITTEN (the generator's skeleton of this class is superseded: do not regenerate it).
 *
 * <p>There is NO per-service wiring code: it is the SAME dependency injection as production, on a small Spring context that holds the given ports as
 * singletons and registers (a) every concrete class of {@code cl.bcs.apv.application} that implements {@link CasoUso} and (b) every
 * {@code @Repository} / {@code @Component} of {@code cl.bcs.apv.infrastructure.persistence} (the persistence adapters). A service therefore declares
 * what it needs in its ONE public constructor, by type: the ports, a repository port, or another use case INTERFACE (a peer is injected typed, never
 * routed by name). A new service or repository needs no edit here. Transaction proxies are not applied (the caller owns the connection); the
 * real-wiring test {@code ApvCableadoRealTest} proves the same graph under Spring's transaction manager.
 */
public final class ApvCasosDeUso {

    private static final String PAQUETE_CASOS = "cl.bcs.apv.application";
    private static final String PAQUETE_PERSISTENCIA = "cl.bcs.apv.infrastructure.persistence";

    private static volatile List<Class<?>> clases;

    private ApvCasosDeUso() {
    }

    public static Map<Class<?>, Object> crear(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        GenericApplicationContext contexto = new GenericApplicationContext();
        contexto.setAllowCircularReferences(false);
        contexto.registerBean(NamedParameterJdbcTemplate.class, () -> jdbc);
        contexto.registerBean(ConfiguracionSistema.class, () -> configuracion);
        contexto.registerBean(DiccionarioMensajes.class, () -> diccionario);
        contexto.registerBean(Secuencias.class, () -> secuencias);
        contexto.registerBean(Reloj.class, () -> reloj);
        contexto.registerBean(AlmacenArchivos.class, () -> archivos);
        contexto.registerBean(Colaboradores.class, () -> colaboradores);
        for (Class<?> c : clasesDelModulo()) {
            contexto.registerBean(c);
        }
        contexto.refresh();
        Map<Class<?>, Object> mapa = new LinkedHashMap<>();
        for (Class<? extends CasoUso<?, ?>> caso : CasosUso.TODOS) {
            mapa.put(caso, contexto.getBean(caso));
        }
        // typed in-process entry point that is NOT a JavaCall operation (not in CasosUso.TODOS, not in the registry, no route)
        mapa.put(ValidarMandatosPACLogica.class, contexto.getBean(ValidarMandatosPACLogica.class));
        return mapa;
    }

    /** The use case implementations and persistence adapters found on the classpath (scanned once: the scan reads class files). */
    private static List<Class<?>> clasesDelModulo() {
        List<Class<?>> encontradas = clases;
        if (encontradas == null) {
            synchronized (ApvCasosDeUso.class) {
                if (clases == null) {
                    clases = escanear();
                }
                encontradas = clases;
            }
        }
        return encontradas;
    }

    private static List<Class<?>> escanear() {
        Set<String> nombres = new TreeSet<>();
        ClassPathScanningCandidateComponentProvider casos = new ClassPathScanningCandidateComponentProvider(false);
        casos.addIncludeFilter(new AssignableTypeFilter(CasoUso.class));
        casos.addIncludeFilter(new AssignableTypeFilter(ValidarMandatosPACLogica.class));
        for (BeanDefinition d : casos.findCandidateComponents(PAQUETE_CASOS)) {
            nombres.add(d.getBeanClassName());
        }
        ClassPathScanningCandidateComponentProvider persistencia = new ClassPathScanningCandidateComponentProvider(true);
        for (BeanDefinition d : persistencia.findCandidateComponents(PAQUETE_PERSISTENCIA)) {
            nombres.add(d.getBeanClassName());
        }
        List<Class<?>> lista = new ArrayList<>();
        for (String nombre : nombres) {
            try {
                lista.add(Class.forName(nombre));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot load " + nombre, e);
            }
        }
        return List.copyOf(lista);
    }
}
