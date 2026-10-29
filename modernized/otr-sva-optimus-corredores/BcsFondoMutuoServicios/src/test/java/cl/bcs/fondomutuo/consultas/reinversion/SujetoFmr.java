package cl.bcs.fondomutuo.consultas.reinversion;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.Guion;

/**
 * The NEW ConsultarFondoMutuoReinversion over the REAL JDBC adapter on the H2 of the case. The class has NO collaborator (no kit, no profile, no basic
 * requirements, no dictionary): the service only reads the one SQL ({@code ConsultarFondoMutuo_SelectReinversion.sql}, verbatim) and copies the rows. The scripted
 * collaborators ({@link Guion}) are accepted for symmetry with the other fronts and are never called (the dump must show {@code colaboradores: []}).
 * Nothing here has state. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoFmr {

    private SujetoFmr() {
    }

    static ConsultarReinversionPort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        return ConsultarReinversionFabrica.crear(new ConsultarReinversionJdbc(conexion));
    }
}
