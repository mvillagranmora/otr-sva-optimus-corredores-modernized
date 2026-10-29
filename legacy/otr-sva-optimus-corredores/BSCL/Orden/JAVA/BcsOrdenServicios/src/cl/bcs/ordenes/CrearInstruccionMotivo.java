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

public class CrearInstruccionMotivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearInstruccionMotivo.class);
	
	private final String SQL_INSERT_MOTIVOS = loadResourceToString("cl/bcs/ordenes/sql/CrearInstruccionMotivo_InsertMotivos.sql");
	
	public static class Motivo{
		public Long IdMotivo;
		public String IdEstado;
		public String Tipo;
		public String Motivo;
		public String IdTipoCanalIngreso;
		public String Activo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaMotivo{

		public String p_IdEstado;
		public String p_Motivo;
		public String p_IdTipoCanalIngreso;
		public String p_Activo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaCrearInstruccionMotivo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<EntradaMotivo> p_Motivos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearInstruccionMotivo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<Motivo> P_OUT_MOTIVOS;

		public SalidaCrearInstruccionMotivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_MOTIVOS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearInstruccionMotivo in = gson.fromJson(jin.toString(), EntradaCrearInstruccionMotivo.class);
		
		SalidaCrearInstruccionMotivo out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	private SalidaCrearInstruccionMotivo validarTipo (EntradaCrearInstruccionMotivo entradaCrear, SalidaConsultarInstruccionMotivo salidaConsulta ) {
		SalidaCrearInstruccionMotivo salida = new SalidaCrearInstruccionMotivo();
		Long contador = 0L;
		
		for(EntradaMotivo motivoModificar : entradaCrear.p_Motivos) {
			
			//validar que el motivo y estado no sean vacios si la accion es modificar
			if(("").equals(motivoModificar.p_Motivo) || ("").equals(motivoModificar.p_IdEstado) || ("").equals(motivoModificar.p_IdTipoCanalIngreso)
					|| motivoModificar.p_Motivo == null || motivoModificar.p_IdEstado == null || motivoModificar.p_IdTipoCanalIngreso == null) {
				contador += 1L;
				salida.P_OUT_ERRORESCONTADOR = contador;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_INFORMACION_FALTANTE","No puede realizar esta accion, motivo, canal y estado no pueden ser vacios"));
			}
			
			for (cl.bcs.ordenes.ConsultarInstruccionMotivo.Motivo motivoConsulta : salidaConsulta.P_OUT_MOTIVOS) {
				
				//validar que combinacion estado-motivo no exista previamente 
				if (motivoConsulta.IdEstado.equals(motivoModificar.p_IdEstado) 
						&& motivoConsulta.Motivo.equals(motivoModificar.p_Motivo)) {
					contador += 1L;
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_REGISTRO_EXISTENTE","No puede realizar esta accion, motivo existente"));
				}
				
				
			}
			
			
		}
		
		return salida;
	}
	
	public SalidaCrearInstruccionMotivo logica(Connection conn, EntradaCrearInstruccionMotivo entrada) throws Exception {
		SalidaCrearInstruccionMotivo salida = new SalidaCrearInstruccionMotivo();
		
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
				List<Long> ids = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::InstruccionMotivo", (long) entrada.p_Motivos.size());
				
				List <Motivo> motivosLista = new ArrayList<>();
				for (int i=0; i < entrada.p_Motivos.size(); i++){
					Motivo motivoLista = new Motivo();
					
					motivoLista.IdMotivo = ids.get(i).longValue();
					motivoLista.Tipo = "M";
					motivoLista.Activo = entrada.p_Motivos.get(i).p_Activo;
					
					motivoLista.IdTipoCanalIngreso = entrada.p_Motivos.get(i).p_IdTipoCanalIngreso;
					motivoLista.IdEstado = entrada.p_Motivos.get(i).p_IdEstado;
					motivoLista.Motivo = entrada.p_Motivos.get(i).p_Motivo;
					
					motivosLista.add(motivoLista);
				}
				
				
				_sql.batch(conn,SQL_INSERT_MOTIVOS, motivosLista, Motivo.class);
				
				for(Motivo motivo : motivosLista) {
					salida.P_OUT_MOTIVOS.add(motivo);
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

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\""
				+ ", \"p_Motivos\": ["
					+ "{\"p_IdEstado\":\"RZD\""
					+ ",\"p_Motivo\":\"pr\""
					+ ",\"p_IdTipoCanalIngreso\":\"SIS\"}"
					+ ", {\"p_IdEstado\":\"RZD\""
					+ ",\"p_Motivo\":\"r2\""
					+ ",\"p_IdTipoCanalIngreso\":\"SIS\"}"
					+ "]"
				+ "}";

		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaCrearInstruccionMotivo entrada = gson.fromJson(jsonRequest, EntradaCrearInstruccionMotivo.class);
			System.out.println("Entrada");
			System.out.println(entrada.toString());

			CrearInstruccionMotivo crearInstruccionMotivo = new CrearInstruccionMotivo();
			try {
				SalidaCrearInstruccionMotivo salidaCrearInstruccionMotivo = crearInstruccionMotivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaCrearInstruccionMotivo.toString());
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
