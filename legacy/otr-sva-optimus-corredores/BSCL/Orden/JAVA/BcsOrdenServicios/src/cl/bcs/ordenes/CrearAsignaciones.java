package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.Asignacion;
import cl.bcs.ordenes.pojo.CobroAsignacion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.com.CalcularCobros;
import cl.bcs.plataforma.com.CalcularCobros.CalcularCobrosEntrada;
import cl.bcs.plataforma.com.CalcularCobros.CalcularCobrosSalida;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe.CalcularCobrosPe_Entrada;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe.CalcularCobrosPe_Salida;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;
import cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.lim.pojo.Validacion;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.*;
import datadog.trace.api.Trace;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class CrearAsignaciones implements JavaCall,JavaCallMetadata {
	
	private static final Logger logger = LogManager.getLogger(CrearAsignaciones.class);

	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final String SQL_SELECT_ESTADO_MODULO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_PLAZO = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectTransaccionesPlazo.sql");
	private final String SQL_SELECT_ASIGNACIONES_OD = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectAsignacionesOd.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectOrdenes.sql");
	private final String SQL_SELECT_COBRO_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectCobros.sql");
	private final String SQL_SELECT_COBROS_RANGO = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectCobrosRango.sql");
	//private final String SQL_SELECT_COBRO_CORRESPONSALES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectCobrosCorresponsal.sql");
	//private final String SQL_SELECT_COBRO_CORRESPONSALES_PERU = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectCobrosCorresponsalPeru.sql");
	private final String SQL_SELECT_INSERT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_InsertAsignaciones.sql");
	private final String SQL_SELECT_INSERT_COBROS_ASIGNACIONs = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_InsertCobrosAsignacion.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectUsuarioSistema.sql");

	private final String SQL_INSERT_ASOCIACIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_InsertAsociaciones.sql");
	private final String SQL_SELECT_ASOCIACIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsignaciones_SelectAsociaciones.sql");
	private final String SQL_DELETE_ASOCIACIONES = "DELETE FROM \"BSCL.Orden.BD::ORD.CrearAsignaciones_Proceso\" WHERE 1=1";
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
			
	public static class CrearAsignaciones_Entrada {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public String	p_simularOperacion;
		public String	p_UsaReglasNegocio;
		public String	p_OrigenAsig;
		public BarraProgreso	p_BarraProgreso;
		public List<Asignacion_Entrada> p_Asignaciones;
		public String p_AplicarComision;
		public String p_EsAsignacionMasivaPrecioPromedio;
		public String p_UsaLogDetallado;
		
		public CrearAsignaciones_Entrada() {
			this.p_simularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
			this.p_OrigenAsig = "";
			this.p_Asignaciones = new ArrayList<Asignacion_Entrada>();
			this.p_AplicarComision = "S";
			this.p_EsAsignacionMasivaPrecioPromedio = "N";
			this.p_UsaLogDetallado = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion_Entrada {
		public Long IdInterno;
		public Long IdOrden;
		public Long IdTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public Long FolioCartera;
		public String CodigoIFRS;
		public String IdClasificacionTipoInstrumento;
		public String FlujoOrigen;
		public java.sql.Timestamp FechaTrade;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public BigDecimal Monto;
				
		public String GlosaRefactura = "";
		public String FechaOperacionOriginal = "";

		public Asignacion_Entrada() {
			FlujoOrigen = "INGRESOORDEN";
			DocumentoIdentidad = "";
			NombreCompleto = "";
			Monto = BigDecimal.ZERO;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearAsignaciones_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<Resultado_Salida> p_out_Resultado;
		public List<Cobro_Salida> p_out_Cobros;
		
		public CrearAsignaciones_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.p_out_Resultado = new ArrayList<Resultado_Salida>();
			this.p_out_Cobros = new ArrayList<Cobro_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String toString(boolean pretty) {
			return Utils.printJson(this, pretty);
		}
	}

	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdAsignacion;
		public String EsAPlazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Salida {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdCobroAsignacion;
		public String CodigoCobroClasificacion;
		public String IdCobro;
		public String IdMonedaCobro;
		public BigDecimal ValorCobroPerfil;
		public String FormaCobro;
		public BigDecimal MontoMinimo;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private static class EntradaValidarLimitesCuentaPropia extends BcsPojo {
		public transient Connection Connection;
		public CrearAsignaciones_Entrada EntradaCrearAsignaciones;
		public List<Asignacion_Proceso> Asignaciones;
		public Map<Long, Orden_Proceso> OrdenPorId;
		public Map<Long, Transaccion_Proceso> TransaccionPorId;
	}
	
	private static class SalidaValidarLimitesCuentaPropia extends BcsPojo {
		public List<Validacion> Validaciones;
		public List<MensajeSalida> Mensajes;
		public List<Asignacion_Proceso> Asignaciones;
		public Map<Long, Validacion> ValidacionPorIdInternoAsignacion;
	}
	
	public static class Usuario extends BcsPojo {
		public Long IdUsuario;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearAsignaciones_Entrada in = gson.fromJson(jin.toString(), CrearAsignaciones_Entrada.class);
		CrearAsignaciones_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		setMetadata(in,out);
		return dataout;
	}	
	
	@Override
	public void setMetadata(Object in,Object out) {
		CrearAsignaciones_Salida s = (CrearAsignaciones_Salida)out;
		metadata[0] = s.p_out_Resultado.size()+"";
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,Asignacion_Proceso asig,List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdInterno", asig.IdInterno)
				  .add("IdOrden", asig.IdOrden)
			      .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				asig.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				asig.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				asig.advertencias++;
			}
			else {
				asig.ok++;
			}
		}else {
			asig.ok++;
		}		
	}
	
	public void limpiarAsignaciones(List<Asignacion_Proceso> asignaciones, List<Asignacion_Proceso> asignacionesConErrores) {
		
		List<Long> idsAsignacionesError = new ArrayList<Long>();
		
		for (Asignacion_Proceso asig : asignaciones) {
			if (asig.errores > 0) {
				asignacionesConErrores.add(asig);
				idsAsignacionesError.add(asig.IdAsignacion);
			}
		}
		
		for (Asignacion_Proceso asig : asignacionesConErrores) {
			if (asignaciones.contains(asig))
				asignaciones.remove(asig);
		}
		
		//LIMPIAMOS ASIGNACIONES A PLAZO
		for (Asignacion_Proceso asig : asignaciones) {
			if (asig.IdAsignacionRef != null && asig.IdAsignacionRef > 0 && idsAsignacionesError.contains(asig.IdAsignacionRef)) {
				asignacionesConErrores.add(asig);
			}
		}
		
		for (Asignacion_Proceso asig : asignacionesConErrores) {
			if (asignaciones.contains(asig))
				asignaciones.remove(asig);
		}
	}
	
	@Trace
	@SuppressWarnings("unchecked")
	public CrearAsignaciones_Salida logica(Connection conn, CrearAsignaciones_Entrada in) throws Exception {
		Long init = System.currentTimeMillis();
		SQL _sql = new SQL();
		Gson gson = Utils.getGsonBuilder();
		JsonParser parser = new JsonParser();
		
		HashMap<String, Object> paramsUsuariSistema = new HashMap<String, Object>();
		paramsUsuariSistema.put("IdEmpresa", in.p_IdEmpresa);
		List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, paramsUsuariSistema, Usuario.class);
		Long idUsuarioSistema = usuarioSistema != null && usuarioSistema.size() > 0  ? usuarioSistema.get(0).IdUsuario :0;
		
		CrearAsignaciones_Salida out = new CrearAsignaciones_Salida();
		logger.info("Inicio servicio CrearAsignaciones");
		if (in.p_BarraProgreso == null) {
            // No habilitado para servicio principal
            
        } else {
            in.p_BarraProgreso.actualizar(1L, "PROCESANDO", "Inicio creacion de asignaciones");
        }
		
		try{
			//validaciones basicas
			{
				RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
				RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
				if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
					out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
				
				if (in.p_Asignaciones.size() == 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ASIGNACIONES",null,conn,in.p_IdEmpresa));
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
				
				ArrayList<Long> correlativos = new ArrayList<Long>();
				for (Asignacion_Entrada asig: in.p_Asignaciones) {
					if (correlativos.contains(asig.IdInterno)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",null,conn,in.p_IdEmpresa));
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
						return out;
					} else {
						correlativos.add(asig.IdInterno);
					}
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("IdModulo", _c.ID_MODULO_ORDENES);
				params.put("IdEmpresa", in.p_IdEmpresa);
				List<EstadoModulo> modulo = _sql.query(conn, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
				if (modulo.size() == 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",null,conn,in.p_IdEmpresa));
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
				if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_c.ESTADO_MODULO_CERRADO)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",null,conn,in.p_IdEmpresa));
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
				
			}
			
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");		
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(conn, in.p_IdEmpresa);
	        Fecha.setLocalOffSet(segundosZonaHoraria);
			String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::Asignacion", new Long(in.p_Asignaciones.size()));
			
			//asignaciones basicas
			ArrayList<Asignacion_Proceso> asignaciones = new ArrayList<Asignacion_Proceso>();
			ArrayList<Asignacion_Proceso> asignacionesConErrores = new ArrayList<Asignacion_Proceso>();
			/*********************/
			logger.debug("INSERT ASOCIACIONES");
			_sql.batch(conn, SQL_INSERT_ASOCIACIONES, in.p_Asignaciones, Asignacion_Entrada.class);
			List<Asociacion> _asociacion = _sql.query(conn, SQL_SELECT_ASOCIACIONES, new HashMap<>(), Asociacion.class);
			logger.debug("SELECT ASOCIACIONES");
			
			HashMap<Long,Asignacion_Proceso> asignacionesHash = new HashMap<Long,Asignacion_Proceso>();
			{
				Timestamp fechaAsignacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa));
				for (Asignacion_Entrada asig : in.p_Asignaciones) {
					//Utils.copyPojo(asig, asigProceso);
					Asignacion_Proceso asigProceso = copyPojoAsignacionEntradaAsignacionProceso(asig);
					
					asigProceso.IdAsignacion = secuencias.get(in.p_Asignaciones.indexOf(asig));
					asigProceso.IdEstadoAsignacion = _c.ESTADO_ASIGNACION_NO_ENVIADA;

					if ("INGRESOINSTRUCCION".equals(asig.FlujoOrigen)) {
						asigProceso.FechaAsignacion = new java.sql.Timestamp(asig.FechaTrade.getTime());
					} else {
						asigProceso.FechaAsignacion = fechaAsignacion;
					}

					asigProceso.EsAPlazo = "N";
					if (_asociacion.size() > 0)
						asigProceso.IdAsignacionGrupo = asig.IdOrden;
					
					asignaciones.add(asigProceso);
					asignacionesHash.put(asigProceso.IdAsignacion, asigProceso);
				}
			}
			
			//VALIDACIONES DATOS BASICOS
			{
				for (Asignacion_Proceso asig : asignaciones) {
					
					ProcesaRequisito(conn, in.p_IdEmpresa,(asig.IdOrden == null), "NO_SE_ENVIO_ORDEN_DE_ASIGNACION", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,(asig.IdTransaccion == null), "NO_SE_ENVIO_TRANSACCION_DE_ASIGNACION", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);		
				}
				
				limpiarAsignaciones(asignaciones, asignacionesConErrores);
				if (asignaciones.size() == 0) {
					if(in.p_BarraProgreso != null) {
						in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Validando asignaciones");
						
					}
					return out;
				}
			}
			
			//transacciones
			HashMap<Long,Transaccion_Proceso> transacciones = new HashMap<>();
			HashMap<Long,Transaccion_Proceso> transaccionesOd = new HashMap<>();
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(10L,"PROCESANDO","Obteniendo transacciones");
					
				}
			}
			
			{
				HashMap<String,Object> params = new HashMap<>();
				ArrayList<Long> idsTransaccion = new ArrayList<>();
				ArrayList<Long> idsTransaccionPlazo = new ArrayList<>();
				ArrayList<Long> idsTransaccionOd = new ArrayList<>();
				ArrayList<Long> idsTransaccionPlazoOd = new ArrayList<>();
				for (Asignacion_Entrada asig : in.p_Asignaciones) {
					idsTransaccion.add(asig.IdTransaccion);
				}
				//params.put("idsTransaccion", idsTransaccion);
				List<Transaccion_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, Transaccion_Proceso.class);
				logger.debug("SELECT transacciones");
				for (Transaccion_Proceso tran : _transacciones) {
					transacciones.put(tran.IdTransaccion, tran);
					if (tran.IdTransaccionPlazo != null && tran.IdTransaccionPlazo > 0) {
						idsTransaccionPlazo.add(tran.IdTransaccionPlazo);
					}
					if (tran.IdTransaccionOd != null && tran.IdTransaccionOd> 0) {
						idsTransaccionOd.add(tran.IdTransaccionOd);
					}
				}
				
				//TODO obtener transacciones con where in, cambiar a tabla temporal.
				if (idsTransaccionPlazo.size() > 0) {
					HashMap<String,Object> paramsPlazo = new HashMap<>();
					paramsPlazo.put("idsTransaccion", idsTransaccionPlazo);
					List<Transaccion_Proceso> _transaccionesPlazo = _sql.query(conn, SQL_SELECT_TRANSACCIONES_PLAZO, paramsPlazo, Transaccion_Proceso.class);
					for (Transaccion_Proceso tran : _transaccionesPlazo) {
						transacciones.put(tran.IdTransaccion, tran);
						
					}
				}
				if (idsTransaccionOd.size() > 0) {
					HashMap<String,Object> paramsOd = new HashMap<>();
					paramsOd.put("idsTransaccion", idsTransaccionOd);
					List<Transaccion_Proceso> _transaccionesOd = _sql.query(conn, SQL_SELECT_TRANSACCIONES_PLAZO, paramsOd, Transaccion_Proceso.class);
					for (Transaccion_Proceso tran : _transaccionesOd) {
						transaccionesOd.put(tran.IdTransaccionOd, tran);
						transacciones.put(tran.IdTransaccion, tran);
						if (tran.IdTransaccionPlazo != null && tran.IdTransaccionPlazo > 0) {
							idsTransaccionPlazoOd.add(tran.IdTransaccionPlazo);
						}
					}
				}
				if (idsTransaccionPlazoOd.size() > 0) {
					HashMap<String,Object> paramsPlazo = new HashMap<>();
					paramsPlazo.put("idsTransaccion", idsTransaccionPlazoOd);
					List<Transaccion_Proceso> _transaccionesPlazoOd = _sql.query(conn, SQL_SELECT_TRANSACCIONES_PLAZO, paramsPlazo, Transaccion_Proceso.class);
					for (Transaccion_Proceso tran : _transaccionesPlazoOd) {
						transaccionesOd.put(tran.IdTransaccionOd, tran);
						transacciones.put(tran.IdTransaccion, tran);
					}
				}
				
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(25L,"PROCESANDO","Creando asignaciones");
					
				}
			}
			HashMap<Long,Object> ordenesAsoc = new HashMap<>();
			{
				//{\"IdInterno\":23650,\"IdOrden\":23650,\"IdTransaccion\":30501,\"Cantidad\":4000,\"ValorRescate\":0,\"FolioCartera\":0}
				//-********Buscar Asociaciones para reporte. Corresponde a las Ordenes a las cuales se le debe asignar el palo de compra PH y Venta TP****
				/*HashMap<String,Object> params = new HashMap<>();
				ArrayList<Long> idsOrdenes = new ArrayList<>();
				for (Asignacion_Entrada asig : in.p_Asignaciones) {
					idsOrdenes.add(asig.IdOrden);
				}
				params.put("idsOrdenes", idsOrdenes);
				List<Asociacion> _asociacion = _sql.query(conn, SQL_SELECT_ASOCIACIONES, params, Asociacion.class);*/
				List<Long> secuenciasAsoc = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::Asignacion", new Long(_asociacion.size()));
				for (Asignacion_Entrada asig : in.p_Asignaciones) {
					List<Asociacion> asocList = new ArrayList<Asociacion>();
					for (Asociacion asoc : _asociacion){
						if (asig.IdOrden.equals(asoc.IdOrdenN)){
							asocList.add(asoc);
						}
					}
					ordenesAsoc.put(asig.IdOrden, asocList);
				}
				for (Asignacion_Entrada asig : in.p_Asignaciones) {
					List<Asociacion> ordAsoc = (List<Asociacion>)ordenesAsoc.get(asig.IdOrden);
					for (Asociacion asoc: ordAsoc){
						if (asig.IdOrden.equals(asoc.IdOrdenN)){
							Transaccion_Proceso tranOd = transaccionesOd.get(asig.IdTransaccion);
							Asignacion_Proceso asigOd = new Asignacion_Proceso();
							asigOd.IdAsignacion = secuenciasAsoc.get(ordAsoc.indexOf(asoc));
							asigOd.IdInterno = asoc.IdOrdenM;
							asigOd.IdOrden = asoc.IdOrdenM;
							asigOd.IdTransaccion = tranOd.IdTransaccion;
							asigOd.Cantidad = asoc.Cantidad;
							asigOd.ValorRescate = BigDecimal.ZERO;
							asigOd.FolioCartera = 0L;
							asigOd.EsAPlazo = "N";
							asigOd.FechaAsignacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa));
							asigOd.IdEstadoAsignacion = _c.ESTADO_ASIGNACION_NO_ENVIADA;
							asigOd.IdAsignacionGrupo = asoc.IdOrdenN;
							
							asignaciones.add(asigOd);
							asignacionesHash.put(asigOd.IdAsignacion, asigOd);
							//logger.debug(tranOd);
						}
					}
					
				}
				// --**************************************************************************************************************************************
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(40L,"PROCESANDO","Vinculando asignaciones");
					
				}
			}
			//ordenes
			HashMap<Long,Orden_Proceso> ordenes = new HashMap<>();
			{
				HashMap<String,Object> params = new HashMap<>();
				ArrayList<Long> idsOrdenes = new ArrayList<>();
				for (Asignacion_Proceso asig : asignaciones){//in.p_Asignaciones) {
					idsOrdenes.add(asig.IdOrden);
				}
				//params.put("idsOrdenes", idsOrdenes);
				logger.debug("SELECT ordenes");
				List<Orden_Proceso> _ordenes = _sql.query(conn, SQL_SELECT_ORDENES, params, Orden_Proceso.class);
				for (Orden_Proceso ord : _ordenes) {
					ordenes.put(ord.IdOrden, ord);
				}
			}
			
			//asignaciones plazo
			{
				List<Long> secuenciasPlazo = null;
				ArrayList<Asignacion_Proceso> asignacionesPlazo = new ArrayList<Asignacion_Proceso>();
				
				for (Asignacion_Proceso asig : asignaciones) {
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					if (tran != null && tran.IdTransaccionPlazo > 0) {
						Orden_Proceso ord = ordenes.get(asig.IdOrden);
						if (secuenciasPlazo == null) 
							secuenciasPlazo = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::Asignacion", new Long(asignaciones.size()));
						
						//Utils.copyPojo(asig, asig_plazo);
						Asignacion_Proceso asig_plazo = copyPojoAsignacionProcesoAsignacionProceso(asig);
						asig_plazo.EsAPlazo = "S";
						asig_plazo.IdTransaccion = tran.IdTransaccionPlazo;
						asig_plazo.IdAsignacion = secuenciasPlazo.get(asignaciones.indexOf(asig));
						asig_plazo.IdAsignacionRef = asig.IdAsignacion;
						asig_plazo.Tasa = ord.TasaReporte;
						asig.IdAsignacionRef = asig_plazo.IdAsignacion;
						asignacionesPlazo.add(asig_plazo);
						asignacionesHash.put(asig_plazo.IdAsignacion, asig_plazo);
					}
				}
				asignaciones.addAll(asignacionesPlazo);
			}
			
			//hash para asignaciones od PALO-PORTAFOLIO (o CLIENTE?)
			List<Asignacion_OD> asignacionesOd = new ArrayList<>();
			{
				List<Long> txs = new ArrayList<>();
				for (Asignacion_Proceso asig : asignaciones) {
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					
					if (tran.IdTransaccionOd == null || "S".equalsIgnoreCase(ord.PermiteAsignarOEODMismoRut)) {
						continue;
					}
					
					Asignacion_OD asigOd = new Asignacion_OD();
					asigOd.IdAsignacion = asig.IdAsignacion;
					asigOd.IdOrden = asig.IdOrden;
					asigOd.IdPortafolio = ord.IdPortafolio;
					asigOd.IdTransaccion = asig.IdTransaccion;
					asigOd.IdTransaccionOd = tran.IdTransaccionOd;
					asigOd.IdClasificacionTipoInstrumento = tran.IdClasificacionTipoInstrumento;
					txs.add(tran.IdTransaccionOd);
					
					asignacionesOd.add(asigOd);
				}
				
				if(txs.size() > 0){
					HashMap<String,Object> params1 = new HashMap<>();
					//params1.put("idsTransaccion", txs);
					logger.debug("SELECT ordenes od");
					List<Asignacion_OD> _asignacionesOd = _sql.query(conn, SQL_SELECT_ASIGNACIONES_OD, params1, Asignacion_OD.class);
					asignacionesOd.addAll(_asignacionesOd);
					
					//RECORRO ASIGNACIONES, BUSCO SU TRANSACCION, BUSCO SU OD SI TIENE, BUSCO SI TIENE ASIGNACIONES, BUSCO SI ESAS ASIGNACIONES ES EL MISMO CLIENTE
					Map<Long, List<Asignacion_OD>> hashAsignacionesOD = asignacionesOd.stream().collect(Collectors.groupingBy(asig -> asig.IdTransaccion));
					for (Asignacion_OD asig : asignacionesOd) {
						List<Asignacion_OD> asignacionesOD2 = hashAsignacionesOD.getOrDefault(asig.IdTransaccionOd, new ArrayList<>());
						
						for (Asignacion_OD asig2 : asignacionesOD2) {
							if (asig == asig2) {
								continue;
							}
							
							if (!asig.IdClasificacionTipoInstrumento.equalsIgnoreCase("DV") && (asig.IdTransaccionOd.equals(asig2.IdTransaccion) && asig.IdPortafolio.equals(asig2.IdPortafolio))) {
								Asignacion_Proceso asigReal = asignacionesHash.get(asig.IdAsignacion);
								if (asigReal != null) {
									asigReal.odMismoPortafolio = true;
								}
							}	
						}
					}
				}
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(45L,"PROCESANDO","Validando ingreso de asignaciones");
					
				}
			}
			//VALIDACIONES
			logger.debug("validar asignaciones");
			{
				String asignacionCronologicaObligatoria = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "AsignacionCronologicaObligatoria");
				String PermitePalosNegociacionReservado = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "FACTURACION", "PALOS_NEGOCIACION_RESERVADO");
				for (Asignacion_Proceso asig : asignaciones) {
	
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					
					ProcesaRequisito(conn, in.p_IdEmpresa,(ord == null), "ORDEN_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,(tran == null), "TRANSACCION_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
		
					if (ord == null || tran == null)
						continue;
					
					if (asig.EsAPlazo.equalsIgnoreCase(_c.NO)) {
						if("S".equalsIgnoreCase(PermitePalosNegociacionReservado)) {
							ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Reservado.equalsIgnoreCase(_c.SI) && !(tran.IdUsuarioReserva.equals(in.p_IdUsuario) || in.p_IdUsuario.equals(idUsuarioSistema)) ), "TRANSACCION_RESERVADA", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);	
						}else {
							ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Reservado.equalsIgnoreCase(_c.SI) && !tran.IdUsuarioReserva.equals(in.p_IdUsuario) ), "TRANSACCION_RESERVADA", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);							
						}
						
						ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Bloqueado.equalsIgnoreCase(_c.SI)), "TRANSACCION_BLOQUEADA", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.IdTipoOperacion.equalsIgnoreCase(tran.IdTipoOperacion)), "TIPO_OPERACION_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(ord.IdCorredorCorresponsal != null && tran.IdCorredoraCorresponsal != null && !ord.IdCorredorCorresponsal.equals(tran.IdCorredoraCorresponsal)), "UI_CORREDOR_CORRESPONSAL_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.IdInstrumento.equals(tran.IdInstrumento)), "INSTRUMENTO_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.IdCondicionDeLiquidacion.equalsIgnoreCase(tran.IdCondicionDeLiquidacion)), "COND_LIQ_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(!tran.IdMoneda.equalsIgnoreCase(ord.IdMonedaOrden)), "MONEDA_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(!(ord.IdBolsa.equalsIgnoreCase(tran.IdBolsa) || ord.IdBolsa.equalsIgnoreCase(_c.ID_CUALQUIER_BOLSA))), "BOLSA_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.EsVentaCorta.equalsIgnoreCase(tran.EsVentaCorta)), "MARCA_VENTA_CORTA_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa,(asig.odMismoPortafolio), "ASIGNACION_OD_MISMO_PORTAFOLIO", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					}
					
					//COMPRA DE SIMULTANEA Y COMPRA DE REPORTES POR AGENTE NO LLEVA SUBYACENTE
					if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV)) {
						if (ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER))
							ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.IdInstrumentoSubyacente.equals(tran.IdInstrumentoSubyacente)), "SUBYACENTE_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);	
					}
					
					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						ProcesaRequisito(conn, in.p_IdEmpresa,(asig.ValorRescate.doubleValue() <= 0), "VAL_RESC_MENOR_IGUAL_CERO", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					}
					else {
						ProcesaRequisito(conn, in.p_IdEmpresa,(asig.Cantidad.doubleValue() <= 0), "CANTIDAD_MENOR_IGUAL_CERO", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
	
					}
					
					//regla de precio
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
						if (ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
							
							if (tran.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM))
							{								
								//IMPMBIOPT-805: VENTA DE SIMULTANEA DEBE VALIDAR INSTRUMENTO SUBYACENTE
								if (ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
									ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.IdInstrumentoSubyacente.equals(tran.IdInstrumentoSubyacente)), "SUBYACENTE_NO_COINCIDE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
								}
							}

						}
						else if (ord.IdTipoPrecio.equalsIgnoreCase(_c.TIPO_PRECIO_LIMITE) && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
							ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Precio.doubleValue() > ord.PrecioOrden.doubleValue()), "PRECIO_NO_VALIDO", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						}
						else if (ord.IdTipoPrecio.equalsIgnoreCase(_c.TIPO_PRECIO_LIMITE) && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA)) {
							ProcesaRequisito(conn, in.p_IdEmpresa,(tran.Precio.doubleValue() < ord.PrecioOrden.doubleValue()), "PRECIO_NO_VALIDO", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
						}
					}
					
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE) && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
						//SI ES COMPRA DE REPORTE NO VALIDA ORDEN OFERTADA
					}
					else{
						ProcesaRequisito(conn, in.p_IdEmpresa,(!ord.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)), "ESTADO_ORDEN_INCORRECTO_ASIGNACION", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					}
					ProcesaRequisito(conn, in.p_IdEmpresa,(!tran.IdEstado.equalsIgnoreCase(_c.ESTADO_TRANSACCION_INGRESADA) &&! tran.IdEstado.equalsIgnoreCase(_c.ESTADO_TRANSACCION_EN_PROCESO)), "ESTADO_TRANSACCION_INCORRECTO_ASIGNACION", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,(!tran.IdLibroOrdenes.equals(ord.IdLibroOrdenes)), "CLIENTE_NO_PERTENECE_LIBRO_ORDENES", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
						ProcesaRequisito(conn, in.p_IdEmpresa,(tran.CorteMinimo.doubleValue() != 0 && asig.Cantidad.doubleValue() % tran.CorteMinimo.doubleValue() != 0), "CANTIDAD_NO_ES_MULTIPLO_CORTES", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						BigDecimal ValorRescateMODCorteMinimo = calcularModulo(asig.ValorRescate.doubleValue(), tran.CorteMinimo.doubleValue());
						BigDecimal CeroBigDecimal = new BigDecimal(0);
						ProcesaRequisito(conn, in.p_IdEmpresa,(tran.CorteMinimo.doubleValue() != 0 && ValorRescateMODCorteMinimo.compareTo(CeroBigDecimal) != 0), "VALOR_RESCATE_NO_ES_MULTIPLO_CORTES", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					}
				}
				
				limpiarAsignaciones(asignaciones, asignacionesConErrores);
				if (asignaciones.size() == 0) {
					if(in.p_BarraProgreso != null) {
						in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Validando asignaciones");
						
					}
					return out;
				}
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(50L,"PROCESANDO","Validando cantidades y montos");
					
				}
			}
			//actualiza cantidades, montos y valor de rescate
			logger.debug("actualiza cantidades, montos y valor de rescate");
			{
				for (Asignacion_Proceso asig : asignaciones) {
					
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					
					//cantidad
					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.EsFungible.equalsIgnoreCase(_c.SI)) {
						asig.Cantidad = BigDecimal.ONE;
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.EsFungible.equalsIgnoreCase(_c.NO)) {
						asig.Cantidad = asig.ValorRescate.divide(tran.CorteMinimo,0,RoundingMode.FLOOR);
					}
					else {
						asig.Cantidad = asig.Cantidad;
					}
					
					
					//monto
					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
						asig.Monto = tran.Precio.multiply(asig.Cantidad);
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
						asig.Monto = tran.Precio.multiply(asig.Cantidad).multiply(new BigDecimal(tran.Tamano));
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						if (tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_RRF)){
							asig.Monto = tran.Precio.divide(new BigDecimal(100)).multiply(asig.Cantidad);
						}
						else {
							asig.Monto = tran.Precio.multiply(asig.Cantidad);
						}
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
						asig.Monto = tran.Monto.multiply(asig.Cantidad).divide(tran.Cantidad,20,RoundingMode.HALF_UP);
					}
					else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						asig.Monto = tran.Monto.multiply(asig.ValorRescate).divide(tran.ValorRescate,20,RoundingMode.HALF_UP);
					}
					
					//valor rescate
					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						asig.ValorRescate = asig.ValorRescate;
					}
					else {
						asig.ValorRescate = BigDecimal.ZERO;
					}
					
					asig.Monto = MonedaFormato.formatear(conn, asig.Monto, MonedaFormato.MONTO, tran.IdMoneda).valor;
				}
			}
			//PARA OPERACIONES A PLAZO SIM, REPORTES
			logger.debug("PARA OPERACIONES A PLAZO SIM, REPORTES");
			{
				for (Asignacion_Proceso asig : asignaciones) {
					asig.PrecioPlazo = BigDecimal.ZERO;
					asig.MontoAsignadoPlazo = BigDecimal.ZERO;
					asig.Tasa = BigDecimal.ZERO;
					Asignacion_Proceso asigPlazo = asignacionesHash.get(asig.IdAsignacionRef);
					if (asigPlazo == null) continue;
					Transaccion_Proceso tranPlazo = transacciones.get(asigPlazo.IdTransaccion);
					Orden_Proceso orden = ordenes.get(asig.IdOrden);
					//asignacion a plazo
					//if ("S".equalsIgnoreCase(asig.EsAPlazo)){
						asig.MontoAsignadoInteresPlazo = (asig.Cantidad.multiply(orden.MontoInteresPlazo)).divide(orden.Cantidad,20,RoundingMode.HALF_UP);
						//continue;
					//}
					//else {
						asig.MontoAsignadoInteresContado = (asig.Cantidad.multiply(orden.MontoInteresContado)).divide(orden.Cantidad,20,RoundingMode.HALF_UP);
					//}
					//if (asig.IdAsignacionRef != null && asig.IdAsignacionRef > 0 && "S".equalsIgnoreCase(asig.EsAPlazo)){
						if (tranPlazo != null ){
							if (tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)) {
								asig.PrecioPlazo = tranPlazo.Precio.add(tranPlazo.Precio.multiply(tranPlazo.Tasa).divide(new BigDecimal(orden.BaseTasa),20,RoundingMode.HALF_UP).divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
								asig.MontoAsignadoPlazo = asig.Cantidad.multiply(asig.PrecioPlazo);
							}
							else if (tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)) {
								asig.PrecioPlazo = BigDecimal.ZERO;;
								asig.MontoAsignadoPlazo = asig.Monto.multiply(tranPlazo.Tasa).divide(new BigDecimal(orden.BaseTasa),20,RoundingMode.HALF_UP).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
							}/*
							else if (tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV) || tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF)) {
								BigDecimal tasa = orden.TasaPromedioReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
								BigDecimal periodos = new BigDecimal(orden.PlazoReporte).divide(new BigDecimal(orden.BaseTasa),20,RoundingMode.HALF_UP);
								Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
								asig.PrecioPlazo = orden.PrecioOrden.multiply(new BigDecimal(factor));//tranPlazo.Precio.multiply(new BigDecimal(factor));
								asig.MontoAsignadoPlazo = asig.Cantidad.multiply(asig.PrecioPlazo);
								asig.Tasa = orden.TasaReporte;
								asig.TasaPromedio = orden.TasaPromedioReporte;
							}
							else if (tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV) || tranPlazo.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)) {
								BigDecimal tasa = orden.TasaReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
								BigDecimal periodos = new BigDecimal(orden.PlazoReporte).divide(new BigDecimal(orden.BaseTasa),20,RoundingMode.HALF_UP);
								Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
								asig.PrecioPlazo = BigDecimal.ZERO;
								asig.MontoAsignadoPlazo = asig.Monto.multiply(new BigDecimal(factor));
								asig.Tasa = orden.TasaReporte;
								asig.TasaPromedio = orden.TasaPromedioReporte;
							}*/
							else if (orden.TipoInteres!= null && orden.TipoInteres.equalsIgnoreCase(_c.INTERES_COMPUESTO) && (orden.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV) || orden.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV))) {
								BigDecimal tasa = orden.TasaPromedioReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
								BigDecimal periodos = new BigDecimal(orden.PlazoReporte).divide(new BigDecimal(orden.BaseTasa),20,RoundingMode.HALF_UP);
								Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
								asig.PrecioPlazo = orden.PrecioOrden.multiply(new BigDecimal(factor));
								asig.MontoAsignadoPlazo = asig.Cantidad.multiply(asig.PrecioPlazo);
								asig.Tasa = orden.TasaReporte;
								asig.TasaPromedio = orden.TasaPromedioReporte;
							}
							else if (orden.TipoInteres!= null && orden.TipoInteres.equalsIgnoreCase(_c.INTERES_COMPUESTO) && (orden.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF) || orden.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF))) {
								BigDecimal tasa = orden.TasaPromedioReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
								BigDecimal periodos = new BigDecimal(orden.PlazoReporte).divide(new BigDecimal(orden.BaseTasa),20,RoundingMode.HALF_UP);
								BigDecimal montoMasInteres = asig.Monto.add(asig.MontoAsignadoInteresContado);
								Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
								BigDecimal montoMasInteresFactor = montoMasInteres.multiply(new BigDecimal(factor));
								asig.PrecioPlazo = (montoMasInteresFactor.subtract(asig.MontoAsignadoInteresPlazo)).divide(asig.Cantidad,20,RoundingMode.HALF_UP);
								asig.MontoAsignadoPlazo = asig.PrecioPlazo.multiply(asig.Cantidad);
								asig.Tasa = orden.TasaReporte;
								asig.TasaPromedio = orden.TasaPromedioReporte;
							}
							
							asig.MontoAsignadoInteresContado = (asig.Cantidad.multiply(orden.MontoInteresContado)).divide(orden.Cantidad);
						}
						asig.MontoAsignadoPlazo = MonedaFormato.formatear(conn, asig.MontoAsignadoPlazo, MonedaFormato.MONTO, tranPlazo.IdMoneda).valor;
					//}
					
				}
			}
			
			//totales por orden y transaccion
			logger.debug("totales por orden y transaccion");
			{
				for (Asignacion_Proceso asig : asignaciones) {
	
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					Orden_Proceso orden = ordenes.get(asig.IdOrden);
					
					tran.CantidadTotalAsignada = tran.CantidadTotalAsignada.add(asig.Cantidad);
					tran.MontoTotalAsignado = tran.MontoTotalAsignado.add(asig.Monto);
					tran.ValorRescateTotalAsignado = tran.ValorRescateTotalAsignado.add(asig.ValorRescate);
	
					if (asig.EsAPlazo.equalsIgnoreCase(_c.NO)) {
						orden.CantidadTotalAsignada = orden.CantidadTotalAsignada.add(asig.Cantidad);
						orden.MontoTotalAsignado = orden.MontoTotalAsignado.add(asig.Monto);
						orden.ValorRescateTotalAsignado = orden.ValorRescateTotalAsignado.add(asig.ValorRescate);
					}
					
				}
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(55L,"PROCESANDO","Validando sobre asignaciones");
					
				}
			}
			//VALIDACION DE SOBREASIGNACION
			logger.debug("VALIDACION DE SOBREASIGNACION");
			{
				if (in.p_OrigenAsig.equalsIgnoreCase(_c.ORIGEN_REFACTURACION) || in.p_OrigenAsig.equalsIgnoreCase(_c.ORIGEN_REASIGNACION)) {
					
				} 
				else
				{
					HashSet<Orden_Proceso> ordenesSobreasignadas = new HashSet<>();
					for (Long key : ordenes.keySet()) {
						Orden_Proceso ord = ordenes.get(key);
						
						//DERIVADOS, RF
						if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)
								|| ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
							if (ord.CantidadTotalAsignada.doubleValue() > ord.CantidadDisponible.doubleValue()) {
								ordenesSobreasignadas.add(ord);
							}
						}					
						//IF
						else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
							if (ord.ValorRescateTotalAsignado.doubleValue() > ord.ValorRescate.doubleValue()) {
								ordenesSobreasignadas.add(ord);
							}
						}
						
						//COMSIM SIMPRE POR MONTO
						else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)) {
							if (ord.MontoTotalAsignado.doubleValue() > ord.MontoDisponible.doubleValue()) {
								ordenesSobreasignadas.add(ord);
							}
						}
						
						//RESTO DE RV
						else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
							if (ord.CantidadTotalAsignada.doubleValue() > ord.CantidadDisponible.doubleValue()) {
								ordenesSobreasignadas.add(ord);
							}
						}
						//COMRRV y COMRRF SIMPRE POR MONTO
						else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE) && 
								(ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV) )) {
							if (ord.MontoTotalAsignado.doubleValue() > ord.MontoDisponible.doubleValue()) {
								ordenesSobreasignadas.add(ord);
							}
						}
						
						//RESTO DE RV
						else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE) && 
								(ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV) )) {
							if (ord.CantidadTotalAsignada.doubleValue() > ord.CantidadDisponible.doubleValue()) {
								ordenesSobreasignadas.add(ord);
							}
						}
					}
				
					HashSet<Transaccion_Proceso> transaccionesSobreasignadas = new HashSet<>();
					for (Long key : transacciones.keySet()) {
						Transaccion_Proceso tran = transacciones.get(key);
						
						//DERIVADOS, RV, RF
						if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)
								|| tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
								|| tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)
								|| tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
							if (tran.CantidadTotalAsignada.doubleValue() > tran.CantidadDisponible.doubleValue()) {
								transaccionesSobreasignadas.add(tran);
							}
						}
						//IF FUNGIBLE
						else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
							if (tran.ValorRescateTotalAsignado.doubleValue() > tran.ValorRescateDisponible.doubleValue()) {
								transaccionesSobreasignadas.add(tran);
							}
						}
					}
					
					for (Asignacion_Proceso asig : asignaciones) {
						if (asig.EsAPlazo.equalsIgnoreCase(_c.SI))
							continue;
						Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
						Orden_Proceso ord = ordenes.get(asig.IdOrden);
						
						if (transaccionesSobreasignadas.contains(tran)) {
							String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa,"TRANSACCION_SOBREASIGNADA").msg();
							out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_SOBREASIGNADA",msg));
							asig.errores++;
						}
						
						if (ordenesSobreasignadas.contains(ord)) {
							String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ORDEN_SOBREASIGNADA").msg();
							out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,"ORDEN_SOBREASIGNADA",msg));
							asig.errores++;
						}
						
					}
	
					limpiarAsignaciones(asignaciones, asignacionesConErrores);
					if (asignaciones.size() == 0) {
						if(in.p_BarraProgreso != null) {
							in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Validando asignaciones");
							
						}
						return out;
					}	
				}
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(60L,"PROCESANDO","Validando cobros de asignaciones");
					
				}
			}
			//LECTURA DE COBROS
			logger.debug("LECTURA DE COBROS");
			Long IdInternoCobro = 0L;
			HashMap<Long,CobroAsignacion_Proceso> cobrosHash = new HashMap<Long,CobroAsignacion_Proceso>();
			{
				HashMap<String,Object> params = new HashMap<>();
				List<Long> idsGrupoCobro = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					if (!idsGrupoCobro.contains(ord.IdGrupoCobro))
						idsGrupoCobro.add(ord.IdGrupoCobro);
				}
				params.put("idsGrupoCobro", idsGrupoCobro);
				List<CobroOrden_Proceso> _cobrosOrden = _sql.query(conn, SQL_SELECT_COBRO_ORDENES, params, CobroOrden_Proceso.class);
				List<DefinicionRangoCobro> _cobrosRangoOrden = _sql.query(conn, SQL_SELECT_COBROS_RANGO, params, DefinicionRangoCobro.class);
				
				for (CobroOrden_Proceso cobOrden : _cobrosOrden) {
					Orden_Proceso ord = ordenes.get(cobOrden.IdOrden);
					
					for (DefinicionRangoCobro rango : _cobrosRangoOrden){
						if (cobOrden.IdCobroOrden.equals(rango.IdCobroOrden) && cobOrden.IdOrden.equals(rango.IdOrden) && cobOrden.IdGrupoCobro.equals(rango.IdGrupoCobro)){
							cobOrden.RangoCobros.add(rango);
						}
					}
					ord.cobrosOrden.add(cobOrden);
				}
					
				for (Asignacion_Proceso asig : asignaciones) {
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					for (CobroOrden_Proceso cobOrden : ord.cobrosOrden) {
						if (localizacionSistema.equalsIgnoreCase("CL") && asig.EsAPlazo.equalsIgnoreCase("S")) {
							continue;
						}
						if (asig.EsAPlazo.equalsIgnoreCase("N")//(localizacionSistema.equalsIgnoreCase("CL") && asig.EsAPlazo.equalsIgnoreCase("N")) 
								//|| (localizacionSistema.equalsIgnoreCase("PE") && asig.EsAPlazo.equalsIgnoreCase("N") && (cobOrden._Otros_CheckOperacionContado.equalsIgnoreCase(_c.SI)))
								//|| (localizacionSistema.equalsIgnoreCase("PE") && asig.EsAPlazo.equalsIgnoreCase("S") && (cobOrden._Otros_CheckOperacionPlazo.equalsIgnoreCase(_c.SI)))
								) {
							//Utils.copyPojo(cobOrden, cobAsig);
							CobroAsignacion_Proceso cobAsig = copyPojoCobroAsignacionProceso(cobOrden);
							cobAsig.IdInternoCobro = IdInternoCobro++;
							cobAsig.IdAsignacion = asig.IdAsignacion;
							cobAsig.ValorCobroAplicado = cobOrden.ValorCobroFinal;
							cobAsig.FechaIngresoCobro = asig.FechaAsignacion;
							cobAsig._RangoCobros = new ArrayList<DefinicionRangoCobro>(cobOrden.RangoCobros);
							asig.cobros.add(cobAsig);
							cobrosHash.put(cobAsig.IdInternoCobro,cobAsig);
						}
					}
				}			
			}
			
			//COBRO DE CORRESPONSALES
			{
				/*HashMap<String,Object> params = new HashMap<>();
				List<Long> idsCorredorCorresponsal = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					if (tran.IdCorredoraCorresponsal != null && tran.IdCorredoraCorresponsal.compareTo(0L) == 1)
						idsCorredorCorresponsal.add(tran.IdCorredoraCorresponsal);
				}
				
				if (localizacionSistema.equalsIgnoreCase("CL") && idsCorredorCorresponsal.size() > 0) {
					params.put("idsCorredorCorresponsal", idsCorredorCorresponsal);
					List<CobroOrden_Proceso> cobrosCorresponsales = _sql.query(conn, SQL_SELECT_COBRO_CORRESPONSALES, params, CobroOrden_Proceso.class);
					
					for (CobroOrden_Proceso cobCorresponsal : cobrosCorresponsales) {
						for (Asignacion_Proceso asig : asignaciones) {
							
							Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
							if (tran.IdCorredoraCorresponsal.equals(cobCorresponsal.IdCorredoraCorresponsal)) {
								CobroAsignacion_Proceso cobAsig = new CobroAsignacion_Proceso();
								Utils.copyPojo(cobCorresponsal, cobAsig);
								cobAsig.IdInternoCobro = IdInternoCobro++;
								cobAsig.IdAsignacion = asig.IdAsignacion;
								cobAsig.ValorCobroPerfil = cobCorresponsal.ValorCobroAplicado;
								cobAsig.FechaIngresoCobro = asig.FechaAsignacion;
								cobAsig.PermiteCambiarValor = "N";
								asig.cobros.add(cobAsig);
								cobrosHash.put(cobAsig.IdInternoCobro,cobAsig);
							}
						}
					}
				}
				
				if (localizacionSistema.equalsIgnoreCase("PE") && idsCorredorCorresponsal.size() > 0) {
					
					
					//COBRO DE CORRESPONSAL	
					cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.ConsultarCobrosOperacionesEntrada entradaCorr = new cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.ConsultarCobrosOperacionesEntrada();
					for (Asignacion_Proceso asig : asignaciones) {
						
						Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
						Orden_Proceso ord = ordenes.get(asig.IdOrden);
						
						if (tran.IdCorredoraCorresponsal != null) {
							cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.InformacionOperacion arg0 = new cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.InformacionOperacion();
							arg0.IdBolsa = tran.IdBolsa;
							arg0.IdCorredorCorresponsal = tran.IdCorredoraCorresponsal;
							arg0.IdEmisor = "";
							arg0.IdInstrumento = tran.IdInstrumento;
							arg0.IdInterno = asig.IdAsignacion;
							arg0.IdTipoOperacion = tran.IdTipoOperacion;
							arg0.IdTipoProducto = tran.IdTipoProducto;
							arg0.IdTipoCanalIngreso = ord.IdTipoCanalIngreso;
							entradaCorr.p_Operaciones.add(arg0);
						}
					}
					
					params.put("idsCorredorCorresponsal", idsCorredorCorresponsal);
					List<CobroOrden_Proceso> cobrosCorresponsales = _sql.query(conn, SQL_SELECT_COBRO_CORRESPONSALES_PERU, params, CobroOrden_Proceso.class);
					
					for (CobroOrden_Proceso cobCorresponsal : cobrosCorresponsales) {
						for (Asignacion_Proceso asig : asignaciones) {
							
							Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
							if (tran.IdCorredoraCorresponsal.equals(cobCorresponsal.IdCorredoraCorresponsal)) {
								CobroAsignacion_Proceso cobAsig = new CobroAsignacion_Proceso();
								Utils.copyPojo(cobCorresponsal, cobAsig);
								cobAsig.IdInternoCobro = IdInternoCobro++;
								cobAsig.IdAsignacion = asig.IdAsignacion;
								cobAsig.ValorCobroPerfil = cobCorresponsal.ValorCobroAplicado;
								cobAsig.ValorCobroFinal =  cobCorresponsal.ValorCobroAplicado;
								cobAsig.FechaIngresoCobro = asig.FechaAsignacion;
								cobAsig.PermiteCambiarValor = "N";
								
								if (cobAsig._CheckCobroMonedaOperacion.equalsIgnoreCase("S")) {
									cobAsig.IdMoneda = tran.IdMoneda;
								}
								else {
									if (cobAsig._IdMonedaCobroOtraMoneda == null) throw new Exception("Moneda cobro no configurada");
									cobAsig.IdMoneda = cobAsig._IdMonedaCobroOtraMoneda;
								}
								
								asig.cobros.add(cobAsig);
								cobrosHash.put(cobAsig.IdInternoCobro,cobAsig);
							}
						}
					}
				}*/
			}
			
			//SECUENCIA PARA LOS COBROS
			logger.debug("SECUENCIA PARA LOS COBROS");
			{
				List<Long> secuenciasCobro = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::CobroAsignacion", new Long(cobrosHash.size()));
				int i = 0;
				for (Asignacion_Proceso asig : asignaciones) {
					for (CobroAsignacion_Proceso cob : asig.cobros) {
						cob.IdCobroAsignacion = secuenciasCobro.get(i++);
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(65L,"PROCESANDO","Validando monto de cobros");
					
				}
			}
			//MONTO ASIGNACION A MONEDA DEL COBRO
			logger.debug("MONTO ASIGNACION A MONEDA DEL COBRO");
			{
				CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
				calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 1001L;
				
				for (Asignacion_Proceso asig : asignaciones) {
					for (CobroAsignacion_Proceso cobAsig : asig.cobros) {
						
						CalculoEntrada c1 = new CalculoEntrada();
						c1.Fecha = fechaSistema;
						c1.IdInterno = cobAsig.IdInternoCobro;
						c1.IdMonedaDestino = cobAsig.IdMoneda;
						Orden_Proceso ord = ordenes.get(asig.IdOrden);
						c1.IdMonedaOrigen = ord.IdMonedaOrden;
						c1.IdTrabajo = 1001L;
						c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion);
						c1.MontoOrigen = asig.Monto;
						c1.ValorCambioLocalModificado = ord.TipoCambio;
						calcularParidadEntrada.p_CalculosEntrada.add(c1);
					}
				}
				
				CalcularParidad calcularParidadService = new CalcularParidad();
				CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
				
				for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
					
					CobroAsignacion_Proceso cob = cobrosHash.get(calc.IdInterno);
					Asignacion_Proceso asig = asignacionesHash.get(cob.IdAsignacion);
					
					if (!calc.Resultado.equals("OK")) {
						String codigo = calc.CodigoMensaje;
						String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo)
								.add("IdMonedaOrigen", calc.IdMonedaOrigen)
								.add("IdMonedaDestino", calc.IdMonedaDestino)
								.msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
						asig.errores++;
					}
					else {
						cob.MontoAsignacionMonedaCobro = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO, cob.IdMoneda).valor;
					}
				}
				
				
			}
			logger.debug("MONTO ASIGNACION A MONEDA DE LIQUIDACION Y LOCAL");
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(70L,"PROCESANDO","Validando monto liquidacion");
					
				}
			}
			//MONTO ASIGNACION A MONEDA DE LIQUIDACION Y LOCAL
			{
				CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
				calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 2001L;
				
				for (Asignacion_Proceso asig : asignaciones) {
					CalculoEntrada c1 = new CalculoEntrada();
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					c1.Fecha = _c.ORIGEN_REFACTURACION.equalsIgnoreCase(asig.GlosaRefactura) && _c.CLASIFICACION_IF.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento) ? asig.FechaOperacionOriginal : fechaSistema;
					c1.IdInterno = asig.IdAsignacion;
					c1.IdMonedaDestino = ord.IdMonedaLiquidacionOrden;
					c1.IdMonedaOrigen = ord.IdMonedaOrden;
					c1.IdTrabajo = 2001L;
					c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion);
					c1.MontoOrigen = asig.Monto;
					c1.ValorCambioLocalModificado = ord.TipoCambio;
					calcularParidadEntrada.p_CalculosEntrada.add(c1);
				}
				
				CalcularParidad calcularParidadService = new CalcularParidad();
				CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
				
				for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
					
					Asignacion_Proceso asig = asignacionesHash.get(calc.IdInterno);
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					
					if (!calc.Resultado.equals("OK")) {
						String codigo = calc.CodigoMensaje;
						String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo)
								.add("IdMonedaOrigen", calc.IdMonedaOrigen)
								.add("IdMonedaDestino", calc.IdMonedaDestino)
								.msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
						asig.errores++;
					}
					else {
						asig.MontoAsignadoLiquidacion = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO,calc.IdMonedaDestino).valor;
						asig.MontoAsignadoLocal = MonedaFormato.formatear(conn, calc.MontoLocal, MonedaFormato.MONTO,idMonedaSistema).valor;
						asig.TipoCambio = calc.FactorCambioLocal;
						asig.Paridad = calc.FactorOrigenCambio;
						asig.FechaTipoCambio = calc.FechaCambioLocal;
						asig.FechaParidad = calc.FechaOrigenCambio;
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(75L,"PROCESANDO","Calculando cobros");
					
				}
			}
			//CALCULAR COBROS
			logger.debug("CALCULAR COBROS");
			if (localizacionSistema.equalsIgnoreCase("CL"))
			{
				
				for (Asignacion_Proceso asig : asignaciones) {
					CalcularCobrosEntrada calcularCobrosEntrada = new CalcularCobrosEntrada();
					
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					Transaccion_Proceso tran = transacciones.get(asig.IdTransaccion);
					
					calcularCobrosEntrada.DiasPlazo = ord.PlazoSimultanea.longValue();
					calcularCobrosEntrada.Tasa = tran.Tasa;
					
					for (CobroAsignacion_Proceso cob: asig.cobros) {
						CalcularCobros.CobroEntrada c1 = new CalcularCobros.CobroEntrada();
						//Utils.copyPojo(cob, c1);
						c1.IdInternoCobro = cob.IdInternoCobro;
						//c1.MontoOperacionMonedaCobro = cob.MontoOperacionMonedaCobro;
						c1.IdCobro = cob.IdCobro;
						c1.ValorCobroPerfil = cob.ValorCobroPerfil;
						c1.MontoMinimo = cob.MontoMinimo;
						c1.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
						c1.PermiteCambiarValor = cob.PermiteCambiarValor;
						c1.ValorCobroAplicado = cob.ValorCobroAplicado;
						//c1.IdMonedaCobro = cob.IdMonedaCobro;
						
						c1.IdMonedaCobro = cob.IdMoneda;
						c1.MontoOperacionMonedaCobro = cob.MontoAsignacionMonedaCobro;
						calcularCobrosEntrada.cobros.add(c1);
					}
					CalcularCobros calcularCobrosService = new CalcularCobros();
					CalcularCobrosSalida calcularCobrosSalida = calcularCobrosService.logica(conn,calcularCobrosEntrada);
					for (CalcularCobros.CobroSalida cobroSalida : calcularCobrosSalida.p_out_Cobros) {
						CobroAsignacion_Proceso cobroAsignacion = cobrosHash.get(cobroSalida.IdInternoCobro);
						cobroAsignacion.ValorCobroAplicado = cobroSalida.ValorCobroFinal;
						cobroAsignacion.ValorCobroFinal= cobroSalida.ValorCobroFinal;
						cobroAsignacion.MontoCobroCalculado = cobroSalida.MontoCobroCalculado;
						cobroAsignacion.MontoCobroCalculado = MonedaFormato.formatear(conn, cobroSalida.MontoCobroCalculado, MonedaFormato.MONTO, cobroAsignacion.IdMoneda).valor;
					}
					
				}
				
			}
			
			//CALCULAR COBROS PERU
			if (localizacionSistema.equalsIgnoreCase("PE"))
			{
				CalcularCobrosPe_Entrada calcularCobrosEntrada = new CalcularCobrosPe_Entrada();
				
				for (Asignacion_Proceso asig : asignaciones) {
					
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					if ("S".equalsIgnoreCase(asig.EsAPlazo)){
						continue;
					}
					CalcularCobrosPe.InformacionOperacion_Entrada info = new CalcularCobrosPe.InformacionOperacion_Entrada();
					info.CantidadOperacion = asig.Cantidad;
					info.FechaOperacion = Fecha.toDate(Fecha.toStringDate((asig.FechaAsignacion)));
					info.IdInterno = asig.IdAsignacion;
					info.IdMonedaLiquidacion = ord.IdMonedaLiquidacionOrden;
					info.IdMonedaOperacion = ord.IdMonedaOrden;
					info.MontoOperacion = asig.Monto;
					info.MontoLiquidacionOperacion = asig.MontoAsignadoLiquidacion;
					info.MontoOperacionPlazo = asig.MontoAsignadoPlazo;//BigDecimal.ZERO;
					info.MontoLiquidacionOperacionPlazo= asig.MontoAsignadoPlazo.compareTo(BigDecimal.ZERO) == 0? asig.MontoAsignadoLiquidacion:asig.MontoAsignadoPlazo;
					info.Tasa = asig.TasaPromedio;
					info.TasaReportado = asig.Tasa;
					info.PlazoEnDias = ord.PlazoReporte;
					info.BaseTasa = ord.BaseTasa;
					info.TipoCambio = ord.TipoCambio;
					info.MontoTotalComExternaContado = BigDecimal.ZERO;
					info.ValorIGVContado = BigDecimal.ZERO;
					info.PorcentajeAllIn = ord.PorcentajeCobros;
					info.MontoInteresContado = asig.MontoAsignadoInteresContado;
					info.MontoInteresPlazo = asig.MontoAsignadoInteresPlazo;
					info.AccionOrden = ord.AccionOrden;
					calcularCobrosEntrada.p_Operaciones.add(info);
					
					for (CobroAsignacion_Proceso cob: asig.cobros) {
						/*if (("S".equalsIgnoreCase(asig.EsAPlazo) && !asig.EsAPlazo.equalsIgnoreCase(cob._Otros_CheckOperacionPlazo)) || 
							("N".equalsIgnoreCase(asig.EsAPlazo) && !asig.EsAPlazo.equalsIgnoreCase(cob._Otros_CheckOperacionContado))){
							continue; //ya los filtra 
						}*/
						//TODO AGREGAR CORRESPONSALES
						if (cob.IdCobro.equalsIgnoreCase("CCOR")) {
							cob.MontoCobroCalculado = BigDecimal.ZERO;
							continue;
						}
						CalcularCobrosPe.DefinicionCobro_Entrada c1 = new CalcularCobrosPe.DefinicionCobro_Entrada();
						c1.CheckCobroMonedaOperacion = cob._CheckCobroMonedaOperacion;
						c1.CodigoDefinicionCobro = cob.IdCobro;
						c1.IdClaseCobro = cob.CodigoCobroClasificacion;
						c1.IdClaseCobroBase = cob.CodigoCobroClasificacionBase;
						c1.IdInterno = asig.IdAsignacion;
						c1.IdInternoCobro = cob.IdInternoCobro;
						c1.IdMonedaCobroOtraMoneda = cob._IdMonedaCobroOtraMoneda;
						c1.IdTipoCobro = cob.FormaCobro;
						c1.MontoFijo_CheckMontoOperacionMenor = cob._MontoFijo_CheckMontoOperacionMenor;
						c1.MontoFijo_CheckSiNo = cob._MontoFijo_CheckSiNo;
						c1.MontoFijo_MontoCobro = cob._MontoFijo_MontoCobro;
						c1.MontoFijo_ValorMontoOperacionMenor = cob._MontoFijo_ValorMontoOperacionMenor;
						c1.MontoFijo_ValorMontoSiNo = cob._MontoFijo_ValorMontoSiNo;
						c1.MontoFijoPorUnidad_AllInCheckValor = cob._MontoFijoPorUnidad_AllInCheckValor;
						c1.MontoFijoPorUnidad_MontoCobro = cob._MontoFijoPorUnidad_MontoCobro;
						c1.Otros_CheckAfectoImpuesto = cob._Otros_CheckAfectoImpuesto;
						c1.Otros_CheckOperacionContado = cob._Otros_CheckOperacionContado;
						c1.Otros_CheckOperacionPlazo = cob._Otros_CheckOperacionPlazo;
						c1.Otros_CheckPermiteAumentarValor = cob._Otros_CheckPermiteAumentarValor;
						c1.Otros_CheckPermiteDisminuirValor = cob._Otros_CheckPermiteDisminuirValor;
						c1.PorcentualFijo_CheckMayorEntreDos = cob._PorcentualFijo_CheckMayorEntreDos;
						c1.PorcentualFijo_CheckMenorEntreDos = cob._PorcentualFijo_CheckMenorEntreDos;
						c1.PorcentualFijo_CheckMontoMaximo = cob._PorcentualFijo_CheckMontoMaximo;
						c1.PorcentualFijo_CheckMontoMinimo = cob._PorcentualFijo_CheckMontoMinimo;
						c1.PorcentualFijo_CheckSobreCantidad = cob._PorcentualFijo_CheckSobreCantidad;
						c1.PorcentualFijo_CheckSobreMonto = cob._PorcentualFijo_CheckSobreMonto;
						c1.PorcentualFijo_IdMonedaMinimoMaximo = cob._PorcentualFijo_IdMonedaMinimoMaximo;
						c1.PorcentualFijo_ValorCobro = cob._PorcentualFijo_ValorCobro;
						c1.PorcentualFijo_ValorMontoMaximo = cob._PorcentualFijo_ValorMontoMaximo;
						c1.PorcentualFijo_ValorMontoMinimo = cob._PorcentualFijo_ValorMontoMinimo;
						c1.PorcentualTasaPlazo_ValorCobro = cob._PorcentualTasaPlazo_ValorCobro;
						c1.Otros_CheckAllIn = cob._Otros_CheckAllIn;
						c1.ValorCobroAplicado = cob.ValorCobroAplicado;
						c1.RangoCobros = cob._RangoCobros;
						c1.Otros_CheckInteresCompuesto = cob._Otros_CheckInteresCompuesto;
						c1.Otros_CheckInteresSimple = cob._Otros_CheckInteresSimple;
						c1.PorcentualRango_IdMonedaRangoMonto = cob._PorcentualRango_IdMonedaRangoMonto;
						c1.PorcentualRango_IdTipoRangoCobro = cob._PorcentualRango_IdTipoRangoCobro;
						calcularCobrosEntrada.p_Cobros.add(c1);
					}
				}
				
				calcularCobrosEntrada.p_IdEmpresa = in.p_IdEmpresa;
				CalcularCobrosPe calcularCobrosService = new CalcularCobrosPe();
				CalcularCobrosPe_Salida calcularCobrosSalida = calcularCobrosService.logica(conn,calcularCobrosEntrada);
				for (cl.bcs.plataforma.com.pe.CalcularCobrosPe.Cobro_Salida cobroSalida : calcularCobrosSalida.p_out_Cobros) {
					CobroAsignacion_Proceso cobroAsignacion = cobrosHash.get(cobroSalida.IdInternoCobro);
					if (cobroAsignacion.IdMoneda == null)
						cobroAsignacion.IdMoneda = cobroSalida.IdMonedaCobro;
					cobroAsignacion.ValorCobroAplicado = cobroSalida.ValorCobroFinal;
					cobroAsignacion.MontoCobroCalculado = cobroSalida.MontoCobroCalculado;
					cobroAsignacion.MontoCobroCalculado = MonedaFormato.formatear(conn, cobroSalida.MontoCobroCalculado, MonedaFormato.MONTO, cobroAsignacion.IdMoneda).valor;
				}
				
			}
			
			
			//MONTO COBROS A MONEDA LIQUIDACION Y LOCAL
			logger.debug("MONTO COBROS A MONEDA LIQUIDACION Y LOCAL");
			{
				CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
				calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 3001L;
				
				for (Asignacion_Proceso asig : asignaciones) {
					
					for (CobroAsignacion_Proceso cobAsig : asig.cobros) {
						CalculoEntrada c1 = new CalculoEntrada();
						c1.Fecha = fechaSistema;
						c1.IdInterno = cobAsig.IdInternoCobro;
						Orden_Proceso ord = ordenes.get(asig.IdOrden);
						c1.IdMonedaDestino = ord.IdMonedaLiquidacionOrden;
						c1.IdMonedaOrigen = cobAsig.IdMoneda;
						c1.IdTrabajo = 3001L;
						c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion);
						c1.MontoOrigen = cobAsig.MontoCobroCalculado;
						c1.ValorCambioLocalModificado = ord.TipoCambio;
						calcularParidadEntrada.p_CalculosEntrada.add(c1);
					}
				}
				
				CalcularParidad calcularParidadService = new CalcularParidad();
				CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
				
				for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
					
					CobroAsignacion_Proceso cob = cobrosHash.get(calc.IdInterno);
					Asignacion_Proceso asig = asignacionesHash.get(cob.IdAsignacion);
					Orden_Proceso ord = ordenes.get(asig.IdOrden);
					
					if (!calc.Resultado.equals("OK")) {
						String codigo = calc.CodigoMensaje;
						String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo)
								.add("IdMonedaOrigen", calc.IdMonedaOrigen)
								.add("IdMonedaDestino", calc.IdMonedaDestino)
								.msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
						asig.errores++;
					}
					else {
						cob.MontoCobroCalculadoLiquidacion = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO,ord.IdMonedaLiquidacionOrden).valor;
						cob.MontoCobroCalculadoLocal = MonedaFormato.formatear(conn, calc.MontoLocal, MonedaFormato.MONTO,idMonedaSistema).valor;
					}
				}
				
				limpiarAsignaciones(asignaciones, asignacionesConErrores);
				if (asignaciones.size() == 0) {
					if(in.p_BarraProgreso != null) {
						in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Validando asignaciones");
						
					}
					return out;
				}
			}
			
			EntradaValidarLimitesCuentaPropia entradaValidarLimitesCuentaPropia = new EntradaValidarLimitesCuentaPropia();
			entradaValidarLimitesCuentaPropia.Asignaciones = asignaciones;
			entradaValidarLimitesCuentaPropia.OrdenPorId = ordenes;
			entradaValidarLimitesCuentaPropia.TransaccionPorId = transacciones;
			entradaValidarLimitesCuentaPropia.EntradaCrearAsignaciones = in;
			entradaValidarLimitesCuentaPropia.Connection = conn;
			SalidaValidarLimitesCuentaPropia salidaValidarLimitesCuentaPropia = validarLimitesCuentaPropia(entradaValidarLimitesCuentaPropia);
			out.P_OUT_MENSAJES.addAll(salidaValidarLimitesCuentaPropia.Mensajes);
			asignaciones = new ArrayList<>(salidaValidarLimitesCuentaPropia.Asignaciones);
			
			if (asignaciones.size() == 0) {
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Validando asignaciones");
					
				}
				return out;
			}
			
			logger.debug("CREAR ASIGNACIONES");
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(80L,"PROCESANDO","Registrando asignaciones");
					
				}
			}
			if (in.p_simularOperacion.equalsIgnoreCase(_c.NO)) {
				
				List<Asignacion> asigsPojos = new ArrayList<Asignacion>();
				List<CobroAsignacion> cobrosAsigPojos = new ArrayList<CobroAsignacion>();
				
				for (Asignacion_Proceso asig : asignaciones) {
					//Utils.copyPojo(asig, asigPojo);
					Asignacion asigPojo = copyPojoAsignacion(asig);
					
					asigPojo.CantidadAsignada = asig.Cantidad;
	
					Transaccion_Proceso tran = transacciones.get(asigPojo.IdTransaccion);
					Orden_Proceso ord = ordenes.get(asigPojo.IdOrden);
					
					asigPojo.IdAsignacion = asig.IdAsignacion;
					asigPojo.IdAsignacionRef = asig.IdAsignacionRef;
					asigPojo.TipoCambio = asig.TipoCambio;
					asigPojo.IdLibroOrdenes = ord.IdLibroOrdenes;
					asigPojo.MontoAsignado = asig.Monto;
					asigPojo.ValorRescateAsignado = asig.ValorRescate;
					asigPojo.CodigoIFRS = asig.CodigoIFRS == null ? "" : asig.CodigoIFRS;
					
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)){
						asigPojo.MontoAsignadoInteres = asig.EsAPlazo.equalsIgnoreCase("S")?asig.MontoAsignadoInteresPlazo:asig.MontoAsignadoInteresContado;
					}
					else if(!tran.Intereses.equals(BigDecimal.ZERO)) {
							asigPojo.MontoAsignadoInteres = asig.Cantidad.divide(tran.Cantidad,20,RoundingMode.HALF_UP).multiply(tran.Intereses);
					}
					
					//Si es cuenta propia y compra, debe tener codigoIfrs
					if(ord.EsCuentaPropia!=null && ord.EsCuentaPropia.equalsIgnoreCase("S") && tran.AccionOrden.equalsIgnoreCase("COMPRA") && (asig.CodigoIFRS==null || asig.CodigoIFRS.equals("")) && ("RF".equals(ord.IdClasificacionTipoInstrumento) || "IF".equals(ord.IdClasificacionTipoInstrumento))) {
						String codigo = "CODIGO_IFRS_OBLIGATORIO";
						String desc = Diccionario.getMensaje(conn,in.p_IdEmpresa, codigo).msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
						return out;
					}	
					
					asigsPojos.add(asigPojo);
					
					if (asig.EsAPlazo.equalsIgnoreCase(_c.NO)){//solo las contado
						for (CobroAsignacion_Proceso cob : asig.cobros) {
							if (cob._Otros_CheckOperacionContado.equalsIgnoreCase("S")){
								//Utils.copyPojo(cob, cobAsig);
								CobroAsignacion cobAsig = copyPojoCobroAsignacion(cob);
								cobAsig.ValorCobro = cob.ValorCobroPerfil;
								cobrosAsigPojos.add(cobAsig);
							}						
							else if (cob._Otros_CheckOperacionPlazo.equalsIgnoreCase("S")){
								if (asig.IdAsignacionRef!=null && asig.IdAsignacionRef > 0){
									//Utils.copyPojo(cob, cobAsigPlazo);
									CobroAsignacion cobAsigPlazo = copyPojoCobroAsignacion(cob);
									cobAsigPlazo.ValorCobro = cob.ValorCobroPerfil;
									cobAsigPlazo.IdAsignacion = asig.IdAsignacionRef;
									cobrosAsigPojos.add(cobAsigPlazo);
								}
							}
							else{//solo para chile, anterior es peru
								//Utils.copyPojo(cob, cobAsig);
								CobroAsignacion cobAsig = copyPojoCobroAsignacion(cob);
								cobAsig.ValorCobro = cob.ValorCobroPerfil;
								cobrosAsigPojos.add(cobAsig);
							}
						}
					}
				}
	
				if (!in.p_simularOperacion.equalsIgnoreCase(_c.SI)) {
					if("S".equals(in.p_EsAsignacionMasivaPrecioPromedio)) {
						asigsPojos = obtenerIdAsignacionMasiva(conn, asigsPojos, in.p_AplicarComision);
						cobrosAsigPojos = "N".equals(in.p_AplicarComision) ? limpiarCobrosAsignasionMasiva(cobrosAsigPojos) : cobrosAsigPojos;
					}
					_sql.batch(conn, SQL_SELECT_INSERT_ASIGNACIONES, asigsPojos, Asignacion.class);
					_sql.batch(conn, SQL_SELECT_INSERT_COBROS_ASIGNACIONs, cobrosAsigPojos, CobroAsignacion.class);
					
					CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
					entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = in.p_IdEmpresa;
					entradaCrearEventoAutorizacionMasivo.P_IDMODULO = in.p_IdModulo;
					entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = in.p_IdUsuario;
					entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = _cl.NO;
					entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
					
					List<CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS> eventos = new ArrayList<>(asigsPojos.size());
					List<CrearEventoAutorizacionMasivo.ObjTABLADETALLE> detalles = new ArrayList<>(asigsPojos.size());
					AtomicLong atomicLong = new AtomicLong(0L);
					asigsPojos.stream().filter(asignacion -> _c.ESTADO_ASIGNACION_PENDIENTE_AUTORIZACION.equals(asignacion.IdEstadoAsignacion)).forEach(asignacion -> {
						final long idTrabajo = atomicLong.getAndIncrement();
						final String idAsignacionStr = String.valueOf(asignacion.IdAsignacion);
						final Orden_Proceso orden = ordenes.get(asignacion.IdOrden);
						final Transaccion_Proceso transaccion = transacciones.get(asignacion.IdTransaccion);
						final Asignacion_Proceso asignacionProceso = asignacionesHash.get(asignacion.IdAsignacion);
						final Validacion validacionLimiteCtaPropia = salidaValidarLimitesCuentaPropia.ValidacionPorIdInternoAsignacion.get(asignacionProceso.IdInterno);
						final Optional<Validacion> maybeValidacionLimiteCtaPropia = Optional.ofNullable(validacionLimiteCtaPropia);
						
						CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
						evento.IdUnion = idTrabajo;
						evento.IdTipoEvento = _c.TIPO_EVENTO_AUTORIZACION_INGRESAR_ASIGNACION;
						evento.IdConcepto = _c.TIPO_EVENTO_AUTORIZACION_INGRESAR_ASIGNACION;
						evento.IdModuloSolicita = in.p_IdModulo;
						evento.NumeroOperacionConcepto = idAsignacionStr;
						evento.FechaOperacionConcepto = Fecha.toStringDate(asignacion.FechaAsignacion);
						evento.IdCliente = orden.IdCliente;
						evento.IdUsuarioSolicita = in.p_IdUsuario;
						evento.Fecha = fechaSistema;
						evento.IdInstrumento = orden.IdInstrumento;
						evento.Cantidad = asignacion.CantidadAsignada.longValue();
						evento.Monto = asignacion.MontoAsignado;
						evento.Precio = orden.PrecioOrden;
						evento.Tasa = transaccion.Tasa;
						evento.IdMoneda = orden.IdMonedaOrden;
						evento.Glosa = maybeValidacionLimiteCtaPropia.map(u -> _cl.TIPO_VALIDACION_AUTORIZACION.equals(u.TipoComportamiento)).orElse(false) ? "FUERA_LIM_CTA_PROPIA" : "";
						eventos.add(evento);
						
						CrearEventoAutorizacionMasivo.ObjTABLADETALLE detalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
						detalle.IdUnion = idTrabajo;
						detalle.NombreCampo = "IdAsignacion";
						detalle.Valor = idAsignacionStr;
						detalle.TipoDato = "LONG";
						detalle.Clasificacion = "1";
						detalles.add(detalle);
					});
					
					entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = eventos;
					entradaCrearEventoAutorizacionMasivo.P_DETALLE = detalles;
					
					CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo().logica(conn, entradaCrearEventoAutorizacionMasivo);
					List<MensajeSalida> autorizacionErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.stream()
							.map(errorDetalle -> {
								MensajeSalida mensajeSalida = new MensajeSalida();
								mensajeSalida.tipo = MensajeSalida.MENSAJE_ERROR;
								mensajeSalida.id = 0L;
								mensajeSalida.descripcion = errorDetalle.Descripcion;
								mensajeSalida.codigo = errorDetalle.Codigo;
								return mensajeSalida;
							})
							.collect(Collectors.toList());
					out.P_OUT_MENSAJES.addAll(autorizacionErrores);
				}
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(90L,"PROCESANDO","Gestionando notificaciones");
					
				}
			}
			//Notificacion Orden Asignada
			logger.debug("Notificacion Orden Asignada");
			List<NotificacionOrdenAsignada.Orden> ordenesNotificacion = new ArrayList<NotificacionOrdenAsignada.Orden>();
			
			HashMap<String,String> params = new HashMap<String,String>(6);
			
			for (Asignacion_Proceso asig : asignaciones) {
				
				Resultado_Salida resultado = new Resultado_Salida();
				resultado.IdAsignacion = asig.IdAsignacion;
				resultado.IdInterno = asig.IdInterno;
				resultado.EsAPlazo = asig.EsAPlazo;
				out.p_out_Resultado.add(resultado);
				
				if("S".equals(in.p_UsaLogDetallado)) {
					Transaccion_Proceso transaccion = transacciones.get(asig.IdTransaccion);
					Asignacion_Proceso asignacion = asignacionesHash.get(asig.IdAsignacion);
					
					params.put("DocumentoIdentidad", asignacion.DocumentoIdentidad);
					params.put("NombreCompleto", asignacion.NombreCompleto);
					params.put("Folio", transaccion.Folio);
					params.put("IdTipoOperacion", transaccion.IdTipoOperacion);
					params.put("Nemotecnico", transaccion.Nemotecnico);
					params.put("Cantidad", asignacion.Cantidad.toString());
					params.put("Monto", asignacion.Monto.toString());
					
					out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_INFORMACION,"ASIGNACION_MASIVA_PRECIO_PROMEDIO_MENSAJE_SALIDA_BASE",params, conn, in.p_IdEmpresa));
				} else {
					String codigo = "ASIGNACION_CREADA";
					String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo).add("IdAsignacion", asig.IdAsignacion).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
				}
				
				params.clear();
						
				//Notificacion Orden Asignada
				NotificacionOrdenAsignada.Orden ordenNotificar = new NotificacionOrdenAsignada.Orden();
				ordenNotificar.IdOrden = asig.IdOrden;					
				ordenesNotificacion.add(ordenNotificar);
				
				for (CobroAsignacion_Proceso cob : asig.cobros) {
					Cobro_Salida cobAsig = new Cobro_Salida();
					//Utils.copyPojo(cob, cobAsig);
					//cobAsig.IdInterno = cob.IdInterno;
					cobAsig.IdAsignacion = cob.IdAsignacion;
					cobAsig.IdCobroAsignacion = cob.IdCobroAsignacion;
					cobAsig.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
					cobAsig.IdCobro = cob.IdCobro;
					//cobAsig.IdMonedaCobro = cob.IdMonedaCobro;
					cobAsig.ValorCobroPerfil = cob.ValorCobroPerfil;
					cobAsig.FormaCobro = cob.FormaCobro;
					cobAsig.MontoMinimo = cob.MontoMinimo;
					cobAsig.ValorCobroAplicado = cob.ValorCobroAplicado;
					cobAsig.MontoCobroCalculado = cob.MontoCobroCalculado;
					cobAsig.MontoCobroCalculadoLocal = cob.MontoCobroCalculadoLocal;
					cobAsig.MontoCobroCalculadoLiquidacion = cob.MontoCobroCalculadoLiquidacion;
					
					cobAsig.IdMonedaCobro = cob.IdMoneda;
					cobAsig.IdInterno = asig.IdInterno;
					out.p_out_Cobros.add(cobAsig);
				}
			}
			
			if (ordenesNotificacion.size() > 0) {
			NotificacionOrdenAsignada.NotificarOrdenAsignada_Entrada entradaNotificarEstadoOrden = new NotificacionOrdenAsignada.NotificarOrdenAsignada_Entrada();
			entradaNotificarEstadoOrden.p_IdEmpresa = in.p_IdEmpresa;
			entradaNotificarEstadoOrden.p_IdModulo = in.p_IdModulo;
			entradaNotificarEstadoOrden.p_IdUsuario = in.p_IdUsuario;
			entradaNotificarEstadoOrden.p_IdEstadoAsignacion= "ASIGNADA";
			entradaNotificarEstadoOrden.p_Ordenes = ordenesNotificacion;
			
			NotificacionOrdenAsignada notificaOrdenAsig = new NotificacionOrdenAsignada();
			notificaOrdenAsig.logica(conn, entradaNotificarEstadoOrden);
			}
		}catch(Exception e) {
			throw e;
		}finally {
			SQL.exec(conn, SQL_DELETE_ASOCIACIONES);
		}
		{
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Fin crear asignaciones");
				
			}
		}
		logger.info("Termino del servicio CrearAsignaciones, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	private List<CobroAsignacion> limpiarCobrosAsignasionMasiva(List<CobroAsignacion> cobrosAsigPojos) {
		logger.info("INICIO::CrearAsignaciones.limpiarCobrosAsignasionMasiva");
		long inicio = System.currentTimeMillis();
		
		cobrosAsigPojos.stream().forEach(cobro -> {
			cobro.MontoCobroCalculado = BigDecimal.ZERO;
			cobro.MontoCobroCalculadoLocal = BigDecimal.ZERO;
			cobro.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearAsignaciones.limpiarCobrosAsignasionMasiva {}", fin - inicio);
		return cobrosAsigPojos;
	}

	private List<Asignacion> obtenerIdAsignacionMasiva(Connection conn, List<Asignacion> asigsPojos, String p_AplicarComision) throws SQLException {
		logger.info("INICIO::CrearAsignaciones.obtenerIdAsignacionMasiva");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::IdAsignacionMasiva", 1L);
		
		for(Asignacion asig : asigsPojos) {
			asig.IdAsignacionMasiva = secuencia.get(0);
			asig.IdTipoAsignacion = "ASIGNACION_MASIVA_PRECIO_PROMEDIO";
			asig.AplicaComisionAsignacionMasiva = "S".equals(p_AplicarComision) ? "S" : "N";
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearAsignaciones.obtenerIdAsignacionMasiva {}", fin - inicio);
		return asigsPojos;
	}

	private SalidaValidarLimitesCuentaPropia validarLimitesCuentaPropia(EntradaValidarLimitesCuentaPropia entrada) throws Exception {
		logger.trace("INICIO::CrearAsignaciones.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		CrearAsignaciones_Entrada entradaCrearAsignaciones = entrada.EntradaCrearAsignaciones;
		ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF entradaValidarLimiteCuentaPropiaIFRF = new ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF();
		entradaValidarLimiteCuentaPropiaIFRF.p_IdEmpresa = entradaCrearAsignaciones.p_IdEmpresa;
		entradaValidarLimiteCuentaPropiaIFRF.p_IdUsuario = entradaCrearAsignaciones.p_IdUsuario;
		entradaValidarLimiteCuentaPropiaIFRF.p_UsaReglasNegocio = entradaCrearAsignaciones.p_UsaReglasNegocio;
		entradaValidarLimiteCuentaPropiaIFRF.p_IdTipoProceso = _cl.PROCESO_ASIGNAR;
		entradaValidarLimiteCuentaPropiaIFRF.p_Operaciones = entrada.Asignaciones.stream()
				.map(asignacion -> {
					Transaccion_Proceso transaccionProceso = entrada.TransaccionPorId.get(asignacion.IdTransaccion);
					Orden_Proceso ordenProceso = entrada.OrdenPorId.get(asignacion.IdOrden);
					ValidarLimiteCuentaPropiaIFRF.Operacion operacion = new ValidarLimiteCuentaPropiaIFRF.Operacion();
					operacion.IdOperacion = asignacion.IdAsignacion;
					operacion.IdInterno = asignacion.IdInterno;
					operacion.IdInternoGrupo = asignacion.IdAsignacionGrupo;
					operacion.IdCliente = ordenProceso.IdCliente;
					operacion.IdPortafolio = ordenProceso.IdPortafolio;
					operacion.IdInstrumento = ordenProceso.IdInstrumento;
					operacion.IdTipoCanalIngreso = ordenProceso.IdTipoCanalIngreso;
					operacion.IdTipoOperacion = ordenProceso.IdTipoOperacion;
					operacion.IdMonedaOperacion = ordenProceso.IdMonedaOrden;
					operacion.MontoCompraMonedaSistema = asignacion.MontoAsignadoLocal;
					operacion.Cantidad = asignacion.Cantidad;
					operacion.ValorRescate = asignacion.ValorRescate;
					operacion.FechaVencimientoInstrumento = transaccionProceso.FechaVencimiento;
					operacion.Folio = transaccionProceso.Folio;
					operacion.Tasa = transaccionProceso.Tasa;
					operacion.NemoSVS = transaccionProceso.NemoSVS;
					return operacion;
				})
				.collect(Collectors.toList());
		
		ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF salidaValidarLimiteCuentaPropiaIFRF = new ValidarLimiteCuentaPropiaIFRF().logica(entrada.Connection, entradaValidarLimiteCuentaPropiaIFRF);
		Map<Long, Validacion> validacionPorIdInternoAsignacion = salidaValidarLimiteCuentaPropiaIFRF.P_OUT_VALIDACIONES.stream().collect(Collectors.toMap(u -> u.IdInternoOperacion, u -> u));
		Set<Long> gruposRechazados = salidaValidarLimiteCuentaPropiaIFRF.p_out_IdInternoGruposRechazados;
		List<Asignacion_Proceso> asignacionesAceptadas = new ArrayList<>(entrada.Asignaciones.size());
		entrada.Asignaciones.forEach(asignacion -> {
			Optional<Validacion> maybeValidacion = Optional.ofNullable(validacionPorIdInternoAsignacion.get(asignacion.IdInterno));
			if (maybeValidacion.map(u -> _cl.TIPO_VALIDACION_AUTORIZACION.equals(u.TipoComportamiento)).orElse(false)) {
				asignacion.IdEstadoAsignacion = _c.ESTADO_ASIGNACION_PENDIENTE_AUTORIZACION;
			}
			
			boolean grupoAceptado = gruposRechazados.contains(asignacion.IdAsignacionGrupo) == false;
			boolean operacionAceptada = maybeValidacion.map(u -> _cl.TIPO_VALIDACION_RECHAZAR.equals(u.TipoComportamiento) == false).orElse(true); 
			if (grupoAceptado && operacionAceptada) {
				asignacionesAceptadas.add(asignacion);
			}
		});
		
		SalidaValidarLimitesCuentaPropia salidaValidarLimitesCuentaPropia = new SalidaValidarLimitesCuentaPropia();
		salidaValidarLimitesCuentaPropia.Asignaciones = asignacionesAceptadas;
		salidaValidarLimitesCuentaPropia.Validaciones = salidaValidarLimiteCuentaPropiaIFRF.P_OUT_VALIDACIONES;
		salidaValidarLimitesCuentaPropia.Mensajes = salidaValidarLimiteCuentaPropiaIFRF.P_OUT_MENSAJES;
		salidaValidarLimitesCuentaPropia.ValidacionPorIdInternoAsignacion = validacionPorIdInternoAsignacion;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearAsignaciones.validarLimitesCuentaPropia {}", fin - inicio);
		
		return salidaValidarLimitesCuentaPropia;
	}
	
	private BigDecimal calcularModulo(final Double valorRescate, final Double corteMinimo) {
		BigDecimal valorRescateBigDec = new BigDecimal(valorRescate.toString());
		BigDecimal corteMinimoBigDec = new BigDecimal(corteMinimo.toString());
		return valorRescateBigDec.remainder(corteMinimoBigDec);
	}
	
	private String obtenerIdUsoPorIdTipoOperacion(String idTipoOperacion) {
		String idUso = "";
		if ("COMMIL".equalsIgnoreCase(idTipoOperacion))
			idUso = "COMPRA MILA";
		else if ("VENMIL".equalsIgnoreCase(idTipoOperacion))
			idUso = "VENTA MILA";
		else if ("COMETX".equalsIgnoreCase(idTipoOperacion) || "COMCFX".equalsIgnoreCase(idTipoOperacion) || "COMACX".equalsIgnoreCase(idTipoOperacion)) 
			idUso = "COMPRA EXTRANJEROS"; //COMETX;COMCFX;COMACX
		else if ("VENETX".equalsIgnoreCase(idTipoOperacion) || "VENCFX".equalsIgnoreCase(idTipoOperacion) || "VENACX".equalsIgnoreCase(idTipoOperacion))
			idUso = "VENTA EXTRANJEROS"; //VENETX;VENCFX;VENACX
		else
			idUso = "ORDENES";
		return idUso;
	}

	public static class Asignacion_OD {
		public Long IdTransaccion;
		public Long IdOrden;
		public Long IdTransaccionOd;
		public Long IdAsignacion;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
	}
	
	public static class Asignacion_Proceso {
		public Date FechaParidad;
		public Date FechaTipoCambio;
		public BigDecimal Paridad;
		public BigDecimal TipoCambio;
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdOrden;
		public Long IdTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal MontoAsignadoLocal;
		public BigDecimal	MontoAsignadoPlazo = BigDecimal.ZERO;
		public BigDecimal	PrecioPlazo = BigDecimal.ZERO;
		public BigDecimal Tasa = BigDecimal.ZERO;
		public BigDecimal TasaPromedio = BigDecimal.ZERO;
		public String IdEstadoAsignacion;
		public BigDecimal MontoAsignadoLiquidacion;
		public java.sql.Timestamp FechaAsignacion;
		public BigDecimal ValorRescate;
		public String EsAPlazo;
		public Long IdAsignacionRef = 0L;
		public Long FolioCartera;
		public java.sql.Timestamp FechaIngresoRegistro;
		public boolean odMismoPortafolio = false;
		public String CodigoIFRS;
		public BigDecimal MontoAsignadoInteresContado = BigDecimal.ZERO;
		public BigDecimal MontoAsignadoInteresPlazo = BigDecimal.ZERO;
		
		public Long IdAsignacionGrupo = 0L;
		public String DocumentoIdentidad = "";
		public String NombreCompleto = "";

		/* VALIDACIONES */
		public long errores = 0;
		public long advertencias = 0;
		public long autorizaciones = 0;
		public long ok = 0;
		
		public List<CobroAsignacion_Proceso> cobros;
		
		public String GlosaRefactura = "";
		public String FechaOperacionOriginal = "";
		
		public Asignacion_Proceso() {
			this.cobros = new ArrayList<CobroAsignacion_Proceso>();
			this.IdAsignacionRef = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CobroOrden_Proceso {
		public String		PermiteCambiarValor;
		public BigDecimal 	ValorCobroPerfil;
		public BigDecimal 	ValorCobroFinal;
		public Long 		IdCorredoraCorresponsal;
		public Long 		IdOrden;
		public String 		IdCobro;
		public BigDecimal 	ValorCobroAplicado;
		public BigDecimal 	MontoMinimo;
		public String 		IdMoneda;
		public String 		FormaCobro;
		public String		CodigoCobroClasificacion;
		public String		CodigoCobroClasificacionBase;
		public Long			IdGrupoCobro;
		public Long			IdCobroOrden;
		
		//CAMPOS PERU
		public String			_CheckCobroMonedaOperacion;
		public String			_IdMonedaCobroOtraMoneda;
		public BigDecimal		_MontoFijo_MontoCobro;
		public String			_MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal		_MontoFijo_ValorMontoOperacionMenor;
		public String			_MontoFijo_CheckSiNo;
		public BigDecimal		_MontoFijo_ValorMontoSiNo;
		public BigDecimal		_MontoFijoPorUnidad_MontoCobro;
		public String			_MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal		_PorcentualFijo_ValorCobro;
		public String			_PorcentualFijo_CheckSobreMonto;
		public String			_PorcentualFijo_CheckSobreCantidad;
		public String			_PorcentualFijo_CheckMayorEntreDos;
		public String			_PorcentualFijo_CheckMontoMinimo;
		public BigDecimal		_PorcentualFijo_ValorMontoMinimo;
		public String			_PorcentualFijo_CheckMontoMaximo;
		public BigDecimal		_PorcentualFijo_ValorMontoMaximo;
		public String			_PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal		_PorcentualTasaPlazo_ValorCobro;
		public String			_PorcentualRango_IdTipoRangoCobro;
		public String			_Otros_CheckAfectoImpuesto;
		public String			_Otros_CheckPermiteAumentarValor;
		public String			_Otros_CheckPermiteDisminuirValor;
		public String			_Otros_CheckOperacionContado;
		public String			_Otros_CheckOperacionPlazo;
		public String			_Otros_CheckAllIn;
		public List<DefinicionRangoCobro> RangoCobros = new ArrayList<DefinicionRangoCobro>();
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class CobroAsignacion_Proceso {
		public BigDecimal 			ValorCobroAplicado;
		public Long 				IdInternoCobro;
		public Long 				IdAsignacion;
		public Long 				IdCobroAsignacion;
		public String 				IdCobro;
		public BigDecimal 			ValorCobroPerfil;
		public BigDecimal 			ValorCobroFinal;
		public BigDecimal 			MontoMinimo;
		public String 				IdMoneda;
		public String 				FormaCobro;
		public java.sql.Timestamp 	FechaIngresoCobro;
		public String 				CodigoCobroClasificacion;
		public String 				CodigoCobroClasificacionBase;
		public BigDecimal 			MontoAsignacionMonedaCobro;
		public String 				PermiteCambiarValor;
		public BigDecimal 			MontoCobroCalculado;
		public BigDecimal 			MontoCobroCalculadoLocal;
		public BigDecimal 			MontoCobroCalculadoLiquidacion;
		
		//CAMBIOS PERU
		public String			_CheckCobroMonedaOperacion;
		public String			_IdMonedaCobroOtraMoneda;
		public BigDecimal		_MontoFijo_MontoCobro;
		public String			_MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal		_MontoFijo_ValorMontoOperacionMenor;
		public String			_MontoFijo_CheckSiNo;
		public BigDecimal		_MontoFijo_ValorMontoSiNo;
		public String 			_MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal		_MontoFijoPorUnidad_MontoCobro;
		public BigDecimal		_PorcentualFijo_ValorCobro;
		public String			_PorcentualFijo_CheckSobreMonto;
		public String			_PorcentualFijo_CheckSobreCantidad;
		public String			_PorcentualFijo_CheckMayorEntreDos;
		public String			_PorcentualFijo_CheckMenorEntreDos;
		public String			_PorcentualFijo_CheckMontoMinimo;
		public BigDecimal		_PorcentualFijo_ValorMontoMinimo;
		public String			_PorcentualFijo_CheckMontoMaximo;
		public BigDecimal		_PorcentualFijo_ValorMontoMaximo;
		public String			_PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal		_PorcentualTasaPlazo_ValorCobro;
		public String			_Otros_CheckAfectoImpuesto;
		public String			_Otros_CheckPermiteAumentarValor;
		public String			_Otros_CheckPermiteDisminuirValor;
		public String			_Otros_CheckOperacionContado;
		public String			_Otros_CheckOperacionPlazo;
		public String			_Otros_CheckAllIn;
		public List<DefinicionRangoCobro> _RangoCobros = new ArrayList<DefinicionRangoCobro>();
		public String			_Otros_CheckInteresCompuesto;
		public String			_Otros_CheckInteresSimple;
		public String			_PorcentualRango_IdMonedaRangoMonto;
		public String			_PorcentualRango_IdTipoRangoCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Orden_Proceso {
		public Long 		IdOrden;
		public String 		IdTipoOperacion;
		public String 		IdTipoPrecio;
		public String 		EsVentaCorta;
		public BigDecimal 	PrecioOrden;
		public Long Tamano;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal 	CantidadDisponible;
		public BigDecimal 	MontoDisponible;
		public BigDecimal 	ValorRescateDisponible;
		public String 		IdTipoCanalIngreso;
		public String 		IdEstadoOrden;
		public String 		IdMonedaOrden;
		public String 		IdMonedaLiquidacionOrden;
		public BigDecimal 	Cantidad;
		public Long 		IdGrupoCobro;
		public Long 		IdInstrumento;
		public String 		IdCondicionDeLiquidacion;
		public String 		IdBolsa;
		public java.sql.Timestamp FechaOperacion;
		public String 		IdClasificacionTipoInstrumento;
		public BigDecimal 	TipoCambio;
		public Long 		IdLibroOrdenes;
		public BigDecimal 	ValorRescate;
		public String 		AccionOrden;
		public Long 		IdInstrumentoSubyacente;
		public String 		IdTipoInstrumento;
		public Long			PlazoSimultanea;
		public Long			PlazoVentaCorta;
		public Long			PlazoIF;
		public Long			PlazoReporte;
		public String 		EsFungible;
		public Long			BaseTasa;
		public BigDecimal	MontoInteresContado;
		public BigDecimal	MontoInteresPlazo;
		public BigDecimal	PorcentajeCobros;
		public BigDecimal	TasaReporte;
		public BigDecimal	TasaPromedioReporte;
		public String		TipoInteres;
		public Long			IdCorredorCorresponsal;
		public String		EsCuentaPropia;
		public BigDecimal	Tasa;
		public String		PermiteAsignarOEODMismoRut;
		
		public List<CobroOrden_Proceso> cobrosOrden;
		
		//para calculo de sobreasignacion
		public BigDecimal CantidadTotalAsignada  = BigDecimal.ZERO;
		public BigDecimal MontoTotalAsignado  = BigDecimal.ZERO;
		public BigDecimal ValorRescateTotalAsignado  = BigDecimal.ZERO;
		
		public Orden_Proceso() {
			this.cobrosOrden = new ArrayList<CobroOrden_Proceso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	public static class Transaccion_Proceso {
		public String Folio;
		public Long IdTransaccion;
		public String IdTipoOperacion;
		public Long IdInstrumento;
		public Long IdLibroOrdenes;
		public BigDecimal CantidadDisponible;
		public BigDecimal MontoDisponible;
		public BigDecimal ValorRescateDisponible;
		public String IdCondicionDeLiquidacion;
		public String IdBolsa;
		public java.sql.Timestamp FechaOperacion;
		public java.sql.Date FechaVencimiento;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public String EsVentaCorta;
		public String IdEstado;
		public String EsFungible;
		public BigDecimal CorteMinimo;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal ValorRescate;
		public Long IdInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumento;
		public Long Tamano;
		public Long IdTransaccionPlazo;
		public String EsAPlazo;
		public String IdMoneda;
		public String AccionOrden;
		public String IdTipoInstrumento;
		public Long IdCorredoraCorresponsal;
		public String Reservado;
		public String Bloqueado;
		public Long IdUsuarioReserva;
		public Long IdTransaccionOd;
		public BigDecimal Intereses;
		public String NemoSVS;
		public String Nemotecnico;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		
		//para calculo de sobreasignacion
		public BigDecimal CantidadTotalAsignada = BigDecimal.ZERO;
		public BigDecimal MontoTotalAsignado = BigDecimal.ZERO;
		public BigDecimal ValorRescateTotalAsignado = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asociacion {
		public Long IdOrdenAsociada;
		public Long IdOrdenN;
		public Long IdOrdenM;
		public Date FechaIngresoRegistroCorta;
		public Date FechaIngresoRegistro;
		public String ModalidadCalculo;
		public BigDecimal Monto;
		public BigDecimal Cantidad;
		public Long IdUsuario;
		public Long IdOrdenMOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	/*
	public static class Orden_Asociacion {
		public Long IdOrden;//corresponde al reportado
		public List<Asociacion> Asociaciones;
				
		public Orden_Asociacion() {
			Asociaciones = new ArrayList<Asociacion>() ;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}*/
	
	private Asignacion_Proceso copyPojoAsignacionEntradaAsignacionProceso(Asignacion_Entrada asig) {
		Asignacion_Proceso asigProceso = new Asignacion_Proceso();
		//asigProceso.FechaParidad = asig.FechaParidad;
		//asigProceso.FechaTipoCambio = asig.FechaTipoCambio;
		//asigProceso.Paridad = asig.Paridad;
		//asigProceso.TipoCambio = asig.TipoCambio;
		asigProceso.IdInterno = asig.IdInterno;
		//asigProceso.IdAsignacion = asig.IdAsignacion;
		asigProceso.IdOrden = asig.IdOrden;
		asigProceso.IdTransaccion = asig.IdTransaccion;
		asigProceso.Cantidad = asig.Cantidad;
		//asigProceso.Monto = asig.Monto;
		//asigProceso.MontoAsignadoLocal = asig.MontoAsignadoLocal;
		//asigProceso.MontoAsignadoPlazo = asig.MontoAsignadoPlazo;
		//asigProceso.PrecioPlazo = asig.PrecioPlazo;
		//asigProceso.Tasa = asig.Tasa;
		//asigProceso.TasaPromedio = asig.TasaPromedio;
		//asigProceso.IdEstadoAsignacion = asig.IdEstadoAsignacion;
		//asigProceso.MontoAsignadoLiquidacion = asig.MontoAsignadoLiquidacion;
		//asigProceso.FechaAsignacion = asig.FechaAsignacion;
		asigProceso.ValorRescate = asig.ValorRescate;
		//asigProceso.EsAPlazo = asig.EsAPlazo;
		//asigProceso.IdAsignacionRef = asig.IdAsignacionRef;
		asigProceso.FolioCartera = asig.FolioCartera;
		//asigProceso.FechaIngresoRegistro = asig.FechaIngresoRegistro;
		//asigProceso.odMismoPortafolio = asig.odMismoPortafolio;
		asigProceso.CodigoIFRS = asig.CodigoIFRS;
		//asigProceso.MontoAsignadoInteresContado = asig.MontoAsignadoInteresContado;
		//asigProceso.MontoAsignadoInteresPlazo = asig.MontoAsignadoInteresPlazo;
		//asigProceso.IdAsignacionGrupo = asig.IdAsignacionGrupo;
		//asigProceso.errores = asig.errores;
		//asigProceso.advertencias = asig.advertencias;
		//asigProceso.autorizaciones = asig.autorizaciones;
		//asigProceso.ok = asig.ok;
		//asigProceso.cobros = new ArrayList<CobroAsignacion_Proceso>(asig.cobros);
		asigProceso.DocumentoIdentidad = asig.DocumentoIdentidad;
		asigProceso.NombreCompleto = asig.NombreCompleto;
		asigProceso.Monto = asig.Monto;
		asigProceso.GlosaRefactura = asig.GlosaRefactura;
		asigProceso.FechaOperacionOriginal = asig.FechaOperacionOriginal;
		return asigProceso;
	}
	
	private Asignacion_Proceso copyPojoAsignacionProcesoAsignacionProceso(Asignacion_Proceso asig) {
		Asignacion_Proceso asig_plazo = new Asignacion_Proceso();
		asig_plazo.FechaParidad = asig.FechaParidad;
		asig_plazo.FechaTipoCambio = asig.FechaTipoCambio;
		asig_plazo.Paridad = asig.Paridad;
		asig_plazo.TipoCambio = asig.TipoCambio;
		asig_plazo.IdInterno = asig.IdInterno;
		asig_plazo.IdAsignacion = asig.IdAsignacion;
		asig_plazo.IdOrden = asig.IdOrden;
		asig_plazo.IdTransaccion = asig.IdTransaccion;
		asig_plazo.Cantidad = asig.Cantidad;
		asig_plazo.Monto = asig.Monto;
		asig_plazo.MontoAsignadoLocal = asig.MontoAsignadoLocal;
		asig_plazo.MontoAsignadoPlazo = asig.MontoAsignadoPlazo;
		asig_plazo.PrecioPlazo = asig.PrecioPlazo;
		asig_plazo.Tasa = asig.Tasa;
		asig_plazo.TasaPromedio = asig.TasaPromedio;
		asig_plazo.IdEstadoAsignacion = asig.IdEstadoAsignacion;
		asig_plazo.MontoAsignadoLiquidacion = asig.MontoAsignadoLiquidacion;
		asig_plazo.FechaAsignacion = asig.FechaAsignacion;
		asig_plazo.ValorRescate = asig.ValorRescate;
		asig_plazo.EsAPlazo = asig.EsAPlazo;
		asig_plazo.IdAsignacionRef = asig.IdAsignacionRef;
		asig_plazo.FolioCartera = asig.FolioCartera;
		asig_plazo.FechaIngresoRegistro = asig.FechaIngresoRegistro;
		asig_plazo.odMismoPortafolio = asig.odMismoPortafolio;
		asig_plazo.CodigoIFRS = asig.CodigoIFRS;
		asig_plazo.MontoAsignadoInteresContado = asig.MontoAsignadoInteresContado;
		asig_plazo.MontoAsignadoInteresPlazo = asig.MontoAsignadoInteresPlazo;
		asig_plazo.IdAsignacionGrupo = asig.IdAsignacionGrupo;
		asig_plazo.errores = asig.errores;
		asig_plazo.advertencias = asig.advertencias;
		asig_plazo.autorizaciones = asig.autorizaciones;
		asig_plazo.ok = asig.ok;
		asig_plazo.cobros = new ArrayList<CobroAsignacion_Proceso>(asig.cobros);
		return asig_plazo;
	}
	
	private CobroAsignacion_Proceso copyPojoCobroAsignacionProceso(CobroOrden_Proceso cobOrden) {
		CobroAsignacion_Proceso cobAsig = new CobroAsignacion_Proceso();
		cobAsig.ValorCobroAplicado = cobOrden.ValorCobroAplicado;
		//cobAsig.IdInternoCobro = cobOrden.IdInternoCobro;
		//cobAsig.IdAsignacion = cobOrden.IdAsignacion;
		//cobAsig.IdCobroAsignacion = cobOrden.IdCobroAsignacion;
		cobAsig.IdCobro = cobOrden.IdCobro;
		cobAsig.ValorCobroPerfil = cobOrden.ValorCobroPerfil;
		cobAsig.ValorCobroFinal = cobOrden.ValorCobroFinal;
		cobAsig.MontoMinimo = cobOrden.MontoMinimo;
		cobAsig.IdMoneda = cobOrden.IdMoneda;
		cobAsig.FormaCobro = cobOrden.FormaCobro;
		//cobAsig.FechaIngresoCobro = cobOrden.FechaIngresoCobro;
		cobAsig.CodigoCobroClasificacion = cobOrden.CodigoCobroClasificacion;
		cobAsig.CodigoCobroClasificacionBase = cobOrden.CodigoCobroClasificacionBase;
		//cobAsig.MontoAsignacionMonedaCobro = cobOrden.MontoAsignacionMonedaCobro;
		cobAsig.PermiteCambiarValor = cobOrden.PermiteCambiarValor;
		//cobAsig.MontoCobroCalculado = cobOrden.MontoCobroCalculado;
		//cobAsig.MontoCobroCalculadoLocal = cobOrden.MontoCobroCalculadoLocal;
		//cobAsig.MontoCobroCalculadoLiquidacion = cobOrden.MontoCobroCalculadoLiquidacion;
		cobAsig._CheckCobroMonedaOperacion = cobOrden._CheckCobroMonedaOperacion;
		cobAsig._IdMonedaCobroOtraMoneda = cobOrden._IdMonedaCobroOtraMoneda;
		cobAsig._MontoFijo_MontoCobro = cobOrden._MontoFijo_MontoCobro;
		cobAsig._MontoFijo_CheckMontoOperacionMenor = cobOrden._MontoFijo_CheckMontoOperacionMenor;
		cobAsig._MontoFijo_ValorMontoOperacionMenor = cobOrden._MontoFijo_ValorMontoOperacionMenor;
		cobAsig._MontoFijo_CheckSiNo = cobOrden._MontoFijo_CheckSiNo;
		cobAsig._MontoFijo_ValorMontoSiNo = cobOrden._MontoFijo_ValorMontoSiNo;
		cobAsig._MontoFijoPorUnidad_AllInCheckValor = cobOrden._MontoFijoPorUnidad_AllInCheckValor;
		cobAsig._MontoFijoPorUnidad_MontoCobro = cobOrden._MontoFijoPorUnidad_MontoCobro;
		cobAsig._PorcentualFijo_ValorCobro = cobOrden._PorcentualFijo_ValorCobro;
		cobAsig._PorcentualFijo_CheckSobreMonto = cobOrden._PorcentualFijo_CheckSobreMonto;
		cobAsig._PorcentualFijo_CheckSobreCantidad = cobOrden._PorcentualFijo_CheckSobreCantidad;
		cobAsig._PorcentualFijo_CheckMayorEntreDos = cobOrden._PorcentualFijo_CheckMayorEntreDos;
		//cobAsig._PorcentualFijo_CheckMenorEntreDos = cobOrden._PorcentualFijo_CheckMenorEntreDos;
		cobAsig._PorcentualFijo_CheckMontoMinimo = cobOrden._PorcentualFijo_CheckMontoMinimo;
		cobAsig._PorcentualFijo_ValorMontoMinimo = cobOrden._PorcentualFijo_ValorMontoMinimo;
		cobAsig._PorcentualFijo_CheckMontoMaximo = cobOrden._PorcentualFijo_CheckMontoMaximo;
		cobAsig._PorcentualFijo_ValorMontoMaximo = cobOrden._PorcentualFijo_ValorMontoMaximo;
		cobAsig._PorcentualFijo_IdMonedaMinimoMaximo = cobOrden._PorcentualFijo_IdMonedaMinimoMaximo;
		cobAsig._PorcentualTasaPlazo_ValorCobro = cobOrden._PorcentualTasaPlazo_ValorCobro;
		cobAsig._Otros_CheckAfectoImpuesto = cobOrden._Otros_CheckAfectoImpuesto;
		cobAsig._Otros_CheckPermiteAumentarValor = cobOrden._Otros_CheckPermiteAumentarValor;
		cobAsig._Otros_CheckPermiteDisminuirValor = cobOrden._Otros_CheckPermiteDisminuirValor;
		cobAsig._Otros_CheckOperacionContado = cobOrden._Otros_CheckOperacionContado;
		cobAsig._Otros_CheckOperacionPlazo = cobOrden._Otros_CheckOperacionPlazo;
		cobAsig._Otros_CheckAllIn = cobOrden._Otros_CheckAllIn;
		//cobAsig._RangoCobros = cobOrden._RangoCobros;
		//cobAsig._Otros_CheckInteresCompuesto = cobOrden._Otros_CheckInteresCompuesto;
		//cobAsig._Otros_CheckInteresSimple = cobOrden._Otros_CheckInteresSimple;
		//cobAsig._PorcentualRango_IdMonedaRangoMonto = cobOrden._PorcentualRango_IdMonedaRangoMonto;
		cobAsig._PorcentualRango_IdTipoRangoCobro = cobOrden._PorcentualRango_IdTipoRangoCobro;
		return cobAsig;
	}
	
	private Asignacion copyPojoAsignacion(Asignacion_Proceso asig) {
		Asignacion asigPojo = new Asignacion();
		//asigPojo.CantidadAsignada = asig.CantidadAsignada;
		//asigPojo.CantidadAsignadaRefact = asig.CantidadAsignadaRefact;
		asigPojo.CodigoIFRS = asig.CodigoIFRS;
		asigPojo.EsAPlazo = asig.EsAPlazo;
		asigPojo.FechaAsignacion = asig.FechaAsignacion;
		asigPojo.FechaIngresoRegistro = asig.FechaIngresoRegistro;
		asigPojo.FechaParidad = asig.FechaParidad;
		asigPojo.FechaTipoCambio = asig.FechaTipoCambio;
		asigPojo.FolioCartera = asig.FolioCartera;
		asigPojo.IdAsignacion = asig.IdAsignacion;
		asigPojo.IdAsignacionRef = asig.IdAsignacionRef;
		asigPojo.IdAsignacionGrupo = asig.IdAsignacionGrupo;
		asigPojo.IdEstadoAsignacion = asig.IdEstadoAsignacion;
		asigPojo.IdTransaccion = asig.IdTransaccion;
		asigPojo.IdOrden = asig.IdOrden;
		//asigPojo.IdLibroOrdenes = asig.IdLibroOrdenes;
		//asigPojo.IdMovimientoComprobante = asig.IdMovimientoComprobante;
		//asigPojo.IdComprobanteRefact = asig.IdComprobanteRefact;
		//asigPojo.MontoAsignado = asig.MontoAsignado;
		asigPojo.MontoAsignadoLocal = asig.MontoAsignadoLocal;
		asigPojo.MontoAsignadoLiquidacion = asig.MontoAsignadoLiquidacion;
		//asigPojo.MontoAsignadoInteres = asig.MontoAsignadoInteres;
		asigPojo.Paridad = asig.Paridad;
		asigPojo.TipoCambio = asig.TipoCambio;
		//asigPojo.ValorRescateAsignado = asig.ValorRescateAsignado;
		//asigPojo.ValorRescateAsignadoRefact = asig.ValorRescateAsignadoRefact;
		return asigPojo;
	}
	
	private CobroAsignacion copyPojoCobroAsignacion(CobroAsignacion_Proceso cob) {
		CobroAsignacion cobAsig = new CobroAsignacion();
		cobAsig.IdCobroAsignacion = cob.IdCobroAsignacion;
		cobAsig.IdAsignacion = cob.IdAsignacion;
		cobAsig.IdCobro = cob.IdCobro;
		cobAsig.FechaIngresoCobro = cob.FechaIngresoCobro;
		//cobAsig.FechaIngresoRegistro = cob.FechaIngresoRegistro;
		cobAsig.FormaCobro = cob.FormaCobro;
		cobAsig.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
		cobAsig.CodigoCobroClasificacionBase = cob.CodigoCobroClasificacionBase;
		//cobAsig.ValorCobro = cob.ValorCobro;
		cobAsig.ValorCobroFinal = cob.ValorCobroFinal;
		cobAsig.ValorCobroAplicado = cob.ValorCobroAplicado;
		cobAsig.MontoCobroCalculado = cob.MontoCobroCalculado;
		cobAsig.MontoCobroCalculadoLocal = cob.MontoCobroCalculadoLocal;
		cobAsig.MontoCobroCalculadoLiquidacion = cob.MontoCobroCalculadoLiquidacion;
		cobAsig.IdMoneda = cob.IdMoneda;
		return cobAsig;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String a = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_Asignaciones\":[{\"IdInterno\":41869,\"IdOrden\":41869,\"IdTransaccion\":98098222,\"Cantidad\":10,\"ValorRescate\":0,\"FolioCartera\":0}]}";
		
		String b = "{\r\n"
				+ "    \"p_IdEmpresa\": 1,\r\n"
				+ "    \"p_IdUsuario\": 97,\r\n"
				+ "    \"p_IdModulo\": \"ORD\",\r\n"
				+ "    \"p_Asignaciones\": [\r\n"
				+ "        {\r\n"
				+ "            \"IdInterno\": 14050,\r\n"
				+ "            \"IdOrden\": 1271584,\r\n"
				+ "            \"IdTransaccion\": 2883604,\r\n"
				+ "            \"Cantidad\": 120,\r\n"
				+ "            \"CodigoIFRS\": \"\",\r\n"
				+ "            \"ValorRescate\": 0,\r\n"
				+ "            \"FolioCartera\": 0\r\n"
				+ "        }\r\n"
				+ "    ]\r\n"
				+ "}";
		CrearAsignaciones_Entrada ent = gson.fromJson(b, CrearAsignaciones_Entrada.class);
		//CrearAsignaciones_Entrada ent =//new CrearAsignaciones_Entrada(); 
		
		/*ent.p_simularOperacion = "S";
		Asignacion_Entrada a1 = new Asignacion_Entrada();
		a1.IdInterno = 0L;
		a1.IdOrden = 314025L;
		a1.IdTransaccion = 70286652L;
		a1.Cantidad = new BigDecimal(1000);
		a1.ValorRescate =  new BigDecimal(0);
		a1.FolioCartera = 0L;
		ent.p_Asignaciones.add(a1);
		ent.p_IdEmpresa = 1313L;
		ent.p_IdModulo = "ORD";
		ent.p_IdUsuario = 97L;*/
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
		CrearAsignaciones asig = new CrearAsignaciones();
		CrearAsignaciones_Salida aaa = asig.logica(con, ent);
		System.out.println(aaa.toString(true));
		con.rollback();
		//con.commit();
		}
	}


}