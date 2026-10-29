package cl.bcs.custodia.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.custodia.ValorizarCustodiaUIbatch;
import cl.bcs.custodia.ValorizarCustodiaUIbatch.InValorizarCustodiaUIbatch;
import cl.bcs.custodia.ValorizarCustodiaUIbatch.OutValorizarCustodiaUIbatch;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class ValorizarCustodiaUIbatchJob extends BcsJob implements JavaCall{
	private final Log log = new Log("ValorizarCustodiaUIbatchJob"); 
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		log.write("Inicia ejecucion del job");
		Long 	parIdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		Long 	parIdUsuario=Long.parseLong(this.getParam("IdUsuario"));
		String 	parIdModulo = this.getParam("IdModulo");
		String	parFechaSistema = this.getParam("FechaSistema");
		Long	parIdPais = Long.parseLong(this.getParam("IdPais"));
		//--------------------------------------------------
		String  parSimular = this.getParam("Simular");
		Long 	parIdPortafolio = Long.parseLong(this.getParam("IdPortafolio"));
		Long 	parIdCliente = Long.parseLong(this.getParam("IdCliente"));
		String 	parValorizaCarteraRV = this.getParam("ValorizaCarteraRV");
		String 	parValorizaCarteraFFMM = this.getParam("ValorizaCarteraFFMM");
		String	parValorizaCarteraRF = this.getParam("ValorizaCarteraRF");
		String	parValorizaCarteraIF = this.getParam("ValorizaCarteraIF");
		String	parValorizaPrestamosEmprestitos = this.getParam("ValorizaPrestamosEmprestitos");
		String	parValorizaGarantiasRVRFIF = this.getParam("ValorizaGarantiasRVRFIF");
		String	parValorizaSimultaneas = this.getParam("ValorizaSimultaneas");
		String	parValorizaCupones = this.getParam("ValorizaCupones");
		String 	parValorizaPactos = this.getParam("ValorizaPactos");
		String 	parValorizaFuturos = this.getParam("ValorizarCarteraFuturos");
		String 	parValorizaOpciones = this.getParam("ValorizarCarteraOpciones");
		
		ValorizarCustodiaUIbatch valorizarCustodiaUIbatch = new ValorizarCustodiaUIbatch();	
		InValorizarCustodiaUIbatch entrada = new InValorizarCustodiaUIbatch();
		
		entrada.p_IdEmpresa = parIdEmpresa;	
		entrada.p_IdUsuario = parIdUsuario;
		entrada.p_IdModulo = parIdModulo;
		entrada.p_FechaSistema = parFechaSistema;
		entrada.p_IdPais = parIdPais;
		entrada.p_Simular = parSimular;
		entrada.p_IdPortafolio = parIdPortafolio;
		entrada.p_IdCliente = parIdCliente;
		entrada.p_ValorizaCarteraRV = parValorizaCarteraRV;
		entrada.p_ValorizaCarteraFFMM = parValorizaCarteraFFMM;
		entrada.p_ValorizaCarteraRF = parValorizaCarteraRF;
		entrada.p_ValorizaCarteraIF = parValorizaCarteraIF;
		entrada.p_ValorizaPrestamosEmprestitos = parValorizaPrestamosEmprestitos;
		entrada.p_ValorizaGarantiasRVRFIF = parValorizaGarantiasRVRFIF;
		entrada.p_ValorizaSimultaneas = parValorizaSimultaneas;
		entrada.p_ValorizaCupones = parValorizaCupones;
		entrada.p_ValorizaPactos = parValorizaPactos;
		entrada.p_ValorizaFuturos = parValorizaFuturos;
		entrada.p_ValorizaOpciones = parValorizaOpciones;
		log.writeAsJson("Parametros de entrada del servicio",entrada);
		try{
			OutValorizarCustodiaUIbatch res = valorizarCustodiaUIbatch.logica(con,entrada);
			log.writeAsJson("JOB, SALIDA: ",res);
			SQL sql=new SQL();
			boolean resultado = false;
			if(new Long(0).compareTo(res.P_OUT_ERRORESCONTADOR) == 0){
				resultado=true;
			}
			//ESCRITURA EN ARCHIVO
			String rutaBaseAcrhivo = Utils.getConfiguracion(con, Utils.getSchema(), parIdEmpresa, "PLA.RutaBaseArchivos");
			String rutaRelativaArchivo = "VAL/ValorizacionUI" + Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmss") + ".csv";
			String rutaAbsolutaArchivo = (rutaBaseAcrhivo+"/"+rutaRelativaArchivo).replace("//", "/");
			log.write("ruta archivo: "+rutaAbsolutaArchivo);
			File folder = new File(rutaBaseAcrhivo+"/VAL");
			if(!folder.exists()){
				folder.mkdirs();
			}
			FileOutputStream os = new FileOutputStream(rutaAbsolutaArchivo);
			os.write(0xef);
			os.write(0xbb);
			os.write(0xbf);
			CSVWriter EscribeCSV=new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');
			boolean archivoEscrito = false;
			try{
				String [] FilaEscribir= new String[]{"Hora","Accion","Resultado","Detalle"};
				EscribeCSV.writeNext(FilaEscribir);
				int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
				Fecha.setLocalOffSet(SegundosZonaHoraria);
				for(OutValorizarCustodiaUIbatch.DetalleEjecucion linea : res.P_OUT_DETALLE_EJEUCION){
					FilaEscribir = new String[]{linea.Hora,linea.Accion,linea.Resultado,linea.Detalle};
					EscribeCSV.writeNext(FilaEscribir);
				}
				if(!resultado){
					FilaEscribir = new String[]{Utils.getCurrentTimeStamp(),"JOB","ERROR","Error en ejecucion de servicio invocado por job"};
				}
				archivoEscrito = true;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					EscribeCSV.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(archivoEscrito){
				log.write("Escribiendo Archivo");
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(rutaRelativaArchivo,this.getJobId()));
				sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
				con.commit();
			}
			if(archivoEscrito && resultado){
				return true;
			}
			return false;
		}catch(Exception e){
			log.write("JOB, excepcion "+e.getStackTrace());
			con.rollback();
			e.printStackTrace();
			throw e;
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
		// TODO Auto-generated method stub
		return null;
	}
}