package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrated from legacy cl.bcs.plataforma.pla.RevisarRequisitosBasicos.
 * Validates that the empresa, usuario, and módulo exist and are active.
 */
@Service
public class RevisarRequisitosBasicosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(RevisarRequisitosBasicosService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_CONSULTAR_EMPRESA_SOCIO;
    private final String SQL_CONSULTAR_USUARIO;
    private final String SQL_CONSULTAR_MODULO;

    public RevisarRequisitosBasicosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_CONSULTAR_EMPRESA_SOCIO = loadSql("cl/bcs/plataforma/pla/sql/RevisarRequisitosBasicos_ConsultarSocioDeNegocio.sql");
        this.SQL_CONSULTAR_USUARIO       = loadSql("cl/bcs/plataforma/pla/sql/RevisarRequisitosBasicos_ConsultarUsuario.sql");
        this.SQL_CONSULTAR_MODULO        = loadSql("cl/bcs/plataforma/pla/sql/RevisarRequisitosBasicos_ConsultarModulo.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.RevisarRequisitosBasicos"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::RevisarRequisitosBasicos");
        EntradaRevisar in = GSON.fromJson(params, EntradaRevisar.class);
        RevisarRequisitosBasicosSalida salida = logica(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
        return JsonParser.parseString(GSON.toJson(salida)).getAsJsonObject();
    }

    public RevisarRequisitosBasicosSalida logica(Long p_IdEmpresa, Long p_IdUsuario, String p_IdModulo) {
        RevisarRequisitosBasicosSalida salida = new RevisarRequisitosBasicosSalida();

        // Validate empresa
        if (p_IdEmpresa == null) {
            salida.p_out_Mensajes.add(new Msg(0L, "E", "EMPRESA_VACIA", "Empresa vacía"));
        } else {
            List<EmpresaSocio> datosEmpresa = jdbc.query(SQL_CONSULTAR_EMPRESA_SOCIO,
                new MapSqlParameterSource("IdEmpresa", p_IdEmpresa),
                BeanPropertyRowMapper.newInstance(EmpresaSocio.class));

            if (datosEmpresa.isEmpty()) {
                salida.p_out_Mensajes.add(new Msg(0L, "E", "EMPRESA_NO_EXISTE", "Empresa no existe"));
            } else {
                EmpresaSocio e = datosEmpresa.get(0);
                if (e.IdSocioDeNegocioEmpresa == null || e.IdSocioDeNegocioEmpresa == 0)
                    salida.p_out_Mensajes.add(new Msg(0L, "E", "SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA", "Sin socio de negocio para empresa"));
                if (e.IdSocioDeNegocio == null || e.IdSocioDeNegocio == 0)
                    salida.p_out_Mensajes.add(new Msg(0L, "E", "SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE", "Socio de negocio de empresa no existe"));
                if (e.IdCliente == null || e.IdCliente == 0)
                    salida.p_out_Mensajes.add(new Msg(0L, "E", "EMPRESA_NO_CONFIGURADA_COMO_CLIENTE", "Empresa no configurada como cliente"));
            }
        }

        // Validate usuario
        if (p_IdUsuario == null) {
            salida.p_out_Mensajes.add(new Msg(0L, "E", "USUARIO_VACIO", "Usuario vacío"));
        } else {
            List<Usuario> datosUsuario = jdbc.query(SQL_CONSULTAR_USUARIO,
                new MapSqlParameterSource()
                    .addValue("IdUsuario", p_IdUsuario)
                    .addValue("IdEmpresa", p_IdEmpresa),
                BeanPropertyRowMapper.newInstance(Usuario.class));

            if (datosUsuario.isEmpty()) {
                salida.p_out_Mensajes.add(new Msg(0L, "E", "USUARIO_NO_EXISTE", "Usuario no existe"));
            } else if (!"ACT".equalsIgnoreCase(datosUsuario.get(0).IdEstado)) {
                salida.p_out_Mensajes.add(new Msg(0L, "E", "USUARIO_NO_VIGENTE", "Usuario no vigente"));
            }
        }

        // Validate modulo
        if (p_IdModulo == null) {
            salida.p_out_Mensajes.add(new Msg(0L, "E", "MODULO_VACIO", "Módulo vacío"));
        } else {
            List<Modulo> datosModulo = jdbc.query(SQL_CONSULTAR_MODULO,
                new MapSqlParameterSource("IdModulo", p_IdModulo),
                BeanPropertyRowMapper.newInstance(Modulo.class));
            if (datosModulo.isEmpty()) {
                salida.p_out_Mensajes.add(new Msg(0L, "E", "MODULO_NO_EXISTE", "Módulo no existe"));
            }
        }

        return salida;
    }

    // ---- DTOs ----
    public static class EntradaRevisar {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
    }

    public static class EmpresaSocio {
        public Long IdEmpresa;
        public Long IdSocioDeNegocioEmpresa;
        public Long IdSocioDeNegocio;
        public Long IdContraparte;
        public Long IdCliente;
    }

    public static class Modulo { public String IdModulo; }

    public static class Usuario {
        public Long IdUsuario;
        public String IdEstado;
    }

    public static class Msg {
        public Long idMensaje;
        public String tipoMensaje;
        public String codigo;
        public String descripcion;
        public Msg(Long id, String tipo, String cod, String desc) {
            this.idMensaje = id; this.tipoMensaje = tipo; this.codigo = cod; this.descripcion = desc;
        }
    }

    public static class RevisarRequisitosBasicosSalida {
        public List<Msg> p_out_Mensajes = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = RevisarRequisitosBasicosService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
