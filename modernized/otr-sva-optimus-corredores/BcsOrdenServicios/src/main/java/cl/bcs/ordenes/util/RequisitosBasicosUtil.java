package cl.bcs.ordenes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Local equivalent of cl.bcs.plataforma.pla.RevisarRequisitosBasicos.
 * Validates empresa, usuario, and modulo against HANA tables.
 * Used by Orden service classes to avoid a cross-service dependency on BcsPlataformaServicios.
 */
@Component
public class RequisitosBasicosUtil {

    private static final Logger log = LoggerFactory.getLogger(RequisitosBasicosUtil.class);

    public static final String MENSAJE_ERROR = "ERROR";

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_EMPRESA;
    private final String SQL_USUARIO;
    private final String SQL_MODULO;

    public RequisitosBasicosUtil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_EMPRESA  = loadSql("cl/bcs/ordenes/sql/RequisitosBasicos_ConsultarSocioDeNegocio.sql");
        this.SQL_USUARIO  = loadSql("cl/bcs/ordenes/sql/RequisitosBasicos_ConsultarUsuario.sql");
        this.SQL_MODULO   = loadSql("cl/bcs/ordenes/sql/RequisitosBasicos_ConsultarModulo.sql");
    }

    public static class MensajeSalida {
        public Long    IdInterno;
        public String  tipo;
        public String  codigo;
        public String  descripcion;

        public MensajeSalida(Long idInterno, String tipo, String codigo) {
            this.IdInterno   = idInterno;
            this.tipo        = tipo;
            this.codigo      = codigo;
            this.descripcion = codigo;
        }
    }

    public static class Salida {
        public List<MensajeSalida> p_out_Mensajes = new ArrayList<>();

        public long contarErrores() {
            return p_out_Mensajes.stream()
                    .filter(m -> MENSAJE_ERROR.equals(m.tipo))
                    .count();
        }
    }

    public Salida validar(Long idEmpresa, Long idUsuario, String idModulo) {
        Salida salida = new Salida();

        if (idEmpresa == null) {
            salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "EMPRESA_VACIA"));
            return salida;
        }

        List<Map<String, Object>> empresa = jdbc.queryForList(SQL_EMPRESA,
                new MapSqlParameterSource("IdEmpresa", idEmpresa));
        if (empresa.isEmpty()) {
            salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "EMPRESA_NO_EXISTE"));
        } else {
            Map<String, Object> e = empresa.get(0);
            if (isNullOrZero(e.get("IdSocioDeNegocioEmpresa")))
                salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA"));
            if (isNullOrZero(e.get("IdSocioDeNegocio")))
                salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE"));
            if (isNullOrZero(e.get("IdCliente")))
                salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "EMPRESA_NO_CONFIGURADA_COMO_CLIENTE"));
        }

        if (idUsuario == null) {
            salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "USUARIO_VACIO"));
        } else {
            MapSqlParameterSource up = new MapSqlParameterSource()
                    .addValue("IdUsuario", idUsuario)
                    .addValue("IdEmpresa", idEmpresa);
            List<Map<String, Object>> usuario = jdbc.queryForList(SQL_USUARIO, up);
            if (usuario.isEmpty()) {
                salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "USUARIO_NO_EXISTE"));
            } else {
                Object estado = usuario.get(0).get("IdEstado");
                if (estado == null || !"ACT".equalsIgnoreCase(estado.toString()))
                    salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "USUARIO_NO_VIGENTE"));
            }
        }

        if (idModulo == null) {
            salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "MODULO_VACIO"));
        } else {
            List<Map<String, Object>> modulo = jdbc.queryForList(SQL_MODULO,
                    new MapSqlParameterSource("IdModulo", idModulo));
            if (modulo.isEmpty())
                salida.p_out_Mensajes.add(new MensajeSalida(0L, MENSAJE_ERROR, "MODULO_NO_EXISTE"));
        }

        return salida;
    }

    private static boolean isNullOrZero(Object val) {
        if (val == null) return true;
        if (val instanceof Number) return ((Number) val).longValue() == 0L;
        return false;
    }

    private static String loadSql(String path) {
        try (var is = RequisitosBasicosUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
