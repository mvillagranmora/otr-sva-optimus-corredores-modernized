package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class WsConsultaComprobantes implements JavaCall {
	
	static final Logger logger								= LogManager.getLogger(WsConsultaComprobantes.class);
	public String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectComprobantes.sql");
	public String SQL_SELECT_DETALLES    = loadResourceToString("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectDetallesComprobantes.sql");
	public String SQL_SELECT_COBROS 	= loadResourceToString("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectCobrosComprobantes.sql");
	public String SQL_SELECT_PORTAFOLIOS    = loadResourceToString("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectPortafolios.sql");
	public String SQL_CREATE_TABLA_IDCOMPROBANTE = "CREATE LOCAL TEMPORARY TABLE #WsConsultaComprobantes_IdComprobantes (\"IdComprobante\" BIGINT)";
	public String SQL_INSERT_TABLA_IDCOMPROBANTE = "INSERT INTO #WsConsultaComprobantes_IdComprobantes (\"IdComprobante\") VALUES (:IdComprobante)";
	public String SQL_DROP_TABLA_IDCOMPROBANTE   = "DROP TABLE #WsConsultaComprobantes_IdComprobantes";
	
	public static class EntradaWsConsultaComprobantes extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Rut;		
		public String p_FechaInicial;
		public String p_FechaFinal;
		
		public List<String> p_CodigoPortafolio;
		public List<String> p_TipoDocumento;
		
		public EntradaWsConsultaComprobantes () {
			this.p_CodigoPortafolio= new ArrayList<String>();
			this.p_TipoDocumento   = new ArrayList<String>();
			this.p_FechaInicial = "0001-01-01";
			this.p_FechaFinal = "0001-01-01";
		}
	}

	public static class SalidaWsConsultaComprobantes extends BcsPojo {
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Comprobante> P_OUT_COMPROBANTES;
		
		public SalidaWsConsultaComprobantes() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_COMPROBANTES = new ArrayList<Comprobante>();
		}
	}
	
	public static class Comprobante {
		public Long   IdComprobante;
		public Long   FolioComprobante;
		public Timestamp FechaComprobante;
		public Date FechaLiquidacion; 
		public String NombreCliente;
		public String NombreEjecutivo;
		public String IdCondicionLiquidacion; 
		public String IdTipoComprobante; 
		public String IdTipoProducto;
		public String IdConcepto;
		public String Glosa;
		public Long   IdPortafolio;
		public String CodigoPortafolio; 
		public Long IdSucursalEmpleadoEjecutivoPortafolio; 
		public String IdTipoOperacion;
		public BigDecimal Monto; 
		public BigDecimal MontoComprobante;
		public BigDecimal MontoLiquidacionCobro;
		public BigDecimal MontoFinal; 
		public BigDecimal MontoPagado; 
		public BigDecimal MontoLiquidacion; 
		public BigDecimal MontoComprobanteFinal;
		public String IdEstadoComprobante;
		public String IdEstadoDTE; 
		public String IdEstadoSII;
		public String IdClasificacionTipoInstrumento;
		public String AccionTipoOperacion;
		public List<Detalle> detalles;
		public List<Cobro> cobros;
		
		
		public Comprobante () {
			this.Monto= BigDecimal.ZERO; 
			this.MontoComprobante= BigDecimal.ZERO;
			this.MontoLiquidacionCobro= BigDecimal.ZERO;
			this.MontoFinal= BigDecimal.ZERO; 
			this.MontoPagado= BigDecimal.ZERO; 
			this.MontoLiquidacion= BigDecimal.ZERO; 
			this.MontoComprobanteFinal= BigDecimal.ZERO;
			this.detalles =new ArrayList<Detalle>();
			this.cobros=new ArrayList<Cobro>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Detalle {
		public Long IdComprobante;
		public String NemotecnicoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal Monto;
		public String IdFormaCustodia;
		public String IdTipoProducto;
		public String IdTipoInstrumento;
		public String Descripcion;
		public Date FechaLiquidacion;
		public String IdEmisor;
		public Date FechaVencimiento;
		public Integer PresenciaBursatilAjustada;
		public String Glosa;
		
		public Detalle () {
			this.Cantidad= BigDecimal.ZERO;
			this.Precio= BigDecimal.ZERO;
			this.Monto= BigDecimal.ZERO;
			this.Tasa= BigDecimal.ZERO;
			this.Glosa= "";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Cobro{
		public Long IdComprobante;
		public String NombreCobro;
		public String FormaCobro;
		public BigDecimal Porcentaje;
		public String CodigoMonedaLocalCobro;
		public BigDecimal MontoLocalCobro;
		public String CodigoMonedaLiquidacionCobro;
		public BigDecimal MontoLiquidacionCobro;
		
		public Cobro() {
			this.Porcentaje = BigDecimal.ZERO;
			this.MontoLocalCobro = BigDecimal.ZERO;
			this.MontoLiquidacionCobro = BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public String codigoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaWsConsultaComprobantes logica (Connection conn , EntradaWsConsultaComprobantes entrada ) throws Exception{
		logger.info("INICIO::WsConsultaComprobantes");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWsConsultaComprobantes salida = new SalidaWsConsultaComprobantes();
		List<Comprobante> listaComprobantes = new ArrayList<Comprobante>();
		HashMap<Long,Comprobante> hashComprobante = new HashMap<Long,Comprobante>();		
		List<Detalle> listaDetalles= new ArrayList<Detalle>();		
		List<Cobro> listaCobros = new ArrayList<Cobro>();
		
		SQL sql = new SQL();	
		
		this.validacionesBasicas(entrada, salida, conn);
		
		if(!salida.P_OUT_MENSAJES.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR=1L;
			return salida;
		}
		
		
		listaComprobantes = this.obtieneComprobantes(conn, entrada);
		
		for(Comprobante comp : listaComprobantes) {
			hashComprobante.put(comp.IdComprobante, comp);
		}
		
		//Tabla Temporal
		SQL.exec(conn, SQL_CREATE_TABLA_IDCOMPROBANTE);		
		sql.batch(conn,SQL_INSERT_TABLA_IDCOMPROBANTE ,listaComprobantes, Comprobante.class);
		
		//buscamos detalles
		listaDetalles = this.obtieneDetalles(conn);
		
		//buscamos cobros
		listaCobros = this.obtieneCobros(conn);
		//listaCobros.forEach(elem -> System.out.print("RespuestaCOBROS"+elem));
		
		SQL.exec(conn, SQL_DROP_TABLA_IDCOMPROBANTE);	
		
		listaDetalles.stream().forEach(detalle -> {				
			detalle.Glosa = this.crearGlosa(hashComprobante.get(detalle.IdComprobante),detalle);			
			hashComprobante.get(detalle.IdComprobante).detalles.add(detalle);			
		});	
		
		listaCobros.stream().forEach(cobro -> {
			hashComprobante.get(cobro.IdComprobante).cobros.add(cobro);
		});
		
		for(Map.Entry<Long, Comprobante> comprobante : hashComprobante.entrySet()){			
			 salida.P_OUT_COMPROBANTES.add(comprobante.getValue());
		}
		

        if(salida.P_OUT_COMPROBANTES.isEmpty()) {
        	MensajeSalida mensajeSalida = new MensajeSalida(3L, MensajeSalida.MENSAJE_ERROR, "CONSULTACOMPROBANTES_NO_HAY_COMPROBANTE","No existen comprobantes");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
        }
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::WsConsultaComprobantes :" , finServicio- inicioServicio);		
		return salida;
		
	}
	
	
	public void validacionesBasicas(EntradaWsConsultaComprobantes entrada , SalidaWsConsultaComprobantes Salida , Connection conn) throws Exception {
		logger.trace("INICIO::WsConsultaComprobantes.validacionesBasicas");
		long inicioMetodo = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		if(entrada.p_Rut==null || "".equals(entrada.p_Rut)) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CONSULTACOMPROBANTES_RUT_OBLIGATORIO","Rut Cliente s obligatorio");
			mensajes.add(mensajeSalida);			
		}
		
		if(!("".equals(entrada.p_FechaInicial)) && !("".equals(entrada.p_FechaFinal)) ) {
			
			SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
			Date fechaInicial = formato.parse(entrada.p_FechaInicial) ;
			Date fechaFinal   = formato.parse(entrada.p_FechaFinal);
			
			java.util.Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_FechaInicial); 			    
			java.util.Date date2=new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_FechaFinal);
			if(date1.after(date2)) {
				MensajeSalida mensajeSalida = new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "CONSULTACOMPROBANTES_FECHA_INICIO_MAYOR","Fecha inicial mayor que final");
				mensajes.add(mensajeSalida);
			}
		}
		
		if(!entrada.p_CodigoPortafolio.isEmpty()) {
			//select portafolio cliente  ver cual no es valido.
			List<Portafolio> listaCodigoPortafolio = obtienePortafolio(conn , entrada.p_Rut);
			HashMap<String,Object> mapPortafolioCliente =  Utils.List2Hash(listaCodigoPortafolio, "codigoPortafolio");
			
			//codigo portafolio se repite 
			entrada.p_CodigoPortafolio.stream().forEach( portafolio -> {
				if(mapPortafolioCliente.get(portafolio)== null) {
					MensajeSalida mensajeSalida = new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "CONSULTACOMPROBANTES_PORTAFOLIO_NO_EXISTE","Portafolio "+portafolio+" cliente no existe");
					mensajes.add(mensajeSalida);
				}				
			});
		}
		
		Salida.P_OUT_MENSAJES.addAll(mensajes);
		
		long finMetodo = System.currentTimeMillis();
		logger.info("FIN::WsConsultaComprobantes.validacionesBasicas :" , finMetodo- inicioMetodo);		
	}
	
	
	public String crearGlosa(Comprobante comprobante,Detalle detalle) {
		logger.trace("INICIO::WsConsultaComprobantes.crearGlosa");
		long inicioMetodo = System.currentTimeMillis();
		String glosaOut = "";
		
		if("RV".equals(comprobante.IdClasificacionTipoInstrumento)|| "RF".equals(comprobante.IdClasificacionTipoInstrumento)) {			
			if(!"FOE".equals(comprobante.IdTipoComprobante)) {
				glosaOut=comprobante.AccionTipoOperacion+" "
						+detalle.NemotecnicoInstrumento+" "
						+detalle.Cantidad+" "
						+detalle.Precio+" "
						+(detalle.PresenciaBursatilAjustada >=25?" APB ":" ")
						+detalle.IdFormaCustodia+" ";
			}			
			
		}else if ("IF".equals(comprobante.IdClasificacionTipoInstrumento) ) {
			glosaOut=detalle.NemotecnicoInstrumento+" "
					+detalle.IdEmisor+" "
					+detalle.FechaVencimiento+" " 
					+(detalle.PresenciaBursatilAjustada >=25?" APB ":" ");
		}
		
		
		long finMetodo = System.currentTimeMillis();
		logger.info("FIN::WsConsultaComprobantes.crearGlosa :" , finMetodo- inicioMetodo);	
		return glosaOut;
	}
	
	public List<Detalle> obtieneDetalles(Connection conn)throws Exception {
		logger.debug("INICIO::WsConsultaComprobantes.obtieneDetalles");
		long inicioMetodo = System.currentTimeMillis();
		List<Detalle> listaDetalles = new ArrayList<Detalle>();
		SQL sql = new SQL();		
			
		listaDetalles = sql.query(conn, SQL_SELECT_DETALLES, new HashMap<String,Object>(), Detalle.class);		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaComprobantes.obtieneDetalles :" , finMetodo- inicioMetodo);		
		return listaDetalles;
	}
	
	public List<Cobro> obtieneCobros(Connection conn) throws Exception {
		logger.debug("INICIO::WsConsultaComprobantes.obtieneCobros");
		long inicioMetodo = System.currentTimeMillis();
		List<Cobro> listaCobros = new ArrayList<Cobro>();
		SQL sql = new SQL();		
		
		listaCobros = sql.query(conn, SQL_SELECT_COBROS, new HashMap<String,Object>(), Cobro.class);	
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaComprobantes.obtieneCobros :" , finMetodo- inicioMetodo);
		return listaCobros;
	}
	
	public List<Comprobante> obtieneComprobantes(Connection conn, EntradaWsConsultaComprobantes entrada )throws Exception {
		logger.debug("INICIO::WsConsultaComprobantes.obtieneComprobantes");
		long inicioMetodo = System.currentTimeMillis();
		List<Comprobante> listaComprobantes = new ArrayList<Comprobante>();
		List<Comprobante> listaComprobantesPortafolio = new ArrayList<Comprobante>();
		List<Comprobante> listaComprobantesFiltrados = new ArrayList<Comprobante>();
		SQL sql = new SQL();		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("rut", entrada.p_Rut);
		params.put("fechaInicio","".equals(entrada.p_FechaInicial)?"0001-01-01":entrada.p_FechaInicial);
		params.put("fechaTermino", "".equals(entrada.p_FechaFinal)?"0001-01-01":entrada.p_FechaFinal);		
		listaComprobantes = sql.query(conn, SQL_SELECT_COMPROBANTES, params, Comprobante.class);		
		
		//Filtrado 
		if(!listaComprobantes.isEmpty()) {			
			if(!entrada.p_CodigoPortafolio.isEmpty()) {
				listaComprobantesPortafolio = this.filtraComprobantesPortafolio(listaComprobantes, entrada);
			}else {
				listaComprobantesPortafolio.addAll(listaComprobantes);
			}
			
			if(!entrada.p_TipoDocumento.isEmpty()) {
				if(entrada.p_CodigoPortafolio.isEmpty()) {
					listaComprobantesFiltrados = this.filtraComprobantesTipoDocumento(listaComprobantes, entrada);
				}else {
					listaComprobantesFiltrados = this.filtraComprobantesTipoDocumento(listaComprobantesPortafolio, entrada);
				}				
			}else {
				listaComprobantesFiltrados.addAll(listaComprobantesPortafolio);
			}
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaComprobantes.obtieneComprobantes :" , finMetodo- inicioMetodo);		
		return listaComprobantesFiltrados;
	}
	
	public List<Comprobante> filtraComprobantesPortafolio (List<Comprobante> listaComprobantes, EntradaWsConsultaComprobantes entrada){
		logger.trace("INICIO::WsConsultaComprobantes.filtraComprobantes");
		long inicioMetodo = System.currentTimeMillis();
		List<Comprobante> listaComprobantesFiltrados = new ArrayList<Comprobante>();
		Set<String> hashPortafoliosEntrada = new HashSet<>(entrada.p_CodigoPortafolio);
			
		listaComprobantes.stream().forEach(portafolio -> {			
			if(hashPortafoliosEntrada.contains(portafolio.CodigoPortafolio)) {
				listaComprobantesFiltrados.add(portafolio);
			}			
		});		
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::WsConsultaComprobantes.obtieneComprobantes :" , finMetodo- inicioMetodo);
		return listaComprobantesFiltrados;		
	}
	
	public List<Comprobante> filtraComprobantesTipoDocumento (List<Comprobante> listaComprobantes, EntradaWsConsultaComprobantes entrada){
		logger.trace("INICIO::WsConsultaComprobantes.filtraComprobantes");
		long inicioMetodo = System.currentTimeMillis();
		List<Comprobante> listaComprobantesFiltrados = new ArrayList<Comprobante>();
		Set<String> hashTipoDocumentoEntrada = new HashSet<>(entrada.p_TipoDocumento);
		
		
		if(!entrada.p_TipoDocumento.isEmpty()) {
			
			listaComprobantes.stream().forEach(tipoDocumento -> {			
				if(hashTipoDocumentoEntrada.contains(tipoDocumento.IdTipoComprobante)) {
					listaComprobantesFiltrados.add(tipoDocumento);
				}			
			});
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::WsConsultaComprobantes.obtieneComprobantes :" , finMetodo- inicioMetodo);
		return listaComprobantesFiltrados;		
	}
	
	
	
	public List<Portafolio> obtienePortafolio(Connection conn, String rut)throws Exception {
		logger.debug("INICIO::WsConsultaComprobantes.obtienePortafolio");
		long inicioMetodo = System.currentTimeMillis();
		
		List<Portafolio> listaPortafolios = new ArrayList<Portafolio>();
		SQL sql = new SQL();		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("rut", rut);
		
		listaPortafolios = sql.query(conn, SQL_SELECT_PORTAFOLIOS, params, Portafolio.class);		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::WsConsultaComprobantes.obtienePortafolio :" , finMetodo- inicioMetodo);		
		return listaPortafolios;
	}
	
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaWsConsultaComprobantes jin = gson.fromJson(in.toString(), EntradaWsConsultaComprobantes.class);
		SalidaWsConsultaComprobantes out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Throwable {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FAC\",\"p_Rut\":\"333-6\",\"p_FechaInicial\":\"2021-08-29\",\"p_FechaFinal\":\"2022-01-29\",\"p_CodigoPortafolio\":[1],\"p_TipoDocumento\":[]}";
		
		EntradaWsConsultaComprobantes Objin = gson.fromJson(json, EntradaWsConsultaComprobantes.class);
		WsConsultaComprobantes demo = new WsConsultaComprobantes();				
		try {
			SalidaWsConsultaComprobantes ObjOut = demo.logica(con, Objin);			
			System.out.println(ObjOut.toString());
			con.rollback();
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			
    	}
		System.out.println("== END ==");
	}

}
