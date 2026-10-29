package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Salida;
import cl.bcs.facturacion.AnularMovimientoComprobante.Movimiento_Entrada;
import cl.bcs.facturacion.CrearComprobante.ResultadoProceso_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearVistaPreliminarFacturacionCobroMensual.EntradaCrearVistaPreliminarFacturacionCobroMensual;
import cl.bcs.facturacion.CrearVistaPreliminarFacturacionCobroMensual.SalidaCrearVistaPreliminarFacturacionCobroMensual;
import cl.bcs.facturacion.CrearVistaPreliminarFacturacionCobroMensual.SalidaFacturacionCobro;
import cl.bcs.facturacion.EnviarFacturacionCobroMensual.ComprobanteCobros_Salida;
import cl.bcs.facturacion.EnviarFacturacionCobroMensual.EntradaCobroComision;
import cl.bcs.facturacion.EnviarFacturacionCobroMensual.EntradaEnviarFacturacionComisionMensual;
import cl.bcs.facturacion.EnviarFacturacionCobroMensual.SalidaEnviarFacturacionComisionMensual;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearFacturacionCobroMensual implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearFacturacionCobroMensual.class);
	
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/CrearFacturacionCobroMensual_SelectMovimientoComprobante.sql");
	private final String SQL_DELETE_MOVIMIENTO_COMPROBANTE_COMISION_MENSUAL = loadResourceToString("cl/bcs/facturacion/sql/CrearFacturacionCobroMensual_DeleteMovimientosCobroComisionMensual.sql");
	private final String SQL_DELETE_MOVIMIENTO_COMPROBANTE_COMISION_MENSUAL_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/CrearFacturacionCobroMensual_DeleteMovimientosCobroComisionMensualDetalle.sql");

	public static class EntradaCrearFacturacionCobroMensual extends BcsPojo {
		  public Long		p_IdEmpresa;		
		  public Long		p_IdUsuario;
		  public String		p_IdModulo;   
		  public String		p_simularOperacion;
		  public String		p_UsaReglasNegocio;
		  public BigDecimal	p_PorcentajeIva;
		  public String		p_MesProcesar;
		  public List<EntradaComprobante>	p_ComprobanteCobro;
			
		  public EntradaCrearFacturacionCobroMensual (){
			  p_ComprobanteCobro	= new ArrayList<EntradaComprobante>();
		  }
	};
		
	public static class EntradaComprobante extends BcsPojo {
		public Long   		IdComprobante;
		public String 		ModoFacturacion;
	};
	
	public static class SalidaCrearFacturacionCobroMensual extends BcsPojo {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<ComprobanteCobros_Salida>		P_OUT_COMPROBANTE_COBROS;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		public List<MovimientoComprobante_Salida>	P_OUT_MOVIMIENTO;
		
		public SalidaCrearFacturacionCobroMensual () {
			 P_OUT_MENSAJES	= new ArrayList<>();
			 P_OUT_COMPROBANTE = new ArrayList<>();
			 P_OUT_COMPROBANTE_COBROS = new ArrayList<>();
			 P_OUT_RESULTADOPROCESO = new ArrayList<>();
			 P_OUT_MOVIMIENTO = new ArrayList<>();
		  }
	};
		
	public SalidaCrearFacturacionCobroMensual logica(Connection con, EntradaCrearFacturacionCobroMensual entrada) {
		logger.info("INICIO::CrearFacturacionCobroMensual");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearFacturacionCobroMensual salida = new SalidaCrearFacturacionCobroMensual();
		try {
			if(entrada.p_ComprobanteCobro.isEmpty()) {
				String codigo = "SIN_ESPECIFICAR_COMPROBANTES";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, codigo, Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg()));
				salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_MENSAJES.size();
				return salida;
			}
			
				SQL sql = new SQL();
				HashMap<String,Object> params = new HashMap<>();				
				List<Long> idsComprobantes = entrada.p_ComprobanteCobro.stream().map(c -> c.IdComprobante).collect(Collectors.toList());
				params.put("idsComprobante",idsComprobantes);
				List<MovimientoComprobante> movsAnular = sql.query(con, SQL_SELECT_MOVIMIENTO_COMPROBANTE, params , MovimientoComprobante.class);
				AnularMovimientoComprobante_Entrada entradaAnularMovComprobante = new AnularMovimientoComprobante_Entrada();
				Set<Long> idsUnicos = new HashSet<>();
				List<Movimiento_Entrada> movimientosFiltrados = movsAnular.stream()
						.filter(mov -> "ING".equals(mov.IdEstadoFacturacion))
						.filter(mov -> idsUnicos.add(mov.IdMovimientoComprobante))
						.map(mov -> {
					        Movimiento_Entrada arg0 = new Movimiento_Entrada();
					        arg0.IdMovimientoComprobante = mov.IdMovimientoComprobante;
					        return arg0;
					    })
					    .collect(Collectors.toList());
				List<ComprobanteCobros_Salida> paramsMov = movsAnular.stream().filter(mov -> !"ING".equals(mov.IdEstadoFacturacion) && !"FAC".equals(mov.IdEstadoFacturacion))
					    .map(mov -> {
					        ComprobanteCobros_Salida obj = new ComprobanteCobros_Salida();
					        obj.IdMovimientoComprobanteComisionMensual = mov.IdMovimientoComprobante;
					        return obj;
					    })
					    .collect(Collectors.toList());
				
				Set<Long> idsFAC = movsAnular.stream().filter(mov -> "FAC".equals(mov.IdEstadoFacturacion))
					    .map(mov -> mov.IdComprobante)
					    .collect(Collectors.toSet());
				entrada.p_ComprobanteCobro.removeIf(c -> idsFAC.contains(c.IdComprobante));
				
				if (movsAnular.size() > 0) {
					
					if (!movimientosFiltrados.isEmpty()) {
				        entradaAnularMovComprobante.p_MovimientoAAnular.addAll(movimientosFiltrados);
					    
					    entradaAnularMovComprobante.p_EsIngresoAutomatico = "S";
						entradaAnularMovComprobante.p_Glosa = "";
						entradaAnularMovComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaAnularMovComprobante.p_IdModulo = entrada.p_IdModulo;
						entradaAnularMovComprobante.p_IdUsuario = entrada.p_IdUsuario;
						entradaAnularMovComprobante.p_simularOperacion = entrada.p_simularOperacion;
						entradaAnularMovComprobante.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
						
						AnularMovimientoComprobante anularMovComprobanteService = new AnularMovimientoComprobante();
						AnularMovimientoComprobante_Salida salidaAnularMovComprobante = anularMovComprobanteService.logica(con, entradaAnularMovComprobante );
						logger.debug("Salida AnularMovimientoComprobante: " + salidaAnularMovComprobante.toString());	
						
						salida.P_OUT_MENSAJES.addAll(salidaAnularMovComprobante.P_OUT_MENSAJES);				
						
						if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {
							salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
							salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
							logger.debug("Fin");
							logger.debug(salida);
							return salida;
						}
						 paramsMov.addAll(
						            salidaAnularMovComprobante.P_OUT_RESULTADOPROCESO.stream()
						                .map(comp -> {
						                    ComprobanteCobros_Salida obj = new ComprobanteCobros_Salida();
						                    obj.IdMovimientoComprobanteComisionMensual = comp.Id;
						                    return obj;
						                })
						                .collect(Collectors.toList())
						        );						
					}	
					
					if (!paramsMov.isEmpty()) {
						sql.batch(con, SQL_DELETE_MOVIMIENTO_COMPROBANTE_COMISION_MENSUAL, paramsMov, EnviarFacturacionCobroMensual.ComprobanteCobros_Salida.class);
						sql.batch(con, SQL_DELETE_MOVIMIENTO_COMPROBANTE_COMISION_MENSUAL_DETALLE, paramsMov, EnviarFacturacionCobroMensual.ComprobanteCobros_Salida.class);
					}
				}
				
				EntradaCrearVistaPreliminarFacturacionCobroMensual entradaCalculoFacturacionMensual = new EntradaCrearVistaPreliminarFacturacionCobroMensual();
				
				entradaCalculoFacturacionMensual.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCalculoFacturacionMensual.p_IdModulo = entrada.p_IdModulo;
				entradaCalculoFacturacionMensual.p_simularOperacion = entrada.p_simularOperacion;
				entradaCalculoFacturacionMensual.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				entradaCalculoFacturacionMensual.p_IdUsuario = entrada.p_IdUsuario;
				entradaCalculoFacturacionMensual.p_ComprobanteCobro = entrada.p_ComprobanteCobro.stream().map(c ->{
					CrearVistaPreliminarFacturacionCobroMensual.EntradaComprobante com = new CrearVistaPreliminarFacturacionCobroMensual.EntradaComprobante();		
					com.IdComprobante = c.IdComprobante;
			        com.ModoFacturacion = c.ModoFacturacion;
			        return com;
				}).collect(Collectors.toList());
				
				CrearVistaPreliminarFacturacionCobroMensual calculoFacturacionMensual = new CrearVistaPreliminarFacturacionCobroMensual();
				SalidaCrearVistaPreliminarFacturacionCobroMensual salidaCalculoFacturacionMensual = calculoFacturacionMensual.logica(con, entradaCalculoFacturacionMensual);
				
				salida.P_OUT_MENSAJES.addAll(salidaCalculoFacturacionMensual.P_OUT_MENSAJES);
			
				if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(salida);
					return salida;
				}
				
				EntradaEnviarFacturacionComisionMensual entradaEnviarFactComMen = new EntradaEnviarFacturacionComisionMensual();
				for(SalidaFacturacionCobro fc: salidaCalculoFacturacionMensual.P_OUT_FACTURACION_COBRO) {
					EntradaCobroComision cobro = new EntradaCobroComision();
					
					cobro.IdCliente = fc.IdCliente;
				    cobro.NombreCliente = fc.NombreCliente;
				    cobro.DocumentoIdentidad = fc.DocumentoIdentidad;
				    cobro.IdPortafolio = fc.IdPortafolio;
				    cobro.IdMoneda = fc.IdMoneda;
				    cobro.ModoFacturacion = fc.ModoFacturacion;
				    cobro.IdContratoComision = fc.IdContratoComision;
				    cobro.IdClasificaciontipoInstrumento = fc.IdClasificacionTipoInstrumento;
				    cobro.NombreConvenioContrato = fc.NombreConvenioContrato;
				    cobro.Monto = fc.Monto;
				    cobro.MontoDerecho = fc.MontoDerecho;
				    cobro.MontoComision = fc.MontoComision;
				    cobro.IdConcepto = fc.IdConceptoFacturacion;
				    cobro.NombreConcepto = fc.NombreConceptoFacturacion;
				    cobro.IdComprobanteRel = fc.IdComprobanteRel;
				    cobro.IdInstrumento = fc.IdInstrumento;

				    entradaEnviarFactComMen.p_CobroComision.add(cobro);
				}	
				
				entradaEnviarFactComMen.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaEnviarFactComMen.p_IdModulo = entrada.p_IdModulo;
				entradaEnviarFactComMen.p_simularOperacion = entrada.p_simularOperacion;
				entradaEnviarFactComMen.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				entradaEnviarFactComMen.p_IdUsuario = entrada.p_IdUsuario;
				entradaEnviarFactComMen.p_simularOperacion = entrada.p_simularOperacion;
				entradaEnviarFactComMen.p_EsCargaMasiva 	= "N";
				entradaEnviarFactComMen.p_EsIngresoAutomatico = "S";
				entradaEnviarFactComMen.p_CrearComprobanteAutomatico = "N";		
				entradaEnviarFactComMen.p_PorcentajeIva = entrada.p_PorcentajeIva;
				entradaEnviarFactComMen.p_MesProcesar = entrada.p_MesProcesar;
			
				EnviarFacturacionCobroMensual enviarFactComMen = new EnviarFacturacionCobroMensual();
				SalidaEnviarFacturacionComisionMensual salidaEnvFactComMen = enviarFactComMen.logica(con, entradaEnviarFactComMen);
				
				salida.P_OUT_MENSAJES.addAll(salidaEnvFactComMen.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = salidaEnvFactComMen.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_COMPROBANTE.addAll(salidaEnvFactComMen.P_OUT_COMPROBANTE);
				salida.P_OUT_COMPROBANTE_COBROS.addAll(salidaEnvFactComMen.P_OUT_COMPROBANTE_COBROS);
				salida.P_OUT_RESULTADOPROCESO.addAll(salidaEnvFactComMen.P_OUT_RESULTADOPROCESO);
				salida.P_OUT_MOVIMIENTO.addAll(salidaEnvFactComMen.P_OUT_MOVIMIENTO);
				
				logger.debug("Fin");
				logger.debug(salida);
				return salida;	
			
			
		} catch (Exception e) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, e.getMessage(), e.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_MENSAJES.size();
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearFacturacionCobroMensual", finServicio - inicioServicio);
		return salida;
	}
			
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearFacturacionCobroMensual in = gson.fromJson(jin.toString(), EntradaCrearFacturacionCobroMensual.class);
		SalidaCrearFacturacionCobroMensual out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_PorcentajeIva\":19,\"p_MesProcesar\":\"MAYO 2025\",\"p_ComprobanteCobro\":[{\"IdComprobante\":2863777,\"ModoFacturacion\":\"MODPREFCONT\"}]}";

			EntradaCrearFacturacionCobroMensual Objin = gson.fromJson(json, EntradaCrearFacturacionCobroMensual.class);
			
			CrearFacturacionCobroMensual demo = new CrearFacturacionCobroMensual();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCrearFacturacionCobroMensual ObjOut=demo.logica(con, Objin); 				
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
