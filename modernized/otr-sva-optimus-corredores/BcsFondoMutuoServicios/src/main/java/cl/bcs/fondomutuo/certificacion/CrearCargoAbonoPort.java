package cl.bcs.fondomutuo.certificacion;

import com.google.gson.JsonObject;

/**
 * Port of the legacy CuentaInversion {@code CrearCargoAbono.logica} (cargos and abonos of the cuenta de inversion) as the legacy JSON of its {@code CrearCargoAbonoEntrada} /
 * {@code CrearCargoAbonoSalida}. No production implementation exists yet (T5): a bean of it must not be wired by accident.
 */
public interface CrearCargoAbonoPort {

    JsonObject crear(JsonObject entrada);
}
