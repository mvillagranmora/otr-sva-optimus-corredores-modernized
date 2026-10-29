package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDireccionElectronicaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearDireccionElectronicaMasivo.class);
	
	private final String SQL_INSERT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDireccionElectronicaMasivo_InsertDireccionElectronica.sql");
	private String SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_UpdateDireccionElectronicaNoPredeterminada.sql");
	private String SQL_UPDATE_DIRECCION_ELECTRONICA_PRINCIPAL_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDireccionElectronicaMasivo_UpdateDireccionElectronicaPrincipalTablaPersona.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_SelectCliente.sql");
	private String SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_SelectIndicadoresModeloTipoCliente.sql");
	private String SQL_UPDATE_INDICADORES= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_UpdateIndicadores.sql");
	
	private final String ID_CONCEPTO = "NUEDIE";
	
	public static class EntradaCrearDireccionElectronicaMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<DireccionElectronica> p_DireccionesElectronica;
		
		public EntradaCrearDireccionElectronicaMasivo() {
			this.p_DireccionesElectronica = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearDireccionElectronicaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearDireccionElectronicaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}
	
	public static class DireccionElectronica extends BcsPojo {
		public Long IdInterno;
		public Long IdDireccionElectronica;
		public String Direccion;
		public String CodigoProposito;
		public String Comentario;
		public String IdEstado;
		public Long IdPersona;
		public String Predeterminado;
		public Long IdUsuarioCreador;
		public String IdEstadoAutorizacion;
	}
	

	public static class Cliente {
		public Long IdCliente;
		public Long IdPersona;
		public String IdTipoCliente;
	}
	
	public static class Indicador{
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public Long IdEntidad;
		public String ValorIndicador;
	}
	public static class Portafolio{
		public Long IdPortafolio;	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearDireccionElectronicaMasivo jin = gson.fromJson(in.toString(), EntradaCrearDireccionElectronicaMasivo.class);
		SalidaCrearDireccionElectronicaMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public SalidaCrearDireccionElectronicaMasivo logica(Connection con, EntradaCrearDireccionElectronicaMasivo entrada) throws Exception {
		logger.info("INICIO::CrearDireccionElectronicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearDireccionElectronicaMasivo out = new SalidaCrearDireccionElectronicaMasivo();
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<DireccionElectronica> direccionesElectronica = obtenerDireccionesElectronica(con, entrada.p_DireccionesElectronica, esOperacionConAutorizacion);
		insertarDireccionesElectronica(con, direccionesElectronica);
		predeterminarDireccionElectronica(con, direccionesElectronica);
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, direccionesElectronica);
		} else {
			agregarLog(con, entrada, direccionesElectronica);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(direccionesElectronica);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDireccionElectronicaMasivo", finServicio - inicioServicio);
		return out;
	}
		
	private List<DireccionElectronica> obtenerDireccionesElectronica(Connection con, List<DireccionElectronica> entradaDireccionesElectronica, Boolean esOperacionConAutorizacion) throws Exception {
		logger.trace("INICIO::CrearDireccionElectronicaMasivo.obtenerDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDireccionesElectronica = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica", Long.valueOf(entradaDireccionesElectronica.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<DireccionElectronica> direccionesElectronica = entradaDireccionesElectronica
				.stream()
				.map(entradaDireccionElectronica->{
					Long idDireccionElectronica = idsDireccionesElectronica.get(index.getAndIncrement());
					DireccionElectronica direccionElectronica = obtenerEntidadDireccionElectronica(entradaDireccionElectronica, idDireccionElectronica, esOperacionConAutorizacion);
					return direccionElectronica;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionElectronicaMasivo.obtenerDireccionesElectronica", fin - inicio);
		return direccionesElectronica;
	}
	
	private DireccionElectronica obtenerEntidadDireccionElectronica(DireccionElectronica entradaDireccionElectronica, Long idDireccionElectronica, Boolean esOperacionConAutorizacion) {
		logger.trace("INICIO::CrearDireccionElectronicaMasivo.obtenerEntidadDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		DireccionElectronica direccionElectronica = new DireccionElectronica();
		direccionElectronica.IdInterno = entradaDireccionElectronica.IdInterno;
		direccionElectronica.IdDireccionElectronica = idDireccionElectronica;
		direccionElectronica.Direccion = entradaDireccionElectronica.Direccion;
		direccionElectronica.CodigoProposito = entradaDireccionElectronica.CodigoProposito;
		direccionElectronica.Comentario = SocioNegocioUtils.obtenerValorNoNulo(entradaDireccionElectronica.Comentario, "");
		direccionElectronica.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaDireccionElectronica.IdEstado, "ACT");
		direccionElectronica.IdPersona = entradaDireccionElectronica.IdPersona;
		direccionElectronica.Predeterminado = entradaDireccionElectronica.Predeterminado;
		direccionElectronica.IdUsuarioCreador = entradaDireccionElectronica.IdUsuarioCreador;
		direccionElectronica.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionElectronicaMasivo.obtenerEntidadDireccionElectronica", fin - inicio);
		
		return direccionElectronica;
	}
	
	private void insertarDireccionesElectronica(Connection con, List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.trace("INICIO::CrearDireccionElectronicaMasivo.insertarDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DIRECCION_ELECTRONICA, direccionesElectronica, DireccionElectronica.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionElectronicaMasivo.insertarDireccionesElectronica", fin - inicio);
	}
	
	private void predeterminarDireccionElectronica(Connection con, List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.trace("INICIO::CrearDireccionElectronicaMasivo.predeterminarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		List<DireccionElectronica> direccionFisicaPredeterminada = direccionesElectronica.stream()
				.filter(dirE->dirE.Predeterminado.equals("S"))
				.collect(Collectors.toList());
		
		if(direccionFisicaPredeterminada.size() > 0 ) {
			SQL sql = new SQL();
			if(direccionFisicaPredeterminada.size()>0) {
				HashMap<String, Object> parametros = new HashMap<String, Object>();
				Long idDireccionElectronicaPredeterminada = direccionFisicaPredeterminada.get(0).IdDireccionElectronica;
				parametros.put("IdDireccionElectronica", idDireccionElectronicaPredeterminada);
				List<Cliente> cliente = sql.query(con, SQL_SELECT_CLIENTE, parametros, Cliente.class);
				if(cliente.size()>0) {
					Long idCliente = cliente.get(0).IdCliente;
					Long idPersona = cliente.get(0).IdPersona;
					SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADA = SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADA.replace("(:IdDireccionElectronica)", idDireccionElectronicaPredeterminada.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADA);
					
					//Actualizar direccion electronica principal en tabla PERSONA
					SQL_UPDATE_DIRECCION_ELECTRONICA_PRINCIPAL_TABLA_PERSONA = SQL_UPDATE_DIRECCION_ELECTRONICA_PRINCIPAL_TABLA_PERSONA.replace("(:IdDireccionElectronica)", idDireccionElectronicaPredeterminada.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_ELECTRONICA_PRINCIPAL_TABLA_PERSONA);
					
					Long idCheckBoxEnvioFacturaCorreo = 24L; //Si envia factura por correo
					Long idCheckBoxEnvioCartolaCorreo = 36L; //Si envia cartola por correo
					
					Long idCampoMailNotificacion = 34L;//email grupo notificacion
					Long idCampoMailOrden =43L;//email grupo ordenes
					Long idCampoMailFactura= 25L;//email grupo facturacion
					Long idCampoMailCartola =37L;//email grupo cartola
					
					List<Long> listaIdTipoIndicador = new ArrayList<>();
					listaIdTipoIndicador.add(idCheckBoxEnvioFacturaCorreo);
					listaIdTipoIndicador.add(idCheckBoxEnvioCartolaCorreo);
					listaIdTipoIndicador.add(idCampoMailNotificacion);
					listaIdTipoIndicador.add(idCampoMailOrden);
					listaIdTipoIndicador.add(idCampoMailFactura);
					listaIdTipoIndicador.add(idCampoMailCartola);
					
					parametros = new HashMap<String, Object>();
					parametros.put("IdCliente",idCliente);
					parametros.put("IdTipoIndicador",listaIdTipoIndicador);
					List<Indicador> indicadores = sql.query(con, SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE, parametros, Indicador.class);
					List<Indicador> indicadoresAActualizar = new ArrayList<Indicador>();
					HashMap<String, Object> hashIndicadoresCheck = new HashMap<String,Object>();
					
					for(Indicador indicador : indicadores) {
						if(idCheckBoxEnvioFacturaCorreo.equals(indicador.IdTipoIndicador) || idCheckBoxEnvioCartolaCorreo.equals(indicador.IdTipoIndicador))
							hashIndicadoresCheck.putIfAbsent(indicador.IdTipoIndicador + "_" + indicador.IdEntidad, indicador);
					}
					
					if(indicadores.size()>0) {
						String idDireccionElectronicaPredeterminadaString = idDireccionElectronicaPredeterminada.toString();
						for(Indicador indicador : indicadores) {
							if(idCheckBoxEnvioFacturaCorreo.equals(indicador.IdTipoIndicador) || idCheckBoxEnvioCartolaCorreo.equals(indicador.IdTipoIndicador))
								continue;
							
							if (idCampoMailFactura.equals(indicador.IdTipoIndicador)) {
								Indicador checkBoxEnvioFacturaCorreo = (Indicador) hashIndicadoresCheck.get(idCheckBoxEnvioFacturaCorreo + "_" + indicador.IdEntidad);
								if (checkBoxEnvioFacturaCorreo != null && "S".equalsIgnoreCase(checkBoxEnvioFacturaCorreo.ValorIndicador)) {
									indicador.ValorIndicador = idDireccionElectronicaPredeterminadaString;
									indicadoresAActualizar.add(indicador);
								}
							} else if (idCampoMailCartola.equals(indicador.IdTipoIndicador)) {
								Indicador checkBoxEnvioCartolaCorreo = (Indicador) hashIndicadoresCheck.get(idCheckBoxEnvioCartolaCorreo + "_" + indicador.IdEntidad);
								if (checkBoxEnvioCartolaCorreo != null && "S".equalsIgnoreCase(checkBoxEnvioCartolaCorreo.ValorIndicador)) {
									indicador.ValorIndicador = idDireccionElectronicaPredeterminadaString;
									indicadoresAActualizar.add(indicador);
								}
							} else {
								indicador.ValorIndicador = idDireccionElectronicaPredeterminadaString;
								indicadoresAActualizar.add(indicador);
							}
						}
					}
					
					if (!indicadoresAActualizar.isEmpty()) {
						sql.batch(con, SQL_UPDATE_INDICADORES, indicadoresAActualizar, Indicador.class);
					}
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionElectronicaMasivo.predeterminarDireccionElectronica", fin - inicio);
	}
	
	private SalidaCrearDireccionElectronicaMasivo crearEventoAutorizacion(Connection con, EntradaCrearDireccionElectronicaMasivo entrada, List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.debug("INICIO::CrearDireccionElectronicaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearDireccionElectronicaMasivo salidaDireccionElectronicaMasiva = new SalidaCrearDireccionElectronicaMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = direccionesElectronica
				.parallelStream()
				.map(direccionElectronica->direccionElectronica.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = direccionesElectronica.stream().map(direccionElectronica->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(direccionElectronica.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(direccionElectronica.IdDireccionElectronica);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = direccionElectronica.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " /Dir: " + direccionElectronica.Direccion;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaDireccionElectronicaMasiva.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaDireccionElectronicaMasiva.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaDireccionElectronicaMasiva.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaDireccionElectronicaMasiva.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaDireccionElectronicaMasiva.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionElectronicaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaDireccionElectronicaMasiva;	
	}
	
	private void agregarLog(Connection con, EntradaCrearDireccionElectronicaMasivo entrada, List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.trace("INICIO::CrearDireccionElectronicaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(DireccionElectronica direccionElectronica : direccionesElectronica) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionElectronica.IdDireccionElectronica, "Direccion", "", direccionElectronica.Direccion));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionElectronica.IdDireccionElectronica, "CodigoProposito", "", direccionElectronica.CodigoProposito));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionElectronica.IdDireccionElectronica, "Comentario", "", direccionElectronica.Comentario));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionElectronica.IdDireccionElectronica, "IdEstado", "", direccionElectronica.IdEstado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionElectronica.IdDireccionElectronica, "Predeterminado", "", direccionElectronica.Predeterminado));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = direccionElectronica.IdDireccionElectronica;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = direccionElectronica.IdPersona;
			log.IdTipoRegistro = "CREACION_DIRECCION_ELECTRONICA";
			log.EstadoRegistro = direccionElectronica.IdEstadoAutorizacion;
			log.EstadoProceso = direccionElectronica.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Direccion Electronica";
			log.IdRegistro = String.valueOf(direccionElectronica.IdDireccionElectronica);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionElectronicaMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<DireccionElectronica> direccionesElectronica){
		logger.trace("INICIO::CrearDireccionElectronicaMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = direccionesElectronica
				.parallelStream()
				.map(direccionElectronica->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = direccionElectronica.IdInterno;
					entidad.IdEntidad = direccionElectronica.IdDireccionElectronica;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionElectronicaMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}

}
