package cl.bcs.plataforma.login.clave;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.jdbc.SqlNombrado;
import cl.bcs.utils.clave.EstadoCriterio;
import cl.bcs.utils.clave.PoliticaClave;
import cl.bcs.utils.clave.ResultadoValidacion;
import cl.bcs.utils.clave.ValidadorClave;

/**
 * The password services of the login, ported from the legacy servlets of BcsPlataformaWeb over the legacy SQL, on the connection of the request:
 * {@code ActualizarClave} (T-12), {@code ValidaClaveProvisoria} (T-24), {@code SolicitudRecuperacionClave} (T-25), {@code RecuperacionClave} (T-26)
 * and {@code ConfirmarCambioClave} (T-27). Each method answers a status and a body; the caller owns the transaction (T-13): a normal return
 * is committed, an exception rolls back.
 *
 * <p>Stored password (T-12): {@code encryptPassword} = SHA-256 of the SHA-256 hex of the password, upper case: the value the login compares with
 * ({@link cl.bcs.plataforma.login.sesion.CrearSesionServicio}). A new password goes through the policy of {@link ValidadorClave} (RULE-031..034; the
 * digit criterion of RULE-032 is the approved separate rule of BcsUtils; the policy values are those of company 1, as the legacy, RULE-033).
 * Reviewed rule RULE-040 ("wrong"): the provisional password locks at exactly {@code PLA.ClaveUsuarioIntentos} failed attempts, not one more.
 *
 * <p>These services need no session: the user proves who they are with the provisional password or the token of the recovery. The legacy answers the
 * dictionary text of {@code PLA.Diccionario} (language CL); a code the dictionary lacks is answered with an empty body, as the legacy.
 */
@Service
public class GestionClaveServicio {

    private static final Logger log = LoggerFactory.getLogger(GestionClaveServicio.class);

    /** What the endpoint answers. */
    public record Respuesta(int estado, String cuerpo) {
    }

    /** Row of the dictionary query. */
    public static class Dic {
        public String CodDiccionario;
        public String Descripcion;
    }

    /** Row of the provisional password query. */
    public static class ClaveProvisoria {
        public Long IdUsuario;
        public Long IdEmpresa;
        public String ClaveProvisoria;
        public String FechaClaveProvisoria;
        public Integer IntentosConexionClaveProvisoria;
        public String CantidadIntentosMaximo;   // PLA.Configuracion.Valor is text; read as the legacy getInt: empty means 0
    }

    /** Row of the recovery query. */
    public static class DatosRecuperacion {
        public Long IdPersona;
        public Long IdDireccionElectronicaPrincipal;
        public String NombreCompleto;
        public String corredora;
        public Long IdEmpresa;
        public Long IdUsuario;
        public String TipoEntidad;
    }

    public static class IdEmpresaUsuario {
        public Long IdEmpresa;
    }

    public static class ParametroPolitica {
        public String CodigoParametro;
        public String Valor;
    }

    public static class Cuenta {
        public Long Cantidad;
    }

    public static class Valor {
        public String Valor;
        public String RutaPublica;
    }

    static final String SP_FECHA_HORA = "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema";
    static final String SP_RECUPERAR = "BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario";
    static final String SP_CONFIRMAR = "BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario";
    static final String SP_AGREGAR_EVENTO = "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento";

