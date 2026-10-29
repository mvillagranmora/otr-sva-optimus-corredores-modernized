package cl.bcs.ordenes;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearTransaccionesHistoricas.EntradaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.CrearTransaccionesHistoricas.SalidaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.ConsultarInstrumentoNegociacion_Entrada;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.DatosInstrumento;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.DatosInstrumentoNegociacion_Salida;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerNemoSVS;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Salida;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearTransacciones implements JavaCall {

	private final Logger logger = LogManager.getLogger(CrearTransacciones.class);
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	private final String SQL_SELECT_ESTADO_MODULO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_SELECT_LIBRO_ORDENES_BY_OPERADOR = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByOperador.sql");
	private final String SQL_SELECT_LIBRO_ORDENES_BY_DEFAULT = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByDefault.sql");
	private final String SQL_INSERT_TRANSACCIONES_WRAPPER = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_InsertTransaccionesWrapper.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectTransacciones.sql");
	private final String SQL_SELECT_FOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectFolios.sql");
	private final String SQL_SELECT_CONFIGURACION_OPERADOR = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionOperador.sql");
	private final String SQL_INSERT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_InsertTransacciones.sql");
	private final String SQL_INSERT_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_InsertControlMercado.sql");
	private final String SQL_SELECT_VALIDA_MONEDA_INSTRUMENTO_RF = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectValidaMonedaInstrumentoRF.sql");
	private final String SQL_SELECT_CONFIGURACION_ASIGNACION_DIRECTA = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionAsignacionDirecta.sql");
	private final String SQL_SELECT_RUTS_MARCA_NEGOCIACION = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectRutsMarcaNegociacion.sql");
	private final String SQL_SELECT_CONFIGURACION_SHORTNAME = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionShortName.sql");
	private final String SQL_UPDATE_LIBRO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_UpdateLibroOrdenes.sql");
	private final String SQL_CREATE_TABLA_TEMPORAL = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_CreateTablaTemporal.sql");
	private final String SQL_DROP_TABLA_TEMPORAL = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_DropTablaTemporal.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_InsertTablaTemporal.sql");
	
	List<LibrosOrdenes> libroOrdenesDefault = null;
	List<LibrosOrdenes> libroOrdenesPorOperador = null;
	
	private Long obtenerLibroOrdenes(Connection conn, String codigoOperadorCorredor, Long idEmpresa) throws SQLException {
		SQL sql = new SQL();
		
		if (libroOrdenesDefault == null) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", idEmpresa);
			libroOrdenesDefault = sql.query(conn, SQL_SELECT_LIBRO_ORDENES_BY_DEFAULT, params, LibrosOrdenes.class);
		}
		
		if (libroOrdenesPorOperador == null) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", idEmpresa);
			libroOrdenesPorOperador = sql.query(conn, SQL_SELECT_LIBRO_ORDENES_BY_OPERADOR, params, LibrosOrdenes.class);	
		}
		
		if(codigoOperadorCorredor == null || codigoOperadorCorredor.equals("")) {
			return libroOrdenesDefault.get(0).IdLibroOrdenes;			
		}
		else {
			for (LibrosOrdenes conf : libroOrdenesPorOperador) {
				if (conf.CodigoOperador != null && conf.CodigoOperador.equalsIgnoreCase(codigoOperadorCorredor) && conf.IdLibroOrdenes != null) {
					return conf.IdLibroOrdenes;
				}
			}
		}		
		return libroOrdenesDefault.get(0).IdLibroOrdenes;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearTransacciones_Entrada in = gson.fromJson(jin.toString(), CrearTransacciones_Entrada.class);
		CrearTransacciones_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class FolioTemp {
	    public String FolioFecha;
	    
	    public FolioTemp() {}
	}
	
	public CrearTransacciones_Salida logica(Connection conn, CrearTransacciones_Entrada in) throws Exception {
		CrearTransacciones_Salida salida = new CrearTransacciones_Salida();
		logger.info("INICIO");
		SQL _sql = new SQL();
	
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		int PlazoMenorSimultanea = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "PlazoMenorSimultanea"));
		int PlazoMayorSimultanea = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "PlazoMayorSimultanea"));
		int BaseTasaSimultaneaRvBcs = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "BaseTasaSimultaneaRvBcs"));
		int BaseTasaSimultaneaRvBec = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "BaseTasaSimultaneaRvBec"));
		
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		if (in.p_Transacciones.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_TRANSACCIONES",null,conn,in.p_IdEmpresa));
			return salida;
		}
		
		if ( in.p_Transacciones.stream().filter( transaccion -> transaccion.IdTipoOperacion.equals(_c.TIPO_OPERACION_VENTA_CORTA_RF) ).count() > 0 ) {
			HashMap<String,Object> params = new HashMap<>();
			params.put( "p_IdInstrumento", in.p_Transacciones.stream()
					.filter( transaccion -> transaccion.IdTipoOperacion.equals(_c.TIPO_OPERACION_VENTA_CORTA_RF) )
					.map( transaccion -> transaccion.IdInstrumento )
					.collect(Collectors.toList()) );
			List<InstrumentoMonedaValidar> instrumentoMonedaValidar = _sql.query(conn, SQL_SELECT_VALIDA_MONEDA_INSTRUMENTO_RF, params, InstrumentoMonedaValidar.class);
			for ( Transaccion_Entrada tran : in.p_Transacciones ) {	
				if( tran.IdTipoOperacion.equals(_c.TIPO_OPERACION_VENTA_CORTA_RF) ) {
					List<InstrumentoMonedaValidar> instrumentoMonedaValidarFilter = instrumentoMonedaValidar.stream()
							.filter( instrumento_moneda -> instrumento_moneda.IdInstrumento.equals(tran.IdInstrumento) )
							.collect( Collectors.toList() );
					if ( instrumentoMonedaValidarFilter.size() > 0 && instrumentoMonedaValidarFilter.get(0).MonedaEmisionValida.equals(_c.NO) ) {
						salida.P_OUT_ERRORESCONTADOR++;
						salida.P_OUT_MENSAJES.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_INSTRUMENTO_SIN_MONEDA_EMISION", new HashMap<>(), conn, in.p_IdEmpresa) );
						return salida;
					}
				}
			}
		}
		
		for (Transaccion_Entrada tran: in.p_Transacciones) {	
			if(tran.IdLibroOrdenes == null ) {
				tran.IdLibroOrdenes = obtenerLibroOrdenes(conn, tran.CodigoOperadorCorredor, in.p_IdEmpresa);
			}
		}
		
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (Transaccion_Entrada tran: in.p_Transacciones) {
			if (correlativos.contains(tran.IdInterno)) {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",null,conn,in.p_IdEmpresa));
				return salida;
			} else {
				correlativos.add(tran.IdInterno);
			}
		}
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdModulo", _c.ID_MODULO_ORDENES);
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<EstadoModulo> modulo = _sql.query(conn, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
		if (modulo.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",null,conn,in.p_IdEmpresa));
			return salida;
		}
		if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_c.ESTADO_MODULO_CERRADO)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",null,conn,in.p_IdEmpresa));
			return salida;
		}
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "MonedaSimultanea");
		String PermitePalosNegociacionReservado = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "FACTURACION", "PALOS_NEGOCIACION_RESERVADO");
		
		String regex = "\\d+-(?:[0-9]|K|k)"; // formato marca negociacion solo rut ejemplo : 2345-K    o    1234-6
		Pattern pattern = Pattern.compile(regex);
		
		String regex2 = "\\d+-(?:[0-9]|K|k);[a-zA-Z0-9'’\\-|]+";
		Pattern pattern2 = Pattern.compile(regex2);
		
		_sql.batch(conn,SQL_INSERT_TRANSACCIONES_WRAPPER, in.p_Transacciones, Transaccion_Entrada.class);
		List<InformacionTransaccion> _infotransacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<>(), InformacionTransaccion.class);
		HashMap<Long,Object> infoTransacciones = Utils.List2Hash(_infotransacciones, "IdInterno");
		
		ArrayList<Transaccion_Proceso> transaccionesConErrores = new ArrayList<>();
		ArrayList<Transaccion_Proceso> transacciones = new ArrayList<>();
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(conn, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        
		List<String> codigosOperadorDirecto = new ArrayList<>();
		HashMap<String,InformacionOperador> infoOperadoresDirectos =new HashMap<>(in.p_Transacciones.size(),1.0f);
        
		for (Transaccion_Entrada tran : in.p_Transacciones) {
			if (!codigosOperadorDirecto.contains(tran.CodigoOperadorCorredor)) {
				codigosOperadorDirecto.add(tran.CodigoOperadorCorredor);
			}
			if (!codigosOperadorDirecto.contains(tran.CodigoOperadorContraparte) && tran.IndicadorOrdenDirecta != null && !tran.IndicadorOrdenDirecta.equalsIgnoreCase("")) {
				codigosOperadorDirecto.add(tran.CodigoOperadorContraparte);
			}
		}
		HashMap<String,Object> param = new HashMap<>();
		param.put("IdEmpresa", in.p_IdEmpresa);
		param.put("codigosOperador", codigosOperadorDirecto);
		if (codigosOperadorDirecto.size() > 0) {
			for(InformacionOperador io : _sql.query(conn, SQL_SELECT_CONFIGURACION_OPERADOR, param, InformacionOperador.class)) {
				infoOperadoresDirectos.put(claveMapa(io.CodigoOperador,io.IdTipoInstrumento), io);
			}
			
		}
		param.clear();
		
		
		HashMap<String,ConfiguracionShortName> ConfiguracionesShortNamePorPortafolio = new HashMap<>(in.p_Transacciones.size(),1.0f);
		for(ConfiguracionShortName sn :_sql.query(conn, SQL_SELECT_CONFIGURACION_SHORTNAME, param, ConfiguracionShortName.class)) {
			ConfiguracionesShortNamePorPortafolio.put(claveMapa(sn.ShortName,sn.IdTipoInstrumento), sn);
		}
		
		param.clear();
		
		
		param.put("IdEmpresa", in.p_IdEmpresa);
		HashMap<String,ConfiguracionAsignacion> ConfiguracionesAsignacion = new HashMap<>(in.p_Transacciones.size(),1.0f);
		List<ConfiguracionAsignacion> configuracionesAsignacion = new ArrayList<ConfiguracionAsignacion>();
		configuracionesAsignacion = _sql.query(conn, SQL_SELECT_CONFIGURACION_ASIGNACION_DIRECTA, param, ConfiguracionAsignacion.class);
		for(ConfiguracionAsignacion Ca: configuracionesAsignacion ) {
			ConfiguracionesAsignacion.put(claveMapa(Ca.IdTipoInstrumento), Ca);
		}
		param.clear();
		
		
		List<String> listaRutsIn = new ArrayList<String>();
		for (Transaccion_Entrada tran : in.p_Transacciones) {
			if(tran.MarcaNegociacion != null) {
				String[] parts = tran.MarcaNegociacion.split(";");
				parts[0] = parts[0].trim().toUpperCase();
				if(parts[0] != null && parts[0].length() >0) {
					listaRutsIn.add(parts[0]);				
				}				
			}
			if(tran.MarcaNegociacionContraparte != null &&  tran.MarcaNegociacion != null && !tran.MarcaNegociacionContraparte.equalsIgnoreCase(tran.MarcaNegociacion) ) {
				String[] parts = tran.MarcaNegociacionContraparte.split(";");
				parts[0] = parts[0].trim().toUpperCase();
				if(parts[0] != null && parts[0].length() >0) {
					listaRutsIn.add(parts[0]);				
				}
			}
			
		}
		HashMap<String,Object> paramsRuts = new HashMap<>(in.p_Transacciones.size(),1.0f);
		listaRutsIn  = listaRutsIn.stream().distinct().collect(Collectors.toList());
		paramsRuts.put("Rut", listaRutsIn);
		HashMap<String, RutPortafolio> ListaRutsPrelacion = new HashMap<>(in.p_Transacciones.size(),1.0f);

		for (RutPortafolio rp : _sql.query(conn, SQL_SELECT_RUTS_MARCA_NEGOCIACION, paramsRuts, RutPortafolio.class)) {
			ListaRutsPrelacion.put(claveMapa(rp.DocumentoIdentidad,rp.CodigoPortafolio), rp);
		}
		paramsRuts.clear();

		
		
		for (Transaccion_Entrada tranE : in.p_Transacciones) {
			Transaccion_Proceso tranP = new Transaccion_Proceso();
			InformacionTransaccion info = (InformacionTransaccion) infoTransacciones.get(tranE.IdInterno);
			
			tranP.IdInterno = tranE.IdInterno;
			tranP.IdTransaccion = info.IdTransaccion;
			tranP.Folio = tranE.FolioTransaccion;
			tranP.FechaSistemaCreacion = Fecha.toDate(tranE.FechaCreacion);
			tranP.HoraSistemaCreacion = Fecha.toTime(tranE.HoraCreacion);
			tranP.EsModificacionMarketData = tranE.EsModificacionMarketData;
			tranP.EstadoModificacion = tranE.EstadoModificacion;
			tranP.IdTransaccionHistoricoAnulacion = tranE.IdTransaccionHistoricoAnulacion;
			//FechaCreacion es la Fecha Local en que se crea el palo
			//HoraCreacion es la hora UTC en que se crea el palo
			//para calcular la fecha operacion en UTC para guardarla en BD se calcula toFechaHoraUTC(FechaCreacion + toHoraLocal(HoraCreacion))
	        tranP.FechaOperacion = Fecha.fechaSistemaHoraUTC_fechaHoraUTC(tranE.FechaCreacion, tranP.HoraSistemaCreacion);
			
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && _c.nemosBolson.contains(info.NemotecnicoInstrumento)) {
				tranP.Nemotecnico = tranE.NemotecnicoEspecificoBolson;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && _c.bonoReconocimiento.contains(info.NemotecnicoInstrumento)) {
				tranP.Nemotecnico = tranE.NemotecnicoEspecificoBolson;
			}
			else {
				tranP.Nemotecnico = info.NemotecnicoInstrumento;
			}
			
			// Asignacion de camara CCLV a las transacciones para Clearing (esto no indica que vaya a clearing ya que existen otras condiciones que van en la consulta y procesamiento de clearing)
			// Esta logica debe ser la misma en:  CrearTransaccion.java / ModificarTransaccion.js / ConsultarClearingFinanciero.sql / ModificarTransaccionMarketData.java
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && tranE.IdCondicionDeLiquidacionContado.equalsIgnoreCase("T2"))
			{   // Toda operacion RV, CN (Incluye simultaneas)) --> Camara RV
				tranP.IdCamara = "RV";
			}
			else if ((info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) || info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) && tranE.IdCondicionDeLiquidacionContado.equalsIgnoreCase("T1"))
			{   // Toda operacion RF o IF  T1 --> Camara PM
				tranP.IdCamara = "PM";
			}
			else if ((info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) || info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) && tranE.IdCondicionDeLiquidacionContado.equalsIgnoreCase("T0"))
			{   // Toda operacion RF o IF  T0 --> Camara PH
				tranP.IdCamara = "PH";
			}
			else if ((info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) || info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) && tranE.IdCondicionDeLiquidacionContado.equalsIgnoreCase("T2"))
			{   // Toda operacion RF o IF  T2 --> Camara PH
				tranP.IdCamara = "PH";
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) && tranE.IdCondicionDeLiquidacionContado.equalsIgnoreCase("T0"))
			{   // Toda operacion de SIMULTANEA, T0 --> Camara SM
				tranP.IdCamara = "SM";
			}
			else
			{
				tranP.IdCamara = "";	
			}

			
			
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && _c.bonoReconocimiento.contains(info.NemotecnicoInstrumento)) {
				
				String nemoBase = _c.bonoReconocimiento.get(_c.bonoReconocimiento.indexOf(info.NemotecnicoInstrumento));
				
				String codigoMes =  tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length(), nemoBase.length()+1);
				String codigoAgno =  tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length()+2, nemoBase.length()+3);
				String mes = _c.codigosMesEmisionBonoReconocimiento.get(codigoMes);
				String agno = _c.codigosAgnoEmisionBonoReconocimiento.get(codigoAgno);
				
				tranP.FechaEmision = Fecha.toDate(agno + "-" + mes + "-01");
				
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				tranP.FechaEmision = Fecha.toDate(tranE.FechaEmision);
			}
			else {
				tranP.FechaEmision = info.FechaEmisionInstrumento;
			}
			
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && _c.nemosBolson.contains(info.NemotecnicoInstrumento)) {
				
				//CERO, XERO, ZERO
				String dia = tranE.NemotecnicoEspecificoBolson.substring(4, 6);
				String mes = tranE.NemotecnicoEspecificoBolson.substring(6, 8);
				String agno = tranE.NemotecnicoEspecificoBolson.substring(8, 10);
				if (Integer.parseInt(agno) > 49) {
					agno = "19" + agno;
				}
				else {
					agno = "20" + agno;
				}
				tranP.FechaVencimiento = Fecha.toDate(agno + "-" + mes + "-" + dia);
					
				
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && (_c.nemosBolson.contains(info.NemotecnicoInstrumento) || _c.bonoReconocimiento.contains(info.NemotecnicoInstrumento))) {
				
				String nemoBase = _c.bonoReconocimiento.get(_c.bonoReconocimiento.indexOf(info.NemotecnicoInstrumento));
				
				String dia = tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length()+2, nemoBase.length() + 4);
				String mes = tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length() + 4, nemoBase.length() + 6);
				String agno = tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length() + 6, nemoBase.length() + 8);
				if (Integer.parseInt(agno) > 49) {
					agno = "19" + agno;
				}
				else {
					agno = "20" + agno;
				}
				tranP.FechaVencimiento = Fecha.toDate(agno + "-" + mes + "-" + dia);
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
				tranP.FechaVencimiento = info.FechaVencimientoInstrumento;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
				tranP.FechaVencimiento = info.FechaVencimientoInstrumento;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				tranP.FechaVencimiento = Fecha.toDate(tranE.FechaVencimiento);
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) && !tranE.FechaVencimiento.equalsIgnoreCase("0001-01-01")) {
				tranP.FechaVencimiento = Fecha.toDate(tranE.FechaVencimiento);
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				tranP.FechaVencimiento = Fecha.toDate(tranE.FechaVencimiento);
			}
			else {
				tranP.FechaVencimiento = Fecha.toDate("0001-01-01");
			}
			
			//TODO: Agregar llamada a Web Services.
			tranP.FechaIngresoOferta = tranP.FechaOperacion;
			tranP.IdSistemaBursatil = tranE.IdSistemaBursatil;
			
			if (tranE.IdCorredoraContraparte.equals(empresa.IdCorredora)) {
				if (!tranE.IndicadorOrdenDirecta.equalsIgnoreCase(_c.OE) && !tranE.IndicadorOrdenDirecta.equalsIgnoreCase(_c.OD)) {
					String codigo ="INDICADOR_ORDEN_DIRECTA_INCONSISTENTE";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(tranE.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					tranP.errores++;
				}
				else {
					tranP.IndicadorOrdenDirecta = tranE.IndicadorOrdenDirecta;
				}
			}
			else {
				tranP.IndicadorOrdenDirecta = "";
			}
			
			tranP.EsCuentaPropia = tranE.EsCuentaPropia;
			tranP.EsFungible = tranE.EsFungible;
			tranP.EsAPlazo = _c.NO;
			tranP.AccionOrden = info.AccionOrden;
			tranP.EsVentaCortaOD = tranE.EsVentaCorta;
			if (info.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
				tranP.EsVentaCorta = _c.NO;
			}
			else {
				tranP.EsVentaCorta = tranE.EsVentaCorta;
			}
			
			tranP.CodigoOperadorContraparte = tranE.CodigoOperadorContraparte != null ? tranE.CodigoOperadorContraparte : "";
			tranP.CodigoOperadorCorredor = tranE.CodigoOperadorCorredor != null ? tranE.CodigoOperadorCorredor : "";
		
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				tranP.IdEmisor = tranE.IdEmisor;
			}/*
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
				tranP.IdEmisor = info.IdEmisorInstrumentoSubyacente;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				tranP.IdEmisor = info.IdEmisorInstrumentoSubyacente;
			}*/
			else {
				tranP.IdEmisor = info.IdEmisorInstrumento;
			}
			
			tranP.Cantidad = tranE.Cantidad;
			tranP.Monto = tranE.Monto;
			
			tranP.MontoPlazo = tranE.MontoPlazo;
			tranP.Precio = tranE.Precio;
			tranP.PrecioPlazo = tranE.PrecioPlazo;
			tranP.Tasa = tranE.Tasa;
			
			tranP.NemotecnicoSVS = "";
			tranP.ValorRescate = tranE.ValorRescate;
			tranP.IdEstado = _c.ESTADO_TRANSACCION_INGRESADA;
			tranP.IdTransaccionPlazo = 0L;
			tranP.IdTransaccionOd = 0L;
			tranP.IdTipoInstrumento = info.IdTipoInstrumento;
			tranP.IdClasificacionTipoInstrumento = info.IdClasificacionTipoInstrumento;
			tranP.IdEmpleado = tranE.IdEmpleado;
			tranP.IdTipoOperacion = tranE.IdTipoOperacion;
			tranP.IdCondicionDeLiquidacion = tranE.IdCondicionDeLiquidacionContado;
			
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
				tranP.IdTipoReajuste = info.IdTipoReajusteInstrumento;
				tranP.IdMonedaEmision = info.IdMonedaEmisionInstrumento;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE) && info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_RRF)) {
				tranP.IdTipoReajuste = info.IdTipoReajusteInstrumento;
				tranP.IdMonedaEmision = info.IdMonedaEmisionInstrumento;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				tranP.IdTipoReajuste = tranE.IdTipoReajuste;
				tranP.IdMonedaEmision = tranE.IdMonedaEmision;
			}
			else {
				tranP.IdTipoReajuste = "";
				tranP.IdMonedaEmision = "";
			}
			
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				tranP.IdMonedaInstrumento = tranE.IdMoneda;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
				tranP.IdMonedaInstrumento = monedaSimultanea;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				tranP.IdMonedaInstrumento = tranE.IdMoneda;
			}
			else {
				tranP.IdMonedaInstrumento = info.IdMonedaInstrumento;
			}
			
			tranP.IdCorredoraContraparte = tranE.IdCorredoraContraparte;
			tranP.IdCorredoraLocal = empresa.IdCorredora;
			tranP.IdEmpresa = empresa.IdEmpresa;
			tranP.IdBolsa = tranE.IdBolsa;
			tranP.IdBoveda = tranE.IdBoveda;
			tranP.IdPaisBolsa = info.IdPaisBolsa;
			

			tranP.BolsaEsExtranjera = info.BolsaEsExtranjera;
			
			tranP.IdLibroOrdenes = tranE.IdLibroOrdenes;
			tranP.IdInstrumento = tranE.IdInstrumento;
			tranP.NemotecnicoSVS = tranE.NemotecnicoSVS;
			tranP.NemotecnicoSubyacente = info.NemotecnicoInstrumentoSubyacente;
			tranP.IdInstrumentoSubyacente = info.IdInstrumentoSubyacente;
			tranP.IdMonedaSubyacente = info.IdMonedaInstrumentoSubyacente;
			tranP.DiasLiquidacionContado = new Long(info.DiasLiquidacionContado);
			tranP.FechaParidad = Fecha.toDate("0001-01-01");
			tranP.ValorParidad = BigDecimal.ZERO;
			tranP.FechaTipoCambio = Fecha.toDate("0001-01-01");
			tranP.ValorTipoCambio = BigDecimal.ZERO;
			tranP.Plazo = tranE.Plazo;
			tranP.FechaModificacionOperacion = Fecha.toDateTime(tranE.FechaCreacion + " " + tranE.HoraCreacion);
			tranP.MarcaNegociacion =  tranE.MarcaNegociacion != null ? tranE.MarcaNegociacion: "" ;
			tranP.MarcaNegociacionContraparte = tranE.MarcaNegociacionContraparte != null ? tranE.MarcaNegociacionContraparte : "";
			tranP.IndicadorDCV = tranE.IndicadorDCV;
			tranP.EsBilateral = tranE.EsBilateral;
			tranP.EsFueraRueda = tranE.EsFueraRueda;
			tranP.FolioTransaccionPlazo = tranE.FolioTransaccionPlazo;
			tranP.CorteMinimo = info.CorteMinimo;
			tranP.ProcesaAutomatico = "N";
			tranP.IdProgramaProcesaAutomatico = 0L;
			tranP.TipoCambioCorresponsal = tranE.TipoCambioCorresponsal;
			tranP.IdCorredoraCorresponsal = tranE.IdCorredoraCorresponsal;
			tranP.MontoLiquidacionCorresponsal = tranE.MontoLiquidacionCorresponsal;
			tranP.IdMonedaLiquidacionCorresponsal = tranE.IdMonedaLiquidacionCorresponsal;
			tranP.GastosCorresponsal = tranE.GastosCorresponsal;			
			
			
			tranP.MejorPrecioCompra = tranE.MejorPrecioCompra;
			tranP.MejorPrecioVenta = tranE.MejorPrecioVenta;
			tranP.InstrumentoPermiteVentaCorta = info.InstrumentoPermiteVentaCorta;
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				tranP.BaseTasa = 360L;
				tranP.TipoInteres = "C";
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
				if (tranE.IdBolsa.equalsIgnoreCase("BCS") ) {
					if( Integer.valueOf(BaseTasaSimultaneaRvBcs )!= null || BaseTasaSimultaneaRvBcs > 0 ) {
						tranP.BaseTasa = (long) BaseTasaSimultaneaRvBcs;
					}else {
						tranP.BaseTasa = 30L;
					}	
				}else {			
					if( Integer.valueOf(BaseTasaSimultaneaRvBec )!= null || BaseTasaSimultaneaRvBec > 0 ) {
						tranP.BaseTasa = (long) BaseTasaSimultaneaRvBec;
					}else {
						tranP.BaseTasa = 30L;
					}
				}
				tranP.TipoInteres = "S";
			}else {
				tranP.BaseTasa = 0L;
				tranP.TipoInteres = "";
			}
			
			tranP.IdEstadoLibroOrdenes = info.IdEstadoLibroOrdenes;
			tranP.NemotecnicoRelacionado = info.NemotecnicoRelacionado;
			tranP.IdInstrumentoRelacionado = info.IdInstrumentoRelacionado;
			tranP.IdTipoOperacionRelacionada = info.IdTipoOperacionRelacionada;
			
			tranP.Intereses = tranE.Intereses;   //RF PERU
			tranP.Reajustes = tranE.Reajustes;   //RF PERU
			tranP.EsRuteo = tranE.EsRuteo;
			tranP.Reservado = (tranP.EsRuteo!=null && "S".equals(tranP.EsRuteo)) ? "S" : "N"; 
			if(!"S".equalsIgnoreCase(PermitePalosNegociacionReservado)) {
				tranP.IdUsuarioReserva = (tranP.EsRuteo!=null && "S".equals(tranP.EsRuteo)) ? in.p_IdUsuario : 0L;
			}else {
				tranP.IdUsuarioReserva = in.p_IdUsuario;
			}	

			tranP.FlujoOrigen = tranE.FlujoOrigen;
			tranP.IdMonedaRaiz = tranE.IdMoneda;
			tranP.PrecioRaiz =  info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) ? BigDecimal.ZERO : tranE.Precio;
			tranP.MontoRaiz = info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) ? BigDecimal.ZERO : tranE.Monto;
			tranP.IdInstrumentoNegociacion = 0L;
			
			transacciones.add(tranP);
		}
		
		
		
		limpiarTransacciones(transacciones,transaccionesConErrores);
		if (transacciones.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		//FECHA LIQUIDACION CONTADO
		{
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = empresa.IdPais;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			
			for (Transaccion_Proceso tran : transacciones) {
				FechasIn f = new FechasIn();
				f.fechaInicial = Fecha.toStringDate(tran.FechaSistemaCreacion);
				if (tran.DiasLiquidacionContado == null) {
					tran.DiasLiquidacionContado = 0L;
				}
				f.numeroDias = new Long(tran.DiasLiquidacionContado);
				inObtenerDiaSiguiente.p_fechas.add(f);
				
			}
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(conn, inObtenerDiaSiguiente);
			for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				for (Transaccion_Proceso tran : transacciones) {
					if (Fecha.toStringDate(tran.FechaSistemaCreacion).equalsIgnoreCase(fecha.fechaInicial) && tran.DiasLiquidacionContado.equals(fecha.numeroDiasInicial)){
						tran.FechaLiquidacion = Fecha.toDate(fecha.fechaCalculada);
					}
					
				}
			}
		}
		
		//CALCULAR PLAZO PARA SIMULTANEAS POR RUTEO, TRAEN VENCIMIENTO, PERO NO PLAZO
		{
			ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
			entradaObtenerPlazo.p_IdPais = empresa.IdPais;
			
			for (Transaccion_Proceso tran : transacciones) {
				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE") || tran.IdTipoInstrumento.equalsIgnoreCase("SIM")) {
					if (!Fecha.toStringDate(tran.FechaVencimiento).equalsIgnoreCase("0001-01-01") && tran.Plazo == -1L) {
						ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
						f.FechaDesde = Fecha.toStringDate(tran.FechaLiquidacion);
						f.FechaHasta = Fecha.toStringDate(tran.FechaVencimiento);
						f.IdInterno = tran.IdInterno;
						f.SoloDiasHabiles = "N";
						entradaObtenerPlazo.p_Fechas.add(f);
					}
				}
			}
			
			if (entradaObtenerPlazo.p_Fechas.size() > 0) {
				ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
				OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
				for (cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut fecha : salidaObtenerPlazo.p_out_Fechas) {
					for (Transaccion_Proceso tran : transacciones) {
						if (tran.IdInterno.equals(fecha.IdInterno)) {
							
							tran.Plazo = fecha.Plazo;
							
						}
					}
					
				}
			}
			
		}
		
		
		//FECHA VENCIMIENTO PARA SIMULTANEAS
		{
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = empresa.IdPais;
			inObtenerDiaSiguiente.p_diaHabil = "N";
			
			for (Transaccion_Proceso tran : transacciones) {
				if ((tran.IdTipoInstrumento.equalsIgnoreCase("SIM") || tran.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") || tran.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE") )&& Fecha.toStringDate(tran.FechaVencimiento).equalsIgnoreCase("0001-01-01")) {
					FechasIn f = new FechasIn();
					f.fechaInicial = Fecha.toStringDate(tran.FechaLiquidacion);
					f.numeroDias = tran.Plazo;
					inObtenerDiaSiguiente.p_fechas.add(f);
				}
				
			}
			
			if (inObtenerDiaSiguiente.p_fechas.size() > 0) {
				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(conn, inObtenerDiaSiguiente);
				for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
					for (Transaccion_Proceso tran : transacciones) {
						if (Fecha.toStringDate(tran.FechaLiquidacion).equalsIgnoreCase(fecha.fechaInicial) && tran.Plazo.equals(fecha.numeroDiasInicial)){
							tran.FechaVencimiento= Fecha.toDate(fecha.fechaCalculada);
						}
						
					}
				}
			}
		}
		
		
		// CONSULTAR INSTRUMENTO NEGOCIACION

		ConsultarNemoNegociacion consultarNemoNegociable = new ConsultarNemoNegociacion();
		ConsultarInstrumentoNegociacion_Entrada inConsultarInstrumentoNegociable = new ConsultarInstrumentoNegociacion_Entrada();
		
		if ( transacciones.stream().filter( transaccion -> transaccion.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RV) ).count() > 0 ) {
			for (Transaccion_Proceso tran : transacciones) {
				DatosInstrumento nemoRaiz = new DatosInstrumento();
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {					
					nemoRaiz.IdInstrumentoRaiz = tran.IdInstrumento;
					nemoRaiz.IdCondLiquidacion = tran.IdCondicionDeLiquidacion;
					nemoRaiz.IdMoneda = tran.IdMonedaRaiz;
					nemoRaiz.IdConsulta = new Long(tran.Folio);		
					inConsultarInstrumentoNegociable.p_DatosInstrumentoConsulta.add(nemoRaiz);					
				}
			}
			
			DatosInstrumentoNegociacion_Salida nemoNegociable = consultarNemoNegociable.logica(conn, inConsultarInstrumentoNegociable);	
			for (Transaccion_Proceso tran : transacciones) {							
				if (tran.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RV)) {					
			        nemoNegociable.P_InstrumentoSalida.stream()
		            .filter(nemo -> nemo.IdConsulta.equals(new Long(tran.Folio)) &&
		                            !nemo.IdInstrumento.equals(0L) &&
		                            !nemo.Nemotecnico.equals(""))
		            .findFirst() 
		            .ifPresent(nemo -> {						
						tran.IdInstrumentoNegociacion = nemoNegociable.P_InstrumentoSalida.get(nemoNegociable.P_InstrumentoSalida.indexOf(nemo)).IdInstrumento;						
		            });
			        
			        if (nemoNegociable.P_InstrumentoSalida.stream()
			                .noneMatch(nemo -> nemo.IdConsulta.equals(new Long(tran.Folio)) &&
			                                   !nemo.IdInstrumento.equals(0L) &&
			                                   !nemo.Nemotecnico.equals(""))) {			        	
			        	
			                tran.PrecioRaiz = BigDecimal.ZERO;
			                tran.MontoRaiz = BigDecimal.ZERO;
			                tran.IdMonedaRaiz = "";
			        }					
				}
			}						
								
			CalcularParidad.CalcularParidadEntrada CalcParidad = new CalcularParidad.CalcularParidadEntrada();	
			CalcularParidad CP = new CalcularParidad();
				
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && !tran.IdMonedaRaiz.equals("") && !tran.IdMonedaInstrumento.equals(tran.IdMonedaRaiz)) {
					CalcularParidad.CalculoEntrada calcularParidad = new CalcularParidad.CalculoEntrada();
					calcularParidad.IdTrabajo 		= new Long(tran.Folio);
					calcularParidad.IdInterno 		= new Long(tran.Folio);
					calcularParidad.Fecha			= fechaSistema;
					calcularParidad.IdMonedaOrigen 	= tran.IdMonedaRaiz;
					calcularParidad.MontoOrigen		= tran.Precio;
					calcularParidad.IdMonedaDestino = tran.IdMonedaInstrumento;
					calcularParidad.IdUso			= "CARTERA";
					CalcParidad.p_CalculosEntrada.add(calcularParidad);	
				}			
			}			
					
			CalcParidad.p_IdEmpresa 		= in.p_IdEmpresa; 
			CalcParidad.p_IdTrabajo 		= -100L;
			CalcularParidad.CalcularParidadSalida ResultParidad = CP.logica(conn, CalcParidad);
						
			HashMap<Long,Object> RCP = Utils.List2Hash(ResultParidad.p_out_CalculosSalida, "IdInterno");	
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")  && !tran.IdMonedaRaiz.equals("")) {
					if (tran.IdMonedaInstrumento.equals(tran.IdMonedaRaiz)){
						tran.PrecioRaiz = 	tran.Precio;
						tran.MontoRaiz = tran.Monto;					
					}
					else				
					{
						CalcularParidad.CalculoSalida resultadoParidad = (CalcularParidad.CalculoSalida) RCP.get(new Long(tran.Folio));
						tran.PrecioRaiz = 	(resultadoParidad.MontoDestino);
						tran.MontoRaiz = tran.Cantidad.multiply(resultadoParidad.MontoDestino);
					}
				}else{
					tran.PrecioRaiz = tran.Precio;
					tran.MontoRaiz = tran.Monto;	
					tran.IdMonedaRaiz = tran.IdMonedaInstrumento;
				}
			}	
		}
		
		
		//VALIDACION FOLIO REPETIDO
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		ArrayList<String> foliosFechas = new ArrayList<>();
		for (Transaccion_Proceso tran : transacciones) {
			if (!foliosFechas.contains(tran.Folio + " " + tran.FechaSistemaCreacion)) {
				foliosFechas.add(tran.Folio + " " + tran.FechaSistemaCreacion);
			}
		}
		
		for (Transaccion_Proceso tran : transacciones) {
			if (!foliosFechas.contains(tran.FolioTransaccionPlazo + " " + tran.FechaSistemaCreacion)) {
				if ((tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) ) {
					foliosFechas.add(tran.FolioTransaccionPlazo + " " + tran.FechaSistemaCreacion);
				}
				else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
					foliosFechas.add(tran.FolioTransaccionPlazo + " " + tran.FechaSistemaCreacion);
				}
			}
			
		}
		/*
		params1.put("foliosFechas", foliosFechas);
		params1.put("idEmpresa", empresa.IdEmpresa);
		List<FolioExistente> foliosExistentes = _sql.query(conn, SQL_SELECT_FOLIOS, params1, FolioExistente.class);
		*/
		
		List<FolioExistente> foliosExistentes = new ArrayList<>();

		try {
		    SQL.exec(conn, SQL_CREATE_TABLA_TEMPORAL);

		    if (!foliosFechas.isEmpty()) {

		        List<FolioTemp> foliosTemp = new ArrayList<>();
		        for (String folioFecha : foliosFechas) {
		            FolioTemp temp = new FolioTemp();
		            temp.FolioFecha = folioFecha;
		            foliosTemp.add(temp);
		        }
		        _sql.batch(conn, SQL_INSERT_TABLA_TEMPORAL, foliosTemp, FolioTemp.class);
		    }
		    
		    params1.put("idEmpresa", empresa.IdEmpresa);
		    foliosExistentes = _sql.query(conn, SQL_SELECT_FOLIOS, params1, FolioExistente.class);
		    
		} finally {
		    try {
		        SQL.exec(conn, SQL_DROP_TABLA_TEMPORAL);
		    } catch (Exception e) {
		        logger.warn("No se pudo eliminar tabla temporal: " + e.getMessage());
		    }
		}
		
		//VALIDACIONES TRANSACCION
		{
			for (Transaccion_Proceso tran : transacciones) {
				ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Folio.length() == 0), "FOLIO_TRANSACCION_VACIO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);	
				ProcesaRequisito(conn, in.p_IdEmpresa,(foliosExistentes.contains(new FolioExistente(tran.IdTransaccion, tran.Folio + " " + tran.FechaSistemaCreacion))), "FOLIO_TRANSACCION_NO_DISPONIBLE", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) || tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Precio.compareTo(BigDecimal.ZERO) != 1), "PRECIO_MENOR_CERO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.IdMonedaEmision == null), "MONEDA_EMISION_OBLIGATORIA", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
					ProcesaRequisito(conn, in.p_IdEmpresa, (foliosExistentes.contains(new FolioExistente(tran.IdTransaccion, tran.FolioTransaccionPlazo + " " + tran.FechaSistemaCreacion))), "FOLIO_TRANSACCION_PLAZO_NO_DISPONIBLE", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Folio != null && tran.Folio.equalsIgnoreCase(tran.FolioTransaccionPlazo)), "FOLIO_TRANSACCION_IGUAL_FOLIO_TRANSACCION_PLAZO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					ProcesaRequisito(conn,in.p_IdEmpresa, (tran.Plazo < PlazoMenorSimultanea || tran.Plazo > PlazoMayorSimultanea), "PLAZO_INVALIDO_NO_SE_ENCUENTRA_DENTRO_DEL_RANGO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)){
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.PrecioRaiz.equals(BigDecimal.ZERO)), "UI_INSTRUMENTO_NEGOCIADO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
			
				//Validaciones para bilaterales por ingreso manual. Las transacciones que cumplan para ser bilaterales denen tener la marca bilateral
				//Estas validaciones estan en CrearTransaccion.java y ServicioCumpleRequisitosModificarTransaccion.java (mantener en ambos lados igual)
				if (tran.IdSistemaBursatil.equalsIgnoreCase("MANUAL") && tran.EsBilateral.equalsIgnoreCase("N") && localizacionSistema.equalsIgnoreCase("CL"))
				{

					ProcesaRequisito(conn,in.p_IdEmpresa, (tran.BolsaEsExtranjera.equalsIgnoreCase("S")), "TRANSACCION_BOLSA_EXTRANJERA_DEBE_SER_BILATERAL", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					ProcesaRequisito(conn,in.p_IdEmpresa, (!tran.IdMonedaInstrumento.equalsIgnoreCase("CLP")), "TRANSACCION_MONEDA_EXTRANJERA_DEBE_SER_BILATERAL", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					
					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && !tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
						//Transacciones RV CLP, PH o PM no puede ser compensadas, debe liquidarse bilateral
						ProcesaRequisito(conn,in.p_IdEmpresa, (tran.IdMonedaInstrumento.equalsIgnoreCase("CLP") && tran.IndicadorOrdenDirecta.equalsIgnoreCase("") && (tran.IdCondicionDeLiquidacion.equalsIgnoreCase("T0") || tran.IdCondicionDeLiquidacion.equalsIgnoreCase("T1"))), "TRANSACCION_DEBE_SER_BILATERAL", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					}

					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)){
						//Transacciones de Simultanea PM no puede ser compensadas, debe ser bilaterales
						ProcesaRequisito(conn,in.p_IdEmpresa, (tran.IdMonedaInstrumento.equalsIgnoreCase("CLP") && tran.IndicadorOrdenDirecta.equalsIgnoreCase("") && tran.IdCondicionDeLiquidacion.equalsIgnoreCase("T1")), "TRANSACCION_DEBE_SER_BILATERAL", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					}										
					
				} // fin validaciones para bilaterales por ingreso manual

				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.PrecioPlazo.compareTo(BigDecimal.ZERO) != 1), "PRECIO_PLAZO_MENOR_CERO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,(foliosExistentes.contains(new FolioExistente(tran.IdTransaccionPlazo, tran.FolioTransaccionPlazo + " " + tran.FechaSistemaCreacion))), "FOLIO_TRANSACCION_PLAZO_NO_DISPONIBLE", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Folio != null && tran.Folio.equalsIgnoreCase(tran.FolioTransaccionPlazo)), "FOLIO_TRANSACCION_IGUAL_FOLIO_TRANSACCION_PLAZO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
					ProcesaRequisito(conn,in.p_IdEmpresa, (tran.Plazo <= 0), "PLAZO_INVALIDO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
				
				if (!tran.IdPaisBolsa.equals(empresa.IdPais)) {
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran.IdCorredoraCorresponsal == null || tran.IdCorredoraCorresponsal == 0), "CORREDOR_CORRESPONSAL_NO_VALIDO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
				
				ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Cantidad.compareTo(BigDecimal.ZERO) != 1), "CANTIDAD_MENOR_CERO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && tran.CorteMinimo.compareTo(BigDecimal.ZERO) == 1) {
					ProcesaRequisito(conn,in.p_IdEmpresa, (tran.Cantidad.remainder(tran.CorteMinimo).compareTo(BigDecimal.ZERO) != 0), "CORTE_MINIMO_INVALIDO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				}
				
				ProcesaRequisito(conn, in.p_IdEmpresa,(tran.EsFungible.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.ValorRescate.compareTo(BigDecimal.ZERO) != 1), "VALOR_RESCATE_MENOR_CERO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa,(tran.EsVentaCorta.equalsIgnoreCase(_c.SI) && tran.InstrumentoPermiteVentaCorta.equalsIgnoreCase(_c.NO)), "INSTRUMENTO_NO_PERMITE_VENTA_CORTA", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa,(tran.IdEstadoLibroOrdenes.equalsIgnoreCase(_c.ESTADO_LIBRO_CERRADO)), "LIBRO_ORDENES_CERRADO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa,tran.GastosCorresponsal.compareTo(BigDecimal.ZERO) != 0 && (tran.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) == 0 || tran.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) == 0), "DATOS_CORRESPONSAL_INCOMPLETOS", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa,tran.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) != 0 && (tran.GastosCorresponsal.compareTo(BigDecimal.ZERO) == 0 || tran.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) == 0), "DATOS_CORRESPONSAL_INCOMPLETOS", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa,tran.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) != 0 && (tran.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) == 0 || tran.GastosCorresponsal.compareTo(BigDecimal.ZERO) == 0), "DATOS_CORRESPONSAL_INCOMPLETOS", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);				

				
				if (tran.GastosCorresponsal.compareTo(BigDecimal.ZERO) != 0 && tran.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) != 0 && tran.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) != 0 )
				{		//Valida que el monto liquidación corresponsal ingresado, esté correcto	
						BigDecimal montoMonedaExtranjera = tran.Monto.divide(tran.TipoCambioCorresponsal,6,BigDecimal.ROUND_HALF_UP);
						BigDecimal epsilon = new BigDecimal("0.5");    //variación permitida
						
						BigDecimal valorCompra = ((tran.MontoLiquidacionCorresponsal.subtract(tran.GastosCorresponsal)).subtract(montoMonedaExtranjera));
						BigDecimal valorVenta = ((tran.MontoLiquidacionCorresponsal.add(tran.GastosCorresponsal)).subtract(montoMonedaExtranjera));
				
						if (tran.AccionOrden.equalsIgnoreCase("COMPRA") && (valorCompra.compareTo(epsilon) == 1 || valorCompra.compareTo(epsilon.multiply(new BigDecimal(-1))) == -1))
						{
							tran.MontoLiquidacionCorresponsal = montoMonedaExtranjera.add(tran.GastosCorresponsal);   // Obtiene MontoLiquidacionCorresponsal correcto(para mensaje de salida)
							ProcesaRequisito(conn, in.p_IdEmpresa, true, "VALOR_LIQUIDACION_CORRESPONSAL_ERRONEO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
						}
						
						if (tran.AccionOrden.equalsIgnoreCase("VENTA") && (valorVenta.compareTo(epsilon) == 1 || valorVenta.compareTo(epsilon.multiply(new BigDecimal(-1))) == -1))
						{
							tran.MontoLiquidacionCorresponsal = montoMonedaExtranjera.subtract(tran.GastosCorresponsal);// Obtiene MontoLiquidacionCorresponsal correcto(para mensaje de salida)
							ProcesaRequisito(conn, in.p_IdEmpresa, true, "VALOR_LIQUIDACION_CORRESPONSAL_ERRONEO", MensajeSalida.MENSAJE_ERROR, tran, salida.P_OUT_MENSAJES);
						}
						
				}
				
			}
			
		}

		// GUARDAR HISTORICO INGRESO PARA MODIFICACION MARKET DATA
		limpiarTransacciones(transacciones,transaccionesConErrores);
		
		ArrayList<Transaccion_Proceso> modificacionesMarketDataConErrores = transaccionesConErrores.stream().filter(t -> "S".equals(t.EsModificacionMarketData)).collect(Collectors.toCollection(ArrayList::new));
		
		if (!modificacionesMarketDataConErrores.isEmpty()) {
			
			//CONTAMOS TRANSACCIONES OD Y PLAZO
			List<Long> idsTransaccion = obtenerIdsTransacciones(conn, modificacionesMarketDataConErrores);
			
			// OBTENEMOS ORDENES DIRECTAS
	 		procesarOrdenDirecta(modificacionesMarketDataConErrores, idsTransaccion);
	 		
	 		ArrayList<Transaccion> transaccionesFinales = new ArrayList<>();
	 		
	 		for (Transaccion_Proceso tranP : modificacionesMarketDataConErrores) {
				Transaccion tranF = new Transaccion();
				Utils.copyPojo(tranP, tranF);
				tranF.IdMoneda = tranP.IdMonedaInstrumento;

				if (tranP.IdClasificacionTipoInstrumento.equals("RV")){
					tranF.IdMoneda = tranP.IdMonedaRaiz;
					tranF.IdMonedaRaiz= tranP.IdMonedaInstrumento;
				}
				
				tranF.IdMonedaEmision = tranP.IdMonedaEmision;
				transaccionesFinales.add(tranF);
			}
	 		
			insertarTransaccionesHistoricas(conn, in, salida, transaccionesFinales);
		}
		
		if (transacciones.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}		
		
		//OBTENER NEMO SVS
		{
			List<Instrumento_Entrada> instrumentosNemoSVS = new ArrayList<>();
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.NemotecnicoSVS.equalsIgnoreCase("")) {
					Instrumento_Entrada e = new Instrumento_Entrada();
					e.FechaVencimiento = Fecha.toStringDate(tran.FechaVencimiento);
					e.IdEmisor = tran.IdEmisor;
					e.IdInterno = tran.IdInterno;
					e.IdTipoReajuste = tran.IdTipoReajuste;
					e.Nemotecnico = tran.Nemotecnico;
					e.PagaderoEn = tran.IdMonedaInstrumento;
					e.Plazo = tran.Plazo;
					instrumentosNemoSVS.add(e);
				}
			}
			
			ObtenerNemoSVS serviceObtenerNemoSVS = new  ObtenerNemoSVS();
			ObtenerNemoSVS_Entrada entradaObtenerNemoSVS = new ObtenerNemoSVS_Entrada();
			
			entradaObtenerNemoSVS.p_Instrumentos = instrumentosNemoSVS;
			if (instrumentosNemoSVS.size() > 0) {
				ObtenerNemoSVS_Salida salidaObtenerNemoSVS = serviceObtenerNemoSVS.logica(conn, entradaObtenerNemoSVS);
				
				for (Instrumento_Salida inst : salidaObtenerNemoSVS.p_out_Instrumento) {
					
					for (Transaccion_Proceso tran : transacciones) {
						if (tran.IdInterno.equals(inst.IdInterno)) {
							
							tran.NemotecnicoSVS = inst.NemoSVS;
							
							
						}
					}
					
				}
				
			}
		}
		
		//CONTAMOS TRANSACCIONES OD Y PLAZO
		List<Long> idsTransaccion = obtenerIdsTransacciones(conn, transacciones);
		
		// OBTENEMOS ORDENES DIRECTAS
 		int pos = procesarOrdenDirecta(transacciones, idsTransaccion);
		
		//TRANSACCIONES SIMULTANEA Y REPORTES
		{
			ArrayList<Transaccion_Proceso> _transaccionesPlazo = new ArrayList<>();
			for (Transaccion_Proceso tran : transacciones) {
				
				if ((tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM))
						|| tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
					
					Transaccion_Proceso tranPlazo = new Transaccion_Proceso();
					Utils.copyPojo(tran, tranPlazo);
					tranPlazo.IdTransaccion = idsTransaccion.get(pos++);
					tranPlazo.IdTransaccionPlazo = tran.IdTransaccion;
					tran.IdTransaccionPlazo = tranPlazo.IdTransaccion;
					tranPlazo.Folio = tran.FolioTransaccionPlazo;
					tranPlazo.FechaLiquidacion = tran.FechaVencimiento;
					tranPlazo.EsAPlazo = "S";
					tranPlazo.Monto = tran.MontoPlazo;
					tranPlazo.Precio = tran.PrecioPlazo;
					tranPlazo.IdCondicionDeLiquidacion = "TP";
					
					if (tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)
							|| tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)
							|| tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV)
							|| tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV)
							|| tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)
							|| tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF)) {
						
						tranPlazo.NemotecnicoRelacionado = tranPlazo.Nemotecnico;
						tranPlazo.IdInstrumentoRelacionado = tranPlazo.IdInstrumento;
						tranPlazo.Nemotecnico = tran.NemotecnicoRelacionado;
						tranPlazo.IdInstrumento= tran.IdInstrumentoRelacionado;
					}
					tranPlazo.IdTipoOperacionRelacionada = tranPlazo.IdTipoOperacion;
					tranPlazo.IdTipoOperacion= tran.IdTipoOperacionRelacionada;
					_transaccionesPlazo.add(tranPlazo);
				}
			}
			transacciones.addAll(_transaccionesPlazo);
		}
		
		//correccion campos OD simultaneas plazo
		for (Transaccion_Proceso tranP : transacciones) { 
			if (tranP.EsAPlazo.equalsIgnoreCase(_c.SI)) {
				for (Transaccion_Proceso tranPlazoMala : transacciones) {
					if (tranP.IdTransaccionOd.equals(tranPlazoMala.IdTransaccion)) {
						tranP.IdTransaccionOd = tranPlazoMala.IdTransaccionPlazo;
						break;
					}
				}
			}
		}
		
		

		List<Transaccion_Proceso> transaccionesPreladas = ProcesarPrelacion(conn, _sql, in, pattern, pattern2, transacciones,infoTransacciones , infoOperadoresDirectos, ConfiguracionesShortNamePorPortafolio, ListaRutsPrelacion,ConfiguracionesAsignacion,PermitePalosNegociacionReservado,fechaSistema);
		
		ArrayList<Transaccion> transaccionesFinales = obtenerTransaccionesProceso(conn, in, salida, transacciones);
	
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;
			
			for (Transaccion inst : transaccionesFinales) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

				log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
				log.IdCabeceraLog = inst.IdTransaccion;
				log.IdTipoLog = "INGRESO";
				log.IdModulo = "ORD";
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = 0L;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "ORDEN";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = inst.IdTipoOperacion;
				log.Folio = inst.FolioCartera;
				log.Cantidad = inst.Cantidad;
				log.PrecioTasa = inst.Precio;
				log.Monto = inst.Monto;
				log.IdMonedaOrigen = inst.IdMoneda;
				log.IdMonedaLiquidacion = inst.IdMonedaLiquidacionCorresponsal;
				log.TipoCambio = "";
				log.Nemo = inst.Nemotecnico;
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.NombreProceso = "";						
				entradaAgregarLog.p_Log.add(log);
			}
			agregarLogService.logica(conn, entradaAgregarLog);
		}
		
		if (in.p_simularOperacion.equalsIgnoreCase(_c.NO) && transaccionesFinales.size() > 0) {
			_sql.batch(conn, SQL_INSERT_TRANSACCIONES, transaccionesFinales , Transaccion.class);

			//LOG
			{
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				for (Transaccion_Proceso tran : transacciones) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = tran.IdTransaccion;
					log.IdTipoLog = "INGRESO";
					log.IdModulo = _c.ID_MODULO_ORDENES;
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdCliente = 0L;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio = 0L;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "TRANSACCION";
					log.IdRegistro = tran.IdTransaccion+"";
					log.EstadoRegistro = tran.IdEstado;
					log.IdTipoOperacion = tran.IdTipoOperacion;
					log.Folio = tran.Folio != null && !tran.Folio.isEmpty() ? Long.parseLong(tran.Folio) : 0L ;
					log.Cantidad = tran.Cantidad;
					log.PrecioTasa = tran.Precio;
					log.Monto = tran.Monto;
					log.IdMonedaOrigen = tran.IdMonedaEmision;
					log.IdMonedaLiquidacion = tran.IdMonedaInstrumento;
					log.TipoCambio = "";
					log.Nemo = tran.Nemotecnico;
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "Ingresar Transaccion";						
					entradaAgregarLog.p_Log.add(log);
				}
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
			
			
			}
			
			//INSERT CONTROL MERCADO
			
			if(transaccionesPreladas != null && transaccionesPreladas.size() > 0){
				_sql.batch(conn, SQL_INSERT_CONTROL_MERCADO, transaccionesPreladas, Transaccion_Proceso.class);
			}
		
			//INSERT TRANSACCION EN EL HISTORICO
			insertarTransaccionesHistoricas(conn, in, salida, transaccionesFinales);
			
		}
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		logger.info("Termino del servicio CrearTransacciones");
		return salida;
		
	}

	private int procesarOrdenDirecta(ArrayList<Transaccion_Proceso> transacciones, List<Long> idsTransaccion)
			throws Exception {
		int pos = 0;
		//TRANSACCIONES OD
		{
			ArrayList<Transaccion_Proceso> _transaccionesOD = new ArrayList<>();
			for (Transaccion_Proceso tran : transacciones) {
				
				if (tran.IndicadorOrdenDirecta.equalsIgnoreCase(_c.OD) || tran.IndicadorOrdenDirecta.equalsIgnoreCase(_c.OE)) {
					Transaccion_Proceso tranOD = new Transaccion_Proceso();
					Utils.copyPojo(tran, tranOD);
					tranOD.MarcaNegociacion = tranOD.MarcaNegociacionContraparte;
					tranOD.IdTransaccion = idsTransaccion.get(pos++);
					
					if (_c.ACCION_COMPRA.equalsIgnoreCase(tranOD.AccionOrden) && _c.SI.equalsIgnoreCase(tranOD.EsVentaCortaOD)) {
						tranOD.EsVentaCorta = tranOD.EsVentaCortaOD;
					} else {
						tranOD.EsVentaCorta = "N";
					}
					
					tranOD.CodigoOperadorContraparte = tran.CodigoOperadorCorredor;
					tranOD.CodigoOperadorCorredor = tran.CodigoOperadorContraparte;
					tranOD.IdTransaccionOd = tran.IdTransaccion;
					tran.IdTransaccionOd = tranOD.IdTransaccion;
					
					if (tranOD.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)
							|| tranOD.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)
							|| tranOD.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV)
							|| tranOD.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV)
							|| tranOD.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)
							|| tranOD.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF)) {
						
						tranOD.NemotecnicoRelacionado = tranOD.Nemotecnico;
						tranOD.IdInstrumentoRelacionado = tranOD.IdInstrumento;
						tranOD.Nemotecnico = tran.NemotecnicoRelacionado;
						tranOD.IdInstrumento= tran.IdInstrumentoRelacionado;
					}
					
					tranOD.IdTipoOperacionRelacionada = tranOD.IdTipoOperacion;
					tranOD.IdTipoOperacion= tran.IdTipoOperacionRelacionada;
					_transaccionesOD.add(tranOD);
				}
				
			}
			transacciones.addAll(_transaccionesOD);
		}
		return pos;
	}

	private List<Long> obtenerIdsTransacciones(Connection conn, ArrayList<Transaccion_Proceso> transacciones)
			throws SQLException {
		Long countIdsTransaccion = 0L;
		List<Long> idsTransaccion = new ArrayList<>();
		{
			for (Transaccion_Proceso tran : transacciones) {
				
				
				if (tran.IndicadorOrdenDirecta.equalsIgnoreCase(_c.OD) || tran.IndicadorOrdenDirecta.equalsIgnoreCase(_c.OE)) {
					if (tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
						countIdsTransaccion+=3;
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						countIdsTransaccion+=3;
					}
					else {
						countIdsTransaccion+=1;
					}
				}
				else {
					if (tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
						countIdsTransaccion+=1;
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						countIdsTransaccion+=1;
					}
					else {
						countIdsTransaccion+=0;
					}
				}
				
			}
			idsTransaccion = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::Transaccion", countIdsTransaccion);
		}
		return idsTransaccion;
	}

	private void insertarTransaccionesHistoricas(Connection conn, CrearTransacciones_Entrada in, CrearTransacciones_Salida salida, ArrayList<Transaccion> transaccionesFinales) throws Exception {
		if("N".equals(in.p_Reproceso)) {
			CrearTransaccionesHistoricas crearTransaccionesHistoricas = new CrearTransaccionesHistoricas();
			EntradaCrearTransaccionesHistoricas entradaCrearTransaccionesHistoricas = new EntradaCrearTransaccionesHistoricas();
			entradaCrearTransaccionesHistoricas.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearTransaccionesHistoricas.p_IdUsuario = in.p_IdUsuario;
			entradaCrearTransaccionesHistoricas.p_IdModulo = in.p_IdModulo;
			entradaCrearTransaccionesHistoricas.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaCrearTransaccionesHistoricas.p_Transacciones = transaccionesFinales.parallelStream().map(transaccion->{
				CrearTransaccionesHistoricas.InTransaccionHistorica transaccionHistorica = new CrearTransaccionesHistoricas.InTransaccionHistorica();
				transaccionHistorica.TipoRegistro = "ING";
				transaccionHistorica.IdEstadoRegistro = transaccion.EstadoModificacion;
				transaccionHistorica.Transaccion = transaccion;
				transaccionHistorica.EsModificacionMarketData = transaccion.EsModificacionMarketData;
				transaccionHistorica.IdTransaccionHistoricoAnulacion = transaccion.IdTransaccionHistoricoAnulacion;		
				return transaccionHistorica;
			}).collect(Collectors.toList());
			
			SalidaCrearTransaccionesHistoricas salidaTransaccionHistorica = crearTransaccionesHistoricas.logica(conn, entradaCrearTransaccionesHistoricas);
			
			salida.P_OUT_MENSAJES.addAll(salidaTransaccionHistorica.P_OUT_MENSAJES);
		}
	}

	private ArrayList<Transaccion> obtenerTransaccionesProceso(Connection conn, CrearTransacciones_Entrada in, CrearTransacciones_Salida salida, ArrayList<Transaccion_Proceso> transacciones) throws Exception {
		ArrayList<Transaccion> transaccionesFinales = new ArrayList<>();
		{
			for (Transaccion_Proceso tranP : transacciones) {
				Transaccion tranF = new Transaccion();
				Utils.copyPojo(tranP, tranF);
				tranF.IdMoneda = tranP.IdMonedaInstrumento;

				if (tranP.IdClasificacionTipoInstrumento.equals("RV")){
					tranF.IdMoneda = tranP.IdMonedaRaiz;
					tranF.IdMonedaRaiz= tranP.IdMonedaInstrumento;
				}
				
				tranF.IdMonedaEmision = tranP.IdMonedaEmision;
				transaccionesFinales.add(tranF);
				
				String codigo = "TRANSACCION_INGRESADA";
				String desc = Diccionario.getMensaje(conn,in.p_IdEmpresa,codigo).add("IdTransaccion", tranF.IdTransaccion).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tranP.IdInterno,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
				
				Resultado_Salida res = new Resultado_Salida();
				res.IdInterno =  tranP.IdInterno;
				res.EsAPlazo = tranP.EsAPlazo;
				res.IdTransaccion = tranP.IdTransaccion;
				res.IdTipoOperacion = tranP.IdTipoOperacion;
				salida.P_OUT_RESULTADO.add(res);
				
			}
		}
		return transaccionesFinales;
	}
	
	public static class OperacionRelacionada {
		public Long IdInterno;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String NemotecnicoRelacionado;
		public String IdTipoOperacionRelacionada;
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,Transaccion_Proceso tran,List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdInterno", tran.IdInterno)
				  .add("MontoLiquidacionOk", tran.MontoLiquidacionCorresponsal)
				  .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(tran.IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				tran.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(tran.IdInterno,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				tran.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(tran.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				tran.advertencias++;
			}
			else {
				tran.ok++;
			}
		}else {
			tran.ok++;
		}		
	}
	
	public void limpiarTransacciones(List<Transaccion_Proceso> transacciones, List<Transaccion_Proceso> transaccionesConErrores) {
		for (Transaccion_Proceso tran : transacciones) {
			if (tran.errores > 0) {
				transaccionesConErrores.add(tran);
			}
		}
		
		for (Transaccion_Proceso tran : transaccionesConErrores) {
			if (transaccionesConErrores.contains(tran))
				transacciones.remove(tran);
		}
	}
	
	
	public List<Transaccion_Proceso>  ProcesarPrelacion(Connection conn, SQL sql, CrearTransacciones_Entrada in,Pattern pattern,Pattern pattern2,
			List<Transaccion_Proceso> Transacciones,HashMap<Long, Object> infoTransacciones  ,HashMap<String,InformacionOperador> infoOperadores,
			HashMap<String,ConfiguracionShortName> ConfiguracionesShortNamePorPortafolio,
			HashMap<String, RutPortafolio>  ListaRuts,
			HashMap<String,ConfiguracionAsignacion> ConfiguracionesAsignacion,String parametroPrelacion,String fechaSistema) throws Exception {
		
		boolean out  = false;
		String criterioDePrelacion = "";
		String operadorRuteoAgente = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "OperadorRuteoAgente");
		String operadorRuteoRetail = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "OperadorRuteoRetail");
		
		List<Transaccion_Proceso> transaccionesControlMercado = new ArrayList<Transaccion_Proceso>();
		List<LibrosOrdenesCambiar> libroOrdenCompartido = new ArrayList<>();
		
		for ( Transaccion_Proceso tran : Transacciones) {
			InformacionOperador ope = infoOperadores.get(claveMapa(tran.CodigoOperadorCorredor, tran.IdTipoInstrumento));
			ConfiguracionShortName shortNameFiltrado = ConfiguracionesShortNamePorPortafolio.get(claveMapa(tran.MarcaNegociacion, tran.IdTipoInstrumento));
			InformacionTransaccion info = (InformacionTransaccion) infoTransacciones.get(tran.IdInterno);
			ConfiguracionAsignacion configAsignacion = ConfiguracionesAsignacion.get(claveMapa(tran.IdTipoInstrumento));
			
			
			if (_c.SI.equalsIgnoreCase(tran.EsAPlazo)) {
				continue;
			}
	
			if (tran.CodigoOperadorCorredor.equalsIgnoreCase(operadorRuteoAgente) || tran.CodigoOperadorCorredor.equalsIgnoreCase(operadorRuteoRetail)) {
				tran.EsRuteo = "S";
			}
			boolean procesado = false;
			if("S".equalsIgnoreCase(parametroPrelacion)) {
				procesado = PrelacionOperador(conn, sql, in, tran, ope, parametroPrelacion,transaccionesControlMercado);//Operador
				if(!procesado) {
					procesado = PrelacionClientePortafolio(conn, sql, in, tran, pattern, pattern2, ListaRuts, configAsignacion, parametroPrelacion, fechaSistema,transaccionesControlMercado, libroOrdenCompartido); //Marca cliente portafolio
				}
				if(!procesado) {
					procesado = PrelacionShortname(conn,sql, in, tran, info , shortNameFiltrado, parametroPrelacion,transaccionesControlMercado, libroOrdenCompartido);//ShortName
				}
				if(!procesado) {
					procesado = PrelacionMarcaNeg(conn, sql, in, tran, parametroPrelacion, transaccionesControlMercado); //Marca Negociacion
				}
			}else {
				procesado = PrelacionShortname(conn,sql, in, tran, info , shortNameFiltrado, parametroPrelacion,transaccionesControlMercado, libroOrdenCompartido); // Shortname
				if(!procesado) {
					procesado = PrelacionClientePortafolio(conn, sql, in, tran, pattern, pattern2, ListaRuts, configAsignacion, parametroPrelacion, fechaSistema,transaccionesControlMercado, libroOrdenCompartido); //Marca cliente portafolio
				}
				if(!procesado) {
					procesado = PrelacionOperador(conn, sql, in, tran, ope, parametroPrelacion,transaccionesControlMercado);//Operador
				}
			}
			if(!procesado && tran.EsRuteo != null  && _c.NO.equalsIgnoreCase(tran.EsRuteo)) {
				tran.IdUsuarioReserva = 0L;
			}
		}
		sql.batch(conn, SQL_UPDATE_LIBRO_ORDEN, libroOrdenCompartido, LibrosOrdenesCambiar.class);
		
		return transaccionesControlMercado;

	

	}
	
	public boolean PrelacionShortname (Connection conn,SQL sql, CrearTransacciones_Entrada in,Transaccion_Proceso tran,InformacionTransaccion info,
			ConfiguracionShortName conf, String parametroPrelacion,List<Transaccion_Proceso> transaccionesControlMercado,List<LibrosOrdenesCambiar> libroOrdenCompartido) throws SQLException {
		
			List<LibrosOrdenesCambiar> LibroOrden = new ArrayList<>();
			if(conf == null) {
				return false;
			}
			if (info == null) {
				return false;
			}
			if(conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion)) {
				if ("N".equalsIgnoreCase(parametroPrelacion) && (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) || ("SIM".equalsIgnoreCase(info.IdTipoInstrumento) && info.IdTipoInstrumentoSubyacente!= null && info.IdTipoInstrumentoSubyacente.equalsIgnoreCase(conf.IdTipoInstrumento))) 
						&& "N".equalsIgnoreCase(tran.EsRuteo) && _c.SI.equalsIgnoreCase(conf.AsignacionAuto)) {
						tran.EsAsignacionDirecta = "S";
						tran.TipoMensaje = "SHORTNAME";
						if(conf.IdLibroOrdenes != null) {
							if (!tran.IdLibroOrdenes.equals(conf.IdLibroOrdenes)) {
								tran.IdLibroOrdenes = conf.IdLibroOrdenes;
								LibrosOrdenesCambiar Libro = new  LibrosOrdenesCambiar();
								Libro.IdLibroOrdenes = conf.IdLibroOrdenes;
								Libro.IdTransaccion = tran.IdTransaccion;
								libroOrdenCompartido.add(Libro);
								}
							}
						transaccionesControlMercado.add(tran);
						return true;
				}else if ("S".equalsIgnoreCase(parametroPrelacion) && (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) || ("SIM".equalsIgnoreCase(info.IdTipoInstrumento) && info.IdTipoInstrumentoSubyacente!= null && info.IdTipoInstrumentoSubyacente.equalsIgnoreCase(conf.IdTipoInstrumento))) 
						&& "N".equalsIgnoreCase(tran.EsRuteo)) {
						tran.TipoMensaje = "SHORTNAME";
						if(conf.IdLibroOrdenes != null) {
							if (!tran.IdLibroOrdenes.equals(conf.IdLibroOrdenes)) {
								tran.IdLibroOrdenes = conf.IdLibroOrdenes;
								LibrosOrdenesCambiar Libro = new  LibrosOrdenesCambiar();
								Libro.IdLibroOrdenes = conf.IdLibroOrdenes;
								Libro.IdTransaccion = tran.IdTransaccion;
								libroOrdenCompartido.add(Libro);
								}
							}
						transaccionesControlMercado.add(tran);
						return true;
				}
			}
		
		return false;
		
	}
	
	public boolean PrelacionClientePortafolio (Connection conn,SQL sql, CrearTransacciones_Entrada in,Transaccion_Proceso tran,Pattern pattern,Pattern pattern2,HashMap<String, RutPortafolio>  ListaRuts,
			ConfiguracionAsignacion conf, String parametroPrelacion, String fechaSistema,List<Transaccion_Proceso> transaccionesControlMercado,List<LibrosOrdenesCambiar> libroOrdenCompartido) throws Exception {
		
		//INSERT CONTROL MERCADO PARA ASIGNACION AUTOMATICA
					       
			List<LibrosOrdenesCambiar> LibroOrden = new ArrayList<>();
			Matcher matcher = pattern.matcher(tran.MarcaNegociacion);
			if(matcher.matches()){
				tran.MarcaNegociacion = tran.MarcaNegociacion.concat(";0");
			}

	
				Matcher matcher2 = pattern2.matcher(tran.MarcaNegociacion);
				String[] parts = tran.MarcaNegociacion.split(";");
				if(parts.length <= 1) {
					return false;
				}
				RutPortafolio lista = ListaRuts.get(claveMapa(parts[0], parts[1]));
				if(matcher2.matches() && lista == null) {
					//Log no existe rut
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

					String mensajeError = "Error: No existe Cliente con Documento Identidad";
					
					AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
					log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
					log2.IdCabeceraLog = tran.IdTransaccion;
					log2.IdTipoLog = "INGRESO";
					log2.IdModulo = "ORD";
					log2.IdUsuario = in.p_IdUsuario;
					log2.IdEmpresa = in.p_IdEmpresa;
					log2.FechaSistemaLog = fechaSistema;
					log2.IdUsuarioAtendido = in.p_IdUsuario;
					log2.IdCliente = 0L;  
					log2.EstadoCliente = "";
					log2.NombreCliente = "";
					log2.IdTipoDocumentoIdentidad = "";
					log2.IdDocumentoIdentidad = parts[0];
					log2.IdPortafolio = 0L;
					log2.EstadoPortafolio = "";
					log2.IdTipoRegistro = "ASIGNACION-RUTCUENTA";
					log2.IdRegistro = tran.IdTransaccion+"";
					log2.EstadoRegistro = "";
					log2.IdTipoOperacion = tran.IdTipoOperacion;
					log2.Folio = tran.IdTransaccion;
					log2.Cantidad = tran.Cantidad;
					log2.PrecioTasa =  new BigDecimal(0);        
					log2.Monto = new BigDecimal(0); 
					log2.IdMonedaOrigen = "";
					log2.IdMonedaLiquidacion = "";
					log2.TipoCambio = "";
					log2.Nemo = mensajeError + parts[0] + "y portafolio:" + parts[1];
					log2.IdProceso = new Long(0);
					log2.EstadoProceso = "ING";
					log2.NombreProceso = "Error Asignacion Automatica";						
					
					entradaAgregarLog.p_Log.add(log2);
					
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(conn, entradaAgregarLog);
					return false;
				}else if(lista != null) {
						if(lista.NombreEstado != null) {
							if(matcher2.matches() && lista.DocumentoIdentidad.equalsIgnoreCase(parts[0]) && "BLOQUEADO".equalsIgnoreCase(lista.NombreEstado)) {
								//Log cliente bloqueado
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
								
								String mensajeError = "Error: Cliente Bloqueado Documento Identidad ";
								
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = in.p_IdUsuario;
								log2.IdEmpresa = in.p_IdEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = in.p_IdUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = parts[0];
								log2.IdPortafolio = 0L;
								log2.EstadoPortafolio = "";
								log2.IdTipoRegistro = "ASIGNACION-RUTCUENTA";
								log2.IdRegistro = tran.IdTransaccion+"";
								log2.EstadoRegistro = "";
								log2.IdTipoOperacion = tran.IdTipoOperacion;
								log2.Folio = tran.IdTransaccion;
								log2.Cantidad = tran.Cantidad;
								log2.PrecioTasa =  new BigDecimal(0);        
								log2.Monto = new BigDecimal(0); 
								log2.IdMonedaOrigen = "";
								log2.IdMonedaLiquidacion = "";
								log2.TipoCambio = "";
								log2.Nemo = mensajeError + parts[0];
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = "Error Asignacion Automatica";						
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);
								return false;
							}
						}
					if(parts[0].equalsIgnoreCase(lista.DocumentoIdentidad)  && parts[1].equalsIgnoreCase(lista.CodigoPortafolio)) {
						tran.MarcaNegociacionCorrecta = "S";
						if(!tran.IdLibroOrdenes.equals(lista.IdLibroOrdenes)) {
							tran.IdLibroOrdenes = lista.IdLibroOrdenes;
							LibrosOrdenesCambiar Libro = new  LibrosOrdenesCambiar();
							Libro.IdLibroOrdenes = lista.IdLibroOrdenes;
							Libro.IdTransaccion = tran.IdTransaccion;
							libroOrdenCompartido.add(Libro);
						}
					}
				}


					if ("S".equalsIgnoreCase(tran.MarcaNegociacionCorrecta)) {
						if ("N".equalsIgnoreCase(parametroPrelacion) &&  "N".equalsIgnoreCase(tran.EsRuteo) ) {
							if( conf != null && tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) && _c.SI.equalsIgnoreCase(conf.AsignacionAuto)) {
								tran.EsAsignacionDirecta = "S";
								tran.TipoMensaje = "ASIGNACION_DIRECTA";
								transaccionesControlMercado.add(tran);
								return true;
							}else {
								return false;
							}
							
						}else if ("S".equalsIgnoreCase(parametroPrelacion) && "N".equalsIgnoreCase(tran.EsRuteo) ) {
							tran.TipoMensaje = "ASIGNACION_DIRECTA";
							transaccionesControlMercado.add(tran);
							return true;
							
						}
					}
		
		return false;
	}

	public boolean PrelacionOperador(Connection conn,SQL sql, CrearTransacciones_Entrada in,Transaccion_Proceso tran,InformacionOperador ope, String parametroPrelacion,List<Transaccion_Proceso> transaccionesControlMercado ) {


		if(ope == null) {
			return false;
		}
		
		if ("N".equalsIgnoreCase(parametroPrelacion) && tran.CodigoOperadorCorredor.equalsIgnoreCase(ope.CodigoOperador) && tran.IdTipoInstrumento.equalsIgnoreCase(ope.IdTipoInstrumento) && "N".equalsIgnoreCase(tran.EsAsignacionDirecta)) {
			if (_c.SI.equalsIgnoreCase(ope.UtilizaPortafolio) && ope.IdPortafolio > 0 && _c.SI.equalsIgnoreCase(ope.AsignacionAuto)) {
				tran.TipoMensaje= "TRANSACCION_OPERADOR";
				transaccionesControlMercado.add(tran);
				return true;
				}
			else if (_c.SI.equalsIgnoreCase(ope.ReservaTransaccion)) { 
				tran.TipoMensaje= "TRANSACCION_OPERADOR";
				transaccionesControlMercado.add(tran);
				return true;
			}
		}else if ("S".equalsIgnoreCase(parametroPrelacion) && tran.CodigoOperadorCorredor.equalsIgnoreCase(ope.CodigoOperador) && tran.IdTipoInstrumento.equalsIgnoreCase(ope.IdTipoInstrumento)) {
			if (_c.SI.equalsIgnoreCase(ope.UtilizaPortafolio) && ope.IdPortafolio > 0 ) {
				tran.TipoMensaje= "TRANSACCION_OPERADOR";
				transaccionesControlMercado.add(tran);
				return true;
				}
			else if (_c.SI.equalsIgnoreCase(ope.ReservaTransaccion)) { 
				tran.TipoMensaje= "TRANSACCION_OPERADOR";
				transaccionesControlMercado.add(tran);
				return true;
			}
		}
					
		return false;
	}
	
	public boolean PrelacionMarcaNeg(Connection conn,SQL sql, CrearTransacciones_Entrada in,Transaccion_Proceso tran,String parametroPrelacion,List<Transaccion_Proceso> transaccionesControlMercado) {
		if("S".equalsIgnoreCase(parametroPrelacion) &&tran.EsRuteo != null && _c.NO.equalsIgnoreCase(tran.EsRuteo) && tran.MarcaNegociacion != null && tran.MarcaNegociacion.length() > 0) {
			tran.TipoMensaje = "TRANSACCION_MARCA_NEGOCIACION";
			transaccionesControlMercado.add(tran);
			return true;
		}
		return false; 
	}
	
	private String claveMapa(String... valores) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < valores.length; i++) {
			if (i > 0) sb.append("-");
			sb.append(valores[i] == null ? "" : valores[i].trim().toUpperCase());
		}
		return sb.toString();
	}
	
	public static class FolioExistente {
		public Long IdTransaccion;
		public String FolioFecha;
		
		public FolioExistente(Long IdTransaccion, String FolioFecha) {
			this.IdTransaccion = IdTransaccion;
			this.FolioFecha = FolioFecha;
		}
		
		public FolioExistente() {
			
		}
		
		@Override
		public boolean equals(Object o) {
			FolioExistente e = (FolioExistente)o;
			return this.FolioFecha.equalsIgnoreCase(e.FolioFecha);
		}
		
		@Override
		public int hashCode() {
			return (FolioFecha).hashCode();
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso {

		public int ok;
		public int advertencias;
		public int autorizaciones;
		public int errores = 0;
		
		public String 			AccionOrden;
		
		public Long 			BaseTasa;
		
		public BigDecimal 		CorteMinimo;
		public BigDecimal Cantidad;
		public String CodigoOperadorCorredor;
		public String CodigoOperadorContraparte;
		
		
		public Long 			DiasLiquidacionContado;
		
		public String 			EsFueraRueda;
		public String 			EsBilateral;
		public String EsVentaCorta;
		public String EsVentaCortaOD = "N"; //cuando sea OD, solo la punta venta trae la marca VC
		public String EsAPlazo;
		public String EsFungible;
		public String EsCuentaPropia;
		
		public Date 			FechaLiquidacion;
		public java.sql.Date 	FechaTipoCambio;
		public String 			FolioTransaccionPlazo;
		public Timestamp FechaIngresoOferta;
		public Date FechaVencimiento;
		public Date FechaEmision;
		public Date FechaSistemaCreacion;
		public String Folio;
		public Timestamp FechaOperacion;
		
		public Timestamp 		FechaModificacionOperacion;
		public java.sql.Date 	FechaParidad;

		public BigDecimal GastosCorresponsal;
		
		public Time HoraSistemaCreacion;
		
		public String 		IdTipoOperacionRelacionada;
		public Long 		IdInstrumentoRelacionado;
		public String 		IdEstadoLibroOrdenes;
		public String 		InstrumentoPermiteVentaCorta;
		public String 		IdMonedaLiquidacionCorresponsal;
		public Long 		IdCorredoraCorresponsal;
		public String 		IndicadorDCV;
		public Long 		IdProgramaProcesaAutomatico;
		public String 		IdBolsa;
		public String 		IdBoveda;
		public Long 		IdCorredoraLocal;
		public Long 		IdCorredoraContraparte;
		public String 		IdMonedaInstrumento;
		public String 		IdMonedaEmision;
		public Long 		IdEmpleado;
		public String 		IdTipoOperacion;
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoReajuste;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public Long			IdTransaccionOd;
		public Long 		IdTransaccionPlazo;
		public String 		IdEstado;
		public Long 		IdInterno;
		public Long 		IdTransaccion;
		public BigDecimal 	Intereses;
		public Long 			IdPaisBolsa;
		public Long 			IdLibroOrdenes;
		public Long 			IdEmpresa;
		public String 			IdMonedaSubyacente;
		public Long 			IdInstrumentoSubyacente;
		public Long 			IdInstrumento;
		public String IndicadorOrdenDirecta;
		public String IdEmisor;
		public String IdSistemaBursatil;
		
		public String 		MarcaNegociacion;
		public String 		MarcaNegociacionContraparte;
		public BigDecimal 	MejorPrecioVenta;
		public BigDecimal 	MontoLiquidacionCorresponsal;
		public BigDecimal 	MejorPrecioCompra;
		public BigDecimal MontoPlazo;
		public BigDecimal Monto;
		

		public String NemotecnicoSVS;
		public String Nemotecnico;
		public String 			NemotecnicoSubyacente;
		public String 		NemotecnicoRelacionado;
		public String 		ProcesaAutomatico;
		public Long 		Plazo;
		public BigDecimal PrecioPlazo;
		public BigDecimal Precio;

		public BigDecimal Reajustes;

		public BigDecimal Tasa;
		public String 		TipoInteres;
		public BigDecimal 	TipoCambioCorresponsal;
		
		public BigDecimal 	ValorTipoCambio;
		public BigDecimal 		ValorParidad;

		public BigDecimal ValorRescate;
		

		public String IdCamara;  // Camara del CCLV para Clearing 
		
		public String EsRuteo="N";
		public Long IdUsuarioReserva = 0L;
		public String Reservado = "N";

		public String BolsaEsExtranjera = "N";

		public String FlujoOrigen = "INGRESOORDEN";
		
		public BigDecimal PrecioRaiz;
		public BigDecimal MontoRaiz;
		public String IdMonedaRaiz;
		public Long IdInstrumentoNegociacion;
		
		
		public String TipoMensaje;
		public String EsAsignacionDirecta = "N";
		public String MarcaNegociacionCorrecta = "N";
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion;
		public Long IdTransaccionHistoricoAnulacion = 0L;
		public String MotivoReserva = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionTransaccion {
		
		public String IdMonedaEmisionInstrumento;
		public Long IdInterno;
		public Long IdTransaccion;
		public java.sql.Date FechaEmisionInstrumento;
		public java.sql.Date FechaVencimientoInstrumento;
		public String NemotecnicoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String AccionOrden;
		public String IdEmisorInstrumento;
		public String IdTipoReajusteInstrumento;
		public String IdMonedaInstrumento;
		public Long IdPaisBolsa;
		public String NemotecnicoInstrumentoSubyacente;
		public Long IdInstrumentoSubyacente;
		public String IdMonedaInstrumentoSubyacente;
		public String IdEmisorInstrumentoSubyacente;
		public BigDecimal CorteMinimo;
		public Long IdCorredoraCorresponsal;
		public String InstrumentoPermiteVentaCorta;
		public String IdEstadoLibroOrdenes;
		
		public Integer DiasLiquidacionContado;
		
		public String NemotecnicoRelacionado;
		public Long IdInstrumentoRelacionado;
		public String IdTipoOperacionRelacionada;

		public String BolsaEsExtranjera;
		
		public String IdTipoInstrumentoSubyacente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionOperador {
		public String CodigoOperador;
		public String IdTipoInstrumento;
		public String ReservaTransaccion;
		public String AsignacionAuto;
		public Long IdPortafolio;
		public String UtilizaPortafolio;
	}
	
	public static class ConfiguracionAsignacion {
		public String IdTipoInstrumento;
		public String AsignacionAuto;
		public String MovimientoFacturarAuto;
		public String ComprobanteAuto;
	}
	
	public static class ConfiguracionShortName {
		public String ShortName;
		public String IdTipoInstrumento;
		public String AsignacionAuto;
		public String MovimientoFacturarAuto;
		public String ComprobanteAuto;
		public Long IdLibroOrdenes;
	}
	
	public static class CrearTransacciones_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion = "N";
		public String p_UsaReglasNegocio;
		public String p_Reproceso;
		
		public List<Transaccion_Entrada> p_Transacciones;
		
		public CrearTransacciones_Entrada() {
			this.p_Reproceso = "N";
			this.p_Transacciones = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Transaccion_Entrada {
		public String IdMoneda;		//IdMonedaInstrumento o moneda de pago, la moneda del monto
		public String IdMonedaEmision;	//solo para if, moneda de los nominales
		public Long IdInterno;
		public String FolioTransaccion;
		public String FechaCreacion;
		public String HoraCreacion;
		public String FechaEmision;
		public Long Plazo;
		public String EsCuentaPropia;	//eliminar
		
		public String EsFungible;
		public String EsVentaCorta;
		public String CodigoOperadorContraparte;
		public String CodigoOperadorCorredor;
		public String IdSistemaBursatil;
		public String IdEmisor;
		
		public BigDecimal Cantidad;
		
		public BigDecimal Monto;
		
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal MontoPlazo;
		public BigDecimal PrecioPlazo;
		public BigDecimal ValorRescate;
		public String IdTipoReajuste;
		
		public Long IdEmpleado;				//eliminar
		public String IdTipoOperacion;
		
		public String IdCondicionDeLiquidacionContado;
		public String FechaLiquidacionContado;	//eliminar
		public String FechaVencimiento;
		
		public Long IdInstrumento;
		public Long IdCorredoraContraparte;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdLibroOrdenes;
		public Long IdInstrumentoSubyacente;
		public String EsFueraRueda;				//eliminar, cambiar por bolsa, agregar columna para fuera de rueda
		public String IndicadorDCV;				//cambiar por boveda
		
		public String EsBilateral;
		public String MarcaNegociacion;
		public String MarcaNegociacionContraparte = "";
		public BigDecimal TipoCambioCorresponsal;
		public Long IdCorredoraCorresponsal;

		public BigDecimal MontoLiquidacionCorresponsal;
		public String IdMonedaLiquidacionCorresponsal;
		public String FolioTransaccionPlazo;
		public BigDecimal MejorPrecioCompra;
		public BigDecimal MejorPrecioVenta;
		public String NemotecnicoEspecificoBolson;
		public String IndicadorOrdenDirecta = "";
		public String NemotecnicoSVS = "";
		
		public BigDecimal Intereses;		// Para RF PERU
		public BigDecimal Reajustes;		// Para RF PERU		
		public BigDecimal GastosCorresponsal;
		
		public String EsRuteo = "N";
		public String LiqFisicaOpciones = "N";

		public String FlujoOrigen = "INGRESOORDEN";
		
		public String EsModificacionMarketData = "N";
		public String EstadoModificacion;
		public Long IdTransaccionHistoricoAnulacion = 0L;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearTransacciones_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<Resultado_Salida> P_OUT_RESULTADO;
		
		public CrearTransacciones_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_RESULTADO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LibrosOrdenes {
		//public Long IdLibroOrdenesOperador;
		public Long IdEmpresa;
		public Long IdLibroOrdenes;
		public String CodigoOperador;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class RutPortafolio {

		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdLibroOrdenes;
		public String NombreEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class LibrosOrdenesCambiar {
		public Long IdLibroOrdenes;
		public Long IdTransaccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdTransaccion;
		public String IdTipoOperacion;
		public String EsAPlazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoMonedaValidar extends BcsPojo {
		public Long IdInstrumento;
		public String MonedaEmisionValida;
	}
	
	public static void main(String[]args) throws Exception{
		
		Gson gson = Utils.getGsonBuilder();

		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Transacciones\":[{\"IdInterno\":0,\"FolioTransaccion\":46461,\"FechaCreacion\":\"2025-09-17\",\"HoraCreacion\":\"12:39:37\",\"FechaEmision\":\"2025-09-17\",\"Plazo\":0,\"EsCuentaPropia\":\"N\",\"EsFungible\":\"N\",\"EsVentaCorta\":\"N\",\"CodigoOperadorContraparte\":\"\",\"CodigoOperadorCorredor\":\"\",\"IdSistemaBursatil\":\"MANUAL\",\"IdEmisor\":\"\",\"Cantidad\":1000,\"Monto\":10000000,\"Precio\":1250,\"Tasa\":1.2,\"MontoPlazo\":1250000,\"PrecioPlazo\":1250,\"ValorRescate\":0,\"IdTipoReajuste\":\"CLP\",\"IdMonedaEmision\":\"CLP\",\"IdEmpleado\":108,\"IdTipoOperacion\":\"COMRF\",\"IdCondicionDeLiquidacionContado\":\"T2\",\"FechaLiquidacionContado\":\"2025-09-23\",\"FechaVencimiento\":\"0001-01-01\",\"IdCorredoraContraparte\":240,\"IdBolsa\":\"BCS\",\"MarcaNegociacion\":\"\",\"MejorPrecioCompra\":1250,\"MejorPrecioVenta\":1250,\"TipoCambioCorresponsal\":0,\"IdCorredoraCorresponsal\":0,\"MontoLiquidacionCorresponsal\":0,\"GastosCorresponsal\":0,\"IdMonedaLiquidacionCorresponsal\":\"\",\"EsFueraRueda\":\"S\",\"IndicadorDCV\":\"N\",\"EsBilateral\":\"N\",\"FolioTransaccionPlazo\":0,\"NemotecnicoEspecificoBolson\":\"BAAGUAS-C1\",\"IndicadorOrdenDirecta\":\"N\",\"IdMoneda\":\"CLP\",\"Intereses\":0,\"Reajustes\":0,\"IdLibroOrdenes\":3,\"IdBoveda\":\"\",\"IdInstrumento\":5120,\"IdInstrumentoSubyacente\":0}]}";

		
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties"))
		{
			CrearTransacciones_Entrada Objin = gson.fromJson(json, CrearTransacciones_Entrada.class);
			
			CrearTransacciones demo=new CrearTransacciones();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearTransacciones_Salida ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}

		 	
		System.out.println("== END ==");
	}
}

	