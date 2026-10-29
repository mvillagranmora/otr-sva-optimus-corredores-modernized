package cl.bcs.fondomutuo.jobs.adapter.out;

import cl.bcs.fondomutuo.jobs.domain.CustodiaPortafolio;
import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Phase 2 stub implementation of FondoMutuoServicioPort.
 *
 * All methods throw UnsupportedOperationException. Phase 3 will replace this
 * with actual REST client calls to BcsFondoMutuoServicios and related services.
 *
 * Configuration (Phase 3): services.fondo-mutuo.base-url, services.custodia.base-url.
 */
@Component
public class FondoMutuoServicioRestClient implements FondoMutuoServicioPort {

    private static final String PHASE3_MSG =
            "requires Phase 3 REST client — configure services.fondo-mutuo.base-url";

    @Override
    public Map<String, Object> ejecutarAdministracionLiquidez(Long idEmpresa, Long idUsuario,
            String idModulo, String simularOperacion, String usaReglasNegocio) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> ejecutarBloqueoCierreSeries(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException("requires HANA: CierreBloqueoAutomaticoFondos stored procedure");
    }

    @Override
    public Map<String, Object> procesarCargaMasivaSolicitudes(Long idEmpresa, Long idUsuario,
            String idModulo, List<Long> idCargas) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public List<Map<String, Object>> consultarSolicitudesParaAGF(Long idEmpresa, Long idUsuario,
            String idModulo) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public void cambiarEstadoSolicitudesAGF(Long idEmpresa, List<Long> idSolicitudes,
            Long idEstado, String observacion) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> certificarPorFiltros(Long idEmpresa, Long idUsuario, String idModulo,
            String simularOperacion, String usaReglasNegocio, String fechaCertificacion,
            List<Map<String, Object>> filtros) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> certificarSolicitudes(Long idEmpresa, Long idUsuario, String idModulo,
            String simularOperacion, String usaReglasNegocio, String fechaCertificacion,
            List<Long> idSolicitudes) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> generarCertificado31Masivo(Long idEmpresa, Long idUsuario, String idModulo,
            String ciudad, String fecha, List<Long> idTraspasos) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> generarInformeRentabilidadExtranjero(Long idEmpresa, Long idUsuario,
            String idModulo, String agnoComercial, String esMasivo, List<Long> idClientes) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> procesarTrxCFileManager(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> procesarTrxPFileManager(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> procesarValorCuotaFileManager(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(
                "requires HANA: CargarValorCuota uses #temp_table input parameter syntax");
    }

    @Override
    public Map<String, Object> procesarValorCuotaInternacionalFileManager(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(
                "requires HANA: ValorCuotaInternacional uses #temp_table input parameter syntax");
    }

    @Override
    public Map<String, Object> generarArchivoAGF(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaDesde, String fechaHasta, String idAmbiente, String ruta) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> confirmarTraspaso(Long idEmpresa, Long idUsuario, String idModulo,
            List<Long> idTraspasos) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> generarArchivoMovimientos(Long idEmpresa, Long idUsuario, String fechaCurse,
            String fondoMutuo, String emisor, String estado, String tipoSolicitud, String idMoneda) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> generarArchivoParticipes(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaCurse, String fondoMutuo, String emisor, String estado,
            String tipoSolicitud, String idMoneda) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public void notificarTraspaso(Long idEmpresa, Long idUsuario, String idModulo) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> procesarComisionesNN(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaDesde, String fechaHasta, List<Long> idCarteras) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public Map<String, Object> generarArchivoSolicitudAporteRescate(Long idEmpresa, Long idUsuario,
            String idModulo, String fechaDesde, String fechaHasta, Map<String, Object> filtros) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }

    @Override
    public List<CustodiaPortafolio> consultarCustodiaPortafolio(Long idEmpresa, Long idUsuario,
            String idModulo, Long idCliente, String fechaConsulta) {
        throw new UnsupportedOperationException("requires Phase 3 REST client — services.custodia.base-url");
    }

    @Override
    public Map<String, Object> crearSolicitudRescateReinversion(Long idEmpresa, Long idUsuario,
            String idModulo, List<Map<String, Object>> solicitudesRescate,
            List<Map<String, Object>> solicitudesAporte) {
        throw new UnsupportedOperationException(PHASE3_MSG);
    }
}
