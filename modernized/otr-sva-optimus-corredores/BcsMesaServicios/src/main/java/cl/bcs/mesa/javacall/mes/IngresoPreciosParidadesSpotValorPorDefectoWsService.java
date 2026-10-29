package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
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
import java.util.List;

@Service
public class IngresoPreciosParidadesSpotValorPorDefectoWsService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(IngresoPreciosParidadesSpotValorPorDefectoWsService.class);
    private static final String SQL_CREATE_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_CreateTempTableIngresoParidad.sql");
    private static final String SQL_INSERT_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_InsertTempTableIngresoParidad.sql");
    private static final String SQL_UPDATE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_UpdatePreciosParidad.sql");
    private static final String SQL_DROP_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_DropTempTableIngresoParidad.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public IngresoPreciosParidadesSpotValorPorDefectoWsService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ws.IngresoPreciosParidadesSpotValorPorDefectoWs"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);
            if (in.p_Paridades != null) {
                for (Paridad par : in.p_Paridades) {
                    var p = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdMonedaPrincipal", par.IdMonedaPrincipal)
                            .addValue("IdMonedaSecundaria", par.IdMonedaSecundaria)
                            .addValue("ParidadPorDefecto", par.ParidadPorDefecto)
                            .addValue("Fecha", in.p_Fecha);
                    jdbc.update(SQL_INSERT_TEMP, p);
                }
            }
            var up = new MapSqlParameterSource().addValue("IdEmpresa", in.p_IdEmpresa).addValue("Fecha", in.p_Fecha);
            jdbc.update(SQL_UPDATE, up);
            jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP);

            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("IngresoPreciosParidadesSpotValorPorDefectoWs failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Paridad { public String IdMonedaPrincipal; public String IdMonedaSecundaria; public BigDecimal ParidadPorDefecto; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_Fecha; public List<Paridad> p_Paridades;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
