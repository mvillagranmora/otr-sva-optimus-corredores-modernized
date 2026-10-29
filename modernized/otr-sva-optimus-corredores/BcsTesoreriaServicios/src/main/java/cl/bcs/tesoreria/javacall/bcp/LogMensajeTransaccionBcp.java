package cl.bcs.tesoreria.javacall.bcp;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * LOG.MensajeTransaccionBCP: the record of every message received from BCP (inquiry, payment, reversal) and of the answer sent back.
 * Shared by RecaudacionBCP (TES-B-64..88, 96) and ConsultaBCP (TES-B-89..95), which kept two identical copies of this code in the legacy.
 */
@Component
public class LogMensajeTransaccionBcp {

    /** Row of the log; the field names are the column / parameter names of the SQL files. */
    public static class Registro {
        public Long IdMensajeTransaccion;
        public String RqUID;
        public String TipoTrasaccion; // CONSULTA | PAGO | EXTORNO
        public String MensajeEntrada; // message received from BCP (as json)
        public String MensajeSalida;  // answer of Optimus to BCP (as json)
        public Timestamp FechaHoraRecepcion;
        public Date FechaSistema;
        public Long IdEmpresa;
    }

    private static final String SQL_INSERT = SqlNombrado.recurso("RecaudacionBCP_InsertLogMensajeTransaccion");
    private static final String SQL_SELECT = SqlNombrado.recurso("RecaudacionBCP_SelectLogMensajeTransaccion");
    private static final String SQL_UPDATE = SqlNombrado.recurso("RecaudacionBCP_UpdateLogMensajeTransaccion");

    private final SqlNombrado sql;

    public LogMensajeTransaccionBcp(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** Inserts the received message (the id comes from the HANA sequence inside the statement, the reception time from the database). */
    public void registrar(String rqUid, String tipoTransaccion, String mensajeEntrada, String fechaSistema, Long idEmpresa)
            throws SQLException, ParseException {
        Registro r = new Registro();
        r.RqUID = rqUid;
        r.TipoTrasaccion = tipoTransaccion;
        r.MensajeEntrada = mensajeEntrada;
        // lenient SimpleDateFormat as the legacy (a malformed business date is a ParseException)
        r.FechaSistema = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(fechaSistema).getTime());
        r.IdEmpresa = idEmpresa;
        sql.lote(SQL_INSERT, List.of(r));
    }

    /**
     * The row just inserted, keyed by its RqUID as stored: the first of the rows whose RqUID equals (ignoring case) the requested one
     * (the query is ordered newest first); empty when none.
     */
    public Map<String, Registro> buscar(Long idEmpresa, String tipoTransaccion, String rqUid) throws SQLException {
        Map<String, Registro> encontrados = new HashMap<>();
        Map<String, Object> parametros = new HashMap<>(); // null values allowed (a request without type or id binds NULL)
        parametros.put("IdEmpresa", idEmpresa);
        parametros.put("TipoTrasaccion", tipoTransaccion);
        parametros.put("RqUIDs", java.util.Collections.singletonList(rqUid));
        List<Registro> filas = sql.consultar(SQL_SELECT, parametros, Registro.class);
        for (Registro fila : filas) {
            if (fila.RqUID.equalsIgnoreCase(rqUid)) {
                encontrados.put(fila.RqUID, fila);
                break;
            }
        }
        return encontrados;
    }

    /** Stores the answer sent to BCP. */
    public void guardarSalida(List<Registro> registros) throws SQLException {
        if (!registros.isEmpty()) {
            sql.lote(SQL_UPDATE, registros);
        }
    }
}
