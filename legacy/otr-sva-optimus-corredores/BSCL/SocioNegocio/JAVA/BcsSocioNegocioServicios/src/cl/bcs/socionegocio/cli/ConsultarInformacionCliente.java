package cl.bcs.socionegocio.cli;

import java.math.BigDecimal;
import java.sql.Connection;
//import java.sql.Date;
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.YearMonth;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
//import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
//import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
//import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarInformacionCliente implements JavaCall{
	
	private final Log log = new Log("ConsultarInformacionCliente");
	
	private final String SQL_CONSULTA_INFORMACION_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarInformacionCliente.sql");
	
	public static class EntradaConsultarInformacionCliente{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdPersonaEjecutivo = 0L;
		public String 	p_IdTipoPersona = "";	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class SalidaConsultarInformacionCliente{
		public List<SalidaInformacionCliente> P_OUT_TABLA;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public String P_OUT_MENSAJE = "";
		
		public SalidaConsultarInformacionCliente(){
			this.P_OUT_TABLA = new ArrayList<SalidaInformacionCliente>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaInformacionCliente{
		
		public Long 			IdPersona;
		public Long 			IdPersonaEjecutivo;
		public String 			DocumentoIdentidadCliente; 
		public String 			NombreCliente; 
		public String 			IdTipoPersona;
		public String 			NombreTipoPersona; 
		public String		 	FechaNacimiento; 
		public String 			Cargo;
		public String 			NombreEjecutivo; 
		public String 			DireccionElectronica;
		public String 			DocumentoIdentidadRepLegal;
		public String 			NombreRepLegal;
		public BigDecimal 		Patrimonio;
		public Long             IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class PortafoliosCliente{
		public Long             IdCliente;
		public Long             IdPortafolio;
	}	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarInformacionCliente jin = gson.fromJson(in.toString(), EntradaConsultarInformacionCliente.class);
		SalidaConsultarInformacionCliente out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarInformacionCliente logica(Connection con, EntradaConsultarInformacionCliente entrada) throws Exception {
		log.write("INICIO::ConsultarInformacionCliente");
		SalidaConsultarInformacionCliente out = new SalidaConsultarInformacionCliente();
		
		SQL sql=new SQL();
		
		try{
			
			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
			
			HashMap<String,Object> params =new HashMap<String,Object>();
			params.put("p_IdPersonaEjecutivo", entrada.p_IdPersonaEjecutivo);
			params.put("p_IdTipoPersona", entrada.p_IdTipoPersona);
			params.put("p_FechaSistema", FechaSistema);

			List<SalidaInformacionCliente> InformacionCliente = sql.query(con, SQL_CONSULTA_INFORMACION_CLIENTE, params, SalidaInformacionCliente.class);
			
			out.P_OUT_TABLA = InformacionCliente;
			
			
		}catch(Exception e){
			log.write("ERROR",e);
			throw e;
		}finally{}
		log.write("FIN.ConsultarInformacionCliente");
		return out;
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("== DESA ==");
		Connection con = PlataformaUtils.getDESAConnection();
		try {
			EntradaConsultarInformacionCliente entrada = new EntradaConsultarInformacionCliente();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "SOC";
			entrada.p_IdUsuario = 12L;
			entrada.p_IdPersonaEjecutivo = 214039L;//1774650L;//214039L;//1774650L;
			entrada.p_IdTipoPersona = "N";

			SalidaConsultarInformacionCliente Salida = new SalidaConsultarInformacionCliente();
			ConsultarInformacionCliente sp = new ConsultarInformacionCliente();
			Salida = sp.logica(con,entrada);
			System.out.println(Salida.P_OUT_MENSAJE);
			System.out.println(Salida.P_OUT_TABLA);
			con.commit();
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
		}finally{
			con.close();
		}
		System.out.println("== END ==");
	}
}
