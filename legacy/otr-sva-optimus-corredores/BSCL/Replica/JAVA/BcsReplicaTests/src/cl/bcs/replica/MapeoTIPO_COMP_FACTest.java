package cl.bcs.replica;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import cl.bcs.utils.ConnectionUtils;

@TestInstance(Lifecycle.PER_CLASS)
public class MapeoTIPO_COMP_FACTest {

    private static final String PROPERTIES_FILE = "connections.properties";
    private static final String AMBIENTE_HANA   = "desa01";
    private static final String AMBIENTE_PG     = "desapg";

    private static final String SQL_HANA =
        "SELECT \"BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC\"(?) FROM DUMMY";
    private static final String SQL_PG = 
        "SELECT \"BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC\"(?)";

    @ParameterizedTest
    @ValueSource(strings = { "FAC", "FAE", "FOC", "FOE", "FEE", "FOX", "NCR", "NCE", "NDR", "NEO", "NEE", "XXX" })
    public void compararHanaVsPostgres(String idTipoComprobante) throws Exception {
        String resultadoHana;
        String resultadoPg;

        try (Connection conn = ConnectionUtils.getPropertiesConnection(AMBIENTE_HANA, PROPERTIES_FILE)) {
            resultadoHana = ejecutarFuncion(conn, SQL_HANA, idTipoComprobante);
        }

        try (Connection conn = ConnectionUtils.getPropertiesConnection(AMBIENTE_PG, PROPERTIES_FILE)) {
            resultadoPg = ejecutarFuncion(conn, SQL_PG, idTipoComprobante);
        }

        assertEquals(resultadoHana, resultadoPg,
            "Diferencia para input '" + idTipoComprobante + "': HANA=" + resultadoHana + " PG=" + resultadoPg);
    }

    private String ejecutarFuncion(Connection conn, String sql, String input) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }
}
