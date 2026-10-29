package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.util.List;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM: only the RequisitoOperativoCrearOrden POJO of cl.bcs.ordenes.CrearOrdenes (verbatim).
 */
public class CrearOrdenes {

	// legacy cl/bcs/ordenes/CrearOrdenes.java:827-882 (verbatim)
	public static class RequisitoOperativoCrearOrden {
		public String IdTipoCanalIngreso;
		public String CO_ContratoSimultanea;
		public String CO_ContratoCustodia;
		public String CO_PortafolioBloqueado;
		public String CO_ContratoFuturos;
		public String CO_ContratoVentaCortaRV;
		public String CO_ContratoMercantil;
		public String CO_CompraNominalRV;
		public String CO_CompraNominalRFIF;
		public String CO_VentaNominalRV;
		public String CO_VentaNominalRFIF;
		public String CO_ConfirmacionOrdenVerbalEscrita;
		public String CO_ContratoCondicionesGenerales;
		public String CO_ClienteBloqueado;
		public String CO_PersonaBloqueada;
		public String CO_EjecutivoSinProductoPlantilla;
		public String CO_EjecutivoSinRuteoPlantilla;
		public String CO_EjecutivoPortafolioBloqueado;
		public String CO_OrdenRequiereAutorizacion;
		public String CO_FotocopiaCarnet;
		public String CO_AntecedentesLegales;
		public String CO_OperacionSinFlujoCustodia;
		public String CO_MontoOperacionSuperaPermitidoPlantilla;
		public String CO_ComisionSuperaMaximoPermitido;
		public String CO_ComisionBajoMinimoPermitido;
		public String CO_MontoSuperaMaximoPermitidoCanalDistribucion;
		public String CO_DescuentoComisionSuperaPermitidoPlantilla;
		public String CO_OrdenPermanentePrecioMercado;
		public String CO_OperacionSinFlujoCaja;
		public String CO_DiferenciaPrecioOrden;
		public String CO_MecanismoCaptacion;
		public String CO_MonedaInstrumentoDiferenteMonedaLiquidacion;
		public String CO_OrdenMismaOperacionInstrumento;
		public String CO_RuteoPrecioMercado;
		public String CO_NoUsaPreferenciaNegocioOperaCuentaInversion;
		public String CO_ExigibilidadCorredora;
		public String CO_ExigibilidadEntidad;
		public String CO_PorcentajeCastigo;
		public String CO_OperacionSinFlujoVentaDescubierta;	
		public String CO_ContratoEmprestitos;
		public String CO_ContratoCanalInternet;
		public String CO_ContratoOpciones;
		public String CO_FlujoCantidadCustodiaInsuficiente;   	//Flujo custodia Insufuciente
		public String CO_MontoMinimoCrearOrden;   		
		public String CO_MontoMinimoCrearOrdenAutorizar;
		public String CO_CobroMaxPermitido; 
		public String CO_PorcentajeMaxOrden;
		public String CO_PorcentajeVisibilidad;
		public String CO_ContratoDeclaracionCRSFatca;
		public String CO_InstrumentoCalificado;
		public String CO_MontoOrdenVentaMenorCostos;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


}
