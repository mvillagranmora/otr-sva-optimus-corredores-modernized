package cl.bcs.normativa.generacion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.ResumenGeneracion;
import cl.bcs.normativa.interfaces.CambiarEstadoDJPreDeclaracion;
import cl.bcs.normativa.interfaces.CambiarEstadoDJValidacion;
import cl.bcs.normativa.lectores.Lector;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.normativa.registros.RegistroDJ1;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.utils.PropertiesUtil;
import cl.bcs.utils.StatsD;

/**
 * Implementaci&oacute;n de la generaci&oacute;n de documentos.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoGeneracion implements Proceso {
	private static final List<String> S1_EVENTOREPORTERECTIFICA = Arrays.asList("RVE", "RVI", "RNU", "REL");
	
	public static final String METRIC_PREFIX = "normativa.generacion.proceso_generacion";
	public static final String METRIC_TIEMPO_GENERACION = METRIC_PREFIX + "." + "tiempo_generacion";
	
    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoGeneracion.class.getName());

    /**
     * Obtiene instancia de {@link ProcesoGeneracion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param map
     *            {@link Map} de par&aacute;metros proporcionado por el Job
     *            Manager.
     * @param idProceso
     *            El id del proceso.
     * @return Instancia de {@link ProcesoGeneracion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ProcesoGeneracion newInstance(final Connection conn,
            final Map<String, String> map, final long idProceso, final DataSource ds) throws SQLException {
        final Properties props = PropertiesUtil.newProperties(map);
        return newInstance(conn, props, idProceso, ds);
    }

    /**
     * Obtiene instancia de {@link ProcesoGeneracion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return Instancia de {@link ProcesoGeneracion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ProcesoGeneracion newInstance(final Connection conn, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException {
        return new ProcesoGeneracion(conn, props, idProceso, ds);
    }

    /**
     * Contexto del generador.
     */
    private final ContextoGeneracion contexto;

    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoGeneracion(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {
        this.contexto = new ContextoGeneracion(conn, props, idProceso, ds);
    }

    /**
     * Implementa el proceso de generaci&oacute;n.
     *
     * @param reporte
     *            El reporte.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    private void generar(final ReporteGeneracion reporte)
            throws IOException, SQLException, SerializadorException {
        
    	final SqlMetadata metadata = contexto.getMetadata();     

    	final String nombreTablaDestino = contexto.getEsquema().getNombreVistaDestino();
    	
    	final SqlTable tablaDestino = metadata.getTable(contexto.getSchema(), nombreTablaDestino);

        String tipoArchivo = "";
        
        if(!"DJ1".equals(contexto.getEsquema().getNombre())) {   	 
        	generarArchivoNormativa(reporte, tablaDestino, tipoArchivo);
        }
        
        if("DJ1".equals(contexto.getEsquema().getNombre())) {	
        	tipoArchivo = "FX";      	      	
        	generarArchivoNormativa(reporte, tablaDestino, tipoArchivo);
        }
        
        if("DJ1".equals(contexto.getEsquema().getNombre())) {
        	tipoArchivo = "IR";
        	generarArchivoNormativa(reporte, tablaDestino,tipoArchivo);
        }
        
        if("DJ1".equals(contexto.getEsquema().getNombre())) {
        	tipoArchivo = "FI";
        	generarArchivoNormativa(reporte, tablaDestino, tipoArchivo);
        }
        
    }

	private void generarArchivoNormativa(final ReporteGeneracion reporte, final SqlTable tablaDestino, final String tipoArchivo) throws SQLException {
		Long init = System.currentTimeMillis();
		ResumenGeneracion resumen = new ResumenGeneracion();
				
		try (final Lector lector = LectorBuilder.newLectorDestino(contexto, tablaDestino, null,true, null, false)) {

            Registro registro = lector.siguiente();
            

            final Lector lectorAUX = LectorBuilder.newLectorDestino(contexto, tablaDestino, null,true, null, false);
            Registro registroAUX = lectorAUX.siguiente();
            


            if("DJ1".equals(contexto.getEsquema().getNombre())){
            	RegistroDJ1 registroDJ1 = (registro==null) ? null : new RegistroDJ1(registro);
            	
            	String nombreArchivo = contexto.getRutaArchivoGenerado();
            	nombreArchivo = nombreArchivo.replace("RutEntidadLegal", contexto.getRutDeclarante().replace("-", ""));
        		nombreArchivo = nombreArchivo.replace("Periodicidad", ("S".equals(contexto.getRectifica()) && "M".equals(contexto.getPeriodicidad())) ? "C" : contexto.getPeriodicidad());
        		String fechaPeriodoComercial = "M".equals(contexto.getPeriodicidad()) ? contexto.getDocumento().getVersionEsquema().replace("/", "") : registroDJ1 != null ? registroDJ1.getkPeriodoComercial().toString().replace("-", "") : contexto.getPeriodoComercial().replace("-", "");
        		nombreArchivo = nombreArchivo.replace("PeriodoComercial", fechaPeriodoComercial);
            	
        		if(tipoArchivo.equals("FX")) {       		            		
            		nombreArchivo = nombreArchivo.replace("TipoArchivo", "FX");            		
            		contexto.setRutaArchivoGenerado(nombreArchivo);
            	}
            	
            	if(tipoArchivo.equals("IR")) {       		
            		nombreArchivo = nombreArchivo.replace("FX", "IR");
            		contexto.setRutaArchivoGenerado(nombreArchivo);
            	}
            	
            	if(tipoArchivo.equals("FI")) {       		
            		nombreArchivo = nombreArchivo.replace("IR", "FI");
            		contexto.setRutaArchivoGenerado(nombreArchivo);
            	}
            }
            
            final Generador generador = GeneradorBuilder.newInstance(contexto);
            
            resumen = ResumenGeneracionBuilder.crearResumen(contexto,
            contexto.getRutaArchivoGenerado(),contexto.getNumeroPresentacion());
            

            int contadorAUX=0;
            
            //RECORRIDO PARA CONTAR EL NUMERO DE REGISTROS TIPO 2 Y MOSTRARLO EN REGISTRO TIPO 0
            while (registroAUX != null) {
            	
            	contadorAUX=contadorAUX+1;
            	registroAUX = lectorAUX.siguiente();
            }

            long contadorDJ1 = 0;
            
            try {
            	if("DJ1".equals(contexto.getEsquema().getNombre())) {
            		String periodicidad = "M".equals(contexto.getPeriodicidad()) && "S".equals(contexto.getRectifica()) ? "C" : contexto.getPeriodicidad();
            		generador.generarEncabezadoRegistro(contexto.getPeriodoComercial(), tipoArchivo, periodicidad, contexto.getRutDeclarante(), contexto.getEsquema().getRangoFechas().getFechaHasta());
            	}else {
                    generador.iniciarContadorCasosInformados(contadorAUX);
            		generador.generarEncabezado(); 
                    generador.reiniciarContadorCasosInformados();
            	}          	
            } catch (SerializadorException ex) {
                manejar(reporte, ex);
            }

            List<SeccionLinea> lstSeccionLineasFinales = new ArrayList<SeccionLinea>();
           
            while (registro != null) {
                try {
                	if("DJ1".equals(contexto.getEsquema().getNombre()) && ((String)registro.get("kTipoArchivo")).equals(tipoArchivo)) {
                		if (contexto.getRectifica().equals("S")) {
                			if ("S".equals((String)registro.get("Rectifica"))) {
                				List<SeccionLinea> lineas = generador.generarDetalleDJ1(registro, tipoArchivo, generador.getContexto().getConnection(), contexto.getPeriodicidad(), contexto.getEsquema().getRangoFechas().getFechaHasta()); 
                				lstSeccionLineasFinales.addAll(lineas);
                				contadorDJ1++;
                			}
                		} else {
                			if ("N".equals((String)registro.get("Rectifica"))) {
                				if (!S1_EVENTOREPORTERECTIFICA.contains((String)registro.get("S1_EventoReporte"))){
                					List<SeccionLinea> lineas = generador.generarDetalleDJ1(registro, tipoArchivo, generador.getContexto().getConnection(), contexto.getPeriodicidad(), contexto.getEsquema().getRangoFechas().getFechaHasta()); 
                					lstSeccionLineasFinales.addAll(lineas);
                					contadorDJ1++;
                				}
                			}
                		}
                	}else {
                		generador.generarDetalle(registro);
                	}   
                  
                } catch (SerializadorException ex) {
                    manejar(reporte, ex);
                }

                registro = lector.siguiente();
            }
           // generador.generarDetalle(registro);            
            if("DJ1".equals(contexto.getEsquema().getNombre())) {
            	generador.imprimirLineasDJ1(lstSeccionLineasFinales);
            }
            
            // Datadog: tiempo generacion archivo DJ1
            if ("DJ1".equals(contexto.getEsquema().getNombre())) {
            	StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_GENERACION, (System.currentTimeMillis() - init), "esquema:"+contexto.getEsquema().getNombre(), "tipo_archivo:"+tipoArchivo);
            }
            
            try {
                generador.generarResumen();
            } catch (SerializadorException ex) {
                manejar(reporte, ex);
            }

            long cantidadRegistros = lector.getCantidadRegistrosLeidos();
            if ("DJ1".equals(contexto.getEsquema().getNombre())) {
            	cantidadRegistros = contadorDJ1;
            }
            resumen.setGlosa("Finalizada");
            resumen.setTotalRegistrosGenerados(cantidadRegistros);

            LOGGER.info(
                    String.format("%d registros generados", cantidadRegistros));
                    
        } catch (Exception ex) {
            resumen.setGlosa("Interrumpida");
            LOGGER.log(Level.SEVERE, "Error durante proceso de generacion", ex);
        } finally {
            ResumenGeneracionBuilder.actualizarResumen(contexto, resumen);
        }
	}
    
    /**
     * Maneja la excepcion al generar registros.
     *
     * @param reporte
     *            El reporte.
     * @param ex
     *            La {@link Exception}.
     */
    private void manejar(final ReporteGeneracion reporte, final SerializadorException ex) {
        reporte.println("Error: " + ex.getCauseMessage());
    }

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de generacion."));

        final String rutaReporte = contexto.buscarRutaReporte();
        LOGGER.info("RUTA REPORTE: " + rutaReporte);
        try (final PrintStream out = new PrintStream(rutaReporte)) {
        	
            final ReporteGeneracion reporte = new ReporteGeneracion(out);
            reporte.printEncabezado(contexto);

            try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
                try {
                    generar(reporte);
                    //Se actualiza estado de proceso de la DJ
                    CambiarEstadoDJPreDeclaracion cambiarEstadoDJPreDeclaracion = new CambiarEstadoDJPreDeclaracion();
                    CambiarEstadoDJPreDeclaracion.InCambiarEstadoDJ inCambiarEstadoDJ = new CambiarEstadoDJPreDeclaracion.InCambiarEstadoDJ();
                    inCambiarEstadoDJ.Esquema = contexto.getEsquema().getNombre();
                    inCambiarEstadoDJ.Version = contexto.getEsquema().getVersion();
                    cambiarEstadoDJPreDeclaracion.logica(contexto.getConnection(), inCambiarEstadoDJ);
                } catch (Exception ex) {
                    final String msg = "Error al procesar generacion";
                    reporte.println(msg);
                    reporte.println(ex.toString());
                    LOGGER.log(Level.SEVERE, msg, ex);
                    throw new ProcesoException(msg, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            final String msg = "Error al abrir archivo de reporte";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
    }

}
