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
import cl.bcs.socionegocio.per.pojo.Telefono;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaTelefonoMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona {
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaTelefonoMasivo.class);
	
	private String SQL_UPDATE_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_UpdateTelefono.sql");
	private String SQL_UPDATE_TELEFONO_NO_PREDETERMINADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_UpdateTelefonoNoPredeterminado.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_SelectCliente.sql");
	private String SQL_UPDATE_TELEFONO_PORTAFOLIO= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_UpdateTelefonoPortafolio.sql");
	private String SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_UpdateTelefonoPrincipalTablaPersona.sql");
	
	private final String TIPO_LOG = "MODIFICACION";
	private final String ID_TIPO_ENTIDAD = "TELEFONO";
	private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_MODIFICAR_TELEFONO";
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
		SalidaConfirmarEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Cliente {
		public Long IdCliente;
		public Long IdPersona;
	}

	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaTelefonoMasivo");
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
		
		List<Telefono> telefonos = obtenerEntidadesTelefono(solicitudesCambios);
		
		modificarTelefono(con, telefonos);
		predeterminarTelefonoPortafolio(con, telefonos);
		
		out = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);

		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaTelefonoMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaTelefonoMasivo.obtenerSolicitudesDeCambios");
		long inicio = System.currentTimeMillis();

		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaTelefonoMasivo.obtenerSolicitudesDeCambios", fin - inicio);
		
		return solicitudesCambios;
	}
	
	private List<Telefono> obtenerEntidadesTelefono(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaTelefonoMasivo.obtenerEntidadesTelefono");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<Telefono> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> telefono : solicitudesPorIdEntidad.entrySet()) {
			Telefono telefonoModificar = new Telefono();
			List<SolicitudCambio> cambiosPersona = telefono.getValue();
			telefonoModificar.IdTelefono = telefono.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosPersona) {
				telefonoModificar = ValorAntiguo.asignarValorNuevo(telefonoModificar, solicitudCambio.CampoModificado, solicitudCambio.ValorNuevo);
			}
			datosAActualizar.add(telefonoModificar);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaTelefonoMasivo.obtenerEntidadesTelefono", fin - inicio);
		return datosAActualizar;
	}
	
	private void modificarTelefono(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaTelefonoMasivo.modificarTelefono");
		long inicio = System.currentTimeMillis();
		
		if(telefonos.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_TELEFONO, telefonos, Telefono.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaTelefonoMasivo.modificarTelefono", fin - inicio);
	}
	
	private void predeterminarTelefonoPortafolio(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaTelefonoMasivo.predeterminarTelefonoPortafolio");
		long inicio = System.currentTimeMillis();
		boolean predeterminadoNulo = false;
		for(Telefono tel:telefonos) {
			if(tel.Predeterminado == null)predeterminadoNulo=true;
		}
		if(!predeterminadoNulo) {
			List<Telefono> telefonoPredeterminado = telefonos.stream()
					.filter(tel->tel.Predeterminado.equals("S"))
					.collect(Collectors.toList());
			
			if(telefonos.size() > 0 ) {
				SQL sql = new SQL();		
				if(telefonoPredeterminado.size()>0) {
					HashMap<String, Object> parametros = new HashMap<String, Object>();
					Long idTelefonoPredeterminado = telefonoPredeterminado.get(0).IdTelefono;
					parametros.put("IdTelefono", idTelefonoPredeterminado);
					List<Cliente> cliente = sql.query(con, SQL_SELECT_CLIENTE, parametros, Cliente.class);
					Long idCliente = cliente.get(0).IdCliente;
					Long idPersona = cliente.get(0).IdPersona;
					SQL_UPDATE_TELEFONO_NO_PREDETERMINADO = SQL_UPDATE_TELEFONO_NO_PREDETERMINADO.replace("(:IdTelefono)", idTelefonoPredeterminado.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_TELEFONO_NO_PREDETERMINADO);
					SQL_UPDATE_TELEFONO_PORTAFOLIO  = SQL_UPDATE_TELEFONO_PORTAFOLIO.replace("(:IdTelefono)", idTelefonoPredeterminado.toString()).replace("(:IdCliente)", idCliente.toString());
					SQL.exec(con, SQL_UPDATE_TELEFONO_PORTAFOLIO);
					
					//Actualizar telefono principal en tabla PERSONA
					SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA = SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA.replace("(:IdTelefono)", idTelefonoPredeterminado.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA);
					
					}
				}
			}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaTelefonoMasivo.predeterminarTelefonoPortafolio", fin - inicio);
	}
	
	private SalidaConfirmarEjecutarCambiosPersona modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaTelefonoMasivo.modificarEstadoRegistroSolicitudCambio");
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
		logger.debug("FIN::ConfirmarModificarPersonaTelefonoMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return out;
    }
}



