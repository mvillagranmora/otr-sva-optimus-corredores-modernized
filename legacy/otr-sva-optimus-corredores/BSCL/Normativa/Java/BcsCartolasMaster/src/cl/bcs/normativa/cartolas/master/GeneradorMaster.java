package cl.bcs.normativa.cartolas.master;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;

import cl.bcs.normativa.cartolas.dal.CartolaService;
import cl.bcs.normativa.cartolas.dal.ComunService;
import cl.bcs.normativa.cartolas.dal.NominaService;
import cl.bcs.normativa.cartolas.dal.PlantillaService;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Cliente;
import cl.bcs.normativa.cartolas.entidades.EstadoProceso;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.Opcion;
import cl.bcs.normativa.cartolas.entidades.Plantilla;
//import cl.bcs.normativa.cartolas.entidades.Portafolios;
import cl.bcs.normativa.cartolas.master.config.GraficoMaster;
import cl.bcs.normativa.cartolas.master.config.ReporteMaster;
import cl.bcs.normativa.cartolas.master.graficos.ProveedorGraficos;
import cl.bcs.normativa.cartolas.nombres.ResolvedorNombre;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.cartolas.procesos.JasperUtils;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSource;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSourcePersistente;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSourceVolatil;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.ConcurrentArchiver;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

public class GeneradorMaster {

    public static final String NOMBRE_PLANTILLA = "CARTOLA_MASTER";

    private final ContextoCartolas contexto;
    private final DataSource dataSource;
    public static ProveedorGraficos proveedorGraficos;

    public GeneradorMaster(
            final ContextoCartolas contexto,
            final DataSource dataSource) throws SQLException, IOException {
        this.contexto = contexto;
        this.dataSource = dataSource;
    }

    public void generar(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final ConcurrentArchiver archiver)
                    throws JRException,
                    IOException,
                    SQLException,
                    PropertyVetoException,
                    ParseException {
        try (final Connection connection = dataSource.getConnection()) {
        	
            final CartolaMaster cartolaMaster = generar(connection, cartola, plantilla,proveedorJasperDataSource);
            archiver.archive(cartolaMaster.getNombreArchivo(), cartolaMaster.getBytes());
        }
    }

    public CartolaMaster generar(
            final Connection connection,
            final Cartola cartola,
            final Plantilla plantilla,
            
            final ProveedorJasperDataSource proveedorJasperDataSource)
                    throws SQLException,
                    JRException,
                    IOException,
                    ParseException {

        this.proveedorGraficos = new ProveedorGraficos(proveedorJasperDataSource);
        final List<JasperPrint> jpList = new ArrayList();

        //Parametros
        Map<String, Object> parametros = plantilla.newParametros();
       
        //Servicios
        proveedorJasperDataSource.load(connection, contexto.getSchema(), cartola);

    	jpList.add(getReportePagina1(cartola, plantilla, proveedorJasperDataSource, parametros));
        
        if (incluir(ReporteMaster.InfoCarteraMasterPag2, parametros)) {
            jpList.add(
                    getReportePagina2(cartola, plantilla, proveedorJasperDataSource, parametros));
        }

        if (incluir(ReporteMaster.InfoCarteraFM, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.InfoCarteraFM.getNombreDataSource())) {
            jpList.add(
                    getReporteResumenFM(cartola, plantilla, proveedorJasperDataSource, parametros));
        }

        if (incluir(ReporteMaster.CarteraRV, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraRV.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraRV, parametros));
        }

