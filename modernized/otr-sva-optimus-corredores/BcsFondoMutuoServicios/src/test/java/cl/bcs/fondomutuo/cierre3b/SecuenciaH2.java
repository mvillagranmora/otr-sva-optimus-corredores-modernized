package cl.bcs.fondomutuo.cierre3b;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * H2 stand-in of {@link SecuenciaPort} (H2 has no {@code SERIES_GENERATE_BIGINT}): one {@code NEXT VALUE FOR} of the sequence of the same name per value,
 * and the request is recorded as {secuencia, cantidad} like the oracle's shim of {@code Utils.getSecuencia}.
 */
public final class SecuenciaH2 implements SecuenciaPort {

    private final Connection con;
    private final JsonArray registro;

    public SecuenciaH2(Connection con, JsonArray registro) {
        this.con = con;
        this.registro = registro;
    }

    @Override
    public List<Long> siguientes(String nombreSecuencia, Long cantidad) {
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return salida;
        }
        JsonObject o = new JsonObject();
        o.addProperty("secuencia", nombreSecuencia);
        o.addProperty("cantidad", cantidad);
        registro.add(o);
        try {
            for (long k = 0; k < cantidad; k++) {
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT NEXT VALUE FOR \"" + nombreSecuencia + "\"")) {
                    rs.next();
                    salida.add(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return salida;
    }
}
