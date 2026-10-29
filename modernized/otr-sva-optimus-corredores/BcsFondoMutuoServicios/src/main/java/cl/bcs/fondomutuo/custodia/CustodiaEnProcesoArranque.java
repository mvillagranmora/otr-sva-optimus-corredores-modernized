package cl.bcs.fondomutuo.custodia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;

/**
 * Start-up check of G-CUS (F3, 2026-10-28; same style as {@code consultas.ListasLecturaArranque}): the gate was closed because moving custody over HTTP
 * breaks the atomicity of the request. It may be open ONLY with the in-process adapter ({@link FlujoCustodiaEnProceso}, same transaction). If
 * {@code fondomutuo.custodia.habilitada} is true and the effective {@link CustodiaPort} is anything else (the HTTP adapter, a double), the start-up fails
 * with {@link IllegalStateException}: otherwise a request would move custody in another transaction and a later failure would leave it applied.
 * The gate is read from {@link Compuertas} (the same source as the gate itself), not from the property again.
 *
 * <p>Scope of the three custody switches (review of 2026-10-28, H3, user decision: the switch is SEPARATED): {@code fondomutuo.custodia.habilitada} opens the in-process custody
 * for RescateReinversion (and registers the {@code @Primary} adapter); {@code fondomutuo.custodia.normal-habilitada} (default false) lets Normal call the custody flow;
 * {@code fondomutuo.custodia.comisiones-en-proceso} (default false) makes {@code CalcularComisiones} use the in-process adapter instead of HTTP. Start-up rules, all fail
 * closed with {@link IllegalStateException}: {@code normal-habilitada=true} or {@code comisiones-en-proceso=true} WITHOUT {@code habilitada=true} (Normal over HTTP would break
 * the atomicity of the request; the commissions need the in-process adapter), and any of the three open while the effective port is not the in-process adapter.
 */
@Component
public class CustodiaEnProcesoArranque {

    private static final Logger log = LoggerFactory.getLogger(CustodiaEnProcesoArranque.class);

    public CustodiaEnProcesoArranque(Compuertas compuertas, ObjectProvider<CustodiaPort> custodia) {
        boolean alguna = compuertas.custodiaHabilitada() || compuertas.custodiaNormalHabilitada() || compuertas.comisionesEnProceso();
        iniciar(compuertas.custodiaHabilitada(), compuertas.custodiaNormalHabilitada(), compuertas.comisionesEnProceso(), alguna ? custodia.getIfAvailable() : null);
    }

    /** @throws IllegalStateException G-CUS open while the effective custody port is not the in-process adapter */
    static void iniciar(boolean custodiaHabilitada, CustodiaPort efectivo) {
        iniciar(custodiaHabilitada, false, false, efectivo);
    }

    /**
     * @throws IllegalStateException {@code normalHabilitada} or {@code comisionesEnProceso} without {@code custodiaHabilitada}; or any of them open while the effective custody
     *                               port is not the in-process adapter
     */
    static void iniciar(boolean custodiaHabilitada, boolean normalHabilitada, boolean comisionesEnProceso, CustodiaPort efectivo) {
        if (!custodiaHabilitada && (normalHabilitada || comisionesEnProceso)) {
            throw new IllegalStateException((normalHabilitada ? "fondomutuo.custodia.normal-habilitada" : "fondomutuo.custodia.comisiones-en-proceso")
                    + " is true but fondomutuo.custodia.habilitada is false: Normal and the commissions need the in-process custody adapter (over HTTP the custody "
                    + "would move outside the transaction of the request, no atomicity). Open habilitada too or close this switch");
        }
        if (!custodiaHabilitada) {
            return;
        }
        if (!(efectivo instanceof FlujoCustodiaEnProceso)) {
            throw new IllegalStateException("fondomutuo.custodia.habilitada is true but the effective CustodiaPort is "
                    + (efectivo == null ? "absent" : efectivo.getClass().getSimpleName())
                    + " and not the in-process adapter FlujoCustodiaEnProceso: custody would move outside the transaction of the request "
                    + "(no atomicity). Open G-CUS only with the in-process custody library");
        }
        log.info("G-CUS open: custody flow in process (same transaction as the request), RescateReinversion in process; Normal custody flow: {}; CalcularComisiones uses: {}",
                normalHabilitada ? "OPEN (normal-habilitada=true)" : "closed (503 G-CUS; normal-habilitada=false)",
                comisionesEnProceso ? "the in-process adapter (comisiones-en-proceso=true)" : "the HTTP port (comisiones-en-proceso=false)");
    }
}