        if (incluir(ReporteMaster.CarteraRF, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraRF.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraRF, parametros));
        }

        if (incluir(ReporteMaster.CarteraIF, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraIF.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraIF, parametros));
        }

        if (incluir(ReporteMaster.CarteraFuturos, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraFuturos.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraFuturos, parametros));
        }
        
        if (incluir(ReporteMaster.CarteraFM, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraFM.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraFM, parametros));
        }

        if (incluir(ReporteMaster.CarteraSimultaneas, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraSimultaneas.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraSimultaneas, parametros));
        }

        if (incluir(ReporteMaster.CarteraPrestamos, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraPrestamos.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraPrestamos, parametros));
        }

        if (incluir(ReporteMaster.CarteraPactos, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraPactos.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraPactos, parametros));
        }

        if (incluir(ReporteMaster.CarteraFwd, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraFwd.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraFwd, parametros));
        }

        if (incluir(ReporteMaster.CarteraForward, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.CarteraForward.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.CarteraForward, parametros));
        }

        if (incluir(ReporteMaster.MovimientosCaja, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.MovimientosCaja.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.MovimientosCaja, parametros));
        }

        if (incluir(ReporteMaster.MovimientosTitulos, parametros) && proveedorJasperDataSource
                .hayDatos(ReporteMaster.MovimientosTitulos.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.MovimientosTitulos, parametros));
        }

        if (incluir(ReporteMaster.MovimientosTitulosForward, parametros)
                && proveedorJasperDataSource
                        .hayDatos(ReporteMaster.MovimientosTitulosForward.getNombreDataSource())) {
            jpList.add(getReporteCartera(cartola, plantilla, proveedorJasperDataSource,
                    ReporteMaster.MovimientosTitulosForward, parametros));
        }
        
        
        JasperUtils.trim(jpList.get(0));	        
        JasperUtils.setPageNumbers(jpList, "paginacion",(String) parametros.get("FORMATO_PAGINACION"));

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            final JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(jpList));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));
            exporter.exportReport();
            final byte[] bytes = os.toByteArray();
            final String name = ResolvedorNombre.getNombreArchivoCartola(contexto, cartola);
            final CartolaMaster cartolaMaster = new CartolaMaster(name, bytes);
            return cartolaMaster;
        }
    }

    public void generar(
            final Connection connection,
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final String outputPath)
                    throws JRException,
                    IOException,
                    SQLException,
                    PropertyVetoException,
                    ParseException {
        final CartolaMaster cartolaMaster = generar(connection, cartola, plantilla,
                proveedorJasperDataSource);
        final String path = outputPath + File.separator + cartolaMaster.getNombreArchivo();
        try (final OutputStream out = new FileOutputStream(path)) {
            out.write(cartolaMaster.getBytes());
        }
    }

    public CartolaMaster generar(
            final long idCartola)
                    throws SQLException,
                    JRException,
                    IOException,
                    ParseException {

        try (final Connection conn = dataSource.getConnection()) {
            final Cartola cartola = CartolaService.lookupByIdCartola(conn, contexto.getSchema(),
                    idCartola);
            final Nomina nomina = NominaService.lookupByIdNomina(conn, contexto.getSchema(),
                    cartola.getIdNomina());
            final ProveedorJasperDataSource proveedorJasperDataSource = new ProveedorJasperDataSourcePersistente();
            return generar(conn, cartola, nomina.getPlantilla(), proveedorJasperDataSource);
        }
    }

    public CartolaMaster generarOnline(
            final long idCliente,
            final Long idPortafolio,
            final Date fechaDesde,
            final Date fechaHasta)
                    throws SQLException,
                    JRException,
                    IOException,
                    ParseException {

        try (final Connection conn = dataSource.getConnection()) {
            final Plantilla plantilla = PlantillaService.lookupByNombreMasFecha(conn,
                    contexto.getSchema(), contexto.getEmpresa().getId(), NOMBRE_PLANTILLA,
                    SqlUtil.getDate());
            final Nomina nomina = newNomina(plantilla, contexto.getEmpresa().getId(), idCliente,
                    idPortafolio, contexto.getIdUsuario(), fechaDesde, fechaHasta);
            final List<Cartola> cartolas = getCartolas(idCliente, idPortafolio, nomina.getId());
            final ProveedorJasperDataSource proveedorJasperDataSource = new ProveedorJasperDataSourceVolatil(nomina, cartolas);
            return generar(conn, cartolas.get(0), plantilla, proveedorJasperDataSource);
        }
    }

    private List<Cartola> getCartolas(
            final long idCliente,
            final Long idPortafolio,
            final long idNomina) {
        final Cartola cartola = Cartola.newInstance(idNomina, idCliente, idPortafolio);
        final List<Cartola> cartolas = new ArrayList();
        cartolas.add(cartola);
        return cartolas;
    }

    private Map<String, Object> getParametersCartera(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final ReporteMaster reporte,
            final Map<String, Object> parametros)
                    throws JRException,
                    IOException,
                    SQLException {
        final Map<String, Object> map = new ConcurrentHashMap();
        map.put("subGrilla", plantilla.getReporte(reporte.name()));
        map.put("DataGrilla", proveedorJasperDataSource.getJRDataSource(cartola, reporte.getNombreDataSource()));
        map.put("parametros", parametros);
        return map;
    }

    public static Map<String, Object> getParametersPagina0(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros)
                    throws JRException,
                    SQLException,
                    IOException {
        final Map<String, Object> map = new ConcurrentHashMap();
        
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,ReporteMaster.ResumenInversionConsolidado);        
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,ReporteMaster.ResumenFlujosProyectadosConsolidado);
        
        if (proveedorJasperDataSource.hayDatos(GraficoMaster.GraficoConsolidadoPorProducto.name())) {
            setGrafico(cartola, map, GraficoMaster.GraficoConsolidadoPorProducto, "");
        }
        
        map.put("parametros", parametros);
        return map;
    }
    
    private Map<String, Object> getParametersPagina1(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros)
                    throws JRException,
                    SQLException,
                    IOException {
        final Map<String, Object> map = new ConcurrentHashMap();

        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.PortadaIzquierda);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.PortadaDerecha);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map, ReporteMaster.CodigoPDF417);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.FlujoPatrimonial);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.FlujoProyectado);

        /*if (incluir(ReporteMaster.ItemDerivados, parametros)) {
        	//setReporte(cartola, plantilla, proveedorJasperDataSource, parametros,
            //        ReporteMaster.ItemDerivadosForward);
        	
            setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                    ReporteMaster.ItemDerivados);
        }*/

        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.PatrimonioValorizado);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.ResumenDerivados);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.ResumenInversion);
        setReporte(cartola, plantilla, proveedorJasperDataSource, map,
                ReporteMaster.ResumenMovimientos);

        if (proveedorJasperDataSource.hayDatos(GraficoMaster.GraficoActivoPorProducto.name())) {
            setGrafico(cartola, map, GraficoMaster.GraficoActivoPorProducto, "");
        }

        map.put("parametros", parametros);
        return map;
    }

    private Map<String, Object> getParametersPagina2(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map parametros,
            final ReporteMaster reporte0,
            final ReporteMaster reporte1)
                    throws JRException,
                    IOException,
                    SQLException {
        final Map<String, Object> map = new ConcurrentHashMap();
        map.put("subGrilla", plantilla.getReporte(reporte0.name()));
        map.put("DataGrilla",
                proveedorJasperDataSource.getJRDataSource(cartola, reporte0.getNombreDataSource()));
        map.put("subGrilla2", plantilla.getReporte(reporte1.name()));
        map.put("DataGrilla2",
                proveedorJasperDataSource.getJRDataSource(cartola, reporte1.getNombreDataSource()));
        map.put("parametros", parametros);
        return map;
    }

    private JasperPrint getReporteCartera(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final ReporteMaster reporte,
            final Map<String, Object> parametros)
                    throws JRException,
                    SQLException,
                    IOException {
        final Map<String, Object> parameters = getParametersCartera(cartola, plantilla,proveedorJasperDataSource, reporte, parametros);
        final ReporteMaster reporteMaster = ReporteMaster.InfoCarteraMasterPag4;
        final JasperReport report = plantilla.getReporte(reporteMaster.name());
        final JRDataSource dataSource = proveedorJasperDataSource.getJRDataSource(cartola,reporteMaster.getNombreDataSource());
        return JasperFillManager.fillReport(report, parameters, dataSource);
    }

    ////////////////////////////// CARTOLA CONSOLIDADA (TODOS LOS PORTAFOLIOS) 
    public static JasperPrint getReportePagina0(
    		final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros)
                    throws JRException,
                    IOException,
                    SQLException {
        final Map<String, Object> parameters = getParametersPagina0(cartola, plantilla,proveedorJasperDataSource, parametros);
        final ReporteMaster reporteMaster = ReporteMaster.InfoCarteraMasterPag0;
        final JasperReport report = plantilla.getReporte(reporteMaster.name());
        final JRDataSource dataSource = proveedorJasperDataSource.getJRDataSource(cartola,reporteMaster.getNombreDataSource());
        return JasperFillManager.fillReport(report, parameters, dataSource);
    }
    
    //////////////////////////////CARTOLA (UNICO PORTAFOLIO) 
    private JasperPrint getReportePagina1(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros)
                    throws JRException,
                    IOException,
                    SQLException {
        final Map<String, Object> parameters = getParametersPagina1(cartola, plantilla,proveedorJasperDataSource, parametros);
        final ReporteMaster reporteMaster = ReporteMaster.InfoCarteraMasterPag1;
        final JasperReport report = plantilla.getReporte(reporteMaster.name());
        final JRDataSource dataSource = proveedorJasperDataSource.getJRDataSource(cartola,reporteMaster.getNombreDataSource());
        return JasperFillManager.fillReport(report, parameters, dataSource);
    }

    private JasperPrint getReportePagina2(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros)
                    throws JRException,
                    IOException,
                    SQLException {
        final Map<String, Object> parameters = getParametersPagina2(cartola, plantilla,
                proveedorJasperDataSource, parametros, 
                ReporteMaster.ProximosEventosCapital,
                ReporteMaster.ObservacionesAnotacionesCliente);
        final ReporteMaster reporteMaster = ReporteMaster.InfoCarteraMasterPag2;
        final JasperReport report = plantilla.getReporte(reporteMaster.name());
        final JRDataSource dataSource = proveedorJasperDataSource.getJRDataSource(cartola,reporteMaster.getNombreDataSource());
        return JasperFillManager.fillReport(report, parameters, dataSource);
    }

    private JasperPrint getReporteResumenFM(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros)
                    throws JRException,
                    IOException,
                    SQLException {

        setReporte(cartola, plantilla, proveedorJasperDataSource, parametros,
                ReporteMaster.ResumenInversionFM);

        if (proveedorJasperDataSource.hayDatos(GraficoMaster.GraficoFM.name())) {
            setGrafico(cartola, parametros, GraficoMaster.GraficoFM,
                    (String) parametros.get("TITULO_GRAFICO_FM"));
        }

        final Map<String, Object> parameters = getParametersCartera(cartola, plantilla,
                proveedorJasperDataSource, ReporteMaster.InfoCarteraFM, parametros);
        final ReporteMaster reporteMaster = ReporteMaster.InfoCarteraMasterPag4;
        final JasperReport report = plantilla.getReporte(reporteMaster.name());
        final JRDataSource dataSource = proveedorJasperDataSource.getJRDataSource(cartola,
                reporteMaster.getNombreDataSource());
        return JasperFillManager.fillReport(report, parameters, dataSource);
    }

    private boolean incluir(
            final ReporteMaster reporte,
            final Map<String, Object> parametros)
                    throws IOException {
        if (reporte.getNombreParametroInclusion() != null) {
            return (boolean) parametros.get(reporte.getNombreParametroInclusion());
        }

        return true;
    }

    private Nomina newNomina(
            final Plantilla plantilla,
            final long idEmpresa,
            final long idCliente,
            final long idPortafolio,
            final long idUsuario,
            final Date fechaDesde,
            final Date fechaHasta) {

        final Nomina nomina = new Nomina();
        nomina.setCertificada(Opcion.NO);
        nomina.setComentario(null);
        nomina.setEstadoProceso(EstadoProceso.EN_PROCESO);
        nomina.setFechaCertificacion(null);
        nomina.setFechaDesde(fechaDesde);
        nomina.setFechaHasta(fechaHasta);
        nomina.setFechaInicioProceso(null);
        nomina.setFechaProgramadaProceso(null);
        nomina.setFechaTerminoProceso(null);
        nomina.setId(0);
        nomina.setIdEmpresa(idEmpresa);
        nomina.setIdPlantilla(plantilla.getId());
        nomina.setIdUsuario(idUsuario);
        nomina.setMensajeProceso(null);
        nomina.setNombreUsuarioCertificador(null);
        nomina.setNotaCertificacion(null);
        nomina.setPeriodo(null);
        nomina.setPlantilla(plantilla);
        nomina.setTipoProceso("ONLINE");
        return nomina;
    }

    public static void setGrafico(
            final Cartola cartola,
            final Map<String, Object> parametros,
            final GraficoMaster grafico,
            final String title)
                    throws SQLException,
                    IOException,
                    JRException {
        parametros.put(grafico.getNombreParametroReporte(),
                proveedorGraficos.createGrafico(grafico, cartola, title));
    }

    public static void setReporte(
            final Cartola cartola,
            final Plantilla plantilla,
            final ProveedorJasperDataSource proveedorJasperDataSource,
            final Map<String, Object> parametros,
            final ReporteMaster reporte)
                    throws JRException,
                    SQLException,
                    IOException {
        parametros.put(reporte.getNombreParametroReporte(), plantilla.getReporte(reporte.name()));
        parametros.put(reporte.getNombreParametroDataSource(),proveedorJasperDataSource.getJRDataSource(cartola, reporte.getNombreDataSource()));
    }
    
    public static void main(String[] args) {
    	
		String dbURL  = "jdbc:sap://161.238.34.246:30215/?";
		String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "RULLOA";
        String pass   = "B0lsa123456";	        
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
		try {
		Properties props = new Properties();
		props.put("idEmpresa", "1");	
		props.put("idUsuario", "7");	
		props.put("schema", "BSCL");
        final ContextoCartolas contexto = new ContextoCartolas(ds.getConnection(),props, 0);
        final GeneradorMaster generador = new GeneradorMaster(contexto, ds);
        CartolaMaster cartolaMaster;

        final long idCliente = Long.valueOf("123570");
        final Long idPortafolio = Long.valueOf("512345");
        final Date fechaDesde = SqlUtil.getDate("2017-04-01");
        final Date fechaHasta = SqlUtil.getDate("2017-06-30");
        cartolaMaster = generador.generarOnline(idCliente, idPortafolio, fechaDesde,fechaHasta);
        final String filename = cartolaMaster.getNombreArchivo();
        System.out.println(filename+" bytes "+cartolaMaster.getBytes().length);
        IOUtils.write(cartolaMaster.getBytes(), new FileOutputStream(new File(filename+".pdf")));
        
        
		}catch(Exception er) {
			er.printStackTrace();
		}
    }

}
