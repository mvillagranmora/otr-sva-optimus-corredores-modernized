package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.CalculoInteres;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarResumenPrestamoVigente  implements JavaCall {

	private final String SQL_SELECT_PRESTAMOS_VIGENTES = loadResourceToString("cl/bcs/custodia/sql/prt/ConsultarResumenPrestamoVigente_SelectPrestamosVigentes.sql");

	public static class ConsultarResumenPrestamoVigente_Entrada extends BcsPojo {
		public Long 			p_IdEmpresa;
		public Long				p_IdUsuario;
		public Long 			p_FolioCartera;
		public String			p_FechaConsulta;
		public String			p_ConsultaPor = "OPERACION";
		public String 			p_IdTipoPrestamo;
		public Long 			p_IdCliente;
		public Long 			p_IdPortafolio;
		public String 			p_IdClasificacionTipoInstrumento;
		
		public ConsultarResumenPrestamoVigente_Entrada() {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
		}
	}
	
	public static class ConsultarResumenPrestamoVigente_Salida extends BcsPojo {
		public List<PrestamoVigente>	P_OUT_PRESTAMOS_VIGENTES;
		
		public ConsultarResumenPrestamoVigente_Salida() {
			this.P_OUT_PRESTAMOS_VIGENTES	= new ArrayList<>();
		}
	}
	
	public static class PrestamoVigente extends BcsPojo {
		public Long IdInstrumentoSubyacente;
		public Long FolioSubyacente;
		public String IdEmisorSubyacente;
		public String NemoSubyacente;
		public String NemoSVSSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public String NombreTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public BigDecimal CantidadAsignada;
		public BigDecimal CantidadPorAsignar;
		public String IdMonedaSubyacente;
		public String IdTipoReajusteSubyacente; 
		public Long IdSubyacente;
		public String IdMonedaEmisionSubyacente;
		public String NombreMercado;
		public String NombreEmisorSubyacente;
		public String IdBovedaSubyacente;
		public Long IdCartera;
		public Long IdEmpresa;
		public Long FolioPrestamo;
		public Long FolioCarteraRelacionada;
		public String IdEstado;
		public String NombreEstado;
		public String IdMonedaSistema;
		public String IdMonedaInstrumento;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdBoveda;
		public String IdTipoReajuste;
		public String IdTipoProducto;
		public Long IdSucursal;
		public Long IdAgente;
		public String IdBolsa;
		public String NombreBolsa;
		public String IdConcepto;
		public String NombreConcepto;
		public String IdCondicionLiquidacion;
		public String NombreCondicionLiquidacion;
		public String IdModulo;
		public Long IdCliente;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String DescripcionPortafolio;
		public String RazonSocial;
		public String NemotecnicoPrestamo;
		public String NemoSVS;
		public Long IdInstrumentoPrestamo; 
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String TipoInstrumento;
		public String IdEmisor;
		public java.sql.Date FechaPrestamo;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public java.sql.Date FechaComprobante;
		public BigDecimal Cantidad;
		public BigDecimal CorteMinimo;
		public BigDecimal PrecioInstrumento;
		public BigDecimal MontoInstrumento;
		public BigDecimal TasaPrestamo;
		public BigDecimal ValorRescate;
		public java.sql.Date FechaVencimientoPrestamo;
		public java.sql.Date FechaEmisionInstrumento;
		public java.sql.Date FechaVencimientoInstrumento;
		public Long PlazoEnDiasOperacion;
		public String EsFungible;
		public String EsFungibleSubyacente;
		public String CodigoIFRS;
		public java.sql.Timestamp FechaIngresoRegistro;
		public java.sql.Timestamp FechaModificacionRegistro;
		public BigDecimal TasaAgente;
		public java.sql.Date FechaLiquidacion;
		public String IdTipoPrestamo;
		public String NombreTipoPrestamo;
		public Long MinimoDiasAnticipo;
		public Long MaximoDiasPrestamo;
		public String EsDiaHabilPlazoPrestamo;
		public String EsDiaHabilParaCalculoInteres;
		public String UsaGarantiaEspecifica;
		public String FolioEntidad;
		public String CobraPrimaAlInicio;
		public String IdTipoCanalIngreso;
		public String INGGlosa;
		public java.sql.Date INGFechaOperacion;
		public BigDecimal INGValorRescate;
		public BigDecimal INGTasaPrestamo;
		public BigDecimal MontoPrimaOriginal;
		public BigDecimal INGMontoTotalPrestamo;
		public BigDecimal INGPrecioInstrumento;
		public BigDecimal INGTasaInstrumento;
		public java.sql.Date INGFechaLiquidacionPrestamo;
		public BigDecimal INGMontoInstrumento;
		public java.sql.Date INGFechaPrecioTasaMercadoActual;
		public BigDecimal INGPrecioMercadoInstrumento;
		public BigDecimal INGTasaMercadoActual;
		public BigDecimal INGMontoMercadoInstrumento;
		public BigDecimal EGRCantidadPrestamo;
		public java.sql.Date EGRFechaLiquidacionPrestamo;
		public java.sql.Date EGRFechaOperacion;
		public java.sql.Date INGFechaValorizacionAnterior;
		public java.sql.Date INGFechaValorizacionActual;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal INGPrecioMercadoAnterior;
		public BigDecimal INGMontoPrecioPromedioCompra;
		public BigDecimal INGMontoATasaMercadoAnterior;
		public BigDecimal INGMontoATasaMercadoProximo;
		public BigDecimal INGMontoATasaCompraAnterior;
		public BigDecimal INGMontoInteresCompraProximo;
		public BigDecimal INGMontoInteresCorridoCompraActual;
		public String TipoValorizacion;
		public Long IdInstrumento;
		public String FolioSistemaExterno;
		public String EstadoAnticipo;
		public String ExigirGarantia;
		public String EstadoLiquidacion;
		public String TipoCustodia;
		public String CodigoCondicionLiquidacion;
		public Long MinimoDiasPrestamo;
		
		public BigDecimal INGCantidad;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGMontoAPlazo;
		public BigDecimal INGMontoPrima;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoAPlazoMonedaInstrumento;
		public BigDecimal INGMontoPremioMonedaInstrumento;
		
		public BigDecimal PrecioCompraInicial;
		public BigDecimal MontoCompraInicial;
		
		//Campos Calculados
		public BigDecimal InteresAcumulado;
		public BigDecimal InteresDiario;

		public BigDecimal MontoPrecioMedio;
		public BigDecimal PrecioMedioInicial;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		ConsultarResumenPrestamoVigente_Entrada in = gson.fromJson(jin.toString(), ConsultarResumenPrestamoVigente_Entrada.class);
		ConsultarResumenPrestamoVigente_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarResumenPrestamoVigente_Salida logica(Connection con, ConsultarResumenPrestamoVigente_Entrada entrada) throws Exception {
		ConsultarResumenPrestamoVigente_Salida salida = new ConsultarResumenPrestamoVigente_Salida();
		SQL sql = new SQL();
		
		String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		String PaisSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		String UsaFechaLiquidacion	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		
		//Obtener dia habil anterior
		ObtenerDiasHabilesAnteriores ObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores();
		InObtenerDiasHabilesAnteriores inObtenerDiaAnterior = new InObtenerDiasHabilesAnteriores();
		inObtenerDiaAnterior.idPais 			= Long.parseLong(PaisSistema);
		inObtenerDiaAnterior.diaHabil			= "S";
		inObtenerDiaAnterior.soloCaeDiaHabil	= "S";
		InObtenerDiasHabilesAnteriores.FechasIn fechasIn = new InObtenerDiasHabilesAnteriores.FechasIn();
		fechasIn.fechaInicial 					= fechaSistema;
		fechasIn.numeroDias 					= 1L;
		inObtenerDiaAnterior.fechas.add(fechasIn);
		OutObtenerDiasHabilesAnteriores outObtenerDiasHabilesAnteriores = new OutObtenerDiasHabilesAnteriores();
		outObtenerDiasHabilesAnteriores 		= ObtenerDiasHabilesAnteriores.logica(con,inObtenerDiaAnterior);
		String fechaHabilAnterior 				= outObtenerDiasHabilesAnteriores.fechas.get(0).fechaCalculada; 
		
		
		try {
			HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa"							, entrada.p_IdEmpresa);
			parametroEntrada.put("p_FechaConsulta"						, entrada.p_FechaConsulta);
			parametroEntrada.put("p_ConsultaPor"						, entrada.p_ConsultaPor);
			parametroEntrada.put("p_FolioCartera"						, entrada.p_FolioCartera);
			parametroEntrada.put("p_IdClasificacionTipoInstrumento"		, entrada.p_IdClasificacionTipoInstrumento);
			parametroEntrada.put("p_IdTipoPrestamo"						, entrada.p_IdTipoPrestamo);
			parametroEntrada.put("p_IdCliente"							, entrada.p_IdCliente);
			parametroEntrada.put("p_IdPortafolio"						, entrada.p_IdPortafolio);
			
			
			List<PrestamoVigente> prestamos = sql.query(con, SQL_SELECT_PRESTAMOS_VIGENTES, parametroEntrada, PrestamoVigente.class);
			
			prestamos.forEach( prestamo ->{
				String fechaOperacion = ("S".equalsIgnoreCase(UsaFechaLiquidacion) ? prestamo.FechaLiquidacion.toString() : prestamo.FechaPrestamo.toString());
				
				prestamo.InteresDiario = 
						InteresCalculado (con,
								(Fecha.toDate(fechaSistema).compareTo(Fecha.toDate(fechaOperacion)) <= 0 ? fechaSistema : fechaHabilAnterior), 
								fechaSistema, 
								prestamo.INGMontoATasaCompraActual != null ? prestamo.INGMontoATasaCompraActual : new BigDecimal(0) ,
								prestamo.IdMonedaSistema,
								prestamo.INGTasaPrestamo != null ? prestamo.INGTasaPrestamo : new BigDecimal(0)) ;
				
				prestamo.InteresAcumulado =
						InteresCalculado(con,
								fechaOperacion, 
								fechaSistema, 
								prestamo.INGMontoATasaCompraActual != null ? prestamo.INGMontoATasaCompraActual : new BigDecimal(0) ,
								prestamo.IdMonedaSistema,
								prestamo.INGTasaPrestamo != null ? prestamo.INGTasaPrestamo : new BigDecimal(0)) ;
				
			});
			salida.P_OUT_PRESTAMOS_VIGENTES = prestamos;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return salida;
	}
	
	private BigDecimal InteresCalculado (Connection con, String FechaDesde, String FechaHasta, BigDecimal Monto, String IdMoneda, BigDecimal Prima) {
		BigDecimal Interes = new BigDecimal(0);
		
		CalculoInteres calculoInteres = new CalculoInteres();
		CalculoInteres.CalculoInteresEntrada in = new CalculoInteres.CalculoInteresEntrada();
		in.p_SoloDiasHabiles 	= "N";
		in.p_FechaDesde			= FechaDesde;
		in.p_FechaHasta			= FechaHasta;
		in.p_Monto				= Monto;
		in.p_IdMoneda			= IdMoneda;
		in.p_Tasa				= Prima;
		
		try {
			Interes = calculoInteres.CalculoInteres(con, in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Interes;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con =  PlataformaUtils.getMASTER01Connection()) {
			Gson gson = Utils.getGsonBuilder();					
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ConsultaPor\":\"OPERACION\",\"p_FechaConsulta\":\"2024-04-12\"}";
			
			ConsultarResumenPrestamoVigente_Entrada Objin = gson.fromJson(json, ConsultarResumenPrestamoVigente_Entrada.class);
			ConsultarResumenPrestamoVigente demo = new ConsultarResumenPrestamoVigente();
 			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ConsultarResumenPrestamoVigente_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
	   }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}
