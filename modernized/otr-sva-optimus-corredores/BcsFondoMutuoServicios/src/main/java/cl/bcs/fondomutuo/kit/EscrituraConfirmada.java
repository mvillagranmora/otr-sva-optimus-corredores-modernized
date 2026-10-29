package cl.bcs.fondomutuo.kit;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Per-request flag "something this request wrote may already be committed, so a 503 can no longer be answered": if the router (TomEE fallback) repeated
 * the whole request after that, the writes would be duplicated (architecture review M3). Two things set it:
 * <ul>
 *   <li>a successful call to an HTTP WRITE collaborator (the remote module committed on its own: AgregarLog, InscribirProcesoBatch, NotificarEstadoOrdenesOWP);</li>
 *   <li>a DDL of the anti-duplicate lock (CREATE LOCAL TEMPORARY TABLE / DROP TABLE) executed while a local write of this request was still uncommitted:
 *       on HANA that DDL implicitly COMMITS the open transaction (HANA_DDL_COMMIT_HALLAZGO; accepted legacy behaviour, option A), on H2 it does not.</li>
 * </ul>
 * Once set, {@code RouterClient} raises {@link ColaboradorErrorException} (generic 500, the router does NOT repeat) instead of
 * {@link ServicioNoServidoException}, and {@code JavaCallController} answers 500 for any {@code ServicioNoServidoException} that still arrives. The flag lives
 * in the resources of the CURRENT Spring transaction (the unit of work of the request) and disappears with it: nothing is kept between requests. Outside a
 * transaction (in-process unit tests) the marks are ignored.
 */
public final class EscrituraConfirmada {

    private static final Object CLAVE = new Object();

    private static final class Estado {
        boolean pendienteLocal;
        boolean confirmada;
    }

    private EscrituraConfirmada() {
    }

    private static Estado estado() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return null;
        }
        Estado e = (Estado) TransactionSynchronizationManager.getResource(CLAVE);
        if (e == null) {
            e = new Estado();
            TransactionSynchronizationManager.bindResource(CLAVE, e);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(CLAVE);
                }
            });
        }
        return e;
    }

    /** A local INSERT of this request has been executed and is not committed yet. */
    public static void escrituraLocalPendiente() {
        Estado e = estado();
        if (e != null) e.pendienteLocal = true;
    }

    /** The lock DDL just ran: on HANA it committed whatever local write was pending. */
    public static void ddlConfirmaEscrituraPendiente() {
        Estado e = estado();
        if (e != null && e.pendienteLocal) e.confirmada = true;
    }

    /** An HTTP write collaborator answered successfully: it committed in its own transaction. */
    public static void escrituraRemotaConfirmada() {
        Estado e = estado();
        if (e != null) e.confirmada = true;
    }

    public static boolean confirmada() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        Estado e = (Estado) TransactionSynchronizationManager.getResource(CLAVE);
        return e != null && e.confirmada;
    }
}
