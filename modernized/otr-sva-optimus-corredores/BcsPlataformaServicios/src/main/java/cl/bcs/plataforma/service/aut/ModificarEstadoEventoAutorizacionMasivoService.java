package cl.bcs.plataforma.service.aut;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ModificarEstadoEventoAutorizacionMasivoService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ModificarEstadoEventoAutorizacionMasivoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlConsultaEvento = loadSql("cl/bcs/plataforma/aut/sql/ConsultaEvento.sql");
            String sqlConsultaEtapaEvento = loadSql("cl/bcs/plataforma/aut/sql/ConsultaEtapaEvento.sql");
            String sqlConsultaRolUsuario = loadSql("cl/bcs/plataforma/aut/sql/ConsultaRolUsuario.sql");
            String sqlConsultaAutorizadoEtapaEvento = loadSql("cl/bcs/plataforma/aut/sql/ConsultaAutorizadoEtapaEvento.sql");
            String sqlActualizaEvento = loadSql("cl/bcs/plataforma/aut/sql/ActualizaEvento.sql");
            String sqlInsertaAutorizadoEtapaEvento = loadSql("cl/bcs/plataforma/aut/sql/InsertaAutorizadoEtapaEvento.sql");
            String sqlActualizaAutorizadoEtapaEvento = loadSql("cl/bcs/plataforma/aut/sql/ActualizaAutorizadoEtapaEvento.sql");
            String sqlActualizaEtapaEvento = loadSql("cl/bcs/plataforma/aut/sql/ActualizaEtapaEvento.sql");
            String sqlActualizaEventoPorEtapa = loadSql("cl/bcs/plataforma/aut/sql/ActualizaEventoPorEtapa.sql");

            Long idEmpresa = params.has("P_IDEMPRESA") && !params.get("P_IDEMPRESA").isJsonNull() ? params.get("P_IDEMPRESA").getAsLong() : null;
            Long idUsuario = params.has("P_IDUSUARIO") && !params.get("P_IDUSUARIO").isJsonNull() ? params.get("P_IDUSUARIO").getAsLong() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdUsuario", idUsuario);

            if (params.has("P_TABLAEVENTOS") && params.get("P_TABLAEVENTOS").isJsonArray()) {
                for (var el : params.getAsJsonArray("P_TABLAEVENTOS")) {
                    JsonObject ev = el.getAsJsonObject();
                    Long idEvento = ev.has("IdEvento") && !ev.get("IdEvento").isJsonNull() ? ev.get("IdEvento").getAsLong() : null;
                    String idEstado = ev.has("IdEstado") && !ev.get("IdEstado").isJsonNull() ? ev.get("IdEstado").getAsString() : null;
                    String comentario = ev.has("Comentario") && !ev.get("Comentario").isJsonNull() ? ev.get("Comentario").getAsString() : null;

                    List<Map<String, Object>> eventos = jdbc.queryForList(sqlConsultaEvento,
                            new MapSqlParameterSource().addValue("IdEvento", idEvento).addValue("IdEmpresa", idEmpresa));
                    List<Map<String, Object>> etapasEvento = jdbc.queryForList(sqlConsultaEtapaEvento,
                            new MapSqlParameterSource().addValue("IdEvento", idEvento).addValue("IdEmpresa", idEmpresa));
                    List<Map<String, Object>> rolesUsuario = jdbc.queryForList(sqlConsultaRolUsuario,
                            new MapSqlParameterSource().addValue("IdUsuario", idUsuario).addValue("IdEmpresa", idEmpresa));
                    List<Map<String, Object>> autorizadosEtapa = jdbc.queryForList(sqlConsultaAutorizadoEtapaEvento,
                            new MapSqlParameterSource().addValue("IdEvento", idEvento));

                    jdbc.update(sqlActualizaEvento, new MapSqlParameterSource()
                            .addValue("IdEvento", idEvento)
                            .addValue("IdEstado", idEstado)
                            .addValue("Comentario", comentario)
                            .addValue("IdUsuario", idUsuario));
                }
            }

            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
            result.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
            result.add("P_OUT_ERRORESDETALLE", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
            result.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
            JsonArray errs = new JsonArray(); JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR"); err.addProperty("Descripcion", e.getMessage()); errs.add(err); result.add("P_OUT_ERRORESDETALLE", errs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
