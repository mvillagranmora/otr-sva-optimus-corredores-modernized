package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class CrearEventoAutorizacionMasivo {

    private CrearEventoAutorizacionMasivo() {
    }

    public static final String CLASE = "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo";

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:51-68 (verbatim)
	public static class ServiceEntrada {

		public Long						P_IDEMPRESA;
		public Long						P_IDUSUARIO;
		public String					P_IDMODULO;
		public String					P_SIMULAROPERACION;
		public String					P_USAREGLASNEGOCIO;
		public List<ObjTABLAEVENTOS>	P_TABLAEVENTOS;
		public List<ObjTABLADETALLE> 	P_DETALLE = null ;
		public ServiceEntrada() {
			this.P_TABLAEVENTOS = new ArrayList<ObjTABLAEVENTOS>();
			this.P_DETALLE      = new ArrayList<ObjTABLADETALLE>(); 
		}

}

	public static class ServiceSalida {

		public List<ObjIDEVENTO>			P_OUT_IDEVENTO;
		public Long							P_OUT_ADVERTENCIASCONTADOR;
		public List<ObjADVERTENCIASDETALLE>	P_OUT_ADVERTENCIASDETALLE;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjERRORESDETALLE>		P_OUT_ERRORESDETALLE;
		
		public ServiceSalida() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_IDEVENTO = new ArrayList<ObjIDEVENTO>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<ObjADVERTENCIASDETALLE>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ObjERRORESDETALLE>();
		}
		
}

	public static class ObjTABLAEVENTOS {
		public Long			IdUnion = null;
		public String		IdTipoEvento;
		public String		IdModuloSolicita;
		public Long			IdUsuarioSolicita;
		public String		Fecha;
		public String		IdTipoOperacion;
		public String		IdConcepto;
		public String		NumeroOperacionConcepto;
		public String		FechaOperacionConcepto;
		public Long			IdCliente;
		public Long			IdFuncionario;
		public Long			IdProveedor;
		public String		IdTipoProducto;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public Long			IdInstrumento;
		public String		Nemo;
		public Long			Cantidad;
		public BigDecimal	Monto;
		public BigDecimal	Precio;
		public BigDecimal	Tasa;
		public String		IdMoneda;
		public String		Glosa;
		
}

	public static class ObjTABLADETALLE {
		public Long		  IdUnion          = null;
		public String	  NombreCampo      = "";
		public String	  TipoDato         = "";
		public String	  Valor            = "";
		public String     Clasificacion    = "";
		
	}

	public static class ObjIDEVENTO {
		public Long		IdEvento;
		public String	NumeroOperacionConcepto;
	}

	public static class ObjADVERTENCIASDETALLE {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}

	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
}
