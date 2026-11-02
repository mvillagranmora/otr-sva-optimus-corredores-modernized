package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

/**
 * F3 W3 T5: the ONE thing of the asynchronous notification (FM-CER-62, legacy {@code NotificarEstadoOrdenFondoMutuo.logicaAsync}) that the oracle can observe and that is NOT a scripted answer.
 * The real method (a) turns the entry into JSON, (b) reads {@code PLA.CallBatchURL} and {@code PLA.CallBatchToken} through {@code ConfiguracionSistema} (which THROWS an {@code Exception}
 * "Parametro ... en modulo PLATAFORMA no encontrado" when the row is missing) and (c) calls {@code Utils.CallBatchAsync}, which starts a thread that does the HTTP POST (its failure never
 * reaches the caller). The oracle's double (generated from the real source, see record_cer_t5.sh) records the call and answers from the script; when the case carries
 * {@code "asyncParametros": true} it ALSO makes the two reads of (b), with the same calls to the real class, so that a missing parameter fails exactly where the real method fails.
 * (c) is never made: no network. Nothing here is a credential: the values are whatever the case seeds (fake text) and they are never used.
 */
public final class T5Notif {

    private static boolean parametros;

    private T5Notif() {
    }

    /** Called by the runner before every run with the case JSON. */
    public static void reiniciar(JsonObject caso) {
        parametros = caso.has("asyncParametros") && caso.get("asyncParametros").getAsBoolean();
    }

    /** The two reads of the real logicaAsync (C: NotificarEstadoOrdenFondoMutuo.java, logicaAsync), in the same order. */
    public static void parametros(Connection con, Long idEmpresa) throws Exception {
        if (!parametros) return;
        ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
    }
}
