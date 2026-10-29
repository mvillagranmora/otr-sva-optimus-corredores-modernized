package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.facturacion.AnularMovimientoComprobante;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Salida;
import cl.bcs.ordenes.AnularAsignaciones;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Salida;
import cl.bcs.ordenes.AnularTransaccionMasiva;
import cl.bcs.ordenes.CrearOrdenesPostreras;
import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearTransacciones;
import cl.bcs.ordenes.EnviarAsignacionesFacturar;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;
import cl.bcs.ordenes.CrearTransacciones.Resultado_Salida;
import cl.bcs.ordenes.CrearTransacciones.Transaccion_Entrada;
import cl.bcs.ordenes.CrearTransaccionesHistoricas;
import cl.bcs.ordenes.CrearTransaccionesHistoricas.EntradaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.CrearTransaccionesHistoricas.InTransaccionHistorica;
import cl.bcs.ordenes.CrearTransaccionesHistoricas.SalidaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.jobs.ControlMercadoPalosRVMME.Usuario;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

public class ControlMercadoModificacionPalosRuebas extends BcsJob{
	
	private static final Logger logger = LogManager.getLogger(ControlMercadoModificacionPalosRuebas.class);
	
	private final String SQL_SELECT_MODIFICACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoModificacionPalosRuebas_SelectModificaciones.sql");
	private final String SQL_SELECT_ORDEN_ANTERIOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoModificacionPalosRuebas_SelectOrdenAnterior.sql");
	private final String SQL_SELECT_ASIGNACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoModificacionPalosRuebas_SelectAsignaciones.sql");
	private final String SQL_SELECT_MOVIMIENTOS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoModificacionPalosRuebas_SelectMovimientos.sql");
	private final String SQL_SELECT_OPERADORES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoModificacionPalosRuebas_SelectOperadores.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_UPDATE_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	
	public static class Asignaciones extends BcsPojo {
		public Long IdAsignacion;
		public Long IdTransaccion;
		public String IdEstadoAsignacion;
	}

	public static class MovimientoFacturar extends BcsPojo  {
		public Long IdAsignacion;
		public Long IdTransaccion;
		public Long IdMovimientoComprobante;
		public Long IdPortafolio;
		public Long IdCliente;
		public String IdEstadoFacturacion;
	}
	
	public static class RegistroModificacion {
		public String IdMensaje1;
		public String IdMensaje2;
		public String TipoMensaje;
		public String Procesado;
		public String Log;
		public BigDecimal FolioMod;
		public String FechaMod;
		public String Fecha;
		public BigDecimal CodComNuevo;
		public BigDecimal CodVenNuevo;
		
		public BigDecimal Cantidad;
		public String CodigoOperadorContraparte;
		public String CodigoOperadorCorredor;
		public String EsBilateral;
		public String EsCuentaPropia;
		public String EsFueraRueda;
		public String EsFungible;
		public String EsVentaCorta;
		public java.sql.Date FechaEmision;
		public java.sql.Date FechaLiquidacion;
		public java.sql.Date FechaSistemaCreacion;
		public java.sql.Date FechaVencimiento;
		public String Folio;
		public BigDecimal GastosCorresponsal;
		public java.sql.Time HoraSistemaCreacion;
		public String IdBolsa;
		public String IdBoveda;
		public String IdCondicionDeLiquidacion;
		public Long IdCorredorContraparte;
		public Long IdCorredorCorresponsal;
		public String IdEmisor;
		public Long IdEmpleado;
		public Long IdInstrumento;
		public Long IdInstrumentoSubyacente;
		public Long IdLibroOrdenes;
		public String IdMoneda;
		public String IdMonedaEmision;
		public String IdMonedaLiquidacionCorresponsal;
		public String IdSistemaBursatil;
		public String IdTipoOperacion;
		public String IdTipoReajuste;
		public Long IdTransaccion;
		public String IndicadorDCV;
		public String IndicadorOrdenDirecta;
		public BigDecimal Intereses;
		public String MarcaNegociacion;
		public BigDecimal MejorPrecioCompra;
		public BigDecimal MejorPrecioVenta;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacionCorresponsal;
		public String Nemotecnico;
		public Long Plazo;
		public BigDecimal Precio;
		public BigDecimal Reajustes;
		public BigDecimal Tasa;
		public BigDecimal TipoCambioCorresponsal;
		public BigDecimal ValorRescate;
		public Long IdTransaccionOd;
		public Long BaseTasa;
		public String Bloqueado;
		public String EsAPlazo;
		public java.sql.Timestamp FechaIngresoOferta;
		public java.sql.Timestamp FechaIngresoRegistro;
		public java.sql.Timestamp FechaModificacionOperacion;
		public java.sql.Timestamp FechaOperacion;
		public java.sql.Date FechaParidad;
		public java.sql.Date FechaTipoCambio;
		public Long FolioCartera;
		public String IdCamara;
		public Long IdCarga;
		public String IdClasificacionTipoInstrumento;
		public Long IdComprobanteTesoreria;
		public Long IdEmpresa;
		public String IdEstado;
		public String IdMonedaSubyacente;
		public Long IdProgramaProcesaAutomatico;
		public String IdTipoInstrumento;
		public Long IdTransaccionPlazo;
		public Long IdUsuarioBloqueo;
		public Long IdUsuarioReserva;
		public String MotivoBloqueo;
		public String MotivoReserva;
		public String NemotecnicoSubyacente;
		public String NemotecnicoSVS;
		public String Reservado;
		public String TipoInteres;
		public BigDecimal ValorParidad;
		public BigDecimal ValorTipoCambio;
		public String BloqueoLiquidacion;
		public BigDecimal MontoRetencion;
		public String IdGrupoReserva;
		public String FlujoOrigen;
		public BigDecimal PrecioRaiz;
		public BigDecimal MontoRaiz;
		public String IdMonedaRaiz;
		public Long IdInstrumentoNegociacion;
		public Long IdCorredoraContraparte;
		public Long IdCorredoraCorresponsal;
		public Long IdCorredoraLocal;
	}
	
	public static class InformacionTransaccion {
		public Long IdTransaccionAntigua;
		public Long IdTransaccionNueva;
		public boolean TieneAsignacion;
		public boolean TieneMovimiento;
		public InformacionTransaccion(Long IdTransaccionAntigua) {
			this.IdTransaccionAntigua = IdTransaccionAntigua;
			this.IdTransaccionNueva = null;
			this.TieneAsignacion = false;
			this.TieneMovimiento = false;
		}
	}
	
