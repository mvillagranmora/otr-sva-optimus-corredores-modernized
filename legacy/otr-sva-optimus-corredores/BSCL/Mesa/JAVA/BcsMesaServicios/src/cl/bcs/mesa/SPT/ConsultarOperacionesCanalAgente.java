package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarOperacionesCanalAgente implements JavaCall {
	
	private final String SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_InsertIdPortafolioTablaPaso.sql");
	private final String SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_InsertFullIdPortafolioTablaPaso.sql");
	
	private final String SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_DeleteIdPortafolioTablaPaso.sql");
	private final String SQL_SELECT_OPERACIONES = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_SelectOperaciones.sql");
	
	static final Logger logger = LogManager.getLogger(ConsultarOperacionesCanalAgente.class.getName());
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarOperacionesCanalAgente in = gson.fromJson(jin.toString(), EntradaConsultarOperacionesCanalAgente.class);
		SalidaConsultarOperacionesCanalAgente out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
		public Portafolio(Long idPortafolio, Long IdTrabajo) {
			this.IdPortafolio = idPortafolio;
			this.IdTrabajo = IdTrabajo;
		}
		public Portafolio() {}
	}
	
	public static class EntradaConsultarOperacionesCanalAgente{
		//Parametros Fijos
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		
		//cliente
		public Long		p_IdPortafolio;
		public Long 	p_IdCliente;
		public Long 	p_Producto;
		
		//filtros
		public String p_Mesa;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_IdTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultar{
		//lista resultado consulta
		public Long IdCartera;
		public Long IdOperacion;
		public Long IdOperacionUI;
		public Long FolioOperacion;
		public Timestamp FechaOperacion;
		public Date FechaLiquidacion;
		public Date FechaEntregamos;
		public Date FechaRecibimos;
		public String HoraOperacion;
		public Timestamp FechaOperacionOPE;
		public String NombreInstrumento;
		public String Nemotecnico;
		public String IdTipoOperacion;
		public String TipoOperacion;
		public String IdTipoInstrumento;
		public String RutCliente;
		public String NombreCliente;
		public String CodigoPropositoPortafolio;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal ResultadoMonedaLocalPuntas;
		public BigDecimal ResultadoMonedaLocalBidAsk;
		public BigDecimal ResultadoMonedaLocalDO;
		public BigDecimal ResultadoMonedaLocalTCSpot;
		public BigDecimal ResultadoMonedaLocalTCCierreMesa;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal Margen;
		public BigDecimal Utilidad;
		public BigDecimal MontoDiferido;
		public Long Modalidad;
		public String ModalidadLiquidacion;
		public String ModalidadLiquidacionValor;
		public Long Plazo;
		public Integer PlazoResidual;
		public Timestamp FechaVencimiento;
		public Date FechaEfectiva;
		public Long Agente;
		public String NombreAgente;
		public String OperacionFicticia;
		public String CodigoUsuario;
		public String Estado;
		public String Mesa;
		public String IdTipoCanalIngreso;
		public BigDecimal INGCantidad;
		public BigDecimal INGMontoMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public String Accion;
		public Long IdOperacionSpotReferencia;
		public String EstadoConfirmacion;
		public Long FolioOrigenFW;
		public Long IdOperacionSpotOrigen;
		public Long IdOperacionSpotVigente;
		public String IdEstado;
		public String TipoVencimiento;
		public Long IdSolicitudAporteRescate;
		public String EstadoAporteRescate;
		public String TipoAporteRescate;
		public Date FechaLiquidacionAporteRescate;
		public Long IdPortafolio;
		public Long CodigoComercio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarOperacionesCanalAgente{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<SalidaConsultar> P_OUT_OPERACIONES;
		
		public SalidaConsultarOperacionesCanalAgente() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_OPERACIONES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	private void insertTablaPaso(Connection conn, EntradaConsultarOperacionesCanalAgente entrada, Long i_idTrabajo) throws Exception {
		SQL _sql = new SQL();

		List<Long> listIdPortafolio = new ArrayList<Long>();
		logger.debug("INICIO PORTAFOLIO EJECUTIVO");
		try {
			if(entrada.p_IdPortafolio != 0) 
			{
				listIdPortafolio.add(entrada.p_IdPortafolio);
				logger.debug("INSERT TABLA DE PASO");
				//se utiliza una tabla de paso para guardar idsPortafolio
				List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
					Portafolio portafolio = new Portafolio();
					portafolio.IdPortafolio = id;
					portafolio.IdTrabajo = i_idTrabajo;
					return portafolio;
				}).collect(Collectors.toList());
				
				_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
			}
			else 
			{
				//Obtenemos los portafolios del ejecutivo
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
				boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
				if (restringeCartera) {
					listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
					logger.debug("INSERT TABLA DE PASO");
					//se utiliza una tabla de paso para guardar idsPortafolio
					List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
						Portafolio portafolio = new Portafolio();
						portafolio.IdPortafolio = id;
						portafolio.IdTrabajo = i_idTrabajo;
						return portafolio;
					}).collect(Collectors.toList());
					
					_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
				}
				else {
					logger.debug("No restringe cartera");
					
					Portafolio portafolio = new Portafolio();
					portafolio.IdPortafolio = null;
					portafolio.IdTrabajo = i_idTrabajo;
					List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
					idsPortafolioTablaPaso.add(portafolio);
					
					_sql.batch(conn, SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
					
				}
				
			}
		}
		catch (Exception e) {
			logger.debug("ERROR INSERT TABLA DE PASO - " + e.getMessage());
			throw e;
		}
		logger.debug("FIN PORTAFOLIO EJECUTIVO");
		
	}
	
	
	@SuppressWarnings("deprecation")
	public SalidaConsultarOperacionesCanalAgente logica(Connection conn, EntradaConsultarOperacionesCanalAgente entrada) throws Exception{
		SalidaConsultarOperacionesCanalAgente salida = new SalidaConsultarOperacionesCanalAgente();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		logger.info("INICIO CONSULTA MONEDA EXTRANJERA");
		SQL _sql = new SQL();
		
		//ingresa portafolios a tabla de paso de cliente correspondientes al ejecutivo 
		insertTablaPaso(conn, entrada, idTrabajo); 
		
		try {
			//logica consulta
			int varMesa = 0;
			int varIdTipoOperacion = 1;
			
			if (entrada.p_Mesa.equalsIgnoreCase("0")) {
				entrada.p_Mesa = "";
				varMesa = 1;
			}
			
			if (entrada.p_IdTipoOperacion.equalsIgnoreCase("") || !entrada.p_IdTipoOperacion.equalsIgnoreCase("COMPRA") || !entrada.p_IdTipoOperacion.equalsIgnoreCase("VENTA")) {
				entrada.p_IdTipoOperacion = "";
				varIdTipoOperacion = 1;
			}
			
			HashMap<String,Object> params =  new HashMap<>();
			params.put("IdTrabajo", idTrabajo);
			params.put("Mesa", entrada.p_Mesa);
			params.put("var_Mesa", varMesa);
			params.put("FechaDesde", entrada.p_FechaDesde);
			params.put("FechaHasta", entrada.p_FechaHasta);
			params.put("IdTipoOperacion", entrada.p_IdTipoOperacion);
			params.put("var_IdTipoOperacion", varIdTipoOperacion);
			
					
			salida.P_OUT_OPERACIONES = _sql.query(conn, SQL_SELECT_OPERACIONES, params, SalidaConsultar.class);
			
		}
		catch(Exception e) {
			logger.error(e);
			throw e;
		}
		finally {
			Portafolio portafolio = new Portafolio();
			portafolio.IdPortafolio = null;
			portafolio.IdTrabajo = idTrabajo;
			List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
			idsPortafolioTablaPaso.add(portafolio);
			
			_sql.batch(conn, SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
			
		}
				
		logger.info("FIN CONSULTA MONEDA EXTRANJERA");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();			
			//body:{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"MSP","p_IdEmpleadoConsulta":7889,"p_IdPortafolio":0,"p_IdCliente":0,"p_Producto":"0","p_Mesa":"MEME","p_FechaDesde":"2021-04-13T00:00:00","p_FechaHasta":"2021-04-13T23:59:59","p_IdTipoOperacion":"0"}
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":7305,\"p_IdPortafolio\":0,\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-04-13T23:59:59\",\"p_IdTipoOperacion\":\"COMPRA\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"MSP\",\"p_IdEmpleadoConsulta\":7889,\"p_IdPortafolio\":0,\"p_IdCliente\":0,\"p_Producto\":\"0\",\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-09-30T23:59:59\",\"p_IdTipoOperacion\":\"0\"}";
			EntradaConsultarOperacionesCanalAgente Objin = gson.fromJson(json, EntradaConsultarOperacionesCanalAgente.class);
			ConsultarOperacionesCanalAgente demo = new ConsultarOperacionesCanalAgente();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				SalidaConsultarOperacionesCanalAgente ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA:" + ObjOut.P_OUT_OPERACIONES.size());
				
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				//System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	

}
