package cl.bcs.plataforma.service.acc;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ValidaSesionUsuarioClienteService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValidaSesionUsuarioClienteService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;

    public ValidaSesionUsuarioClienteService(DataSource dataSource, NamedParameterJdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ValidaSesionUsuarioCliente"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ValidaSesionUsuarioCliente");
        String token = params.has("p_Token") ? params.get("p_Token").getAsString() : "";
        Salida out = new Salida();
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("BSCL")
                .withProcedureName("Plataforma.BD.Servicios::ACC.ValidaSesionUsuario")
                .withoutProcedureColumnMetaDataAccess();

            MapSqlParameterSource in = new MapSqlParameterSource("p_Token", token);
            Map<String, Object> res = call.execute(in);

            out.P_OUT_IDEMPRESA   = toLong(res.get("p_out_IdEmpresa"));
            out.P_OUT_IDUSUARIO   = toLong(res.get("p_out_IdUsuario"));
            out.P_OUT_NOMBREEMPRESA = str(res.get("p_out_NombreEmpresa"));
            out.P_OUT_NOMBREUSUARIO = str(res.get("p_out_NombreUsuario"));
            out.P_OUT_CODIGOMENUUSUARIO = str(res.get("p_out_CodigoMenuUsuario"));
            out.P_OUT_DEBECAMBIARCLAVE = str(res.get("p_out_DebeCambiarClave"));
            out.P_OUT_IDSOCIODENEGOCIO = toLong(res.get("p_out_IdSocioDeNegocio"));
            out.P_OUT_IDPERSONACORREDORA = toLong(res.get("p_out_IdPersonaCorredora"));
            out.P_OUT_IDMONEDASISTEMA = str(res.get("p_out_IdMonedaSistema"));
            out.P_OUT_IDPAISSISTEMA = toLong(res.get("p_out_IdPaisSistema"));
            out.P_OUT_FECHASISTEMA = str(res.get("p_out_FechaSistema"));
            out.P_OUT_BOLSADEFECTOSISTEMA = str(res.get("p_out_BolsaDefectoSistema"));
            out.P_OUT_IDEMPLEADO = toLong(res.get("p_out_IdEmpleado"));
            out.P_OUT_IDCLIENTE = toLong(res.get("p_out_IdCliente"));
            out.P_OUT_IDPERSONA = toLong(res.get("p_out_IdPersona"));
            out.P_OUT_ERRORESCONTADOR = toLong(res.get("p_out_ErroresContador"));
            out.P_OUT_ADVERTENCIASCONTADOR = toLong(res.get("p_out_AdvertenciasContador"));
        } catch (Exception e) {
            log.error("Error en ValidaSesionUsuarioCliente: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private Long toLong(Object v) { return v instanceof Number ? ((Number)v).longValue() : 0L; }
    private String str(Object v) { return v != null ? v.toString() : ""; }

    public static class Salida {
        public Long P_OUT_IDEMPRESA = 0L;
        public Long P_OUT_IDUSUARIO = 0L;
        public String P_OUT_NOMBREEMPRESA = "";
        public String P_OUT_NOMBREUSUARIO = "";
        public String P_OUT_CODIGOMENUUSUARIO = "";
        public String P_OUT_DEBECAMBIARCLAVE = "";
        public Long P_OUT_IDSOCIODENEGOCIO = 0L;
        public Long P_OUT_IDPERSONACORREDORA = 0L;
        public String P_OUT_IDMONEDASISTEMA = "";
        public Long P_OUT_IDPAISSISTEMA = 0L;
        public String P_OUT_FECHASISTEMA = "";
        public String P_OUT_BOLSADEFECTOSISTEMA = "";
        public Long P_OUT_IDEMPLEADO = 0L;
        public Long P_OUT_IDCLIENTE = 0L;
        public Long P_OUT_IDPERSONA = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_TOKEN = "";
        public List<Object> P_OUT_VALORESDEFECTOSISTEMA = new ArrayList<>();
    }
}
