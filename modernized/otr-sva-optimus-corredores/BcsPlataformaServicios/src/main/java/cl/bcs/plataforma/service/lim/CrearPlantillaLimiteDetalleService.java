package cl.bcs.plataforma.service.lim;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CrearPlantillaLimiteDetalleService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearPlantillaLimiteDetalleService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.CrearPlantillaLimiteDetalle"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray errores = new JsonArray();
        JsonArray advertencias = new JsonArray();
        try {
            String sqlInsert = loadSql("cl/bcs/plataforma/lim/sql/CrearPlantillaLimiteDetalle_Insert_Detalle.sql");
            String sqlGlosa = loadSql("cl/bcs/plataforma/lim/sql/CrearPlantillaLimiteDetalle_Select_Glosa.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString needed for v_FechaIngreso/v_FechaHoraSistema
            // Utils.getSecuencia needed for IdLimite sequence
            // CrearEventoAutorizacionMasivo needed for authorization workflow
            // AgregarLog needed for audit logging
            String idPlantilla = params.has("p_IdPlantillaLimite") && !params.get("p_IdPlantillaLimite").isJsonNull() ? params.get("p_IdPlantillaLimite").getAsString() : null;
            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") && !params.get("p_IdUsuario").isJsonNull() ? params.get("p_IdUsuario").getAsLong() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("IdLimite", (Long) null)
                    .addValue("IdPlantillaLimite", idPlantilla)
                    .addValue("IdTipoLimite", params.has("p_IdTipoLimite") && !params.get("p_IdTipoLimite").isJsonNull() ? params.get("p_IdTipoLimite").getAsString() : "")
                    .addValue("Nombre", params.has("p_Nombre") && !params.get("p_Nombre").isJsonNull() ? params.get("p_Nombre").getAsString() : "")
                    .addValue("IdMesa", params.has("p_IdMesa") && !params.get("p_IdMesa").isJsonNull() ? params.get("p_IdMesa").getAsLong() : 0L)
                    .addValue("IdCanalIngreso", params.has("p_IdCanalIngreso") && !params.get("p_IdCanalIngreso").isJsonNull() ? params.get("p_IdCanalIngreso").getAsString() : "")
                    .addValue("IdTipoInstrumento", params.has("p_IdTipoInstrumento") && !params.get("p_IdTipoInstrumento").isJsonNull() ? params.get("p_IdTipoInstrumento").getAsString() : "")
                    .addValue("IdTipoOperacion", params.has("p_IdTipoOperacion") && !params.get("p_IdTipoOperacion").isJsonNull() ? params.get("p_IdTipoOperacion").getAsString() : "")
                    .addValue("IdTipoProducto", params.has("p_IdTipoProducto") && !params.get("p_IdTipoProducto").isJsonNull() ? params.get("p_IdTipoProducto").getAsString() : "")
                    .addValue("IdInstrumento", params.has("p_IdInstrumento") && !params.get("p_IdInstrumento").isJsonNull() ? params.get("p_IdInstrumento").getAsLong() : 0L)
                    .addValue("UnidadPlazo", params.has("p_UnidadPlazo") && !params.get("p_UnidadPlazo").isJsonNull() ? params.get("p_UnidadPlazo").getAsString() : "D")
                    .addValue("Plazo", params.has("p_Plazo") && !params.get("p_Plazo").isJsonNull() ? params.get("p_Plazo").getAsInt() : 0)
                    .addValue("IdTipoReajuste", params.has("p_IdTipoReajuste") && !params.get("p_IdTipoReajuste").isJsonNull() ? params.get("p_IdTipoReajuste").getAsString() : "")
                    .addValue("CodigoOperador", params.has("p_CodigoOperador") && !params.get("p_CodigoOperador").isJsonNull() ? params.get("p_CodigoOperador").getAsString() : "")
                    .addValue("MontoTotal", params.has("p_MontoTotal") && !params.get("p_MontoTotal").isJsonNull() ? params.get("p_MontoTotal").getAsDouble() : 0.0)
                    .addValue("MontoIntraday", params.has("p_MontoIntraday") && !params.get("p_MontoIntraday").isJsonNull() ? params.get("p_MontoIntraday").getAsDouble() : 0.0)
                    .addValue("MontoOvernight", params.has("p_MontoOvernight") && !params.get("p_MontoOvernight").isJsonNull() ? params.get("p_MontoOvernight").getAsDouble() : 0.0)
                    .addValue("IdMoneda", params.has("p_IdMoneda") && !params.get("p_IdMoneda").isJsonNull() ? params.get("p_IdMoneda").getAsString() : "")
                    .addValue("IdMonedaCalculo", params.has("p_IdMonedaCalculo") && !params.get("p_IdMonedaCalculo").isJsonNull() ? params.get("p_IdMonedaCalculo").getAsString() : "")
                    .addValue("ClasificacionRiesgoMinima", params.has("p_ClasificacionRiesgoMinima") && !params.get("p_ClasificacionRiesgoMinima").isJsonNull() ? params.get("p_ClasificacionRiesgoMinima").getAsString() : "")
                    .addValue("IdEstado", params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull() ? params.get("p_IdEstado").getAsString() : "ACT")
                    .addValue("EsPersonalizada", params.has("p_EsPersonalizada") && !params.get("p_EsPersonalizada").isJsonNull() ? params.get("p_EsPersonalizada").getAsString() : "N")
                    .addValue("EsTotalizador", params.has("p_EsTotalizador") && !params.get("p_EsTotalizador").isJsonNull() ? params.get("p_EsTotalizador").getAsString() : "N")
                    .addValue("FechaIngreso", (String) null)
                    .addValue("FechaCreacion", (String) null)
                    .addValue("FechaModificacion", (String) null)
                    .addValue("IdEstadoAutorizacion", "PEN")
                    .addValue("IdUsuarioCreador", idUsuario)
                    .addValue("UI_Limite", "PLANTILLA")
                    .addValue("IdAgente", params.has("p_IdAgente") && !params.get("p_IdAgente").isJsonNull() ? params.get("p_IdAgente").getAsLong() : 0L)
                    .addValue("IdClasificacionTipoInstrumento", params.has("p_IdClasificacionTipoInstrumento") && !params.get("p_IdClasificacionTipoInstrumento").isJsonNull() ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : "")
                    .addValue("Porcentaje", params.has("p_Porcentaje") && !params.get("p_Porcentaje").isJsonNull() ? params.get("p_Porcentaje").getAsDouble() : 0.0);
            jdbc.update(sqlInsert, ps);

            result.addProperty("P_OUT_REFID", 0L);
            result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_ADVERTENCIASDETALLE", advertencias); result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        } catch (Exception e) {
            JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR_CREAR_PLANTILLA_LIMITE_DETALLE"); err.addProperty("Descripcion", e.getMessage()); errores.add(err);
            result.add("P_OUT_ERRORESDETALLE", errores); result.addProperty("P_OUT_ERRORESCONTADOR", errores.size()); result.addProperty("P_OUT_REFID", 0L);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
