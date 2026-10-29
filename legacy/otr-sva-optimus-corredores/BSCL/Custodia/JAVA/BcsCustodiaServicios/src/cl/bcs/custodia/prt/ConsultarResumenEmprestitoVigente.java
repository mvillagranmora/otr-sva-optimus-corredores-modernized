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

public class ConsultarResumenEmprestitoVigente  implements JavaCall {

	private final String SQL_SELECT_EMPRESTITOS_VIGENTES = loadResourceToString("cl/bcs/custodia/sql/prt/ConsultarResumenEmprestitoVigente_SelectEmprestitosVigentes.sql");

	public static class ConsultarResumenEmprestitoVigente_Entrada extends BcsPojo {
		public Long 			p_IdEmpresa;
		public Long				p_IdUsuario;
		public Long 			p_FolioCartera;
		public String			p_FechaConsulta;
		public String			p_ConsultaPor = "OPERACION";
		public String 			p_IdTipoPrestamo;
		public Long 			p_IdCliente;
		public Long 			p_IdPortafolio;
		public String 			p_IdClasificacionTipoInstrumento;
		
		public ConsultarResumenEmprestitoVigente_Entrada() {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
		}
	}
	
	public static class ConsultarResumenEmprestitoVigente_Salida extends BcsPojo {
		public List<EmprestitoVigente>	P_OUT_EMPRESTITOS_VIGENTES;
		
		public ConsultarResumenEmprestitoVigente_Salida() {
			this.P_OUT_EMPRESTITOS_VIGENTES	= new ArrayList<>();
		}
	}
	
	public static class EmprestitoVigente extends BcsPojo {
		public Long IdSubyacente;
		public String IdEstadoSubyacente;
		public String IdTipoPrestamoPrestamo;
		public String NombreTipoPrestamo;
		public Long IdInstrumentoSubyacente;
		public String NemoSubyacente;
		public String NemoSVSSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public Long FolioSubyacente;
		public String EsFungibleSubyacente;
		public String NombreTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public String IdTipoReajusteSubyacente;
		public String IdEmisorSubyacente;
		public String IdMonedaSubyacente;
		public String IdMonedaEmisionSubyacente;
		public Long IdCarteraRetirada;
		public String IdBovedaSubyacente;
		public Long FolioPrestamo;
		public Long IdClientePrestamo;
		public Long IdPortafolioPrestamo;
		public java.sql.Date FechaPrestamo;
		public Long MinimoDiasAnticipo;
		public Long MaximoDiasPrestamo;
		public String EsDiaHabilPlazoPrestamo;
		public String EsDiaHabilParaCalculoInteres;
		public Long IdCartera;
		public Long IdEmpresa;
		public String NombreEmpresa;
		public Long FolioEmprestito;
		public String IdEstado;
		public String IdMonedaSistema;
		public String Moneda;
		public String IdMonedaInstrumento;
		public String MonedaInstrumento;
		public String NombreEstado;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdBoveda;
		public String NombreBoveda;
		public String IdTipoReajuste;
		public String NombreTipoReajuste;
		public String NombreLargoTipoReajuste;
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
		public String RazonSocial;
		public String NemotecnicoEmprestito;
		public String NemoSVS;
		public Long IdInstrumentoEmprestito; 
		public String IdClasificacionTipoInstrumento;
		public String ClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String TipoInstrumento;
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
		public String UsaGarantiaEspecifica;
		public String FolioEntidad;
		public String CobraPrimaAlInicio;
		public String IdTipoCanalIngreso;
		public String INGGlosa;
		public java.sql.Date INGFechaOperacion;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoTotalEmprestito;
		public BigDecimal INGPrecioInstrumento;
		public BigDecimal INGTasaEmprestito;
		public BigDecimal INGMontoInstrumento;
		public java.sql.Date INGFechaPrecioTasaMercadoActual;
		public BigDecimal INGPrecioMercadoInstrumento;
		public BigDecimal INGTasaMercadoActual;
		public BigDecimal INGMontoMercadoInstrumento;
		public Long IdInstrumento;
		public BigDecimal INGMontoInteresCompraProximo;		
		public BigDecimal INGMontoInteresCorridoCompraActual;
		public String FolioSistemaExterno;
		public String ExigirGarantia;
		public Long IdMovimientoCarteraEnTransito;
		public BigDecimal INGTasaSubyacente;

		public BigDecimal INGCantidad;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGMontoAPlazo;
		public BigDecimal INGMontoPremio;
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
		ConsultarResumenEmprestitoVigente_Entrada in = gson.fromJson(jin.toString(), ConsultarResumenEmprestitoVigente_Entrada.class);
		ConsultarResumenEmprestitoVigente_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarResumenEmprestitoVigente_Salida logica(Connection con, ConsultarResumenEmprestitoVigente_Entrada entrada) throws Exception {
		ConsultarResumenEmprestitoVigente_Salida salida = new ConsultarResumenEmprestitoVigente_Salida();
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
			
			
			List<EmprestitoVigente> Emprestitos = sql.query(con, SQL_SELECT_EMPRESTITOS_VIGENTES, parametroEntrada, EmprestitoVigente.class);
			
			Emprestitos.forEach( Emprestito ->{
				String fechaOperacion = ("S".equalsIgnoreCase(UsaFechaLiquidacion) ? Emprestito.FechaLiquidacion.toString() : Emprestito.FechaEmprestito.toString());
				
				Emprestito.InteresDiario = 
						InteresCalculado(con,
								(Fecha.toDate(fechaSistema).compareTo(Fecha.toDate(fechaOperacion)) <= 0 ? fechaSistema : fechaHabilAnterior), 
								fechaSistema, 
								Emprestito.INGMontoATasaCompraActual != null ? Emprestito.INGMontoATasaCompraActual : new BigDecimal(0),
								Emprestito.IdMonedaSistema,
								Emprestito.INGTasaEmprestito != null ? Emprestito.INGTasaEmprestito : new BigDecimal(0));
				Emprestito.InteresAcumulado =
						InteresCalculado(con,
								fechaOperacion, 
								fechaSistema, 
								Emprestito.INGMontoATasaCompraActual != null ? Emprestito.INGMontoATasaCompraActual : new BigDecimal(0),
								Emprestito.IdMonedaSistema,
								Emprestito.INGTasaEmprestito != null ? Emprestito.INGTasaEmprestito : new BigDecimal(0));
				
			});
			salida.P_OUT_EMPRESTITOS_VIGENTES = Emprestitos;
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
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ConsultaPor\":\"OPERACION\",\"p_FechaConsulta\":\"2024-04-11\"}";
			
			
			ConsultarResumenEmprestitoVigente_Entrada Objin = gson.fromJson(json, ConsultarResumenEmprestitoVigente_Entrada.class);
			ConsultarResumenEmprestitoVigente demo = new ConsultarResumenEmprestitoVigente();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ConsultarResumenEmprestitoVigente_Salida ObjOut = demo.logica(con, Objin);
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
