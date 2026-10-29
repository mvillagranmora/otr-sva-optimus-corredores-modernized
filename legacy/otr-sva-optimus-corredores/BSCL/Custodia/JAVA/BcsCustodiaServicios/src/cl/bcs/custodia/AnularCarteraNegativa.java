package cl.bcs.custodia;


import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.dte.CallerWSDte;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularCarteraNegativa implements JavaCall {

	private static final Logger logger = LogManager.getLogger(AnularCarteraNegativa.class);
	private final String SQL_UPDATE_CARTERANEGATIVA = loadResourceToString("cl/bcs/custodia/sql/AnularCarteraNegativa_UpdateCarteraNegativa.sql");
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		return null;
	}	
		
	public AnularCarteraNegativa_Salida logica(Connection conn, AnularCarteraNegativa_Entrada in) throws Exception {
		logger.info("Inicio");
		AnularCarteraNegativa_Salida salida = new AnularCarteraNegativa_Salida();
		
		SQL _sql = new SQL();
		if (in.p_simularOperacion.equalsIgnoreCase("N")) {
			logger.debug("Actualizando " + in.p_CarterasNegativas.size() + " carteras negativas");
			int[] res = _sql.batch(conn, SQL_UPDATE_CARTERANEGATIVA, in.p_CarterasNegativas, CarteraNegativa_Entrada.class);
			for (int i = 0; i<in.p_CarterasNegativas.size();i++) {
				if (res[i] == 1) {
					logger.debug("Cartera Negativa " + in.p_CarterasNegativas.get(0).IdCarteraNegativa + " actualizada");
					String codigo = "UI_CARTERA_NEGATIVA_ANULADA";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdCarteraNegativa", in.p_CarterasNegativas.get(i).IdCarteraNegativa).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida((long)i, MensajeSalida.MENSAJE_INFORMACION, "UI_CARTERA_NEGATIVA_ANULADA", descripcion));
				}
			}
		}
		logger.info("Fin");
		return salida;
	}
	
	public static class AnularCarteraNegativa_Entrada {
	
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_simularOperacion = "N";
		
		public List<CarteraNegativa_Entrada> p_CarterasNegativas;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public AnularCarteraNegativa_Entrada() {
			p_CarterasNegativas = new ArrayList<CarteraNegativa_Entrada>();
		}
	}

	public static class CarteraNegativa_Entrada extends BcsPojo {
		public Long IdCarteraNegativa;
	}
	
	public static class AnularCarteraNegativa_Salida extends BcsPojo {
		public List<PlataformaUtils.MensajeSalida> 		P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public AnularCarteraNegativa_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
		public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		System.out.println("== END ==");
	}
	
}

	