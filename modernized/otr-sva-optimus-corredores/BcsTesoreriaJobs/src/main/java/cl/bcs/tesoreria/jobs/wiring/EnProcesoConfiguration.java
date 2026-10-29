package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.apv.adapter.javacall.ApvJavaCallPipeline;
import cl.bcs.apv.adapter.javacall.ApvJavaCallPipelineEnProceso;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.application.ApvCasosDeUso;
import cl.bcs.apv.infrastructure.adapter.AlmacenArchivosLocal;
import cl.bcs.apv.infrastructure.adapter.ConfiguracionSistemaJdbc;
import cl.bcs.apv.infrastructure.adapter.DiccionarioJdbc;
import cl.bcs.apv.infrastructure.adapter.ProcedimientoAlmacenado;
import cl.bcs.apv.infrastructure.adapter.RelojSistema;
import cl.bcs.apv.infrastructure.adapter.SecuenciasJdbc;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.tesoreria.jobs.adapter.ColaboradoresApvEnProceso;
import cl.bcs.tesoreria.jobs.adapter.ColaboradoresCuentaInversionEnProceso;
import cl.bcs.tesoreria.jobs.adapter.ConfiguracionCuentaInversionJdbc;
import cl.bcs.tesoreria.jobs.adapter.DespachadorEnProceso;
import cl.bcs.tesoreria.jobs.adapter.EntradaEnProceso;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Step 5 (JOBS_PATTERN.md H-1): collaborators that are ALREADY migrated as libraries run in process, in the transaction of the job. Active only with
 * {@code tesoreria.jobs.colaboradores-en-proceso=true}.
 *
 * <p>RESOLUTION OF THE Tesoreria &lt;-&gt; APV CYCLE (JOBS_H1_H4_INVENTORY.md C.6.5). Tesoreria calls APV ({@code ValidarMandatosPAC},
 * {@code CambiarEstadoContratosSAAPV}) and APV calls Tesoreria ({@code CrearIngresoEgreso}, {@code LiquidarComprobante}, {@code CrearComprobante},
 * the three {@code *MandatoPAC}). No library depends on another: each keeps its own {@code Colaboradores} port, and THIS class (the jobs application)
 * gives every port the same {@link DespachadorEnProceso}, which holds the three in-process entry points of the owners (Tesoreria's and
 * CuentaInversion's {@code JavaCallDispatcher}, APV's {@code ApvJavaCallPipelineEnProceso}). The entry points are used lazily by name, so the beans
 * form no Spring cycle either (the dispatchers look their services up at call time; the APV use case graph is built on first use).
 *
 * <p>The APV use cases are built with {@code ApvCasosDeUso.crear}, the composition root that apv-service documents for in-process callers: plain
 * instances (no transaction proxy) on the {@code NamedParameterJdbcTemplate} of this application, so they use the connection the job transaction bound
 * to the thread. The Tesoreria and CuentaInversion services are Spring beans {@code @Transactional} (REQUIRED) on the same {@code DataSource} and join it.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ColaboradoresJobsConfiguration.PROPIEDAD, havingValue = "true")
@Import(ServiciosCuentaInversionEnProcesoConfiguration.class)
public class EnProcesoConfiguration {

    /**
     * The CuentaInversion classes whose beans are scanned ({@link ServiciosCuentaInversionEnProcesoConfiguration}): the dispatcher knows 40 names but
     * only these two services exist in this application, so only these two are claimed. Any other CuentaInversion class goes over HTTP.
     */
    static final Set<String> CUENTA_INVERSION = Set.of("cl.bcs.cuentaInversion.CrearCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono");

    @Bean
    EntradaEnProceso entradaTesoreria(cl.bcs.tesoreria.javacall.JavaCallDispatcher dispatcher) {
        return EntradaEnProceso.de("tesoreria", dispatcher::maneja, dispatcher::despachar);
    }

