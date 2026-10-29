package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;

/**
 * Spring Boot implementation of cl.bcs.portafolio.EliminarSinTransacciones.
 *
 * Replicates the legacy logic:
 *   1. Load client's portfolios via SQL
 *   2. Validate the requested portfolio exists and is not already deleted
 *   3. Check for open transactions (Saldo, Movimientos, Cartera, etc.)
 *   4. UPDATE the portfolio to Borrado='S', IdEstado='BOR'
 *
 * AgregarLog is intentionally skipped — audit logging via HANA AgregarLog
 * J-type class has cross-service dependencies not yet migrated.
 */
@Service
public class EliminarSinTransaccionesService {

    private static final Logger log = LoggerFactory.getLogger(EliminarSinTransaccionesService.class);

    private static final String SQL_SELECT_PORTAFOLIO = loadSql(
            "cl/bcs/portafolio/sql/EliminarSinTransacciones_SelectPortafolio.sql");
    private static final String SQL_SELECT_TRANSACCIONES = loadSql(
            "cl/bcs/portafolio/sql/EliminarSinTransacciones_SelectTransaccionesPortafolio.sql");
    private static final String SQL_UPDATE_PORTAFOLIO = loadSql(
            "cl/bcs/portafolio/sql/EliminarSinTransacciones_ModificarPortafolio.sql");

    public JsonObject eliminarSinTransacciones(Connection conn, JsonObject in) throws Exception {
        log.info("INICIO::EliminarSinTransacciones");
        long inicio = System.currentTimeMillis();

        Gson gson = new Gson();
        Entrada entrada = gson.fromJson(in.toString(), Entrada.class);
        Salida salida = new Salida();

        // 1. Obtener portafolios del cliente
        List<PortafolioRow> portafolios = queryPortafolios(conn, entrada.p_IdCliente);

        // 2. Validar que el portafolio solicitado existe
        PortafolioRow target = portafolios.stream()
                .filter(p -> entrada.p_IdPortafolio.equals(p.IdPortafolio))
                .findFirst()
                .orElse(null);

        if (target == null) {
            salida.P_OUT_MENSAJES.add(MensajeSalida.error("ERROR_ELIMINAR_PORTAFOLIO",
                    "UI_ERROR_PORTAFOLIO_NO_EXISTE"));
            salida.P_OUT_ERRORESCONTADOR = 1L;
            log.info("FIN::EliminarSinTransacciones (portafolio no existe)");
            return toJson(gson, salida);
        }

        // 3. Validar que no tenga transacciones
        boolean tieneTransacciones = validarTransacciones(conn, entrada.p_IdPortafolio, salida);
        if (tieneTransacciones) {
            log.info("FIN::EliminarSinTransacciones (tiene transacciones)");
            return toJson(gson, salida);
        }

        // 4. Marcar como borrado
        PortafolioUpdate update = new PortafolioUpdate();
        update.IdPortafolio     = entrada.p_IdPortafolio;
        update.FechaModificacion = new Date(System.currentTimeMillis());
        update.ComentarioBloqueo = entrada.p_Comentario;
        update.CodigoPortafolio  = target.CodigoPortafolio + "-BORRADO";

        MapSqlParameterSource[] sources = {
                new MapSqlParameterSource()
                        .addValue("IdPortafolio",     update.IdPortafolio)
                        .addValue("FechaModificacion", update.FechaModificacion)
                        .addValue("ComentarioBloqueo", update.ComentarioBloqueo)
                        .addValue("CodigoPortafolio",  update.CodigoPortafolio)
        };
        batch(conn, SQL_UPDATE_PORTAFOLIO, sources);

        salida.P_OUT_MENSAJES.add(MensajeSalida.ok("EXITO_ELIMINAR_PORTAFOLIO",
                "UI_EXITO_ELIMINAR_PORTAFOLIO"));

        log.info("FIN::EliminarSinTransacciones {}ms", System.currentTimeMillis() - inicio);
        return toJson(gson, salida);
    }

    private List<PortafolioRow> queryPortafolios(Connection conn, Long idCliente) {
        return query(conn, SQL_SELECT_PORTAFOLIO,
                Map.of("IdCliente", idCliente),
                fieldMapper(PortafolioRow.class));
    }

    private boolean validarTransacciones(Connection conn, Long idPortafolio, Salida salida) {
        List<TransaccionRow> transacciones = query(conn, SQL_SELECT_TRANSACCIONES,
                Map.of("IdPortafolio", idPortafolio),
                fieldMapper(TransaccionRow.class));

        Map<String, Long> porTabla = transacciones.stream()
                .filter(t -> t.Cantidad != null && t.Cantidad > 0)
                .collect(Collectors.groupingBy(t -> t.Tabla, Collectors.summingLong(t -> t.Cantidad)));

        for (String tabla : porTabla.keySet()) {
            String desc = "UI_ERROR_PORTAFOLIO_OPERACIONES_" + tabla;
            salida.P_OUT_MENSAJES.add(MensajeSalida.error("ERROR_ELIMINAR_PORTAFOLIO", desc));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(salida.P_OUT_MENSAJES);
        }
        return !porTabla.isEmpty();
    }

    // ─── DTOs ────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long   p_IdCliente;
        public Long   p_IdPortafolio;
        public String p_Comentario;
    }

    public static class PortafolioRow {
        public Long   IdPortafolio;
        public String CodigoPortafolio;
        public Long   IdPortafolioDefecto;
    }

    public static class TransaccionRow {
        public Long   Cantidad;
        public String Tabla;
    }

    private static class PortafolioUpdate {
        public Long   IdPortafolio;
        public Date   FechaModificacion;
        public String ComentarioBloqueo;
        public String CodigoPortafolio;
    }

    private static class Salida {
        public Long               P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES       = new ArrayList<>();
    }

    private static JsonObject toJson(Gson gson, Object obj) {
        return JsonParser.parseString(gson.toJson(obj)).getAsJsonObject();
    }
}
