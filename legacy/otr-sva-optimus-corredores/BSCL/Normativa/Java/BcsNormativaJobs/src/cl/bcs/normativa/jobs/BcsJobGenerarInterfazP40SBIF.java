package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.JsonObject;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIFBase;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF_Scotia;
import cl.bcs.normativa.interfaces.ClienteConfig;
import cl.bcs.normativa.interfaces.ClienteConfigFactory;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;

public class BcsJobGenerarInterfazP40SBIF extends BcsJob implements JavaCall {
	/*
	 * Job para generar servicio de Interfaz P40 SBIF
	 */
	private final Log log = new Log("BcsJobGenerarInterfazP40SBIF");
	private final Logger logger = LogManager.getLogger(BcsJobGenerarInterfazP40SBIF.class);
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	private final String SQL_CODIGO_CORREDORA = loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarInterfazP40_SelectCorredoraCodigos.sql");

	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		public String 	Comentario;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma, String Comentario){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog     = RutaLog;
			this.IdPrograma  = IdPrograma; 
			this.Comentario  = Comentario; 
		}
	}

	public static class DatoCodigoCorredora {
		public String 		CodigoCorredora;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		SQL sql = new SQL();
		HashMap<String, Object> paramsRut = new HashMap<String, Object>();
		paramsRut.put("IdEmpresa", Long.parseLong(this.getParam("IdEmpresa")));
		List<DatoCodigoCorredora> datoCodigo 	= sql.query(con, SQL_CODIGO_CORREDORA, paramsRut, DatoCodigoCorredora.class); 
		String testCodigo = datoCodigo.get(0).CodigoCorredora;
		logger.debug(testCodigo);
		logger.info(testCodigo);
		String idEmpresa = this.getParam("IdEmpresa");
        String idUsuario = this.getParam("IdUsuario");
		String rutaBaseArchivos = Utils.getConfiguracion(con,Utils.getSchema(),Long.parseLong(this.getParam("IdEmpresa")),"PLA.RutaBaseArchivos");
		GenerarInterfazP40SBIFBase generarInterfazP40 = selectP40Implementation(datoCodigo.get(0).CodigoCorredora);
		EntradaGenerarInterfazP40SBIF entradap40 = new EntradaGenerarInterfazP40SBIF();
		//Nota: la clase ClienteConfig configura a la entradap40 según el código de corredora que se envía
		ClienteConfig clienteConfig = ClienteConfigFactory.getClienteConfig(datoCodigo.get(0).CodigoCorredora);
		clienteConfig.configurarParametros(entradap40, idEmpresa, idUsuario, rutaBaseArchivos);

		// IMPBESTOPT-901
		DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateformat = this.getParam("FechaUI");
		OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
		entradap40.p_Fecha  = dateTime.format(formatoFecha);		
		
		try{
			SalidaGenerarInterfazP40SBIF out = generarInterfazP40.logica(con, entradap40);
			if(!out.P_OUT_ARCHIVOS.isEmpty()){
				String sufijo = datoCodigo.get(0).CodigoCorredora;
				String zip = "P40_" + sufijo + ".zip";
				//String sufijo = Utils.getConfiguracion(con, Utils.getSchema(), entradap40.p_IdEmpresa, "PLA.SufijoArchivos"); forma antigua de obtener el sufijo para el archivo zip

				ZipUtil.generateMultipleZip(out.P_OUT_ARCHIVOS, entradap40.p_Ruta + "/" + zip);   
				
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				
				actualizar.add(new ActualizarPrograma(zip,out.P_OUT_NOMBRE_ARCHIVO_LOG,this.getJobId(), out.P_OUT_COMENTARIO)); 
				sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
				con.commit();
				return true;
			}else{
				return false;
			}			
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
	}

	private GenerarInterfazP40SBIFBase selectP40Implementation(String codigoCorredora) {
        switch (codigoCorredora) {
            case "85":
                return new GenerarInterfazP40SBIF_Scotia();
            default:
                return new GenerarInterfazP40SBIF();
        }
    }

	@Override
	public void onError(Throwable t) {
        log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
	}

	@Override
	public void pause() {
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}	

}
