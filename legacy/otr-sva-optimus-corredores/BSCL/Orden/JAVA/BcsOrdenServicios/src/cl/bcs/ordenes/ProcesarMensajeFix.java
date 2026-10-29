package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CambiarEstadoOrdenes.CambiarEstadoOrdenes_Entrada;
import cl.bcs.ordenes.CambiarEstadoOrdenes.CambiarEstadoOrdenes_Salida;
import cl.bcs.ordenes.CambiarEstadoOrdenes.Orden_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.Asignacion_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.CrearAsignaciones_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.CrearAsignaciones_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.Corredora;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.CrearTransaccionesMarketDataBEC_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.CrearTransaccionesMarketDataBEC_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.TransaccionBECMarketData_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.ordenes.pojo.ConfiguracionCanalDistribucion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EventoBitacora;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.ordenes.pojo.RespuestaFix;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ProcesarMensajeFix implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ProcesarMensajeFix.class);
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	private final String SQL_SELECT_CORREDORAS_BEC = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajeFix_SelectCorredoras.sql");
	private final String SQL_INSERT_BITACORA = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFix_InsertBitacora.sql");
	private final String SQL_SELECT_COUNT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectCountAsignaciones.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectTransacciones.sql");
	private final String SQL_SELECT_ORDENES= loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectOrdenes.sql");
	private final String SQL_SELECT_CONFIGURACION_CANAL_DISTRIBUCION = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectConfiguracionCanalDistribucion.sql");
	private final String SQL_SELECT_MENSAJES_PENDIENTES = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectMensajesPendientes.sql");
	
	public static class InformacionTransaccion extends Transaccion{
		public String AccionOrden;
	}
	
	public static class OrdenAsignacion {
		public Long IdOrden;
		public Long NumeroAsignaciones;
	}
	
	public static class RespuestaFix_Entrada extends RespuestaFix {
		public Long IdInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RespuestaFix_Proceso extends RespuestaFix_Entrada {
		public Long IdAsignacion;
		public Orden orden;
		public Transaccion transaccion;
		public Long IdMovimientoComprobante;
		public String GeneraMovimientoComprobante = "N";
		public String GeneraComprobante = "N";
	}
	
	public static class RespuestaFix_Pendiente extends RespuestaFix {
		public Long IdInterno;
		public String TipoMensaje;
		public String IdMensaje1;
		public String IdMensaje2;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
		
	public ProcesarMensajeFix_Salida logica(Connection conn,ProcesarMensajeFix_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ProcesarMensajeFix_Salida salida = new ProcesarMensajeFix_Salida();
		Timestamp ts = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		List<EventoBitacora> bitacora = new ArrayList<EventoBitacora>();
		
		List<RespuestaFix_Proceso> ordenesEnNegociacion = new ArrayList<RespuestaFix_Proceso>();
		List<RespuestaFix_Proceso> ordenesEnviadasBEC = new ArrayList<RespuestaFix_Proceso>();
		List<RespuestaFix_Proceso> ordenesPorRechazar = new ArrayList<RespuestaFix_Proceso>();
		List<RespuestaFix_Proceso> ordenesPorAnular = new ArrayList<RespuestaFix_Proceso>();
		List<RespuestaFix_Proceso> mensajesError = new ArrayList<RespuestaFix_Proceso>();
		List<RespuestaFix_Proceso> calces = new ArrayList<RespuestaFix_Proceso>();
		List<RespuestaFix_Proceso> mensajesNews = new ArrayList<RespuestaFix_Proceso>();
		
		for (RespuestaFix_Entrada _fix : entrada.p_MensajesFix) {
			
			//si hay mensaje pendiente previo de la misma orden, no proceso este mensaje
			if (_fix.SEC_ORDEN != null && _fix.SEC_ORDEN.compareTo(BigDecimal.ZERO) >=1) {
				String queryMensajesPendientes = SQL_SELECT_MENSAJES_PENDIENTES.replace("<NOMBRE_TABLA>", entrada.p_Tabla);
				HashMap<String,Object> params = new HashMap<String, Object>();
				params.put("SEC_ORDEN",_fix.SEC_ORDEN);
				params.put("FECHA",_fix.FECHA);
				params.put("NRO_ENTRADA",_fix.NRO_ENTRADA);
				List<RespuestaFix_Pendiente> pendientes = _sql.query(conn, queryMensajesPendientes, params , RespuestaFix_Pendiente.class);
				if (pendientes.size() > 0) {
					
					logger.debug("Existen mensajes pendientes para la orden:" + _fix.SEC_ORDEN);
					
					String codigo = "MENSAJES_PENDIENTES";
					String desc = "Mensajes pendientes para orden " + _fix.SEC_ORDEN + " " + _fix.FECHA;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(_fix.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,codigo,desc));
					
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = _fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
					r.IdReferencia = _fix.IdInterno;
					r.MensajeReferencia = desc;
					salida.P_OUT_RESULTADO.add(r);
					continue;
				}
				
			}
			
			RespuestaFix_Proceso fix = new RespuestaFix_Proceso();
			Utils.copyPojo(_fix, fix);
			
			if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_EN_NEGOCIACION)) {
				ordenesEnNegociacion.add(fix);
			}
			else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_ORDEN_RECHAZADA)) {
				ordenesPorRechazar.add(fix);
			}
			else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_ROUTED_XBCL)) { 
				ordenesEnviadasBEC.add(fix);
			}
			else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_ORDEN_ANULADA)) {
				ordenesPorAnular.add(fix);
			}
			else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_CALCE_COMPLETO) || fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_CALCE_PARCIAL)) {
				calces.add(fix);
			}
			else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_NEWS)) {
				mensajesNews.add(fix);
			}
			else {
				
			}
		}
		
		//MENSAJES "ROUTED TO XBCL
		{
			for (RespuestaFix_Entrada fix : ordenesEnviadasBEC) {
				EventoBitacora ev = new EventoBitacora();
				ev.FechaEventoBitacora = ts;
				ev.IdAsignacion = 0L;
				ev.IdOrden = fix.SEC_ORDEN.longValue();
				ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL;
				ev.IdTransaccion = 0L;
				ev.MensajeEventoBitacora = fix.MENSAJE;
				ev.NombreEntidad = "ORDEN";
				bitacora.add(ev);
				
				Resultado_Salida r = new Resultado_Salida();
				r.IdInterno = fix.IdInterno;
				r.Tipo = MensajeSalida.MENSAJE_OK;
				r.IdReferencia = new Long(fix.SEC_ORDEN.longValue());
				r.MensajeReferencia = _c.TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL;
				salida.P_OUT_RESULTADO.add(r);
			}
		}
		
		//MENSAJES "EN NEGOCIACION"
		{
			if (ordenesEnNegociacion.size() > 0) {
				CambiarEstadoOrdenes_Entrada entradaCambiarEstadoOrdenes = new CambiarEstadoOrdenes_Entrada();
				entradaCambiarEstadoOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCambiarEstadoOrdenes.p_IdUsuario = entrada.p_IdUsuario;
				
				List<Long> _hashOrdenes = new ArrayList<>();
				for (RespuestaFix_Entrada fix : ordenesEnNegociacion) {
					if (_hashOrdenes.contains(new Long(fix.SEC_ORDEN.longValue()))) {
						continue;
					}
					Orden_Entrada o1 = new Orden_Entrada();
					o1.IdOrden = new Long(fix.SEC_ORDEN.longValue());
					o1.IdNuevoEstado = _c.ESTADO_ORDEN_OFERTADA;
					entradaCambiarEstadoOrdenes.p_Ordenes.add(o1);
					_hashOrdenes.add(new Long(fix.SEC_ORDEN.longValue()));
				}
				
				CambiarEstadoOrdenes serviceCambiarEstadoOrdenes = new CambiarEstadoOrdenes();
				CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = serviceCambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
				salida.P_OUT_MENSAJES.addAll(salidaCambiarEstadoOrdenes.P_OUT_MENSAJES);
				
				for (MensajeSalida msj : salidaCambiarEstadoOrdenes.P_OUT_MENSAJES) {
					for (RespuestaFix_Entrada fix : ordenesEnNegociacion) {
						if (new Long(fix.SEC_ORDEN.longValue()).equals(msj.id) && msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
							EventoBitacora ev = new EventoBitacora();
							ev.FechaEventoBitacora = ts;
							ev.IdAsignacion = 0L;
							ev.IdOrden = msj.id;
							ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_EN_NEGOCIACION;
							ev.IdTransaccion = 0L;
							ev.MensajeEventoBitacora = fix.MENSAJE;
							ev.NombreEntidad = "ORDEN";
							bitacora.add(ev);
							
							Resultado_Salida r = new Resultado_Salida();
							r.IdInterno = fix.IdInterno;
							r.Tipo = MensajeSalida.MENSAJE_OK;
							r.IdReferencia = new Long(fix.SEC_ORDEN.longValue());
							r.MensajeReferencia = msj.descripcion + " " + _c.TIPO_EVENTO_BITACORA_EN_NEGOCIACION;
							salida.P_OUT_RESULTADO.add(r);
						}
					}
					
				}
			}
		}
		
		//MENSAJES "ORDEN RECHAZADA"
		{
			if (ordenesPorRechazar.size() > 0) {
				CambiarEstadoOrdenes_Entrada entradaCambiarEstadoOrdenes = new CambiarEstadoOrdenes_Entrada();
				entradaCambiarEstadoOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCambiarEstadoOrdenes.p_IdUsuario = entrada.p_IdUsuario;
				
				List<Long> _hashOrdenes = new ArrayList<>();
				for (RespuestaFix_Entrada fix : ordenesPorRechazar) {
					if (_hashOrdenes.contains(new Long(fix.SEC_ORDEN.longValue()))) {
						continue;
					}
					Orden_Entrada o1 = new Orden_Entrada();
					o1.IdOrden = new Long(fix.SEC_ORDEN.longValue());
					o1.IdNuevoEstado = _c.ESTADO_ORDEN_RECHAZADA;
					entradaCambiarEstadoOrdenes.p_Ordenes.add(o1);
					_hashOrdenes.add(new Long(fix.SEC_ORDEN.longValue()));
				}
				
				CambiarEstadoOrdenes serviceCambiarEstadoOrdenes = new CambiarEstadoOrdenes();
				CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = serviceCambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
				salida.P_OUT_MENSAJES.addAll(salidaCambiarEstadoOrdenes.P_OUT_MENSAJES);
				
				for (MensajeSalida msj : salidaCambiarEstadoOrdenes.P_OUT_MENSAJES) {
					for (RespuestaFix_Entrada fix : ordenesPorRechazar) {
						if (new Long(fix.SEC_ORDEN.longValue()).equals(msj.id) && msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
							EventoBitacora ev = new EventoBitacora();
							ev.FechaEventoBitacora = ts;
							ev.IdAsignacion = 0L;
							ev.IdOrden = msj.id;
							ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA;
							ev.IdTransaccion = 0L;
							ev.MensajeEventoBitacora = fix.MENSAJE;
							ev.NombreEntidad = "ORDEN";
							bitacora.add(ev);
							
							Resultado_Salida r = new Resultado_Salida();
							r.IdInterno = fix.IdInterno;
							r.Tipo = MensajeSalida.MENSAJE_OK;
							r.IdReferencia = new Long(fix.SEC_ORDEN.longValue());
							r.MensajeReferencia = msj.descripcion + " " + _c.TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA;
							salida.P_OUT_RESULTADO.add(r);
						}
					}
					
				}
			}
		}
		
		//MENSAJES "ORDEN ANULADA"
		{
			if (ordenesPorAnular.size() > 0) {
				
				List<Long> idsOrden = new ArrayList<>();
				for (RespuestaFix_Entrada fix : ordenesPorAnular) {
					if (idsOrden.contains(new Long(fix.SEC_ORDEN.longValue()))) {
						continue;
					}
					
					idsOrden.add(new Long(fix.SEC_ORDEN.longValue()));
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("idsOrden", idsOrden);
				List<OrdenAsignacion> _countAsignaciones = _sql.query(conn, SQL_SELECT_COUNT_ASIGNACIONES, params, OrdenAsignacion.class);
				HashMap<Long, Object> hashCountAsignaciones = Utils.List2Hash(_countAsignaciones, "IdOrden");
				CambiarEstadoOrdenes_Entrada entradaCambiarEstadoOrdenes = new CambiarEstadoOrdenes_Entrada();
				entradaCambiarEstadoOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCambiarEstadoOrdenes.p_IdUsuario = entrada.p_IdUsuario;
				
				List<Long> _hashOrdenes = new ArrayList<>();
				for (RespuestaFix_Entrada fix : ordenesPorAnular) {
					
					//CUANDO LA ORDEN NO EXISTE
					if (!hashCountAsignaciones.containsKey(new Long(fix.SEC_ORDEN.longValue()))) {
						RespuestaFix_Proceso fixP = new RespuestaFix_Proceso();
						Utils.copyPojo(fix, fixP);
						mensajesError.add(fixP);
						continue;
					}
					
					if (_hashOrdenes.contains(new Long(fix.SEC_ORDEN.longValue()))) {
						continue;
					}
					
					OrdenAsignacion oa = (OrdenAsignacion) hashCountAsignaciones.get(new Long(fix.SEC_ORDEN.longValue()));
					Orden_Entrada o1 = new Orden_Entrada();
					o1.IdOrden = new Long(fix.SEC_ORDEN.longValue());
					
					if (oa.NumeroAsignaciones > 0) {
						o1.IdNuevoEstado = _c.ESTADO_ORDEN_ANULADA_PARCIAL;
					}
					else {
						o1.IdNuevoEstado = _c.ESTADO_ORDEN_ANULADA;
					}
					
					entradaCambiarEstadoOrdenes.p_Ordenes.add(o1);
					_hashOrdenes.add(new Long(fix.SEC_ORDEN.longValue()));
				}
				
				if (entradaCambiarEstadoOrdenes.p_Ordenes.size() >0) {
					CambiarEstadoOrdenes serviceCambiarEstadoOrdenes = new CambiarEstadoOrdenes();
					CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = serviceCambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
					salida.P_OUT_MENSAJES.addAll(salidaCambiarEstadoOrdenes.P_OUT_MENSAJES);
					
					for (MensajeSalida msj : salidaCambiarEstadoOrdenes.P_OUT_MENSAJES) {
						for (RespuestaFix_Entrada fix : ordenesPorAnular) {
							if (new Long(fix.SEC_ORDEN.longValue()).equals(msj.id) && msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
								
								OrdenAsignacion oa = (OrdenAsignacion) hashCountAsignaciones.get(new Long(fix.SEC_ORDEN.longValue()));
								
								EventoBitacora ev = new EventoBitacora();
								ev.FechaEventoBitacora = ts;
								ev.IdAsignacion = 0L;
								ev.IdOrden = msj.id;
								if (oa.NumeroAsignaciones > 0) {
									ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_ORDEN_ANULADA_PARCIALMENTE;
								}
								else {
									ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_ORDEN_ANULADA;
								}
								
								ev.IdTransaccion = 0L;
								ev.MensajeEventoBitacora = fix.MENSAJE;
								ev.NombreEntidad = "ORDEN";
								bitacora.add(ev);
								
								Resultado_Salida r = new Resultado_Salida();
								r.IdInterno = fix.IdInterno;
								r.Tipo = MensajeSalida.MENSAJE_OK;
								r.IdReferencia = new Long(fix.SEC_ORDEN.longValue());
								r.MensajeReferencia = msj.descripcion + " " + ev.IdTipoEventoBitacora;
								salida.P_OUT_RESULTADO.add(r);
							}
						}
						
					}					
				}
			}
		}
		
		//MENSAJES CALCE 1 Y 2
		{
			if (calces.size() > 0) {
				
				//BUSCAR PALOS
				List<String> transaccionKeys = new ArrayList<>();
				List<Long> idsOrden = new ArrayList<>();
				for (RespuestaFix_Entrada fix : calces) {
					if (fix.FOLIO_TRANS != null) {
						transaccionKeys.add(fix.FOLIO_TRANS.trim() + fix.FECHA+(fix.BOLSA.equalsIgnoreCase("XBCL")?"BEC":"BCS"));
						idsOrden.add(fix.SEC_ORDEN.longValue());
					}
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("transaccionKeys", transaccionKeys);
				
				List<InformacionTransaccion> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, InformacionTransaccion.class);
				
				HashMap<String,Object> params2 = new HashMap<>();
				params2.put("idsOrden", idsOrden);
				List<Orden> ordenes = _sql.query(conn, SQL_SELECT_ORDENES, params2, Orden.class);
				
				List<Asignacion_Entrada> asignaciones = new ArrayList<>();;
				for (RespuestaFix_Proceso fix : calces) {
					
					for (InformacionTransaccion tran : transacciones) {
						if (fix.FOLIO_TRANS.trim().equalsIgnoreCase(tran.Folio) 
								&& fix.FECHA.equalsIgnoreCase(Fecha.toStringDate(tran.FechaSistemaCreacion))
								&& fix.TIPO_OPERAC.equalsIgnoreCase("C") && tran.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
							fix.transaccion = tran;
							break;
						}
						else if (fix.FOLIO_TRANS.trim().equalsIgnoreCase(tran.Folio) 
								&& fix.FECHA.equalsIgnoreCase(Fecha.toStringDate(tran.FechaSistemaCreacion))
								&& fix.TIPO_OPERAC.equalsIgnoreCase("V") && tran.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA)) {
							fix.transaccion = tran;
							break;
						}
					}
					
					if (fix.transaccion == null) {
						
						//SI EL CALCE ES BEC Y EL PARAMETRO ESTA ENCENDIDO, CREAMOS EL PALO DESDE ACA
						String crearPalosBEC = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "CrearPalosBEC");
						if (fix.BOLSA != null && fix.BOLSA.equalsIgnoreCase("XBCL") && crearPalosBEC.equalsIgnoreCase("S")) {
							int res = crearTransaccion(conn,fix,entrada.p_IdEmpresa,entrada.p_IdUsuario);
							
							//repetir select, ahora deberia venir el palo
							transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, InformacionTransaccion.class);
							for (InformacionTransaccion tran : transacciones) {
								if (fix.FOLIO_TRANS.trim().equalsIgnoreCase(tran.Folio) 
										&& fix.FECHA.equalsIgnoreCase(Fecha.toStringDate(tran.FechaSistemaCreacion))
										&& fix.TIPO_OPERAC.equalsIgnoreCase("C") && tran.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
									fix.transaccion = tran;
									break;
								}
								else if (fix.FOLIO_TRANS.trim().equalsIgnoreCase(tran.Folio) 
										&& fix.FECHA.equalsIgnoreCase(Fecha.toStringDate(tran.FechaSistemaCreacion))
										&& fix.TIPO_OPERAC.equalsIgnoreCase("V") && tran.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA)) {
									fix.transaccion = tran;
									break;
								}
							}
						}
					}
					
					if (fix.transaccion == null) {
						
						String codigo = "TRANSACCION_NO_HA_LLEGADO";
						String desc = "No ha llegado transaccion " + fix.FOLIO_TRANS + " " + fix.FECHA;
						salida.P_OUT_MENSAJES.add(new MensajeSalida(fix.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,codigo,desc));
						
						Resultado_Salida r = new Resultado_Salida();
						r.IdInterno = fix.IdInterno;
						r.Tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
						r.IdReferencia = fix.IdInterno;
						r.MensajeReferencia = desc;
						salida.P_OUT_RESULTADO.add(r);
						
						continue;
					}
					
					for (Orden o : ordenes) {
						if (o.IdOrden.equals(fix.SEC_ORDEN.longValue())) {
							fix.orden = o;
							break;
						}
					}
					if (fix.orden == null) {
						String codigo = "NO_SE_ENCONTRO_ORDEN";
						String desc = "No se encontro orden" + fix.SEC_ORDEN.longValue();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(fix.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
						continue;
					}
					
					Asignacion_Entrada asig = new Asignacion_Entrada();
					asig.Cantidad = fix.CANTIDAD;
					asig.FolioCartera = 0L;
					asig.IdInterno = fix.IdInterno;
					asig.IdOrden = fix.orden.IdOrden;
					asig.IdTransaccion = fix.transaccion.IdTransaccion;
					asig.ValorRescate = BigDecimal.ZERO;
					
					asignaciones.add(asig);
					
				}
				
				if (asignaciones.isEmpty()) {
					return salida;
				}
				
				CrearAsignaciones serviceCrearAsignaciones = new CrearAsignaciones();
				CrearAsignaciones_Entrada entradaCrearAsignaciones = new CrearAsignaciones_Entrada();
				entradaCrearAsignaciones.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearAsignaciones.p_IdModulo = _c.ID_MODULO_ORDENES;
				entradaCrearAsignaciones.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearAsignaciones.p_simularOperacion = _c.NO;
				entradaCrearAsignaciones.p_UsaReglasNegocio = _c.SI;
				entradaCrearAsignaciones.p_Asignaciones = asignaciones;
				CrearAsignaciones_Salida salidaCrearAsignaciones = serviceCrearAsignaciones.logica(conn, entradaCrearAsignaciones);
				salida.P_OUT_MENSAJES.addAll(salidaCrearAsignaciones.P_OUT_MENSAJES);
				
				for (RespuestaFix_Proceso fix : calces) {
					for (cl.bcs.ordenes.CrearAsignaciones.Resultado_Salida res : salidaCrearAsignaciones.p_out_Resultado) {
						if (res.EsAPlazo.equalsIgnoreCase(_c.NO) && fix.IdInterno.equals(res.IdInterno)) {
							fix.IdAsignacion = res.IdAsignacion;
						}
					}
				}
				//BUSCAR CONFIGURACION CANAL DISTRIBUCION
				HashMap<String,Object> params3 = new HashMap<>();
				params3.put("IdEmpresa", entrada.p_IdEmpresa);
				List<ConfiguracionCanalDistribucion> configuracionCanal = _sql.query(conn, SQL_SELECT_CONFIGURACION_CANAL_DISTRIBUCION, params3, ConfiguracionCanalDistribucion.class);
				
				for (RespuestaFix_Proceso fix : calces) {
					
					for (ConfiguracionCanalDistribucion c1 : configuracionCanal) {
						String bolsa = fix.BOLSA.equalsIgnoreCase("XBCL")?"BEC":"BCS";
						if (fix.IdAsignacion != null && c1.IdTipoCanalDistribucion.equalsIgnoreCase(fix.orden.IdTipoCanalDistribucion)
								&& c1.IdBolsa.equalsIgnoreCase(bolsa)
								&& c1.IdTipoCanalIngreso.equalsIgnoreCase(fix.orden.IdTipoCanalIngreso)
								&& c1.IdTipoInstrumento.equalsIgnoreCase(fix.orden.IdTipoInstrumento)) {
							
							fix.GeneraMovimientoComprobante = c1.GeneraMovimientoFacturacionAutomatico;
							fix.GeneraComprobante = c1.GeneraComprobanteFacturacionAutomatico;
							
						}
					}
				}
				//ENVIAR A FACTURAR Y GENERAR COMPROBANTE
				
				List<cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada> asignacionesFacturar = new ArrayList<>();
				for (RespuestaFix_Proceso fix : calces) {
					
					if (fix.IdAsignacion != null && fix.GeneraMovimientoComprobante != null && fix.GeneraMovimientoComprobante.equalsIgnoreCase(_c.SI)) {
						
						cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada asd = new cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada();
						asd.IdAsignacion = fix.IdAsignacion;
						if (fix.GeneraComprobante != null && fix.GeneraComprobante.equalsIgnoreCase(_c.SI)) {
							asd.GeneraComprobante = _c.SI;	
						}
						else {
							asd.GeneraComprobante = _c.NO;
						}
						
						asignacionesFacturar.add(asd);
						
					}
				}
				
				
				if (asignacionesFacturar.size() > 0) {
					EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionFacturar = new EnviarAsignacionesFacturar_Entrada();
					entradaEnviarAsignacionFacturar.p_Asignaciones = asignacionesFacturar;
					entradaEnviarAsignacionFacturar.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaEnviarAsignacionFacturar.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaEnviarAsignacionFacturar.p_IdUsuario = entrada.p_IdUsuario;
					EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
					EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionFacturar);
					
					if (salidaEnviarAsignacionesFacturar.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
						logger.error(salida.P_OUT_MENSAJES);
					}
					
					for (RespuestaFix_Proceso fix : calces) {
						for (Resultado res : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
							if (res.IdAsignacion.equals(fix.IdAsignacion)) {
								fix.IdMovimientoComprobante = res.IdMovimientoComprobante;								
							}
						}
						
						for (MensajeSalida msj : salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES) {
							
							if (msj.id.equals(fix.IdAsignacion)) {
								MensajeSalida msj2 = new MensajeSalida();
								Utils.copyPojo(msj, msj2);
								msj2.id = fix.IdInterno;
								salida.P_OUT_MENSAJES.add(msj2);
							}
						}
					}
				}
			}
			
			for (RespuestaFix_Proceso fix : calces) {
				boolean hayBitacora = false; 
				if (fix.GeneraMovimientoComprobante.equalsIgnoreCase(_c.SI) && fix.IdMovimientoComprobante.longValue() > 0){
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_OK;
					r.IdReferencia = fix.IdMovimientoComprobante;
					r.MensajeReferencia = "MOVIMIENTO COMPROBANTE GENERADO";
					salida.P_OUT_RESULTADO.add(r);
					hayBitacora = true;
				} 
				else if (fix.GeneraMovimientoComprobante.equalsIgnoreCase(_c.SI) && fix.IdMovimientoComprobante == null){
					//SE CAYO AL GENERAR MOVIMIENTOCOMPROBANTE
					
				}
				else if (fix.IdAsignacion != null && fix.IdAsignacion.longValue() > 0) {
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_OK;
					r.IdReferencia = fix.IdAsignacion;
					r.MensajeReferencia = "ASIGNACION GENERADA";
					salida.P_OUT_RESULTADO.add(r);
					hayBitacora = true;
				}
				else {
					//NO ASIGNA
				}
				
				if (hayBitacora) {
					EventoBitacora ev = new EventoBitacora();
					ev.FechaEventoBitacora = ts;
					if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_CALCE_COMPLETO)) {
						ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_EJECUTADA_COMPLETAMENTE;
					}
					else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_CALCE_PARCIAL)) {
						ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_EJECUTADA_PARCIALMENTE;
					}
					ev.IdAsignacion = 0L;
					ev.IdOrden = fix.orden.IdOrden;
					ev.IdTransaccion = 0L;
					ev.MensajeEventoBitacora = "Calce por " + fix.CANTIDAD;
					ev.NombreEntidad = "ORDEN";
					bitacora.add(ev);
				}
			}
			
			
			
			
		}
		
		//NEWS Y OTROS
		{
			if (mensajesNews.size() > 0) {
				for (RespuestaFix_Entrada fix : mensajesNews) {
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_OK;
					r.IdReferencia = 0L;
					r.MensajeReferencia = "";
					salida.P_OUT_RESULTADO.add(r);
				}
				
				
			}
		}
		
		//ERRORES
		{
			if (mensajesError.size() > 0) {
				for (RespuestaFix_Entrada fix : mensajesError) {
					String codigo = "NO_SE_ENCONTRO_ORDEN";
					String desc = "No se encontro orden " + fix.SEC_ORDEN.longValue();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(fix.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					
				}
				
				
			}
		}
		
		if (bitacora.size() > 0) {
			_sql.batch(conn, SQL_INSERT_BITACORA, bitacora, EventoBitacora.class);
		}
		
		return salida;
	}
	
	public static class Orden_Proceso {
		public Long IdOrden;
		public Date FechaLiquidacion;
	}
	
	public static class Corredora {
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private int crearTransaccion(Connection conn, RespuestaFix_Proceso fix, Long idEmpresa, Long idUsuario) throws Exception {
		
		ConsultarEmpresa emp = new ConsultarEmpresa();
		InformacionEmpresa d = emp.logica(conn, idEmpresa);
		
		HashMap<String,Corredora> corredoras = new HashMap<String,Corredora>();
		{
			SQL sql = new SQL();
			HashMap<String,Object> params1 = new HashMap<>();
			List<Corredora> _corredoras = sql.query(conn, SQL_SELECT_CORREDORAS_BEC, params1, Corredora.class);
			corredoras = new HashMap<>();
			for (Corredora corr : _corredoras) {
				corredoras.put(corr.CodigoCorredoraFix, corr);
			}
		}
		
		CrearTransaccionesMarketDataBEC_Entrada entradaCrearTransaccionesMarketData = new CrearTransaccionesMarketDataBEC_Entrada();
		entradaCrearTransaccionesMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionesMarketData.p_IdUsuario = idUsuario;
		entradaCrearTransaccionesMarketData.transacciones = new ArrayList<>();
		
		if (fix.CONTRA_BROKER == null || fix.CONTRA_BROKER.trim().length() == 0) {
			logger.error("No viene la contraparte");
			return 0;
		}
		
		TransaccionBECMarketData_Entrada letran = new TransaccionBECMarketData_Entrada();
		
		letran.IdInterno = 0L;
		letran.Accion = 0;
		letran.Anticipable = false; 
		if (fix.TIPO_OPERAC.equalsIgnoreCase("C")) {
			letran.BCSCodeComprador =  d.Codigo;	
		}
		else {
			if (fix.CONTRA_BROKER == null) letran.BCSCodeVendedor = "";
			else {
				letran.BCSCodeComprador =  (fix.CONTRA_BROKER.charAt(0) == '0')? fix.CONTRA_BROKER.substring(1):fix.CONTRA_BROKER;
				letran.BCSCodeComprador = corredoras.get(letran.BCSCodeComprador).CodigoCorredora;
			}
		}
		
		letran.BCSCodeCompradorOpdir = ""; 
				
		if (fix.TIPO_OPERAC.equalsIgnoreCase("C")) {
			if (fix.CONTRA_BROKER == null) letran.BCSCodeVendedor = "";
			else
				letran.BCSCodeVendedor = (fix.CONTRA_BROKER.charAt(0) == '0')? fix.CONTRA_BROKER.substring(1):fix.CONTRA_BROKER;
				letran.BCSCodeVendedor = corredoras.get(letran.BCSCodeVendedor).CodigoCorredora;
		}
		else {
			letran.BCSCodeVendedor =  d.Codigo;
		}
		
		letran.BCSCodeVendedorOpdir = ""; 
		letran.Board = "";
		letran.Cantidad = fix.CANTIDAD.doubleValue();
		letran.ClientIDCompra = "";
		letran.ClientIDVenta = "";
		letran.CompradorBCSTraderID = ""; 
		letran.CompradorIdCorredor = letran.BCSCodeComprador;
		letran.CompradorIdOpdir =  "";
		letran.CompradorTraderID =  "";
		letran.CompradorTraderIdOpdir = 
		letran.CompradorXStreamIdCorredor = 
		letran.CompradorXStreamIdOpdir = 
		letran.EmisorID = "";
		letran.EmisorNombre = ""; 
		letran.Fecha =  fix.FECHA.replaceAll("-", "");
		letran.FechaRegistro =  fix.IN_SGC.replaceFirst(":", " ");
		letran.Folio =  Integer.parseInt(fix.FOLIO_TRANS.trim());
		letran.FolioRelacionado = 0;
		letran.FolioXStream = "";
		letran.Generico = "";
		letran.Grupo = "";
		letran.Hora = fix.IN_SGC.substring(11,19);
		letran.ID = 0;
		letran.Instrumento = fix.NEMO.trim();
		letran.LugarPago = "";
		letran.Mercado = 0;
		letran.Moneda = "";
		letran.Monto = fix.CANTIDAD.multiply(fix.PRECIO).doubleValue();
		letran.Plazo = 0;
		letran.PlazoAAMM = ""; 
		letran.PlazoDias = "";
		letran.Precio = fix.PRECIO.doubleValue();
		letran.Reajustabilidad = "";
		letran.TipoInstrumentoRF = "";
		letran.ValorPar = 0L;
		letran.Variacion = 0L;
		letran.VendedorBCSTraderID = ""; 
		letran.VendedorIdCorredor = letran.BCSCodeVendedor;
		letran.VendedorIdOpdir = "";
		letran.VendedorTraderID = "";
		letran.VendedorTraderIdOpdir = ""; 
		letran.VendedorXStreamIdCorredor = ""; 
		letran.VendedorXStreamIdOpdir = "";
		letran.fondoCompra = "";
		letran.fondoVenta = "";
		letran.madurez = false;
		letran.odAutomatica = false; 
		letran.offshore = false;
		letran.puntaCompraBolsa = ""; 
		letran.puntaCompraCantidad = 0;
		letran.puntaCompraPrecio = 0; 
		letran.puntaVentaBolsa = "";
		letran.puntaVentaCantidad = 0;
		letran.puntaVentaPrecio = 0;
		letran.ventaCorta = false;
		
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdOrden", fix.SEC_ORDEN.toString());
		List<Orden_Proceso> ord = sql.query(conn, "SELECT \"IdOrden\", \"FechaLiquidacion\" FROM \"BSCL.Orden.BD::ORD.Orden\" WHERE \"IdOrden\" = :IdOrden", params, Orden_Proceso.class);
		if (ord.size() == 0) {
			logger.error("No encontre la orden");
			return 0; //nos fue mal
		}
		
		letran.FechaLiquidacion = ord.get(0).FechaLiquidacion.toString().replaceAll("-", "");
		
		ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
		entradaObtenerPlazo.p_IdPais = d.IdPais;
		
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		
		ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
		f.FechaDesde = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(letran.Fecha)));
		f.FechaHasta = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(letran.FechaLiquidacion)));
		f.IdInterno = 0L;
		f.SoloDiasHabiles = "S";
		entradaObtenerPlazo.p_Fechas.add(f);


		ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
		OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
		for (cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut fecha : salidaObtenerPlazo.p_out_Fechas) {
			letran.Plazo = fecha.Plazo.intValue();
		}
		
		
		entradaCrearTransaccionesMarketData.transacciones.add(letran);
		
		CrearTransaccionesMarketDataBEC serviceCrearTransaccionesMarketData = new CrearTransaccionesMarketDataBEC();
		CrearTransaccionesMarketDataBEC_Salida salidaCrearTransaccionesMarketData = serviceCrearTransaccionesMarketData.logica(conn, entradaCrearTransaccionesMarketData);
		logger.info("Salida CrearTransacciones BEC");
		logger.info(salidaCrearTransaccionesMarketData.P_OUT_RESULTADO);
		return 1; //nos fue bien
		
	}

	public static class ProcesarMensajeFix_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<RespuestaFix_Entrada> p_MensajesFix;
		public String p_Tabla;
		public String p_TipoMensaje;
		
		
		public ProcesarMensajeFix_Entrada() {
			p_MensajesFix = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado_Salida {
		
		public String Tipo;
		public String MensajeReferencia;
		public Long IdInterno;
		public Long IdReferencia;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesarMensajeFix_Salida {
		
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ProcesarMensajeFix_Salida() {
			this.P_OUT_RESULTADO = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
			
	

		}	
	}
}
	