    static final String SQL_DICCIONARIO = "SELECT \"CodDiccionario\", \"Descripcion\" FROM \"BSCL.Plataforma.BD::PLA.Diccionario\" WHERE \"CodLenguaje\" = 'CL' AND \"CodDiccionario\" IN (:Codigos)";
    static final String SQL_EMPRESA_USUARIO = "SELECT \"IdEmpresa\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"CodigoUsuario\" = :CodigoUsuario";
    static final String SQL_PARAMETROS_POLITICA = "SELECT \"CodigoParametro\", \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = :p_IdEmpresa AND \"CodigoParametro\" IN "
            + "('ACC.numLongitud','ACC.numMayusculas','ACC.numMinuscula','ACC.numCaracteres','ACC.numDigitos')";
    static final String SQL_PALABRA_PROHIBIDA = "SELECT COUNT(1) AS \"Cantidad\" FROM \"BSCL.Plataforma.BD::ACC.PasswordProhibidas\" WHERE \"Palabra\" = :p_ClaveNueva";
    static final String SQL_ACTUALIZAR_CLAVE = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"Clave\" = :Clave, \"FechaIngreso\" = :FechaIngreso, \"IdEstado\" = 'ACT', "
            + "\"DebeCambiarClave\" = 'N', \"UltimoCambioClave\" = :UltimoCambioClave WHERE \"CodigoUsuario\" = :CodigoUsuario";
    static final String SQL_CLAVE_PROVISORIA = "SELECT TOP 1 USU.\"IdUsuario\",USU.\"IdEmpresa\",USU.\"ClaveProvisoria\", USU.\"FechaClaveProvisoria\", USU.\"IntentosConexionClaveProvisoria\", "
            + "CON.\"Valor\" AS \"CantidadIntentosMaximo\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" AS USU "
            + "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" AS CON ON CON.\"CodigoParametro\" = 'PLA.ClaveUsuarioIntentos' AND CON.\"IdEmpresa\" = USU.\"IdEmpresa\" "
            + "WHERE USU.\"CodigoUsuario\" = :CodigoUsuario";
    static final String SQL_LIMPIAR_PROVISORIA = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = '', \"FechaClaveProvisoria\" = '0001-01-01 00:00:00', "
            + "\"IntentosConexionClaveProvisoria\" = 0 WHERE \"CodigoUsuario\" = :CodigoUsuario";
    static final String SQL_INTENTO_PROVISORIA = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IntentosConexionClaveProvisoria\" = :Intentos WHERE \"CodigoUsuario\" = :CodigoUsuario";
    static final String SQL_DATOS_RECUPERACION = "SELECT TOP 1 CLI.\"IdPersona\" AS \"IdPersona\", PER.\"IdDireccionElectronicaPrincipal\", PER.\"NombreCompleto\", DTOS.\"RazonSocial\" AS \"corredora\", "
            + "CLI.\"IdEmpresa\", USU.\"IdUsuario\", USU.\"TipoEntidad\" from \"BSCL.Plataforma.BD::ACC.Usuario\" USU "
            + "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" CLI ON (USU.\"TipoEntidad\" = 'CLI' AND USU.\"IdEntidad\" = CLI.\"IdCliente\") "
            + "OR (USU.\"TipoEntidad\" = 'EMP' AND USU.\"IdEntidad\" = CLI.\"IdEmpleado\") OR (USU.\"TipoEntidad\" = 'PER' AND USU.\"IdEntidad\" = CLI.\"IdPersona\") "
            + "LEFT JOIN \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" PER ON PER.\"IdPersona\" = CLI.\"IdPersona\" "
            + "LEFT JOIN \"BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales\" DTOS ON DTOS.\"IdEmpresa\" = CLI.\"IdEmpresa\" WHERE USU.\"CodigoUsuario\" = :CodigoUsuario";
    static final String SQL_LARGO_TOKEN = "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.LargoMaximoToken' AND \"IdEmpresa\" = :IdEmpresa";
    static final String SQL_LOGO = "SELECT \"RutaPublica\" FROM \"BSCL.Plataforma.BD::PLA.Imagenes\" WHERE \"NombreImagen\" = 'LOGO_CORREDOR' AND \"IdEmpresa\" = :IdEmpresa";
    static final String SQL_GUARDAR_PROVISORIA = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = :ClaveProvisoria, \"FechaClaveProvisoria\" = :FechaClaveProvisoria, "
            + "\"IntentosConexionClaveProvisoria\" = 0 WHERE \"CodigoUsuario\" = :CodigoUsuario";

    private static final String ALFABETO_TOKEN = "BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz1234567890!#$%&=?@";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ProcedimientoAlmacenado procedimientos;

    public GestionClaveServicio(ProcedimientoAlmacenado procedimientos) {
        this.procedimientos = procedimientos;
    }

    // ------------------------------------------------------------------------------------------------ ActualizarClave (T-12)

    /**
     * Changes the password of a user. Needs a session (owner decision 2026-10-26; the legacy did not): the session filter asks for the token, and here the
     * session must be the one OF THE SAME USER whose password is changed, so a user with a session cannot change someone else's password. 403 otherwise.
     */
    public Respuesta actualizarClave(Connection con, String body, String token) throws Exception {
        Map<String, String> diccionario = diccionario(con, "CONTRASENA_ACTUALIZADA", "CONTRASENA_Y_CONFIRMACION_NO_SON_IGUALES", "CONTRASENA_INVALIDA");
        if (body == null || body.isEmpty()) {
            return respuesta(diccionario, "NOK", "CONTRASENA_INVALIDA");
        }
        JsonObject entrada = JsonParser.parseString(body).getAsJsonObject();
        String codigoUsuario = texto(entrada, "p_CodigoUsuario").toUpperCase();
        if (!sesionEsDelUsuario(con, token, codigoUsuario)) {
            return new Respuesta(403, "");
        }
        String clave1 = texto(entrada, "Clave1");
        String clave2 = texto(entrada, "Clave2");
        if (clave1.isEmpty() || clave2.isEmpty()) {
            return respuesta(diccionario, "NOK", "CONTRASENA_INVALIDA");
        }
        if (!clave1.equals(clave2)) {
            return respuesta(diccionario, "NOK", "CONTRASENA_Y_CONFIRMACION_NO_SON_IGUALES");
        }

        long idEmpresa = 0L;
        Map<String, Object> pu = new HashMap<>();
        pu.put("CodigoUsuario", codigoUsuario);
        for (IdEmpresaUsuario e : SqlNombrado.consultar(con, SQL_EMPRESA_USUARIO, pu, IdEmpresaUsuario.class)) {
            idEmpresa = e.IdEmpresa;
        }
        String fechaSistema = fechaSistema(con, idEmpresa);

        ResultadoValidacion validacion = null;
        try {
            validacion = validarNuevaClave(con, clave1);
        } catch (Exception e) {
            log.error("Error al validar la politica de claves: {}", e.getClass().getSimpleName());
        }
        if (validacion != null && validacion.claveValida()) {
            Map<String, Object> p = new HashMap<>();
            p.put("Clave", encryptPassword(clave1));
            p.put("FechaIngreso", fechaSistema);
            p.put("UltimoCambioClave", fechaSistema);
            p.put("CodigoUsuario", codigoUsuario);
            SqlNombrado.actualizar(con, SQL_ACTUALIZAR_CLAVE, p);
            return respuesta(diccionario, "OK", "CONTRASENA_ACTUALIZADA");
        }
        // the clave does not meet the policy (or it could not be checked): Estado NOK and the list of criteria (all of them, as the legacy)
        JsonObject salida = new JsonObject();
        salida.addProperty("Estado", "NOK");
        JsonArray status = new JsonArray();
        if (validacion != null) {
            status.add("La contraseña debe contener:");
            for (EstadoCriterio c : validacion.estadoCriterios()) {
                status.add("- " + c.criterio());
            }
        }
        salida.add("status", status);
        return new Respuesta(200, salida.toString());
    }

    /** Row of the session query. */
    public static class UsuarioDeSesion {
        public String Codigo;
    }

    static final String SQL_USUARIO_DE_SESION = "SELECT UPPER(U.\"CodigoUsuario\") AS \"Codigo\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" S "
            + "JOIN \"BSCL.Plataforma.BD::ACC.Usuario\" U ON U.\"IdUsuario\" = S.\"IdUsuario\" WHERE S.\"IdTokenSesion\" = :Token AND S.\"TokenValido\" = 'S'";

    private boolean sesionEsDelUsuario(Connection con, String token, String codigoUsuario) throws Exception {
        if (token == null || token.isEmpty() || codigoUsuario.isEmpty()) {
            return false;
        }
        Map<String, Object> p = new HashMap<>();
        p.put("Token", token);
        for (UsuarioDeSesion u : SqlNombrado.consultar(con, SQL_USUARIO_DE_SESION, p, UsuarioDeSesion.class)) {
            return codigoUsuario.equals(u.Codigo);
        }
        return false;
    }

    /** The new-password policy (legacy {@code ValidarNuevaClave.logica}): values of company 1 (RULE-033), prohibited words by exact match. */
    ResultadoValidacion validarNuevaClave(Connection con, String clave) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("p_IdEmpresa", 1L);
        Map<String, String> valores = new HashMap<>();
        for (ParametroPolitica p : SqlNombrado.consultar(con, SQL_PARAMETROS_POLITICA, params, ParametroPolitica.class)) {
            valores.put(p.CodigoParametro, p.Valor);
        }
        PoliticaClave politica = PoliticaClave.desdeConfiguracion(valores);
        Predicate<String> prohibida = c -> {
            try {
                Map<String, Object> q = new HashMap<>();
                q.put("p_ClaveNueva", c);
                return SqlNombrado.consultar(con, SQL_PALABRA_PROHIBIDA, q, Cuenta.class).get(0).Cantidad > 0;
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }
        };
        return ValidadorClave.validar(clave, politica, prohibida);
    }

