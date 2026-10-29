package cl.bcs.socionegocio.cli.ws;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaConsultarClientePorDocumentoIdentidad;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaDocumentoIdentidad;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.SalidaConsultarClientePorDocumentoIdentidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarEstadoClienteWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarEstadoClienteWS.class);
	
	private final String SQL_CALL_MODIFICAR_ESTADO_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/ws/sql/ModificarEstadoClienteWS_CallModificarEstadoCliente.sql");
	
	public static class EntradaModificarEstadoClienteWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_DocumentoIdentidad;
		public String p_IdEstado;
		public String p_IdTipo;
		public String p_FechaHasta;
		public String p_Comentario;
		
		public EntradaModificarEstadoClienteWS() {
			this.p_FechaHasta = "0001-01-01 00:00:00";
		}
	}
	
	public static class SalidaModificarEstadoClienteWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarEstadoClienteWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class ErrorDetalle extends BcsPojo {
		public String	Codigo;
		public String	Descripcion;
	}
	
	public static class AdvertenciaDetalle extends BcsPojo {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarEstadoClienteWS jin = gson.fromJson(in.toString(), EntradaModificarEstadoClienteWS.class);
		SalidaModificarEstadoClienteWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarEstadoClienteWS logica(Connection con, EntradaModificarEstadoClienteWS entrada) throws Exception {
		logger.info("INICIO::SalidaModificarEstadoClienteWS");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarEstadoClienteWS out = new SalidaModificarEstadoClienteWS();
		
		SalidaConsultarClientePorDocumentoIdentidad salidaConsultarClientePorDocumentoIdentidad = obtenerCliente(con, entrada);
		out.P_OUT_ERRORESCONTADOR += salidaConsultarClientePorDocumentoIdentidad.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaConsultarClientePorDocumentoIdentidad.P_OUT_MENSAJES);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		Long idCliente = salidaConsultarClientePorDocumentoIdentidad.P_OUT_CLIENTES.get(0).IdCliente;
		out = modificarEstadoCliente(con, entrada, idCliente);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::SalidaModificarEstadoClienteWS", finServicio - inicioServicio);
		return out;
	}
	
	private SalidaConsultarClientePorDocumentoIdentidad obtenerCliente(Connection con, EntradaModificarEstadoClienteWS entrada) throws Exception {
		logger.debug("INICIO::ModificarEstadoClienteWS.obtenerCliente");
		long inicio = System.currentTimeMillis();
		
		EntradaConsultarClientePorDocumentoIdentidad entradaConsultarClientePorDocumentoIdentidad = new EntradaConsultarClientePorDocumentoIdentidad();
		entradaConsultarClientePorDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarClientePorDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarClientePorDocumentoIdentidad.p_IdModulo = "SOC";
		entradaConsultarClientePorDocumentoIdentidad.p_UsaReglasNegocio = "S";
		
		EntradaDocumentoIdentidad entradaDocumentoIdentidad = new EntradaDocumentoIdentidad();
		entradaDocumentoIdentidad.DocumentoIdentidad = entrada.p_DocumentoIdentidad;
		entradaConsultarClientePorDocumentoIdentidad.p_DocumentosIdentidad = Arrays.asList(entradaDocumentoIdentidad);
		
		ConsultarClientePorDocumentoIdentidad consultarClientePorDocumentoIdentidad = new ConsultarClientePorDocumentoIdentidad();
		SalidaConsultarClientePorDocumentoIdentidad salidaConsultarClientePorDocumentoIdentidad = consultarClientePorDocumentoIdentidad.logica(con, entradaConsultarClientePorDocumentoIdentidad);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoClienteWS.obtenerCliente", fin - inicio);
		return salidaConsultarClientePorDocumentoIdentidad;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaModificarEstadoClienteWS modificarEstadoCliente(Connection con, EntradaModificarEstadoClienteWS entrada, Long idCliente) throws Exception {
		logger.debug("INICIO::ModificarEstadoClienteWS.modificarEstadoCliente");
		long inicio = System.currentTimeMillis();
		
		String fechaHasta = entrada.p_FechaHasta;
		if(fechaHasta == null || fechaHasta.isEmpty()) {
			fechaHasta = "0001-01-01 00:00:00";
		}
		
		String fecha = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		
		SalidaModificarEstadoClienteWS salidaModificarEstadoClienteWS = new SalidaModificarEstadoClienteWS();
		HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>();
		parametrosEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", entrada.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", entrada.p_IdModulo);
		parametrosEntrada.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
		parametrosEntrada.put("p_IdCliente", idCliente);
		parametrosEntrada.put("p_IdEstado", entrada.p_IdEstado);
		parametrosEntrada.put("p_IdTipo", entrada.p_IdTipo);
		parametrosEntrada.put("p_Fecha", fecha);
		parametrosEntrada.put("p_FechaHasta", fechaHasta);
		parametrosEntrada.put("p_Comentario", entrada.p_Comentario);
		
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_RefId", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
	    
	    SQL sql = new SQL();
	    HashMap<String, Object> respuestaModificarEstadoCliente = sql.call(con, SQL_CALL_MODIFICAR_ESTADO_CLIENTE, parametrosEntrada, parametrosSalida);
	    
	    List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaModificarEstadoCliente.get("p_out_ErroresDetalle");
	    List<AdvertenciaDetalle> advertenciasDetalle = (List<AdvertenciaDetalle>) respuestaModificarEstadoCliente.get("p_out_AdvertenciasDetalle");
	    
	    salidaModificarEstadoClienteWS.P_OUT_MENSAJES.addAll(obtenerMensajesErrores(erroresDetalle));
	    salidaModificarEstadoClienteWS.P_OUT_MENSAJES.addAll(obtenerMensajesAdvertencia(advertenciasDetalle));
	    salidaModificarEstadoClienteWS.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarEstadoClienteWS.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoClienteWS.modificarEstadoCliente", fin - inicio);
		return salidaModificarEstadoClienteWS;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ModificarEstadoClienteWS.obtenerFechaSistema");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarEstadoClienteWS.obtenerFechaSistema", fin - inicio);
		return fechaSistema;
	}
	
	private List<MensajeSalida> obtenerMensajesErrores(List<ErrorDetalle> erroresDetalle){
		logger.debug("INICIO::ModificarEstadoClienteWS.obtenerMensajesErrores");
		long inicio = System.currentTimeMillis();
		 List<MensajeSalida> mensajesError = erroresDetalle
		    		.stream()
		    		.map(errorDetalle->{
				    	MensajeSalida mensajeSalida = new MensajeSalida();
				    	mensajeSalida.id = 0L;
				    	mensajeSalida.tipo = MensajeSalida.MENSAJE_ERROR;
				    	mensajeSalida.codigo = errorDetalle.Codigo;
				    	mensajeSalida.descripcion = errorDetalle.Descripcion;
				    	return mensajeSalida;
		    		})
		    		.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoClienteWS.obtenerMensajesErrores", fin - inicio);
		return mensajesError;
	}
	
	private List<MensajeSalida> obtenerMensajesAdvertencia(List<AdvertenciaDetalle> advertenciasDetalle){
		logger.debug("INICIO::ModificarEstadoClienteWS.obtenerMensajesAdvertencia");
		long inicio = System.currentTimeMillis();
		 List<MensajeSalida> mensajesError = advertenciasDetalle
		    		.stream()
		    		.map(advertenciaDetalle->{
				    	MensajeSalida mensajeSalida = new MensajeSalida();
				    	mensajeSalida.id = 0L;
				    	mensajeSalida.tipo = "S".equals(advertenciaDetalle.RequiereAutorizacion) ? MensajeSalida.MENSAJE_AUTORIZACION : MensajeSalida.MENSAJE_ADVERTENCIA;
				    	mensajeSalida.codigo = advertenciaDetalle.Codigo;
				    	mensajeSalida.descripcion = advertenciaDetalle.Descripcion;
				    	return mensajeSalida;
		    		})
		    		.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoClienteWS.obtenerMensajesAdvertencia", fin - inicio);
		return mensajesError;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_DocumentoIdentidad\":\"16338637-2\",\"p_IdEstado\":\"ACT\",\"p_IdTipo\":\"DESBACTUALIZADOC\",\"p_FechaHasta\":\"\",\"p_Comentario\":\"SADASDAS\"}";

		EntradaModificarEstadoClienteWS Objin = gson.fromJson(json, EntradaModificarEstadoClienteWS.class);
		ModificarEstadoClienteWS demo = new ModificarEstadoClienteWS();				
		try {
			SalidaModificarEstadoClienteWS ObjOut = demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.rollback();
			}else {
				con.rollback();
			}
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
			//con.commit();
    	}
		System.out.println("== END ==");
	}

}
