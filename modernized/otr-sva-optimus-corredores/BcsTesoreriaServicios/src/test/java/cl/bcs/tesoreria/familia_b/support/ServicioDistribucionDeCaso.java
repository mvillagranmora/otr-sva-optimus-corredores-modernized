package cl.bcs.tesoreria.familia_b.support;

import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;
import cl.bcs.tesoreria.javacall.distribucion.MovimientosDistribucion;
import cl.bcs.tesoreria.javacall.distribucion.PreRendicionPac;

/** Builds ProcesarDistribucionService and its two step beans the way Spring wires them, over the doubles of one case. */
public final class ServicioDistribucionDeCaso {

    private ServicioDistribucionDeCaso() {
    }

    public static ProcesarDistribucionService de(Caso caso, ColaboradoresDeCaso colaboradores) {
        DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
        return new ProcesarDistribucionService(caso.jdbc(), new ConfiguracionDelCaso(caso),
                new PreRendicionPac(caso.jdbc(), diccionario),
                new MovimientosDistribucion(caso.jdbc(), diccionario, colaboradores),
                colaboradores);
    }
}
