package cl.bcs.socionegocio.per;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaTelefonoMasivo.Cliente;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo.DireccionFisica;
import cl.bcs.socionegocio.per.pojo.Telefono;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaDireccionFisicaMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona  {

	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaDireccionFisicaMasivo.class);
	
	private String SQL_UPDATE_DATOS_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateDatosDireccionFisica.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateDireccionFisicaNoPredeterminada.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_SelectCliente.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateDireccionFisicaPortafolio.sql");
	private String SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_SelectIndicadoresModeloTipoCliente.sql");
	private String SQL_UPDATE_INDICADORES= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateIndicadores.sql");
	private String SQL_UPDATE_INDICADORES_AUX= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateIndicadores.sql");
	private String SQL_SELECT_PORTAFOLIOS_CLIENTE= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_SelectPortafoliosCliente.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateDireccionFisicaPrincipalTablaPersona.sql");
	
	private final String TIPO_LOG = "MODIFICACION";
	private final String ID_TIPO_ENTIDAD = "DIRECCIONFISICA";
	private final String NOMBRE_PROCESO_LOG  = "LOG_NOMBRE_PROCESO_MODIFICAR_DIRECCION_FISICA";
	
	
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
		EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
		SalidaConfirmarEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Override
	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDireccionFisicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_DIRECCION_FISICA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}

		List<DireccionFisica> datosEntidad = obtenerEntidades(solicitudesCambios);
		
		modificarCampoDireccionFisica(con, datosEntidad);
		predeterminarDireccionFisica(con, datosEntidad);
		
		ModificarEstadoRegistroSolicitudCambioPersona modificarEstadoRegistroSolicitudCambioPersona = new ModificarEstadoRegistroSolicitudCambioPersona();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambioPersona.logica(con, entrada, solicitudesCambios);
		
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaDireccionFisicaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<DireccionFisica> obtenerEntidades(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDireccionFisicaMasivo.obtenerEntidadesDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<DireccionFisica> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			DireccionFisica datosEntidad = new DireccionFisica();
			
			List<SolicitudCambio> cambiosEntidad = persona.getValue();
			datosEntidad.IdDireccionFisica = persona.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosEntidad) {
				Field campo = datosEntidad.getClass().getField(solicitudCambio.CampoModificado);
				if(campo.getType().isAssignableFrom(String.class)) {
					campo.set(datosEntidad, solicitudCambio.ValorNuevo);
				}
				if(campo.getType().isAssignableFrom(Long.class)) {
					campo.set(datosEntidad, Long.parseLong(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
					campo.set(datosEntidad, Fecha.toDateTime(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Date.class)) {
					campo.set(datosEntidad, Fecha.toDate(solicitudCambio.ValorNuevo));
				}
			}
			datosAActualizar.add(datosEntidad);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDireccionFisicaMasivo.obtenerEntidadesDatosGenerales", fin - inicio);
		return datosAActualizar;
	}
	
	private void modificarCampoDireccionFisica(Connection con, List<DireccionFisica> lstEntidad) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDireccionFisicaMasivo.modificarCampoDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		if( lstEntidad.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_DATOS_DIRECCION_FISICA, lstEntidad, DireccionFisica.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDireccionFisicaMasivo.modificarCampoDireccionFisica", fin - inicio);
	}
	
	private void predeterminarDireccionFisica(Connection con, List<DireccionFisica> lstEntidad) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDireccionFisicaMasivo.predeterminarDireccionFisica");
		long inicio = System.currentTimeMillis();
		boolean predeterminadoNulo = false;
		for(DireccionFisica dirf:lstEntidad) {
			if(dirf.Predeterminado == null)predeterminadoNulo=true;
		}
		if(!predeterminadoNulo) {
			List<DireccionFisica> direccionFisicaPredeterminada = lstEntidad.stream()
					.filter(tel->tel.Predeterminado.equals("S"))
					.collect(Collectors.toList());
			
			if(lstEntidad.size() > 0 ) {
				SQL sql = new SQL();
				if(direccionFisicaPredeterminada.size()>0) {
					HashMap<String, Object> parametros = new HashMap<String, Object>();
					Long idDireccionFisicaPredeterminada = direccionFisicaPredeterminada.get(0).IdDireccionFisica;
					parametros.put("IdDireccionFisica", idDireccionFisicaPredeterminada);
					List<Cliente> cliente = sql.query(con, SQL_SELECT_CLIENTE, parametros, Cliente.class);
					Long idCliente = cliente.get(0).IdCliente;
					Long idPersona = cliente.get(0).IdPersona;
					String idTipoCliente = cliente.get(0).IdTipoCliente;
					SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA = SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA.replace("(:IdDireccionFisica)", idDireccionFisicaPredeterminada.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA);
					SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO  = SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO.replace("(:IdDireccionEnvioCorrespondencia)", idDireccionFisicaPredeterminada.toString()).replace("(:IdCliente)", idCliente.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO);
					
					//Actualizar direccion fisica principal en tabla PERSONA
					SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA = SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA.replace("(:IdDireccionFisica)", idDireccionFisicaPredeterminada.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA);

					parametros = new HashMap<String, Object>();
					parametros.put("ModeloCliente",idTipoCliente);
					Long idCheckBoxEnvioDireccionFisica = 10676L; //Si envia a direccion propia
					Long idCheckBoxEnvioDireccionCartolaFisica = 1020L; //Si envia cartola a direccion fisica
					Long idComboBoxDireccionFisica = 10670L;
					List<Indicador> Indicadores = sql.query(con, SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE, parametros, Indicador.class);
					parametros = new HashMap<String, Object>();
					parametros.put("IdCliente",idCliente);
					List<Portafolio> idPortafolios = sql.query(con,SQL_SELECT_PORTAFOLIOS_CLIENTE,parametros,Portafolio.class);
					if(Indicadores.size()>0) {
						List<Indicador> indicadorEnvioDireccionFisica = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idCheckBoxEnvioDireccionFisica))
								.collect(Collectors.toList());
						List<Indicador> indicadorEnvioDireccionCartolaFisica = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idCheckBoxEnvioDireccionCartolaFisica))
								.collect(Collectors.toList());
						List<Indicador> ComboDireccionCartolaFisica = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idComboBoxDireccionFisica))
								.collect(Collectors.toList());
						
						List<Indicador> resultIndicadorPortafolioEnvioDireccion = new ArrayList<>();
						List<Indicador> resultIndicadorPortafolioEnvioDireccionCartola = new ArrayList<>();
						List<Indicador> resultindicadorComboPortafolioEnvioDireccionCartola = new ArrayList<>();
						
						for(Portafolio port :idPortafolios) {
							List<Indicador> buscarIndicador = indicadorEnvioDireccionFisica.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							List<Indicador> buscarIndicadorCatola = indicadorEnvioDireccionCartolaFisica.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							List<Indicador> ComboIndicadorCatola = ComboDireccionCartolaFisica.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							
							if(buscarIndicador.size()>0) {
								resultIndicadorPortafolioEnvioDireccion.add(buscarIndicador.get(0));
							}
							if(buscarIndicadorCatola.size()>0) {
								resultIndicadorPortafolioEnvioDireccionCartola.add(buscarIndicadorCatola.get(0));
							}
							if(ComboIndicadorCatola.size()>0) {
								resultindicadorComboPortafolioEnvioDireccionCartola.add(ComboIndicadorCatola.get(0));
							}
						} 
						if(indicadorEnvioDireccionFisica.size()>0 || indicadorEnvioDireccionCartolaFisica.size()>0) {
							if(indicadorEnvioDireccionFisica.get(0).ValorIndicador.equals("S")|| indicadorEnvioDireccionCartolaFisica.get(0).ValorIndicador.equals("S") ){
								for(Indicador indCombo: resultindicadorComboPortafolioEnvioDireccionCartola) {
									SQL_UPDATE_INDICADORES_AUX = SQL_UPDATE_INDICADORES;
									SQL_UPDATE_INDICADORES_AUX = SQL_UPDATE_INDICADORES_AUX.replace("(:IdIndicador)",indCombo.IdIndicador.toString()).replace("(:ValorIndicador)", idDireccionFisicaPredeterminada.toString());
									SQL.exec(con, SQL_UPDATE_INDICADORES_AUX);
								}
							}
						}	
					}		
				}	
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDireccionFisicaMasivo.predeterminarDireccionFisica", fin - inicio);
	}
	
	
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\",p_PersonaDatosGenerales: [{IdPersona:1790725}],p_IdEstadoAutorizacion: \"AUT\"}";
			EntradaConfirmarEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaConfirmarEjecutarCambiosPersona.class);
			
			ConfirmarModificarPersonaDireccionFisicaMasivo demo = new ConfirmarModificarPersonaDireccionFisicaMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaConfirmarEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");

	}
}
