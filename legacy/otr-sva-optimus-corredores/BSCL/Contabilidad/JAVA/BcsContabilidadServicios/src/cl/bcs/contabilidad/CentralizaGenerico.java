package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.pojo.TabDatosEvento;
import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaGenerico implements JavaCall{
	
	private final Logger log = LogManager.getLogger(CentralizaGenerico.class);
	
	private final String SQL_SELECT_TIPO_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectTipoCentralizacion.sql");
	private final String SQL_SELECT_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectCentralizacion.sql");
	private final String SQL_SELECT_MOVIMIENTO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectMovimiento.sql");
	private final String SQL_SELECT_EVENTO_TIPO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectEventoTipo.sql");
	private final String SQL_SELECT_EVENTO_TIPO_DATO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectEventoTipoDato.sql");
	private final String SQL_SELECT_EVENTO_TIPO_DATO_VALOR = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectEventoTipoDatoValor.sql");
	
	private final String SQL_INSERT_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertCentralizacion.sql");
	private final String SQL_INSERT_MOVIMIENTO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertMovimiento.sql");
	private final String SQL_INSERT_MOVIMIENTO_DATO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertMovimientoDato.sql");
	private final String SQL_INSERT_EVENTO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertEvento.sql");
	private final String SQL_INSERT_LOG = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertLog.sql");
	private final String SQL_INSERT_EVENTO_TIPO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertEventoTipo.sql");
	private final String SQL_INSERT_MOVIMIENTO_PASO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertMovimientoPaso.sql");
	private final String SQL_INSERT_DATO_VALOR_PASO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertDatoValorPaso.sql");
	
	public static class EntradaCentralizaGenerico{
		public Long p_IdEmpresa;
		public List<InIdTipoCentralizacion>	p_IdTipoCentralizacion;
		public Long p_Ejercicio;
		public String p_FechaContable;
		public String p_CodigoUsuario;
		List<TabMovimientos> p_TabMovimientos;
		List<TabEventos> p_TabEventos;
		List<TabDatosMovimiento> p_TabDatosMovimiento;
		List<TabDatosEvento> p_TabDatosEvento;
		
		public EntradaCentralizaGenerico(){
			this.p_IdTipoCentralizacion = new ArrayList<InIdTipoCentralizacion>();
			this.p_TabMovimientos = new ArrayList<TabMovimientos>();
			this.p_TabEventos = new ArrayList<TabEventos>();
			this.p_TabDatosMovimiento = new ArrayList<TabDatosMovimiento>();
			this.p_TabDatosEvento = new ArrayList<TabDatosEvento>();
		}
	}
	
	public static class InIdTipoCentralizacion{
		public Long	IdTipoCentralizacion;
		public String Nombre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class SalidaCentralizaGenerico{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCentralizaGenerico(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoCentralizacion{
		public Long IdTipoCentralizacion;
		public String Nombre;
		public String TipoEnvio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Centralizacion{
		public Long IdTipoCentralizacion;
		public Long IdCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento{
		public Long IdEmpresa;
		public String Fecha;
		public String IdTipoMovimiento;
		public Long Numero;
		public String DocumentoIdentidad;
		public Long Cuenta;
		public Long IdTipoCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoProceso{
		public TabMovimientos Movimiento;
		public List<TabEventos> Eventos;
		public List<TabDatosMovimiento> DatosMovimiento;
		public List<TabDatosEvento> DatosEvento;
		
		public MovimientoProceso(){
			this.Eventos = new ArrayList<TabEventos>();
			this.DatosMovimiento = new ArrayList<TabDatosMovimiento>();
			this.DatosEvento = new ArrayList<TabDatosEvento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoTipoDatoValor{
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Estado;
	}
	
	public static class InsertCentralizacion{
		public Long IdCentralizacion;  
		public Long IdTipoCentralizacion;
		public Long IdEmpresa;
		public String FechaProceso;
		public String HoraProceso;
		public String FechaContable;
		public String Glosa;
		public String CodigoUsuario;
		public String TipoGeneracion;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertMovimiento{
		public Long IdMovimiento;
		public String IdTipoMovimiento;
		public Long IdEmpresa;
		public Long Numero;
		public Long Linea;
		public String Fecha;
		public String DocumentoIdentidad;
		public Long Cuenta;
		public Long Ejercicio;
		public String Periodo;
		public Long IdCentralizacion;
		public String IdMoneda;
		public BigDecimal Monto;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertMovimientoDato{
		public Long IdMovimiento;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Estado;
		public String TipoEvento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertEvento{
		public Long IdEmpresa;
		public Long IdEvento;
		public String IdTipoEvento;
		public Long IdCentralizacion;
		public Long IdMovimiento;
		public String FechaContable;
		public BigDecimal Monto;
		public String CodigoMoneda;
		public String CodigoUsuario;
		public String FechaProceso;
		public String HoraProceso;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class EventoTipoDato{
		
		public String IdTipoDatoEvento;
		public String TipoDato;
		public String DatosPreDefinidos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoTipo{
		
		public String IdTipoEvento;
		public String Nombre;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertLog{
		public Long IdLog;
		public Long IdTipoLog;
		public Long IdEmpresa;
		public Long Ejercicio;
		public Long IdTipoProceso;
		public String Fecha;
		public String Hora;
		public String CodigoUsuario;
		public String Glosa;
		public Long IdCentralizacion;
		public Long IdMovimiento;
		public Long IdEvento;
		public Long IdDefinicionAsiento;
		public Long IdPreComprobante;
		public Long IdPreComprobanteAsiento;
		public String IdCuentaContable;
		public Long IdCondicion;
		public Long IdTipoContabilidad;
		public String IdTipoEvento;
		public String IdTipoDatoEvento;
		public Long IdTipoCentralizacion;
		public String IdTipoMovimiento;
		public Long NumeroMovimiento;
		public String FechaMovimiento;
		public String RutMovimiento;
		public Long CuentaMovimiento;
		public String IndicadorError;
		public String Estado;
		
		public InsertLog(){
			this.IdEmpresa = 0L;
			this.Fecha = "";
			this.Hora = "";
			this.CodigoUsuario = "";
			this.Glosa = "";
			this.IdCuentaContable = "";
			this.IdTipoDatoEvento = "";
			this.FechaMovimiento = "";
			this.RutMovimiento = "";
			this.IndicadorError = "";
			this.Estado = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoPaso{
		public Long IdEmpresa;
		public String Fecha;
		public String IdTipoMovimiento;
		public Long Numero;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorDatoPaso{
		public String TipoDato;
		public String ValorVarChar;
		public BigDecimal ValorDecimal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCentralizaGenerico in = gson.fromJson(jin.toString(), EntradaCentralizaGenerico.class);
		SalidaCentralizaGenerico out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCentralizaGenerico logica(Connection con, EntradaCentralizaGenerico entrada) throws Exception {
		log.info("INICIO::CentralizaGenerico");
		SalidaCentralizaGenerico out = new SalidaCentralizaGenerico();
		SQL sql = new SQL();
		
		List<Long> idsIdTipoCentralizacion = new ArrayList<Long>();
		List<String> eventosTipos = new ArrayList<String>();
		List<MovimientoPaso> movimientosPaso = new ArrayList<MovimientoPaso>(); 
		List<ValorDatoPaso> valoresDatoPaso = new ArrayList<ValorDatoPaso>();
		
		HashMap<Long,Boolean> hashIdTipoCentralizacion = new HashMap<Long,Boolean>();
		HashMap<String,Boolean> hashEventosTipos = new HashMap<String,Boolean>();
		HashMap<String,Boolean> hashMovimientos = new HashMap<String,Boolean>();
		HashMap<String,Boolean> hashValorPaso = new HashMap<String,Boolean>();
				
		HashMap<String,Object> params = new HashMap<String,Object>();
		params = new HashMap<String,Object>();
		log.debug("GET EVENTO TIPO DATO");
		List<EventoTipoDato> eventosTipoDato = sql.query(con, SQL_SELECT_EVENTO_TIPO_DATO, params, EventoTipoDato.class);
		
		HashMap<String,Object> hTipoDatoEvento = Utils.List2Hash(eventosTipoDato, "IdTipoDatoEvento");
		
		for(InIdTipoCentralizacion id : entrada.p_IdTipoCentralizacion){
			if(hashIdTipoCentralizacion.get(id.IdTipoCentralizacion) == null) {
				idsIdTipoCentralizacion.add(id.IdTipoCentralizacion);
				hashIdTipoCentralizacion.put(id.IdTipoCentralizacion, true);
			} 
		}
		
		for(TabMovimientos mov : entrada.p_TabMovimientos){
			String id = mov.IdEmpresa + "_" + mov.Fecha + "_" + mov.Numero + "_" + mov.DocumentoIdentidad + "_" + mov.IdPortafolio + "_" + mov.IdTipoMovimiento;
			if(hashMovimientos.get(id) == null) {
				MovimientoPaso movimientoPaso = new MovimientoPaso();
				movimientoPaso.IdEmpresa = mov.IdEmpresa;
				movimientoPaso.Fecha = mov.Fecha;
				movimientoPaso.Numero = mov.Numero;
				movimientoPaso.DocumentoIdentidad = mov.DocumentoIdentidad;
				movimientoPaso.IdPortafolio = mov.IdPortafolio;
				movimientoPaso.IdTipoMovimiento = mov.IdTipoMovimiento;
				movimientosPaso.add(movimientoPaso);
				hashMovimientos.put(id, true);
			}
			
		}
				
		for(TabDatosMovimiento dat : entrada.p_TabDatosMovimiento){
			String id = dat.TipoDato + "_" + dat.ValorVarChar + "_" + dat.ValorDecimal.toPlainString();
			EventoTipoDato tipoDato = (EventoTipoDato) hTipoDatoEvento.get(dat.TipoDato);
			if(tipoDato != null && "S".equals(tipoDato.DatosPreDefinidos) && hashValorPaso.get(id) == null) {
				ValorDatoPaso valorDatoPaso = new ValorDatoPaso();
				valorDatoPaso.TipoDato = dat.TipoDato;
				valorDatoPaso.ValorVarChar = dat.ValorVarChar;
				valorDatoPaso.ValorDecimal = dat.ValorDecimal;
				valoresDatoPaso.add(valorDatoPaso);
				hashValorPaso.put(id,true);
			}
			
		}
		
		for(TabDatosEvento dat : entrada.p_TabDatosEvento){
			String id = dat.TipoDato + "_" + dat.ValorVarChar + "_" + dat.ValorDecimal.toPlainString();
			EventoTipoDato tipoDato = (EventoTipoDato) hTipoDatoEvento.get(dat.TipoDato);
			if(tipoDato != null && "S".equals(tipoDato.DatosPreDefinidos) && hashValorPaso.get(id) == null) {
				ValorDatoPaso valorDatoPaso = new ValorDatoPaso();
				valorDatoPaso.TipoDato = dat.TipoDato;
				valorDatoPaso.ValorVarChar = dat.ValorVarChar;
				valorDatoPaso.ValorDecimal = dat.ValorDecimal;
				valoresDatoPaso.add(valorDatoPaso);
				hashValorPaso.put(id,true);
			}
		}
		
		for(TabEventos evt : entrada.p_TabEventos){
			if(hashEventosTipos.get(evt.IdEvento) == null) {
				eventosTipos.add(evt.IdEvento);
				hashEventosTipos.put(evt.IdEvento, true);
			}
		}
		
		log.debug("INSERT MOVIMIENTO PASO");
		sql.batch(con, SQL_INSERT_MOVIMIENTO_PASO, movimientosPaso, MovimientoPaso.class);

		log.debug("INSERT DATO VALOR PASO");
		sql.batch(con, SQL_INSERT_DATO_VALOR_PASO, valoresDatoPaso, ValorDatoPaso.class);

		log.debug("SELECT TIPO CENTRALIZACION");
		params.put("IdTipoCentralizacion", idsIdTipoCentralizacion);
		List<TipoCentralizacion> tiposCentralizacion = sql.query(con, SQL_SELECT_TIPO_CENTRALIZACION, params, TipoCentralizacion.class);
		
		log.debug("SELECT CENTRALIZACION");
		params = new HashMap<String,Object>();
		params.put("IdTipoCentralizacion", idsIdTipoCentralizacion);
		params.put("FechaContable", entrada.p_FechaContable);
		List<Centralizacion> centralizaciones = sql.query(con, SQL_SELECT_CENTRALIZACION, params, Centralizacion.class);
		log.debug("SELECT MOVIMIENTO");
		params = new HashMap<String,Object>();
		params.put("IdTipoCentralizacion", idsIdTipoCentralizacion);
		List<Movimiento> movimientosVigentes = sql.query(con, SQL_SELECT_MOVIMIENTO, params, Movimiento.class);
		
		HashMap<String, Movimiento> movimientosVigentesMap = new HashMap<>();
		movimientosVigentes.forEach(movVigente -> {
			movimientosVigentesMap.put(
					Utils.appendKeys(movVigente.IdTipoCentralizacion.toString(), 
							movVigente.Cuenta.toString(), 
							movVigente.Fecha.toString(),
							movVigente.IdEmpresa.toString(),
							movVigente.Numero.toString(),
							movVigente.DocumentoIdentidad),movVigente);
		});
		HashMap<String, TabEventos> eventosMap = new HashMap<>();
		entrada.p_TabEventos.forEach(evt ->{
			eventosMap.put(Utils.appendKeys(evt.IdTipoCentralizacion.toString(),evt.IdMovimiento.toString()), evt);
		});
		Map<Long, Map<Long, List<TabEventos>>> eventosAgrupados = entrada.p_TabEventos.stream()
				.collect(Collectors.groupingBy(evt->evt.IdTipoCentralizacion, Collectors.groupingBy(evt->evt.IdMovimiento)));
		Map<Long, Map<Long, List<TabDatosMovimiento>>> movimientosAgrupados = entrada.p_TabDatosMovimiento.stream()
				.collect(Collectors.groupingBy(evt->evt.IdTipoCentralizacion, Collectors.groupingBy(evt->evt.IdMovimiento)));
		
		Map<Long, Map<Long, List<TabDatosEvento>>> datosEventoAgrupados = entrada.p_TabDatosEvento.stream()
				.collect(Collectors.groupingBy(evt->evt.IdTipoCentralizacion, Collectors.groupingBy(evt->evt.IdMovimiento)));
			
		log.debug("SELECT EVENTO TIPO");
		params = new HashMap<String,Object>();
		params.put("IdTipoEvento", eventosTipos);
		List<EventoTipo> eventosTipo = sql.query(con, SQL_SELECT_EVENTO_TIPO, params, EventoTipo.class);
		log.debug("SELECT EVENTO TIPO DATO VALOR");
		params = new HashMap<String,Object>();
		List<EventoTipoDatoValor> eventosDatoValor = sql.query(con, SQL_SELECT_EVENTO_TIPO_DATO_VALOR, params, EventoTipoDatoValor.class);
		params = new HashMap<String,Object>();
		
		HashMap<Long,Object> hTiposCentralizacion = Utils.List2Hash(tiposCentralizacion, "IdTipoCentralizacion");
		HashMap<Long,Object> hCentralizaciones = Utils.List2Hash(centralizaciones, "IdTipoCentralizacion");
		HashMap<String,Object> hEventoTipo = Utils.List2Hash(eventosTipo, "IdTipoEvento");
		
		
		Long nCentralizaciones = 0L;
		Long nEventos = 0L;
		for(InIdTipoCentralizacion id : entrada.p_IdTipoCentralizacion){
			Centralizacion centralizacion = (Centralizacion) hCentralizaciones.get(id.IdTipoCentralizacion);
			TipoCentralizacion tipoCentralizacion = (TipoCentralizacion) hTiposCentralizacion.get(id.IdTipoCentralizacion);
			
			if(centralizacion == null || "M".equals(tipoCentralizacion.TipoEnvio)){
				nCentralizaciones++;
				
			}
		}
		
		HashMap<String, String> nuevoMapa= new HashMap<>();
		List<MovimientoProceso> tabMovimientosProceso = new ArrayList<MovimientoProceso>();
		for(TabMovimientos mov : entrada.p_TabMovimientos){
			Date fecha = Fecha.toDate(mov.Fecha);
			boolean existe = (movimientosVigentesMap.get( Utils.appendKeys(mov.IdTipoCentralizacion.toString(), mov.IdPortafolio.toString(),Fecha.stringToSqlDate(fecha),mov.IdEmpresa.toString(),mov.Numero.toString(),mov.DocumentoIdentidad) ) != null);

			if(!existe){
				MovimientoProceso movProceso = new MovimientoProceso();
				if("MAMEVLRFIF".equals(mov.IdTipoMovimiento) || "MAMEVALRV".equals(mov.IdTipoMovimiento)){
					if(!nuevoMapa.containsKey(mov.IdMovimiento.toString()) ) {
						eventosAgrupados.getOrDefault(mov.IdTipoCentralizacion,new HashMap<>()).getOrDefault(mov.IdMovimiento, new ArrayList<>()).forEach(evento ->{
							if(!nuevoMapa.containsKey(mov.IdMovimiento.toString())) {
								nuevoMapa.put(mov.IdMovimiento.toString(), "EventosMovimiento"+mov.IdMovimiento.toString());
							}
							evento.IdMovimiento= mov.Numero;
							movProceso.Eventos.add(evento);
						});
					}
					movimientosAgrupados.getOrDefault(mov.IdTipoCentralizacion,new HashMap<>()).getOrDefault(mov.IdMovimiento, new ArrayList<>()).forEach(datoMovimiento -> {
						datoMovimiento.IdMovimiento= mov.Numero;
						movProceso.DatosMovimiento.add(datoMovimiento);
					});
					
					datosEventoAgrupados.getOrDefault(mov.IdTipoCentralizacion,new HashMap<>()).getOrDefault(mov.IdMovimiento, new ArrayList<>()).forEach(datoEvento -> {
						datoEvento.IdMovimiento= mov.Numero;	
						movProceso.DatosEvento.add(datoEvento);
						});
					mov.IdMovimiento= mov.Numero;
				}else {
					eventosAgrupados.getOrDefault(mov.IdTipoCentralizacion,new HashMap<>()).getOrDefault(mov.IdMovimiento, new ArrayList<>()).forEach(evento ->{
						movProceso.Eventos.add(evento);
						});
					movimientosAgrupados.getOrDefault(mov.IdTipoCentralizacion,new HashMap<>()).getOrDefault(mov.IdMovimiento, new ArrayList<>()).forEach(datoMovimiento -> {
						movProceso.DatosMovimiento.add(datoMovimiento);
						});
					
					datosEventoAgrupados.getOrDefault(mov.IdTipoCentralizacion,new HashMap<>()).getOrDefault(mov.IdMovimiento, new ArrayList<>()).forEach(datoEvento -> {
						movProceso.DatosEvento.add(datoEvento);
						});
				}

				nEventos+=movProceso.Eventos.size();
				movProceso.Movimiento = mov;
				tabMovimientosProceso.add(movProceso);
			}
		}
		
		Map<Object, List<MovimientoProceso>> movimientosProcesoAgrupados = tabMovimientosProceso.stream().collect(Collectors.groupingBy(mov -> mov.Movimiento.IdTipoCentralizacion));
		
		String fechaProceso = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyy-MM-dd");
		String horaProceso = FechaDB.ObtenerHoraUTC(con);
		log.debug("SELECT SECUENCIAS");
		List<Long> secuenciasCentralizaciones = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdCentralizacion", nCentralizaciones);
		List<Long> secuenciasMovimientos = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdMovimiento", new Long(tabMovimientosProceso.size()));
		List<Long> secuenciasEventos = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdEvento", new Long(nEventos));
		log.debug("END SELECT SECUENCIAS");
		int i = 0;
		int j = 0;
		int z = 0;
		List<InsertCentralizacion> centralizacionesInsert = new ArrayList<InsertCentralizacion>();
		List<InsertMovimiento> movimientosInsert = new ArrayList<InsertMovimiento>();
		List<InsertEvento> eventoInsert = new ArrayList<InsertEvento>();
		List<InsertMovimientoDato> insertMovimientoDato = new ArrayList<InsertMovimientoDato>();
		List<EventoTipo> insertEventoTipo = new ArrayList<EventoTipo>();
		List<String> strInsertEventoTipo = new ArrayList<String>();
		List<InsertLog> insertLogs = new ArrayList<InsertLog>();
		
		for(InIdTipoCentralizacion id : entrada.p_IdTipoCentralizacion){
			int numMov = 0;
			Centralizacion centralizacion = (Centralizacion) hCentralizaciones.get(id.IdTipoCentralizacion);
			TipoCentralizacion tipoCentralizacion = (TipoCentralizacion) hTiposCentralizacion.get(id.IdTipoCentralizacion);
			Long idCentralizacion = 0L;
			InsertCentralizacion insertCentralizacion = new InsertCentralizacion();
			if(centralizacion == null || "M".equals(tipoCentralizacion.TipoEnvio)){
				idCentralizacion = secuenciasCentralizaciones.get(i);
				insertCentralizacion.IdCentralizacion = idCentralizacion;
				insertCentralizacion.IdTipoCentralizacion = id.IdTipoCentralizacion;
				insertCentralizacion.IdEmpresa = entrada.p_IdEmpresa;
				insertCentralizacion.FechaProceso = fechaProceso;
				insertCentralizacion.HoraProceso = horaProceso;
				insertCentralizacion.FechaContable = entrada.p_FechaContable;
				insertCentralizacion.Glosa = "";
				insertCentralizacion.CodigoUsuario = entrada.p_CodigoUsuario;
				insertCentralizacion.TipoGeneracion = tipoCentralizacion.TipoEnvio;
				insertCentralizacion.Estado = "V";
			}else{
				idCentralizacion = centralizacion.IdCentralizacion;
			}
			List<MovimientoProceso> movs = movimientosProcesoAgrupados.getOrDefault(id.IdTipoCentralizacion, new ArrayList<>());
			for(MovimientoProceso mov : movs) {
				InsertMovimiento inMov = new InsertMovimiento();
				inMov.IdMovimiento = secuenciasMovimientos.get(j);
				inMov.IdTipoMovimiento = mov.Movimiento.IdTipoMovimiento;
				inMov.IdEmpresa = mov.Movimiento.IdEmpresa;
				inMov.Numero = mov.Movimiento.Numero;
				inMov.Linea = mov.Movimiento.Linea;
				inMov.Fecha = mov.Movimiento.Fecha;
				inMov.DocumentoIdentidad = Optional.ofNullable(mov.Movimiento.DocumentoIdentidad).orElse("");
				inMov.Cuenta = mov.Movimiento.IdPortafolio;
				inMov.Ejercicio = mov.Movimiento.Ejercicio;
				inMov.Periodo = mov.Movimiento.Periodo;
				inMov.IdCentralizacion = idCentralizacion;
				inMov.IdMoneda = mov.Movimiento.IdMoneda;
				inMov.Monto = mov.Movimiento.Monto;
				inMov.Estado = "V";
				movimientosInsert.add(inMov);
				numMov++;
				j++;
				for(TabEventos evento : mov.Eventos){
					InsertEvento inEv = new InsertEvento();
					inEv.IdEmpresa = inMov.IdEmpresa;
					inEv.IdEvento = secuenciasEventos.get(z);
					inEv.IdTipoEvento = evento.IdEvento;
					inEv.IdCentralizacion = idCentralizacion;
					inEv.IdMovimiento = inMov.IdMovimiento;
					inEv.FechaContable = entrada.p_FechaContable;
					inEv.Monto = evento.MontoEvento == null ? new BigDecimal(0l) : evento.MontoEvento;
					inEv.CodigoMoneda = evento.CodigoMoneda;
					inEv.CodigoUsuario = entrada.p_CodigoUsuario;
					inEv.FechaProceso = fechaProceso;
					inEv.HoraProceso = horaProceso;
					inEv.Estado = "V";
					EventoTipo eventoTipo = (EventoTipo) hEventoTipo.get(inEv.IdTipoEvento);
					if(eventoTipo == null && !strInsertEventoTipo.contains(inEv.IdTipoEvento)){
						EventoTipo inEvtTipo = new EventoTipo();
						inEvtTipo.IdTipoEvento = inEv.IdTipoEvento;
						inEvtTipo.Nombre = evento.Nombre;
						inEvtTipo.Estado = "I";
						insertEventoTipo.add(inEvtTipo);
						strInsertEventoTipo.add(inEv.IdTipoEvento);
					}
					eventoInsert.add(inEv);
					z++;
				}
				for(TabDatosMovimiento datoMovimiento : mov.DatosMovimiento){
					EventoTipoDato eventoTipoDato = (EventoTipoDato) hTipoDatoEvento.get(datoMovimiento.TipoDato);
					InsertMovimientoDato inDatMov = new InsertMovimientoDato();
					inDatMov.IdMovimiento = inMov.IdMovimiento;
					inDatMov.IdTipoDatoEvento = datoMovimiento.TipoDato;
					inDatMov.ValorVarchar = datoMovimiento.ValorVarChar;
					inDatMov.ValorDecimal = datoMovimiento.ValorDecimal;
					inDatMov.Estado = ValorDatoEventoValido(eventoTipoDato, inDatMov.ValorVarchar, inDatMov.ValorDecimal, eventosDatoValor);
					inDatMov.TipoEvento = "";
					if("E".equals(inDatMov.Estado)){
						insertLogs.add(GenerarLog(entrada,inDatMov,inMov,id,fechaProceso,horaProceso));
					}
					insertMovimientoDato.add(inDatMov);
				}
				for(TabDatosEvento datoEvento : mov.DatosEvento){
					EventoTipoDato eventoTipoDato = (EventoTipoDato) hTipoDatoEvento.get(datoEvento.TipoDato);
					InsertMovimientoDato inDatMov = new InsertMovimientoDato();
					inDatMov.IdMovimiento = inMov.IdMovimiento;
					inDatMov.IdTipoDatoEvento = datoEvento.TipoDato;
					inDatMov.ValorVarchar = datoEvento.ValorVarChar;
					inDatMov.ValorDecimal = datoEvento.ValorDecimal;
					inDatMov.Estado = ValorDatoEventoValido(eventoTipoDato, inDatMov.ValorVarchar, inDatMov.ValorDecimal, eventosDatoValor);
					inDatMov.TipoEvento = datoEvento.TipoEvento;
					if("E".equals(inDatMov.Estado)){
						insertLogs.add(GenerarLog(entrada,inDatMov,inMov,id,fechaProceso,horaProceso));
					}
					insertMovimientoDato.add(inDatMov);
				}
			}
			String resultadoMovimientos = "";
			if (numMov == 0) {
				insertCentralizacion.Estado = "M";//SIN MOVIMIENTOS
				resultadoMovimientos = "(SIN MOVIMIENTOS)";
			}
			if(centralizacion == null || "M".equals(tipoCentralizacion.TipoEnvio)){
				centralizacionesInsert.add(insertCentralizacion);
				i++;
			}

			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("Centralizacion", id.Nombre);
			valores.put("IdCentralizacion", String.valueOf(idCentralizacion));
			valores.put("ResultadoMovimientos", resultadoMovimientos);
			out.P_OUT_MENSAJES.add(new MensajeSalida(id.IdTipoCentralizacion,MensajeSalida.MENSAJE_INFORMACION,"CENTRALIZACIONES_GENERADA_CORRECTAMENTE",valores,con,entrada.p_IdEmpresa));	
		}
		log.debug("INSERT CENTRALIZACION");
		sql.batch(con, SQL_INSERT_CENTRALIZACION, centralizacionesInsert, InsertCentralizacion.class);
		log.debug("INSERT MOVIMIENTO");
		sql.batch(con, SQL_INSERT_MOVIMIENTO, movimientosInsert, InsertMovimiento.class);
		
		//eliminar posibles elementos duplicados
		Set<String> mapa = new HashSet<>();
		List<InsertMovimientoDato> datosMovimientosUnicos = insertMovimientoDato.stream()
				.filter(mov -> mapa.add(mov.IdMovimiento + mov.IdTipoDatoEvento))
				.collect(Collectors.toList());
		log.debug("INSERT MOVIMIENTO DATO");
		sql.batch(con, SQL_INSERT_MOVIMIENTO_DATO, datosMovimientosUnicos, InsertMovimientoDato.class);
		log.debug("INSERT EVENTO TIPO");
		sql.batch(con, SQL_INSERT_EVENTO_TIPO, insertEventoTipo, EventoTipo.class);
		log.debug("INSERT EVENTO");
		sql.batch(con, SQL_INSERT_EVENTO, eventoInsert, InsertEvento.class);
		log.debug("INSERT LOG");
		sql.batch(con, SQL_INSERT_LOG, insertLogs, InsertLog.class);
		
		log.info("FIN::CentralizaGenerico");
		return out;
	}
	
	private String ValorDatoEventoValido(EventoTipoDato eventoTipoDato, String valorVarchar, BigDecimal valorDecimal, List<EventoTipoDatoValor> eventosDatoValor){
		String estado = "E";
		if(eventoTipoDato != null) {
			if("S".equals(eventoTipoDato.DatosPreDefinidos)){
				for(EventoTipoDatoValor evt : eventosDatoValor){
					if(evt.IdTipoDatoEvento.equals(eventoTipoDato.IdTipoDatoEvento)
					&& (("V".equals(eventoTipoDato.TipoDato) && valorVarchar.equals(evt.ValorVarchar)) || ("N".equals(eventoTipoDato.TipoDato) && valorDecimal.compareTo(evt.ValorDecimal) == 0))){
						estado = "I";
						break;
					}
				}
			}else{
				estado = "I";
			}
			
		}
		return estado;
	}
	
	private InsertLog GenerarLog(EntradaCentralizaGenerico entrada, InsertMovimientoDato dato, InsertMovimiento mov, InIdTipoCentralizacion id,String fechaProceso, String horaProceso){
		InsertLog insertLog = new InsertLog();
		insertLog.IdTipoLog = 2L;
		insertLog.IdTipoProceso = 1L;
		insertLog.IdEmpresa = entrada.p_IdEmpresa;
		insertLog.Ejercicio = entrada.p_Ejercicio;
		insertLog.Fecha = fechaProceso;
		insertLog.Hora = horaProceso;
		insertLog.CodigoUsuario = entrada.p_CodigoUsuario;
		insertLog.Glosa = "ErrorPrg - Al ingresar dato movimiento, valor de " + dato.IdTipoDatoEvento + " " + dato.ValorVarchar + " , no es valido" + " Procedimiento[CTB_ItzCentralizaGenerico][700]";
		insertLog.IdCentralizacion = mov.IdCentralizacion;
		insertLog.IdMovimiento = mov.IdMovimiento;
		insertLog.IdEvento = 0L;
		insertLog.IdDefinicionAsiento = 0L;
		insertLog.IdPreComprobante = 0L;
		insertLog.IdPreComprobanteAsiento = 0L;
		insertLog.IdCuentaContable = "";
		insertLog.IdCondicion = 0L;
		insertLog.IdTipoContabilidad = 0L;
		insertLog.IdTipoEvento = "";
		insertLog.IdTipoDatoEvento = dato.IdTipoDatoEvento;
		insertLog.IdTipoMovimiento = mov.IdTipoMovimiento;
		insertLog.IdTipoCentralizacion = id.IdTipoCentralizacion;
		insertLog.NumeroMovimiento = mov.Numero;
		insertLog.FechaMovimiento = mov.Fecha;
		insertLog.RutMovimiento = Optional.ofNullable(mov.DocumentoIdentidad).orElse("").replaceAll("\\.", "");
		insertLog.CuentaMovimiento = mov.Cuenta;
		insertLog.IndicadorError = "700";
		insertLog.Estado = "E";
		
		return insertLog;
	}
	
}
