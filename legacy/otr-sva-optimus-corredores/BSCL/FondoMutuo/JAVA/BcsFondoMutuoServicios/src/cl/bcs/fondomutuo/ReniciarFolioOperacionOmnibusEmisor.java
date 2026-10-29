package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConfiguracionEmisorOmnibus;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReniciarFolioOperacionOmnibusEmisor implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ReniciarFolioOperacionOmnibusEmisor.class);
	
	private final String SQL_SELECT_CONFIGURACION_EMISOR_OMNIBUS_REINICIO_DIARIO = loadResourceToString("cl/bcs/fondomutuo/sql/ReniciarFolioOperacionOmnibusEmisor_SelectConfiguracionEmisorOmnibusReinicioDiario.sql");
	private final String SQL_UPDATE_REINICIO_FOLIO_OPERACION = loadResourceToString("cl/bcs/fondomutuo/sql/ReniciarFolioOperacionOmnibusEmisor_UpdateReinicioFolioOperacion.sql");
	
	public static class EntradaReniciarFolioOperacionOmnibusEmisor extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	}
	
	public static class SalidaReniciarFolioOperacionOmnibusEmisor extends BcsPojo {
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaReniciarFolioOperacionOmnibusEmisor() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaReniciarFolioOperacionOmnibusEmisor in = gson.fromJson(jin.toString(), EntradaReniciarFolioOperacionOmnibusEmisor.class);
		SalidaReniciarFolioOperacionOmnibusEmisor out = logica(conn, in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaReniciarFolioOperacionOmnibusEmisor logica(Connection con, EntradaReniciarFolioOperacionOmnibusEmisor entrada) throws Exception {
		logger.info("INICIO::ReniciarFolioOperacionOmnibusEmisor");
		long inicioServicio = System.currentTimeMillis();
		SalidaReniciarFolioOperacionOmnibusEmisor out = new SalidaReniciarFolioOperacionOmnibusEmisor();
		
		List<ConfiguracionEmisorOmnibus> configuracionesEmisorOmnibusReinicioDiario = obtenerConfiguracionesConReinicioDiario(con);
		reiniciarFolioOperacion(con, configuracionesEmisorOmnibusReinicioDiario);

		out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "FOLIO_OPERACION_CONFIGURACION_OMNIBUS_REINICIADOS", new HashMap<String, String>(1), con, entrada.p_IdEmpresa));
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ReniciarFolioOperacionOmnibusEmisor", finServicio - inicioServicio);
		return out;
	}
	
	private List<ConfiguracionEmisorOmnibus> obtenerConfiguracionesConReinicioDiario(Connection con) throws Exception {
		logger.debug("INICIO::ReniciarFolioOperacionOmnibusEmisor.obtenerConfiguracionesConReinicioDiario");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<ConfiguracionEmisorOmnibus> configuracionesEmisorOmnibusReinicioDiario = sql.query(con, SQL_SELECT_CONFIGURACION_EMISOR_OMNIBUS_REINICIO_DIARIO, new HashMap<String, Object>(1), ConfiguracionEmisorOmnibus.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReniciarFolioOperacionOmnibusEmisor.obtenerConfiguracionesConReinicioDiario", fin - inicio);
		return configuracionesEmisorOmnibusReinicioDiario;
	}
	
	private void reiniciarFolioOperacion(Connection con, List<ConfiguracionEmisorOmnibus> configuracionesEmisorOmnibusReinicioDiario) throws Exception {
		logger.debug("INICIO::ReniciarFolioOperacionOmnibusEmisor.obtenerConfiguracionesConReinicioDiario");
		long inicio = System.currentTimeMillis();
		
		configuracionesEmisorOmnibusReinicioDiario.stream().forEach(configuracion->{
			configuracion.FolioOperacion = 0L;
		});
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_REINICIO_FOLIO_OPERACION, configuracionesEmisorOmnibusReinicioDiario, ConfiguracionEmisorOmnibus.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReniciarFolioOperacionOmnibusEmisor.obtenerConfiguracionesConReinicioDiario", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaReniciarFolioOperacionOmnibusEmisor entradaReniciarFolioOperacionOmnibusEmisor = new EntradaReniciarFolioOperacionOmnibusEmisor();
			entradaReniciarFolioOperacionOmnibusEmisor.p_IdEmpresa = 1L;
			entradaReniciarFolioOperacionOmnibusEmisor.p_IdUsuario = 21L;
			entradaReniciarFolioOperacionOmnibusEmisor.p_IdModulo = "PLA";

			ReniciarFolioOperacionOmnibusEmisor reniciarFolioOperacionOmnibusEmisor = new ReniciarFolioOperacionOmnibusEmisor();
			try {
				SalidaReniciarFolioOperacionOmnibusEmisor salidaReniciarFolioOperacionOmnibusEmisor = reniciarFolioOperacionOmnibusEmisor.logica(con, entradaReniciarFolioOperacionOmnibusEmisor);
				con.rollback();
				System.out.println(salidaReniciarFolioOperacionOmnibusEmisor.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
	}

}
