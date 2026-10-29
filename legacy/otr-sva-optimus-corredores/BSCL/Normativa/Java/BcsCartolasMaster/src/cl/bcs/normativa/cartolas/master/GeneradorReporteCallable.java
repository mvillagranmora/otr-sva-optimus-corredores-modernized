package cl.bcs.normativa.cartolas.master;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSourcePersistente;
import cl.bcs.utils.ConcurrentArchiver;

public class GeneradorReporteCallable implements Callable<Boolean> {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GeneradorReporteCallable.class.getName());

    private final ConcurrentArchiver archiver;
    private final Cartola cartola;
    private final ContextoCartolas contexto;
    private final GeneradorMaster generador;
    private final ProveedorJasperDataSourcePersistente proveedorJasperDataSource;

    public GeneradorReporteCallable(
            final ContextoCartolas contexto,
            final DataSource dataSource,
            final ConcurrentArchiver archiver,
            final Cartola cartola) throws SQLException, IOException {
        this.contexto = contexto;
        this.cartola = cartola;
        this.archiver = archiver;
        this.proveedorJasperDataSource = new ProveedorJasperDataSourcePersistente();
        this.generador = new GeneradorMaster(contexto, dataSource);
    }

    @Override
    public Boolean call() {
        try {
            cartola.abrir(contexto);
            generador.generar(cartola, contexto.getNomina().getPlantilla(),
                    proveedorJasperDataSource, archiver);
            cartola.cerrar(contexto);
            return true;
        } catch (Exception ex) {
            cartola.cerrar(contexto, ex);
            LOGGER.log(Level.SEVERE, "Error al generar cartola", ex);
            return false;
        }
    }

}
