package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
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
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.ordenes.pojo.ConfiguracionCanalDistribucion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EventoBitacora;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.ordenes.pojo.RespuestaFixMME;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ProcesarMensajeFixMME implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ProcesarMensajeFixMME.class);
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	private final String SQL_INSERT_BITACORA = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFixMME_InsertBitacora.sql");
	private final String SQL_SELECT_COUNT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFixMME_SelectCountAsignaciones.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFixMME_SelectTransacciones.sql");
	private final String SQL_SELECT_ORDENES= loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFixMME_SelectOrdenes.sql");
	private final String SQL_SELECT_CONFIGURACION_CANAL_DISTRIBUCION = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFixMME_SelectConfiguracionCanalDistribucion.sql");
	private final String SQL_SELECT_MENSAJES_PENDIENTES = loadResourceToString("cl/bcs/ordenes/sql/ProcesarMensajesFixMME_SelectMensajesPendientes.sql");
	
	public static class InformacionTransaccion extends Transaccion{
		public String AccionOrden;
	}
	
	public static class OrdenAsignacion {
		public Long IdOrden;
		public Long NumeroAsignaciones;
	}
	
	public static class RespuestaFixMME_Entrada extends RespuestaFixMME {
		public Long IdInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RespuestaFixMME_Proceso extends RespuestaFixMME_Entrada {
		public Long IdAsignacion;
		public Orden orden;
		public Transaccion transaccion;
		public Long IdMovimientoComprobante;
		public String GeneraMovimientoComprobante = "N";
		public String GeneraComprobante = "N";
	}
	
	public static class RespuestaFix_Pendiente extends RespuestaFixMME {
		public Long IdInterno;
		public String TipoMensaje;
		public String IdMensaje1;
		public String IdMensaje2;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
		
	public ProcesarMensajeFixMME_Salida logica(Connection conn,ProcesarMensajeFixMME_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ProcesarMensajeFixMME_Salida salida = new ProcesarMensajeFixMME_Salida();
		Timestamp ts = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		List<EventoBitacora> bitacora = new ArrayList<EventoBitacora>();
		
		List<RespuestaFixMME_Proceso> ordenesEnNegociacion = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> ordenesEnviadasBEC = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> ordenesPorRechazar = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> ordenesPorAnular = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> ordenesCanceladas = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> mensajesError = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> calces = new ArrayList<RespuestaFixMME_Proceso>();
		List<RespuestaFixMME_Proceso> mensajesNews = new ArrayList<RespuestaFixMME_Proceso>();
		
		for (RespuestaFixMME_Entrada _fix : entrada.p_MensajesFix) {
			
			//si hay mensaje pendiente previo de la misma orden, no proceso este mensaje se ignoran Errores
			if (_fix.SEC_ORDEN != null && new BigDecimal(_fix.SEC_ORDEN).compareTo(BigDecimal.ZERO) >=1) {
				String queryMensajesPendientes = SQL_SELECT_MENSAJES_PENDIENTES.replace("<NOMBRE_TABLA>", entrada.p_Tabla);
				HashMap<String,Object> params = new HashMap<String, Object>();
					//anulacion desde optimus divide entre 10
					Long idOrden = Long.parseLong(_fix.SEC_ORDEN);
						if(_c.ESTADO_FIX_ORDEN_ANULADA.equalsIgnoreCase(_fix.ESTADO) && !"N".equalsIgnoreCase(_fix.ESOPTIMUS)) {
							idOrden = Long.parseLong(_fix.SEC_ORDEN)/10;
						}
					params.put("SEC_ORDEN",idOrden);
				params.put("FECHA",_fix.FECHA);
				params.put("SECUENCIA",_fix.SECUENCIA);
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
			
			RespuestaFixMME_Proceso fix = new RespuestaFixMME_Proceso();
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
			else if (fix.ESTADO.equalsIgnoreCase(_c.ESTADO_FIX_CANCELADA)) {
				ordenesCanceladas.add(fix);
			}
			else {
				
			}
		}
		
		//MENSAJES "ROUTED TO XBCL
		{
			for (RespuestaFixMME_Entrada fix : ordenesEnviadasBEC) {
				EventoBitacora ev = new EventoBitacora();
				ev.FechaEventoBitacora = ts;
				ev.IdAsignacion = 0L;
				ev.IdOrden = Long.parseLong(fix.SEC_ORDEN);
				ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL;
				ev.IdTransaccion = 0L;
				ev.MensajeEventoBitacora = fix.MENSAJE;
				ev.NombreEntidad = "ORDEN";
				bitacora.add(ev);
				
				Resultado_Salida r = new Resultado_Salida();
				r.IdInterno = fix.IdInterno;
				r.Tipo = MensajeSalida.MENSAJE_OK;
				r.IdReferencia = Long.parseLong(fix.SEC_ORDEN);
				r.MensajeReferencia = "Orden " + fix.SEC_ORDEN + " enviada a la BEC";
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
				for (RespuestaFixMME_Entrada fix : ordenesEnNegociacion) {
					if (_hashOrdenes.contains(Long.parseLong(fix.SEC_ORDEN))) {
						continue;
					}
					Orden_Entrada o1 = new Orden_Entrada();
					o1.IdOrden = Long.parseLong(fix.SEC_ORDEN);
					o1.IdNuevoEstado = _c.ESTADO_ORDEN_OFERTADA;
					entradaCambiarEstadoOrdenes.p_Ordenes.add(o1);
					_hashOrdenes.add(Long.parseLong(fix.SEC_ORDEN));
				}
				
				CambiarEstadoOrdenes serviceCambiarEstadoOrdenes = new CambiarEstadoOrdenes();
				CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = serviceCambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
				salida.P_OUT_MENSAJES.addAll(salidaCambiarEstadoOrdenes.P_OUT_MENSAJES);
				
				for (MensajeSalida msj : salidaCambiarEstadoOrdenes.P_OUT_MENSAJES) {
					for (RespuestaFixMME_Entrada fix : ordenesEnNegociacion) {
						if (Long.parseLong(fix.SEC_ORDEN) == msj.id && msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
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
							r.IdReferencia = Long.parseLong(fix.SEC_ORDEN);
							r.MensajeReferencia = msj.descripcion + " " + _c.TIPO_EVENTO_BITACORA_EN_NEGOCIACION + " para orden " + r.IdReferencia;
							
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
				for (RespuestaFixMME_Entrada fix : ordenesPorRechazar) {
					if (_hashOrdenes.contains(Long.parseLong(fix.SEC_ORDEN))) {
						continue;
					}
					Orden_Entrada o1 = new Orden_Entrada();
					o1.IdOrden = Long.parseLong(fix.SEC_ORDEN);
					o1.IdNuevoEstado = _c.ESTADO_ORDEN_RECHAZADA;
					entradaCambiarEstadoOrdenes.p_Ordenes.add(o1);
					_hashOrdenes.add(Long.parseLong(fix.SEC_ORDEN));
				}
				
				CambiarEstadoOrdenes serviceCambiarEstadoOrdenes = new CambiarEstadoOrdenes();
				CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = serviceCambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
				salida.P_OUT_MENSAJES.addAll(salidaCambiarEstadoOrdenes.P_OUT_MENSAJES);
				
				for (MensajeSalida msj : salidaCambiarEstadoOrdenes.P_OUT_MENSAJES) {
					for (RespuestaFixMME_Entrada fix : ordenesPorRechazar) {
						if (Long.parseLong(fix.SEC_ORDEN) == msj.id && msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
							EventoBitacora ev = new EventoBitacora();
							ev.FechaEventoBitacora = ts;
							ev.IdAsignacion = 0L;
							ev.IdOrden = msj.id;
							ev.IdTipoEventoBitacora = _c.TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA;
							ev.IdTransaccion = 0L;
							ev.MensajeEventoBitacora = fix.MENSAJE; //ACA HABRIA QUE TRADUCIR LOS MENSAJES
							ev.NombreEntidad = "ORDEN";
							bitacora.add(ev);
							
							Resultado_Salida r = new Resultado_Salida();
							r.IdInterno = fix.IdInterno;
							r.Tipo = MensajeSalida.MENSAJE_OK;
							r.IdReferencia = Long.parseLong(fix.SEC_ORDEN);
							r.MensajeReferencia = msj.descripcion + " " + _c.TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA + " para orden " + r.IdReferencia;
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
				for (RespuestaFixMME_Entrada fix : ordenesPorAnular) {
					Long idOrden = Long.parseLong(fix.SEC_ORDEN);
					if(!"N".equalsIgnoreCase(fix.ESOPTIMUS)) {
						idOrden = Long.parseLong(fix.SEC_ORDEN)/10;
					}
					
					
					if (idsOrden.contains(idOrden)) {
						continue;
					}
					
					idsOrden.add(idOrden); //porque enviamos el sec_orden multiplicado por 10 para evitar el mensaje de ClOrdId duplicado
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("idsOrden", idsOrden);
				List<OrdenAsignacion> _countAsignaciones = _sql.query(conn, SQL_SELECT_COUNT_ASIGNACIONES, params, OrdenAsignacion.class);
				HashMap<Long, Object> hashCountAsignaciones = Utils.List2Hash(_countAsignaciones, "IdOrden");
				CambiarEstadoOrdenes_Entrada entradaCambiarEstadoOrdenes = new CambiarEstadoOrdenes_Entrada();
				entradaCambiarEstadoOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCambiarEstadoOrdenes.p_IdUsuario = entrada.p_IdUsuario;
				
				List<Long> _hashOrdenes = new ArrayList<>();
				for (RespuestaFixMME_Entrada fix : ordenesPorAnular) {
					
					Long idOrden = Long.parseLong(fix.SEC_ORDEN);
					if(!"N".equalsIgnoreCase(fix.ESOPTIMUS)) {
						idOrden = Long.parseLong(fix.SEC_ORDEN)/10;
					}

					
					
					//CUANDO LA ORDEN NO EXISTE
					if (!hashCountAsignaciones.containsKey(idOrden)) {
						RespuestaFixMME_Proceso fixP = new RespuestaFixMME_Proceso();
						Utils.copyPojo(fix, fixP);
						mensajesError.add(fixP);
						continue;
					}
					
					if (_hashOrdenes.contains(idOrden)) {
						continue;
					}
					
					OrdenAsignacion oa = (OrdenAsignacion) hashCountAsignaciones.get(idOrden);
					Orden_Entrada o1 = new Orden_Entrada();
					o1.IdOrden = idOrden;
					
					if (oa.NumeroAsignaciones > 0) {
						o1.IdNuevoEstado = _c.ESTADO_ORDEN_ANULADA_PARCIAL;
					}
					else {
						o1.IdNuevoEstado = _c.ESTADO_ORDEN_ANULADA;
					}
					
					entradaCambiarEstadoOrdenes.p_Ordenes.add(o1);
					_hashOrdenes.add(idOrden);
				}
				
				if (entradaCambiarEstadoOrdenes.p_Ordenes.size() >0) {
					CambiarEstadoOrdenes serviceCambiarEstadoOrdenes = new CambiarEstadoOrdenes();
					CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = serviceCambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
					salida.P_OUT_MENSAJES.addAll(salidaCambiarEstadoOrdenes.P_OUT_MENSAJES);
					
					for (MensajeSalida msj : salidaCambiarEstadoOrdenes.P_OUT_MENSAJES) {
						for (RespuestaFixMME_Entrada fix : ordenesPorAnular) {
							
							Long idOrden = Long.parseLong(fix.SEC_ORDEN);
							if(!"N".equalsIgnoreCase(fix.ESOPTIMUS)) {
								idOrden = Long.parseLong(fix.SEC_ORDEN)/10;
							}							
							
							if (idOrden.compareTo(msj.id) == 0 && msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
								
								OrdenAsignacion oa = (OrdenAsignacion) hashCountAsignaciones.get(idOrden);
								
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
								r.IdReferencia = idOrden;
								r.MensajeReferencia = msj.descripcion + " " + ev.IdTipoEventoBitacora + " para orden " + r.IdReferencia;
								
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
				for (RespuestaFixMME_Entrada fix : calces) {
					if (fix.FOLIO_TRANS != null) {
						transaccionKeys.add(fix.FOLIO_TRANS.trim() + fix.FECHA+(fix.BOLSA.equalsIgnoreCase("XBCL")?"BEC":"BCS"));
						idsOrden.add(Long.parseLong(fix.SEC_ORDEN));
					}
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("transaccionKeys", transaccionKeys);
				
				List<InformacionTransaccion> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, InformacionTransaccion.class);
				
				HashMap<String,Object> params2 = new HashMap<>();
				params2.put("idsOrden", idsOrden);
				List<Orden> ordenes = _sql.query(conn, SQL_SELECT_ORDENES, params2, Orden.class);
				
				List<Asignacion_Entrada> asignaciones = new ArrayList<>();;
				for (RespuestaFixMME_Proceso fix : calces) {
					
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
						if (o.IdOrden.compareTo(Long.parseLong(fix.SEC_ORDEN)) == 0) {
							fix.orden = o;
							break;
						}
					}
					if (fix.orden == null) {
						String codigo = "NO_SE_ENCONTRO_ORDEN";
						String desc = "No se encontro orden" + Long.parseLong(fix.SEC_ORDEN);
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
				
				for (RespuestaFixMME_Proceso fix : calces) {
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
				
				for (RespuestaFixMME_Proceso fix : calces) {
					
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
				for (RespuestaFixMME_Proceso fix : calces) {
					
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
					
					//TODO SI ME VAN MAL, FALTA CONSIDERAR ESE CASO, EJ MODULO FACTURACION CERRADO
					
					for (RespuestaFixMME_Proceso fix : calces) {
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
			
			for (RespuestaFixMME_Proceso fix : calces) {
				boolean hayBitacora = false; 
				if (fix.GeneraMovimientoComprobante.equalsIgnoreCase(_c.SI) && fix.IdMovimientoComprobante.longValue() > 0){
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_OK;
					r.IdReferencia = fix.IdMovimientoComprobante;
					r.MensajeReferencia = "Asignacion:" + fix.IdAsignacion +" Movimiento a Facturar:" + fix.IdMovimientoComprobante;
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
					r.MensajeReferencia = "Asignacion:" + fix.IdAsignacion;;
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
				for (RespuestaFixMME_Entrada fix : mensajesNews) {
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_OK;
					r.IdReferencia = 0L;
					r.MensajeReferencia = "Mensaje Ignorado News";
					salida.P_OUT_RESULTADO.add(r);
				}
				
				
			}
		}
		
		//Ordenes Canceladas
		{
			if (ordenesCanceladas.size() > 0) {
				for (RespuestaFixMME_Entrada fix : ordenesCanceladas) {
					Resultado_Salida r = new Resultado_Salida();
					r.IdInterno = fix.IdInterno;
					r.Tipo = MensajeSalida.MENSAJE_OK;
					r.IdReferencia = 0L;
					r.MensajeReferencia = "Mensaje de cancelacion de orden " + fix.SEC_ORDEN + " ignorado";
					salida.P_OUT_RESULTADO.add(r);
				}
				
				
			}
		}
		
		//ERRORES
		{
			if (mensajesError.size() > 0) {
				for (RespuestaFixMME_Entrada fix : mensajesError) {
					String codigo = "NO_SE_ENCONTRO_ORDEN";
					String desc = "No se encontro orden " + Long.parseLong(fix.SEC_ORDEN);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(fix.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					
				}
				
				
			}
		}
		
		if (bitacora.size() > 0) {
			_sql.batch(conn, SQL_INSERT_BITACORA, bitacora, EventoBitacora.class);
		}
		
		return salida;
	}
	
	public static class ProcesarMensajeFixMME_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<RespuestaFixMME_Entrada> p_MensajesFix;
		public String p_Tabla;
		public String p_TipoMensaje;
		
		
		public ProcesarMensajeFixMME_Entrada() {
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
	
	public static class ProcesarMensajeFixMME_Salida {
		
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ProcesarMensajeFixMME_Salida() {
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
	