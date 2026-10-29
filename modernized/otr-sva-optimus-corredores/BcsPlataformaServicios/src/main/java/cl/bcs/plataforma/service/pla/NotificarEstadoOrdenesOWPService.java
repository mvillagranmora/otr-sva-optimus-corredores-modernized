package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificarEstadoOrdenesOWPService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(NotificarEstadoOrdenesOWPService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_HOMOLOGACION;

    public NotificarEstadoOrdenesOWPService(DataSource dataSource, NamedParameterJdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.SQL_HOMOLOGACION = loadSql("cl/bcs/plataforma/pla/sql/ConsultarHomologacionEstadoOrden.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::NotificarEstadoOrdenesOWP");
        Salida out = new Salida();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;

            JsonArray ordenes = params.has("p_Ordenes") ? params.getAsJsonArray("p_Ordenes") : new JsonArray();
            if (ordenes.size() == 0) {
                addMsg(out, "ERROR", "SIN_ORDENES", "Se requiere al menos una orden en p_Ordenes");
                out.P_OUT_ERRORESCONTADOR = 1L;
                return toJson(out);
            }

            JsonObject primeraOrden = ordenes.get(0).getAsJsonObject();
            String estadoRaw = primeraOrden.has("Estado") ? primeraOrden.get("Estado").getAsString() : "";
            String mercadoRaw = primeraOrden.has("Mercado") ? primeraOrden.get("Mercado").getAsString() : "";
            String mercadoClasif = "A".equalsIgnoreCase(mercadoRaw) ? "RV" : "FM";

            // Homologar estado
            List<Map<String, Object>> homRows = jdbc.queryForList(SQL_HOMOLOGACION,
                new MapSqlParameterSource()
                    .addValue("p_Estado", estadoRaw)
                    .addValue("p_Mercado", mercadoClasif));

            if (homRows.isEmpty()) {
                addMsg(out, "ERROR", "ESTADO_NO_HOMOLOGADO", "Estado OWP no encontrado para: " + estadoRaw + "/" + mercadoClasif);
                out.P_OUT_ERRORESCONTADOR = 1L;
                return toJson(out);
            }
            String estadoOWP = String.valueOf(homRows.get(0).get("IdEstadoOWP"));

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);

            JsonArray operaciones = new JsonArray();
            for (JsonElement el : ordenes) {
                JsonObject ord = el.getAsJsonObject();
                JsonObject operacion = new JsonObject();
                operacion.addProperty("Solsec",      str(ord, "Solsec"));
                operacion.addProperty("NTitulos",    dfFmt(df, ord, "NTitulos"));
                operacion.addProperty("Precio",      dfFmt(df, ord, "Precio"));
                operacion.addProperty("Fecha",       str(ord, "Fecha"));
                operacion.addProperty("Estado",      estadoOWP);
                operacion.addProperty("Comision",    dfFmt(df, ord, "Comision"));
                operacion.addProperty("Gastos",      dfFmt(df, ord, "Gastos"));
                operacion.addProperty("Mercado",     str(ord, "Mercado"));
                operacion.addProperty("Divisa",      str(ord, "Divisa"));
                operacion.addProperty("TipoCambio",  dfFmt(df, ord, "TipoCambio"));
                operacion.addProperty("SolSec_Asoc", str(ord, "SolsecAsoc"));
                operacion.addProperty("Nemo",        str(ord, "Nemo"));
                operaciones.add(operacion);
            }

            // InscribirProcesoBatch via stored procedure
            try {
                SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                    .withSchemaName("BSCL")
                    .withProcedureName("Plataforma.BD.Servicios::BAT.InscribirProcesoBatch")
                    .withoutProcedureColumnMetaDataAccess();

                JsonObject batchParams = new JsonObject();
                batchParams.addProperty("IdEmpresa", idEmpresa.toString());
                batchParams.addProperty("IdUsuario", idUsuario.toString());
                batchParams.addProperty("Operaciones", operaciones.toString());

                call.execute(new MapSqlParameterSource()
                    .addValue("p_IdEmpresa", idEmpresa)
                    .addValue("p_IdUsuario", idUsuario)
                    .addValue("p_IdModulo", "PLA")
                    .addValue("p_TipoPrograma", "NOTIFICAR_ORDEN_OWP")
                    .addValue("p_Parametros", batchParams.toString())
                    .addValue("p_SegundosDesfase", 0L));
            } catch (Exception batchEx) {
                log.warn("Error al inscribir proceso batch OWP (no bloqueante): {}", batchEx.getMessage());
            }

            addMsg(out, "OK", "INSCRITO", "Notificación OWP inscrita para " + ordenes.size() + " orden(es)");
        } catch (Exception e) {
            log.error("Error en NotificarEstadoOrdenesOWP: {}", e.getMessage(), e);
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
            out.P_OUT_ERRORESCONTADOR = 1L;
        }
        return toJson(out);
    }

    private String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private String dfFmt(DecimalFormat df, JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "0";
        try { return df.format(o.get(key).getAsBigDecimal()); } catch (Exception e) { return "0"; }
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) { return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject(); }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje { public String tipo; public String codigo; public String descripcion; }
}
