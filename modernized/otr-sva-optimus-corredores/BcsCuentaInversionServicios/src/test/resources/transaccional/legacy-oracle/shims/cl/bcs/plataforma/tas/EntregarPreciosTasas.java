package cl.bcs.plataforma.tas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of cl.bcs.plataforma.tas.EntregarPreciosTasas.
 */
public class EntregarPreciosTasas implements JavaCall {

	// legacy cl/bcs/plataforma/tas/EntregarPreciosTasas.java:80-96 (verbatim)
	public static class EntregarPreciosTasas_Entrada 
	{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdPais;
		public List<EntregarPreciosTasas_Entrada_DatosInstrumentos> p_DatosInstrumentos;
		
		public EntregarPreciosTasas_Entrada() 
		{			
			p_DatosInstrumentos = new ArrayList<EntregarPreciosTasas_Entrada_DatosInstrumentos>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/tas/EntregarPreciosTasas.java:98-116 (verbatim)
	public static class EntregarPreciosTasas_Entrada_DatosInstrumentos 
	{
		public Long 		 IdRegistro;
		public String 		 TipoCartera;   		// --> PROPIA: CARTERA PROPIA  /  TERCEROS: CUENTA TERCEROS / "0": TODOS 
		public String 		 IdUso; 				// --> CARTERA / GARANTIA / SUBYACENTE / ORDEN / PRECIOMEDIO / SUSOSA
		public String 		 IdTipoCriterioTasa;  	// --> COMERCIAL /  NORMATIVO  / "0": TODOS
		public String 		 IdClasificacionTipoInstrumento; //--> RV, RF, IF
		public String 		 IdTipoInstrumento; 	// -->  ACN, ACX, BBC,.. / "0": TODOS
		public Long 		 IdInstrumento;
		public String 		 Nemotecnico;
		public String 		 IdMoneda;
		public java.sql.Date FechaConsulta;
		public Long 		 PlazoResidual;
		public String		 NemoSVS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/tas/EntregarPreciosTasas.java:122-139 (verbatim)
	public static class EntregarPreciosTasas_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<EntregarPreciosTasas_Salida_PreciosTasas> 	P_OUT_PRECIOSTASAS;
		
		
		public EntregarPreciosTasas_Salida() 
		{				
			P_OUT_MENSAJES		= new ArrayList<MensajeSalida>();
			P_OUT_PRECIOSTASAS	= new ArrayList<EntregarPreciosTasas_Salida_PreciosTasas>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/tas/EntregarPreciosTasas.java:141-160 (verbatim)
	public static class EntregarPreciosTasas_Salida_PreciosTasas {
		public Long 				IdRegistro;
		public String 				IdClasificacionTipoInstrumento;
		public String 				IdTipoInstrumento;
		public Long 				IdInstrumento;
		public String 				Nemotecnico;
		public String 				IdTipoTasa;
		public BigDecimal 			Tasa;
		public String 				IdTipoPrecio;
		public BigDecimal 			Precio;
		public String 				CodigoFuente;
		public String 				MensajeFuente;
		public java.sql.Timestamp 	FechaFuente;
		public String 				MensajeError;
		public String 				IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/**
	 * FAKE of EntregarPreciosTasas.logica: records the input and returns the prices scripted in "colaboradores.preciosTasas"
	 * (list of {Nemotecnico, Precio, FechaFuente "yyyy-MM-dd HH:mm:ss"}); none by default.
	 */
	public EntregarPreciosTasas_Salida logica(Connection con, EntregarPreciosTasas_Entrada entrada) throws Exception {
		Hooks.registrar("EntregarPreciosTasas", entrada);
		EntregarPreciosTasas_Salida out = new EntregarPreciosTasas_Salida();
		for (com.google.gson.JsonElement e : Hooks.lista("preciosTasas")) {
			com.google.gson.JsonObject o = e.getAsJsonObject();
			EntregarPreciosTasas_Salida_PreciosTasas p = new EntregarPreciosTasas_Salida_PreciosTasas();
			p.Nemotecnico = o.get("Nemotecnico").getAsString();
			p.Precio = o.get("Precio").getAsBigDecimal();
			if (o.has("FechaFuente") && !o.get("FechaFuente").isJsonNull()) p.FechaFuente = Timestamp.valueOf(o.get("FechaFuente").getAsString());
			out.P_OUT_PRECIOSTASAS.add(p);
		}
		return out;
	}

}
