package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaDocumentos implements JavaCall{
	private final Log log=new Log("SOC.ConsultaDocumentos");
	private final String SQL_SELECT_ANEXOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultaDocumentos_SelectAnexos.sql");
	private final String SQL_UPDATE_USUARIO_DESCARGA_DOCUMENTO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultaDocumentos_UpdateUsuarioDescargaDocumento.sql");
	
	public static class Entrada {
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public List<Documentos>	p_Documentos;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Documentos {
		public long IdDocumento;
		public long IdAnexo;
	}
	
	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<Documento_Salida> P_FILES;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Salida() {
			this.P_FILES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class Documento_Salida {
		public String encodedString;
		public String NombreArchivo;
		public String FormatoArchivo;
		public String resultado;
		public String mensaje;	
	}
	
	public static class DocumentoAnexo {
		public long IdDocumento;
		public long IdAnexo;
		public String NombreArchivoAdjunto;
		public String FormatoArchivoAdjunto;
		public byte[] ArchivoAdjunto;
	}
	
	public static class UsuarioDescargaDocumento {
	    public long IdUsuario;
	    public long IdAnexo;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida out=logica(con,in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public Salida logica(Connection con, Entrada in) throws Exception{
		SQL sql=new SQL();
		Salida out=new Salida();
		
		try {
			List<Long> idDocumentos = in.p_Documentos.stream().map(p -> p.IdDocumento).collect(Collectors.toList());
			List<Long> idAnexos = in.p_Documentos.stream().map(p -> p.IdAnexo).collect(Collectors.toList());
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdDocumentos", idDocumentos);
			params.put("IdAnexos", idAnexos);
			List<DocumentoAnexo> documentos = sql.query(con, SQL_SELECT_ANEXOS, params, DocumentoAnexo.class);

			List<Documento_Salida> archivosSalida = new ArrayList<Documento_Salida>();
			List<UsuarioDescargaDocumento> descargaDocumento = new ArrayList<>();
			for(Documentos doc : in.p_Documentos) {
				Documento_Salida docSalida = new Documento_Salida();
				DocumentoAnexo anexo = documentos.stream().filter(p -> p.IdAnexo == doc.IdAnexo).findFirst().get();
				if(anexo != null) {
					
					UsuarioDescargaDocumento descargaDocumentoUsuario = new UsuarioDescargaDocumento();
					descargaDocumentoUsuario.IdUsuario = in.p_IdUsuario;
					descargaDocumentoUsuario.IdAnexo = doc.IdAnexo;
					
					descargaDocumento.add(descargaDocumentoUsuario);
					
					docSalida.encodedString = new String(Base64.encodeBase64(anexo.ArchivoAdjunto));
					docSalida.NombreArchivo = anexo.NombreArchivoAdjunto;
					docSalida.FormatoArchivo = anexo.FormatoArchivoAdjunto;
					docSalida.resultado = "OK";
					docSalida.mensaje = "EXITO";
				}
				else {
					docSalida.resultado = "NOK";
					docSalida.mensaje = "ERROR";
				}
				archivosSalida.add(docSalida);
			}
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_FILES = archivosSalida;
			
			if(!descargaDocumento.isEmpty()) {
			    sql.batch(con, SQL_UPDATE_USUARIO_DESCARGA_DOCUMENTO, descargaDocumento, UsuarioDescargaDocumento.class);
			}
			
		}
		catch(Exception e) {
			log.write("Error excepcion generar archivo",e);
			out.P_OUT_ERRORESCONTADOR=1L;
			return out;
		}
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_Documentos\":[{\"IdDocumento\":243, \"IdAnexo\":299}]}";
				Entrada Objin = gson.fromJson(json, Entrada.class);				
				ConsultaDocumentos demo=new ConsultaDocumentos();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			Salida ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.commit();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		
		System.out.println("== END ==");
	}	
}
