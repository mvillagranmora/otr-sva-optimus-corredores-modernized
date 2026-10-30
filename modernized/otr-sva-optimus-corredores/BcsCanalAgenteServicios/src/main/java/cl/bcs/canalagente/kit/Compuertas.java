package cl.bcs.canalagente.kit;

import org.springframework.beans.factory.annotation.Value;

/**
 * Gate G-CTA as CONFIGURATION: {@code canalagente.compuertas.cartola}. FAIL CLOSED: a property that is missing or false keeps it shut. The legacy has no gates (it
 * always calls ConsultarCartolaDeMovimiento); the gate exists because that collaborator is not ported into this module. The class is not annotated as a component: whoever
 * wires the module decides to create the bean, and nothing opens the gate by itself. The state is immutable and read once at start-up.
 */
public final class Compuertas {

    private final boolean cartola;

    public Compuertas(@Value("${canalagente.compuertas.cartola:false}") boolean cartola) {
        this.cartola = cartola;
    }

    /** G-CTA: the call to ConsultarCartolaDeMovimiento. Evaluated BEFORE the first SQL of the request. */
    public void gCartola() {
        if (!cartola) {
            throw new ServicioNoServidoException("G-CTA", "ConsultarCartolaDeMovimiento is not served by this module");
        }
    }
}
