package cl.bcs.normativa.cartolas.entidades;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Portafolios {
    public static Portafolios newInstance(final ResultSet rs) throws SQLException {
        final Portafolios portafolios = new Portafolios();
        portafolios.setId(rs.getLong("IdPortafolio"));

        return portafolios;
    }

    private long id;    

    public long getId() {
        return id;
    }

    public void setId(
            final long id) {
        this.id = id;
    }
   
}
