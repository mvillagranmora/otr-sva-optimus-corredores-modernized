package cl.bcs.mesa.javacall.mrf;

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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CumpleRequisitosClienteIngresoOperacionRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CumpleRequisitosClienteIngresoOperacionRFService.class);
    private static final String SQL_CREATE_CLIENTES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTableClientesVal.sql");
    private static final String SQL_CREATE_FLUJO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTableFlujoCaja.sql");
    private static final String SQL_CREATE_PORT_FINAL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTablePortafolioMRFFinal.sql");
    private static final String SQL_CREATE_PORT = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTablePortafolioMRF.sql");
    private static final String SQL_CREATE_PREFERENCIA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTablePortafolioPreferencia.sql");
    private static final String SQL_CREATE_RESULTADO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTableResultado.sql");
    private static final String SQL_INS_CLIENTES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTableClientesVal.sql");
    private static final String SQL_INS_FLUJO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTableFlujoCaja.sql");
    private static final String SQL_INS_PORT_FINAL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTablePortafolioMRFFinal.sql");
    private static final String SQL_INS_PORT = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTablePortafolioMRF.sql");
    private static final String SQL_INS_PREFERENCIA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTablePortafoliosPreferencia.sql");
    private static final String SQL_INS_RESULTADO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTableResultado.sql");
    private static final String SQL_SEL_CLIENTES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectClientesVal.sql");
    private static final String SQL_SEL_PORT_FINAL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectPortafolioMRFFinal.sql");
    private static final String SQL_SEL_PORT = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectPortafolioMRF.sql");
    private static final String SQL_SEL_PREFERENCIA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectPortafolioPreferencia.sql");
    private static final String SQL_SEL_RESULTADO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectResultado.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CumpleRequisitosClienteIngresoOperacionRFService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.CumpleRequisitosClienteIngresoOperacionRF"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Create temp tables
            jdbc.getJdbcTemplate().execute(SQL_CREATE_CLIENTES);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_FLUJO);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_PORT_FINAL);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_PORT);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_PREFERENCIA);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_RESULTADO);

            // Populate temp tables
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdSocioNegocio", in.p_IdSocioNegocio);
            p.put("IdInstrumento", in.p_IdInstrumento);
            p.put("Monto", in.p_Monto);
            p.put("Fecha", in.p_Fecha);

            var mp = new MapSqlParameterSource(p);
            jdbc.update(SQL_INS_CLIENTES, mp);
            jdbc.update(SQL_INS_FLUJO, mp);
            jdbc.update(SQL_INS_PORT, mp);
            jdbc.update(SQL_INS_PORT_FINAL, mp);
            jdbc.update(SQL_INS_PREFERENCIA, mp);
            jdbc.update(SQL_INS_RESULTADO, mp);

            Salida out = new Salida();
            out.P_OUT_CLIENTES = JavaCallInfra.query(conn, SQL_SEL_CLIENTES, p, JavaCallInfra.fieldMapper(Cliente.class));
            out.P_OUT_PORTAFOLIO = JavaCallInfra.query(conn, SQL_SEL_PORT_FINAL, p, JavaCallInfra.fieldMapper(Portafolio.class));
            out.P_OUT_PREFERENCIA = JavaCallInfra.query(conn, SQL_SEL_PREFERENCIA, p, JavaCallInfra.fieldMapper(Preferencia.class));
            out.P_OUT_RESULTADO = JavaCallInfra.query(conn, SQL_SEL_RESULTADO, p, JavaCallInfra.fieldMapper(Resultado.class));

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CumpleRequisitosClienteIngresoOperacionRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdSocioNegocio; public String p_IdInstrumento; public BigDecimal p_Monto;
        public String p_Fecha;
    }
    public static class Cliente { public Long IdSocioNegocio; public String RazonSocial; public Boolean Validado; }
    public static class Portafolio { public Long IdPortafolio; public BigDecimal MontoDisponible; public BigDecimal LimiteInversion; }
    public static class Preferencia { public Long IdPortafolio; public String IdMoneda; public Long IdModalidadPago; }
    public static class Resultado { public Boolean Cumple; public String Mensaje; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cliente> P_OUT_CLIENTES = new ArrayList<>();
        public List<Portafolio> P_OUT_PORTAFOLIO = new ArrayList<>();
        public List<Preferencia> P_OUT_PREFERENCIA = new ArrayList<>();
        public List<Resultado> P_OUT_RESULTADO = new ArrayList<>();
    }
}
