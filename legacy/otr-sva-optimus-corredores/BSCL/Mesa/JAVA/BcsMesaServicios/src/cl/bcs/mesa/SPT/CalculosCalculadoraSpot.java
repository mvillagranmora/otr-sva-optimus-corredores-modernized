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

public class CalculosCalculadoraSpot implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CalculosCalculadoraSpot.class);
	
	private final String SQL_SELECT_PARIDADES = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectParidades.sql");
	private final String SQL_SELECT_POSICION_INICIAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectPosicionInicial.sql");
	private final String SQL_SELECT_POSICION_INICIAL_MONEDA = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectPosicionInicialMoneda.sql");
	private final String SQL_SELECT_MOVIMIENTOS_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/CalculosCalculadoraSpot_SelectMovimientosSpot.sql");

	public static class EntradaCalculosCalculadoraSpot{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaOperacion;
		public String p_IdMoneda;
		public BigDecimal p_precio;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	 
	 
	public static class SalidaCalculosCalculadoraSpot{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public String P_OUT_HORA;
		public SalidaTipoCambio P_OUT_TIPODECAMBIO;
		public SalidaInterbancario P_OUT_INTERBANCARIO;
		public SalidaArbitrajeInterbancario P_OUT_ARBITRAJEINTERBANCARIO;
		public SalidaDistribucion P_OUT_DISTRIBUCION;
		public SalidaArbitrajeDistribucion P_OUT_ARBITRAJEDISTRIBUCION;
		public SalidaResumenOperacionesSpot P_OUT_RESUMENOPERACIONESSPOT;
		public SalidaCalculosCalculadoraSpot() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_HORA = "00:00:00";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TIPODECAMBIO = new SalidaTipoCambio();
			this.P_OUT_INTERBANCARIO = new SalidaInterbancario();
			this.P_OUT_ARBITRAJEINTERBANCARIO = new SalidaArbitrajeInterbancario();
			this.P_OUT_DISTRIBUCION = new SalidaDistribucion();
			this.P_OUT_ARBITRAJEDISTRIBUCION = new SalidaArbitrajeDistribucion();
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
		
		public BigDecimal ResultadosVentasCLPT0;
		public BigDecimal ResultadosVentasCLPT1;
		public BigDecimal ResultadosVentasCLPT2;
		public BigDecimal ResultadosVentasCLPT3;
		public BigDecimal ResultadosComprasCLPT0;
		public BigDecimal ResultadosComprasCLPT1;
		public BigDecimal ResultadosComprasCLPT2;
		public BigDecimal ResultadosComprasCLPT3;
		
		public BigDecimal ResultadosVentasUSDT0;
		public BigDecimal ResultadosVentasUSDT1;
		public BigDecimal ResultadosVentasUSDT2;
		public BigDecimal ResultadosVentasUSDT3;
		public BigDecimal ResultadosComprasUSDT0;
		public BigDecimal ResultadosComprasUSDT1;
		public BigDecimal ResultadosComprasUSDT2;
		public BigDecimal ResultadosComprasUSDT3;
		
		public BigDecimal UtilidadCompra;
		public BigDecimal UtilidadVenta;
		
		
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
			
			this.ResultadosVentasCLPT0 = BigDecimal.ZERO;
			this.ResultadosVentasCLPT1 = BigDecimal.ZERO;
			this.ResultadosVentasCLPT2 = BigDecimal.ZERO;
			this.ResultadosVentasCLPT3 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT0 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT1 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT2 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT3 = BigDecimal.ZERO;
			
			this.ResultadosVentasUSDT0 = BigDecimal.ZERO;
			this.ResultadosVentasUSDT1 = BigDecimal.ZERO;
			this.ResultadosVentasUSDT2 = BigDecimal.ZERO;
			this.ResultadosVentasUSDT3 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT0 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT1 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT2 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT3 = BigDecimal.ZERO;
			
			this.UtilidadCompra = BigDecimal.ZERO;
			this.UtilidadVenta = BigDecimal.ZERO;
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
		
		public BigDecimal ResultadosVentasCLPT0;
		public BigDecimal ResultadosVentasCLPT1;
		public BigDecimal ResultadosVentasCLPT2;
		public BigDecimal ResultadosVentasCLPT3;
		public BigDecimal ResultadosComprasCLPT0;
		public BigDecimal ResultadosComprasCLPT1;
		public BigDecimal ResultadosComprasCLPT2;
		public BigDecimal ResultadosComprasCLPT3;
		
		public BigDecimal ResultadosVentasUSDT0;
		public BigDecimal ResultadosVentasUSDT1;
		public BigDecimal ResultadosVentasUSDT2;
		public BigDecimal ResultadosVentasUSDT3;
		public BigDecimal ResultadosComprasUSDT0;
		public BigDecimal ResultadosComprasUSDT1;
		public BigDecimal ResultadosComprasUSDT2;
		public BigDecimal ResultadosComprasUSDT3;
		
		public BigDecimal UtilidadCompra;
		public BigDecimal UtilidadVenta;
		
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
			
			this.ResultadosVentasCLPT0 = BigDecimal.ZERO;
			this.ResultadosVentasCLPT1 = BigDecimal.ZERO;
			this.ResultadosVentasCLPT2 = BigDecimal.ZERO;
			this.ResultadosVentasCLPT3 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT0 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT1 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT2 = BigDecimal.ZERO;
			this.ResultadosComprasCLPT3 = BigDecimal.ZERO;
			
			this.ResultadosVentasUSDT0 = BigDecimal.ZERO;
			this.ResultadosVentasUSDT1 = BigDecimal.ZERO;
			this.ResultadosVentasUSDT2 = BigDecimal.ZERO;
			this.ResultadosVentasUSDT3 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT0 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT1 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT2 = BigDecimal.ZERO;
			this.ResultadosComprasUSDT3 = BigDecimal.ZERO;
				
			this.UtilidadCompra = BigDecimal.ZERO;
			this.UtilidadVenta = BigDecimal.ZERO;
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
		public String FechaValutaRecibimos;
		public String FechaValutaEntregamos;
		public String IdMonedaRecibimos;
		public String IdMonedaEntregamos;
		
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
		EntradaCalculosCalculadoraSpot jin = gson.fromJson(in.toString(), EntradaCalculosCalculadoraSpot.class);
		SalidaCalculosCalculadoraSpot out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	VariablesBreakEven VariablesBreak = new VariablesBreakEven();
	VariablesMontoTotal VariablesMontoTotal = new VariablesMontoTotal();
	public SalidaCalculosCalculadoraSpot logica (Connection con, EntradaCalculosCalculadoraSpot entrada) throws Exception {
		
		HashMap<String,HashMap<String,String>> ValoresIndicadores = Utils.getConfiguraciones(con,entrada.p_IdEmpresa);
		 if(ValoresIndicadores.get("MESA").get("VER_BREAKEVEN")!=null){
				VariablesBreak.valorIndicador =ValoresIndicadores.get("MESA").get("VER_BREAKEVEN");
		 }else {
			 VariablesBreak.valorIndicador="N";
		 }
		 
		logger.debug("INICIO::ConsultaPosicion");
		SalidaCalculosCalculadoraSpot out = new SalidaCalculosCalculadoraSpot();
		
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
		
		List<MovimientoSpot> movimientosVigentesSpot = obtenerMovimientosVigentesSpot(movimientosSpot);
		List<MovimientoSpot> movimientosInterbancarios = obtenerMovimientosInterbancarios(movimientosVigentesSpot);
		List<MovimientoSpot> movimientosDistribucion = obtenerMovimientosDistribucion(movimientosVigentesSpot);
		
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
			out.P_OUT_INTERBANCARIO = obtenerInterbancario(movimientosInterbancarios, fechas, entrada.p_precio);
		}
		
		
		//DISTRIBUCION
		{
			out.P_OUT_DISTRIBUCION = obtenerDistribucion(movimientosDistribucion,fechas,entrada,distribucionCosto);
		}
		

		//TIPO DE CAMBIO
		{
			BigDecimal posicionInicialMoneda = obtenerPosicionInicialMoneda(posicionesIniciales,paridades,tipoParidad, entrada.p_IdMoneda);
			BigDecimal resultadoPosicionInicial = obtenerResultadoPosicionInicial(posicionesIniciales,paridades, entrada.p_IdMoneda);
			out.P_OUT_TIPODECAMBIO = obtenerTipoDeCambio(posicionesInicialesMonedas,paridades,tipoParidad,posicionInicialMoneda,resultadoPosicionInicial,fechas, entrada.p_IdMoneda);
			VariablesBreak.posicionInicial    = out.P_OUT_TIPODECAMBIO.PosicionInicial;
			VariablesBreak.precioInicial	  = out.P_OUT_TIPODECAMBIO.TipoCambioInicial;/**/
		}
		
		
		//RESUMEN OPERACIONES SPOT
		{
			out.P_OUT_RESUMENOPERACIONESSPOT = obtenerResumenOperacionesSpot(movimientosSpot);
		}
		
		

		logger.debug("FIN::ConsultaPosicion");
		return out;
	}
	
	private RevisarRequisitosBasicosSalida validaRequisitosMinimos(Connection con, EntradaCalculosCalculadoraSpot entrada) throws Exception{
		logger.info("INICIO::ConsultaPosicion.validaRequisitosMinimos");
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salida = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, "MSP");
		logger.info("FIN::ConsultaPosicion.validaRequisitosMinimos");
		return salida;
	}
	
	private Map<String,Paridad> obtenerParidades(Connection con, EntradaCalculosCalculadoraSpot entrada) throws Exception{
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
	
	private Fecha obtenerFechas(Connection con,EntradaCalculosCalculadoraSpot entrada,String fechaSistema) throws Exception {
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
	
	private List<PosicionInicial> obtenerPosicionesIniciales(Connection con, EntradaCalculosCalculadoraSpot entrada) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerPosicionesIniciales");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		List<PosicionInicial> posicionesIniciales = sql.query(con, SQL_SELECT_POSICION_INICIAL, params, PosicionInicial.class);
		logger.debug("FIN::ConsultaPosicion.obtenerPosicionesIniciales");
		return posicionesIniciales;
	}
	
	private List<PosicionInicial> obtenerPosicionesInicialesMonedas(Connection con, EntradaCalculosCalculadoraSpot entrada, String fechaInicio, String fechaFinal) throws Exception{
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
	
	private List<MovimientoSpot> obtenerMovimientosSpot(Connection con,EntradaCalculosCalculadoraSpot entrada) throws Exception{
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosSpot");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		params.put("MonedaPrincipal", entrada.p_IdMoneda);
		List<MovimientoSpot> movimientosSpot = sql.query(con, SQL_SELECT_MOVIMIENTOS_SPOT, params, MovimientoSpot.class);
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosSpot");
		return movimientosSpot;
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
			salidaTipoCambio.TipoCambioInicial = posicionInicial.TipoCambioInicial;
			salidaTipoCambio.TipoCambioCierreIngresado = posicionInicial.CierreIngresado;

			//Set precioIngreso a cada valuta
			if(formatoFecha.format(posicionInicial.Fecha).equals(fechas.FechaT0)) {
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
			if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) resultadoInterbancario.T0 = resultadoInterbancario.T0.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT1)) resultadoInterbancario.T1 = resultadoInterbancario.T1.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT2)) resultadoInterbancario.T2 = resultadoInterbancario.T2.add(valor,MathContext.DECIMAL128);
			if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT3)) resultadoInterbancario.T3 = resultadoInterbancario.T3.add(valor,MathContext.DECIMAL128);
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
	
	private SalidaInterbancario obtenerInterbancario(List<MovimientoSpot> movimientosInterbancarios, Fecha fechas, BigDecimal precioCalculadora) {
		logger.debug("INICIO::CalculosCalculadora.obtenerInterbancario");
		SalidaInterbancario salidaInterbancario = new SalidaInterbancario();
		BigDecimal montoParaPromedioCompra = BigDecimal.ZERO;
		BigDecimal montoParaPromedioVenta = BigDecimal.ZERO;
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
			BigDecimal montoAdd = movimiento.MontoMonedaPrincipal;
			if(!movimiento.IdMonedaPrincipal.equals(movimiento.IdMonedaEquivalente) && movimiento.IdMonedaSecundaria.equals(movimiento.IdMonedaLocal)) {
				
				if("COMSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.ComprasPuntas = salidaInterbancario.ComprasPuntas.add(montoAdd,MathContext.DECIMAL128);
					montoParaPromedioCompra = montoParaPromedioCompra.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					salidaInterbancario.UtilidadCompra = salidaInterbancario.UtilidadCompra.add(montoAdd.multiply(precioCalculadora.subtract(movimiento.TipoCambioCierre,MathContext.DECIMAL128),MathContext.DECIMAL128) ,MathContext.DECIMAL128);
					
					if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaRecibimos.equals("USD")) {
							salidaInterbancario.ResultadosComprasUSDT0 = salidaInterbancario.ResultadosComprasUSDT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						}
						
					}
					if(movimiento.FechaValutaEntregamos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("CLP") && movimiento.IdMonedaSecundaria.equalsIgnoreCase("CLP")) {
							salidaInterbancario.ResultadosComprasCLPT0 = salidaInterbancario.ResultadosComprasCLPT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}
						if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("USD")&& movimiento.IdMonedaSecundaria.equalsIgnoreCase("USD")) {
							salidaInterbancario.ResultadosComprasUSDT0 = salidaInterbancario.ResultadosComprasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}
						
					}
				}else if("VENSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.VentasPuntas = salidaInterbancario.VentasPuntas.add(montoAdd,MathContext.DECIMAL128);
					montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					salidaInterbancario.UtilidadVenta = salidaInterbancario.UtilidadVenta.add(montoAdd.multiply(movimiento.TipoCambioCierre.subtract(precioCalculadora,MathContext.DECIMAL128),MathContext.DECIMAL128) ,MathContext.DECIMAL128);
					if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaRecibimos.equals("CLP")&& movimiento.IdMonedaSecundaria.equalsIgnoreCase("CLP")) {
							salidaInterbancario.ResultadosVentasUSDT0 = salidaInterbancario.ResultadosVentasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}
						if (movimiento.IdMonedaRecibimos.equals("USD") && movimiento.IdMonedaSecundaria.equalsIgnoreCase("USD")) {
							salidaInterbancario.ResultadosVentasUSDT0 = salidaInterbancario.ResultadosVentasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}	
						
					}
					if(movimiento.FechaValutaEntregamos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("USD")&& movimiento.IdMonedaPrincipal.equalsIgnoreCase("USD")) {
							salidaInterbancario.ResultadosVentasUSDT0 = salidaInterbancario.ResultadosVentasUSDT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						}	
					}
					
				}
			}else {
				
				
				if("COMSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.ComprasPuntas = salidaInterbancario.ComprasPuntas.add(montoAdd,MathContext.DECIMAL128);
					montoParaPromedioCompra = montoParaPromedioCompra.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					salidaInterbancario.UtilidadCompra = salidaInterbancario.UtilidadCompra.add(montoAdd.multiply(precioCalculadora.subtract(movimiento.TipoCambioCierre,MathContext.DECIMAL128),MathContext.DECIMAL128) ,MathContext.DECIMAL128);
					if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaRecibimos.equals("USD")) {
							salidaInterbancario.ResultadosComprasUSDT0 = salidaInterbancario.ResultadosComprasUSDT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						}
						
					}
					if(movimiento.FechaValutaEntregamos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("CLP") && movimiento.IdMonedaSecundaria.equalsIgnoreCase("CLP")) {
							salidaInterbancario.ResultadosComprasCLPT0 = salidaInterbancario.ResultadosComprasCLPT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}
						if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("USD")&& movimiento.IdMonedaSecundaria.equalsIgnoreCase("USD")) {
							salidaInterbancario.ResultadosComprasUSDT0 = salidaInterbancario.ResultadosComprasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}
						
					}

				}else if("VENSPT".equals(movimiento.IdTipoOperacion)) {
					salidaInterbancario.VentasPuntas = salidaInterbancario.VentasPuntas.add(montoAdd,MathContext.DECIMAL128);
					montoParaPromedioVenta = montoParaPromedioVenta.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					salidaInterbancario.UtilidadVenta = salidaInterbancario.UtilidadVenta.add(montoAdd.multiply(movimiento.TipoCambioCierre.subtract(precioCalculadora,MathContext.DECIMAL128),MathContext.DECIMAL128) ,MathContext.DECIMAL128);
					if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaRecibimos.equals("CLP")&& movimiento.IdMonedaSecundaria.equalsIgnoreCase("CLP")) {
							salidaInterbancario.ResultadosVentasCLPT0 = salidaInterbancario.ResultadosVentasCLPT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}
						if (movimiento.IdMonedaRecibimos.equals("USD") && movimiento.IdMonedaSecundaria.equalsIgnoreCase("USD")) {
							salidaInterbancario.ResultadosVentasUSDT0 = salidaInterbancario.ResultadosVentasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
						}	
						
					}
					if(movimiento.FechaValutaEntregamos.equals(fechas.FechaT0)) {
						if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("USD")&& movimiento.IdMonedaPrincipal.equalsIgnoreCase("USD")) {
							salidaInterbancario.ResultadosVentasUSDT0 = salidaInterbancario.ResultadosVentasUSDT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
						}	
					}
				}
			}
			
			
		}
		
		if(salidaInterbancario.ComprasPuntas.compareTo(BigDecimal.ZERO) != 0)   salidaInterbancario.PromedioDeCompra   = montoParaPromedioCompra.divide(salidaInterbancario.ComprasPuntas,MathContext.DECIMAL128);
		if(salidaInterbancario.VentasPuntas.compareTo(BigDecimal.ZERO) != 0)   salidaInterbancario.PromedioDeVenta   = montoParaPromedioVenta.divide(salidaInterbancario.VentasPuntas,MathContext.DECIMAL128);
		
		salidaInterbancario.MontoParaPromedioCompra = montoParaPromedioCompra;
		salidaInterbancario.MontoParaPromedioVenta = montoParaPromedioVenta;
		
		logger.debug("FIN::CalculosCalculadora.obtenerInterbancario");
		return salidaInterbancario;
		
	}
	
	private List<MovimientoSpot> obtenerMovimientosArbitrajeInterbancarios(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosArbitrajeInterbancarios");
		List<MovimientoSpot> movimientosArbitrajeInterbancarios = movimientosSpot.parallelStream().filter(movimiento->"ARBINT".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosArbitrajeInterbancarios");
		return movimientosArbitrajeInterbancarios;
	}
	

	
	private List<MovimientoSpot> obtenerMovimientosDistribucion(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosDistribucion");
		List<MovimientoSpot> movimientosDistribucion = movimientosSpot.parallelStream().filter(movimiento->"DISTRI".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		List<MovimientoSpot> movimientosDistribucionArbDis = movimientosSpot.parallelStream().filter(movimiento->"ARBDIS".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		movimientosDistribucion.addAll(movimientosDistribucionArbDis);
		
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosDistribucion");
		return movimientosDistribucion;
	}
	
	private SalidaDistribucion obtenerDistribucion(List<MovimientoSpot> movimientosDistribucion, Fecha fechas,EntradaCalculosCalculadoraSpot entrada, DistribucionTCCosto DistribucionCosto) {
		logger.debug("INICIO::CalculosCalculadora.obtenerDistribucion");
		SalidaDistribucion salidaDistribucion = new SalidaDistribucion();

		BigDecimal cantidadVentaT0 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT1 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT2 = BigDecimal.ZERO;
		BigDecimal cantidadVentaT3 = BigDecimal.ZERO;

		
		for(MovimientoSpot movimiento : movimientosDistribucion) {

			//no debe tomar los arbitrajes para estos calculos
			if((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT"))) continue;

			if(movimiento.FechaValutaRecibimos != null) {
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) salidaDistribucion.ResultadoDistribucionT0 = salidaDistribucion.ResultadoDistribucionT0.add(movimiento.Margen);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT1)) salidaDistribucion.ResultadoDistribucionT1 = salidaDistribucion.ResultadoDistribucionT1.add(movimiento.Margen);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT2)) salidaDistribucion.ResultadoDistribucionT2 = salidaDistribucion.ResultadoDistribucionT2.add(movimiento.Margen);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT3)) salidaDistribucion.ResultadoDistribucionT3 = salidaDistribucion.ResultadoDistribucionT3.add(movimiento.Margen);
			}

			if((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT"))  && movimiento.IdTipoOperacion.equals("COMSPT"))
			{	movimiento.IdTipoOperacion="VENSPT";
			  continue;
				
			}else if ((movimiento.Nemotecnico.equals("ARBDIS") || movimiento.Nemotecnico.equals("ARBINT")) && movimiento.IdTipoOperacion.equals("VENSPT")) {
				movimiento.IdTipoOperacion="COMSPT";
				  continue;
			}
			BigDecimal montoAdd = movimiento.MontoMonedaPrincipal;
			
			if(movimiento.IdTipoOperacion.equals("COMSPT")) {
				salidaDistribucion.ComprasDistribucion = salidaDistribucion.ComprasDistribucion.add(montoAdd,MathContext.DECIMAL128);
				salidaDistribucion.MontoParaPromedioCompra  = salidaDistribucion.MontoParaPromedioCompra.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto,MathContext.DECIMAL128),MathContext.DECIMAL128);
				salidaDistribucion.UtilidadCompra = salidaDistribucion.UtilidadCompra.add(montoAdd.multiply(movimiento.TipoCambioCosto.subtract(movimiento.TipoCambioCierre,MathContext.DECIMAL128),MathContext.DECIMAL128) ,MathContext.DECIMAL128);
				
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) {
					if (movimiento.IdMonedaRecibimos.equals("USD")) {
						salidaDistribucion.ResultadosComprasUSDT0 = salidaDistribucion.ResultadosComprasUSDT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
					}
					
				}
				if(movimiento.FechaValutaEntregamos.equals(fechas.FechaT0)) {
					if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("CLP") && movimiento.IdMonedaSecundaria.equalsIgnoreCase("CLP")) {
						salidaDistribucion.ResultadosComprasCLPT0 = salidaDistribucion.ResultadosComprasCLPT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					}
					if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("USD")&& movimiento.IdMonedaSecundaria.equalsIgnoreCase("USD")) {
						salidaDistribucion.ResultadosComprasUSDT0 = salidaDistribucion.ResultadosComprasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					}
					
				}
				
			}else if(movimiento.IdTipoOperacion.equals("VENSPT")) {
				salidaDistribucion.VentasDistribucion = salidaDistribucion.VentasDistribucion.add(montoAdd,MathContext.DECIMAL128);
				salidaDistribucion.MontoParaPromedioVenta = salidaDistribucion.MontoParaPromedioVenta.add(movimiento.MontoMonedaPrincipal.multiply(movimiento.TipoCambioCosto,MathContext.DECIMAL128),MathContext.DECIMAL128);
				salidaDistribucion.UtilidadVenta = salidaDistribucion.UtilidadVenta.add(montoAdd.multiply(movimiento.TipoCambioCierre.subtract(movimiento.TipoCambioCosto,MathContext.DECIMAL128),MathContext.DECIMAL128) ,MathContext.DECIMAL128);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) cantidadVentaT0 = cantidadVentaT0.add(BigDecimal.ONE);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT1)) cantidadVentaT1 = cantidadVentaT1.add(BigDecimal.ONE);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT2)) cantidadVentaT2 = cantidadVentaT2.add(BigDecimal.ONE);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT3)) cantidadVentaT3 = cantidadVentaT3.add(BigDecimal.ONE);
				if(movimiento.FechaValutaRecibimos.equals(fechas.FechaT0)) {
					if (movimiento.IdMonedaRecibimos.equals("CLP")&& movimiento.IdMonedaSecundaria.equalsIgnoreCase("CLP")) {
						salidaDistribucion.ResultadosVentasCLPT0 = salidaDistribucion.ResultadosVentasCLPT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					}
					if (movimiento.IdMonedaRecibimos.equals("USD") && movimiento.IdMonedaSecundaria.equalsIgnoreCase("USD")) {
						salidaDistribucion.ResultadosVentasUSDT0 = salidaDistribucion.ResultadosVentasUSDT0.add(movimiento.MontoMonedaSecundaria,MathContext.DECIMAL128);
					}	
					
				}
				if(movimiento.FechaValutaEntregamos.equals(fechas.FechaT0)) {
					if (movimiento.IdMonedaEntregamos.equalsIgnoreCase("USD")&& movimiento.IdMonedaPrincipal.equalsIgnoreCase("USD")) {
						salidaDistribucion.ResultadosVentasUSDT0 = salidaDistribucion.ResultadosVentasUSDT0.add(movimiento.MontoMonedaPrincipal,MathContext.DECIMAL128);
					}	
				}

			}
		}
		

		logger.debug("FIN::CalculosCalculadora.obtenerDistribucion");
		return salidaDistribucion;
	}
	
	private List<MovimientoSpot> obtenerMovimientosArbitrajeDistribucion(List<MovimientoSpot> movimientosSpot){
		logger.debug("INICIO::ConsultaPosicion.obtenerMovimientosArbitrajeDistribucion");
		List<MovimientoSpot> movimientosArbitrajeDistribucion = movimientosSpot.parallelStream().filter(movimiento->"ARBDIS".equals(movimiento.Nemotecnico)).collect(Collectors.toList());
		logger.debug("FIN::ConsultaPosicion.obtenerMovimientosArbitrajeDistribucion");
		return movimientosArbitrajeDistribucion;
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
	
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
																				//4	
		String jsonRF = "{\"p_IdMoneda\":\"EUR\",\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_Resultado\":1,\"p_FechaOperacion\":\"2025-07-29\"}";
		
		EntradaCalculosCalculadoraSpot Objin = gson.fromJson(jsonRF, EntradaCalculosCalculadoraSpot.class);																
		CalculosCalculadoraSpot  demo=new CalculosCalculadoraSpot();				
		try {
			SalidaCalculosCalculadoraSpot ObjOut=demo.logica(con, Objin);
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

