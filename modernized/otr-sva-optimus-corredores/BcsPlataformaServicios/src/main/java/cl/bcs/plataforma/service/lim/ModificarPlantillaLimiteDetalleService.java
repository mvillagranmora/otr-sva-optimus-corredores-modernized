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
public class ModificarPlantillaLimiteDetalleService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ModificarPlantillaLimiteDetalleService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ModificarPlantillaLimiteDetalle"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray errores = new JsonArray();
        JsonArray advertencias = new JsonArray();
        try {
            String sqlCount = loadSql("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Count_Existe.sql");
            String sqlIdBase = loadSql("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_IdBase.sql");
            String sqlValoresAntiguos = loadSql("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_ValoresAntiguos.sql");
            String sqlTiposDato = loadSql("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_TiposDato.sql");
            String sqlGlosa = loadSql("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_Glosa.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString needed for v_FechaIngreso
            // Utils.getFechaSistema needed for v_FechaHoraSistema
            // Utils.getSecuencia needed for IdSolicitudCambio sequence
            // CrearRegistroSolicitudCambio needed for change tracking
            // CrearEventoAutorizacionMasivo needed for authorization workflow
            // AgregarLog needed for audit logging

            Long idLimite = params.has("p_IdLimite") && !params.get("p_IdLimite").isJsonNull()
                    ? params.get("p_IdLimite").getAsLong() : null;

            List<Map<String, Object>> existeList = jdbc.queryForList(sqlCount,
                    new MapSqlParameterSource().addValue("IdLimite", idLimite));
            if (existeList.isEmpty() || Long.parseLong(existeList.get(0).get("countEntidad").toString()) == 0) {
                JsonObject err = new JsonObject(); err.addProperty("Codigo", "ENTIDAD_NO_EXISTE"); err.addProperty("Descripcion", "El detalle límite no existe"); errores.add(err);
                result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", errores.size()); result.addProperty("P_OUT_REFID", 0L);
                return result;
            }

            List<Map<String, Object>> idBaseList = jdbc.queryForList(sqlIdBase,
                    new MapSqlParameterSource().addValue("IdLimite", idLimite));
            List<Map<String, Object>> valoresAntiguos = jdbc.queryForList(sqlValoresAntiguos,
                    new MapSqlParameterSource().addValue("IdLimite", idLimite));
            List<Map<String, Object>> tiposDato = jdbc.queryForList(sqlTiposDato, new MapSqlParameterSource());
            List<Map<String, Object>> glosaList = jdbc.queryForList(sqlGlosa,
                    new MapSqlParameterSource().addValue("IdLimite", idLimite));

            result.addProperty("P_OUT_REFID", 0L);
            result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_ADVERTENCIASDETALLE", advertencias); result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        } catch (Exception e) {
            JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR_MODIFICAR_PLANTILLA_LIMITE_DETALLE"); err.addProperty("Descripcion", e.getMessage()); errores.add(err);
            result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", errores.size()); result.addProperty("P_OUT_REFID", 0L);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
