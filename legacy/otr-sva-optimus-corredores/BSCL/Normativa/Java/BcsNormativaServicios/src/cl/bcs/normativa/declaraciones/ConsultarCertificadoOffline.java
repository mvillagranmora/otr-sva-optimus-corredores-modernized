package cl.bcs.normativa.declaraciones;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCertificadoOffline implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarCertificadoOffline.class);
	private final String SQL_SELECT_CERTIFICADOS = loadResourceToString("cl/bcs/normativa/declaraciones/sql/ConsultarCertificadoOffline_Select.sql");
	
	public static class ConsultarCertificadoOffline_Entrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		public Long				p_IdCliente;
		public Long				p_IdPortafolio;
		public String			p_IdTipoCertificado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ConsultarCertificadoOffline_Salida {
		public Long 						P_OUT_ERRORESCONTADOR;
		public List<Certificado_Salida>		P_FILES;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCertificadoOffline_Salida() {
			this.P_FILES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Certificado_Salida {
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombreArchivo;
		public String CertificadoDecoded;
		public byte[] CertificadoBytes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		ConsultarCertificadoOffline_Entrada in = gson.fromJson(jin.toString() ,ConsultarCertificadoOffline_Entrada.class);
		ConsultarCertificadoOffline_Salida	out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarCertificadoOffline_Salida logica(Connection con, ConsultarCertificadoOffline_Entrada in) throws Exception {
		ConsultarCertificadoOffline_Salida out=new ConsultarCertificadoOffline_Salida();

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdCliente", in.p_IdCliente);
		params.put("IdPortafolio", in.p_IdPortafolio);
		params.put("IdEmpresa",in.p_IdEmpresa);
		params.put("IdTipoCertificado",in.p_IdTipoCertificado);
		
		List<Certificado_Salida> salida = sql.query(con, SQL_SELECT_CERTIFICADOS, params, Certificado_Salida.class);
		for (Certificado_Salida s : salida) {
			s.CertificadoDecoded = new String(Base64.encodeBase64(s.CertificadoBytes));
		}
		out.P_FILES = salida;
		return out;
	}
	

	public static void main(String[] args) throws Exception {
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("mbi")) {
			ConsultarCertificadoOffline_Entrada e = new ConsultarCertificadoOffline_Entrada();
			e.p_IdCliente = 65L;
			e.p_IdPortafolio = 4352L;
			e.p_IdEmpresa = 1L;
			e.p_IdTipoCertificado = "CERT21";
			e.p_IdUsuario = 9999L;
			e.p_IdModulo = "ORD";
			ConsultarCertificadoOffline c = new ConsultarCertificadoOffline();
			ConsultarCertificadoOffline_Salida salida = c.logica(con, e);
			System.out.println(salida);
			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		
		System.out.println("== END ==");
	}		
}
