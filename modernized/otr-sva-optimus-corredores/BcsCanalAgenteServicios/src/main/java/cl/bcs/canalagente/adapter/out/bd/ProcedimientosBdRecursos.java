package cl.bcs.canalagente.adapter.out.bd;

import cl.bcs.canalagente.adapter.out.jdbc.SqlRecursos;
import cl.bcs.canalagente.port.out.ProcedimientosBdPort;

/** The procedure texts as classpath resources {@code cl/bcs/canalagente/bd/servicios/<nombre>.sql}. A missing resource fails. */
public class ProcedimientosBdRecursos implements ProcedimientosBdPort {

    @Override
    public String texto(String nombre) {
        return SqlRecursos.cargar(SqlRecursos.BD_SERVICIOS + nombre + ".sql");
    }
}
