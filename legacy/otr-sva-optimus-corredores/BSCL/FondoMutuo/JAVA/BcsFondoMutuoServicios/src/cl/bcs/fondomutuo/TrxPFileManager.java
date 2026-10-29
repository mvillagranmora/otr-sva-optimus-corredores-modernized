package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CuadrarOmnibusDetalle.EntradaCuadrarOmnibusDetalle;
import cl.bcs.fondomutuo.TrxFileManager.Cabecera;
import cl.bcs.fondomutuo.TrxFileManager.TrxDetalle;
import cl.bcs.fondomutuo.pojo.Emisor;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class TrxPFileManager implements JavaCall{
	
	private final Log log = new Log("FMM.TrxPFileManager");
	
	private final String SQL_SELECT_EMISOR = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectEmisor.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectCabeceras.sql");
	private final String SQL_UPDATE_ESTADO_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateEstadoCabecera.sql");
	private final String SQL_UPDATE_OMNIBUS_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/TrxPFileManager_UpdateOmnibusDetalle.sql");
	
	private static final String ARCHIVO_TRX_P = "TRXP";
	private static final String ESTADO_PROCESADO = "PRO";
	
	public static class EntradaTrxPFileManager{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaTrxPFileManager{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaTrxPFileManager(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateOmnibusDetalle {
		public Long IdOmnibusDetalle;
		public BigDecimal ValorCuotaAGF;
		public BigDecimal MontoAGF;
		public BigDecimal CantidadCuotasAGF;
		
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
		EntradaTrxPFileManager in=gson.fromJson(jin.toString() ,EntradaTrxPFileManager.class);
		SalidaTrxPFileManager out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaTrxPFileManager logica(Connection conn, EntradaTrxPFileManager in) throws Exception {
		log.write("INICIO::SalidaTrxPFileManager");
		SalidaTrxPFileManager out = new SalidaTrxPFileManager();
		SQL sql = new SQL();
		TrxFileManager trxFileManager = new TrxFileManager();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Cabecera> cabeceraList = new ArrayList<Cabecera>();
		
		try {
			List<TrxDetalle> detalleTrxCFileList = new ArrayList<TrxDetalle>();
			List<UpdateOmnibusDetalle> updateOmnibusDetalles = new ArrayList<UpdateOmnibusDetalle>();
			List<Long> trxs = new ArrayList<Long>();
			List<Emisor> emisores = sql.query(conn, SQL_SELECT_EMISOR, params, Emisor.class);
			List<String> codigosTipo = emisores.parallelStream().map(emisor->ARCHIVO_TRX_P + "_" + emisor.IdEmisor).collect(Collectors.toList());
			params.clear();
			params.put("IdCodigoTipo", codigosTipo);
			cabeceraList = sql.query(conn, SQL_SELECT_CABECERA, params, Cabecera.class);
			for (Cabecera cabecera : cabeceraList) {
				detalleTrxCFileList = trxFileManager.processTrxFiles(conn, cabecera.IdCabecera);
				
				if(null != detalleTrxCFileList) {
					for (TrxDetalle trxDetalle : detalleTrxCFileList) {
						if(null != trxDetalle && null != trxDetalle.idTrx && trxDetalle.idTrx > 0) {
							if(!trxs.contains(trxDetalle.idTrx)){
								UpdateOmnibusDetalle updateOmnibusDetalle = new UpdateOmnibusDetalle();
								updateOmnibusDetalle.IdOmnibusDetalle = trxDetalle.idTrx;
								updateOmnibusDetalle.CantidadCuotasAGF = trxDetalle.numeroCuotasPesosEquivalentes;
								updateOmnibusDetalle.MontoAGF = trxDetalle.montoClp;
								updateOmnibusDetalle.ValorCuotaAGF = trxDetalle.valorCuota;
								updateOmnibusDetalles.add(updateOmnibusDetalle);
								trxs.add(trxDetalle.idTrx);
							}
						}
					}
				}
				cabecera.IdEstado = ESTADO_PROCESADO;
				sql.batch(conn, SQL_UPDATE_ESTADO_CABECERA, cabeceraList, Cabecera.class);
				sql.batch(conn, SQL_UPDATE_OMNIBUS_DETALLE, updateOmnibusDetalles, UpdateOmnibusDetalle.class);
				CuadrarOmnibusDetalle c = new CuadrarOmnibusDetalle();
				EntradaCuadrarOmnibusDetalle e = new EntradaCuadrarOmnibusDetalle();
				e.p_IdEmpresa = in.p_IdEmpresa;
				e.p_IdUsuario = in.p_IdUsuario;
				e.p_OmnibusDetalle = trxs;
				c.logica(conn, e);
			}
		}catch(Exception e){
			log.write("Error al parsear archivo",e);
			throw e;
		}
		
		
		return out;
	}
	
}
