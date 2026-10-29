package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.oracle.Hooks;

/**
 * ORACLE FAKE (hand-written, minimal) of cl.bcs.ordenes.CalcularMontoYCobrosOrden: only the members ProcesarDistribucion touches
 * (ProcesarDistribucion.java:1243-1244,1268,1284-1290,1365-1381,2116-2158). The legacy source has no nested Orden_Entrada (the import of
 * ProcesarDistribucion would not resolve against it); its fields here are the ones ProcesarDistribucion assigns. No case exercises this path.
 */
public class CalcularMontoYCobrosOrden {

	public static class Orden_Entrada {
		public BigDecimal PrecioOrden;
		public Long IdInterno;
		public BigDecimal CantidadOrden;
		public String EsFungible;
		public String IdBolsa;
		public Long IdCorredorCorresponsal;
		public Long IdInstrumento;
		public Long IdInstrumentoSubyacente;
		public String IdMonedaLiquidacionOrden;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String FechaOperacion;
		public BigDecimal MontoOrden;
		public BigDecimal TasaSimultanea;
		public BigDecimal TasaRFIF;
		public BigDecimal TasaVentaCorta;
		public BigDecimal TipoCambio;
		public BigDecimal ValorRescateOrden;
		public Long PlazoSimultanea;
		public Long PlazoVentaCorta;
		public Long PlazoIF;
		public String EsAPlazo;
		public BigDecimal MontoTotal;
		public String EsPorMonto;
		public BigDecimal TasaReporte;
		public BigDecimal TasaReportado;
		public Long PlazoReporte;
		public Long BaseTasa;
		public String IdEmisor;
	}

	public static class OrdenSalida {
		public Long IdInterno;
		public BigDecimal CantidadOperacion;
	}

	public static class CalcularMontoYCobrosOrden_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Orden_Entrada> p_Ordenes = new ArrayList<>();
	}

	public static class CalcularMontoYCobrosOrden_Salida {
		public List<OrdenSalida> p_out_Ordenes = new ArrayList<>();
	}

	/** FAKE of the legacy logica: records its input (Hooks.registrar) and answers the script colaboradores.<Servicio> of the case (see oracle/Hooks.java); nothing of the legacy behaviour is reproduced. */
	public CalcularMontoYCobrosOrden_Salida logica(Connection con, CalcularMontoYCobrosOrden_Entrada in) throws Exception {
		Hooks.registrar("CalcularMontoYCobrosOrden", in);
		return Hooks.salida("CalcularMontoYCobrosOrden", CalcularMontoYCobrosOrden_Salida.class);
	}
}
