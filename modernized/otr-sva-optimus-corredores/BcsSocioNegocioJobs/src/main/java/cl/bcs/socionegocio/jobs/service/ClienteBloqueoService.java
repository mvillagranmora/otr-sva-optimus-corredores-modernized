package cl.bcs.socionegocio.jobs.service;

import cl.bcs.socionegocio.jobs.domain.ClienteABloquear;
import cl.bcs.socionegocio.jobs.domain.DocumentoCliente;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Pure-logic service for client blocking rules.
 *
 * All methods are public static so they can be tested without Spring context.
 * Business rules extracted from BloquearClientePorFechaVencimientoJob and
 * PautaBloqueoAutomaticoClientes (legacy).
 *
 * Key behaviors:
 *   B-BLQ-01: calcularFechaVencimiento — FechaVigencia = FechaModificacion + diasVencimiento
 *   B-BLQ-02: esPautaAnteriorTerminada — gate: IdEstadoPrograma must be "TER"
 *   B-BLQ-03: clienteVenceHoy — FechaVigencia equals fechaSistema
 *   B-BLQ-04: debeBloquearAlVencer — BloquearAlVencer == "S"
 *   B-BLQ-05: buildClienteABloquear — IdTipoBloqueo="BAUTOM", hard-coded comment
 *   B-BLQ-06: calcularFechasVencimientoLote — skip PEN state and null FechaModificacion
 *   B-BLQ-07: filtrarClientesABloquear — both venceHoy AND bloquearAlVencer
 */
@Service
public class ClienteBloqueoService {

    // Hard-coded constants preserved from legacy BloquearClientePorFechaVencimientoJob
    public static final String ID_TIPO_BLOQUEO_AUTOMATICO = "BAUTOM";
    public static final String COMENTARIO_BLOQUEO_AUTOMATICO =
            "Cliente bloqueado automaticamente por fecha de vencimiento";
    public static final String ESTADO_PENDIENTE = "PEN";
    public static final String ESTADO_PAUTA_TERMINADA = "TER";
    public static final String BLOQUEAR_AL_VENCER_SI = "S";

    // Private constructor — utility-style, all methods are static
    private ClienteBloqueoService() {}

    /**
     * B-BLQ-01: Computes the expiry date by adding diasVencimiento to fechaModificacion.
     */
    public static LocalDate calcularFechaVencimiento(LocalDate fechaModificacion, int diasVencimiento) {
        return fechaModificacion.plusDays(diasVencimiento);
    }

    /**
     * B-BLQ-02: Returns true only when idEstadoPrograma equals "TER".
     * Uses "TER".equals() to safely handle null input.
     */
    public static boolean esPautaAnteriorTerminada(String idEstadoPrograma) {
        return ESTADO_PAUTA_TERMINADA.equals(idEstadoPrograma);
    }

    /**
     * B-BLQ-03: Returns true when the document's expiry date matches today (system date).
     */
    public static boolean clienteVenceHoy(LocalDate fechaVigencia, LocalDate fechaSistema) {
        return fechaVigencia != null && fechaVigencia.equals(fechaSistema);
    }

    /**
     * B-BLQ-04: Returns true when bloquearAlVencer is "S".
     */
    public static boolean debeBloquearAlVencer(String bloquearAlVencer) {
        return BLOQUEAR_AL_VENCER_SI.equals(bloquearAlVencer);
    }

    /**
     * B-BLQ-05: Builds a ClienteABloquear with hard-coded type and comment from legacy.
     */
    public static ClienteABloquear buildClienteABloquear(Long idCliente) {
        return new ClienteABloquear(idCliente, ID_TIPO_BLOQUEO_AUTOMATICO, COMENTARIO_BLOQUEO_AUTOMATICO);
    }

    /**
     * B-BLQ-06: Computes FechaVigencia for each document in the batch.
     * Rules preserved from legacy calcularFechaVencimiento():
     * - Skip if FechaModificacion is null
     * - Skip if IdEstado == "PEN"
     * - Otherwise: FechaVigencia = FechaModificacion + diasVencimiento
     *
     * Returns a new list with updated records; original list is not mutated.
     */
    public static List<DocumentoCliente> calcularFechasVencimientoLote(
            List<DocumentoCliente> docs, int diasVencimiento) {
        return docs.stream()
                .map(doc -> {
                    if (doc.fechaModificacion() == null) {
                        return doc; // skip: no modification date
                    }
                    if (ESTADO_PENDIENTE.equals(doc.idEstado())) {
                        return doc; // skip: pending documents are not processed
                    }
                    LocalDate nuevaFechaVigencia = calcularFechaVencimiento(
                            doc.fechaModificacion(), diasVencimiento);
                    return doc.withFechaVigencia(nuevaFechaVigencia);
                })
                .toList();
    }

    /**
     * B-BLQ-07: Filters documents to build the list of clients to block.
     * Condition: fechaVigencia equals fechaSistema AND bloquearAlVencer == "S"
     */
    public static List<ClienteABloquear> filtrarClientesABloquear(
            List<DocumentoCliente> docs, LocalDate fechaSistema) {
        return docs.stream()
                .filter(doc -> clienteVenceHoy(doc.fechaVigencia(), fechaSistema))
                .filter(doc -> debeBloquearAlVencer(doc.bloquearAlVencer()))
                .map(doc -> buildClienteABloquear(doc.idCliente()))
                .toList();
    }
}
