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
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IngresoPreciosParidadesSpotWsService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(IngresoPreciosParidadesSpotWsService.class);
    private static final String SQL_VALIDA_MONEDA = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_ValidaMoneda.sql");
    private static final String SQL_CREATE_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_CreateTempTableIngresoParidad.sql");
    private static final String SQL_INSERT_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_InsertTempTableIngresoParidad.sql");
    private static final String SQL_UPDATE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_UpdatePreciosParidad.sql");
    private static final String SQL_DROP_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_DropTempTableIngresoParidad.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public IngresoPreciosParidadesSpotWsService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ws.IngresoPreciosParidadesSpotWs"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Create temp table
            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);

            // Insert paridades into temp table
            if (in.p_Paridades != null) {
                for (Paridad par : in.p_Paridades) {
                    var p = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdMonedaPrincipal", par.IdMonedaPrincipal)
                            .addValue("IdMonedaSecundaria", par.IdMonedaSecundaria)
                            .addValue("Paridad", par.Paridad)
                            .addValue("Fecha", in.p_Fecha);
                    jdbc.update(SQL_INSERT_TEMP, p);
                }
            }

            // Update precios
            var up = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("Fecha", in.p_Fecha);
            jdbc.update(SQL_UPDATE, up);

            // Drop temp table
            jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP);

            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("IngresoPreciosParidadesSpotWs failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Paridad { public String IdMonedaPrincipal; public String IdMonedaSecundaria; public BigDecimal Paridad; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_Fecha; public List<Paridad> p_Paridades;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
