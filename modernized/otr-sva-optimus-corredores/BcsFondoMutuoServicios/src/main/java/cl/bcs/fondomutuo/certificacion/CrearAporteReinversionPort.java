package cl.bcs.fondomutuo.certificacion;

import com.google.gson.JsonObject;

/**
 * Port of the legacy {@code CrearSolicitudAporteReinversion.logica} (AR) as the legacy JSON of its {@code EntradaCrearSolicitudAporteReinversion} /
 * {@code SalidaCrearSolicitudAporteReinversion} (F-I, FM-CER-57). AR is ported into its own package; its only implementation is {@code aportereinversion.AporteReinversionJsonAdapter}
 * (T4-C), wired in {@code AporteReinversionConfiguracion}: one bean of this type, never a second by accident.
 */
public interface CrearAporteReinversionPort {

    JsonObject crear(JsonObject entrada) throws Exception;
}
