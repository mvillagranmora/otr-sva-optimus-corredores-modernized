package cl.bcs.plataforma.login.dialecto;

import cl.bcs.plataforma.config.NoDisponibleException;

/**
 * Postgres. The login knows two sequences and their names are constants here; any other name is a 503 ({@link UnsupportedOperationException}), never an
 * invented name.
 */
public class PostgresDialecto implements SqlDialecto {

    private static final String SECUENCIA_LOG = "BSCL.Plataforma.BD.Secuencias::Log";
    private static final String SECUENCIA_DETALLE_LOG = "BSCL.Plataforma.BD.Secuencias::DetalleLog";

    public PostgresDialecto() {
    }

    @Override
    public String nombre() {
        return "postgres";
    }

    @Override
    public String siguienteValor(String secuenciaHana) {
        if (SECUENCIA_LOG.equals(secuenciaHana)) {
            return "nextval('sq_pla_log')";
        }
        if (SECUENCIA_DETALLE_LOG.equals(secuenciaHana)) {
            return "nextval('sq_pla_detalle_log')";
        }
        throw new NoDisponibleException("Sequence " + secuenciaHana + " is not available in postgres mode");
    }

    @Override
    public String seleccionarSinTabla(String expresion) {
        return "SELECT " + expresion;
    }
}
