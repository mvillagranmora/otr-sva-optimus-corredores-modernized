package cl.bcs.plataforma.jobs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.ctt.ImprimirContrato;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Entrada;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Salida;
import cl.bcs.socionegocio.enr.GenerarDocumentosFirmados;
import cl.bcs.socionegocio.enr.GenerarDocumentosFirmados.EntidadEntradaFirmada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarDocumentosAutomaticos extends BcsJob implements JavaCall {
	AtomicBoolean running = new AtomicBoolean(false);
	public final Log log = new Log("PLA.GenerarDocumentosAutomaticos");

	public static final Logger logger = LogManager.getLogger(GenerarDocumentosAutomaticos.class);

	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_CLIENTES = loadResourceToString("cl/bcs/plataforma/jobs/sql/GenerarDocumentosAutomaticos_SelectClientes.sql");
	private final String SQL_SELECT_LISTA_DOCUMENTOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/GenerarDocumentosAutomaticos_SelectDocumentosClientes.sql");
	public final String SQL_SELECT_VALIDACION = loadResourceToString("cl/bcs/plataforma/jobs/sql/GenerarDocumentosAutomaticos_SelectValidacion.sql");
	public String SQL_DOCUMENTO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Documento\" (\"IdDocumento\",\"IdTipoDocumento\",\"IdEmpresa\",\"IdCliente\",\"IdUltimoAnexo\")values( :IdDocumento,:IdTipoDocumento,:IdEmpresa,:IdCliente,:IdUltimoAnexo);";
	public String SQL_ANEXO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Anexo\"(\"IdAnexo\",\"IdDocumento\",\"IdEstado\",\"NumeroFirmantesEmpresa\",\"NumeroFirmantesCliente\",\"NombreFirmanteEmpresa1\", \"NombreFirmanteEmpresa2\",\"NombreFirmanteEmpresa3\",\"NombreFirmanteCliente1\",\"NombreFirmanteCliente2\",\"NombreFirmanteCliente3\",\"FechaIngreso\",\"FechaModificacion\",\"FechaVigencia\",\"DocumentoFisicoReferencia\",\"NombreArchivoAdjunto\", \"FormatoArchivoAdjunto\", \"IdTipoCanalIngreso\", \"NumeroFirmasMinimas\", \"NumeroFirmasMaximas\")values(:IdAnexo,:IdDocumento,:IdEstado,:NumeroFirmantesEmpresa,:NumeroFirmantesCliente,'','','','','','',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'0001-01-01','',:NombreArchivoAdjunto,:FormatoArchivoAdjunto,:IdTipoCanalIngreso, :NumeroFirmasMinimas, :NumeroFirmasMaximas);";
	public String SQL_ANEXO_UPDATE_ESTADO = "UPDATE \"BSCL.Plataforma.BD::DOC.Anexo\" SET \"IdEstado\" = :IdEstado ,\"FechaVigencia\" = CURRENT_UTCTIMESTAMP WHERE \"IdAnexo\" = :IdAnexo AND \"IdDocumento\" = :IdDocumento";
	public String SQL_SELECT_TIPO_DOCUMENTO = "SELECT TD.\"IdTipoDocumento\", TD.\"NumeroFirmasMinimas\", TD.\"NumeroFirmasMaximas\" FROM \"BSCL.Plataforma.BD::DOC.TipoDocumento\" TD WHERE TD.\"IdTipoDocumento\" = :IdTipoDocumento;";


	
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class EntidadEntrada {
		public Long v_IdEmpresa;
		public Long v_IdCliente;
		public String v_IdTipoCanal;
		public String v_FirmaAutomaticaDocumento;
		public Long v_IdUsuario;
		public EntidadEntrada() {
			this.v_FirmaAutomaticaDocumento = "N";
		}
	}

	public static class EntidadCliente {

		public Long IdEmpresa;
		public Long IdCliente;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
	}

	public static class EntidadListaDocumento {
		public Long IdEmpresa;
		public String IdTipoDocumento;
		public String NombreDocumento;
		public String CodPlantilla;
		public Integer NumeroFirmantesEmpresa;
		public Integer NumeroFirmantesCliente;
		public String RequiereFirmaCliente;

	}

	public static class EntidadTipoDocumento {
		public String IdTipoDocumento;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;

	}

	public static class EntidadDocumento {
		public Long IdDocumento;
		public String IdTipoDocumento;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
	}

	public static class EntidadAnexo {
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

	public static class EntidadValidaExistenDatos {
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
		public Long IdDocumento;
		public String IdTipoDocumento;

	}

	public EntidadCliente BuscaCliente(Connection conn, EntidadEntrada param) throws Exception {
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", param.v_IdEmpresa);
		params.put("IdCliente", param.v_IdCliente);
		SQL sql = new SQL();
		List<EntidadCliente> ClienteX = sql.query(conn, SQL_SELECT_CLIENTES, params, EntidadCliente.class);
		return ClienteX.get(0);
	}

	public List<EntidadListaDocumento> BuscaDocumentos(Connection conn, EntidadEntrada param) throws Exception {
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEmpresa", param.v_IdEmpresa);
		params.put("IdCanalIngreso", param.v_IdTipoCanal);
		List<String> CanalesDefault = new ArrayList<String>();
		CanalesDefault.add("AGE");
		CanalesDefault.add("CLI");
		CanalesDefault.add("OPT");
		CanalesDefault.add("APP");
		CanalesDefault.add("OWP");
		CanalesDefault.add("WBS");
		if(param.v_IdTipoCanal.length()==0) {
			params.put("IdCanalIngreso", CanalesDefault);
		}
		SQL sql = new SQL();
		List<EntidadListaDocumento> ListaDocumentoX = sql.query(conn, SQL_SELECT_LISTA_DOCUMENTOS, params,
				EntidadListaDocumento.class);
		return ListaDocumentoX;
	}

	public boolean logica(Connection conn, boolean debug) throws Exception {
		running.set(false);

		logger.info("INICIO::GenerarDocumentosAutomaticos");
		long inicio = System.currentTimeMillis();

		EntidadEntrada ingreso = new EntidadEntrada();

		if (debug) { // Paramatros para Debug

			ingreso.v_IdEmpresa = (long) 1;
			ingreso.v_IdCliente = (long) 1421406;// 1421406;
			ingreso.v_IdTipoCanal = "";
			ingreso.v_FirmaAutomaticaDocumento = "N";
			ingreso.v_IdUsuario = 1L;
		} else {
			ingreso.v_IdEmpresa = Long.parseLong(this.getParam("p_IdEmpresa"));
			ingreso.v_IdCliente = Long.parseLong(this.getParam("p_IdCliente"));
			ingreso.v_IdTipoCanal = this.getParam("p_IdTipoCanal");
			ingreso.v_FirmaAutomaticaDocumento = this.getParam("p_FirmaAutomaticaDocumento");
			ingreso.v_IdUsuario = Long.parseLong(this.getParam("p_IdUsuario"));
		}

		EntidadCliente Cliente = new EntidadCliente();
		Cliente = BuscaCliente(conn, ingreso);

		List<EntidadListaDocumento> ListaDocumentos = new ArrayList<EntidadListaDocumento>();
		ListaDocumentos = BuscaDocumentos(conn, ingreso);
		
		String CodigoPlantilla = null;
		
		for (int x = 0; x < ListaDocumentos.size(); ++x) {
		
			if ( ListaDocumentos.get(x).CodPlantilla == null ||ListaDocumentos.get(x).CodPlantilla.equalsIgnoreCase("")  ) {
				logger.info( "Plantilla Nula" ); 
			}
			  else if ( !ListaDocumentos.get(x).CodPlantilla.toUpperCase().equalsIgnoreCase("FALSE")	) {

				InputStream ArchivoBuffer = null;
				Thread.sleep(500);
				try {
					ArchivoBuffer = ImprimeContratosPDF(conn, Cliente.IdEmpresa.toString(),
							ListaDocumentos.get(x).CodPlantilla, "PLA", Cliente.IdTipoDocumentoIdentidad,
							Cliente.DocumentoIdentidad);

					SubeDocumentoDB(conn, ArchivoBuffer, ListaDocumentos.get(x).IdTipoDocumento, ingreso.v_IdEmpresa,
							ingreso.v_IdCliente, String.valueOf(ListaDocumentos.get(x).NumeroFirmantesEmpresa),
							String.valueOf(ListaDocumentos.get(x).NumeroFirmantesCliente), ingreso.v_IdTipoCanal

					);

					logger.info(String.valueOf(ingreso.v_IdEmpresa) + " " + String.valueOf(ingreso.v_IdCliente) + " "
							+ Cliente.DocumentoIdentidad + " " + ListaDocumentos.get(x).CodPlantilla + " " + ingreso.v_IdTipoCanal + " "
							+ ListaDocumentos.get(x).IdTipoDocumento + " " + Cliente.IdTipoDocumentoIdentidad);

				} catch (Exception e) {
					e.printStackTrace();
					logger.info(e);
				} finally {

				}
				System.out.println(String.valueOf(ingreso.v_IdEmpresa) + " " + String.valueOf(ingreso.v_IdCliente) + " "
						+ Cliente.DocumentoIdentidad + " " + ListaDocumentos.get(x).CodPlantilla + " " + ingreso.v_IdTipoCanal + " "
						+ ListaDocumentos.get(x).IdTipoDocumento + " " + Cliente.IdTipoDocumentoIdentidad);

			} else {

				logger.info("sin plantilla");
				System.out.println("sin plantilla");
			}
		}
		
		
		if(ingreso.v_FirmaAutomaticaDocumento.equals("S")) {
			EntidadEntradaFirmada in = new EntidadEntradaFirmada();
			in.v_IdCliente = ingreso.v_IdCliente;
			in.v_IdEmpresa = ingreso.v_IdEmpresa;
			in.v_IdTipoCanal = ingreso.v_IdTipoCanal;
			in.v_IdUsuario = ingreso.v_IdUsuario;
			
			GenerarDocumentosFirmados generarDoc = new GenerarDocumentosFirmados();
			generarDoc.logica(conn, in);
		
		}
		logger.info("== END ==");

		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarDocumentosAutomaticos", fin - inicio);

		return true;

	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

		public InputStream ImprimeContratosPDF(Connection conn, String IdEmpresa, String IdPlantilla, String IdModulo,
			String TipoDocumentoIdentidad, String DocumentoIdentidad) {
		InputStream filePart = null;
		try {

			ImprimirContrato_Entrada entrada = new ImprimirContrato_Entrada();
			
			entrada.p_IdEmpresa = Long.valueOf(IdEmpresa) ;	
			entrada.p_CodigoContrato = IdPlantilla;
			entrada.p_IdModulo = IdModulo ;
			entrada.p_TipoDocumentoIdentidad =  TipoDocumentoIdentidad;
			entrada.p_DocumentoIdentidad =  DocumentoIdentidad ;
			

			ImprimirContrato_Salida salida = new ImprimirContrato().logica(conn, entrada);

			String b64 = salida.p_out_ContratoB64;

			salida.p_out_ContratoB64 = "ContenidoEnBase64";

			
			// esta seccion de codigo genera una carpeta con los contratos .pdf			
			/*
			 * String printedContractsDirPathString = System.getProperty("user.home") +
			 * "/Desktop/ImpresionesContratos"; Path printedContractsDirPath =
			 * Paths.get(printedContractsDirPathString); if
			 * (Files.exists(printedContractsDirPath) == false) {
			 * Files.createDirectory(printedContractsDirPath); } String baseContractPath =
			 * printedContractsDirPathString + "/"+ IdPlantilla+ "-" ; long correlative =
			 * 0L; Path finalContractPath; do { finalContractPath =
			 * Paths.get(baseContractPath + correlative++ + ".pdf"); } while
			 * (finalContractPath.toFile().exists()); Files.write(finalContractPath,
			 * Base64.decodeBase64(b64));
			 */
			 

			filePart = new ByteArrayInputStream(Base64.decodeBase64(b64));

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

		}

		return filePart;
	}

	public void SubeDocumentoDB(Connection conn, InputStream filePart, String IdTipoDocumento, Long IdEmpresa,
			Long IdCliente, String NumeroFirmantesEmpresa, String NumeroFirmantesCliente, String IdTipoCanalIngreso)
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
				conn.rollback();
				throw new Exception(e);
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

				IdAnexo = idAnexo.toString();

			} catch (Exception e) {
				conn.rollback();
				throw new Exception(e);
			}
		}
		System.out.println(IdDocumento + "  " + IdAnexo);

		// Se actualiza el El ultimo Anexo
		String update = "UPDATE \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::DOC.Documento\" "
				+ "SET \"IdUltimoAnexo\" =?, \"DocumentoSubido\" ='N' WHERE \"IdDocumento\" = ?";

		logger.debug(update);

		try (PreparedStatement pstmt = conn.prepareStatement(update)) {
			pstmt.setLong(1, entidadAnexo.IdAnexo);
			pstmt.setLong(2, entidadAnexo.IdDocumento);
			pstmt.executeUpdate();

		} catch (SQLException ex) {
		
			conn.rollback();
			throw new Exception(ex);
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
			
			conn.rollback();
			throw new Exception(ex);
		}

		
		  if (IdAnexoEntrada != null && IdAnexoEntrada != 0) { 
			  try { EntidadAnexo   VenceAnexo = new EntidadAnexo(); 
			  VenceAnexo.IdAnexo = IdAnexoEntrada;
			  VenceAnexo.IdDocumento = Long.parseLong(IdDocumento); 
			  VenceAnexo.IdEstado = "VCD"; 
			  
		     sql.batch(conn, SQL_ANEXO_UPDATE_ESTADO,  Arrays.asList(VenceAnexo), EntidadAnexo.class); 
		     } catch (Exception e) {
		  conn.rollback(); 
		  throw new Exception(e); 
		  }
		  
		  }
		 

	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	@Override
	public void onError(Throwable t) {
		log.write(t.getMessage());
		log.write("****** ERROR TAREA  *******");

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		String logg = "";
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			/*String jsonCL = "{\"v_IdEmpresa\":\"1\",\"v_IdCliente\":\"13795\",\"v_IdTipoCanal\":\"OWP\",\"v_FirmaAutomaticaDocumento\":\"S\"}";
			Gson gson = new Gson();
			EntidadEntrada entrada = gson.fromJson(jsonCL,EntidadEntrada.class);*/

			try {

				GenerarDocumentosAutomaticos demo = new GenerarDocumentosAutomaticos();
				demo.logica(con, true);

			} catch (Exception er) {
				er.printStackTrace();
				// con.rollback();
			}
			con.rollback();
			//con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
//demo.GenerarDocumentos(conn, 1, 9, 524893);