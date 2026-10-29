package cl.bcs.fondomutuo.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.fondomutuo.ProcesarComisionesNN;
import cl.bcs.fondomutuo.ProcesarComisionesNN.EntradaProcesarComisionesNN;
import cl.bcs.fondomutuo.ProcesarComisionesNN.SalidaProcesarComisionesNN;
import cl.bcs.fondomutuo.ProcesarComisionesNN.Cartera;
import cl.bcs.fondomutuo.ProcesarComisionesNN.DocumentoPorFolio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;


public class BcsJobProcesarComisionesNN extends BcsJob implements JavaCall {
	
	 	AtomicBoolean running = new AtomicBoolean(false);
	    public String IdTipoPrograma = "ComisionesNN";
	    private final Log log = new Log("FM.BcsJobProcesarComisionesNN");
	    private final String SQL_UPDATE_ID_PROGRAMA =  loadResourceToString("cl/bcs/facturacion/jobs/sql/ComprobanteEmisionMasiva_update.sql");
		
		public static class ActualizarPrograma{
			public String RutaLog;
			public Long IdPrograma;
			
			public ActualizarPrograma(String RutaLog,Long IdPrograma){
				this.RutaLog = RutaLog;
				this.IdPrograma = IdPrograma;
			}
		}
		

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("*ProcesarComisionesNN* Inicio de Job");
		running.set(true);

		try {	
			/*
			Long idEmpresa = 1L;
			Long idUsuario = 3L;
			String idModulo = "FMM";
			String 	fechaDesde = "2022-01-01";
		    String  fechaHasta = "2022-03-31";
			List<Cartera> comisionesList = new ArrayList<Cartera>();
			String comisiones = "1531974,1534108,1531990,1533921,1530791,1532244,1530640,1536906,1526930,1537445,1537608,1532208,1532864,1526914,1534032,1530647,1535260,1531324,1537414,1530636,1530804,1530676,1531317,1537571,1530973,1532242,1535223,1536037,1530085,1527174,1536057,1526822,1527176,1536974,1537612,1536905,1530609,1533920,1531249";
			*/
			List<Cartera> comisionesList = new ArrayList<Cartera>();
			Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
			String idModulo = this.getParam("IdModulo"); 
			String fechaDesde = this.getParam("FechaDesde");
		    String fechaHasta = this.getParam("FechaHasta");
		    String comisiones = this.getParam("Comisiones");
		    
		    String parRuta =  Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
			SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmmss");
			String P_OUT_NOMBREARCHIVO="FMM/LOG_PROCESAR_COMISIONES_NN_MASIVO_"+idUsuario+"_"+fm.format(new java.util.Date())+".txt";
			String P_OUT_RUTAARCHIVOLOG=parRuta+"/"+P_OUT_NOMBREARCHIVO;
			
			File folder = new File(parRuta+"/FMM");
			if(!folder.exists()){
				folder.mkdirs();
			}
			File archivoLog = new File (P_OUT_RUTAARCHIVOLOG);
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(archivoLog));
			 
            String[] comisionesNN = comisiones.split(",");

            if (comisionesNN.length == 0) {
            	log.write("*ProcesarComisionesNN* No se enviaron cargas para procesar");
            	bw.write( "*ProcesarComisionesNN* No se enviaron cargas para procesar\n");
                running.set(false);
                return false;
            }
            
        	for (String id : comisionesNN) {
				Cartera cartera = new Cartera();
				cartera.IdCartera =  Long.parseLong(id);
				comisionesList.add(cartera);				
			}
        	log.write("*ProcesarComisionesNN* idEmpresa: " + idEmpresa);
            bw.write( "*ProcesarComisionesNN* idEmpresa: " + idEmpresa + "\n");
            log.write("*ProcesarComisionesNN* idUsuario: " + idUsuario);
            bw.write( "*ProcesarComisionesNN* idUsuario: " + idUsuario + "\n");

            log.write( "*ProcesarComisionesNN* Preparando llamada");
            bw.write( "*ProcesarComisionesNN* Preparando llamada\n");
            
