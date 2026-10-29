package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/** Port H (WRITE; gate G5, same failure rules as {@link LogPort}): legacy {@code cl.bcs.plataforma.bat.InscribirProcesoBatch} (id 20043; {@code BAT.Programa} and parameters). */
public interface BatchPort {

    JsonObject inscribir(JsonObject entrada);
}
