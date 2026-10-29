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
import java.util.Map;

@Service
public class CrearInstrumentosService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CrearInstrumentosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.CrearInstrumentos"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (ConfiguracionSistema, secuencia) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/ins/sql/CrearInstrumentos_SelectInstrumentos.sql");
            String sqlSelectTipo = loadSql("cl/bcs/plataforma/ins/sql/CrearInstrumentos_SelectTipoInstrumento.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/ins/sql/CrearInstrumentos_InsertInstrumentos.sql");
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

            List<String> nemos = new ArrayList<>();
            for (JsonElement el : instrumentos) {
                JsonObject ins = el.getAsJsonObject();
                if (ins.has("Nemotecnico") && !ins.get("Nemotecnico").isJsonNull())
                    nemos.add(ins.get("Nemotecnico").getAsString());
            }

            List<Map<String, Object>> existing = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("Nemotecnico", nemos.isEmpty() ? List.of("") : nemos));
            if (!existing.isEmpty()) {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "ERROR");
                msg.addProperty("Codigo", "NEMOTECNICO_YA_EXISTE");
                mensajes.add(msg);
                result.add("P_OUT_MENSAJES", mensajes);
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                return result;
            }

            for (JsonElement el : instrumentos) {
                JsonObject ins = el.getAsJsonObject();
                // Each instrument is inserted using the named parameters extracted from the JSON
                MapSqlParameterSource insParams = buildInstrumentoParams(ins);
                jdbc.update(sqlInsert, insParams);
            }

            JsonArray riesgos = params.has("p_ValoresRiesgo") && params.get("p_ValoresRiesgo").isJsonArray()
                    ? params.getAsJsonArray("p_ValoresRiesgo") : new JsonArray();
            for (JsonElement el : riesgos) {
                JsonObject r = el.getAsJsonObject();
                jdbc.update(sqlInsertRiesgo, new MapSqlParameterSource()
                        .addValue("IdInstrumento", r.has("IdInstrumento") && !r.get("IdInstrumento").isJsonNull() ? r.get("IdInstrumento").getAsLong() : null)
                        .addValue("IdEntidadClasificadoraRiesgo", r.has("IdEntidadClasificadoraRiesgo") && !r.get("IdEntidadClasificadoraRiesgo").isJsonNull() ? r.get("IdEntidadClasificadoraRiesgo").getAsString() : null)
                        .addValue("ValorClasificacionRiesgo", r.has("ValorClasificacionRiesgo") && !r.get("ValorClasificacionRiesgo").isJsonNull() ? r.get("ValorClasificacionRiesgo").getAsString() : null));
            }

            JsonArray bolsas = params.has("p_Bolsas") && params.get("p_Bolsas").isJsonArray()
                    ? params.getAsJsonArray("p_Bolsas") : new JsonArray();
            for (JsonElement el : bolsas) {
                JsonObject b = el.getAsJsonObject();
                jdbc.update(sqlInsertBolsa, new MapSqlParameterSource()
                        .addValue("IdBolsaInstrumento", b.has("IdBolsaInstrumento") && !b.get("IdBolsaInstrumento").isJsonNull() ? b.get("IdBolsaInstrumento").getAsLong() : null)
                        .addValue("IdBolsa", b.has("IdBolsa") && !b.get("IdBolsa").isJsonNull() ? b.get("IdBolsa").getAsString() : null)
                        .addValue("IdInstrumento", b.has("IdInstrumento") && !b.get("IdInstrumento").isJsonNull() ? b.get("IdInstrumento").getAsLong() : null));
            }

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "INSTRUMENTO_CREADO");
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

    private MapSqlParameterSource buildInstrumentoParams(JsonObject ins) {
        return new MapSqlParameterSource()
                .addValue("IdInstrumento", getL(ins, "IdInstrumento"))
                .addValue("Nemotecnico", getS(ins, "Nemotecnico"))
                .addValue("ISIN", getS(ins, "ISIN"))
                .addValue("CorteMinimo", getS(ins, "CorteMinimo"))
                .addValue("EstaInscrito", getS(ins, "EstaInscrito"))
                .addValue("RazonSocial", getS(ins, "RazonSocial"))
                .addValue("FechaEmision", getS(ins, "FechaEmision"))
                .addValue("FechaVencimiento", getS(ins, "FechaVencimiento"))
                .addValue("IdEmisor", getS(ins, "IdEmisor"))
                .addValue("IdTipoReajuste", getS(ins, "IdTipoReajuste"))
                .addValue("IdMoneda", getS(ins, "IdMoneda"))
                .addValue("IdTipoInstrumento", getS(ins, "IdTipoInstrumento"))
                .addValue("IdEstado", getS(ins, "IdEstado"))
                .addValue("Tamano", getL(ins, "Tamano"))
                .addValue("Precio", getS(ins, "Precio"))
                .addValue("PrecioReferencia", getS(ins, "PrecioReferencia"))
                .addValue("FechaReferencia", getS(ins, "FechaReferencia"))
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
                .addValue("FechaInicioVigencia", getS(ins, "FechaInicioVigencia"))
                .addValue("TipoValorizacion", getS(ins, "TipoValorizacion"))
                .addValue("IdPaisEmisor", getS(ins, "IdPaisEmisor"))
                .addValue("Serie", getS(ins, "Serie"))
                .addValue("ModalidadLiquidacion", getS(ins, "ModalidadLiquidacion"))
                .addValue("ModalidadEjercicio", getS(ins, "ModalidadEjercicio"))
                .addValue("EstiloOpcion", getS(ins, "EstiloOpcion"))
                .addValue("FechaHastaAcreditado", getS(ins, "FechaHastaAcreditado"))
                .addValue("AfectoArt107", getS(ins, "AfectoArt107"))
                .addValue("AfectoArt104", getS(ins, "AfectoArt104"))
                .addValue("CobroComisionesCustodia", getS(ins, "CobroComisionesCustodia"))
                .addValue("EsInstrumentoCalificado", getS(ins, "EsInstrumentoCalificado"));
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
