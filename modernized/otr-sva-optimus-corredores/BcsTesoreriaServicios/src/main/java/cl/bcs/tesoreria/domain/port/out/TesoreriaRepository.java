package cl.bcs.tesoreria.domain.port.out;

import cl.bcs.tesoreria.domain.model.*;
import java.util.List;

public interface TesoreriaRepository {

    // IngresoEgreso
    void persistirIngresoEgreso(MovimientoRequest movimiento);

    // LiquidarComprobante
    ComprobanteInfo consultarComprobante(Long idComprobante);
    List<Long> consultarFormasDePago(Long idComprobante, Long idEmpresa);
    void persistirLiquidacion(ComprobanteInfo info);

    // GirarComprobante
    List<String> verificarRequisitosGirarIngreso(Long idComprobante);
    List<String> verificarRequisitosGirarEgreso(Long idComprobante);
    Long crearComprobante(GirarComprobanteIngresoRequest req);
    void persistirGirarComprobanteIngreso(GirarComprobanteIngresoRequest req);
    void persistirGirarComprobantesEgreso(GirarComprobantesEgresoRequest req);

    // ConfirmarRechazarAnular
    List<Long> consultarMovimientosRechazados(List<Long> ids);
    void rechazarAnularMovimientosIngresoEgreso(List<Long> ids);

    // MandatoPAC
    Long insertarMandatoPAC(InIngresoPAC ingreso);
    List<Long> consultarContratosCSCRelacionados(Long idMandato);
    void actualizarEstadoMandato(Long idMandato, String estado);

    // Orchestrator
    List<ConfiguracionServicioTesoreria> consultarConfiguracionServicios(List<String> nombres);
    List<ResultadoServicioTesoreria> ejecutarServicioTesoreria(String beanName, ContextoServicioTesoreria ctx);
    void persistirResultado(SalidaOrquestadorTesoreria resultado);
}
