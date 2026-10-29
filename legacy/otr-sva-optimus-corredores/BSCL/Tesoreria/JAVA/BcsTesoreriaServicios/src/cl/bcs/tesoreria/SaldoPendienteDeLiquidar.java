package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class SaldoPendienteDeLiquidar implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(SaldoPendienteDeLiquidar.class);
	
	private final String SQL_SELECT_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_SelectClientePortafolio.sql");
	private final String SQL_INSERT_PERSONAS = "INSERT INTO \"BSCL.Tesoreria.BD::TES.SaldoPendienteDeLiquidar_Persona_Proceso\"(\"IdPersona\") VALUES(:IdPersona)";
	private final String SQL_DELETE_PERSONAS = "DELETE FROM \"BSCL.Tesoreria.BD::TES.SaldoPendienteDeLiquidar_Persona_Proceso\" WHERE 1=1";
	
	private final String SQL_SELECT_SALDO_ANTERIOR_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_SelectSaldoAnteriorMovimientos.sql");
	private final String SQL_SELECT_SALDO_ANTERIOR_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_SelectSaldoAnteriorComprobantes.sql");
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_SelectMovimientos.sql");
	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_SelectComprobantes.sql");
	private final String SQL_INSERT_SALDO_TABLA_PASO = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_InsertSaldoTablaPaso.sql");
	private final String SQL_DELETE_SALDO_TABLA_PASO = loadResourceToString("cl/bcs/tesoreria/sql/SaldoPendienteDeLiquidar_DeleteSaldoTablaPaso.sql");
	
	
	public static class EntradaSaldoPendienteDeLiquidar{
		public Long p_IdEmpresa;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public List<SocioConsulta> p_SociosAConsultar;
		
		public EntradaSaldoPendienteDeLiquidar(){
			this.p_SociosAConsultar = new ArrayList<SocioConsulta>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SocioConsulta{
		public Long IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaSaldoPendienteDeLiquidar{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<SaldoPorFechaLiquidacion> P_OUT_SALDOPORFECHALIQUIDACION;
		
		public SalidaSaldoPendienteDeLiquidar(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SALDOPORFECHALIQUIDACION = new ArrayList<SaldoPorFechaLiquidacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoPorFechaLiquidacion{
		public Long IdPersona;
		public Long IdCliente;
     	public Long IdPortafolio;
     	public String FechaSaldo;
     	public String IdMoneda;
     	public BigDecimal SaldoMovIng;
     	public BigDecimal SaldoMovEgr;
     	public BigDecimal Saldo;
     	
     	public SaldoPorFechaLiquidacion(){
     		this.SaldoMovIng = BigDecimal.ZERO;
     		this.SaldoMovEgr = BigDecimal.ZERO;
     		this.Saldo = BigDecimal.ZERO;
     	}
     	
     	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoFecha{
		public String Id;
		public Long IdPersona;
 	  	public Long IdCliente;
 	  	public Long IdPortafolio;
	  	public String IdMoneda;
	  	public Date Fecha;

	  	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoAnterior{
		public String Id;
		public Long IdPersona;
 	  	public Long IdPortafolio;
	  	public String IdMoneda;
	  	public BigDecimal TotalMontoIngresoAnterior;
	  	public BigDecimal TotalMontoEgresoAnterior;
	  	public BigDecimal TotalMontoAnterior;
	  	
	  	public SaldoAnterior(){
	  		this.TotalMontoIngresoAnterior = BigDecimal.ZERO;
	  		this.TotalMontoEgresoAnterior = BigDecimal.ZERO;
	  		this.TotalMontoAnterior = BigDecimal.ZERO;
	  	}
	  	
	  	public String toString() {
			return Utils.printJson(this, false);
	  	}
	}
	
	public static class SocioProceso{
		public String Id;
		public Long IdPersona;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		
		public SocioProceso() {
			
		}
		
		public SocioProceso(long idPersona, long idCliente, long idPortafolio, String idMoneda) {
			this.IdPersona = idPersona;
			this.IdCliente = idCliente;
			this.IdPortafolio = idPortafolio;
			this.IdMoneda = idMoneda;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoTesoreria{
		public String Id;
		public Date FechaLiquidacion;
		public Date FechaMovimiento;
		public Long IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal MontoMOVING;
		public BigDecimal MontoMOVEGR;
		public BigDecimal Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaSaldoPendienteDeLiquidar jin = gson.fromJson(in.toString(), EntradaSaldoPendienteDeLiquidar.class);
		SalidaSaldoPendienteDeLiquidar out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public SalidaSaldoPendienteDeLiquidar logica (Connection con, EntradaSaldoPendienteDeLiquidar entrada) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio SaldoPendienteDeLiquidar");
		
		SQL sql=new SQL();
		SalidaSaldoPendienteDeLiquidar out = new SalidaSaldoPendienteDeLiquidar();
		
		try {
			Date fDesde = Fecha.toDate(entrada.p_FechaDesde);
			Date fHasta = Fecha.toDate(entrada.p_FechaHasta);
			
			HashMap<Long, Boolean> idPersonasFiltro = new HashMap<Long, Boolean>();
			Set<Long> idPersonas = new HashSet<Long>();
			
			for(SocioConsulta socio : entrada.p_SociosAConsultar){
				if (!idPersonasFiltro.containsKey(socio.IdPersona)) {
					idPersonasFiltro.put(socio.IdPersona, true);
				}
			}
			
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			//insertar en tabla temporal los id de persona.
			sql.batch(con, SQL_INSERT_PERSONAS, entrada.p_SociosAConsultar.parallelStream().filter(soc -> idPersonas.add(soc.IdPersona)).collect(Collectors.toList()), SocioConsulta.class);
			idPersonas.clear();
			logger.debug("Inicio SQL_SELECT_CLIENTE_PORTAFOLIO ->To hashmap");
			startTime = System.nanoTime();
			List<SocioProceso> sociosProcesoPendiente = new ArrayList<>();//sql.query(con, SQL_SELECT_CLIENTE_PORTAFOLIO, params, SocioProceso.class);
			HashMap<String, SocioProceso> sociosProcesoPendienteHash = new HashMap<String, SocioProceso>();
			try(PreparedStatement ps = con.prepareStatement(SQL_SELECT_CLIENTE_PORTAFOLIO)){
				try(ResultSet rs = ps.executeQuery()){
					while(rs!=null && rs.next()) {
						SocioProceso soc = new SocioProceso(rs.getLong("IdPersona"), rs.getLong("IdCliente"), rs.getLong("IdPortafolio"), rs.getString("IdMoneda"));
						soc.Id = soc.IdPersona+"_"+soc.IdPortafolio+"_"+soc.IdMoneda;
						
						if (!sociosProcesoPendienteHash.containsKey(soc.Id)) {
							sociosProcesoPendienteHash.put(soc.Id, soc);
						}
						
					}
				}
			}
			SQL.exec(con, SQL_DELETE_PERSONAS);
			
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CLIENTE_PORTAFOLIO ->To hashmap, COUNT: " + sociosProcesoPendienteHash.size());//la lista da 7.339.806 registros
			
			logger.debug("Inicio Obtenemos todos los socios que debemos buscar su saldo");
			List<SocioProceso> sociosProceso = new ArrayList<SocioProceso>();
			idPersonasFiltro.clear();
			HashMap<Long, Boolean> idPortafoliosFiltro = new HashMap<Long, Boolean>();
			HashMap<String, Boolean> idMonedasFiltro = new HashMap<String, Boolean>();
			
			List<Long> idPortafolios = new ArrayList<Long>();
			List<String> idMonedas = new ArrayList<String>();
			for(SocioConsulta socio : entrada.p_SociosAConsultar){
				if (socio.IdPortafolio != null && socio.IdMoneda != null) {
					SocioProceso sp = sociosProcesoPendienteHash.get(socio.IdPersona+"_"+socio.IdPortafolio+"_"+socio.IdMoneda);
					if (sp != null) {
						if (!idPersonasFiltro.containsKey(sp.IdPersona) || !idPortafoliosFiltro.containsKey(sp.IdPortafolio) || !idMonedasFiltro.containsKey(sp.IdMoneda)) {
							sociosProceso.add(sp);
						}
						if(!idPersonasFiltro.containsKey(sp.IdPersona)) {
							idPersonasFiltro.put(sp.IdPersona, true);
							idPersonas.add(sp.IdPersona);
						}
						if(!idPortafoliosFiltro.containsKey(sp.IdPortafolio)) {
							idPortafoliosFiltro.put(sp.IdPortafolio, true);
							idPortafolios.add(sp.IdPortafolio);
						}
						if(!idMonedasFiltro.containsKey(sp.IdMoneda)) {
							idMonedasFiltro.put(sp.IdMoneda, true);
							idMonedas.add(sp.IdMoneda);
						}
					}
				}
				else {
					List<SocioProceso> sociosProcesoPendienteFilter = sociosProcesoPendiente.stream()
							.filter(s -> socio.IdPersona.equals(s.IdPersona) && 
										(socio.IdPortafolio == null || socio.IdPortafolio.equals(s.IdPortafolio)) && 
										(socio.IdMoneda == null || socio.IdMoneda.equals(s.IdMoneda)))
							.collect(Collectors.toList());
					for(SocioProceso sp : sociosProcesoPendienteFilter){
						if (!idPersonasFiltro.containsKey(socio.IdPersona) || !idPortafoliosFiltro.containsKey(socio.IdPortafolio) || !idMonedasFiltro.containsKey(socio.IdMoneda)) {
							sociosProceso.add(sp);
							if(!idPersonasFiltro.containsKey(sp.IdPersona)) {
								idPersonasFiltro.put(sp.IdPersona, true);
								idPersonas.add(sp.IdPersona);
							}
							if(!idPortafoliosFiltro.containsKey(sp.IdPortafolio)) {
								idPortafoliosFiltro.put(sp.IdPortafolio, true);
								idPortafolios.add(sp.IdPortafolio);
							}
							if(!idMonedasFiltro.containsKey(sp.IdMoneda)) {
								idMonedasFiltro.put(sp.IdMoneda, true);
								idMonedas.add(sp.IdMoneda);
							}
						}
					}
				}
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin Obtenemos todos los socios que debemos buscar su saldo");
			
			/*Completar rango fecha*/
			List<SaldoFecha> saldosFecha = new ArrayList<SaldoFecha>();
			for(SocioProceso socio : sociosProceso){
				Date fDesdePeriodo = Fecha.toDate(entrada.p_FechaDesde);
				while(fDesdePeriodo.compareTo(fHasta) != 1){
					SaldoFecha saldoFecha = new SaldoFecha();
					//saldoFecha = Utils.copyPojo(socio, saldoFecha);
					saldoFecha.Id = socio.Id;
					saldoFecha.IdPersona = socio.IdPersona;
					saldoFecha.IdCliente = socio.IdCliente;
					saldoFecha.IdPortafolio = socio.IdPortafolio;
					saldoFecha.IdMoneda = socio.IdMoneda;
					//saldoFecha.Fecha = socio.Fecha;
					saldoFecha.Fecha = fDesdePeriodo;
					saldosFecha.add(saldoFecha);
					fDesdePeriodo = Fecha.addDays(fDesdePeriodo, 1);
				}
			}
			
			//Insert a tabla de paso para usar en las querys siguientes
			sql.batch(con, SQL_INSERT_SALDO_TABLA_PASO, sociosProceso, SocioProceso.class);
			
			params.clear();
			/*params.put("IdPersona", idPersonas);
			params.put("IdMoneda",idMonedas);
			params.put("IdPortafolio",idPortafolios);*/
			params.put("FechaDesde", entrada.p_FechaDesde);
			logger.debug("Inicio SQL_SELECT_SALDO_ANTERIOR_MOVIMIENTOS");
			startTime = System.nanoTime();
			List<SaldoAnterior> saldosAnteriorTesoreria = sql.query(con, SQL_SELECT_SALDO_ANTERIOR_MOVIMIENTOS, params, SaldoAnterior.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_SALDO_ANTERIOR_MOVIMIENTOS");
			//HashMap<String, Object> hSaldoAnteriorTesoreria = Utils.List2Hash(saldosAnteriorTesoreria, "Id");
			HashMap<String, SaldoAnterior> hSaldoAnteriorTesoreria = new HashMap<String, SaldoAnterior>();
			for (SaldoAnterior saldoAnterior : saldosAnteriorTesoreria) {
				saldoAnterior.Id = saldoAnterior.IdPersona+"_"+saldoAnterior.IdPortafolio+"_"+saldoAnterior.IdMoneda;
				
				if (!hSaldoAnteriorTesoreria.containsKey(saldoAnterior.Id)) {
					hSaldoAnteriorTesoreria.put(saldoAnterior.Id, saldoAnterior);
				}
			}
			
			logger.debug("Inicio SQL_SELECT_SALDO_ANTERIOR_COMPROBANTES");
			startTime = System.nanoTime();
			List<SaldoAnterior> saldosAnteriorComprobante = sql.query(con, SQL_SELECT_SALDO_ANTERIOR_COMPROBANTES, params, SaldoAnterior.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_SALDO_ANTERIOR_COMPROBANTES");
			//HashMap<String, Object> hSaldoAnteriorComprobante = Utils.List2Hash(saldosAnteriorComprobante, "Id");
			HashMap<String, SaldoAnterior> hSaldoAnteriorComprobante = new HashMap<String, SaldoAnterior>();
			for (SaldoAnterior saldoAnterior : saldosAnteriorComprobante) {
				saldoAnterior.Id = saldoAnterior.IdPersona+"_"+saldoAnterior.IdPortafolio+"_"+saldoAnterior.IdMoneda;
				
				if (!hSaldoAnteriorComprobante.containsKey(saldoAnterior.Id)) {
					hSaldoAnteriorComprobante.put(saldoAnterior.Id, saldoAnterior);
				}
			}
			
			params.put("FechaHasta", entrada.p_FechaHasta);
			logger.debug("Inicio SQL_SELECT_MOVIMIENTOS");
			startTime = System.nanoTime();
			List<MovimientoTesoreria> movimientosTesoreria = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, MovimientoTesoreria.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_MOVIMIENTOS");
			HashMap<String, MovimientoTesoreria> movimientosTesoreriaHash = new HashMap<String, MovimientoTesoreria>();
			for (MovimientoTesoreria movimientoTesoreria : movimientosTesoreria) {
				movimientoTesoreria.Id = movimientoTesoreria.IdPersona+"_"+movimientoTesoreria.IdPortafolio+"_"+movimientoTesoreria.IdMoneda;
				
				if (!movimientosTesoreriaHash.containsKey(movimientoTesoreria.Id)) {
					movimientosTesoreriaHash.put(movimientoTesoreria.Id, movimientoTesoreria);
				}
			}
			
			logger.debug("Inicio SQL_SELECT_COMPROBANTES");
			startTime = System.nanoTime();
			List<MovimientoTesoreria> movimientosComprobante = sql.query(con, SQL_SELECT_COMPROBANTES, params, MovimientoTesoreria.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_COMPROBANTES");
			HashMap<String, MovimientoTesoreria> movimientosComprobanteHash = new HashMap<String, MovimientoTesoreria>();
			for (MovimientoTesoreria movimientoTesoreria : movimientosComprobante) {
				movimientoTesoreria.Id = movimientoTesoreria.IdPersona+"_"+movimientoTesoreria.IdPortafolio+"_"+movimientoTesoreria.IdMoneda;
				
				if (!movimientosComprobanteHash.containsKey(movimientoTesoreria.Id)) {
					movimientosComprobanteHash.put(movimientoTesoreria.Id, movimientoTesoreria);
				}
			}
			
			for(SaldoFecha saldo : saldosFecha){
				SaldoPorFechaLiquidacion saldoPorFechaLiquidacion = new SaldoPorFechaLiquidacion();
				//saldoPorFechaLiquidacion = Utils.copyPojo(saldo, saldoPorFechaLiquidacion);
				saldoPorFechaLiquidacion.IdPersona = saldo.IdPersona;
				saldoPorFechaLiquidacion.IdCliente = saldo.IdCliente;
				saldoPorFechaLiquidacion.IdPortafolio = saldo.IdPortafolio;
				//saldoPorFechaLiquidacion.FechaSaldo = saldo.FechaSaldo;
				saldoPorFechaLiquidacion.IdMoneda = saldo.IdMoneda;
				//saldoPorFechaLiquidacion.SaldoMovIng = saldo.SaldoMovIng;
				//saldoPorFechaLiquidacion.SaldoMovEgr = saldo.SaldoMovEgr;
				//saldoPorFechaLiquidacion.Saldo = saldo.Saldo;
				saldoPorFechaLiquidacion.FechaSaldo = Fecha.stringToSqlDate(saldo.Fecha);
				
				/*for(MovimientoTesoreria mov : movimientosTesoreria){
					if(saldo.Id.equals(mov.Id) && mov.FechaMovimiento.compareTo(fDesde) != -1 && mov.FechaMovimiento.compareTo(saldo.Fecha) != 1){
						saldoPorFechaLiquidacion.SaldoMovIng = saldoPorFechaLiquidacion.SaldoMovIng.add(mov.MontoMOVING, MathContext.DECIMAL128);
						saldoPorFechaLiquidacion.SaldoMovEgr = saldoPorFechaLiquidacion.SaldoMovEgr.add(mov.MontoMOVEGR, MathContext.DECIMAL128);
						saldoPorFechaLiquidacion.Saldo = saldoPorFechaLiquidacion.Saldo.add(mov.Monto, MathContext.DECIMAL128);
					}
				}*/
				MovimientoTesoreria movTes = movimientosTesoreriaHash.get(saldo.Id);
				if (movTes != null && movTes.FechaLiquidacion.compareTo(fDesde) != -1 && movTes.FechaLiquidacion.compareTo(saldo.Fecha) != 1) {
					saldoPorFechaLiquidacion.SaldoMovIng = saldoPorFechaLiquidacion.SaldoMovIng.add(movTes.MontoMOVING, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.SaldoMovEgr = saldoPorFechaLiquidacion.SaldoMovEgr.add(movTes.MontoMOVEGR, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.Saldo = saldoPorFechaLiquidacion.Saldo.add(movTes.Monto, MathContext.DECIMAL128);
				}
				
				/*for(MovimientoTesoreria mov : movimientosComprobante){
					if(saldo.Id.equals(mov.Id) && mov.FechaMovimiento.compareTo(fDesde) != -1 && mov.FechaMovimiento.compareTo(saldo.Fecha) != 1){
						saldoPorFechaLiquidacion.SaldoMovIng = saldoPorFechaLiquidacion.SaldoMovIng.add(mov.MontoMOVING, MathContext.DECIMAL128);
						saldoPorFechaLiquidacion.SaldoMovEgr = saldoPorFechaLiquidacion.SaldoMovEgr.add(mov.MontoMOVEGR, MathContext.DECIMAL128);
						saldoPorFechaLiquidacion.Saldo = saldoPorFechaLiquidacion.Saldo.add(mov.Monto, MathContext.DECIMAL128);
					}
				}*/
				
				
				
				MovimientoTesoreria movCom = movimientosComprobanteHash.get(saldo.Id); 
				if (movCom != null && movCom.FechaLiquidacion.compareTo(fDesde) != -1 && movCom.FechaLiquidacion.compareTo(saldo.Fecha) != 1) {
					saldoPorFechaLiquidacion.SaldoMovIng = saldoPorFechaLiquidacion.SaldoMovIng.add(movCom.MontoMOVING, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.SaldoMovEgr = saldoPorFechaLiquidacion.SaldoMovEgr.add(movCom.MontoMOVEGR, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.Saldo = saldoPorFechaLiquidacion.Saldo.add(movCom.Monto, MathContext.DECIMAL128);
				}
				
				SaldoAnterior saldoAnteriorTesoreria = hSaldoAnteriorTesoreria.get(saldo.Id);
				if(saldoAnteriorTesoreria != null){
					saldoPorFechaLiquidacion.SaldoMovIng = saldoPorFechaLiquidacion.SaldoMovIng.add(saldoAnteriorTesoreria.TotalMontoIngresoAnterior, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.SaldoMovEgr = saldoPorFechaLiquidacion.SaldoMovEgr.add(saldoAnteriorTesoreria.TotalMontoEgresoAnterior, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.Saldo = saldoPorFechaLiquidacion.Saldo.add(saldoAnteriorTesoreria.TotalMontoAnterior, MathContext.DECIMAL128);
				}
				
				SaldoAnterior saldoAnteriorComprobante = (SaldoAnterior) hSaldoAnteriorComprobante.get(saldo.Id);
				if(saldoAnteriorComprobante != null){
					saldoPorFechaLiquidacion.SaldoMovIng = saldoPorFechaLiquidacion.SaldoMovIng.add(saldoAnteriorComprobante.TotalMontoIngresoAnterior, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.SaldoMovEgr = saldoPorFechaLiquidacion.SaldoMovEgr.add(saldoAnteriorComprobante.TotalMontoEgresoAnterior, MathContext.DECIMAL128);
					saldoPorFechaLiquidacion.Saldo = saldoPorFechaLiquidacion.Saldo.add(saldoAnteriorComprobante.TotalMontoAnterior, MathContext.DECIMAL128);
				}
				
				out.P_OUT_SALDOPORFECHALIQUIDACION.add(saldoPorFechaLiquidacion);
			}
			
			SQL.exec(con, SQL_DELETE_SALDO_TABLA_PASO);
		}
		catch(Exception e){
		    logger.error("Error",e);
			throw e;
		}
		
		logger.info("Termino del servicio SaldoPendienteDeLiquidar, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\"p_IdEmpresa\":1,\"p_FechaDesde\":\"2019-03-27\",\"p_FechaHasta\":\"2019-03-27\",\"p_SociosAConsultar\":[{\"IdPersona\":9074,\"IdPortafolio\":19110,\"IdMoneda\":\"CLP\"},{\"IdPersona\":127,\"IdPortafolio\":21809,\"IdMoneda\":\"CLP\"},{\"IdPersona\":9093,\"IdPortafolio\":10841,\"IdMoneda\":\"CLP\"}]}";
			EntradaSaldoPendienteDeLiquidar Objin = gson.fromJson(json, EntradaSaldoPendienteDeLiquidar.class);
			SaldoPendienteDeLiquidar  demo= new SaldoPendienteDeLiquidar();				
			try {
				SalidaSaldoPendienteDeLiquidar ObjOut=demo.logica(con, Objin);
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
