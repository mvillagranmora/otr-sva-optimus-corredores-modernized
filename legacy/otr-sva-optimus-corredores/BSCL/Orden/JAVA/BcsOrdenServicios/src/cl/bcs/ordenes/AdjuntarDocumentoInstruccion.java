package cl.bcs.ordenes;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import javax.sql.rowset.serial.SerialBlob;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.Instruccion;
import cl.bcs.ordenes.pojo.DocumentoInstruccion;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AdjuntarDocumentoInstruccion implements JavaCall {

	static final Logger logger = LogManager.getLogger(AdjuntarDocumentoInstruccion.class);
	public String SQL_DOCUMENTO_INSERT = loadResourceToString("cl/bcs/ordenes/sql/AdjuntarDocumentoInstruccion_InsertDocumentoInstruccion.sql");
	public String SQL_SELECT_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/AdjuntarDocumentoInstruccion_SelectInstruccion.sql");
	
	public static class EntradaAdjuntarDocumentoInstruccion extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdTipoCanalIngreso;
		public DocumentoInstruccion p_DocumentoInstruccion;

	}
	
	public static class SalidaAdjuntarDocumentoInstruccion extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaAdjuntarDocumentoInstruccion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}

	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAdjuntarDocumentoInstruccion in = gson.fromJson(jin.toString(), EntradaAdjuntarDocumentoInstruccion.class);
		SalidaAdjuntarDocumentoInstruccion out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private SalidaAdjuntarDocumentoInstruccion logica(Connection conn, EntradaAdjuntarDocumentoInstruccion in)  throws Exception {
		logger.info("INICIO::AdjuntarDocumentoInstruccion");
		long inicioServicio = System.currentTimeMillis();
		
		java.sql.Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
		SalidaAdjuntarDocumentoInstruccion salida = new SalidaAdjuntarDocumentoInstruccion();
		List<Instruccion> instruccion = obtenerInstruccion(conn, in.p_DocumentoInstruccion);
		
		if(instruccion.size()==0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_INSTRUCCION_NO_EXISTE",null,conn,in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR += 1L;
			return salida;
		}
		//TODO Maybe Sacar datos de la entrada
		byte[] archivoDecoded = Base64.getDecoder().decode(in.p_DocumentoInstruccion.Adjunto);
		Blob blob = new SerialBlob(archivoDecoded);
		//TODO Obtener secuencia de DocumentoInstruccion
		Long idDocumentoInstruccion = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::DocumentoInstruccion", (long) 1).get(0);
		
		//Se actualiza el Adjunto y el estado
		{
			try (PreparedStatement pstmt = conn
					.prepareStatement(SQL_DOCUMENTO_INSERT)) {
				pstmt.setLong(1, idDocumentoInstruccion);
				pstmt.setLong(2, in.p_DocumentoInstruccion.IdInstruccion);
				pstmt.setString(3, in.p_DocumentoInstruccion.NombreDocumentoAdjunto);
				pstmt.setString(4, in.p_DocumentoInstruccion.Descripcion);
				pstmt.setBlob(5, blob);
				pstmt.setTimestamp(6, fechaHoraSistema);
				pstmt.executeUpdate();
				//dbConnection.rollback();
				conn.commit();
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"SE_ADJUNTA_DOCUMENTO_INSTRUCCION",null,conn,in.p_IdEmpresa));
			} catch (SQLException ex) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_LOGRO_ADJUNTAR_DOCUMENTO_INSTRUCCION",null,conn,in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR += 1L;
				conn.rollback();
				throw new Exception(ex);
			}
			if(salida.P_OUT_ERRORESCONTADOR==0) {
				salida.P_OUT_REFID = obtenerIdsInsertados(in.p_DocumentoInstruccion,idDocumentoInstruccion );		
			}
			
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AdjuntarDocumentoInstruccion", finServicio - inicioServicio);
		return salida;
	}

	private List<Instruccion> obtenerInstruccion(Connection conn, DocumentoInstruccion documento) throws Exception {
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("IdInstruccion", documento.IdInstruccion);
	
		SQL sql = new SQL();
		List<Instruccion> instruccion = sql.query(conn, SQL_SELECT_INSTRUCCION, parametros,Instruccion.class);
	
		return instruccion;
	}

	private List<Entidad> obtenerIdsInsertados(DocumentoInstruccion adjunto, Long idDocumentoInstruccion){
		logger.trace("INICIO::AdjuntarDocumentoInstruccion.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> idsInsertados = new ArrayList<Entidad>();
		Entidad entidad = new Entidad();
		entidad.IdInterno = adjunto.IdInstruccion;
		entidad.IdEntidad = idDocumentoInstruccion;
		idsInsertados.add(entidad);
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::AdjuntarDocumentoInstruccion.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[]args) throws Exception{
		
		Gson gson = Utils.getGsonBuilder();

		String json = "{\r\n"
				+ "    \"p_DocumentoInstruccion\":{\r\n"
				+ "        \"Adjunto\": \"ZGF0YTp0ZXh0L3BsYWluO2Jhc2U2NCxWR1Z6ZEE9PQ==\",\r\n"
				+ "        \"Descripcion\": \"\",\r\n"
				+ "        \"IdInstruccion\": 84,\r\n"
				+ "        \"NombreDocumentoAdjunto\": \"DocumentoPrueba.txt\"\r\n"
				+ "    },\r\n"
				+ "    \"p_IdEmpresa\": 1,\r\n"
				+ "    \"p_IdModulo\": \"ORD\",\r\n"
				+ "    \"p_IdTipoCanalIngreso\": \"AGE\",\r\n"
				+ "    \"p_IdUsuario\": 9\r\n"
				+ "}";
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			EntradaAdjuntarDocumentoInstruccion Objin = gson.fromJson(json, EntradaAdjuntarDocumentoInstruccion.class);
			
			AdjuntarDocumentoInstruccion demo=new AdjuntarDocumentoInstruccion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaAdjuntarDocumentoInstruccion ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
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
