package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.ConsultarPuntasMantenedor.PuntasEntrada;
import cl.bcs.mesa.SPT.ConsultarPuntasMantenedor.PuntasSalida;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPosicion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarPosicion.class);
	
	private final String SQL_SELECT_PARIDADES = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectParidades.sql");
	private final String SQL_SELECT_POSICION_INICIAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectPosicionInicial.sql");
	private final String SQL_SELECT_POSICION_INICIAL_MONEDA = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectPosicionInicialMoneda.sql");
	private final String SQL_SELECT_MOVIMIENTOS_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosSpot.sql");
	private final String SQL_SELECT_MOVIMIENTOS_FORWARD = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosForward.sql");
	private final String SQL_SELECT_MOVIMIENTOS_VENCIDOS_FORWARD = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosVencidosForward.sql");
	private final String SQL_SELECT_MOVIMIENTOS_FUTUROS_FORWARD = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosFuturosForward.sql");

	public static class EntradaConsultarPosicion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaOperacion;
		public String p_IdMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	 
	 
	public static class SalidaConsultaPosicion{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public String P_OUT_HORA;
		public SalidaTipoCambio P_OUT_TIPODECAMBIO;
		public SalidaInterbancario P_OUT_INTERBANCARIO;
		public SalidaArbitrajeInterbancario P_OUT_ARBITRAJEINTERBANCARIO;
		public SalidaDistribucion P_OUT_DISTRIBUCION;
		public SalidaArbitrajeDistribucion P_OUT_ARBITRAJEDISTRIBUCION;
		public SalidaFwd P_OUT_FWD;
		public SalidaConsolidadoSpot P_OUT_CONSOLIDADOSPOT;
		public SalidaResumenOperacionesSpot P_OUT_RESUMENOPERACIONESSPOT;
		public SalidaConsultaPosicion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_HORA = "00:00:00";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TIPODECAMBIO = new SalidaTipoCambio();
			this.P_OUT_INTERBANCARIO = new SalidaInterbancario();
			this.P_OUT_ARBITRAJEINTERBANCARIO = new SalidaArbitrajeInterbancario();
			this.P_OUT_DISTRIBUCION = new SalidaDistribucion();
			this.P_OUT_ARBITRAJEDISTRIBUCION = new SalidaArbitrajeDistribucion();
			this.P_OUT_FWD = new SalidaFwd();
			this.P_OUT_CONSOLIDADOSPOT = new SalidaConsolidadoSpot();
			this.P_OUT_RESUMENOPERACIONESSPOT = new SalidaResumenOperacionesSpot();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaTipoCambio{
		public BigDecimal PosicionInicial;
		public BigDecimal TipoCambioInicial;
		public BigDecimal TipoCambioCierreIngresado;
		public BigDecimal ResultadoPosicionInicial;
		
		public BigDecimal PosicionInicialT0;
		public BigDecimal TipoCambioInicialT0;
		public BigDecimal TipoCambioCierreIngresadoT0;
		public BigDecimal ResultadoPosicionInicialT0;
		
		public BigDecimal PosicionInicialT1;
		public BigDecimal TipoCambioInicialT1;
		public BigDecimal TipoCambioCierreIngresadoT1;
		public BigDecimal ResultadoPosicionInicialT1;
		
		public BigDecimal PosicionInicialT2;
		public BigDecimal TipoCambioInicialT2;
		public BigDecimal TipoCambioCierreIngresadoT2;
		public BigDecimal ResultadoPosicionInicialT2;
		
		public BigDecimal PosicionInicialT3;
		public BigDecimal TipoCambioInicialT3;
		public BigDecimal TipoCambioCierreIngresadoT3;
		public BigDecimal ResultadoPosicionInicialT3;
		
		public SalidaTipoCambio() {
			this.PosicionInicial = BigDecimal.ZERO;
			this.TipoCambioInicial = BigDecimal.ZERO;
			this.TipoCambioCierreIngresado = BigDecimal.ZERO;
			this.ResultadoPosicionInicial = BigDecimal.ZERO;
			
			this.PosicionInicialT0 = BigDecimal.ZERO;
			this.TipoCambioInicialT0 = BigDecimal.ZERO;
			this.TipoCambioCierreIngresadoT0 = BigDecimal.ZERO;
			this.ResultadoPosicionInicialT0 = BigDecimal.ZERO;
			
			this.PosicionInicialT1 = BigDecimal.ZERO;
			this.TipoCambioInicialT1 = BigDecimal.ZERO;
			this.TipoCambioCierreIngresadoT1 = BigDecimal.ZERO;
			this.ResultadoPosicionInicialT1 = BigDecimal.ZERO;
			
			this.PosicionInicialT2 = BigDecimal.ZERO;
			this.TipoCambioInicialT2 = BigDecimal.ZERO;
			this.TipoCambioCierreIngresadoT2 = BigDecimal.ZERO;
			this.ResultadoPosicionInicialT2 = BigDecimal.ZERO;
			
			this.PosicionInicialT3 = BigDecimal.ZERO;
			this.TipoCambioInicialT3 = BigDecimal.ZERO;
			this.TipoCambioCierreIngresadoT3 = BigDecimal.ZERO;
			this.ResultadoPosicionInicialT3 = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class VariablesBreakEven {
		public BigDecimal posicionInicial;
		public BigDecimal precioInicial;
		public BigDecimal BreakEvenCons;
		public BigDecimal BreakEvenT0;
		public BigDecimal BreakEvenT1;
		public BigDecimal BreakEvenT2;
		public BigDecimal BreakEvenT3;
		public String     valorIndicador;
	
		public VariablesBreakEven() {
			this.posicionInicial 		= BigDecimal.ZERO;
			this.precioInicial			= BigDecimal.ZERO;
			this.BreakEvenCons			= BigDecimal.ZERO;
			this.BreakEvenT0			= BigDecimal.ZERO;
			this.BreakEvenT1			= BigDecimal.ZERO;
			this.BreakEvenT2			= BigDecimal.ZERO;
			this.BreakEvenT3			= BigDecimal.ZERO;
			this.valorIndicador 		=null;
		}
	}

	public class VariablesMontoTotal {
		public BigDecimal MontoTotalConsolidado;
		public BigDecimal MontoTotalT0;
		public BigDecimal MontoTotalT1;
		public BigDecimal MontoTotalT2;
		public BigDecimal MontoTotalT3;
	
		public VariablesMontoTotal() {
			this.MontoTotalConsolidado			= BigDecimal.ZERO;
			this.MontoTotalT0			= BigDecimal.ZERO;
			this.MontoTotalT1			= BigDecimal.ZERO;
			this.MontoTotalT2			= BigDecimal.ZERO;
			this.MontoTotalT3			= BigDecimal.ZERO;
		}
	}
	public static class SalidaInterbancario{
		public BigDecimal ComprasPuntas;
		public BigDecimal VentasPuntas;
		public BigDecimal Posicion;
		public BigDecimal PromedioDeCompra;
		public BigDecimal PromedioDeVenta;
		public BigDecimal ResultadoInterbancario;
		public BigDecimal MontoParaPromedioCompra;
		public BigDecimal MontoParaPromedioVenta;
		
		public BigDecimal ComprasPuntasT0;
		public BigDecimal VentasPuntasT0;
		public BigDecimal PosicionT0;
		public BigDecimal PromedioDeCompraT0;
		public BigDecimal PromedioDeVentaT0;
		public BigDecimal ResultadoInterbancarioT0;
		public BigDecimal MontoParaPromedioCompraT0;
		public BigDecimal MontoParaPromedioVentaT0;
		
		public BigDecimal ComprasPuntasT1;
		public BigDecimal VentasPuntasT1;
		public BigDecimal PosicionT1;
		public BigDecimal PromedioDeCompraT1;
		public BigDecimal PromedioDeVentaT1;
		public BigDecimal ResultadoInterbancarioT1;
		public BigDecimal MontoParaPromedioCompraT1;
		public BigDecimal MontoParaPromedioVentaT1;
		
		public BigDecimal ComprasPuntasT2;
		public BigDecimal VentasPuntasT2;
		public BigDecimal PosicionT2;
		public BigDecimal PromedioDeCompraT2;
		public BigDecimal PromedioDeVentaT2;
		public BigDecimal ResultadoInterbancarioT2;
		public BigDecimal MontoParaPromedioCompraT2;
		public BigDecimal MontoParaPromedioVentaT2;
		
		public BigDecimal ComprasPuntasT3;
		public BigDecimal VentasPuntasT3;
		public BigDecimal PosicionT3;
		public BigDecimal PromedioDeCompraT3;
		public BigDecimal PromedioDeVentaT3;
		public BigDecimal ResultadoInterbancarioT3;
		public BigDecimal MontoParaPromedioCompraT3;
		public BigDecimal MontoParaPromedioVentaT3;
		
		
		public SalidaInterbancario() {
			this.ComprasPuntas = BigDecimal.ZERO;
			this.VentasPuntas = BigDecimal.ZERO;
			this.Posicion = BigDecimal.ZERO;
			this.PromedioDeCompra = BigDecimal.ZERO;
			this.PromedioDeVenta = BigDecimal.ZERO;
			this.ResultadoInterbancario = BigDecimal.ZERO;
			this.MontoParaPromedioCompra = BigDecimal.ZERO;
			this.MontoParaPromedioVenta = BigDecimal.ZERO;
			
			this.ComprasPuntasT0 = BigDecimal.ZERO;
			this.VentasPuntasT0 = BigDecimal.ZERO;
			this.PosicionT0 = BigDecimal.ZERO;
			this.PromedioDeCompraT0 = BigDecimal.ZERO;
			this.PromedioDeVentaT0 = BigDecimal.ZERO;
			this.ResultadoInterbancarioT0 = BigDecimal.ZERO;
			this.MontoParaPromedioCompra = BigDecimal.ZERO;
			this.MontoParaPromedioVenta = BigDecimal.ZERO;
			
			this.ComprasPuntasT1 = BigDecimal.ZERO;
			this.VentasPuntasT1 = BigDecimal.ZERO;
			this.PosicionT1 = BigDecimal.ZERO;
			this.PromedioDeCompraT1 = BigDecimal.ZERO;
			this.PromedioDeVentaT1 = BigDecimal.ZERO;
			this.ResultadoInterbancarioT1 = BigDecimal.ZERO;
			this.MontoParaPromedioCompra = BigDecimal.ZERO;
			this.MontoParaPromedioVenta = BigDecimal.ZERO;
			
			this.ComprasPuntasT2 = BigDecimal.ZERO;
			this.VentasPuntasT2 = BigDecimal.ZERO;
			this.PosicionT2 = BigDecimal.ZERO;
			this.PromedioDeCompraT2 = BigDecimal.ZERO;
			this.PromedioDeVentaT2 = BigDecimal.ZERO;
			this.ResultadoInterbancarioT2 = BigDecimal.ZERO;
			this.MontoParaPromedioCompra = BigDecimal.ZERO;
			this.MontoParaPromedioVenta = BigDecimal.ZERO;
			
			this.ComprasPuntasT3 = BigDecimal.ZERO;
			this.VentasPuntasT3 = BigDecimal.ZERO;
			this.PosicionT3 = BigDecimal.ZERO;
			this.PromedioDeCompraT3 = BigDecimal.ZERO;
			this.PromedioDeVentaT3 = BigDecimal.ZERO;
			this.ResultadoInterbancarioT3 = BigDecimal.ZERO;
			this.MontoParaPromedioCompra = BigDecimal.ZERO;
			this.MontoParaPromedioVenta = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BreakEvenVariables{
		
		public BigDecimal EquivalentePesosPosicionInicial;
		public BigDecimal EquivalentePesosComprasComprasPromedio;
		public BigDecimal EquivalenteEnPesosVentasVentasPromedio;
		public BigDecimal PosicionActualMonedaSeleccionada;
		public BigDecimal EquivalenteEnPesosMonedaSeleccionada;
		public BigDecimal TipoCambioBreakEven;
		public BigDecimal EquivalentePesosBreakEven;
		public BigDecimal UtilidadPerdidaBreakEven;
				
		public BreakEvenVariables() {
			this.EquivalentePesosPosicionInicial 		= BigDecimal.ZERO;
			this.EquivalentePesosComprasComprasPromedio = BigDecimal.ZERO;
			this.EquivalenteEnPesosVentasVentasPromedio	= BigDecimal.ZERO;
			this.PosicionActualMonedaSeleccionada 		= BigDecimal.ZERO;
			this.EquivalenteEnPesosMonedaSeleccionada	= BigDecimal.ZERO;
			this.TipoCambioBreakEven 					= BigDecimal.ZERO;
			this.EquivalentePesosBreakEven 				= BigDecimal.ZERO;
			this.UtilidadPerdidaBreakEven 				= BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaArbitrajeInterbancario{
		public BigDecimal PosicionPorArbitrajes;
		public BigDecimal ResultadoArbitrajes;
		public BigDecimal PosicionPorArbitrajesCom;
		public BigDecimal PosicionPorArbitrajesVen;
				
		public SalidaArbitrajeInterbancario() {
			this.PosicionPorArbitrajes = BigDecimal.ZERO;
			this.ResultadoArbitrajes = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaDistribucion{
		public BigDecimal ComprasDistribucion;
		public BigDecimal VentasDistribucion;
		public BigDecimal Posicion;
		public BigDecimal PromedioDeCompra;
		public BigDecimal PromedioDeVenta;
		public BigDecimal ResultadoDistribucion;
		public BigDecimal MontoParaPromedioCompra;
		public BigDecimal MontoParaPromedioVenta;
		
		public BigDecimal ComprasDistribucionT0;
		public BigDecimal VentasDistribucionT0;
		public BigDecimal PosicionT0;
		public BigDecimal PromedioDeCompraT0;
		public BigDecimal PromedioDeVentaT0;
		public BigDecimal ResultadoDistribucionT0;
		public BigDecimal MontoParaPromedioCompraT0;
		public BigDecimal MontoParaPromedioVentaT0;
		
		public BigDecimal ComprasDistribucionT1;
		public BigDecimal VentasDistribucionT1;
		public BigDecimal PosicionT1;
		public BigDecimal PromedioDeCompraT1;
		public BigDecimal PromedioDeVentaT1;
		public BigDecimal ResultadoDistribucionT1;
		public BigDecimal MontoParaPromedioCompraT1;
		public BigDecimal MontoParaPromedioVentaT1;
		
		public BigDecimal ComprasDistribucionT2;
		public BigDecimal VentasDistribucionT2;
		public BigDecimal PosicionT2;
		public BigDecimal PromedioDeCompraT2;
		public BigDecimal PromedioDeVentaT2;
		public BigDecimal ResultadoDistribucionT2;
		public BigDecimal MontoParaPromedioCompraT2;
		public BigDecimal MontoParaPromedioVentaT2;
		
		public BigDecimal ComprasDistribucionT3;
		public BigDecimal VentasDistribucionT3;
		public BigDecimal PosicionT3;
		public BigDecimal PromedioDeCompraT3;
		public BigDecimal PromedioDeVentaT3;
		public BigDecimal ResultadoDistribucionT3;
		public BigDecimal MontoParaPromedioCompraT3;
		public BigDecimal MontoParaPromedioVentaT3;
		
		public SalidaDistribucion() {
			this.ComprasDistribucion = BigDecimal.ZERO;
			this.VentasDistribucion = BigDecimal.ZERO;
			this.Posicion = BigDecimal.ZERO;
			this.PromedioDeCompra = BigDecimal.ZERO;
			this.PromedioDeVenta = BigDecimal.ZERO;
			this.ResultadoDistribucion = BigDecimal.ZERO;
			this.MontoParaPromedioCompra = BigDecimal.ZERO;
			this.MontoParaPromedioVenta = BigDecimal.ZERO;
			
			this.ComprasDistribucionT0 = BigDecimal.ZERO;
			this.VentasDistribucionT0 = BigDecimal.ZERO;
			this.PosicionT0 = BigDecimal.ZERO;
			this.PromedioDeCompraT0 = BigDecimal.ZERO;
			this.PromedioDeVentaT0 = BigDecimal.ZERO;
			this.ResultadoDistribucionT0 = BigDecimal.ZERO;
			this.MontoParaPromedioCompraT0 = BigDecimal.ZERO;
			this.MontoParaPromedioVentaT0 = BigDecimal.ZERO;
			
			this.ComprasDistribucionT1 = BigDecimal.ZERO;
			this.VentasDistribucionT1 = BigDecimal.ZERO;
			this.PosicionT1 = BigDecimal.ZERO;
			this.PromedioDeCompraT1 = BigDecimal.ZERO;
			this.PromedioDeVentaT1 = BigDecimal.ZERO;
			this.ResultadoDistribucionT1 = BigDecimal.ZERO;
			this.MontoParaPromedioCompraT1 = BigDecimal.ZERO;
			this.MontoParaPromedioVentaT1 = BigDecimal.ZERO;
			
			this.ComprasDistribucionT2 = BigDecimal.ZERO;
			this.VentasDistribucionT2 = BigDecimal.ZERO;
			this.PosicionT2 = BigDecimal.ZERO;
			this.PromedioDeCompraT2 = BigDecimal.ZERO;
			this.PromedioDeVentaT2 = BigDecimal.ZERO;
			this.ResultadoDistribucionT2 = BigDecimal.ZERO;
			this.MontoParaPromedioCompraT2 = BigDecimal.ZERO;
			this.MontoParaPromedioVentaT2 = BigDecimal.ZERO;
			
			this.ComprasDistribucionT3 = BigDecimal.ZERO;
			this.VentasDistribucionT3 = BigDecimal.ZERO;
			this.PosicionT3 = BigDecimal.ZERO;
			this.PromedioDeCompraT3 = BigDecimal.ZERO;
			this.PromedioDeVentaT3 = BigDecimal.ZERO;
			this.ResultadoDistribucionT3 = BigDecimal.ZERO;
			this.MontoParaPromedioCompraT3 = BigDecimal.ZERO;
			this.MontoParaPromedioVentaT3 = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaArbitrajeDistribucion{
		public BigDecimal MargenArbitrajes;
		public BigDecimal ResultadoArbitrajes;
		
		public BigDecimal MargenArbitrajesT0;
		public BigDecimal ResultadoArbitrajesT0;
		
		public BigDecimal MargenArbitrajesT1;
		public BigDecimal ResultadoArbitrajesT1;
		
		public BigDecimal MargenArbitrajesT2;
		public BigDecimal ResultadoArbitrajesT2;
		
		public BigDecimal MargenArbitrajesT3;
		public BigDecimal ResultadoArbitrajesT3;
		
		
		public SalidaArbitrajeDistribucion() {
			
			this.MargenArbitrajes = BigDecimal.ZERO;
			this.ResultadoArbitrajes = BigDecimal.ZERO;
			
			this.MargenArbitrajesT0 = BigDecimal.ZERO;
			this.ResultadoArbitrajesT0 = BigDecimal.ZERO;
			
			this.MargenArbitrajesT1 = BigDecimal.ZERO;
			this.ResultadoArbitrajesT1 = BigDecimal.ZERO;
			
			this.MargenArbitrajesT2 = BigDecimal.ZERO;
			this.ResultadoArbitrajesT2 = BigDecimal.ZERO;
			
			this.MargenArbitrajesT3 = BigDecimal.ZERO;
			this.ResultadoArbitrajesT3 = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class DistribucionTCCosto {
		public BigDecimal MontoCompraT0;
		public BigDecimal MontoCompraT1;
		public BigDecimal MontoCompraT2;
		public BigDecimal MontoCompraT3;
		public BigDecimal MontoVentaT0;
		public BigDecimal MontoVentaT1;
		public BigDecimal MontoVentaT2;
		public BigDecimal MontoVentaT3;
	
		public DistribucionTCCosto() {
			this.MontoCompraT0			= BigDecimal.ZERO;
			this.MontoCompraT1			= BigDecimal.ZERO;
			this.MontoCompraT2			= BigDecimal.ZERO;
			this.MontoCompraT3			= BigDecimal.ZERO;
			this.MontoVentaT0			= BigDecimal.ZERO;
			this.MontoVentaT1			= BigDecimal.ZERO;
			this.MontoVentaT2			= BigDecimal.ZERO;
			this.MontoVentaT3			= BigDecimal.ZERO;
		}
	}

	public static class SalidaFwd{
		public BigDecimal ComprasFwd;
		public BigDecimal VentasFwd;
		public BigDecimal Descalce;
		public BigDecimal VFisicoCompras;
		public BigDecimal VFisicoVentas;
		public BigDecimal VFisicoNeto;
		public BigDecimal VFutCompras;
		public BigDecimal VFutVentas;
		public BigDecimal VFutNeto;
		
		public SalidaFwd() {
			this.ComprasFwd = BigDecimal.ZERO;
			this.VentasFwd = BigDecimal.ZERO;
			this.Descalce = BigDecimal.ZERO;
			this.VFisicoCompras = BigDecimal.ZERO;
			this.VFisicoVentas = BigDecimal.ZERO;
			this.VFisicoNeto = BigDecimal.ZERO;
			this.VFutCompras = BigDecimal.ZERO;
			this.VFutVentas = BigDecimal.ZERO;
			this.VFutNeto = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsolidadoSpot{
		public BigDecimal Compra;
		public BigDecimal Venta;
		public BigDecimal Posicion;
		public BigDecimal UtilidadPerdidaPosicion;
		public BigDecimal PrecioPromedioCompra;
		public BigDecimal PrecioPromedioVenta;
		public BigDecimal ResultadoOperacionesCPAVTA;
		public BigDecimal ResultadoOperacionesConsolidado;
		public BigDecimal PrecioPromedioReferencial;
		
		public BigDecimal CompraT0;
		public BigDecimal VentaT0;
		public BigDecimal PosicionT0;
		public BigDecimal UtilidadPerdidaPosicionT0;
		public BigDecimal PrecioPromedioCompraT0;
		public BigDecimal PrecioPromedioVentaT0;
		public BigDecimal ResultadoOperacionesCPAVTAT0;
		public BigDecimal ResultadoOperacionesConsolidadoT0;
		public BigDecimal PrecioPromedioReferencialT0;
		
		public BigDecimal CompraT1;
		public BigDecimal VentaT1;
		public BigDecimal PosicionT1;
		public BigDecimal UtilidadPerdidaPosicionT1;
		public BigDecimal PrecioPromedioCompraT1;
		public BigDecimal PrecioPromedioVentaT1;
		public BigDecimal ResultadoOperacionesCPAVTAT1;
		public BigDecimal ResultadoOperacionesConsolidadoT1;
		public BigDecimal PrecioPromedioReferencialT1;
		
		public BigDecimal CompraT2;
		public BigDecimal VentaT2;
		public BigDecimal PosicionT2;
		public BigDecimal UtilidadPerdidaPosicionT2;
		public BigDecimal PrecioPromedioCompraT2;
		public BigDecimal PrecioPromedioVentaT2;
		public BigDecimal ResultadoOperacionesCPAVTAT2;
		public BigDecimal ResultadoOperacionesConsolidadoT2;
		public BigDecimal PrecioPromedioReferencialT2;
		
		public BigDecimal CompraT3;
		public BigDecimal VentaT3;
		public BigDecimal PosicionT3;
		public BigDecimal UtilidadPerdidaPosicionT3;
		public BigDecimal PrecioPromedioCompraT3;
		public BigDecimal PrecioPromedioVentaT3;
		public BigDecimal ResultadoOperacionesCPAVTAT3;
		public BigDecimal ResultadoOperacionesConsolidadoT3;
		public BigDecimal PrecioPromedioReferencialT3;
		
		public BigDecimal MontoTotalConsolidado;
		public BigDecimal MontoTotalT0;
		public BigDecimal MontoTotalT1;
		public BigDecimal MontoTotalT2;
		public BigDecimal MontoTotalT3;
		
		public BigDecimal BreakEvenConsolidado;
		public BigDecimal BreakEvenT0;
		public BigDecimal BreakEvenT1;
		public BigDecimal BreakEvenT2;
		public BigDecimal BreakEvenT3;
		public SalidaConsolidadoSpot() {
			this.Compra = BigDecimal.ZERO;
			this.Venta = BigDecimal.ZERO;
			this.Posicion = BigDecimal.ZERO;
			this.UtilidadPerdidaPosicion = BigDecimal.ZERO;
			this.PrecioPromedioCompra = BigDecimal.ZERO;
			this.PrecioPromedioVenta = BigDecimal.ZERO;
			this.ResultadoOperacionesCPAVTA = BigDecimal.ZERO;
			this.ResultadoOperacionesConsolidado = BigDecimal.ZERO;
			this.PrecioPromedioReferencial = BigDecimal.ZERO;
			
			this.CompraT0 = BigDecimal.ZERO;
			this.VentaT0 = BigDecimal.ZERO;
			this.PosicionT0 = BigDecimal.ZERO;
			this.UtilidadPerdidaPosicionT0 = BigDecimal.ZERO;
			this.PrecioPromedioCompraT0 = BigDecimal.ZERO;
			this.PrecioPromedioVentaT0 = BigDecimal.ZERO;
			this.ResultadoOperacionesCPAVTAT0 = BigDecimal.ZERO;
			this.ResultadoOperacionesConsolidadoT0 = BigDecimal.ZERO;
			this.PrecioPromedioReferencialT0 = BigDecimal.ZERO;
			
			this.CompraT1 = BigDecimal.ZERO;
			this.VentaT1 = BigDecimal.ZERO;
			this.PosicionT1 = BigDecimal.ZERO;
			this.UtilidadPerdidaPosicionT1 = BigDecimal.ZERO;
			this.PrecioPromedioCompraT1 = BigDecimal.ZERO;
			this.PrecioPromedioVentaT1 = BigDecimal.ZERO;
			this.ResultadoOperacionesCPAVTAT1 = BigDecimal.ZERO;
			this.ResultadoOperacionesConsolidadoT1 = BigDecimal.ZERO;
			this.PrecioPromedioReferencialT1 = BigDecimal.ZERO;
			
			this.CompraT2 = BigDecimal.ZERO;
			this.VentaT2 = BigDecimal.ZERO;
			this.PosicionT2 = BigDecimal.ZERO;
			this.UtilidadPerdidaPosicionT2 = BigDecimal.ZERO;
			this.PrecioPromedioCompraT2 = BigDecimal.ZERO;
			this.PrecioPromedioVentaT2 = BigDecimal.ZERO;
			this.ResultadoOperacionesCPAVTAT2 = BigDecimal.ZERO;
			this.ResultadoOperacionesConsolidadoT2 = BigDecimal.ZERO;
			this.PrecioPromedioReferencialT2 = BigDecimal.ZERO;
			
			this.CompraT3 = BigDecimal.ZERO;
			this.VentaT3 = BigDecimal.ZERO;
			this.PosicionT3 = BigDecimal.ZERO;
			this.UtilidadPerdidaPosicionT3 = BigDecimal.ZERO;
			this.PrecioPromedioCompraT3 = BigDecimal.ZERO;
			this.PrecioPromedioVentaT3 = BigDecimal.ZERO;
			this.ResultadoOperacionesCPAVTAT3 = BigDecimal.ZERO;
			this.ResultadoOperacionesConsolidadoT3 = BigDecimal.ZERO;
			this.PrecioPromedioReferencialT3 = BigDecimal.ZERO;
			
			this.BreakEvenConsolidado 	= BigDecimal.ZERO;
			this.BreakEvenT0			= BigDecimal.ZERO;
			this.BreakEvenT1			= BigDecimal.ZERO;
			this.BreakEvenT2			= BigDecimal.ZERO;
			this.BreakEvenT3			= BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaResumenOperacionesSpot{
		public Long NumeroAnuladas;
		public Long NumeroCompras;
		public Long NumeroVentas;
		public Long TotalOperaciones;
		
		public SalidaResumenOperacionesSpot() {
			this.NumeroAnuladas = 0L;
			this.NumeroCompras = 0L;
			this.NumeroVentas = 0L;
			this.TotalOperaciones = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Paridad{
		public Long IdParidadYTipoCambio;
		public String IdMoneda;
		public String NombreMoneda;
		public java.sql.Timestamp Fecha;
		public String IdEstado;
		public String Origen;
		public Long IdUsuarioIngreso;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBID;
		public BigDecimal ParidadASK;
		public BigDecimal LimiteSuperiorParidad;
		public BigDecimal LimiteInferiorParidad;
		public BigDecimal ParidadSBIF;
		public BigDecimal TipoCambioMercado;
		public BigDecimal LimiteSuperiorTipoCambio;
		public BigDecimal LimiteInferiorTipoCambio;
		public BigDecimal PrecioSpotCompra;
		public BigDecimal PrecioSpotVenta;
		public BigDecimal PrecioSpotPromedio;
		public String MultiplicaODivide;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PosicionInicial{
		public Long IdPosicionInicial;
		public Long IdEmpresa;
		public java.sql.Timestamp Fecha;
		public String IdMoneda;
		public BigDecimal TipoCambioInicial;
		public BigDecimal CierreIngresado;
		public BigDecimal PosicionInicial;
		public String TipoInstrumento;
		public String IdUsuario;
		public String Estado;
		public BigDecimal ResultadoPosicionInicial;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoSpot{
		public String IdTipoOperacion;
		public String Nemotecnico;
		public String IdTipoCanalIngreso;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String IdMonedaEquivalente;
		public String IdMonedaLocal;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal MontoMonedaSecundariaTCCosto;
		public BigDecimal ResultadoMonedaLocalConsolidado;
		public BigDecimal MontoMonedaPrincipalEquivalente;
		public BigDecimal ResultadoMonedaLocalPuntas;
		public BigDecimal ResultadoMonedaLocalBidAsk;
		public BigDecimal ResultadoMonedaLocalDO;
		public BigDecimal ResultadoMonedaLocalTCSpot;
		public BigDecimal ResultadoMonedaLocalTCCierreMesa;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBid;
		public BigDecimal ParidadAsk;
		public BigDecimal Margen;
		public String MultiplicaODivide;
		public String Estado;
		public String FechaValuta;
		public String OperacionNoFacturable;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoForward{
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String Nemotecnico;
		public Long Modalidad;
		public String Estado;
		public String ValorParametro;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParidadAUtilizarPosicionInicial{
		public String IdMoneda;
		public BigDecimal TipoCambioInicial;
		public BigDecimal CierreIngresado;
		public BigDecimal PosicionInicial;
		public BigDecimal ParidadAUtilizar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado{
		public BigDecimal Consolidado;
		public BigDecimal T0;
		public BigDecimal T1;
		public BigDecimal T2;
		public BigDecimal T3;
		
		public Resultado() {
			this.Consolidado = BigDecimal.ZERO;
			this.T0 = BigDecimal.ZERO;
			this.T1 = BigDecimal.ZERO;
			this.T2 = BigDecimal.ZERO;
			this.T3 = BigDecimal.ZERO;
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CompraVentaForward{
		public BigDecimal Compra;
		public BigDecimal Venta;
		
		public CompraVentaForward() {
			this.Compra = BigDecimal.ZERO;
			this.Venta = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Fecha{
		public String FechaSistema;
		public String FechaProceso;
		public String FechaT0;
		public String FechaT1;
		public String FechaT2;
		public String FechaT3;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class DatosBreakEven{
		public BigDecimal BreakEven;
		public BigDecimal MontoTotal;
		
		public DatosBreakEven() {
			this.BreakEven = BigDecimal.ZERO;
			this.MontoTotal = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarPosicion jin = gson.fromJson(in.toString(), EntradaConsultarPosicion.class);
		SalidaConsultaPosicion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	VariablesBreakEven VariablesBreak = new VariablesBreakEven();
	VariablesMontoTotal VariablesMontoTotal = new VariablesMontoTotal();
	public SalidaConsultaPosicion logica (Connection con, EntradaConsultarPosicion entrada) throws Exception {
		
		HashMap<String,HashMap<String,String>> ValoresIndicadores = Utils.getConfiguraciones(con,entrada.p_IdEmpresa);
		 if(ValoresIndicadores.get("MESA").get("VER_BREAKEVEN")!=null){
				VariablesBreak.valorIndicador =ValoresIndicadores.get("MESA").get("VER_BREAKEVEN");
		 }else {
			 VariablesBreak.valorIndicador="N";
		 }
		 
		logger.debug("INICIO::ConsultaPosicion");
		SalidaConsultaPosicion out = new SalidaConsultaPosicion();
		
		RevisarRequisitosBasicosSalida salidaBasicos = validaRequisitosMinimos(con,entrada);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		logger.debug("INICIO::ConsultaPosicion.ObtenerParametros");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String tipoParidad = "MERCADO";
		String codigoResultado = "0";
		String usarPuntaUtilidadPerdida = "N";
		try {
			tipoParidad = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "MESA", "PARIDADCALCULARPOSICION");
		}catch(Exception e) {
			logger.debug("ConsultaPosicion::No existe parametro PARIDADCALCULARPOSICION, se deja por defecto MERCADO");
		}

		try {
			codigoResultado = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "MESA", "CODRESULTADO");
		}catch(Exception e) {
			logger.debug("ConsultaPosicion::No existe parametro CODRESULTADO, se deja por defecto 0");
		}

		try {
			usarPuntaUtilidadPerdida = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "MESA", "USAR_PUNTA_UTILIDAD_PERDIDA");
		}catch(Exception e) {
			logger.debug("ConsultaPosicion::No existe parametro USAR_PUNTA_UTILIDAD_PERDIDA, se deja por defecto N");
		}
		
		logger.debug("FIN::ConsultaPosicion.ObtenerParametros");

		//DATOS NECESARIOS
		Map<String,Paridad> paridades = obtenerParidades(con, entrada);
		PuntasSalida puntas = new PuntasSalida();
		Fecha fechas = obtenerFechas(con, entrada, fechaSistema);
		String fechaAnterior = obtenerFechaAnterior(con, entrada.p_IdEmpresa,entrada.p_FechaOperacion);
		String fechaFinal = fechas.FechaT3;
		List<PosicionInicial> posicionesIniciales = obtenerPosicionesIniciales(con, entrada);
		List<PosicionInicial> posicionesInicialesMonedas = obtenerPosicionesInicialesMonedas(con, entrada,fechaAnterior,fechaFinal);
		List<MovimientoSpot> movimientosSpot = obtenerMovimientosSpot(con, entrada);
		List<MovimientoForward> movimientosForward = obtenerMovimientosForward(con, entrada, fechas);
		List<MovimientoForward> movimientosVencidoForward = obtenerMovimientosVencidosForward(con, entrada, fechas);
		List<MovimientoForward> movimientosFuturosForward = obtenerMovimientosFuturosForward(con, entrada, fechas);
		
		List<MovimientoSpot> movimientosVigentesSpot = obtenerMovimientosVigentesSpot(movimientosSpot);
		List<MovimientoSpot> movimientosInterbancarios = obtenerMovimientosInterbancarios(movimientosVigentesSpot);
		List<MovimientoSpot> movimientosArbitrajeInterbancarios = obtenerMovimientosArbitrajeInterbancarios(movimientosVigentesSpot);
		List<MovimientoSpot> movimientosDistribucion = obtenerMovimientosDistribucion(movimientosVigentesSpot);
		List<MovimientoSpot> movimientosArbitrajeDistribucion = obtenerMovimientosArbitrajeDistribucion(movimientosVigentesSpot);
		
		DistribucionTCCosto distribucionCosto = new DistribucionTCCosto();
		
		//Puntas Default
		if("S".equals(usarPuntaUtilidadPerdida)) {
			ConsultarPuntasMantenedor consultaPuntas = new ConsultarPuntasMantenedor();
	
			PuntasEntrada consultaPuntas_in = new PuntasEntrada();
			consultaPuntas_in.IdMoneda = entrada.p_IdMoneda;
			consultaPuntas_in.IdEmpresa = entrada.p_IdEmpresa;
			puntas = consultaPuntas.logica(con, consultaPuntas_in);
			
		}
			
		//HORA
		{
			out.P_OUT_HORA = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa).substring(11);
		}
		
		
		//INTERBANCARIO
		{
			out.P_OUT_INTERBANCARIO = obtenerInterbancario(movimientosInterbancarios, fechas);
		}
		
		//ARBITRAJE INTERBANCARIO
		{
			Resultado resultadoArbitrajeInterbancario = obtenerResultado(movimientosArbitrajeInterbancarios,codigoResultado,fechas);
			out.P_OUT_ARBITRAJEINTERBANCARIO = obtenerArbitrajeInterbancario(movimientosArbitrajeInterbancarios,resultadoArbitrajeInterbancario);
			
		}
		
		//ARBITRAJE DISTRIBUCION
		{
			Resultado resultadoArbitrajeDistribucion = obtenerResultado(movimientosArbitrajeDistribucion,codigoResultado,fechas);
			out.P_OUT_ARBITRAJEDISTRIBUCION = obtenerArbitrajeDistribucion(movimientosArbitrajeDistribucion,resultadoArbitrajeDistribucion,fechas);
		}
		
		//DISTRIBUCION
		{
			out.P_OUT_DISTRIBUCION = obtenerDistribucion(movimientosDistribucion,fechas,entrada,distribucionCosto); 
		}
		
		//FWD
		{
			CompraVentaForward compraVentaForward = obtenerCompraVentaForward(movimientosForward, entrada.p_IdMoneda);
			CompraVentaForward compraVentaVencidosForward = obtenerCompraVentaForward(movimientosVencidoForward, entrada.p_IdMoneda);
			CompraVentaForward compraVentaFuturosForward = obtenerCompraVentaForward(movimientosFuturosForward, entrada.p_IdMoneda);
			out.P_OUT_FWD = obtenerForward(compraVentaForward,compraVentaVencidosForward,compraVentaFuturosForward);
			
		}
		
		//TIPO DE CAMBIO
		{
			BigDecimal posicionInicialMoneda = obtenerPosicionInicialMoneda(posicionesIniciales,paridades,tipoParidad, entrada.p_IdMoneda);
			BigDecimal resultadoPosicionInicial = obtenerResultadoPosicionInicial(posicionesIniciales,paridades, entrada.p_IdMoneda);
			out.P_OUT_TIPODECAMBIO = obtenerTipoDeCambio(posicionesInicialesMonedas,paridades,tipoParidad,posicionInicialMoneda,resultadoPosicionInicial,fechas, entrada.p_IdMoneda);
			VariablesBreak.posicionInicial    = out.P_OUT_TIPODECAMBIO.PosicionInicial;
			VariablesBreak.precioInicial	  = out.P_OUT_TIPODECAMBIO.TipoCambioInicial;/**/
		}
		
		//CONSOLIDADO SPOT
		{
			out.P_OUT_CONSOLIDADOSPOT = obtenerConsolidadoSpot(out.P_OUT_INTERBANCARIO,out.P_OUT_DISTRIBUCION,out.P_OUT_TIPODECAMBIO,distribucionCosto,paridades,usarPuntaUtilidadPerdida,puntas);
		}
		
		//RESUMEN OPERACIONES SPOT
		{
			out.P_OUT_RESUMENOPERACIONESSPOT = obtenerResumenOperacionesSpot(movimientosSpot);
		}
		
		//ACTUALIZACION TIPO CAMBIO y UTILIDAD PERDIDA CONSOLIDADO
		{
			actualizarTipoDeCambio(out.P_OUT_TIPODECAMBIO,out.P_OUT_CONSOLIDADOSPOT,paridades,usarPuntaUtilidadPerdida,puntas,fechas);
		}
		
		//ACTUALIZACION RESULTADO INTERBANCARIO
		{
			actualizaInterbancario(out.P_OUT_INTERBANCARIO,out.P_OUT_TIPODECAMBIO,out.P_OUT_CONSOLIDADOSPOT, movimientosInterbancarios,fechas, entrada.p_IdMoneda);
		}
		

		logger.debug("FIN::ConsultaPosicion");
		if(VariablesBreak.valorIndicador.equals("S")) {
			out.P_OUT_CONSOLIDADOSPOT.MontoTotalConsolidado = VariablesMontoTotal.MontoTotalConsolidado;
			out.P_OUT_CONSOLIDADOSPOT.MontoTotalT0 = VariablesMontoTotal.MontoTotalT0;
			out.P_OUT_CONSOLIDADOSPOT.MontoTotalT1 = VariablesMontoTotal.MontoTotalT1;
			out.P_OUT_CONSOLIDADOSPOT.MontoTotalT2 = VariablesMontoTotal.MontoTotalT2;
			out.P_OUT_CONSOLIDADOSPOT.MontoTotalT3 = VariablesMontoTotal.MontoTotalT3;
			out.P_OUT_CONSOLIDADOSPOT.BreakEvenConsolidado = VariablesBreak.BreakEvenCons;
			out.P_OUT_CONSOLIDADOSPOT.BreakEvenT0=VariablesBreak.BreakEvenT0;
			out.P_OUT_CONSOLIDADOSPOT.BreakEvenT1=VariablesBreak.BreakEvenT1;
			out.P_OUT_CONSOLIDADOSPOT.BreakEvenT2=VariablesBreak.BreakEvenT2;
			out.P_OUT_CONSOLIDADOSPOT.BreakEvenT3=VariablesBreak.BreakEvenT3;
		}
		return out;
	}
	
	private RevisarRequisitosBasicosSalida validaRequisitosMinimos(Connection con, EntradaConsultarPosicion entrada) throws Exception{
		logger.info("INICIO::ConsultaPosicion.validaRequisitosMinimos");
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salida = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, "MSP");
		logger.info("FIN::ConsultaPosicion.validaRequisitosMinimos");
		return salida;
	}
	
	private Map<String,Paridad> obtenerParidades(Connection con, EntradaConsultarPosicion entrada) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerParidades");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		Map<String,Paridad> paridades = sql.query(con, SQL_SELECT_PARIDADES, params, Paridad.class).parallelStream().collect(Collectors.toMap(paridad->paridad.IdMoneda, paridad->paridad));
		logger.debug("FIN::ConsultaPosicion.obtenerParidades");
		return paridades;
	}

	private String obtenerFechaAnterior(Connection con, Long idEmpresa, String fechaConsulta) throws Exception {
		logger.debug("INICIO::ConsultaPosicion.obtenerFechaAnterior");
		Long idPaisEmpresa = obtenerIdPaisEmpresa(con, idEmpresa);

		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior diaAnteriorIn = new InObtenerDiaAnterior();
		InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
		fecha.fechaInicial = fechaConsulta;
		fecha.numeroDias = 1L;
		
		diaAnteriorIn.idPais = idPaisEmpresa;
		diaAnteriorIn.diaHabil = "S";
		diaAnteriorIn.fechas.add(fecha);
		OutObtenerDiaAnterior diaAnteriorOut = obtenerDiaAnterior.logica(con, diaAnteriorIn);
		String fechaAnterior = diaAnteriorOut.fechas.get(0).fechaCalculada;
		logger.debug("FIN::ConsultaPosicion.obtenerFechaAnterior");
		return fechaAnterior;
	}
	
	private Fecha obtenerFechas(Connection con,EntradaConsultarPosicion entrada,String fechaSistema) throws Exception {
		logger.debug("INICIO::ConsultaPosicion.obtenerFechas");
		Fecha fechas = new Fecha();
		fechas.FechaProceso = entrada.p_FechaOperacion;
		fechas.FechaSistema = fechaSistema;
		fechas.FechaT0 = entrada.p_FechaOperacion;
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		
		
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente.FechasIn fechaInT1 = new InObtenerDiaSiguiente.FechasIn();
		InObtenerDiaSiguiente.FechasIn fechaInT2 = new InObtenerDiaSiguiente.FechasIn();
		InObtenerDiaSiguiente.FechasIn fechaInT3 = new InObtenerDiaSiguiente.FechasIn();
		fechaInT1.fechaInicial = fechas.FechaProceso;
		fechaInT1.numeroDias = 1L;
		fechaInT1.pais = empresa.IdPais;
		fechaInT2.fechaInicial = fechas.FechaProceso;
		fechaInT2.numeroDias = 2L;
		fechaInT2.pais = empresa.IdPais;
		fechaInT3.fechaInicial = fechas.FechaProceso;
		fechaInT3.numeroDias = 3L;
		fechaInT3.pais = empresa.IdPais;
		InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_idPais = empresa.IdPais;
		inObtenerDiaSiguiente.p_diaHabil = "S";
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
		inObtenerDiaSiguiente.p_fechas.add(fechaInT1);
		inObtenerDiaSiguiente.p_fechas.add(fechaInT2);
		inObtenerDiaSiguiente.p_fechas.add(fechaInT3);
		OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		
		for(OutObtenerDiaSiguiente.FechasOut fechaOut : outObtenerDiaSiguiente.p_out_fechas) {
			switch(fechaOut.numeroDiasInicial.intValue()) {
			case 1: {
				fechas.FechaT1 = fechaOut.fechaCalculada;
				break;
			}
			case 2: {
				fechas.FechaT2 = fechaOut.fechaCalculada;
				break;
			}
			case 3: {
				fechas.FechaT3 = fechaOut.fechaCalculada;
				break;
			}
			}
		}
		
		logger.debug("FIN::ConsultaPosicion.obtenerFechas");
		return fechas;
	}
	
	private List<PosicionInicial> obtenerPosicionesIniciales(Connection con, EntradaConsultarPosicion entrada) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerPosicionesIniciales");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		List<PosicionInicial> posicionesIniciales = sql.query(con, SQL_SELECT_POSICION_INICIAL, params, PosicionInicial.class);
		logger.debug("FIN::ConsultaPosicion.obtenerPosicionesIniciales");
		return posicionesIniciales;
	}
	
	private List<PosicionInicial> obtenerPosicionesInicialesMonedas(Connection con, EntradaConsultarPosicion entrada, String fechaInicio, String fechaFinal) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerPosicionesInicialesMonedas");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaOperacionDesde", fechaInicio);
		params.put("FechaOperacionHasta", fechaFinal);
		List<PosicionInicial> posicionesInicialesMonedas = sql.query(con, SQL_SELECT_POSICION_INICIAL_MONEDA, params, PosicionInicial.class);
		logger.debug("FIN::ConsultaPosicion.obtenerPosicionesInicialesMonedas");
		return posicionesInicialesMonedas;
	}
	
	private ParidadAUtilizarPosicionInicial obtenerParidadAUtilizarPosicionInicial(PosicionInicial posicionInicial, Paridad paridad, String tipoParidad){
		logger.debug("INICIO::ConsultaPosicion.obtenerParidadAUtilizarPosicionInicial");
		ParidadAUtilizarPosicionInicial paridadAUtilizarPosicionInicial = new ParidadAUtilizarPosicionInicial();
		paridadAUtilizarPosicionInicial.IdMoneda = posicionInicial.IdMoneda;
		paridadAUtilizarPosicionInicial.TipoCambioInicial = posicionInicial.TipoCambioInicial;
		paridadAUtilizarPosicionInicial.CierreIngresado = posicionInicial.CierreIngresado;
		paridadAUtilizarPosicionInicial.PosicionInicial = posicionInicial.PosicionInicial;
		paridadAUtilizarPosicionInicial.ParidadAUtilizar = BigDecimal.ZERO;
		if(paridad != null) {
			if("BIDASK".equals(tipoParidad)) {
				paridadAUtilizarPosicionInicial.ParidadAUtilizar = posicionInicial.PosicionInicial.compareTo(BigDecimal.ZERO) != -1 ? paridad.ParidadBID : paridad.ParidadASK;
			}else {
				paridadAUtilizarPosicionInicial.ParidadAUtilizar = paridad.ParidadMercado;
			}
			
		}
		logger.debug("FIN::ConsultaPosicion.obtenerParidadAUtilizarPosicionInicial");
		return paridadAUtilizarPosicionInicial;
	}
	
	private BigDecimal obtenerPosicionInicialMoneda(List<PosicionInicial> posicionesInicialesMonedas, Map<String,Paridad> paridades, String tipoParidad, String idMoneda) {
		logger.debug("INICIO::ConsultaPosicion.obtenerPosicionInicialMoneda");
		BigDecimal posicionInicialMoneda = posicionesInicialesMonedas.stream().map(posicionMoneda->{
			BigDecimal posicion = idMoneda.equals(posicionMoneda.IdMoneda) ? posicionMoneda.PosicionInicial : BigDecimal.ZERO;
			
			return posicion;
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		
		logger.debug("FIN::ConsultaPosicion.obtenerPosicionInicialMoneda");
		return posicionInicialMoneda;
	}
	
	private BigDecimal obtenerResultadoPosicionInicial(List<PosicionInicial> posicionesInicialesMonedas, Map<String,Paridad> paridades, String idMoneda) {
		logger.debug("INICIO::ConsultaPosicion.obtenerResultadoPosicionInicial");
		BigDecimal resultadoPosicionInicial = posicionesInicialesMonedas.stream().map(posicionMoneda->{
			BigDecimal diferencia = posicionMoneda.CierreIngresado.subtract(posicionMoneda.TipoCambioInicial, MathContext.DECIMAL128);
			BigDecimal resultado = BigDecimal.ZERO;
			BigDecimal posicion = idMoneda.equals(posicionMoneda.IdMoneda) ? posicionMoneda.PosicionInicial : BigDecimal.ZERO;
			
			resultado = diferencia.multiply(posicion, MathContext.DECIMAL128);
			return resultado;
			
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		logger.debug("FIN::ConsultaPosicion.obtenerResultadoPosicionInicial");
		return resultadoPosicionInicial;
	}
	
	private List<MovimientoSpot> obtenerMovimientosSpot(Connection con,EntradaConsultarPosicion entrada) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosSpot");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		params.put("MonedaPrincipal", entrada.p_IdMoneda);
		List<MovimientoSpot> movimientosSpot = sql.query(con, SQL_SELECT_MOVIMIENTOS_SPOT, params, MovimientoSpot.class);
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosSpot");
		return movimientosSpot;
	}
	
	private List<MovimientoForward> obtenerMovimientosForward(Connection con,EntradaConsultarPosicion entrada,Fecha fechas) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosForward");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(6);
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		params.put("FechaSistema", fechas.FechaSistema);
		params.put("IdMoneda", entrada.p_IdMoneda);
		List<MovimientoForward> movimientosForward = sql.query(con, SQL_SELECT_MOVIMIENTOS_FORWARD, params, MovimientoForward.class);
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosForward");
		return movimientosForward;
	}
	
	private List<MovimientoForward> obtenerMovimientosVencidosForward(Connection con,EntradaConsultarPosicion entrada,Fecha fechas) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosVencidosForward");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(6);
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		params.put("FechaSistema", fechas.FechaSistema);
		params.put("IdMoneda", entrada.p_IdMoneda);
		List<MovimientoForward> movimientosVencidosForward = sql.query(con, SQL_SELECT_MOVIMIENTOS_VENCIDOS_FORWARD, params, MovimientoForward.class);
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosVencidosForward");
		return movimientosVencidosForward;
	}
	
	private List<MovimientoForward> obtenerMovimientosFuturosForward(Connection con, EntradaConsultarPosicion entrada, Fecha fechas) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosFuturosForward");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(4);
		params.put("Fecha", fechas.FechaT1);
		params.put("IdMoneda", entrada.p_IdMoneda);
		List<MovimientoForward> movimientoFuturosForward = sql.query(con, SQL_SELECT_MOVIMIENTOS_FUTUROS_FORWARD, params, MovimientoForward.class);
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosFuturosForward");
		return movimientoFuturosForward;
	}
	
	private List<MovimientoSpot> obtenerMovimientosVigentesSpot(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosSpotVigentes");
		List<MovimientoSpot> movimientosVigentesSpot = movimientosSpot.parallelStream().filter(movimiento->"VIG".equals(movimiento.Estado) || "PEL".equals(movimiento.Estado) || "PDA".equals(movimiento.Estado)).collect(Collectors.toList());
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosSpotVigentes");
		return movimientosVigentesSpot;
	}
	
	private SalidaTipoCambio obtenerTipoDeCambio(List<PosicionInicial> posicionesIniciales, Map<String, Paridad> paridades, String tipoParidad, BigDecimal posicionInicialMoneda, BigDecimal resultadoPosicionInicial,Fecha fechas, String IdMoneda) {
		logger.debug("INICIO::ConsultaPosicion.obtenerTipoDeCambio");
		SalidaTipoCambio salidaTipoCambio = new SalidaTipoCambio();
		salidaTipoCambio.PosicionInicial = posicionInicialMoneda;
		salidaTipoCambio.ResultadoPosicionInicial = resultadoPosicionInicial;
		SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		
		posicionesIniciales.stream().filter(posicionInicial -> posicionInicial.IdMoneda.equals(IdMoneda)).forEach(posicionInicial -> {
			salidaTipoCambio.TipoCambioCierreIngresado = posicionInicial.CierreIngresado;

			//Set precioIngreso a cada valuta
			if(formatoFecha.format(posicionInicial.Fecha).equals(fechas.FechaT0)) {
				salidaTipoCambio.TipoCambioInicial = posicionInicial.TipoCambioInicial;
				salidaTipoCambio.TipoCambioInicialT0 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioCierreIngresadoT0 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioInicialT1 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioCierreIngresadoT1 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioInicialT2 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioCierreIngresadoT2 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioInicialT3 = posicionInicial.CierreIngresado;
				salidaTipoCambio.TipoCambioCierreIngresadoT3 = posicionInicial.CierreIngresado;
			}
			if (salidaTipoCambio.PosicionInicial.compareTo(BigDecimal.ZERO) != 0) {
				Paridad paridad = paridades.get(posicionInicial.IdMoneda);
				if (paridad != null) {
					ParidadAUtilizarPosicionInicial paridadAUtilizarPosicionInicial = obtenerParidadAUtilizarPosicionInicial(
							posicionInicial, paridad, tipoParidad);
					BigDecimal tipoCambioInicialEquivalente = BigDecimal.ZERO;
					BigDecimal cierreIngresadoEquivalente = BigDecimal.ZERO;
					BigDecimal monedaSeleccionadaEquivalente = BigDecimal.ZERO;
					if ("D".equals(paridad.MultiplicaODivide)) {
						tipoCambioInicialEquivalente = paridadAUtilizarPosicionInicial.TipoCambioInicial.multiply(paridadAUtilizarPosicionInicial.ParidadAUtilizar, MathContext.DECIMAL128);
						cierreIngresadoEquivalente = paridadAUtilizarPosicionInicial.CierreIngresado.multiply(paridadAUtilizarPosicionInicial.ParidadAUtilizar, MathContext.DECIMAL128);
						monedaSeleccionadaEquivalente = paridadAUtilizarPosicionInicial.ParidadAUtilizar.compareTo(BigDecimal.ZERO) != 0 ? paridadAUtilizarPosicionInicial.PosicionInicial.divide(paridadAUtilizarPosicionInicial.ParidadAUtilizar,MathContext.DECIMAL128) : BigDecimal.ZERO;
					} else {
						if (paridadAUtilizarPosicionInicial.ParidadAUtilizar.compareTo(BigDecimal.ZERO) != 0) {
							tipoCambioInicialEquivalente = paridadAUtilizarPosicionInicial.TipoCambioInicial.divide(paridadAUtilizarPosicionInicial.ParidadAUtilizar, MathContext.DECIMAL128);
							cierreIngresadoEquivalente = paridadAUtilizarPosicionInicial.CierreIngresado.divide(paridadAUtilizarPosicionInicial.ParidadAUtilizar, MathContext.DECIMAL128);
						}
						monedaSeleccionadaEquivalente = paridadAUtilizarPosicionInicial.PosicionInicial.multiply(paridadAUtilizarPosicionInicial.ParidadAUtilizar, MathContext.DECIMAL128);
					}
				}
			}
		});
		logger.debug("FIN::ConsultaPosicion.obtenerTipoDeCambio");
		return salidaTipoCambio;
	}
	
	private List<MovimientoSpot> obtenerMovimientosInterbancarios(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosInterbancarios");
		List<MovimientoSpot> movimientosInterbancarios = movimientosSpot.parallelStream().filter(movimiento-> "INTERB".equals(movimiento.Nemotecnico) && !"FWD".equals(movimiento.IdTipoCanalIngreso)).collect(Collectors.toList());
		List<MovimientoSpot> movimientosInterbancarioArbDis = movimientosSpot.parallelStream().filter(movimiento->"ARBINT".equals(movimiento.Nemotecnico)&& !"FWD".equals(movimiento.IdTipoCanalIngreso)).collect(Collectors.toList());
		movimientosInterbancarios.addAll(movimientosInterbancarioArbDis);
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosInterbancarios");
		return movimientosInterbancarios;
	}
	
	private Resultado obtenerResultado(List<MovimientoSpot> movimientos,String codigoResultado,Fecha fechas) {
		logger.debug("INICIO::ConsultaPosicion.obtenerResultado");
		Resultado resultadoInterbancario = new Resultado();
		movimientos.stream().forEach(movimiento->{
			BigDecimal valor = obtenerValorResultadoSpot(movimiento,codigoResultado);
			//TODO:Ver como hacerlo para los T1...
			resultadoInterbancario.Consolidado = resultadoInterbancario.Consolidado.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT0)) resultadoInterbancario.T0 = resultadoInterbancario.T0.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT1)) resultadoInterbancario.T1 = resultadoInterbancario.T1.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT2)) resultadoInterbancario.T2 = resultadoInterbancario.T2.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT3)) resultadoInterbancario.T3 = resultadoInterbancario.T3.add(valor,MathContext.DECIMAL128);
		});
		logger.debug("FIN::ConsultaPosicion.obtenerResultado");
		
		return resultadoInterbancario;
	}
	
	private BigDecimal obtenerValorResultadoSpot(MovimientoSpot movimiento, String codigoResultado) {
		logger.debug("INICIO::ConsultaPosicion.obtenerValorResultadoSpot");
		BigDecimal valor = BigDecimal.ZERO;
		switch(codigoResultado) {
		case "1":
			valor = movimiento.ResultadoMonedaLocalPuntas;
			break;
		case "2":
			valor = movimiento.ResultadoMonedaLocalBidAsk;
			break;
		case "3":
			valor = movimiento.ResultadoMonedaLocalDO;
			break;
		case "4":
			valor = movimiento.ResultadoMonedaLocalTCSpot;
			break;
		case "5":
			valor = movimiento.ResultadoMonedaLocalTCCierreMesa;
			break;
		
		}
		logger.debug("FIN::ConsultaPosicion.obtenerValorResultadoSpot");
		return valor;
	}
	
	private SalidaInterbancario obtenerInterbancario(List<MovimientoSpot> movimientosInterbancarios, Fecha fechas) {
		logger.debug("INICIO::ConsultaPosicion.obtenerInterbancario");
		BigDecimal cantidadCompraT0 = BigDecimal.ZERO;
		BigDecimal cantidadCompraT1 = BigDecimal.ZERO;
		BigDecimal cantidadCompraT2 = BigDecimal.ZERO;
		BigDecimal cantidadCompraT3 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT0 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT1 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT2 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT3 = BigDecimal.ZERO;
		
		BigDecimal montoParaPromedioCompra = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT0 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT1 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT2 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT3 = BigDecimal.ZERO;
		
		BigDecimal montoParaPromedioVenta = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT0 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT1 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT2 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT3 = BigDecimal.ZERO;
		
		SalidaInterbancario salidaInterbancario = new SalidaInterbancario();
		for(MovimientoSpot movimiento : movimientosInterbancarios) {
			
			//Se agrega para los Arbitrajes interbancarios 
			//Compra MX - > Dolar (+ Venta Dolar)
			//Vendo MX -> (+ Compra Dolar)

			if((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT"))) continue;
			
			if (movimiento.Nemotecnico.equals("ARBINT")) {
				if(movimiento.IdTipoOperacion.equals("VENSPT")) {
					movimiento.IdTipoOperacion = "COMSPT";
				}else {
					movimiento.IdTipoOperacion = "VENSPT";
				} 			
			}
			
			if(!movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) && movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaLocal)) {
				if("COMSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.ComprasPuntas = salidaInterbancario.ComprasPuntas.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
					montoParaPromedioCompra = montoParaPromedioCompra.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						cantidadCompraT0 = cantidadCompraT0.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT0 = salidaInterbancario.ComprasPuntasT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioCompraT0 = montoParaPromedioCompraT0.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						cantidadCompraT1 = cantidadCompraT1.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT1 = salidaInterbancario.ComprasPuntasT1.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioCompraT1 = montoParaPromedioCompraT1.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						cantidadCompraT2 = cantidadCompraT2.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT2 = salidaInterbancario.ComprasPuntasT2.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioCompraT2 = montoParaPromedioCompraT2.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						cantidadCompraT3 = cantidadCompraT3.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT3 = salidaInterbancario.ComprasPuntasT3.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioCompraT3 = montoParaPromedioCompraT3.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
				}else if("VENSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.VentasPuntas = salidaInterbancario.VentasPuntas.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
					montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						cantidadVentaT0 = cantidadVentaT0.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT0 = salidaInterbancario.VentasPuntasT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioVentaT0 = montoParaPromedioVentaT0.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						cantidadVentaT1 = cantidadVentaT1.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT1 = salidaInterbancario.VentasPuntasT1.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioVentaT1 = montoParaPromedioVentaT1.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						cantidadVentaT2 = cantidadVentaT2.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT2 = salidaInterbancario.VentasPuntasT2.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioVentaT2 = montoParaPromedioVentaT2.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						cantidadVentaT3 = cantidadVentaT3.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT3 = salidaInterbancario.VentasPuntasT3.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						montoParaPromedioVentaT3 = montoParaPromedioVentaT3.add(movimiento.ResultadoMonedaLocalConsolidado,MathContext.DECIMAL128);
					}
				}
			}else {
				BigDecimal montoAdd = movimiento.MontoMonedaPrincipal;
				
				if(movimiento.Nemotecnico.equals("ARBINT")) {
					montoAdd = movimiento.MontoMonedaSecundaria;
				}
				
				if("COMSPT".equals(movimiento.IdTipoOperacion)) {
						
					salidaInterbancario.ComprasPuntas = salidaInterbancario.ComprasPuntas.add(montoAdd,MathContext.DECIMAL128);
					montoParaPromedioCompra = montoParaPromedioCompra.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						cantidadCompraT0 = cantidadCompraT0.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT0 = salidaInterbancario.ComprasPuntasT0.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioCompraT0 = montoParaPromedioCompraT0.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						cantidadCompraT1 = cantidadCompraT1.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT1 = salidaInterbancario.ComprasPuntasT1.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioCompraT1 = montoParaPromedioCompraT1.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						cantidadCompraT2 = cantidadCompraT2.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT2 = salidaInterbancario.ComprasPuntasT2.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioCompraT2 = montoParaPromedioCompraT2.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						cantidadCompraT3 = cantidadCompraT3.add(BigDecimal.ONE);
						salidaInterbancario.ComprasPuntasT3 = salidaInterbancario.ComprasPuntasT3.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioCompraT3 = montoParaPromedioCompraT3.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
				}else if("VENSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.VentasPuntas = salidaInterbancario.VentasPuntas.add(montoAdd,MathContext.DECIMAL128);
					montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						cantidadVentaT0 = cantidadVentaT0.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT0 = salidaInterbancario.VentasPuntasT0.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioVentaT0 = montoParaPromedioVentaT0.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						cantidadVentaT1 = cantidadVentaT1.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT1 = salidaInterbancario.VentasPuntasT1.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioVentaT1 = montoParaPromedioVentaT1.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						cantidadVentaT2 = cantidadVentaT2.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT2 = salidaInterbancario.VentasPuntasT2.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioVentaT2 = montoParaPromedioVentaT2.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						cantidadVentaT3 = cantidadVentaT3.add(BigDecimal.ONE);
						salidaInterbancario.VentasPuntasT3 = salidaInterbancario.VentasPuntasT3.add(montoAdd,MathContext.DECIMAL128);
						montoParaPromedioVentaT3 = montoParaPromedioVentaT3.add(movimiento.MontoMonedaSecundariaTCCosto,MathContext.DECIMAL128);
					}
				}
			}
			
			salidaInterbancario.Posicion = salidaInterbancario.ComprasPuntas.subtract(salidaInterbancario.VentasPuntas,MathContext.DECIMAL128);
			salidaInterbancario.PosicionT0 = salidaInterbancario.ComprasPuntasT0.subtract(salidaInterbancario.VentasPuntasT0,MathContext.DECIMAL128);
			salidaInterbancario.PosicionT1 = salidaInterbancario.ComprasPuntasT1.subtract(salidaInterbancario.VentasPuntasT1,MathContext.DECIMAL128);
			salidaInterbancario.PosicionT2 = salidaInterbancario.ComprasPuntasT2.subtract(salidaInterbancario.VentasPuntasT2,MathContext.DECIMAL128);
			salidaInterbancario.PosicionT3 = salidaInterbancario.ComprasPuntasT3.subtract(salidaInterbancario.VentasPuntasT3,MathContext.DECIMAL128);
			
		}

		if(salidaInterbancario.ComprasPuntasT0.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeCompraT0 = montoParaPromedioCompraT0.divide(salidaInterbancario.ComprasPuntasT0,MathContext.DECIMAL128);
		if(salidaInterbancario.ComprasPuntasT1.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeCompraT1 = montoParaPromedioCompraT1.divide(salidaInterbancario.ComprasPuntasT1,MathContext.DECIMAL128);
		if(salidaInterbancario.ComprasPuntasT2.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeCompraT2 = montoParaPromedioCompraT2.divide(salidaInterbancario.ComprasPuntasT2,MathContext.DECIMAL128);
		if(salidaInterbancario.ComprasPuntasT3.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeCompraT3 = montoParaPromedioCompraT3.divide(salidaInterbancario.ComprasPuntasT3,MathContext.DECIMAL128);
		if(salidaInterbancario.ComprasPuntas.compareTo(BigDecimal.ZERO) != 0)   salidaInterbancario.PromedioDeCompra   = montoParaPromedioCompra.divide(salidaInterbancario.ComprasPuntas,MathContext.DECIMAL128);
		
		if(salidaInterbancario.VentasPuntasT0.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeVentaT0 = montoParaPromedioVentaT0.divide(salidaInterbancario.VentasPuntasT0,MathContext.DECIMAL128);
		if(salidaInterbancario.VentasPuntasT1.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeVentaT1 = montoParaPromedioVentaT1.divide(salidaInterbancario.VentasPuntasT1,MathContext.DECIMAL128);
		if(salidaInterbancario.VentasPuntasT2.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeVentaT2 = montoParaPromedioVentaT2.divide(salidaInterbancario.VentasPuntasT2,MathContext.DECIMAL128);
		if(salidaInterbancario.VentasPuntasT3.compareTo(BigDecimal.ZERO) != 0) salidaInterbancario.PromedioDeVentaT3 = montoParaPromedioVentaT3.divide(salidaInterbancario.VentasPuntasT3,MathContext.DECIMAL128);
		if(salidaInterbancario.VentasPuntas.compareTo(BigDecimal.ZERO) != 0)   salidaInterbancario.PromedioDeVenta   = montoParaPromedioVenta.divide(salidaInterbancario.VentasPuntas,MathContext.DECIMAL128);

		salidaInterbancario.MontoParaPromedioCompra = montoParaPromedioCompra;
		salidaInterbancario.MontoParaPromedioCompraT0 = montoParaPromedioCompraT0;
		salidaInterbancario.MontoParaPromedioCompraT1 = montoParaPromedioCompraT1;
		salidaInterbancario.MontoParaPromedioCompraT2 = montoParaPromedioCompraT2;
		salidaInterbancario.MontoParaPromedioCompraT3 = montoParaPromedioCompraT3;
		salidaInterbancario.MontoParaPromedioVenta = montoParaPromedioVenta;
		salidaInterbancario.MontoParaPromedioVentaT0 = montoParaPromedioVentaT0;
		salidaInterbancario.MontoParaPromedioVentaT1 = montoParaPromedioVentaT1;
		salidaInterbancario.MontoParaPromedioVentaT2 = montoParaPromedioVentaT2;
		salidaInterbancario.MontoParaPromedioVentaT3 = montoParaPromedioVentaT3;
		logger.debug("FIN::ConsultaPosicion.obtenerInterbancario");
		return salidaInterbancario;
		
	}
	
	private List<MovimientoSpot> obtenerMovimientosArbitrajeInterbancarios(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosArbitrajeInterbancarios");
		List<MovimientoSpot> movimientosArbitrajeInterbancarios = movimientosSpot.parallelStream().filter(movimiento->"ARBINT".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosArbitrajeInterbancarios");
		return movimientosArbitrajeInterbancarios;
	}
	
	private SalidaArbitrajeInterbancario obtenerArbitrajeInterbancario(List<MovimientoSpot> movimientosArbitrajeInterbancario, Resultado resultadoArbitrajeInterbancario) {
		logger.debug("INICIO::ConsultaPosicion.obtenerArbitrajeInterbancario");
		SalidaArbitrajeInterbancario salidaArbitrajeInterbancario = new SalidaArbitrajeInterbancario();
		salidaArbitrajeInterbancario.ResultadoArbitrajes = resultadoArbitrajeInterbancario.Consolidado;
		
		movimientosArbitrajeInterbancario.stream().forEach(movimiento->{
			if(movimiento.IdTipoOperacion.substring(0,3).equals("COM")) {
				salidaArbitrajeInterbancario.PosicionPorArbitrajesCom = salidaArbitrajeInterbancario.PosicionPorArbitrajes.add(movimiento.Margen);
			}else
			{
				salidaArbitrajeInterbancario.PosicionPorArbitrajesVen = salidaArbitrajeInterbancario.PosicionPorArbitrajes.add(movimiento.Margen);
			}
		});
		movimientosArbitrajeInterbancario.stream().forEach(movimiento->{
			salidaArbitrajeInterbancario.PosicionPorArbitrajes = salidaArbitrajeInterbancario.PosicionPorArbitrajes.add(movimiento.Margen);
		});
		
		
		logger.debug("FIN::ConsultaPosicion.obtenerArbitrajeInterbancario");
		
		return salidaArbitrajeInterbancario;
	}
	
	private List<MovimientoSpot> obtenerMovimientosDistribucion(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosDistribucion");
		List<MovimientoSpot> movimientosDistribucion = movimientosSpot.parallelStream().filter(movimiento->"DISTRI".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		List<MovimientoSpot> movimientosDistribucionArbDis = movimientosSpot.parallelStream().filter(movimiento->"ARBDIS".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		movimientosDistribucion.addAll(movimientosDistribucionArbDis);
		
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosDistribucion");
		return movimientosDistribucion;
	}
	
	private SalidaDistribucion obtenerDistribucion(List<MovimientoSpot> movimientosDistribucion, Fecha fechas,EntradaConsultarPosicion entrada, DistribucionTCCosto DistribucionCosto) {
		logger.debug("INICIO::ConsultaPosicion.obtenerDistribucion");
		SalidaDistribucion salidaDistribucion = new SalidaDistribucion();

		BigDecimal cantidadCompraT0 = BigDecimal.ZERO;
		BigDecimal cantidadCompraT1 = BigDecimal.ZERO;
		BigDecimal cantidadCompraT2 = BigDecimal.ZERO;
		BigDecimal cantidadCompraT3 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT0 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT1 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT2 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT3 = BigDecimal.ZERO;

		BigDecimal montoParaPromedioCompra = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT0 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT1 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT2 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioCompraT3 = BigDecimal.ZERO;
		
		BigDecimal montoParaPromedioVenta = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT0 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT1 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT2 = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVentaT3 = BigDecimal.ZERO;
		
		BigDecimal montoPrecioPromedioCompraTemp = BigDecimal.ZERO;

		BigDecimal montoPrecioPromedioVentaTemp = BigDecimal.ZERO;
		
		for(MovimientoSpot movimiento : movimientosDistribucion) {

			//no debe tomar los arbitrajes para estos calculos
			if((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT"))) continue;

			if(movimiento.FechaValuta != null) {
				if(movimiento.FechaValuta.equals(fechas.FechaT0)) salidaDistribucion.ResultadoDistribucionT0 = salidaDistribucion.ResultadoDistribucionT0.add(movimiento.Margen);
				if(movimiento.FechaValuta.equals(fechas.FechaT1)) salidaDistribucion.ResultadoDistribucionT1 = salidaDistribucion.ResultadoDistribucionT1.add(movimiento.Margen);
				if(movimiento.FechaValuta.equals(fechas.FechaT2)) salidaDistribucion.ResultadoDistribucionT2 = salidaDistribucion.ResultadoDistribucionT2.add(movimiento.Margen);
				if(movimiento.FechaValuta.equals(fechas.FechaT3)) salidaDistribucion.ResultadoDistribucionT3 = salidaDistribucion.ResultadoDistribucionT3.add(movimiento.Margen);
			}

			if((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT"))  && movimiento.IdTipoOperacion.equals("COMSPT"))
			{	movimiento.IdTipoOperacion="VENSPT";
			  continue;
				
			}else if ((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT")) && movimiento.IdTipoOperacion.equals("VENSPT")) {
				movimiento.IdTipoOperacion="COMSPT";
				  continue;
			}
			
			if(movimiento.IdTipoOperacion.equals("COMSPT")) {
				
				if(movimiento.FechaValuta.equals(fechas.FechaT0)) cantidadCompraT0 = cantidadCompraT0.add(BigDecimal.ONE);
				if(movimiento.FechaValuta.equals(fechas.FechaT1)) cantidadCompraT1 = cantidadCompraT1.add(BigDecimal.ONE);
				if(movimiento.FechaValuta.equals(fechas.FechaT2)) cantidadCompraT2 = cantidadCompraT2.add(BigDecimal.ONE);
				if(movimiento.FechaValuta.equals(fechas.FechaT3)) cantidadCompraT3 = cantidadCompraT3.add(BigDecimal.ONE);
				
				//MX/CLP
				if(!movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) && movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaLocal)) {
					salidaDistribucion.ComprasDistribucion = salidaDistribucion.ComprasDistribucion.add(movimiento.MontoMonedaPrincipal);
					montoPrecioPromedioCompraTemp = movimiento.MontoMonedaSecundariaTCCosto;
					montoParaPromedioCompra = montoParaPromedioCompra.add(montoPrecioPromedioCompraTemp, MathContext.DECIMAL128);
					if (movimiento.FechaValuta.equals(fechas.FechaT0)) {
						montoParaPromedioCompraT0 = montoParaPromedioCompraT0.add(montoPrecioPromedioCompraTemp, MathContext.DECIMAL128);
						salidaDistribucion.ComprasDistribucionT0 = salidaDistribucion.ComprasDistribucionT0.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT0 = DistribucionCosto.MontoCompraT0.add(movimiento.MontoMonedaSecundariaTCCosto);
					}
					if (movimiento.FechaValuta.equals(fechas.FechaT1)) {
						montoParaPromedioCompraT1 = montoParaPromedioCompraT1.add(montoPrecioPromedioCompraTemp, MathContext.DECIMAL128);
						salidaDistribucion.ComprasDistribucionT1 = salidaDistribucion.ComprasDistribucionT1.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT1 = DistribucionCosto.MontoCompraT1.add(movimiento.MontoMonedaSecundariaTCCosto);
					}
					if (movimiento.FechaValuta.equals(fechas.FechaT2)) {
						montoParaPromedioCompraT2 = montoParaPromedioCompraT2.add(montoPrecioPromedioCompraTemp, MathContext.DECIMAL128);
						salidaDistribucion.ComprasDistribucionT2 = salidaDistribucion.ComprasDistribucionT2.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT2 = DistribucionCosto.MontoCompraT2.add(movimiento.MontoMonedaSecundariaTCCosto);
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						montoParaPromedioCompraT3 = montoParaPromedioCompraT3.add(montoPrecioPromedioCompraTemp, MathContext.DECIMAL128);
						salidaDistribucion.ComprasDistribucionT3 = salidaDistribucion.ComprasDistribucionT3.add(movimiento.MontoMonedaPrincipal);
	                    DistribucionCosto.MontoCompraT3 = DistribucionCosto.MontoCompraT3.add(movimiento.MontoMonedaSecundariaTCCosto);
	                }
				//MX/MX
				}else if(!movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) && !movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaLocal) && movimiento.IdMonedaPrincipal.equals(entrada.p_IdMoneda) || movimiento.IdMonedaSecundaria.equals(entrada.p_IdMoneda))
				{
					montoParaPromedioCompra = montoParaPromedioCompra.add(movimiento.MontoMonedaPrincipal.compareTo(BigDecimal.ZERO) != 0 ? movimiento.MontoMonedaSecundaria.divide(movimiento.MontoMonedaPrincipal, MathContext.DECIMAL128) : BigDecimal.ZERO, MathContext.DECIMAL128);
		            salidaDistribucion.ComprasDistribucion = salidaDistribucion.ComprasDistribucion.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) salidaDistribucion.ComprasDistribucionT0 = salidaDistribucion.ComprasDistribucionT0.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) salidaDistribucion.ComprasDistribucionT1 = salidaDistribucion.ComprasDistribucionT1.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) salidaDistribucion.ComprasDistribucionT2 = salidaDistribucion.ComprasDistribucionT2.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) salidaDistribucion.ComprasDistribucionT3 = salidaDistribucion.ComprasDistribucionT3.add(movimiento.MontoMonedaSecundaria);
				}
				//USD/CLP
				else if (movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) || movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaEquivalente)){
					salidaDistribucion.ComprasDistribucion = salidaDistribucion.ComprasDistribucion.add(movimiento.MontoMonedaPrincipal);
					montoParaPromedioCompra = montoParaPromedioCompra.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) montoParaPromedioCompraT0 = montoParaPromedioCompraT0.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT1) ) montoParaPromedioCompraT1 = montoParaPromedioCompraT1.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT2) ) montoParaPromedioCompraT2 = montoParaPromedioCompraT2.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT3) ) montoParaPromedioCompraT3 = montoParaPromedioCompraT3.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						salidaDistribucion.ComprasDistribucionT0 = salidaDistribucion.ComprasDistribucionT0.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT0 = DistribucionCosto.MontoCompraT0.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto, MathContext.DECIMAL128)); //se utiliza para preciopromedioconsolidado
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						salidaDistribucion.ComprasDistribucionT1 = salidaDistribucion.ComprasDistribucionT1.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT1 = DistribucionCosto.MontoCompraT1.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto, MathContext.DECIMAL128));
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						salidaDistribucion.ComprasDistribucionT2 = salidaDistribucion.ComprasDistribucionT2.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT2 = DistribucionCosto.MontoCompraT2.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto, MathContext.DECIMAL128));
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						salidaDistribucion.ComprasDistribucionT3 = salidaDistribucion.ComprasDistribucionT3.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoCompraT3 = DistribucionCosto.MontoCompraT3.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto, MathContext.DECIMAL128));
					}
				}
			}else if(movimiento.IdTipoOperacion.equals("VENSPT")) {
				if(movimiento.FechaValuta.equals(fechas.FechaT0)) cantidadVentaT0 = cantidadVentaT0.add(BigDecimal.ONE);
				if(movimiento.FechaValuta.equals(fechas.FechaT1)) cantidadVentaT1 = cantidadVentaT1.add(BigDecimal.ONE);
				if(movimiento.FechaValuta.equals(fechas.FechaT2)) cantidadVentaT2 = cantidadVentaT2.add(BigDecimal.ONE);
				if(movimiento.FechaValuta.equals(fechas.FechaT3)) cantidadVentaT3 = cantidadVentaT3.add(BigDecimal.ONE);

				//MX/CLP
				if (!movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) && movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaLocal)) {
				    salidaDistribucion.VentasDistribucion = salidaDistribucion.VentasDistribucion.add(movimiento.MontoMonedaPrincipal);
				    if ((movimiento.ParidadBid != null && movimiento.ParidadBid.compareTo(BigDecimal.ZERO) != 0) ||
				        (movimiento.ParidadMercado != null && movimiento.ParidadMercado.compareTo(BigDecimal.ZERO) != 0)) {				        
				        montoPrecioPromedioVentaTemp = movimiento.MontoMonedaSecundariaTCCosto;
				        montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
				    } else {
				        montoPrecioPromedioVentaTemp = BigDecimal.ZERO;
				    }
					
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						 montoParaPromedioVentaT0 = montoParaPromedioVentaT0.add(montoPrecioPromedioVentaTemp, MathContext.DECIMAL128);
				         salidaDistribucion.VentasDistribucionT0 = salidaDistribucion.VentasDistribucionT0.add(movimiento.MontoMonedaPrincipal);
				         DistribucionCosto.MontoVentaT0 = DistribucionCosto.MontoVentaT0.add(movimiento.MontoMonedaSecundariaTCCosto);			                
				        }
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						montoParaPromedioVentaT1 = montoParaPromedioVentaT1.add(montoPrecioPromedioVentaTemp, MathContext.DECIMAL128);
			            salidaDistribucion.VentasDistribucionT1 = salidaDistribucion.VentasDistribucionT1.add(movimiento.MontoMonedaPrincipal);
			            DistribucionCosto.MontoVentaT1 = DistribucionCosto.MontoVentaT1.add(movimiento.MontoMonedaSecundariaTCCosto);
			        }
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						montoParaPromedioVentaT2 = montoParaPromedioVentaT2.add(montoPrecioPromedioVentaTemp, MathContext.DECIMAL128);
			            salidaDistribucion.VentasDistribucionT2 = salidaDistribucion.VentasDistribucionT2.add(movimiento.MontoMonedaPrincipal);
			            DistribucionCosto.MontoVentaT2 = DistribucionCosto.MontoVentaT2.add(movimiento.MontoMonedaSecundariaTCCosto);
			        }
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						montoParaPromedioVentaT3 = montoParaPromedioVentaT3.add(montoPrecioPromedioVentaTemp, MathContext.DECIMAL128);
			            salidaDistribucion.VentasDistribucionT3 = salidaDistribucion.VentasDistribucionT3.add(movimiento.MontoMonedaPrincipal);
			            DistribucionCosto.MontoVentaT3 = DistribucionCosto.MontoVentaT3.add(movimiento.MontoMonedaSecundariaTCCosto);
			        }
				//MX/MX
				}else if (!movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) && !movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaLocal) && (movimiento.IdMonedaPrincipal.equals(entrada.p_IdMoneda) || movimiento.IdMonedaSecundaria.equals(entrada.p_IdMoneda))) {
				    salidaDistribucion.VentasDistribucion = salidaDistribucion.VentasDistribucion.add(movimiento.MontoMonedaSecundaria);
				    montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) salidaDistribucion.VentasDistribucionT0 = salidaDistribucion.VentasDistribucionT0.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) salidaDistribucion.VentasDistribucionT1 = salidaDistribucion.VentasDistribucionT1.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) salidaDistribucion.VentasDistribucionT2 = salidaDistribucion.VentasDistribucionT2.add(movimiento.MontoMonedaSecundaria);
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) salidaDistribucion.VentasDistribucionT3 = salidaDistribucion.VentasDistribucionT3.add(movimiento.MontoMonedaSecundaria);
				}
				//USD/CLP
				else if (movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) || movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaEquivalente)){
					salidaDistribucion.VentasDistribucion = salidaDistribucion.VentasDistribucion.add(movimiento.MontoMonedaPrincipal);
					montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) montoParaPromedioVentaT0 = montoParaPromedioVentaT0.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) montoParaPromedioVentaT1 = montoParaPromedioVentaT1.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) montoParaPromedioVentaT2 = montoParaPromedioVentaT2.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) montoParaPromedioVentaT3 = montoParaPromedioVentaT3.add(movimiento.MontoMonedaSecundariaTCCosto, MathContext.DECIMAL128);
					
					if(movimiento.FechaValuta.equals(fechas.FechaT0)) {
						salidaDistribucion.VentasDistribucionT0 = salidaDistribucion.VentasDistribucionT0.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoVentaT0 = DistribucionCosto.MontoVentaT0.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto)); //se utiliza para preciopromedioconsolidado
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT1)) {
						salidaDistribucion.VentasDistribucionT1 = salidaDistribucion.VentasDistribucionT1.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoVentaT1 = DistribucionCosto.MontoVentaT1.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto));
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT2)) {
						salidaDistribucion.VentasDistribucionT2 = salidaDistribucion.VentasDistribucionT2.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoVentaT2 = DistribucionCosto.MontoVentaT2.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto));
					}
					if(movimiento.FechaValuta.equals(fechas.FechaT3)) {
						salidaDistribucion.VentasDistribucionT3 = salidaDistribucion.VentasDistribucionT3.add(movimiento.MontoMonedaPrincipal);
						DistribucionCosto.MontoVentaT3 = DistribucionCosto.MontoVentaT3.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto));
					}
				}
			}
		}
		
		salidaDistribucion.Posicion = salidaDistribucion.ComprasDistribucion.subtract(salidaDistribucion.VentasDistribucion, MathContext.DECIMAL128);
		salidaDistribucion.PosicionT0 = salidaDistribucion.ComprasDistribucionT0.subtract(salidaDistribucion.VentasDistribucionT0, MathContext.DECIMAL128);
		salidaDistribucion.PosicionT1 = salidaDistribucion.ComprasDistribucionT1.subtract(salidaDistribucion.VentasDistribucionT1, MathContext.DECIMAL128);
		salidaDistribucion.PosicionT2 = salidaDistribucion.ComprasDistribucionT2.subtract(salidaDistribucion.VentasDistribucionT2, MathContext.DECIMAL128);
		salidaDistribucion.PosicionT3 = salidaDistribucion.ComprasDistribucionT3.subtract(salidaDistribucion.VentasDistribucionT3, MathContext.DECIMAL128);
		
		salidaDistribucion.ResultadoDistribucion = salidaDistribucion.ResultadoDistribucionT0.add(salidaDistribucion.ResultadoDistribucionT1, MathContext.DECIMAL128).add(salidaDistribucion.ResultadoDistribucionT2, MathContext.DECIMAL128).add(salidaDistribucion.ResultadoDistribucionT3, MathContext.DECIMAL128);

		if(cantidadCompraT0.compareTo(BigDecimal.ZERO) != 0) { 
			if (salidaDistribucion.ComprasDistribucionT0.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeCompraT0 = montoParaPromedioCompraT0.divide(salidaDistribucion.ComprasDistribucionT0, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeCompraT0 = BigDecimal.ZERO;
		}
		
		if(cantidadCompraT1.compareTo(BigDecimal.ZERO) != 0) {
			if (salidaDistribucion.ComprasDistribucionT1.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeCompraT1 = montoParaPromedioCompraT1.divide(salidaDistribucion.ComprasDistribucionT1, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeCompraT1 = BigDecimal.ZERO;
		} if(cantidadCompraT2.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.ComprasDistribucionT2.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeCompraT2 = montoParaPromedioCompraT2.divide(salidaDistribucion.ComprasDistribucionT2, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeCompraT2 = BigDecimal.ZERO;
		}	
		
		if(cantidadCompraT3.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.ComprasDistribucionT3.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeCompraT3 = montoParaPromedioCompraT3.divide(salidaDistribucion.ComprasDistribucionT3, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeCompraT3 = BigDecimal.ZERO;
		}
		
		if(salidaDistribucion.ComprasDistribucion.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.ComprasDistribucion.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeCompra = montoParaPromedioCompra.divide(salidaDistribucion.ComprasDistribucion,MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeCompra = BigDecimal.ZERO;
		}
		
		if(cantidadVentaT0.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.VentasDistribucionT0.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeVentaT0 = montoParaPromedioVentaT0.divide(salidaDistribucion.VentasDistribucionT0, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeVentaT0 = BigDecimal.ZERO;
		}
		
		if(cantidadVentaT1.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.VentasDistribucionT1.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeVentaT1 = montoParaPromedioVentaT1.divide(salidaDistribucion.VentasDistribucionT1, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeVentaT1 = BigDecimal.ZERO;
		}
		
		if(cantidadVentaT2.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.VentasDistribucionT2.compareTo(BigDecimal.ZERO)!= 0) 
				salidaDistribucion.PromedioDeVentaT2 = montoParaPromedioVentaT2.divide(salidaDistribucion.VentasDistribucionT2, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeVentaT2 = BigDecimal.ZERO;
		}
		
		if(cantidadVentaT3.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.VentasDistribucionT3.compareTo(BigDecimal.ZERO) != 0) 
				salidaDistribucion.PromedioDeVentaT3 = montoParaPromedioVentaT3.divide(salidaDistribucion.VentasDistribucionT3, MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeVentaT3 = BigDecimal.ZERO;
		}
		
		if(salidaDistribucion.VentasDistribucion.compareTo(BigDecimal.ZERO) != 0) {
			if(salidaDistribucion.VentasDistribucion.compareTo(BigDecimal.ZERO) != 0)
				salidaDistribucion.PromedioDeVenta = montoParaPromedioVenta.divide(salidaDistribucion.VentasDistribucion,MathContext.DECIMAL128);
		} else {
			salidaDistribucion.PromedioDeVenta = BigDecimal.ZERO;
		}

		salidaDistribucion.MontoParaPromedioCompra = montoParaPromedioCompra;
		salidaDistribucion.MontoParaPromedioCompraT0 = montoParaPromedioCompraT0;
		salidaDistribucion.MontoParaPromedioCompraT1 = montoParaPromedioCompraT1;
		salidaDistribucion.MontoParaPromedioCompraT2 = montoParaPromedioCompraT2;
		salidaDistribucion.MontoParaPromedioCompraT3 = montoParaPromedioCompraT3;
		salidaDistribucion.MontoParaPromedioVenta = montoParaPromedioVenta;
		salidaDistribucion.MontoParaPromedioVentaT0 = montoParaPromedioVentaT0;
		salidaDistribucion.MontoParaPromedioVentaT1 = montoParaPromedioVentaT1;
		salidaDistribucion.MontoParaPromedioVentaT2 = montoParaPromedioVentaT2;
		salidaDistribucion.MontoParaPromedioVentaT3 = montoParaPromedioVentaT3;
		
		
		logger.debug("FIN::ConsultaPosicion.obtenerDistribucion");
		return salidaDistribucion;
	}
	
	private List<MovimientoSpot> obtenerMovimientosArbitrajeDistribucion(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosArbitrajeDistribucion");
		List<MovimientoSpot> movimientosArbitrajeDistribucion = movimientosSpot.parallelStream().filter(movimiento->"ARBDIS".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosArbitrajeDistribucion");
		return movimientosArbitrajeDistribucion;
	}
	
	private SalidaArbitrajeDistribucion obtenerArbitrajeDistribucion(List<MovimientoSpot> movimientosArbitrajeDistribucion, Resultado resultadoArbitrajeDistribucion, Fecha fechas) {
		logger.debug("INICIO::ConsultaPosicion.obtenerArbitrajeDistribucion");
		SalidaArbitrajeDistribucion salidaArbitrajeDistribucion = new SalidaArbitrajeDistribucion();
		
		movimientosArbitrajeDistribucion.stream().forEach(movimiento->{

			//Margen
			if(movimiento.FechaValuta.equals(fechas.FechaT0)) salidaArbitrajeDistribucion.MargenArbitrajesT0 = salidaArbitrajeDistribucion.MargenArbitrajesT0.add(movimiento.Margen, MathContext.DECIMAL128);			
			if(movimiento.FechaValuta.equals(fechas.FechaT1)) salidaArbitrajeDistribucion.MargenArbitrajesT1 = salidaArbitrajeDistribucion.MargenArbitrajesT1.add(movimiento.Margen, MathContext.DECIMAL128);		
			if(movimiento.FechaValuta.equals(fechas.FechaT2)) salidaArbitrajeDistribucion.MargenArbitrajesT2 = salidaArbitrajeDistribucion.MargenArbitrajesT2.add(movimiento.Margen, MathContext.DECIMAL128);			
			if(movimiento.FechaValuta.equals(fechas.FechaT3)) salidaArbitrajeDistribucion.MargenArbitrajesT3 = salidaArbitrajeDistribucion.MargenArbitrajesT3.add(movimiento.Margen, MathContext.DECIMAL128);
			
			salidaArbitrajeDistribucion.MargenArbitrajes = salidaArbitrajeDistribucion.MargenArbitrajes.add(movimiento.Margen, MathContext.DECIMAL128);
			
		});
		salidaArbitrajeDistribucion.ResultadoArbitrajes = resultadoArbitrajeDistribucion.Consolidado;
		salidaArbitrajeDistribucion.ResultadoArbitrajesT0 = resultadoArbitrajeDistribucion.T0;
		salidaArbitrajeDistribucion.ResultadoArbitrajesT1 = resultadoArbitrajeDistribucion.T1;
		salidaArbitrajeDistribucion.ResultadoArbitrajesT2 = resultadoArbitrajeDistribucion.T2;
		salidaArbitrajeDistribucion.ResultadoArbitrajesT3 = resultadoArbitrajeDistribucion.T3;
		
		
		logger.debug("FIN::ConsultaPosicion.obtenerArbitrajeDistribucion");
		return salidaArbitrajeDistribucion;
	}
	
	private CompraVentaForward obtenerCompraVentaForward(List<MovimientoForward> movimientosForward, String idMoneda) {
		logger.debug("INICIO::ConsultaPosicion.obtenerCompraVentaForward");
		CompraVentaForward compraVentaForward = new CompraVentaForward();
		
		movimientosForward.stream()
		.forEach(movimiento->{
			if("COMFWD".equals(movimiento.IdTipoOperacion)) {
				compraVentaForward.Compra = idMoneda.equals(movimiento.IdMonedaPrincipal) ? compraVentaForward.Compra.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128) : compraVentaForward.Compra.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
			}
			if("VENFWD".equals(movimiento.IdTipoOperacion)) {
				compraVentaForward.Venta = idMoneda.equals(movimiento.IdMonedaPrincipal) ? compraVentaForward.Venta.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128) : compraVentaForward.Venta.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
			}
		});
		logger.debug("FIN::ConsultaPosicion.obtenerCompraVentaForward");
		return compraVentaForward;
	}
	
	private SalidaFwd obtenerForward(CompraVentaForward compraVentaForward, CompraVentaForward compraVentaVencidosForward, CompraVentaForward compraVentaFuturosForward) {
		logger.debug("INICIO::ConsultaPosicion.obtenerForward");
		SalidaFwd salidaFwd = new SalidaFwd();
		salidaFwd.ComprasFwd = compraVentaForward.Compra;
		salidaFwd.VentasFwd = compraVentaForward.Venta;
		salidaFwd.Descalce = salidaFwd.ComprasFwd.subtract(salidaFwd.VentasFwd, MathContext.DECIMAL128);
		salidaFwd.VFisicoCompras = compraVentaVencidosForward.Compra;
		salidaFwd.VFisicoVentas = compraVentaVencidosForward.Venta;
		salidaFwd.VFisicoNeto = salidaFwd.VFisicoCompras.subtract(salidaFwd.VFisicoVentas, MathContext.DECIMAL128);
		salidaFwd.VFutCompras = compraVentaFuturosForward.Venta; //Vencimiento Futuro se ve al revez porque indica lo que debo cubrir para la operacion de t+1
		salidaFwd.VFutVentas = compraVentaFuturosForward.Compra;
		salidaFwd.VFutNeto = salidaFwd.VFutCompras.subtract(salidaFwd.VFutVentas, MathContext.DECIMAL128);
		logger.debug("FIN::ConsultaPosicion.obtenerForward");
		return salidaFwd;
	}
	
	private SalidaResumenOperacionesSpot obtenerResumenOperacionesSpot(List<MovimientoSpot> movimientosSpot) {
		logger.debug("INICIO::ConsultaPosicion.obtenerResumenOperacionesSpot");
		SalidaResumenOperacionesSpot salidaResumenOperacionesSpot = new SalidaResumenOperacionesSpot();
		movimientosSpot.stream().forEach(movimiento->{
			if("ANU".equals(movimiento.Estado)) {
				salidaResumenOperacionesSpot.NumeroAnuladas++;
			}
			if("COMSPT".equals(movimiento.IdTipoOperacion) && "VIG".equals(movimiento.Estado)) {
				salidaResumenOperacionesSpot.NumeroCompras++;
			}
			if("VENSPT".equals(movimiento.IdTipoOperacion) && "VIG".equals(movimiento.Estado)) {
				salidaResumenOperacionesSpot.NumeroVentas++;
			}
			
			if(("COMSPT".equals(movimiento.IdTipoOperacion) || "VENSPT".equals(movimiento.IdTipoOperacion)) && ("VIG".equals(movimiento.Estado) || "PEL".equals(movimiento.Estado) || "PDA".equals(movimiento.Estado) || "ANU".equals(movimiento.Estado))) {
				salidaResumenOperacionesSpot.TotalOperaciones++;
			}
		});
		logger.debug("FIN::ConsultaPosicion.obtenerResumenOperacionesSpot");
		return salidaResumenOperacionesSpot;
	}
	
	private SalidaConsolidadoSpot obtenerConsolidadoSpot(SalidaInterbancario interbancario, SalidaDistribucion distribucion, SalidaTipoCambio tipoCambio, DistribucionTCCosto DistribucionCosto, Map<String,Paridad> paridades, String usarPuntaUtilidadPerdida, PuntasSalida puntas) {
		logger.debug("INICIO::ConsultaPosicion.obtenerConsolidadoSpot");
		SalidaConsolidadoSpot salidaConsolidadoSpot = new SalidaConsolidadoSpot();
		Long existeMovT0 = 0L;
		Long existeComprasT0 = 0L;
		Long existeVentasT0 = 0L;
		
		Long existeMovT1 = 0L;
		Long existeComprasT1 = 0L;
		Long existeVentasT1 = 0L;
		
		Long existeMovT2 = 0L;
		Long existeComprasT2 = 0L;
		Long existeVentasT2 = 0L;
		
		Long existeMovT3 = 0L;
		Long existeComprasT3 = 0L;
		Long existeVentasT3 = 0L;

		BigDecimal puntaCompra = BigDecimal.ZERO;
		BigDecimal puntaVenta = BigDecimal.ZERO;
		BigDecimal valorDO = BigDecimal.ZERO;

		BigDecimal montoCompraTotal =interbancario.MontoParaPromedioCompraT0.add(distribucion.MontoParaPromedioCompraT0).add(interbancario.MontoParaPromedioCompraT1.add(distribucion.MontoParaPromedioCompraT1)).add(interbancario.MontoParaPromedioCompraT2.add(distribucion.MontoParaPromedioCompraT2)).add(interbancario.MontoParaPromedioCompraT3.add(distribucion.MontoParaPromedioCompraT3));
		BigDecimal montoVentaTotal =interbancario.MontoParaPromedioVentaT0.add(distribucion.MontoParaPromedioVentaT0).add(interbancario.MontoParaPromedioVentaT1.add(distribucion.MontoParaPromedioVentaT1)).add(interbancario.MontoParaPromedioVentaT2.add(distribucion.MontoParaPromedioVentaT2)).add(interbancario.MontoParaPromedioVentaT3.add(distribucion.MontoParaPromedioVentaT3));
		BigDecimal cantidadCompraTotal = interbancario.ComprasPuntasT0.add(distribucion.ComprasDistribucionT0).add(interbancario.ComprasPuntasT1.add(distribucion.ComprasDistribucionT1)).add(interbancario.ComprasPuntasT2.add(distribucion.ComprasDistribucionT2)).add(interbancario.ComprasPuntasT3.add(distribucion.ComprasDistribucionT3));
		BigDecimal cantidadVentaTotal = interbancario.VentasPuntasT0.add(distribucion.VentasDistribucionT0).add(interbancario.VentasPuntasT1.add(distribucion.VentasDistribucionT1)).add(interbancario.VentasPuntasT2.add(distribucion.VentasDistribucionT2)).add(interbancario.VentasPuntasT3.add(distribucion.VentasDistribucionT3));

		//T0
		{
			
			
			salidaConsolidadoSpot.CompraT0 = interbancario.ComprasPuntasT0.add(distribucion.ComprasDistribucionT0,MathContext.DECIMAL128);
			salidaConsolidadoSpot.VentaT0 = interbancario.VentasPuntasT0.add(distribucion.VentasDistribucionT0,MathContext.DECIMAL128);
			salidaConsolidadoSpot.PosicionT0 = salidaConsolidadoSpot.CompraT0.add(tipoCambio.PosicionInicial, MathContext.DECIMAL128).subtract(salidaConsolidadoSpot.VentaT0, MathContext.DECIMAL128);
			
			BigDecimal montoCompra = interbancario.MontoParaPromedioCompraT0.add(distribucion.MontoParaPromedioCompraT0);
			BigDecimal montoVenta = interbancario.MontoParaPromedioVentaT0.add(distribucion.MontoParaPromedioVentaT0);
			
			salidaConsolidadoSpot.PrecioPromedioCompraT0 = precioPromedio("COMPRA",montoCompra,interbancario.ComprasPuntasT0.add(distribucion.ComprasDistribucionT0),tipoCambio.PosicionInicial,tipoCambio.TipoCambioInicial);
			salidaConsolidadoSpot.PrecioPromedioVentaT0 = precioPromedio("VENTA",montoVenta,interbancario.VentasPuntasT0.add(distribucion.VentasDistribucionT0),tipoCambio.PosicionInicial,tipoCambio.TipoCambioInicial);
			salidaConsolidadoSpot.PrecioPromedioReferencialT0 = precioPromedioReferencial(salidaConsolidadoSpot.PrecioPromedioCompraT0,salidaConsolidadoSpot.PrecioPromedioVentaT0);
			
			
			
		}
		//T1
		{
			
			salidaConsolidadoSpot.CompraT1 = interbancario.ComprasPuntasT1.add(distribucion.ComprasDistribucionT1,MathContext.DECIMAL128);
			salidaConsolidadoSpot.VentaT1 = interbancario.VentasPuntasT1.add(distribucion.VentasDistribucionT1,MathContext.DECIMAL128);
			salidaConsolidadoSpot.PosicionT1 = salidaConsolidadoSpot.CompraT1.add(salidaConsolidadoSpot.PosicionT0, MathContext.DECIMAL128).subtract(salidaConsolidadoSpot.VentaT1, MathContext.DECIMAL128);

			BigDecimal montoCompra = interbancario.MontoParaPromedioCompraT1.add(distribucion.MontoParaPromedioCompraT1);
			BigDecimal montoVenta = interbancario.MontoParaPromedioVentaT1.add(distribucion.MontoParaPromedioVentaT1);

			salidaConsolidadoSpot.PrecioPromedioCompraT1 = precioPromedio("COMPRA",montoCompra,interbancario.ComprasPuntasT1.add(distribucion.ComprasDistribucionT1),salidaConsolidadoSpot.PosicionT0,tipoCambio.TipoCambioInicialT0);
			salidaConsolidadoSpot.PrecioPromedioVentaT1 = precioPromedio("VENTA",montoVenta,interbancario.VentasPuntasT1.add(distribucion.VentasDistribucionT1),salidaConsolidadoSpot.PosicionT0,tipoCambio.TipoCambioInicialT0);
			salidaConsolidadoSpot.PrecioPromedioReferencialT1 = precioPromedioReferencial(salidaConsolidadoSpot.PrecioPromedioCompraT1,salidaConsolidadoSpot.PrecioPromedioVentaT1);
			
			
			
		}
		//T2
		{
			salidaConsolidadoSpot.CompraT2 = interbancario.ComprasPuntasT2.add(distribucion.ComprasDistribucionT2,MathContext.DECIMAL128);
			salidaConsolidadoSpot.VentaT2 = interbancario.VentasPuntasT2.add(distribucion.VentasDistribucionT2,MathContext.DECIMAL128);
			salidaConsolidadoSpot.PosicionT2 = salidaConsolidadoSpot.CompraT2.add(salidaConsolidadoSpot.PosicionT1, MathContext.DECIMAL128).subtract(salidaConsolidadoSpot.VentaT2, MathContext.DECIMAL128);

			BigDecimal montoCompra = interbancario.MontoParaPromedioCompraT2.add(distribucion.MontoParaPromedioCompraT2);
			BigDecimal montoVenta = interbancario.MontoParaPromedioVentaT2.add(distribucion.MontoParaPromedioVentaT2);

			salidaConsolidadoSpot.PrecioPromedioCompraT2 = precioPromedio("COMPRA",montoCompra,interbancario.ComprasPuntasT2.add(distribucion.ComprasDistribucionT2),salidaConsolidadoSpot.PosicionT1,tipoCambio.TipoCambioInicialT1);
			salidaConsolidadoSpot.PrecioPromedioVentaT2 = precioPromedio("VENTA",montoVenta,interbancario.VentasPuntasT2.add(distribucion.VentasDistribucionT2),salidaConsolidadoSpot.PosicionT1,tipoCambio.TipoCambioInicialT1);
			salidaConsolidadoSpot.PrecioPromedioReferencialT2 = precioPromedioReferencial(salidaConsolidadoSpot.PrecioPromedioCompraT2,salidaConsolidadoSpot.PrecioPromedioVentaT2);

		}
		//T3
		{
			salidaConsolidadoSpot.CompraT3 = interbancario.ComprasPuntasT3.add(distribucion.ComprasDistribucionT3,MathContext.DECIMAL128);
			salidaConsolidadoSpot.VentaT3 = interbancario.VentasPuntasT3.add(distribucion.VentasDistribucionT3,MathContext.DECIMAL128);
			salidaConsolidadoSpot.PosicionT3 = salidaConsolidadoSpot.CompraT3.add(salidaConsolidadoSpot.PosicionT2, MathContext.DECIMAL128).subtract(salidaConsolidadoSpot.VentaT3, MathContext.DECIMAL128);
			
			BigDecimal montoCompra = interbancario.MontoParaPromedioCompraT3.add(distribucion.MontoParaPromedioCompraT3);
			BigDecimal montoVenta = interbancario.MontoParaPromedioVentaT3.add(distribucion.MontoParaPromedioVentaT3);

			salidaConsolidadoSpot.PrecioPromedioCompraT3 = precioPromedio("COMPRA",montoCompra,interbancario.ComprasPuntasT3.add(distribucion.ComprasDistribucionT3),salidaConsolidadoSpot.PosicionT2,tipoCambio.TipoCambioInicialT2);
			salidaConsolidadoSpot.PrecioPromedioVentaT3 = precioPromedio("VENTA",montoVenta,interbancario.VentasPuntasT3.add(distribucion.VentasDistribucionT3),salidaConsolidadoSpot.PosicionT2,tipoCambio.TipoCambioInicialT2);
			salidaConsolidadoSpot.PrecioPromedioReferencialT3 = precioPromedioReferencial(salidaConsolidadoSpot.PrecioPromedioCompraT3,salidaConsolidadoSpot.PrecioPromedioVentaT3);


		}
		
		//CONSOLIDADO
		{
			Long compras = existeComprasT0 + existeComprasT1 + existeComprasT2 + existeComprasT3;
			Long ventas = existeVentasT0 + existeVentasT1 + existeVentasT2 + existeVentasT3;
			Long totales = existeMovT0 + existeMovT1 + existeMovT2 + existeMovT3;
			
			int NroTC_Compra = 0;
			int NroTC_Venta = 0;
			
			salidaConsolidadoSpot.Compra = interbancario.ComprasPuntas.add(distribucion.ComprasDistribucion,MathContext.DECIMAL128);
			salidaConsolidadoSpot.Venta = interbancario.VentasPuntas.add(distribucion.VentasDistribucion,MathContext.DECIMAL128);
			
			salidaConsolidadoSpot.Posicion = tipoCambio.PosicionInicial.add(salidaConsolidadoSpot.Compra,MathContext.DECIMAL128).subtract(salidaConsolidadoSpot.Venta,MathContext.DECIMAL128);
			
			//Precio promedio

			salidaConsolidadoSpot.PrecioPromedioCompra = precioPromedio("COMPRA",montoCompraTotal,cantidadCompraTotal,tipoCambio.PosicionInicial,tipoCambio.TipoCambioInicial);
			salidaConsolidadoSpot.PrecioPromedioVenta = precioPromedio("VENTA",montoVentaTotal,cantidadVentaTotal,tipoCambio.PosicionInicial,tipoCambio.TipoCambioInicial);
			
			//Utilidad Perdida
			if("S".equals(usarPuntaUtilidadPerdida)) {
				puntaCompra = puntas.P_OUT_PUNTAS.get(0).PuntaCompra;
				puntaVenta = puntas.P_OUT_PUNTAS.get(0).PuntaVenta;
			}
			
			if(paridades.get("DO") != null) {
				valorDO = paridades.get("DO").TipoCambioMercado;
			}
			salidaConsolidadoSpot.UtilidadPerdidaPosicion = utilidadPerdidaConsolidado(salidaConsolidadoSpot.Posicion, salidaConsolidadoSpot.PrecioPromedioCompra, salidaConsolidadoSpot.PrecioPromedioVenta, puntaCompra, puntaVenta, tipoCambio.TipoCambioCierreIngresado, tipoCambio.TipoCambioInicial, valorDO, usarPuntaUtilidadPerdida);
			salidaConsolidadoSpot.UtilidadPerdidaPosicionT0 = utilidadPerdidaConsolidado(salidaConsolidadoSpot.PosicionT0, salidaConsolidadoSpot.PrecioPromedioCompraT0, salidaConsolidadoSpot.PrecioPromedioVentaT0, puntaCompra, puntaVenta, tipoCambio.TipoCambioCierreIngresadoT0, tipoCambio.TipoCambioInicialT0, valorDO, usarPuntaUtilidadPerdida);
			salidaConsolidadoSpot.UtilidadPerdidaPosicionT1 = utilidadPerdidaConsolidado(salidaConsolidadoSpot.PosicionT1, salidaConsolidadoSpot.PrecioPromedioCompraT1, salidaConsolidadoSpot.PrecioPromedioVentaT1, puntaCompra, puntaVenta, tipoCambio.TipoCambioCierreIngresadoT0, tipoCambio.TipoCambioInicialT0, valorDO, usarPuntaUtilidadPerdida);
			salidaConsolidadoSpot.UtilidadPerdidaPosicionT2 = utilidadPerdidaConsolidado(salidaConsolidadoSpot.PosicionT2, salidaConsolidadoSpot.PrecioPromedioCompraT2, salidaConsolidadoSpot.PrecioPromedioVentaT2, puntaCompra, puntaVenta, tipoCambio.TipoCambioCierreIngresadoT1, tipoCambio.TipoCambioInicialT1, valorDO, usarPuntaUtilidadPerdida);
			salidaConsolidadoSpot.UtilidadPerdidaPosicionT3 = utilidadPerdidaConsolidado(salidaConsolidadoSpot.PosicionT3, salidaConsolidadoSpot.PrecioPromedioCompra, salidaConsolidadoSpot.PrecioPromedioVenta, puntaCompra, puntaVenta, tipoCambio.TipoCambioCierreIngresado, tipoCambio.TipoCambioInicial, valorDO, usarPuntaUtilidadPerdida);
			
			//Resultados
			salidaConsolidadoSpot.ResultadoOperacionesCPAVTA = resultadoOpCPAVTA(salidaConsolidadoSpot.Compra,salidaConsolidadoSpot.Venta,salidaConsolidadoSpot.PrecioPromedioCompra,salidaConsolidadoSpot.PrecioPromedioVenta,tipoCambio.PosicionInicial);
			salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT0 = resultadoOpCPAVTA(salidaConsolidadoSpot.CompraT0,salidaConsolidadoSpot.VentaT0,salidaConsolidadoSpot.PrecioPromedioCompraT0,salidaConsolidadoSpot.PrecioPromedioVentaT0,tipoCambio.PosicionInicial);
			salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT1 = resultadoOpCPAVTA(salidaConsolidadoSpot.CompraT1,salidaConsolidadoSpot.VentaT1,salidaConsolidadoSpot.PrecioPromedioCompraT1,salidaConsolidadoSpot.PrecioPromedioVentaT1,salidaConsolidadoSpot.PosicionT0);
			salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT2 = resultadoOpCPAVTA(salidaConsolidadoSpot.CompraT2,salidaConsolidadoSpot.VentaT2,salidaConsolidadoSpot.PrecioPromedioCompraT2,salidaConsolidadoSpot.PrecioPromedioVentaT2,salidaConsolidadoSpot.PosicionT1);
			salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT3 = resultadoOpCPAVTA(salidaConsolidadoSpot.CompraT3,salidaConsolidadoSpot.VentaT3,salidaConsolidadoSpot.PrecioPromedioCompraT3,salidaConsolidadoSpot.PrecioPromedioVentaT3,salidaConsolidadoSpot.PosicionT2);

			salidaConsolidadoSpot.ResultadoOperacionesConsolidado = salidaConsolidadoSpot.UtilidadPerdidaPosicion.add(salidaConsolidadoSpot.ResultadoOperacionesCPAVTA);
			salidaConsolidadoSpot.ResultadoOperacionesConsolidadoT0 = salidaConsolidadoSpot.UtilidadPerdidaPosicionT0.add(salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT0);
			salidaConsolidadoSpot.ResultadoOperacionesConsolidadoT1 = salidaConsolidadoSpot.UtilidadPerdidaPosicionT1.add(salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT1);
			salidaConsolidadoSpot.ResultadoOperacionesConsolidadoT2 = salidaConsolidadoSpot.UtilidadPerdidaPosicionT2.add(salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT2);
			salidaConsolidadoSpot.ResultadoOperacionesConsolidadoT3 = salidaConsolidadoSpot.UtilidadPerdidaPosicionT3.add(salidaConsolidadoSpot.ResultadoOperacionesCPAVTAT3);
			
			
			
			salidaConsolidadoSpot.PrecioPromedioReferencial = precioPromedioReferencial(salidaConsolidadoSpot.PrecioPromedioCompra,salidaConsolidadoSpot.PrecioPromedioVenta);
			if(VariablesBreak.valorIndicador.equals("S")) {
				DatosBreakEven datosBreakEven = CalculoBreakEven(VariablesBreak.posicionInicial,VariablesBreak.precioInicial,salidaConsolidadoSpot.Compra,salidaConsolidadoSpot.PrecioPromedioCompra,salidaConsolidadoSpot.Venta,salidaConsolidadoSpot.PrecioPromedioVenta); 
				VariablesBreak.BreakEvenCons = datosBreakEven.BreakEven;
				VariablesMontoTotal.MontoTotalConsolidado = datosBreakEven.MontoTotal;
			}
							
		}
		logger.debug("FIN::ConsultaPosicion.obtenerConsolidadoSpot");
		
		return salidaConsolidadoSpot;
	}
	
	private void actualizarTipoDeCambio(SalidaTipoCambio tipoCambio, SalidaConsolidadoSpot consolidadoSpot,Map<String,Paridad> paridades, String usarPuntaUtilidadPerdida, PuntasSalida puntas, Fecha fechas) {
		logger.debug("INICIO::ConsultaPosicion.actualizarTipoDeCambio");
		tipoCambio.PosicionInicialT0 = tipoCambio.PosicionInicial;
		tipoCambio.TipoCambioInicialT0 = tipoCambio.TipoCambioInicial; 
		tipoCambio.ResultadoPosicionInicialT0 = tipoCambio.TipoCambioCierreIngresadoT0.subtract(tipoCambio.TipoCambioInicialT0, MathContext.DECIMAL128).multiply(tipoCambio.PosicionInicialT0, MathContext.DECIMAL128);
		tipoCambio.PosicionInicialT1 = consolidadoSpot.PosicionT0;
		tipoCambio.ResultadoPosicionInicialT1 = tipoCambio.TipoCambioCierreIngresadoT1.subtract(tipoCambio.TipoCambioInicialT1, MathContext.DECIMAL128).multiply(tipoCambio.PosicionInicialT1, MathContext.DECIMAL128);
		tipoCambio.PosicionInicialT2 = consolidadoSpot.PosicionT1;
		tipoCambio.ResultadoPosicionInicialT2 = tipoCambio.TipoCambioCierreIngresadoT2.subtract(tipoCambio.TipoCambioInicialT2, MathContext.DECIMAL128).multiply(tipoCambio.PosicionInicialT2, MathContext.DECIMAL128);
		tipoCambio.PosicionInicialT3 = consolidadoSpot.PosicionT2;
		tipoCambio.ResultadoPosicionInicialT3 = tipoCambio.TipoCambioCierreIngresadoT3.subtract(tipoCambio.TipoCambioInicialT3, MathContext.DECIMAL128).multiply(tipoCambio.PosicionInicialT3, MathContext.DECIMAL128);
		
		//tipo cambio inicial
		int contadorTCInicial = 0;
		contadorTCInicial += tipoCambio.TipoCambioInicialT0.compareTo(BigDecimal.ZERO) != 0  ? 1 : 0;
		contadorTCInicial += tipoCambio.TipoCambioInicialT1.compareTo(BigDecimal.ZERO) != 0  ? 1 : 0;
		contadorTCInicial += tipoCambio.TipoCambioInicialT2.compareTo(BigDecimal.ZERO) != 0  ? 1 : 0;
		contadorTCInicial += tipoCambio.TipoCambioInicialT3.compareTo(BigDecimal.ZERO) != 0  ? 1 : 0;
		
		//resultado posicion inicial
		tipoCambio.ResultadoPosicionInicial = tipoCambio.TipoCambioCierreIngresado.subtract(tipoCambio.TipoCambioInicial,MathContext.DECIMAL128).multiply(tipoCambio.PosicionInicial, MathContext.DECIMAL128);
		
		//celzorro-676
		if(VariablesBreak.valorIndicador.equals("S")) {
			DatosBreakEven datosBreakEvenT0 = CalculoBreakEven(tipoCambio.PosicionInicialT0,tipoCambio.TipoCambioInicialT0,consolidadoSpot.CompraT0,consolidadoSpot.PrecioPromedioCompraT0,consolidadoSpot.VentaT0,consolidadoSpot.PrecioPromedioVentaT0);
			VariablesBreak.BreakEvenT0 = datosBreakEvenT0.BreakEven;
			VariablesMontoTotal.MontoTotalT0 = datosBreakEvenT0.MontoTotal;
			DatosBreakEven datosBreakEvenT1 = CalculoBreakEven(tipoCambio.PosicionInicialT1,tipoCambio.TipoCambioInicialT1,consolidadoSpot.CompraT1,consolidadoSpot.PrecioPromedioCompraT1,consolidadoSpot.VentaT1,consolidadoSpot.PrecioPromedioVentaT1);
			VariablesBreak.BreakEvenT1 = datosBreakEvenT1.BreakEven;
			VariablesMontoTotal.MontoTotalT1 = datosBreakEvenT1.MontoTotal;
			DatosBreakEven datosBreakEvenT2 = CalculoBreakEven(tipoCambio.PosicionInicialT2,tipoCambio.TipoCambioInicialT2,consolidadoSpot.CompraT2,consolidadoSpot.PrecioPromedioCompraT2,consolidadoSpot.VentaT2,consolidadoSpot.PrecioPromedioVentaT2); 
			VariablesBreak.BreakEvenT2 = datosBreakEvenT2.BreakEven;
			VariablesMontoTotal.MontoTotalT2 = datosBreakEvenT2.MontoTotal;
			DatosBreakEven datosBreakEvenT3 = CalculoBreakEven(tipoCambio.PosicionInicialT3,tipoCambio.TipoCambioInicialT3,consolidadoSpot.CompraT3,consolidadoSpot.PrecioPromedioCompraT3,consolidadoSpot.VentaT3,consolidadoSpot.PrecioPromedioVentaT3); 
			VariablesBreak.BreakEvenT3 = datosBreakEvenT3.BreakEven;
			VariablesMontoTotal.MontoTotalT3 = datosBreakEvenT3.MontoTotal;
			
		}
		logger.debug("FIN::ConsultaPosicion.actualizarTipoDeCambio");
	}
	
	private DatosBreakEven CalculoBreakEven(BigDecimal PosicionIncial,BigDecimal PrecioInicial,BigDecimal Compras,BigDecimal PromCompras, BigDecimal Ventas, BigDecimal PromVentas) {
		BreakEvenVariables breakEven = new BreakEvenVariables();
		DatosBreakEven salida = new DatosBreakEven(); 
		breakEven.EquivalentePesosPosicionInicial 				= PosicionIncial.multiply(PrecioInicial).setScale(4, BigDecimal.ROUND_HALF_UP); // preguntar
		breakEven.EquivalentePesosComprasComprasPromedio 	    = Compras.multiply(PromCompras);
		breakEven.EquivalenteEnPesosVentasVentasPromedio		= Ventas.multiply(PromVentas);
		breakEven.PosicionActualMonedaSeleccionada 				= Compras.subtract(Ventas).add(PosicionIncial).setScale(4, BigDecimal.ROUND_HALF_UP);
		breakEven.EquivalenteEnPesosMonedaSeleccionada			= breakEven.EquivalentePesosComprasComprasPromedio.subtract(breakEven.EquivalenteEnPesosVentasVentasPromedio).add(breakEven.EquivalentePesosPosicionInicial).setScale(4, BigDecimal.ROUND_HALF_UP);
		breakEven.TipoCambioBreakEven 							= (!(breakEven.PosicionActualMonedaSeleccionada.compareTo(BigDecimal.ZERO)==0) && !(breakEven.EquivalenteEnPesosMonedaSeleccionada.compareTo(BigDecimal.ZERO)==0))?breakEven.EquivalenteEnPesosMonedaSeleccionada.divide(breakEven.PosicionActualMonedaSeleccionada, MathContext.DECIMAL128).setScale(4, BigDecimal.ROUND_HALF_UP):BigDecimal.ZERO;
		breakEven.EquivalentePesosBreakEven						= breakEven.PosicionActualMonedaSeleccionada.multiply(breakEven.TipoCambioBreakEven);
		breakEven.UtilidadPerdidaBreakEven						= breakEven.EquivalenteEnPesosMonedaSeleccionada.subtract(breakEven.EquivalentePesosBreakEven).setScale(4, BigDecimal.ROUND_HALF_UP); // tiene que dar 0
		salida.BreakEven 										= breakEven.TipoCambioBreakEven;
		salida.MontoTotal										= breakEven.EquivalenteEnPesosMonedaSeleccionada;
		return salida;	
	}
	
	private void actualizaInterbancario(SalidaInterbancario interbancario,SalidaTipoCambio tipoCambio, SalidaConsolidadoSpot consolidadoSpot, List<MovimientoSpot> movimientosInterbancarios, Fecha fechas, String idMoneda) {
		logger.debug("INICIO::ConsultaPosicion.actualizaInterbancario");
		
		movimientosInterbancarios.stream().forEach(movimiento->{
			BigDecimal montoMonedaSeleccionada = BigDecimal.ZERO;
			BigDecimal montoCLP = BigDecimal.ZERO;
			BigDecimal monto = BigDecimal.ZERO;
			BigDecimal tipocambio = BigDecimal.ZERO;
			BigDecimal resultado = BigDecimal.ZERO;
			if(movimiento.FechaValuta.equals(fechas.FechaT0)) tipocambio = tipoCambio.TipoCambioCierreIngresadoT0.compareTo(BigDecimal.ZERO) != 0 ? tipoCambio.TipoCambioCierreIngresadoT0 : consolidadoSpot.PrecioPromedioReferencialT0;
			if(movimiento.FechaValuta.equals(fechas.FechaT1)) tipocambio = tipoCambio.TipoCambioCierreIngresadoT1.compareTo(BigDecimal.ZERO) != 0 ? tipoCambio.TipoCambioCierreIngresadoT1 : consolidadoSpot.PrecioPromedioReferencialT1;
			if(movimiento.FechaValuta.equals(fechas.FechaT2)) tipocambio = tipoCambio.TipoCambioCierreIngresadoT2.compareTo(BigDecimal.ZERO) != 0 ? tipoCambio.TipoCambioCierreIngresadoT2 : consolidadoSpot.PrecioPromedioReferencialT2;
			if(movimiento.FechaValuta.equals(fechas.FechaT3)) tipocambio = tipoCambio.TipoCambioCierreIngresadoT3.compareTo(BigDecimal.ZERO) != 0 ? tipoCambio.TipoCambioCierreIngresadoT3 : consolidadoSpot.PrecioPromedioReferencialT3;
			
			if(idMoneda.equals(movimiento.IdMonedaPrincipal)) {
				montoMonedaSeleccionada = movimiento.MontoMonedaPrincipal;
				montoCLP 				= movimiento.MontoMonedaSecundaria;
				monto = tipocambio.multiply(montoMonedaSeleccionada, MathContext.DECIMAL128);
				resultado = "COMSPT".equals(movimiento.IdTipoOperacion) ? monto.subtract(montoCLP, MathContext.DECIMAL128) : montoCLP.subtract(monto, MathContext.DECIMAL128);
			}else {
				resultado = obtenerMontoResultadoEquivalente(movimiento,tipocambio);
			}
			
			if(movimiento.FechaValuta.equals(fechas.FechaT0)) interbancario.ResultadoInterbancarioT0 = interbancario.ResultadoInterbancarioT0.add(resultado, MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT1)) interbancario.ResultadoInterbancarioT1 = interbancario.ResultadoInterbancarioT1.add(resultado, MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT2)) interbancario.ResultadoInterbancarioT2 = interbancario.ResultadoInterbancarioT2.add(resultado, MathContext.DECIMAL128);
			if(movimiento.FechaValuta.equals(fechas.FechaT3)) interbancario.ResultadoInterbancarioT3 = interbancario.ResultadoInterbancarioT3.add(resultado, MathContext.DECIMAL128);
			
		});
		
		interbancario.ResultadoInterbancario = interbancario.ResultadoInterbancarioT0.add(interbancario.ResultadoInterbancarioT1, MathContext.DECIMAL128).add(interbancario.ResultadoInterbancarioT2, MathContext.DECIMAL128).add(interbancario.ResultadoInterbancarioT3, MathContext.DECIMAL128);
		
		logger.debug("INICIO::ConsultaPosicion.actualizaInterbancario");
		
	}

	private Long obtenerIdPaisEmpresa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CalcularFechaSolicitudAporteRescate.obtenerIdPaisEmpresa");
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresa.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		logger.trace("FIN::CalcularFechaSolicitudAporteRescate.obtenerIdPaisEmpresa");
		return idPaisEmpresa;
	}

	private BigDecimal obtenerMontoResultadoEquivalente(MovimientoSpot movimiento, BigDecimal TipoCambioADiferenciar) {
	    BigDecimal monto = BigDecimal.ZERO;

	    if (movimiento.ParidadBid.compareTo(BigDecimal.ZERO) != 0 && movimiento.ParidadMercado.compareTo(BigDecimal.ZERO) != 0 && 
	    	movimiento.TipoCambioCierre.compareTo(BigDecimal.ZERO) != 0  && movimiento.MontoMonedaPrincipalEquivalente.compareTo(BigDecimal.ZERO) != 0) {

	        if (movimiento.ParidadBid.compareTo(BigDecimal.ZERO) != 0) {
	            if ("M".equals(movimiento.MultiplicaODivide)) {
	                if (movimiento.ParidadMercado != null && movimiento.ParidadMercado.compareTo(BigDecimal.ZERO) != 0) {
	                    monto = movimiento.TipoCambioCierre.compareTo(BigDecimal.ZERO) != 0 ? movimiento.TipoCambioCierre.divide(movimiento.ParidadMercado, MathContext.DECIMAL128): BigDecimal.ZERO;
	                }
	            } else {
	                monto = movimiento.TipoCambioCierre.multiply(movimiento.ParidadMercado, MathContext.DECIMAL128);
	            }
	        } else {
	            if ("M".equals(movimiento.MultiplicaODivide)) {
	                if (movimiento.ParidadBid != null && movimiento.ParidadBid.compareTo(BigDecimal.ZERO) != 0) {
	                    monto = movimiento.TipoCambioCierre.compareTo(BigDecimal.ZERO) !=0 ? movimiento.TipoCambioCierre.divide(movimiento.ParidadBid, MathContext.DECIMAL128): BigDecimal.ZERO;
	                }
	            } else {
	                monto = movimiento.TipoCambioCierre.multiply(movimiento.ParidadBid, MathContext.DECIMAL128);
	            }
	        }

	        if (TipoCambioADiferenciar != null && TipoCambioADiferenciar.compareTo(BigDecimal.ZERO) != 0) {
	            if ("COMSPT".equals(movimiento.IdTipoOperacion)) {
	                monto = TipoCambioADiferenciar.subtract(monto, MathContext.DECIMAL128);
	            } else {
	                monto = monto.subtract(TipoCambioADiferenciar, MathContext.DECIMAL128);
	            }
	        }

	        monto = monto.multiply(movimiento.MontoMonedaPrincipalEquivalente, MathContext.DECIMAL128);
	    }

	    return monto;
	}
	
	private BigDecimal precioPromedio (String tipo, BigDecimal monto, BigDecimal cantidad, BigDecimal posicion, BigDecimal tipoCambio) {
		BigDecimal resultado = BigDecimal.ZERO;
		
			if((tipo.equals("COMPRA") && (posicion.compareTo(BigDecimal.ZERO) > 0)) ||
					(tipo.equals("VENTA") && (posicion.compareTo(BigDecimal.ZERO) < 0))) {

				monto = monto.add(posicion.abs().multiply(tipoCambio, MathContext.DECIMAL128), MathContext.DECIMAL128);
				cantidad = cantidad.add(posicion.abs(), MathContext.DECIMAL128);
			}

			if(monto.compareTo(BigDecimal.ZERO) != 0) {
				resultado = cantidad.compareTo(BigDecimal.ZERO) != 0 ? monto.divide(cantidad, MathContext.DECIMAL128) : BigDecimal.ZERO;
			}
			
		return resultado.setScale(2, RoundingMode.HALF_UP);
	}
		

	private BigDecimal utilidadPerdidaConsolidado (BigDecimal posicionNeta, BigDecimal precioPromedioCompra, BigDecimal precioPromedioVenta, BigDecimal puntaCompra, BigDecimal puntaVenta, BigDecimal tcCierre, BigDecimal tcInicial, BigDecimal valorDO, String usarPuntaUtilidadPerdida) {
		BigDecimal resultado = BigDecimal.ZERO;
		
		if(precioPromedioCompra.compareTo(BigDecimal.ZERO) == 0 && precioPromedioVenta.compareTo(BigDecimal.ZERO) == 0) 
			resultado = BigDecimal.ZERO;
		//Neta Positiva
		else if(posicionNeta.compareTo(BigDecimal.ZERO)==1) {
			
			if("N".equals(usarPuntaUtilidadPerdida)) resultado = tcCierre.subtract(precioPromedioCompra, MathContext.DECIMAL128).multiply(posicionNeta, MathContext.DECIMAL128);
			else{

				BigDecimal valCalculoUtilidadPerdida = BigDecimal.ZERO;
				if(puntaCompra.compareTo(BigDecimal.ZERO)==1) valCalculoUtilidadPerdida = puntaCompra;
				else if (tcCierre.compareTo(BigDecimal.ZERO)==1) valCalculoUtilidadPerdida = tcCierre;
				else if (tcInicial.compareTo(BigDecimal.ZERO)==1) valCalculoUtilidadPerdida = tcInicial;
				else valCalculoUtilidadPerdida = valorDO;
				
				resultado = valCalculoUtilidadPerdida.subtract(precioPromedioCompra, MathContext.DECIMAL128).multiply(posicionNeta, MathContext.DECIMAL128).setScale(0, RoundingMode.HALF_UP);
				
			}
		//Neta Negativa
		}else {   
			
			if("N".equals(usarPuntaUtilidadPerdida)) resultado = precioPromedioVenta.subtract(tcCierre, MathContext.DECIMAL128).multiply(posicionNeta.abs(), MathContext.DECIMAL128);
			else {

				BigDecimal valCalculoUtilidadPerdida = BigDecimal.ZERO;
				if(puntaVenta.compareTo(BigDecimal.ZERO)==1) valCalculoUtilidadPerdida = puntaVenta;
				else if (tcCierre.compareTo(BigDecimal.ZERO)==1) valCalculoUtilidadPerdida = tcCierre;
				else if (tcInicial.compareTo(BigDecimal.ZERO)==1) valCalculoUtilidadPerdida = tcInicial;
				else valCalculoUtilidadPerdida = valorDO;
				
				resultado = precioPromedioVenta.subtract(valCalculoUtilidadPerdida, MathContext.DECIMAL128).multiply(posicionNeta.abs(), MathContext.DECIMAL128).setScale(0, RoundingMode.HALF_UP);
				
			}
			
		}
		return resultado;
	}

	private BigDecimal resultadoOpCPAVTA (BigDecimal compra, BigDecimal venta, BigDecimal precioPromedioCompra, BigDecimal precioPromedioVenta, BigDecimal posicion) {
		BigDecimal resultado = BigDecimal.ZERO;
		BigDecimal ventaAbs =venta;
		
		if(posicion.compareTo(BigDecimal.ZERO) > 0){
			compra = compra.add(posicion);
		}else if(posicion.compareTo(BigDecimal.ZERO) < 0) {
			ventaAbs = venta.add(posicion.abs());
			venta = venta.add(posicion);
		}
		
		if(compra.compareTo(BigDecimal.ZERO)>0 || venta.compareTo(BigDecimal.ZERO)>0) {
			resultado = precioPromedioVenta.subtract(precioPromedioCompra);
			if(compra.compareTo(ventaAbs)>0) {
				resultado = resultado.multiply(ventaAbs, MathContext.DECIMAL128);
			}else {
				resultado = resultado.multiply(compra, MathContext.DECIMAL128);
			}
		}


			
		return resultado;
	}

	private BigDecimal precioPromedioReferencial (BigDecimal PPcompra, BigDecimal PPventa) {
		BigDecimal resultado = BigDecimal.ZERO;

		
		if(PPcompra.compareTo(BigDecimal.ZERO) == 0 || PPventa.compareTo(BigDecimal.ZERO) == 0){
			resultado = PPcompra.add(PPventa);
		}else{
			resultado = PPcompra.add(PPventa, MathContext.DECIMAL128).divide(new BigDecimal(2), MathContext.DECIMAL128);
		}
		return resultado;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("prebe")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
																				//4	
		String jsonRF = "{\"p_IdMoneda\":\"USD\",\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_Resultado\":1,\"p_FechaOperacion\":\"2026-09-02\"}";
		
		EntradaConsultarPosicion Objin = gson.fromJson(jsonRF, EntradaConsultarPosicion.class);																
		ConsultarPosicion  demo=new ConsultarPosicion();				
		try {
			SalidaConsultaPosicion ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}

