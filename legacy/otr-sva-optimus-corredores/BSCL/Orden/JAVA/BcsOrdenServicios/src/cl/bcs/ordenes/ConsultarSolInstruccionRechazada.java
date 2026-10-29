package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSolInstruccionRechazada implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarSolInstruccionRechazada.class);
	
	private final String SQL_SELECT_INSTRUCCIONES_RECHAZADAS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarSolInstruccionRechazada_SelectInstruccionesRechazadas.sql");
	
	public static class EntradaConsultarSolInstruccionRechazada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public Long p_IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarSolInstruccionRechazada{
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<InstruccionHistorica> P_OUT_INSTRUCCION_HISTORICA;

		public SalidaConsultarSolInstruccionRechazada() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_INSTRUCCION_HISTORICA = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstruccionHistorica{
		public Long IdCliente;
		public String IdEstadoInstruccion;
		public Long IdInstruccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarSolInstruccionRechazada in = gson.fromJson(jin.toString(), EntradaConsultarSolInstruccionRechazada.class);
		
		SalidaConsultarSolInstruccionRechazada out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public SalidaConsultarSolInstruccionRechazada logica(Connection conn, EntradaConsultarSolInstruccionRechazada entrada) throws Exception {
		SalidaConsultarSolInstruccionRechazada salida = new SalidaConsultarSolInstruccionRechazada();

		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa informacionEmpresa = consultarEmpresa.logica(conn, entrada.p_IdEmpresa);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
		fechasIn.fechaInicial = fechaSistema;
		fechasIn.numeroDias = 1L;
		Long diaTemp = 1L;
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = new OutObtenerDiaAnterior();
		while (diaTemp<= 3L){
			InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
			inObtenerDiaAnterior.fechas.add(fechasIn);
			inObtenerDiaAnterior.idPais = informacionEmpresa.IdPais;
			inObtenerDiaAnterior.diaHabil = "N";
			inObtenerDiaAnterior.soloCaeDiaHabil = "S";
			outObtenerDiaAnterior = obtenerDiaAnterior.logica(conn, inObtenerDiaAnterior);
			diaTemp++;
			fechasIn.fechaInicial = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		}
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params =  new HashMap<>();
		params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("p_FechaDesde", outObtenerDiaAnterior.fechas.get(0).fechaCalculada);
		
		List<InstruccionHistorica> instruccionesHistoricas = null;
		logger.debug("INICIO - Select instrucciones historicas");
		instruccionesHistoricas = _sql.query(conn, SQL_SELECT_INSTRUCCIONES_RECHAZADAS, params, InstruccionHistorica.class);
		logger.debug("FIN - Select instrucciones historicas"); 
		
		logger.debug("INICIO - Ciclo agregar instrucciones historicas");
		if(instruccionesHistoricas != null && instruccionesHistoricas.size() > 0) {
			for(InstruccionHistorica instruccion : instruccionesHistoricas) {
				salida.P_OUT_INSTRUCCION_HISTORICA.add(instruccion);
			}
		}
		logger.debug("FIN - Ciclo agregar instrucciones historicas");
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\""
				+ ",\"p_IdCliente\":1418223}";

		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarSolInstruccionRechazada entrada = gson.fromJson(jsonRequest, EntradaConsultarSolInstruccionRechazada.class);
			System.out.println("Entrada");
			System.out.println(entrada.toString());

			ConsultarSolInstruccionRechazada consultarSolInstruccionRechazada = new ConsultarSolInstruccionRechazada();
			try {
				SalidaConsultarSolInstruccionRechazada salidaConsultarSolInstruccionRechazada = consultarSolInstruccionRechazada.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaConsultarSolInstruccionRechazada.toString());
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
