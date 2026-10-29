package cl.bcs.plataforma.ins;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;
import java.util.Date;

/**
 * ORACLE FAKE of legacy cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa (cl/bcs/plataforma/ins/ConsultarInstrumentoPrecioBolsa.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by ProcesarDistribucion (order path; no case exercises it).
 */
public class ConsultarInstrumentoPrecioBolsa implements JavaCall {

	// legacy cl/bcs/plataforma/ins/ConsultarInstrumentoPrecioBolsa.java:50-65 (verbatim)
	public static class ConsultarInstrumentoPrecioBolsa_Entrada {
		public Long p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_SoloInscritos;
		public List<Instrumento> p_Instrumentos;
		public String p_IncluyeNemoNegociacion;
		
		public ConsultarInstrumentoPrecioBolsa_Entrada(){
			p_Instrumentos = new ArrayList<>();
			this.p_SoloInscritos = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/ins/ConsultarInstrumentoPrecioBolsa.java:67-82 (verbatim)
	public static class Instrumento{
		public Long IdInterno;
		public Long IdEmpresa;
		public Long IdInstrumento; 
		public String IdBolsa;//oblig
		public String Fecha;//oblig
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;//oblig	
		public String IdTipoInstrumento; 				
		public String IdCondicionDeLiquidacion;
		public String Nemotecnico;
		
		public Instrumento() {
			
		};
	}

	// legacy cl/bcs/plataforma/ins/ConsultarInstrumentoPrecioBolsa.java:131-158 (verbatim)
	public static class DatosInstrumentosPrecioBolsa_Salida {
		public Long 		IdInterno;
		public Long 		IdInstrumento;
		public String 		IdBolsa;
		public String 		FechaConsulta;
		public String 		Fecha;
		public BigDecimal 	VolumenTransado;
		public BigDecimal   MontoTransado;
		public BigDecimal 	PrecioBase;
		public BigDecimal 	PrecioCierre;
		public BigDecimal 	PuntaCompra;
		public BigDecimal 	PuntaVenta;
		public BigDecimal 	Variacion;
		public String 	    IdMoneda;
		public BigDecimal 	PrecioApertura;
		public BigDecimal	PrecioMayor;
		public BigDecimal 	PrecioMenor;
		public BigDecimal 	PrecioMedio;
		public BigDecimal 	PrecioMercado;
		public BigDecimal   TasaMercado;
		public BigDecimal 	CantidadPuntaCompra;
		public BigDecimal	CantidadPuntaVenta;
		
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	// legacy cl/bcs/plataforma/ins/ConsultarInstrumentoPrecioBolsa.java:115-129 (verbatim)
	public static class ConsultarInstrumentoPrecioBolsa_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<DatosInstrumentosPrecioBolsa_Salida> P_OUT_INSTRUMENTO;
		
		public ConsultarInstrumentoPrecioBolsa_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_INSTRUMENTO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/** FAKE of ConsultarInstrumentoPrecioBolsa.logica (legacy reads the closing prices): records its input and answers the scripted "colaboradores.precioBolsa": {"instrumentos":[{IdInstrumento,PrecioCierre,IdMoneda}], "errores": n}. */
	public ConsultarInstrumentoPrecioBolsa_Salida logica(Connection con, ConsultarInstrumentoPrecioBolsa_Entrada in) throws Exception {
		Hooks.registrar("ConsultarInstrumentoPrecioBolsa", in);
		JsonObject script = Hooks.objeto("precioBolsa");
		ConsultarInstrumentoPrecioBolsa_Salida out = new ConsultarInstrumentoPrecioBolsa_Salida();
		if (script.has("instrumentos")) {
			long i = 1;
			for (com.google.gson.JsonElement e : script.getAsJsonArray("instrumentos")) {
				JsonObject o = e.getAsJsonObject();
				DatosInstrumentosPrecioBolsa_Salida d = new DatosInstrumentosPrecioBolsa_Salida();
				d.IdInterno = i++;
				d.IdInstrumento = o.get("IdInstrumento").getAsLong();
				d.PrecioCierre = o.get("PrecioCierre").getAsBigDecimal();
				d.IdMoneda = o.has("IdMoneda") ? o.get("IdMoneda").getAsString() : "CLP";
				out.P_OUT_INSTRUMENTO.add(d);
			}
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : 0L;
		return out;
	}

}
