package cl.bcs.portafolio;

import java.math.BigDecimal;
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
import cl.bcs.utils.Utils;

public class ConsultarConsumoLimite implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarConsumoLimite.class);
	
	public static class EntradaConsultarConsumoLimite {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ObjPortafoliosEntrada> p_Portafolios;

		public String toString() {
			return Utils.printJson(this, false);
		}
		public EntradaConsultarConsumoLimite() {
			p_Portafolios = new ArrayList<ObjPortafoliosEntrada>();
		}
	}

	public static class ObjPortafoliosEntrada {
		public Long IdInterno;
		public String IdClasificacionTipoInstrumento;
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsumoLimite {
		public Long IdInterno;
		public Long IdPortafolio;
		public BigDecimal MontoDisponible;
		public String IdTipoLimite;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaConsultarConsumoLimite {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ConsumoLimite> P_OUT_CONSUMOLIMITES;

		public SalidaConsultarConsumoLimite() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_CONSUMOLIMITES = new ArrayList<ConsumoLimite>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) {
		JsonObject dataout = new JsonObject();
		try {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarConsumoLimite in = gson.fromJson(jin.toString(), EntradaConsultarConsumoLimite.class);
			SalidaConsultarConsumoLimite out = logica(conn, in);

			JsonParser parser = new JsonParser();
			dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataout;
	}

	public SalidaConsultarConsumoLimite logica(Connection con, EntradaConsultarConsumoLimite entrada) {
		logger.info("Inicio ConsultarConsumoLimite");
		SalidaConsultarConsumoLimite salida = new SalidaConsultarConsumoLimite();
		
		try {
			ConsultarConsumoEnLinea consultarConsumoEnLinea = new ConsultarConsumoEnLinea();
			
			for (ObjPortafoliosEntrada portafolios : entrada.p_Portafolios) {
				ConsultarConsumoEnLinea.EntradaObtieneConsumoEnLinea entradaObtieneConsumoEnLinea = new ConsultarConsumoEnLinea.EntradaObtieneConsumoEnLinea();
				entradaObtieneConsumoEnLinea.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaObtieneConsumoEnLinea.p_IdUsuario = entrada.p_IdUsuario;
				entradaObtieneConsumoEnLinea.p_IdModulo = entrada.p_IdModulo;
				entradaObtieneConsumoEnLinea.p_IdCliente = portafolios.IdCliente;
				entradaObtieneConsumoEnLinea.p_IdPortafolio = portafolios.IdPortafolio;
				entradaObtieneConsumoEnLinea.p_IdInterno = portafolios.IdInterno;
				ConsultarConsumoEnLinea.SalidaObtieneConsumoEnLinea salidaObtieneConsumoEnLinea = consultarConsumoEnLinea.logica(con, entradaObtieneConsumoEnLinea);
				for (ConsultarConsumoEnLinea.PlantillaLimites plantilla : salidaObtieneConsumoEnLinea.P_OUT_PLANTILLASLIMITES) {
					if (plantilla.IdClasificacionTipoInstrumento.equalsIgnoreCase(portafolios.IdClasificacionTipoInstrumento)) {
						ConsumoLimite consumoLimite = new ConsumoLimite();
						consumoLimite.IdInterno = plantilla.IdInterno;
						consumoLimite.IdPortafolio = plantilla.IdPortafolio;
						consumoLimite.MontoDisponible = plantilla.MontoDisponible;
						consumoLimite.IdTipoLimite = plantilla.IdTipoLimite;
						salida.P_OUT_CONSUMOLIMITES.add(consumoLimite);
					}
				}
			}
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
		}
		logger.info("Fin ConsultarConsumoLimite");
		return salida;
	}

	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			conn = PlataformaUtils.getDESAConnection();
			System.out.println("== INICIO ==");
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_Portafolios\":[{\"IdInterno\":0, \"IdCliente\":123569, \"IdPortafolio\":27499, \"IdClasificacionTipoInstrumento\":\"RV\"}]}";
			EntradaConsultarConsumoLimite entrada = gson.fromJson(json, EntradaConsultarConsumoLimite.class);

			ConsultarConsumoLimite demo = new ConsultarConsumoLimite();
			SalidaConsultarConsumoLimite ObjOut = demo.logica(conn, entrada);
			System.out.println("== SALIDA ==");
			System.out.println(ObjOut.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");
	}
}