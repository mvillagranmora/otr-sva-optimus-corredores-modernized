package cl.bcs.normativa.cartolas.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import cl.bcs.normativa.cartolas.dal.DatosService;
import cl.bcs.normativa.cartolas.entidades.Datos;
import cl.bcs.normativa.cartolas.entidades.FormatoDatos;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.master.config.ReporteMaster;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.utils.SqlConnectionBuilder;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.utils.FileUtils;
import cl.bcs.utils.PropertiesUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

public final class EmisorParcialCartolasApp {

    private static final String CSV_FOLDER_NAME = "csv";
    private static final String PDF_FOLDER_NAME = "pdf";

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException {
        final Properties properties = PropertiesUtil.newProperties("cartolas.properties");
        final Properties connProps = PropertiesUtil.newProperties("connection.properties");
        final SqlConnectionSettings connSettings = new SqlConnectionSettings(connProps, "source");
        try (final Connection conn = SqlConnectionBuilder.getConnection(connSettings)) {
            final EmisorParcialCartolasApp emisor = new EmisorParcialCartolasApp(conn, properties,
                    0);
            emisor.emitir();
        }
    }

    private final ContextoCartolas contexto;
    private final File csvFolder;
    private final File pdfFolder;

    public EmisorParcialCartolasApp(
            final Connection conn,
            final Properties props,
            final long idProceso) throws SQLException {
        this.contexto = new ContextoCartolas(conn, props, idProceso);
        final File outputFolder = new File(contexto.getOutputPath());
        this.csvFolder = FileUtils.createFolder(outputFolder, CSV_FOLDER_NAME);
        this.pdfFolder = FileUtils.createFolder(outputFolder, PDF_FOLDER_NAME);
    }

    private void emitir() {
        for (final ReporteMaster reporte : ReporteMaster.values()) {
            try {
                print(reporte);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getCsvFilePath(
            final String nombre) {
        return csvFolder.getAbsolutePath() + File.separator + nombre + ".csv";
    }

    private Datos getDatos(
            final long idCartola,
            final String nombre,
            final String tipo)
                    throws SQLException {
        final Datos datos = Datos.newInstance(idCartola, nombre, tipo);
        return DatosService.lookup(contexto.getConnection(), contexto.getSchema(), datos);
    }

    private JRDataSource getJRDataSource(
            final long idCartola,
            final String nombre)
                    throws FileNotFoundException,
                    SQLException {
        final Datos datos = getDatos(idCartola, nombre, FormatoDatos.CSV);
        save(nombre, datos);

        final InputStream datosStream = new ByteArrayInputStream(
                datos.getValor().getBytes(StandardCharsets.UTF_8));
        final JRCsvDataSource dataSource = new JRCsvDataSource(datosStream);
        dataSource.setRecordDelimiter("\n");
        dataSource.setLocale("en-US");
        dataSource.setUseFirstRowAsHeader(true);
        return dataSource;
    }

    private String getPdfFilePath(
            final ReporteMaster reporte) {
        return pdfFolder.getAbsolutePath() + File.separator + reporte.name() + ".pdf";
    }

    private void print(
            final ReporteMaster reporte)
                    throws SQLException,
                    JRException,
                    IOException {
        final String nombreDataSource = reporte.getNombreDataSource();
        if (nombreDataSource != null) {
            final Nomina nomina = contexto.getNomina();
            final String nombre = reporte.name();
            final JasperReport report = nomina.getPlantilla().getReporte(nombre);
            final JRDataSource dataSource = getJRDataSource(contexto.getIdCartola(),
                    nombreDataSource);
            final Map<String, Object> parameters = new ConcurrentHashMap();
            parameters.put("parametros", nomina.getPlantilla().newParametros());
            final JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters,
                    dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, getPdfFilePath(reporte));
        }
    }

    private void save(
            final String nombre,
            final Datos datos)
                    throws FileNotFoundException {
        try (final PrintStream ps = new PrintStream(getCsvFilePath(nombre))) {
            ps.print(datos.getValor());
        }
    }

}
