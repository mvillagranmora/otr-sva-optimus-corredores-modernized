package cl.bcs.normativa.cartolas.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import cl.bcs.normativa.cartolas.dal.ConfigService;
import cl.bcs.normativa.cartolas.dal.PlantillaService;
import cl.bcs.normativa.cartolas.dal.RecursoService;
import cl.bcs.normativa.cartolas.entidades.Config;
import cl.bcs.normativa.cartolas.entidades.Plantilla;
import cl.bcs.normativa.cartolas.entidades.Recurso;
import cl.bcs.normativa.cartolas.entidades.TipoRecurso;
import cl.bcs.normativa.cartolas.master.GeneradorMaster;
import cl.bcs.normativa.cartolas.master.config.GraficoMaster;
import cl.bcs.normativa.cartolas.master.config.ImagenMaster;
import cl.bcs.normativa.cartolas.master.config.ReporteMaster;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.cartolas.procesos.JasperUtils;
import cl.bcs.sql.utils.SqlConnectionBuilder;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.PropertiesUtil;
import net.sf.jasperreports.engine.JRException;

public final class ConfiguradorCartolasApp {

    private static final String SP_PREFIX = "BSCL.Normativa.BD.Servicios::CAR.";
    private static final Date VIGENTE_DESDE = SqlUtil.getDate(2016, Calendar.JANUARY, 1);

    private static void cargarConfig(
            final ContextoCartolas contexto,
            final Plantilla plantilla)
                    throws IOException,
                    SQLException {
        try (final InputStream is = ConfiguradorCartolasApp.class
                .getResourceAsStream("/csv/configs.csv")) {
            final List<Config> configs = Config.loadFromCsv(is);
            for (final Config config : configs) {
                config.setIdEmpresa(plantilla.getIdEmpresa());
                ConfigService.save(contexto, config);
            }
        }
    }

    private static void cargarGrafico(
            final ContextoCartolas contexto,
            final Plantilla plantilla,
            final GraficoMaster grafico)
                    throws SQLException,
                    IOException {
        cargarRecurso(contexto, plantilla, grafico.name(), TipoRecurso.PROCEDIMIENTO,
                getNombreSP(grafico.name()));
    }

    private static void cargarGraficos(
            final ContextoCartolas contexto,
            final Plantilla plantilla)
                    throws SQLException,
                    IOException {
        cargarGrafico(contexto, plantilla, GraficoMaster.GraficoActivoPorProducto);
        cargarGrafico(contexto, plantilla, GraficoMaster.GraficoConsolidadoPorProducto);
        cargarGrafico(contexto, plantilla, GraficoMaster.GraficoFM);
    }

    private static void cargarImagen(
            final ContextoCartolas contexto,
            final Plantilla plantilla,
            final ImagenMaster imagen)
                    throws IOException,
                    SQLException {
        final String path = "/img/" + imagen.getNombreArchivo();
        final String nombre = imagen.getNombreParametroReporte();
        try (final InputStream is = ConfiguradorCartolasApp.class.getResourceAsStream(path)) {
            RecursoService.createImage(contexto, plantilla, imagen.getGrupo(), nombre,
                    imagen.getDescripcion(), is);
        }
    }

    private static void cargarImagenes(
            final ContextoCartolas contexto,
            final Plantilla plantilla)
                    throws IOException,
                    SQLException {
        cargarImagen(contexto, plantilla, ImagenMaster.Logo);
        cargarImagen(contexto, plantilla, ImagenMaster.Banner);
    }

    private static void cargarParametros(
            final ContextoCartolas contexto,
            final Plantilla plantilla)
                    throws IOException,
                    SQLException {
        try (final InputStream is = ConfiguradorCartolasApp.class
                .getResourceAsStream("/csv/parametros.csv")) {
            final List<Recurso> recursos = Recurso.loadFromCsv(is);
            for (final Recurso recurso : recursos) {
                recurso.setIdPlantilla(plantilla.getId());
                RecursoService.save(contexto, recurso);
            }
        }
    }

