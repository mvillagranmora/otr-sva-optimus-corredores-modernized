package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarLogCertificacionAporteRescate implements JavaCall{
	
	private final Logger logger =  LogManager.getLogger(AgregarLogCertificacionAporteRescate.class);
	
	private final String SQL_INSERT_LOG_CERTIFICACION = loadResourceToString("cl/bcs/fondomutuo/sql/AgregarLogCertificacionAporteRescate_InsertLogCertificacion.sql");
	
	public static class EntradaAgregarLogCertificacionAporteRescate{
		
		List<InLog> p_Log;
		
		public EntradaAgregarLogCertificacionAporteRescate(){
			this.p_Log = new ArrayList<InLog>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InLog{
		public Long IdSolicitudAporteRescate;
		public String Codigo;
		public String IdEstado;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public Long IdUsuario;
		public Long IdEmpresa;
		public String FechaSistema;
		public String IdSerieFondo;
		public String IdEmisor;
		public BigDecimal Monto;
		public String IdMoneda;
		public BigDecimal MontoOriginal;
		public BigDecimal TipoCambio;
		public String IdMonedaOriginal;
		public BigDecimal ValorCuota;
		public BigDecimal NumeroCuotas;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Simulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAgregarLogCertificacionAporteRescate{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAgregarLogCertificacionAporteRescate(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaAgregarLogCertificacionAporteRescate jin = gson.fromJson(in.toString(), EntradaAgregarLogCertificacionAporteRescate.class);
		SalidaAgregarLogCertificacionAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAgregarLogCertificacionAporteRescate logica (Connection con, EntradaAgregarLogCertificacionAporteRescate entrada) throws Exception {
		SalidaAgregarLogCertificacionAporteRescate salida = new SalidaAgregarLogCertificacionAporteRescate();
		SQL sql=new SQL();
		logger.info("INICIO::AgregarLogCertificacionAporteRescate");
		
		sql.batch(con, SQL_INSERT_LOG_CERTIFICACION, entrada.p_Log, InLog.class);
		
		logger.info("FIN::AgregarLogCertificacionAporteRescate");
		return salida;
	}

}
