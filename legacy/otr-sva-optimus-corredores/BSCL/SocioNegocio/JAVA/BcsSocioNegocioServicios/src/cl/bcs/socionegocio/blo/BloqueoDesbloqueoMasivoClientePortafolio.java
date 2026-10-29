package cl.bcs.socionegocio.blo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class BloqueoDesbloqueoMasivoClientePortafolio implements JavaCall {

	private static final Logger logger 								= LogManager.getLogger(BloqueoDesbloqueoMasivoClientePortafolio.class);
	private final String SQL_SELECT_CLIENTES_PORTAFOLIOS_PROCESAR	= loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoMasivoClientePortafolio_SelectClientePortafolioProcesar.sql");
	private final String SQL_SELECT_TIPO_BLOQUEO					= loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoMasivoClientePortafolio_SelectTipoBloqueo.sql");
	private final String SQL_UPDATE_ESTADO_CLIENTE					= loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoMasivoClientePortafolio_UpdateEstadoCliente.sql");
	private final String SQL_UPDATE_ESTADO_CLIENTE_PORTAFOLIO		= loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoMasivoClientePortafolio_UpdateEstadoClientePortafolio.sql");

	private static final String COMENTARIO_BLOQUEO 		= "BLOQUEO MASIVO";
	private static final String COMENTARIO_DESBLOQUEO 	= "DESBLOQUEO MASIVO";
	private Long ID_CABECERA_LOG  						= 1L;


	public static class EntradaBloqueoDesbloqueoMasivoClientesPortafolios extends BcsPojo {
		
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Accion; 
		public List<RegistroArchivo> p_Registros;
		
		public EntradaBloqueoDesbloqueoMasivoClientesPortafolios () {
			
			p_Registros = new ArrayList<RegistroArchivo>();

		}
			
	}
	
	public static class SalidaBloqueoDesbloqueoMasivoClientesPortafolios extends BcsPojo {
		
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<Operacion>				P_OUT_RESULTADO;
		
		public SalidaBloqueoDesbloqueoMasivoClientesPortafolios() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<Operacion>();
		}
	
	}
	
	public static class Operacion extends BcsPojo {
		
		public String	Id;
		public String	Resultado;
		public String	Mensaje;
		
	}
	
	public static class RegistroArchivo extends BcsPojo {
		
		public String	TipoDocumentoIdentidad;
		public String	DocumentoIdentidad;
		public String	CodigoPortafolio;
		public String	TipoCambioEstadoCliente;
		public String	TipoCambioEstadoPortafolio;
		
		public RegistroArchivo () {
			
			this.DocumentoIdentidad = "";
			this.CodigoPortafolio 	= "";
		}
		
	}
	
	public static class ClientePortafolio extends BcsPojo {
		
		public Long 		IdPersona;
		public String		IdTipoDocumentoIdentidad;
		public String		DocumentoIdentidad;
		public Long 		IdCliente;
		public String		IdEstadoCliente;
		public Long 		IdPortafolio;
		public String		CodigoPortafolio;
		public String 		IdEstadoPortafolio;
		public String 		IdTipoBloqueoCliente;
		public String 		IdTipoDesbloqueoCliente;
		public String 		ComentarioBloqueoCliente;
		public String 		ComentarioDesbloqueoCliente;
		public String 		IdTipoBloqueoPortafolio;
		public String 		IdTipoDesbloqueoPortafolio;
		public String 		ComentarioBloqueoPortafolio;
		public String 		ComentarioDesbloqueoPortafolio;
		public Timestamp 	FechaBloqueoCliente;
		public Timestamp 	FechaDesbloqueoCliente;
		public Timestamp 	FechaBloqueoPortafolio;
		public Timestamp 	FechaDesbloqueoPortafolio;
		
		public ClientePortafolio() {
			this.IdPortafolio = 0L;
		}
		
	}
	
	public static class ListadoActualizar extends BcsPojo {
		
		public List<ActualizarCliente> clientesActualizar;
		public List<ActualizarPortafolioCliente> portafoliosClientesActualizar;
		
		public ListadoActualizar() {
			this.clientesActualizar 			= new ArrayList<ActualizarCliente>(0);
			this.portafoliosClientesActualizar 	= new ArrayList<ActualizarPortafolioCliente>(0);

		}
		
	}
	
	public static class ActualizarCliente extends BcsPojo {
		
		public Long 		IdCliente;
		public String		DocumentoIdentidad;
		public String		IdEstado;
		public String		IdEstadoAnterior;
		public String 		IdTipoBloqueo;
		public String 		IdTipoBloqueoAnterior;
		public String		IdTipoDesbloqueo;
		public String		IdTipoDesbloqueoAnterior;
		public String		ComentarioBloqueo;
		public String		ComentarioBloqueoAnterior;
		public String		ComentarioDesbloqueo;
		public String		ComentarioDesbloqueoAnterior;
		public Timestamp	FechaBloqueo;
		public Timestamp	FechaBloqueoAnterior;
		public Timestamp	FechaDesbloqueo;
		public Timestamp	FechaDesbloqueoAnterior;
		
		public ActualizarCliente() {
			this.IdCliente = 0L;
			this.IdTipoBloqueo = "";
			this.IdTipoDesbloqueo = "";
			this.ComentarioBloqueo = "";
			this.ComentarioDesbloqueo = "";
		}
	}
	
	public static class ActualizarPortafolioCliente extends BcsPojo {
		
		public Long 		IdPortafolio;
		public String		DocumentoIdentidad;
		public String		CodigoPortafolio;
		public String		IdEstado;
		public String		IdEstadoAnterior;
		public String 		IdTipoBloqueo;
		public String 		IdTipoBloqueoAnterior;
		public String		IdTipoDesbloqueo;
		public String		IdTipoDesbloqueoAnterior;
		public String		ComentarioBloqueo;
		public String		ComentarioBloqueoAnterior;
		public String		ComentarioDesbloqueo;
		public String		ComentarioDesbloqueoAnterior;
		public Timestamp	FechaBloqueo;
		public Timestamp	FechaBloqueoAnterior;
		public Timestamp	FechaDesbloqueo;
		public Timestamp	FechaDesbloqueoAnterior;
		
		public ActualizarPortafolioCliente() {
			this.IdPortafolio = 0L;
			this.IdTipoBloqueo = "";
			this.IdTipoDesbloqueo = "";
			this.ComentarioBloqueo = "";
			this.ComentarioDesbloqueo = "";
		}
	}
	
	public static class TipoBloqueoDesbloqueo extends BcsPojo {
		
		public String 	IdTipoBloqueo;
		public String 	NombreTipoBloqueo;
		public Long 	IdEmpresa;
		public String 	IdAccion;
		public String 	IdTipoEntidad;
		public String 	IdEstado;
		
	}
	
	public SalidaBloqueoDesbloqueoMasivoClientesPortafolios logica(Connection con, EntradaBloqueoDesbloqueoMasivoClientesPortafolios in) throws Exception {
		
		logger.info("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.logica");
		
		SalidaBloqueoDesbloqueoMasivoClientesPortafolios out 	= new SalidaBloqueoDesbloqueoMasivoClientesPortafolios();
		RevisarRequisitosBasicos basicos 						= new RevisarRequisitosBasicos();
	    Long inicio 											= System.currentTimeMillis();
		RevisarRequisitosBasicosSalida salidaBasicos 			= basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		
		List<RegistroArchivo> registrosProcesar = in.p_Registros;
		
		try {
			
			List<ClientePortafolio> listadoClientePortafolio 	= obtenerClientesPortafolios(con, in);
			List<TipoBloqueoDesbloqueo> listadoTipoMotivos 		= obtenerTipoBloqueoDesbloqueo(con, in.p_IdEmpresa, in.p_Accion);
			ListadoActualizar registrosPreparadosActualizar 	= validarGenerarRegistrosActualizar(con, in, out, registrosProcesar, listadoClientePortafolio, listadoTipoMotivos);
			
			actualizarClientesPortafolios(con, registrosPreparadosActualizar);
			prepararSalidaYAgregaLog(con, in, registrosPreparadosActualizar, out);
						
		} catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_AL_ACTUALIZAR",null,con,in.p_IdEmpresa));
		}
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::BloqueoDesbloqueoMasivoClientePortafolio.logica", fin - inicio);
		
		return out;
		
	}

	private List<ClientePortafolio> obtenerClientesPortafolios(Connection con, EntradaBloqueoDesbloqueoMasivoClientesPortafolios in) throws SQLException {
		
		logger.info("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.obtenerClientesPortafolios");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>(2);
		
		List<String> listaDocumentoIdentidad = in.p_Registros
												.stream()
												.map(item -> item.DocumentoIdentidad)
												.collect(Collectors.toSet())
												.stream()
												.collect(Collectors.toList());
		
	    parametros.put("IdEmpresa", in.p_IdEmpresa);
	    parametros.put("ListaDocumentoIdentidad", listaDocumentoIdentidad);

		List<ClientePortafolio> datosConsulta = sql.query(con, SQL_SELECT_CLIENTES_PORTAFOLIOS_PROCESAR, parametros, ClientePortafolio.class);
		
		logger.info("FIN::BloqueoDesbloqueoMasivoClientePortafolio.obtenerClientesPortafolios");		

		return datosConsulta;
		
	}
	
	private List<TipoBloqueoDesbloqueo> obtenerTipoBloqueoDesbloqueo(Connection con, Long idEmpresa, String accion) throws SQLException {
		
		logger.info("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.obtenerTipoBloqueoDesbloqueo");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>(1);
		
		parametros.put("IdEmpresa", idEmpresa);
		
		List<TipoBloqueoDesbloqueo> listadoTipoBloqueo = sql.query(con, SQL_SELECT_TIPO_BLOQUEO, parametros, TipoBloqueoDesbloqueo.class);
		
		logger.info("FIN::BloqueoDesbloqueoMasivoClientePortafolio.obtenerTipoBloqueoDesbloqueo");		
		return listadoTipoBloqueo;
	}
	
	private ListadoActualizar validarGenerarRegistrosActualizar(Connection con, EntradaBloqueoDesbloqueoMasivoClientesPortafolios in, SalidaBloqueoDesbloqueoMasivoClientesPortafolios out, 
																List<RegistroArchivo> registrosProcesar, List<ClientePortafolio> listadoClientePortafolio, List<TipoBloqueoDesbloqueo> listadoTipoMotivos) throws Exception {

		logger.info("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.validarGenerarRegistrosActualizar");
		
		List<ClientePortafolio> busquedaCliente 				= new ArrayList<ClientePortafolio>();
		ClientePortafolio busquedaPortafolio 					= new ClientePortafolio();
		ListadoActualizar  listadoActualizar 					= new ListadoActualizar();
		HashMap<String,String> parametrosError 					= new HashMap<String,String>(1);
		Boolean validaClienteDesbloqueo							= true;
		
		for( RegistroArchivo datosClienteArchivo : registrosProcesar ) {
			
			ActualizarCliente clienteActualizar					= new ActualizarCliente();
			parametrosError.clear();
			busquedaCliente = listadoClientePortafolio
								.stream()
								.filter(item -> item.IdTipoDocumentoIdentidad.equalsIgnoreCase(datosClienteArchivo.TipoDocumentoIdentidad) && 
												item.DocumentoIdentidad.equalsIgnoreCase(datosClienteArchivo.DocumentoIdentidad) )
								.collect(Collectors.toList());
			
			if (busquedaCliente.size() > 0 ) {
				
				
				busquedaPortafolio 							= busquedaCliente
																.stream()
																.filter(item -> item.CodigoPortafolio.equalsIgnoreCase(datosClienteArchivo.CodigoPortafolio) )
																.findFirst()
																.orElse(new ClientePortafolio());
				 
				TipoBloqueoDesbloqueo tipoMotivoCliente 	= listadoTipoMotivos
																.stream()
																.filter(item -> item.IdTipoBloqueo.equalsIgnoreCase(datosClienteArchivo.TipoCambioEstadoCliente) )
																.findFirst()
																.orElse(new TipoBloqueoDesbloqueo());
				
				TipoBloqueoDesbloqueo tipoMotivoPortafolio = listadoTipoMotivos
																.stream()
																.filter(item -> item.IdTipoBloqueo.equalsIgnoreCase(datosClienteArchivo.TipoCambioEstadoPortafolio) )
																.findFirst()
																.orElse(new TipoBloqueoDesbloqueo());
				
				validaClienteDesbloqueo = "ACT".equals(in.p_Accion) && in.p_Accion.equals(busquedaPortafolio.IdEstadoCliente) ? false : true;					
				
				if( !("".equals(datosClienteArchivo.CodigoPortafolio)) && busquedaPortafolio.IdPortafolio.compareTo(0L) == 0 ) {
					
					parametrosError.put("DocumentoIdentidad", datosClienteArchivo.DocumentoIdentidad);
					parametrosError.put("CodigoPortafolio", datosClienteArchivo.CodigoPortafolio);
					Operacion operacion 	= new Operacion();
					MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_CLIENTE_PORTAFOLIO_NO_EXISTE",parametrosError,con,in.p_IdEmpresa);
					operacion.Id 			= "CLIENTE";
					operacion.Resultado 	= "UI_ERROR";
					operacion.Mensaje 		= mensaje.descripcion;
					out.P_OUT_RESULTADO.add(operacion);
					continue;
					
				}
				
				
				if( in.p_Accion.equals(busquedaPortafolio.IdEstadoCliente) ) {
					
					if( "BLO".equals(in.p_Accion) ) {
						
						parametrosError.put("DocumentoIdentidad", busquedaPortafolio.DocumentoIdentidad);
						Operacion operacion 	= new Operacion();
						String tipoError 		= "UI_CLIENTE_YA_BLOQUEADO";
						MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
						operacion.Id 			= "CLIENTE";
						operacion.Resultado 	= "UI_ERROR";
						operacion.Mensaje 		= mensaje.descripcion;
						out.P_OUT_RESULTADO.add(operacion);
						continue;

					} else {
						
						parametrosError.put("DocumentoIdentidad", busquedaPortafolio.DocumentoIdentidad);
						Operacion operacion 	= new Operacion();
						String tipoError 		= "UI_CLIENTE_YA_ACTIVO";
						MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,tipoError,parametrosError,con,in.p_IdEmpresa);
						operacion.Id 			= "CLIENTE";
						operacion.Resultado 	= "UI_ADVERTENCIA";
						operacion.Mensaje 		= mensaje.descripcion;
						out.P_OUT_RESULTADO.add(operacion);
						
					}
					
				}
				
				if( in.p_Accion.equals(busquedaPortafolio.IdEstadoPortafolio) ) {
					
					parametrosError.put("DocumentoIdentidad", busquedaPortafolio.DocumentoIdentidad);
					parametrosError.put("CodigoPortafolio", datosClienteArchivo.CodigoPortafolio);
					Operacion operacion 	= new Operacion();
					String tipoError 		= "ACT".equals(in.p_Accion) ? "UI_CLIENTE_PORTAFOLIO_YA_ACTIVO" : "UI_CLIENTE_PORTAFOLIO_YA_BLOQUEADO";
					MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
					operacion.Id 			= "CLIENTE";
					operacion.Resultado 	= "UI_ERROR";
					operacion.Mensaje 		= mensaje.descripcion;
					out.P_OUT_RESULTADO.add(operacion);
					continue;
					
				}
				
				if( tipoMotivoCliente.IdTipoBloqueo == null ) {
					
					if( validaClienteDesbloqueo ) {
						String idTipoBloqueo 	=  datosClienteArchivo.TipoCambioEstadoCliente;
						parametrosError.put("IdTipoBloqueo", idTipoBloqueo);
						Operacion operacion 	= new Operacion();
						String tipoError 		= "UI_TIPO_MOTIVO_BLOQUEO_CLIENTE_NO_EXISTE";
						MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
						operacion.Id 			= "CLIENTE";
						operacion.Resultado 	= "UI_ERROR";
						operacion.Mensaje 		= mensaje.descripcion;
						out.P_OUT_RESULTADO.add(operacion);
						continue;
					}
				}
				
				if( tipoMotivoPortafolio.IdTipoBloqueo == null ) {
					
					String idTipoBloqueo 	= datosClienteArchivo.TipoCambioEstadoPortafolio;
					parametrosError.put("IdTipoBloqueo", idTipoBloqueo);
					Operacion operacion 	= new Operacion();
					String tipoError 		= "UI_TIPO_MOTIVO_BLOQUEO_CLIENTE_PORTAFOLIO_NO_EXISTE";
					MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
					operacion.Id 			= "CLIENTE";
					operacion.Resultado 	= "UI_ERROR";
					operacion.Mensaje 		= mensaje.descripcion;
					out.P_OUT_RESULTADO.add(operacion);
					continue;
						
				}
				
				if( "ACT".equals(in.p_Accion) && !in.p_Accion.equals(tipoMotivoCliente.IdAccion) ) {
					
					if( validaClienteDesbloqueo ) {
						parametrosError.put("IdTipoBloqueo", datosClienteArchivo.TipoCambioEstadoCliente);
						Operacion operacion 	= new Operacion();
						String tipoError 		= "UI_TIPO_DEBLOQUEO_CLIENTE_NO_PERTENECE_TIPO";
						MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
						operacion.Id 			= "CLIENTE";
						operacion.Resultado 	= "UI_ERROR";
						operacion.Mensaje 		= mensaje.descripcion;
						out.P_OUT_RESULTADO.add(operacion);
						continue;
					}
					
				}
				
				if( "BLO".equals(in.p_Accion) && !in.p_Accion.equals(tipoMotivoCliente.IdAccion) ) {
					
					parametrosError.put("IdTipoBloqueo", datosClienteArchivo.TipoCambioEstadoCliente);
					Operacion operacion 	= new Operacion();
					String tipoError 		= "UI_TIPO_BLOQUEO_CLIENTE_NO_PERTENECE_TIPO";
					MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
					operacion.Id 			= "CLIENTE";
					operacion.Resultado 	= "UI_ERROR";
					operacion.Mensaje 		= mensaje.descripcion;
					out.P_OUT_RESULTADO.add(operacion);
					continue;
					
				}
				
				if( !in.p_Accion.equals(tipoMotivoPortafolio.IdAccion) ) { 
					
					parametrosError.put("IdTipoBloqueo", datosClienteArchivo.TipoCambioEstadoPortafolio);
					Operacion operacion 	= new Operacion();
					String tipoError 		= "ACT".equals(in.p_Accion) ? "UI_TIPO_DEBLOQUEO_PORTAFOLIO_NO_PERTENECE_TIPO" : "UI_TIPO_BLOQUEO_PORTAFOLIO_NO_PERTENECE_TIPO";
					MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,tipoError,parametrosError,con,in.p_IdEmpresa);
					operacion.Id 			= "CLIENTE";
					operacion.Resultado 	= "UI_ERROR";
					operacion.Mensaje 		= mensaje.descripcion;
					out.P_OUT_RESULTADO.add(operacion);
					continue;
					
				}
				
			} else {
				
				parametrosError.put("DocumentoIdentidad", datosClienteArchivo.DocumentoIdentidad);
				Operacion operacion 	= new Operacion();
				MensajeSalida mensaje 	= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_BLOQUEO_DESBLOQUEO_DOCUMENTO_IDENTIDAD_NO_EXISTE",parametrosError,con,in.p_IdEmpresa);
				operacion.Id 			= "CLIENTE";
				operacion.Resultado 	= "UI_ERROR";
				operacion.Mensaje 		= mensaje.descripcion;
				out.P_OUT_RESULTADO.add(operacion);
				continue;
				
			}
			
			List<ClientePortafolio> portafoliosAfectados = new ArrayList<ClientePortafolio>();
			if( "".equals(datosClienteArchivo.CodigoPortafolio) ) 
				portafoliosAfectados.addAll(busquedaCliente);
			else
				portafoliosAfectados.add(busquedaPortafolio);
						
			if( validaClienteDesbloqueo ) {
				clienteActualizar = prepararClienteActualizar( con, busquedaCliente, in.p_Accion, datosClienteArchivo );
				listadoActualizar.clientesActualizar.add(clienteActualizar);
			}
			List<ActualizarPortafolioCliente> portafolioClienteActualizar = prepararPortafolioClienteActualizar( con, portafoliosAfectados, in.p_Accion, datosClienteArchivo );
			listadoActualizar.portafoliosClientesActualizar.addAll(portafolioClienteActualizar);


		}
		
		logger.info("FIN::BloqueoDesbloqueoMasivoClientePortafolio.validarGenerarRegistrosActualizar");		
		return listadoActualizar;
	}
	
	private ActualizarCliente prepararClienteActualizar(Connection con, List<ClientePortafolio> clientePortafolio, String estado, RegistroArchivo datosClienteArchivo) throws Exception {
		
		logger.info("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.prepararClienteActualizar");
		
	    Timestamp currentUtcTimestamp 					= FechaDB.ObtenerFechaHoraUTCTs(con);
		ClientePortafolio cliente 						= clientePortafolio.stream().findFirst().orElse(new ClientePortafolio());
		ActualizarCliente clienteActualizar 			= new ActualizarCliente();
		clienteActualizar.IdCliente 					= cliente.IdCliente;
		clienteActualizar.DocumentoIdentidad 			= cliente.DocumentoIdentidad;
		clienteActualizar.IdEstado 						= estado;
		clienteActualizar.IdEstadoAnterior				= cliente.IdEstadoCliente;
		clienteActualizar.IdTipoBloqueo 				= "BLO".equalsIgnoreCase(estado) ? datosClienteArchivo.TipoCambioEstadoCliente : "";
		clienteActualizar.IdTipoBloqueoAnterior 		= cliente.IdTipoBloqueoCliente;
		clienteActualizar.IdTipoDesbloqueo 				= "ACT".equalsIgnoreCase(estado) ? datosClienteArchivo.TipoCambioEstadoCliente : "";
		clienteActualizar.IdTipoDesbloqueoAnterior 		= cliente.IdTipoDesbloqueoCliente;
		clienteActualizar.ComentarioBloqueo 			= "BLO".equalsIgnoreCase(estado) ? COMENTARIO_BLOQUEO : "";
		clienteActualizar.ComentarioBloqueoAnterior 	= cliente.ComentarioBloqueoCliente;
		clienteActualizar.ComentarioDesbloqueo 			= "ACT".equalsIgnoreCase(estado) ? COMENTARIO_DESBLOQUEO : "";
		clienteActualizar.ComentarioDesbloqueoAnterior 	= cliente.ComentarioDesbloqueoCliente;
		clienteActualizar.FechaBloqueo					= "BLO".equalsIgnoreCase(estado) ? currentUtcTimestamp : null;
		clienteActualizar.FechaBloqueoAnterior			= cliente.FechaBloqueoCliente;
		clienteActualizar.FechaDesbloqueo				= "ACT".equalsIgnoreCase(estado) ? currentUtcTimestamp : null;
		clienteActualizar.FechaDesbloqueoAnterior		= cliente.FechaDesbloqueoCliente;
		
		logger.info("FIN::BloqueoDesbloqueoMasivoClientePortafolio.prepararClienteActualizar");
		return clienteActualizar;		

	}
	
	private List<ActualizarPortafolioCliente> prepararPortafolioClienteActualizar(Connection con, List<ClientePortafolio> clientePortafolios, String estado, RegistroArchivo datosClienteArchivo) throws Exception {
		
		logger.info("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.prepararPortafolioClienteActualizar");
		
		List<ActualizarPortafolioCliente> portafoliosActualizados 	= new ArrayList<ActualizarPortafolioCliente>(clientePortafolios.size());
	    Timestamp currentUtcTimestamp 								= FechaDB.ObtenerFechaHoraUTCTs(con);

		
		for( ClientePortafolio clientePortafolio: clientePortafolios) {
			
			ActualizarPortafolioCliente portafolioActualizar 	= new ActualizarPortafolioCliente();
			portafolioActualizar.IdPortafolio 					= clientePortafolio.IdPortafolio;
			portafolioActualizar.DocumentoIdentidad 			= clientePortafolio.DocumentoIdentidad;
			portafolioActualizar.CodigoPortafolio 				= clientePortafolio.CodigoPortafolio;
			portafolioActualizar.IdEstado 						= estado;
			portafolioActualizar.IdEstadoAnterior				= clientePortafolio.IdEstadoPortafolio;
			portafolioActualizar.IdTipoBloqueo 					= "BLO".equalsIgnoreCase(estado) ? datosClienteArchivo.TipoCambioEstadoPortafolio : "";
			portafolioActualizar.IdTipoBloqueoAnterior 			= clientePortafolio.IdTipoBloqueoPortafolio;
			portafolioActualizar.IdTipoDesbloqueo 				= "ACT".equalsIgnoreCase(estado) ? datosClienteArchivo.TipoCambioEstadoPortafolio : "";
			portafolioActualizar.IdTipoDesbloqueoAnterior 		= clientePortafolio.IdTipoDesbloqueoPortafolio;
			portafolioActualizar.ComentarioBloqueo 				= "BLO".equalsIgnoreCase(estado) ? COMENTARIO_BLOQUEO : "";
			portafolioActualizar.ComentarioBloqueoAnterior 		= clientePortafolio.ComentarioBloqueoPortafolio;
			portafolioActualizar.ComentarioDesbloqueo 			= "ACT".equalsIgnoreCase(estado) ? COMENTARIO_DESBLOQUEO : "";
			portafolioActualizar.ComentarioDesbloqueoAnterior 	= clientePortafolio.ComentarioDesbloqueoPortafolio;
			portafolioActualizar.FechaBloqueo					= "BLO".equalsIgnoreCase(estado) ? currentUtcTimestamp : null;
			portafolioActualizar.FechaBloqueoAnterior			= clientePortafolio.FechaBloqueoPortafolio;
			portafolioActualizar.FechaDesbloqueo				= "ACT".equalsIgnoreCase(estado) ? currentUtcTimestamp : null;
			portafolioActualizar.FechaDesbloqueoAnterior		= clientePortafolio.FechaDesbloqueoPortafolio;
			portafoliosActualizados.add(portafolioActualizar);
		}
		
		logger.info("FIN::BloqueoDesbloqueoMasivoClientePortafolio.prepararPortafolioClienteActualizar");		
		return portafoliosActualizados;
	}
	
	private void actualizarClientesPortafolios(Connection con, ListadoActualizar registrosPreparadosActualizar) throws SQLException {
		
		logger.debug("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.actualizarClientesPortafolios");
		SQL sql = new SQL();

		sql.batch(con,SQL_UPDATE_ESTADO_CLIENTE, registrosPreparadosActualizar.clientesActualizar,ActualizarCliente.class);
		sql.batch(con,SQL_UPDATE_ESTADO_CLIENTE_PORTAFOLIO, registrosPreparadosActualizar.portafoliosClientesActualizar,ActualizarPortafolioCliente.class);
		
		logger.debug("FIN::BloqueoDesbloqueoMasivoClientePortafolio.actualizarClientesPortafolios");

	}
	
	private void prepararSalidaYAgregaLog(Connection con, EntradaBloqueoDesbloqueoMasivoClientesPortafolios in, ListadoActualizar registrosPreparadosActualizar, SalidaBloqueoDesbloqueoMasivoClientesPortafolios out) throws Exception {
		
		logger.debug("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.prepararSalidaYAgregaLog");
		
		AgregarLog.InAgregarLog entradaAgregarLogCompleta 	= new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog entradaParcial 				= new AgregarLog.InAgregarLog();
		AgregarLog agregarLogService 						= new AgregarLog();

		for( ActualizarCliente clienteActualizado :registrosPreparadosActualizar.clientesActualizar ) {
			
			Operacion operacion = new Operacion();
			operacion.Id 		= "CLIENTE";
			operacion.Resultado = "UI_EXITO";
			operacion.Mensaje 	= operacion.Id + " " + clienteActualizado.DocumentoIdentidad + ("BLO".equalsIgnoreCase(clienteActualizado.IdEstado) ? " BLOQUEADO." : " DESBLOQUEADO.");
			out.P_OUT_RESULTADO.add(operacion);
			entradaParcial = generarLog(con, in, clienteActualizado, new ActualizarPortafolioCliente());
			entradaAgregarLogCompleta.p_Log.addAll(entradaParcial.p_Log);
			entradaAgregarLogCompleta.p_LogDetalle.addAll(entradaParcial.p_LogDetalle);
		}
			
		for( ActualizarPortafolioCliente portafolioActualizado :registrosPreparadosActualizar.portafoliosClientesActualizar ) {
			
			Operacion operacion = new Operacion();
			operacion.Id 		= "PORTAFOLIO";
			operacion.Resultado = "UI_EXITO";
			operacion.Mensaje 	= portafolioActualizado.DocumentoIdentidad + ": " + "CodigoPortafolio " + portafolioActualizado.CodigoPortafolio + ("BLO".equalsIgnoreCase(portafolioActualizado.IdEstado) ? " BLOQUEADO." : " DESBLOQUEADO.");
			out.P_OUT_RESULTADO.add(operacion);
			entradaParcial = generarLog(con, in, new ActualizarCliente(), portafolioActualizado);
			entradaAgregarLogCompleta.p_Log.addAll(entradaParcial.p_Log);
			entradaAgregarLogCompleta.p_LogDetalle.addAll(entradaParcial.p_LogDetalle);
		}

		agregarLogService.logica(con, entradaAgregarLogCompleta);
		
		logger.debug("FIN::BloqueoDesbloqueoMasivoClientePortafolio.prepararSalidaYAgregaLog");		

	}
	
	
	private InAgregarLog generarLog(Connection con, EntradaBloqueoDesbloqueoMasivoClientesPortafolios in, ActualizarCliente clienteActualizado, ActualizarPortafolioCliente portafolioActualizado) throws Exception {
		
		logger.debug("INICIO::BloqueoDesbloqueoMasivoClientePortafolio.generarLog");
		
		AgregarLog.InAgregarLog entradaAgregarLog 		= new AgregarLog.InAgregarLog();
	    AgregarLog.InAgregarLog.DetalleLog detalleLog 	= new AgregarLog.InAgregarLog.DetalleLog();
		String fechaSistema 							= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String entidadLog 								= clienteActualizado.IdCliente.compareTo(0L) > 0 ? "CLIENTE" : "PORTAFOLIO";
		
	    AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
	    log.IdCabeceraLog 				= ID_CABECERA_LOG;
	    log.IdTipoLog 					= "MODIFICACION";
	    log.IdModulo 					= in.p_IdModulo;
	    log.IdUsuario 					= in.p_IdUsuario;
	    log.IdEmpresa 					= in.p_IdEmpresa;
	    log.FechaSistemaLog 			= fechaSistema;
	    log.IdUsuarioAtendido 			= in.p_IdUsuario;
	    log.IdRegistro 					= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdCliente.toString() : portafolioActualizado.IdPortafolio.toString();
	    log.IdCliente 					= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdCliente : 0L;
	    log.IdPortafolio 				= "PORTAFOLIO".equalsIgnoreCase(entidadLog) ? portafolioActualizado.IdPortafolio : 0L;
	    log.IdTipoRegistro 				= entidadLog;
	    log.EstadoRegistro 				= in.p_Accion;
	    log.IdProceso 					= 0L;
	    log.EstadoProceso 				= in.p_Accion;
	    log.NombreProceso 				= "Bloqueo/Desbloqueo masivo de clientes y portafolios";
	    entradaAgregarLog.p_Log.add(log);
    	    
	    detalleLog.IdTrabajo 		= 0L;
	    detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    detalleLog.SubRegistro 		= entidadLog;
	    detalleLog.CampoRegistro 	= "IdEstado";
	    detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdEstadoAnterior : portafolioActualizado.IdEstadoAnterior;
	    detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdEstado : portafolioActualizado.IdEstado;
	    entradaAgregarLog.p_LogDetalle.add(detalleLog);
	    
	    if( "BLO".equalsIgnoreCase(in.p_Accion) ) {
	    
		    detalleLog 					= new AgregarLog.InAgregarLog.DetalleLog();
	    	detalleLog.IdTrabajo 		= 0L;
	    	detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    	detalleLog.SubRegistro 		= entidadLog;
	    	detalleLog.CampoRegistro 	= "IdTipoBloqueo";
	    	detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdTipoBloqueoAnterior : portafolioActualizado.IdTipoBloqueoAnterior;
	    	detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdTipoBloqueo : portafolioActualizado.IdTipoBloqueo;
		    entradaAgregarLog.p_LogDetalle.add(detalleLog);
		    
	    }
	    
	    if( "ACT".equalsIgnoreCase(in.p_Accion) ) {
	    
	    	detalleLog 					= new AgregarLog.InAgregarLog.DetalleLog();
	    	detalleLog.IdTrabajo 		= 0L;
	    	detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    	detalleLog.SubRegistro 		= entidadLog;
	    	detalleLog.CampoRegistro 	= "IdTipoDesbloqueo";
	    	detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdTipoDesbloqueoAnterior : portafolioActualizado.IdTipoDesbloqueoAnterior;
	    	detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.IdTipoDesbloqueo : portafolioActualizado.IdTipoDesbloqueo;
		    entradaAgregarLog.p_LogDetalle.add(detalleLog);
		    
	    }
	    
	    if( "BLO".equalsIgnoreCase(in.p_Accion) ) {
	    	
		    detalleLog 					= new AgregarLog.InAgregarLog.DetalleLog();
	    	detalleLog.IdTrabajo 		= 0L;
	    	detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    	detalleLog.SubRegistro 		= entidadLog;
	    	detalleLog.CampoRegistro 	= "ComentarioBloqueo";
	    	detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.ComentarioBloqueoAnterior : portafolioActualizado.ComentarioBloqueoAnterior;
	    	detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.ComentarioBloqueo : portafolioActualizado.ComentarioBloqueo;
		    entradaAgregarLog.p_LogDetalle.add(detalleLog);
		    
	    }
	    
	    if( "ACT".equalsIgnoreCase(in.p_Accion) ) {
	    	
	    	detalleLog 					= new AgregarLog.InAgregarLog.DetalleLog();
	    	detalleLog.IdTrabajo 		= 0L;
	    	detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    	detalleLog.SubRegistro 		= entidadLog;
	    	detalleLog.CampoRegistro 	= "ComentarioDesbloqueo";
	    	detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.ComentarioDesbloqueoAnterior : portafolioActualizado.ComentarioDesbloqueoAnterior;
	    	detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.ComentarioDesbloqueo : portafolioActualizado.ComentarioDesbloqueo;
		    entradaAgregarLog.p_LogDetalle.add(detalleLog);
		    
	    }
	    
	    if( "BLO".equalsIgnoreCase(in.p_Accion) ) {
	    
		    detalleLog 					= new AgregarLog.InAgregarLog.DetalleLog();
	    	detalleLog.IdTrabajo 		= 0L;
	    	detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    	detalleLog.SubRegistro 		= entidadLog;
	    	detalleLog.CampoRegistro 	= "FechaBloqueo";
	    	detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.FechaBloqueoAnterior.toString() : portafolioActualizado.FechaBloqueoAnterior.toString();
	    	detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.FechaBloqueo.toString() : portafolioActualizado.FechaBloqueo.toString();
		    entradaAgregarLog.p_LogDetalle.add(detalleLog);
		    
	    }
	    
	    if( "ACT".equalsIgnoreCase(in.p_Accion) ) {
	    	
		    detalleLog 					= new AgregarLog.InAgregarLog.DetalleLog();
	    	detalleLog.IdTrabajo 		= 0L;
	    	detalleLog.IdCabeceraLog 	= ID_CABECERA_LOG;
	    	detalleLog.SubRegistro 		= entidadLog;
	    	detalleLog.CampoRegistro 	= "FechaDesbloqueo";
	    	detalleLog.ValorAntiguo 	= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.FechaDesbloqueoAnterior.toString() : portafolioActualizado.FechaDesbloqueoAnterior.toString();
	    	detalleLog.ValorNuevo 		= "CLIENTE".equalsIgnoreCase(entidadLog) ? clienteActualizado.FechaDesbloqueo.toString() : portafolioActualizado.FechaDesbloqueo.toString();
		    entradaAgregarLog.p_LogDetalle.add(detalleLog);
		    
	    }
	    
		ID_CABECERA_LOG++;
		logger.debug("FIN::BloqueoDesbloqueoMasivoClientePortafolio.generarLog");
		return entradaAgregarLog;
	
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaBloqueoDesbloqueoMasivoClientesPortafolios jin=gson.fromJson(in.toString() ,EntradaBloqueoDesbloqueoMasivoClientesPortafolios.class);
		SalidaBloqueoDesbloqueoMasivoClientesPortafolios out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Accion\":\"ACT\",\"p_Registros\":[{\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"17909783-4\",\"CodigoPortafolio\":\"0\",\"TipoCambioEstadoCliente\":\"DESBACTUALIZADOC\",\"TipoCambioEstadoPortafolio\":\"DESBACTUALIZADOC\"},{\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"14730571-0\",\"CodigoPortafolio\":\"1\",\"TipoCambioEstadoCliente\":\"DESBACTUALIZADOC\",\"TipoCambioEstadoPortafolio\":\"DESBACTUALIZADOC\"}]}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Accion\":\"BLO\",\"p_Registros\":[{\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"17909783-4\",\"CodigoPortafolio\":\"0\",\"TipoCambioEstadoCliente\":\"BFRAUD\",\"TipoCambioEstadoPortafolio\":\"BCOBRANZA\"},{\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"14730571-0\",\"CodigoPortafolio\":\"1\",\"TipoCambioEstadoCliente\":\"BFRAUD\",\"TipoCambioEstadoPortafolio\":\"BCOBRANZA\"}]}";
				EntradaBloqueoDesbloqueoMasivoClientesPortafolios Objin = gson.fromJson(json, EntradaBloqueoDesbloqueoMasivoClientesPortafolios.class);
								
				BloqueoDesbloqueoMasivoClientePortafolio  kk = new BloqueoDesbloqueoMasivoClientePortafolio();
				SalidaBloqueoDesbloqueoMasivoClientesPortafolios Objout = kk.logica(con, Objin);
				System.out.println(Objout.toString());

				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
};