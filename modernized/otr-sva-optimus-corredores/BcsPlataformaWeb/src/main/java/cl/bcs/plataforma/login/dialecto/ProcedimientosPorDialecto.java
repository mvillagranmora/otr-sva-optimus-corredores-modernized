package cl.bcs.plataforma.login.dialecto;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;

/** Sends each call to the HANA dispatcher or to the Postgres implementation, according to the active dialect. */
public class ProcedimientosPorDialecto implements ProcedimientoAlmacenado {

    private final SqlDialecto dialecto;
    private final ProcedimientoAlmacenado hana;
    private final ProcedimientoAlmacenado postgres;

    public ProcedimientosPorDialecto(SqlDialecto dialecto, ProcedimientoAlmacenado hana, ProcedimientoAlmacenado postgres) {
        this.dialecto = dialecto;
        this.hana = hana;
        this.postgres = postgres;
    }

    @Override
    public JsonObject llamar(Connection con, String rutaServicio, JsonObject entrada) throws Exception {
        return "postgres".equals(dialecto.nombre()) ? postgres.llamar(con, rutaServicio, entrada) : hana.llamar(con, rutaServicio, entrada);
    }
}
