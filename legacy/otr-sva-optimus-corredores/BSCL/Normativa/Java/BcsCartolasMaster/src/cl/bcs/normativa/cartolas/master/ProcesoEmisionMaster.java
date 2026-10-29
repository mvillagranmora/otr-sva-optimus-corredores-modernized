package cl.bcs.normativa.cartolas.master;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.cartolas.dal.CartolaService;
import cl.bcs.normativa.cartolas.dal.ComunService;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Cliente;
import cl.bcs.normativa.cartolas.nombres.ResolvedorNombre;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSource;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSourcePersistente;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.utils.ConcurrentArchiver;
import net.sf.jasperreports.engine.JRException;;

public class ProcesoEmisionMaster implements Proceso {

    private static final Logger LOGGER = Logger.getLogger(ProcesoEmisionMaster.class.getName());

    public static ProcesoEmisionMaster newInstance(
            final Connection conn,
            final Properties props,
            final long jobId,
            final DataSource dataSource)
                    throws SQLException {
        return new ProcesoEmisionMaster(conn, props, jobId, dataSource);
    }

    public static ProcesoEmisionMaster newInstance(
            final ContextoCartolas contexto,
            final DataSource dataSource) {
        return new ProcesoEmisionMaster(contexto, dataSource);
    }

    private final ContextoCartolas contexto;

    private final DataSource dataSource;

    public ProcesoEmisionMaster(
            final Connection conn,
            final Properties props,
            final long jobId,
            final DataSource dataSource) throws SQLException {
        this(new ContextoCartolas(conn, props, jobId), dataSource);
    }

    public ProcesoEmisionMaster(
            final ContextoCartolas contexto,
            final DataSource dataSource) {
        this.contexto = contexto;
        this.dataSource = dataSource;
    }

    public void generarCartola()
            throws SQLException,
            IOException,
            JRException,
            PropertyVetoException,
            ParseException {
        final GeneradorMaster generador = new GeneradorMaster(contexto, dataSource);
        final ProveedorJasperDataSource proveedorJasperDataSource = new ProveedorJasperDataSourcePersistente();
        final Cartola cartola = CartolaService.lookupByIdCartola(contexto.getConnection(),contexto.getSchema(), contexto.getIdCartola());
        generador.generar(contexto.getConnection(), cartola, contexto.getNomina().getPlantilla(),proveedorJasperDataSource, contexto.getOutputPath());
    }

    private String getFolderPath() {
        String folderPath = contexto.getConfigs().getRutaRaizArchivos();
        if (folderPath == null) {
            return contexto.getOutputPath();
        }

        final File folder = new File(folderPath);
        if (!folder.exists()) {
            folderPath = contexto.getOutputPath();
        }

        return folderPath;
    }

    private String getTextoResumen()
            throws SQLException {
        String texto = "";
        for (final Cartola cartola : contexto.getCartolas()) {
            String rut = "SIN_RUT";
            String nombre = "SIN_NOMBRE";
            String email = "SIN_EMAIL";
            final Cliente cliente = ComunService.getCliente(contexto.getConnection(),
                    contexto.getSchema(), cartola.getIdCliente());
            if (cliente != null) {
                nombre = cliente.getNombre();
                email = cliente.getEmail();
                rut = cliente.getDocumentoIdentidad();
            }
            final String codigo = ComunService.getCodigoPortafolio(contexto.getConnection(),
                    contexto.getSchema(), cartola.getIdPortafolio());
            texto += rut + ";";
            texto += codigo + ";";
            texto += nombre + ";";
            texto += email + ";";
            texto += cartola.getEstadoProceso() + "\n";
        }

        texto += contexto.getCartolas().size();
        return texto;
    }

    @Override
    public void procesar() {
        try {
            contexto.getConnection().setAutoCommit(true);

            final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
                    .newFixedThreadPool(contexto.getConfigs().getThreadPoolSize());

            String nombre = ResolvedorNombre.getNombreArchivoZip(contexto);
            final String path = getFolderPath() + File.separator + nombre;
            try (final ConcurrentArchiver archiver = new ConcurrentArchiver(path)) {
                final Set<Callable<Boolean>> callables = new HashSet<Callable<Boolean>>();
                for (final Cartola cartola : contexto.getCartolas()) {
                    callables.add(
                            new GeneradorReporteCallable(contexto, dataSource, archiver, cartola));
                }

                executor.invokeAll(callables);
                executor.shutdown();

                String texto = getTextoResumen();
                nombre = ResolvedorNombre.getNombreArchivoNomina(contexto);
                archiver.archive(nombre, texto.getBytes());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF", ex);
            throw new ProcesoException("Error al generar PDF: " + ex.getMessage(), ex);
        }
    }

}
