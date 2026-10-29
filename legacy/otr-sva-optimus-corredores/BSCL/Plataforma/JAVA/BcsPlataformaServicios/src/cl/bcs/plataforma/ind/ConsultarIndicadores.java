package cl.bcs.plataforma.ind;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarIndicadores implements JavaCall {
	
	public String SQL_CONSULTAR_INDICADORES_Call = loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarIndicadores_Call.sql");
	static final Logger logger = LogManager.getLogger(ConsultarIndicadores.class);
	
	public static class Indicador {
		
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public String NombreTipoIndicador;
		public String ClaseDato;
		public String CodigoTipoIndicador;
		public String ValorIndicador;
		public String IdEstadoAutorizacion;
		public String GrupoIndicador;
		public String IdUso;
		public Long CantidadDecimales;
		public Long LongitudMinima;
		public Long LongitudMaxima;
		public String IdTipoEntidad;
		public Long IdEntidad;
		public String ModeloCliente;
		public String IdTipoPersona;
		
		@Override
		public String toString() {
			return "Indicador [IdIndicador=" + IdIndicador + ", IdTipoIndicador=" + IdTipoIndicador
					+ ", NombreTipoIndicador=" + NombreTipoIndicador + ", ClaseDato=" + ClaseDato + ", CodigoTipoIndicador="
					+ CodigoTipoIndicador + ", ValorIndicador=" + ValorIndicador + ", IdEstadoAutorizacion="
					+ IdEstadoAutorizacion + ", GrupoIndicador=" + GrupoIndicador + ", IdUso=" + IdUso
					+ ", CantidadDecimales=" + CantidadDecimales + ", LongitudMinima=" + LongitudMinima
					+ ", LongitudMaxima=" + LongitudMaxima + ", IdTipoEntidad=" + IdTipoEntidad + ", IdEntidad=" + IdEntidad
					+ ", ModeloCliente=" + ModeloCliente + ", IdTipoPersona=" + IdTipoPersona + "]";
		}
	}

	
	public static class EntradaServicioIndicadores{
		public Long p_IdEmpresa;
		public Long p_IdEntidad;
		public String p_IdTipoEntidad;
		public String p_CodigoTipoIndicador;
		public String p_GrupoIndicador;
		public String p_ModeloCliente;
		public Long p_IdIndicador;
		@Override
		public String toString() {
			return "EntradaServicioIndicadores [p_IdEmpresa=" + p_IdEmpresa + ", p_IdEntidad=" + p_IdEntidad
					+ ", p_IdTipoEntidad=" + p_IdTipoEntidad + ", p_CodigoTipoIndicador=" + p_CodigoTipoIndicador
					+ ", p_GrupoIndicador=" + p_GrupoIndicador + ", p_ModeloCliente=" + p_ModeloCliente
					+ ", p_IdIndicador=" + p_IdIndicador + "]";
		}		
	}
	
	public static class GrupoIndicadores{
		public String GrupoIndicador;
	}
	
	public static class SalidaServicioIndicadores{
		public Long p_out_IndicadoresContador;
		public List<Indicador> p_out_Indicadores;
		public Long p_out_GrupoIndicadoresContador;
		public List<GrupoIndicadores> p_out_GrupoIndicadores;
		@Override
		public String toString() {
			return "SalidaServicioIndicadores [p_out_IndicadoresContador=" + p_out_IndicadoresContador
					+ ", p_out_Indicadores=" + p_out_Indicadores + ", p_out_GrupoIndicadoresContador="
					+ p_out_GrupoIndicadoresContador + ", p_out_GrupoIndicadores=" + p_out_GrupoIndicadores
					+ "]";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaServicioIndicadores logica(Connection conn, EntradaServicioIndicadores in) {
		SQL sql = new SQL();
		SalidaServicioIndicadores salida = new SalidaServicioIndicadores();
		
		//crear cuenta custodia
		HashMap<String, Object> paramsIn = new HashMap<String,Object>();
		paramsIn.put("p_IdEmpresa", in.p_IdEmpresa);
		
		paramsIn.put("p_IdEntidad", in.p_IdEntidad);
		paramsIn.put("p_IdTipoEntidad", in.p_IdTipoEntidad);
		paramsIn.put("p_CodigoTipoIndicador", in.p_CodigoTipoIndicador);
		paramsIn.put("p_GrupoIndicador", in.p_GrupoIndicador);
		paramsIn.put("p_ModeloCliente", in.p_ModeloCliente);
		paramsIn.put("p_IdIndicador", in.p_IdIndicador);
		
		//Salida HashMap
		HashMap<String, Class> paramsOut = new HashMap<String,Class>();
		paramsOut.put("p_out_IndicadoresContador", Long.class);
		paramsOut.put("p_out_Indicadores", Indicador.class);
		paramsOut.put("p_out_GrupoIndicadoresContador", Long.class);
		paramsOut.put("p_out_GrupoIndicadores", GrupoIndicadores.class);
		
		try {
			logger.debug("INICIO::SQL_CONSULTAR_INDICADORES_Call");
			HashMap<String,Object> resultSP = sql.call(conn, SQL_CONSULTAR_INDICADORES_Call, paramsIn, paramsOut);
			logger.debug("FIN::SQL_CONSULTAR_INDICADORES_Call");
			salida.p_out_IndicadoresContador = (Long) resultSP.get("p_out_IndicadoresContador");
			salida.p_out_GrupoIndicadoresContador = (Long) resultSP.get("p_out_GrupoIndicadoresContador");
			if(!(salida.p_out_IndicadoresContador).equals(new Long(0))){
				salida.p_out_Indicadores = (List<Indicador>) resultSP.get("p_out_Indicadores");
			}
			if(!(salida.p_out_GrupoIndicadoresContador).equals(new Long(0))){
				salida.p_out_GrupoIndicadores = (List<GrupoIndicadores>) resultSP.get("p_out_GrupoIndicadores");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdEntidad\":0,\"p_IdTipoEntidad\":\"PORTAFOLIO\",\"p_CodigoTipoIndicador\":\"\",\"p_GrupoIndicador\":\"\",\"p_ModeloCliente\":\"CLIENTE AGENCIA\",\"p_IdIndicador\":0}";
			EntradaServicioIndicadores Objin = gson.fromJson(json, EntradaServicioIndicadores.class);

			ConsultarIndicadores demo = new ConsultarIndicadores();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				SalidaServicioIndicadores ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
