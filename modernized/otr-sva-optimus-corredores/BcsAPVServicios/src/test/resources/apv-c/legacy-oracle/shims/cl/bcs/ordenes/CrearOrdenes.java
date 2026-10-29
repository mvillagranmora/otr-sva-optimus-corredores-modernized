package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

/**
 * ORACLE FAKE (hand-written, minimal) of cl.bcs.ordenes.CrearOrdenes: only the members ProcesarDistribucion touches
 * (ProcesarDistribucion.java:1240-1241,1292-1347,1385-1440). The assigned fields of ObjOrdenesEntrada are listed with the types of the assignments.
 * No case exercises this path.
 */
public class CrearOrdenes {

	public static class ObjOrdenesEntrada {
		public String NemotecnicoSVS = "";   // legacy CrearOrdenes.java:327 (the inferred Tesoreria shim lacked it; APV.ProcesarDistribucion:1201 reads it)
		public Long IdInterno;
		public String FechaRecepcion, FechaVigencia, FechaEmision, FechaOperacion, IdCondicionDeLiquidacion, IdTipoOperacion, IdTipoVigencia, Glosa;
		public BigDecimal MontoNeto, Cantidad, ValorRescate, PrecioLimite, TipoCambio, TasaVentaCorta, TasaSimultanea, TasaRFIF;
		public String EsPorMonto, IdBoveda, IdFormaCustodia, EsFacturaInmediata, IdTipoPrecio, IdTipoCanalIngreso, IdTipoReajuste, IdEmisor, IdBolsa, EspecificaBolsa;
		public String IdMonedaLiquidacionOrden, IdCondicionDeLiquidacionPlazo, EsVentaCorta, EsOD, EsFungible, IdTipoLibroOrden, IdTipoCanalDistribucion, EsOrdenEspecial;
		public String PagoCobroCuentaInversion, TipoInteres, IdMecanismoCaptacion;
		public Long IdPorcentajeVisibilidad, IdPortafolio, IdInstrumento, IdInstrumentoSubyacente, PlazoVentaCorta, PlazoSimultanea, PlazoIF, IdLibroOrdenes;
		public Long IdCuentaCustodioPortafolio, IdCorredorCorresponsal, IdPersonaAutorizadoDarOrdenes, IdPreferenciaPagoPortafolio, BaseTasa;
	}

	public static class ObjRESULTADO {
		public Long IdInterno;
		public Long IdOrden;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Timestamp FechaOperacionOrden;
		public BigDecimal Cantidad;
	}

	public static class CrearOrdenesEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public List<ObjOrdenesEntrada> p_Ordenes = new ArrayList<>();
	}

	public static class CrearOrdenesSalida {
		public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<ObjRESULTADO> P_OUT_RESULTADO = new ArrayList<>();
	}

	/** FAKE of the legacy logica: records its input (Hooks.registrar) and answers the script colaboradores.<Servicio> of the case (see oracle/Hooks.java); nothing of the legacy behaviour is reproduced. */
	public CrearOrdenesSalida logica(Connection con, CrearOrdenesEntrada in) throws Exception {
		Hooks.registrar("CrearOrdenes", in);
		return Hooks.salida("CrearOrdenes", CrearOrdenesSalida.class);
	}
}
