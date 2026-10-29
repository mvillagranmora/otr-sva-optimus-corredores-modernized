package cl.bcs.plataforma.pla;

import cl.bcs.plataforma.pla.ModificarDiccionario.ServicioSalidaError;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ModificarCoberturaGarantia implements JavaCall {

	
	private final String SQL_CoberturaGarantia_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarCoberturaGarantia_Update.sql");

	public static class EntradaModificarCoberturaGarantia {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public String					p_SimularOperacion;
		public String 					p_UsaReglasNegocio;
		
		public List<ModificarCobertura>	        p_Cobertura;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarCoberturaGarantia {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarCoberturaGarantia() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarCobertura {
		public Long  	  p_IdCoberturaGarantia;
		public Long  	  p_IdEntidadDestinoGarantia;
		public String	  p_IdTipoInstrumentoExigeGarantia;
		public Long   	  p_IdInstrumentoExigeGarantia;
		public String	  p_IdTipoInstrumentoComoGarantia;
		public String	  p_IdTipoClasificacionRiesgo;
		public String	  p_ClasificacionRiesgo;
		public Long  	  p_PlazoTope;
		public String	  p_UnidadPlazo;
		public BigDecimal p_CoberturaEntidad;
		public BigDecimal p_CoberturaCorredora;
		public String	  p_IdEstado;
		public String	  p_IdEntidadClasificadoraRiesgo;		

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjProcesamientoModificarCobertura {
		public Long       IdCoberturaGarantia;
		public Long  	  IdEntidadDestinoGarantia;
		public String	  IdTipoInstrumentoExigeGarantia;
		public Long   	  IdInstrumentoExigeGarantia;
		public String	  IdTipoInstrumentoComoGarantia;
		public String	  IdTipoClasificacionRiesgo;
		public String	  ClasificacionRiesgo;
		public Long  	  PlazoTope;
		public String	  UnidadPlazo;
		public BigDecimal CoberturaEntidad;
		public BigDecimal CoberturaCorredora;
		public String	  IdEstado;
		public String	  IdEntidadClasificadoraRiesgo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarCoberturaGarantia in = gson.fromJson(jin.toString(), EntradaModificarCoberturaGarantia.class);
		SalidaModificarCoberturaGarantia out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout; 
	}
	
	public SalidaModificarCoberturaGarantia logica(Connection con, EntradaModificarCoberturaGarantia entrada) throws Exception{
		SalidaModificarCoberturaGarantia salida = new SalidaModificarCoberturaGarantia();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new  SQL();
		List<ObjProcesamientoModificarCobertura> objProcModificarCobertura= new ArrayList<ObjProcesamientoModificarCobertura>();
		if(entrada.p_Cobertura != null ) {
			for(ModificarCobertura mc : entrada.p_Cobertura) {
				ObjProcesamientoModificarCobertura pmc = new ObjProcesamientoModificarCobertura();
				pmc.IdCoberturaGarantia = mc.p_IdCoberturaGarantia;
				pmc.IdEntidadDestinoGarantia = mc.p_IdEntidadDestinoGarantia;
				pmc.IdTipoInstrumentoExigeGarantia = mc.p_IdTipoInstrumentoExigeGarantia;
				pmc.IdInstrumentoExigeGarantia = mc.p_IdInstrumentoExigeGarantia;
				pmc.IdTipoInstrumentoComoGarantia = mc.p_IdTipoInstrumentoComoGarantia;
				pmc.IdTipoClasificacionRiesgo = mc.p_IdTipoClasificacionRiesgo;
				pmc.ClasificacionRiesgo = mc.p_ClasificacionRiesgo;
				pmc.PlazoTope = mc.p_PlazoTope;
				pmc.UnidadPlazo = mc.p_UnidadPlazo;
				pmc.CoberturaEntidad = mc.p_CoberturaEntidad;
				pmc.CoberturaCorredora = mc.p_CoberturaCorredora;
				pmc.IdEstado = mc.p_IdEstado;
				pmc.IdEntidadClasificadoraRiesgo = mc.p_IdEntidadClasificadoraRiesgo;
				objProcModificarCobertura.add(pmc);
			}
		}	
		sql.batch(con, SQL_CoberturaGarantia_Update, objProcModificarCobertura, ObjProcesamientoModificarCobertura.class);		
		return salida;
	}
	
	public static void main (String[] args) throws Exception{
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Cobertura\":[{\"p_IdCoberturaGarantia\":38,\"p_IdEntidadDestinoGarantia\":1,\"p_IdTipoInstrumentoExigeGarantia\":\"SIM\",\"p_IdInstrumentoExigeGarantia\":2249,\"p_IdTipoInstrumentoComoGarantia\":\"ACX\",\"p_ClasificacionRiesgo\":\"C\",\"p_IdTipoClasificacionRiesgo\":\"CLRV\",\"p_IdEntidadClasificadoraRiesgo\":\"BCS\",\"p_PlazoTope\":2,\"p_UnidadPlazo\":\"\",\"p_CoberturaEntidad\":11,\"p_CoberturaCorredora\":22,\"p_IdEstado\":\"ACT\"}]}";
			EntradaModificarCoberturaGarantia Objin = gson.fromJson(json, EntradaModificarCoberturaGarantia.class);
			ModificarCoberturaGarantia demo = new ModificarCoberturaGarantia();
			try {
				SalidaModificarCoberturaGarantia ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		
		}
		System.out.println("== END ==");
	}


}
