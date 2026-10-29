package cl.bcs.plataforma.jobs.port.out;

import cl.bcs.plataforma.jobs.domain.DetalleEjecucion;
import cl.bcs.plataforma.jobs.domain.Ejecucion;
import cl.bcs.plataforma.jobs.domain.EstadoModulo;
import cl.bcs.plataforma.jobs.domain.ProcesoPauta;

import java.util.List;

/**
 * Output port for persisting pauta execution state to HANA.
 */
public interface PautaRepositoryPort {

    List<EstadoModulo> findEstadoModulos();

    List<ProcesoPauta> findProcesosPauta(Long idEmpresa, List<String> codigosProceso,
                                          int filtroCodigoProceso, String ejecutarDiaInhabil);

    List<Ejecucion> findEjecucion(Long idEmpresa, String fechaSistema);

    void insertEjecucion(Ejecucion ejecucion);

    void updateEjecucion(Ejecucion ejecucion);

    void insertDetalleEjecucion(DetalleEjecucion detalle);

    void updateDetalleEjecucion(DetalleEjecucion detalle);

    List<DetalleEjecucion> findDetalleEjecucion(Long idEmpresa, String fechaSistema);
}
