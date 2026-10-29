package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearComprobante.ResultadoProceso_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearVistaPreliminarFacturacionCobroMensual.EntradaComprobante;
import cl.bcs.facturacion.CrearVistaPreliminarFacturacionCobroMensual.FacturacionCobro;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarFacturacionCobroMensual implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EnviarFacturacionCobroMensual.class);
	
	private final String SQL_SELECT_COMPROBANTES_COBROS = loadResourceToString("cl/bcs/facturacion/sql/EnviarFacturacionCobroMensual_SelectComprobantesCobros.sql");
	private final String SQL_INSERT_COMPROBANTES_COBROS = loadResourceToString("cl/bcs/facturacion/sql/EnviarFacturacionCobroMensual_InsertComprobantesCobros.sql");
	private final String SQL_INSERT_COMPROBANTES_COBROS_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/EnviarFacturacionCobroMensual_InsertComprobantesCobrosDetalle.sql");	

	public static class EntradaEnviarFacturacionComisionMensual extends BcsPojo {
	  public Long		p_IdEmpresa;		
	  public Long		p_IdUsuario;   
	  public String		p_IdModulo;   
	  public String		p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
	  public String		p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
	  public String		p_EsIngresoAutomatico;			// S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente
	  public String		p_CrearComprobanteAutomatico;
	  public String		p_EsCargaMasiva;
	  public BigDecimal	p_PorcentajeIva;
	  public String		p_MesProcesar;
	  public List<EntradaCobroComision>	p_CobroComision;
	  
		
	  public EntradaEnviarFacturacionComisionMensual (){
		  p_EsCargaMasiva = "N";
		  p_simularOperacion = "N";
		  p_EsIngresoAutomatico = "S";
		  p_CrearComprobanteAutomatico = "S";
		  p_CobroComision	= new ArrayList<EntradaCobroComision>();
	  }
	};
	
	public static class EntradaCobroComision extends BcsPojo {
		public Long   		IdCliente;
		public String 		NombreCliente;
		public String 		DocumentoIdentidad;
		public Long   		IdPortafolio;
		public String 		IdMoneda;
		public String 		ModPrefFact;
		public String 		ModoFacturacion;
		public Long   		IdContratoComision;
		public String 		IdClasificaciontipoInstrumento;
		public String 		NombreConvenioContrato;
		public BigDecimal 	Monto;
		public BigDecimal 	MontoDerecho;
		public BigDecimal 	MontoComision;
		public String 		IdConcepto;
		public String 		NombreConcepto;
		public Long			IdInstrumento = 0L;
		public List<Long> 	IdComprobanteRel;
		
		public EntradaCobroComision() {
			Monto = BigDecimal.ZERO;
			MontoDerecho = BigDecimal.ZERO;
			MontoComision = BigDecimal.ZERO;
			IdComprobanteRel = new ArrayList();
		}
	};
		
	public static class SalidaEnviarFacturacionComisionMensual extends BcsPojo {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<ComprobanteCobros_Salida>		P_OUT_COMPROBANTE_COBROS;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		public List<MovimientoComprobante_Salida>	P_OUT_MOVIMIENTO;
		
		public SalidaEnviarFacturacionComisionMensual () {
			 P_OUT_MENSAJES	= new ArrayList<>();
			 P_OUT_COMPROBANTE = new ArrayList<>();
			 P_OUT_COMPROBANTE_COBROS = new ArrayList<>();
			 P_OUT_RESULTADOPROCESO = new ArrayList<>();
			 P_OUT_MOVIMIENTO = new ArrayList<>();
		  }
	};
	
	public static class ComprobanteCobros_Salida extends BcsPojo {
		public Long 		IdComprobanteComisionMensual;
		public Long 		IdMovimientoComprobanteComisionMensual;
		public String 		IdMoneda;
		public BigDecimal 	Monto;
		public BigDecimal 	MontoIva;
		public BigDecimal 	MontoComision;
		public BigDecimal 	MontoDerecho;
				
		public Long			NumeroMvtoCmbte;
		public List<Long> 	IdComprobanteRel;
		public Long			IdContratoComision;
		
		public ComprobanteCobros_Salida() {
			Monto 			= BigDecimal.ZERO;
			MontoDerecho 	= BigDecimal.ZERO;
			MontoComision 	= BigDecimal.ZERO;	
			IdComprobanteRel = new ArrayList<Long>();
		}
	}

	public static class ComprobanteCobrosDetalle_Salida extends BcsPojo {
		public Long 		IdContratoComision;
		public Long 		IdComprobante;
		public Long 		IdComprobanteComisionMensual;
		public Long 		IdMovimientoComprobanteComisionMensual = 0L;
		
		public void setIdComprobanteComisionMensual(Long idComprobanteComisionMensual) {
			IdComprobanteComisionMensual = idComprobanteComisionMensual;
		}
	}
	
	public SalidaEnviarFacturacionComisionMensual logica (Connection con, EntradaEnviarFacturacionComisionMensual entrada) throws Exception {
		logger.info("INICIO::EnviarFacturacionCobroMensual");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaEnviarFacturacionComisionMensual salida = new SalidaEnviarFacturacionComisionMensual();
		try {
			salida.P_OUT_ERRORESCONTADOR = 0L;
			
			if(entrada.p_CobroComision.isEmpty()) {
				String codigo = "SIN_ESPECIFICAR_COMPROBANTES";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, codigo, Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg()));
				salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_MENSAJES.size();
				return salida;	
			}
			
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			List<ComprobanteCobros_Salida> lstComprobantes = new ArrayList<>();
			//List<ComprobanteCobrosDetalle_Salida> lstComprobantesDetalle = obtenerComprobantesCobros(con, entrada);
			
			CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante_Entrada();
			entradaCrearMovimientoComprobante.p_IdEmpresa			= entrada.p_IdEmpresa;
			entradaCrearMovimientoComprobante.p_IdModulo 			= entrada.p_IdModulo;
			entradaCrearMovimientoComprobante.p_IdUsuario 			= entrada.p_IdUsuario;
			entradaCrearMovimientoComprobante.p_EsCargaMasiva 		= entrada.p_EsCargaMasiva;
			entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = entrada.p_EsIngresoAutomatico;
			entradaCrearMovimientoComprobante.p_simularOperacion   	= entrada.p_simularOperacion;
			entradaCrearMovimientoComprobante.p_UsaReglasNegocio	= entrada.p_UsaReglasNegocio;
			
			for (EntradaCobroComision ec : entrada.p_CobroComision) {
		
				MovimientoComprobante_Entrada mov = new MovimientoComprobante_Entrada();
				mov.NumeroMvtoCmbte 					= (long) entrada.p_CobroComision.indexOf(ec);
				mov.IdEmpresa 							= entrada.p_IdEmpresa;
				mov.IdConcepto 							= ec.IdConcepto;
				mov.SecuenciaAgrupacion 				= "";
				mov.IdCliente 							= ec.IdCliente;
				mov.IdPortafolio 						= ec.IdPortafolio;
				mov.ModPrefFact 						= "MODPREFNORM";
				mov.IdEmpleadoDigitador 				= 0L;
				mov.IdInstrumento						= 0L;
				mov.IdInstrumentoRelacionado 			= 0L;
				mov.IdInstrumentoSubyacente 			= 0L;
				mov.IdTipoProducto 						= ""; 
				mov.IdMoneda 							= ec.IdMoneda;
				mov.IdMonedaSubyacente 					= "";
				mov.IdMonedaLiquidacion 				= ec.IdMoneda;
				mov.IdTipoComprobante 					= "FOE";		// FACTURA ELECTRONICA OTROS CONCEPTOS
				mov.IdTipoOperacion 					= "COMOTC";		// COMPRAS OTROS CONCEPTOS
				mov.IdBoveda 							= "";
				mov.IdCorredoraContraparte 				= 0L;
				mov.IdCondicionLiquidacion 				= "T0";
				mov.FechaLiquidacion 					= fechaSistema;
				mov.IdBolsa 							= "";
				mov.IdTipoReajuste 						= "CLP";
				mov.IdTipoConvenio 						= "SCV";
				mov.IdFormaCustodia 					= "";
				mov.IdOrden 							= 0L;
				mov.IdAsignacion 						= 0L;
				mov.IdAsignacionRef 					= 0L;
				mov.IdTipoCanalIngreso 					= "AGE";
				mov.FechaOrden 							= fechaSistema;
				mov.IdTransaccion 						= 0L;
				mov.FolioTransaccion 					= "";
				mov.FechaTransaccion 					= fechaSistema;
				mov.FechaVencimiento 					= "0001-01-01";
				mov.MontoAPlazo 						= BigDecimal.ZERO;
				mov.FechaParidad 						= "0001-01-01";
				mov.ValorParidad 						= BigDecimal.ZERO;
				mov.FechaTipoCambio 					= "0001-01-01";
				mov.ValorTipoCambio 					= BigDecimal.ZERO;
				mov.ValorCaptacionMonedaOrigen 			= BigDecimal.ZERO;
				mov.Tasa 								= BigDecimal.ZERO;
				mov.Cantidad 							= BigDecimal.ONE;
				mov.Precio 								= BigDecimal.ZERO;
				mov.Monto 								= ec.Monto;
				mov.MontoMovimientoComprobante 			= ec.Monto;
				mov.MontoLiquidacion 					= ec.Monto;
				mov.MontoDescuento 						= BigDecimal.ZERO;
				mov.MontoInteres 						= BigDecimal.ZERO;
				mov.Glosa 								= "COMISION MENSUAL "+ entrada.p_MesProcesar + " " + obtenerGlosa(ec.ModoFacturacion, ec.NombreConvenioContrato, ec.IdConcepto);
				mov.ValorRescate 						= BigDecimal.ZERO;
				mov.TasaAgente 							= BigDecimal.ZERO;
				mov.Spread 								= BigDecimal.ZERO;
				mov.DiasPlazo 							= BigDecimal.ZERO;
				mov.FechaMovimientoComprobante 			= fechaSistema;
				mov.FolioCartera 						= 0L;
				mov.FechaEmision 						= fechaSistema;
				mov.TasaEmision 						= BigDecimal.ZERO;
				mov.EsComprobIndividual 				= "N";
				mov.EsCuentaPropia 						= "N";
				mov.IndicadorOrdenDirecta 				= "";
				mov.EsVentaCorta 						= "N";
				mov.EsPrecioPrima 						= "N";
				mov.EsEnRueda 							= "N";
				mov.EsRuteo 							= "N";
				mov.EsAPlazo 							= "N";
				mov.EsFungible 							= "N";
				mov.ActualizarCustodia 					= "N";
				mov.ActualizarCuentaInversion 			= "S";
				mov.ActualizarTesoreria 				= "S";
				mov.ActualizarCustodiaRelacionado		= "N";
				mov.CrearComprobanteAutomatico 			= entrada.p_CrearComprobanteAutomatico;
				mov.IdUsuario 							= entrada.p_IdUsuario;
				mov.CodigoIFRS 							= "";
				mov.CodigoBroker 						= "";
				mov.CodigoBancoLiquidador 				= "";
				mov.CodigoBancoCustodio 				= "";
				mov.IdMonedaSpot 						= "";
				mov.IdMovimientoRelacionado 			= 0L;
				mov.IdPersonaBroker 					= 0L;
				mov.IdPersonaBancoCustodio 				= 0L;
				mov.IdPersonaBancoLiquidador 			= 0L;
				mov.MercadoSMV 							= "";
				mov.ModalidadSMV 						= "";
				mov.PrecioAPlazo 						= BigDecimal.ZERO;
				mov.TasaOperacion 						= BigDecimal.ZERO;
				mov.IdTipoLiquidacionComisionFactura	= "MEN";
			
				entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
				
				// calculo de IVA
				MovimientoComprobanteCobro_Entrada cob = new MovimientoComprobanteCobro_Entrada();
				cob.IdCobro = "IVA";
				cob.NumeroMvtoCmbte = (long) entrada.p_CobroComision.indexOf(ec);
				cob.NumeroMvtoCmbteCobro = (long) entrada.p_CobroComision.indexOf(ec);
				cob.IdMonedaCobro = ec.IdMoneda;
				cob.IdMonedaLiquidacionCobro = ec.IdMoneda;
				cob.MontoCobro = (ec.Monto.multiply(entrada.p_PorcentajeIva)).divide(new BigDecimal(100));
				cob.MontoLiquidacionCobro = cob.MontoCobro;
				cob.MontoMovimientoComprobanteCobro = cob.MontoCobro;
				cob.Porcentaje = entrada.p_PorcentajeIva;
				
				entradaCrearMovimientoComprobante.p_MovimientosCobros.add(cob);
				
				// registra los comprobantes de facturas generadas
				ComprobanteCobros_Salida comprobantefacturacion = new ComprobanteCobros_Salida();
				comprobantefacturacion.NumeroMvtoCmbte = mov.NumeroMvtoCmbte;
				comprobantefacturacion.IdComprobanteComisionMensual = 0L;
				comprobantefacturacion.IdMoneda = ec.IdMoneda;
				comprobantefacturacion.Monto = ec.Monto;
				comprobantefacturacion.MontoComision = ec.MontoComision;
				comprobantefacturacion.MontoDerecho = ec.MontoDerecho;
				comprobantefacturacion.MontoIva = cob.MontoCobro;
				comprobantefacturacion.IdComprobanteRel = ec.IdComprobanteRel;
				comprobantefacturacion.IdContratoComision = ec.IdContratoComision;
				lstComprobantes.add(comprobantefacturacion);
			}
			
			CrearMovimientoComprobante crearMovimientoComprobante = new CrearMovimientoComprobante();
			CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = crearMovimientoComprobante.logica(con, entradaCrearMovimientoComprobante);
			List<MovimientoComprobante_Salida> movimientos = salidaCrearMovimientoComprobante.p_out_Movimientos;
			List<Comprobante_Salida> comprobantes = salidaCrearMovimientoComprobante.p_out_Comprobantes;
						
				// asigna el comprobante generado a los comprobantes origenes que tienen el mismo contrato
				//asignarComprobanteFactuacion(lstComprobantesDetalle, comprobantes.get(0).IdComprobante, ec.IdContratoComision);
								
			salida.P_OUT_MENSAJES.addAll(salidaCrearMovimientoComprobante.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			
			if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {	
				// registra los comprobantes de facturas generadas
				lstComprobantes.forEach(lst-> {//entradaCrearMovimientoComprobante.p_Movimientos) {
					MovimientoComprobante_Salida mov = movimientos.parallelStream().filter(c->c.NumeroMvtoCmbte == lst.NumeroMvtoCmbte).findFirst().get();
					Comprobante_Salida cmp = comprobantes.parallelStream().filter(c->c.IdMovimientoComprobante == mov.IdMovimientoComprobante).findFirst().orElse(null);
					lst.IdComprobanteComisionMensual = cmp != null ? cmp.IdComprobante : 0L;				
					lst.IdMovimientoComprobanteComisionMensual = cmp != null ? cmp.IdMovimientoComprobante : mov.IdMovimientoComprobante;
				});
			
				
				salida.P_OUT_MOVIMIENTO.addAll(movimientos);
				salida.P_OUT_COMPROBANTE.addAll(comprobantes);
				salida.P_OUT_COMPROBANTE_COBROS.addAll(lstComprobantes);
				// inserta los comprobantes generados y los comprobantes de origen en sus respectivas tablas
				insertComprobantes(con, lstComprobantes);	
				
				String codigo = "UI_FACTURAS_CREADAS_SATISFACTORIAMENTE";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION, codigo, Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg()));
			}	
			long finServicio = System.currentTimeMillis();
			logger.info("FIN::EnviarFacturacionCobroMensual", finServicio - inicioServicio);
			return salida;
		
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, e.getMessage(), e.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_MENSAJES.size();
		}
		
		return salida;

	}
	/*
	private List<ComprobanteCobrosDetalle_Salida> asignarComprobanteFactuacion(List<ComprobanteCobrosDetalle_Salida> lstComprobantesDetalle, Long idComprobante, Long idContratoComision) {
		// asigna el comprobante generado a los comprobantes originales que tienen el mismo contrato
		// para luego insertar en la tabla BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle
		lstComprobantesDetalle.stream().filter(comp -> comp.IdContratoComision.equals(idContratoComision))
								.forEach(comp -> {
									comp.setIdComprobanteComisionMensual(idComprobante);
								});
		return lstComprobantesDetalle;
	}*/
