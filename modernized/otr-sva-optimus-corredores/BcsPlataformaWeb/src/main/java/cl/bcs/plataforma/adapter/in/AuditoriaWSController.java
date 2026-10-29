package cl.bcs.plataforma.adapter.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Web-service audit log endpoint.
 * Source: AuditoriaWS — @WebServlet("/AuditoriaWS")
 */
@RestController
public class AuditoriaWSController {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaWSController.class);

    // Source: AuditoriaWS.doPost — 7 @@@-separated fields
    static final int EXPECTED_PARAM_COUNT = 7;

    // Source: AuditoriaWS.insert SQL
    private static final String INSERT_AUDITORIA =
        "INSERT INTO \"BSCL.Plataforma.BD::PLA.AuditoriaWebServices\"" +
        "(\"Servicio\",\"Entrada\",\"Salida\",\"FechaInicio\",\"FechaFin\",\"IpOrigen\",\"HTTPCode\") " +
        "VALUES(?,?,?,?,?,?,?)";

    private final DataSource dataSource;

    public AuditoriaWSController(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /**
     * POST /AuditoriaWS — inserts a WS audit record.
     *
     * Body format (raw, @@@-delimited):
     * SERVICIO@@@ENTRADA@@@SALIDA@@@FECHA_INICIO@@@FECHA_FIN@@@IP_ORIGEN@@@HTTP_CODE
     * Source: AuditoriaWS.performTask
     */
    @PostMapping("/AuditoriaWS")
    public ResponseEntity<String> auditar(@RequestBody(required = false) String body) {
        if (body == null) {
            return ResponseEntity.badRequest().body("ERROR");
        }
        String[] params = body.split("@@@");
        if (params.length != EXPECTED_PARAM_COUNT) {
            return ResponseEntity.badRequest().body("ERROR");
        }

        String    servicio    = params[0];
        String    entrada     = params[1];
        String    salida      = params[2];
        Timestamp fechaInicio = parseTimestamp(params[3]);
        Timestamp fechaFin    = parseTimestamp(params[4]);
        String    ipOrigen    = params[5];
        int       httpCode    = Integer.parseInt(params[6].trim());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_AUDITORIA)) {
            ps.setString(1, servicio);
            ps.setString(2, entrada);
            ps.setString(3, salida);
            ps.setTimestamp(4, fechaInicio);
            ps.setTimestamp(5, fechaFin);
            ps.setString(6, ipOrigen);
            ps.setInt(7, httpCode);
            ps.execute();
            conn.commit();
        } catch (Exception e) {
            log.error("Error registrando auditoria WS", e);
            return ResponseEntity.status(500).body("ERROR");
        }
        return ResponseEntity.ok("OK");
    }

    /**
     * Parses a timestamp string.
     * Source: AuditoriaWS.performTask — Fecha.toTimeStamp(params[3])
     */
    public static Timestamp parseTimestamp(String s) {
        Objects.requireNonNull(s, "timestamp string must not be null");
        try {
            return Timestamp.valueOf(s.replace("T", " ").trim());
        } catch (Exception e) {
            return new Timestamp(0);
        }
    }
}
