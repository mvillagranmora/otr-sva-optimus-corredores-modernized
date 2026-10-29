package cl.bcs.ordenes.jobs;

import java.io.Console;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearOrdenesPostreras;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.ordenes.CrearTransacciones.Transaccion_Proceso;
import cl.bcs.ordenes.EnviarAsignacionesFacturar;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.ordenes.ReservarLiberarTransacciones;
import cl.bcs.ordenes.ReservarLiberarTransacciones.ReservarLiberarTransacciones_Entrada;
import cl.bcs.ordenes.ReservarLiberarTransacciones.ReservarLiberarTransacciones_Salida;
import cl.bcs.ordenes.ReservarLiberarTransacciones.Transaccion_Entrada;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pla.AgregarLog; // para generar logs 

public class ProcesamientoTransaccionesOperadores extends BcsJob implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ProcesamientoTransaccionesOperadores.class);
	
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_ObtenerMensajesSinProcesar.sql");
	private final String SQL_CONSULTAR_CONFIGURACION = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_ConsultarConfiguracionOperador.sql");
	private final String SQL_CONSULTAR_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_ConsultarIndicadoresPortafolio.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_CONSULTAR_PREFERENCIAS_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_ConsultarPreferenciasPagoPortafolio.sql");
	private final String SQL_SELECT_CONFIGURACION_ASIGNACION_DIRECTA = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionAsignacionDirecta.sql");
	private final String SQL_CONSULTAR_DATOS_CLIENTE = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_ConsultarDatosCliente.sql");
	private final String SQL_SELECT_CONFIGURACION_SHORTNAME = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_SelectConfiguracionShortName.sql");
	private final String SQL_SELECT_SHORTNAMES = loadResourceToString("cl/bcs/ordenes/jobs/sql/ProcesamientoTransaccionesOperadores_SelectShortNames.sql");
	
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		go(false, conn);
		return true;
	}
	
	public static class Transaccion_Proceso {
		public Long IdInterno;
		public String _TipoMensaje;
		public String IdMensaje1;
		public String IdMensaje2;
		public Long IdTransaccion;
		public String FolioTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String EsVentaCorta;
		public String CodigoOperadorCorredor;
		public String IdMonedaTransaccion;
		public String EsAPlazo;
		public String IdTipoOperacion;
		
		public List<Long> asignaciones;
		public HashMap<Long,Long> movimientos;		//LLAVE ID ASIGNACION - ID MOVIMIENTO COMPROBANTE 
		public List<MensajeSalida> mensajes;
		public ControlMercado registroControlMercado;
		public String MarcaNegociacion;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		
		public String IdTipoInstrumentoSubyacente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Transaccion_Proceso() {
			asignaciones = new ArrayList<Long>();
			movimientos = new HashMap<>();
			mensajes = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class ConfiguracionOperador {
		public String CodigoOperador;
		public String UtilizaPortafolio;
		public Long IdPortafolio;
		public String ReservaTransaccion;
		public String IdTipoInstrumento;
		public String AsignacionAuto;
		public String MovimientoFacturarAuto;
		public String ComprobanteAuto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorPortafolio {
		public Long IdPortafolio;
		public String IdLibroOrdenes;
		public String IdDireccionNotificacion;
		public String OperaCustodiaRV;
		public String OperaCustodiaRFIF;
		public String PermiteVenderNominalRV;
		public String PermiteVenderNominalRFIF;
		public String OperaCuentaInversion;
		public String OperaLineaSobregiro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreferenciaPagoPortafolio {
		public Long IdPortafolio;
		public Long IdPreferenciaPagoPortafolio;
		public String IdTipoOperacion;
	    public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Usuario {
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionAsignacionDirecta {
		public String IdTipoInstrumento;
		public String AsignacionAuto;
		public String MovimientoFacturarAuto;
		public String ComprobanteAuto;
		public Long IdPortafolio;
	
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	public static class ConfiguracionShortName {
		public String ShortName;
		public String IdTipoInstrumento;
		public String AsignacionAuto;
		public String MovimientoFacturarAuto;
		public String ComprobanteAuto;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
	
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ClienteAsignacionDirecta {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdPortafolio;
		public String IdTipoInstrumento;

		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	public static class ClienteShortName {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdPortafolio;
		public String IdTipoInstrumento;
		public String ShortName;

		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public void go(boolean debugMode, Connection conn) throws InterruptedException, Throwable {
		logger.info("INICIANDO JOB");
			try {
			SQL _sql = new SQL();
			
			Long idEmpresa = null;
			
			if (debugMode) {
				idEmpresa = 1L;
			}
			else {
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			}
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String PermitePalosNegociacionReservado = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FACTURACION", "PALOS_NEGOCIACION_RESERVADO");
			
			HashMap<String,Object> paramsUser = new HashMap<String,Object>();
			paramsUser.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, paramsUser, Usuario.class);        
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
			
			String idBovedaDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "ORDENES", "IdBovedaDefecto");
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn,idEmpresa);
			List<Transaccion_Proceso> transacciones = _sql.query(conn, SQL_OBTENER_MENSAJES_SIN_PROCESAR, new HashMap<String, Object>(), Transaccion_Proceso.class);	    
			
			if (transacciones.size() == 0) {
				logger.debug("No hay transacciones para procesar, finalizando proceso");
				return;
			}
			
			String key = transacciones.get(0)._TipoMensaje;
			transacciones = transacciones.stream().filter(x -> x._TipoMensaje.equalsIgnoreCase(key)).collect(Collectors.toList());
			
			int tamanoLote = 1;
			try {
				tamanoLote = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "ORDENES", "TamanoLoteAsignacionXMarca"));
			}
			catch(Exception e) {}
					
			if (tamanoLote < transacciones.size() ) {
				transacciones = transacciones.subList(0, tamanoLote);	
			}
			
			logger.debug(transacciones.size() + " transacciones de tipo " + key + " para procesar.");
			for (Transaccion_Proceso tran : transacciones) {
				logger.debug(tran.toString());
			}
			
			//Consulta Configuracion caso operador
			List<String> codigosOperador = new ArrayList<>();
			for (Transaccion_Proceso tran : transacciones) {
				if (!codigosOperador.contains(tran.CodigoOperadorCorredor) && tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR"))
					codigosOperador.add(tran.CodigoOperadorCorredor);
			}
			HashMap<String, Object> params1 = new HashMap<String, Object>();
			params1.put("IdEmpresa", idEmpresa);
			params1.put("codigosOperador",codigosOperador);
			List<ConfiguracionOperador> configuracionOperador = _sql.query(conn, SQL_CONSULTAR_CONFIGURACION, params1, ConfiguracionOperador.class);
			
			//Consulta configuracion caso asignacion directa
			List<ConfiguracionAsignacionDirecta> configuracionesAsigDirecta = new ArrayList<>(); 
			params1.clear();
			params1.put("IdEmpresa", idEmpresa);
			configuracionesAsigDirecta = _sql.query(conn, SQL_SELECT_CONFIGURACION_ASIGNACION_DIRECTA, params1, ConfiguracionAsignacionDirecta.class);
			
			//Consulta configuracion caso shortname
			List<ConfiguracionShortName> configuracionesShortName = new ArrayList<>(); 
			params1.clear();
			
			configuracionesShortName = _sql.query(conn, SQL_SELECT_CONFIGURACION_SHORTNAME, params1, ConfiguracionShortName.class);
			
			//Obtiene indicadores portafolio caso transaccion operador
			HashMap<Long,IndicadorPortafolio> indicadores = new HashMap<Long,IndicadorPortafolio>();
			List<PreferenciaPagoPortafolio> _preferenciasPagoPortafolio = new ArrayList<PreferenciaPagoPortafolio>();
			List<Long> idsPortafolio = new ArrayList<Long>();
			if (configuracionOperador.size() > 0) {
				{
					for (ConfiguracionOperador conf : configuracionOperador) {
						if (conf.IdPortafolio != null && conf.IdPortafolio > 0 && !idsPortafolio.contains(conf.IdPortafolio))
							idsPortafolio.add(conf.IdPortafolio);
					}	
				}
			}
			//Obtiene indicadores portafolio caso transaccion shortname  
			List<ClienteShortName> lstClienteShortName = new ArrayList<ClienteShortName>();
			List<ConfiguracionShortName> configuracionCompletaShortName = new ArrayList<ConfiguracionShortName>();
			
			if (configuracionesShortName.size() > 0) {
				HashMap<String, Object> params2 = new HashMap<>();
				List<ClienteShortName> datosCliente = _sql.query(conn, SQL_SELECT_SHORTNAMES, params2, ClienteShortName.class);
				
				for (Transaccion_Proceso tran : transacciones) {
					for (ClienteShortName shortnames : datosCliente) {
						if(tran.MarcaNegociacion.equalsIgnoreCase(shortnames.ShortName) && tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
							
							//ACA
							if (tran.IdTipoInstrumento.equalsIgnoreCase("SIM")) {
								shortnames.IdTipoInstrumento = tran.IdTipoInstrumentoSubyacente;
							}
							else {
								shortnames.IdTipoInstrumento = tran.IdTipoInstrumento;
							}
							
							lstClienteShortName.add(shortnames);
							tran.IdPortafolio = shortnames.IdPortafolio;
							tran.DocumentoIdentidad= shortnames.DocumentoIdentidad;
						}
					}
				}
				for (ClienteShortName dato : lstClienteShortName) {
					
					ConfiguracionShortName objConfiguracionShortName = new ConfiguracionShortName();
					objConfiguracionShortName.IdTipoInstrumento = dato.IdTipoInstrumento;
					objConfiguracionShortName.IdPortafolio = dato.IdPortafolio; 
				
					for (ConfiguracionShortName datoAnt : configuracionesShortName) {
						if (dato.IdTipoInstrumento.equalsIgnoreCase(datoAnt.IdTipoInstrumento) && (dato.ShortName.equalsIgnoreCase(datoAnt.ShortName))) {
							objConfiguracionShortName.ShortName = datoAnt.ShortName;
							objConfiguracionShortName.AsignacionAuto = datoAnt.AsignacionAuto;
							objConfiguracionShortName.MovimientoFacturarAuto = datoAnt.MovimientoFacturarAuto; 
							objConfiguracionShortName.ComprobanteAuto = datoAnt.ComprobanteAuto;

							configuracionCompletaShortName.add(objConfiguracionShortName);
							
							if (datoAnt.IdPortafolio != null && datoAnt.IdPortafolio > 0 && !idsPortafolio.contains(datoAnt.IdPortafolio)) {
								idsPortafolio.add(datoAnt.IdPortafolio);
							}
						}
					}
				
				}
			}
			
			//Obtiene indicadores portafolio caso transaccion asignacion directa  
			List<ClienteAsignacionDirecta> lstClienteAsignacionDirecta = new ArrayList<ClienteAsignacionDirecta>();
			List<ClienteAsignacionDirecta> datosClienteAsignacionDirecta = new ArrayList<ClienteAsignacionDirecta>();
			List<ConfiguracionAsignacionDirecta> configuracionCompletaAsignDirecta = new ArrayList<ConfiguracionAsignacionDirecta>();
			
			String regex = "\\d+-(?:[0-9]|K);[a-zA-Z0-9'’\\-|]+"; // formato marca negociacion ejemplo : 2345-K;1    o    1234-6;2 
			Pattern pattern = Pattern.compile(regex);
			String regex2 = "\\d+-(?:[0-9]|K|k)"; // formato marca negociacion solo rut ejemplo : 2345-K    o    1234-6
			Pattern pattern2 = Pattern.compile(regex2);
			
			if (configuracionesAsigDirecta.size() > 0) {
				HashMap<String, Object> params2 = new HashMap<>();
				
				for (Transaccion_Proceso tran : transacciones) {
					
					//Validacion si la transaccion posee Marca de negociacion
					if(tran.MarcaNegociacion != null ) {
						 
						Matcher matcher2 = pattern2.matcher(tran.MarcaNegociacion);
						
						if(matcher2.matches() && tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")){
							tran.MarcaNegociacion = tran.MarcaNegociacion.concat(";0");
						}
						Matcher matcher = pattern.matcher(tran.MarcaNegociacion);
						if (matcher.matches() && tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
							String[] parts = tran.MarcaNegociacion.split(";"); //se divide marca negociacion ejemplo : 1023-5;0 en 1023-5 y 0 
							
							ClienteAsignacionDirecta objClienteAsignacionDirecta = new ClienteAsignacionDirecta();
							
							//Se agrega la marca de negociacion y IdTipoInstrumento en la lista lstClienteAsignacionDirecta
							objClienteAsignacionDirecta.DocumentoIdentidad = parts[0];
							objClienteAsignacionDirecta.CodigoPortafolio = parts[1];
							objClienteAsignacionDirecta.IdTipoInstrumento = tran.IdTipoInstrumento;
							
							lstClienteAsignacionDirecta.add(objClienteAsignacionDirecta);	
						 
						}
					}	
				}
				
				//Consultamos datos del cliente por cada transaccion 
				for (ClienteAsignacionDirecta consultarDatosClientes : lstClienteAsignacionDirecta) {
					params2.clear(); 
					params2.put("DocumentoIdentidad", consultarDatosClientes.DocumentoIdentidad);
				}
				if(!params2.isEmpty()) {
					List<ClienteAsignacionDirecta> datosCliente = _sql.query(conn, SQL_CONSULTAR_DATOS_CLIENTE, params2, ClienteAsignacionDirecta.class);
					for (ClienteAsignacionDirecta consultarDatosClientes : lstClienteAsignacionDirecta) {
						ClienteAsignacionDirecta objClienteAsignacionDirecta = new ClienteAsignacionDirecta();
						for (ClienteAsignacionDirecta datos : datosCliente) {
							if(datos.CodigoPortafolio.equalsIgnoreCase(consultarDatosClientes.CodigoPortafolio) && datos.DocumentoIdentidad.equalsIgnoreCase(consultarDatosClientes.DocumentoIdentidad)) {
								objClienteAsignacionDirecta = datos;
								break;
							}
								
						}
							
						datosClienteAsignacionDirecta.add(objClienteAsignacionDirecta);
					}
					for (ClienteAsignacionDirecta datos : datosClienteAsignacionDirecta) {
						for (Transaccion_Proceso tran : transacciones) {
							String[] parts = tran.MarcaNegociacion.split(";");
							if(datos.CodigoPortafolio.equalsIgnoreCase(parts[1]) && (datos.DocumentoIdentidad.equalsIgnoreCase(parts[0]))){
								tran.IdPortafolio = datos.IdPortafolio;
							}
						}
					}
				}
				
				
					
				//Comparamos los datos del cliente antes de insertar en idsPortafolio 
				for (ClienteAsignacionDirecta dato : lstClienteAsignacionDirecta) {
					for (ClienteAsignacionDirecta datoSql : datosClienteAsignacionDirecta) {
						
						if (dato.DocumentoIdentidad.equalsIgnoreCase(datoSql.DocumentoIdentidad) && 
								!idsPortafolio.contains(datoSql.IdPortafolio)) {
							
				            idsPortafolio.add(datoSql.IdPortafolio); 
				            dato.IdPortafolio = datoSql.IdPortafolio; //se ocupa para crear configuracion completa
				            
				        }	
					}
			    }
				
				//Vamos armando nuestra configuracion completa
				for (ClienteAsignacionDirecta dato : lstClienteAsignacionDirecta) {
					
					ConfiguracionAsignacionDirecta objConfiguracionAsignacionDirecta = new ConfiguracionAsignacionDirecta();
					objConfiguracionAsignacionDirecta.IdTipoInstrumento = dato.IdTipoInstrumento;
					objConfiguracionAsignacionDirecta.IdPortafolio = dato.IdPortafolio; 
				
					for (ConfiguracionAsignacionDirecta datoAnt : configuracionesAsigDirecta) {
						if (dato.IdTipoInstrumento.equalsIgnoreCase(datoAnt.IdTipoInstrumento)) {
							
							objConfiguracionAsignacionDirecta.AsignacionAuto = datoAnt.AsignacionAuto;
							objConfiguracionAsignacionDirecta.MovimientoFacturarAuto = datoAnt.MovimientoFacturarAuto; 
							objConfiguracionAsignacionDirecta.ComprobanteAuto = datoAnt.ComprobanteAuto;

							configuracionCompletaAsignDirecta.add(objConfiguracionAsignacionDirecta);
						}
					}
				
				}
			}
				
					
			//Se obtienen los indicadores y preferencias de portafolio para las transacciones
			if (idsPortafolio.size() > 0) {
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("idsPortafolio", idsPortafolio);
				List<IndicadorPortafolio> _indicadores = _sql.query(conn, SQL_CONSULTAR_INDICADORES_PORTAFOLIO, params, IndicadorPortafolio.class);
				for (IndicadorPortafolio ind : _indicadores) {
					indicadores.put(ind.IdPortafolio, ind);
				}
				_preferenciasPagoPortafolio = _sql.query(conn, SQL_CONSULTAR_PREFERENCIAS_PAGO_PORTAFOLIO, params, PreferenciaPagoPortafolio.class);
				
			}
			
			//RESERVA DE TRANSACCIONES proceso1 , para caso operador, caso asignacion directa y shortname
			
			List<Transaccion_Entrada> transaccionesReservar = new ArrayList<>(); //operador
			List<Transaccion_Entrada> transaccionesReservarAsignacionDirecta = new ArrayList<>(); //asignacion directa
			List<Transaccion_Entrada> transaccionesReservarShortName = new ArrayList<>(); //shortname
			List<Transaccion_Entrada> transaccionesReservarMarcaNegociacion = new ArrayList<>(); //Marca Negociacion
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.EsAPlazo.equalsIgnoreCase(_c.SI)) {
					continue;
				}

				//caso configuracion Operador proceso 1 reservaciones
				for (ConfiguracionOperador conf : configuracionOperador) {
					if (tran.CodigoOperadorCorredor.equalsIgnoreCase(conf.CodigoOperador) &&
							conf.ReservaTransaccion.equalsIgnoreCase(_c.SI) &&
							!tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA") && // no tenga mensaje asignacion directa
							!transaccionesReservar.contains(tran.IdTransaccion)
							) {
						Transaccion_Entrada tx = new Transaccion_Entrada();
						tx.IdTransaccion = tran.IdTransaccion;
						transaccionesReservar.add(tx);
						break;
					}
				}
				
				//caso configuracion Asignacion Directa proceso 1 reservaciones
				if(tran.MarcaNegociacion != null ) {
					Matcher matcher = pattern.matcher(tran.MarcaNegociacion); 
				
					for (ConfiguracionAsignacionDirecta conf : configuracionesAsigDirecta) {
						if (conf.AsignacionAuto.equalsIgnoreCase("S") &&
								matcher.matches() &&
								tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA") &&//ver si falta algun if
								tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&
								!transaccionesReservarAsignacionDirecta.contains(tran.IdTransaccion) &&
								_c.NO.equalsIgnoreCase(PermitePalosNegociacionReservado)
								) {  
							Transaccion_Entrada tx = new Transaccion_Entrada();
							tx.IdTransaccion = tran.IdTransaccion;
							transaccionesReservarAsignacionDirecta.add(tx);
							break;
						}
					}
					if (matcher.matches() &&
							"ASIGNACION_DIRECTA".equalsIgnoreCase(tran._TipoMensaje) &&//ver si falta algun if
							!transaccionesReservarAsignacionDirecta.contains(tran.IdTransaccion) &&
							_c.SI.equalsIgnoreCase(PermitePalosNegociacionReservado)
							) {  
						Transaccion_Entrada tx = new Transaccion_Entrada();
						tx.IdTransaccion = tran.IdTransaccion;
						transaccionesReservarAsignacionDirecta.add(tx);
					}
					//caso configuracion shortname proceso 1 reservaciones
					for (ConfiguracionShortName conf : configuracionCompletaShortName) {
						if ("S".equalsIgnoreCase(conf.AsignacionAuto) &&
								conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion) &&
								"SHORTNAME".equalsIgnoreCase(tran._TipoMensaje) &&//ver si falta algun if
								tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&
								!transaccionesReservarShortName.contains(tran.IdTransaccion) &&
								_c.NO.equalsIgnoreCase(PermitePalosNegociacionReservado)
								) {
							Transaccion_Entrada tx = new Transaccion_Entrada();
							tx.IdTransaccion = tran.IdTransaccion;
							transaccionesReservarShortName.add(tx);
							break;
						}
						if (conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion) &&
								"SHORTNAME".equalsIgnoreCase(tran._TipoMensaje) &&//ver si falta algun if
								tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&
								!transaccionesReservarShortName.contains(tran.IdTransaccion) && 
								_c.SI.equalsIgnoreCase(PermitePalosNegociacionReservado)
								) {
							Transaccion_Entrada tx = new Transaccion_Entrada();
							tx.IdTransaccion = tran.IdTransaccion;
							transaccionesReservarShortName.add(tx);
							break;
						}
					}
					if("S".equalsIgnoreCase(PermitePalosNegociacionReservado) && 
							"TRANSACCION_MARCA_NEGOCIACION".equalsIgnoreCase(tran._TipoMensaje) &&
							!transaccionesReservarMarcaNegociacion.contains(tran.IdTransaccion)) {
						Transaccion_Entrada tx = new Transaccion_Entrada();
						tx.IdTransaccion = tran.IdTransaccion;
						transaccionesReservarMarcaNegociacion.add(tx);				
					}
				}
			}
			
			//caso operador
			if (transaccionesReservar.size() > 0) {
				
				ReservarLiberarTransacciones_Entrada entradaReservarLiberar = new ReservarLiberarTransacciones_Entrada();
				entradaReservarLiberar.p_Accion = "R";
				entradaReservarLiberar.p_IdEmpresa = idEmpresa;
				entradaReservarLiberar.p_IdModulo = _c.ID_MODULO_ORDENES;
				entradaReservarLiberar.p_IdUsuario = idUsuario;
				entradaReservarLiberar.p_Transacciones = transaccionesReservar;
				entradaReservarLiberar.p_Motivo = "RESERVA POR CONFIGURACION DE OPERADOR";

				ReservarLiberarTransacciones serviceReservarLiberar = new ReservarLiberarTransacciones();
				ReservarLiberarTransacciones_Salida salidaReservarLiberar = serviceReservarLiberar.logica(conn, entradaReservarLiberar);
				for (Transaccion_Proceso tran : transacciones) {
					if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) { //Se agrego para evitar problemas cuando vienen multiple transacciones de distinto tipo 
						
						for (MensajeSalida msg : salidaReservarLiberar.P_OUT_MENSAJES) {
							if (tran.IdTransaccion.equals(msg.id) && msg.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_INFORMACION)) {
								tran.mensajes.add(msg);
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Procesado = "S";
								tran.registroControlMercado.Log = "TRANSACCION RESERVADA";
							}
							else {
								tran.mensajes.add(msg);
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log = "ERROR AL RESERVAR TRANSACCION";	
							}
						}
					}
				}
			}
			
			//caso asignacion directa
			if (transaccionesReservarAsignacionDirecta.size() > 0) {
				
				ReservarLiberarTransacciones_Entrada entradaReservarLiberar = new ReservarLiberarTransacciones_Entrada();
				entradaReservarLiberar.p_Accion = "R";
				entradaReservarLiberar.p_IdEmpresa = idEmpresa;
				entradaReservarLiberar.p_IdModulo = _c.ID_MODULO_ORDENES;
				entradaReservarLiberar.p_IdUsuario = idUsuario;
				entradaReservarLiberar.p_Transacciones = transaccionesReservarAsignacionDirecta;
				entradaReservarLiberar.p_Motivo ="RESERVA POR ASIGNACION DIRECTA";
	
				ReservarLiberarTransacciones serviceReservarLiberar = new ReservarLiberarTransacciones();
				ReservarLiberarTransacciones_Salida salidaReservarLiberar = serviceReservarLiberar.logica(conn, entradaReservarLiberar);
				for (Transaccion_Proceso tran : transacciones) {
					if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {//Se agrego para evitar problemas cuando vienen multiples transacciones de distinto tipo 
					
						for (MensajeSalida msg : salidaReservarLiberar.P_OUT_MENSAJES) {
							if (tran.IdTransaccion.equals(msg.id) && msg.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_INFORMACION)) {
								tran.mensajes.add(msg);
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Procesado = "S";
								tran.registroControlMercado.Log = "TRANSACCION RESERVADA";
							}
							else {
								tran.mensajes.add(msg);
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log = "ERROR AL RESERVAR TRANSACCION";	
							}
						}
					}
				}
			}
			//caso shortname
			if (transaccionesReservarShortName.size() > 0) {
				
				ReservarLiberarTransacciones_Entrada entradaReservarLiberar = new ReservarLiberarTransacciones_Entrada();
				entradaReservarLiberar.p_Accion = "R";
				entradaReservarLiberar.p_IdEmpresa = idEmpresa;
				entradaReservarLiberar.p_IdModulo = _c.ID_MODULO_ORDENES;
				entradaReservarLiberar.p_IdUsuario = idUsuario;
				entradaReservarLiberar.p_Transacciones = transaccionesReservarShortName;
				entradaReservarLiberar.p_Motivo ="RESERVA POR SHORTNAME";
	
				ReservarLiberarTransacciones serviceReservarLiberar = new ReservarLiberarTransacciones();
				ReservarLiberarTransacciones_Salida salidaReservarLiberar = serviceReservarLiberar.logica(conn, entradaReservarLiberar);
				for (Transaccion_Proceso tran : transacciones) {
					if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) { 
					
						for (MensajeSalida msg : salidaReservarLiberar.P_OUT_MENSAJES) {
							if (tran.IdTransaccion.equals(msg.id)) {
								if (msg.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_INFORMACION)) {
									tran.mensajes.add(msg);
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Procesado = "S";
									tran.registroControlMercado.Log = "TRANSACCION RESERVADA";
								}
								else {
									tran.mensajes.add(msg);
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Procesado = "E";
									tran.registroControlMercado.Log = "ERROR AL RESERVAR TRANSACCION";	
								}
							}
						}
					}
				}
			}
			
			//caso Marca Negociacion (solo Reserva)
			if (transaccionesReservarMarcaNegociacion.size() > 0 && "S".equalsIgnoreCase(PermitePalosNegociacionReservado)) {
				
				ReservarLiberarTransacciones_Entrada entradaReservarLiberar = new ReservarLiberarTransacciones_Entrada();
				entradaReservarLiberar.p_Accion = "R";
				entradaReservarLiberar.p_IdEmpresa = idEmpresa;
				entradaReservarLiberar.p_IdModulo = _c.ID_MODULO_ORDENES;
				entradaReservarLiberar.p_IdUsuario = idUsuario;
				entradaReservarLiberar.p_Transacciones = transaccionesReservarMarcaNegociacion;
				entradaReservarLiberar.p_Motivo ="RESERVA POR MARCA NEGOCIACION";
	
				ReservarLiberarTransacciones serviceReservarLiberar = new ReservarLiberarTransacciones();
				ReservarLiberarTransacciones_Salida salidaReservarLiberar = serviceReservarLiberar.logica(conn, entradaReservarLiberar);
				for (Transaccion_Proceso tran : transacciones) {
					if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_MARCA_NEGOCIACION")) { 
					
						for (MensajeSalida msg : salidaReservarLiberar.P_OUT_MENSAJES) {
							if (tran.IdTransaccion.equals(msg.id)) {
								if (msg.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_INFORMACION)) {
									tran.mensajes.add(msg);
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Procesado = "S";
									tran.registroControlMercado.Log = "TRANSACCION RESERVADA";
								}
								else {
									tran.mensajes.add(msg);
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Procesado = "E";
									tran.registroControlMercado.Log = "ERROR AL RESERVAR TRANSACCION";	
								}
							}
						}
					}
				}
			}
			
			//GENERACION DE ASIGNACIONES POSTRERAS proceso2 
			List<OrdenPostrera_Entrada> ordenesPostreras = new ArrayList<OrdenPostrera_Entrada>(); //caso operador
			List<OrdenPostrera_Entrada> ordenesPostrerasAsignacionDirecta = new ArrayList<OrdenPostrera_Entrada>(); //caso asignacion directa
			List<OrdenPostrera_Entrada> ordenesPostrerasShortname = new ArrayList<OrdenPostrera_Entrada>(); //caso shortname
			
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.EsAPlazo.equalsIgnoreCase(_c.SI)) {
					if (tran.registroControlMercado == null) {
						tran.registroControlMercado = new ControlMercado();
						tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
						tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
						tran.registroControlMercado.Log = "";
					}
					tran.registroControlMercado.Procesado = "S";
					tran.registroControlMercado.Log+= "REGISTRO OMITIDO, TRANSACCION A PLAZO";
					continue;
				}
				
				//Caso configuracion operador proceso2
				ConfiguracionOperador configuracionEncontrada = null;
				for (ConfiguracionOperador conf : configuracionOperador) {
					if (tran.CodigoOperadorCorredor.equalsIgnoreCase(conf.CodigoOperador) && 
							// !tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA") && //alternativa 
							tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR") && // validar que ese mensaje este en todas las transacciones 
							tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) && 
							tran.EsVentaCorta.equalsIgnoreCase(_c.NO) &&
							conf.IdPortafolio != null &&
							conf.IdPortafolio > 0 &&
							conf.AsignacionAuto.equalsIgnoreCase(_c.SI)) {
						configuracionEncontrada = conf;
						break;
					}
				}
				

				//Caso configuracion asignacion directa proceso2 
				ConfiguracionAsignacionDirecta configuracionAsignacionDirectaEncontrada = null;
				if(tran.MarcaNegociacion != null ) {
					Matcher matcher = pattern.matcher(tran.MarcaNegociacion); 
					
					for (ConfiguracionAsignacionDirecta conf : configuracionCompletaAsignDirecta) {
						if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&
								matcher.matches() && 
								tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA") &&
								tran.EsVentaCorta.equalsIgnoreCase(_c.NO) &&
								conf.IdPortafolio != null &&
								conf.IdPortafolio > 0 &&
								matcher.matches() &&
								conf.AsignacionAuto.equalsIgnoreCase("S")) {
							configuracionAsignacionDirectaEncontrada = conf;
							break;
						}
					}
				}
				
				//caso asignacion directa 
				if (configuracionAsignacionDirectaEncontrada != null) {
					OrdenPostrera_Entrada orden = new OrdenPostrera_Entrada();
					orden.Cantidad = tran.Cantidad;
					orden.EsFacturaInmediata = _c.NO;
					orden.FechaRecepcion = fechaHoraSistema;
					orden.FolioCartera = 0L;
					orden.Glosa = "";
					orden.IdBoveda = idBovedaDefecto;
					if (indicadores.get(configuracionAsignacionDirectaEncontrada.IdPortafolio).OperaCustodiaRV.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
						orden.IdFormaCustodia = "CUS";
					}
					else if  (indicadores.get(configuracionAsignacionDirectaEncontrada.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)){
						orden.IdFormaCustodia = "CUS";
					}
					else if  (indicadores.get(configuracionAsignacionDirectaEncontrada.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						orden.IdFormaCustodia = "CUS";
					}
					else {
						orden.IdFormaCustodia = "NOM";
					}
					
					orden.IdInterno = tran.IdTransaccion;
					orden.IdMonedaLiquidacionOrden = tran.IdMonedaTransaccion;
					orden.IdPortafolio = configuracionAsignacionDirectaEncontrada.IdPortafolio;
					orden.IdTransaccion = tran.IdTransaccion;
					orden.IdPreferenciaPagoPortafolio = null;
					if (indicadores.get(configuracionAsignacionDirectaEncontrada.IdPortafolio).OperaCuentaInversion.equalsIgnoreCase(_c.SI)) {
						orden.PagoCobroCuentaInversion = _c.SI;
					} else {
						orden.PagoCobroCuentaInversion = _c.NO;
						for (PreferenciaPagoPortafolio pref : _preferenciasPagoPortafolio) {
							if (pref.IdPortafolio.compareTo(configuracionAsignacionDirectaEncontrada.IdPortafolio) == 0 &&
									pref.IdTipoOperacion.equalsIgnoreCase(tran.IdTipoOperacion) &&
									pref.IdMoneda.equalsIgnoreCase(tran.IdMonedaTransaccion)) {
								orden.IdPreferenciaPagoPortafolio = pref.IdPreferenciaPagoPortafolio;
								break;
							}
						}
					}
					orden.PlazoVentaCorta = 0L;
					orden.TasaVentaCorta = BigDecimal.ZERO;
					orden.ValorRescate = tran.ValorRescate;
					ordenesPostrerasAsignacionDirecta.add(orden);
					}
				
				//Caso configuracion shortname proceso2 
				ConfiguracionShortName configuracionShortNameEncontrada = null;
				if(tran.MarcaNegociacion != null ) {
					for (ConfiguracionShortName conf : configuracionCompletaShortName) {
						if ((tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) || (tran.IdTipoInstrumentoSubyacente != null && tran.IdTipoInstrumentoSubyacente.equalsIgnoreCase(conf.IdTipoInstrumento))) &&
								conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion) && 
								tran._TipoMensaje.equalsIgnoreCase("SHORTNAME") &&
								tran.EsVentaCorta.equalsIgnoreCase(_c.NO) &&
								conf.IdPortafolio != null &&
								conf.IdPortafolio > 0 &&
								conf.AsignacionAuto.equalsIgnoreCase("S")) {
							configuracionShortNameEncontrada = conf;
							break;
						}
					}
				}
				
				//caso shortname 
				if (configuracionShortNameEncontrada != null) {
					OrdenPostrera_Entrada orden = new OrdenPostrera_Entrada();
					orden.Cantidad = tran.Cantidad;
					orden.EsFacturaInmediata = _c.NO;
					orden.FechaRecepcion = fechaHoraSistema;
					orden.FolioCartera = 0L;
					orden.Glosa = "";
					orden.IdBoveda = idBovedaDefecto;
					if (indicadores.get(configuracionShortNameEncontrada.IdPortafolio).OperaCustodiaRV.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
						orden.IdFormaCustodia = "CUS";
					}
					else if  (indicadores.get(configuracionShortNameEncontrada.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)){
						orden.IdFormaCustodia = "CUS";
					}
					else if  (indicadores.get(configuracionShortNameEncontrada.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						orden.IdFormaCustodia = "CUS";
					}
					else {
						orden.IdFormaCustodia = "NOM";
					}
					
					orden.IdInterno = tran.IdTransaccion;
					orden.IdMonedaLiquidacionOrden = tran.IdMonedaTransaccion;
					orden.IdPortafolio = configuracionShortNameEncontrada.IdPortafolio;
					orden.IdTransaccion = tran.IdTransaccion;
					orden.IdPreferenciaPagoPortafolio = null;
					if (indicadores.get(configuracionShortNameEncontrada.IdPortafolio).OperaCuentaInversion.equalsIgnoreCase(_c.SI)) {
						orden.PagoCobroCuentaInversion = _c.SI;
					} else {
						orden.PagoCobroCuentaInversion = _c.NO;
						for (PreferenciaPagoPortafolio pref : _preferenciasPagoPortafolio) {
							if (pref.IdPortafolio.compareTo(configuracionShortNameEncontrada.IdPortafolio) == 0 &&
									pref.IdTipoOperacion.equalsIgnoreCase(tran.IdTipoOperacion) &&
									pref.IdMoneda.equalsIgnoreCase(tran.IdMonedaTransaccion)) {
								orden.IdPreferenciaPagoPortafolio = pref.IdPreferenciaPagoPortafolio;
								break;
							}
						}
					}
					orden.PlazoVentaCorta = 0L;
					orden.TasaVentaCorta = BigDecimal.ZERO;
					orden.ValorRescate = tran.ValorRescate;
					ordenesPostrerasShortname.add(orden);
					}
				
				//caso operador
				if (configuracionEncontrada != null) {
					OrdenPostrera_Entrada orden = new OrdenPostrera_Entrada();
					orden.Cantidad = tran.Cantidad;
					orden.EsFacturaInmediata = _c.NO;
					orden.FechaRecepcion = fechaHoraSistema;
					orden.FolioCartera = 0L;
					orden.Glosa = "";
					orden.IdBoveda = idBovedaDefecto;
					if (indicadores.get(configuracionEncontrada.IdPortafolio).OperaCustodiaRV.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
						orden.IdFormaCustodia = "CUS";
					}
					else if  (indicadores.get(configuracionEncontrada.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)){
						orden.IdFormaCustodia = "CUS";
					}
					else if  (indicadores.get(configuracionEncontrada.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						orden.IdFormaCustodia = "CUS";
					}
					else {
						orden.IdFormaCustodia = "NOM";
					}
					
					orden.IdInterno = tran.IdTransaccion;
					orden.IdMonedaLiquidacionOrden = tran.IdMonedaTransaccion;
					orden.IdPortafolio = configuracionEncontrada.IdPortafolio;
					orden.IdTransaccion = tran.IdTransaccion;
					orden.IdPreferenciaPagoPortafolio = null;
					if (indicadores.get(configuracionEncontrada.IdPortafolio).OperaCuentaInversion.equalsIgnoreCase(_c.SI)) {
						orden.PagoCobroCuentaInversion = _c.SI;
					} else {
						orden.PagoCobroCuentaInversion = _c.NO;
						for (PreferenciaPagoPortafolio pref : _preferenciasPagoPortafolio) {
							if (pref.IdPortafolio.compareTo(configuracionEncontrada.IdPortafolio) == 0 &&
									pref.IdTipoOperacion.equalsIgnoreCase(tran.IdTipoOperacion) &&
									pref.IdMoneda.equalsIgnoreCase(tran.IdMonedaTransaccion)) {
								orden.IdPreferenciaPagoPortafolio = pref.IdPreferenciaPagoPortafolio;
								break;
							}
						}
					}
					orden.PlazoVentaCorta = 0L;
					orden.TasaVentaCorta = BigDecimal.ZERO;
					orden.ValorRescate = tran.ValorRescate;
					ordenesPostreras.add(orden);
				}				
			}
			
			//caso operador
			if (ordenesPostreras.size() > 0) {
				
				try {
					CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
					entradaCrearOrdenesPostreras.p_IdEmpresa = idEmpresa;
					entradaCrearOrdenesPostreras.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaCrearOrdenesPostreras.p_IdUsuario = idUsuario;
					entradaCrearOrdenesPostreras.p_Ordenes = ordenesPostreras;
					CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
					CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) {
						
							for (Resultado_Salida res : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
								if (tran.IdTransaccion.equals(res.IdInterno)) {
									tran.asignaciones.add(res.IdAsignacion);
								}
							}
							
							for (MensajeSalida mensaje : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
								if(tran.IdTransaccion.equals(mensaje.id)) {
									tran.mensajes.add(mensaje);
								}
							}
						}
					}
					
					
					int asignacionesOk = 0;
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) {
							
							if (tran.asignaciones.size() > 0) {
								String log = "";
								for (Long idAsignacion : tran.asignaciones) {
									log = log + "," + idAsignacion;
									if (tran.asignaciones.indexOf(idAsignacion) != tran.asignaciones.size() -1 )
										log+=" ";
								}
								if (tran.registroControlMercado == null) {
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Log = "";
								}
								tran.registroControlMercado.Procesado = "S";
								asignacionesOk++;
								tran.registroControlMercado.Log+= " Asignaciones generadas:" + log;
							}else {
								String log = "";
								for (MensajeSalida msg : tran.mensajes) {
									log = log + " | " + msg.toString();
								}
								if (tran.registroControlMercado == null) {
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Log = "";
								}
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log+=log;
							}
						}
					}
					
					
					//checkpoint
					if (asignacionesOk > 0) {
						conn.commit();
					}
					else {
						conn.rollback();
					}
				}
				catch(Exception e) {
					logger.error("ERROR AL CREAR ORDENES POSTRERAS",e);
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) {
							
							if (tran.registroControlMercado == null) {
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Log = "";
							}
							tran.registroControlMercado.Procesado = "E";
							tran.registroControlMercado.Log = e.getMessage();
						}
					}
					conn.rollback();
				}
				
			}
			
			//caso asignacion directa
			if (ordenesPostrerasAsignacionDirecta.size() > 0) {
				
				try {
					CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
					entradaCrearOrdenesPostreras.p_IdEmpresa = idEmpresa;
					entradaCrearOrdenesPostreras.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaCrearOrdenesPostreras.p_IdUsuario = idUsuario;
					entradaCrearOrdenesPostreras.p_Ordenes = ordenesPostrerasAsignacionDirecta;
					CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
					CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							for (Resultado_Salida res : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
								if (tran.IdTransaccion.equals(res.IdInterno)) {
									tran.asignaciones.add(res.IdAsignacion);
								}
							}
							
							for (MensajeSalida mensaje : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
								if(tran.IdTransaccion.equals(mensaje.id)) {
									tran.mensajes.add(mensaje);
								}
							}
						}	
					}
					
					
					int asignacionesOk = 0;

					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							if (tran.asignaciones.size() > 0) {
								String log = "";
								for (Long idAsignacion : tran.asignaciones) {
									log = log + "," + idAsignacion;
									if (tran.asignaciones.indexOf(idAsignacion) != tran.asignaciones.size() -1 )
										log+=" ";
								}
								if (tran.registroControlMercado == null) {
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Log = "";
								}
								tran.registroControlMercado.Procesado = "S";
								asignacionesOk++;
								tran.registroControlMercado.Log+= " Asignaciones generadas:" + log;
								
								//LOG CASO EXITO 
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
								
								String[] parts = tran.MarcaNegociacion.split(";");
								
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = idUsuario; //
								log2.IdEmpresa = idEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = idUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = parts[0];
								log2.IdPortafolio = tran.IdPortafolio;
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
								log2.Nemo = "Asignacion Automatica Sin Errores";
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = "Asignacion Automatica";						
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);

						
							}
						}
					}
					//checkpoint
					if (asignacionesOk > 0) {
						conn.commit();
					}
					else {
						conn.rollback();
					}
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							if (tran.asignaciones.size() == 0) {
								String log = "";
								String mensajeLog = "";
								for (MensajeSalida msg : tran.mensajes) {
									log = log + " | " + msg.toString();
								}
								if (!tran.mensajes.isEmpty()) {
									String ultimoMensaje = tran.mensajes.get(tran.mensajes.size() - 1).descripcion;
									mensajeLog = ultimoMensaje;
								}
								if (mensajeLog.length() > 195) {
									mensajeLog = mensajeLog.substring(0, 195);
						        }
								if (tran.registroControlMercado == null) {
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Log = "";
								}
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log+=log;
								
								//LOG CASO ERROR 	
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
								
								String[] parts = tran.MarcaNegociacion.split(";"); 
									
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = idUsuario; //
								log2.IdEmpresa = idEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = idUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = parts[0];
								log2.IdPortafolio = tran.IdPortafolio;
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
								log2.Nemo = mensajeLog;
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = "Error Asignacion Automatica";						
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);

								
							}
						}
					}
					conn.commit();
				}
				catch(Exception e) {
					logger.error("ERROR AL CREAR ORDENES POSTRERAS",e);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							if (tran.registroControlMercado == null) {
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Log = "";
							}
							tran.registroControlMercado.Procesado = "E";
							tran.registroControlMercado.Log = e.getMessage();
						}
					}
					conn.rollback();
				}
				
			}
			
			//caso shortname
			if (ordenesPostrerasShortname.size() > 0) {
				
				try {
					CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
					entradaCrearOrdenesPostreras.p_IdEmpresa = idEmpresa;
					entradaCrearOrdenesPostreras.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaCrearOrdenesPostreras.p_IdUsuario = idUsuario;
					entradaCrearOrdenesPostreras.p_Ordenes = ordenesPostrerasShortname;
					CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
					CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
						
							for (Resultado_Salida res : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
								if (tran.IdTransaccion.equals(res.IdInterno)) {
									tran.asignaciones.add(res.IdAsignacion);
								}
							}
							
							for (MensajeSalida mensaje : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
								if(tran.IdTransaccion.equals(mensaje.id)) {
									tran.mensajes.add(mensaje);
								}
							}
						}	
					}
					
					
					int asignacionesOk = 0;

					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
						
							if (tran.asignaciones.size() > 0) {
								String log = "";
								for (Long idAsignacion : tran.asignaciones) {
									log = log + "," + idAsignacion;
									if (tran.asignaciones.indexOf(idAsignacion) != tran.asignaciones.size() -1 )
										log+=" ";
								}
								if (tran.registroControlMercado == null) {
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Log = "";
								}
								tran.registroControlMercado.Procesado = "S";
								asignacionesOk++;
								tran.registroControlMercado.Log+= " Asignaciones generadas:" + log;
								
								//LOG CASO EXITO 
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
								
								
								
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = idUsuario; //
								log2.IdEmpresa = idEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = idUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = tran.DocumentoIdentidad;
								log2.IdPortafolio = tran.IdPortafolio;
								log2.EstadoPortafolio = "";
								log2.IdTipoRegistro = "ASIGNACION-SHORTNAME";
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
								log2.Nemo = "Asignacion Automatica Sin Errores";
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = "Asignacion Automatica";						
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);

							}
							else {
								//si cae aca es porque no se asigno
								logger.error(tran.IdTransaccion + " " + " Folio " + tran.FolioTransaccion + " no genero orden postrera");
							}
						}
					}
					//checkpoint
					if (asignacionesOk > 0) {
						conn.commit();
					}
					else {
						conn.rollback();
					}
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
							if (tran.asignaciones.size() == 0) {
								String log = "";
								String mensajeLog = "";
								for (MensajeSalida msg : tran.mensajes) {
									log = log + " | " + msg.toString();
								}
								if (!tran.mensajes.isEmpty()) {
									String ultimoMensaje = tran.mensajes.get(tran.mensajes.size() - 1).descripcion;
									mensajeLog = ultimoMensaje;
								}
								if (mensajeLog.length() > 195) {
									mensajeLog = mensajeLog.substring(0, 195);
						        }
								if (tran.registroControlMercado == null) {
									tran.registroControlMercado = new ControlMercado();
									tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
									tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
									tran.registroControlMercado.Log = "";
								}
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log+=log;
								
								//LOG CASO ERROR 	
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
								 
									
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = idUsuario; //
								log2.IdEmpresa = idEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = idUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = tran.DocumentoIdentidad;
								log2.IdPortafolio = tran.IdPortafolio;
								log2.EstadoPortafolio = "";
								log2.IdTipoRegistro = "ASIGNACION-SHORTNAME";
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
								log2.Nemo = mensajeLog;
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = "Error Asignacion Automatica";						
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);

								
							
							}
						}
					}
					conn.commit();
				}
				catch(Exception e) {
					logger.error("ERROR AL CREAR ORDENES POSTRERAS",e);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
						
							if (tran.registroControlMercado == null) {
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Log = "Error al crear ordenes postreras";
							}
							tran.registroControlMercado.Procesado = "E";
							tran.registroControlMercado.Log = e.getMessage();
						}
					}
					conn.commit();
				}
				
			}
			//****************************************************************************************/
			
			
			
			//************************************************************
			
			//GENERACION DE MOVIMIENTOS Y COMPROBANTES proceso 3 
			List<Asignacion_Entrada> movimientos = new ArrayList<Asignacion_Entrada>(); //caso operador
			List<Asignacion_Entrada> movimientosAsignacionDirecta = new ArrayList<Asignacion_Entrada>(); //caso asignacion directa 
			List<Asignacion_Entrada> movimientosShortname = new ArrayList<Asignacion_Entrada>(); //caso shortname
			
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.EsAPlazo.equalsIgnoreCase(_c.SI)) {
					//parte a plazo de la sim no la procesamos
					continue;
				}
				
				//caso operador 
				for (ConfiguracionOperador conf : configuracionOperador) {
					if (tran.CodigoOperadorCorredor.equalsIgnoreCase(conf.CodigoOperador) && 
							tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) && 
							//!tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA") && // alternativa 
							tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR") &&
							tran.EsVentaCorta.equalsIgnoreCase(_c.NO) &&
							conf.AsignacionAuto.equalsIgnoreCase(_c.SI) &&
							conf.MovimientoFacturarAuto.equalsIgnoreCase(_c.SI) &&
							tran.asignaciones.size() > 0) {
						
						for (Long idAsignacion : tran.asignaciones) {
							Asignacion_Entrada asig = new Asignacion_Entrada();
							if (conf.ComprobanteAuto.equalsIgnoreCase(_c.SI)) {
								asig.GeneraComprobante = _c.SI;
							}
							else {
								asig.GeneraComprobante = _c.NO;
							}
							asig.IdAsignacion = idAsignacion;
							movimientos.add(asig);
						}
					}
				}
				
				//caso asignacion directa
				if(tran.MarcaNegociacion != null ) {
					Matcher matcher = pattern.matcher(tran.MarcaNegociacion); 
					
					for (ConfiguracionAsignacionDirecta conf : configuracionCompletaAsignDirecta) {
						if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
								matcher.matches() &&
								tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA") &&
								tran.EsVentaCorta.equalsIgnoreCase(_c.NO) &&
								conf.AsignacionAuto.equalsIgnoreCase("S") &&
								conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
								tran.asignaciones.size() > 0) { //validar tran.asignaciones
							
							for (Long idAsignacion : tran.asignaciones) {
								Asignacion_Entrada asig = new Asignacion_Entrada();
								if (conf.ComprobanteAuto.equalsIgnoreCase("S")) {
									asig.GeneraComprobante = _c.SI;
								}
								else {
									asig.GeneraComprobante = _c.NO;
								}
								asig.IdAsignacion = idAsignacion;
								movimientosAsignacionDirecta.add(asig);
							}
						}
					}
				}
				//CASO SHORTNAME
				if(tran.MarcaNegociacion != null ) {
					for (ConfiguracionShortName conf : configuracionCompletaShortName) {
						if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
								conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion) &&
								tran._TipoMensaje.equalsIgnoreCase("SHORTNAME") &&
								tran.EsVentaCorta.equalsIgnoreCase(_c.NO) &&
								conf.AsignacionAuto.equalsIgnoreCase("S") &&
								conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
								tran.asignaciones.size() > 0) { //validar tran.asignaciones
							
							for (Long idAsignacion : tran.asignaciones) {
								Asignacion_Entrada asig = new Asignacion_Entrada();
								if (conf.ComprobanteAuto.equalsIgnoreCase("S")) {
									asig.GeneraComprobante = _c.SI;
								}
								else {
									asig.GeneraComprobante = _c.NO;
								}
								asig.IdAsignacion = idAsignacion;
								movimientosShortname.add(asig);
							}
						}
					}
				}
			}
			
			//caso operador 
			if (movimientos.size() > 0) {
				try {
					EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
					EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
					
					entradaEnviarAsignacionesFacturar.p_Asignaciones = movimientos;
					entradaEnviarAsignacionesFacturar.p_IdEmpresa = idEmpresa;
					entradaEnviarAsignacionesFacturar.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaEnviarAsignacionesFacturar.p_IdUsuario = idUsuario; 
					entradaEnviarAsignacionesFacturar.p_simularOperacion = _c.NO;
					EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);
					
					int movimientosOk = 0;
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) {
						
							for (Resultado res : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
								for (Long idAsignacion : tran.asignaciones) {
									if (idAsignacion.equals(res.IdAsignacion)) {
										movimientosOk++;
										tran.movimientos.put(idAsignacion, res.IdMovimientoComprobante);
										tran.registroControlMercado.Log+= "|IdAsignacion: " + idAsignacion + " - IdMovimientoComprobante:" + res.IdMovimientoComprobante+"| ";
									}
								}
							}
						}
					}
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) {
						
							for (MensajeSalida res : salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES) {
								for (Long idAsignacion : tran.asignaciones) {
									if (idAsignacion.equals(res.id)) {
										tran.mensajes.add(res);
									}
								}
							}
							
							if (tran.movimientos.keySet().size() == 0) {
								tran.registroControlMercado.Procesado = "E";
								String log = "";
								for (MensajeSalida msg : tran.mensajes) {
									log+=msg.toString();
								}
								
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log = log;
							}
						}
					}
					
					if (movimientosOk > 0) {
						conn.commit();
						//conn.rollback();
					}
					else {
						conn.rollback();
					}
				}
				catch (Exception e) {
					logger.error("ERROR AL ENVIAR A FACTURAR",e);
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("TRANSACCION_OPERADOR")) {
						
							if (tran.registroControlMercado == null) {
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Log = "";
							}
							tran.registroControlMercado.Procesado = "E";
							tran.registroControlMercado.Log = e.getMessage();
						}
					}
					conn.rollback();
				}
			}
			
			//**************************/
			
			//caso asignacion directa
			if (movimientosAsignacionDirecta.size() > 0) {
				try {
					EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
					EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
					
					entradaEnviarAsignacionesFacturar.p_Asignaciones = movimientosAsignacionDirecta;
					entradaEnviarAsignacionesFacturar.p_IdEmpresa = idEmpresa;
					entradaEnviarAsignacionesFacturar.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaEnviarAsignacionesFacturar.p_IdUsuario = idUsuario; 
					entradaEnviarAsignacionesFacturar.p_simularOperacion = _c.NO;
					EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);
					
					int movimientosOk = 0;
					 
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							for (Resultado res : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
								for (Long idAsignacion : tran.asignaciones) {
									if (idAsignacion.equals(res.IdAsignacion)) {
										movimientosOk++;
										tran.movimientos.put(idAsignacion, res.IdMovimientoComprobante);
										tran.registroControlMercado.Log+= "|IdAsignacion: " + idAsignacion + " - IdMovimientoComprobante:" + res.IdMovimientoComprobante+"| "; 
										
										
										//LOG CASO EXITO 	
										AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
										
										String[] parts = tran.MarcaNegociacion.split(";");
										String mensajeLog = "";
										String mensajeNemoLog = "";
										
										for (ConfiguracionAsignacionDirecta conf : configuracionCompletaAsignDirecta) {
											if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
													conf.AsignacionAuto.equalsIgnoreCase("S") &&
													conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
													conf.ComprobanteAuto.equalsIgnoreCase("N")) {
													mensajeLog = "Movimiento Facturar Automatico";
													mensajeNemoLog = "Movimiento Facturar Automatico Sin Errores";
											}
											
											if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
													conf.AsignacionAuto.equalsIgnoreCase("S") &&
													conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
													conf.ComprobanteAuto.equalsIgnoreCase("S")) {
													mensajeLog = "Envio a facturar y Comprobante Automatico";
													mensajeNemoLog = "Envio a facturar y Comprobante Automatico Sin Errores";
											}
										}	
									
										AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
										log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
										log2.IdCabeceraLog = tran.IdTransaccion;
										log2.IdTipoLog = "INGRESO";
										log2.IdModulo = "ORD";
										log2.IdUsuario = idUsuario; //
										log2.IdEmpresa = idEmpresa;
										log2.FechaSistemaLog = fechaSistema;
										log2.IdUsuarioAtendido = idUsuario;
										log2.IdCliente = 0L;  
										log2.EstadoCliente = "";
										log2.NombreCliente = "";
										log2.IdTipoDocumentoIdentidad = "";
										log2.IdDocumentoIdentidad = parts[0];
										log2.IdPortafolio = tran.IdPortafolio;
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
										log2.Nemo = mensajeNemoLog;
										log2.IdProceso = new Long(0);
										log2.EstadoProceso = "ING";
										log2.NombreProceso = mensajeLog;								
										
										entradaAgregarLog.p_Log.add(log2);
										
										AgregarLog agregarLogService = new AgregarLog();
										agregarLogService.logica(conn, entradaAgregarLog);
										
									}
								}
							}
						}
					}
					if (movimientosOk > 0) {
						conn.commit();
						//conn.rollback();
					}
					else {
						conn.rollback();
					}
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							for (MensajeSalida res : salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES) {
								for (Long idAsignacion : tran.asignaciones) {
									if (idAsignacion.equals(res.id)) {
										tran.mensajes.add(res);
									}
								}
							}
							
							if (tran.movimientos.keySet().size() == 0) {
								//LOG CASO ERROR
								tran.registroControlMercado.Procesado = "E";
								String log = "";
								String mensajeLog = "";
								String nombreProceso = "";
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
								
								String[] parts = tran.MarcaNegociacion.split(";");
								
								for (ConfiguracionAsignacionDirecta conf : configuracionCompletaAsignDirecta) {
									if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
											conf.AsignacionAuto.equalsIgnoreCase("S") &&
											conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
											conf.ComprobanteAuto.equalsIgnoreCase("N")) {
											nombreProceso = "Error MovFacturar: ";
									}
									
									if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
											conf.AsignacionAuto.equalsIgnoreCase("S") &&
											conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
											conf.ComprobanteAuto.equalsIgnoreCase("S")) {
											nombreProceso = "Error Enviofacturar CompAuto: ";
									}
								}
								
								for (MensajeSalida msg : tran.mensajes) {
									log+=msg.toString();;
								}
								if (!tran.mensajes.isEmpty()) {
									String ultimoMensaje = tran.mensajes.get(tran.mensajes.size() - 1).descripcion;
									mensajeLog = ultimoMensaje;
								}
								if (mensajeLog.length() > 195) {
									mensajeLog = mensajeLog.substring(0, 195);
						        }
								
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = idUsuario; //
								log2.IdEmpresa = idEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = idUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = parts[0];
								log2.IdPortafolio = tran.IdPortafolio;
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
								log2.Nemo = mensajeLog;
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = nombreProceso;								
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log = log;
							}
						}
					}
					conn.commit();
					
				}
				catch (Exception e) {
					logger.error("ERROR AL ENVIAR A FACTURAR",e);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("ASIGNACION_DIRECTA")) {
						
							if (tran.registroControlMercado == null) {
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Log = "";
							}
							tran.registroControlMercado.Procesado = "E";
							tran.registroControlMercado.Log = e.getMessage();
						}
					}
					conn.rollback();
				}
			}
