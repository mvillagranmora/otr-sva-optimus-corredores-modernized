package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.DocumentoInstruccion;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BorrarDocumentoInstruccion implements JavaCall {

	static final Logger logger = LogManager.getLogger(BorrarDocumentoInstruccion.class);
	public String SQL_DOCUMENTO_UPDATE_BORRADO = loadResourceToString("cl/bcs/ordenes/sql/BorrarDocumentoInstruccion_UpdateBorradoDocumentoInstruccion.sql");
	public String SQL_SELECT_DOCUMENTO_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/BorrarDocumentoInstruccion_SelectDocumentoInstruccion.sql");
	
	public static class EntradaBorrarDocumentoInstruccion extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdTipoCanalIngreso;
		public List<DocumentoInstruccion> p_DocumentosInstruccion;
		
		public EntradaBorrarDocumentoInstruccion() {
			this.p_DocumentosInstruccion = new  ArrayList<DocumentoInstruccion>();
		}
	}
	
	public static class SalidaBorrarDocumentoInstruccion extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaBorrarDocumentoInstruccion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}

	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaBorrarDocumentoInstruccion in = gson.fromJson(jin.toString(), EntradaBorrarDocumentoInstruccion.class);
		SalidaBorrarDocumentoInstruccion out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private SalidaBorrarDocumentoInstruccion logica(Connection conn, EntradaBorrarDocumentoInstruccion in) throws Exception {
		logger.info("INICIO::BorrarDocumentoInstruccion");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaBorrarDocumentoInstruccion salida = new SalidaBorrarDocumentoInstruccion();
		List<DocumentoInstruccion> documentos = obtenerDocumentoInstruccion(conn, in.p_DocumentosInstruccion);
		
		if(documentos.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_DOCUMENTOS_INSTRUCCION_NO_EXISTE",null,conn,in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR += 1L;
			return salida;
		}
		
		documentos = actualizarDatosEntidadesDocumentoInstruccion(conn, documentos, in.p_IdEmpresa);
		eliminarDocumentosInstruccion(conn,documentos);
		
		if(salida.P_OUT_ERRORESCONTADOR==0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_DOCUMENTO_INSTRUCCION_BORRADO_CORRECTAMENTE",null,conn,in.p_IdEmpresa));
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::BorrarDocumentoInstruccion", finServicio - inicioServicio);
		return salida;
	}

	private List<DocumentoInstruccion> actualizarDatosEntidadesDocumentoInstruccion(Connection conn,
			List<DocumentoInstruccion> documentos, Long idEmpresa) throws Exception {
		java.sql.Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa);
		
		List<DocumentoInstruccion> entidadesModificadas = new ArrayList<DocumentoInstruccion>();
		
		for(DocumentoInstruccion documento : documentos) {
			documento.Borrado = "S";
			documento.FechaActualizacion = fechaHoraSistema;
			entidadesModificadas.add(documento);
		}
		return entidadesModificadas;
	}

	private void eliminarDocumentosInstruccion(Connection conn, List<DocumentoInstruccion> documentosEliminar) throws Exception {
		logger.debug("INICIO::BorrarDocumentoInstruccion.eliminarDocumentosInstruccion");
		long inicio = System.currentTimeMillis();
		
		if(documentosEliminar.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(conn, SQL_DOCUMENTO_UPDATE_BORRADO, documentosEliminar, DocumentoInstruccion.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BorrarDocumentoInstruccion.eliminarDocumentosInstruccion", fin - inicio);
	}

	private List<DocumentoInstruccion> obtenerDocumentoInstruccion(Connection conn, List<DocumentoInstruccion> documentosInstruccion) throws Exception {
		logger.debug("INICIO::BorrarDocumentoInstruccion.obtenerDocumentoInstruccion");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDocumentos = documentosInstruccion.stream()
				  .map(documento -> documento.IdDocumentoInstruccion)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("IdsDocumentosInstruccion", idsDocumentos);
		
		SQL sql = new SQL();
		List<DocumentoInstruccion> documentos = sql.query(conn, SQL_SELECT_DOCUMENTO_INSTRUCCION, parametros,DocumentoInstruccion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BorrarDocumentoInstruccion.obtenerDocumentoInstruccion", fin - inicio);
		return documentos;
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\r\n"
					+ "    \"p_DocumentosInstruccion\":[\r\n"
					+ "        {\r\n"
					+ "            \"IdDocumentoInstruccion\": 2\r\n"
					+ "        }\r\n"
					+ "    ],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_IdTipoCanalIngreso\": \"AGE\",\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaBorrarDocumentoInstruccion Objin = gson.fromJson(json, EntradaBorrarDocumentoInstruccion.class);
			BorrarDocumentoInstruccion demo=new BorrarDocumentoInstruccion();				
			try {
				SalidaBorrarDocumentoInstruccion ObjOut=demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback(); 
			}
			finally {
				con.rollback(); 
				con.close();
	    	}
			System.out.println("== END ==");
		}
	}
}