    // ------------------------------------------------------------------------------------------------ ValidaClaveProvisoria (T-24)

    public Respuesta validaClaveProvisoria(Connection con, String body) throws Exception {
        Map<String, String> diccionario = diccionario(con, "CLAVE_PROVISORIA_VALIDA", "CLAVE_PROVISORIA_VENCIDA", "CLAVE_PROVISORIA_INVALIDA", "SUPERA_INTENTOS_MAXIMOS");
        if (body == null || body.isEmpty()) {
            return respuesta(diccionario, "NOK", "CLAVE_PROVISORIA_INVALIDA");
        }
        JsonObject entrada = JsonParser.parseString(body).getAsJsonObject();
        String codigoUsuario = texto(entrada, "p_CodigoUsuario").toUpperCase();
        String claveIngresada = texto(entrada, "p_ClaveProvisoria");

        Map<String, Object> p = new HashMap<>();
        p.put("CodigoUsuario", codigoUsuario);
        List<ClaveProvisoria> filas = SqlNombrado.consultar(con, SQL_CLAVE_PROVISORIA, p, ClaveProvisoria.class);
        if (filas.isEmpty() || filas.get(0).IdUsuario == null || filas.get(0).IdEmpresa == null) {
            return new Respuesta(200, "");   // the legacy answers nothing for an unknown user
        }
        ClaveProvisoria u = filas.get(0);
        String fechaSistema = fechaSistema(con, u.IdEmpresa);
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        String fechaClaveProvisoria = dt.format(dt.parse(u.FechaClaveProvisoria));
        int cantidadMaxima = u.CantidadIntentosMaximo == null ? 0 : Integer.parseInt(u.CantidadIntentosMaximo.trim());
        int intentos = u.IntentosConexionClaveProvisoria == null ? 0 : u.IntentosConexionClaveProvisoria;

        if (encryptPassword(claveIngresada).equals(u.ClaveProvisoria)) {
            if (fechaClaveProvisoria.equalsIgnoreCase(fechaSistema)) {
                // RULE-040 (reviewed "wrong"): it locks at exactly the configured number of attempts (the legacy allowed one more)
                if (intentos < cantidadMaxima) {
                    limpiarProvisoria(con, codigoUsuario);
                    return respuesta(diccionario, "OK", "CLAVE_PROVISORIA_VALIDA");
                }
                limpiarProvisoria(con, codigoUsuario);
                return respuesta(diccionario, "NOK", "SUPERA_INTENTOS_MAXIMOS");
            }
            limpiarProvisoria(con, codigoUsuario);
            return respuesta(diccionario, "NOK", "CLAVE_PROVISORIA_VENCIDA");
        }
        // a different password counts one attempt
        Map<String, Object> q = new HashMap<>();
        q.put("Intentos", intentos + 1);
        q.put("CodigoUsuario", codigoUsuario);
        SqlNombrado.actualizar(con, SQL_INTENTO_PROVISORIA, q);
        return respuesta(diccionario, "NOK", "CLAVE_PROVISORIA_INVALIDA");
    }

