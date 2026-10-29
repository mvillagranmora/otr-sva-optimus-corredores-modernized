package cl.bcs.socionegocio.por;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarCrearPreferenciaPago implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConfirmarCrearPreferenciaPago.class);
	
	private String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/por/sql/SelectConfirmarCrearPreferenciaPago_SelectPreferenciaPagoPortafolio.sql");
	private String SQL_SELECT_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/socionegocio/por/sql/SelectConfirmarCrearPreferenciaPago_SelectPreferenciaPago.sql");
	private String SQL_UPDATE_CONFIRMAR_CREAR_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/socionegocio/por/sql/UpdateConfirmarCrearPreferenciaPago_UpdatePreferenciaPagoPortafolio.sql");
	private String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA_NO_DEFECTO = loadResourceToString("cl/bcs/socionegocio/por/sql/UpdateConfirmarCrearPreferenciaPago_UpdatereferenciaPagoPortafolioNoDefecto.sql");
	
	public static class EntradaConfirmarCrearPreferenciaPago extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPreferenciaPagoPortafolio;
		public String p_IdEstadoAutorizacion;
	}
	
	public static class SalidaConfirmarCrearPreferenciaPago extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
        public SalidaConfirmarCrearPreferenciaPago() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<>();
        }
	}
	
	public static class PreferenciaPago extends BcsPojo{
		public String IdPreferenciaPago;
		public String IdTipoOperacion;
		public String IdMedioDePago;
		public String IdMoneda;
		public String UsaPersonaRelacionada;
		public java.sql.Timestamp FechaCreacion;
		public java.sql.Timestamp FechaModificacion;
		public String IdEstado;
	}
	
	public static class PreferenciaPagoPreferenciaPagoPortafolio extends BcsPojo{
		public Long IdPortafolio;
		public String IdMoneda;
		public Long IdEmpresa;
		public String IdTipoOperacion;
		public Long IdPreferenciaPagoPortafolio;
	}  

	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarCrearPreferenciaPago jin = gson.fromJson(in.toString(), EntradaConfirmarCrearPreferenciaPago.class);
		SalidaConfirmarCrearPreferenciaPago out = logica(con,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarCrearPreferenciaPago logica(Connection con, EntradaConfirmarCrearPreferenciaPago entrada) throws Exception{
        logger.info("INICIO::ConfirmarCrearPreferenciaPago");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        SalidaConfirmarCrearPreferenciaPago out = new SalidaConfirmarCrearPreferenciaPago();
        PreferenciaPagoPortafolio preferenciaPagoPortafolio = new PreferenciaPagoPortafolio();
        PreferenciaPago preferenciaPago = new PreferenciaPago();
        
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p_IdPreferenciaPagoPortafolio", entrada.p_IdPreferenciaPagoPortafolio);
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, params, PreferenciaPagoPortafolio.class);
		
		if(preferenciasPagoPortafolio.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PREFERENCIA_PAGO_PORTAFOLIO_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		} else {
			preferenciaPagoPortafolio = preferenciasPagoPortafolio.get(0);
		}
		
		params = new HashMap<String, Object>();
		params.put("p_IdPreferenciaPago", preferenciaPagoPortafolio.IdPreferenciaPago);
		params.put("p_IdEmpresa", preferenciaPagoPortafolio.IdEmpresa);
		List<PreferenciaPago> preferenciasPago = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO, params, PreferenciaPago.class);
		
		if(preferenciasPago.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PREFERENCIA_PAGO_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		} else {
			preferenciaPago = preferenciasPago.get(0);
		}
		
		sql.batch(con, SQL_UPDATE_CONFIRMAR_CREAR_PREFERENCIA_PAGO, preferenciasPagoPortafolio, PreferenciaPagoPortafolio.class);	
		

		if("S".equals(preferenciaPagoPortafolio.PorDefecto)) {
			PreferenciaPagoPreferenciaPagoPortafolio preferenciaPagoPreferenciaPagoPortafolio = new PreferenciaPagoPreferenciaPagoPortafolio();
			preferenciaPagoPreferenciaPagoPortafolio.IdPortafolio = preferenciaPagoPortafolio.IdPortafolio;
			preferenciaPagoPreferenciaPagoPortafolio.IdEmpresa = preferenciaPagoPortafolio.IdEmpresa;
			preferenciaPagoPreferenciaPagoPortafolio.IdTipoOperacion = preferenciaPago.IdTipoOperacion;
			preferenciaPagoPreferenciaPagoPortafolio.IdMoneda = preferenciaPago.IdMoneda;
			preferenciaPagoPreferenciaPagoPortafolio.IdPreferenciaPagoPortafolio = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
			
			List<PreferenciaPagoPreferenciaPagoPortafolio> preferenciasPagoPreferenciaPagoPortafolio = new ArrayList<PreferenciaPagoPreferenciaPagoPortafolio>();
			preferenciasPagoPreferenciaPagoPortafolio.add(preferenciaPagoPreferenciaPagoPortafolio);
			
			sql.batch(con, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA_NO_DEFECTO, preferenciasPagoPreferenciaPagoPortafolio, PreferenciaPagoPreferenciaPagoPortafolio.class);	
		}
        long fin = System.currentTimeMillis();
        logger.info("FIN::ConfirmarCrearPreferenciaPago", fin - inicio);
        return out;
	}

}
