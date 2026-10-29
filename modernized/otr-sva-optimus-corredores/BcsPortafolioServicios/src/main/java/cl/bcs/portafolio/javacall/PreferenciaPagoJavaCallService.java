package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrates IngresaPreferenciaPagoPortafolio and ModificaPreferenciaPagoPortafolio.
 *
 * Both delegate to HANA stored procedures via SimpleJdbcCall / direct JDBC.
 * The legacy code used SQL.call() which executed CALL statements directly.
 */
@Service
public class PreferenciaPagoJavaCallService {

    private static final Logger log = LoggerFactory.getLogger(PreferenciaPagoJavaCallService.class);
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public PreferenciaPagoJavaCallService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entrada / Salida POJOs
    // ─────────────────────────────────────────────────────────────────────────

    public static class IngresaEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo = "SOC";
        public String p_UsaReglasNegocio = "N";
        public List<PreferenciaPagoItem> p_PreferenciaPagoPortafolioIng = new ArrayList<>();
    }

    public static class PreferenciaPagoItem {
        public Long IdPortafolio = 0L;
        public String IdPreferenciaPago = "";
        public Long IdCuentaBancaria = 0L;
        public Long IdTarjetaBancaria = 0L;
        public String IdEstado = "ACT";
        public String PorDefecto = "N";
    }

    public static class IngresaSalida {
        public List<JavaCallInfra.MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<Resultado> P_OUT_RESULTADOPROCESO = new ArrayList<>();
        public List<ObjDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public List<ObjDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    public static class Resultado {
        public long RefId = 0L;
    }

    public static class ObjDetalle {
        public Long IdPreferenciaPagoPortafolio;
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ModificaEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo = "SOC";
        public String p_UsaReglasNegocio = "N";
        public List<ModificaItem> p_PreferenciaPagoPortafolioMod = new ArrayList<>();
    }

    public static class ModificaItem {
        public Long IdPreferenciaPagoPortafolio;
        public Long IdPortafolio;
        public String IdPreferenciaPago;
        public Long IdCuentaBancaria;
        public Long IdTarjetaBancaria;
        public String IdEstado;
        public String PorDefecto;
        public Long IdPersonaRelacionada;
        public Long IdCarga;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IngresaPreferenciaPagoPortafolio
    // ─────────────────────────────────────────────────────────────────────────

    public JsonObject ingresaPreferenciaPago(Connection conn, JsonObject in) throws Exception {
        IngresaEntrada entrada = GSON.fromJson(in.toString(), IngresaEntrada.class);
        IngresaSalida salida = new IngresaSalida();

        // SimpleJdbcCall on DataSource — SP accepts scalar IN + two TABLE OUT params
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
            .withSchemaName("BSCL")
            .withProcedureName("Portafolio.BD.Servicios::CrearPreferenciaPago")
            .withoutProcedureColumnMetaDataAccess();

        for (PreferenciaPagoItem item : entrada.p_PreferenciaPagoPortafolioIng) {
            try {
                MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("P_IDEMPRESA", entrada.p_IdEmpresa)
                    .addValue("P_IDUSUARIO", entrada.p_IdUsuario)
                    .addValue("P_IDMODULO", entrada.p_IdModulo)
                    .addValue("P_USAREGLASNEGOCIO", entrada.p_UsaReglasNegocio)
                    .addValue("P_IDPORTAFOLIO", item.IdPortafolio)
                    .addValue("P_IDPREFERENCIAPAGO", item.IdPreferenciaPago)
                    .addValue("P_IDCUENTABANCARIA", item.IdCuentaBancaria)
                    .addValue("P_IDTARJETABANCARIA", item.IdTarjetaBancaria)
                    .addValue("P_IDESTADO", item.IdEstado)
                    .addValue("P_PORDEFECTO", item.PorDefecto);

                Map<String, Object> result = call.execute(params);

                Resultado r = new Resultado();
                Object refId = result.get("P_OUT_REFID");
                r.RefId = refId instanceof Number ? ((Number) refId).longValue() : 0L;
                salida.P_OUT_RESULTADOPROCESO.add(r);

                Object errCnt = result.get("P_OUT_ERRORESCONTADOR");
                if (errCnt instanceof Number) salida.P_OUT_ERRORESCONTADOR += ((Number) errCnt).longValue();
                Object advCnt = result.get("P_OUT_ADVERTENCIASCONTADOR");
                if (advCnt instanceof Number) salida.P_OUT_ADVERTENCIASCONTADOR += ((Number) advCnt).longValue();

            } catch (Exception e) {
                log.error("Error al llamar SP CrearPreferenciaPago para IdPortafolio={}", item.IdPortafolio, e);
                salida.P_OUT_ERRORESCONTADOR++;
                ObjDetalle err = new ObjDetalle();
                err.Codigo = "ERROR_SP_CREAR_PREFERENCIA_PAGO";
                err.Descripcion = e.getMessage();
                salida.P_OUT_ERRORESDETALLE.add(err);
            }
        }

        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ModificaPreferenciaPagoPortafolio
    // Legacy pattern: CREATE LOCAL TEMPORARY TABLE #ModificaPPPMasivo_P_REGISTROCAMBIO,
    // INSERT into it, then CALL the SP, then DROP TABLE.
    // We replicate this via direct JDBC to preserve HANA temp-table semantics.
    // ─────────────────────────────────────────────────────────────────────────

    public JsonObject modificaPreferenciaPago(Connection conn, JsonObject in) throws Exception {
        ModificaEntrada entrada = GSON.fromJson(in.toString(), ModificaEntrada.class);
        IngresaSalida salida = new IngresaSalida();

        for (ModificaItem item : entrada.p_PreferenciaPagoPortafolioMod) {
            try {
                String tmpTable = "#ModificaPPPMasivo_P_REGISTROCAMBIO";
                String createSql = "CREATE LOCAL TEMPORARY TABLE " + tmpTable +
                    " (\"IdPreferenciaPagoPortafolio\" BIGINT, \"IdPortafolio\" BIGINT," +
                    " \"IdPreferenciaPago\" NVARCHAR(50), \"IdCuentaBancaria\" BIGINT," +
                    " \"IdTarjetaBancaria\" BIGINT, \"IdEstado\" NVARCHAR(10)," +
                    " \"PorDefecto\" NVARCHAR(1), \"IdPersonaRelacionada\" BIGINT, \"IdCarga\" BIGINT)";

                try (Statement stmt = conn.createStatement()) {
                    // DROP TABLE if from a previous failed run in same connection
                    try { stmt.execute("DROP TABLE " + tmpTable); } catch (Exception ignored) {}
                    stmt.execute(createSql);
                }

                String insertSql = "INSERT INTO " + tmpTable +
                    " VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setObject(1, item.IdPreferenciaPagoPortafolio);
                    ps.setObject(2, item.IdPortafolio);
                    ps.setString(3, item.IdPreferenciaPago);
                    ps.setObject(4, item.IdCuentaBancaria);
                    ps.setObject(5, item.IdTarjetaBancaria);
                    ps.setString(6, item.IdEstado);
                    ps.setString(7, item.PorDefecto);
                    ps.setObject(8, item.IdPersonaRelacionada);
                    ps.setObject(9, item.IdCarga);
                    ps.executeUpdate();
                }

                String callSql = "CALL \"BSCL\".\"Portafolio.BD.Servicios::ModificarPreferenciaPago\"" +
                    "(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_UsaReglasNegocio," + tmpTable +
                    ",?,?,?,?,?)";

                long outRefId = 0L;
                long outErrCount = 0L;
                long outAdvCount = 0L;

                try (CallableStatement cs = conn.prepareCall(
                        "CALL \"BSCL\".\"Portafolio.BD.Servicios::ModificarPreferenciaPago\"(?,?,?,?," + tmpTable + ",?,?,?,?,?)")) {
                    cs.setLong(1, entrada.p_IdEmpresa);
                    cs.setLong(2, entrada.p_IdUsuario);
                    cs.setString(3, entrada.p_IdModulo);
                    cs.setString(4, entrada.p_UsaReglasNegocio);
                    cs.registerOutParameter(5, java.sql.Types.BIGINT);    // P_OUT_REFID
                    cs.registerOutParameter(6, java.sql.Types.BIGINT);    // P_OUT_ADVERTENCIASCONTADOR
                    cs.registerOutParameter(7, java.sql.Types.OTHER);     // P_OUT_ADVERTENCIASDETALLE (TABLE)
                    cs.registerOutParameter(8, java.sql.Types.BIGINT);    // P_OUT_ERRORESCONTADOR
                    cs.registerOutParameter(9, java.sql.Types.OTHER);     // P_OUT_ERRORESDETALLE (TABLE)
                    cs.execute();
                    outRefId = cs.getLong(5);
                    outAdvCount = cs.getLong(6);
                    outErrCount = cs.getLong(8);
                    ResultSet rsAdv = (ResultSet) cs.getObject(7);
                    if (rsAdv != null) rsAdv.close();
                    ResultSet rsErr = (ResultSet) cs.getObject(9);
                    if (rsErr != null) rsErr.close();
                }

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DROP TABLE " + tmpTable);
                }

                Resultado r = new Resultado();
                r.RefId = outRefId;
                salida.P_OUT_RESULTADOPROCESO.add(r);
                salida.P_OUT_ERRORESCONTADOR += outErrCount;
                salida.P_OUT_ADVERTENCIASCONTADOR += outAdvCount;

            } catch (Exception e) {
                log.error("Error al llamar SP ModificarPreferenciaPago para IdPreferenciaPagoPortafolio={}", item.IdPreferenciaPagoPortafolio, e);
                salida.P_OUT_ERRORESCONTADOR++;
                ObjDetalle err = new ObjDetalle();
                err.Codigo = "ERROR_SP_MODIFICAR_PREFERENCIA_PAGO";
                err.Descripcion = e.getMessage();
                salida.P_OUT_ERRORESDETALLE.add(err);
            }
        }

        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }
}