    private void limpiarProvisoria(Connection con, String codigoUsuario) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("CodigoUsuario", codigoUsuario);
        SqlNombrado.actualizar(con, SQL_LIMPIAR_PROVISORIA, p);
    }

    // ------------------------------------------------------------------------------------------------ SolicitudRecuperacionClave (T-25)

    /** Asks the stored procedure to start the recovery; answers the masked address the token was sent to. */
    public Respuesta solicitudRecuperacionClave(Connection con, String body) {
        try {
            JsonObject entrada = JsonParser.parseString(body).getAsJsonObject();
            String idEmpresa = texto(entrada, "p_IdEmpresa");
            String nombreUsuario = texto(entrada, "p_Username");
            JsonObject in = new JsonObject();
            in.addProperty("p_IdEmpresa", Long.parseLong(idEmpresa));
            in.addProperty("p_Username", nombreUsuario.toUpperCase());
            in.addProperty("p_Token", generateToken(5));
            JsonObject out = procedimientos.llamar(con, SP_RECUPERAR, in);
            if (esCero(out.get("P_OUT_ERRORESCONTADOR"))) {
                JsonObject ok = new JsonObject();
                ok.addProperty("statusCode", "0");
                ok.addProperty("statusMessage", "OK");
                ok.addProperty("recoveryTo", obfuscateEmail(out.get("P_OUT_MAIL").getAsString()));
                return new Respuesta(200, ok.toString());
            }
            return errorRecuperacion("ERROR_RECUPERAR_CLAVE", contador(out));
        } catch (Exception e) {
            log.warn("Error al solicitar la recuperacion de clave: {}", causa(e));
            return errorRecuperacion("ERROR_RECUPERAR_CLAVE", 3);
        }
    }

    // ------------------------------------------------------------------------------------------------ ConfirmarCambioClave (T-27)

    /** Confirms the change of password with the token of the recovery; a procedure error rolls the request back. */
    public Respuesta confirmarCambioClave(Connection con, String body) {
        try {
            JsonObject entrada = JsonParser.parseString(body).getAsJsonObject();
            JsonObject in = new JsonObject();
            in.addProperty("p_IdEmpresa", Long.parseLong(texto(entrada, "p_IdEmpresa")));
            in.addProperty("p_Username", texto(entrada, "p_Username").toUpperCase());
            in.addProperty("p_Password", texto(entrada, "p_Password"));
            in.addProperty("p_Token", texto(entrada, "p_Token"));
            JsonObject out = procedimientos.llamar(con, SP_CONFIRMAR, in);
            if (esCero(out.get("P_OUT_ERRORESCONTADOR"))) {
                JsonObject ok = new JsonObject();
                ok.addProperty("statusCode", "0");
                ok.addProperty("statusMessage", "OK");
                return new Respuesta(200, ok.toString());
            }
            con.rollback();
            return errorRecuperacion("ERROR_CONFIRMAR_CLAVE", contador(out));
        } catch (Exception e) {
            log.warn("Error al confirmar el cambio de clave: {}", causa(e));
            return errorRecuperacion("ERROR_CONFIRMAR_CLAVE", 3);
        }
    }

    /** The legacy {@code ConfirmarCambioClave.doGet}: always the error with code 3. */
    public Respuesta confirmarCambioClaveGet() {
        return errorRecuperacion("ERROR_CONFIRMAR_CLAVE", 3);
    }

    // ------------------------------------------------------------------------------------------------ RecuperacionClave (T-26)

    /**
     * Sends the user a provisional password: generates it, queues the notification {@code SOLICITUD_CAMBIO_CLAVE} through the events procedure and
     * stores the provisional password (double SHA-256) with today's date. 200 {@code {"Estado":"OK","status":"CLAVE ENVIADA"}}; any problem 401
     * {@code ERROR_RECUPERAR_CLAVE}.
     */
    public Respuesta recuperacionClave(Connection con, String body) {
        try {
            if (body == null || body.isEmpty()) {
                return errorRecuperacionClave();
            }
            JsonObject entrada = JsonParser.parseString(body).getAsJsonObject();
            String codigoUsuario = texto(entrada, "p_CodigoUsuario").toUpperCase();
            String canal = texto(entrada, "p_CanalSistema");

            Map<String, Object> p = new HashMap<>();
            p.put("CodigoUsuario", codigoUsuario);
            List<DatosRecuperacion> filas = SqlNombrado.consultar(con, SQL_DATOS_RECUPERACION, p, DatosRecuperacion.class);
            DatosRecuperacion d = filas.isEmpty() ? new DatosRecuperacion() : filas.get(0);
            if (!filas.isEmpty() && (d.IdPersona == null || d.IdDireccionElectronicaPrincipal == null || d.IdEmpresa == null || d.IdUsuario == null)) {
                return errorRecuperacionClave();   // the legacy read these four with Long.parseLong(getString(..)): a NULL was a NumberFormatException, i.e. the error answer
            }
            String fechaSistema = "";
            if (d.IdUsuario != null || d.IdEmpresa != null) {
                fechaSistema = fechaSistema(con, d.IdEmpresa);
            }
            String tipo = d.TipoEntidad;
            boolean delCanal = tipo != null && (canal.equals("CLIENTE") && (tipo.equals("CLI") || tipo.equals("PER"))) || (canal.equals("OPTIMUS") && tipo.equals("EMP"))
                    || (canal.equals("AGENTE") && tipo.equals("EMP"));
            if (!delCanal) {
                return errorRecuperacionClave();   // the user does not belong to the channel
            }

            Map<String, Object> pe = new HashMap<>();
            pe.put("IdEmpresa", d.IdEmpresa);
            int largoToken = 0;
            for (Valor v : SqlNombrado.consultar(con, SQL_LARGO_TOKEN, pe, Valor.class)) {
                largoToken = Integer.parseInt(v.Valor);
            }
            String claveProvisoria = generateToken(largoToken);
            String claveProvisoriaNueva = encryptPassword(claveProvisoria);
            String logo = "";
            for (Valor v : SqlNombrado.consultar(con, SQL_LOGO, pe, Valor.class)) {
                logo = v.RutaPublica;
            }

            notificar(con, d, fechaSistema, claveProvisoria, logo);

            Map<String, Object> g = new HashMap<>();
            g.put("ClaveProvisoria", claveProvisoriaNueva);
            g.put("FechaClaveProvisoria", fechaSistema);
            g.put("CodigoUsuario", codigoUsuario);
            SqlNombrado.actualizar(con, SQL_GUARDAR_PROVISORIA, g);

            JsonObject ok = new JsonObject();
            ok.addProperty("Estado", "OK");
            ok.addProperty("status", "CLAVE ENVIADA");
            return new Respuesta(200, ok.toString());
        } catch (Exception e) {
            log.warn("Error en la recuperacion de clave: {}", causa(e));
            return errorRecuperacionClave();
        }
    }

    /** Legacy {@code AgregarNotificacion.logica}: one event SOLICITUD_CAMBIO_CLAVE to the main e-mail address of the person, with the values of the template. */
    private void notificar(Connection con, DatosRecuperacion d, String fechaSistema, String claveProvisoria, String logo) throws Exception {
        long idSecuencia = 0L;
        JsonObject evento = new JsonObject();
        evento.addProperty("CodigoTipoEvento", "SOLICITUD_CAMBIO_CLAVE");
        evento.addProperty("IdSecuencia", idSecuencia);
        evento.addProperty("FechaCreacionSistema", fechaSistema);
        evento.addProperty("IdPersonaRemitente", 0L);
        evento.addProperty("IdPersonaDestinatario", d.IdPersona);
        evento.addProperty("IdPortafolio", 0L);
        JsonObject direccion = new JsonObject();
        direccion.addProperty("IdSecuencia", idSecuencia);
        direccion.addProperty("TipoDestinatario", "TO");
        direccion.addProperty("IdDireccion", d.IdDireccionElectronicaPrincipal);
        JsonArray valores = new JsonArray();
        valores.add(valor(idSecuencia, "Nombre", d.NombreCompleto));
        valores.add(valor(idSecuencia, "Corredora", d.corredora));
        valores.add(valor(idSecuencia, "ClaveProvisoria", claveProvisoria));
        valores.add(valor(idSecuencia, "LogoCorredora", logo));
        JsonObject in = new JsonObject();
        in.addProperty("P_IDEMPRESA", d.IdEmpresa);
        in.addProperty("P_IDUSUARIO", d.IdUsuario);
        in.addProperty("P_IDMODULO", "PLA");
        in.add("P_EVENTOS", array(evento));
        in.add("P_VALORES", valores);
        in.add("P_ARCHIVOSADJUNTOS", new JsonArray());
        in.add("P_IDDIRECCIONES", array(direccion));
        in.addProperty("P_APLICAORIGENEVENTO", "");   // the legacy AgregarNotificacion passes '' (not NULL) in this position
        procedimientos.llamar(con, SP_AGREGAR_EVENTO, in);
    }

    // ------------------------------------------------------------------------------------------------ helpers

    /** What to log of a failure: the class and, for a database error, its message (no data of the request is in it). */
    private static String causa(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof java.sql.SQLException) {
                String m = String.valueOf(t.getMessage());
                return t.getClass().getSimpleName() + ": " + (m.length() > 300 ? m.substring(0, 300) : m);
            }
        }
        return e.getClass().getSimpleName();
    }

    /** Legacy {@code Utils.getFechaSistema}: the date part of the procedure {@code ObtenerFechaHoraSistema}. */
    String fechaSistema(Connection con, long idEmpresa) throws Exception {
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", idEmpresa);
        JsonObject out = procedimientos.llamar(con, SP_FECHA_HORA, in);
        return out.get("P_OUT_FECHAHORASISTEMA").getAsString().split(" ")[0];
    }

    private Map<String, String> diccionario(Connection con, String... codigos) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("Codigos", List.of((Object[]) codigos));
        Map<String, String> m = new HashMap<>();
        for (Dic d : SqlNombrado.consultar(con, SQL_DICCIONARIO, p, Dic.class)) {
            m.put(d.CodDiccionario, d.Descripcion);
        }
        return m;
    }

    private static Respuesta respuesta(Map<String, String> diccionario, String estado, String codigo) {
        String texto = diccionario.get(codigo);
        if (texto == null) {
            return new Respuesta(200, "");
        }
        JsonObject salida = new JsonObject();
        salida.addProperty("Estado", estado);
        salida.addProperty("status", texto);
        return new Respuesta(200, salida.toString());
    }

    private static Respuesta errorRecuperacion(String mensaje, int codigo) {
        JsonObject out = new JsonObject();
        out.addProperty("statusCode", codigo);
        out.addProperty("statusMessage", mensaje);
        return new Respuesta(200, out.toString());
    }

    private static Respuesta errorRecuperacionClave() {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 1);
        JsonObject err = new JsonObject();
        err.addProperty("CODIGO", "ERROR_RECUPERAR_CLAVE");
        err.addProperty("Descripcion", "Error al Recuperar Clave.");
        JsonArray arr = new JsonArray();
        arr.add(err);
        out.add("P_OUT_ERRORESDETALLE", arr);
        return new Respuesta(401, out.toString());
    }

    private static boolean esCero(JsonElement e) {
        return e != null && !e.isJsonNull() && e.getAsString().equals("0");
    }

    /** The error count of a procedure answer; 3 when it cannot be read (the legacy code of "something went wrong"). */
    private static int contador(JsonObject out) {
        try {
            return Integer.parseInt(out.get("P_OUT_ERRORESCONTADOR").getAsString());
        } catch (RuntimeException e) {
            return 3;
        }
    }

    private static JsonObject valor(long idSecuencia, String llave, String valor) {
        JsonObject o = new JsonObject();
        o.addProperty("IdSecuencia", idSecuencia);
        o.addProperty("Llave", llave);
        o.addProperty("Valor", valor);
        return o;
    }

    private static JsonArray array(JsonObject o) {
        JsonArray a = new JsonArray();
        a.add(o);
        return a;
    }

    private static String texto(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return e != null && !e.isJsonNull() ? e.getAsString() : "";
    }

    /** Legacy {@code Utils.encryptPassword}: SHA-256 of the SHA-256 hex of the password, upper case. */
    public static String encryptPassword(String clara) {
        return sha256Hex(sha256Hex(clara)).toUpperCase();
    }

    private static String sha256Hex(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Legacy {@code Utils.generateToken}: random text of the given length from the legacy alphabet. */
    public static String generateToken(int size) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < size; i++) {
            b.append(ALFABETO_TOKEN.charAt(ALEATORIO.nextInt(ALFABETO_TOKEN.length())));
        }
        return b.toString();
    }

    /** Legacy {@code Utils.obfuscateEmail}: only the first character, the {@code @} and the character after it, the last dot and the one after it stay. */
    public static String obfuscateEmail(String email) {
        if (email != null && email.length() > 1) {
            StringBuilder o = new StringBuilder();
            int first = 0;
            int at = email.lastIndexOf("@");
            int dot = email.lastIndexOf(".");
            for (int i = 0; i < email.length(); i++) {
                if (i != first && i != at && i != at + 1 && i != dot && i != dot + 1) {
                    o.append("*");
                } else {
                    o.append(email.charAt(i));
                }
            }
            email = o.toString();
        }
        return email != null ? email : "";
    }
}
