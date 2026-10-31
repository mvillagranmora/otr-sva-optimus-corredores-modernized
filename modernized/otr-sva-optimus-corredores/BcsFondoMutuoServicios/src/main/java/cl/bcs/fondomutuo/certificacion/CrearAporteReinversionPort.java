package cl.bcs.fondomutuo.certificacion;

import com.google.gson.JsonObject;

/**
 * Port of the legacy {@code CrearSolicitudAporteReinversion.logica} (AR) as the legacy JSON of its {@code EntradaCrearSolicitudAporteReinversion} /
 * {@code SalidaCrearSolicitudAporteReinversion} (F-I, FM-CER-57). AR is ported by another front into its own package: no production implementation exists here, a bean of it must not
 * be wired by accident.
 */
public interface CrearAporteReinversionPort {

    JsonObject crear(JsonObject entrada);
}
