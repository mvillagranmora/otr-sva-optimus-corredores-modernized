package cl.bcs.APV.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CargaMasivaPrevired;
import cl.bcs.APV.CargaMasivaPrevired.ParametrosSalidaServicio;
import cl.bcs.APV.CargaMasivaPrevired.Parametros_Entrada;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired;

public class BcsJobCargaMasivaPrevired extends BcsJob implements JavaCall {
	private final Logger logger = LogManager.getLogger(BcsJobCargaMasivaPrevired.class);
	
	private final String SQL_UPDATE_ID_PROGRAMA =  loadResourceToString("cl/bcs/APV/jobs/sql/BcsJobCargaMasivaPrevired_UpdatePrograma.sql");
	
	public static class ActualizarPrograma extends BcsPojo{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn);
	}

	@Override
	public void onError(Throwable t) {
        logger.info("****** ERROR  *******");
        logger.info(t);
        t.printStackTrace();

	}

	@Override
	public void pause() {
		
	}

	public boolean logica(Connection con ) throws Exception {
		logger.info("INICIO::BcsJobCargaMasivaPrevired"); 
		Long inicio = System.currentTimeMillis();
		
		String parametrosEntrada = this.getParam("p_parametros");
		
		Parametros_Entrada paramsEntrada = prepararParametrosEntradaCargaPrevired(parametrosEntrada);
		
		String rutaAbsolutaBase =  Utils.getConfiguracion(con, Utils.getSchema(), paramsEntrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
		SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmmss");
		String P_OUT_NOMBREARCHIVO="APV/LOG_CARGA_MASIVA_PREVIRED_"+paramsEntrada.p_IdUsuario+"_"+fm.format(new java.util.Date())+".txt";
		String P_OUT_RUTAARCHIVOSALIDA=rutaAbsolutaBase+"/"+P_OUT_NOMBREARCHIVO;
		
		File archivoLog = new File (P_OUT_RUTAARCHIVOSALIDA);
		BufferedWriter bw;		
		bw = new BufferedWriter(new FileWriter(archivoLog));
		
		try {
			File folder = new File(rutaAbsolutaBase+"/APV");
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			CargaMasivaPrevired cargaMasivaPrevired = new CargaMasivaPrevired();
			
			logger.info("EJECUCION SERVICIO DE CARGA MASIVA PREVIRED*****");
			bw.write("EJECUCION SERVICIO DE CARGA MASIVA PREVIRED*****\n");
			
			ParametrosSalidaServicio salidaCargaMasiva = cargaMasivaPrevired.logica(con, paramsEntrada);
			SQL sql=new SQL();

			logger.info("RESULTADO DE CARGA MASIVA PREVIRED*****");
			bw.write("RESULTADO DE CARGA MASIVA PREVIRED*****\n");
			
			if(P_OUT_NOMBREARCHIVO != null){
    			List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
    			actualizar.add(new ActualizarPrograma(P_OUT_NOMBREARCHIVO,this.getJobId()));
    			sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
    			
    			for(MensajeSalida a : salidaCargaMasiva.P_OUT_MENSAJES){
					logger.info("{ id: "+ a.id + ", tipo: "+ a.tipo + ", codigo: " + a.codigo+ ", descripcion: " + a.descripcion + " }");
					bw.write("{ id: "+ a.id + ", tipo: "+ a.tipo + ", codigo: " + a.codigo+ ", descripcion: " + a.descripcion + " }\n");
				}
			}
			logger.info("FIN RESULTADO*****");
			bw.write("FIN RESULTADO*****");
			
			Long fin = System.currentTimeMillis();
			logger.info("FIN::BcsJobCargaMasivaPrevired{}", fin - inicio);
			return true;
		} catch(Exception er) {
			con.rollback();
			er.printStackTrace();
		} finally {
			if(bw != null) {
				bw.close();
			}
		}
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobCargaMasivaPrevired{}", fin - inicio);
		return true;
	}
	
	private Parametros_Entrada prepararParametrosEntradaCargaPrevired(String parametrosEntrada) {
		logger.info("INICIO::BcsJobCargaMasivaPrevired.prepararParametrosEntradaCargaPrevired"); 
		Long inicio = System.currentTimeMillis();
		
		Parametros_Entrada entrada = new Parametros_Entrada();
		JsonParser parser     = new JsonParser();
		JsonObject jsonEntrada  = parser.parse(parametrosEntrada).getAsJsonObject().getAsJsonObject("p_parametros");
		String stringParametros = jsonEntrada.toString();
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
    	
		entrada = gson.fromJson(stringParametros, Parametros_Entrada.class);
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::BcsJobCargaMasivaPrevired.prepararParametrosEntradaCargaPrevired{}", fin - inicio);
		return entrada;
	}

	//DEBUG
    public static void main(String[] args) throws SQLException {
        System.out.println("== START ==");
    	BcsJobCargaMasivaPrevired demo=new BcsJobCargaMasivaPrevired();
        Connection con = PlataformaUtils.getBOLSAConnection();
        try {
            	JsonParser parser     = new JsonParser();
			
        	demo.transaction(con);
            con.rollback();
        } catch (Throwable er) {
            er.printStackTrace();
            con.rollback();
        }
        System.out.println("== END ==");       
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}