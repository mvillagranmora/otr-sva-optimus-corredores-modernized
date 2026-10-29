package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ConsultarInstruccionMotivo.EntradaConsultarInstruccionMotivo;
import cl.bcs.ordenes.ConsultarInstruccionMotivo.SalidaConsultarInstruccionMotivo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarInstruccionMotivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarInstruccionMotivo.class);
	
	private final String SQL_UPDATE_MOTIVOS = loadResourceToString("cl/bcs/ordenes/sql/ModificarInstruccionMotivo_UpdateMotivos.sql");
	private final String SQL_DELETE_MOTIVOS = loadResourceToString("cl/bcs/ordenes/sql/ModificarInstruccionMotivo_DeleteMotivos.sql");
	
	public static class Motivo{
		public Long p_IdMotivo;
		public String p_IdEstado;
		public String p_Motivo;
		public String p_Activo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EntradaModificarInstruccionMotivo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_Accion;
		
		public List<Motivo> p_Motivos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarInstruccionMotivo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public SalidaModificarInstruccionMotivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarInstruccionMotivo in = gson.fromJson(jin.toString(), EntradaModificarInstruccionMotivo.class);
		
		SalidaModificarInstruccionMotivo out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	private SalidaModificarInstruccionMotivo validarTipo (EntradaModificarInstruccionMotivo entradaModificar, SalidaConsultarInstruccionMotivo salidaConsulta ) {
		SalidaModificarInstruccionMotivo salida = new SalidaModificarInstruccionMotivo();
		Long contador = 0L;
		
		for(Motivo motivoModificar : entradaModificar.p_Motivos) {
			
			//validar que el motivo y estado no sean vacios si la accion es modificar
			if(("UPDATE").equals(entradaModificar.p_Accion)
					&& (("").equals(motivoModificar.p_Motivo) || ("").equals(motivoModificar.p_IdEstado) || ("").equals(motivoModificar.p_Activo)
						 || motivoModificar.p_IdEstado == null || motivoModificar.p_Motivo == null || motivoModificar.p_Activo == null)) {
				contador += 1L;
				salida.P_OUT_ERRORESCONTADOR = contador;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(contador, MensajeSalida.MENSAJE_ERROR, "ERROR_INFORMACION_FALTANTE","No puede realizar esta accion, activo, motivo y estado no pueden ser vacios"));
			}
			
			for (cl.bcs.ordenes.ConsultarInstruccionMotivo.Motivo motivoConsulta : salidaConsulta.P_OUT_MOTIVOS) {
				//validar que los que se quieren modificar sean tipo 'M' para ambas acciones
				if (motivoConsulta.IdMotivo.equals(motivoModificar.p_IdMotivo) && !("M").equals(motivoConsulta.Tipo)) {
					contador += 1L;
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(contador, MensajeSalida.MENSAJE_ERROR, "ERROR_TIPO","No puede modificar registros que no sean de tipo 'M'"));
				}
				
				//validar que combinacion estado-motivo no exista previamente si la accion es modificar
				if (("UPDATE").equals(entradaModificar.p_Accion) && !("N").equals(motivoModificar.p_Activo)
						&& motivoConsulta.IdEstado.equals(motivoModificar.p_IdEstado) 
						&& motivoConsulta.Motivo.equals(motivoModificar.p_Motivo)) {
					contador += 1L;
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(contador, MensajeSalida.MENSAJE_ERROR, "ERROR_REGISTRO_EXISTENTE","No puede realizar esta accion, motivo existente"));
				}
				
				
			}
			
			
		}
		
		return salida;
	}
	
	public SalidaModificarInstruccionMotivo logica(Connection conn, EntradaModificarInstruccionMotivo entrada) throws Exception {
		SalidaModificarInstruccionMotivo salida = new SalidaModificarInstruccionMotivo();
		
		SQL _sql = new SQL();
		try {
			//traer todos los motivos
			ConsultarInstruccionMotivo consultarInstruccionMotivo = new ConsultarInstruccionMotivo();
			EntradaConsultarInstruccionMotivo entradaConsulta = new EntradaConsultarInstruccionMotivo();
			entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsulta.p_IdModulo = entrada.p_IdModulo;
			entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsulta.p_IdEstado = "";
			entradaConsulta.p_Activo = "S";
			entradaConsulta.p_IdTipoCanalIngreso = "";
			entradaConsulta.p_Motivo = "";
			entradaConsulta.p_Tipo = "";
			
			SalidaConsultarInstruccionMotivo salidaConsulta = consultarInstruccionMotivo.logica(conn, entradaConsulta);
			
			salida = validarTipo(entrada, salidaConsulta);
			if (Long.valueOf(0L).equals(salida.P_OUT_ERRORESCONTADOR)) {
				if (("UPDATE").equals(entrada.p_Accion)) {
					_sql.batch(conn,SQL_UPDATE_MOTIVOS,entrada.p_Motivos, Motivo.class);
				}
				else if (("DELETE").equals(entrada.p_Accion)) {
					_sql.batch(conn,SQL_DELETE_MOTIVOS,entrada.p_Motivos, Motivo.class);
				}
			}
						
			
			
		}
		catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
 
		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\"," 
				+ "\"p_Accion\" : \"UPDATE\""
				+ ", \"p_Motivos\": [{\"p_IdMotivo\":9"
				+ ",\"p_Motivo\":\"aaa\""
				+ ",\"p_Activo\":\"S\""
				+ ",\"p_IdEstado\":\"RZD\"}]"
				+ "}";

		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaModificarInstruccionMotivo entrada = gson.fromJson(jsonRequest, EntradaModificarInstruccionMotivo.class);
			System.out.println("Entrada");
			System.out.println(entrada.toString());

			ModificarInstruccionMotivo ModificarInstruccionMotivo = new ModificarInstruccionMotivo();
			try {
				SalidaModificarInstruccionMotivo salidaModificarInstruccionMotivo = ModificarInstruccionMotivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaModificarInstruccionMotivo.toString());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
