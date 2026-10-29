package cl.bcs.plataforma.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servlet implementation 
 */
@WebServlet("/DocumentosClienteBin")
@MultipartConfig
public class DocumentosServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DocumentosServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String filename = null;
		String IdAnexo = request.getParameter("IdAnexo");
		String IdDocumento = request.getParameter("IdDocumento");

			try (Connection dbConnection = dataSource.getConnection()){
				//Get Session Data
				JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
				if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null || dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
					dbConnection.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - NOT AUTHORIZED");
					response.getOutputStream().flush();
					return;
				}
					//Obtener archivo o contrato estaticos (pdf)
					final String selectSQL = "select top 1 \"ArchivoAdjunto\" from \"BSCL.Plataforma.BD::DOC.Anexo\" where \"IdAnexo\"=? AND \"IdDocumento\"=?";
					try(PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)){
						pstmt.setLong(1, new Long(IdAnexo));
						pstmt.setLong(2, new Long(IdDocumento));
						
						ResultSet rs = pstmt.executeQuery();
						
						while(rs.next()){
							try(InputStream is = rs.getBlob(1).getBinaryStream()){
								IOUtils.copy(is,response.getOutputStream());
							}
						}
						filename = "ANEXO.PDF";//TODO se podria utilizar el nombre del documento
					}

				response.setHeader("Content-disposition","inline; filename="+filename);
				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
				response.setHeader("Pragma", "no-cache");//HTTP 1.0
				response.getOutputStream().flush();
				
			} catch (Exception e) {
				System.out.println("DOWNLOAD ERROR: "+e.getMessage());
				response.setStatus(500);
				response.getOutputStream().print("500 - BAD REQUEST");	
				response.getOutputStream().flush();
				return;
			}
		}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public static class ErrorDetalleCall{
		public String Codigo;
		public String Descripcion;
		@Override
		public String toString() {
			return String.format("ErrorDetalleCall [Codigo=%s, Descripcion=%s]", Codigo, Descripcion);
		}
	}
	
	public static class AdvertenciaDetalleCall{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		@Override
		public String toString() {
			return String.format("AdvertenciaDetalleCall [Codigo=%s, RequiereAutorizacion=%s, Descripcion=%s]", Codigo,
					RequiereAutorizacion, Descripcion);
		}
	}

	public static class EntidadTipoDocumento {
		public String IdTipoDocumento;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;

		public EntidadTipoDocumento() {
			IdTipoDocumento = "";
			NumeroFirmasMinimas = 0;
			NumeroFirmasMaximas = 0;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadDocumento{
		public Long IdDocumento;
		public String IdTipoDocumento;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
	}
	
	public static class EntidadAnexo{
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
	
	public static class SalidaServlet{
		public Long IdAnexo;
		public Long IdDocumento;
		public Long IdAnexoVencido;
		public String respuesta;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public String SQL_DOCUMENTO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Documento\" (\"IdDocumento\",\"IdTipoDocumento\",\"IdEmpresa\",\"IdCliente\",\"IdUltimoAnexo\",\"DocumentoSubido\")values( :IdDocumento,:IdTipoDocumento,:IdEmpresa,:IdCliente,:IdUltimoAnexo,'S');";
	public String SQL_ANEXO_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::DOC.Anexo\"(\"IdAnexo\",\"IdDocumento\",\"IdEstado\",\"NumeroFirmantesEmpresa\",\"NumeroFirmantesCliente\",\"NombreFirmanteEmpresa1\", \"NombreFirmanteEmpresa2\",\"NombreFirmanteEmpresa3\",\"NombreFirmanteCliente1\",\"NombreFirmanteCliente2\",\"NombreFirmanteCliente3\",\"FechaIngreso\",\"FechaModificacion\",\"FechaVigencia\",\"DocumentoFisicoReferencia\",\"NombreArchivoAdjunto\", \"FormatoArchivoAdjunto\", \"IdTipoCanalIngreso\", \"NumeroFirmasMinimas\", \"NumeroFirmasMaximas\")values(:IdAnexo,:IdDocumento,:IdEstado,:NumeroFirmantesEmpresa,:NumeroFirmantesCliente,'','','','','','',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'0001-01-01','',:NombreArchivoAdjunto,:FormatoArchivoAdjunto,:IdTipoCanalIngreso, :NumeroFirmasMinimas, :NumeroFirmasMaximas);";
	public String SQL_ANEXO_UPDATE_ESTADO = "UPDATE \"BSCL.Plataforma.BD::DOC.Anexo\" SET \"IdEstado\" = :IdEstado ,\"FechaVigencia\" = CURRENT_UTCTIMESTAMP WHERE \"IdAnexo\" = :IdAnexo AND \"IdDocumento\" = :IdDocumento";
	public String SQL_SELECT_TIPO_DOCUMENTO = "SELECT TD.\"IdTipoDocumento\", TD.\"NumeroFirmasMinimas\", TD.\"NumeroFirmasMaximas\" FROM \"BSCL.Plataforma.BD::DOC.TipoDocumento\" TD WHERE TD.\"IdTipoDocumento\" = :IdTipoDocumento;";
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SalidaServlet salida = new SalidaServlet();
		
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");

		// POST: Ingresar archivo o datos nuevos; PUT:actualizar archivo o datos
		String IdDocumento = request.getParameter("IdDocumento");
		String IdAnexo = request.getParameter("IdAnexo");
		String IdCliente = request.getParameter("IdCliente");
		String IdTipoDocumento = request.getParameter("IdTipoDocumento");
		String NombreArchivoAdjunto = request.getParameter("NombreArchivoAdjunto");
		String FormatoArchivoAdjunto = request.getParameter("FormatoArchivoAdjunto");
		String NumeroFirmantesEmpresa = request.getParameter("NumeroFirmantesEmpresa");
		String NumeroFirmantesCliente = request.getParameter("NumeroFirmantesCliente");
		String IdTipoCanalIngreso = request.getParameter("IdTipoCanalIngreso");
		Part filePart = request.getPart("data");
		Long IdAnexoEntrada = Long.parseLong(IdAnexo);
		
		
		try (Connection dbConnection = dataSource.getConnection()) {
			SQL sql = new SQL();
			// Get Session Data
			JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
			if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null || dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
				dbConnection.rollback();
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");
				response.getOutputStream().flush();
				return;
			}
			System.out.println(dataJSOut);

			HashMap<String, Object> params = new HashMap<>();
			params.put("IdTipoDocumento", IdTipoDocumento);
			EntidadTipoDocumento tipoDocumento = sql.query(dbConnection, SQL_SELECT_TIPO_DOCUMENTO, params, EntidadTipoDocumento.class).stream().findFirst().orElse(new EntidadTipoDocumento());

						
			//verificar si existe el documento
			if(IdDocumento!=null && IdDocumento.equals("0")){
				//crear documento
				Long idDocumento = Utils.getSecuencia(dbConnection, "BSCL.Plataforma.BD.Secuencias::Documento", (long) 1).get(0);
				
				try{
					EntidadDocumento entidadDocumento = new EntidadDocumento();
					entidadDocumento.IdDocumento = idDocumento;
					entidadDocumento.IdTipoDocumento = IdTipoDocumento;
					entidadDocumento.IdEmpresa = dataJSOut.get("P_OUT_IDEMPRESA").getAsLong();
					entidadDocumento.IdCliente = Long.parseLong(IdCliente);
					entidadDocumento.IdUltimoAnexo = 0L;
					sql.batch(dbConnection, SQL_DOCUMENTO_INSERT, Arrays.asList(entidadDocumento), EntidadDocumento.class);
					
					//para subir el pdf.
					IdDocumento = idDocumento.toString();
				}catch(Exception e){
					dbConnection.rollback();
					throw new Exception(e);
				}
			}
			
			//verificar si existe Anexo
			if(IdAnexo!=null){
				//crear Anexo
				Long idAnexo = Utils.getSecuencia(dbConnection, "BSCL.Plataforma.BD.Secuencias::Anexo", (long) 1).get(0);
				
				try{
					EntidadAnexo entidadAnexo = new EntidadAnexo();
					entidadAnexo.IdAnexo = idAnexo;
					entidadAnexo.IdDocumento = new Long(IdDocumento);
					entidadAnexo.IdEstado = "PEN";
					entidadAnexo.NumeroFirmantesEmpresa = Long.parseLong(NumeroFirmantesEmpresa);
					entidadAnexo.NumeroFirmantesCliente = Long.parseLong(NumeroFirmantesCliente);
					entidadAnexo.NumeroFirmasMinimas = tipoDocumento.NumeroFirmasMinimas;
					entidadAnexo.NumeroFirmasMaximas = tipoDocumento.NumeroFirmasMaximas;
					entidadAnexo.NombreArchivoAdjunto = NombreArchivoAdjunto;
					entidadAnexo.FormatoArchivoAdjunto = FormatoArchivoAdjunto;
					entidadAnexo.IdTipoCanalIngreso = IdTipoCanalIngreso;
					sql.batch(dbConnection, SQL_ANEXO_INSERT, Arrays.asList(entidadAnexo), EntidadAnexo.class);
					
					//para subir el pdf.
					IdAnexo = idAnexo.toString();
				}catch(Exception e){
					dbConnection.rollback();
					throw new Exception(e);
				}
			}
			
			//Se actualiza el El ultimo Anexo
			{
				String update = "UPDATE \"BSCL.Plataforma.BD::DOC.Documento\" "
						+ "SET \"IdUltimoAnexo\" =? ,\"DocumentoSubido\"='S' WHERE \"IdDocumento\" = ?";
				
				
				System.out.println(update);
				
				try (PreparedStatement pstmt = dbConnection
						.prepareStatement(update)) {
					pstmt.setLong(1, new Long(IdAnexo));
					pstmt.setLong(2, new Long(IdDocumento));
					pstmt.executeUpdate();
					
				} catch (SQLException ex) {
					dbConnection.rollback();
					throw new Exception(ex);
				}
			}
			
			//Se actualiza el Adjunto y el estado
			{
				String update = "UPDATE \"BSCL.Plataforma.BD::DOC.Anexo\" "
						+ "SET \"ArchivoAdjunto\" =?, \"IdEstado\" = 'PDF'"
						+ ",\"NombreArchivoAdjunto\" =?, \"FormatoArchivoAdjunto\" =?"
						+ "WHERE \"IdDocumento\" = ? AND \"IdAnexo\" = ?";
				
				
				System.out.println(update);
				
				try (PreparedStatement pstmt = dbConnection
						.prepareStatement(update)) {
					pstmt.setBlob(1, filePart.getInputStream());
					pstmt.setString(2, NombreArchivoAdjunto);
					pstmt.setString(3, FormatoArchivoAdjunto);
					pstmt.setLong(4, new Long(IdDocumento));
					pstmt.setLong(5, new Long(IdAnexo));
					pstmt.executeUpdate();
					//dbConnection.rollback();
					dbConnection.commit();
					
					salida.IdAnexo = new Long(IdAnexo);
					salida.IdDocumento = new Long(IdDocumento);
				} catch (SQLException ex) {
					dbConnection.rollback();
					throw new Exception(ex);
				}
			}
			
			//Se vencen los anexos
			if(IdAnexoEntrada!=null && IdAnexoEntrada!=0){
				try{
					EntidadAnexo entidadAnexo = new EntidadAnexo();
					entidadAnexo.IdAnexo = IdAnexoEntrada;
					entidadAnexo.IdDocumento = Long.parseLong(IdDocumento);
					entidadAnexo.IdEstado = "VCD";
					sql.batch(dbConnection, SQL_ANEXO_UPDATE_ESTADO, Arrays.asList(entidadAnexo), EntidadAnexo.class);
				}catch(Exception e){
					dbConnection.rollback();
					throw new Exception(e);
				}
				salida.IdAnexoVencido = IdAnexoEntrada;
			}
			
			salida.respuesta = "OK";
			try (PrintWriter out = response.getWriter()) {
				response.setContentType("application/text");
				out.print(salida.toString());
				out.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			response.getOutputStream().print("500 - Internal server error");
			response.getOutputStream().flush();
			return;
		}
	}
	
}