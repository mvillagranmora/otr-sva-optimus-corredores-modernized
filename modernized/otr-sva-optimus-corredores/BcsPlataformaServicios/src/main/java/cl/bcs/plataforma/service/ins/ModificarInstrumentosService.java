package cl.bcs.plataforma.service.ins;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModificarInstrumentosService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ModificarInstrumentosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ModificarInstrumentos"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlUpdate = loadSql("cl/bcs/plataforma/ins/sql/ModificarInstrumentos_UpdateInstrumentos.sql");
            String sqlDeleteRiesgo = "DELETE FROM \"BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo\" WHERE \"IdInstrumento\" IN (:IdInstrumento)";
            String sqlDeleteBolsa = "DELETE FROM \"BSCL.Plataforma.BD::INS.BolsaInstrumento\" WHERE \"IdInstrumento\" IN (:IdInstrumento)";
            String sqlInsertRiesgo = loadSql("cl/bcs/plataforma/ins/sql/CrearInstrumentos_InsertValorRiesgo.sql");
            String sqlInsertBolsa = loadSql("cl/bcs/plataforma/ins/sql/CrearInstrumentos_InsertBolsaInstrumento.sql");

            JsonArray instrumentos = params.has("p_Instrumentos") && params.get("p_Instrumentos").isJsonArray()
                    ? params.getAsJsonArray("p_Instrumentos") : new JsonArray();
            if (instrumentos.size() == 0) {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "ERROR");
                msg.addProperty("Codigo", "NO_SE_INGRESARON_INSTRUMENTOS");
                mensajes.add(msg);
                result.add("P_OUT_MENSAJES", mensajes);
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                return result;
            }

            for (JsonElement el : instrumentos) {
                JsonObject ins = el.getAsJsonObject();
                Long idInstrumento = ins.has("IdInstrumento") && !ins.get("IdInstrumento").isJsonNull()
                        ? ins.get("IdInstrumento").getAsLong() : null;

                jdbc.update(sqlUpdate, buildUpdateParams(ins));

                // Refresh valor riesgo
                JsonArray riesgos = params.has("p_ValoresRiesgo") && params.get("p_ValoresRiesgo").isJsonArray()
                        ? params.getAsJsonArray("p_ValoresRiesgo") : new JsonArray();
                if (!riesgos.isEmpty() && idInstrumento != null) {
                    jdbc.update(sqlDeleteRiesgo, new MapSqlParameterSource().addValue("IdInstrumento", List.of(idInstrumento)));
                    for (JsonElement rel : riesgos) {
                        JsonObject r = rel.getAsJsonObject();
                        jdbc.update(sqlInsertRiesgo, new MapSqlParameterSource()
                                .addValue("IdInstrumento", idInstrumento)
                                .addValue("IdEntidadClasificadoraRiesgo", getS(r, "IdEntidadClasificadoraRiesgo"))
                                .addValue("ValorClasificacionRiesgo", getS(r, "ValorClasificacionRiesgo")));
                    }
                }

                // Refresh bolsas
                JsonArray bolsas = params.has("p_Bolsas") && params.get("p_Bolsas").isJsonArray()
                        ? params.getAsJsonArray("p_Bolsas") : new JsonArray();
                if (!bolsas.isEmpty() && idInstrumento != null) {
                    jdbc.update(sqlDeleteBolsa, new MapSqlParameterSource().addValue("IdInstrumento", List.of(idInstrumento)));
                    for (JsonElement bel : bolsas) {
                        JsonObject b = bel.getAsJsonObject();
                        jdbc.update(sqlInsertBolsa, new MapSqlParameterSource()
                                .addValue("IdBolsaInstrumento", getL(b, "IdBolsaInstrumento"))
                                .addValue("IdBolsa", getS(b, "IdBolsa"))
                                .addValue("IdInstrumento", idInstrumento));
                    }
                }
            }

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "INSTRUMENTO_MODIFICADO");
            mensajes.add(msg);
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
        }

        result.add("P_OUT_MENSAJES", mensajes);
        result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return result;
    }

    private MapSqlParameterSource buildUpdateParams(JsonObject ins) {
        return new MapSqlParameterSource()
                .addValue("IdInstrumento", getL(ins, "IdInstrumento"))
                .addValue("ISIN", getS(ins, "ISIN"))
                .addValue("CorteMinimo", getS(ins, "CorteMinimo"))
                .addValue("EstaInscrito", getS(ins, "EstaInscrito"))
                .addValue("RazonSocial", getS(ins, "RazonSocial"))
                .addValue("FechaVencimiento", getS(ins, "FechaVencimiento"))
                .addValue("IdEmisor", getS(ins, "IdEmisor"))
                .addValue("IdTipoReajuste", getS(ins, "IdTipoReajuste"))
                .addValue("IdMoneda", getS(ins, "IdMoneda"))
                .addValue("IdTipoInstrumento", getS(ins, "IdTipoInstrumento"))
                .addValue("IdEstado", getS(ins, "IdEstado"))
                .addValue("Tamano", getL(ins, "Tamano"))
                .addValue("Precio", getS(ins, "Precio"))
                .addValue("PrecioReferencia", getS(ins, "PrecioReferencia"))
                .addValue("FrecuenciaCupon", getS(ins, "FrecuenciaCupon"))
                .addValue("NumeroCupones", getS(ins, "NumeroCupones"))
                .addValue("InstrumentoAcreditado", getS(ins, "InstrumentoAcreditado"))
                .addValue("ValorizaPor", getS(ins, "ValorizaPor"))
                .addValue("IdInstrumentoRelacionado", getL(ins, "IdInstrumentoRelacionado"))
                .addValue("EsPutEsCall", getS(ins, "EsPutEsCall"))
                .addValue("PrecioStrike", getS(ins, "PrecioStrike"))
                .addValue("TipoBonoIRF", getS(ins, "TipoBonoIRF"))
                .addValue("OrigenInstrumento", getS(ins, "OrigenInstrumento"))
                .addValue("TipoRenta", getS(ins, "TipoRenta"))
                .addValue("RelacionadoGrupo", getS(ins, "RelacionadoGrupo"))
                .addValue("FormadorMercado", getS(ins, "FormadorMercado"))
                .addValue("PactoAndino", getS(ins, "PactoAndino"))
                .addValue("FactorExposicion", getS(ins, "FactorExposicion"))
                .addValue("LargoPlazo", getS(ins, "LargoPlazo"))
                .addValue("Tasa", getS(ins, "Tasa"))
                .addValue("TipoValorizacion", getS(ins, "TipoValorizacion"))
                .addValue("IdPaisEmisor", getS(ins, "IdPaisEmisor"))
                .addValue("Serie", getS(ins, "Serie"))
                .addValue("ModalidadLiquidacion", getS(ins, "ModalidadLiquidacion"))
                .addValue("ModalidadEjercicio", getS(ins, "ModalidadEjercicio"))
                .addValue("EstiloOpcion", getS(ins, "EstiloOpcion"))
                .addValue("FechaHastaAcreditado", getS(ins, "FechaHastaAcreditado"))
                .addValue("AfectoArt107", getS(ins, "AfectoArt107"))
                .addValue("AfectoArt104", getS(ins, "AfectoArt104"))
                .addValue("CobroComisionesCustodia", getS(ins, "CobroComisionesCustodia"));
    }

    private String getS(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private Long getL(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsLong() : null;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
