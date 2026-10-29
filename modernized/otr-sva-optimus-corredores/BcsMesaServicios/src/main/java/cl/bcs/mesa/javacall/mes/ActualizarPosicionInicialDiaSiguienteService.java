package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActualizarPosicionInicialDiaSiguienteService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarPosicionInicialDiaSiguienteService.class);
    private static final String SQL_SELECT_PAIS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_SelectPaisPlataforma.sql");
    private static final String SQL_SELECT_POSICION = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_SelectPosicionInicial.sql");
    private static final String SQL_CREATE_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_CrearTablaTemporalParametrosPosicion.sql");
    private static final String SQL_INSERT_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_InsertTablaTemporalParametrosPosicion.sql");
    private static final String SQL_INSERT_POSICION = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_InsertPosicionInicial.sql");
    private static final String SQL_UPDATE_POSICION = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/ActualizarPosicionInicialDiaSiguiente_ActualizarPosicionInicial.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ActualizarPosicionInicialDiaSiguienteService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ws.ActualizarPosicionInicialDiaSiguiente"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Get next business day from plataforma
            JsonObject diaResult = plataforma.callObtenerDiaSiguiente(in.p_IdEmpresa, in.p_Fecha);
            String diaSiguiente = diaResult.has("p_FechaDiaSiguiente")
                    ? diaResult.get("p_FechaDiaSiguiente").getAsString() : in.p_Fecha;

            // Get paises
            Map<String, Object> pp = new HashMap<>();
            pp.put("IdEmpresa", in.p_IdEmpresa);
            List<Map<String, Object>> paises = JavaCallInfra.query(conn, SQL_SELECT_PAIS, pp, (rs, i) -> {
                Map<String, Object> m = new HashMap<>(); m.put("IdPais", rs.getLong("IdPais")); return m;
            });

            // Create temp table and update posicion
            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);

            for (Map<String, Object> pais : paises) {
                var tp = new MapSqlParameterSource()
                        .addValue("IdEmpresa", in.p_IdEmpresa)
                        .addValue("IdPais", pais.get("IdPais"))
                        .addValue("FechaPosicion", diaSiguiente);
                jdbc.update(SQL_INSERT_TEMP, tp);
            }

            var up = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("FechaPosicion", diaSiguiente);
            jdbc.update(SQL_INSERT_POSICION, up);
            jdbc.update(SQL_UPDATE_POSICION, up);

            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ActualizarPosicionInicialDiaSiguiente failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
