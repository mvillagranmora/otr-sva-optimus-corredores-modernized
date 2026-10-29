package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/** Port H (external WRITE, nature unknown, Q-08; gate G4 keeps it closed): legacy {@code cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP}. */
public interface NotificacionOwpPort {

    JsonObject notificar(JsonObject entrada);
}
