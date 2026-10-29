package cl.bcs.plataforma.login.sesion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import cl.bcs.plataforma.login.jdbc.SqlNombrado;

/**
 * Opens a session with user and password (RULE-010..030 of the login). Port of legacy {@code CrearSesion.crearSesionUsuario}
 * ({@code CrearSesion.java:226-393}) over the legacy SQL, on the connection of the request, bug for bug where it matters for the answer:
 * every refusal gives the SAME answer (an empty entity) so that nothing tells an unknown user from a wrong password; a wrong password adds one to the
 * attempt counter and, at the configured maximum, blocks the user for a while; the entity of the user (employee, person, client) must be active for the
 * channel; a new session closes the open ones of the user and channel when the single-session switch is on; the web-service channel reuses its token
 * while it is not idle. The caller commits in every case except an exception (T-13).
 *
 * <p>Stored password: the browser sends {@code SHA-256(clave)} in hex; the server hashes that text again and compares with {@code ACC.Usuario.Clave}
 * in upper case (T-09). The hash is never re-encoded to BCrypt here (T-08): other applications still read it.
 */
@Service
public class CrearSesionServicio {

    public static final String CANAL_CORREDORES = "COR";
    public static final String CANAL_NEGOCIOS = "AGE";
    public static final String CANAL_CLIENTES = "CLI";
    public static final String CANAL_WEB_SERVICES = "WS";
    public static final String INICIO_SESION_EXITO = "INICIO SESION EXITOSA";

    /** Row of the user query (legacy {@code CrearSesion.UsuarioProceso}). */
    public static class UsuarioProceso {
        public Long IdUsuario;
        public String Clave;
        public java.sql.Date FechaVigencia;
        public String IdEstado;
        public Integer IntentosConexion;
        public Timestamp FechaBloqueoTemporal;
        public String EsAutorizado;
        public String TipoEntidad;
        public Long IdEntidad;
        public String UsuarioClientes;
        public String UsuarioNegocios;
        public String UsuarioServicios;
        public String UsuarioCorredores;
        public String IdTokenSesion;
        public String Param_FechaSistema;
        public String Param_IntentosMaximo;
        public Timestamp CurrentTimestamp;
        public String Param_ClaveUsuarioTiempoBloqueo;
        public String IdCanal;
    }

    /** Row of the entity queries (employee, person, client). */
    public static class SocioNegocio {
        public Long IdEmpleado;
        public Long IdCliente;
        public Long IdPersona;
        public String IdEstado;
    }

    /** Row of the session query. */
    public static class Ses {
        public Long IdUsuario;
        public Timestamp FechaUltimoUso;
    }

    /** Row of the parameter query. */
    public static class Parametro {
        public String Valor;
    }

    /** Row of the clock query. */
    public static class RelojUtc {
        public Timestamp UTC;
    }

    /** Update of a session (legacy {@code ProcesaLlamadaServletCall.SesionUpd}). */
    public static class SesionUpd {
        public java.sql.Timestamp p_FechaUso;
        public String p_Token;
        public String p_TokenValido;
    }

    /** Row written to ACC.Sesion (legacy {@code Sesion.UsuarioProceso}). */
    public static class SesionNueva {
        public Long IdUsuario;
        public String IdTokenSesion;
        public String IdCanal;
        public String IdEntidadSSO;
    }

    /** The answer. {@code entidad} is "" when the session was refused. */
    public static class Resultado {
        public String entidad = "";
        public String token = "";
    }

    static final String QUERY_USUARIOS =
            "SELECT \"IdUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\",\"IntentosConexion\",\"FechaBloqueoTemporal\",\"EsAutorizado\", \"TipoEntidad\", \"IdEntidad\","
            + "\"UsuarioClientes\", \"UsuarioNegocios\", \"UsuarioServicios\", \"UsuarioCorredores\",c1.\"Valor\" as \"Param_FechaSistema\",c2.\"Valor\" as \"Param_IntentosMaximo\","
            + "CURRENT_UTCTIMESTAMP as \"CurrentTimestamp\",c3.\"Valor\" AS \"Param_ClaveUsuarioTiempoBloqueo\" "
            + "FROM \"BSCL.Plataforma.BD::ACC.Usuario\" u "
            + "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" c1 ON u.\"IdEmpresa\" = c1.\"IdEmpresa\" AND c1.\"CodigoParametro\" = 'PLA.FechaSistema' "
            + "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" c2 ON u.\"IdEmpresa\" = c2.\"IdEmpresa\" AND c2.\"CodigoParametro\" = 'PLA.ClaveUsuarioIntentos' "
            + "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" c3 ON u.\"IdEmpresa\" = c3.\"IdEmpresa\" AND c3.\"CodigoParametro\" = 'PLA.ClaveUsuarioTiempoBloqueo' "
            + "WHERE u.\"IdEmpresa\" = :p_IdEmpresa AND \"CodigoUsuario\" = :p_NombreUsuario";

