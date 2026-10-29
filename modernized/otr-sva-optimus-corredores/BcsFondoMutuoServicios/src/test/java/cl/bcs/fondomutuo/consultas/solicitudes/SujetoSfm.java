package cl.bcs.fondomutuo.consultas.solicitudes;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;

/**
 * The NEW ConsultarSolicitudesFFMM and ConsultarSolicitudesFFMMWS over the REAL JDBC adapters on the H2 of the case (the kit adapter of the F1 closure for the
 * parameters and the two new read adapters of this package); no collaborator outside the module is involved (the classes only read the view). Nothing here has state.
 * Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoSfm {

    private SujetoSfm() {
    }

    static ConsultarSolicitudesPort crear(ConexionUnidadDeTrabajo conexion) {
        ConsultarSolicitudesFabrica.Dependencias d = new ConsultarSolicitudesFabrica.Dependencias();
        d.configuracion = new ConfiguracionSistemaJdbc(conexion);
        d.datos = new ConsultarSolicitudesJdbc(conexion);
        return ConsultarSolicitudesFabrica.crear(d);
    }

    static ConsultarSolicitudesWsPort crearWs(ConexionUnidadDeTrabajo conexion) {
        ConsultarSolicitudesWsFabrica.Dependencias d = new ConsultarSolicitudesWsFabrica.Dependencias();
        d.configuracion = new ConfiguracionSistemaJdbc(conexion);
        d.datos = new ConsultarSolicitudesWsJdbc(conexion);
        return ConsultarSolicitudesWsFabrica.crear(d);
    }
}