	public static class OrdenAnterior extends OrdenPostrera_Entrada{
		public String IdEstadoAsignacion;
		public OrdenPostrera_Entrada ConvertirAEntradaOrdenPostrera() {
			OrdenPostrera_Entrada entrada = new OrdenPostrera_Entrada();
			entrada.IdInterno = this.IdInterno;
			entrada.FechaRecepcion = this.FechaRecepcion;
			entrada.Glosa = this.Glosa;
			entrada.IdBoveda = this.IdBoveda;
			entrada.IdFormaCustodia = this.IdFormaCustodia;
			entrada.EsFacturaInmediata = this.EsFacturaInmediata;
			entrada.IdTransaccion = this.IdTransaccion;
			entrada.IdPortafolio = this.IdPortafolio;
			entrada.Cantidad = this.Cantidad;
			entrada.ValorRescate = this.ValorRescate;
			entrada.IdGrupo = this.IdGrupo;
			entrada.FolioCartera = this.FolioCartera;
			entrada.TasaVentaCorta = this.TasaVentaCorta;
			entrada.PlazoVentaCorta = this.PlazoVentaCorta;
			entrada.TipoCambio = this.TipoCambio;
			entrada.IdMonedaLiquidacionOrden = this.IdMonedaLiquidacionOrden;
			entrada.PagoCobroCuentaInversion = this.PagoCobroCuentaInversion;
			entrada.IdPreferenciaPagoPortafolio = this.IdPreferenciaPagoPortafolio;
			entrada.IdCuentaCustodioPortafolio = this.IdCuentaCustodioPortafolio;
			entrada.CodigoIFRS = this.CodigoIFRS;
			entrada.PorcentajeAllIn = this.PorcentajeAllIn;
			entrada.FlujoOrigen = this.FlujoOrigen;
			entrada.FechaTrade = this.FechaTrade;
			entrada.IngresoDesde = this.IngresoDesde;
			entrada.FolioTransaccion = this.FolioTransaccion;
			entrada.CodigoPortafolio = this.CodigoPortafolio;
			entrada.DocumentoIdentidad = this.DocumentoIdentidad;
			entrada.NombreCompleto = this.NombreCompleto;
			entrada.Nemotecnico = this.Nemotecnico;
			entrada.Monto = this.Monto;
			entrada.IdTipoOperacion = this.IdTipoOperacion;
			return entrada;
		}
	}
	
	public static class Operador {
		public String CodigoOperador;
		public String CodigoOperadorAsociado;
		public String Fondo;
		public Long IdPortafolio;
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}
	
