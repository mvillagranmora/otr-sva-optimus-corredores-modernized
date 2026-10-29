package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.ModificarTransaccion.EntradaModificarTransaccion;
import cl.bcs.ordenes.ModificarTransaccion.SalidaModificarTransaccion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.TransaccionSIMMarketData;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarTransaccionMarketData implements JavaCall {
	
	private final static Logger logger = LogManager.getLogger(ModificarTransaccionMarketData.class);
	
	private final String SQL_SELECT_CORREDORAS					= loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccionMarketData_SelectCorredoras.sql");
	private final String SQL_SELECT_IDCONDICIONLIQUIDACION		= loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccionMarketData_SelectIdCondicionDeLiquidacion.sql");
	private final String SQL_SELECT_TRANSACCIONES_MODIFICAR_SIM	= loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccionMarketData_SelectTransaccionesModificarSIM.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final String ORIGEN_MARKET_DATA = "MARKET_DATA";
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class EntradaModificarTransaccionMarketData extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<TransaccionSIM_ProcesoModificacion> transaccionesSIM;
		
		public EntradaModificarTransaccionMarketData() {
			this.transaccionesSIM = new ArrayList<>();		
		}
		
	}
	
	public static class TransaccionSIM_ProcesoModificacion extends TransaccionSIMMarketData {
		public Long 	IdInterno;
		public String	_TipoMensaje;
		public String	_IdMensaje1;
		public String	_IdMensaje2;
		public String	IdCondicionDeLiquidacion;
		public String	FechaVencimiento;
		public String	AccionTipoOperacion;
		public Long		IdTransaccion;
		public String 	Hash;
		public String 	HashOD;
	}
		
	public static class SalidaModificarTransaccionMarketData extends BcsPojo{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public SalidaModificarTransaccionMarketData() {
			this.P_OUT_MENSAJES 	= new ArrayList<>();
		}
	}
	
	public static class ResultadoSalida extends BcsPojo{
		public Long IdInterno;
		public Long IdTransaccion;
		
	}
	
	public static class TransaccionParaModificar extends ModificarTransaccion.InTransaccion{
		public String		Hash;
		public String		Folio;
		public String		FechaSistemaCreacion;
		public String		AccionOrden;
		public String		Nemotecnico;
		public TransaccionSIM_ProcesoModificacion referencia;
	}
	
	public static class Corredora extends BcsPojo{
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
		
	}
	
	public static class IdCondicionDeLiquidacion extends BcsPojo{
		public String	IdCondicionDeLiquidacion;
		public Long		PlazoEnDias;
	}
	
	public SalidaModificarTransaccionMarketData logica(Connection conn, EntradaModificarTransaccionMarketData entrada) throws Exception {
		logger.info("INICIO::ModificarTransaccionMarketData");
		
		SalidaModificarTransaccionMarketData salida = new SalidaModificarTransaccionMarketData();
		SQL sql = new SQL();
		ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, entrada.p_IdEmpresa);
		
		HashMap<String,Corredora> corredoras = buscarCorredoras(conn, sql);
		
		//Palos SIM
		if(entrada.transaccionesSIM.size() > 0) {
			
			SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
			
			definirIdCondicionDeLiquidacionEntrada(entrada.transaccionesSIM);
			
			List<IdCondicionDeLiquidacion> listaIdCondicionDeLiquidacion = buscarIdCondicionDeLiquidacion(conn, sql);
			HashMap<String, Object> hashIdCondicionDeLiquidacion = Utils.List2Hash(listaIdCondicionDeLiquidacion,"IdCondicionDeLiquidacion");
			
			List<String> llavesModificacion = new ArrayList<>();
			
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = empresa.IdPais;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<>();
			
			for (TransaccionSIM_ProcesoModificacion tx : entrada.transaccionesSIM ) {
				
				String fechaTransaccion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.transactionDay)));
				String accionTransaccion = "";
				Corredora corredoraCompra = corredoras.get(tx.buyExecutingFirm);
				Corredora corredoraVenta = corredoras.get(tx.sellExecutingFirm);
				tx.FechaVencimiento = sdfTradeTimeOUT.format(sdfTradeTimeIN.parse(tx.expirationDate));
				
				if (corredoraCompra != null && corredoraVenta != null) {
					
					accionTransaccion = definirAccion(corredoraCompra, corredoraVenta, empresa, accionTransaccion);
					
					tx.AccionTipoOperacion = accionTransaccion;
					
					if(!"".equalsIgnoreCase(accionTransaccion)) {
						llavesModificacion.add(tx.folio+"_"+fechaTransaccion+"_"+tx.symbol+"_"+accionTransaccion);
						tx.Hash = tx.folio+"_"+fechaTransaccion+"_"+tx.symbol+"_"+accionTransaccion;
						tx.HashOD = "";
						if(esOrdenDirecta(corredoraCompra, corredoraVenta)) {
							String accionTransaccionOD = definirAccionOD(accionTransaccion);
							if(!accionTransaccionOD.isEmpty()) {
								llavesModificacion.add(tx.folio+"_"+fechaTransaccion+"_"+tx.symbol+"_"+accionTransaccionOD);
								tx.HashOD = tx.folio+"_"+fechaTransaccion+"_"+tx.symbol+"_"+accionTransaccionOD;
							}
						}
						
						if(hashIdCondicionDeLiquidacion.size() > 0) {
							IdCondicionDeLiquidacion condicionLiquidacion = (IdCondicionDeLiquidacion) hashIdCondicionDeLiquidacion.get(tx.IdCondicionDeLiquidacion);
							if(condicionLiquidacion != null) {
								FechasIn f = new FechasIn();
								f.fechaInicial	= fechaTransaccion;
								f.numeroDias 	= condicionLiquidacion.PlazoEnDias;
								inObtenerDiaSiguiente.p_fechas.add(f);
							}
						}
					}
				}
			}
			
			List<FechasOut> listaFechas = new ArrayList<>();
			
			if (inObtenerDiaSiguiente.p_fechas.size() > 0) {
				ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(conn, inObtenerDiaSiguiente);
				if(outObtenerDiaSiguiente.p_out_fechas.size() > 0) {
					listaFechas = outObtenerDiaSiguiente.p_out_fechas;
				}
			}
			
			List<TransaccionParaModificar> transaccionesPorModificar = buscarTransaccionesPorModificarSIM(conn, sql, llavesModificacion);
			
			HashMap<String, Object> hashTransaccionesPorModificar = Utils.List2Hash(transaccionesPorModificar,"Hash");
			
			List<ModificarTransaccion.InTransaccion> listaTransaccionesModificar = new ArrayList<>();
			
			ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
			entradaObtenerPlazo.p_IdPais = empresa.IdPais;
			
			for (TransaccionSIM_ProcesoModificacion tran : entrada.transaccionesSIM) {
				
				if(hashTransaccionesPorModificar.size() > 0) {
					
					TransaccionParaModificar transaccion = (TransaccionParaModificar) hashTransaccionesPorModificar.get(tran.Hash);
					
					if(transaccion != null) {
						prepararEntradaModificarTransaccionSIM(tran, transaccion, hashIdCondicionDeLiquidacion, listaFechas, sdfTradeTimeIN, sdfTradeTimeOUT, entradaObtenerPlazo, listaTransaccionesModificar);
						
						if(!tran.HashOD.isEmpty()) {
							TransaccionParaModificar transaccionOD = (TransaccionParaModificar) hashTransaccionesPorModificar.get(tran.HashOD);
							
							if(transaccionOD != null) {
								prepararEntradaModificarTransaccionSIM(tran, transaccionOD, hashIdCondicionDeLiquidacion, listaFechas, sdfTradeTimeIN, sdfTradeTimeOUT, entradaObtenerPlazo, listaTransaccionesModificar);
							}
						}
					}
				}
			}
			
			List<cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut> PlazosCalculados = new ArrayList<>();
			
			if(entradaObtenerPlazo.p_Fechas.size() > 0) {
				ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
				OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
				if(salidaObtenerPlazo.p_out_Fechas.size() > 0) {
					PlazosCalculados = salidaObtenerPlazo.p_out_Fechas;
				}
			}
			
			SalidaModificarTransaccion salidaModificarTransaccion = new SalidaModificarTransaccion();
			List<MensajeSalida> mensajeSalidaModificarTransaccion = new ArrayList<>();
			
			if(listaTransaccionesModificar.size() > 0) {
				if(PlazosCalculados.size() > 0) {
					for(ModificarTransaccion.InTransaccion tran : listaTransaccionesModificar) {
						cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut fecha = PlazosCalculados.stream()
								.filter(f -> f.IdInterno.compareTo(-1L) != 0 && f.IdInterno.compareTo(tran.IdTransaccion) == 0)
								.findFirst()
								.orElse(null);
						
						if(fecha != null) {
							tran.Plazo = fecha.Plazo;
						}
					}
				}
				
				ModificarTransaccion modificarTransaccion = new ModificarTransaccion();
				EntradaModificarTransaccion entradaModificarTransaccion = new EntradaModificarTransaccion();
				entradaModificarTransaccion.p_IdEmpresa			= entrada.p_IdEmpresa;
				entradaModificarTransaccion.p_IdModulo			= _c.ID_MODULO_ORDENES;
				entradaModificarTransaccion.p_IdUsuario 		= entrada.p_IdUsuario;
				entradaModificarTransaccion.p_UsaReglasNegocio	= _c.SI;
				entradaModificarTransaccion.p_Reproceso			= _c.NO;
				entradaModificarTransaccion.p_OrigenTransaccion	= ORIGEN_MARKET_DATA;
				entradaModificarTransaccion.p_Transacciones		= listaTransaccionesModificar;
				
				salidaModificarTransaccion = modificarTransaccion.logica(conn, entradaModificarTransaccion);
				mensajeSalidaModificarTransaccion = salidaModificarTransaccion.P_OUT_MENSAJES;
					
			}
			
			HashMap<Long, Object> hashMensajeSalida = Utils.List2Hash(mensajeSalidaModificarTransaccion,"id");
			
			for(TransaccionSIM_ProcesoModificacion tran : entrada.transaccionesSIM) {
				
				String mensaje = "ERROR, NO SE ENCONTRO TRANSACCION A MODIFICAR" + tran._IdMensaje1 + " - " + tran._IdMensaje2;
				String tipoMensaje = MensajeSalida.MENSAJE_ERROR;
				
				if(hashTransaccionesPorModificar.size() > 0) {

					TransaccionParaModificar transaccion = (TransaccionParaModificar) hashTransaccionesPorModificar.get(tran.Hash);

					if(transaccion != null) {
						if(hashMensajeSalida.size() > 0) {
							MensajeSalida mensajeModificarTransaccion = (MensajeSalida) hashMensajeSalida.get(transaccion.IdTransaccion);
							if(mensajeModificarTransaccion != null) {
								mensaje = mensajeModificarTransaccion.codigo;
								tipoMensaje = mensajeModificarTransaccion.tipo;
							}
						}
					}
				}
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdInterno,tipoMensaje,mensaje,new HashMap<>(),conn,entrada.p_IdEmpresa));
				
			}
			
		}
		
		logger.info("FIN::ModificarTransaccionMarketData");
		return salida;
	}
	
	private HashMap<String,Corredora> buscarCorredoras (Connection con, SQL sql) throws SQLException{
		logger.debug("INICIO::ModificarTransaccionMarketData.buscarCorredoras");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> params1 = new HashMap<>();
		List<Corredora> _corredoras = sql.query(con, SQL_SELECT_CORREDORAS, params1, Corredora.class);
		HashMap<String,Corredora> corredoras = new HashMap<>();
		for (Corredora corr : _corredoras) {
			corredoras.put(corr.CodigoCorredoraFix, corr);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarTransaccionMarketData.buscarCorredoras tiempo: " + (fin - inicio));
		return corredoras;
	}
	
	public void definirIdCondicionDeLiquidacionEntrada (List<TransaccionSIM_ProcesoModificacion> transaccionesOriginal) {
		logger.trace("INICIO::ModificarTransaccionMarketData.definirIdCondicionDeLiquidacionEntrada");
		long inicio = System.currentTimeMillis();
		
		transaccionesOriginal.stream().forEach(tran -> tran.IdCondicionDeLiquidacion = definirIdCondicionDeLiquidacion(tran.settlementType.trim()));			
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccionMarketData.definirIdCondicionDeLiquidacionEntrada tiempo: " + (fin - inicio));
	}
	
	private String definirIdCondicionDeLiquidacion(String idCondicionDeLiquidacionHT) {
			
			String idCondicionDeLiquidacion = "";
			
			if ("T+3".equalsIgnoreCase(idCondicionDeLiquidacionHT)) {
				idCondicionDeLiquidacion = "T3";
			}
			else if ("CN".equalsIgnoreCase(idCondicionDeLiquidacionHT)) {
				idCondicionDeLiquidacion = "T2";
			}
			else if ("".equalsIgnoreCase(idCondicionDeLiquidacionHT)) {
				idCondicionDeLiquidacion = "T2";
			}
			else if ("PM".equalsIgnoreCase(idCondicionDeLiquidacionHT)) {
				idCondicionDeLiquidacion = "T1";
			}
			else if ("PH".equalsIgnoreCase(idCondicionDeLiquidacionHT)) {
				idCondicionDeLiquidacion = "T0";
			}
	
			return idCondicionDeLiquidacion;
		
	}
	
	private List<IdCondicionDeLiquidacion> buscarIdCondicionDeLiquidacion (Connection con, SQL sql) throws SQLException{
		logger.debug("INICIO::ModificarTransaccionMarketData.buscarIdCondicionDeLiquidacion");
		long inicio = System.currentTimeMillis();
		
		List<IdCondicionDeLiquidacion> listaIdCondicionDeLiquidacion = new ArrayList<>();
		
		HashMap<String,Object> params1 = new HashMap<>();
		listaIdCondicionDeLiquidacion = sql.query(con, SQL_SELECT_IDCONDICIONLIQUIDACION, params1, IdCondicionDeLiquidacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarTransaccionMarketData.buscarIdCondicionDeLiquidacion tiempo: " + (fin - inicio));
		return listaIdCondicionDeLiquidacion;
	}
	
	private List<TransaccionParaModificar> buscarTransaccionesPorModificarSIM (Connection con, SQL sql, List<String> llaveBusqueda) throws SQLException{
		logger.debug("INICIO::ModificarTransaccionMarketData.buscarTransaccionesPorModificarSIM");
		long inicio = System.currentTimeMillis();
		
		List<TransaccionParaModificar> transacciones = new ArrayList<>();
		
		HashMap<String,Object> params = new HashMap<String, Object>(1);
		params.put("Llave", llaveBusqueda);
		transacciones = sql.query(con, SQL_SELECT_TRANSACCIONES_MODIFICAR_SIM, params, TransaccionParaModificar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarTransaccionMarketData.buscarTransaccionesPorModificarSIM tiempo: " + (fin - inicio));
		return transacciones;
	}
	
	private String definirIdCamara (TransaccionParaModificar transaccion, TransaccionSIM_ProcesoModificacion transaccionProcesoModificacion) {
		logger.trace("INICIO::ModificarTransaccionMarketData.definirIdCamara");
		long inicio = System.currentTimeMillis();
		
		String IdCamara;
		
		// Asignacion de camara CCLV a las transacciones para Clearing (esto no indica que vaya a clearing ya que existen otras condiciones que van en la consulta y procesamiento de clearing)
		// Esta logica debe ser la misma en:  CrearTransaccion.java / ModificarTransaccion.js / ConsultarClearingFinanciero.sql / ModificarTransaccionMarketData.java
		if (_c.CLASIFICACION_RV.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento) && "T2".equalsIgnoreCase(transaccionProcesoModificacion.IdCondicionDeLiquidacion))
		{   // Toda operacion RV, CN (Incluye simultaneas)) --> Camara RV
			IdCamara = "RV";
		}
		else if ((_c.CLASIFICACION_RF.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento) || _c.CLASIFICACION_IF.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento)) && "T1".equalsIgnoreCase(transaccionProcesoModificacion.IdCondicionDeLiquidacion))
		{   // Toda operacion RF o IF  T1 --> Camara PM
			IdCamara = "PM";
		}
		else if ((_c.CLASIFICACION_RF.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento) || _c.CLASIFICACION_IF.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento)) && "T0".equalsIgnoreCase(transaccionProcesoModificacion.IdCondicionDeLiquidacion))
		{   // Toda operacion RF o IF  T0 --> Camara PH
			IdCamara = "PH";
		}
		else if ((_c.CLASIFICACION_RF.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento) || _c.CLASIFICACION_IF.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento)) && "T2".equalsIgnoreCase(transaccionProcesoModificacion.IdCondicionDeLiquidacion))
		{   // Toda operacion RF o IF  T2 --> Camara PH
			IdCamara = "PH";
		}
		else if (_c.CLASIFICACION_RV.equalsIgnoreCase(transaccion.IdClasificacionTipoInstrumento) && _c.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(transaccion.IdTipoInstrumento) && "T0".equalsIgnoreCase(transaccionProcesoModificacion.IdCondicionDeLiquidacion))
		{   // Toda operacion de SIMULTANEA, T0 --> Camara SM
			IdCamara = "SM";
		}
		else
		{
			IdCamara = "";	
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccionMarketData.definirIdCamara tiempo: " + (fin - inicio));
		return IdCamara;
	}
	
	private String definirAccion (Corredora corredoraCompra, Corredora corredoraVenta, InformacionEmpresa empresa, String accionTransaccion) {
		logger.trace("INICIO::ModificarTransaccionMarketData.definirAccion");
		long inicio = System.currentTimeMillis();
		
		if(corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
			accionTransaccion = _c.ACCION_COMPRA;
		}else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)){
			accionTransaccion = _c.ACCION_VENTA;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccionMarketData.definirAccion tiempo: " + (fin - inicio));
		return accionTransaccion;
	}
	
	private String definirAccionOD (String accionTransaccion) {
		logger.trace("INICIO::ModificarTransaccionMarketData.definirAccionOD");
		long inicio = System.currentTimeMillis();
		
		String accionTransaccionOD = "";
		if(_c.ACCION_COMPRA.equals(accionTransaccion)) {
			accionTransaccionOD = _c.ACCION_VENTA;
		} else {
			accionTransaccionOD = _c.ACCION_COMPRA;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccionMarketData.definirAccionOD tiempo: " + (fin - inicio));
		return accionTransaccionOD;
	}
	
	private boolean esOrdenDirecta (Corredora corredoraCompra, Corredora corredoraVenta) {
		return corredoraCompra.IdCorredora.equals(corredoraVenta.IdCorredora);
	}
	
	private void prepararEntradaModificarTransaccionSIM (TransaccionSIM_ProcesoModificacion tran, TransaccionParaModificar transaccion, 
														HashMap<String, Object> hashIdCondicionDeLiquidacion, List<FechasOut> listaFechas,
														SimpleDateFormat sdfTradeTimeIN, SimpleDateFormat sdfTradeTimeOUT, ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo,
														List<ModificarTransaccion.InTransaccion> listaTransaccionesModificar) throws Exception {
		logger.trace("INICIO::ModificarTransaccionMarketData.prepararEntradaModificarTransaccion");
		long inicio = System.currentTimeMillis();
		ModificarTransaccion.InTransaccion t = new ModificarTransaccion.InTransaccion();
		t.Cantidad 							= new BigDecimal(tran.qty);
		t.IdTransaccion						= transaccion.IdTransaccion != null ? transaccion.IdTransaccion : -1L;
		t.Precio							= tran.spotPrice;
		t.PrecioAPlazo						= tran.forwardPrice;
		t.Monto								= tran.amount;
		t.MontoAPlazo						= tran.forwardPrice.multiply(t.Cantidad);
		t.Tasa								= tran.pubRate;
		t.EsBilateral						= transaccion.EsBilateral;
		t.GastosCorresponsal				= transaccion.GastosCorresponsal != null ? transaccion.GastosCorresponsal : BigDecimal.ZERO;
		t.MontoLiquidacionCorresponsal		= transaccion.MontoLiquidacionCorresponsal != null ? transaccion.MontoLiquidacionCorresponsal : BigDecimal.ZERO;
		t.TipoCambioCorresponsal			= transaccion.TipoCambioCorresponsal != null ? transaccion.TipoCambioCorresponsal : BigDecimal.ZERO;
		t.IdMonedaLiquidacionCorresponsal	= transaccion.IdMonedaLiquidacionCorresponsal != null ? transaccion.IdMonedaLiquidacionCorresponsal: "";
		t.IdCorredorCorresponsal			= transaccion.IdCorredorCorresponsal != null ? transaccion.IdCorredorCorresponsal : 0L;
		t.IdCamara							= definirIdCamara(transaccion, tran);
		t.FechaVencimiento					= tran.FechaVencimiento;
		t.IdTipoInstrumento					= _c.TIPO_INSTRUMENTO_SIM;
		t.MontoRetencion					= transaccion.MontoRetencion != null ? transaccion.MontoRetencion : BigDecimal.ZERO;
		
		if(hashIdCondicionDeLiquidacion.size() > 0) {
			IdCondicionDeLiquidacion condicionLiquidacion = (IdCondicionDeLiquidacion) hashIdCondicionDeLiquidacion.get(tran.IdCondicionDeLiquidacion);
			if(condicionLiquidacion != null) {
				t.IdCondicionDeLiquidacion = condicionLiquidacion.IdCondicionDeLiquidacion;
				if(listaFechas.size() > 0) {			
					String fechaOperacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.transactionDay)));
					FechasOut fecha = listaFechas.stream()
							.filter(f-> f.fechaInicial.equalsIgnoreCase(fechaOperacion) && f.numeroDiasInicial.compareTo(condicionLiquidacion.PlazoEnDias) == 0)
							.findFirst()
							.orElse(null);
					if(fecha != null) {
						t.FechaLiquidacion = fecha.fechaCalculada;
						ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
						f.FechaDesde = t.FechaLiquidacion;
						f.FechaHasta = tran.FechaVencimiento;
						f.IdInterno = t.IdTransaccion;
						f.SoloDiasHabiles = "N";
						entradaObtenerPlazo.p_Fechas.add(f);
					}
				}
			}
		}
		
		listaTransaccionesModificar.add(t);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccionMarketData.prepararEntradaModificarTransaccion tiempo: " + (fin - inicio));
	}
}

	