    private static void cargarProcedimiento(
            final ContextoCartolas contexto,
            final Plantilla plantilla,
            final String nombre)
                    throws SQLException,
                    IOException {
        cargarRecurso(contexto, plantilla, nombre, TipoRecurso.PROCEDIMIENTO, getNombreSP(nombre));
    }

    private static void cargarProcedimientos(
        final ContextoCartolas contexto,
        final Plantilla plantilla)
             throws SQLException,
              IOException {

        final Set<String> nombres = new HashSet();
        for (final ReporteMaster reporte : ReporteMaster.values()) {
            if (reporte.getNombreDataSource() != null) {
                nombres.add(reporte.getNombreDataSource());
            }
        }

        for (final String nombre : nombres) {
            cargarProcedimiento(contexto, plantilla, nombre);
        }
    }

    private static void cargarRecurso(
            final ContextoCartolas contexto,
            final Plantilla plantilla,
            final String nombre,
            final String tipo,
            final String valor)
                    throws SQLException,
                    IOException {
    	
    	String name = nombre;
    	if (tipo.equals("jasper")){
    		name = nombre + "Jasper";
    	};
    	
        final Recurso recurso = Recurso.newInstance(plantilla, "SISTEMA", name, tipo, valor);
        RecursoService.save(contexto, recurso);
    }

    private static void cargarReporte(
            final ContextoCartolas contexto,
            final Plantilla plantilla,
            final ReporteMaster reporte)
                    throws IOException,
                    SQLException,
                    JRException {
        final String nombre = reporte.name();
        final String currentPath=new java.io.File( "." ).getCanonicalPath();        
        final String path = "/rpt/" + reporte.getNombreArchivo() + ".jrxml";        
        File fFile=new File( currentPath+path);        
        if (!fFile.isFile()) {
        	System.err.println("Archivo no encontrado:"+path);
        	System.exit(0);
        }
        else {     
        	System.out.println("Nombre:"+nombre+" Reporte:"+path);
        }         
        try (final InputStream is =  new FileInputStream(fFile)) {
            final String valor = JasperUtils.compileAndSerializeReport(is);
            cargarRecurso(contexto, plantilla, nombre, TipoRecurso.REPORTE_EXE, valor);
        }catch(Exception er) {
        	System.out.println("Path:"+currentPath);
        	System.out.println("No se puede cargar"+path);
        	er.printStackTrace();
        	System.exit(0);
        }
    }

    private static void cargarReportes(
            final ContextoCartolas contexto,
            final Plantilla plantilla)
                    throws SQLException,
                    IOException,
                    JRException {
        for (final ReporteMaster reporte : ReporteMaster.values()) {
            cargarReporte(contexto, plantilla, reporte);
        }
    }

    private static Plantilla crearPlantilla(
            final ContextoCartolas contexto)
                    throws SQLException {
        final Plantilla plantilla = Plantilla.newInstance(contexto.getEmpresa().getId(),
                GeneradorMaster.NOMBRE_PLANTILLA, VIGENTE_DESDE, true);
        PlantillaService.save(contexto, plantilla);
        return plantilla;
    }

    private static String getNombreSP(
            final String nombre) {
        return SP_PREFIX + nombre;
    }

    public static void main(
            final String[] args)
                    throws SQLException,
                    IOException,
                    JRException {
        final Properties properties = PropertiesUtil.newProperties("cartolas.properties");
        final Properties connProps = PropertiesUtil.newProperties("connection.properties");
        final SqlConnectionSettings connSettings = new SqlConnectionSettings(connProps, "source");

        try (final Connection conn = SqlConnectionBuilder.getConnection(connSettings)) {
            final ContextoCartolas contexto = new ContextoCartolas(conn, properties, 0);

            try {
            	Plantilla plantilla = new Plantilla();
            	plantilla.setId(1);
            	
            	
                //final Plantilla plantilla = crearPlantilla(contexto);
                cargarReportes(contexto, plantilla);
                //cargarProcedimientos(contexto, plantilla);
                //cargarGraficos(contexto, plantilla);
                //cargarParametros(contexto, plantilla);
                //cargarConfig(contexto, plantilla);
                //cargarImagenes(contexto, plantilla);
                conn.commit();
                System.out.println("FIN NADIA!");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private ConfiguradorCartolasApp() {
    }

}
