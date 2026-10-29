package cl.bcs.cartolas.master.service;

import cl.bcs.cartolas.master.domain.ConfiguracionCartola;
import cl.bcs.cartolas.master.domain.ParametroCartola;
import cl.bcs.cartolas.master.domain.ReporteMaster;
import cl.bcs.cartolas.master.port.in.CartolaMasterUseCase;
import cl.bcs.cartolas.master.port.out.CartolaMasterDataPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio de aplicación para la generación de Cartolas Master.
 *
 * <p>Implementa {@link CartolaMasterUseCase}. Las operaciones que requieren HANA
 * (generación de PDF con JasperReports y stored procedures) lanzan
 * {@link UnsupportedOperationException} cuando HANA no está disponible.</p>
 *
 * <p>La lógica pure-logic (sin dependencias externas) está en métodos
 * {@code public static} para ser testeable sin Spring.</p>
 */
@Service
public class CartolaMasterService implements CartolaMasterUseCase {

    private final CartolaMasterDataPort dataPort;

    public CartolaMasterService(final CartolaMasterDataPort dataPort) {
        this.dataPort = Objects.requireNonNull(dataPort, "dataPort es requerido");
    }

    // ---- Puerto de entrada ----

    @Override
    public List<ConfiguracionCartola> obtenerConfiguracion(final long idEmpresa) {
        return dataPort.findConfiguracionByEmpresa(idEmpresa);
    }

    @Override
    public List<ParametroCartola> obtenerParametros(final long idNomina) {
        return dataPort.findParametrosByNomina(idNomina);
    }

    @Override
    public CartolaSummary generarOnline(
            final long idCliente,
            final Long idPortafolio,
            final LocalDate fechaDesde,
            final LocalDate fechaHasta) {
        Objects.requireNonNull(fechaDesde, "fechaDesde es requerido");
        Objects.requireNonNull(fechaHasta, "fechaHasta es requerido");
        throw new UnsupportedOperationException(
                "generarOnline requiere SAP HANA — operación no disponible en entorno de prueba");
    }

    @Override
    public CartolaSummary generarPorIdCartola(final long idCartola) {
        throw new UnsupportedOperationException(
                "generarPorIdCartola requiere SAP HANA — operación no disponible en entorno de prueba");
    }

    // ---- Pure-logic: inclusión de reportes ----

    /**
     * Determina si un reporte debe incluirse según los parámetros de la nómina.
     *
     * <p>Si el reporte no tiene parámetro de inclusión configurado, siempre se incluye.
     * Si tiene, se evalúa el valor booleano del mapa de parámetros.</p>
     *
     * @param reporte   reporte a evaluar
     * @param parametros mapa de parámetros de la nómina (clave → valor Boolean)
     * @return {@code true} si debe incluirse
     */
    public static boolean incluir(
            final ReporteMaster reporte,
            final Map<String, Object> parametros) {
        Objects.requireNonNull(reporte, "reporte es requerido");
        Objects.requireNonNull(parametros, "parametros es requerido");

        if (!reporte.tieneParametroInclusion()) {
            return true;
        }
        final Object val = parametros.get(reporte.getNombreParametroInclusion());
        if (val instanceof Boolean b) {
            return b;
        }
        // Valor ausente → no incluir
        return false;
    }

    /**
     * Construye el nombre de archivo de una cartola a partir del idCliente y periodo.
     *
     * @param idCliente   identificador del cliente
     * @param fechaDesde  fecha inicio del periodo
     * @param fechaHasta  fecha fin del periodo
     * @return nombre de archivo con formato {@code cartola_{idCliente}_{desde}_{hasta}.pdf}
     */
    public static String construirNombreArchivo(
            final long idCliente,
            final LocalDate fechaDesde,
            final LocalDate fechaHasta) {
        Objects.requireNonNull(fechaDesde, "fechaDesde es requerido");
        Objects.requireNonNull(fechaHasta, "fechaHasta es requerido");
        return "cartola_" + idCliente + "_" + fechaDesde + "_" + fechaHasta + ".pdf";
    }
}
