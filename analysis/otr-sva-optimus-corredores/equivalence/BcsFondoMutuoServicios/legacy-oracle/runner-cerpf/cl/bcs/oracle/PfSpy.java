package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;

/**
 * F3 W3 T6-A: OBSERVATION of the ONE collaborator of CertificarPorFiltrosSolicitudAporteRescate: the REAL {@code CertificarSolicitudAporteRescate.logica}. record_cerpf.sh compiles a COPY of the
 * legacy source (outside legacy/) in which that ONE call is replaced by {@code cl.bcs.oracle.PfSpy.certificar(certificar, con, entrada)}. This method records the COMPLETE entry the legacy built
 * (header copied from the request, p_Solicitudes = the rows of every filter, duplicates included) and the answer, as an ordinary collaborator call named {@link #CERTIFICAR} (same shape as the doubles and
 * as the record T4Spy leaves for the child: {colaborador, metodo, argumentos, respuesta, internas}), and moves everything Certificar did to ITS collaborators (and the child records) into the field
 * {@code internas}. The call is delegated untouched and the exception, if any, goes up untouched.
 */
public final class PfSpy {

    public static final String CERTIFICAR = "CertificarPorFiltros.certificar";

    private static final Gson OUT = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().disableHtmlEscaping().create();

    private PfSpy() {
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    private static JsonArray internas(JsonArray calls, int desde) {
        JsonArray out = new JsonArray();
        while (calls.size() > desde) out.add(calls.remove(desde));
        return out;
    }

    public static SalidaCertificarSolicitudAporteRescate certificar(CertificarSolicitudAporteRescate certificador, Connection con, EntradaCertificarSolicitudAporteRescate entrada) throws Exception {
        JsonArray calls = Doubles.calls();
        int antes = calls.size();
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", CERTIFICAR);
        rec.addProperty("metodo", "logica");
        JsonArray args = new JsonArray();
        args.add(OUT.toJsonTree(entrada));
        rec.add("argumentos", args);
        SalidaCertificarSolicitudAporteRescate salida;
        try {
            salida = certificador.logica(con, entrada);
        } catch (Throwable t) {
            rec.addProperty("respuesta", "error: " + t.getClass().getSimpleName());
            rec.add("internas", internas(calls, antes));
            calls.add(rec);
            PfSpy.<RuntimeException>sneaky(t);
            return null;
        }
        rec.add("respuesta", OUT.toJsonTree(salida));
        rec.add("internas", internas(calls, antes));
        calls.add(rec);
        return salida;
    }
}
