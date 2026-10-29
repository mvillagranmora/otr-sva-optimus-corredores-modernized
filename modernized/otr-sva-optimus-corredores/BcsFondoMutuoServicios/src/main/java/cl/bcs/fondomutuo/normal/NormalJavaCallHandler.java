package cl.bcs.fondomutuo.normal;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/**
 * JavaCall entry of {@code cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal} (registry id 357): legacy {@code call(Connection, JsonObject)}, with the plain
 * Gson of the legacy in and out (no null keys written). The connection is the one of the unit of work; the service reaches it through the ambient transaction.
 */
@Component
public class NormalJavaCallHandler implements JavaCallHandler {

    private final NormalPort normal;

    public NormalJavaCallHandler(NormalPort normal) {
        this.normal = normal;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CREAR_SOLICITUD_NORMAL;
    }

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        NormalContrato.Entrada entrada = JsonLegacy.plano().fromJson(jin.toString(), NormalContrato.Entrada.class);
        NormalContrato.Salida salida = normal.logica(entrada);
        return JsonParser.parseString(JsonLegacy.plano().toJson(salida)).getAsJsonObject();
    }
}
