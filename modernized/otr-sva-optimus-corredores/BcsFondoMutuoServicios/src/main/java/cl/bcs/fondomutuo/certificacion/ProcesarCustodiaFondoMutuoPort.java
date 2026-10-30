package cl.bcs.fondomutuo.certificacion;

import com.google.gson.JsonObject;

/**
 * Port of the legacy {@code ProcesarCustodiaFondoMutuo.logica} (entry and retirement of the custody of the fund units) as the legacy JSON of its
 * {@code EntradaProcesarCustodiaFondoMutuo} / {@code SalidaProcesarCustodiaFondoMutuo}. No production implementation exists yet (gate G-CUS closed: the in-process
 * adapter comes with the custody line, T5): a bean of it must not be wired by accident.
 */
public interface ProcesarCustodiaFondoMutuoPort {

    JsonObject procesar(JsonObject entrada);
}
