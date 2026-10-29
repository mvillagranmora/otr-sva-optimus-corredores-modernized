package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class CrearEventoAutorizacionMasivo {

	private CrearEventoAutorizacionMasivo() {
	}


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

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:70-89 (verbatim)
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
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:97-126 (verbatim)
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
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:128-135 (verbatim)
	public static class ObjTABLADETALLE {
		public Long		  IdUnion          = null;
		public String	  NombreCampo      = "";
		public String	  TipoDato         = "";
		public String	  Valor            = "";
		public String     Clasificacion    = "";
		
	}

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:136-139 (verbatim)
	public static class ObjIDEVENTO {
		public Long		IdEvento;
		public String	NumeroOperacionConcepto;
	}

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:141-145 (verbatim)
	public static class ObjADVERTENCIASDETALLE {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}

	// legacy cl/bcs/plataforma/aut/CrearEventoAutorizacionMasivo.java:147-150 (verbatim)
	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
}
