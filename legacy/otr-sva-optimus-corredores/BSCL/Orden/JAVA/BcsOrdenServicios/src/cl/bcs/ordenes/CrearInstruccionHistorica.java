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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearInstruccionHistorica 	implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearInstruccionHistorica.class);
	
	private final String SQL_INSERT_INSTRUCCION_HISTORICA = loadResourceToString("cl/bcs/ordenes/sql/CrearInstruccionHistorica_InsertInstruccionHistorica.sql");
	
	public static class InstruccionHistorica{
		public Long IdInstruccionHistorica;
		public Long IdInstruccion;
		public String IdEstadoInstruccion;
		public Long IdMotivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaInstruccionHistorica{

		public Long p_IdInstruccion;
		public String p_IdEstadoInstruccion;
		public Long p_IdMotivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaCrearInstruccionHistorica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		
		public List<EntradaInstruccionHistorica> p_InstruccionesHistoricas;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearInstruccionHistorica{
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<InstruccionHistorica> P_OUT_INSTRUCCIONES_HISTORICAS;

		public SalidaCrearInstruccionHistorica() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_INSTRUCCIONES_HISTORICAS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearInstruccionHistorica in = gson.fromJson(jin.toString(), EntradaCrearInstruccionHistorica.class);
		
		SalidaCrearInstruccionHistorica out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	private SalidaCrearInstruccionHistorica validarEntrada(Connection conn, EntradaCrearInstruccionHistorica entrada) {
		SalidaCrearInstruccionHistorica salida = new SalidaCrearInstruccionHistorica();
		Long contador = 0L;
		
		for(EntradaInstruccionHistorica registro : entrada.p_InstruccionesHistoricas) {
			if (registro.p_IdInstruccion == null || registro.p_IdInstruccion == 0 ) {
				contador += 1L;
				salida.P_OUT_ERRORESCONTADOR = contador;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_INFORMACION_FALTANTE","No puede realizar esta accion, IdInstruccion no puede ser vacio"));
			}
			
			if (registro.p_IdEstadoInstruccion == null || ("0").equals(registro.p_IdEstadoInstruccion ) || ("").equals(registro.p_IdEstadoInstruccion )) {
				contador += 1L;
				salida.P_OUT_ERRORESCONTADOR = contador;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_INFORMACION_FALTANTE","No puede realizar esta accion, IdEstadoInstruccion no puede ser vacio"));
			}
			
		}
		
		
		return salida;
	}
	
	public SalidaCrearInstruccionHistorica logica(Connection conn, EntradaCrearInstruccionHistorica entrada) throws Exception {
		SalidaCrearInstruccionHistorica salida = new SalidaCrearInstruccionHistorica();
		
		SQL _sql = new SQL();
		try {
			
			salida = validarEntrada(conn, entrada);
			if (Long.valueOf(0L).equals(salida.P_OUT_ERRORESCONTADOR)) {
				List<Long> ids = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::InstruccionHistorica", (long) entrada.p_InstruccionesHistoricas.size());
				
				List <InstruccionHistorica> instruccionHistoricaLista = new ArrayList<>();
				for (int i=0; i < entrada.p_InstruccionesHistoricas.size(); i++){
					InstruccionHistorica instruccionHistorica = new InstruccionHistorica();
					
					instruccionHistorica.IdInstruccionHistorica = ids.get(i).longValue();
					
					instruccionHistorica.IdInstruccion = entrada.p_InstruccionesHistoricas.get(i).p_IdInstruccion;
					instruccionHistorica.IdEstadoInstruccion = entrada.p_InstruccionesHistoricas.get(i).p_IdEstadoInstruccion;
					instruccionHistorica.IdMotivo = entrada.p_InstruccionesHistoricas.get(i).p_IdMotivo;
					
					instruccionHistoricaLista.add(instruccionHistorica);
				}
				
				
				_sql.batch(conn,SQL_INSERT_INSTRUCCION_HISTORICA, instruccionHistoricaLista, InstruccionHistorica.class);
				
				for(InstruccionHistorica motivo : instruccionHistoricaLista) {
					salida.P_OUT_INSTRUCCIONES_HISTORICAS.add(motivo);
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
				+ ", \"p_InstruccionesHistoricas\": ["
					+ "{\"p_IdInstruccion\":212"
					+ ",\"p_IdEstadoInstruccion\":\"\""
					+ ",\"p_IdMotivo\":1}"
					+ "]"
				+ "}";

		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaCrearInstruccionHistorica entrada = gson.fromJson(jsonRequest, EntradaCrearInstruccionHistorica.class);
			System.out.println("Entrada");
			System.out.println(entrada.toString());

			CrearInstruccionHistorica crearInstruccionHistorica = new CrearInstruccionHistorica();
			try {
				SalidaCrearInstruccionHistorica salidaCrearInstruccionHistorica = crearInstruccionHistorica.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaCrearInstruccionHistorica.toString());
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
