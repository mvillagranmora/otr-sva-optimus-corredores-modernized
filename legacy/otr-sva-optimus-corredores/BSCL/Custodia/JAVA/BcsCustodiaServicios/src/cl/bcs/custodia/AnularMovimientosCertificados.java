package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.AnularGarantia.EntradaAnularGarantia;
import cl.bcs.custodia.AnularGarantia.MovimientoEnTransito;
import cl.bcs.custodia.AnularGarantia.SalidaAnularGarantia;
import cl.bcs.custodia.pojo.Cartera;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.facturacion.AnularComprobanteFinal.CargoAbono_Proceso;
import cl.bcs.facturacion.AnularComprobanteFinal.EntidadTesoreria_Proceso;
import cl.bcs.facturacion.AnularComprobanteFinal.MovimientoCarteraEnTransito_Proceso;
import cl.bcs.facturacion.AnularComprobanteFinal.Subyacente_Proceso;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularMovimientosCertificados implements JavaCall {
	
	private final Log log = new Log("CUS.AnularMovimientosEnTransito");

	private final String SQL_SELECT_ES_ASOCIADO = loadResourceToString(
			"cl/bcs/custodia/sql/AnularMovimientosCertificados_Select_EsAsociado.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString(
			"cl/bcs/custodia/sql/AnularMovimientosCertificados_Select_Carteras.sql");
	private final String SQL_SELECT_MOVIMIENTO_ASOCIADO = loadResourceToString(
			"cl/bcs/custodia/sql/AnularMovimientosCertificados_Select_MovimientoEnTransitoRelacionado.sql");
	private final String SQL_SELECT_CANTIDAD_CARTERA= loadResourceToString(
			"cl/bcs/custodia/sql/AnularMovimientosCertificados_Select_CantidadCartera.sql");
	private final String SQL_SELECT_MOVIMIENTO_CERTIFICAR = loadResourceToString(
			"cl/bcs/custodia/sql/AnularMovimientosCertificados_Select_MovimientoACertificar.sql");
	private final String SQL_SELECT_MOVIMIENTO_NETEO_CARTERA = loadResourceToString(
			"cl/bcs/custodia/sql/AnularMovimientosCertificados_Select_MovimientoNeteoCartera.sql");

	public static class ObjMovimientos {
		public Long		IdMovimientoCarteraEnTransito;
		public String	IdTipoOperacion;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjValidaciones {
		public String EsAsociado;
		public Long IdCartera;
		public Long IdMovimientoCarteraEnTransito;
		public String PermiteCertificacionPorUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class Movimiento_Cartera {
		public Long IdMovimientoCarteraEnTransito;
		public Long IdMovimientoCarteraEnTransitoRelacionado;
		public String IdTipoOperacion;
		public String PermiteCertificacionPorUsuario;
		public String EsNominal;
		public String MueveCustodia;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Update_Folio{
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AnularMovimientosCertificadosEntrada {
		
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_simularOperacion = "N";
		public String 	p_UsaReglasNegocio = "S";
		public String	p_EsIngresoAutomatico = "N";
		public String	p_EjecutaAutorizacion = "S";
		public String	p_OmiteRegistroSinAutorizar = "S";
		public String 	p_Glosa = "";
		
		public List<Comprobante_Entrada> P_MOVIMIENTOSANULAR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public AnularMovimientosCertificadosEntrada() {
			P_MOVIMIENTOSANULAR = new ArrayList<Comprobante_Entrada>();
		}
	};
	
	public static class Comprobante_Entrada {
		public Long 	IdMovimientoCarteraTransito;
		public String	IdTipoOperacion;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AnularMovimientosCertificadosSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_TICKETATENCION;
		
		public AnularMovimientosCertificadosSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class Cartera_Proceso {
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public Long 		FolioComprobante;
		public Long 		IdMovimientoCarteraEnTransito;
		public String 		IdTipoComprobante;
		public String 		IdModulo;
		public Long 		FolioCartera;
		public String 		IdEstado;
		public String 		IdTipoOperacion;
		public Long 		IdCartera;
		public Long 		IdGarantia;
		public String		IdConcepto;
		public Long 		PlazoEnDiasOperacion;
		public String 		IdEmisor;
		public String 		IdTipoReajuste;
		public String 		IdMonedaEmision;
		public String IdMonedaAdicional;
		public String IdTipoReajusteInstrumento;
		public String IdMonedaInstrumento;

		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal INGCantidad;
		public BigDecimal CorteMinimo;
		public BigDecimal INGPrecioCompra;
		public BigDecimal INGTasaCompra;
		public BigDecimal EGRCantidad;
		public Long EGRFolioComprobante;
		public String TipoCartera;
		public Long IdInstrumento;
		public BigDecimal ValorRescate;
		public BigDecimal INGValorRescate;
		public BigDecimal EGRValorRescate;
		public String EsFungible;
		public Date INGFechaLiquidacion;
		public Date FechaLiquidacion;
		public String IdClasificacionTipoInstrumento;
		public Date FechaVencimientoOperacion;
		public Date FechaOperacionOriginal;
		public Date FechaEmisionInstrumento;

		public String IdOrigenGarantia;
		public Date FechaVencimientoInstrumento;
		public Long EGRIdOperacion;

		public String IdBoveda;
		public Long IdMesa;
		public String IdBolsa;
		public String IdCondicionLiquidacion;
		public Long IdAgente;
		public Long IdSucursal;
		public BigDecimal MontoCompra;
		public BigDecimal INGMontoATasaCompraActual;
		public String IdTipoCanalIngreso;
		public String FolioExternoReferencia;
		public String IdTipoProducto;
		public String NemoSVS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class retiroCustodiaEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario = 0L;
		public String p_IdModulo;
		public String p_SimularOperacion = "N";
		public Long p_TicketAtencion;
		public List<ObjDatosEntrada> p_DatosEntrada;
		public String p_ValorizacionAutomatica = "S";

		public Long p_idPais = null;

		public String toString() {
			return Utils.printJson(this, false);
		};
		public retiroCustodiaEntrada(){
	    	p_DatosEntrada = new ArrayList<ObjDatosEntrada>();
	    };	
	};
	
	public static class AnulacionCompraNormalCustodia {
		
		
		public RetiroCustodia.ObjDatosEntrada pasoAnulacionCartera;	
		
		public AnulacionCompraNormalCustodia(RetiroCustodia.ObjDatosEntrada dato) {
			this.pasoAnulacionCartera = dato;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class Comprobante_Proceso extends Cartera {
		public long ok;
		public long errores;
		public long autorizaciones;
		public long advertencias;
		
		public String periodoLiquidacion;

		public List<CargoAbono_Proceso> cargosAbonos;
		public List<EntidadTesoreria_Proceso> entidadesTesoreria;
		public List<MovimientoCarteraEnTransito_Proceso> movsCarteraTransito;
		public List<Cartera_Proceso> carteras;
		public List<Subyacente_Proceso> subyacentes;
		
		public Comprobante_Proceso() {
			super();
			cargosAbonos = new ArrayList<>();
			entidadesTesoreria = new ArrayList<>();
			movsCarteraTransito = new ArrayList<>();
			carteras = new ArrayList<>();
			subyacentes = new ArrayList<>();
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoCarteraEnTransitoNeteo extends BcsPojo {
		public Long IdMovimientoCarteraEnTransitoAnularUsuario;
		public BigDecimal CantidadAnularUsuario;
		public Long IdMovimientoCarteraEnTransitoFAERelacionada;
		public BigDecimal CantidadFAERelacionadaEgreso;
		public Long IdMovimientoCarteraEnTransitoAnularPosterior;
		public String IdEstadoAnularPosterior;
		public Long FolioCarteraUltimaRelacionada;
		public String IdEstadoCarteraUltimaRelacionada;
		public String EsNominal;
		public String IdTipoOperacion;
		public String IdConcepto;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public Long IdAgente;
		public BigDecimal MontoCompraMonedaInstrumento;
		public String EsVentaDescubierta;
		public String IdTipoProducto;
		public Long FolioCustodia;
	}
	
	public static class MovimientoNeteoResultado extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public MovimientoNeteoResultado() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}


	public AnularMovimientosCertificadosSalida logica(Connection con, AnularMovimientosCertificadosEntrada in)
			throws Exception {

		log.write("Inicio Anular Movimiento en Transito: " + in.toString());

		RetiroCustodia 	retiroCustodiaService 	= new RetiroCustodia();
		IngresoCustodia ingresoCustodiaService	= new IngresoCustodia(); 

		AnularMovimientosCertificadosSalida out = new AnularMovimientosCertificadosSalida();
		out.P_OUT_ERRORESCONTADOR 				= 0L;
		
		SQL sql = new SQL();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA",
				"PLA.FechaSistema");
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(con, in.p_IdEmpresa);

		try {

			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario,
					in.p_IdModulo);

			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			}
			;

			if (in.P_MOVIMIENTOSANULAR.size() == 0) {
				HashMap<String, String> valores = new HashMap<String, String>();
				valores.put("", "No hay movimientos para procesar");

				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS",
						valores, con, in.p_IdEmpresa));
			}
			;

			if (out.P_OUT_MENSAJES.size() > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			;

			Long TicketAtencion = LogCustodia.logAnularMovimientosCertificados(con, in);
			
			if (TicketAtencion.equals(0L)){
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			;

			List<Long> movimientos = in.P_MOVIMIENTOSANULAR.parallelStream()
					.map(movimiento -> movimiento.IdMovimientoCarteraTransito).collect(Collectors.toList());

			HashMap<String, Object> params = new HashMap<>();

			params.put("IdMovimientoCarteraEnTransito", movimientos);
			List<Movimiento_Cartera> movAnular = sql.query(con, SQL_SELECT_MOVIMIENTO_CERTIFICAR, params,
					Movimiento_Cartera.class);
			
			// Traemos las carteras relacionadas al movimiento
			List<Movimiento_Cartera> movAsociado = sql.query(con, SQL_SELECT_MOVIMIENTO_ASOCIADO, params,
					Movimiento_Cartera.class);
			movimientos.addAll(movAsociado.parallelStream().map(asociado -> asociado.IdMovimientoCarteraEnTransito)
					.collect(Collectors.toList()));
			List<Cartera_Proceso> carteras = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera_Proceso.class);
			List<ObjValidaciones> esAsociado = sql.query(con, SQL_SELECT_ES_ASOCIADO, params, ObjValidaciones.class);

			Map<Long, List<ObjValidaciones>> mapEsAsociado = esAsociado.parallelStream().collect(Collectors.groupingBy(cartera -> cartera.IdMovimientoCarteraEnTransito));
			
			
			if (("N").equalsIgnoreCase(movAnular.get(0).PermiteCertificacionPorUsuario)) {
				
				
				HashMap<String, String> valores = new HashMap<String, String>();
				
				valores.put("Movimiento", movAnular.get(0).IdMovimientoCarteraEnTransitoRelacionado.toString());
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"ERROR_ANULAR_MOVIMIENTO_CERTIFICADO", valores, con, in.p_IdEmpresa));

				if (out.P_OUT_MENSAJES.size() > 0) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;
				};
				
			}
			
			params.clear();
			params.put("p_IdMovimientoCarteraEnTransito", in.P_MOVIMIENTOSANULAR.parallelStream()
					.map(movimiento -> movimiento.IdMovimientoCarteraTransito).collect(Collectors.toList()));
			List<MovimientoCarteraEnTransitoNeteo> movimientosNeteoCartera = sql.query(con, SQL_SELECT_MOVIMIENTO_NETEO_CARTERA, params, MovimientoCarteraEnTransitoNeteo.class);
			
			List<Cartera_Proceso> carterasIngresadas = carteras.parallelStream().filter(cartera -> cartera.EGRIdOperacion.equals(0L)).collect(Collectors.toList());
			
			Map<Long, List<Cartera_Proceso>> mapCarteraProcesosIngresos = carterasIngresadas.parallelStream().collect(Collectors.groupingBy(cartera -> cartera.IdMovimientoCarteraEnTransito));
			
			List<Cartera_Proceso> carterasRetiradas = carteras.parallelStream().filter(cartera -> !cartera.EGRIdOperacion.equals(0L)).collect(Collectors.toList());
			Map<Long, List<Cartera_Proceso>> mapCarteraProcesosRetiros = carterasRetiradas.parallelStream().collect(Collectors.groupingBy(cartera -> cartera.EGRIdOperacion));
			Map<Long, Movimiento_Cartera> mapMovAsociados = movAsociado.parallelStream().collect(Collectors.toMap(mov -> mov.IdMovimientoCarteraEnTransitoRelacionado, mov -> mov));
			
			List<Long> valoresCartera = carterasRetiradas.parallelStream().map(cartera->cartera.FolioCartera).collect(Collectors.toList());
			
			Map<Long, Movimiento_Cartera> datosMovimientoAnular = movAnular.parallelStream().collect(Collectors.toMap(movimiento->movimiento.IdMovimientoCarteraEnTransito, movimiento->movimiento));
			
			if (movimientosNeteoCartera.stream()
					.filter(movimiento -> movimiento.IdEstadoCarteraUltimaRelacionada.equals("VIG")).count() > 0) {
				valoresCartera.addAll(movimientosNeteoCartera.stream()
						.filter(movimiento -> movimiento.IdEstadoCarteraUltimaRelacionada.equals("VIG"))
						.map(cartera->cartera.FolioCarteraUltimaRelacionada)
						.collect(Collectors.toList()));
			}
			params.clear();
			params.put("FolioCartera", valoresCartera);
			List<Cartera_Proceso> carterasVigentes = sql.query(con, SQL_SELECT_CANTIDAD_CARTERA, params, Cartera_Proceso.class);
			Map<Long,Cartera_Proceso> mapCarterasVigentes = carterasVigentes.parallelStream().collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
			
			IngresoCustodiaEntrada ingresoCustodiaEntrada = new IngresoCustodiaEntrada();
			RetiroCustodiaEntrada retiroCustodiaEntrada = new RetiroCustodiaEntrada();
			EntradaAnularGarantia entradaAnularGarantia = new EntradaAnularGarantia();
			
			if(carterasVigentes != null && carterasVigentes.size() > 0) {
				for(Cartera_Proceso cartera : carterasVigentes) {
					RetiroCustodia.ObjDatosEntrada carteraVigente = retiroCartera(cartera, fechaHoraSistema, fechaSistema);
					retiroCustodiaEntrada.p_DatosEntrada.add(carteraVigente);
				}
			}
			List<Comprobante_Entrada> movimientosAnularNeteo = new ArrayList<>();
			List<Long> carteraVigenteAnularNeteo = new ArrayList<>();
			
			for (Comprobante_Entrada mov : in.P_MOVIMIENTOSANULAR) {
				List<ObjValidaciones> listaDeAsociados = mapEsAsociado.get(mov.IdMovimientoCarteraTransito);
				Movimiento_Cartera datoMovimiento = datosMovimientoAnular.get(mov.IdMovimientoCarteraTransito);
				// Movimiento_Cartera movimientoAsociado =
				// mapMovAsociados.get(mov.IdMovimientoCarteraTransito);

				switch (mov.IdTipoOperacion) {
				case "INGTIT":
					List<Cartera_Proceso> carteraIngresos = mapCarteraProcesosIngresos.get(mov.IdMovimientoCarteraTransito);
					Movimiento_Cartera movimientoAsociadoIngreso = mapMovAsociados.get(mov.IdMovimientoCarteraTransito);
					
					if (listaDeAsociados != null && listaDeAsociados.size() > 0 && listaDeAsociados.get(0).EsAsociado.equalsIgnoreCase("N")) {
						for (Cartera_Proceso cartera : carteraIngresos) {
							RetiroCustodia.ObjDatosEntrada anulacionC = anulacionCartera(cartera, fechaHoraSistema, fechaSistema);
							retiroCustodiaEntrada.p_DatosEntrada.add(anulacionC);
							
						}
						if(movimientoAsociadoIngreso != null) {
							List<Cartera_Proceso> carterasMovimientoAsociado = mapCarteraProcesosRetiros.get(movimientoAsociadoIngreso.IdMovimientoCarteraEnTransito);
							for(Cartera_Proceso cartera : carterasMovimientoAsociado) {
								IngresoCustodia.ObjDatosEntrada ingresoC = reingresoCartera(cartera, fechaSistema, mapCarterasVigentes.get(cartera.FolioCartera));
								ingresoCustodiaEntrada.p_DatosEntrada.add(ingresoC);
							}
						}
					} else if(datoMovimiento != null && "N".equals(datoMovimiento.EsNominal) && "S".equals(datoMovimiento.MueveCustodia)) {
						List<MovimientoCarteraEnTransitoNeteo> movimientosNeteoCarteraPorTransito = movimientosNeteoCartera.stream()
								.filter(movimiento -> movimiento.IdMovimientoCarteraEnTransitoAnularUsuario.equals(mov.IdMovimientoCarteraTransito))
								.collect(Collectors.toList());
						List<Cartera_Proceso> carterasPorTransito = carteras.stream()
								.filter(cartera -> cartera.IdMovimientoCarteraEnTransito.equals(mov.IdMovimientoCarteraTransito))
								.collect(Collectors.toList());
						if (movimientosNeteoCarteraPorTransito == null || movimientosNeteoCarteraPorTransito.size() == 0 || carterasPorTransito == null || carterasPorTransito.size() == 0 || carteraIngresos == null || carteraIngresos.size() == 0) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraTransito, MensajeSalida.MENSAJE_ERROR, "EXISTE_MOVIMIENTO_ASOCIADO",new HashMap<String,String>(), con, in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR =+ 1L;
							continue;
						}
						for (Cartera_Proceso cartera : carteraIngresos) {
							IngresoCustodia.ObjDatosEntrada ingresoC = reingresoCartera(cartera, fechaSistema, mapCarterasVigentes.get(cartera.FolioCartera));
							ingresoCustodiaEntrada.p_DatosEntrada.add(ingresoC);
							RetiroCustodia.ObjDatosEntrada anulacionC = anulacionCartera(cartera, fechaHoraSistema, fechaSistema);
							retiroCustodiaEntrada.p_DatosEntrada.add(anulacionC);
							carteraVigenteAnularNeteo.add(cartera.IdCartera);
						}
						MovimientoNeteoResultado movimientoNeteoResultado = validaMovimientosPosterioresAAnular(con, movimientosNeteoCarteraPorTransito, in.p_IdEmpresa);
						out.P_OUT_MENSAJES.addAll(movimientoNeteoResultado.P_OUT_MENSAJES);
						out.P_OUT_ERRORESCONTADOR =+ movimientoNeteoResultado.P_OUT_ERRORESCONTADOR;
						IngresoCustodia.ObjDatosEntrada ingresoCarteraNegtiva = ingresaCustodiaNegativa(carterasPorTransito, movimientosNeteoCarteraPorTransito, fechaSistema, in.p_IdUsuario);
						ingresoCustodiaEntrada.p_DatosEntrada.add(ingresoCarteraNegtiva);
						List<Comprobante_Entrada> movimientosAnularNeteoFiltrado = movimientosNeteoCarteraPorTransito.stream()
								.map(mov_transito -> {
									Comprobante_Entrada movimientoTransito = new Comprobante_Entrada();
									movimientoTransito.IdMovimientoCarteraTransito = mov_transito.IdMovimientoCarteraEnTransitoFAERelacionada;
									movimientoTransito.IdTipoOperacion = mov_transito.IdTipoOperacion;
									return movimientoTransito;
								}).distinct().collect(Collectors.toList());
						if (movimientosAnularNeteoFiltrado.size() > 0) {
							movimientosAnularNeteo.addAll(movimientosAnularNeteoFiltrado);
						}
					}
					break;
				case "RETTIT":
					// INGRESOS
					List<Cartera_Proceso> carterasMovimiento = mapCarteraProcesosRetiros.get(mov.IdMovimientoCarteraTransito);
					Movimiento_Cartera movimientoAsociado = mapMovAsociados.get(mov.IdMovimientoCarteraTransito);
					/*for(Cartera_Proceso cartera : carterasMovimiento) {
						
					}*/
					// FALTA IF SI CARTERAS ASOCIADAS ES NULL
					if (carterasMovimiento != null) {
						for (Cartera_Proceso cartera : carterasMovimiento) {
							IngresoCustodia.ObjDatosEntrada ingresoC = reingresoCartera(cartera, fechaSistema, mapCarterasVigentes.get(cartera.FolioCartera));
							ingresoCustodiaEntrada.p_DatosEntrada.add(ingresoC);
						}
						if (movimientoAsociado != null) {
							List<Cartera_Proceso> carterasMovimientoAsociado = mapCarteraProcesosIngresos.get(movimientoAsociado.IdMovimientoCarteraEnTransito);
							for (Cartera_Proceso cartera : carterasMovimientoAsociado) {
								RetiroCustodia.ObjDatosEntrada anulacionC = anulacionCartera(cartera, fechaHoraSistema, fechaSistema);
								retiroCustodiaEntrada.p_DatosEntrada.add(anulacionC);
							}
						}
					}else if(datoMovimiento != null && "N".equals(datoMovimiento.EsNominal) && "S".equals(datoMovimiento.MueveCustodia)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraTransito, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_CARTERA",new HashMap<String,String>(), con, in.p_IdEmpresa));
					}
					break;
				case "INGGTA":
					entradaAnularGarantia.p_MovimientosEnTransito.add(new MovimientoEnTransito(mov.IdMovimientoCarteraTransito, mov.IdTipoOperacion));
					
					break;
				case "RETGTA":
					entradaAnularGarantia.p_MovimientosEnTransito.add(new MovimientoEnTransito(mov.IdMovimientoCarteraTransito, mov.IdTipoOperacion));
					
					break;
				default:
					//Solo se pueden anular movimientos de ingreso/retiro titulos, el flujo no esta preparado para otros casos
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraTransito, MensajeSalida.MENSAJE_ERROR, "MOVIMIENTO_NO_SE_PUEDE_ANULAR",new HashMap<String,String>(), con, in.p_IdEmpresa));
					return out;
				}
			}
			
			if (out.P_OUT_ERRORESCONTADOR > 0L) {
				return out;
			}
			
			if("N".equals(in.p_simularOperacion)) {
				
				IngresoCustodiaSalida ingresoCustodiaSalidaNeteo = new IngresoCustodiaSalida();
				
				if (ingresoCustodiaEntrada.p_DatosEntrada.size() > 0) {
					ingresoCustodiaEntrada.p_IdEmpresa = in.p_IdEmpresa;
					ingresoCustodiaEntrada.p_IdModulo = in.p_IdModulo;
					ingresoCustodiaEntrada.p_IdUsuario = in.p_IdUsuario;
					ingresoCustodiaEntrada.p_SimularOperacion = in.p_simularOperacion;
					ingresoCustodiaEntrada.p_TicketAtencion = 0L;
					ingresoCustodiaEntrada.p_ValorizacionAutomatica = "N"; 
					IngresoCustodiaSalida ingresoCustodiaSalida = ingresoCustodiaService.logica(con, ingresoCustodiaEntrada);
					out.P_OUT_MENSAJES.addAll(ingresoCustodiaSalida.P_OUT_MENSAJES);
					ingresoCustodiaSalidaNeteo = ingresoCustodiaSalida;
					ingresoCustodiaSalida.P_OUT_DATOSSALIDA
					.forEach(salida -> {
						if (salida.TablaOrigen.equalsIgnoreCase("NEGATIVA")) {
							List<IngresoCustodia.ObjDatosEntrada> p_DatosEntrada = ingresoCustodiaEntrada.p_DatosEntrada.stream()
							.filter(entrada -> entrada.TablaAfectada.equals("NEGATIVA") && entrada.IdGenerarMovimientoCustodia.equals(salida.IdGenerarMovimientoCustodia))
							.collect(Collectors.toList());
							List<String> idClasificacionTipoInstrumento = carteras.stream()
									.filter(cartera -> cartera.IdCartera.equals(salida.IdGenerarMovimientoCustodia))
									.map(cartera -> cartera.IdClasificacionTipoInstrumento).collect(Collectors.toList());
							String nemo = idClasificacionTipoInstrumento.size() > 0 && idClasificacionTipoInstrumento.get(0).equalsIgnoreCase("IF") ? p_DatosEntrada.get(0).NemoSVS : p_DatosEntrada.get(0).Nemotecnico;
							HashMap<String, String> valores = new HashMap<String, String>();
							valores.put("FolioCartera", salida.IdCustodia.toString());
							valores.put("Nemotecnico",nemo);
							try {
								out.P_OUT_MENSAJES.add(new MensajeSalida(salida.IdGenerarMovimientoCustodia, MensajeSalida.MENSAJE_INFORMACION, "UI_CARTERA_NEGATIVA_INGRESADA", valores,con, in.p_IdEmpresa));
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
					});
					
				}
				
				if (carteraVigenteAnularNeteo.size() > 0) {
					for(Long idCartera : carteraVigenteAnularNeteo) {
					    ingresoCustodiaSalidaNeteo.P_OUT_DATOSSALIDA.stream()
					    		.filter(dato_salida -> dato_salida.IdGenerarMovimientoCustodia.equals(idCartera) && dato_salida.TablaOrigen.equals("CARTERA"))
					    		.forEach(resultado -> {
					    			retiroCustodiaEntrada.p_DatosEntrada.stream()
								    .filter(retiro -> retiro.IdGenerarMovimientoCustodia.equals(idCartera) && retiro.AccionOperacion.equals("ANULACION"))
								    .forEach(retiro -> {
								    	retiro.IdCustodia = resultado.IdCustodia;
								    	retiro.IdGenerarMovimientoCustodia = resultado.IdCustodia;
								    });
							    });
					}
				}
				
				if(retiroCustodiaEntrada.p_DatosEntrada.size() > 0) {
					retiroCustodiaEntrada.p_IdEmpresa = in.p_IdEmpresa;
					retiroCustodiaEntrada.p_IdModulo = in.p_IdModulo;
					retiroCustodiaEntrada.p_IdUsuario = in.p_IdUsuario;
					retiroCustodiaEntrada.p_SimularOperacion = in.p_simularOperacion;
					retiroCustodiaEntrada.p_TicketAtencion = 0L;
					retiroCustodiaEntrada.p_ValorizacionAutomatica = "N";
					RetiroCustodiaSalida retiroCustodiaSalida = retiroCustodiaService.logica(con,retiroCustodiaEntrada);
					out.P_OUT_MENSAJES.addAll(retiroCustodiaSalida.P_OUT_MENSAJES);
				}
				
				if(entradaAnularGarantia.p_MovimientosEnTransito.size() >0) {
					AnularGarantia servicioAnularGarantia = new AnularGarantia();
					entradaAnularGarantia.p_IdEmpresa = in.p_IdEmpresa;
					entradaAnularGarantia.p_IdModulo = in.p_IdModulo;
					entradaAnularGarantia.p_IdUsuario = in.p_IdUsuario;
					entradaAnularGarantia.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					entradaAnularGarantia.p_SimularOperacion = in.p_simularOperacion;
					SalidaAnularGarantia salidaAnularGarantia = servicioAnularGarantia.logica(con, entradaAnularGarantia);
					out.P_OUT_MENSAJES.addAll(salidaAnularGarantia.P_OUT_MENSAJES);
				}
				
				CacheValorizacion.valorizar(con);
				
				if (out.P_OUT_ERRORESCONTADOR == 0) {
					// se agrega movimientos FAE correspondientes a Neteo de cartera
					if (movimientosAnularNeteo.size() > 0) {
						in.P_MOVIMIENTOSANULAR.addAll(movimientosAnularNeteo);
					}
					// Anular Movimiento Cartera En Transito
					AnularMovimientoEnCartera(con,in);
				}
			}
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		} catch (Exception er) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(er);
			er.printStackTrace();
		}
		;
		return out;
	}

	private RetiroCustodia.ObjDatosEntrada anulacionCartera(Cartera_Proceso cartera, Timestamp fechaHoraSistema,
			String fechaSistema) {
		RetiroCustodia.ObjDatosEntrada anulacionCartera = new RetiroCustodia.ObjDatosEntrada();

		anulacionCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
		anulacionCartera.TablaAfectada = cartera.TipoCartera;
		anulacionCartera.IdCustodia = cartera.IdCartera;
		anulacionCartera.FolioCustodia = cartera.FolioCartera;
		anulacionCartera.AccionOperacion = "ANULACION";
		anulacionCartera.FechaRetiroRegistro = fechaHoraSistema;
		anulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
		anulacionCartera.IdInstrumento = cartera.IdInstrumento;
		anulacionCartera.Cantidad = cartera.Cantidad;
		anulacionCartera.ValorRescate = cartera.ValorRescate;
		anulacionCartera.IdCliente = cartera.IdCliente;
		anulacionCartera.IdPortafolio = cartera.IdPortafolio;
		anulacionCartera.PrecioCompra = cartera.INGPrecioCompra;
		anulacionCartera.TasaCompra = cartera.INGTasaCompra;
		anulacionCartera.IdOrigenGarantia = cartera.IdOrigenGarantia;
		anulacionCartera.IdTipoOperacion = "RETTIT";
		anulacionCartera.IdConcepto = cartera.IdConcepto;
		anulacionCartera.FechaLiquidacion = cartera.FechaLiquidacion;
		anulacionCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
		anulacionCartera.EsFungible= cartera.EsFungible;
		return anulacionCartera;
	}
	
	private RetiroCustodia.ObjDatosEntrada retiroCartera(Cartera_Proceso cartera, Timestamp fechaHoraSistema,
			String fechaSistema) {
		RetiroCustodia.ObjDatosEntrada retiroCartera = new RetiroCustodia.ObjDatosEntrada();

		retiroCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
		retiroCartera.TablaAfectada = cartera.TipoCartera;
		retiroCartera.IdCustodia = cartera.IdCartera;
		retiroCartera.FolioCustodia = cartera.FolioCartera;
		retiroCartera.AccionOperacion = "RETIRO";
		retiroCartera.FechaRetiroRegistro = fechaHoraSistema;
		retiroCartera.FechaOperacion = Fecha.toDate(fechaSistema);
		retiroCartera.IdInstrumento = cartera.IdInstrumento;
		retiroCartera.Cantidad = cartera.INGCantidad;
		retiroCartera.ValorRescate = cartera.INGValorRescate;
		retiroCartera.IdCliente = cartera.IdCliente;
		retiroCartera.IdPortafolio = cartera.IdPortafolio;
		retiroCartera.PrecioCompra = cartera.INGPrecioCompra;
		retiroCartera.TasaCompra = cartera.INGTasaCompra;
		retiroCartera.IdOrigenGarantia = cartera.IdOrigenGarantia;
		retiroCartera.IdTipoOperacion = "RETTIT";
		retiroCartera.IdConcepto = cartera.IdConcepto;
		retiroCartera.FechaLiquidacion = cartera.FechaLiquidacion;
		retiroCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
		retiroCartera.EsFungible = cartera.EsFungible;
		return retiroCartera;
	}

	private IngresoCustodia.ObjDatosEntrada reingresoCartera(Cartera_Proceso cartera, String fechaSistema, Cartera_Proceso map){

		IngresoCustodia.ObjDatosEntrada reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
		BigDecimal cantidadVigente = BigDecimal.ZERO;
		BigDecimal cantidadRescate = BigDecimal.ZERO;
		if(map != null) {
			switch (cartera.IdClasificacionTipoInstrumento) {
			case "IF":
				cantidadRescate = map.INGValorRescate;
				break;
			case "RV":
				cantidadVigente = map.INGCantidad;
				break;
			case "RF":
				cantidadVigente = map.INGCantidad;
				break;
			}
		}
		
		reingresoCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
		reingresoCartera.TablaAfectada = "CARTERA";
		reingresoCartera.AccionOperacion = "INGRESO";
		reingresoCartera.IdCliente = cartera.IdCliente;
		reingresoCartera.IdPortafolio = cartera.IdPortafolio;
		reingresoCartera.FolioCustodia = cartera.FolioCartera;
		reingresoCartera.IdCustodia = cartera.IdCartera;
		reingresoCartera.TasaCompra = cartera.INGTasaCompra;
		reingresoCartera.PrecioCompra = cartera.INGPrecioCompra;
		reingresoCartera.FechaLiquidacion = cartera.INGFechaLiquidacion;
		reingresoCartera.Cantidad = cartera.EGRCantidad.add(cantidadVigente, MathContext.DECIMAL128);
		reingresoCartera.IdInstrumento = cartera.IdInstrumento;
		reingresoCartera.ValorRescate = cartera.EGRValorRescate.add(cantidadRescate, MathContext.DECIMAL128);
		reingresoCartera.FechaOperacion = Fecha.toDate(fechaSistema);
		reingresoCartera.EsRegistroNuevo = "N";
		reingresoCartera.GenerarNuevoFolioCustodia = "N";
		reingresoCartera.MantieneFolioCustodia = "S";
		reingresoCartera.PlazoEnDiasOperacion = cartera.PlazoEnDiasOperacion;
		reingresoCartera.IdEmisor = cartera.IdEmisor;
		reingresoCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
		reingresoCartera.IdMonedaEmision = cartera.IdMonedaEmision;
		reingresoCartera.IdTipoOperacion = cartera.IdTipoOperacion;
		reingresoCartera.IdConcepto = cartera.IdConcepto;
		reingresoCartera.Nemotecnico = cartera.Nemotecnico;
		reingresoCartera.IdTipoComprobante = cartera.IdTipoComprobante;
		reingresoCartera.FolioComprobante = cartera.FolioComprobante;
		reingresoCartera.MontoCompra = cartera.INGCantidad;
		reingresoCartera.IdBoveda = cartera.IdBoveda;
		reingresoCartera.FechaVencimientoInstrumento = cartera.FechaVencimientoInstrumento;
		reingresoCartera.FechaVencimientoOperacion = cartera.FechaVencimientoOperacion;
		reingresoCartera.IdMonedaAdicional = cartera.IdMonedaAdicional;		
		reingresoCartera.CorteMinimo = cartera.CorteMinimo;
		reingresoCartera.FechaEmisionInstrumento = cartera.FechaEmisionInstrumento;
		reingresoCartera.IdMonedaInstrumento = cartera.IdMonedaInstrumento;
		reingresoCartera.IdMesa = cartera.IdMesa;
		reingresoCartera.IdBolsa = cartera.IdBolsa;
		reingresoCartera.IdCondicionLiquidacion = cartera.IdCondicionLiquidacion;
		reingresoCartera.IdAgente = cartera.IdAgente;
		reingresoCartera.IdSucursal = cartera.IdSucursal;
		reingresoCartera.MontoCompra = cartera.MontoCompra;
		//reingresoCartera.INGMontoATasaCompraActual = cartera.INGMontoATasaCompraActual;
		reingresoCartera.IdMovimientoCarteraEnTransitoOrigen = cartera.IdMovimientoCarteraEnTransito;
		reingresoCartera.EsFungible = cartera.EsFungible;
		
		return reingresoCartera;
	}

	private void AnularMovimientoEnCartera(Connection con,
			AnularMovimientosCertificadosEntrada in) throws Exception {

		try {
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> movimientos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
			movimientos = in.P_MOVIMIENTOSANULAR.parallelStream().map(movimiento->{
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = movimiento.IdMovimientoCarteraTransito;
				MOV.EsAnulacion = "S";
				MOV.IdUsuario = in.p_IdUsuario;
				return MOV;
			}).collect(Collectors.toList());

			GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, movimientos);

		} catch (Exception e) {
			
		}		
	}
	
	private MovimientoNeteoResultado validaMovimientosPosterioresAAnular(Connection con, List<MovimientoCarteraEnTransitoNeteo> movimientosNeteoCartera, Long idEmpresa) throws Exception {
		MovimientoNeteoResultado salida = new MovimientoNeteoResultado();
		if (movimientosNeteoCartera.size() == 0) {
			return salida;
		}
		// Verifica si existe movimiento posterior relacionado a una misma operacion neteada que deba ser anulado previamente
		List<Long> movimientosAnularPreviamente = movimientosNeteoCartera.stream()
		.filter(movimiento -> movimiento.IdMovimientoCarteraEnTransitoAnularPosterior != null && movimiento.IdEstadoAnularPosterior.equalsIgnoreCase("CRT"))
		.map(mov -> mov.IdMovimientoCarteraEnTransitoAnularPosterior)
		.collect(Collectors.toList());
		if (movimientosAnularPreviamente.size() > 0) {
			Long idMovimientoCarteraEnTransitoAnularCascadaMaximo = Collections.max(movimientosAnularPreviamente);
			HashMap<String,String> datos = new HashMap<String,String>();
			datos.put("IdMovimientoCarteraEnTransitoRelacionado", idMovimientoCarteraEnTransitoAnularCascadaMaximo.toString());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(idMovimientoCarteraEnTransitoAnularCascadaMaximo, MensajeSalida.MENSAJE_ERROR, "ANULAR_MOVIMIENTO_NETEO_POSTERIOR",datos, con, idEmpresa));
			salida.P_OUT_ERRORESCONTADOR =+ 1L;
			return salida;
		}
		return salida;
	}
	
	private IngresoCustodia.ObjDatosEntrada ingresaCustodiaNegativa(List<Cartera_Proceso> cartera, List<MovimientoCarteraEnTransitoNeteo> movimientosNeteoCarteraPorTransito, String fechaSistema, Long idUsuario) throws Exception {
		
		BigDecimal sumaCantidadFAERelacionadaEgreso = BigDecimal.ZERO;
		BigDecimal cantidadCarteraNegativa = BigDecimal.ZERO;
		for (MovimientoCarteraEnTransitoNeteo movimiento : movimientosNeteoCarteraPorTransito) {
			if (movimiento.IdMovimientoCarteraEnTransitoAnularPosterior != null) continue;
			sumaCantidadFAERelacionadaEgreso = sumaCantidadFAERelacionadaEgreso.add(movimiento.CantidadFAERelacionadaEgreso);
		}
		if (sumaCantidadFAERelacionadaEgreso.compareTo(BigDecimal.ZERO) == 1 && movimientosNeteoCarteraPorTransito.get(0).CantidadAnularUsuario.compareTo(sumaCantidadFAERelacionadaEgreso) == 1) {
			cantidadCarteraNegativa = sumaCantidadFAERelacionadaEgreso;
		} else {
			cantidadCarteraNegativa = movimientosNeteoCarteraPorTransito.get(0).CantidadAnularUsuario;
		}
		
		IngresoCustodia.ObjDatosEntrada c = new IngresoCustodia.ObjDatosEntrada();
		c.IdGenerarMovimientoCustodia 			= cartera.get(0).IdCartera;	//debe ser el mismo para que la salida pueda interpretar
		c.TablaAfectada							= "NEGATIVA";
		c.AccionOperacion						= "INGRESO";
		c.IdMovimientoCarteraEnTransitoOrigen 	= movimientosNeteoCarteraPorTransito.get(0).IdMovimientoCarteraEnTransitoAnularUsuario;// mov.IdMovimientoCarteraEnTransito;
		c.IdCliente								= cartera.get(0).IdCliente;
		c.IdPortafolio							= cartera.get(0).IdPortafolio;
		c.FechaOperacion						= Fecha.toDate(fechaSistema);
		c.IdTipoOperacion						= movimientosNeteoCarteraPorTransito.get(0).IdTipoOperacion; 	
		c.IdConcepto							= movimientosNeteoCarteraPorTransito.get(0).IdConcepto;
		c.IdCondicionLiquidacion				= cartera.get(0).IdCondicionLiquidacion;
		c.FechaLiquidacion						= cartera.get(0).FechaLiquidacion;
		c.IdTipoCanalIngreso					= cartera.get(0).IdTipoCanalIngreso;	
		c.IdTipoComprobante						= movimientosNeteoCarteraPorTransito.get(0).IdTipoComprobante;
		c.FolioComprobante						= movimientosNeteoCarteraPorTransito.get(0).FolioComprobante;
		c.FolioCustodia							= 0L;
		c.FolioExternoReferencia				= cartera.get(0).FolioExternoReferencia;
		c.IdUsuario								= idUsuario;
		c.IdSucursal							= cartera.get(0).IdSucursal;
		c.IdAgente								= movimientosNeteoCarteraPorTransito.get(0).IdAgente;
		c.Glosa									= "";
		c.IdInstrumento							= cartera.get(0).IdInstrumento;
		c.Nemotecnico                      		= cartera.get(0).Nemotecnico;
		c.NemoSVS                          		= cartera.get(0).NemoSVS;
		c.IdMonedaInstrumento					= cartera.get(0).IdMonedaInstrumento;
		c.IdMonedaEmision						= cartera.get(0).IdMonedaEmision;
		c.IdTipoReajusteInstrumento				= cartera.get(0).IdTipoReajuste;
		c.IdEmisor								= cartera.get(0).IdEmisor;
		c.EsFungible							= cartera.get(0).EsFungible;
		c.IdBoveda								= cartera.get(0).IdBoveda;
		c.EsNominal								= movimientosNeteoCarteraPorTransito.get(0).EsNominal;
		c.Cantidad								= cartera.get(0).IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && cartera.get(0).EsFungible.equalsIgnoreCase("S") ? BigDecimal.ONE : cantidadCarteraNegativa;
		c.CorteMinimo							= cartera.get(0).CorteMinimo;
		c.ValorRescate							= cartera.get(0).ValorRescate;
		c.PrecioCompra							= cartera.get(0).INGPrecioCompra;
		c.TasaCompra							= cartera.get(0).INGTasaCompra;
		c.MontoCompraMonedaInstrumento			= movimientosNeteoCarteraPorTransito.get(0).MontoCompraMonedaInstrumento;
		c.EsVentaDescubierta					= movimientosNeteoCarteraPorTransito.get(0).EsVentaDescubierta;
		c.IdTipoProducto						= movimientosNeteoCarteraPorTransito.get(0).IdTipoProducto;
		c.FechaVencimientoInstrumento  			= cartera.get(0).FechaVencimientoInstrumento;
		if (cartera.get(0).IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
			c.FechaVencimientoOperacion				= cartera.get(0).FechaVencimientoInstrumento;
		}
		c.IdMovimientoComprobante				= 0L;//TODO ESTE DATO NO ESTA EN EL RETIRO
		c.EsRegistroNuevo 						= "S";
		return c;
	}

	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		AnularMovimientosCertificadosEntrada jin = gson.fromJson(in.toString(),
				AnularMovimientosCertificadosEntrada.class);
		AnularMovimientosCertificadosSalida out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) {
			try {
				System.out.println("COMIENZA");
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"P_MOVIMIENTOSANULAR\":[{\"IdMovimientoCarteraTransito\":779119,\"IdTipoOperacion\":\"INGGTA\",\"IdEstado\":\"CRT\"}],\"p_GlosaAnulacion\":\"\",\"p_FechaProceso\":\"2024-09-09\",\"p_IdSucursal\":0}";
				AnularMovimientosCertificadosEntrada Objin = gson.fromJson(json, AnularMovimientosCertificadosEntrada.class);

				AnularMovimientosCertificados kk = new AnularMovimientosCertificados();
				AnularMovimientosCertificadosSalida asdf = kk.logica(con, Objin);
				System.out.println(asdf);
//				con.commit();
				con.rollback();
				System.out.println("END");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
