package cl.bcs.fondomutuo.certificacion;

import com.google.gson.JsonObject;

/**
 * Port of the legacy {@code CalculaCustodiaRescate.logica} (available custody of the fund units a rescate can take) as the legacy JSON of its
 * {@code EntradaCalculaCustodiaRescate} / {@code SalidaCalculaCustodiaRescate}. No production implementation exists yet (gate G-CUS closed, T5).
 */
public interface CalculaCustodiaRescatePort {

    JsonObject calcular(JsonObject entrada);
}
