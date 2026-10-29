package cl.bcs.plataforma.service.acc;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CrearAutorizadoOrdenService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearAutorizadoOrdenService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_VERIFICAR_EVENTO;
    private final String SQL_UPDATE_ESTADO;

    public CrearAutorizadoOrdenService(DataSource dataSource, NamedParameterJdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.SQL_VERIFICAR_EVENTO = loadSql("cl/bcs/plataforma/acc/sql/CrearAutorizadoOrden_VerificarEventoAutorizacion.sql");
        this.SQL_UPDATE_ESTADO    = loadSql("cl/bcs/plataforma/acc/sql/CrearAutorizadoOrden_UpdateEstadoConfirmado.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CrearAutorizadoOrden"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CrearAutorizadoOrden");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("BSCL")
                .withProcedureName("Plataforma.BD.Servicios::ACC.CrearAutorizadoOrden")
                .withoutProcedureColumnMetaDataAccess();

            MapSqlParameterSource sp = new MapSqlParameterSource()
                .addValue("p_IdEmpresa", in.p_IdEmpresa)
                .addValue("p_IdUsuario", in.p_IdUsuario)
                .addValue("p_IdModulo", in.p_IdModulo)
                .addValue("p_UsaReglasNegocio", in.p_UsaReglasNegocio)
                .addValue("p_IdEstado", in.p_IdEstado)
                .addValue("p_IdPersona", in.p_IdPersona)
                .addValue("p_IdPortafolio", in.p_IdPortafolio)
                .addValue("p_IdTipoAutorizado", in.p_IdTipoAutorizado)
                .addValue("p_FirmaOptimusCliente", in.p_FirmaOptimusCliente)
                .addValue("p_NumeroRegistroPublico", in.p_NumeroRegistroPublico)
                .addValue("p_Asiento", in.p_Asiento)
                .addValue("p_FichaPartida", in.p_FichaPartida)
                .addValue("p_TipoFirma", in.p_TipoFirma);

            Map<String, Object> res = call.execute(sp);
            long errores = toLong(res.get("p_out_ErroresContador"));
            long advertencias = toLong(res.get("p_out_AdvertenciasContador"));

            if (errores > 0) {
                out.P_OUT_ERRORESCONTADOR = errores;
                addMensaje(out, "ERROR", "ERROR_SP", "Error en SP CrearAutorizadoOrden");
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            if (advertencias > 0) {
                addMensaje(out, "ADVERTENCIA", "REQUIERE_AUTORIZACION", "Requiere autorización");
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            Long idAutorizado = toLong(res.get("p_out_RefId"));
            if (idAutorizado != null && idAutorizado > 0) {
                List<EventoRow> eventos = jdbc.query(SQL_VERIFICAR_EVENTO,
                    new MapSqlParameterSource()
                        .addValue("IdAutorizado", String.valueOf(idAutorizado))
                        .addValue("IdEmpresa", in.p_IdEmpresa),
                    BeanPropertyRowMapper.newInstance(EventoRow.class));

                if (!eventos.isEmpty() && eventos.get(0).CantidadEventos > 0) {
                    addMensaje(out, "AUTORIZACION", "REQUIERE_AUTORIZACION", "Autorizado requiere autorización");
                } else {
                    jdbc.update(SQL_UPDATE_ESTADO, new MapSqlParameterSource()
                        .addValue("IdAutorizado", idAutorizado)
                        .addValue("IdEstado", "ACT")
                        .addValue("IdEstadoAutorizacion", "ING"));
                }
            }
        } catch (Exception e) {
            log.error("Error en CrearAutorizadoOrden: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addMensaje(out, "ERROR", "ERROR_INTERNO", e.getMessage());
        }
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private void addMensaje(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje();
        m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private Long toLong(Object v) { return v instanceof Number ? ((Number)v).longValue() : 0L; }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_IdEstado; public String p_UsaReglasNegocio;
        public Long p_IdPersona; public Long p_IdPortafolio; public String p_IdTipoAutorizado;
        public String p_FirmaOptimusCliente = "";
        public String p_NumeroRegistroPublico = ""; public String p_Asiento = "";
        public String p_FichaPartida = ""; public String p_TipoFirma = "INDIVIDUAL";
    }

    public static class Salida {
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    public static class Mensaje {
        public String tipo; public String codigo; public String descripcion;
    }

    public static class EventoRow {
        public Long CantidadEventos;
    }
}