/*
	private List<ComprobanteCobrosDetalle_Salida> obtenerComprobantesCobros(Connection con, EntradaEnviarFacturacionComisionMensual entrada) throws SQLException {
		List<Long> lstIdComprobanteConvenio = entrada.p_ComprobanteCobro.stream().filter(comp -> comp.ModoFacturacion.equals("MODPREFCONV"))
																				 .map(comp -> comp.IdComprobante).collect(Collectors.toList());
		List<Long> lstIdComprobanteContrato = entrada.p_ComprobanteCobro.stream().filter(comp -> comp.ModoFacturacion.equals("MODPREFCONT"))
																				 .map(comp -> comp.IdComprobante).collect(Collectors.toList());
		SQL sql = new SQL();
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("IdComprobanteConvenio", lstIdComprobanteConvenio);
		param.put("IdComprobanteContrato", lstIdComprobanteContrato);	
		param.put("IdEmpresa", entrada.p_IdEmpresa);

		return sql.query(con, SQL_SELECT_COMPROBANTES_COBROS, param, ComprobanteCobrosDetalle_Salida.class);
	}
*/
	
	private String obtenerGlosa(String modoFacturacion, String nombreConvenio, String NombreConcepto) {
		StringBuilder glosa = new StringBuilder();
		glosa.append(NombreConcepto);
		glosa.append(" / ");
		glosa.append(modoFacturacion);
		glosa.append(": ");
		glosa.append(nombreConvenio);
		return glosa.toString();
	}
	
	private void insertComprobantes(Connection con, List<ComprobanteCobros_Salida> comprobantes) throws SQLException {
		logger.debug("INICIO::EnviarFacturacionCobroMensual.insertComprobantes");
		long inicio = System.currentTimeMillis();
		
		if( comprobantes.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_INSERT_COMPROBANTES_COBROS, comprobantes, ComprobanteCobros_Salida.class);			
		}
		
		//ComprobanteCobrosDetalle_Salida 
		/*List<ComprobanteCobrosDetalle_Salida> comprobantesDetalle = comprobantes.parallelStream().map(comp -> {
			ComprobanteCobrosDetalle_Salida p = new ComprobanteCobrosDetalle_Salida();
			comp.IdComprobanteRel.forEach(id -> {
				p.IdContratoComision = comp.IdContratoComision;
				p.IdComprobante = id.longValue();
				p.IdComprobanteComisionMensual = comp.IdComprobanteComisionMensual;
			});
			return p;
		}).collect(Collectors.toList());*/
		List<ComprobanteCobrosDetalle_Salida> comprobantesDetalle = new ArrayList<ComprobanteCobrosDetalle_Salida>();
		comprobantes.forEach(comp -> {			
			comp.IdComprobanteRel.forEach(id -> {
				ComprobanteCobrosDetalle_Salida p = new ComprobanteCobrosDetalle_Salida();
				p.IdContratoComision = comp.IdContratoComision;
				p.IdComprobante = id.longValue();
				p.IdComprobanteComisionMensual = comp.IdComprobanteComisionMensual;
				p.IdMovimientoComprobanteComisionMensual = comp.IdMovimientoComprobanteComisionMensual;
				comprobantesDetalle.add(p);
			});
		});
		
		if( comprobantesDetalle.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_INSERT_COMPROBANTES_COBROS_DETALLE, comprobantesDetalle, ComprobanteCobrosDetalle_Salida.class);			
		}	
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnviarFacturacionCobroMensual.insertComprobantes", fin - inicio);
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEnviarFacturacionComisionMensual in = gson.fromJson(jin.toString(), EntradaEnviarFacturacionComisionMensual.class);
		SalidaEnviarFacturacionComisionMensual out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdModulo\":\"FAC\",\"p_EsCargaMasiva\":\"N\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"S\",\"p_CrearComprobanteAutomatico\":\"N\",\"p_PorcentajeIva\":19,\"p_CobroComision\":[{\"IdCliente\":114250,\"NombreCliente\":\"ANTONIA LUKSIC PUGA\",\"DocumentoIdentidad\":\"10929727-5\",\"IdPortafolio\":130282,\"IdMoneda\":\"CLP\",\"ModPrefFact\":\"MODPREFCONT\",\"ModoFacturacion\":\"CONTRATO\",\"IdContratoComision\":639,\"IdClasificacionTipoInstrumento\":\"RV\",\"NombreConvenioContrato\":\"RV\",\"Monto\":90000,\"MontoDerecho\":0,\"MontoComision\":90000,\"IdConcepto\":\"CMACNA\",\"IdInstrumento\":356,\"IdComprobanteRel\":[1170847]}]}";
			EntradaEnviarFacturacionComisionMensual Objin = gson.fromJson(json, EntradaEnviarFacturacionComisionMensual.class);
			
			EnviarFacturacionCobroMensual demo = new EnviarFacturacionCobroMensual();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaEnviarFacturacionComisionMensual ObjOut=demo.logica(con, Objin); 				
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
			    er.printStackTrace();	
				con.rollback();
			    //con.commit();
			}
 			con.commit();
 			//con.rollback();
	    	}		
		 	
		System.out.println("== END ==");		
	}
}