    static final String UPDATE_INTENTOS = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IntentosConexion\" = :IntentosConexion WHERE \"IdUsuario\" = :IdUsuario";

    static final String UPDATE_BLOQUEO_TEMPORAL = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" "
            + "SET \"FechaBloqueoTemporal\" = ADD_SECONDS (CURRENT_UTCTIMESTAMP, to_int(:Param_ClaveUsuarioTiempoBloqueo)*60),\"IntentosConexion\" = 0 "
            + "WHERE \"IdUsuario\" = :IdUsuario";

    static final String QUERY_EMPLEADO = "SELECT \"IdEmpleado\","
            + "CASE \"EMP\".\"IdEstado\" WHEN 'ACT' THEN CASE \"EMP\".\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstado\""
            + " FROM \"BSCL.SocioNegocio.BD::EMP.Empleado\" \"EMP\" WHERE \"IdEmpleado\" = :IdEntidad";

    static final String QUERY_PERSONA = "SELECT \"IdPersona\","
            + "CASE \"PER\".\"IdEstado\" WHEN 'ACT' THEN CASE \"PER\".\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstado\""
            + " FROM \"BSCL.SocioNegocio.BD::PER.Persona\" \"PER\" WHERE \"IdPersona\" = :IdEntidad";

    static final String QUERY_CLIENTE = "SELECT \"IdCliente\","
            + "CASE \"CLI\".\"IdEstado\" WHEN 'ACT' THEN CASE \"CLI\".\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstado\""
            + " FROM \"BSCL.SocioNegocio.BD::CLI.Cliente\" \"CLI\" WHERE \"IdCliente\" = :IdEntidad";

    static final String QUERY_BUSCA_TOKEN = "SELECT TOP 1 \"IdTokenSesion\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" "
            + "WHERE \"TokenValido\" = 'S' AND \"IdUsuario\" = :IdUsuario AND \"IdCanal\" = :IdCanal";

    static final String QUERY_SESION = "SELECT \"IdUsuario\", \"FechaUltimoUso\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" "
            + "WHERE \"IdTokenSesion\" = :p_Token AND \"TokenValido\" = 'S'";

    static final String UPDATE_SESION = "UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" "
            + "SET \"FechaUltimoUso\"= :p_FechaUso, \"UsoToken\"= \"UsoToken\" + 1, \"TokenValido\" = :p_TokenValido WHERE \"IdTokenSesion\" = :p_Token";

    static final String INSERT_SESION = "INSERT INTO \"BSCL.Plataforma.BD::ACC.Sesion\" "
            + "(\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\",\"IdEntidadSSO\") "
            + "VALUES (:IdTokenSesion,:IdUsuario,'S',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,:IdCanal,:IdEntidadSSO)";

    /** T-04: closes the open sessions of the user and channel when {@code PLA.SesionUnicaUsuario} is "S" and the channel is in {@code PLA.CanalesSesionUnica}. */
    static final String CLOSE_SESION = "UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" SET \"TokenValido\" = 'N' "
            + "WHERE \"IdUsuario\" = :IdUsuario AND \"IdCanal\" = :IdCanal AND \"TokenValido\" = 'S' "
            + "AND ( SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.SesionUnicaUsuario' ) = 'S' "
            + "AND EXISTS ( SELECT 1 FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" c "
            + "WHERE c.\"CodigoParametro\" = 'PLA.CanalesSesionUnica' AND ';' || c.\"Valor\" || ';' LIKE '%;' || \"IdCanal\" || ';%' )";

    static final String QUERY_PARAMETRO = "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = :p_Parametro";

    static final String QUERY_RELOJ_UTC = "SELECT CURRENT_UTCTIMESTAMP as \"UTC\" FROM DUMMY";

    private final AuditoriaSesion auditoria;

    public CrearSesionServicio(AuditoriaSesion auditoria) {
        this.auditoria = auditoria;
    }

