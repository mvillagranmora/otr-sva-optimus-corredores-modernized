package cl.bcs.fondomutuo.manejador;

import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * F1 implementation of the seven destinations that are not ported: "not served" (503, the router repeats the whole request in TomEE). Gate G0 of the
 * Manejador already rejects the request BEFORE any destination is called, and {@code ManejadorConfiguracion.validarListasServidas} stops the start-up when
 * the configuration names a list that still has this implementation, so this is only the last line of defence (a direct call of the service).
 */
public final class TipoNoServido implements TipoSolicitudPort {

    private final String lista;

    public TipoNoServido(String lista) {
        this.lista = lista;
    }

    @Override
    public Respuesta logica(Pedido pedido) {
        throw new ServicioNoServidoException("G0", "list " + lista + " has no handler in this module");
    }
}
