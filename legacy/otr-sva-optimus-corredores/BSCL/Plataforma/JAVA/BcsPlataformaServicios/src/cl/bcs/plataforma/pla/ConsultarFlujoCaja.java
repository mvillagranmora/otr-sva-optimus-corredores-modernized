package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjERRORESDETALLE;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ServiceEntrada;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ServiceSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarFlujoCaja implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ConsultarFlujoCaja.class);
	
	private final String SQL_SELECT_USOS = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectUso.sql");
	private final String SQL_INSERT_PORTAFOLIOS_TEMPORAL = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_InsertTablaTemporalPortafolios.sql");
	private final String SQL_DELETE_PORTAFOLIOS_TEMPORAL = "DELETE FROM \"BSCL.Plataforma.BD::PLA.ConsultarFlujoCaja_Portafolio_Proceso\" WHERE 1=1";
	private final String SQL_SELECT_PORTAFOLIOS_POR_MONEDA = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectPortafoliosPorMoneda.sql");
	private final String SQL_SELECT_PORTAFOLIOS_POR_CUENTA_INVERSION = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectPortafoliosPorCuentaInversion.sql");
	private final String SQL_SELECT_PORTAFOLIOS_POR_CLIENTE = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectPortafoliosPorCliente.sql");
	
	public static class EntradaConsultarFlujoCaja{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	   	public String p_UsaReglasNegocio;
		public String p_FechaDesde;
	    public String p_FechaHasta;
		public String p_Uso;
		public String p_DesplegarEnUI;
		public String p_AgruparPorOrigen;
	    public List<PortafolioAConsultar> p_PortafolioAConsultar;
	    public List<ClienteAConsultar> p_ClienteAConsultar;
	    
	    public EntradaConsultarFlujoCaja() {
	    	this.p_PortafolioAConsultar = new ArrayList<PortafolioAConsultar>();
	    	this.p_ClienteAConsultar = new ArrayList<ClienteAConsultar>();
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafolioAConsultar{
		public Long IdPortafolio;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClienteAConsultar{
		public Long IdCliente;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientePortafolio{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Uso{
		public String NombreUso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarFlujoCaja{
		public Long P_OUT_ERRORESCONTADOR;
		public List<ObjFLUJOCAJA> P_OUT_FLUJOCAJA;
		public List<ObjFLUJOCAJADETALLADO> P_OUT_FLUJOCAJADETALLADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConsultarFlujoCaja() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_FLUJOCAJA = new ArrayList<ObjFLUJOCAJA>();
			this.P_OUT_FLUJOCAJADETALLADO = new ArrayList<ObjFLUJOCAJADETALLADO>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutFlujoCaja{
		public String NombreUso;
		public String FechaSaldo;
		public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Saldo;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutFlujoCajaDetallado{
		public String FechaSaldo;
		public Long IdPortafolio;
        public String IdMoneda;
        public Long IdUsoFlujoCaja;
        public String NombreUso;
        public Long IdConfiguracionFlujoCaja;
        public String ItemCalculo;
        public BigDecimal Monto;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaConsultarFlujoCaja in = gson.fromJson(jin.toString(), EntradaConsultarFlujoCaja.class);
		SalidaConsultarFlujoCaja out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public SalidaConsultarFlujoCaja logica(Connection con, EntradaConsultarFlujoCaja entrada) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio ConsultarFlujoCaja");
		
		SalidaConsultarFlujoCaja out = new SalidaConsultarFlujoCaja();
		SQL sql=new SQL();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		Date dFechaDesde = Fecha.toDate(entrada.p_FechaDesde);
		Date dFechaHasta = Fecha.toDate(entrada.p_FechaHasta);
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Uso", entrada.p_Uso);
		List<Uso> usos = sql.query(con, SQL_SELECT_USOS, params,Uso.class);
		
		/*
		 * Validaciones
		 */
		if("0001-01-01".equals(entrada.p_FechaDesde)) {
			HashMap<String,String> mje = new HashMap<String,String>();
			mje.put("Fecha Desde", entrada.p_FechaDesde);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FECHA_NO_VALIDA",mje,con,entrada.p_IdEmpresa));
		}
		
		if("0001-01-01".equals(entrada.p_FechaHasta)) {
			HashMap<String,String> mje = new HashMap<String,String>();
			mje.put("Fecha Hasta", entrada.p_FechaHasta);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FECHA_NO_VALIDA",mje,con,entrada.p_IdEmpresa));
		}
		
		if(dFechaDesde.compareTo(dFechaHasta) == 1) {
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FECHA_NO_VALIDA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		if(usos.size() == 0 && entrada.p_Uso != null) {
			HashMap<String,String> mje = new HashMap<String,String>();
			mje.put("NombreUso", entrada.p_Uso);
			out.P_OUT_ERRORESCONTADOR += 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"USO_FLUJO_CAJA_NO_EXISTE",mje,con,entrada.p_IdEmpresa));
		}
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		//Si viene entrada.p_ClienteAConsultar, buscar sus portafolios y asignarlos a entrada.p_PortafolioAConsultar
		if (entrada.p_ClienteAConsultar.size() > 0) {
			List<Long> idsCliente = new ArrayList<Long>();
			HashMap<Long, ClienteAConsultar> clienteAConsultarHash = new HashMap<Long, ClienteAConsultar>();
			
			for(ClienteAConsultar cli : entrada.p_ClienteAConsultar) {
				if(clienteAConsultarHash.get(cli.IdCliente) == null) {
					idsCliente.add(cli.IdCliente);
					clienteAConsultarHash.put(cli.IdCliente, cli);
				}
			}
			
			params.clear();
			params.put("IdCliente", idsCliente);
			List<ClientePortafolio> clientePortafolioList = sql.query(con, SQL_SELECT_PORTAFOLIOS_POR_CLIENTE, params, ClientePortafolio.class);
			for (ClientePortafolio clientePortafolio : clientePortafolioList) {
				PortafolioAConsultar portafolioAConsultar = new PortafolioAConsultar();
				portafolioAConsultar.IdPortafolio = clientePortafolio.IdPortafolio;
				portafolioAConsultar.IdMoneda = clienteAConsultarHash.get(clientePortafolio.IdCliente) != null ? ((ClienteAConsultar) clienteAConsultarHash.get(clientePortafolio.IdCliente)).IdMoneda : null;
				entrada.p_PortafolioAConsultar.add(portafolioAConsultar);
			}
		}
		
		HashMap<Long, Boolean> idsPortafolioFiltro = new HashMap<Long, Boolean>();
		HashMap<String, Boolean> idsMonedaFiltro = new HashMap<String, Boolean>();
		List<Long> idsPortafolio = new ArrayList<Long>();
		List<String> idsMoneda = new ArrayList<String>();
		for(PortafolioAConsultar port : entrada.p_PortafolioAConsultar) {
			if (!idsPortafolioFiltro.containsKey(port.IdPortafolio)) {
				idsPortafolioFiltro.put(port.IdPortafolio, true);
				idsPortafolio.add(port.IdPortafolio);
			}
			if (port.IdMoneda != null && !idsMonedaFiltro.containsKey(port.IdMoneda)) {
				idsMonedaFiltro.put(port.IdMoneda, true);
				idsMoneda.add(port.IdMoneda);
			}
		}
		
		List<ObjPORTAFOLIOACONSULTAR> portafolioAConsultar = new ArrayList<ObjPORTAFOLIOACONSULTAR>();
		if (idsPortafolio.size() > 0) {
			logger.debug("Inicio SQL_SELECT_PORTAFOLIOS_POR_CUENTA_INVERSION : SQL_SELECT_PORTAFOLIOS_POR_MONEDA");
			startTime = System.nanoTime();
			
			params.clear();
			params.put("FiltroMonedaValido", 0);
			if (idsMoneda.size() == 0) {
				idsMoneda.add(""); 
				params.put("FiltroMonedaValido", 1);
			}
			
			//insertar los portafolios a consultar
			sql.batch(con, SQL_INSERT_PORTAFOLIOS_TEMPORAL, entrada.p_PortafolioAConsultar, PortafolioAConsultar.class);
			params.put("IdMoneda", idsMoneda);
			
			portafolioAConsultar.addAll(sql.query(con, "CUENTA_INVERSION".equals(entrada.p_Uso) ? SQL_SELECT_PORTAFOLIOS_POR_CUENTA_INVERSION : SQL_SELECT_PORTAFOLIOS_POR_MONEDA, params, ObjPORTAFOLIOACONSULTAR.class));
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_PORTAFOLIOS_POR_CUENTA_INVERSION : SQL_SELECT_PORTAFOLIOS_POR_MONEDA");
		}
		
		HashMap<String, ObjPORTAFOLIOACONSULTAR> portafolioAConsultarHash = new HashMap<String, ObjPORTAFOLIOACONSULTAR>();
		for(ObjPORTAFOLIOACONSULTAR portafolio : portafolioAConsultar) {
			if (!portafolioAConsultarHash.containsKey(portafolio.IdPortafolio+"_"+portafolio.IdMoneda)) {
				portafolioAConsultarHash.put(portafolio.IdPortafolio+"_"+portafolio.IdMoneda, portafolio);
			}
		}
		
		ServiceEntrada serviceEntrada = new ServiceEntrada();
		serviceEntrada.P_IDEMPRESA = entrada.p_IdEmpresa;
		serviceEntrada.P_FECHADESDE = entrada.p_FechaDesde;
		serviceEntrada.P_FECHAHASTA = entrada.p_FechaHasta;
		serviceEntrada.P_USO = entrada.p_Uso;
		serviceEntrada.P_DESPLEGARENUI = entrada.p_DesplegarEnUI;
		serviceEntrada.P_AGRUPARPORORIGEN = entrada.p_AgruparPorOrigen;
		
		for(PortafolioAConsultar port : entrada.p_PortafolioAConsultar) {
			if(port.IdMoneda == null) {
				serviceEntrada.P_PORTAFOLIOACONSULTAR.addAll(portafolioAConsultar);
				break;
			}
			else {
				if (portafolioAConsultarHash.get(port.IdPortafolio+"_"+port.IdMoneda) != null) {
					serviceEntrada.P_PORTAFOLIOACONSULTAR.add(portafolioAConsultarHash.get(port.IdPortafolio+"_"+port.IdMoneda));
				}
			}
		}
		//logger.debug("serviceEntrada : " + serviceEntrada.toString());
		SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
		ServiceSalida serviceSalida = saldoFlujoCaja.logica(con, serviceEntrada);
		out.P_OUT_ERRORESCONTADOR = serviceSalida.P_OUT_ERRORESCONTADOR;
		out.P_OUT_FLUJOCAJA = serviceSalida.P_OUT_FLUJOCAJA;
		out.P_OUT_FLUJOCAJADETALLADO = serviceSalida.P_OUT_FLUJOCAJADETALLADO;
		
		for(ObjERRORESDETALLE err : serviceSalida.P_OUT_ERRORESDETALLE) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,err.Codigo,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_TEMPORAL);
		logger.info("Termino del servicio ConsultarFlujoCaja, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":null,\"p_FechaDesde\":\"2018-10-22\",\"p_FechaHasta\":\"2018-10-22\",\"p_Uso\":\"ORDENES_DE_COMPRA\",\"p_DesplegarEnUI\":null,\"p_AgruparPorOrigen\":null,\"p_PortafolioAConsultar\":[{\"IdPortafolio\":343699,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":343691,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":153967,\"IdMoneda\":\"CLP\"}],\"p_ClienteAConsultar\":[]}";
			//String jsonMBPOR = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":282,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":null,\"p_FechaDesde\":\"2019-03-27\",\"p_FechaHasta\":\"2019-03-27\",\"p_Uso\":\"ORDENES_DE_COMPRA\",\"p_DesplegarEnUI\":null,\"p_AgruparPorOrigen\":null,\"p_PortafolioAConsultar\":[{\"IdPortafolio\":19110,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":21809,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":10841,\"IdMoneda\":\"CLP\"}],\"p_ClienteAConsultar\":[]}";
			//String jsonMBCLI = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":377,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaDesde\":\"2019-02-15\",\"p_FechaHasta\":\"2019-02-15\",\"p_Uso\":null,\"p_DesplegarEnUI\":\"S\",\"p_AgruparPorOrigen\":\"S\",\"p_PortafolioAConsultar\":[],\"p_ClienteAConsultar\":[{\"IdCliente\":18139,\"IdMoneda\":\"CLP\"},{\"IdCliente\":18145}]}";
			
			EntradaConsultarFlujoCaja Objin = gson.fromJson(jsonDE, EntradaConsultarFlujoCaja.class);
			ConsultarFlujoCaja  demo=new ConsultarFlujoCaja();				
			try {
				SalidaConsultarFlujoCaja ObjOut=demo.logica(con, Objin);
				//con.commit();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();
			}
			finally {
				con.rollback();
				con.close();
			}
    	}
		System.out.println("== END ==");
	}

}
