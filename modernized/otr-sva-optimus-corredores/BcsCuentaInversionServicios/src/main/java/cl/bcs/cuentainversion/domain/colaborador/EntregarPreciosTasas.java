package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.tas.EntregarPreciosTasas}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class EntregarPreciosTasas {

    private EntregarPreciosTasas() {
    }

    public static final String CLASE = "cl.bcs.plataforma.tas.EntregarPreciosTasas";

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
		
}
}
