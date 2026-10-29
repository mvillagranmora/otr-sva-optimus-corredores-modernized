package cl.bcs.normativa.circulares.ReportesBCC;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarProgramaRepoSicam implements JavaCall {
	private final Log log = new Log("AgregarProgramaRepoSicam");
	private final String SQL_SELECT_ID_PROCESO_RESUMEN_GENERACION = "SELECT MAX(\"ID\") + 1 FROM \"BSCL.Normativa.BD::TRI.ResumenGeneracion\"";
	private final String SQL_SELECT_DOCUMENTO = "SELECT TOP 1 \"IdDocumento\", \"IdTipoDocumento\", \"Esquema\", \"Version\", \"IdEmpresa\" FROM BSCL.\"BSCL.Normativa.BD::DOC.Documento\" WHERE \"Esquema\"= :p_IdEsquema AND \"Version\" = :p_Version";
	
	public static class Entrada extends BcsPojo{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_IdEsquema;
		public String 		p_Version;
		public String 		p_FechaInicio;
		public String 		p_FechaFin;
			
	} 
	
	public static class Documento extends BcsPojo{
		public Long 	IdDocumento;
		public String 	IdTipoDocumento;
		public String 	Esquema;
		public String 	Version;
		public Long 	IdEmpresa;
	    
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	public static class SalidaPrograma {
		public Long						P_OUT_IDNOMINA;
		public Long						P_OUT_IDPROGRAMA;
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long 					P_OUT_IDAGENDA;
		
		public SalidaPrograma() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		SalidaPrograma out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	private  SalidaPrograma logica (Connection con, Entrada in) throws Exception{	
		SalidaPrograma out = new SalidaPrograma();
		try {
			log.write("Inicio del proceso ingresar programa");
			System.out.println("MLT::AGREGAR PROGRAMAS INICIO");
			HashMap<String, Object> params = new HashMap<>(2);
			params.put("p_IdEsquema", in.p_IdEsquema);
			params.put("p_Version", in.p_Version); 
			List<Documento> doc = new SQL().query(con, SQL_SELECT_DOCUMENTO, params, Documento.class);
			//00 VARIABLES DE SISTEMA
			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

	
					
								
			//02 INSCRIBIR PROGRAMAS
			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdEmpresa.NombreParametro 	= "IdEmpresa";
			paramIdEmpresa.Valor			= in.p_IdEmpresa.toString();
			paramsParametros.add(paramIdEmpresa);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdUsuario.NombreParametro 	= "IdUsuario";
			paramIdUsuario.Valor			= in.p_IdUsuario.toString();
			paramsParametros.add(paramIdUsuario);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramidModulo = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramidModulo.NombreParametro 	= "IdModulo";
			paramidModulo.Valor				= in.p_IdModulo.toString();
			paramsParametros.add(paramidModulo);
				
			AgregarPrograma.InAgregarPrograma.Parametro	paramFechaIncio = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramFechaIncio.NombreParametro 	= "FechaIncio";
			paramFechaIncio.Valor				= in.p_FechaInicio;
			paramsParametros.add(paramFechaIncio);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramFechaFin = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramFechaFin.NombreParametro 	= "FechaFin";
			paramFechaFin.Valor				= in.p_FechaFin;
			paramsParametros.add(paramFechaFin);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdTipoDocumento = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdTipoDocumento.NombreParametro 	= "IdTipoDocumento";
			paramIdTipoDocumento.Valor				= doc.get(0).IdTipoDocumento;
			paramsParametros.add(paramIdTipoDocumento);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramVersionArchivo = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramVersionArchivo.NombreParametro 	= "VersionArchivo";
			paramVersionArchivo.Valor				= in.p_Version;
			paramsParametros.add(paramVersionArchivo);
			
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramCodigoPrograma = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramCodigoPrograma.NombreParametro 	= "CodigoPrograma";
			paramCodigoPrograma.Valor				= doc.get(0).IdTipoDocumento;
			paramsParametros.add(paramCodigoPrograma);
						
			AgregarPrograma.InAgregarPrograma.Parametro	paramEsquemaDJ = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramEsquemaDJ.NombreParametro 	= "Esquema";
			paramEsquemaDJ.Valor				= in.p_IdEsquema;
			paramsParametros.add(paramEsquemaDJ);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramVersionDJ = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramVersionDJ.NombreParametro 	= "Version";
			paramVersionDJ.Valor				= in.p_Version;
			paramsParametros.add(paramVersionDJ);
						
			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
			paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
			paramsIn.p_IdModulo			= in.p_IdModulo;
			paramsIn.p_IdUsuario		= in.p_IdUsuario;
			paramsIn.p_IdTipoPrograma	= "REPO_SICAM_BCCH";
			paramsIn.p_Segundos			= 0L;
			paramsIn.p_FechaSistema		= FechaSistema; 
			paramsIn.p_Parametros		= paramsParametros;
			AgregarPrograma AP = new AgregarPrograma();
			AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
			
			out.P_OUT_IDPROGRAMA		= result.P_OUT_IDPROGRAMA;
			out.P_OUT_ERRORESCONTADOR 	= result.P_OUT_ERRORESCONTADOR;
			System.out.println("MLT::AGREGAR PROGRAMAS FIN");
			if ( out.P_OUT_ERRORESCONTADOR.equals(1L) ){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, result.P_OUT_MENSAJE, new HashMap<>() ,con, in.p_IdEmpresa));
			} else {
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "PROGRAMA_INGRESADO", new HashMap<String,String>() ,con,in.p_IdEmpresa));
			};
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, e.getMessage(), new HashMap<>() ,con, in.p_IdEmpresa));
			log.write("== ERROR ==",e);
			throw e;
		}	
		return out;
	};
	
	public static void main(String[] args) throws Exception, SQLException {
		Connection con = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			//(con = PlataformaUtils.getPRUEBAConnection();
			con = PlataformaUtils.getPropertiesConnection("desa01", "C:\\GIT\\OPTIMUS\\optimus-corredores\\connections.properties");
		
			String json1 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"NOR\",\"p_IdEsquema\":\"REPO3P1\",\"p_Version\":\"2024/06\",\"p_FechaInicio\":\"2024-06-14\",\"p_FechaFin\":\"2024-06-14\"}";
			
			Entrada objIn = gson.fromJson(json1, Entrada.class);

			AgregarProgramaRepoSicam demo = new AgregarProgramaRepoSicam();

			SalidaPrograma ObjOut = demo.logica(con, objIn);

			System.out.println(ObjOut.toString());
			//con.commit();
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			con.close();
		}
		System.out.println("== END ==");

	};
	
}


