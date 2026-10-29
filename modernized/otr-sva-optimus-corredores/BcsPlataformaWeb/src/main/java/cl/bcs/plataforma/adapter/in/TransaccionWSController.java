package cl.bcs.plataforma.adapter.in;

import cl.bcs.plataforma.service.TransaccionWSService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Transaction WS idempotency-locking endpoint.
 * Source: ProcesarIdTransaccion — @WebServlet("/ProcesarIdTransaccion")
 */
@RestController
public class TransaccionWSController {

    // Source: ProcesarIdTransaccion.INSERT_TRANSACCION_WS
    private static final String INSERT_TX =
        "INSERT INTO \"BSCL.Plataforma.BD::PLA.TransaccionWebServices\"" +
        "(\"IdTransaccionWS\",\"Servicio\",\"FechaInicio\") VALUES(?,?,?)";

    // Source: ProcesarIdTransaccion.DELETE_TRANSACCION_WS
    private static final String DELETE_TX =
        "DELETE FROM \"BSCL.Plataforma.BD::PLA.TransaccionWebServices\" " +
        "WHERE \"IdTransaccionWS\" = ?";

    private final DataSource dataSource;

    public TransaccionWSController(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /**
     * POST /ProcesarIdTransaccion — INSERT or DELETE an idempotency lock.
     *
     * Body format (raw, @@@-delimited): ACTION@@@ID@@@SERVICIO@@@FECHA
     * Source: ProcesarIdTransaccion.performTask
     */
    @PostMapping(value = "/ProcesarIdTransaccion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> procesar(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank()) {
            return errorResponse("CANTIDAD_PARAMETROS_INCORRECTOS",
                    "Cantidad de parámetros incorrectos", 400);
        }
        String[] params = body.split("@@@");

        if (!TransaccionWSService.isValidParams(params)) {
            return errorResponse("CANTIDAD_PARAMETROS_INCORRECTOS",
                    "Cantidad de parámetros incorrectos", 400);
        }

        String action = params[0];
        if (!TransaccionWSService.isValidAction(action)) {
            return errorResponse("ACCION_INVALIDA", "Acción no soportada", 400);
        }

        return switch (action) {
            case TransaccionWSService.ACTION_INSERT -> insert(params);
            case TransaccionWSService.ACTION_DELETE -> delete(params);
            default -> errorResponse("ACCION_INVALIDA", "Acción no soportada", 400);
        };
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private ResponseEntity<String> insert(String[] params) {
        String    idTx       = params[1];
        String    servicio   = params[2];
        String    fechaStr   = params[3];

        if (!TransaccionWSService.isValidIdLength(idTx)) {
            return errorResponse("LARGO_ID_SUPERA_LIMITE",
                    "El largo máximo del ID transacción es de 36 caracteres", 400);
        }

        try {
            Timestamp fechaInicio = Timestamp.valueOf(fechaStr.replace("T", " "));
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT_TX)) {
                ps.setString(1, idTx);
                ps.setString(2, servicio);
                ps.setTimestamp(3, fechaInicio);
                ps.executeUpdate();
                conn.commit();
            }
            return successResponse("TRANSACCION_INGRESADA_WS", "ID transacción ingresado", 201);
        } catch (Exception e) {
            if (isDuplicateKeyError(e)) {
                return errorResponse("TRANSACCION_BLOQUEADA_WS", "ID transacción bloqueado", 409);
            }
            return errorResponse("ERROR_DESCONOCIDO", "Error al procesar el ID de transacción", 500);
        }
    }

    private ResponseEntity<String> delete(String[] params) {
        String idTx = params[1];
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_TX)) {
            ps.setString(1, idTx);
            ps.executeUpdate();
            conn.commit();
            return successResponse("ID_TRANSACCION_ELIMINADO_WS", "ID transacción eliminado", 200);
        } catch (Exception e) {
            return errorResponse("ERROR_DESCONOCIDO", "Error al procesar el ID de transacción", 500);
        }
    }

    private static boolean isDuplicateKeyError(Exception e) {
        // HANA error code 301 = duplicate key; H2 uses a message check
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("unique") || msg.contains("duplicate") || msg.contains("301");
    }

    static ResponseEntity<String> errorResponse(String code, String msg, int status) {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 1);
        JsonObject detail = new JsonObject();
        detail.addProperty("IdInterno",   0L);
        detail.addProperty("TipoMensaje", "ERROR");
        detail.addProperty("Codigo",      code);
        detail.addProperty("Mensaje",     msg);
        JsonArray arr = new JsonArray();
        arr.add(detail);
        out.add("P_OUT_MENSAJES", arr);
        return ResponseEntity.status(status).body(out.toString());
    }

    static ResponseEntity<String> successResponse(String code, String msg, int status) {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonObject detail = new JsonObject();
        detail.addProperty("IdInterno",   0L);
        detail.addProperty("TipoMensaje", "AVISO");
        detail.addProperty("Codigo",      code);
        detail.addProperty("Mensaje",     msg);
        JsonArray arr = new JsonArray();
        arr.add(detail);
        out.add("P_OUT_MENSAJES", arr);
        return ResponseEntity.status(status).body(out.toString());
    }
}
