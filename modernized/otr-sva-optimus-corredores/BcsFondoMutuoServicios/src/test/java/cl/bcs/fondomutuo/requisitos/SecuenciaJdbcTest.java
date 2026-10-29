package cl.bcs.fondomutuo.requisitos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.SesionH2;

/**
 * Legacy {@code Utils.getSecuencia} (HANA branch): the statement is the legacy one ({@code "seq".NEXTVAL ... FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)}).
 * H2 has neither the table function nor the HANA quoted {@code NEXTVAL}: the fixture gives it an alias with the same name and the session rewrite of
 * the oracle ({@link SesionH2}); what is proven is that the adapter asks for the right number of values of the right sequence and that nothing is read
 * for a quantity of 0 or less. The real HANA behaviour (sequence ordering, caching) is not exercised.
 */
public class SecuenciaJdbcTest {

    /** H2 table function standing for HANA {@code SERIES_GENERATE_BIGINT(start, step, count)}: {@code count} rows. */
    public static ResultSet generar(Connection conn, long inicio, long paso, long cantidad) throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("GENERATED_PERIOD_START", Types.BIGINT, 20, 0);
        if (conn.getMetaData().getURL().equals("jdbc:columnlist:connection")) {
            return rs;
        }
        for (long i = 0; i < cantidad; i++) {
            rs.addRow(i);
        }
        return rs;
    }

    private SecuenciaJdbc adaptador() throws SQLException {
        Connection raw = DriverManager.getConnection("jdbc:h2:mem:sec" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement st = raw.createStatement()) {
            st.execute("CREATE ALIAS SERIES_GENERATE_BIGINT FOR \"" + SecuenciaJdbcTest.class.getName() + ".generar\"");
            st.execute("CREATE SEQUENCE \"BSCL.Test.Secuencias::Demo\" START WITH 500 INCREMENT BY 1");
        }
        return new SecuenciaJdbc(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(new SesionH2(raw, "2026-03-02 14:30:00").conexion(), true)));
    }

    @Test
    @DisplayName("asks for N consecutive values of the named sequence")
    void pideNValores() throws Exception {
        SecuenciaJdbc s = adaptador();
        assertEquals(List.of(500L, 501L, 502L), s.siguientes("BSCL.Test.Secuencias::Demo", 3L));
        assertEquals(List.of(503L), s.siguientes("BSCL.Test.Secuencias::Demo", 1L));
    }

    @Test
    @DisplayName("a quantity of 0, negative or null returns an empty list and reads nothing (the sequence does not exist here)")
    void sinCantidadNoLee() throws Exception {
        SecuenciaJdbc s = adaptador();
        assertTrue(s.siguientes("BSCL.Test.Secuencias::NoExiste", 0L).isEmpty());
        assertTrue(s.siguientes("BSCL.Test.Secuencias::NoExiste", -2L).isEmpty());
        assertTrue(s.siguientes("BSCL.Test.Secuencias::NoExiste", null).isEmpty());
    }
}
