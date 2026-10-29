package cl.bcs.fondomutuo.custodia;

import java.util.Collection;

import cl.bcs.fondomutuo.port.out.http.CustodiaPort;

/**
 * Which {@link CustodiaPort} {@code CalcularComisiones} uses (review of 2026-10-28, H3): with {@code fondomutuo.custodia.comisiones-en-proceso=false} (default) it keeps
 * the HTTP port ({@code ColaboradoresHttp}) even when {@code fondomutuo.custodia.habilitada=true} makes the in-process adapter the {@code @Primary} one; only with the property
 * true it uses the effective (in-process) port. With {@code habilitada=false} the effective port IS the HTTP one and nothing changes.
 */
public final class CustodiaParaComisiones {

    private CustodiaParaComisiones() {
    }

    /**
     * @param efectivo     the {@code @Primary} / only port (what every other consumer gets)
     * @param todos        every {@link CustodiaPort} bean of the context
     * @param enProceso    {@code fondomutuo.custodia.comisiones-en-proceso}
     */
    public static CustodiaPort elegir(CustodiaPort efectivo, Collection<CustodiaPort> todos, boolean enProceso) {
        if (enProceso || !(efectivo instanceof FlujoCustodiaEnProceso)) {
            return efectivo;
        }
        return todos.stream().filter(p -> !(p instanceof FlujoCustodiaEnProceso)).findFirst().orElse(efectivo);
    }
}
