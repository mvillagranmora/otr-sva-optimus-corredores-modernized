package cl.bcs.cartolas.master.port.in;

import cl.bcs.cartolas.master.domain.ConfiguracionCartola;
import cl.bcs.cartolas.master.domain.ParametroCartola;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de entrada (use-case) para la generación de Cartolas Master.
 *
 * <p>Las operaciones que dependen de HANA lanzan {@link UnsupportedOperationException}
 * en el perfil de test; sólo se ejecutan en producción con HANA disponible.</p>
 */
public interface CartolaMasterUseCase {

    /**
     * Retorna la configuración de cartolas de la empresa indicada.
     *
     * @param idEmpresa identificador de empresa
     * @return lista de configuraciones (puede ser vacía)
     */
    List<ConfiguracionCartola> obtenerConfiguracion(long idEmpresa);

    /**
     * Retorna los parámetros asociados a la nómina indicada.
     *
     * @param idNomina identificador de nómina
     * @return lista de parámetros (puede ser vacía)
     */
    List<ParametroCartola> obtenerParametros(long idNomina);

    /**
     * Genera la cartola para un cliente / portafolio en el rango de fechas dado.
     *
     * <p><b>Requiere HANA.</b> Lanza {@link UnsupportedOperationException} en el perfil test.</p>
     *
     * @param idCliente    identificador del cliente
     * @param idPortafolio identificador del portafolio (puede ser null para consolidada)
     * @param fechaDesde   fecha de inicio del periodo
     * @param fechaHasta   fecha de fin del periodo
     * @return nombre del archivo PDF generado y su tamaño en bytes como resumen
     */
    CartolaSummary generarOnline(long idCliente, Long idPortafolio,
                                 LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Genera la cartola por id de cartola (proceso batch).
     *
     * <p><b>Requiere HANA.</b></p>
     *
     * @param idCartola identificador único de la cartola en HANA
     * @return resumen del resultado de generación
     */
    CartolaSummary generarPorIdCartola(long idCartola);

    /**
     * DTO de respuesta con el nombre del archivo generado y su tamaño.
     */
    record CartolaSummary(String nombreArchivo, int sizeBytes) {}
}