	public boolean logica(Connection con,boolean debug) {
		try {
			logger.info("INICIO::JOB::ProcesarModificacionesPalos");
			SQL sql = new SQL();
			Long idEmpresa = 0L;
			if (debug) {
				idEmpresa = 1L;
			} else{
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			}
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = sql.query(con, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
			params.clear();
			
			List<RegistroModificacion> transacciones = new ArrayList<RegistroModificacion>();
			HashMap<String, Object> parametros 	= new HashMap<String, Object>();
			parametros.put("FechaSistema", fechaSistema);
			transacciones = sql.query(con, SQL_SELECT_MODIFICACIONES, parametros, RegistroModificacion.class);
			if(transacciones.size() <= 0) {
				logger.debug("NO HAY MODIFICACIONES PENDIENTES");
				return true;
			}
			
			List<Long> listaIdTransacciones = new ArrayList<Long>();
			HashMap<String, ControlMercado> hashControlMercado = new HashMap<String, ControlMercado>();
			HashMap<Long,Integer> hashCantidadAsignaciones = new HashMap<Long,Integer>();
			HashMap<Long,String> hashLlaveControlMercado = new HashMap<Long,String>();
			HashMap<Long,RegistroModificacion> hashTransacciones = new HashMap<Long,RegistroModificacion>();
			for(RegistroModificacion mod : transacciones) {
				listaIdTransacciones.add(mod.IdTransaccion);
				ControlMercado cm = new ControlMercado();
				cm.IdMensaje1 = mod.IdMensaje1;
				cm.IdMensaje2 = mod.IdMensaje2;
				cm.TipoMensaje = mod.TipoMensaje;
				cm.Procesado = "E";
				cm.Log = "";
				hashControlMercado.put(mod.IdMensaje1, cm);
				hashCantidadAsignaciones.put(mod.IdTransaccion, 0);
				hashLlaveControlMercado.put(mod.IdTransaccion,mod.IdMensaje1);
				hashTransacciones.put(mod.IdTransaccion, mod);				
			}
			try {
				params.put("IdTransaccion", listaIdTransacciones);
				List<Asignaciones> asignaciones = sql.query(con, SQL_SELECT_ASIGNACIONES, params, Asignaciones.class);
				List<MovimientoFacturar> movimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, MovimientoFacturar.class);
				params.clear();
	
				// Validaciones
				Set<Long> setModificacionesRechazadas = new HashSet<Long>();
				
				// Validar que la transacción solo tenga una asignación
				HashMap<Long,Asignaciones> hashEstadoAsignacion = new HashMap<Long,Asignaciones>();
				HashMap<Long,Long> hashAsignacionTransaccion = new HashMap<Long,Long>();
				List<Asignaciones> asignacionesAModificar = new ArrayList<Asignaciones>();
				for (Asignaciones asig : asignaciones) {
					hashAsignacionTransaccion.put(asig.IdAsignacion, asig.IdTransaccion);
					int cantidadAsignaciones = hashCantidadAsignaciones.get(asig.IdTransaccion) + 1;
					hashCantidadAsignaciones.put(asig.IdTransaccion, cantidadAsignaciones);
					if (cantidadAsignaciones > 1) {					
						setModificacionesRechazadas.add(asig.IdTransaccion);
						String estado = "E";
						String log = "Se descarta modificación para transacción "+asig.IdTransaccion+" por tener mas de una asignación. ";
						logControlMercado(asig.IdTransaccion, hashLlaveControlMercado, hashControlMercado, estado, log);
					}
				}
	
				// Validar que la transacción no esté facturada
				Set<Long> setIdTransaccionesFacturadas = new HashSet<Long>();
				List<MovimientoFacturar> movimientosAModificar = new ArrayList<MovimientoFacturar>();
				HashMap<Long,Long> hashMovimientoTransaccion = new HashMap<Long,Long>();
				for (MovimientoFacturar mov: movimientos) {
					hashMovimientoTransaccion.put(mov.IdMovimientoComprobante, mov.IdTransaccion);
					if (!mov.IdEstadoFacturacion.equalsIgnoreCase("ING")) {
						setIdTransaccionesFacturadas.add(mov.IdTransaccion);
						setModificacionesRechazadas.add(mov.IdTransaccion);
						String log = "Se descarta modificación para transacción "+mov.IdTransaccion+" por estar facturada. ";
						String estado = "E";
						logControlMercado(mov.IdTransaccion, hashLlaveControlMercado, hashControlMercado, estado, log);
					}
				}
				
				// Validar que el operador esté configurado
				HashMap<String, String> hashOperadores = obtenerOperadores(transacciones);
				List<String> operadoresParam = new ArrayList<String>();
				operadoresParam.addAll(hashOperadores.values());
				params.put("IdEmpresa", idEmpresa);
				params.put("Operadores", operadoresParam);
				List<Operador> operadoresConfigurados = sql.query(con, SQL_SELECT_OPERADORES, params, Operador.class);
				params.clear();
				HashMap<String,Operador> hashOperadoresConfigurados = new HashMap<String,Operador>();
				for (Operador op: operadoresConfigurados) {
					hashOperadoresConfigurados.put(op.CodigoOperador, op);
				}
				for(RegistroModificacion mod : transacciones) {
					String llaveConsultaOperador = mod.IdTransaccion+mod.IdTipoOperacion;
					String operador = hashOperadores.get(llaveConsultaOperador);
					Operador operadorConfigurado = hashOperadoresConfigurados.get(operador);
					if (operadorConfigurado == null) {
						String estado = "E";
						String log = "Se descarta modificación para transacción "+mod.IdTransaccion+" ya que el operador no está configurado. ";
						logControlMercado(mod.IdTransaccion, hashLlaveControlMercado, hashControlMercado, estado, log);
						setModificacionesRechazadas.add(mod.IdTransaccion);
					}
				}
				
				for (Asignaciones asig : asignaciones) {
					hashEstadoAsignacion.put(asig.IdAsignacion, asig);
					if (!setModificacionesRechazadas.contains(asig.IdTransaccion)) {				
						asignacionesAModificar.add(asig);
					}
				}
				for (MovimientoFacturar mov: movimientos) {
					if (!setModificacionesRechazadas.contains(mov.IdTransaccion)) {
						movimientosAModificar.add(mov);
					}
				}
				
				List<RegistroModificacion> transaccionesAModificar = new ArrayList<RegistroModificacion>();	
				List<InTransaccion> transaccionesAnular = new ArrayList<InTransaccion>();
				for(RegistroModificacion mod : transacciones) {
					if (!setModificacionesRechazadas.contains(mod.IdTransaccion)) {
						InTransaccion tr = new InTransaccion();
						tr.IdTransaccion = mod.IdTransaccion;
						tr.Motivo = "MODIFICACIÓN DE FONDOS";
						tr.Observaciones = "";
						tr.FechaAnulacion = mod.FechaMod;
						transaccionesAnular.add(tr);
						transaccionesAModificar.add(mod);
					}
				}
	
				List<Long> llaveTransaccionesPorReasignar = new ArrayList<Long>();
				for(Asignaciones asig : asignacionesAModificar) {				
					llaveTransaccionesPorReasignar.add(asig.IdTransaccion);
				}
				
				// Anulaciones
				if (movimientosAModificar.size() > 0) {
					AnularMovimientoComprobante_Salida  salida = anularMovimientosFacturados(con, movimientosAModificar, idEmpresa, idUsuario);
					HashMap<Long,MensajeSalida> hashSalidaMovimientosAnulados = new HashMap<Long,MensajeSalida>();
					for (MensajeSalida msj : salida.P_OUT_MENSAJES) {
						hashSalidaMovimientosAnulados.put(msj.id, msj);
					}
					for(MovimientoFacturar movEntrada : movimientosAModificar) {
						if(hashSalidaMovimientosAnulados.get(movEntrada.IdMovimientoComprobante) == null || hashSalidaMovimientosAnulados.get(movEntrada.IdMovimientoComprobante).tipo.equalsIgnoreCase("ERROR")) {
							Long idTransaccionRelacionada = hashMovimientoTransaccion.get(movEntrada.IdTransaccion);
							String estado = "E";
							String log = "Error al anular movimiento "+movEntrada.IdMovimientoComprobante+". ";
							logControlMercado(idTransaccionRelacionada, hashLlaveControlMercado, hashControlMercado, estado, log);
							setModificacionesRechazadas.add(idTransaccionRelacionada);
						}
					}
				}
				if (asignacionesAModificar.size() > 0) {
					List<Asignaciones> asignacionesSinErrores = quitarAsignacionesPorAnular(setModificacionesRechazadas, asignacionesAModificar);
					AnularAsignaciones_Salida salida = anularAsignaciones(con, asignacionesSinErrores, idEmpresa, idUsuario);
					HashMap<Long,MensajeSalida> hashSalidaAsignacionesAnuladas = new HashMap<Long,MensajeSalida>();
					for (MensajeSalida msj : salida.p_out_Mensajes) {
						hashSalidaAsignacionesAnuladas.put(msj.id, msj);
					}
					for (Asignaciones asigEntrada : asignacionesSinErrores) {
						if (hashSalidaAsignacionesAnuladas.get(asigEntrada.IdAsignacion) == null || hashSalidaAsignacionesAnuladas.get(asigEntrada.IdAsignacion).tipo.equalsIgnoreCase("ERROR")) {
							Long idTransaccionRelacionada = hashAsignacionTransaccion.get(asigEntrada.IdAsignacion);
							String estado = "E";
							String log = "Error al anular asignacion "+asigEntrada.IdAsignacion+". ";
							logControlMercado(idTransaccionRelacionada, hashLlaveControlMercado, hashControlMercado, estado, log);
							setModificacionesRechazadas.add(idTransaccionRelacionada);
						}
					}
				}
				if (transaccionesAnular.size() > 0) {
					List<InTransaccion> transaccionesSinErrores = quitarTransaccionesPorAnular(setModificacionesRechazadas, transaccionesAnular);
					SalidaAnularTransaccionMasiva salida = anularTransacciones(con, idEmpresa, idUsuario, transaccionesSinErrores);
					HashMap<Long,MensajeSalida> hashTransaccionesAnuladas = new HashMap<Long,MensajeSalida>();
					for (MensajeSalida msj : salida.P_OUT_MENSAJES) {
						hashTransaccionesAnuladas.put(msj.id, msj);
					}
					for(InTransaccion trEntrada : transaccionesSinErrores) {
						if (hashTransaccionesAnuladas.get(trEntrada.IdTransaccion) == null || hashTransaccionesAnuladas.get(trEntrada.IdTransaccion).tipo.equalsIgnoreCase("ERROR")) {
							String estado = "E";
							String log = "Error al anular transacción "+trEntrada.IdTransaccion+". ";
							logControlMercado(trEntrada.IdTransaccion, hashLlaveControlMercado, hashControlMercado, estado, log);
							setModificacionesRechazadas.add(trEntrada.IdTransaccion);
						}
					}
				}
				
				// Crear transacciones
				List<RegistroModificacion> historicoTransaccionesProcesadas= new ArrayList<RegistroModificacion>();
				HashMap<Long, Long> hashIdTransaccionAntigua = new HashMap<Long,Long>();
				CrearTransacciones_Salida salidaCrearTransaccion = new CrearTransacciones_Salida();
				if (transaccionesAModificar.size() > 0) {
					List<RegistroModificacion> transaccionesSinErrores = quitarTransaccionesPorCrear(setModificacionesRechazadas, transaccionesAModificar);
					salidaCrearTransaccion = crearTransaccion(con, idEmpresa, idUsuario, transaccionesSinErrores);
					HashMap<Long,MensajeSalida> hashSalidaMensajes = new HashMap<Long,MensajeSalida>();
					HashMap<String,Resultado_Salida> hashSalidaResultados = new HashMap<String,Resultado_Salida>();
					for (MensajeSalida msj : salidaCrearTransaccion.P_OUT_MENSAJES) {
						hashSalidaMensajes.put(msj.id, msj);
						if(msj.tipo.equalsIgnoreCase("ERROR")) {
							String estado = "E";
							String log = "Error al crear transacción "+msj.id+". ";
							logControlMercado(msj.id, hashLlaveControlMercado, hashControlMercado, estado, log);
							setModificacionesRechazadas.add(msj.id);
						}
					}
					for (Resultado_Salida res : salidaCrearTransaccion.P_OUT_RESULTADO) {
						hashSalidaResultados.put(res.IdInterno+res.IdTipoOperacion, res);
					}
					for(RegistroModificacion tr: transaccionesSinErrores) {
						String estado = "S";
						String log = "";
						boolean esOd = tr.IndicadorOrdenDirecta.equalsIgnoreCase("OD") || tr.IndicadorOrdenDirecta.equalsIgnoreCase("OE") ? true : false;
						Long idUsada = hashSalidaResultados.get(tr.IdTransaccion+tr.IdTipoOperacion) != null ? tr.IdTransaccion : (hashSalidaResultados.get(tr.IdTransaccionOd+tr.IdTipoOperacion) != null && esOd ? tr.IdTransaccionOd : 0L);
						Resultado_Salida res = hashSalidaResultados.get(idUsada+tr.IdTipoOperacion);
						if (res != null) {
							log = log + "Se genera transacción "+res.IdTransaccion+". ";
							hashIdTransaccionAntigua.put(res.IdTransaccion, res.IdInterno);
							logControlMercado(tr.IdTransaccion, hashLlaveControlMercado, hashControlMercado, estado, log);
							historicoTransaccionesProcesadas.add(hashTransacciones.get(tr.IdTransaccion));
						}
					}
				}
				
				
				// INICIO : Asignacion -- CANCELADO
	//			params.clear();
	//			params.put("TransaccionesAReasignar", llaveTransaccionesPorReasignar);
	//			List<OrdenAnterior> asignacionesCreadasAnteriormente = sql.query(con, SQL_SELECT_ORDEN_ANTERIOR, params, OrdenAnterior.class);
	//			params.clear();
	//			List<OrdenPostrera_Entrada> entradaAsignaciones = new ArrayList<OrdenPostrera_Entrada>();
	//			int idInterno = 0;
	//			for (OrdenAnterior orden : asignacionesCreadasAnteriormente) {
	//				OrdenPostrera_Entrada entrada = orden.ConvertirAEntradaOrdenPostrera();
	//				if (orden.IdInterno.compareTo(0L) == 0) {
	//					entrada.IdInterno = (long) idInterno;
	//					idInterno++;
	//				}
	//				entradaAsignaciones.add(entrada);
	//			}
	//			CrearOrdenesPostreras_Salida salidaAsignacion = new CrearOrdenesPostreras_Salida();
	//			if (entradaAsignaciones.size() > 0) {
	//				salidaAsignacion = crearOrdenPostrera(con, entradaAsignaciones, idEmpresa, idUsuario);
	//			}
	//			if (salidaAsignacion.P_OUT_ERRORESCONTADOR != null && salidaAsignacion.P_OUT_ERRORESCONTADOR > 0) {
	//				String errorJob = "Error al asignar";
	//				insertarControlMercado(con, hashControlMercado, errorJob);
	//				throw new Exception(errorJob);
	//			}
	//			List<Long> listaIdAsignacionesEnviadas = new ArrayList<Long>();
	//			for (CrearOrdenesPostreras.Resultado_Salida asig : salidaAsignacion.P_OUT_RESULTADO) {
	//				Asignaciones asignacionAntigua = hashEstadoAsignacion.get(asig.IdInterno);
	//				Long idTransaccionAntigua = null;
	//				if (asignacionAntigua != null && asignacionAntigua.IdTransaccion != null) {
	//					idTransaccionAntigua = asignacionAntigua.IdTransaccion;
	//				}
	//				if (asignacionAntigua != null && asignacionAntigua.IdEstadoAsignacion.equalsIgnoreCase("EAF")) {				
	//					listaIdAsignacionesEnviadas.add(asig.IdAsignacion);
	//				}
	//				String estado = "S";
	//				String log = "Se genera asignación "+asig.IdAsignacion+". ";
	//				logControlMercado(idTransaccionAntigua, hashLlaveControlMercado, hashControlMercado, estado, log);
	//			}
				// FIN : Asignacion -- CANCELADO
				
				// INICIO : Enviar a facturar - CANCELADO
	//			EnviarAsignacionesFacturar_Salida salidaEnvioFactura = new EnviarAsignacionesFacturar_Salida();
	//			
	//			if (listaIdAsignacionesEnviadas.size() > 0) {			
	//				salidaEnvioFactura = enviarAsignacionFacturar(con, listaIdAsignacionesEnviadas, idEmpresa, idUsuario);
	//			}
	//			if (salidaAsignacion.P_OUT_ERRORESCONTADOR != null && salidaEnvioFactura.P_OUT_ERRORESCONTADOR > 0) {
	//				String errorJob = "Error al enviar a facturar";
	//				insertarControlMercado(con, hashControlMercado, errorJob);
	//				throw new Exception(errorJob);
	//			}
	//			for (EnviarAsignacionesFacturar.Resultado mov : salidaEnvioFactura.P_OUT_RESULTADO) {
	//				Long IdTransaccionAntigua = 0L;
	//				if (hashIdTransaccionAntigua.get(mov.IdTransaccion) != null) {
	//					IdTransaccionAntigua = hashIdTransaccionAntigua.get(mov.IdTransaccion);
	//				}
	//				if (IdTransaccionAntigua.compareTo(0L) > 0) {
	//					String estado = "S";
	//					String log = "Se genera movimiento "+mov.IdMovimientoComprobante+". ";
	//					logControlMercado(IdTransaccionAntigua, hashLlaveControlMercado, hashControlMercado, estado, log);
	//				}
	//			}
				// FIN : Enviar a facturar - CANCELADO
								
				// Historial de transacciones
				insertarControlMercado(con, hashControlMercado, null);
				List<RegistroModificacion> historicoTransaccionesPendientes = new ArrayList<RegistroModificacion>();
				for(Long IdModificacionPendiente : setModificacionesRechazadas) {
					historicoTransaccionesPendientes.add(hashTransacciones.get(IdModificacionPendiente));
				}
				List<InTransaccionHistorica> historicoTransacciones = new ArrayList<InTransaccionHistorica>();
				String tipoRegistro = "MOD";
				historicoTransacciones.addAll(agregarTransaccionesHistoricas(historicoTransaccionesPendientes, tipoRegistro, "PEN"));
				historicoTransacciones.addAll(agregarTransaccionesHistoricas(historicoTransaccionesProcesadas, tipoRegistro, "PRO"));
				registrarHistorico(con, historicoTransacciones, idUsuario, idEmpresa);
				logger.info("FIN::JOB::ProcesarModificacionesPalos");
				return true;
			} catch (Exception e) {
				insertarControlMercado(con, hashControlMercado, e.getMessage());
				e.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private List<Asignaciones> quitarAsignacionesPorAnular(Set<Long> transaccionesRechazadas, List<Asignaciones> asignacionesPorAnular){
		List<Asignaciones> nuevaListaAsignaciones = new ArrayList<Asignaciones>();
		for(Asignaciones asig: asignacionesPorAnular) {
			if (!transaccionesRechazadas.contains(asig.IdTransaccion)) {
				nuevaListaAsignaciones.add(asig);
			}
		}
		return nuevaListaAsignaciones;
	}
	
	private List<InTransaccion> quitarTransaccionesPorAnular(Set<Long> transaccionesRechazadas, List<InTransaccion> transaccionesPorAnular){
		List<InTransaccion> nuevaListaTransacciones = new ArrayList<InTransaccion>();
		for(InTransaccion tr : transaccionesPorAnular) {
			if (!transaccionesRechazadas.contains(tr.IdTransaccion)) {
				nuevaListaTransacciones.add(tr);
			}
		}
		return nuevaListaTransacciones;
	}
	
	private List<RegistroModificacion> quitarTransaccionesPorCrear(Set<Long> transaccionesRechazadas, List<RegistroModificacion> transaccionesPorCrear){
		List<RegistroModificacion> nuevaListaTransacciones = new ArrayList<RegistroModificacion>();
		for(RegistroModificacion tr : transaccionesPorCrear) {
			if (!transaccionesRechazadas.contains(tr.IdTransaccion)) {
				nuevaListaTransacciones.add(tr);
			}
		}
		return nuevaListaTransacciones;
	}
	
	private HashMap<String, String> obtenerOperadores(List<RegistroModificacion> transacciones){
		HashMap<String,String> operadores = new HashMap<String,String>();
		for(RegistroModificacion tr : transacciones) {
			boolean considerarOperadorContraparte = (tr.IndicadorOrdenDirecta.equalsIgnoreCase("OD") || tr.IndicadorOrdenDirecta.equalsIgnoreCase("OE")) ? true : false;
			String codigoOperadorCorredor = null;
			String codigoOperadorContraparte = null;
			if (tr.IdTipoOperacion.length() >= 3 && tr.IdTipoOperacion.substring(0,3).equalsIgnoreCase("COM")) {
				codigoOperadorCorredor = codigoOperadorString(tr.CodComNuevo);
				codigoOperadorContraparte = considerarOperadorContraparte ? codigoOperadorString(tr.CodVenNuevo) : codigoOperadorContraparte;
			} else if (tr.IdTipoOperacion.length() >= 3 && tr.IdTipoOperacion.substring(0,3).equalsIgnoreCase("VEN")){
				codigoOperadorCorredor = codigoOperadorString(tr.CodVenNuevo);
				codigoOperadorContraparte = considerarOperadorContraparte ? codigoOperadorString(tr.CodComNuevo) : codigoOperadorContraparte;
			}
			if (codigoOperadorCorredor != null) {
				codigoOperadorCorredor = codigoOperadorString(codigoOperadorCorredor);
				operadores.put(tr.IdTransaccion+tr.IdTipoOperacion,codigoOperadorCorredor);
			}
			if (codigoOperadorContraparte != null) {
				codigoOperadorContraparte = codigoOperadorString(codigoOperadorContraparte);
				operadores.put(tr.IdTransaccion+tr.IdTipoOperacion,codigoOperadorContraparte);
			}
		}
		return operadores;
	}
	
	private SalidaCrearTransaccionesHistoricas registrarHistorico(Connection con, List<InTransaccionHistorica> transacciones, Long usuario, Long empresa) throws Exception {
		EntradaCrearTransaccionesHistoricas entrada = new EntradaCrearTransaccionesHistoricas();
		entrada.p_IdEmpresa = empresa;
		entrada.p_IdUsuario = usuario;
		entrada.p_IdModulo = "ORD";
		entrada.p_UsaReglasNegocio = "S";
		entrada.p_Transacciones.addAll(transacciones);
		CrearTransaccionesHistoricas crearTransaccionesHistoricas = new CrearTransaccionesHistoricas();
		SalidaCrearTransaccionesHistoricas salida = crearTransaccionesHistoricas.logica(con, entrada);
		return salida;
	}

	private List<InTransaccionHistorica> agregarTransaccionesHistoricas(List<RegistroModificacion> transaccionesModificadas, String tipo, String estado) {
		List<InTransaccionHistorica> entradasHistorico = new ArrayList<InTransaccionHistorica>();
		for (RegistroModificacion mod : transaccionesModificadas) {
			InTransaccionHistorica entradaTrHis = new InTransaccionHistorica();
			entradaTrHis.TipoRegistro = tipo;
			entradaTrHis.IdEstadoRegistro = estado;
			entradaTrHis.EsModificacionMarketData = null;
			entradaTrHis.IdTransaccionHistoricoAnulacion = null;
			Transaccion trHis = new Transaccion();
			trHis.IdInterno = mod.IdTransaccion;
			trHis.BaseTasa = mod.BaseTasa;
			trHis.Bloqueado = mod.Bloqueado;
			trHis.EsAPlazo = mod.EsAPlazo;
			trHis.FechaIngresoOferta = mod.FechaIngresoOferta;
			trHis.FechaIngresoRegistro = mod.FechaIngresoRegistro;
			trHis.FechaModificacionOperacion = mod.FechaModificacionOperacion;
			trHis.FechaOperacion = mod.FechaOperacion;
			trHis.FechaParidad = mod.FechaParidad;
			trHis.FechaTipoCambio = mod.FechaTipoCambio;
			trHis.FolioCartera = mod.FolioCartera;
			trHis.IdCamara = mod.IdCamara;
			trHis.IdCarga = mod.IdCarga;
			trHis.IdClasificacionTipoInstrumento = mod.IdClasificacionTipoInstrumento;
			trHis.IdComprobanteTesoreria = mod.IdComprobanteTesoreria;
			trHis.IdCorredoraContraparte = mod.IdCorredoraContraparte;
			trHis.IdCorredoraCorresponsal = mod.IdCorredoraCorresponsal;
			trHis.IdCorredoraLocal = mod.IdCorredoraLocal;
			trHis.IdEmpresa = mod.IdEmpresa;
			trHis.IdEstado = mod.IdEstado;
			trHis.IdMonedaSubyacente = mod.IdMonedaSubyacente;
			trHis.IdProgramaProcesaAutomatico = mod.IdProgramaProcesaAutomatico;
			trHis.IdTipoInstrumento = mod.IdTipoInstrumento;
			trHis.IdTransaccionPlazo = mod.IdTransaccionPlazo;
			trHis.IdUsuarioBloqueo = mod.IdUsuarioBloqueo;
			trHis.IdUsuarioReserva = mod.IdUsuarioReserva;
			trHis.MotivoBloqueo = mod.MotivoBloqueo;
			trHis.MotivoReserva = mod.MotivoReserva;
			trHis.NemotecnicoSubyacente = mod.NemotecnicoSubyacente;
			trHis.NemotecnicoSVS = mod.NemotecnicoSVS;
			trHis.Reservado = mod.Reservado;
			trHis.TipoInteres = mod.TipoInteres;
			trHis.ValorParidad = mod.ValorParidad;
			trHis.ValorTipoCambio = mod.ValorTipoCambio;
			trHis.BloqueoLiquidacion = mod.BloqueoLiquidacion;
			trHis.MontoRetencion = mod.MontoRetencion;
			trHis.IdGrupoReserva = mod.IdGrupoReserva;
			trHis.AccionOrden = null;
			trHis.FlujoOrigen = mod.FlujoOrigen;
			trHis.PrecioRaiz = mod.PrecioRaiz;
			trHis.MontoRaiz = mod.MontoRaiz;
			trHis.IdMonedaRaiz = mod.IdMonedaRaiz;
			trHis.IdInstrumentoNegociacion = mod.IdInstrumentoNegociacion;
			trHis.EsModificacionMarketData = null;
			trHis.EstadoModificacion = null;
			trHis.IdTransaccionHistoricoAnulacion = 0L;
			trHis.Cantidad = mod.Cantidad;
			trHis.CodigoOperadorContraparte = mod.CodigoOperadorContraparte;
			trHis.CodigoOperadorCorredor = mod.CodigoOperadorCorredor;
			trHis.EsBilateral = mod.EsBilateral;
			trHis.EsCuentaPropia = mod.EsCuentaPropia;
			trHis.EsFueraRueda = mod.EsFueraRueda;
			trHis.EsFungible = mod.EsFungible;
			trHis.EsVentaCorta = mod.EsVentaCorta;
			trHis.FechaEmision = mod.FechaEmision;
			trHis.FechaLiquidacion = mod.FechaLiquidacion;
			trHis.FechaSistemaCreacion = mod.FechaSistemaCreacion;
			trHis.FechaVencimiento = mod.FechaVencimiento;
			trHis.Folio = mod.Folio;
			trHis.HoraSistemaCreacion = mod.HoraSistemaCreacion;
			trHis.IdBolsa = mod.IdBolsa;
			trHis.IdBoveda = mod.IdBoveda;
			trHis.IdCondicionDeLiquidacion = mod.IdCondicionDeLiquidacion;
			trHis.IdEmisor = mod.IdEmisor;
			trHis.IdEmpleado = mod.IdEmpleado;
			trHis.IdInstrumento = mod.IdInstrumento;
			trHis.IdInstrumentoSubyacente = mod.IdInstrumentoSubyacente;
			trHis.IdLibroOrdenes = mod.IdLibroOrdenes;
			trHis.IdMoneda = mod.IdMoneda;
			trHis.IdMonedaEmision = mod.IdMonedaEmision;
			trHis.IdMonedaLiquidacionCorresponsal = mod.IdMonedaLiquidacionCorresponsal;
			trHis.IdSistemaBursatil = mod.IdSistemaBursatil;
			trHis.IdTipoOperacion = mod.IdTipoOperacion;
			trHis.IdTipoReajuste = mod.IdTipoReajuste;
			trHis.IdTransaccion = mod.IdTransaccion;
			trHis.IdTransaccionOd = mod.IdTransaccionOd;
			trHis.IndicadorDCV = mod.IndicadorDCV;
			trHis.IndicadorOrdenDirecta = mod.IndicadorOrdenDirecta;
			trHis.MarcaNegociacion = mod.MarcaNegociacion;
			trHis.MejorPrecioCompra = mod.MejorPrecioCompra;
			trHis.MejorPrecioVenta = mod.MejorPrecioVenta;
			trHis.Monto = mod.Monto;
			trHis.MontoLiquidacionCorresponsal = mod.MontoLiquidacionCorresponsal;
			trHis.Nemotecnico = mod.Nemotecnico;
			trHis.Plazo = mod.Plazo;
			trHis.Precio = mod.Precio;
			trHis.Tasa = mod.Tasa;
			trHis.TipoCambioCorresponsal = mod.TipoCambioCorresponsal;
			trHis.ValorRescate = mod.ValorRescate;
			trHis.Intereses = mod.Intereses;
			trHis.Reajustes = mod.Reajustes;
			trHis.GastosCorresponsal = mod.GastosCorresponsal;
			entradaTrHis.Transaccion = trHis;
			entradasHistorico.add(entradaTrHis);
		}
		return entradasHistorico;
	}
	
	private void logControlMercado(Long idTransaccion, HashMap<Long,String> hashLlaveControlMercado, HashMap<String,ControlMercado> hashControlMercado, String estado, String log) {		
		String llaveControlMercado = hashLlaveControlMercado.get(idTransaccion);
		if (llaveControlMercado == null) return;
		ControlMercado cm = hashControlMercado.get(llaveControlMercado);
		if (cm != null) {
			cm.Procesado = estado;
			cm.Log = cm.Log + log;
			hashControlMercado.put(llaveControlMercado, cm);
		}
	}
	
	private void insertarControlMercado(Connection con, HashMap<String,ControlMercado> hashControlMercado, String errorJob) throws Exception {
		SQL sql = new SQL();
		List<ControlMercado> updateControlMercado = new ArrayList<ControlMercado>(hashControlMercado.values());
		if (errorJob != null) {
			for(ControlMercado cm : updateControlMercado) {
				cm.Log = errorJob;
				cm.Procesado = "E";
			}
		}
		sql.batch(con, SQL_UPDATE_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
	}
	
	private SalidaAnularTransaccionMasiva anularTransacciones(Connection con, Long idEmpresa, Long idUsuario, List<InTransaccion> transaccionesAnular) throws Exception {
		logger.info("INICIO::JOB::anularTransacciones");
		AnularTransaccionMasiva anularTransaccionesService = new AnularTransaccionMasiva();
		EntradaAnularTransaccionMasiva entradaAnularTransacciones = new EntradaAnularTransaccionMasiva();
		entradaAnularTransacciones.p_IdEmpresa = idEmpresa;
		entradaAnularTransacciones.p_IdUsuario = idUsuario;
		entradaAnularTransacciones.p_IdModulo = "ORD";
		entradaAnularTransacciones.p_Reproceso = "N";
		entradaAnularTransacciones.p_transacciones = transaccionesAnular;				
		SalidaAnularTransaccionMasiva salida = anularTransaccionesService.logica(con, entradaAnularTransacciones);
		logger.info("FIN::JOB::anularTransacciones");
		return salida;
	}
	
	private AnularAsignaciones_Salida anularAsignaciones(Connection con, List<Asignaciones> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::JOB::anularAsignaciones");
		AnularAsignaciones.AnularAsignaciones_Entrada entrada = new AnularAsignaciones.AnularAsignaciones_Entrada();
	    entrada.p_IdEmpresa = idEmpresa;
	    entrada.p_IdUsuario = idUsuario;
	    entrada.p_IdModulo = "ORD";
	    entrada.asignacionesParaAnular = asignaciones.stream().map(a -> {
	        AnularAsignaciones.Asignacion_Entrada asig = new AnularAsignaciones.Asignacion_Entrada();
	        asig.IdAsignacion = a.IdAsignacion;
	        return asig;
	    }).collect(Collectors.toList());
	    AnularAsignaciones anularAsignaciones = new AnularAsignaciones();
	    AnularAsignaciones_Salida salida = anularAsignaciones.logica(con, entrada);
	    logger.info("FIN::JOB::anularAsignaciones");
	    return salida;
	}
	
	private AnularMovimientoComprobante_Salida anularMovimientosFacturados(Connection con, List<MovimientoFacturar> movimientosFacturados, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::JOB::anularMovimientosFacturados");
		AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada entrada = new AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "ORD";
		entrada.p_UsaReglasNegocio = "S";
		entrada.p_simularOperacion = "N";
		entrada.p_EsIngresoAutomatico = "N";
		entrada.p_Glosa = "";
		entrada.p_MovimientoAAnular = movimientosFacturados.stream().map(mov -> {
			AnularMovimientoComprobante.Movimiento_Entrada m = new AnularMovimientoComprobante.Movimiento_Entrada();
			m.IdMovimientoComprobante = mov.IdMovimientoComprobante;
			return m;
		}).collect(Collectors.toList());
		AnularMovimientoComprobante anularMovimientoComprobante = new AnularMovimientoComprobante();
		AnularMovimientoComprobante_Salida salida = anularMovimientoComprobante.logica(con, entrada);
		logger.info("FIN::JOB::anularMovimientosFacturados");
		return salida;
	}

	private EnviarAsignacionesFacturar_Salida enviarAsignacionFacturar(Connection con, List<Long> idAsignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::JOB::enviarAsignacionFacturar");
		List<Asignacion_Entrada> dataEnvio = new ArrayList<>();
		for (Long id : idAsignaciones) {			
			Asignacion_Entrada entradaAsignacion = new Asignacion_Entrada();
			entradaAsignacion.IdAsignacion = id;
			entradaAsignacion.GeneraComprobante = "N";
			dataEnvio.add(entradaAsignacion);
		}
		EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
		EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
		entradaEnviarAsignacionesFacturar.p_IdEmpresa = idEmpresa;
		entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
		entradaEnviarAsignacionesFacturar.p_IdUsuario = idUsuario;
		entradaEnviarAsignacionesFacturar.p_Asignaciones = dataEnvio;
		EnviarAsignacionesFacturar_Salida salida = serviceEnviarAsignacionesFacturar.logica(con, entradaEnviarAsignacionesFacturar);
		logger.info("FIN::JOB::enviarAsignacionFacturar");
		return salida;
	}
	
	private CrearTransacciones_Salida crearTransaccion(Connection con, Long idEmpresa, Long idUsuario, List<RegistroModificacion> transacciones) throws Exception {
		logger.info("INICIO::JOB::crearTransaccion");
		CrearTransacciones_Entrada entrada = new CrearTransacciones_Entrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "ORD";
		entrada.p_UsaReglasNegocio = "S";
		entrada.p_Transacciones = new ArrayList<Transaccion_Entrada>();
		Set<String> setTransaccionesOD = new HashSet<String>();
		for (RegistroModificacion tr: transacciones) {
			if (setTransaccionesOD.contains(tr.Folio+tr.FechaSistemaCreacion)) continue;
			boolean considerarOperadorContraparte = (tr.IndicadorOrdenDirecta.equalsIgnoreCase("OD") || tr.IndicadorOrdenDirecta.equalsIgnoreCase("OE")) ? true : false;
			String codigoOperadorCorredor = tr.CodigoOperadorCorredor;
			String codigoOperadorContraparte = tr.CodigoOperadorContraparte;
			if (tr.IdTipoOperacion.length() >= 3 && tr.IdTipoOperacion.substring(0,3).equalsIgnoreCase("COM")) {
				codigoOperadorCorredor = codigoOperadorString(tr.CodComNuevo);
				codigoOperadorContraparte = considerarOperadorContraparte ? codigoOperadorString(tr.CodVenNuevo) : codigoOperadorContraparte;
			} else if (tr.IdTipoOperacion.length() >= 3 && tr.IdTipoOperacion.substring(0,3).equalsIgnoreCase("VEN")){
				codigoOperadorCorredor = codigoOperadorString(tr.CodVenNuevo);
				codigoOperadorContraparte = considerarOperadorContraparte ? codigoOperadorString(tr.CodComNuevo) : codigoOperadorContraparte;
			}
			Transaccion_Entrada in = new Transaccion_Entrada();
			in.IdMoneda = tr.IdMoneda;
			in.IdMonedaEmision = tr.IdMonedaEmision;
			in.IdInterno = tr.IdTransaccion;
			in.FolioTransaccion = tr.Folio;
			in.FechaCreacion = tr.FechaSistemaCreacion.toString();
			in.HoraCreacion = tr.HoraSistemaCreacion.toString();
			in.FechaEmision = tr.FechaEmision.toString();
			in.Plazo = tr.Plazo;
			in.EsCuentaPropia = tr.EsCuentaPropia;
			in.EsFungible = tr.EsFungible;
			in.EsVentaCorta = tr.EsVentaCorta;
			in.CodigoOperadorContraparte = codigoOperadorContraparte;
			in.CodigoOperadorCorredor = codigoOperadorCorredor;
			in.IdSistemaBursatil = tr.IdSistemaBursatil;
			in.IdEmisor = tr.IdEmisor;
			in.Cantidad = tr.Cantidad;
			in.Monto = tr.Monto;
			in.Precio = tr.Precio;
			in.Tasa = tr.Tasa;
			in.MontoPlazo = BigDecimal.ZERO;
			in.PrecioPlazo = BigDecimal.ZERO;
			in.ValorRescate = tr.ValorRescate;
			in.IdTipoReajuste = tr.IdTipoReajuste;
			in.IdEmpleado = tr.IdEmpleado;
			in.IdTipoOperacion = tr.IdTipoOperacion;
			in.IdCondicionDeLiquidacionContado = tr.IdCondicionDeLiquidacion;
			in.FechaLiquidacionContado = tr.FechaLiquidacion.toString();
			in.FechaVencimiento = tr.FechaVencimiento.toString();
			in.IdInstrumento = tr.IdInstrumento;
			in.IdCorredoraContraparte = tr.IdCorredorContraparte;
			in.IdBolsa = tr.IdBolsa;
			in.IdBoveda = tr.IdBoveda;
			in.IdLibroOrdenes = tr.IdLibroOrdenes;
			in.IdInstrumentoSubyacente = tr.IdInstrumentoSubyacente;
			in.EsFueraRueda = tr.EsFueraRueda;
			in.IndicadorDCV = tr.IndicadorDCV;
			in.EsBilateral = tr.EsBilateral;
			in.MarcaNegociacion = tr.MarcaNegociacion;
			in.TipoCambioCorresponsal = tr.TipoCambioCorresponsal;
			in.IdCorredoraCorresponsal = tr.IdCorredorCorresponsal;
			in.MontoLiquidacionCorresponsal = tr.MontoLiquidacionCorresponsal;
			in.IdMonedaLiquidacionCorresponsal = tr.IdMonedaLiquidacionCorresponsal;
			in.FolioTransaccionPlazo = "0";
			in.MejorPrecioCompra = tr.MejorPrecioCompra;
			in.MejorPrecioVenta = tr.MejorPrecioVenta;
			in.NemotecnicoEspecificoBolson = tr.Nemotecnico;
			in.IndicadorOrdenDirecta = tr.IndicadorOrdenDirecta;
			in.Intereses = tr.Intereses;
			in.Reajustes = tr.Reajustes;
			in.GastosCorresponsal = tr.GastosCorresponsal;
			entrada.p_Transacciones.add(in);
			if (tr.IndicadorOrdenDirecta.equalsIgnoreCase("OD") || tr.IndicadorOrdenDirecta.equalsIgnoreCase("OE")) {
				setTransaccionesOD.add(tr.Folio+tr.FechaSistemaCreacion);
			}
		}
		CrearTransacciones crearTransacciones = new CrearTransacciones();
		CrearTransacciones_Salida salida = crearTransacciones.logica(con, entrada);
		logger.info("FIN::JOB::crearTransaccion");
		return salida;
	}
	
	private CrearOrdenesPostreras_Salida crearOrdenPostrera(Connection conn, List<OrdenPostrera_Entrada> asignacionesCreadasAnteriormente , Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::JOB::crearOrdenPostrera");
		CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
		entradaCrearOrdenesPostreras.p_IdEmpresa = idEmpresa;
		entradaCrearOrdenesPostreras.p_IdModulo = "ORD";
		entradaCrearOrdenesPostreras.p_IdUsuario = idUsuario;
		entradaCrearOrdenesPostreras.p_Ordenes = asignacionesCreadasAnteriormente;
		CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
		CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
		logger.info("FIN::JOB::crearOrdenPostrera");
		return salidaCrearOrdenesPostreras;
	}
	
	private String codigoOperadorString(BigDecimal cod) {
		String codigo = "";
		if (cod != null) {
			String codString = cod.toString();
			int cerosAdicionales = codString.length() < 3 ? (3 - codString.length()) : 0;
			for (int i = 0; i<cerosAdicionales ; i++) codigo = codigo +"0";
			codigo = codigo + codString;
		}
		return codigo;
	}
	
	private String codigoOperadorString(String cod) {
		String codigo = "";
		if (cod != null) {
			int cerosAdicionales = cod.length() < 3 ? (3 - cod.length()) : 0;
			for (int i = 0; i<cerosAdicionales ; i++) codigo = codigo +"0";
			codigo = codigo + cod;
		}
		return codigo;
	}
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
            ControlMercadoModificacionPalosRuebas demo = new ControlMercadoModificacionPalosRuebas();
            try {
            	boolean debug = true;
            	demo.logica(con,debug);
            	con.commit();
//                con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}