package cl.bcs.plataforma.tas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import cl.bcs.oracle.Colaboradores;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.plataforma.tas.EntregarPreciosTasas (BcsPlataformaServicios/.../tas/EntregarPreciosTasas.java, price and rate
 * service over the market data tables). The request/response data classes are copied VERBATIM (EntregarPreciosTasas.java lines: 80-96 EntregarPreciosTasas_Entrada, 98-116 EntregarPreciosTasas_Entrada_DatosInstrumentos, 122-139 EntregarPreciosTasas_Salida, 141-160 EntregarPreciosTasas_Salida_PreciosTasas).
 * logica() records the request under llamadasColaboradores and answers with case.json colaboradores.preciosTasas, a JSON array of
 * EntregarPreciosTasas_Salida_PreciosTasas (Nemotecnico, FechaFuente "yyyy-MM-dd HH:mm:ss", Precio ...). Absent = no prices.
 */
public class EntregarPreciosTasas implements JavaCall {

	@Override
	public com.google.gson.JsonObject call(Connection con, com.google.gson.JsonObject in) throws Exception {
		throw new UnsupportedOperationException("not part of the reportes oracle");
	}

	public EntregarPreciosTasas_Salida logica(Connection con, EntregarPreciosTasas_Entrada entrada) throws Exception {
		Colaboradores.registrar("EntregarPreciosTasas", entrada);
		EntregarPreciosTasas_Salida s = new EntregarPreciosTasas_Salida();
		JsonElement canned = Colaboradores.CONFIG.get("preciosTasas");
		if (canned != null) {
			List<EntregarPreciosTasas_Salida_PreciosTasas> l = Utils.getGsonBuilder().fromJson(canned,
					new TypeToken<ArrayList<EntregarPreciosTasas_Salida_PreciosTasas>>() { }.getType());
			s.P_OUT_PRECIOSTASAS = l;
		}
		return s;
	}

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
}