    /** {@code SHA-256(texto)} in hex, upper case, as {@code DigestUtils.sha256Hex(texto.getBytes()).toUpperCase()}. */
    public static String sha256Mayuscula(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().toUpperCase();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Resultado crearSesionUsuario(Connection conn, Long idEmpresa, String nombreUsuario, String claveUsuario, String idCanal) throws Exception {
        Resultado c = new Resultado();
        if (idEmpresa == null) {
            return c;
        }

        Map<String, Object> p1 = new HashMap<>();
        p1.put("p_IdEmpresa", idEmpresa);
        p1.put("p_NombreUsuario", nombreUsuario.toUpperCase());
        List<UsuarioProceso> u = SqlNombrado.consultar(conn, QUERY_USUARIOS, p1, UsuarioProceso.class);

        // the user exists?
        if (u.isEmpty() || (u.size() == 1 && u.get(0).IdUsuario == null)) {
            return c;
        }
        UsuarioProceso usuario = u.get(0);

        // still blocked for a while? (a null date is a NullPointerException, as the legacy: the session is refused and nothing is committed)
        if (usuario.FechaBloqueoTemporal.after(usuario.CurrentTimestamp)) {
            return c;
        }
        if (usuario.IdEstado.equals("BLO")) {
            return c;
        }
        // authorized?
        if (usuario.EsAutorizado.equals("P")) {
            return c;
        }
        // valid (not expired)?
        if (!fechaTexto(usuario.FechaVigencia).equals("0001-01-01")
                && usuario.FechaVigencia.before(java.sql.Date.valueOf(java.time.LocalDate.parse(usuario.Param_FechaSistema)))) {
            return c;
        }

        // right password? The attempt is counted and, at the maximum, the user is blocked for a while.
        String claveHash = sha256Mayuscula(claveUsuario);
        if (!claveHash.equals(usuario.Clave)) {
            Map<String, Object> intento = new HashMap<>();
            intento.put("IntentosConexion", usuario.IntentosConexion + 1);
            intento.put("IdUsuario", usuario.IdUsuario);
            SqlNombrado.actualizar(conn, UPDATE_INTENTOS, intento);
            if (usuario.Param_IntentosMaximo != null && usuario.IntentosConexion + 1 >= Integer.parseInt(usuario.Param_IntentosMaximo)) {
                Map<String, Object> bloqueo = new HashMap<>();
                bloqueo.put("IdUsuario", usuario.IdUsuario);
                bloqueo.put("Param_ClaveUsuarioTiempoBloqueo", usuario.Param_ClaveUsuarioTiempoBloqueo);
                SqlNombrado.actualizar(conn, UPDATE_BLOQUEO_TEMPORAL, bloqueo);
            }
            return c;
        }

        // T-03: the entity of the user must be active for the channel
        if (usuario.TipoEntidad.equals("EMP") && ((idCanal.equals(CANAL_CORREDORES) && usuario.UsuarioCorredores.equals("S"))
                || (idCanal.equals(CANAL_NEGOCIOS) && usuario.UsuarioNegocios.equals("S"))
                || (idCanal.equals(CANAL_WEB_SERVICES) && usuario.UsuarioServicios.equals("S")))) {
            List<SocioNegocio> lst = SqlNombrado.consultar(conn, QUERY_EMPLEADO, entidad(usuario), SocioNegocio.class);
            if (lst.isEmpty() || lst.get(0).IdEmpleado == null || !lst.get(0).IdEstado.equals("ACT")) {
                return c;
            }
            c.entidad = "EMP";
        } else if (usuario.TipoEntidad.equals("PER") && usuario.UsuarioClientes.equals("S") && idCanal.equals(CANAL_CLIENTES)) {
            List<SocioNegocio> lst = SqlNombrado.consultar(conn, QUERY_PERSONA, entidad(usuario), SocioNegocio.class);
            if (lst.isEmpty() || lst.get(0).IdPersona == null || !lst.get(0).IdEstado.equals("ACT")) {
                return c;
            }
            c.entidad = "PER";
        } else if (usuario.TipoEntidad.equals("CLI") && ((usuario.UsuarioServicios.equals("S") && idCanal.equals(CANAL_WEB_SERVICES))
                || (usuario.UsuarioClientes.equals("S") && idCanal.equals(CANAL_CLIENTES)))) {
            List<SocioNegocio> lst = SqlNombrado.consultar(conn, QUERY_CLIENTE, entidad(usuario), SocioNegocio.class);
            // RULE-018 (reviewed "wrong" on 2026-10-02): the legacy let a BLOCKED client in (ACT or BLO); the reviewer's decision is that only ACT may,
            // as for employees and persons
            if (lst.isEmpty() || lst.get(0).IdCliente == null || !lst.get(0).IdEstado.equals("ACT")) {
                return c;
            }
            c.entidad = "CLI";
        } else {
            // the user is not configured for the channel
            return c;
        }

        long fechaActual = relojUtc(conn).getTime();
        String idleMaxTime = idleMaxTime(conn, idCanal);

        String token = "";
        if (idCanal.equals(CANAL_WEB_SERVICES)) {
            // T-05: the web-service channel reuses its valid token while it is not idle
            Map<String, Object> hash1 = new HashMap<>();
            hash1.put("IdUsuario", usuario.IdUsuario);
            hash1.put("IdCanal", idCanal);
            List<UsuarioProceso> lst = SqlNombrado.consultar(conn, QUERY_BUSCA_TOKEN, hash1, UsuarioProceso.class);
            if (lst.size() == 1 && lst.get(0).IdTokenSesion != null && !lst.get(0).IdTokenSesion.isEmpty()) {
                Map<String, Object> params = new HashMap<>();
                params.put("p_Token", lst.get(0).IdTokenSesion);
                List<Ses> ses = SqlNombrado.consultar(conn, QUERY_SESION, params, Ses.class);
                if (ses.size() > 0 && (fechaActual - ses.get(0).FechaUltimoUso.getTime()) > Long.parseLong(idleMaxTime)) {
                    SesionUpd upd = new SesionUpd();
                    upd.p_FechaUso = new Timestamp(fechaActual);
                    upd.p_Token = lst.get(0).IdTokenSesion;
                    upd.p_TokenValido = "N";
                    SqlNombrado.lote(conn, UPDATE_SESION, List.of(upd), SesionUpd.class);
                    conn.commit();
                } else {
                    token = lst.get(0).IdTokenSesion;
                    c.token = token;
                }
            }
        }

        if (token.equals("") || !idCanal.equals(CANAL_WEB_SERVICES)) {
            token = insertarSesion(conn, idEmpresa, nombreUsuario, usuario.IdUsuario, usuario.CurrentTimestamp, idCanal, "");
            c.token = token;
            auditoria.registrarSesion(conn, usuario.IdUsuario, usuario.Param_FechaSistema, idCanal, idEmpresa, INICIO_SESION_EXITO);
        }

        if (usuario.IntentosConexion > 0) {
            Map<String, Object> reset = new HashMap<>();
            reset.put("IntentosConexion", 0);
            reset.put("IdUsuario", usuario.IdUsuario);
            SqlNombrado.actualizar(conn, UPDATE_INTENTOS, reset);
        }
        return c;
    }

    /**
     * Legacy {@code Sesion.insertarSesion}: the token is {@code Base64(idEmpresa:usuario:timestamp + UUID)} (T-07); the open sessions of the user and
     * channel are closed first when the single-session switch applies (T-04) and the new one is written with its SSO entity.
     */
    public static String insertarSesion(Connection conn, Long idEmpresa, String nombreUsuario, Long idUsuario, Timestamp currentTimestamp,
            String idCanal, String idEntidadSSO) throws SQLException {
        String datatoken = idEmpresa + ":" + nombreUsuario + ":" + currentTimestamp + UUID.randomUUID().toString();
        String token = new String(Base64.getEncoder().encode(datatoken.getBytes()));

        SesionNueva p = new SesionNueva();
        p.IdUsuario = idUsuario;
        p.IdTokenSesion = token;
        p.IdCanal = idCanal;
        p.IdEntidadSSO = idEntidadSSO;
        List<SesionNueva> ins = new ArrayList<>();
        ins.add(p);
        SqlNombrado.lote(conn, CLOSE_SESION, ins, SesionNueva.class);
        SqlNombrado.lote(conn, INSERT_SESION, ins, SesionNueva.class);
        return token;
    }

    /** Idle time of a channel in milliseconds (legacy {@code SesionUtils.GetParametro}; no cache: the value is read each time). */
    static String idleMaxTime(Connection conn, String canal) throws SQLException {
        String codigo = switch (canal) {
            case "WS" -> "PLA.IdleMaxTimeWS";
            case "CLI" -> "PLA.IdleMaxTimeCLI";
            case "AGE" -> "PLA.IdleMaxTimeAGE";
            default -> null;
        };
        if (codigo == null) {
            return "1800000";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("p_Parametro", codigo);
        return SqlNombrado.consultar(conn, QUERY_PARAMETRO, params, Parametro.class).get(0).Valor;
    }

    /** Database UTC clock. */
    static Timestamp relojUtc(Connection conn) throws SQLException {
        List<RelojUtc> rows = SqlNombrado.consultar(conn, QUERY_RELOJ_UTC, new HashMap<>(), RelojUtc.class);
        if (rows.isEmpty() || rows.get(0).UTC == null) {
            throw new IllegalStateException("No se puede obtener cache UTC");
        }
        return rows.get(0).UTC;
    }

    private static Map<String, Object> entidad(UsuarioProceso u) {
        Map<String, Object> m = new HashMap<>();
        m.put("IdEntidad", u.IdEntidad);
        return m;
    }

    private static String fechaTexto(java.sql.Date fecha) {
        return fecha == null ? "" : fecha.toString();
    }
}
