package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarOperacionesDiariasPactos implements JavaCall{	
	static final Logger logger = LogManager.getLogger(ConsultarOperacionesDiariasPactos.class);	
	private final String SQL_ConsultaOperaciones = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarOperacionesDiariasPactos.sql");
	private final String SQL_ConsultaDetalleOperaciones = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarOperacionesDiariasPactos_SelectDetalleOperaciones.sql");
	
	public static class ConsultarOperacionesDiariasPactosEntrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public Long							p_IdPais;
		public String						p_FechaOpDesde = null;	
		public String						p_FechaOpHasta = null;	
		public String						p_FechaVeDesde = null;	
		public String						p_FechaVeHasta = null;	
		public String						p_FechaAnDesde = null;	
		public String						p_FechaAnHasta = null;	
		public String						p_IdEstado = null;
		public Long							p_FolioOperacion = null;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ConsultaOperaciones{
	    public Long IdOperacion;
	    public Long FolioOperacion;
	    public String FechaOperacion;
	    public String IdTipoOperacion;
	    public String NombreOperacion;
	    public Long IdCliente;
	    public String DocumentoIdentidad;
	    public String RazonSocial;
	    public Long IdPortafolio;
	    public String CodigoPortafolio;
	    public Long IdClienteCorredor;
	    public Long IdContraparte;
	    public String RazonSocialCorredora;
	    public BigDecimal Tasa;
	    public String CatIFRS;
	    public String IdTipoReajuste;
	    public String FechaLiquidacion;
	    public String FechaVencimiento;
	    public Date DateLiquidacion;
	    public Date DateVencimiento;
	    public Date FechaAnticipo;
	    public String Liquidado;
	    public Long IdAgente;
	    public Long IdPersonaEjecutivo;
	    public String Agente;
	    public Long IdSucursalEjecutivo;
	    public String Sucursal;
	    public BigDecimal MontoOperacion;
	    public BigDecimal MontoCompromisoAnticipo;
	    public BigDecimal MontoCompromiso;
	    public BigDecimal ValorOperacion;
	    public BigDecimal ValorCompromisoAnticipo;
	    public BigDecimal ValorCompromiso;
	    public String IdEstado;
	    public Long IdEmpresa;
	    public Long IdMesaOrigen;
	    public String NombreMesaOrigen;
	    public String IdTipoProducto;
	    public String IdMonedaLiquidacion;
	    public String Mesa;
	    public BigDecimal ValorCompraCartera;
	    public BigDecimal ValorInteresCartera;
	    public BigDecimal ValorReajusteCartera;
	    public BigDecimal ValorCompraProximoCartera;
	    public BigDecimal MontoCompraProximoCartera;
	    public Long FolioCartera;
	    public Long PlazoEnDias;
	    public String RelacionCorredora;
	    public String ValorIndicador;
	    public String ConfirmaAdemdum;
	    public String IdTipoInstrumento;
	    public Date FechaRespaldo;
	    
	    public Long getFolioOperacion() {
	        return FolioOperacion;
	    }
	    
	    public Date getFechaRespaldo() {
	        return FechaRespaldo;
	    }
	    
	    public String getIdEstado() {
	    	return IdEstado;
	    }
	    
	    
	};
	
	public static class DetalleOperaciones{
		public String TipoRegistro;
		public String TipoCompromiso;
		public int IndicadorCobertura;
		public String FechaOperacion;
		public String NombreCliente;
		public String RutCliente;
		public String Relacionado;
		public String Mercado;
		public String Nemotecnico;
		public String IdEmisor;
		public String IdTipoReajuste;
		public BigDecimal Cantidad;			
	    public Long FolioOperacion;
		public String FechaVencimientoOperacion;
		public String FechaVencimientoInstrumentoCadena;
		public Date FechaVencimientoInstrumento;
		public BigDecimal MontoPacto;
		public String IdTipoReajusteOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal Tasa;	
		public BigDecimal MontoSubyacente;
		public BigDecimal MontoCompromisoDetalle;
	    public Long FolioCartera;
		public String FlagDeterioro;
		public BigDecimal PorcentajeDeterioro;
		public BigDecimal Deterioro;
		public int Fase;
		public String IdMonedaInstrumento;

	    public Long getFolioOperacion() {
	        return FolioOperacion;
	    }

	    
	};
		
	public static class ConsultarOperacionesDiariasPactosSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR = 0L;
		public List<ConsultaOperaciones>	P_OUT_OPERACIONES;
		public List<DetalleOperaciones>		P_OUT_DETALLEOPERACIONES;
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public ConsultarOperacionesDiariasPactosSalida logica(Connection conn, ConsultarOperacionesDiariasPactosEntrada in) throws Exception {
		SQL sql = new SQL();
		ConsultarOperacionesDiariasPactosSalida out = new ConsultarOperacionesDiariasPactosSalida();
		
		HashMap<String, Object> params = new HashMap<String, Object>();		
		if (in.p_FechaOpDesde != null) { 		  
		     ZonedDateTime zdt = ZonedDateTime.parse(in.p_FechaOpDesde);
		     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		     String p_FechaOpDesde = zdt.format(formatter);
			params.put("FechaOpDesde", (p_FechaOpDesde)); 
		} else { params.put("FechaOpDesde", null ); }
		if (in.p_FechaOpHasta != null) { 
			ZonedDateTime zdt = ZonedDateTime.parse(in.p_FechaOpHasta);
		     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		     String p_FechaOpHasta = zdt.format(formatter);
			params.put("FechaOpHasta", (p_FechaOpHasta));
		} else { params.put("FechaOpHasta", null ); }
		if (in.p_FechaVeDesde != null) { 
			ZonedDateTime zdt = ZonedDateTime.parse(in.p_FechaVeDesde);
		     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		     String p_FechaVeDesde = zdt.format(formatter);
			params.put("FechaVeDesde", (p_FechaVeDesde)); 
		} else { params.put("FechaVeDesde", null ); }
		if (in.p_FechaVeHasta != null) { 
			ZonedDateTime zdt = ZonedDateTime.parse(in.p_FechaVeHasta);
		     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		     String p_FechaVeHasta = zdt.format(formatter);
			params.put("FechaVeHasta", (p_FechaVeHasta)); 
		} else { params.put("FechaVeHasta", null ); }
		if (in.p_FechaAnDesde != null) { 
			ZonedDateTime zdt = ZonedDateTime.parse(in.p_FechaAnDesde);
		     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		     String p_FechaAnDesde = zdt.format(formatter);
			params.put("FechaAnDesde", (p_FechaAnDesde)); 
		} else { params.put("FechaAnDesde", null ); }
		if (in.p_FechaAnHasta != null) { 
			ZonedDateTime zdt = ZonedDateTime.parse(in.p_FechaAnHasta);
		     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		     String p_FechaAnHasta = zdt.format(formatter);
			params.put("FechaAnHasta", (p_FechaAnHasta)); 
		} else { params.put("FechaAnHasta", null ); }
		if (in.p_IdEstado != null && !in.p_IdModulo.equalsIgnoreCase("NOR")) { params.put("IdEstado", (in.p_IdEstado));} else { params.put("IdEstado", null ); }
		
		List<ConsultaOperaciones> OperacionesPactos = sql.query(conn, SQL_ConsultaOperaciones, params, ConsultaOperaciones.class);
		
		List<DetalleOperaciones> detalleOperacionesPactos = new ArrayList<DetalleOperaciones>();
		List<ConsultaOperaciones> filteredList = OperacionesPactos.stream()
					.sorted(Comparator.comparing(ConsultaOperaciones::getFolioOperacion)
					.thenComparing(Comparator.comparing(ConsultaOperaciones::getFechaRespaldo, Comparator.nullsLast(Comparator.naturalOrder())).reversed()))
					.collect(Collectors.toMap( ConsultaOperaciones::getFolioOperacion, consultaOperacion -> consultaOperacion,  (oldValue, newValue) -> newValue))  
					.values().stream().collect(Collectors.toList());
		
		List<Long> lstFolioOperacion = new ArrayList<Long>(); 
		if(in.p_FolioOperacion != null) {
			lstFolioOperacion.add(in.p_FolioOperacion);
		} 
		
		if(in.p_IdEstado != null && in.p_IdEstado.equalsIgnoreCase("VIG") && in.p_IdModulo != null && in.p_IdModulo.equalsIgnoreCase("NOR")) {
			lstFolioOperacion = filteredList.stream()
				    .filter(operacion -> "VIG".equals(operacion.getIdEstado()))
	            	.map(ConsultaOperaciones::getFolioOperacion)
	            	.sorted()
	            	.collect(Collectors.toList());
		}
			
        HashMap<String, Object> paramsDetalleOperaciones = new HashMap<>();
        paramsDetalleOperaciones.put("FolioOperacion", lstFolioOperacion);
        
        detalleOperacionesPactos = sql.query(conn, SQL_ConsultaDetalleOperaciones, paramsDetalleOperaciones, DetalleOperaciones.class);
       
		ObtenerIndicadorCobertura.ObtenerIndicadorCoberturaEntrada entradaObtenerIndicadorCobertura = new ObtenerIndicadorCobertura.ObtenerIndicadorCoberturaEntrada();
		ObtenerIndicadorCobertura.ObtenerIndicadorCoberturaSalida salidaObtenerIndicadorCobertura = new ObtenerIndicadorCobertura.ObtenerIndicadorCoberturaSalida();
		entradaObtenerIndicadorCobertura.p_IdEmpresa = in.p_IdEmpresa;
		entradaObtenerIndicadorCobertura.p_IdModulo = in.p_IdModulo;
		entradaObtenerIndicadorCobertura.p_IdUsuario = in.p_IdUsuario;
		entradaObtenerIndicadorCobertura.p_Operaciones = procesarOperaciones(filteredList, detalleOperacionesPactos, sql, conn);
	
		ObtenerIndicadorCobertura obtenerindicadorCobertura = new ObtenerIndicadorCobertura();
		salidaObtenerIndicadorCobertura = obtenerindicadorCobertura.logica(conn, entradaObtenerIndicadorCobertura);
		
		for(DetalleOperaciones operacionDetalle: detalleOperacionesPactos) {
			for(ObtenerIndicadorCobertura.DetalleOperaciones operacionesIndicador : salidaObtenerIndicadorCobertura.P_OUT_DETALLEOPERACIONES) {
				if(operacionDetalle.FolioOperacion.compareTo(operacionesIndicador.FolioOperacion) == 0 && operacionDetalle.FolioCartera.compareTo(operacionesIndicador.FolioCartera) == 0) {
					operacionDetalle.IndicadorCobertura = operacionesIndicador.IndicadorCobertura;
					operacionDetalle.TipoRegistro = "2";
				}
			}
		}
		
		out.P_OUT_DETALLEOPERACIONES = detalleOperacionesPactos;	
		out.P_OUT_OPERACIONES = filteredList;
		
		return out;
	}
	
	public List<ObtenerIndicadorCobertura.DetalleOperaciones> procesarOperaciones(List<ConsultaOperaciones> filteredList, List<DetalleOperaciones> detalleOperacionesPactos, SQL sql, Connection conn) throws SQLException {

        List<ObtenerIndicadorCobertura.DetalleOperaciones> lstInDetalleOperacionesPactos = new ArrayList<>();
        
        for (DetalleOperaciones detOperacion : detalleOperacionesPactos) {
            for (ConsultaOperaciones operacion : filteredList) {
                if (operacion.getFolioOperacion().compareTo(detOperacion.getFolioOperacion()) == 0) {
                    ObtenerIndicadorCobertura.DetalleOperaciones inDetalleOperacionesPactos = new ObtenerIndicadorCobertura.DetalleOperaciones();
                    inDetalleOperacionesPactos.FolioOperacion = detOperacion.FolioOperacion;
					inDetalleOperacionesPactos.FolioCartera = detOperacion.FolioCartera;
					inDetalleOperacionesPactos.Nemotecnico = detOperacion.Nemotecnico;
					inDetalleOperacionesPactos.IdEmisor = detOperacion.IdEmisor;
					inDetalleOperacionesPactos.Mercado = detOperacion.Mercado;
					inDetalleOperacionesPactos.IdTipoReajuste = detOperacion.IdTipoReajuste;
					inDetalleOperacionesPactos.Tasa = detOperacion.Tasa;
					inDetalleOperacionesPactos.IndicadorCobertura = detOperacion.IndicadorCobertura;
					inDetalleOperacionesPactos.FechaVencimientoInstrumento = detOperacion.FechaVencimientoInstrumento;
					inDetalleOperacionesPactos.FechaVencimientoInstrumentoCadena = detOperacion.FechaVencimientoInstrumentoCadena;
					inDetalleOperacionesPactos.Cantidad = detOperacion.Cantidad;
					inDetalleOperacionesPactos.MontoSubyacente = detOperacion.MontoSubyacente;
					inDetalleOperacionesPactos.MontoCompromisoDetalle = detOperacion.MontoCompromisoDetalle;						
					inDetalleOperacionesPactos.FechaOperacion = operacion.FechaOperacion;
					inDetalleOperacionesPactos.IdTipoOperacion = operacion.IdTipoOperacion;
					inDetalleOperacionesPactos.NombreOperacion = operacion.NombreOperacion;
					inDetalleOperacionesPactos.FechaLiquidacion = operacion.FechaLiquidacion;
					inDetalleOperacionesPactos.FechaVencimiento = operacion.FechaVencimiento;
					inDetalleOperacionesPactos.DateLiquidacion = operacion.DateLiquidacion;
					inDetalleOperacionesPactos.DateVencimiento = operacion.DateVencimiento;
					inDetalleOperacionesPactos.FechaAnticipo = operacion.FechaAnticipo;
					inDetalleOperacionesPactos.Liquidado = operacion.Liquidado;
					inDetalleOperacionesPactos.PlazoEnDias = operacion.PlazoEnDias;
					inDetalleOperacionesPactos.RelacionCorredora = operacion.RelacionCorredora;
					inDetalleOperacionesPactos.IdTipoInstrumento = operacion.IdTipoInstrumento;
					inDetalleOperacionesPactos.FechaRespaldo = operacion.FechaRespaldo;
					inDetalleOperacionesPactos.FlagDeterioro = detOperacion.FlagDeterioro;
					inDetalleOperacionesPactos.PorcentajeDeterioro = detOperacion.PorcentajeDeterioro;
					inDetalleOperacionesPactos.Deterioro = detOperacion.Deterioro;
					inDetalleOperacionesPactos.Fase = detOperacion.Fase;
					inDetalleOperacionesPactos.IdMonedaInstrumento = detOperacion.IdMonedaInstrumento;

                    
                    lstInDetalleOperacionesPactos.add(inDetalleOperacionesPactos);
                    break;
                }        
            }
        }
        
        return lstInDetalleOperacionesPactos;
    }
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		ConsultarOperacionesDiariasPactosEntrada jin=gson.fromJson(in.toString() ,ConsultarOperacionesDiariasPactosEntrada.class);
		ConsultarOperacionesDiariasPactosSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;		
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdPais\":null,\"p_FechaOpHasta\":\"2024-05-09T03:59:59.000Z\",\"p_FolioOperacion\":132815}" ;
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdPais\":null,\"p_FechaOpHasta\":\"2024-05-09T03:59:59.000Z\",\"p_FolioOperacion\":132815}" ;
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdPais\":null,\"p_FechaOpHasta\":\"2024-03-04T02:59:59.000Z\"}";
		
		Gson gson = Utils.getGsonBuilder();
		ConsultarOperacionesDiariasPactosEntrada entrada = gson.fromJson(json, ConsultarOperacionesDiariasPactosEntrada.class);	
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("qa02","connections.properties");
			ConsultarOperacionesDiariasPactos prueba = new ConsultarOperacionesDiariasPactos(); 
			ConsultarOperacionesDiariasPactosSalida Salida = prueba.logica(conn, entrada);
			System.out.println(Salida);
			conn.rollback();
			
		}catch (Exception e){	
			System.out.println(e);	
			} finally {		conn.close();		}
		System.out.println("== END ==");
	}
}