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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarResumenEmprestitoHistorico  implements JavaCall {

	private final String SQL_SELECT_EMPRESTITOS_HISTORICOS = loadResourceToString("cl/bcs/custodia/sql/prt/ConsultarResumenEmprestitoVigente_SelectEmprestitosHistoricos.sql");

	public static class ConsultarResumenEmprestitoHistorico_Entrada extends BcsPojo {
		public Long 			p_IdEmpresa;
		public Long				p_IdUsuario;
		public Long 			p_FolioCartera;
		public String 			p_FechaConsultaDesde;
		public String			p_FechaConsultaHasta;
		public String 			p_IdEstado;
		public String 			p_IdTipoPrestamo;
		public Long 			p_IdCliente;
		public Long 			p_IdPortafolio;
		public String 			p_IdClasificacionTipoInstrumento;
		
		public ConsultarResumenEmprestitoHistorico_Entrada() {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
		}
	}
	
	public static class ConsultarResumenEmprestitoHistorico_Salida extends BcsPojo {
		public List<EmprestitoHistorico>	P_OUT_EMPRESTITOS_HISTORICOS;
		
		public ConsultarResumenEmprestitoHistorico_Salida() {
			this.P_OUT_EMPRESTITOS_HISTORICOS	= new ArrayList<>();
		}
	}
	
	public static class EmprestitoHistorico extends BcsPojo {
		public Long IdInstrumentoSubyacente;
		public String IdEmisorSubyacente;
		public String NemoSubyacente;
		public String NemoSVSSubyacente;
		public String EsFungibleSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public String IdMonedaSubyacente;
		public String IdTipoReajusteSubyacente;
		public String IdBovedaSubyacente;
		public Long IdCartera;
		public Long IdEmpresa;
		public Long FolioEmprestito;
		public Long FolioPrestamo;
		public String IdEstado;
		public String NombreEstado;
		public String IdMonedaSistema;
		public String IdMonedaInstrumento;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String IdBoveda;
		public String IdTipoReajuste;
		public String IdTipoProducto;
		public Long IdSucursal;
		public Long IdAgente;
		public String IdBolsa;
		public String IdConcepto;
		public String NombreConcepto;
		public String IdCondicionLiquidacion;
		public String IdModulo;
		public Long IdCliente;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String RazonSocial;
		public String NemotecnicoEmprestito;
		public String NemoSVS;
		public Long IdInstrumentoEmprestito;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdEmisor;
		public java.sql.Date FechaEmprestito;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public java.sql.Date FechaComprobante;
		public BigDecimal Cantidad;
		public BigDecimal CorteMinimo;
		public BigDecimal PrecioInstrumento;
		public BigDecimal MontoInstrumento;
		public BigDecimal TasaEmprestito;
		public BigDecimal ValorRescate;
		public java.sql.Date FechaVencimientoEmprestito;
		public java.sql.Date FechaEmisionInstrumento;
		public java.sql.Date FechaVencimientoInstrumento;
		public Long PlazoEnDiasOperacion;
		public String EsFungible;
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
		public java.sql.Date INGFechaLiquidacion;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoPremio;
		public BigDecimal INGMontoPrima;
		public BigDecimal INGMontoTotalEmprestito;
		public BigDecimal INGPrecioInstrumento;
		public BigDecimal INGTasaEmprestito;
		public BigDecimal INGMontoInstrumento;
		public java.sql.Date INGFechaPrecioTasaMercadoActual;
		public BigDecimal INGPrecioMercadoInstrumento;
		public BigDecimal INGTasaMercadoActual;
		public BigDecimal INGMontoMercadoInstrumento;
		
		public java.sql.Date EGRFechaOperacion;
		public String EGRIdTipoOperacion;
		public Long EGRIdOperacion;
		public Long EGRIdMovimientoCarteraEnTransito;
		public String EGRIdTipoCanalIngreso;
		public String EGRIdCondicionLiquidacion;
		public java.sql.Date EGRFechaLiquidacion;
		public String EGRIdTipoComprobante;
		public Long EGRFolioComprobante;
		public java.sql.Date EGRFechaComprobante;
		public String EGRFolioExternoReferencia;
		public String EGRIdModulo;
		public String EGRIdConcepto;
		public Long EGRIdUsuario;
		public Long EGRIdSucursal;
		public Long EGRIdAgente;
		public java.sql.Date EGRFechaCurseRetiro;
		public BigDecimal EGRCantidad;
		public BigDecimal EGRValorRescate;
		public BigDecimal EGRPrecio;
		public BigDecimal EGRTasa;
		public BigDecimal EGRMonto;
		public BigDecimal EGRMontoAPlazo;
		public BigDecimal EGRMontoMonedaInstrumento;
		public BigDecimal EGRMontoAPlazoMonedaInstrumento;
		public BigDecimal EGRSpread;
		public BigDecimal EGRPrecioAPlazo;
		public String EGRIdMonedaLiquidacion;
		public BigDecimal EGRMontoLiquidacion;
		public BigDecimal EGRMontoLiquidacionMonedaPrincipal;
		public BigDecimal INGPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioCompra;
		public Long IdCuentaCustodioPortafolio;
		public String	 		EGRGlosa;
		public BigDecimal 		EGRMontoAnticipo;
		public java.sql.Date 	EGRFechaAnticipo;
		public java.sql.Date	EGRFechaTermino;
		public BigDecimal		EGRMontoPremio;
		public BigDecimal		EGRMontoPremioMonedaInstrumento;
		
		public BigDecimal PrecioCompraInicial;
		public BigDecimal MontoCompraInicial;
		
		//Campos Calculados
		public BigDecimal InteresCorrido;

		public BigDecimal MontoPrecioMedio;
        public BigDecimal PrecioMedioInicial;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		ConsultarResumenEmprestitoHistorico_Entrada in = gson.fromJson(jin.toString(), ConsultarResumenEmprestitoHistorico_Entrada.class);
		ConsultarResumenEmprestitoHistorico_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarResumenEmprestitoHistorico_Salida logica(Connection con, ConsultarResumenEmprestitoHistorico_Entrada entrada) throws Exception {
		ConsultarResumenEmprestitoHistorico_Salida salida = new ConsultarResumenEmprestitoHistorico_Salida();
		SQL sql = new SQL();
		String UsaFechaLiquidacion	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		
		try {
			HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa"							, entrada.p_IdEmpresa);
			parametroEntrada.put("p_FechaConsultaDesde"					, entrada.p_FechaConsultaDesde);
			parametroEntrada.put("p_FechaConsultaHasta"					, entrada.p_FechaConsultaHasta);
			parametroEntrada.put("p_FolioCartera"						, entrada.p_FolioCartera);
			parametroEntrada.put("p_IdEstado"							, entrada.p_IdEstado);
			parametroEntrada.put("p_IdClasificacionTipoInstrumento"		, entrada.p_IdClasificacionTipoInstrumento);
			parametroEntrada.put("p_IdTipoPrestamo"						, entrada.p_IdTipoPrestamo);
			parametroEntrada.put("p_IdCliente"							, entrada.p_IdCliente);
			parametroEntrada.put("p_IdPortafolio"						, entrada.p_IdPortafolio);
			
			
			List<EmprestitoHistorico> Emprestitos = sql.query(con, SQL_SELECT_EMPRESTITOS_HISTORICOS, parametroEntrada, EmprestitoHistorico.class);
			
			Emprestitos.forEach( Emprestito ->{
				Emprestito.InteresCorrido =
						InteresCalculado(con, 
								("S".equalsIgnoreCase(UsaFechaLiquidacion) ? Emprestito.FechaLiquidacion.toString() : Emprestito.FechaVencimientoEmprestito.toString()), 
								Emprestito.EGRFechaTermino.toString(), 
								Emprestito.EGRMonto != null ? Emprestito.EGRMonto : new BigDecimal(0),
								Emprestito.IdMonedaInstrumento,
								Emprestito.EGRTasa != null ? Emprestito.EGRTasa : new BigDecimal(0));
				
			});
			salida.P_OUT_EMPRESTITOS_HISTORICOS = Emprestitos;
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
			
			String json = "{\"p_IdEmpresa\":1,\"p_FolioCartera\":0,\"p_FechaPrestamoDesde\":\"2024-02-02\",\"p_FechaPrestamoHasta\":\"2024-02-07\","
					+ "\"p_IdEstado\":9,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoPrestamo\":\"\",\"p_IdCliente\":4143,\"p_IdPortafolio\":6768}";
			
			
			ConsultarResumenEmprestitoHistorico_Entrada Objin = gson.fromJson(json, ConsultarResumenEmprestitoHistorico_Entrada.class);
			ConsultarResumenEmprestitoHistorico demo = new ConsultarResumenEmprestitoHistorico();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ConsultarResumenEmprestitoHistorico_Salida ObjOut = demo.logica(con, Objin);
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
