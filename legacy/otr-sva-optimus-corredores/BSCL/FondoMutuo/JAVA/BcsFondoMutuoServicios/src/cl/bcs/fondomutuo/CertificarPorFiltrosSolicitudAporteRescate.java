package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.ResultadoCertificacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CertificarPorFiltrosSolicitudAporteRescate implements JavaCall {
	
	private final Logger logger =  LogManager.getLogger(CertificarPorFiltrosSolicitudAporteRescate.class);
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql");
	
	public static class EntradaCertificarPorFiltrosSolicitudAporteRescate{
	    public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_SimularOperacion;
	    public String p_UsaReglasNegocio;
	    public String p_FechaCertificacion;
	    public String p_IdTipoCanalIngreso;
	    public List<InFiltros> p_Filtros;
	    
	    public EntradaCertificarPorFiltrosSolicitudAporteRescate(){
	    	this.p_Filtros = new ArrayList<InFiltros>();
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFiltros{
		public String IdEmisor;
		public String IdMoneda;
		public String IdSerieFondo;
		public String Nacional;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String MoneyMarket;
		public Long IdCarga;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class SalidaCertificarPorFiltrosSolicitudAporteRescate {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCertificacion> P_OUT_RESULTADO;
		
		public SalidaCertificarPorFiltrosSolicitudAporteRescate(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCertificacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCertificarPorFiltrosSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaCertificarPorFiltrosSolicitudAporteRescate.class);
		SalidaCertificarPorFiltrosSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCertificarPorFiltrosSolicitudAporteRescate logica (Connection con, EntradaCertificarPorFiltrosSolicitudAporteRescate entrada) throws Exception {
		logger.info("INICIO::SalidaCertificarPorFiltrosSolicitudAporteRescate");
		SalidaCertificarPorFiltrosSolicitudAporteRescate out = new SalidaCertificarPorFiltrosSolicitudAporteRescate();
		SQL sql=new SQL();
		CertificarSolicitudAporteRescate certificar = new CertificarSolicitudAporteRescate();
		EntradaCertificarSolicitudAporteRescate entradaCertificarSolicitudAporteRescate = new EntradaCertificarSolicitudAporteRescate();
		entradaCertificarSolicitudAporteRescate.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCertificarSolicitudAporteRescate.p_IdUsuario = entrada.p_IdUsuario;
		entradaCertificarSolicitudAporteRescate.p_IdModulo = entrada.p_IdModulo;
		entradaCertificarSolicitudAporteRescate.p_SimularOperacion = entrada.p_SimularOperacion;
		entradaCertificarSolicitudAporteRescate.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCertificarSolicitudAporteRescate.p_FechaCertificacion = entrada.p_FechaCertificacion;
		entradaCertificarSolicitudAporteRescate.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		//GENERALMENTE ES UN SOLO FILTRO
		for(InFiltros filtro : entrada.p_Filtros){
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmisor", filtro.IdEmisor);
			params.put("IdMoneda", filtro.IdMoneda);
			params.put("IdSerieFondo", filtro.IdSerieFondo);
			params.put("Nacional", filtro.Nacional);
			params.put("TipoSolicitud", filtro.TipoSolicitud);
			params.put("IdTipoMovimientoFondoMutuo", filtro.IdTipoMovimientoFondoMutuo);
			params.put("MoneyMarket", filtro.MoneyMarket);
			params.put("IdCarga", filtro.IdCarga);
			params.put("FechaCertificacion", entrada.p_FechaCertificacion);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			entradaCertificarSolicitudAporteRescate.p_Solicitudes.addAll(sql.query(con, SQL_SELECT_SOLICITUDES, params, CertificarSolicitudAporteRescate.InSolicitudes.class));
		}
		
		SalidaCertificarSolicitudAporteRescate salida = certificar.logica(con, entradaCertificarSolicitudAporteRescate);
		
		out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
		out.P_OUT_RESULTADO = salida.P_OUT_RESULTADO;
		
		logger.info("FIN::SalidaCertificarPorFiltrosSolicitudAporteRescate");
		return out;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCertificacion\":\"2019-07-30\",\"p_Solicitudes\":[],\"p_Filtros\":[{\"IdInterno\":0,\"IdEmisor\":\"\",\"IdMoneda\":\"\",\"IdSerieFondo\":\"\",\"Nacional\":\"N\",\"TipoSolicitud\":\"\",\"IdTipoMovimientoFondoMutuo\":\"\",\"MoneyMarket\":\"N\",\"IdCarga\":0}]}";
		EntradaCertificarPorFiltrosSolicitudAporteRescate Objin = gson.fromJson(jsonRF, EntradaCertificarPorFiltrosSolicitudAporteRescate.class);
		CertificarPorFiltrosSolicitudAporteRescate  demo=new CertificarPorFiltrosSolicitudAporteRescate();				
		try {
			SalidaCertificarPorFiltrosSolicitudAporteRescate ObjOut=demo.logica(con, Objin);
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
