package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.ConstantesCustodia;
import cl.bcs.custodia.utils.CalculoInteres;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PrestamoHistoricoLiquidacionAlVencimiento  implements JavaCall {

	private final String SQL_SELECT_PRESTAMOS_HISTORICO									= loadResourceToString("cl/bcs/custodia/sql/prt/PrestamoHistoricoLiquidacionAlVencimiento_SelectPrestamoHistorico.sql");
	private final String SQL_SELECT_PRESTAMOS_HISTORICO_ACTUALIZAR_ESTADO_BLOCKCHAIN	= loadResourceToString("cl/bcs/custodia/sql/prt/PrestamoHistoricoLiquidacionAlVencimiento_SelectPrestamoHistoricoActualizarEstadoBlockChain.sql");
	private final String SQL_UPDATE_PRESTAMO_ESTADO_BLOCKCHAIN							= loadResourceToString("cl/bcs/custodia/sql/prt/PrestamoHistoricoLiquidacionAlVencimiento_UpdatePrestamoEstadoBlockChain.sql");
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 								= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");
	private final String SQL_SELECT_TIPO_DOCUMENTO_ES_BLOCKCHAIN						= loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectTipoDocumento.sql");
	
	public static class PrestamoHistoricoLiquidacionAlVencimiento_Entrada extends BcsPojo {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public Long 		p_FolioCartera;
		public String 		p_FechaConsultaDesde;
		public String		p_FechaConsultaHasta;
		public BigDecimal 	p_CantidadPorAsignar;
		public String 		p_IdEstado;
		public String 		p_IdTipoPrestamo;
		public Long 		p_IdCliente;
		public Long 		p_IdPortafolio;
		
		public PrestamoHistoricoLiquidacionAlVencimiento_Entrada() {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
		}
	}
	
	public static class PrestamoHistoricoLiquidacionAlVencimiento_Salida extends BcsPojo {
		public List<PrestamoHistorico>	P_OUT_PRESTAMOS_HISTORICO;
		
		public PrestamoHistoricoLiquidacionAlVencimiento_Salida() {
			this.P_OUT_PRESTAMOS_HISTORICO	= new ArrayList<>();
		}
	}
	
	public static class PrestamoHistorico extends BcsPojo {
		public Long 				FolioPrestamo;
		public String 				NombreEstado;
		public Long 				FolioComprobante;
		public String 				IdEstado;
		public String 				IdTipoPrestamo;
		public String 				NemoPrestamo;
		public String 				NombreTipoPrestamo;
		public String 				DocumentoIdentidad;
		public String 				CodigoPortafolio;
		public String 				RazonSocial;
		public String 				IdBolsa;
		public String 				NombreBolsa;
		public String 				NemoSubyacente;
		public java.sql.Date 		FechaPrestamo;
		public java.sql.Date 		FechaLiquidacion;
		public java.sql.Date 		FechaVencimientoPrestamo;
		public java.sql.Date 		FechaVencimientoInstrumento;
		public Long 				PlazoEnDiasOperacion;
		public java.sql.Date 		INGFechaLiquidacionPrestamo;
		public BigDecimal 			INGCantidad;
		public BigDecimal 			INGPrecioInstrumento;
		public BigDecimal 			INGTasaInstrumento;
		public BigDecimal 			INGMontoInstrumento;
		public BigDecimal 			MontoCompra;
		public BigDecimal 			INGTasaPrestamo;
		public BigDecimal 			INGMontoPrima;
		public BigDecimal 			INGMontoTotalPrestamo;
		public BigDecimal 			CantidadAsignada;
		public BigDecimal 			CantidadPorAsignar;
		public java.sql.Date 		INGFechaOperacion;
		public String 				NombreMercado;
		public String 				NombreEmisorSubyacente;
		public java.sql.Timestamp 	FechaIngresoRegistro;
		public String 				DescripcionPortafolio;
		public String 				TipoValorizacion;
		public Long 				IdCartera;
		public Long 				IdSubyacente;
		public String 				IdClasificacionTipoInstrumentoSubyacente;
		public Integer 				INGMontoInteresCorridoCompraActual;
		public BigDecimal 			INGMontoInteresCompraProximo;
		public String 				FolioSistemaExterno;
		public BigDecimal 			EGRMontoAnticipo;
		public java.sql.Date 		EGRFechaAnticipo;
		public String 				EstadoAnticipo;
		public String 				EstadoLiquidacion;
		public String 				IdCondicionLiquidacion;
		public Long					IdEmpresa;
		public String 				EsRenovable;
		public Long 				PlazoDisponibleRenovacion;
		public Long 				MinimoDiasPrestamo;
		public String 				NombreTipoInstrumentoSubyacente;
		public String				IdTipoReajusteSubyacente;
		public BigDecimal			CorteMinimo;
		public String				EsDiaHabilPlazoPrestamo;
		public String				IdTipoOperacion;
		public String				CobraPrimaAlInicio;
		public String 				IdMonedaSubyacente;
		public String 				IdMonedaEmision;
		public Long					FolioCarteraRelacionada;
		public String				TipoCustodia;
		public Long					MaximoDiasPrestamo;
		public BigDecimal			TasaPrestamo;
		public String				IdTipoInstrumentoSubyacente;
		public Long					IdInstrumentoSubyacente;
		public java.sql.Date		FechaEmisionInstrumento;
		public String				IdEmisorSubyacente;
		public BigDecimal			INGTasaSubyacente;
		
		public java.sql.Date		EGRFechaTermino;
		public BigDecimal 			EGRCantidad;
		public BigDecimal 			EGRPrecio;
		public BigDecimal 			EGRTasa;
		public BigDecimal 			EGRMonto;
		public BigDecimal 			EGRMontoAPlazo;
		public BigDecimal			EGRMontoPrima;
		public BigDecimal			EGRMontoMonedaInstrumento;
		public BigDecimal			EGRMontoAPlazoMonedaInstrumento;
		public BigDecimal			EGRMontoPrimaMonedaInstrumento;
		
		public BigDecimal PrecioCompraInicial;
		public BigDecimal MontoCompraInicial;
		
		//Campos Calculados
		public BigDecimal 			InteresCorrido;

		public BigDecimal MontoPrecioMedio;
        public BigDecimal PrecioMedioInicial;
	}
	
	public static class PrestamoOptimusBlockChain extends BcsPojo {
		public Long 	IdCartera;
		public String 	LAID;
		public Long 	FolioPrestamo;
		public String 	EstadoLiquidacion;
		public Boolean	ActualizarRegistro;
		
		public PrestamoOptimusBlockChain() {
			this.ActualizarRegistro = false;
		}
	}
	
	public static class LogProcesoPrestamoBlockChain extends BcsPojo {
		public Long 	IdEmpresa;
		public Long 	FolioCartera;
		public String 	FechaProceso;
		public String 	Descripcion;
		public String 	OrigenProcesoClase;
		public String 	OrigenProcesoMetodo;
		public String 	CreadorParametro;
		
		public LogProcesoPrestamoBlockChain(Long idEmpresa, Long folioCartera, String fechaProceso, String descripcion, String origenProcesoClase, String origenProcesoMetodo, String creadoPor) {
			this.IdEmpresa 				= idEmpresa;
			this.FolioCartera 			= folioCartera;
			this.FechaProceso 			= fechaProceso;
			this.Descripcion 			= descripcion;
			this.OrigenProcesoClase 	= origenProcesoClase;
			this.OrigenProcesoMetodo 	= origenProcesoMetodo;
			this.CreadorParametro 		= creadoPor;
		}
	}
	
	public static class TipoDocumento extends BcsPojo {
		public Long 	IdClase = 0L;
		public String 	TipoDocumentoNormativa;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		PrestamoHistoricoLiquidacionAlVencimiento_Entrada in = gson.fromJson(jin.toString(), PrestamoHistoricoLiquidacionAlVencimiento_Entrada.class);
		PrestamoHistoricoLiquidacionAlVencimiento_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public PrestamoHistoricoLiquidacionAlVencimiento_Salida logica(Connection con, PrestamoHistoricoLiquidacionAlVencimiento_Entrada entrada) throws Exception {
		String UsaFechaLiquidacion	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		PrestamoHistoricoLiquidacionAlVencimiento_Salida salida = new PrestamoHistoricoLiquidacionAlVencimiento_Salida();
		HashMap<String,Object> parametroEntrada 				= new HashMap<String,Object>();
		SQL sql 												= new SQL();
		List<TipoDocumento> tiposDocumento						= new ArrayList<>();
        List<String> tiposDocumentoConsulta						= new ArrayList<>();
		
		try {
			
			tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO42);
			tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO43);
			tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO48);
			
			parametroEntrada.put( "p_IdEmpresa"		, entrada.p_IdEmpresa );
			parametroEntrada.put( "p_TipoDocumento"	, tiposDocumentoConsulta );
			tiposDocumento = sql.query(con, SQL_SELECT_TIPO_DOCUMENTO_ES_BLOCKCHAIN, parametroEntrada, TipoDocumento.class);
			parametroEntrada.clear();
			
			String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
			parametroEntrada.put("p_IdEmpresa"			, entrada.p_IdEmpresa);
			parametroEntrada.put("p_FolioCartera"		, entrada.p_FolioCartera);
			parametroEntrada.put("p_FechaConsultaDesde"	, entrada.p_FechaConsultaDesde);
			parametroEntrada.put("p_FechaConsultaHasta"	, entrada.p_FechaConsultaHasta);
			parametroEntrada.put("p_CantidadPorAsignar"	, entrada.p_CantidadPorAsignar);
			parametroEntrada.put("p_IdEstado"			, entrada.p_IdEstado);
			parametroEntrada.put("p_IdTipoPrestamo"		, entrada.p_IdTipoPrestamo);
			parametroEntrada.put("p_IdCliente"			, entrada.p_IdCliente);
			parametroEntrada.put("p_IdPortafolio"		, entrada.p_IdPortafolio);
			
			if ( tiposDocumento.size() == 3 ) {
				
				List<PrestamoOptimusBlockChain> prestamosOptimusBlockChain = sql.query(con, SQL_SELECT_PRESTAMOS_HISTORICO_ACTUALIZAR_ESTADO_BLOCKCHAIN, parametroEntrada, PrestamoOptimusBlockChain.class);
				
				ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
				ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
				consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= entrada.p_IdEmpresa;
				consultaPrestamoPorLAIDEntrada.p_FolioExterno 	= prestamosOptimusBlockChain.parallelStream()
						.map( prestamo_bck -> new ConsultaListaPorFolioBlockChain.Folio(prestamo_bck.LAID) )
						.collect( Collectors.toList() );
				ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(con, consultaPrestamoPorLAIDEntrada);
				
				if ( consultaPrestamoPorLAIDSalida.p_Out_ContadorError == 0 ) {
					
					for (PrestamoOptimusBlockChain prestamoOptimusBlockChainItem : prestamosOptimusBlockChain) {
						if ( consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
								.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equalsIgnoreCase(prestamoOptimusBlockChainItem.LAID) )
								.count() > 0 ) {
							ConstantesCustodia constantesCustodia 	= new ConstantesCustodia();
							String statusBlockChain 			= consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
									.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equalsIgnoreCase(prestamoOptimusBlockChainItem.LAID) )
									.collect( Collectors.toList() )
									.get(0).laDetails.Status;
							String codigoDiccionario 			= constantesCustodia.codigoErroresBlockChain.get(statusBlockChain);
							String statusBlockChainTraducido 	= Diccionario.getMensaje(con, 1L, codigoDiccionario, new HashMap<String, String>());
							prestamoOptimusBlockChainItem.ActualizarRegistro = true;
							prestamoOptimusBlockChainItem.EstadoLiquidacion = statusBlockChainTraducido;
						}	
					}
					
					if ( prestamosOptimusBlockChain.stream()
							.filter( prestamo_optimus_bck -> prestamo_optimus_bck.ActualizarRegistro )
							.count() > 0 ) {
						sql.batch(con, SQL_UPDATE_PRESTAMO_ESTADO_BLOCKCHAIN, prestamosOptimusBlockChain.stream()
								.filter( prestamo_optimus_bck -> prestamo_optimus_bck.ActualizarRegistro )
								.collect( Collectors.toList() ), PrestamoOptimusBlockChain.class);
					}
					
					if ( prestamosOptimusBlockChain.stream()
							.filter( prestamo_optimus_bck -> !prestamo_optimus_bck.ActualizarRegistro )
							.count() > 0 ) {
						List<LogProcesoPrestamoBlockChain> logConsultaEstadoLiquidacionBlockChain = prestamosOptimusBlockChain.parallelStream()
								.filter( prestamo_optimus_bck -> !prestamo_optimus_bck.ActualizarRegistro )
								.map( prestamo_optimus_bck -> {
									return new LogProcesoPrestamoBlockChain(entrada.p_IdEmpresa, prestamo_optimus_bck.FolioPrestamo, fechaSistema, "UI_PRESTAMO_NO_ENCONTRADO_EN_BLOCKCHAIN", "PrestamoHistoricoLiquidacionAlVencimiento", "Logica", entrada.p_IdUsuario.toString());
									} )
								.collect( Collectors.toList() );
						sql.batch(con, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logConsultaEstadoLiquidacionBlockChain, LogProcesoPrestamoBlockChain.class);
					}
					con.commit();
				}
			}
			
			List<PrestamoHistorico> prestamosHistorico = sql.query(con, SQL_SELECT_PRESTAMOS_HISTORICO, parametroEntrada, PrestamoHistorico.class);
			
			prestamosHistorico.forEach( Prestamo ->{
				Prestamo.InteresCorrido =
						InteresCalculado(con,
								("S".equalsIgnoreCase(UsaFechaLiquidacion) ? Prestamo.FechaLiquidacion.toString() : Prestamo.FechaVencimientoPrestamo.toString()), 
								Prestamo.EGRFechaTermino.toString(), 
								Prestamo.EGRMonto != null ? Prestamo.EGRMonto : new BigDecimal(0),
								Prestamo.IdMonedaSubyacente,
								Prestamo.EGRTasa != null ? Prestamo.EGRTasa : new BigDecimal(0));
				
			});
			
			salida.P_OUT_PRESTAMOS_HISTORICO = prestamosHistorico;
			
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
			
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();					
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_FolioCartera\":9301514,\"p_FechaConsultaHasta\":\"2023-12-07\"}";
			PrestamoHistoricoLiquidacionAlVencimiento_Entrada Objin = gson.fromJson(json, PrestamoHistoricoLiquidacionAlVencimiento_Entrada.class);
			PrestamoHistoricoLiquidacionAlVencimiento demo = new PrestamoHistoricoLiquidacionAlVencimiento();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				PrestamoHistoricoLiquidacionAlVencimiento_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				//con.rollback();
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
	   }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}
