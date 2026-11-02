package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.aportereinversion.CasoCsr.Perfil;

/**
 * One run of a case seen by the rule tests: thin accessors over the dump of {@link CasoCsr} (output, rows written, collaborator calls). The cases are named by their slug (the part of the
 * directory name after the number), so a renumbering of the matrix never breaks a rule test.
 */
final class CorridaCsr {

    static final String SOLICITUDES = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
    static final String REINVERSION = "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino";

    final String id;
    final JsonObject j;

    private CorridaCsr(String id, JsonObject j) {
        this.id = id;
        this.j = j;
    }

    static String idDe(String slug) {
        List<String> hallados = new ArrayList<>();
        for (String id : CasoCsr.casos()) {
            if (id.endsWith("_" + slug)) hallados.add(id);
        }
        if (hallados.size() != 1) throw new AssertionError("CSR: the case slug '" + slug + "' must name exactly one case directory, found " + hallados);
        return hallados.get(0);
    }

    /** Runs the case with every gate open (or the self-check of the golden when {@code -Dcsr.autocomprobacion=true}). */
    static CorridaCsr correr(String slug) throws Exception {
        String id = idDe(slug);
        return new CorridaCsr(id, CasoCsr.ejecutar(id, Perfil.ABIERTAS));
    }

    static CorridaCsr correr(String slug, Perfil perfil) throws Exception {
        String id = idDe(slug);
        return new CorridaCsr(id, CasoCsr.ejecutarReal(id, perfil));
    }

    String decision() {
        return j.get("decisionServlet").getAsString();
    }

    String excepcion() {
        return j.has("excepcion") ? j.get("excepcion").getAsString() : null;
    }

    String claseExcepcion() {
        String e = excepcion();
        return e == null ? null : e.substring(0, e.indexOf(':'));
    }

    JsonObject salida() {
        return j.getAsJsonObject("salida");
    }

    long contador() {
        return salida().get("P_OUT_ERRORESCONTADOR").getAsLong();
    }

    List<JsonObject> mensajes() {
        return lista(salida().getAsJsonArray("P_OUT_MENSAJES"));
    }

    /** "id:codigo" of every message of the output, in order. */
    List<String> mensajesIdCodigo() {
        List<String> r = new ArrayList<>();
        for (JsonObject m : mensajes()) r.add(m.get("id").getAsString() + ":" + m.get("codigo").getAsString());
        return r;
    }

    List<JsonObject> resultados() {
        return lista(salida().getAsJsonArray("P_OUT_RESULTADO"));
    }

    JsonObject resultado() {
        return resultados().get(0);
    }

    List<JsonObject> tabla(String nombre) {
        return lista(j.getAsJsonObject("tablas").getAsJsonArray(nombre));
    }

    /** The rows APOREINV written by the class (the seeded rescues 9001 / 9002 are RESREINV). */
    List<JsonObject> filas() {
        List<JsonObject> r = new ArrayList<>();
        for (JsonObject f : tabla(SOLICITUDES)) {
            if ("APOREINV".equals(f.get("IdTipoMovimientoFondoMutuo").getAsString())) r.add(f);
        }
        return r;
    }

    JsonObject fila() {
        return filas().get(0);
    }

    /** FechaFinalReinversion of the rows 1, 2, 3 of the destination table. */
    List<String> fechasFinales() {
        List<String> r = new ArrayList<>();
        for (JsonObject f : tabla(REINVERSION)) r.add(f.get("FechaFinalReinversion").getAsString());
        return r;
    }

    List<JsonObject> llamadas(String colaborador) {
        List<JsonObject> r = new ArrayList<>();
        for (JsonElement e : j.getAsJsonArray("colaboradores")) {
            if (colaborador.equals(e.getAsJsonObject().get("colaborador").getAsString())) r.add(e.getAsJsonObject());
        }
        return r;
    }

    /** The argument (always one object) of the n-th call to the collaborator. */
    JsonObject arg(String colaborador, int n) {
        return llamadas(colaborador).get(n).getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    List<String> orden() {
        List<String> r = new ArrayList<>();
        for (JsonElement e : j.getAsJsonArray("colaboradores")) r.add(e.getAsJsonObject().get("colaborador").getAsString());
        return r;
    }

    boolean llamo(String colaborador) {
        return !llamadas(colaborador).isEmpty();
    }

    /** "secuencia:cantidad" of every request of numbers, in order. */
    List<String> secuencias() {
        List<String> r = new ArrayList<>();
        for (JsonElement e : j.getAsJsonArray("secuenciasSolicitadas")) {
            String nombre = e.getAsJsonObject().get("secuencia").getAsString();
            r.add(nombre.substring(nombre.indexOf("::") + 2) + ":" + e.getAsJsonObject().get("cantidad").getAsString());
        }
        return r;
    }

    static List<JsonObject> lista(JsonArray a) {
        List<JsonObject> r = new ArrayList<>();
        for (JsonElement e : a) r.add(e.getAsJsonObject());
        return r;
    }

    static String s(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    static BigDecimal n(JsonObject o, String clave) {
        return o.get(clave).getAsBigDecimal();
    }
}