            ProcesarComisionesNN procesarComisionesNN = new ProcesarComisionesNN();
            EntradaProcesarComisionesNN entrada = new EntradaProcesarComisionesNN();
            SalidaProcesarComisionesNN salida = new SalidaProcesarComisionesNN();
            entrada.p_IdEmpresa = idEmpresa;
            entrada.p_IdUsuario = idUsuario;
            entrada.p_IdModulo = idModulo;
            entrada.p_FechaConsultaDesde	= fechaDesde;
            entrada.p_FechaConsultaHasta	= fechaHasta;
            entrada.p_Solicitudes = comisionesList;
            entrada.p_EsMasivo = "S";
            log.write( "*ProcesarComisionesNN* Se ejecuta llamada con "+ comisionesList.size() +" registros a procesar");
            bw.write( "*ProcesarComisionesNN* Se ejecuta llamada con "+ comisionesList.size() + " registros a procesar\n");
            
            
            salida = procesarComisionesNN.logica(con, entrada);

        	log.write( "*ProcesarComisionesNN* Fin ejecucion de servicio");
        	bw.write( "*ProcesarComisionesNN* Fin ejecucion de servicio\n");
           	
    	    SQL sql=new SQL();
    		if(new Long(0).compareTo(salida.P_OUT_ERRORESCONTADOR) == 0){
    			con.commit();
   			}else{
   				con.rollback();
   			}
   			if(P_OUT_NOMBREARCHIVO != null){
    			List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
    			actualizar.add(new ActualizarPrograma(P_OUT_NOMBREARCHIVO,this.getJobId()));
    			sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
    			 
    			con.commit();
    			log.write("RESULTADO*****");
    			bw.write("RESULTADO*****\n");
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					bw.write("SE ENCONTRARON ERRORES EN EL PROCESO\n");
					for(MensajeSalida a : salida.P_OUT_MENSAJES){
						if (a.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
							log.write(a.descripcion+"\n");
							bw.write(a.descripcion+"\n");
						}
					}
				} else {
					if(salida.P_OUT_ADVERTENCIASCONTADOR > 0) {
						List<MensajeSalida> advertencias = salida.P_OUT_MENSAJES.stream().filter(mensaje -> mensaje.tipo.equals("ADVERTENCIA")).collect(Collectors.toList());
						//salida.P_OUT_MENSAJES.addAll(advertencias);
						log.write("LAS SIGUIENTES COMISIONES NO SE PUDIERON PROCESAR");
						bw.write("LAS SIGUIENTES COMISIONES NO SE PUDIERON PROCESAR\n");
						for(MensajeSalida a : advertencias ){
							log.write("Folio Cartera: " + a.id + " - DocumentoIdentidad: " + salida.P_OUT_DOCUMENTOS_POR_FOLIO.stream().filter(sal -> sal.FolioCartera.equals(a.id)).findFirst().orElse(new DocumentoPorFolio()).DocumentoIdentidad + " || " + a.descripcion);
							bw.write("Folio Cartera: " + a.id + " - DocumentoIdentidad: " + salida.P_OUT_DOCUMENTOS_POR_FOLIO.stream().filter(sal -> sal.FolioCartera.equals(a.id)).findFirst().orElse(new DocumentoPorFolio()).DocumentoIdentidad + " || " + a.descripcion + "\n");
						}	 

					} else {
						 salida.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
						 for(MensajeSalida a : salida.P_OUT_MENSAJES){
							 log.write(a.descripcion);
							 bw.write(a.descripcion+"\n");
						 }						
					}
	 
				}
				log.write("FIN RESULTADO*****");
    			bw.write("FIN RESULTADO*****");
				bw.close();
				return true;
   			}else{
   				return false;
   			}

		} catch (Exception e) {
			log.write("Error", e);
			con.rollback();
			throw e;
		} finally {
			running.set(false);
		}
	}

	@Override
	public void onError(Throwable t) {
    	log.write(":ProcesadorTransacciones:  ****** ERROR  *******", t);
        t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getBOLSAConnection();
    	BcsJobProcesarComisionesNN p = new BcsJobProcesarComisionesNN();
        try {
            p.transaction(con);
            con.rollback();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
