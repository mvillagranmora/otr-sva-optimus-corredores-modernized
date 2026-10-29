package cl.bcs.contabilidad;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.EnvioContabilidad.EntradaEnvioContabilidad;
import cl.bcs.contabilidad.EnvioContabilidad.SalidaEnvioContabilidad;
import cl.bcs.contabilidad.ReversaPreComprobante.EntradaReversaPreComprobante;
import cl.bcs.contabilidad.ReversaPreComprobante.SalidaReversaPreComprobante;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GeneraPreComprobanteMultiSeleccion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(GeneraPreComprobanteMultiSeleccion.class);
	
	private final String SQL_SELECT_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectCentralizacion.sql");
	private final String SQL_SELECT_TIPO_CONTABILIDAD = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectTipoContabilidad.sql");
	private final String SQL_SELECT_PRECOMPROBANTE_ULTIMO = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectPrecomprobanteUltimo.sql");
	private final String SQL_SELECT_CONDICIONES_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectCondicionesAsiento.sql");
	private final String SQL_SELECT_MOVIMIENTOS_DATOS = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectMovimientosDatos.sql");
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectEventos.sql");
	private final String SQL_SELECT_TIPO_GLOSA = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectDefinicionTipoGlosa.sql");
	private final String SQL_SELECT_CONDICIONES_ASIENTO_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectCondicionesAsientoPrecomprobante.sql");
	private final String SQL_SELECT_TIPO_CONTABILIDAD_TIPO_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectTipoContabilidadTipoCentralizacion.sql");
	private final String SQL_SELECT_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectAsiento.sql");
	private final String SQL_EVENTOS_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_SelectEventosAsiento.sql");
	private final String SQL_INSERT_PRECOMPROBANTES = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_InsertPrecomprobantes.sql");
	private final String SQL_INSERT_PRECOMPROBANTES_ASIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_InsertPrecomprobantesAsientos.sql");
	private final String SQL_INSERT_EVENTO_PRECOMPROBANTES_ASIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/GeneraPreComprobanteMultiSeleccion_InsertEventoPrecomprobantesAsientos.sql");
	
	public static class EntradaGeneraPreComprobanteMultiSeleccion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InTipoContabilidad> p_TipoContabilidad;
		public String p_FechaContable;
		public Long p_Ejercicio;
		
		public EntradaGeneraPreComprobanteMultiSeleccion(){
			this.p_TipoContabilidad = new ArrayList<InTipoContabilidad>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTipoContabilidad{
		public Long IdTipoContabilidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGeneraPreComprobanteMultiSeleccion{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGeneraPreComprobanteMultiSeleccion(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Centralizacion{
		public Long IdTipoCentralizacion;
		public Long IdCentralizacion;
		public Long IdTipoContabilidad;
		public String Nombre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoContabilidad{
		public Long IdTipoContabilidad;
		public String Nombre;
		public String Nemo;
		public String Tipo;
		public String Reversa;
		public String Agrupa;
		public String Condicionado;
		public String TipoGeneracion;
		public Long IdEmpresa;
		public Long Ejercicio;
		public String Formato;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Precomprobante{
		public Long IdPreComprobante;
		public Long IdEmpresa;
		public String CodigoUsuario;
		public Long IdTipoContabilidad;
		public Long Ejercicio;
		public String FechaContable;
		public String Glosa;
		public String FechaProceso;
		public String HoraProceso;
		public Long FolioContabilidad;
		public Long IdReversa;
		public String Estado;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AsientoCondicion{
		public Long IdCondicion;
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Condicion;
		public String Conector;
		public String TipoDato;
		public String TipoCondicion;
		public String Estado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoDato{
		public Long IdMovimiento;
		public String TipoDato;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Estado;
		public String TipoEvento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento{
		public Long IdMovimiento;
		public Long IdTipoCentralizacion;
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
		public BigDecimal Monto;
		public String Estado;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoProceso{
		public Long IdMovimiento;
		public Long IdTipoCentralizacion;
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
		public BigDecimal Monto;
		public String Estado;
		public String IdMoneda;
		public List<MovimientoDato> MovimientosDato;
		public List<Evento> Eventos;
		
		public MovimientoProceso(){
			this.MovimientosDato = new ArrayList<MovimientoDato>();
			this.Eventos = new ArrayList<Evento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Evento{
		public Long IdEvento;
		public Long IdEmpresa;
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
	
	public static class DefinicionTipoEvento{
		
		public String IdTipoEvento;
		public Long IdDefinicionAsiento;
		public String Operador;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoGlosa{
		
		public Long IdTipoContabilidad;
		public Long IdDefinicionAsiento;
		public String Origen;
		public Long Orden;
		public String GlosaCampo;
		public String NombreCampoMovimiento;
		public String IdTipoDatoEvento;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionAsiento{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdCuentaContable;
		public Long Ejercicio;
		public Long Orden;
		public String DebeHaber;
		public String TipoEvento;
		public String Condicionado;
		public String OmiteCondicionCmbte;
		public String Estado;
		public String Formato;
		public Long IdMetadataCuentaContable;
		public String Validacion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoDefinicionAsiento{
		public String IdTipoEvento;
		public Long IdDefinicionAsiento;
		public String Operador;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoContabilidadTipoCentralizacion{
		public Long IdTipoContabilidad;
		public Long IdTipoCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class DefinicionAsientoProceso{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdCuentaContable;
		public Long Ejercicio;
		public Long Orden;
		public String DebeHaber;
		public String TipoEvento;
		public String Condicionado;
		public String OmiteCondicionCmbte;
		public String Estado;
		public String Formato;
		public Long IdMetadataCuentaContable;
		public List<AsientoCondicion> Condiciones;
		public List<EventoDefinicionAsiento> Eventos;
		public List<TipoGlosa> TiposGlosa;
		
		public DefinicionAsientoProceso(){
			this.Condiciones = new ArrayList<AsientoCondicion>();
			this.Eventos = new ArrayList<EventoDefinicionAsiento>();
			this.TiposGlosa = new ArrayList<TipoGlosa>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobanteAsiento{
		public Long IdPreComprobanteAsiento;
		public Long IdEmpresa;
		public Long IdPreComprobante;
		public String IdCuentaContable;
		public Long Ejercicio;
		public Long Linea;
		public BigDecimal MontoDebe;
		public BigDecimal MontoHaber;
		public String FechaContable;
		public String Glosa;
		public String Estado;
		
		public PreComprobanteAsiento(){
			this.MontoDebe = BigDecimal.ZERO;
			this.MontoHaber = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobanteAsientoProceso{
		public Long IdEmpresa;
		public String IdCuentaContable;
		public Long Ejercicio;
		public Long Linea;
		public BigDecimal MontoDebe;
		public BigDecimal MontoHaber;
		public BigDecimal Monto;
		public String FechaContable;
		public String Glosa;
		public String Estado;
		public List<EventoPreComprobanteAsiento> EventosPrecomprobanteAsiento;
		
		public PreComprobanteAsientoProceso(){
			this.MontoDebe = BigDecimal.ZERO;
			this.MontoHaber = BigDecimal.ZERO;
			this.Monto = BigDecimal.ZERO;
			this.EventosPrecomprobanteAsiento = new ArrayList<EventoPreComprobanteAsiento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoPreComprobanteAsiento{
		public Long IdPreComprobanteAsiento;  
		public Long IdEvento;
		public String Operador; 
		public Long Linea;
		public String Estado;
		public String GlosaDetalle;
		public String DebeHaber;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class PrecomprobanteProceso{
		Precomprobante Precomprobate;
		List<PreComprobanteAsientoProceso> PrecomprobantesAsiento;
		
		public PrecomprobanteProceso(){
			this.PrecomprobantesAsiento = new ArrayList<PreComprobanteAsientoProceso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGeneraPreComprobanteMultiSeleccion in = gson.fromJson(jin.toString(), EntradaGeneraPreComprobanteMultiSeleccion.class);
		SalidaGeneraPreComprobanteMultiSeleccion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGeneraPreComprobanteMultiSeleccion logica(Connection con, EntradaGeneraPreComprobanteMultiSeleccion entrada) throws Exception {
		logger.info("INICIO::GeneraPreComprobanteMultiSeleccion");
		
		SalidaGeneraPreComprobanteMultiSeleccion out = new SalidaGeneraPreComprobanteMultiSeleccion();
		
		SQL sql = new SQL();
		
		List<Long> idsTipoCentralizacion = 	entrada.p_TipoContabilidad.stream()
											.map(inTipoContabilidad->inTipoContabilidad.IdTipoContabilidad)
											.distinct()
											.collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Centralizacion> centralizaciones = sql.query(con, SQL_SELECT_CENTRALIZACION, params, Centralizacion.class);
		HashMap<Long,Object> hCentralizaciones = Utils.List2Hash(centralizaciones, "IdTipoContabilidad");
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<TipoContabilidad> tiposContabilidad = sql.query(con, SQL_SELECT_TIPO_CONTABILIDAD, params, TipoContabilidad.class);
		HashMap<Long,Object> hTipoContabilidad = Utils.List2Hash(tiposContabilidad, "IdTipoContabilidad");
		params.clear();
				
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Precomprobante> precomprobantesUltimos = sql.query(con, SQL_SELECT_PRECOMPROBANTE_ULTIMO, params, Precomprobante.class);
		HashMap<Long,Object> hPrecomprobantesUltimos = Utils.List2Hash(precomprobantesUltimos, "IdTipoContabilidad");
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		List<TipoContabilidadTipoCentralizacion> tiposContabilidadTipoCentralizacion = sql.query(con, SQL_SELECT_TIPO_CONTABILIDAD_TIPO_CENTRALIZACION, params, TipoContabilidadTipoCentralizacion.class);
		Map<Long,List<TipoContabilidadTipoCentralizacion>> hashTiposContabilidadTipoCentralizacion = tiposContabilidadTipoCentralizacion.stream().collect(Collectors.groupingBy(tipoContabilidadTipoCentralizacion->tipoContabilidadTipoCentralizacion.IdTipoContabilidad));
		tiposContabilidadTipoCentralizacion.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		List<AsientoCondicion> condicionesTipoContabilidad = sql.query(con, SQL_SELECT_CONDICIONES_ASIENTO, params, AsientoCondicion.class);
		Map<Long,List<AsientoCondicion>> hashCondicionesTipoContabilidad = condicionesTipoContabilidad.stream().collect(Collectors.groupingBy(condicionTipoContabilidad->condicionTipoContabilidad.IdTipoContabilidad));
		condicionesTipoContabilidad.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		List<DefinicionAsiento> asientos = sql.query(con, SQL_SELECT_ASIENTO, params, DefinicionAsiento.class);
		Map<Long,List<DefinicionAsiento>> hashAsientos = asientos.stream().collect(Collectors.groupingBy(asiento->asiento.IdTipoContabilidad));
		asientos.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		List<EventoDefinicionAsiento> eventosAsientos = sql.query(con, SQL_EVENTOS_ASIENTO, params, EventoDefinicionAsiento.class);
		Map<Long,List<EventoDefinicionAsiento>> hashEventosAsientos = eventosAsientos.stream().collect(Collectors.groupingBy(eventoAsiento->eventoAsiento.IdDefinicionAsiento));
		eventosAsientos.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		List<AsientoCondicion> condicionesAsientos = sql.query(con, SQL_SELECT_CONDICIONES_ASIENTO_PRECOMPROBANTE, params, AsientoCondicion.class);
		Map<Long,List<AsientoCondicion>> hashCondicionesAsientos = condicionesAsientos.stream().collect(Collectors.groupingBy(condicionAsiento->condicionAsiento.IdDefinicionAsiento));
		condicionesAsientos.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		params.put("FechaContable",  entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<MovimientoDato> movimientosDatos = sql.query(con, SQL_SELECT_MOVIMIENTOS_DATOS, params, MovimientoDato.class);
		Map<Long,List<MovimientoDato>> hashMovimientosDato = movimientosDatos.stream().collect(Collectors.groupingBy(movimientoDato->movimientoDato.IdMovimiento));
		movimientosDatos.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		params.put("FechaContable",  entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Movimiento> movimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, Movimiento.class);
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		params.put("FechaContable",  entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Evento> eventos = sql.query(con, SQL_SELECT_EVENTOS, params, Evento.class);
		Map<Long,List<Evento>> hashEventos = eventos.stream().collect(Collectors.groupingBy(evento->evento.IdMovimiento));
		eventos.clear();
		params.clear();
		
		params.put("IdTipoContabilidad", idsTipoCentralizacion);
		List<TipoGlosa> tiposGlosas = sql.query(con, SQL_SELECT_TIPO_GLOSA, params, TipoGlosa.class);
		Map<Long,List<TipoGlosa>> hashTiposGlosas = tiposGlosas.stream().collect(Collectors.groupingBy(tipoGlosa->tipoGlosa.IdDefinicionAsiento));
		params.clear();
		
		List<InTipoContabilidad> inTipoContabilidadProceso = new ArrayList<InTipoContabilidad>();
		for(InTipoContabilidad inTipoContabilidad : entrada.p_TipoContabilidad){
			Centralizacion centralizacion = (Centralizacion) hCentralizaciones.get(inTipoContabilidad.IdTipoContabilidad);
			TipoContabilidad tipoContabilidad = (TipoContabilidad) hTipoContabilidad.get(inTipoContabilidad.IdTipoContabilidad);
			Precomprobante precomprobanteExistente = (Precomprobante) hPrecomprobantesUltimos.get(inTipoContabilidad.IdTipoContabilidad);
			List<DefinicionAsiento> asientosComprobante = hashAsientos.get(inTipoContabilidad.IdTipoContabilidad);
			List<String> asientosConCuentasInvalidas = new ArrayList<String>();
			if(asientosComprobante != null) {
				asientosConCuentasInvalidas = asientosComprobante.stream().filter(asiento->"ERR".equals(asiento.Validacion)).map(asiento->asiento.IdCuentaContable).collect(Collectors.toList());
			}
			if(tipoContabilidad == null){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("TipoContabilidad", String.valueOf(inTipoContabilidad.IdTipoContabilidad));
				out.P_OUT_MENSAJES.add(new MensajeSalida(inTipoContabilidad.IdTipoContabilidad,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_TIPO_CONTABILIDAD",valores,con,entrada.p_IdEmpresa));
			}
			else if(asientosConCuentasInvalidas.size() > 0) {
				for(String cuenta : asientosConCuentasInvalidas) {
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("NombreTipoContabilidad", tipoContabilidad.Nombre);
					valores.put("TipoContabilidad", String.valueOf(tipoContabilidad.IdTipoContabilidad));
					valores.put("CuentaContable", cuenta);
					out.P_OUT_MENSAJES.add(new MensajeSalida(inTipoContabilidad.IdTipoContabilidad,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_CUENTA_CONTABLE_ASIENTO",valores,con,entrada.p_IdEmpresa));
				}
			}
			else if(centralizacion == null){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("NombreTipoContabilidad", tipoContabilidad.Nombre);
				valores.put("TipoContabilidad", String.valueOf(tipoContabilidad.IdTipoContabilidad));
				out.P_OUT_MENSAJES.add(new MensajeSalida(inTipoContabilidad.IdTipoContabilidad,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTEN_CENTRALIZACIONES_ASOCIADAS",valores,con,entrada.p_IdEmpresa));
			}else if(precomprobanteExistente != null && entrada.p_FechaContable.equals(precomprobanteExistente.FechaContable) && "N".equals(tipoContabilidad.Reversa)){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("NombreTipoContabilidad", tipoContabilidad.Nombre);
				valores.put("IdPreComprobante", String.valueOf(precomprobanteExistente.IdPreComprobante));
				out.P_OUT_MENSAJES.add(new MensajeSalida(inTipoContabilidad.IdTipoContabilidad,MensajeSalida.MENSAJE_ADVERTENCIA,"YA_EXISTE_PRECOMPROBANTE",valores,con,entrada.p_IdEmpresa));
			}
			else{
				inTipoContabilidadProceso.add(inTipoContabilidad);
			}
		}
		logger.debug("GeneraPreComprobanteMultiSeleccion::Se termina querys de datos");
		if(inTipoContabilidadProceso.size() > 0){
			
			//AGRUPAMOS LOS MOVIMIENTOS CON SUS RESPECTIVOS 
			List<MovimientoProceso> mvtosProceso = movimientos.stream().map(mvto->{
				MovimientoProceso mvtoProceso = new MovimientoProceso();
				mvtoProceso.IdMovimiento = mvto.IdMovimiento;
				mvtoProceso.IdTipoCentralizacion = mvto.IdTipoCentralizacion;
				mvtoProceso.IdTipoMovimiento = mvto.IdTipoMovimiento;
				mvtoProceso.IdEmpresa = mvto.IdEmpresa;
				mvtoProceso.Numero = mvto.Numero;
				mvtoProceso.Linea = mvto.Linea;
				mvtoProceso.Fecha = mvto.Fecha;
				mvtoProceso.DocumentoIdentidad = mvto.DocumentoIdentidad;
				mvtoProceso.Cuenta = mvto.Cuenta;
				mvtoProceso.Ejercicio = mvto.Ejercicio;
				mvtoProceso.Periodo = mvto.Periodo;
				mvtoProceso.IdCentralizacion = mvto.IdCentralizacion;
				mvtoProceso.Monto = mvto.Monto;
				mvtoProceso.Estado = mvto.Estado;
				mvtoProceso.IdMoneda = mvto.IdMoneda;
				mvtoProceso.MovimientosDato = hashMovimientosDato.get(mvto.IdMovimiento) != null ? hashMovimientosDato.get(mvto.IdMovimiento) : new ArrayList<MovimientoDato>();
				mvtoProceso.Eventos = hashEventos.get(mvto.IdMovimiento) != null ? hashEventos.get(mvto.IdMovimiento) : new ArrayList<Evento>();
				return mvtoProceso;
			}).collect(Collectors.toList());
			
			logger.debug("GeneraPreComprobanteMultiSeleccion::Se organiza los movimientos");
			
			Map<Long,List<MovimientoProceso>> hashMvtosProceso = mvtosProceso.stream().collect(Collectors.groupingBy(mvtoProceso->mvtoProceso.IdTipoCentralizacion));
			mvtosProceso.clear();
			
			String fechaProceso = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyy-MM-dd");
			String horaProceso = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);
			EntradaReversaPreComprobante entradaReversaPreComprobante = new EntradaReversaPreComprobante();
			int numeroPrecomprobateAsientos = 0;
			List<PrecomprobanteProceso> precomprobantesProceso = new ArrayList<PrecomprobanteProceso>();
			for(InTipoContabilidad inTipoContabilidad : inTipoContabilidadProceso){
				Precomprobante precomprobante = (Precomprobante) hPrecomprobantesUltimos.get(inTipoContabilidad.IdTipoContabilidad);
				TipoContabilidad tipoContabilidad = (TipoContabilidad) hTipoContabilidad.get(inTipoContabilidad.IdTipoContabilidad);
				List<DefinicionAsientoProceso> definicionesAsientos = new ArrayList<DefinicionAsientoProceso>();
				Map<String,List<AsientoCondicion>> hashCondicionTipoComprobante = new HashMap<String,List<AsientoCondicion>>();
				List<TipoContabilidadTipoCentralizacion> tiposCentralizaciones = new ArrayList<TipoContabilidadTipoCentralizacion>();
				List<MovimientoProceso> movimientoDatoPrecomprobante = new ArrayList<MovimientoProceso>();
				HashMap<Long,Boolean> idsMovimiento = new HashMap<Long,Boolean>();
				PrecomprobanteProceso precomprobanteProceso = new PrecomprobanteProceso();
				if("S".equals(tipoContabilidad.Reversa) && precomprobante != null){
					ReversaPreComprobante.InPrecomprobante inReversa = new ReversaPreComprobante.InPrecomprobante();
					inReversa.IdPreComprobante = 0L;
					inReversa.IdPreComprobanteAnt = precomprobante.IdPreComprobante;
					entradaReversaPreComprobante.p_IdPreComprobantes.add(inReversa);
				}

				String glosa = (tipoContabilidad.Nombre + "/" + tipoContabilidad.Nemo);
				Precomprobante insertPrecomprobante = new Precomprobante();
				insertPrecomprobante.CodigoUsuario = String.valueOf(entrada.p_IdUsuario);
				insertPrecomprobante.IdEmpresa = entrada.p_IdEmpresa;
				insertPrecomprobante.IdTipoContabilidad = inTipoContabilidad.IdTipoContabilidad;
				insertPrecomprobante.Ejercicio = entrada.p_Ejercicio;
				insertPrecomprobante.FechaContable = entrada.p_FechaContable;
				insertPrecomprobante.Glosa = glosa;
				insertPrecomprobante.FechaProceso = fechaProceso;
				insertPrecomprobante.HoraProceso = horaProceso;
				insertPrecomprobante.FolioContabilidad = 0L;
				insertPrecomprobante.IdReversa = 0L;
				insertPrecomprobante.Estado = "V";
				precomprobanteProceso.Precomprobate = insertPrecomprobante;
				//BUSCAMOS LAS CUENTAS CONTABLES
				logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Busqueda las cuentas contables");
				List<DefinicionAsiento> asientosComprobante = hashAsientos.get(inTipoContabilidad.IdTipoContabilidad);
				if(asientosComprobante != null) {
					for(DefinicionAsiento definicionAsiento : asientosComprobante){
						DefinicionAsientoProceso definicionAsientoProceso = new DefinicionAsientoProceso();
						definicionAsientoProceso = Utils.copyPojo(definicionAsiento, definicionAsientoProceso);
						
						List<AsientoCondicion> asientoCondicionComprobante = hashCondicionesAsientos.get(definicionAsiento.IdDefinicionAsiento);
						List<EventoDefinicionAsiento> eventoDefinicionAsientoComprobante = hashEventosAsientos.get(definicionAsiento.IdDefinicionAsiento);
						List<TipoGlosa> tiposGlosaComprobante = hashTiposGlosas.get(definicionAsiento.IdDefinicionAsiento);
						Map<Long,List<TipoGlosa>> hashTiposGlosasDefault = 	tiposGlosas.stream()
																			.filter(tipoGlosa->tipoGlosa.IdDefinicionAsiento != null && tipoGlosa.IdDefinicionAsiento.equals(0L))
																			.collect(Collectors.groupingBy(tipoGlosa->tipoGlosa.IdTipoContabilidad));
						if(asientoCondicionComprobante != null) {
							definicionAsientoProceso.Condiciones.addAll(asientoCondicionComprobante);
						}
						
						if(eventoDefinicionAsientoComprobante != null) {
							definicionAsientoProceso.Eventos.addAll(eventoDefinicionAsientoComprobante);
						}
						
						if(tiposGlosaComprobante != null) {
							definicionAsientoProceso.TiposGlosa.addAll(tiposGlosaComprobante);
						}
						
						if(definicionAsientoProceso.TiposGlosa.size() == 0){
							List<TipoGlosa> tiposGlosaDefault = hashTiposGlosasDefault.get(inTipoContabilidad.IdTipoContabilidad);
							if(tiposGlosaDefault != null) {
								definicionAsientoProceso.TiposGlosa = tiposGlosaDefault;
							}
						}
						
						definicionesAsientos.add(definicionAsientoProceso);
					}
				}
				logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Busqueda las cuentas contables");
				
				logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Busqueda las Condiciones de precomprobante");
				//BUSCAMOS LAS CONDICIONES DEL PRECOMPROBANTE
				List<AsientoCondicion> condicionTipoContabilidadComprobante = hashCondicionesTipoContabilidad.get(inTipoContabilidad.IdTipoContabilidad);
				if(condicionTipoContabilidadComprobante != null) {
					hashCondicionTipoComprobante = condicionTipoContabilidadComprobante.stream().collect(Collectors.groupingBy(condicionComprobante->condicionComprobante.IdTipoDatoEvento));
				}
				logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Busqueda las Condiciones de precomprobante");
				
				logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Busqueda las Centralizaciones de precomprobante");
				//BUSCAMOS LAS CENTRALIACIONES DEL PRECOMPROBANTE
				List<TipoContabilidadTipoCentralizacion> tiposContabilidadTipoCentralizacionComprobante = hashTiposContabilidadTipoCentralizacion.get(inTipoContabilidad.IdTipoContabilidad);
				if(tiposContabilidadTipoCentralizacionComprobante != null) {
					tiposCentralizaciones.addAll(tiposContabilidadTipoCentralizacionComprobante);
				}
				logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Busqueda las Centralizaciones de precomprobante");
				
				logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Busqueda los movimientos que cumpan con las condiciones del asiento");
				//BUSCAMOS LOS MOVIMIENTOS QUE CUMPLAN CON LAS CONDICIONES DEL ASIENTO
				List<Long> idsCentralizaciones = 	tiposCentralizaciones.stream()
													.map(centralizacion->centralizacion.IdTipoCentralizacion)
													.distinct()
													.collect(Collectors.toList());
				
				for(Long idTipoCentralizacion : idsCentralizaciones){
					List<MovimientoProceso> movimientosTipoCentralizacionComprobante = hashMvtosProceso.get(idTipoCentralizacion);
					if(movimientosTipoCentralizacionComprobante != null) {
						for(MovimientoProceso mvto : movimientosTipoCentralizacionComprobante){
							if(idsMovimiento.get(mvto.IdMovimiento) == null && comparar(mvto,hashCondicionTipoComprobante)){
								movimientoDatoPrecomprobante.add(mvto);
								idsMovimiento.put(mvto.IdMovimiento, true);
							}
						}
					}
				}
				logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Busqueda los movimientos que cumpan con las condiciones del asiento");
				
				logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Busqueda los movimientos que cumpan con las condiciones de las cuentas");
				//BUSCAMOS LOS MOVIMIENTOS QUE CUMPLAN CON CONDICIONES DE LA CUENTA Y AGREGAMOS
				Long linea = 1L;
				HashMap<Long,PreComprobanteAsientoProceso> hPrecomprobantesAsiento = new HashMap<Long,PreComprobanteAsientoProceso>();
				Map<Long,Map<String,List<AsientoCondicion>>> hashCondicionesAsientoPorDefinicion = new HashMap<Long,Map<String,List<AsientoCondicion>>>();
				for(DefinicionAsientoProceso definicion : definicionesAsientos){
					Map<String,List<AsientoCondicion>> hashCondicionesAsiento = definicion.Condiciones.stream()
							.collect(Collectors.groupingBy(condicion->condicion.IdTipoDatoEvento));
					hashCondicionesAsientoPorDefinicion.put(definicion.IdDefinicionAsiento, hashCondicionesAsiento);
				}
				
				for(MovimientoProceso mvto : movimientoDatoPrecomprobante){
					Map<String, List<Evento>> hashEventosPorTipo = mvto.Eventos.stream()
							.collect(Collectors.groupingBy(evt->evt.IdTipoEvento));
					
					for(DefinicionAsientoProceso definicion : definicionesAsientos){
						PreComprobanteAsientoProceso precomprobanteAsiento = hPrecomprobantesAsiento.get(definicion.IdDefinicionAsiento);
						Map<String,List<AsientoCondicion>> hashCondicionesAsiento = hashCondicionesAsientoPorDefinicion.get(definicion.IdDefinicionAsiento);
						if(hashCondicionesAsiento == null) hashCondicionesAsiento = new HashMap<String,List<AsientoCondicion>>();
						
						if(precomprobanteAsiento == null){
							precomprobanteAsiento = new PreComprobanteAsientoProceso();
							precomprobanteAsiento.IdEmpresa = entrada.p_IdEmpresa;
							precomprobanteAsiento.IdCuentaContable = definicion.IdCuentaContable;
							precomprobanteAsiento.Ejercicio = entrada.p_Ejercicio;
							precomprobanteAsiento.FechaContable = entrada.p_FechaContable;
							precomprobanteAsiento.Glosa = insertPrecomprobante.Glosa;
							precomprobanteAsiento.Estado = "V";
						}
						
						if(comparar(mvto,hashCondicionesAsiento)){
							BigDecimal montoAcum = BigDecimal.ZERO;
							String glosaDetalle = crearGlosa(mvto, definicion);
							
							for(EventoDefinicionAsiento evtDefinicion : definicion.Eventos){
								List<Evento> eventosTipo = hashEventosPorTipo.get(evtDefinicion.IdTipoEvento);
								if(eventosTipo != null) {
									for(Evento evt : eventosTipo){
										EventoPreComprobanteAsiento eventoPreComprobanteAsiento = new EventoPreComprobanteAsiento();
										eventoPreComprobanteAsiento.IdEvento = evt.IdEvento;
										eventoPreComprobanteAsiento.Operador = evtDefinicion.Operador;
										eventoPreComprobanteAsiento.Linea = 0L;
										eventoPreComprobanteAsiento.Estado = "V";
										eventoPreComprobanteAsiento.GlosaDetalle = glosaDetalle;
										precomprobanteAsiento.EventosPrecomprobanteAsiento.add(eventoPreComprobanteAsiento);
										BigDecimal monto = aplicarOperador(evtDefinicion.Operador,evt.Monto);
										if(esMontoDebe(definicion.DebeHaber, monto)){
											eventoPreComprobanteAsiento.DebeHaber = "D";
										}
										if(esMontoHaber(definicion.DebeHaber, monto)){
											eventoPreComprobanteAsiento.DebeHaber = "H";
										} 
										montoAcum = montoAcum.add(monto, MathContext.DECIMAL128);
									}
								}
							}
							
							precomprobanteAsiento.Monto = precomprobanteAsiento.Monto.add(montoAcum,MathContext.DECIMAL128);
							if(esMontoDebe(definicion.DebeHaber,precomprobanteAsiento.Monto)){
								precomprobanteAsiento.MontoDebe = precomprobanteAsiento.Monto.abs(MathContext.DECIMAL128);
								precomprobanteAsiento.MontoHaber = BigDecimal.ZERO;
							} 
								
							if(esMontoHaber(definicion.DebeHaber,precomprobanteAsiento.Monto)){
								precomprobanteAsiento.MontoHaber = precomprobanteAsiento.Monto.abs(MathContext.DECIMAL128);
								precomprobanteAsiento.MontoDebe = BigDecimal.ZERO;
							} 
						}
						
						hPrecomprobantesAsiento.put(definicion.IdDefinicionAsiento, precomprobanteAsiento);
					}
				}

				
				for(Map.Entry<Long,PreComprobanteAsientoProceso> obj : hPrecomprobantesAsiento.entrySet()){
					PreComprobanteAsientoProceso precomprobanteAsiento = obj.getValue();
					if(precomprobanteAsiento.EventosPrecomprobanteAsiento.size() > 0){
						numeroPrecomprobateAsientos++;
						precomprobanteAsiento.Linea = linea;
						linea++;
						precomprobanteProceso.PrecomprobantesAsiento.add(precomprobanteAsiento);
					}
				}
				//SI TIENE MOV SE INSERTA SINO ADVERTENCIA
				if(precomprobanteProceso.PrecomprobantesAsiento.size() > 0){
					precomprobantesProceso.add(precomprobanteProceso);
				}else{
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("NombreTipoContabilidad", tipoContabilidad.Nombre);
					valores.put("TipoContabilidad", String.valueOf(tipoContabilidad.IdTipoContabilidad));
					out.P_OUT_MENSAJES.add(new MensajeSalida(inTipoContabilidad.IdTipoContabilidad,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTEN_MOVIMIENTOS_CENTRALIZACIONES_ASOCIADAS",valores,con,entrada.p_IdEmpresa));
				}
			}
			logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Busqueda los movimientos que cumpan con las condiciones de las cuentas");
			
			List<Long> secuenciasPrecomprobante = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdPreComprobante", Long.valueOf(precomprobantesProceso.size()));
			List<Long> secuenciasPrecomprobateAsientos = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento", Long.valueOf(numeroPrecomprobateAsientos));
			int i = 0;
			int j = 0;
			
			List<Precomprobante> precomprobantes = new ArrayList<Precomprobante>();
			List<PreComprobanteAsiento> precomprobantesAsiento = new ArrayList<PreComprobanteAsiento>();
			List<EventoPreComprobanteAsiento> eventoPreComprobanteAsiento = new ArrayList<EventoPreComprobanteAsiento>();
			
			logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio preparacion Envio contabilidad");
			EntradaEnvioContabilidad entradaEnvioContabilidad = new EntradaEnvioContabilidad();
			for(PrecomprobanteProceso precomprobanteProceso : precomprobantesProceso){
				Precomprobante precomprobante = precomprobanteProceso.Precomprobate;
				precomprobante.IdPreComprobante = secuenciasPrecomprobante.get(i);
				EnvioContabilidad.Secuencia secuencia = new EnvioContabilidad.Secuencia();
				secuencia.SecuenciaComprobante = precomprobante.IdPreComprobante;
				entradaEnvioContabilidad.p_SecCmbte.add(secuencia);
				
				BigDecimal montoDebe = BigDecimal.ZERO;
				BigDecimal montoHaber = BigDecimal.ZERO;
				for(PreComprobanteAsientoProceso precomprobanteAsientoProceso : precomprobanteProceso.PrecomprobantesAsiento){
					PreComprobanteAsiento precomprobanteAsiento = new PreComprobanteAsiento();
					precomprobanteAsiento = Utils.copyPojo(precomprobanteAsientoProceso, precomprobanteAsiento);
					precomprobanteAsiento.IdPreComprobanteAsiento = secuenciasPrecomprobateAsientos.get(j);
					precomprobanteAsiento.IdPreComprobante = precomprobante.IdPreComprobante;
					montoDebe = montoDebe.add(precomprobanteAsiento.MontoDebe, MathContext.DECIMAL128);
					montoHaber = montoHaber.add(precomprobanteAsiento.MontoHaber, MathContext.DECIMAL128);
					precomprobantesAsiento.add(precomprobanteAsiento);
					for(EventoPreComprobanteAsiento evt : precomprobanteAsientoProceso.EventosPrecomprobanteAsiento){
						evt.IdPreComprobanteAsiento = precomprobanteAsiento.IdPreComprobanteAsiento;
						eventoPreComprobanteAsiento.add(evt);
					}
					j++;
				}
				
				if( montoDebe.setScale(2, RoundingMode.HALF_UP).compareTo(montoHaber.setScale(2, RoundingMode.HALF_UP)) != 0) precomprobante.Estado = "D";
				precomprobantes.add(precomprobante);
				i++;
			}
			logger.debug("GeneraPreComprobanteMultiSeleccion::Fin preparacion Envio contabilidad");
			
			logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Reversa PreComprobante");
			if(entradaReversaPreComprobante.p_IdPreComprobantes.size() > 0){
				ReversaPreComprobante reversaPreComprobante = new ReversaPreComprobante();
				entradaReversaPreComprobante.p_CodigoUsuario = String.valueOf(entrada.p_IdUsuario);
				entradaReversaPreComprobante.p_FechaContable = entrada.p_FechaContable;
				entradaReversaPreComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
				SalidaReversaPreComprobante salidaReversaPreComprobante = reversaPreComprobante.logica(con, entradaReversaPreComprobante);
				
				out.P_OUT_ERRORESCONTADOR += salidaReversaPreComprobante.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaReversaPreComprobante.P_OUT_MENSAJES);
				
			}
			logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Reversa PreComprobante");
			
			logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Guardar Precomprobante");
			sql.batch(con, SQL_INSERT_PRECOMPROBANTES, precomprobantes, Precomprobante.class);
			sql.batch(con, SQL_INSERT_PRECOMPROBANTES_ASIENTOS, precomprobantesAsiento, PreComprobanteAsiento.class);
			sql.batch(con, SQL_INSERT_EVENTO_PRECOMPROBANTES_ASIENTOS, eventoPreComprobanteAsiento, EventoPreComprobanteAsiento.class);
			logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Guardar Precomprobante");
			
			logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Enviar Precomprobante");
			if(entradaEnvioContabilidad.p_SecCmbte.size() > 0){
				entradaEnvioContabilidad.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaEnvioContabilidad.p_IdUsuario = entrada.p_IdUsuario;
				entradaEnvioContabilidad.p_IdModulo = entrada.p_IdModulo;
				entradaEnvioContabilidad.p_UsaReglasNegocio = "S";
				entradaEnvioContabilidad.p_Destino = "B";
				EnvioContabilidad envioContabilidad = new EnvioContabilidad();
				SalidaEnvioContabilidad salidaEnvioContabilidad = envioContabilidad.logica(con, entradaEnvioContabilidad);
				out.P_OUT_ERRORESCONTADOR += salidaEnvioContabilidad.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaEnvioContabilidad.P_OUT_MENSAJES);
			}
			logger.debug("GeneraPreComprobanteMultiSeleccion::FIn Enviar Precomprobante");
			
			logger.debug("GeneraPreComprobanteMultiSeleccion::Inicio Procesar salida");
			Map<Long,Boolean> hashProblemas = new HashMap<Long,Boolean>();
			for(MensajeSalida mje : out.P_OUT_MENSAJES){
				if(mje != null && mje.id != null && (MensajeSalida.MENSAJE_ADVERTENCIA.equals(mje.tipo) || MensajeSalida.MENSAJE_ERROR.equals(mje.tipo))){
					hashProblemas.put((Long)mje.id, true);
				}
			}
			for(Precomprobante precomprobante : precomprobantes){
				TipoContabilidad tipoContabilidad = (TipoContabilidad) hTipoContabilidad.get(precomprobante.IdTipoContabilidad);
				boolean problema = hashProblemas.get(precomprobante.IdPreComprobante) != null;
				if(!problema){
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("NombreTipoContabilidad", tipoContabilidad.Nombre);
					valores.put("IdPreComprobante", String.valueOf(precomprobante.IdPreComprobante));
					out.P_OUT_MENSAJES.add(new MensajeSalida(precomprobante.IdPreComprobante,MensajeSalida.MENSAJE_INFORMACION,"PRE_COMPROBANTE_GENERADO",valores,con,entrada.p_IdEmpresa));
				}
			}
			logger.debug("GeneraPreComprobanteMultiSeleccion::Fin Procesar salida");
		}
		
		logger.info("FIN::GeneraPreComprobanteMultiSeleccion");
		return out;
		
	}
	
	public boolean comparar(MovimientoProceso mvto,  Map<String, List<AsientoCondicion>> hashCondicionTipoComprobante){
		if(hashCondicionTipoComprobante.size() == 0) return true;
		HashMap<String,Boolean> comparaciones = new HashMap<String,Boolean>();
				
		for(MovimientoDato mvtoDato : mvto.MovimientosDato){
			List<AsientoCondicion> condicionTipoComprobante = hashCondicionTipoComprobante.get(mvtoDato.IdTipoDatoEvento);
			if(condicionTipoComprobante != null) {
				for(AsientoCondicion condicion : condicionTipoComprobante){
					
					if(comparaciones.get(condicion.IdTipoDatoEvento) == null || (!comparaciones.get(condicion.IdTipoDatoEvento) && esOR(condicion.Condicion)) || (comparaciones.get(condicion.IdTipoDatoEvento) && esAnd(condicion.Condicion))){
						boolean comparacion = false;
						switch (condicion.Condicion){
							case "=":
								comparacion = "V".equals(mvtoDato.TipoDato) ? mvtoDato.ValorVarchar.equals(condicion.ValorVarchar) : mvtoDato.ValorDecimal.equals(condicion.ValorDecimal);
								break;
							case "<>":
								comparacion = "V".equals(mvtoDato.TipoDato) ? !mvtoDato.ValorVarchar.equals(condicion.ValorVarchar) : !mvtoDato.ValorDecimal.equals(condicion.ValorDecimal);
								break;
							case ">":
								comparacion = "V".equals(mvtoDato.TipoDato) ? mvtoDato.ValorVarchar.compareTo(condicion.ValorVarchar) == 1: mvtoDato.ValorDecimal.compareTo(condicion.ValorDecimal) == 1;
								break;
							case "<":
								comparacion = "V".equals(mvtoDato.TipoDato) ? mvtoDato.ValorVarchar.compareTo(condicion.ValorVarchar) == -1: mvtoDato.ValorDecimal.compareTo(condicion.ValorDecimal) == -1;
								break;
						}
						
						comparaciones.put(condicion.IdTipoDatoEvento, comparacion);
					}
				}
			}
		}

		for(Entry<String, List<AsientoCondicion>> condicion : hashCondicionTipoComprobante.entrySet()){
			String idTipoDatoEvento = condicion.getKey();
			if(comparaciones.get(idTipoDatoEvento) == null || !comparaciones.get(idTipoDatoEvento)) return false;
		}
		
		return true;
	}
	
	private BigDecimal aplicarOperador(String operador, BigDecimal monto){
		if("-".equals(operador)) return monto.abs(MathContext.DECIMAL128).negate(MathContext.DECIMAL128);
		else return monto.abs(MathContext.DECIMAL128);
	}
	
	private Boolean esOR(String Condicion) {
		return "=".equals(Condicion);
	}
	
	private Boolean esAnd(String Condicion) {
		return !"=".equals(Condicion);
	}
	
	private String crearGlosa(MovimientoProceso mvto, DefinicionAsientoProceso definicion){
		String glosaDetalle = "";
		Map<String,MovimientoDato> hashMovDato = new HashMap<String,MovimientoDato>();
		for(MovimientoDato movD : mvto.MovimientosDato){
			if(movD != null && movD.IdTipoDatoEvento != null && hashMovDato.get(movD.IdTipoDatoEvento) == null){
				hashMovDato.put(movD.IdTipoDatoEvento, movD);
			}
		}
		
		for(TipoGlosa glosa : definicion.TiposGlosa){
			if("DAT".equals(glosa.Origen)){
				MovimientoDato movD = hashMovDato.get(glosa.IdTipoDatoEvento);
				if(movD != null) {
					String valor = "V".equals(movD.TipoDato) ? movD.ValorVarchar : movD.ValorDecimal.toPlainString();
					glosaDetalle += " " + glosa.GlosaCampo + " " + valor;
				}
			}
			if("MOV".equals(glosa.Origen)){
				try{
					Field f = mvto.getClass().getDeclaredField(glosa.NombreCampoMovimiento);
					if(f != null){
						glosaDetalle += " " + glosa.GlosaCampo + " " + String.valueOf(f.get(mvto));
					}
				}catch(Exception e){
					logger.error("No se encontro campo glosa",e);
				}
				
			}
		}
		
		return glosaDetalle;
	}
	
	private Boolean esMontoDebe(String debeHaber, BigDecimal monto) {
		return ("D".equals(debeHaber) && monto.compareTo(BigDecimal.ZERO) == 1) || ("H".equals(debeHaber) && monto.compareTo(BigDecimal.ZERO) == -1);
	}
	
	private Boolean esMontoHaber(String debeHaber, BigDecimal monto) {
		return ("H".equals(debeHaber) && monto.compareTo(BigDecimal.ZERO) == 1) || ("D".equals(debeHaber) && monto.compareTo(BigDecimal.ZERO) == -1);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CON\",\"p_TipoContabilidad\":[{\"IdTipoContabilidad\":1882}],\"p_FechaContable\":\"2020-07-30\",\"p_Ejercicio\":\"2020\"}";
		EntradaGeneraPreComprobanteMultiSeleccion Objin = gson.fromJson(json, EntradaGeneraPreComprobanteMultiSeleccion.class);
		GeneraPreComprobanteMultiSeleccion  demo= new GeneraPreComprobanteMultiSeleccion();				
		try {
			SalidaGeneraPreComprobanteMultiSeleccion ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
