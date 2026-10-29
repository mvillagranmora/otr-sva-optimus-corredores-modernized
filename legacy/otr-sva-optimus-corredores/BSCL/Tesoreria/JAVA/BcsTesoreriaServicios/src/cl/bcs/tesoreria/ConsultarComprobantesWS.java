package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarComprobantesWS implements JavaCall {

	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarComprobantesWS_SelectComprobantes.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarComprobantesWS_SelectFormaDePago.sql");
	private final String SQL_SELECT_DETALLE_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarComprobantesWS_SelectDetalle.sql");
    static final Logger logger = LogManager.getLogger(ConsultarComprobantes.class);

    public static class  EntradaConsultarComprobantes extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String TipoMovimiento;
		public Date FechaInLiquidacion;
		public Date FechaTerLiquidacion;
		public String TipoSocioNegocio;
		public Long IdCaja;
		public String DocumentoIdentidad; // SocioNegocio
		public String CodigoPortafolio; //Portafolio
		public String IdMoneda;
		public String IdConcepto;
		public String IdEstado;
		public String IdMedioDePago; // FormadePago
		public String BancoCorredora;
		public String Alias;
		public String BancoCliente;
		public String CuentaCorredora;
		
		
	
		public EntradaConsultarComprobantes() {
		    this.p_IdEmpresa = 0L;
		    this.p_IdUsuario = 0L;
		    this.p_IdModulo = "TES";
	    }
    }
    
    public static class SalidaConsultarComprobantes extends BcsPojo {

		public Long 								P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 					P_OUT_MENSAJES;
		public List<ComprobanteSalida>			P_OUT_COMPROBANTES;				
		
		public SalidaConsultarComprobantes() {
			//this.P_OUT_COMPROBANTES = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
	}
    
    public static class  IdComprobantes extends BcsPojo {
    	
		public Long IdComprobante;
		
	}
    
    public static class ComprobanteSalida extends BcsPojo {
    	
    	public Long IdComprobante; 
    	public Long IdEmpresa; 
    	public String NombreEmpresa; 
    	public Long IdUsuario; 
    	public String TipoComprobante; 
    	public String IdTipoMovimiento; 
    	public String NombreTipoMovimiento; 
    	public String IdConcepto; 
    	public String NombreConcepto; 
    	public Long IdCaja; 
    	public String NombreCaja; 
    	public String CodigoCaja; 
    	public String IdMoneda; 
    	public String NombreMoneda; 
    	public BigDecimal Monto;
    	public BigDecimal MontoEgreso;
    	public BigDecimal MontoIngreso;
    	public BigDecimal MontoConSigno;
    	public Date FechaLiquidacion;
    	public Timestamp FechaIngresoRegistro;
    	public Date FechaMovimiento;
    	public String IdTipoPersona;
    	public String NombreTipoSocioDeNegocio;
    	public Long IdPersona;
    	public String NombrePersona;
    	public String IdTipoDocumentoIdentidad;
    	public String DocumentoIdentidad;
    	public String TitularPago;
    	public String IdTipoPersonaTitularPago;
    	public Long IdPersonaTitularPago;
    	public String NombrePersonaTitularPago;
    	public String MueveCuentaInversion;
    	public Long IdPortafolioTitularPago;
    	public String NombrePortafolioTitularPago;
    	public String SecuenciaAgrupacion;
    	public String EsBilateral;
    	public String ComprobanteNeteado;
    	public String SeparadoPorConcepto;
    	public Long IdUsuarioBloqueo;
    	public Timestamp FechaHoraBloqueo;
    	public Long IdCarga;
    	public String IdEstado;
    	public String NombreEstado;
    	public String IdTipoEvento;
    	public String Autorizacion;
    	public String SolicitudAutorizacion;
    	public String MotivoAutorizacion;
    	public String IdEstadoAutorizacion;
    	public String NombreEstadoAutorizacion;
    	public String IdMedioDePago;
    	public String NombreMedioDePago;
    	public String IdModalidadDePago;
    	public String NombreModalidadDePago;
    	public Long IdCuentaBancoCorredora;
    	public Long IdChequera;
    	public String NombreBancoCorredora;
    	public String NumeroCuentaBancariaCorredora;
    	public String NombreTipoCuentaBancariaCorredora;
    	public String NumeroSerieSocio; 
    	public Long NumeroDocumentoSocio;
    	public Timestamp FechaDocumentoPagoSocio; 
    	public Long IdBancoSocio;
    	public String NombreBancoSocio;
    	public String NumeroCuentaBancariaSocio;
    	public String NombreTipoCuentaBancariaSocio;
    	public String AliasBanco;
    	public String CuentaDCVOrigen;
    	public String CuentaDCVDestino;
    	public String NombreUsuarioLiquidacion;
    	public Long IdUsuarioAnulacion;
    	public Timestamp FechaHoraAnulacion;
    	public Long IdComprobanteOrigen;
    	public Long FolioComprobanteDTE;
    	public Date FechaComprobanteOrigen;
    	public Long IdPortafolio;
    	public String NombrePortafolio;
    	public String IdTipoCanalIngreso;
    	public String Alias;
    	public String Nemotecnico;
    	public Long IdComprobantePartidaEgreso;
    	public String NombreEstadoNomina;
    	public String CondicionDeLiquidacion;
    	public String Glosa;
    	
    	public List<DetalleSalida> DetalleComprobante;
     	public List<FormaDePagoSalida> FormaDePagoComprobante;

	}
    
    public static class DetalleSalida extends BcsPojo {
    	public Long IdComprobanteDetalle; 
    	public Integer NroLinea; 
    	public String IdTipoMovimiento; 
    	public String TipoMovimiento; 
    	public String NombreTipoMovimiento; 
    	public String IdConcepto; 
    	public String NombreConcepto; 
    	public String IdMoneda;
    	public BigDecimal Monto; 
    	public String IdTipoComprobanteOrigen; 
    	public Long IdComprobanteOrigen; 
    	public Timestamp FechaComprobanteOrigen;
    	public String IdTipoProducto; 
    	public String NombreTipoProducto; 
    	public String IdTipoOperacion; 
    	public String NombreTipoOperacion; 
    	public Long IdInstrumento; 
    	public String Nemotecnico; 
    	public String IdTipoInstrumento;
    	public String IdClasificacionTipoInstrumento;  
    	public Long IdComprobante;

    }
    
    public static class FormaDePagoSalida extends BcsPojo {
    	public Long IdComprobanteFormaDePago;
    	public Long IdComprobante;
    	public String IdMedioDePago;
    	public String ClaveDCV;
    	public String NombreMedioDePago;
    	public String TipoMedioDePago;
    	public String IdModalidadDePago;
    	public String NombreModalidadDePago;
    	public Long IdCuentaBancariaCorredora;
    	public String IdTipoCuentaBancariaCorredora;
    	public String NombreTipoCuentaBancariaCorredora;
    	public Long IdBancoCorredora;
    	public String NombreBancoCorredora;
    	public String NombreCuentaBancariaCorredora;
    	public String NumeroCuentaBancariaCorredora;
    	public Long IdTarjetaBancariaCorredora;
    	public Long IdChequera;
    	public String NumeroSerieCorredora;
    	public Long NumeroDocumentoCorredora;
    	public Timestamp FechaDocumentoPagoCorredora;
    	public Long IdCuentaBancariaSocio;
    	public String IdTipoCuentaBancariaSocio;
    	public String NombreTipoCuentaBancariaSocio;
    	public String NombreCuentaBancariaSocio;
    	public Long IdBancoSocio;
    	public String NombreBancoSocio;
    	public String NumeroCuentaBancariaSocio;
    	public Long IdTarjetaBancariaSocio;
    	public String NumeroSerieSocio;
    	public Long NumeroDocumentoSocio;
    	public Timestamp FechaDocumentoPagoSocio;
    	public String IdMoneda;
    	public BigDecimal Monto;
    	public String IdEstado;
    	public String NombreEstado;
    	public Long IdUsuarioGiro;
    	public String NombreUsuarioGiro;
    	public Long IdComprobantePartida;
    	public Long IdUsuarioCertificacion;
    	public String NombreUsuarioCertificacion;
    	public Long IdUsuarioLiquidacion;
    	public String NombreUsuarioLiquidacion;

    }

    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
	Gson gson = Utils.getGsonBuilder();
	EntradaConsultarComprobantes in = gson.fromJson(jin.toString(), EntradaConsultarComprobantes.class);
	SalidaConsultarComprobantes out = logica(conn, in);
	JsonParser parser = new JsonParser();
	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	return dataout;
    }

    private SalidaConsultarComprobantes logica(Connection conn, EntradaConsultarComprobantes entrada) throws Exception {
    	
		logger.debug("INICIO::ConsultarComprobantesWS");
		
		SalidaConsultarComprobantes salida 	= new SalidaConsultarComprobantes();
		SQL sql 							= new SQL();
		
		if (entrada.FechaInLiquidacion != null && entrada.FechaTerLiquidacion != null) {
		Date FechaInicio = entrada.FechaInLiquidacion;
		Date FechaTermino = entrada.FechaTerLiquidacion;		
		Integer  parametroFecha = 0;		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
		String parametroFechaInicioStr = formatter.format(FechaInicio);
		String parametroFechaTerminoStr = formatter.format(FechaTermino);
		
		// OBTENEMOS EL PARAMETRO DE RANGO DE FECHA
		try {
			parametroFecha = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "TESORERIA", "RANGO_CONSULTAR_FECHA_LIQUIDACION_WS"));
		}
		
		catch(Exception er){
			parametroFecha = 30;
		}
		
		// VALIDAMOS QUE LA FECHA NO SUPERE EL LIMITE ESTABLECIDO POR EL PARAMETRO
		
		Boolean validador = ValidadorRangoDeFechas(parametroFechaInicioStr, parametroFechaTerminoStr, parametroFecha);
		
		if(validador) {
			HashMap<String, Object> paramsFiltros = new HashMap<String, Object>();
			
			
			paramsFiltros.put("FechaInLiquidacion", FechaInicio);
			paramsFiltros.put("FechaTerLiquidacion", FechaTermino);
			paramsFiltros.put("CodigoPortafolio", entrada.CodigoPortafolio);
			paramsFiltros.put("IdEstado", entrada.IdEstado);
			
			paramsFiltros.put("IdTipoMovimiento", entrada.TipoMovimiento); 
			paramsFiltros.put("IdTipoSocioNegocio", entrada.TipoSocioNegocio); 
			paramsFiltros.put("IdCaja", entrada.IdCaja); 
			paramsFiltros.put("DocumentoIdentidad", entrada.DocumentoIdentidad); 
			paramsFiltros.put("IdMoneda", entrada.IdMoneda);
			paramsFiltros.put("IdConcepto", entrada.IdConcepto);
			paramsFiltros.put("IdMedioDePago", entrada.IdMedioDePago);
			paramsFiltros.put("NombreBancoCorredora", entrada.BancoCorredora);
			paramsFiltros.put("AliasBanco", entrada.Alias);
			paramsFiltros.put("NombreBancoSocio",entrada.BancoCliente);
			paramsFiltros.put("NumeroCuentaBancariaCorredora", entrada.CuentaCorredora);

			/*SE OBTIENEN LOS COMPROBANTES*/
			List<ComprobanteSalida> ListadoComprobantes = sql.query(conn, SQL_SELECT_COMPROBANTES, paramsFiltros, ComprobanteSalida.class);
			System.out.println("N° de Comprobantes: " + ListadoComprobantes.size());
			
			// VALIDAMOS QUE EXISTAN COMPROBANTES 
			
			if(ListadoComprobantes.size() != 0) {
				
				/*OBTENEMOS LOS ID COMPROBANTES PARA REALIZAR LA CONSULTAS DE FORMAS DE PAGO Y DETALLEL*/
				
				List<Long> listadoIdsComprobantes = new ArrayList<Long>(ListadoComprobantes.size());
				
				for ( ComprobanteSalida Comprobante: ListadoComprobantes) {
					listadoIdsComprobantes.add(Comprobante.IdComprobante);
					
					
				}
				
				System.out.println("ID Comprobantes: " + listadoIdsComprobantes.size());
				
				//OBTENEMOS EL DETALLE y FORMAS DE PAGO DE LOS COMPROBANTES
				
				HashMap<String, Object> paramsComprobantes = new HashMap<String, Object>();
				paramsComprobantes.put("IdsComprobantes", listadoIdsComprobantes);
				
				
				List<DetalleSalida> ListadoDetalles = sql.query(conn, SQL_SELECT_DETALLE_COMPROBANTE, paramsComprobantes, DetalleSalida.class);
			
				System.out.println("N° de Detalles: " + ListadoDetalles.size());
				
				
				List<FormaDePagoSalida> ListadoFormasDePago = sql.query(conn, SQL_SELECT_FORMA_DE_PAGO, paramsComprobantes, FormaDePagoSalida.class);
				
				System.out.println("N° de Formas de Pago: " + ListadoFormasDePago.size());
				
				// ARMAMOS LA SALIDA DEL SERVICIO
				
				
				for (ComprobanteSalida comprobante : ListadoComprobantes) {
				    List<DetalleSalida> detalleComprobante = ListadoDetalles.stream()
				        .filter(detalle -> detalle.IdComprobante.equals(comprobante.IdComprobante))
				        .collect(Collectors.toList());
				    
				    comprobante.DetalleComprobante = detalleComprobante;
				    
				    
				    List<FormaDePagoSalida> FormaDePagoComprobante = ListadoFormasDePago.stream()
				        .filter(formaPago -> formaPago.IdComprobante.equals(comprobante.IdComprobante))
				        .collect(Collectors.toList());
				    
				    
				    comprobante.FormaDePagoComprobante = FormaDePagoComprobante;
				}
				
				System.out.println("N° DE COMPROBANTES SALIDA: " + ListadoComprobantes.size());
				
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "COMPROBANTES_WS_EXISTOSO", new HashMap<>(), conn, entrada.p_IdEmpresa));
				salida.P_OUT_COMPROBANTES = ListadoComprobantes;
				
				return salida;
			}
			
			else {
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "NO_EXISTEN_COMPROBANTES", new HashMap<>(), conn, entrada.p_IdEmpresa));
				
				return salida;
				
			}
		}
		else {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "RANGO_FECHA_LIQUIDACION_NO_VALIDO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		

		}
		
		salida.P_OUT_ERRORESCONTADOR++;
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FECHA_LIQUIDACION_NO_VALIDA", new HashMap<>(), conn, entrada.p_IdEmpresa));
		
		logger.info("FIN::ConsultarComprobantesWS");

		return salida;
    }
    
    public static Boolean ValidadorRangoDeFechas(String fechaInicio, String fechaTermino, Integer parametro) {
		Boolean val = false;
        Integer contador = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate inicio = LocalDate.parse(fechaInicio, formatter);
        LocalDate termino = LocalDate.parse(fechaTermino, formatter);
        
        
        while (!inicio.isAfter(termino)) {
        	contador ++;
            inicio = inicio.plusDays(1); 
        }
        
        if (contador <= parametro && contador != 0) {
        	val = true;
        }
        
        return val;
    }

    public static void main(String[] args) throws SQLException {
    	
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
		    
			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"TipoMovimiento\":null,\"FechaInLiquidacion\":\"2024-11-21\",\"FechaTerLiquidacion\":\"2024-11-18\",\"TipoSocioNegocio\":null,\"IdCaja\":null,\"DocumentoIdentidad\":\"76957190-6\",\"CodigoPortafolio\":\"0\",\"IdMoneda\":null,\"IdConcepto\":null,\"IdEstado\":\"ING\",\"IdMedioDePago\":null,\"BancoCorredora\":null,\"Alias\":null,\"BancoCliente\":null,\"CuentaCorredora\":null}\r\n";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"TipoMovimiento\":\"\",\"FechaInLiquidacion\":\"2024-10-15\",\"FechaTerLiquidacion\":\"2024-11-11\",\"TipoSocioNegocio\":\"\",\"IdCaja\":null,\"DocumentoIdentidad\":\"\",\"CodigoPortafolio\":\"0\",\"IdMoneda\":\"\",\"IdConcepto\":\"\",\"IdEstado\":\"ING\",\"IdMedioDePago\":\"\",\"BancoCorredora\":\"\",\"Alias\":\"\",\"BancoCliente\":\"\",\"CuentaCorredora\":\"\"}\r\n";
		    EntradaConsultarComprobantes entrada = Utils.getGsonBuilder().fromJson(json2, EntradaConsultarComprobantes.class);
	
		    System.out.println("== ENTRADA ==");
		    //System.out.println(entrada);
		    
		    con.rollback();
	
		    try {
		    	SalidaConsultarComprobantes salida = new ConsultarComprobantesWS().logica(con, entrada);
				System.out.println("== SALIDA ==");
				System.out.println(salida);
		    } catch (Exception er) {
		    	er.printStackTrace();
		    } finally {
		    	con.rollback();
		    }
	
		}
		System.out.println("== END ==");
    }

}
