package cl.bcs.fondomutuo.certificacion;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.CertificarSolicitudAporteRescate}: legacy {@code call(Connection, JsonObject)} with the plain Gson of the legacy
 * in and out (no null keys written). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names
 * {@link CertificarAporteRescateConfiguracion#LISTA} the call is a 503 before anything is read. The connection is the one of the unit of work (ambient transaction).
 */
@Component
public class CertificarAporteRescateJavaCallHandler implements JavaCallHandler {

    private final CertificarAporteRescatePort servicio;
    private final Compuertas compuertas;

    public CertificarAporteRescateJavaCallHandler(CertificarAporteRescatePort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CERTIFICAR_SOLICITUD_APORTE_RESCATE;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(CertificarAporteRescateConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "CertificarSolicitudAporteRescate is not enabled in this module");
        }
        CertificarAporteRescateContrato.Entrada entrada = JsonLegacy.plano().fromJson(in.toString(), CertificarAporteRescateContrato.Entrada.class);
        CertificarAporteRescateContrato.Salida salida = servicio.logica(entrada);
        return JsonParser.parseString(JsonLegacy.plano().toJson(salida)).getAsJsonObject();
    }
}
