package cl.bcs.custodia.flujo.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.api.Trigger;

/**
 * Harness-side witness of the work table CUS.FlujoCustodia_MatrizUso (CUS-FLU-14): an H2 row trigger (schema-session.sql) that copies every
 * INSERTED and every DELETED row, in firing order, into "ORACLE.MatrizUsoAuditoria". The dump turns it into the list of rows written (with the
 * random IdTrabajo replaced by a marker, D-CUS-09) and the number deleted. Same class for the legacy oracle and the JUnit harness (SINGLE SOURCE,
 * copied by legacy-oracle/sync.py). Column order = the 18 columns of FlujoCustodia_InsertFlujoCustodia_MatrizUso.sql, as created in schema-flujo.sql.
 */
public class MatrizUsoAuditoria implements Trigger {

    private boolean insert;
    private int columnas;

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {
        insert = (type & Trigger.INSERT) != 0;
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        Object[] row = insert ? newRow : oldRow;
        columnas = row.length;
        StringBuilder q = new StringBuilder("INSERT INTO \"ORACLE.MatrizUsoAuditoria\" VALUES (NEXT VALUE FOR \"ORACLE.MatrizUsoAuditoriaSeq\", ?");
        for (int i = 0; i < columnas; i++) q.append(", ?");
        q.append(")");
        try (PreparedStatement ps = conn.prepareStatement(q.toString())) {
            ps.setString(1, insert ? "I" : "D");
            for (int i = 0; i < columnas; i++) ps.setObject(i + 2, row[i]);
            ps.executeUpdate();
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void remove() {
    }
}
