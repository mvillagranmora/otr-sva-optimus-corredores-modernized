package cl.bcs.fondomutuo.consultas.custodiaffmm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;

/**
 * Implements the port CPF consumes with the legacy {@code call} of {@code ConsultarCustodiaPortafolioFFMM}: JSON in (Gson of {@code Utils.getGsonBuilder()}), service, JSON out
 * (nulls written). A NEW Gson per call; it catches nothing: every exception of the service goes up unchanged to CPF (FM-CPFF-22.6). It is the default bean of the port
 * ({@link ConsultarCustodiaPortafolioFfmmConfiguracion}).
 */
public class ConsultarCustodiaPortafolioFfmmAdaptador implements ConsultarCustodiaPortafolioCustodiaPort {

    private final ConsultarCustodiaPortafolioFfmmPort servicio;

    public ConsultarCustodiaPortafolioFfmmAdaptador(ConsultarCustodiaPortafolioFfmmPort servicio) {
        this.servicio = servicio;
    }

    @Override
    public JsonObject consultar(JsonObject entrada) {
        Gson gson = ConsultarCustodiaPortafolioFfmmJson.gson();
        ConsultarCustodiaPortafolioFfmmContrato.Entrada jin = gson.fromJson(entrada.toString(), ConsultarCustodiaPortafolioFfmmContrato.Entrada.class);
        ConsultarCustodiaPortafolioFfmmContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
