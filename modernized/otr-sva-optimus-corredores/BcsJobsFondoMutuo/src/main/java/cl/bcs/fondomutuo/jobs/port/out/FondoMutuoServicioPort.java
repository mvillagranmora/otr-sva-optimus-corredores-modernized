package cl.bcs.fondomutuo.jobs.port.out;

import cl.bcs.fondomutuo.jobs.domain.CustodiaPortafolio;

import java.util.List;
import java.util.Map;

/**
 * Port for calling BcsFondoMutuoServicios (and related services) via REST.
 *
 * All methods represent calls to the FFMM services microservice.
 * Phase 2 stub throws UnsupportedOperationException for all operations.
 * Phase 3 wires a RestClient implementation.
 *
 * Parameters are passed as generic Maps to avoid coupling to a specific DTO
 * library at the port level; adapters serialize/deserialize as needed.
 */
public interface FondoMutuoServicioPort {

    /** AdministracionLiquidez service call. */
    Map<String, Object> ejecutarAdministracionLiquidez(Long idEmpresa, Long idUsuario, String idModulo,
            String simularOperacion, String usaReglasNegocio);

    /** CierreBloqueoAutomaticoFondos HANA SP — requires HANA. */
    Map<String, Object> ejecutarBloqueoCierreSeries(Long idEmpresa, Long idUsuario);

    /** ProcesarCargaSolicitudesAporteRescate service call. */
    Map<String, Object> procesarCargaMasivaSolicitudes(Long idEmpresa, Long idUsuario, String idModulo,
            List<Long> idCargas);

    /** ConsultarSolicitudesParaAGF service call. */
    List<Map<String, Object>> consultarSolicitudesParaAGF(Long idEmpresa, Long idUsuario, String idModulo);

    /** CambiarEstadoSolicitudAgf service call. */
    void cambiarEstadoSolicitudesAGF(Long idEmpresa, List<Long> idSolicitudes, Long idEstado, String observacion);

    /** CertificarPorFiltrosSolicitudAporteRescate service call. */
    Map<String, Object> certificarPorFiltros(Long idEmpresa, Long idUsuario, String idModulo,
            String simularOperacion, String usaReglasNegocio, String fechaCertificacion,
            List<Map<String, Object>> filtros);

    /** CertificarSolicitudAporteRescate service call. */
    Map<String, Object> certificarSolicitudes(Long idEmpresa, Long idUsuario, String idModulo,
            String simularOperacion, String usaReglasNegocio, String fechaCertificacion,
            List<Long> idSolicitudes);

    /** GenerarCertificado31Masivo service call (Crystal Reports). */
    Map<String, Object> generarCertificado31Masivo(Long idEmpresa, Long idUsuario, String idModulo,
            String ciudad, String fecha, List<Long> idTraspasos);

    /** GenerarInformeRentabilidadFFMMExtranjeroMasivo service call (Crystal Reports). */
    Map<String, Object> generarInformeRentabilidadExtranjero(Long idEmpresa, Long idUsuario, String idModulo,
            String agnoComercial, String esMasivo, List<Long> idClientes);

    /** TrxCFileManager service call. */
    Map<String, Object> procesarTrxCFileManager(Long idEmpresa, Long idUsuario);

    /** TrxPFileManager service call. */
    Map<String, Object> procesarTrxPFileManager(Long idEmpresa, Long idUsuario);

    /** ValorCuotaFileManager service call — includes HANA SP. */
    Map<String, Object> procesarValorCuotaFileManager(Long idEmpresa, Long idUsuario);

    /** ValorCuotaInternacionalFileManager service call — includes HANA SP. */
    Map<String, Object> procesarValorCuotaInternacionalFileManager(Long idEmpresa, Long idUsuario);

    /** GeneracionArchivoAGF service call. */
    Map<String, Object> generarArchivoAGF(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaDesde, String fechaHasta, String idAmbiente, String ruta);

    /** ConfirmarTraspaso service call. */
    Map<String, Object> confirmarTraspaso(Long idEmpresa, Long idUsuario, String idModulo,
            List<Long> idTraspasos);

    /** GenerarArchivoMovimientos service call. */
    Map<String, Object> generarArchivoMovimientos(Long idEmpresa, Long idUsuario, String fechaCurse,
            String fondoMutuo, String emisor, String estado, String tipoSolicitud, String idMoneda);

    /** GenerarArchivoParticipes service call. */
    Map<String, Object> generarArchivoParticipes(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaCurse, String fondoMutuo, String emisor, String estado,
            String tipoSolicitud, String idMoneda);

    /** NotificarTraspasoConfirmar service call. */
    void notificarTraspaso(Long idEmpresa, Long idUsuario, String idModulo);

    /** ProcesarComisionesNN service call. */
    Map<String, Object> procesarComisionesNN(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaDesde, String fechaHasta, List<Long> idCarteras);

    /** GenerarArchivoSolicitudAporteRescate service call. */
    Map<String, Object> generarArchivoSolicitudAporteRescate(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaDesde, String fechaHasta, Map<String, Object> filtros);

    /** ConsultarCustodiaPortafolio from custodia service. */
    List<CustodiaPortafolio> consultarCustodiaPortafolio(Long idEmpresa, Long idUsuario, String idModulo,
            Long idCliente, String fechaConsulta);

    /** CrearSolicitudRescateReinversion service call. */
    Map<String, Object> crearSolicitudRescateReinversion(Long idEmpresa, Long idUsuario, String idModulo,
            List<Map<String, Object>> solicitudesRescate, List<Map<String, Object>> solicitudesAporte);
}
