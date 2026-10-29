package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCoberturaGarantia implements JavaCall{
	
	private final String SQL_CrearCoberturaGarantia_Insert = loadResourceToString("cl/bcs/plataforma/pla/sql/CrearCoberturaGarantia_Insert.sql");
	
	public static class EntradaServicio {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public String					p_SimularOperacion;
		public List<Cobertura>	        p_Cobertura;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobertura {
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
		public String	  p_IdConcepto;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjProcesamientoCobertura {
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
		public String 	  IdConcepto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearCoberturaGarantiaSalida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long                 P_OUT_REF_ID;
		
		public CrearCoberturaGarantiaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();
		EntradaServicio in = gson.fromJson(jin.toString(), EntradaServicio.class);
		CrearCoberturaGarantiaSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearCoberturaGarantiaSalida logica(Connection con, EntradaServicio entrada) throws Exception {
		CrearCoberturaGarantiaSalida salida = new CrearCoberturaGarantiaSalida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		 
		SQL sql = new SQL();
		List<ObjProcesamientoCobertura> objProcesamientoCobertura = new ArrayList<ObjProcesamientoCobertura>();
		Long secuenciaCoberturaGarantia = 0L;
		for (Cobertura cobertura : entrada.p_Cobertura) {
			ObjProcesamientoCobertura prc = new ObjProcesamientoCobertura();
			secuenciaCoberturaGarantia = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::CoberturaGarantia", 1L).get(0);
			prc.IdCoberturaGarantia = secuenciaCoberturaGarantia;
			prc.IdEntidadDestinoGarantia = cobertura.p_IdEntidadDestinoGarantia;
			prc.IdTipoInstrumentoExigeGarantia = cobertura.p_IdTipoInstrumentoExigeGarantia;
			prc.IdInstrumentoExigeGarantia = cobertura.p_IdInstrumentoExigeGarantia;
			prc.IdTipoInstrumentoComoGarantia = cobertura.p_IdTipoInstrumentoComoGarantia;
			prc.IdTipoClasificacionRiesgo = cobertura.p_IdTipoClasificacionRiesgo;
			prc.ClasificacionRiesgo = cobertura.p_ClasificacionRiesgo;
			prc.PlazoTope = cobertura.p_PlazoTope;
			prc.UnidadPlazo = cobertura.p_UnidadPlazo;
			prc.CoberturaEntidad = cobertura.p_CoberturaEntidad;
			prc.CoberturaCorredora = cobertura.p_CoberturaCorredora;
			prc.IdEstado = cobertura.p_IdEstado;
			prc.IdEntidadClasificadoraRiesgo = cobertura.p_IdEntidadClasificadoraRiesgo;
			prc.IdConcepto = cobertura.p_IdConcepto;
			objProcesamientoCobertura.add(prc);
		}
		
		sql.batch(con, SQL_CrearCoberturaGarantia_Insert, objProcesamientoCobertura, ObjProcesamientoCobertura.class);
		salida.P_OUT_REF_ID = secuenciaCoberturaGarantia;
		return salida;
	}	
	
	public static void main(String[] args) {
		
	}
	
}
