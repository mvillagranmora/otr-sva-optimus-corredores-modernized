package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AperturaMesasModulosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AperturaMesasModulosService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_ABRIR_MESAS;
    private final String SQL_ABRIR_MODULOS;
    private final String SQL_PARIDAD;

    private static final String SQL_MODULOS_CONFIG = "SELECT \"Valor\" AS \"Modulos\" FROM \"BSCL\".\"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.AperturaDiariaModulos'";
    private static final String SQL_MESA_SPOT      = "SELECT \"IdMesa\", \"Nombre\" FROM \"BSCL\".\"BSCL.Mesa.BD::MES.Mesa\" WHERE \"Codigo\" = 'MESA_SPOT'";
    private static final String SQL_FECHA_SISTEMA  = "SELECT \"Valor\" FROM \"BSCL\".\"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.FechaSistema'";

    public AperturaMesasModulosService(DataSource dataSource, NamedParameterJdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.SQL_ABRIR_MESAS  = loadSql("cl/bcs/plataforma/pla/sql/AperturaMesasModulosAperturaMesas.sql");
        this.SQL_ABRIR_MODULOS = loadSql("cl/bcs/plataforma/pla/sql/AperturaMesasModulosAperturaModulos.sql");
        this.SQL_PARIDAD      = loadSql("cl/bcs/plataforma/pla/sql/AperturaMesasModulosConsultarParidadTipoCambio.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.AperturaMesasModulos"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AperturaMesasModulos");
        Salida out = new Salida();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "PLA";

            // Get system date
            List<Map<String, Object>> fechaRows = jdbc.queryForList(SQL_FECHA_SISTEMA, new MapSqlParameterSource());
            String fechaSistema = fechaRows.isEmpty() ? LocalDate.now().toString() : fechaRows.get(0).get("Valor").toString();

            // Verify currencies (DO and UF tipo cambio)
            List<Map<String, Object>> paridadRows = jdbc.queryForList(SQL_PARIDAD,
                new MapSqlParameterSource()
                    .addValue("p_IdEmpresa", idEmpresa)
                    .addValue("p_IdUsuario", idUsuario)
                    .addValue("p_Fecha", Date.valueOf(fechaSistema)));

            boolean tieneUSD = paridadRows.stream().anyMatch(r ->
                "DO".equalsIgnoreCase(String.valueOf(r.get("IdMoneda"))) &&
                toBD(r.get("TipoCambioMercado")).compareTo(BigDecimal.ZERO) != 0);
            boolean tieneUF = paridadRows.stream().anyMatch(r ->
                "UF".equalsIgnoreCase(String.valueOf(r.get("IdMoneda"))) &&
                toBD(r.get("TipoCambioMercado")).compareTo(BigDecimal.ZERO) != 0);

            if (!tieneUSD || !tieneUF) {
                addMsg(out, "ADVERTENCIA", "SIN_PARIDAD", "No hay Valores UF o USD para el dia");
                out.P_OUT_ERRORESCONTADOR = 1L;
                return toJson(out);
            }

            // Abrir Mesas - get MESA_SPOT and open
            abrirMesas(idEmpresa, idUsuario);

            // Abrir Módulos
            abrirModulos(idEmpresa, idUsuario, idModulo);

        } catch (Exception e) {
            log.error("Error en AperturaMesasModulos: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
        }
        log.info("FIN::AperturaMesasModulos");
        return toJson(out);
    }

    private void abrirMesas(Long idEmpresa, Long idUsuario) {
        List<Map<String, Object>> mesaSpotRows = jdbc.queryForList(SQL_MESA_SPOT, new MapSqlParameterSource());
        if (mesaSpotRows.isEmpty()) return;
        Long idMesa = ((Number) mesaSpotRows.get(0).get("IdMesa")).longValue();
        String nombreMesa = String.valueOf(mesaSpotRows.get(0).get("Nombre"));

        // Create temp table and insert
        try {
            jdbc.update("DROP TABLE #AperturaMesa_p_DatosMesas", new MapSqlParameterSource());
        } catch (Exception ignored) {}
        jdbc.update("CREATE LOCAL TEMPORARY TABLE #AperturaMesa_p_DatosMesas(\"IdMesa\" BIGINT, \"NombreMesa\" NVARCHAR(30), \"IdEstadoMesa\" NVARCHAR(3))", new MapSqlParameterSource());
        jdbc.update("INSERT INTO #AperturaMesa_p_DatosMesas(\"IdMesa\",\"NombreMesa\",\"IdEstadoMesa\") VALUES(:IdMesa,:NombreMesa,'A')",
            new MapSqlParameterSource().addValue("IdMesa", idMesa).addValue("NombreMesa", nombreMesa));

        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
            .withSchemaName("BSCL")
            .withProcedureName("Plataforma.BD.Servicios::PAU.AbrirMesas")
            .withoutProcedureColumnMetaDataAccess();
        call.execute(new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdUsuario", idUsuario));
    }

    private void abrirModulos(Long idEmpresa, Long idUsuario, String idModulo) {
        List<Map<String, Object>> modRows = jdbc.queryForList(SQL_MODULOS_CONFIG, new MapSqlParameterSource());
        if (modRows.isEmpty()) return;
        String modulosConfig = String.valueOf(modRows.get(0).get("Modulos"));
        List<String> modulos = new ArrayList<>(Arrays.asList(modulosConfig.split(";")));
        modulos.remove("MSP"); // MSP opened separately via CambiarEstadoModuloMesaSpot (cross-domain)

        try {
            jdbc.update("DROP TABLE #AbrirModulo_p_Modulo", new MapSqlParameterSource());
        } catch (Exception ignored) {}
        jdbc.update("CREATE LOCAL TEMPORARY TABLE #AbrirModulo_p_Modulo(\"CodigoModulo\" NVARCHAR(40),\"Estado\" NVARCHAR(3),\"CodigoUsuario\" NVARCHAR(40))", new MapSqlParameterSource());

        for (String cod : modulos) {
            jdbc.update("INSERT INTO #AbrirModulo_p_Modulo(\"CodigoModulo\",\"Estado\",\"CodigoUsuario\") VALUES(:cod,'ABT','SISTEMA')",
                new MapSqlParameterSource("cod", cod));
        }

        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
            .withSchemaName("BSCL")
            .withProcedureName("Plataforma.BD.Servicios::PAU.AbrirModulos")
            .withoutProcedureColumnMetaDataAccess();
        call.execute(new MapSqlParameterSource()
            .addValue("p_IdEmpresa", idEmpresa)
            .addValue("p_IdUsuario", idUsuario)
            .addValue("p_IdModulo", idModulo));
    }

    private BigDecimal toBD(Object v) {
        if (v instanceof Number) return new BigDecimal(v.toString());
        return BigDecimal.ZERO;
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
