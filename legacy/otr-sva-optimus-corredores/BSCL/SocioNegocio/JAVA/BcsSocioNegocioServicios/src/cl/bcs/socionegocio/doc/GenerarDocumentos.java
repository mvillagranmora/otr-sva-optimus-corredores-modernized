package cl.bcs.socionegocio.doc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.ctt.ImprimirContrato;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Entrada;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Salida;
import cl.bcs.socionegocio.doc.ConsultarDocumentos.ConsultarDocumentos_Entrada;
import cl.bcs.socionegocio.doc.ConsultarDocumentos.ConsultarDocumentos_Salida;
import cl.bcs.socionegocio.doc.ConsultarDocumentos.Documentos;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarDocumentos implements JavaCall {
	
	static final Logger logger	 = LogManager.getLogger(GenerarDocumentos.class);
	
	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CLIENTES 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/GenerarDocumentos_SelectClientes.sql");	
	private final String SQL_SELECT_LISTA_DOCUMENTOS 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/GenerarDocumentos_SelectDocumentosClientes.sql");
	private final String SQL_SELECT_LISTA_DOCUMENTOS_POR_TIPO_DOCUMENTO 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/GenerarDocumentos_SelectDocumentosClientesPorTipoDocumento.sql");
	public final String SQL_SELECT_VALIDACION = loadResourceToString("cl/bcs/socionegocio/doc/sql/GenerarDocumentos_SelectValidacion.sql");
	public String SQL_DOCUMENTO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Documento\" (\"IdDocumento\",\"IdTipoDocumento\",\"IdEmpresa\",\"IdCliente\",\"IdUltimoAnexo\")values( :IdDocumento,:IdTipoDocumento,:IdEmpresa,:IdCliente,:IdUltimoAnexo);";
	public String SQL_ANEXO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Anexo\"(\"IdAnexo\",\"IdDocumento\",\"IdEstado\",\"NumeroFirmantesEmpresa\",\"NumeroFirmantesCliente\",\"NombreFirmanteEmpresa1\", \"NombreFirmanteEmpresa2\",\"NombreFirmanteEmpresa3\",\"NombreFirmanteCliente1\",\"NombreFirmanteCliente2\",\"NombreFirmanteCliente3\",\"FechaIngreso\",\"FechaModificacion\",\"FechaVigencia\",\"DocumentoFisicoReferencia\",\"NombreArchivoAdjunto\", \"FormatoArchivoAdjunto\", \"IdTipoCanalIngreso\", \"NumeroFirmasMinimas\", \"NumeroFirmasMaximas\")values(:IdAnexo,:IdDocumento,:IdEstado,:NumeroFirmantesEmpresa,:NumeroFirmantesCliente,'','','','','','',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'0001-01-01','',:NombreArchivoAdjunto,:FormatoArchivoAdjunto,:IdTipoCanalIngreso, :NumeroFirmasMinimas, :NumeroFirmasMaximas);";
	public String SQL_ANEXO_UPDATE_ESTADO = "UPDATE \"BSCL.Plataforma.BD::DOC.Anexo\" SET \"IdEstado\" = :IdEstado ,\"FechaVigencia\" = CURRENT_UTCTIMESTAMP WHERE \"IdAnexo\" = :IdAnexo AND \"IdDocumento\" = :IdDocumento";
	public String SQL_SELECT_TIPO_DOCUMENTO = "SELECT TD.\"IdTipoDocumento\", TD.\"NumeroFirmasMinimas\", TD.\"NumeroFirmasMaximas\" FROM \"BSCL.Plataforma.BD::DOC.TipoDocumento\" TD WHERE TD.\"IdTipoDocumento\" = :IdTipoDocumento;";


	public static class EntradaGenerarDocumentos extends BcsPojo{	
		public Long p_IdEmpresa;
		public Long p_IdCliente;
		public String p_IdTipoCanal;
	}	
	
	public static class SalidaGenerarDocumentos extends BcsPojo{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGenerarDocumentos() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	

	public static class EntidadListaDocumento extends BcsPojo{
		public Long IdEmpresa;
		public String IdTipoDocumento;
		public String NombreDocumento;
		public String CodPlantilla;
		public Integer NumeroFirmantesEmpresa;
		public Integer NumeroFirmantesCliente;
		public String RequiereFirmaCliente;

	}

	public static class EntidadCliente extends BcsPojo{
		public Long IdEmpresa;
		public Long IdCliente;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
	}

	public static class EntidadTipoDocumento extends BcsPojo{
		public String IdTipoDocumento;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;

	}

	public static class EntidadDocumento extends BcsPojo{
		public Long IdDocumento;
		public String IdTipoDocumento;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
	}

	public static class EntidadAnexo extends BcsPojo{
		public Long IdAnexo;
		public Long IdDocumento;
		public String IdEstado;
		public Long NumeroFirmantesEmpresa;
		public Long NumeroFirmantesCliente;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;
		public String NombreArchivoAdjunto;
		public String FormatoArchivoAdjunto;
		public String IdTipoCanalIngreso;
	}

	public static class EntidadValidaExistenDatos extends BcsPojo{
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
		public Long IdDocumento;
		public String IdTipoDocumento;

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarDocumentos jin = gson.fromJson(in.toString(), EntradaGenerarDocumentos.class);
		SalidaGenerarDocumentos out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaGenerarDocumentos logica(Connection conn, EntradaGenerarDocumentos entrada) throws Exception {
		logger.info("INICIO::GenerarDocumentos.logica");
		long inicioServicio = System.currentTimeMillis();
		SalidaGenerarDocumentos salida = new SalidaGenerarDocumentos();
		
		EntidadCliente Cliente = new EntidadCliente();
		Cliente = BuscaCliente(conn, entrada);
		
		List<EntidadListaDocumento> ListaDocumentos = new ArrayList<EntidadListaDocumento>();
		
		if (entrada.p_IdCliente != null) {
			// Si viene IdCliente, se asume que se deben procesar solo los documentos asociados a este IdCliente
			// Por lo cual se crea nuevo filtro por IdCliente
			ListaDocumentos = BuscaDocumentosPorIdCliente(conn, entrada);
		} else {
			ListaDocumentos = BuscaDocumentos(conn, entrada);
		}
		
		String CodigoPlantilla=null;
		
		System.out.println("== EL TAMANO DE DOCUMENTOS ES  =="+ListaDocumentos.size());
		for (int x = 0; x < ListaDocumentos.size(); ++x) {
		
			if ( ListaDocumentos.get(x).CodPlantilla == null ||ListaDocumentos.get(x).CodPlantilla.equalsIgnoreCase("")  ) {
				logger.info( "Plantilla Nula" ); 
				salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_PLANTILLA_NULA","Plantilla nula "));
			}
			  else if ( !ListaDocumentos.get(x).CodPlantilla.toUpperCase().equalsIgnoreCase("FALSE")	) {

				InputStream ArchivoBuffer = null;

				try {
					ArchivoBuffer = ImprimeContratosPDF(conn, Cliente.IdEmpresa.toString(),
							ListaDocumentos.get(x).CodPlantilla, "PLA", Cliente.IdTipoDocumentoIdentidad,
							Cliente.DocumentoIdentidad,salida);

					SubeDocumentoDB(conn, ArchivoBuffer, ListaDocumentos.get(x).IdTipoDocumento, entrada.p_IdEmpresa,
							entrada.p_IdCliente, String.valueOf(ListaDocumentos.get(x).NumeroFirmantesEmpresa),
							String.valueOf(ListaDocumentos.get(x).NumeroFirmantesCliente), "PLA",salida

					);

					logger.info(String.valueOf(entrada.p_IdEmpresa) + " " + String.valueOf(entrada.p_IdCliente) + " "
							+ Cliente.DocumentoIdentidad + " " + ListaDocumentos.get(x).CodPlantilla + " " + "PLA  "
							+ ListaDocumentos.get(x).IdTipoDocumento + " " + Cliente.IdTipoDocumentoIdentidad);

				} catch (Exception e) {
					e.printStackTrace();
					logger.info(e);
				} finally {

				}				

			} else {

				logger.info("sin plantilla");
				System.out.println("sin plantilla");
			}
		}
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarDocumentos.logica :" , finServicio- inicioServicio);	
		
		return salida;
	}
	

	public void SubeDocumentoDB(Connection conn, InputStream filePart, String IdTipoDocumento, Long IdEmpresa,
			Long IdCliente, String NumeroFirmantesEmpresa, String NumeroFirmantesCliente, String IdTipoCanalIngreso,SalidaGenerarDocumentos salida)
			throws Exception {
		
		String IdDocumento = "0";
		String IdAnexo ="";
		String FormatoArchivoAdjunto = "PDF";
		String NombreArchivoAdjunto = IdTipoDocumento + IdAnexo + ".pdf";
		Long IdAnexoEntrada = 0l;

		SQL sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdTipoDocumento", IdTipoDocumento);
		EntidadTipoDocumento tipoDocumento = sql .query(conn, SQL_SELECT_TIPO_DOCUMENTO, params,EntidadTipoDocumento.class).stream().findFirst() .orElse(new
		EntidadTipoDocumento());
		 

		HashMap<String, Object> paramsValida = new HashMap<>();
		paramsValida.put("IdEmpresa", IdEmpresa);
		paramsValida.put("IdCliente", IdCliente);
		paramsValida.put("IdTipoDocumento", IdTipoDocumento);
		
		EntidadValidaExistenDatos ValidaExiste = sql
				.query(conn, SQL_SELECT_VALIDACION, paramsValida,EntidadValidaExistenDatos.class).stream().findFirst()
				.orElse(new EntidadValidaExistenDatos());

		IdDocumento = (ValidaExiste.IdDocumento != null)?ValidaExiste.IdDocumento.toString():"0" ;
		
		EntidadDocumento entidadDocumento = new EntidadDocumento();
		
		if (IdDocumento != null && IdDocumento.equals("0")) {
			
			Long idDocumento = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Documento", (long) 1).get(0);

			try {

				entidadDocumento.IdDocumento = idDocumento;
				entidadDocumento.IdTipoDocumento = IdTipoDocumento;
				entidadDocumento.IdEmpresa = IdEmpresa;
				entidadDocumento.IdCliente = IdCliente;
				entidadDocumento.IdUltimoAnexo = 0L;
				sql.batch(conn, SQL_DOCUMENTO_INSERT, Arrays.asList(entidadDocumento), EntidadDocumento.class);

				IdDocumento = idDocumento.toString();

			} catch (Exception e) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_INSERTAR_DOCUMENTO","Error al guardar documento generado"));
			}

		} else {
			
			IdAnexo = ValidaExiste.IdUltimoAnexo.toString();
			IdAnexoEntrada =Long.parseLong(IdAnexo);
		}
		// crear Anexo
		EntidadAnexo entidadAnexo = new EntidadAnexo();
		
		
		
		if (IdAnexo != null) {
			Long idAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Anexo", (long) 1).get(0);
			try {
				entidadAnexo.IdAnexo = idAnexo;
				entidadAnexo.IdDocumento = Long.valueOf(IdDocumento);
				entidadAnexo.IdEstado = "PDF";
				entidadAnexo.NumeroFirmantesEmpresa = Long.parseLong(NumeroFirmantesEmpresa);
				entidadAnexo.NumeroFirmantesCliente = Long.parseLong(NumeroFirmantesCliente);
				entidadAnexo.NumeroFirmasMinimas = tipoDocumento.NumeroFirmasMinimas;
				entidadAnexo.NumeroFirmasMaximas = tipoDocumento.NumeroFirmasMaximas;
				entidadAnexo.NombreArchivoAdjunto = NombreArchivoAdjunto;
				entidadAnexo.FormatoArchivoAdjunto = FormatoArchivoAdjunto;
				entidadAnexo.IdTipoCanalIngreso = IdTipoCanalIngreso;
				sql.batch(conn, SQL_ANEXO_INSERT, Arrays.asList(entidadAnexo), EntidadAnexo.class);

				System.out.println(entidadAnexo);//CGO
				
				IdAnexo = idAnexo.toString();

			} catch (Exception e) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_INSERTAR_ANEXO","Error al guardar anexo generado"));
			}
		}

	
		
		
		// Se actualiza el El ultimo Anexo
		String update = "UPDATE \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::DOC.Documento\" "
				+ "SET \"IdUltimoAnexo\" =?, \"DocumentoSubido\" ='N' WHERE \"IdDocumento\" = ?";
		
		try (PreparedStatement pstmt = conn.prepareStatement(update)) {
			pstmt.setLong(1, entidadAnexo.IdAnexo);
			pstmt.setLong(2, entidadAnexo.IdDocumento);
			pstmt.executeUpdate();

		} catch (SQLException ex) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_ACTUALIZAR_ANEXO","Error al actualizar documento generado"));
		}

		// Se actualiza el Adjunto y el estado

		update = "UPDATE \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::DOC.Anexo\" " + "SET"
				+ " \"ArchivoAdjunto\" =?," + " \"IdEstado\" = ?,"
				+ "\"NombreArchivoAdjunto\" =?, \"FormatoArchivoAdjunto\" =?"
				+ "WHERE \"IdDocumento\" = ? AND \"IdAnexo\" = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(update)) {
			pstmt.setBlob(1, filePart);
			pstmt.setString(2, "PDF");
			pstmt.setString(3, entidadAnexo.NombreArchivoAdjunto);
			pstmt.setString(4, FormatoArchivoAdjunto);
			pstmt.setLong(5, entidadAnexo.IdDocumento);
			pstmt.setLong(6, entidadAnexo.IdAnexo);
			pstmt.executeUpdate();

		} catch (SQLException ex) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_ACTUALIZAR_ANEXO","Error al actualizar anexo generado"));
		}

		
	   if (IdAnexoEntrada != null && IdAnexoEntrada != 0) { 
		  try { EntidadAnexo   VenceAnexo = new EntidadAnexo(); 
			  VenceAnexo.IdAnexo = IdAnexoEntrada;
			  VenceAnexo.IdDocumento = Long.parseLong(IdDocumento); 
			  VenceAnexo.IdEstado = "VCD"; 
			  
		     sql.batch(conn, SQL_ANEXO_UPDATE_ESTADO,  Arrays.asList(VenceAnexo), EntidadAnexo.class); 
	     } catch (Exception e) {
	    	 salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_ACTUALIZAR_ANEXO_VENCIDO","Error al actualizar anexo vencido"));		 
	     }
	  
	   }
	}
	

	public EntidadCliente BuscaCliente(Connection conn, EntradaGenerarDocumentos param) throws Exception {
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", param.p_IdEmpresa);
		params.put("IdCliente", param.p_IdCliente);
		SQL sql = new SQL();
		List<EntidadCliente> ClienteX = sql.query(conn, SQL_SELECT_CLIENTES, params, EntidadCliente.class);
		return ClienteX.get(0);
	}

	public List<EntidadListaDocumento> BuscaDocumentos(Connection conn, EntradaGenerarDocumentos param) throws Exception {
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEmpresa", param.p_IdEmpresa);
		params.put("IdCanalIngreso", param.p_IdTipoCanal);
		List<String> CanalesDefault = new ArrayList<String>();
		CanalesDefault.add("AGE");
		CanalesDefault.add("CLI");
		CanalesDefault.add("OPT");
		CanalesDefault.add("APP");
		CanalesDefault.add("OWP");
		CanalesDefault.add("WBS");
		//En caso de que idtipocanal sea vacio, por defecto se envia los 6 canales anteriores en la query
		if(param.p_IdTipoCanal.length()==0) {
			params.put("IdCanalIngreso", CanalesDefault);
		}
		SQL sql = new SQL();
		List<EntidadListaDocumento> ListaDocumentoX = sql.query(conn, SQL_SELECT_LISTA_DOCUMENTOS, params,
				EntidadListaDocumento.class);
		return ListaDocumentoX;
	}
	
	public List<EntidadListaDocumento> BuscaDocumentosPorIdCliente(Connection conn, EntradaGenerarDocumentos param) throws Exception {
		
		ConsultarDocumentos_Entrada Objin = new ConsultarDocumentos_Entrada();
		Objin.p_IdEmpresa = param.p_IdEmpresa;
		Objin.p_IdCliente = param.p_IdCliente;
		ConsultarDocumentos doc = new ConsultarDocumentos();
		ConsultarDocumentos_Salida ObjOut = doc.logica(conn, Objin);
		List<String> TipoDocumento = new ArrayList<String>();
		
		for (Documentos obj : ObjOut.p_out_Documentos) {
			TipoDocumento.add(obj.IdTipoDocumento);
		}
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEmpresa", param.p_IdEmpresa);
		params.put("TipoDocumento", TipoDocumento);
	
		SQL sql = new SQL();
		List<EntidadListaDocumento> ListaDocumentoX = sql.query(conn, SQL_SELECT_LISTA_DOCUMENTOS_POR_TIPO_DOCUMENTO, params,
				EntidadListaDocumento.class);
		return ListaDocumentoX;
	}
	
	public InputStream ImprimeContratosPDF(Connection conn, String IdEmpresa, String IdPlantilla, String IdModulo,
			String TipoDocumentoIdentidad, String DocumentoIdentidad ,SalidaGenerarDocumentos out) throws Exception {
		InputStream filePart = null;
		
		ImprimirContrato_Entrada entrada = new ImprimirContrato_Entrada();
		
		entrada.p_IdEmpresa = Long.valueOf(IdEmpresa) ;	
		entrada.p_CodigoContrato = IdPlantilla;
		entrada.p_IdModulo = IdModulo ;
		entrada.p_TipoDocumentoIdentidad =  TipoDocumentoIdentidad;
		entrada.p_DocumentoIdentidad =  DocumentoIdentidad ;
		

		ImprimirContrato_Salida salida = new ImprimirContrato().logica(conn, entrada);
		
		if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L)!=0) {
			out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
		}

		String b64 = salida.p_out_ContratoB64;

		salida.p_out_ContratoB64 = "ContenidoEnBase64";

		
		// esta seccion de codigo genera una carpeta con los contratos .pdf			
		
		/*  String printedContractsDirPathString = System.getProperty("user.home") +
		*  "/Desktop/ImpresionesContratos"; Path printedContractsDirPath =
		*  Paths.get(printedContractsDirPathString); if
		*  (Files.exists(printedContractsDirPath) == false) {
		*  Files.createDirectory(printedContractsDirPath); } String baseContractPath =
		*  printedContractsDirPathString + "/"+ IdPlantilla+ "-" ; long correlative =
		*  0L; Path finalContractPath; do { finalContractPath =
		*  Paths.get(baseContractPath + correlative++ + ".pdf"); } while
		*  (finalContractPath.toFile().exists()); Files.write(finalContractPath,
		*  Base64.decodeBase64(b64));
		*/

		filePart = new ByteArrayInputStream(Base64.decodeBase64(b64));
		return filePart;
	}
	

	
	public static void main(String[] args) throws Throwable {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdCliente\"=28929,\"p_IdTipoCanal\"=\"OPT\"}";
		
		EntradaGenerarDocumentos Objin = gson.fromJson(json, EntradaGenerarDocumentos.class);
		GenerarDocumentos demo = new GenerarDocumentos();				
		try {
			SalidaGenerarDocumentos ObjOut = demo.logica(con, Objin);			
			System.out.println(ObjOut.toString());
			//con.commit();
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			
    	}
		System.out.println("== END ==");
	}


}