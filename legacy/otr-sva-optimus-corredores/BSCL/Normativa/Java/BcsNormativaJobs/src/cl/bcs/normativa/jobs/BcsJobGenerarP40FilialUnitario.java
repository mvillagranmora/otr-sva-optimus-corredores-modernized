package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatoFilial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatosFFMM;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.EntradaGenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.SalidaArchivoFilial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.SalidaGenerarInterfazP40Filial;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Programa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BcsJobGenerarP40FilialUnitario  extends BcsJob implements JavaCall{
	/*
	 * Job para generar servicio de Interfaz P40 Filial Unitarias
	 */
	private final Log log = new Log("BcsJobGenerarInterfazP40FilialUnitarias");
	private final String SQL_DELETE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_Delete_Programa.sql");
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_updates_filiales.sql");
	private final String SQL_P40_SELECT_RUT_FILIAL = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRUTFilial.sql");
	private final String SQL_INSERTPROGRAMA = loadResourceToString("cl/bcs/plataforma/bat/sql/InsertarProcesoBatch_Insert.sql");
	
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
	public static class DeletePrograma{
		public Long 	IdPrograma;

		public DeletePrograma(Long IdPrograma){

			this.IdPrograma  = IdPrograma; 

		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		GenerarInterfazP40Filial generarInterfazP40 = new GenerarInterfazP40Filial();
		EntradaGenerarInterfazP40Filial entradap40 = new EntradaGenerarInterfazP40Filial();
		
		SQL sql = new SQL();
		HashMap<String, Object> paramsRut = new HashMap<String, Object>();
		paramsRut.put("IdEmpresa", Long.parseLong(this.getParam("IdEmpresa")));
		paramsRut.put("Rut", "");
		List<DatoFilial> filiales = sql.query(con, SQL_P40_SELECT_RUT_FILIAL, paramsRut, DatoFilial.class);
		
		for (DatoFilial filial : filiales) {
			entradap40.p_IdEmpresa 					= Long.parseLong(this.getParam("IdEmpresa"));
			entradap40.p_IdUsuario 					= Long.parseLong(this.getParam("IdUsuario"));
			entradap40.p_Rut						= filial.Rut;
			entradap40.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),entradap40.p_IdEmpresa,"PLA.RutaBaseArchivos");
		
	
			
			// IMPBESTOPT-901
			DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String dateformat = this.getParam("FechaUI");
			OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
			entradap40.p_Fecha  = dateTime.format(formatoFecha);		
			
			try{
				SalidaGenerarInterfazP40Filial salida = generarInterfazP40.logica(con, entradap40);
				Long  segundos=0L;
				Timestamp fechaPrograma = FechaDB.ObtenerFechaHoraTs(con, Long.parseLong(this.getParam("IdEmpresa")));
				Timestamp fechaEjecucion = Fecha.addSeconds(fechaPrograma, segundos.intValue());
				Timestamp fechaSistema = FechaDB.ObtenerFechaHoraSistemaTs(con, Long.parseLong(this.getParam("IdEmpresa")));
				if(!salida.P_OUT_ARCHIVOS_POR_FILIAL.isEmpty()) {
					this.getJobId();
					List<DeletePrograma> deleteprograma = new ArrayList<DeletePrograma>();
					deleteprograma.add(new DeletePrograma(this.getJobId()));
					sql.batch(con,SQL_DELETE_ID_PROGRAMA , deleteprograma,DeletePrograma.class);
					//Datos para insertar el nuevo programa 
					LinkedList<Long> secuenciaGrupoParametro = new LinkedList<>(Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma", new Long(1)));
					LinkedList<Long> secuenciaPrograma = new LinkedList<>(Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Programa", Long.valueOf(1)));
					String fechaEjecucionStr = Fecha.toFormat(fechaEjecucion, "yyyy-MM-dd HH:mm:ss");
					String fechaSistemaStr = Fecha.toFormat(fechaSistema, "yyyy-MM-dd");
					
					Long 	idSecuenciaGrupoParametro 	= secuenciaGrupoParametro.pop();
					Long 	idSecuenciaPrograma 	   	= secuenciaPrograma.pop();
					String 	tipoDePrograma		   		="GENERAR_INTERFAZ_P40_FILIAL";
					Long  	idUsuario					=entradap40.p_IdUsuario;
					String nodo							="";
					String 	comentario					="";
					List<Programa> programas = new ArrayList<Programa>();
					programas.add(new Programa(idSecuenciaPrograma,idSecuenciaGrupoParametro,tipoDePrograma,idUsuario,fechaEjecucionStr,fechaSistemaStr,nodo, comentario));
					sql.batch(con, SQL_INSERTPROGRAMA, programas, Programa.class);

					List<SalidaArchivoFilial> salidaArchivoFilial = salida.P_OUT_ARCHIVOS_POR_FILIAL;
					
					if(!salidaArchivoFilial.isEmpty()) {

						List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
						String zip  = salidaArchivoFilial.get(0).P_OUT_NOMBRE_ZIP + ".zip";
						ZipUtil.generateMultipleZip(salidaArchivoFilial.get(0).P_OUT_ARCHIVOS, entradap40.p_Ruta + "/" + zip);  
						actualizar.add(new ActualizarPrograma(zip,salidaArchivoFilial.get(0).P_OUT_NOMBRE_ARCHIVO_LOG,idSecuenciaPrograma, salidaArchivoFilial.get(0).P_OUT_COMENTARIO));
						sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
						
						con.commit();
					}
					}
				
				
			} catch(Exception e){
				con.rollback();
				e.printStackTrace();
				throw e;
			}
		
		}return true;
		
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
	
	/*
	public static void main(String[] args) throws Exception {
		Connection conn = PlataformaUtils.getDESAConnection(); // .getDESAConnection();
    	
		BcsJobGenerarInterfazP40Filial demo = new BcsJobGenerarInterfazP40Filial();		 			

		boolean Errores;

		try {
			Errores = demo.transaction(conn);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}*/


}
