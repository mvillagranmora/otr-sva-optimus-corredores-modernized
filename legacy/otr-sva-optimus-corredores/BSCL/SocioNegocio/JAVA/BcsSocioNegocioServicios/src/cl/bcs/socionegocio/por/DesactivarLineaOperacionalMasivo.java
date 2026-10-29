package cl.bcs.socionegocio.por;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class DesactivarLineaOperacionalMasivo implements JavaCall {
	private static final Logger logger = LogManager.getLogger(DesactivarLineaOperacionalMasivo.class);

	private final String SQL_UPDATE_DESACTIVAR_LIMITE_SOBREGIRO = loadResourceToString("cl/bcs/socionegocio/por/sql/DesactivarLineaOperacionalMasivo_UpdateLimiteSobregiro.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class SalidaDesactivarLineaOperacionalMasivo extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVENTENCIASCONTADOR;
		
		public SalidaDesactivarLineaOperacionalMasivo() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVENTENCIASCONTADOR = 0L;
		}
	}

	public static class EntradaDesactivarLineaOperacionalMasivo extends BcsPojo {
		public Long     p_IdEmpresa;
		public Long     p_IdUsuario;
		public String	p_IdModulo;
		
		public EntradaDesactivarLineaOperacionalMasivo() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "";
		}
	}
	
	public SalidaDesactivarLineaOperacionalMasivo logica(Connection con, EntradaDesactivarLineaOperacionalMasivo entrada) throws Exception {
	    logger.info("INICIO::DesactivarLineaOperacionalMasivo");
	    long inicio = System.currentTimeMillis();

	    SalidaDesactivarLineaOperacionalMasivo salida = new SalidaDesactivarLineaOperacionalMasivo(); 
	    
	    RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
	    RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);

	    if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
	        salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
	        return salida;
	    }

	    try {
	        logger.info("INICIO::DesactivarLineaOperacionalMasivo: DESACTIVACION DE LIMITE SOBREGIRO");
	        SQL.exec(con, SQL_UPDATE_DESACTIVAR_LIMITE_SOBREGIRO);
	        logger.info("FIN::DesactivarLineaOperacionalMasivo:    DESACTIVACION DE LIMITE SOBREGIRO");
	    } catch (Exception e) {
	        logger.error("ERROR::DesactivarLineaOperacionalMasivo: ", e);
	        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DESACTIVAR_LINEA_OPERACIONAL", new HashMap<>(), con, entrada.p_IdEmpresa));
	    }

	    salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::DesactivarLineaOperacionalMasivo - Tiempo de ejecución: " + (fin - inicio) + " ms");

	    return salida;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			DesactivarLineaOperacionalMasivo servicio = new DesactivarLineaOperacionalMasivo();
			
			String entrada = "{ \"p_IdEmpresa\" : 1, \"p_IdUsuario\" : 97, \"p_IdModulo\" : \"PLA\" } ";
			
			EntradaDesactivarLineaOperacionalMasivo  Objin = gson.fromJson(entrada, EntradaDesactivarLineaOperacionalMasivo.class);
			 
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			try {
				SalidaDesactivarLineaOperacionalMasivo salida = servicio.logica(con, Objin);
				System.out.println("SALIDA");
				System.out.println(salida.toString());
				
				con.rollback();
			}catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
			
		} catch(Exception er) {
			er.printStackTrace();
		}
		
		System.out.println("== END ==");
	}
}
