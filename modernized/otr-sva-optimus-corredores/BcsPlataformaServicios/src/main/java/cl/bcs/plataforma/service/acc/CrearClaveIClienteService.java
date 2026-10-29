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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrearClaveIClienteService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearClaveIClienteService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private final NamedParameterJdbcTemplate jdbc;
    private final CrearUsuarioService crearUsuario;
    private final CrearAutorizadoOrdenService crearAutorizadoOrden;
    private final String SQL_NUEVO_CLIENTE;
    private final String SQL_CLIENTE_PORTAFOLIO;
    private final String SQL_INSERT_CLAVE;

    public CrearClaveIClienteService(NamedParameterJdbcTemplate jdbc,
                                     CrearUsuarioService crearUsuario,
                                     CrearAutorizadoOrdenService crearAutorizadoOrden) {
        this.jdbc = jdbc;
        this.crearUsuario = crearUsuario;
        this.crearAutorizadoOrden = crearAutorizadoOrden;
        this.SQL_NUEVO_CLIENTE    = loadSql("cl/bcs/plataforma/acc/sql/ConsultaNuevoCliente.sql");
        this.SQL_CLIENTE_PORTAFOLIO = loadSql("cl/bcs/plataforma/acc/sql/CrearClaveICliente_SelectConsultaClientePortafolio.sql");
        this.SQL_INSERT_CLAVE     = loadSql("cl/bcs/plataforma/acc/sql/InsertarClaveProvisoria.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CrearClaveICliente"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CrearClaveICliente");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();
        try {
            List<NuevoCliente> clientes = jdbc.query(SQL_NUEVO_CLIENTE,
                new MapSqlParameterSource("p_IdCliente", in.p_IdCliente),
                BeanPropertyRowMapper.newInstance(NuevoCliente.class));

            if (clientes.isEmpty() || clientes.get(0).IdDireccionElectronicaPrincipal == null) {
                addError(out, "ERROR_EMAIL", "No tiene EMAIL asociado");
                out.P_OUT_ERRORESCONTADOR = 1L;
                return toJson(out);
            }

            NuevoCliente cli = clientes.get(0);
            String claveProvisoria = generarClave(8);
            String claveHash = sha256(claveProvisoria);

            // Crear usuario con clave hasheada
            JsonObject usuarioParams = new JsonObject();
            usuarioParams.addProperty("p_IdEmpresa", in.p_IdEmpresa);
            usuarioParams.addProperty("p_IdUsuario", in.p_IdUsuario);
            usuarioParams.addProperty("p_IdModulo", in.p_IdModulo);
            usuarioParams.addProperty("p_UsaReglasNegocio", "S");
            usuarioParams.addProperty("p_CodigoUsuario", in.p_CodigoUsuario);
            usuarioParams.addProperty("p_Clave", claveHash);
            usuarioParams.addProperty("p_IdEstado", "ACT");
            usuarioParams.addProperty("p_TipoEntidad", "CLI");
            usuarioParams.addProperty("p_IdEntidad", in.p_IdCliente);
            usuarioParams.addProperty("p_Canal", "AGE");
            usuarioParams.addProperty("p_HeredarRolesUsuario", -1L);

            JsonObject crearUsuarioOut = crearUsuario.execute(usuarioParams);
            long errUsuario = crearUsuarioOut.has("P_OUT_ERRORESCONTADOR") ?
                crearUsuarioOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;

            out.P_OUT_ERRORESCONTADOR = errUsuario;
            if (crearUsuarioOut.has("P_OUT_MENSAJES")) {
                crearUsuarioOut.get("P_OUT_MENSAJES").getAsJsonArray().forEach(m ->
                    out.P_OUT_MENSAJES.add(GSON.fromJson(m, Mensaje.class)));
            }

            if (errUsuario == 0L) {
                jdbc.update(SQL_INSERT_CLAVE, new MapSqlParameterSource()
                    .addValue("p_ClaveProvisoria", claveProvisoria)
                    .addValue("p_IdUsuario", in.p_CodigoUsuario));

                addOk(out, "OK", "Usuario creado correctamente.");
                addOk(out, "OK", "Clave provisoria: " + claveProvisoria);

                // Crear autorizado para el portafolio asociado
                try {
                    List<ClientePortafolio> port = jdbc.query(SQL_CLIENTE_PORTAFOLIO,
                        new MapSqlParameterSource("DocumentoIdentidad", cli.DocumentoIdentidad),
                        BeanPropertyRowMapper.newInstance(ClientePortafolio.class));
                    if (!port.isEmpty()) {
                        JsonObject autParams = new JsonObject();
                        autParams.addProperty("p_IdEmpresa", in.p_IdEmpresa);
                        autParams.addProperty("p_IdUsuario", in.p_IdUsuario);
                        autParams.addProperty("p_IdModulo", in.p_IdModulo);
                        autParams.addProperty("p_IdEstado", "ACT");
                        autParams.addProperty("p_UsaReglasNegocio", "S");
                        autParams.addProperty("p_IdPersona", port.get(0).IdPersona);
                        autParams.addProperty("p_IdPortafolio", port.get(0).IdPortafolio);
                        autParams.addProperty("p_IdTipoAutorizado", in.p_IdTipoAutorizado);
                        autParams.addProperty("p_FirmaOptimusCliente", in.p_FirmaOptimusCliente);
                        JsonObject autOut = crearAutorizadoOrden.execute(autParams);
                        if (autOut.has("P_OUT_MENSAJES")) {
                            autOut.get("P_OUT_MENSAJES").getAsJsonArray().forEach(m ->
                                out.P_OUT_MENSAJES.add(GSON.fromJson(m, Mensaje.class)));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error al crear autorizado: {}", e.getMessage());
                }
                out.P_OUT_RESULTADOPROCESO = "OK";
            }
        } catch (Exception e) {
            log.error("Error en CrearClaveICliente: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addError(out, "ERROR_INTERNO", e.getMessage());
        }
        return toJson(out);
    }

    private String generarClave(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void addError(Salida out, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = "ERROR"; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private void addOk(Salida out, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = "OK"; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Salida out) {
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_CodigoUsuario; public Long p_IdCliente;
        public String p_IdTipoAutorizado = "ICLIENTE-OPERAR"; public String p_FirmaOptimusCliente = "S";
    }

    public static class Salida {
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_RESULTADOPROCESO = "";
    }

    public static class Mensaje {
        public String tipo; public String codigo; public String descripcion; public Long id = 0L;
    }

    public static class NuevoCliente {
        public Long IdDireccionElectronicaPrincipal; public Long IDPERSONA;
        public String CodigoUsuario; public String Nombres; public String CORREDORA;
        public String DocumentoIdentidad; public Long IdUsuario;
    }

    public static class ClientePortafolio {
        public Long IdPortafolio; public Long IdPersona;
    }
}
