package cl.bcs.facturacion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularMovimientoComprobanteWrapper implements JavaCall{
	private final Log log = new Log("AnularMovimientoComprobanteWrapper");
	
	public static class ObjMovimientoAAnular{
		public Long	IdPortafolio;
		public Long	IdCliente;
		public Long	IdMovimientoComprobante;
	
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class AnularMovimientoComprobanteWrapperEntrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_SimularOperacion;
		public String						p_UsaReglasNegocio;
		public String						p_EsIngresoAutomatico;
		public String						p_Glosa;
		public List<ObjMovimientoAAnular>	p_MovimientoAAnular;
		
		public AnularMovimientoComprobanteWrapperEntrada (){			
			p_MovimientoAAnular	= new ArrayList<ObjMovimientoAAnular>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjInformacionDetalle{
		public String	Codigo;
		public String	Descripcion;
	}
	
	public static class ObjResultadoProceso{
		public String	Campo;
		public Long		Id;
		public String	Servicio;
		public String	Resultado;
	}
	
	public static class ObjERRORESDETALLES {
		public Long		IdMovimientoComprobante;
		public String	Codigo;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjADVERTENCIASDETALLES {
		public Long		IdMovimientoComprobante;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class AnularMovimientoComprobanteWrapperSalida{
		public Long								P_OUT_INFORMACIONCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ObjInformacionDetalle>		p_out_InformacionDetalle;
		public List<ObjResultadoProceso>		p_out_ResultadoProceso;
		public List<ObjADVERTENCIASDETALLES>	p_out_AdvertenciasDetalle;
		public List<ObjERRORESDETALLES>			p_out_ErroresDetalle;
		
		public AnularMovimientoComprobanteWrapperSalida (){
			p_out_InformacionDetalle	= new ArrayList<ObjInformacionDetalle>();
			p_out_ResultadoProceso		= new ArrayList<ObjResultadoProceso>();
			p_out_AdvertenciasDetalle	= new ArrayList<ObjADVERTENCIASDETALLES>();
			p_out_ErroresDetalle		= new ArrayList<ObjERRORESDETALLES>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	
	public AnularMovimientoComprobanteWrapperSalida logica(Connection con, AnularMovimientoComprobanteWrapperEntrada in) throws Exception {
		AnularMovimientoComprobanteWrapperSalida out = new AnularMovimientoComprobanteWrapperSalida();
		SQL sql = new SQL();
		
		try {
			
			// ------------------------------
			// Crea las tablas temporales
			SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #AnularMovimientoComprobante_p_MovimientoAAnular (\"IdPortafolio\" BIGINT,\"IdCliente\" BIGINT,\"IdMovimientoComprobante\" BIGINT)");
			// ------------------------------
			// Inserta tablas temporales globales
			sql.batch(con, "INSERT INTO #AnularMovimientoComprobante_p_MovimientoAAnular (\"IdPortafolio\",\"IdCliente\",\"IdMovimientoComprobante\") VALUES (:IdPortafolio,:IdCliente,:IdMovimientoComprobante)",in.p_MovimientoAAnular, ObjMovimientoAAnular.class);
			
			//Escalares de entrada:
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIn.put("p_IdUsuario", in.p_IdUsuario);
			parmsIn.put("p_IdModulo", in.p_IdModulo);
			parmsIn.put("p_simularOperacion", in.p_SimularOperacion);
			parmsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIn.put("p_EsIngresoAutomatico", in.p_EsIngresoAutomatico);
			parmsIn.put("p_Glosa", in.p_Glosa);
			parmsIn.put("p_MovimientoAAnular", in.p_MovimientoAAnular);
			
			//Escalares de salida:
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_InformacionContador", Long.class);
			parmsOut.put("p_out_InformacionDetalle", ObjInformacionDetalle.class);
			parmsOut.put("p_out_ResultadoProceso", ObjResultadoProceso.class);
			parmsOut.put("p_out_AdvertenciasContador", Long.class);
			parmsOut.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLES.class);
			parmsOut.put("p_out_ErroresContador", Long.class);
			parmsOut.put("p_out_ErroresDetalle", ObjERRORESDETALLES.class);
			
			//Realiza el call
			log.write(in.p_MovimientoAAnular);
			log.write(parmsIn);
			
			// ------------------------------
			// Ejecuta el SP
			String queryCall = "call \"BSCL.Facturacion.BD.Servicios::AnularMovimientoComprobante\"(p_IdEmpresa=>:p_IdEmpresa,p_IdUsuario=>:p_IdUsuario,p_IdModulo=>:p_IdModulo,p_simularOperacion=>:p_simularOperacion,p_UsaReglasNegocio=>:p_UsaReglasNegocio,p_EsIngresoAutomatico=>:p_EsIngresoAutomatico,p_Glosa=>:p_Glosa,p_MovimientoAAnular=>#AnularMovimientoComprobante_p_MovimientoAAnular,p_out_InformacionContador=>@p_out_InformacionContador,p_out_InformacionDetalle=>$p_out_InformacionDetalle,p_out_ResultadoProceso=>$p_out_ResultadoProceso,p_out_AdvertenciasContador=>@p_out_AdvertenciasContador, p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle, p_out_ErroresContador=>@p_out_ErroresContador,p_out_ErroresDetalle=>$p_out_ErroresDetalle)";
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIn, parmsOut);
			
			log.write(rpt);
			
			out.P_OUT_INFORMACIONCONTADOR = (Long) rpt.get("p_out_InformacionContador");
			out.P_OUT_ADVERTENCIASCONTADOR = (Long) rpt.get("p_out_AdvertenciasContador");
			out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("p_out_ErroresContador");

			// ------------------------------
			out.p_out_InformacionDetalle = (List<ObjInformacionDetalle>) rpt.get("p_out_InformacionDetalle");
			out.p_out_ResultadoProceso = (List<ObjResultadoProceso>) rpt.get("p_out_ResultadoProceso");
			out.p_out_ErroresDetalle = (List<ObjERRORESDETALLES>) rpt.get("p_out_ErroresDetalle");
			out.p_out_AdvertenciasDetalle = (List<ObjADVERTENCIASDETALLES>) rpt.get("p_out_AdvertenciasDetalle");

		}
		finally {
			SQL.exec(con, "DROP TABLE #AnularMovimientoComprobante_p_MovimientoAAnular");
		}
		log.write(out);
		return out;
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		AnularMovimientoComprobanteWrapperEntrada jin=gson.fromJson(in.toString() ,AnularMovimientoComprobanteWrapperEntrada.class);
		AnularMovimientoComprobanteWrapperSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
};