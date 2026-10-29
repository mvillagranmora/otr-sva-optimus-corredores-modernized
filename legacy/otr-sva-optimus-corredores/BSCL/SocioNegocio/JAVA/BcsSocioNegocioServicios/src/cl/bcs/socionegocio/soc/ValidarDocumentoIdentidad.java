package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para validar la existencia de documentos de identida asignados a distintos tipos de socio de negocio.
 * @author chernandez
 *
 */
public class ValidarDocumentoIdentidad implements JavaCall {
	
	private Logger logger = LogManager.getLogger(ValidarDocumentoIdentidad.class);
	
	public String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/soc/sql/ValidarDocumentoIdentidad_SelectDocumentoIdentidad.sql");
	public String SQL_SELECT_ID_PAIS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ValidarDocumentoIdentidad_SelectIdPais.sql");
	private static final Pattern PATTERN = Pattern.compile("^[0-9]+-[0-9kK]{1}$");	

	public static class Pais{
		public Long IdPais;
	}
	public static class EntradaValidarDocumentoIdentidad{
		public long p_IdEmpresa;
		public String p_Entidad;
		public String p_TipoDocumentoIdentidad;
		public String p_DocumentoIdentidad;
		public String p_TipoPersona;		
	}
	public static class SalidaValidarDocumentoIdentidad{
		public long IdPais;
		public String Valido;
		public String CodigoPais;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Existe{
		public long Contador;
	}
	
	private boolean ValidarDocumentoIdentidad(String TipoDocumentoIdentidad, String DocumentoIdentidad) {
		if("R".equals(TipoDocumentoIdentidad)) {
			Matcher matcher = PATTERN.matcher(DocumentoIdentidad);
			if ( matcher.matches() == false ) return false;
			String[] stringRut = DocumentoIdentidad.split("-");
			return stringRut[1].toLowerCase().equals(this.ObtenerDV(stringRut[0]));
		}
		return true;
	}
		
	private String ObtenerDV(String rut) {
		Integer M=0,S=1,T=Integer.parseInt(rut);
		for (;T!=0;T=(int) Math.floor(T/=10))
			S=(S+T%10*(9-M++%6))%11;
		return ( S > 0 ) ? String.valueOf(S-1) : "k";
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Funcion para identificar si existe un documento de identidad especifico asociado a un tipo de socio de negocio.
	 * @param conn
	 * @param idEmpresa
	 * @param entidad
		 * PERSONA = BUSCA SI UNA PERSONA TIENE ASIGNADO EL DOCUMENTO DE IDENTIDAD como principal
		 * CLIENTE = BUSCA SI UN CLIENTE TIENE EL DOCUMENTO DE IDENTIDAD
		 * EMPLEADO = lo mismo
		 * CONTRAPARTE = lo mismo
		 * PROVEEDOR =  lo mismo 
	 * 
	 * @param tipoDocumento
	 * @param documentoIdentidad
	 * @param tipoPersona
	 * @return
	 * @throws Exception
	 */
	public SalidaValidarDocumentoIdentidad logica(Connection conn, long idEmpresa, String entidad, String tipoDocumento, String documentoIdentidad, String tipoPersona) throws Exception{
		SQL sql = new SQL();

		String vista = null;
		String CodigoPais = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.LocalizacionSistema");
		
		if(entidad.equals("PERSONA")){
			vista = "BSCL.SocioNegocio.BD::PER.Persona";
		}else if(entidad.equals("CLIENTE")){
			vista = "BSCL.SocioNegocio.BD::CLI.Cliente";
		}else if(entidad.equals("EMPLEADO")){
			vista = "BSCL.SocioNegocio.BD::EMP.Empleado";
		}else if(entidad.equals("CONTRAPARTE")){
			vista = "BSCL.SocioNegocio.BD::CNT.Contraparte";
		}else if(entidad.equals("PROVEEDOR")){
			vista = "BSCL.SocioNegocio.BD::PRO.Proveedor";
		}
		
		String queryValidaDocumentoIdentidad = SQL_SELECT_DOCUMENTO_IDENTIDAD.replaceAll("::VISTA::", vista);
		
		HashMap<String, Object> params = new HashMap<String, Object>(4);
		params.put("DocumentoIdentidad", documentoIdentidad);
		params.put("TipoDocumentoIdentidad", tipoDocumento);
		params.put("CodigoPais", CodigoPais);
		params.put("TipoPersona", tipoPersona);

		SalidaValidarDocumentoIdentidad salida = new SalidaValidarDocumentoIdentidad();
		logger.debug("INICIO::SQL_SELECT_DOCUMENTO_IDENTIDAD");
		List<Existe> result = sql.query(conn, queryValidaDocumentoIdentidad, params, Existe.class);
		logger.debug("FIN::SQL_SELECT_DOCUMENTO_IDENTIDAD");
		//obtener pais
		params = new HashMap<String,Object>(1);
		params.put("CodigoPais", CodigoPais);
		logger.debug("INICIO::SQL_SELECT_ID_PAIS");
		List<Pais> paises = sql.query(conn, SQL_SELECT_ID_PAIS, params, Pais.class);
		logger.debug("FIN::SQL_SELECT_ID_PAIS");

		if(!ValidarDocumentoIdentidad(tipoDocumento,documentoIdentidad)) {
			System.out.println("LOG::INGRESO UN RUT CON PUNTOS");
			salida.Valido = "N";
			return salida;
		}

		for(Existe ex : result){
			
			salida.IdPais = (paises!=null && paises.size()>0) ? paises.get(0).IdPais : 0;
			salida.CodigoPais = CodigoPais;
			salida.Valido = (ex.Contador>=1) ? "N" : "S";
		}
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			EntradaValidarDocumentoIdentidad in = new EntradaValidarDocumentoIdentidad();
			in.p_IdEmpresa = 1;
			in.p_Entidad = "CLIENTE";
			in.p_TipoDocumentoIdentidad = "IUP";
			in.p_DocumentoIdentidad = "02103085790000";
			in.p_TipoPersona = "N";	
			ValidarDocumentoIdentidad demo = new ValidarDocumentoIdentidad();
			System.out.println("ENTRADA");
			
			try {
				SalidaValidarDocumentoIdentidad ObjOut = demo.logica(con, in.p_IdEmpresa, in.p_Entidad, in.p_TipoDocumentoIdentidad, in.p_DocumentoIdentidad, in.p_TipoPersona);
				// con.commit();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				 er.printStackTrace();
				con.rollback();
			}
			// con.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
