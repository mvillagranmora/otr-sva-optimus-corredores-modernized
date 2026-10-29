package cl.bcs.jobmanager.support;

import cl.bcs.jobmanager.adapter.out.ProgramaJdbcAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/** Fresh H2 with the real BAT.* names (schema-bat-real.sql), the real adapter on top, and seed helpers. */
public final class BatH2 {

    /** Fixed "now" of the tests (mid-day UTC), so the date window is deterministic. */
    public static final Instant AHORA = Instant.parse("2026-10-21T15:00:00Z");

    private static final String P = "\"BSCL.Plataforma.BD::BAT.Programa\"";
    private static final String TP = "\"BSCL.Plataforma.BD::BAT.TipoPrograma\"";
    private static final String AG = "\"BSCL.Plataforma.BD::BAT.Agenda\"";

    public final DataSource ds = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
            .setName("bat_" + UUID.randomUUID().toString().replace("-", ""))
            .addScript("classpath:schema-bat-real.sql").build();
    public final JdbcTemplate jdbc = new JdbcTemplate(ds);
    public final ProgramaJdbcAdapter adapter = new ProgramaJdbcAdapter(new NamedParameterJdbcTemplate(ds));

    public void tipo(String id, String clase, int maximoIntentos) {
        jdbc.update("INSERT INTO " + TP + " (\"IdTipoPrograma\",\"MaximoIntentos\",\"NombreClasePrograma\",\"TipoClasePrograma\",\"Nodo\")"
                + " VALUES (?,?,?,'JAVA','')", id, maximoIntentos, clase);
    }

    public void agenda(long id, String tipo, String esDemonio) {
        jdbc.update("INSERT INTO " + AG + " (\"IdAgenda\",\"IdTipoPrograma\",\"EsDemonio\") VALUES (?,?,?)", id, tipo, esDemonio);
    }

    /** Eligible by default: PEN, Bloqueo N, node '', Intento 0, executes one hour before AHORA. */
    public ProgramaSeed programa(String tipo) {
        return new ProgramaSeed(tipo);
    }

    public Object columna(long id, String columna) {
        return jdbc.queryForObject("SELECT \"" + columna + "\" FROM " + P + " WHERE \"IdPrograma\" = ?", Object.class, id);
    }

    public List<Object> filaCompleta(long id) {
        return jdbc.query("SELECT \"Intento\",\"idEstado\",\"Resultado\",\"fechaFin\",\"Nodo\",\"Bloqueo\" FROM " + P
                + " WHERE \"IdPrograma\" = ?", (rs, i) -> List.<Object>of(rs.getInt(1), rs.getString(2),
                String.valueOf(rs.getString(3)), String.valueOf(rs.getTimestamp(4)), rs.getString(5), rs.getString(6)), id).get(0);
    }

    public static Timestamp ts(Instant i) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(i, ZoneOffset.UTC));
    }

    public final class ProgramaSeed {
        private final String tipo;
        private String estado = "PEN", bloqueo = "N", nodo = "";
        private int intento = 0;
        private long agenda = 0;
        private Instant ejecucion = AHORA.minusSeconds(3600), creacion = AHORA.minusSeconds(7200);

        ProgramaSeed(String tipo) { this.tipo = tipo; }
        public ProgramaSeed estado(String v) { estado = v; return this; }
        public ProgramaSeed bloqueo(String v) { bloqueo = v; return this; }
        public ProgramaSeed nodo(String v) { nodo = v; return this; }
        public ProgramaSeed intento(int v) { intento = v; return this; }
        public ProgramaSeed agenda(long v) { agenda = v; return this; }
        public ProgramaSeed ejecucion(Instant v) { ejecucion = v; return this; }
        public ProgramaSeed creacion(Instant v) { creacion = v; return this; }

        public long insertar() {
            jdbc.update("INSERT INTO " + P + " (\"IdAgenda\",\"IdTipoPrograma\",\"Intento\",\"idEstado\",\"Resultado\","
                    + "\"fechaCreacion\",\"fechaEjecucion\",\"Nodo\",\"Bloqueo\") VALUES (?,?,?,?,'Esperando ejecucion',?,?,?,?)",
                    agenda, tipo, intento, estado, ts(creacion), ts(ejecucion), nodo, bloqueo);
            return jdbc.queryForObject("SELECT MAX(\"IdPrograma\") FROM " + P, Long.class);
        }
    }
}
