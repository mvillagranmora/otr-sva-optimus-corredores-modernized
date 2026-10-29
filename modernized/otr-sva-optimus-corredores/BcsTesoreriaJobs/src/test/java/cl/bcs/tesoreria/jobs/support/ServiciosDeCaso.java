package cl.bcs.tesoreria.jobs.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.ActivarMandatosArchivoCargaService;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService;
import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;

/**
 * Mocks of the BcsTesoreriaServicios services the jobs call directly. Each one records its input in the {@link Traza} and answers
 * what the oracle fake of the same legacy class answers, including the DEFAULT FIELDS of the legacy output object (the real service answers
 * the complete legacy object, so an absent field of the script takes the legacy constructor default).
 */
public final class ServiciosDeCaso {

    private ServiciosDeCaso() {
    }

    private static JsonObject conDefectos(JsonObject salida, String defectos) {
        JsonObject d = JsonParser.parseString(defectos).getAsJsonObject();
        for (var e : d.entrySet()) {
            if (!salida.has(e.getKey())) {
                salida.add(e.getKey(), e.getValue());
            }
        }
        return salida;
    }

    public static ProcesarDistribucionService procesarDistribucion(Traza traza) throws Exception {
        ProcesarDistribucionService s = mock(ProcesarDistribucionService.class);
        when(s.execute(any(JsonObject.class))).thenAnswer(inv -> {
            traza.registrar("ProcesarDistribucion", inv.<JsonObject>getArgument(0));
            traza.lanzaSiCorresponde("ProcesarDistribucion.lanza");
            JsonObject salida = traza.objeto("ProcesarDistribucion").has("salida")
                    ? traza.objeto("ProcesarDistribucion").getAsJsonObject("salida").deepCopy() : new JsonObject();
            return conDefectos(salida, "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_SOLICITUDES_PROCESADAS\":[],"
                    + "\"P_OUT_SOLICITUDES_NO_PROCESADAS\":[],\"P_OUT_SOLICITUDES_A_PROCESAR\":[],\"P_OUT_RESULTADO\":[]}");
        });
        return s;
    }

    public static CargaMasivaIngresoEgresoDAPFFMMService carga(Traza traza) throws Exception {
        CargaMasivaIngresoEgresoDAPFFMMService s = mock(CargaMasivaIngresoEgresoDAPFFMMService.class);
        when(s.execute(any(JsonObject.class))).thenAnswer(inv -> {
            traza.registrar("CargaMasivaIngresoEgresoDAPFFMM", inv.<JsonObject>getArgument(0));
            traza.lanzaSiCorresponde("CargaMasivaIngresoEgresoDAPFFMM.lanza");
            JsonObject salida = traza.objeto("CargaMasivaIngresoEgresoDAPFFMM").has("salida")
                    ? traza.objeto("CargaMasivaIngresoEgresoDAPFFMM").getAsJsonObject("salida").deepCopy() : new JsonObject();
            return conDefectos(salida, "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RUTA_ARCHIVO\":\"\",\"P_OUT_MENSAJES\":[],\"P_OUT_REGISTROSINGRESADOSCONTADOR\":0}");
        });
        return s;
    }

    /** Like the real service: copies IdEmpresa and IdUsuario of the request into the first format. */
    public static ConsultarFormatoArchivoService formatos(Traza traza) throws Exception {
        ConsultarFormatoArchivoService s = mock(ConsultarFormatoArchivoService.class);
        when(s.consultar(any(JsonObject.class))).thenAnswer(inv -> {
            JsonObject in = inv.getArgument(0);
            traza.registrar("ConsultarFormatoArchivo", in);
            traza.lanzaSiCorresponde("ConsultarFormatoArchivo.lanza");
            JsonArray out = new JsonArray();
            JsonElement f = traza.objeto("ConsultarFormatoArchivo").get("formatos");
            if (f != null && !f.isJsonNull()) {
                f.getAsJsonArray().forEach(e -> out.add(e.deepCopy()));
            }
            if (out.size() > 0) {
                JsonObject primero = out.get(0).getAsJsonObject();
                primero.add("IdEmpresa", in.get("IdEmpresa"));
                primero.add("IdUsuario", in.get("IdUsuario"));
            }
            return out;
        });
        return s;
    }

    public static ActivarMandatosArchivoCargaService mandatos(Traza traza) {
        ActivarMandatosArchivoCargaService s = mock(ActivarMandatosArchivoCargaService.class);
        when(s.logica(any(Long.class))).thenAnswer(inv -> {
            JsonObject o = new JsonObject();
            o.addProperty("valor", inv.<Long>getArgument(0));
            traza.registrar("ActivarMandatosArchivoCargo", o);
            traza.lanzaSiCorresponde("ActivarMandatosArchivoCargo.lanza");
            return new ActivarMandatosArchivoCargaService.Salida();
        });
        return s;
    }
}
