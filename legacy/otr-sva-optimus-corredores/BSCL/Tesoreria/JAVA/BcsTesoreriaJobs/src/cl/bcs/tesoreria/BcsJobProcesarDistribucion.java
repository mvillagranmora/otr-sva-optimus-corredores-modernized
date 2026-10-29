package cl.bcs.tesoreria;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.opencsv.CSVWriter;

import cl.bcs.APV.ValidarMandatosPAC;
import cl.bcs.custodia.jobs.ValorizarCustodiaUIbatchJob.ActualizarPrograma;
import cl.bcs.fondomutuo.ProcesarCargaSolicitudesAporteRescate.EntradaProcesarCargaSolicitudesAporteRescate;
import cl.bcs.fondomutuo.ProcesarCargaSolicitudesAporteRescate.SalidaProcesarCargaSolicitudesAporteRescate;
import cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.tesoreria.ProcesarDistribucion.SalidaRecaudacionTraspaso;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.tesoreria.pojo.Traspaso;

public class BcsJobProcesarDistribucion extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "RENDICION_PAC";
    private final Log log = new Log("FM.BcsJobProcesarDistribucion");
    private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
    private final String SQL_UPDATE_CONFIGURACION = "UPDATE \"BSCL.Plataforma.BD::PLA.Configuracion\" SET \"Valor\" = 'S' WHERE \"CodigoParametro\" = :CodigoParametro;";
    
    public static class UpdateConfiguracion{
		public String CodigoParametro;
		
		public UpdateConfiguracion(String CodigoParametro){
			this.CodigoParametro = CodigoParametro;
		}
	}
    
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        log.write("*RENDICIONPAC* Inicio de Job");
        running.set(true);
        
       
        try{
        	SQL sql=new SQL();
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "TES";
            String simular = "";
            int cantidadErrores = 0;
            
	            
            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
            simular = this.getParam("Simular");  
            
            ProcesarDistribucion procesarDistribucion = new ProcesarDistribucion();
            ProcesarDistribucion.EntradaRecaudacionTraspaso entradaProcesarDistribucion = new ProcesarDistribucion.EntradaRecaudacionTraspaso();
            entradaProcesarDistribucion.p_IdEmpresa = idEmpresa;
            entradaProcesarDistribucion.p_IdUsuario = idUsuario;
            entradaProcesarDistribucion.p_IdModulo = modulo;
            entradaProcesarDistribucion.p_simular = simular;
      
            /***************************************************
             * 	PROCESO DE DISTRIBUCION   *
             ***************************************************/
            
            SalidaRecaudacionTraspaso salida = procesarDistribucion.logica(conn, entradaProcesarDistribucion);
                       
            /***************************************************
             * 	ALMACENAR MANDATOS PROCESADOS Y NO PROCESADOS  *
             ***************************************************/
            
            ValidarMandatosPAC noProcesados = new ValidarMandatosPAC();
            noProcesados.logica(conn, salida, idEmpresa, idUsuario);
            
                       
            String rutaBaseArchivo = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
           
            String rutaRelativaArchivo = "tes/RendicionPAC" + Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(conn, idEmpresa),"yyyyMMddHHmmss") + ".csv";
            String rutaAbsolutaArchivo = (rutaBaseArchivo+"/"+rutaRelativaArchivo).replace("//", "/");

            

            FileOutputStream os = new FileOutputStream(rutaAbsolutaArchivo);
    		os.write(0xef);
    		os.write(0xbb);
    		os.write(0xbf);
    		CSVWriter ArchivoCSV = new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');
            try {
	            String [] FilaEscribir= new String[]{"id","tipo","codigo","descripcion"};
	            ArchivoCSV.writeNext(FilaEscribir);
	            for (MensajeSalida mensaje : salida.P_OUT_MENSAJES) {
	            	if (mensaje.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) cantidadErrores++;
	            	FilaEscribir = new String[]{mensaje.id.toString(),mensaje.tipo,mensaje.codigo,mensaje.descripcion};
	            	ArchivoCSV.writeNext(FilaEscribir);
	            }
            } catch (Exception ex) {
            	running.set(false);
            	throw ex;
            } finally {
            	try {
            		ArchivoCSV.close();
            	} catch (Exception ex) {
            		
            	}
            }
            
            List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
			actualizar.add(new ActualizarPrograma(rutaRelativaArchivo,this.getJobId()));
			sql.batch(conn,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
			
			conn.commit();
        } catch (Exception e) {
            log.write("Error",e);
            conn.rollback();
            throw e;
        } finally {       	
            running.set(false);
        }
        
        ValidaEjecucionProcesoRecPAC ValidaEjecucion = new ValidaEjecucionProcesoRecPAC();
		ValidaEjecucionProcesoRecPAC.ValidaEjecucionProcesoRecPACEntrada EntradaValidaEjecucionProcesoRecPAC = new ValidaEjecucionProcesoRecPAC.ValidaEjecucionProcesoRecPACEntrada();
		
		EntradaValidaEjecucionProcesoRecPAC.p_IdEmpresa = 1L;
		EntradaValidaEjecucionProcesoRecPAC.p_ValorNuevo= "N";
        
    	ValidaEjecucionProcesoRecPAC.ValidaEjecucionProcesoRecPACSalida SalidaValidaEjecucionProcesoRecPAC = ValidaEjecucion.logica(conn, EntradaValidaEjecucionProcesoRecPAC);
    	conn.commit();
    	
        log.write("*RENDICIONPAC* Fin de Job");
        running.set(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(":ProcesadorTransacciones:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getUATConnection();
    	BcsJobProcesarDistribucion p = new BcsJobProcesarDistribucion();
        try {
            p.transaction(con);
            //con.commit();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
