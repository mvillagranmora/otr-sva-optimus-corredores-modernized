package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.ConfiguracionTipoCentralizacion;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.EntradaMultiSeleccion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaMultiSeleccion;
import cl.bcs.contabilidad.domain.port.in.CentralizacionUseCase;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Port-level multi-type centralization run: legacy CentralizaMultiSeleccion.logica (CentralizaMultiSeleccion.java:171-424)
 * for the Java types, dispatching to the {@link BcsCentralizacionService} beans instead of Class.forName.
 *
 * <p>Rules: B-01 (dispatch and accumulate results), B-03 (a failing type is logged, NOT counted, and the batch
 * continues: :361-366), B-05 (unknown id / type without Tipo / dedup, :197-238), B-06 (simulaOperacion and the
 * configuration's IdInforme are forwarded, :335-343), CONT-F-245..251 (vigentes and valuation-date checks only for a real
 * run in a CL installation, :240-270; the type's own errors and messages reach the output, :347-348).
 *
 * <p>Persistence is done by each type on a real run (see {@link CentralizadorSql}); the stored-procedure types, the
 * error header of failed types and the procedure messages are handled by the JavaCall entry point
 * (service/CentralizaMultiSeleccionService), not here.
 */
@Service
public class CentralizacionOrchestrator implements CentralizacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CentralizacionOrchestrator.class);

    private final PlataformaPort plataforma;
    private final ContabilidadRepository repository;
    private final Function<String, BcsCentralizacionService> centralizadores;

    /** Production constructor: the Java types are the Spring beans, keyed by bean name (= Servicio). */
    @Autowired
    public CentralizacionOrchestrator(PlataformaPort plataforma,
                                      ContabilidadRepository repository,
                                      Map<String, BcsCentralizacionService> services) {
        this(plataforma, repository, nombre -> {
            BcsCentralizacionService s = nombre == null ? null : services.get(nombre);
            if (s == null) throw new IllegalStateException("Centralizacion Java no disponible: " + nombre);
            return s;
        });
    }

    /**
     * Phase-1 constructor kept for the original orchestrator characterization tests: every Java type runs as
     * CentralizaAnticipoPactos, the only type the first pilot wired.
     */
    public CentralizacionOrchestrator(PlataformaPort plataforma, ContabilidadRepository repository) {
        this(plataforma, repository, nombre -> new CentralizaAnticipoPactos(plataforma, repository));
    }

    private CentralizacionOrchestrator(PlataformaPort plataforma, ContabilidadRepository repository,
                                       Function<String, BcsCentralizacionService> centralizadores) {
        this.plataforma = plataforma;
        this.repository = repository;
        this.centralizadores = centralizadores;
    }

    @Override
    public SalidaMultiSeleccion ejecutar(EntradaMultiSeleccion entrada) {
        // legacy :183-187 (EntradaCentralizacionInfoAdicional)
        String localizacion = plataforma.obtenerLocalizacionSistema(entrada.idEmpresa());
        plataforma.obtenerFechaSistema(entrada.idEmpresa());
        plataforma.obtenerMonedaSistema(entrada.idEmpresa());
        plataforma.consultarEmpresa(entrada.idEmpresa());

        List<Long> solicitados = new ArrayList<>(new LinkedHashSet<>(entrada.idsTipoCentralizacion()));
        List<ConfiguracionTipoCentralizacion> configuraciones = repository.queryConfiguraciones(solicitados);
        Map<Long, ConfiguracionTipoCentralizacion> porId = new LinkedHashMap<>();
        configuraciones.forEach(c -> porId.putIfAbsent(c.idTipoCentralizacion(), c));

        List<String> mensajes = new ArrayList<>();
        long errores = 0L;

        // :208-238 types to run: the requested ones (deduplicated) plus their related types
        List<ConfiguracionTipoCentralizacion> aProcesar = new ArrayList<>();
        Set<Long> procesados = new LinkedHashSet<>();
        for (Long id : solicitados) {
            ConfiguracionTipoCentralizacion cfg = porId.get(id);
            if (cfg == null) {
                mensajes.add(Tablas.mensaje("UI_CENTRALIZACION_NO_EXISTE", Map.of("Centralizacion", String.valueOf(id))));
            } else if (cfg.tipo() == null) {
                mensajes.add(Tablas.mensaje("UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE", Map.of("Centralizacion", String.valueOf(cfg.nombre()))));
            } else if (procesados.add(id)) {
                aProcesar.add(cfg);
                for (ConfiguracionTipoCentralizacion relacionada : configuraciones) {
                    if (id.equals(relacionada.idTipoCentralizacionOrigen()) && procesados.add(relacionada.idTipoCentralizacion())) {
                        aProcesar.add(relacionada);
                    }
                }
            }
        }

        if ("N".equals(entrada.simulaOperacion()) && "CL".equals(localizacion) && !procesados.isEmpty()) {
            // :240-263 already-posted types are rejected; with a single requested type the run stops there
            List<ConfiguracionTipoCentralizacion> vigentes = repository.queryCentralizacionesVigentes(
                    List.copyOf(procesados), entrada.fechaContable(), entrada.idEmpresa());
            for (ConfiguracionTipoCentralizacion vigente : vigentes) {
                errores++;
                mensajes.add(Tablas.mensaje("UI_CENTRALIZACION_CONTABILIZADA_ANULAR", Map.of("Centralizacion", String.valueOf(vigente.nombre()))));
                if (entrada.idsTipoCentralizacion().size() > 1) {
                    aProcesar.removeIf(c -> c.idTipoCentralizacion().equals(vigente.idTipoCentralizacion()));
                } else {
                    return new SalidaMultiSeleccion(errores, mensajes, List.of());
                }
            }
            // :266-270 valuation date after the accounting date: warning only (legacy's removeIf compared names with
            // ids and removed nothing)
            if (!fechasValidas(configuraciones, entrada.fechaConsulta(), entrada.fechaContable())) {
                mensajes.add("FECHA_VALORIZACION_INVALIDA");
            }
        }

        List<ResultadoCentralizacion> resultados = new ArrayList<>();
        for (ConfiguracionTipoCentralizacion cfg : aProcesar) {
            if (!"J".equals(cfg.tipo())) continue; // stored-procedure types: JavaCall entry point
            try {
                SalidaCentralizacion salida = centralizadores.apply(cfg.servicio()).centralizar(new EntradaCentralizacion(
                        entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(), cfg.idTipoCentralizacion(),
                        entrada.fechaContable(), cfg.idInforme(), entrada.ejercicio(), entrada.simulaOperacion()));
                errores += salida.erroresContador();
                mensajes.addAll(salida.mensajes());
                resultados.addAll(salida.resultados());
            } catch (RuntimeException e) {
                // B-03 (binding decision, legacy :361-366): logged, not counted, the batch continues
                log.error("Centralizacion del tipo {} fallo", cfg.idTipoCentralizacion(), e);
            }
        }
        return new SalidaMultiSeleccion(errores, mensajes, resultados);
    }

    /** Legacy validaFechasCentralizacion (:428-453). */
    private static boolean fechasValidas(List<ConfiguracionTipoCentralizacion> configuraciones, String fechaConsulta,
                                         String fechaContable) {
        boolean requiere = configuraciones.stream().anyMatch(c -> "S".equalsIgnoreCase(c.requiereFechaConsulta()));
        if (!requiere) return true;
        if (fechaConsulta == null) return false;
        return !LocalDate.parse(fechaConsulta).isAfter(LocalDate.parse(fechaContable));
    }
}
