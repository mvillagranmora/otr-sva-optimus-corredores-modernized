package cl.bcs.ordenes;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.DocumentoInstruccion;
import cl.bcs.ordenes.pojo.Instruccion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerDocumentoInstruccion implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ObtenerDocumentoInstruccion.class);
	public String SQL_SELECT_ADJUNTO = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDocumentoInstruccion_SelectAdjunto.sql");
	public String SQL_SELECT_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDocumentoInstruccion_SelectInstruccion.sql");


	public static class EntradaObtenerDocumentoInstruccion extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdTipoCanalIngreso;
		public DocumentoInstruccion p_DocumentoInstruccion;
	}
	
	public static class SalidaObtenerDocumentoInstruccion extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public String encoded;
		
		public SalidaObtenerDocumentoInstruccion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.encoded = "";
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaObtenerDocumentoInstruccion in = gson.fromJson(jin.toString(), EntradaObtenerDocumentoInstruccion.class);
		SalidaObtenerDocumentoInstruccion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private SalidaObtenerDocumentoInstruccion logica(Connection con, EntradaObtenerDocumentoInstruccion in) throws Exception {
		logger.info("INICIO::ObtenerDocumentoInstruccion");
		long inicioServicio = System.currentTimeMillis();

		SalidaObtenerDocumentoInstruccion salida = new SalidaObtenerDocumentoInstruccion();
		List<Instruccion> instruccion = obtenerInstruccion(con, in.p_DocumentoInstruccion);

		if (instruccion.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_INSTRUCCION_NO_EXISTE",
					null, con, in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}

		PreparedStatement pstmt = con.prepareStatement(SQL_SELECT_ADJUNTO);
		pstmt.setLong(1, in.p_DocumentoInstruccion.IdDocumentoInstruccion);
		ResultSet rs = pstmt.executeQuery();
		Blob blob = rs.next() ? rs.getBlob(1) : null;
		InputStream is = blob.getBinaryStream();
		byte[] bytes = IOUtils.toByteArray(is);
		String encoded = Base64.getEncoder().encodeToString(bytes);
		salida.encoded = encoded;

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ObtenerDocumentoInstruccion", finServicio - inicioServicio);
		return salida;
	}
	
	private List<Instruccion> obtenerInstruccion(Connection conn, DocumentoInstruccion documento) throws Exception {
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdDocumentoInstruccion", documento.IdDocumentoInstruccion);
	
		SQL sql = new SQL();
		List<Instruccion> instruccion = sql.query(conn, SQL_SELECT_INSTRUCCION, parametros,Instruccion.class);
	
		return instruccion;
	}
	
	public static void main(String args[]) throws SQLException {
		Gson gson = Utils.getGsonBuilder();

		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":106138,\"p_IdModulo\":\"ORD\",\"p_IdTipoCanalIngreso\":\"AGE\",\"p_DocumentoInstruccion\":{\"IdDocumentoInstruccion\":3}}";
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getUAT2Connection())
		{
			EntradaObtenerDocumentoInstruccion Objin = gson.fromJson(json, EntradaObtenerDocumentoInstruccion.class);
			
			ObtenerDocumentoInstruccion demo = new ObtenerDocumentoInstruccion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaObtenerDocumentoInstruccion ObjOut= demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut);
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.rollback();
 			
    	}
		 	
		System.out.println("== END ==");
	}

}
