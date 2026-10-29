package cl.bcs.normativa.generacircular1064;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
//import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.opencsv.CSVWriter;
 
import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.ResumenGeneracion;
//import cl.bcs.normativa.exportacion.ProcesoExportacion;
import cl.bcs.normativa.generacion.ProcesoGeneracion;
import cl.bcs.normativa.generacion.ResumenGeneracionBuilder;
import cl.bcs.normativa.lectores.Lector;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
//import cl.bcs.scheduler.pojo.Programa;
import cl.bcs.utils.PropertiesUtil;


public class ProcesoGenCir1064 implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoGeneracion.class.getName());

    private static DateFormat FORMATOFECHA;
    private static String SEPARADORDECIMAL;
    private static String SEPARADORLINEAS;
    private static Long IDSOLICITUDCIRCULAR;
    private static Locale LOCALE;

    private static String CrearSolicitudCircularPauta(final long p_IdUsuario,final long p_IdEmpresa 
    	,String p_Observaciones,String p_IdTipoDocumento, String p_FechaInicio,
    	String p_FechaTermino, String p_FechaLimite,String p_FechaPresentacion,
    	String p_NombreArchivo,String p_RutaArchivo, final long p_NumRegistros, ContextoGenCir1064 contexto) {

        String sqlCall = "";

        String PROCEDURE_NAME_CREARSOLCIRPAUTA = "BSCL.Normativa.BD.Servicios.CIR1064::CrearSolicitudCircularPauta";
        p_NombreArchivo = "'" + p_NombreArchivo + "'";
        p_RutaArchivo = "'" + p_RutaArchivo + "'";
        p_Observaciones = "'" + p_Observaciones + "'";
        p_IdTipoDocumento =  "'" + p_IdTipoDocumento + "'";
        p_FechaInicio = "'" + p_FechaInicio + "'";
        p_FechaTermino = "'" + p_FechaTermino + "'";
        p_FechaLimite = "'" + p_FechaLimite + "'";
        p_FechaPresentacion = "'" + p_FechaPresentacion + "'";
        
        
        sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
                getQuotedString(PROCEDURE_NAME_CREARSOLCIRPAUTA),p_IdUsuario,p_IdEmpresa,p_Observaciones,
                p_IdTipoDocumento,p_FechaInicio,p_FechaTermino,p_FechaLimite,p_FechaPresentacion, p_NombreArchivo,
                p_RutaArchivo, p_NumRegistros, contexto.getIdProceso());
        System.out.println(sqlCall);
        Connection conn = contexto.getConnection();

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            conn.setAutoCommit(false);

            if (cstmt.execute()) {
                conn.commit();

                try (final ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        LOGGER.log(Level.SEVERE, "Proceso de creacion solicitud circular pauta exitoso");
                        return "OK";
                    } else {
                        LOGGER.log(Level.SEVERE,
                                "Error durante proceso de creacion solicitud circular pauta, no RS");
                        return "NOK";

                    }

                } catch (Exception ex) {

                    LOGGER.log(Level.SEVERE, "Error durante proceso de creacion solicitud circular pauta",
                            ex);
                    return "NOK";
                }

            } else {

                LOGGER.log(Level.SEVERE, "Error no se pudo ejecutar servicio creacion solicitud circular pauta");
                return "NOK";
            }

        } catch (Exception ex) {

            LOGGER.log(Level.SEVERE, "Error durante proceso de creacion solicitud circular pauta", ex);
            return "NOK";
        }

    }


    private static String formatoCampo(Comparable<?> val, final Campo campo) {

        String formatoCampo = "";

        final String tipoDato = campo.getTipoDato();
        if (tipoDato.equals("DATE_FILTER") || tipoDato.equals("DATE")) {
            formatoCampo = FORMATOFECHA.format(val);
        } else
            if (tipoDato.equals("AMOUNT") || tipoDato.equals("DECIMAL") || tipoDato.equals("QTY")) {
            formatoCampo = String.format(LOCALE, "%." + campo.getCantidadDecimales() + "f",
                    ((Number) val).doubleValue());
        } else {
            formatoCampo = val.toString();
        }
        return formatoCampo;
    }

    private static String getQuotedString(final String name) {
        return "\"" + name + "\"";
    }
    private static String getComillasString(final String name) {
        return "\'" + name + "\'";
    }


    public static Proceso newInstance(final Connection conn, final Map<String, String> map,
            final long idProceso, final DataSource ds) throws SQLException {
        final Properties props = PropertiesUtil.newProperties(map);
        return new ProcesoGenCir1064(conn, props, idProceso, ds);
       
    }

    /**
     * Contexto de exportaci&oacute;n de datos.
     */
    private ContextoGenCir1064 contexto;


    public ProcesoGenCir1064(final Connection conn, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException {
        this.contexto = new ContextoGenCir1064(conn, props, idProceso, ds);
    }



    private void exportarCir1064() throws SQLException {

        final List<Campo> campos = contexto.getEsquema().getCampos();


        String nombreArchivo = "";
        
        nombreArchivo = LocalizadorArchivo.buscarNombreArchivo(
                contexto.getDocumento().getIdTipoDocumento().toString(), contexto.getIdProceso());

        // String rutaNombreResultado =
        //String.format("C:\\ArchivosPrueba\\"+nombreArchivo);
        String rutaNombreResultado = LocalizadorArchivo.buscarRutaArchivo(contexto.getConnection(),
                contexto.getSchema(), contexto.getDocumento().getIdTipoDocumento().toString(),
                contexto.getDocumento(), contexto.getIdProceso());

        FORMATOFECHA = new SimpleDateFormat(
                PropertiesUtil.getRequiredString(contexto.getParametros(), "simboloFecha"));
        SEPARADORLINEAS = PropertiesUtil.getRequiredString(contexto.getParametros(),
                "separadorListas");
        SEPARADORDECIMAL = PropertiesUtil.getRequiredString(contexto.getParametros(),
                "simboloDecimal");
                
       /* IDSOLICITUDCIRCULAR = PropertiesUtil.getRequiredLong(contexto.getParametros(),
                "IdSolicitudCircular");*/

        if (SEPARADORLINEAS.isEmpty())
            SEPARADORLINEAS = ",";
        if (SEPARADORDECIMAL.isEmpty())
            SEPARADORDECIMAL = ".";
        
        

        LOCALE = (SEPARADORDECIMAL.equals(".")) ? new Locale("EN") : new Locale("ES");


        // obtener nombre sp desde bd

        String ADD_PROGRAM_PROCEDURE_NAME = "BSCL.Normativa.BD.Servicios.CIR1064::ConsultarOperaciones1064";

        ResumenGeneracion resumen;
        resumen = ResumenGeneracionBuilder.crearResumen(contexto, rutaNombreResultado, 0);

        // Obtener sp y filtros

        String nombreproc = "";
        nombreproc = ADD_PROGRAM_PROCEDURE_NAME ;

        String sqlCall = "";
        
        Connection conn = contexto.getConnection();

        sqlCall = String.format("{CALL %s.%s(%s)}", getQuotedString("BSCL"),getQuotedString(nombreproc),1);
        

        System.out.println(sqlCall);

        LOGGER.log(Level.SEVERE, sqlCall);
        LOGGER.log(Level.SEVERE, "1");
        LOGGER.log(Level.SEVERE,rutaNombreResultado );
   
        final boolean autoCommit = conn.getAutoCommit();
    
        

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            conn.setAutoCommit(false);

              LOGGER.log(Level.SEVERE, rutaNombreResultado );
            
            System.out.println(sqlCall);
            System.out.println(contexto.getDocumento().getId());
           // System.out.println(IDSOLICITUDCIRCULAR);

            if (cstmt.execute()) {
            	 LOGGER.log(Level.SEVERE, "3");
            	 LOGGER.log(Level.SEVERE, rutaNombreResultado );
                conn.commit();

                try (final ResultSet rs = cstmt.getResultSet()) {
                    try (Lector lector = LectorBuilder.newLectorServicio(contexto, sqlCall, rs); 
                    		
                            final FileWriter fw = new FileWriter(rutaNombreResultado);
                            // CSVWriter writer = new
                            // CSVWriter(fw,separadorLineasChar);
                            CSVWriter writer = new CSVWriter(
                                    new OutputStreamWriter(
                                            new FileOutputStream(rutaNombreResultado), "UTF-8"),
                                    SEPARADORLINEAS.toCharArray()[0]);) {

                        Registro registro = lector.siguiente();
                        boolean withHeader = true;

                        while (registro != null) {
                            if (withHeader) {
                                final String[] entries = new String[campos.size()];
                                for (int i = 0; i < campos.size(); i++) {
                                    entries[i] = campos.get(i).getNombre();
                                }

                                writer.writeNext(entries);
                                withHeader = false;
                            }

                            final String[] entries = new String[campos.size()];
                            for (int i = 0; i < campos.size(); i++) {
                                Comparable<?> val = registro.get(campos.get(i).getNombre());
                                entries[i] = val == null ? null : formatoCampo(val, campos.get(i));// val.toString();
                            }
                            writer.writeNext(entries, false);

                            registro = lector.siguiente();
                        }

                        
                        
                  //crear solicitud de circular por pauta
                        
                  CrearSolicitudCircularPauta(14,1,"Solicitud por Pauta","CIR1064","","","","",nombreArchivo,rutaNombreResultado,lector.getCantidadRegistrosLeidos(),contexto);

                     
                    } catch (Exception ex) {
                        resumen.setGlosa("Interrumpida");
                        LOGGER.log(Level.SEVERE, "Error durante proceso de generacion CIR1064", ex);
                        LOGGER.log(Level.SEVERE, ex.toString());
                        LOGGER.log(Level.SEVERE,rutaNombreResultado);
                    } finally {
                         ResumenGeneracionBuilder.actualizarResumen(contexto,resumen);
                    }
                }
            } else {

            	resumen.setGlosa("Interrumpida");
                LOGGER.log(Level.SEVERE, "Error durante proceso de generacion CIR1064");
            } // fin if (cstmt.execute())
        } finally {
            conn.setAutoCommit(autoCommit);
        }

        
        
        
    }

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso generacion circular 1064."));

        try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
            try {
            	exportarCir1064(); 
            } catch (SQLException ex) {
                final String msg = "Error durante proceso de generacion circular 1064";
                
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProcesoException(msg, ex);
            }
        }
    }

}
