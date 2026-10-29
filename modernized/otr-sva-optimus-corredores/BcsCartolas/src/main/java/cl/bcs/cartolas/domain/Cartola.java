package cl.bcs.cartolas.domain;

import java.util.List;
import java.util.Objects;

/**
 * Entidad Cartola — representa un estado de cuenta de un portafolio de un cliente
 * dentro de una nómina de cartolas.
 *
 * <p>Equivalente a {@code cl.bcs.normativa.cartolas.entidades.Cartola} del legacy.</p>
 */
public class Cartola {

    private long   id;
    private long   idNomina;
    private long   idCliente;
    private long   idPortafolio;
    private String reprocesar   = Opcion.NO;
    private String notificar    = Opcion.NO;
    private String estadoProceso;
    private String mensajeProceso;

    // -------------------------------------------------------------------------
    // Factory methods (pure-logic, replaces legacy Builder)
    // -------------------------------------------------------------------------

    /**
     * Crea una cartola nueva sin persisitir (sin id).
     */
    public static Cartola newPendiente(final long idNomina,
                                       final long idCliente,
                                       final long idPortafolio) {
        final Cartola c = new Cartola();
        c.idNomina    = idNomina;
        c.idCliente   = idCliente;
        c.idPortafolio = idPortafolio;
        c.reprocesar  = Opcion.NO;
        c.notificar   = Opcion.NO;
        return c;
    }

    /**
     * Busca la cartola en una lista por idPortafolio.
     * Lanza {@link IllegalArgumentException} si no la encuentra.
     */
    public static Cartola buscarPorIdPortafolio(final List<Cartola> cartolas,
                                                 final long idPortafolio) {
        Objects.requireNonNull(cartolas, "cartolas es requerido");
        for (final Cartola c : cartolas) {
            if (c.idPortafolio == idPortafolio) {
                return c;
            }
        }
        throw new IllegalArgumentException(
                "Cartola no encontrada para el portafolio: " + idPortafolio);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public long getId()                     { return id; }
    public void setId(final long id)         { this.id = id; }

    public long getIdNomina()               { return idNomina; }
    public void setIdNomina(final long v)    { this.idNomina = v; }

    public long getIdCliente()              { return idCliente; }
    public void setIdCliente(final long v)   { this.idCliente = v; }

    public long getIdPortafolio()           { return idPortafolio; }
    public void setIdPortafolio(final long v){ this.idPortafolio = v; }

    public String getReprocesar()           { return reprocesar; }
    public void setReprocesar(final String v){ this.reprocesar = v; }

    public String getNotificar()            { return notificar; }
    public void setNotificar(final String v) { this.notificar = v; }

    public String getEstadoProceso()        { return estadoProceso; }
    public void setEstadoProceso(final String v){ this.estadoProceso = v; }

    public String getMensajeProceso()       { return mensajeProceso; }
    public void setMensajeProceso(final String v){ this.mensajeProceso = v; }
}
