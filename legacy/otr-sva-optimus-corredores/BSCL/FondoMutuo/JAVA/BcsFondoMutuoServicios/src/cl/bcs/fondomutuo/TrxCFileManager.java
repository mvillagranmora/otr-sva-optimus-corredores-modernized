package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.TrxFileManager.Cabecera;
import cl.bcs.fondomutuo.TrxFileManager.TrxDetalle;
import cl.bcs.fondomutuo.pojo.Emisor;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class TrxCFileManager implements JavaCall {

	private final Log log = new Log("FMM.TrxCFileManager");

	private final String SQL_SELECT_EMISOR = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectEmisor.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectCabeceras.sql");
	private final String SQL_UPDATE_SOLICITUD_APORTE_RESCATE = loadResourceToString("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateSolicitudAporteRescate.sql");
	private final String SQL_UPDATE_ESTADO_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateEstadoCabecera.sql");
	private final String SQL_UPDATE_OMNIBUS_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateOmnibusDetalle.sql");
	
	private static final String ESTADO_CONFIRMADO = "CNF";
	private static final String ARCHIVO_TRX_C = "TRXC";
	private static final String ESTADO_PROCESADO = "PRO";
	
	public static class EntradaTrxCFileManager{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaTrxCFileManager{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaTrxCFileManager(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class UpdateSolicitudAporteRescate {
		public Long idFolioOmnibus;
		public Long idFolioAgfOmnibus;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateOmnibusDetalle {
		public Long IdOmnibusDetalle;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class UpdateEstadoCabecera {
		public Long idCabecera;
		public String idEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaTrxCFileManager in=gson.fromJson(jin.toString() ,EntradaTrxCFileManager.class);
		SalidaTrxCFileManager out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaTrxCFileManager logica(Connection conn, EntradaTrxCFileManager in) throws Exception {
		log.write("COMIENZA PROCESAMIENTO DE ARCHIVO TRX_C");
		SalidaTrxCFileManager out = new SalidaTrxCFileManager();
		SQL sql = new SQL();
		TrxFileManager trxFileManager = new TrxFileManager();
		List<UpdateSolicitudAporteRescate> updateSolicitudList = new ArrayList<UpdateSolicitudAporteRescate>();
		UpdateSolicitudAporteRescate updateSolicitud;
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Cabecera> cabeceraList = new ArrayList<Cabecera>();
		
		
		try {
			List<TrxDetalle> detalleTrxCFileList = new ArrayList<TrxDetalle>();
			List<UpdateOmnibusDetalle> updateOmnibusDetalles = new ArrayList<UpdateOmnibusDetalle>();
			List<Long> trxs = new ArrayList<Long>();
			List<Emisor> emisores = sql.query(conn, SQL_SELECT_EMISOR, params, Emisor.class);
			List<String> codigosTipo = emisores.parallelStream().map(emisor->ARCHIVO_TRX_C + "_" + emisor.IdEmisor).collect(Collectors.toList());
			params.clear();
			params.put("IdCodigoTipo", codigosTipo);
			cabeceraList = sql.query(conn, SQL_SELECT_CABECERA, params, Cabecera.class);
			for (Cabecera cabecera : cabeceraList) {
				detalleTrxCFileList = trxFileManager.processTrxFiles(conn, cabecera.IdCabecera);
				
				if(null != detalleTrxCFileList) {
					for (TrxDetalle trxDetalle : detalleTrxCFileList) {
						if(null != trxDetalle && null != trxDetalle.idTrx && trxDetalle.idTrx > 0) {
							updateSolicitud = new UpdateSolicitudAporteRescate();
							updateSolicitud.idFolioOmnibus = trxDetalle.idTrx;
							updateSolicitud.idFolioAgfOmnibus = trxDetalle.idAdministradora;
							updateSolicitudList.add(updateSolicitud);
							if(!trxs.contains(trxDetalle.idTrx)){
								UpdateOmnibusDetalle updateOmnibusDetalle = new UpdateOmnibusDetalle();
								updateOmnibusDetalle.IdOmnibusDetalle = trxDetalle.idTrx;
								updateOmnibusDetalle.IdEstado = ESTADO_CONFIRMADO;
								updateOmnibusDetalles.add(updateOmnibusDetalle);
								trxs.add(trxDetalle.idTrx);
							}
						}
					}
				}
				cabecera.IdEstado = ESTADO_PROCESADO;
			}
			sql.batch(conn, SQL_UPDATE_SOLICITUD_APORTE_RESCATE, updateSolicitudList, UpdateSolicitudAporteRescate.class);
			sql.batch(conn, SQL_UPDATE_ESTADO_CABECERA, cabeceraList, Cabecera.class);
			sql.batch(conn, SQL_UPDATE_OMNIBUS_DETALLE, updateOmnibusDetalles, UpdateOmnibusDetalle.class);
			
		} catch (Exception e) {
			log.write(e);
			throw e;
		}
		log.write("FINALIZA PROCESAMIENTO DE ARCHIVO TRX_C");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21}";
					
			EntradaTrxCFileManager Objin = gson.fromJson(jsonRF, EntradaTrxCFileManager.class);
			
			TrxCFileManager  demo=new TrxCFileManager();				
 			
 			try {
 				SalidaTrxCFileManager ObjOut=demo.logica(con, Objin);
 				//con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			con.rollback(); 			
	    	}
		 	
		
	
		System.out.println("== END ==");
	}
}
