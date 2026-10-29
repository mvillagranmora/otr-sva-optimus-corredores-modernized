package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearVistaPreliminarFacturacionCobroMensual implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearVistaPreliminarFacturacionCobroMensual.class);
	
	private final String SQL_SELECT_COBROS_COMPROBANTES = loadResourceToString("cl/bcs/facturacion/sql/CrearVistaPreliminarFacturacionCobroMensual_SelectCobrosComprobantes.sql");
	private final String SQL_SELECT_RESUMEN_CONTRATO = loadResourceToString("cl/bcs/facturacion/sql/CrearVistaPreliminarFacturacionCobroMensual_SelectResumenContrato.sql");
	
	public static class EntradaCrearVistaPreliminarFacturacionCobroMensual extends BcsPojo {
		  public Long		p_IdEmpresa;		
		  public Long		p_IdUsuario;
		  public String		p_IdModulo;   
		  public String		p_simularOperacion;
		  public String		p_UsaReglasNegocio;
		  public List<EntradaComprobante>	p_ComprobanteCobro;
			
		  public EntradaCrearVistaPreliminarFacturacionCobroMensual (){
			  p_ComprobanteCobro	= new ArrayList<EntradaComprobante>();
		  }
		};
	
	public static class SalidaCrearVistaPreliminarFacturacionCobroMensual extends BcsPojo {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<SalidaFacturacionCobro>			P_OUT_FACTURACION_COBRO;
	
		public SalidaCrearVistaPreliminarFacturacionCobroMensual () {
			 P_OUT_MENSAJES	= new ArrayList<>();
			 P_OUT_FACTURACION_COBRO = new ArrayList<>();
		  }
	};
		
	public static class EntradaComprobante extends BcsPojo {
		public Long   		IdComprobante;
		public String 		ModoFacturacion;
	};	
		
	public static class SalidaFacturacionCobro extends BcsPojo {
		public Long   		IdCliente;
		public String 		NombreCliente;
		public String 		DocumentoIdentidad;
		public Long 		IdPortafolio;
		public String 		CodigoPortafolio;
		public String 		ModoFacturacion;
		public String 		ModPrefFact;
		public String 		IdMoneda;
		public String 		IdClasificacionTipoInstrumento;
		public Long   		IdContratoComision;
		public String 		NombreConvenioContrato;	
		public String 		IdConceptoComprobante;		// Concepto de comprobante de origen
		public String 		NombreConceptoComprobante;		
		public String 		IdConceptoFacturacion;		// Concepto de factuarcion COMMEN
		public String 		NombreConceptoFacturacion;
		public BigDecimal 	Monto;
		public BigDecimal 	MontoDerecho;
		public BigDecimal 	MontoComision;
		public BigDecimal 	TotalMontoLiquidacion;
		public Long			IdInstrumento = 0L;
		public List<Long> 	IdComprobanteRel;
		
		
		public SalidaFacturacionCobro() {
			super();
			Monto = BigDecimal.ZERO;
			MontoDerecho = BigDecimal.ZERO;
			MontoComision = BigDecimal.ZERO;
			TotalMontoLiquidacion = BigDecimal.ZERO;
			IdComprobanteRel = new ArrayList<Long>();
		}
	};
	
	public static class FacturacionCobro extends SalidaFacturacionCobro {
		public Long		IdComprobante;
		public String	IdFormatoCobro;
	}
	
	public static class ResumenContrato extends BcsPojo {
		public Long   		IdContratoComision;
		public String 		IdTipoContrato;
		public String 		EsPorRangos;
		public String 		IdConcepto;
		public String 		IdMoneda;
		public String 		IdClasificacionTipoInstrumento;
		public String 		CodigoCobroClasificacion;
		public String 		IdCobro;
		public BigDecimal   MontoMinimo;
		public BigDecimal   MontoMaximo;
		public BigDecimal   MontoRango;
		public BigDecimal   PorcentajeRango;
		public BigDecimal   MontoMaximoRango;
	}
	
	public SalidaCrearVistaPreliminarFacturacionCobroMensual logica(Connection con, EntradaCrearVistaPreliminarFacturacionCobroMensual entrada) {
		logger.info("INICIO::CrearVistaPreliminarFacturacionCobroMensual");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearVistaPreliminarFacturacionCobroMensual salida = new SalidaCrearVistaPreliminarFacturacionCobroMensual();
		try {
			if(entrada.p_ComprobanteCobro.isEmpty()) {
				String codigo = "SIN_ESPECIFICAR_COMPROBANTES";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, codigo, Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg()));
				salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_MENSAJES.size();
				return salida;
			}
			salida.P_OUT_ERRORESCONTADOR = 0L;
			List<SalidaFacturacionCobro> lstComprobantesCobros = obtenerComprobantesCobros(con, entrada);
			List<SalidaFacturacionCobro> lstFacturacionCobros = obtenerFacturacionCobrosContrato(con, lstComprobantesCobros);
			salida.P_OUT_FACTURACION_COBRO = lstFacturacionCobros;
			
			return salida;
			
		} catch (Exception e) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, e.getMessage(), e.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_MENSAJES.size();
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearVistaPreliminarFacturacionCobroMensual", finServicio - inicioServicio);
		return salida;
	}
	
	private List<SalidaFacturacionCobro> agruparComprobantesCobros(Connection con, List<FacturacionCobro> entrada) throws Exception {
		List<SalidaFacturacionCobro> salida = new ArrayList<>();
		//Map<Long, List<ContratoCambioPlanInWrapper>> groupByIdCliente = entrada.p_ContratoCambioPlanIn.stream().collect(Collectors.groupingBy(x -> x.IdCliente));
		//BigDecimal montoDeposito = traspasos.entrySet().stream().map(traspaso->traspaso.getValue().MontoPesoDetalle).reduce(BigDecimal.ZERO, (subtotal, montoDetalle)-> subtotal.add(montoDetalle, MathContext.DECIMAL128));
		Map<String, List<FacturacionCobro>> groupByModoFacturacionFormatoCobro = entrada.stream().collect(Collectors.groupingBy(x -> x.ModoFacturacion+"|"+x.IdFormatoCobro));
		//convenio
		List<FacturacionCobro> listConv = groupByModoFacturacionFormatoCobro.get("CONVENIO|CONV");
		List<SalidaFacturacionCobro> salConv = new ArrayList<>();
		if (listConv != null) {
			for(FacturacionCobro p:listConv) {
				Map<String, List<SalidaFacturacionCobro>> salConvMap = salConv.stream()
						.collect(Collectors.groupingBy(x -> x.IdCliente+"|"+x.IdPortafolio+"|"+x.IdMoneda+"|"+x.ModPrefFact+"|"+x.ModoFacturacion+"|"+
								x.IdClasificacionTipoInstrumento+"|"+x.IdContratoComision+"|"+x.IdConceptoFacturacion));
				
				List<SalidaFacturacionCobro> salConvList = salConvMap.get(p.IdCliente+"|"+p.IdPortafolio+"|"+p.IdMoneda+"|"+p.ModPrefFact+"|"+p.ModoFacturacion+"|"+
								p.IdClasificacionTipoInstrumento+"|"+p.IdContratoComision+"|"+p.IdConceptoFacturacion);
				
				SalidaFacturacionCobro sal = new SalidaFacturacionCobro();
				if (salConvList == null) {	
					sal.IdCliente = p.IdCliente;
					sal.NombreCliente = p.NombreCliente;
					sal.DocumentoIdentidad = p.DocumentoIdentidad;
					sal.IdPortafolio = p.IdPortafolio;
					sal.CodigoPortafolio = p.CodigoPortafolio;
					sal.ModoFacturacion = p.ModoFacturacion;
					sal.ModPrefFact = p.ModPrefFact;
					sal.IdMoneda = p.IdMoneda;
					sal.IdClasificacionTipoInstrumento = p.IdClasificacionTipoInstrumento;
					sal.IdContratoComision = p.IdContratoComision;
					sal.NombreConvenioContrato = p.NombreConvenioContrato;	
					sal.IdConceptoComprobante = p.IdConceptoComprobante;		// Concepto de comprobante de origen
					sal.NombreConceptoComprobante = p.NombreConceptoComprobante;		
					sal.IdConceptoFacturacion = p.IdConceptoFacturacion;		// Concepto de factuarcion COMMEN
					sal.NombreConceptoFacturacion = p.NombreConceptoFacturacion;
					sal.Monto = p.Monto.add(p.MontoDerecho.add( p.MontoComision));
					sal.MontoDerecho = p.MontoDerecho;
					sal.MontoComision = p.MontoComision;
					sal.IdComprobanteRel.add(p.IdComprobante);
					sal.TotalMontoLiquidacion = p.TotalMontoLiquidacion;
					salConv.add(sal);
				}else {
					for (SalidaFacturacionCobro sl: salConvList) {
						sl.Monto = sl.Monto.add(p.Monto);
						sl.MontoDerecho = sl.MontoDerecho.add(p.MontoDerecho);
						sl.MontoComision = sl.MontoComision.add(p.MontoComision);
						sl.IdComprobanteRel.add(p.IdComprobante);
						sl.TotalMontoLiquidacion = sl.TotalMontoLiquidacion.add(p.TotalMontoLiquidacion);
					}
				}
				
			}
			salida.addAll(salConv);
		}
		//contrato
		List<FacturacionCobro> listContPor = groupByModoFacturacionFormatoCobro.get("CONTRATO|DETPOR");
		List<SalidaFacturacionCobro> salContPor = new ArrayList<>();
		if (listContPor != null) {
			for(FacturacionCobro p:listContPor) {
				Map<String, List<SalidaFacturacionCobro>> salContPorMap = salContPor.stream()
						.collect(Collectors.groupingBy(x -> x.IdCliente+"|"+x.IdPortafolio+"|"+x.IdMoneda+"|"+x.ModPrefFact+"|"+x.ModoFacturacion+"|"+
								x.IdClasificacionTipoInstrumento+"|"+x.IdContratoComision+"|"+x.IdConceptoFacturacion));
				
				List<SalidaFacturacionCobro> salContPorList = salContPorMap.get(p.IdCliente+"|"+p.IdPortafolio+"|"+p.IdMoneda+"|"+p.ModPrefFact+"|"+p.ModoFacturacion+"|"+
								p.IdClasificacionTipoInstrumento+"|"+p.IdContratoComision+"|"+p.IdConceptoFacturacion);
				
				SalidaFacturacionCobro sal = new SalidaFacturacionCobro();
				if (salContPorList == null) {	
					sal.IdCliente = p.IdCliente;
					sal.NombreCliente = p.NombreCliente;
					sal.DocumentoIdentidad = p.DocumentoIdentidad;
					sal.IdPortafolio = p.IdPortafolio;
					sal.CodigoPortafolio = p.CodigoPortafolio;
					sal.ModoFacturacion = p.ModoFacturacion;
					sal.ModPrefFact = p.ModPrefFact;
					sal.IdMoneda = p.IdMoneda;
					sal.IdClasificacionTipoInstrumento = p.IdClasificacionTipoInstrumento;
					sal.IdContratoComision = p.IdContratoComision;
					sal.NombreConvenioContrato = p.NombreConvenioContrato;	
					sal.IdConceptoComprobante = p.IdConceptoComprobante;		// Concepto de comprobante de origen
					sal.NombreConceptoComprobante = p.NombreConceptoComprobante;		
					sal.IdConceptoFacturacion = p.IdConceptoFacturacion;		// Concepto de factuarcion COMMEN
					sal.NombreConceptoFacturacion = p.NombreConceptoFacturacion;
					sal.Monto = p.Monto;
					sal.MontoDerecho = p.MontoDerecho;
					sal.MontoComision = p.MontoComision;
					sal.IdComprobanteRel.add(p.IdComprobante);
					sal.TotalMontoLiquidacion = p.TotalMontoLiquidacion;
					sal.IdInstrumento = p.IdInstrumento;
					salContPor.add(sal);
				}else {
					for (SalidaFacturacionCobro sl: salContPorList) {
						sl.Monto = sl.Monto.add(p.Monto);
						sl.MontoDerecho = sl.MontoDerecho.add(p.MontoDerecho);
						sl.MontoComision = sl.MontoComision.add(p.MontoComision);
						sl.IdComprobanteRel.add(p.IdComprobante);
						sl.TotalMontoLiquidacion = sl.TotalMontoLiquidacion.add(p.TotalMontoLiquidacion);
					}
				}
				
			}
			salida.addAll(salContPor);
		}
		
		List<FacturacionCobro> listContRut = groupByModoFacturacionFormatoCobro.get("CONTRATO|AGRRUT");
		List<SalidaFacturacionCobro> salContRut = new ArrayList<>();
		if (listContRut != null) {
			for(FacturacionCobro p:listContRut) {
				Map<String, List<SalidaFacturacionCobro>> salContRutMap = salContRut.stream()
						.collect(Collectors.groupingBy(x -> x.IdCliente+"|"+x.IdMoneda+"|"+x.ModPrefFact+"|"+x.ModoFacturacion+"|"+
								x.IdClasificacionTipoInstrumento+"|"+x.IdContratoComision+"|"+x.IdConceptoFacturacion));
				
				List<SalidaFacturacionCobro> salContRutList = salContRutMap.get(p.IdCliente+"|"+p.IdMoneda+"|"+p.ModPrefFact+"|"+p.ModoFacturacion+"|"+
								p.IdClasificacionTipoInstrumento+"|"+p.IdContratoComision+"|"+p.IdConceptoFacturacion);
				
				SalidaFacturacionCobro sal = new SalidaFacturacionCobro();
				if (salContRutList == null) {	
					sal.IdCliente = p.IdCliente;
					sal.NombreCliente = p.NombreCliente;
					sal.DocumentoIdentidad = p.DocumentoIdentidad;
					sal.IdPortafolio = p.IdPortafolio;
					sal.CodigoPortafolio = p.CodigoPortafolio;
					sal.ModoFacturacion = p.ModoFacturacion;
					sal.ModPrefFact = p.ModPrefFact;
					sal.IdMoneda = p.IdMoneda;
					sal.IdClasificacionTipoInstrumento = p.IdClasificacionTipoInstrumento;
					sal.IdContratoComision = p.IdContratoComision;
					sal.NombreConvenioContrato = p.NombreConvenioContrato;	
					sal.IdConceptoComprobante = p.IdConceptoComprobante;		// Concepto de comprobante de origen
					sal.NombreConceptoComprobante = p.NombreConceptoComprobante;		
					sal.IdConceptoFacturacion = p.IdConceptoFacturacion;		// Concepto de factuarcion COMMEN
					sal.NombreConceptoFacturacion = p.NombreConceptoFacturacion;
					sal.Monto = p.Monto;
					sal.MontoDerecho = p.MontoDerecho;
					sal.MontoComision = p.MontoComision;
					sal.IdComprobanteRel.add(p.IdComprobante);
					sal.TotalMontoLiquidacion = p.TotalMontoLiquidacion;
					sal.IdInstrumento = p.IdInstrumento;
					salContRut.add(sal);
				}else {
					for (SalidaFacturacionCobro sl: salContRutList) {
						sl.Monto = sl.Monto.add(p.Monto);
						sl.MontoDerecho = sl.MontoDerecho.add(p.MontoDerecho);
						sl.MontoComision = sl.MontoComision.add(p.MontoComision);
						sl.IdComprobanteRel.add(p.IdComprobante);
						sl.TotalMontoLiquidacion = sl.TotalMontoLiquidacion.add(p.TotalMontoLiquidacion);
					}
				}
				
			}
			salida.addAll(salContRut);
		}
		
		return salida;
	}
	private List<SalidaFacturacionCobro> obtenerComprobantesCobros(Connection con, EntradaCrearVistaPreliminarFacturacionCobroMensual entrada) throws Exception {
		List<Long> lstIdComprobanteContrato = entrada.p_ComprobanteCobro.stream().map(comp->comp.IdComprobante).collect(Collectors.toList());

		SQL sql = new SQL();
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("IdComprobanteContrato", lstIdComprobanteContrato);	
		param.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<FacturacionCobro> cmp = sql.query(con, SQL_SELECT_COBROS_COMPROBANTES, param, FacturacionCobro.class);
		return 	agruparComprobantesCobros(con, cmp);	
	}
	
	private List<SalidaFacturacionCobro> obtenerFacturacionCobrosContrato(Connection con, List<SalidaFacturacionCobro> lstComprobantesCobros) throws Exception {
		// obtiene los contratos relacionados a los comprobantes con preferencia de facturacion en contrato
		List<Long> lstIdContratoComision = lstComprobantesCobros.parallelStream().filter(comp->comp.ModPrefFact.equals("MODPREFCONT") || comp.ModPrefFact.equals("MODPREFCONV"))
				.map(c->c.IdContratoComision).distinct().collect(Collectors.toList());
		
		SQL sql = new SQL();
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("IdContratoComision", lstIdContratoComision);
		List<ResumenContrato> lstResumenContrato = sql.query(con, SQL_SELECT_RESUMEN_CONTRATO, param, ResumenContrato.class);
		
		for (SalidaFacturacionCobro fc : lstComprobantesCobros) {
			if ("MODPREFCONT".equalsIgnoreCase(fc.ModPrefFact)) {
				fc.MontoDerecho = obtenerMontoContratoPorComision(lstResumenContrato, "DERECHO", fc.IdContratoComision, fc.IdConceptoComprobante, fc.TotalMontoLiquidacion);
				fc.MontoComision = obtenerMontoContratoPorComision(lstResumenContrato, "COMISION", fc.IdContratoComision, fc.IdConceptoComprobante, fc.TotalMontoLiquidacion);
				fc.Monto = fc.MontoDerecho.add(fc.MontoComision);
			}
			
		}

		return lstComprobantesCobros;
	}
	
	private BigDecimal obtenerMontoContratoPorComision(List<ResumenContrato> lstResumenContrato, String codigoCobroClasificacion, Long idContratoComision, String idConcepto, BigDecimal totalMontoLiquidacion) {
		BigDecimal monto = BigDecimal.ZERO;
		BigDecimal MontoMaximoRango = BigDecimal.ZERO;
		List<ResumenContrato> resumenContrato = lstResumenContrato.stream().filter(cont->cont.IdContratoComision.equals(idContratoComision)
																						&& cont.CodigoCobroClasificacion.equals(codigoCobroClasificacion))
																					.sorted(Comparator.comparing(rc -> rc.MontoMaximoRango))
																				 	.collect(Collectors.toList());
		if (!resumenContrato.isEmpty()) {
			ResumenContrato ultimoResumenPorRango = null;
			BigDecimal totalMontoLiquidacionOriginal = totalMontoLiquidacion;
			BigDecimal totalMontoLiquidacionPorTramo = totalMontoLiquidacion;
			
			for (ResumenContrato resumen : resumenContrato) {				
				if (resumen.EsPorRangos.equalsIgnoreCase("S")) {
					ultimoResumenPorRango = resumen;				    
				    BigDecimal cotaInferior = MontoMaximoRango;
				    BigDecimal cotaSuperior = resumen.MontoMaximoRango;
				    BigDecimal montoCalculable = totalMontoLiquidacionOriginal.min(cotaSuperior).subtract(cotaInferior);
				    boolean esUltimoTramo = resumen == resumenContrato.get(resumenContrato.size() - 1);
				    
				    if (montoCalculable.compareTo(BigDecimal.ZERO) < 0) {
				    	montoCalculable = BigDecimal.ZERO;
				    }
				    
				    if (totalMontoLiquidacionOriginal.compareTo(cotaInferior) > 0) {
				    	if((resumen.IdCobro.equalsIgnoreCase("DEFI") || resumen.IdCobro.equalsIgnoreCase("COFI"))) {
					    	if (montoCalculable.compareTo(BigDecimal.ZERO) > 0) {
							    monto = monto.add(resumen.MontoRango);
					    	}
					    }
					    else if(montoCalculable.compareTo(BigDecimal.ZERO) > 0) {
					    	BigDecimal porcentaje = resumen.PorcentajeRango.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
						    monto = monto.add(montoCalculable.multiply(porcentaje));
					    }
				    }
				    
				    MontoMaximoRango = resumen.MontoMaximoRango;					
					
					totalMontoLiquidacionPorTramo = totalMontoLiquidacionPorTramo.subtract(montoCalculable);
					if (esUltimoTramo && totalMontoLiquidacionPorTramo.compareTo(cotaSuperior) > 0) {
						BigDecimal excedente = totalMontoLiquidacionOriginal.subtract(cotaSuperior);
						BigDecimal porcentajeFinal = resumen.PorcentajeRango.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
						monto = monto.add(excedente.multiply(porcentajeFinal));
					}
					
					if (totalMontoLiquidacionOriginal.compareTo(cotaSuperior) <= 0) {
						break;
					}
					
				}
				else {
					System.out.println(MontoMaximoRango +"|" + resumen.MontoMaximoRango);
					if (totalMontoLiquidacion.compareTo(MontoMaximoRango) > 0 && (totalMontoLiquidacion.compareTo(resumen.MontoMaximoRango) <= 0 || totalMontoLiquidacion.compareTo(resumen.MontoMaximoRango) > 0)) {
						monto = resumen.IdCobro.toUpperCase().contains("VA")? totalMontoLiquidacion.multiply(resumen.PorcentajeRango.divide(new BigDecimal(100))): resumen.MontoRango;
						System.out.println("SI");
					}
					

					if (resumen.MontoMaximo.compareTo(BigDecimal.ZERO) > 0 && monto.compareTo(resumen.MontoMaximo) == 1) {
						monto = resumen.MontoMaximo;
					}
					else if (resumen.MontoMinimo.compareTo(BigDecimal.ZERO) > 0 && monto.compareTo(resumen.MontoMinimo) <= 0) {
						monto = resumen.MontoMinimo;
					}
				}
				
				MontoMaximoRango = resumen.MontoMaximoRango;			
				
				if (totalMontoLiquidacion.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
			}
			
			if (ultimoResumenPorRango != null) {
		        if (ultimoResumenPorRango.MontoMaximo.compareTo(BigDecimal.ZERO) > 0
		                && monto.compareTo(ultimoResumenPorRango.MontoMaximo) > 0) {
		            monto = ultimoResumenPorRango.MontoMaximo;
		        } else if (ultimoResumenPorRango.MontoMinimo.compareTo(BigDecimal.ZERO) > 0
		                && monto.compareTo(ultimoResumenPorRango.MontoMinimo) <= 0) {
		            monto = ultimoResumenPorRango.MontoMinimo;
		        }
		    }
		}
		return monto;
	}
	
			
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearVistaPreliminarFacturacionCobroMensual in = gson.fromJson(jin.toString(), EntradaCrearVistaPreliminarFacturacionCobroMensual.class);
		SalidaCrearVistaPreliminarFacturacionCobroMensual out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ComprobanteCobro\":[{\"IdComprobante\":1170847,\"ModoFacturacion\":\"MODPREFCONT\"}]}";

			EntradaCrearVistaPreliminarFacturacionCobroMensual Objin = gson.fromJson(json, EntradaCrearVistaPreliminarFacturacionCobroMensual.class);
			
			CrearVistaPreliminarFacturacionCobroMensual demo = new CrearVistaPreliminarFacturacionCobroMensual();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCrearVistaPreliminarFacturacionCobroMensual ObjOut=demo.logica(con, Objin); 				
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
			    er.printStackTrace();	
				con.rollback();
			    //con.commit();
			}
 			//con.commit();
 			con.rollback();
	    	}		
		 	
		System.out.println("== END ==");		
	}	

	
}
