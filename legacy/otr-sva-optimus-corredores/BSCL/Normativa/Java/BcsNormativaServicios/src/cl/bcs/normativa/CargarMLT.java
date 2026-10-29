package cl.bcs.normativa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.normativa.IngresarMLT.ingresarMLTEntrada;
import cl.bcs.normativa.IngresarMLT.ingresarMLTSalida;

public class CargarMLT implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarMLT.class);
	
	
	private final String SQL_SELECT_SOLICITUD = loadResourceToString("cl/bcs/normativa/sql/CargarMLT_ConsultarSolicitudApoResc.sql");
	
	
	public static class EntradaCargarMLT extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<MLT> p_MLT;
		
		public EntradaCargarMLT() {
			this.p_MLT = new ArrayList<MLT>();
		}
		
	}
	
	public static class MLT extends BcsPojo{
		public Long MLT_FolioMandato;
		public Long MLT_IdSolicitudAporteRescate;
		public BigDecimal MLT_Valor;
		public String MENSAJEVALIDACION;
		
		
	}
	
	public static class RegistroSolicitud{
		public Long 	IdSolicitudAporteRescate;
		public Long		FolioContrato;
		public BigDecimal 	RentabilidadReal;
		public BigDecimal 	CostoUF;
		public String 	Regimen="";
		
		public String 	Observacion="NO EXISTE";
		
		
		
		public RegistroSolicitud() {
			this.IdSolicitudAporteRescate = 0L;
			this.FolioContrato = 0L;
		}
		 
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
		
	
	public static class ResultadoCargaMLT extends BcsPojo{
		public Long 	IdSolicitudAporteRescate;
		public Long		FolioMandato;
		public BigDecimal 	Valor;
		public String 	Regimen="";
		
		public String 	Observacion="NO EXISTE";
		public ResultadoCargaMLT() {
			this.Valor = BigDecimal.ZERO;
		}
	}

	
	public static class SalidaCargarMLT extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoCargaMLT> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarMLT() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCargaMLT>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCargarMLT jin = gson.fromJson(in.toString(), EntradaCargarMLT.class);
		SalidaCargarMLT out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarMLT logica(Connection con, EntradaCargarMLT entrada) throws Exception{
		logger.info("INICIO::CargarMLT");
		SalidaCargarMLT out = new SalidaCargarMLT();
		SQL sql=new SQL();
		
		List<Long> IdSolicitudAporteRescate = new ArrayList<Long>();
		for(MLT in : entrada.p_MLT){
			if(!IdSolicitudAporteRescate.contains(in.MLT_IdSolicitudAporteRescate)) IdSolicitudAporteRescate.add(in.MLT_IdSolicitudAporteRescate);
		}
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSolicitudAporteRescate", IdSolicitudAporteRescate);
		
		List<RegistroSolicitud> Solicitudes = sql.query(con, SQL_SELECT_SOLICITUD, params, RegistroSolicitud.class);
		
		List<ResultadoCargaMLT> Resultado = new ArrayList<ResultadoCargaMLT>();
		for(MLT en : entrada.p_MLT){
			ResultadoCargaMLT res = new ResultadoCargaMLT();
			
			res.FolioMandato=en.MLT_FolioMandato;
			res.IdSolicitudAporteRescate=en.MLT_IdSolicitudAporteRescate;
			res.Regimen="";
			res.Valor=en.MLT_Valor;
			res.Observacion="NO EXISTE";
			for(RegistroSolicitud sol : Solicitudes){
				if ((long)sol.IdSolicitudAporteRescate== (long)en.MLT_IdSolicitudAporteRescate)
				{
					res.Regimen=sol.Regimen;
					res.Observacion="OK";
					if("N".equals(entrada.p_SimularOperacion)) {
						
						IngresarMLT ingresoMLT = new IngresarMLT();	
						ingresarMLTEntrada entradaIngresarMLT = new ingresarMLTEntrada();
						entradaIngresarMLT.p_FolioMandato=en.MLT_FolioMandato;
						entradaIngresarMLT.p_IdEmpresa=entrada.p_IdEmpresa;
						entradaIngresarMLT.p_IdSolicitudAporteRescate=en.MLT_IdSolicitudAporteRescate;
						entradaIngresarMLT.p_IdUsuario=entrada.p_IdUsuario;
						entradaIngresarMLT.p_valor=en.MLT_Valor;
						ingresarMLTSalida sal = ingresoMLT.logica(con, entradaIngresarMLT);
						
						out.P_OUT_ERRORESCONTADOR += sal.P_OUT_ERRORESCONTADOR;
						res.Observacion="PROCESADO";
					}
				}
			}
			Resultado.add(res);
		}
		
		out.P_OUT_RESULTADO=Resultado;
		
		logger.info("FIN::CargarMLT");
		return out;
	}
	

	
	
	
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
				
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"NOR\",\"p_UsaReglasNegocio\":\"N\",\"p_SimularOperacion\":\"N\",\"p_MLT\":[{\"MLT_FolioMandato\":10,\"MLT_IdSolicitudAporteRescate\":661778,\"MLT_Valor\":1693.8561,\"MENSAJEVALIDACION\":\"OK\"},{\"MLT_FolioMandato\":11,\"MLT_IdSolicitudAporteRescate\":1026,\"MLT_Valor\":1680.8561,\"MENSAJEVALIDACION\":\"OK\"}]}";
		EntradaCargarMLT Objin = gson.fromJson(json, EntradaCargarMLT.class);
		CargarMLT demo = new CargarMLT();				
		try {
			SalidaCargarMLT ObjOut=demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	}
		System.out.println("== END ==");
	}
}
