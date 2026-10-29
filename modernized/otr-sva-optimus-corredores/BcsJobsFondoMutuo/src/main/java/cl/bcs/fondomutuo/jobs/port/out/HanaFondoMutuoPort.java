package cl.bcs.fondomutuo.jobs.port.out;

import cl.bcs.fondomutuo.jobs.domain.*;

import java.util.List;

/**
 * HANA read-only queries used by Fondo Mutuo jobs.
 * Implementations require a live SAP HANA connection.
 * All methods throw UnsupportedOperationException in the Phase 2 stub.
 */
public interface HanaFondoMutuoPort {

    /**
     * ConfirmarTraspaso_SelectTraspaso.sql
     * Returns traspasos in state 'ING' where days since FechaIngreso == diasTranscurridos.
     */
    List<RegistroTraspaso> consultarTraspasosPendientes(Long idEmpresa, Long diasTranscurridos);

    /**
     * IngresarOrdenFMBonificacionFiscal_SelectFFMMBF.sql
     * Returns aporte/rescate records eligible for BF reinversion.
     */
    List<ApoResFFMM> consultarAportesRescateBF(List<Long> idsAporteRescate);

    /**
     * IngresarOrdenFMBonificacionFiscal_SelectParametros.sql
     * Reads job program parameters.
     */
    List<ParametrosPrograma> consultarParametrosPrograma(Long idPrograma);

    /**
     * IngresarOrdenFMBonificacionFiscal_SelectSerieFondo.sql
     * Returns serie de fondo with decimales for the given series codes.
     */
    List<SerieFondoSolicitudes> consultarSeriesFondo(Long idEmpresa, List<String> idsSerieFondo);

    /**
     * ComunesSolicitudes_SelectReinversionSerieFondoDestino.sql
     * Returns reinversion destination serie records.
     */
    List<ReinversionSerieFondoDestino> consultarReinversionSerieFondoDestino(List<Long> idsAporteRescate);

    /**
     * Reads a system configuration parameter.
     * PLA.Configuracion: IdEmpresa + CodigoParametro → Valor.
     */
    String consultarParametroConfiguracion(Long idEmpresa, String codigoParametro);

    /**
     * Updates RutaSalida on BAT.Programa (used by several jobs to save output path).
     */
    void actualizarRutaSalidaPrograma(Long idPrograma, String rutaSalida);

    /**
     * Appends a log entry to BAT.LogPrograma.
     */
    void agregarLogPrograma(Long idPrograma, String idTipoPrograma, String mensaje);
}
