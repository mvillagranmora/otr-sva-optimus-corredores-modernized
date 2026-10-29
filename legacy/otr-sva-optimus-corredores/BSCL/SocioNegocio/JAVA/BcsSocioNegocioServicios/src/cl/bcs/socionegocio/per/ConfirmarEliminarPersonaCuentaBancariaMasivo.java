package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConfirmarEliminarPersonaCuentaBancariaMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona {

	static final Logger logger = LogManager.getLogger(ConfirmarEliminarPersonaCuentaBancariaMasivo.class);
	
	private String SQL_DELETE_CUENTA_BANCARIA	= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaCuentaBancariaMasivo_DeleteCuentaBancaria.sql");
	private String SQL_DELETE_PREFERENCIA_PAGO	= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaCuentaBancariaMasivo_DeletePrefPago.sql");
	
	private final String TIPO_LOG = "ELIMINACION";
	private final String ID_TIPO_ENTIDAD = "CUENTABANCARIA-BORRAR";
	private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_ELIMINAR_CUENTA_BANCARIA";

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
		logger.info("INICIO::ConfirmarEliminarPersonaCuentaBancariaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_CUENTA_BANCARIA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}

		ObtenerEntidadesPersonaEliminar obtenerEntidadesPersonaEliminar = new ObtenerEntidadesPersonaEliminar();
		List<EntidadPersonaEliminar> datosEntidadPersonaEliminar = obtenerEntidadesPersonaEliminar.logica(solicitudesCambios);
		
		EliminarEntidadPersona eliminarEntidadPersona = new EliminarEntidadPersona();
		eliminarEntidadPersona.logica(con, datosEntidadPersonaEliminar, SQL_DELETE_CUENTA_BANCARIA);
		eliminarEntidadPersona.logica(con, datosEntidadPersonaEliminar, SQL_DELETE_PREFERENCIA_PAGO);
		
		ModificarEstadoRegistroSolicitudCambioPersona modificarEstadoRegistroSolicitudCambioPersona = new ModificarEstadoRegistroSolicitudCambioPersona();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambioPersona.logica(con, entrada, solicitudesCambios);
		
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarEliminarPersonaCuentaBancariaMasivo", finServicio - inicioServicio);
		return out;
	}
	

	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\", p_TarjetaBancaria: [{IdPersona:1790725}], p_IdEstadoAutorizacion: \"AUT\"}";
			EntradaConfirmarEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaConfirmarEjecutarCambiosPersona.class);
			
			ConfirmarEliminarPersonaCuentaBancariaMasivo demo = new ConfirmarEliminarPersonaCuentaBancariaMasivo();
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
