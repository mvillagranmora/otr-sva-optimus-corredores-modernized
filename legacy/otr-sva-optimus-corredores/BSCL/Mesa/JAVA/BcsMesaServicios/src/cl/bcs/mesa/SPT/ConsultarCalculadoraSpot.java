package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.crystaldecisions.reports.formulas.FieldExpression.Value;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CargarArchivoCircular1518.Posicion;
import cl.bcs.mesa.SPT.ConsultarPosicion.MovimientoSpot;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.FechaDB;

import cl.bcs.mesa.SPT.CalculosCalculadoraSpot;

public class ConsultarCalculadoraSpot implements JavaCall {

	static final Logger logger = LogManager.getLogger(ConsultarCalculadoraSpot.class);
	private final String SQL_SELECT_MESAS = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarCalculadoraSpot_SelectMesas.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarCalculadoraSpot_SelectTipoInstrumento.sql");
	private final String SQL_SELECT_TOTALES_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarCalculadoraSpot_ResumenSpot.sql");
	
	public static class EntradaConsultarCalculadoraSpot {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Pre_Mesas p_Mesas;
		public Pre_Tipoinstrumento p_Tipoinstrumento;
		public Pre_valorFecha p_valorFecha;
		public List<PreSalida> p_grillaAll;
		public EntradaConsultarCalculadoraSpot() {
			this.p_grillaAll = new ArrayList<PreSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Pre_Mesas {
		public Long p_IdMesa;
		public String p_DocumentoIdentidad;
		public String p_Nombre;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Pre_Tipoinstrumento {
		public Long p_IdInstrumento;
		public String p_Instrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Pre_valorFecha {
		public BigDecimal p_valorUSD;
		public Date p_Fecha;
	}

	public static class SalidaConsultarCalculadoraSpot {
		public List<PreSalida> P_OUT_RESULTADO;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PreSalida {
		public BigDecimal Precio;
		public Date Fecha;
		public String Mesa;
		public String TipoInstrumento;
		public BigDecimal PrecioCierre;
		public BigDecimal PrecioPromedioCompra;
		public BigDecimal PrecioPromedioVenta;
		public BigDecimal Posicion;
		public BigDecimal Breakeven;
		public BigDecimal UtilidadPerdida;
		public BigDecimal ComprasCLP;
		public BigDecimal VentasCLP;
		public BigDecimal CajaCLP;
		public BigDecimal ComprasUSD;
		public BigDecimal VentasUSD;
		public BigDecimal CajaUSD;
		
		public BigDecimal PromPrecioCierre;
		public BigDecimal PromPrecioCompra;
		public BigDecimal PromPrecioVenta;
		public BigDecimal TotalPosicion;
		public BigDecimal TotalUtilidadPerdida;
		public BigDecimal TotalCajaCLP;
		public BigDecimal TotalCajaUSD;
	}

	public static class EntidadMesas {
		public Long IdMesa;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String Codigo;
		public String Nombre;
		public String IdTipoProducto;
		public String IdEstadoMesa;
		public String IdEstadoRegistro;
		public String ValorRazonable;
		public String CodigoPortafolio;
	}

	public static class EntidadTipoInstrumento {
		public Long IdInstrumento;
		public Long IdEmpresa;
		public String IdTipoProducto;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String IdEstado;
		public String IdTipoInstrumento;
		public String Instrumento;
		public String Nemotecnico;
	}

	public class VariablesMontoTotal {
		public BigDecimal MontoTotalConsolidado;
		public BigDecimal MontoTotalT0;
		public BigDecimal MontoTotalT1;
		public BigDecimal MontoTotalT2;
		public BigDecimal MontoTotalT3;

		public VariablesMontoTotal() {
			this.MontoTotalConsolidado = BigDecimal.ZERO;
			this.MontoTotalT0 = BigDecimal.ZERO;
			this.MontoTotalT1 = BigDecimal.ZERO;
			this.MontoTotalT2 = BigDecimal.ZERO;
			this.MontoTotalT3 = BigDecimal.ZERO;
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
		public String valorIndicador;

		public VariablesBreakEven() {
			this.posicionInicial = BigDecimal.ZERO;
			this.precioInicial = BigDecimal.ZERO;
			this.BreakEvenCons = BigDecimal.ZERO;
			this.BreakEvenT0 = BigDecimal.ZERO;
			this.BreakEvenT1 = BigDecimal.ZERO;
			this.BreakEvenT2 = BigDecimal.ZERO;
			this.BreakEvenT3 = BigDecimal.ZERO;
			this.valorIndicador = null;
		}
	}
	
	public static class TotalesSpot{
		
		public String IdTipoOperacion;
		public String Nemotecnico;
		public BigDecimal TotalUSD;
		public BigDecimal TotalClp;
		public BigDecimal PromedioUtilizado;
		public BigDecimal PromedioCosto;
		public BigDecimal Diferencia;
		public BigDecimal ValorTotal;
		public BigDecimal MargenFinal;
		
		public TotalesSpot() {
			this.IdTipoOperacion= null;
			this.Nemotecnico= null;
			this.TotalUSD= BigDecimal.ZERO;
			this.TotalClp= BigDecimal.ZERO;
			this.PromedioUtilizado= BigDecimal.ZERO;
			this.PromedioCosto= BigDecimal.ZERO;
			this.Diferencia= BigDecimal.ZERO;
			this.ValorTotal= BigDecimal.ZERO;
			this.MargenFinal= BigDecimal.ZERO;			
		}
		
	}
	
	public static class ValoresTotalizadosSpot{
		public BigDecimal PromPrecioCierre;
		public BigDecimal PromPrecioCompra;
		public BigDecimal PromPrecioVenta;
		public BigDecimal TotalPosicion;
		public BigDecimal TotalUtilidadPerdida;
		public BigDecimal TotalCajaCLP;
		public BigDecimal TotalCajaUSD; 
		
		public ValoresTotalizadosSpot() {
			this.PromPrecioCierre = BigDecimal.ZERO;
			this.PromPrecioCompra = BigDecimal.ZERO;
			this.PromPrecioVenta = BigDecimal.ZERO;
			this.TotalPosicion = BigDecimal.ZERO;
			this.TotalUtilidadPerdida = BigDecimal.ZERO;
			this.TotalCajaCLP= BigDecimal.ZERO;
			this.TotalCajaUSD= BigDecimal.ZERO;		
		}
	}
	
	private List<TotalesSpot> ObtenerTotalesSpot(Connection con,String p_FechaOperacion,String p_Nemotecnico) throws SQLException {

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("FechaOperacion", p_FechaOperacion);
		params.put("Nemotecnico", p_Nemotecnico);		 
		List<TotalesSpot> totalesSpot = sql.query(con, SQL_SELECT_TOTALES_SPOT, params, TotalesSpot.class);
		return totalesSpot;
	}

	private List<EntidadMesas> obtenerMesas(Connection con, Long ValorM) throws Exception {
		Long Valor = ValorM;
		List<EntidadMesas> datosMesas;
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdTipoProducto", "MEME");
		if (Valor != null) {
			if ((Valor.longValue() > 0)) {
				datosMesas = sql.query(con, SQL_SELECT_MESAS, params, EntidadMesas.class).stream()
						.filter(entidadMesa -> entidadMesa.IdMesa == Valor.longValue()).collect(Collectors.toList());
			} else {
				datosMesas = sql.query(con, SQL_SELECT_MESAS, params, EntidadMesas.class);
			}
		} else {
			datosMesas = sql.query(con, SQL_SELECT_MESAS, params, EntidadMesas.class);
		}
		return datosMesas;
	}

	private List<EntidadTipoInstrumento> obtenerTipoInstrumento(Connection con, Long ValorE) throws Exception {
		Long Valor = ValorE;
		List<EntidadTipoInstrumento> datosTipoInstrumento;
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdMonedaSecundaria", "CLP");
		if (Valor != null) {
			if (Valor.longValue() > 0) {
				datosTipoInstrumento = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, params, EntidadTipoInstrumento.class)
						.stream()
						.filter(entidadTipoInstrumento -> entidadTipoInstrumento.IdInstrumento == Valor.longValue())
						.collect(Collectors.toList());
			} else {

				datosTipoInstrumento = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, params,
						EntidadTipoInstrumento.class);
			}
		} else {

			datosTipoInstrumento = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, params, EntidadTipoInstrumento.class);
		}
		return datosTipoInstrumento;
	}
	
	private BigDecimal  obtenerUtulidadPerdidaInterBancario(Connection con,Date p_Fecha,PreSalida P_result, ConsultarPosicion.SalidaConsultaPosicion p_Posicion) {
			BigDecimal Resultado = BigDecimal.ZERO;
	    try { 
			 DateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
             List<TotalesSpot> ObtenerTotalesSpot = ObtenerTotalesSpot(con , dt.format(p_Fecha)  ,"INTERB");  
             if (ObtenerTotalesSpot.size()>0) {
                
                 if(P_result.Precio.compareTo(BigDecimal.ZERO) == 0) {
                	Resultado = ((p_Posicion.P_OUT_INTERBANCARIO.MontoParaPromedioVenta.add(p_Posicion.P_OUT_DISTRIBUCION.MontoParaPromedioVenta, MathContext.DECIMAL128))
	 							.subtract((p_Posicion.P_OUT_INTERBANCARIO.MontoParaPromedioCompra.add(p_Posicion.P_OUT_DISTRIBUCION.MontoParaPromedioCompra, MathContext.DECIMAL128)), MathContext.DECIMAL128));
	 				Resultado = Resultado.subtract(p_Posicion.P_OUT_TIPODECAMBIO.PosicionInicial.multiply(p_Posicion.P_OUT_TIPODECAMBIO.TipoCambioInicialT0, MathContext.DECIMAL128), MathContext.DECIMAL128);
	 				Resultado = Resultado.subtract(p_Posicion.P_OUT_DISTRIBUCION.ResultadoDistribucion, MathContext.DECIMAL128);
                 }else {
                	 Resultado = ((p_Posicion.P_OUT_INTERBANCARIO.MontoParaPromedioVenta.add(p_Posicion.P_OUT_DISTRIBUCION.MontoParaPromedioVenta, MathContext.DECIMAL128))
	 							.subtract((p_Posicion.P_OUT_INTERBANCARIO.MontoParaPromedioCompra.add(p_Posicion.P_OUT_DISTRIBUCION.MontoParaPromedioCompra, MathContext.DECIMAL128)), MathContext.DECIMAL128));
	 				Resultado = Resultado.subtract(p_Posicion.P_OUT_TIPODECAMBIO.PosicionInicial.multiply(p_Posicion.P_OUT_TIPODECAMBIO.TipoCambioInicialT0, MathContext.DECIMAL128), MathContext.DECIMAL128);
	 				Resultado = Resultado.subtract(p_Posicion.P_OUT_DISTRIBUCION.ResultadoDistribucion, MathContext.DECIMAL128);
	 				Resultado = Resultado.add(P_result.Posicion.multiply(P_result.Precio, MathContext.DECIMAL128), MathContext.DECIMAL128);
                 }

             }else {
            	 Resultado= BigDecimal.ZERO;	
             }    
        } catch (SQLException e) {
			e.printStackTrace();
		} 	
		return 	Resultado;	
	}

	private BigDecimal  obtenerUtulidadPerdidaDistribucion( Connection con,Date p_Fecha) {
		BigDecimal Resultado = BigDecimal.ZERO;
		try { 
			DateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
            List<TotalesSpot> ObtenerTotalesSpot = ObtenerTotalesSpot(con , dt.format(p_Fecha)  ,"DISTRI");
            if (ObtenerTotalesSpot.size()>0) {
         	   Resultado=  ObtenerTotalesSpot.get(0).MargenFinal;
               }else {
            	Resultado= BigDecimal.ZERO;	
              }
			} catch (SQLException e) {
					e.printStackTrace();
		   } 
		return 	Resultado;	
	}
	
	private BigDecimal  obtenerPrecioCierreConsolidado(ConsultarPosicion.SalidaConsultaPosicion PosicionX) {
		BigDecimal Resultado = BigDecimal.ZERO;
		BigDecimal PrecioCierre = PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT0
	             .add(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT1
	             .add(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT2
	             .add(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT3, MathContext.DECIMAL128  )));
        Integer contadorPrecioCierre=0;
        contadorPrecioCierre+=(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT0.compareTo(BigDecimal.ZERO)>0 )? 1:0;
        contadorPrecioCierre+=(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT1.compareTo(BigDecimal.ZERO)>0 )? 1:0;
        contadorPrecioCierre+=(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT2.compareTo(BigDecimal.ZERO)>0 )? 1:0;
        contadorPrecioCierre+=(PosicionX.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT3.compareTo(BigDecimal.ZERO)>0 )? 1:0;
		
        Resultado = PrecioCierre.divide( BigDecimal.valueOf((contadorPrecioCierre> 0)?contadorPrecioCierre:1 ),  MathContext.DECIMAL128);
		
		return 	Resultado;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// Gson gson = new Gson();
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarCalculadoraSpot jin = gson.fromJson(in.toString(), EntradaConsultarCalculadoraSpot.class);
		SalidaConsultarCalculadoraSpot out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		System.out.println("json" + dataout);
		return dataout;

	}

	VariablesBreakEven VariablesBreak = new VariablesBreakEven();
	VariablesMontoTotal VariablesMontoTotal = new VariablesMontoTotal();

	public SalidaConsultarCalculadoraSpot logica(Connection conn, EntradaConsultarCalculadoraSpot in) throws Exception {

		ConsultarPosicion.EntradaConsultarPosicion entradaP = new ConsultarPosicion.EntradaConsultarPosicion();
		ConsultarPosicion.SalidaConsultaPosicion Posicion = new ConsultarPosicion.SalidaConsultaPosicion();
		CalculosCalculadoraSpot.EntradaCalculosCalculadoraSpot EntradaCalculosSpot = new CalculosCalculadoraSpot.EntradaCalculosCalculadoraSpot();
		CalculosCalculadoraSpot.SalidaCalculosCalculadoraSpot calculosCalculadora = new CalculosCalculadoraSpot.SalidaCalculosCalculadoraSpot();
		entradaP.p_IdEmpresa = in.p_IdEmpresa;
		entradaP.p_IdUsuario = in.p_IdUsuario;
		entradaP.p_IdMoneda = "USD";
		entradaP.p_FechaOperacion = new SimpleDateFormat("yyyy-MM-dd").format(in.p_valorFecha.p_Fecha);
		ConsultarPosicion consultarPosicion = new ConsultarPosicion();
		Posicion = consultarPosicion.logica(conn, entradaP);
		
		
		EntradaCalculosSpot.p_IdEmpresa = in.p_IdEmpresa;
		EntradaCalculosSpot.p_IdUsuario = in.p_IdUsuario;
		EntradaCalculosSpot.p_IdMoneda = "USD";
		EntradaCalculosSpot.p_FechaOperacion = new SimpleDateFormat("yyyy-MM-dd").format(in.p_valorFecha.p_Fecha);
		EntradaCalculosSpot.p_precio = (in.p_valorFecha.p_valorUSD != null) ? in.p_valorFecha.p_valorUSD: BigDecimal.ONE;
		
		CalculosCalculadoraSpot CalculosCalculadoraSpot= new CalculosCalculadoraSpot();
		calculosCalculadora = CalculosCalculadoraSpot.logica(conn,EntradaCalculosSpot);
		BigDecimal sumaCompraCLP= calculosCalculadora.P_OUT_INTERBANCARIO.MontoParaPromedioCompra.add(calculosCalculadora.P_OUT_DISTRIBUCION.MontoParaPromedioCompra, MathContext.DECIMAL128);
		BigDecimal sumaVentaCLP= calculosCalculadora.P_OUT_INTERBANCARIO.MontoParaPromedioVenta.add(calculosCalculadora.P_OUT_DISTRIBUCION.MontoParaPromedioVenta, MathContext.DECIMAL128);
		BigDecimal sumaCompraUSD= calculosCalculadora.P_OUT_INTERBANCARIO.ComprasPuntas.add(calculosCalculadora.P_OUT_DISTRIBUCION.ComprasDistribucion, MathContext.DECIMAL128);
		BigDecimal sumaVentaUSD= calculosCalculadora.P_OUT_INTERBANCARIO.VentasPuntas.add(calculosCalculadora.P_OUT_DISTRIBUCION.VentasDistribucion, MathContext.DECIMAL128);
		BigDecimal resultadoCLP=sumaCompraCLP.subtract(sumaVentaCLP, MathContext.DECIMAL128);
		BigDecimal resultadoUSD=sumaCompraUSD.subtract(sumaVentaUSD, MathContext.DECIMAL128);

		HashMap<String, HashMap<String, String>> ValoresIndicadores = Utils.getConfiguraciones(conn, in.p_IdEmpresa);
		if (ValoresIndicadores.get("MESA").get("VER_BREAKEVEN") != null) {
			VariablesBreak.valorIndicador = ValoresIndicadores.get("MESA").get("VER_BREAKEVEN");
		} else {
			VariablesBreak.valorIndicador = "N";
		}

		SalidaConsultarCalculadoraSpot Salida = new SalidaConsultarCalculadoraSpot();
		List<PreSalida> PreResultado = new ArrayList<PreSalida>();
		List<EntidadMesas> MesasArry = new ArrayList<EntidadMesas>();
		List<EntidadTipoInstrumento> TipoInstrumentoArry = new ArrayList<EntidadTipoInstrumento>();
		ValoresTotalizadosSpot TotalizadosSpot = new ValoresTotalizadosSpot();

		try {

			if (in.p_grillaAll != null) {
				PreResultado = in.p_grillaAll;
			}

			MesasArry = obtenerMesas(conn, in.p_Mesas.p_IdMesa);
			TipoInstrumentoArry = obtenerTipoInstrumento(conn, in.p_Tipoinstrumento.p_IdInstrumento);
			BigDecimal DivisorBreakeven;

			for (int i = 0; i < MesasArry.size(); i++) {
				for (int j = 0; j < TipoInstrumentoArry.size(); j++) {

					DivisorBreakeven = BigDecimal.ZERO;
					PreSalida resultado = new PreSalida();
					resultado.Precio = (in.p_valorFecha.p_valorUSD != null) ? in.p_valorFecha.p_valorUSD: BigDecimal.ONE;
					resultado.Fecha = (in.p_valorFecha.p_Fecha != null) ? in.p_valorFecha.p_Fecha : new Date();
					resultado.Mesa = MesasArry.get(i).Nombre;
					resultado.TipoInstrumento = TipoInstrumentoArry.get(j).Instrumento;
					resultado.PrecioCierre = (in.p_valorFecha.p_valorUSD != null) ? in.p_valorFecha.p_valorUSD: BigDecimal.ONE;

					if (TipoInstrumentoArry.get(j).Instrumento.equalsIgnoreCase("INTERBANCARIO") || TipoInstrumentoArry.get(j).Instrumento.equalsIgnoreCase("ARBITRAJE INTERBANCARIO")) {						
						resultado.PrecioPromedioCompra =calculosCalculadora.P_OUT_INTERBANCARIO.PromedioDeCompra;
						resultado.PrecioPromedioVenta =  calculosCalculadora.P_OUT_INTERBANCARIO.PromedioDeVenta;
						resultado.Posicion = Posicion.P_OUT_CONSOLIDADOSPOT.Posicion;
						resultado.ComprasUSD = calculosCalculadora.P_OUT_INTERBANCARIO.ResultadosComprasUSDT0;
						resultado.VentasUSD = calculosCalculadora.P_OUT_INTERBANCARIO.ResultadosVentasUSDT0;
						resultado.CajaUSD = resultado.ComprasUSD.subtract(resultado.VentasUSD, MathContext.DECIMAL128);				
						resultado.VentasCLP = calculosCalculadora.P_OUT_INTERBANCARIO.ResultadosComprasCLPT0;
						resultado.ComprasCLP = calculosCalculadora.P_OUT_INTERBANCARIO.ResultadosVentasCLPT0;
						resultado.CajaCLP = resultado.ComprasCLP.subtract(resultado.VentasCLP, MathContext.DECIMAL128);						
						DivisorBreakeven = resultado.CajaUSD;
						BigDecimal posicionNetaBreakeven = resultadoUSD.add(Posicion.P_OUT_TIPODECAMBIO.PosicionInicial, MathContext.DECIMAL128);
						if(posicionNetaBreakeven.compareTo(BigDecimal.ZERO) != 0L) {
							resultado.Breakeven= (resultadoCLP.add(Posicion.P_OUT_TIPODECAMBIO.PosicionInicial.multiply(Posicion.P_OUT_TIPODECAMBIO.TipoCambioInicial, MathContext.DECIMAL128), MathContext.DECIMAL128)).divide(posicionNetaBreakeven, MathContext.DECIMAL128);
						} else resultado.Breakeven = BigDecimal.ZERO;
						//UtilidadPerdida derivada del Breakeven: posicionNetaBreakeven * (precioCalculadora - Breakeven)
						resultado.UtilidadPerdida = posicionNetaBreakeven.multiply(resultado.Precio.subtract(resultado.Breakeven, MathContext.DECIMAL128), MathContext.DECIMAL128);
						
					} else {
						resultado.PrecioPromedioCompra =Posicion.P_OUT_DISTRIBUCION.PromedioDeCompra; 
						resultado.PrecioPromedioVenta = Posicion.P_OUT_DISTRIBUCION.PromedioDeVenta;
						resultado.Posicion = Posicion.P_OUT_DISTRIBUCION.Posicion;
						resultado.ComprasUSD = calculosCalculadora.P_OUT_DISTRIBUCION.ResultadosComprasUSDT0;
						resultado.VentasUSD = calculosCalculadora.P_OUT_DISTRIBUCION.ResultadosVentasUSDT0;
						resultado.CajaUSD = resultado.ComprasUSD.subtract(resultado.VentasUSD, MathContext.DECIMAL128);
						resultado.VentasCLP = calculosCalculadora.P_OUT_DISTRIBUCION.ResultadosComprasCLPT0;
						resultado.ComprasCLP = calculosCalculadora.P_OUT_DISTRIBUCION.ResultadosVentasCLPT0;
						resultado.CajaCLP = resultado.ComprasCLP.subtract(resultado.VentasCLP, MathContext.DECIMAL128);
						//DivisorBreakeven = (resultado.Posicion.compareTo(BigDecimal.ZERO) != 0) ? resultado.Posicion : BigDecimal.ONE;
						resultado.Breakeven = BigDecimal.ZERO;
						resultado.UtilidadPerdida =calculosCalculadora.P_OUT_DISTRIBUCION.UtilidadCompra.add(calculosCalculadora.P_OUT_DISTRIBUCION.UtilidadVenta, MathContext.DECIMAL128);
						//resultado.UtilidadPerdida =obtenerUtulidadPerdidaDistribucion(conn,resultado.Fecha,Posicion.P_OUT_CONSOLIDADOSPOT, Posicion. );
					}
					
					BigDecimal divisorPromCierre = BigDecimal.ZERO;
					if(resultado.PrecioCierre.compareTo(BigDecimal.ZERO) > 0) {
						divisorPromCierre = divisorPromCierre.add(BigDecimal.ONE);
					}					
					if(divisorPromCierre.compareTo(BigDecimal.ZERO) == 0) resultado.PromPrecioCierre = BigDecimal.ZERO;
					else resultado.PromPrecioCierre = TotalizadosSpot.PromPrecioCierre.divide(divisorPromCierre, MathContext.DECIMAL128);
					TotalizadosSpot.TotalCajaCLP = TotalizadosSpot.TotalCajaCLP.add(resultado.CajaCLP, MathContext.DECIMAL128);;
					TotalizadosSpot.TotalCajaUSD = TotalizadosSpot.TotalCajaUSD.add(resultado.CajaUSD, MathContext.DECIMAL128);
					resultado.PromPrecioCierre = TotalizadosSpot.PromPrecioCierre.add(resultado.PrecioCierre, MathContext.DECIMAL128);	
					resultado.PromPrecioCompra = Posicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioCompra;				
					resultado.PromPrecioVenta = Posicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioVenta;
					resultado.TotalPosicion = Posicion.P_OUT_CONSOLIDADOSPOT.Posicion;					
					if(Posicion.P_OUT_CONSOLIDADOSPOT.MontoTotalConsolidado != null) resultado.TotalUtilidadPerdida = Posicion.P_OUT_CONSOLIDADOSPOT.MontoTotalConsolidado;
					else resultado.TotalUtilidadPerdida = BigDecimal.ZERO;
					resultado.TotalCajaCLP = TotalizadosSpot.TotalCajaCLP;
					resultado.TotalCajaUSD = TotalizadosSpot.TotalCajaUSD;
					resultado.TotalPosicion = Posicion.P_OUT_CONSOLIDADOSPOT.Posicion;

					
					PreResultado.add(resultado);
				}
		
			}
			
			Salida.P_OUT_RESULTADO = PreResultado;			
			
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
		return Salida;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPREBCOESTADOConnection()) { // -->FIJARSE EN QUE ENTORNO SE ESTA
																		// PROBANDO<--
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_Mesas\":{\"p_IdMesa\":0,\"p_Nombre\":\" TODOS\"},\"p_Tipoinstrumento\":{\"p_IdInstrumento\":0,\"p_Instrumento\":\" TODOS\"},\"p_valorFecha\":{\"p_valorUSD\":1200,\"p_Fecha\":\"2026-09-02T04:00:00.000Z\"}}";
			

			EntradaConsultarCalculadoraSpot Objin = gson.fromJson(jsonRF, EntradaConsultarCalculadoraSpot.class);
			ConsultarCalculadoraSpot demo = new ConsultarCalculadoraSpot();

			try {

				System.out.println("try");
				SalidaConsultarCalculadoraSpot ObjOut = demo.logica(con, Objin);

				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();

			}

		}
		System.out.println("== END ==");
	}

}