//**************************/
			
			//caso SHORTNAME
			if (movimientosShortname.size() > 0) {
				try {
					EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
					EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
					
					entradaEnviarAsignacionesFacturar.p_Asignaciones = movimientosShortname;
					entradaEnviarAsignacionesFacturar.p_IdEmpresa = idEmpresa;
					entradaEnviarAsignacionesFacturar.p_IdModulo = _c.ID_MODULO_ORDENES;
					entradaEnviarAsignacionesFacturar.p_IdUsuario = idUsuario; 
					entradaEnviarAsignacionesFacturar.p_simularOperacion = _c.NO;
					EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);
					
					int movimientosOk = 0;
					 
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
						
							for (Resultado res : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
								for (Long idAsignacion : tran.asignaciones) {
									if (idAsignacion.equals(res.IdAsignacion)) {
										movimientosOk++;
										tran.movimientos.put(idAsignacion, res.IdMovimientoComprobante);
										tran.registroControlMercado.Log+= "|IdAsignacion: " + idAsignacion + " - IdMovimientoComprobante:" + res.IdMovimientoComprobante+"| "; 
										
										
										//LOG CASO EXITO 	
										AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
										

										String mensajeLog = ""; 
										String mensajeNemoLog = "";

										
										for (ConfiguracionShortName conf : configuracionCompletaShortName) {
											if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) && 
													conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion) &&
													conf.AsignacionAuto.equalsIgnoreCase("S") &&
													conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
													conf.ComprobanteAuto.equalsIgnoreCase("N")) {
													mensajeLog = "Movimiento Facturar Automatico";
													mensajeNemoLog = "Movimiento Facturar Automatico Sin Errores";
											}
											
											if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) && 
													conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion) &&
													conf.AsignacionAuto.equalsIgnoreCase("S") &&
													conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
													conf.ComprobanteAuto.equalsIgnoreCase("S")) {
													mensajeLog = "Envio a facturar y Comprobante Automatico ";
													mensajeNemoLog = "Envio a facturar y Comprobante Automatico Sin Errores";
											}
										}	
									
										AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
										log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
										log2.IdCabeceraLog = tran.IdTransaccion;
										log2.IdTipoLog = "INGRESO";
										log2.IdModulo = "ORD";
										log2.IdUsuario = idUsuario; //
										log2.IdEmpresa = idEmpresa;
										log2.FechaSistemaLog = fechaSistema;
										log2.IdUsuarioAtendido = idUsuario;
										log2.IdCliente = 0L;  
										log2.EstadoCliente = "";
										log2.NombreCliente = "";
										log2.IdTipoDocumentoIdentidad = "";
										log2.IdDocumentoIdentidad = tran.DocumentoIdentidad;
										log2.IdPortafolio = tran.IdPortafolio;
										log2.EstadoPortafolio = "";
										log2.IdTipoRegistro = "ASIGNACION-SHORTNAME";
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
										log2.Nemo = mensajeNemoLog;
										log2.IdProceso = new Long(0);
										log2.EstadoProceso = "ING";
										log2.NombreProceso = mensajeLog;								
										
										entradaAgregarLog.p_Log.add(log2);
										
										AgregarLog agregarLogService = new AgregarLog();
										agregarLogService.logica(conn, entradaAgregarLog);
										
									}
								}
							}
						}
					}
					if (movimientosOk > 0) {
						conn.commit();
						//conn.rollback();
					}
					else {
						conn.rollback();
					}
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
						
							for (MensajeSalida res : salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES) {
								for (Long idAsignacion : tran.asignaciones) {
									if (idAsignacion.equals(res.id)) {
										tran.mensajes.add(res);
									}
								}
							}
							
							if (tran.movimientos.keySet().size() == 0) {
								//LOG CASO ERROR
								tran.registroControlMercado.Procesado = "E";
								String log = "";
								String mensajeLog = "";
								String nombreProceso = "";
								AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
																
								for (ConfiguracionAsignacionDirecta conf : configuracionCompletaAsignDirecta) {
									if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
											conf.AsignacionAuto.equalsIgnoreCase("S") &&
											conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
											conf.ComprobanteAuto.equalsIgnoreCase("N")) {
										nombreProceso = "Error Movimiento Facturar Automatico";
									}
									
									if (tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento) &&  
											conf.AsignacionAuto.equalsIgnoreCase("S") &&
											conf.MovimientoFacturarAuto.equalsIgnoreCase("S") &&
											conf.ComprobanteAuto.equalsIgnoreCase("S")) {
										nombreProceso = "Error Envio a facturar y Comprobante Automatico";
									}
								}
								
								for (MensajeSalida msg : tran.mensajes) {
									log+=msg.toString();
								}
								if (!tran.mensajes.isEmpty()) {
									String ultimoMensaje = tran.mensajes.get(tran.mensajes.size() - 1).descripcion;
									mensajeLog = ultimoMensaje;
								}
								if (mensajeLog.length() > 195) {
									mensajeLog = mensajeLog.substring(0, 195);
						        }
								
								AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
								log2.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
								log2.IdCabeceraLog = tran.IdTransaccion;
								log2.IdTipoLog = "INGRESO";
								log2.IdModulo = "ORD";
								log2.IdUsuario = idUsuario; //
								log2.IdEmpresa = idEmpresa;
								log2.FechaSistemaLog = fechaSistema;
								log2.IdUsuarioAtendido = idUsuario;
								log2.IdCliente = 0L;  
								log2.EstadoCliente = "";
								log2.NombreCliente = "";
								log2.IdTipoDocumentoIdentidad = "";
								log2.IdDocumentoIdentidad = tran.DocumentoIdentidad;
								log2.IdPortafolio = tran.IdPortafolio;
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
								log2.Nemo = mensajeLog;
								log2.IdProceso = new Long(0);
								log2.EstadoProceso = "ING";
								log2.NombreProceso = nombreProceso;								
								
								entradaAgregarLog.p_Log.add(log2);
								
								AgregarLog agregarLogService = new AgregarLog();
								agregarLogService.logica(conn, entradaAgregarLog);
								tran.registroControlMercado.Procesado = "E";
								tran.registroControlMercado.Log = log;
								
							}
						}
					}
					conn.commit();
					
				}
				catch (Exception e) {
					logger.error("ERROR AL ENVIAR A FACTURAR",e);
					
					for (Transaccion_Proceso tran : transacciones) {
						if(tran._TipoMensaje.equalsIgnoreCase("SHORTNAME")) {
						
							if (tran.registroControlMercado == null) {
								tran.registroControlMercado = new ControlMercado();
								tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
								tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
								tran.registroControlMercado.Log = "ERROR AL ENVIAR A FACTURAR";
							}
							tran.registroControlMercado.Procesado = "E";
							tran.registroControlMercado.Log = e.getMessage();
						}
					}
					conn.commit();
				}
			}
			//**************************/
			//Registro en controlMercado 
			for (Transaccion_Proceso tran : transacciones) {
				if (tran.registroControlMercado == null) {
					tran.registroControlMercado = new ControlMercado();
					tran.registroControlMercado.IdMensaje1 = tran.IdMensaje1;
					tran.registroControlMercado.IdMensaje2 = tran.IdMensaje2;
					tran.registroControlMercado.Procesado = "S";
					tran.registroControlMercado.Log = "REGISTRO OMITIDO POR CONFIGURACION";
				}
			}			
			
			List<ControlMercado> registros = new ArrayList<ControlMercado>();
			for (Transaccion_Proceso tran : transacciones) {
				registros.add(tran.registroControlMercado);
			}
			
			if (registros.size() > 0) {
				logger.debug("ACTUALIZANDO CONTROL MERCADO");
				_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, registros, ControlMercado.class);
				logger.debug(registros.toString());
				//conn.rollback();
				conn.commit();
			}
			
			logger.info("FIN JOB");
		}
		catch(Exception e) {
			logger.error("ERROR GENERAL",e);
		}
		return;
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(t.getMessage());  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	
	
	public static void main(String[] args) throws InterruptedException, Throwable{
		
		Connection aa = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
		ProcesamientoTransaccionesOperadores p = new ProcesamientoTransaccionesOperadores();
		/*SQL.exec(aa, "UPDATE \"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\"='N'"
				+ "	WHERE \"TipoMensaje\"='SHORTNAME' AND \"IdMensaje1\"='2025-09-04' AND \"IdMensaje2\"='338287'");*/
		p.go(true, aa);
		//aa.commit();
		aa.rollback();
	}





	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
