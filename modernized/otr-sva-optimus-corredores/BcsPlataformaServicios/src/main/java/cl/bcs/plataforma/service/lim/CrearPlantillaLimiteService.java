package cl.bcs.plataforma.service.lim;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CrearPlantillaLimiteService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearPlantillaLimiteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.CrearPlantillaLimite"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray errores = new JsonArray();
        JsonArray advertencias = new JsonArray();
        try {
            String sqlCount = loadSql("cl/bcs/plataforma/lim/sql/CrearPlantillaLimite_Count_Existe.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/lim/sql/CrearPlantillaLimite_Insert_Plantilla.sql");

            String idPlantilla = params.has("p_IdPlantillaLimite") && !params.get("p_IdPlantillaLimite").isJsonNull()
                    ? params.get("p_IdPlantillaLimite").getAsString().toUpperCase() : null;
            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") && !params.get("p_IdUsuario").isJsonNull() ? params.get("p_IdUsuario").getAsLong() : null;
            String descripcion = params.has("p_DescripcionPlantillaLimite") && !params.get("p_DescripcionPlantillaLimite").isJsonNull()
                    ? params.get("p_DescripcionPlantillaLimite").getAsString().toUpperCase() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull()
                    ? params.get("p_IdEstado").getAsString() : "ACT";
            String porDefecto = params.has("p_PorDefecto") && !params.get("p_PorDefecto").isJsonNull()
                    ? params.get("p_PorDefecto").getAsString() : "N";

            List<Map<String, Object>> existing = jdbc.queryForList(sqlCount,
                    new MapSqlParameterSource().addValue("IdPlantillaLimite", idPlantilla));
            if (!existing.isEmpty()) {
                Object cnt = existing.get(0).get("countEntidad");
                if (cnt != null && Long.parseLong(cnt.toString()) > 0) {
                    JsonObject err = new JsonObject(); err.addProperty("Codigo", "PLANTILLA_EXISTE"); err.addProperty("Descripcion", "La plantilla límite ya existe"); errores.add(err);
                    result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", errores.size()); result.addProperty("P_OUT_REFID", 0L);
                    return result;
                }
            }

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString needed for v_FechaIngreso
            // Utils.getFechaSistema needed for v_FechaHoraSistema
            // CrearEventoAutorizacionMasivo needed for authorization workflow
            // AgregarLog needed for audit logging
            MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("IdPlantillaLimite", idPlantilla)
                    .addValue("DescripcionPlantillaLimite", descripcion)
                    .addValue("EsPerfilPersonalizado", "N")
                    .addValue("FechaCreacion", (String) null)
                    .addValue("FechaModificacion", (String) null)
                    .addValue("FechaIngreso", (String) null)
                    .addValue("IdEstadoAutorizacion", "PEN")
                    .addValue("IdUsuarioCreador", idUsuario)
                    .addValue("IdEstado", idEstado)
                    .addValue("PorDefecto", porDefecto)
                    .addValue("IdEmpresa", idEmpresa);
            jdbc.update(sqlInsert, ps);

            result.addProperty("P_OUT_REFID", 1L);
            result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_ADVERTENCIASDETALLE", advertencias); result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        } catch (Exception e) {
            JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR_CREAR_PLANTILLA_LIMITE"); err.addProperty("Descripcion", e.getMessage()); errores.add(err);
            result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", errores.size()); result.addProperty("P_OUT_REFID", 0L);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
