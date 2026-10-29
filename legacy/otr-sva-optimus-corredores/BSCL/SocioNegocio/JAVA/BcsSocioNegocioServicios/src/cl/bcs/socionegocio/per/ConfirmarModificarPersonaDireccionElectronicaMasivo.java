package cl.bcs.socionegocio.per;

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
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.Cliente;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.Indicador;
import cl.bcs.socionegocio.per.pojo.DireccionElectronica;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaDireccionElectronicaMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona {
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaDireccionElectronicaMasivo.class);
	
	private String SQL_UPDATE_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_UpdateDireccionElectronica.sql");
	private String SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_UpdateDireccionElectronicaNoPredeterminada.sql");
	private String SQL_UPDATE_DIRECCION_ELECTRONICA_PRINCIPAL_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_UpdateDireccionElectronicaPrincipalTablaPersona.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_SelectCliente.sql");
	private String SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_SelectIndicadoresModeloTipoCliente.sql");
	private String SQL_UPDATE_INDICADORES= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionElectronicaMasivo_UpdateIndicadores.sql");
	
	private final String TIPO_LOG = "MODIFICACION";
	private final String ID_TIPO_ENTIDAD = "DIRECCIONELECTRONICA";
	private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_MODIFICAR_DIRECCION_ELECTRONICA";
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
		SalidaConfirmarEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDireccionElectronicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();
		
		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_PERSONA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<DireccionElectronica> direccionesElectronica = obtenerEntidadesDireccionElectronica(solicitudesCambios);
		
		modificarDireccionElectronica(con, direccionesElectronica);
		predeterminarDireccionElectronica(con, direccionesElectronica);
		
		out = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		
		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada,solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaDireccionElectronicaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDireccionElectronicaMasivo.obtenerSolicitudesDeCambios");
		long inicio = System.currentTimeMillis();

		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaDireccionElectronicaMasivo.obtenerSolicitudesDeCambios", fin - inicio);
		
		return solicitudesCambios;
	}
	
	private List<DireccionElectronica> obtenerEntidadesDireccionElectronica(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDireccionElectronicaMasivo.obtenerEntidadesDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<DireccionElectronica> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> direccionElectronica : solicitudesPorIdEntidad.entrySet()) {
			DireccionElectronica direccionElectronicaModificar = new DireccionElectronica();
			List<SolicitudCambio> cambiosPersona = direccionElectronica.getValue();
			direccionElectronicaModificar.IdDireccionElectronica = direccionElectronica.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosPersona) {
				direccionElectronicaModificar = ValorAntiguo.asignarValorNuevo(direccionElectronicaModificar, solicitudCambio.CampoModificado, solicitudCambio.ValorNuevo);
			}
			datosAActualizar.add(direccionElectronicaModificar);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDireccionElectronicaMasivo.obtenerEntidadesDireccionElectronica", fin - inicio);
		return datosAActualizar;
	}
	
	private void modificarDireccionElectronica(Connection con, List<DireccionElectronica> direccionesFisica) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDireccionElectronicaMasivo.modificarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		if( direccionesFisica.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_DIRECCION_ELECTRONICA, direccionesFisica, DireccionElectronica.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDireccionElectronicaMasivo.modificarDireccionElectronica", fin - inicio);
	}
	
	private void predeterminarDireccionElectronica(Connection con, List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.trace("INICIO::ConfirmarModificarPersonaDireccionElectronicaMasivo.predeterminarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		boolean predeterminadoNulo = false;
		for(DireccionElectronica dire:direccionesElectronica) {
			if(dire.Predeterminado == null)predeterminadoNulo=true;
		}
		if(!predeterminadoNulo) {
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
		logger.trace("FIN::ConfirmarModificarPersonaDireccionElectronicaMasivo.predeterminarDireccionElectronica", fin - inicio);
	}
	
	private SalidaConfirmarEjecutarCambiosPersona modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDireccionElectronicaMasivo.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();
		ModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio = new ModificarEstadoRegistroSolicitudCambio();
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificarEstadoRegistroSolicitudCambio = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdSolicitudCambio = solicitudesCambios.stream().findFirst().get().IdSolicitudCambio;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio.logica(con, entradaModificarEstadoRegistroSolicitudCambio);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDireccionElectronicaMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return out;
    }
}