    @Bean
    EntradaEnProceso entradaCuentaInversion(cl.bcs.cuentainversion.service.JavaCallDispatcher dispatcher) {
        return EntradaEnProceso.de("cuenta-inversion", c -> CUENTA_INVERSION.contains(c) && dispatcher.maneja(c), dispatcher::despachar);
    }

    /**
     * APV: legacy class name + legacy JSON -> typed use case -> legacy JSON ({@code ApvJavaCallPipelineEnProceso}). A legacy class whose {@code call()}
     * returns null ({@code cl.bcs.APV.ValidarMandatosPAC}, the JavaCall {@code call()}) answers null without building anything, as the legacy does. The
     * typed operation {@code cl.bcs.APV.ValidarMandatosPAC.logica} (what the job BcsJobProcesarDistribucion called) is also claimed: it runs the real
     * use case, which updates {@code LogMandatoPAC} on the connection of the job transaction.
     */
    @Bean
    EntradaEnProceso entradaApv(NamedParameterJdbcTemplate jdbc, ObjectProvider<DespachadorEnProceso> despachador,
                                @Value("${services.javacall.url}") String urlRouter,
                                @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                                @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                                @Value("${services.internal-token:}") String tokenInterno) {
        ApvJavaCallRegistro registro = new ApvJavaCallRegistro();
        Supplier<ApvJavaCallPipeline> pipeline = memoizado(() -> {
            cl.bcs.apv.domain.port.out.Colaboradores colaboradores = new ColaboradoresApvEnProceso(despachador.getObject(),
                    new cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall(new ProcedimientoAlmacenado(jdbc), urlRouter, tiempoConexion,
                            tiempoLectura, tokenInterno));
            return new ApvJavaCallPipelineEnProceso(registro, ApvCasosDeUso.crear(jdbc, new ConfiguracionSistemaJdbc(jdbc),
                    new DiccionarioJdbc(jdbc), new SecuenciasJdbc(jdbc), new RelojSistema(), new AlmacenArchivosLocal(""), colaboradores));
        });
        return EntradaEnProceso.de("apv", c -> ApvJavaCallPipelineEnProceso.esOperacionTipada(c) || registro.buscar(c).isPresent(), (c, entrada) -> {
            // the typed operation (ValidarMandatosPAC.logica) is not in the registry: the pipeline serves it itself
            if (!ApvJavaCallPipelineEnProceso.esOperacionTipada(c) && !registro.buscar(c).orElseThrow().invocable()) {
                return null;
            }
            return pipeline.get().invocar(c, entrada);
        });
    }

    @Bean
    DespachadorEnProceso despachadorEnProceso(List<EntradaEnProceso> entradas) {
        return new DespachadorEnProceso(entradas);
    }

    /** Port of the CuentaInversion services that run in this application (their library ports are REST clients and are not scanned). */
    @Bean
    ConfiguracionSistemaPort configuracionCuentaInversion(ConfiguracionSistema configuracion) {
        return new ConfiguracionCuentaInversionJdbc(configuracion);
    }

    @Bean
    cl.bcs.cuentainversion.port.out.Colaboradores colaboradoresCuentaInversion(DespachadorEnProceso despachador,
                                                                              @Value("${services.javacall.url:}") String urlRouter,
                                                                              @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                                                                              @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                                                                              @Value("${services.internal-token:}") String tokenInterno) {
        return new ColaboradoresCuentaInversionEnProceso(despachador, urlRouter, tiempoConexion, tiempoLectura, tokenInterno);
    }

    private static <T> Supplier<T> memoizado(Supplier<T> origen) {
        return new Supplier<>() {
            private volatile T valor;

            @Override
            public T get() {
                T v = valor;
                if (v == null) {
                    synchronized (this) {
                        v = valor;
                        if (v == null) {
                            v = origen.get();
                            valor = v;
                        }
                    }
                }
                return v;
            }
        };
    }
}
