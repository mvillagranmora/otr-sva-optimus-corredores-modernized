package cl.bcs.fondomutuo.aportereinversion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.certificacion.CrearAporteReinversionPort;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/**
 * What Certificar calls for its reinvestment front (F-I): the legacy JSON of {@code CrearSolicitudAporteReinversion} in and out, over the typed
 * {@link AporteReinversionPort}. No state, no logic of its own: whatever the port throws leaves unchanged (503 included).
 */
public class AporteReinversionJsonAdapter implements CrearAporteReinversionPort {

    private final AporteReinversionPort aporteReinversion;

    public AporteReinversionJsonAdapter(AporteReinversionPort aporteReinversion) {
        this.aporteReinversion = aporteReinversion;
    }

    @Override
    public JsonObject crear(JsonObject entrada) throws Exception {
        AporteReinversionContrato.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(), AporteReinversionContrato.Entrada.class);
        AporteReinversionContrato.Salida out = aporteReinversion.logica(in);
        return JsonParser.parseString(JsonLegacy.plano().toJson(out)).getAsJsonObject();
    }
}
