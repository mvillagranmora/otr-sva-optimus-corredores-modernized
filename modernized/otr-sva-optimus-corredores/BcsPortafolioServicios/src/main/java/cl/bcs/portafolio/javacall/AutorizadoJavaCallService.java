package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;

/**
 * Spring Boot implementation of cl.bcs.portafolio.ModificarAutorizadoOrden.
 * Converts Boolean FirmaOptimusCliente → "S"/"N" string before batch UPDATE.
 */
@Service
public class AutorizadoJavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AutorizadoJavaCallService.class);

    private static final String SQL_UPDATE = loadSql(
            "cl/bcs/portafolio/sql/ModificarAutorizadoOrden_ActualizarAutorizacionOrden.sql");

    public JsonObject modificarAutorizadoOrden(Connection conn, JsonObject in) throws Exception {
        log.info("INICIO::ModificarAutorizadoOrden");
        Gson gson = new Gson();

        var entrada = gson.fromJson(in.toString(), Entrada.class);

        for (Autorizado a : entrada.p_ListaAutorizados) {
            a.FirmaOptimusClienteSN = (a.FirmaOptimusCliente == null || !a.FirmaOptimusCliente)
                    ? "N" : "S";
        }

        MapSqlParameterSource[] sources = entrada.p_ListaAutorizados.stream()
                .map(a -> new MapSqlParameterSource()
                        .addValue("IdAutorizado", a.IdAutorizado)
                        .addValue("IdTipoAutorizado", a.IdTipoAutorizado)
                        .addValue("FirmaOptimusClienteSN", a.FirmaOptimusClienteSN))
                .toArray(MapSqlParameterSource[]::new);

        batch(conn, SQL_UPDATE, sources);

        var salida = new Salida();
        salida.P_OUT_MENSAJES.add(MensajeSalida.ok("CREAR_AUTORIZADO_ORDEN_EXITO"));
        log.info("FIN::ModificarAutorizadoOrden");
        return JsonParser.parseString(gson.toJson(salida)).getAsJsonObject();
    }

    // ─── DTOs ────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public List<Autorizado> p_ListaAutorizados = new ArrayList<>();
    }

    public static class Autorizado {
        public Long    IdAutorizado;
        public Boolean FirmaOptimusCliente;
        public String  IdTipoAutorizado;
        public String  FirmaOptimusClienteSN;
    }

    private static class Salida {
        public Long               P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES       = new ArrayList<>();
    }
}
