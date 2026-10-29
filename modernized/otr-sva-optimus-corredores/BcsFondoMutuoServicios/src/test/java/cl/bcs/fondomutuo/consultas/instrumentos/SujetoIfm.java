package cl.bcs.fondomutuo.consultas.instrumentos;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;

/**
 * The NEW ConsultarInstrumentosFondosMutuos over the REAL JDBC adapters on the H2 of the case: the dictionary of the kit (only for the {@code UI_SIN_VALORES}
 * message) and the three SQL of the class ({@code ..._SelectFondoMutuo.sql}, {@code ..._SelectFondoMutuoReinversion.sql}, {@code ..._SelectRegimen.sql}, verbatim).
 * No collaborator outside the module ({@link Guion} is accepted for symmetry and never called). Nothing here has state. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoIfm {

    private SujetoIfm() {
    }

    static ConsultarInstrumentosPort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        ConsultarInstrumentosFabrica.Dependencias d = new ConsultarInstrumentosFabrica.Dependencias();
        d.diccionario = new DiccionarioJdbc(conexion);
        d.datos = new ConsultarInstrumentosJdbc(conexion);
        return ConsultarInstrumentosFabrica.crear(d);
    }
}
