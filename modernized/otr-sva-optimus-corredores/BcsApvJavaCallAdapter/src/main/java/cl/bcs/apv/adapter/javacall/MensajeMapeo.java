package cl.bcs.apv.adapter.javacall;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.comun.Mensaje;

/** Mapper of the common message record (legacy {@code MensajeSalida}: id, tipo, codigo, descripcion) under the Gson flavour of the operation. */
public final class MensajeMapeo {

    private MensajeMapeo() {
    }

    public static Mensaje desdeJson(Sabor sabor, JsonObject o) {
        Mensaje d = Mensaje.porDefecto();
        return new Mensaje(
                MapeoJson.escalar(sabor, o, "id", Long.class, d.id()),
                MapeoJson.escalar(sabor, o, "tipo", String.class, d.tipo()),
                MapeoJson.escalar(sabor, o, "codigo", String.class, d.codigo()),
                MapeoJson.escalar(sabor, o, "descripcion", String.class, d.descripcion()));
    }

    public static JsonObject aJson(Sabor sabor, Mensaje m) {
        JsonObject j = new JsonObject();
        MapeoJson.poner(sabor, j, "id", m.id());
        MapeoJson.poner(sabor, j, "tipo", m.tipo());
        MapeoJson.poner(sabor, j, "codigo", m.codigo());
        MapeoJson.poner(sabor, j, "descripcion", m.descripcion());
        return j;
    }
}
