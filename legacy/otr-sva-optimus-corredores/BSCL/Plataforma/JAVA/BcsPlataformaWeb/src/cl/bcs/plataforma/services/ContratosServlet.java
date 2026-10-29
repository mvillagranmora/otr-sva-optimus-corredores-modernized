package cl.bcs.plataforma.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.utils.Utils;

/**
 * Servlet implementation class DatosContratos
 */
@WebServlet("/DatosContratosBin")
@MultipartConfig
public class ContratosServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
	private static final Logger logger = LogManager.getLogger(ContratosServlet.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ContratosServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String filename = null;
		String opcion = request.getParameter("OP");
		String code = request.getParameter("filename");
		String estatico = request.getParameter("estatico");
		String type = "";
		String extension = "";
		
		logger.info("INICIO: doGet");
		
		if(code!=null && opcion!=null){
			System.out.println("get: " + code);
			
			try (Connection dbConnection = dataSource.getConnection()){
				//Get Session Data
				JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
				if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
						|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
					dbConnection.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - NOT AUTHORIZED");
					response.getOutputStream().flush();
					return;
				}
				long SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
				
				if(opcion.equals("CONTRACT")){
					if(estatico.toUpperCase().equals("S")){
						//Obtener archivo o contrato estaticos (pdf)
						final String selectSQL = "select top 1 \"CodigoArchivo\", \"Binario\", \"TipoBinario\", \"TipoPresentacion\" from \"BSCL.Plataforma.BD.Vistas::CTT.ConsultarArchivosContrato\" where \"CodigoArchivo\"=? AND \"IdEmpresa\"=?";
						try(PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)){
							pstmt.setString(1, code);
							pstmt.setLong(2, SesionIdEmpresa);
							
							ResultSet rs = pstmt.executeQuery();
							
							while(rs.next()){
								
								filename = rs.getString(1);
								System.out.println(filename);
								try(InputStream is = rs.getBlob(2).getBinaryStream()){
									IOUtils.copy(is,response.getOutputStream());
								}
								extension = rs.getString(3);
								type = rs.getString(4);
							}
							filename = filename+"."+extension;
						}
					}
				}else{
					//Obtener archivo secundario
					final String selectSQL = "select top 1 \""+code+"\", \""+code+"Bin\", \""+code+"Tipo\" from \"BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales\" WHERE \"IdEmpresa\"=?";
					
					try(PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)){
						pstmt.setLong(1, SesionIdEmpresa);
						
						ResultSet rs = pstmt.executeQuery();
						
						type = "";
						while(rs.next()){
							
							filename = rs.getString(1);
							System.out.println(filename);
							try(InputStream is = rs.getBlob(2).getBinaryStream()){
								IOUtils.copy(is,response.getOutputStream());
							}
							type = rs.getString(3);
						}
					}
				}
				
				response.setHeader("Content-disposition","inline; filename="+filename);
				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
				response.setHeader("Pragma", "no-cache");//HTTP 1.0
				response.setContentType(type);
				System.out.println(type);
				response.getOutputStream().flush();
				
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("DOWNLOAD ERROR: "+e.getMessage());
				response.setStatus(500);
				response.getOutputStream().print("500 - BAD REQUEST");	
				response.getOutputStream().flush();
				return;
			}
			
		}else{
			response.setStatus(401);
			response.getOutputStream().print("500 - BAD REQUEST");	
			response.getOutputStream().flush();	
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		logger.info("INICIO: doPost");
		
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String opcion = request.getParameter("OP");
		
		
		logger.info("OPCION: " + opcion);
		//POST: Ingresar archivo o datos nuevos; PUT:actualizar archivo o datos
		if(!opcion.equals("PUT")){
			String modulo = request.getParameter("modulo");
			
			String code = request.getParameter("code");
			String desc = request.getParameter("desc");
			String name = request.getParameter("name");
			String type = request.getParameter("type");
			String estatico = request.getParameter("static");//static reserved word
			String ext = request.getParameter("ext");
			String idEstado = request.getParameter("estado");
			
			String tipoPersona = request.getParameter("tipoPersona");
			String tipoCliente = request.getParameter("tipoCliente");
			Long IdTipoClienteCmf = Long.parseLong(request.getParameter("IdTipoClienteCmf"));

			Part filePart = request.getPart("data");
			
			logger.info("DATOS RECIBIDOS: "  + code + ", " + desc + ", " + name + ", " + type + ", " + estatico + ", " + ext + ", " + idEstado + ", " + tipoPersona + ", " + tipoCliente + ", " + IdTipoClienteCmf);
			
			try (Connection dbConnection = dataSource.getConnection()){
				//Get Session Data
				JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
				if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
						|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
					dbConnection.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - NOT AUTHORIZED");
					response.getOutputStream().flush();
					return;
				}
				long SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
				long SesionIdUsuario = dataJSOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
				
				String respuesta="";
				if(opcion.equals("CONTRACT")){
						//Actualizacion Archivo Contrato
					
						UUID idTemplate = null;
						if(estatico.equals("S")){
							//no es estatico, almacenar archivo en template
							idTemplate = UUID.randomUUID();
							
							logger.info("MOMENTO PREVIO AL INSERT a Template");
							
							String insert = "INSERT INTO \"BSCL.Report::Template\" ( "
									+ "\"Id\", \"ReportName\", \"MasterReportId\", \"ReportFile\", "
									+ "\"ReportVersion\") VALUES ( ?, ?, ?, ?, ?)";
							System.out.println(insert);
							logger.info("INSERT: " + insert);
							try(PreparedStatement pstmt = dbConnection.prepareStatement(insert)){	
								pstmt.setString(1, idTemplate.toString());
								pstmt.setString(2, idTemplate.toString());
								pstmt.setString(3, null);
								pstmt.setBlob(4, filePart.getInputStream());
								pstmt.setInt(5, 1);
								pstmt.executeUpdate();
								dbConnection.commit();
							}catch(SQLException ex){
								ex.printStackTrace();
								dbConnection.rollback();
								throw new Exception(ex);
							}
						}
						else {
							logger.info("MOMENTO PREVIO AL INSERT a ParametroContratos");
							final String selectSQL = "SELECT \"BSCL.Plataforma.BD.Secuencias::ParametroContratos\".NEXTVAL FROM dummy";
							Long idSecuencia = 0L;
							try(PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)){
								ResultSet rs = pstmt.executeQuery();
								if(rs.next()){
									idSecuencia = rs.getLong(1);
								}
							}
							catch(SQLException ex){
								ex.printStackTrace();
								throw new Exception(ex);
							}
							
							String insert = "INSERT INTO \"BSCL.Plataforma.BD::CTT.ParametroContratos\" (\"IdParametroContrato\", \"CodigoArchivo\", \"Nombre\", \"Tipo\", \"Descripcion\", \"IdentificaPersona\") VALUES(?, ?, ?, ?, ?, ?)";
							
							logger.info("INSERT: " + insert);
							try(PreparedStatement pstmt = dbConnection.prepareStatement(insert)){	
								pstmt.setLong(1, idSecuencia);
								pstmt.setString(2, code);
								pstmt.setString(3, "p_Rut");
								pstmt.setString(4, "STRING");
								pstmt.setString(5, "Documento Identidad Cliente");
								pstmt.setString(6, "R");
								pstmt.execute();
								dbConnection.commit();
							}catch(SQLException ex){
								ex.printStackTrace();
								dbConnection.rollback();
								throw new Exception(ex);
							}
						}
						
						logger.info("MOMENTO PREVIO AL INSERT a ArchivosContrato");
						
						final String insertSql = "INSERT INTO \"BSCL.Plataforma.BD::CTT.ArchivosContrato\"(\"IdEmpresa\", \"CodigoArchivo\", \"Descripcion\", \"IdUsuario\", \"FechaModificacion\", \"Binario\", \"TipoBinario\", \"TipoPresentacion\", \"EsEstatico\", \"IdTemplate\", \"IdEstado\", \"IdTipoPersona\", \"IdTipoCliente\", \"IdTipoClienteCmf\") VALUES(?,?,?,?,CURRENT_UTCTIMESTAMP,?,?,?,?,?,?,?,?,?)";
						System.out.println(insertSql);
						logger.info("INSERT: " + insertSql);
						try(PreparedStatement pstmt = dbConnection.prepareStatement(insertSql)){
							pstmt.setLong(1, SesionIdEmpresa);
							pstmt.setString(2, code);
							pstmt.setString(3, desc);
							pstmt.setLong(4, SesionIdUsuario);
							pstmt.setBinaryStream(5, filePart.getInputStream());
							pstmt.setString(6, ext);
							pstmt.setString(7, type);
							pstmt.setString(8, estatico);
							pstmt.setString(9, (idTemplate!=null)?idTemplate.toString(): "");
							pstmt.setString(10, idEstado);
							pstmt.setString(11, tipoPersona);
							pstmt.setString(12, tipoCliente);
							pstmt.setLong(13, IdTipoClienteCmf);
							pstmt.execute();
							dbConnection.commit();
							Utils.addLog(dbConnection, Utils.getSchema(), SesionIdEmpresa, SesionIdUsuario, modulo, "MODIFICACION","UI_MODIFICACION_CONTRATO", SesionIdUsuario, 0);
							respuesta = "OK";
						}catch(SQLException ex){
							System.out.println("UPLOAD CONTRACT:" + ex.getMessage());
							dbConnection.rollback();
							logger.error("CATCH INSERT: " + ex + insertSql);
							respuesta = "NOK";
						}
				}else if(opcion.equals("CONTRACTDATA")){
						//Actualizacion de ContratoDatosGenerales
						InputStream fileContent = filePart.getInputStream();
						logger.info("CONTRACTDATA");
						System.out.println("Archivo recibido, almacenando en BD...");
						//InputStream is = new ByteArrayInputStream(b);
						
						//final String insertSql = "INSERT INTO \"IMAGE_STORE\"(\"Filename\", \"File\") VALUES(?,?)";
						final String updateSql = "UPDATE \"BSCL.Plataforma.BD::CTT.ContratoDatosGenerales\" SET \""+code+"\" = ?, \""+code+"Bin\" = ?, \""+code+"Tipo\"=? WHERE \"IdEmpresa\"=?";
						System.out.println(updateSql);
						logger.info("UPDATE: " + updateSql);
						try(PreparedStatement pstmt = dbConnection.prepareStatement(updateSql)){
							pstmt.setString(1, name);
							pstmt.setBinaryStream(2, fileContent);
							pstmt.setString(3, type);
							pstmt.setLong(4, SesionIdEmpresa);
							pstmt.execute();
							dbConnection.commit();
							Utils.addLog(dbConnection, Utils.getSchema(), SesionIdEmpresa, SesionIdUsuario, modulo, "MODIFICACION","UI_MODIFICACION_RECURSO_CONTRATO", SesionIdUsuario, 0);
							respuesta = "OK";
						}catch(SQLException ex){
							System.out.println("UPLOAD DATA:" + ex.getMessage());
							dbConnection.rollback();
							logger.error("CATCH UPDATE: " + ex);
							respuesta = "NOK";
						}
				}else{
					throw new Exception();
				}
				
				try(PrintWriter out = response.getWriter()){
					response.setContentType("application/text");
					out.print(respuesta);
					out.flush();
				}
				
			} catch (Exception e) {
				response.setStatus(401);
				response.getOutputStream().print("401 - BAD REQUEST");	
				response.getOutputStream().flush();
				return;
			}
			
		}else{
			//workarround puesto que no esta soportado el method PUT con multipart-form
			//http://programmers.stackexchange.com/questions/114156/why-are-there-are-no-put-and-delete-methods-on-html-forms
			execPut(request, response);
		}
	}
	
	protected void execPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		logger.info("INICIO execPut");
		
		//parametros de entrada genericos
		String modulo = request.getParameter("modulo");
		
		String template = request.getParameter("template");
		String initCode = request.getParameter("initCode");
		String code = request.getParameter("code");
		String desc = request.getParameter("desc");
		String estatico = request.getParameter("static");//static reserved word
		String state= request.getParameter("estado");
		
		String name = request.getParameter("name");
		String type = request.getParameter("type");
		String ext = request.getParameter("ext");	
		Part filePart = request.getPart("data");
		
		String tipoPersona = request.getParameter("tipoPersona");
		String tipoCliente = request.getParameter("tipoCliente");
		Long IdTipoClienteCmf = Long.parseLong(request.getParameter("IdTipoClienteCmf"));
		
		logger.info("DATOS RECIBIDOS: " + code + ", " + desc + ", " + name + ", " + type + ", " + estatico + ", " + ext + ", " + "" + ", " + tipoPersona + ", " + tipoCliente + ", " + IdTipoClienteCmf);
		
		try (Connection dbConnection = dataSource.getConnection()){
			//Get Session Data
			JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
			if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
					|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
				dbConnection.rollback();
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");
				response.getOutputStream().flush();
				return;
			}
			long SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
			long SesionIdUsuario = dataJSOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
			
			String respuesta="";
			
			if(estatico.equals("N") && name!=null){
				//no es estatico, almacenar archivo en template
				logger.info("UPDATE TEMPLATE");
				String updateSql = "UPDATE \"BSCL.Report::Template\" SET \"ReportFile\"=? WHERE \"Id\"=?";
				System.out.println(updateSql);
				logger.info("UPDATE TEMPLATE: " + updateSql);
				try(PreparedStatement pstmt = dbConnection.prepareStatement(updateSql)){	
					pstmt.setBlob(1, filePart.getInputStream());
					pstmt.setString(2, template);
					pstmt.executeUpdate();
					dbConnection.commit();
				}catch(SQLException ex){
					dbConnection.rollback();
					logger.error("TEMPLATE catch: "+ ex);
					throw new Exception(ex);
				}
			}
			if(name!=null){
				logger.info("UPDATE ArchivosContrato name not null");
				final String updateSql = "UPDATE \"BSCL.Plataforma.BD::CTT.ArchivosContrato\" "
						+ "SET \"CodigoArchivo\"=?, \"Descripcion\"=?, \"IdUsuario\"=?, \"FechaModificacion\"= CURRENT_UTCTIMESTAMP, \"Binario\"=?, \"TipoBinario\"=?, \"TipoPresentacion\"=?, \"EsEstatico\"=?, \"IdTemplate\"=?, \"IdEstado\"=?, \"IdTipoPersona\"=?, \"IdTipoCliente\"=?, \"IdTipoClienteCmf\"=? WHERE \"CodigoArchivo\"=? and \"IdEmpresa\"=?";
				System.out.println(updateSql);
				logger.info("UPDATE ArchivosContrato: " + updateSql);
				try(PreparedStatement pstmt = dbConnection.prepareStatement(updateSql)){
					pstmt.setString(1, code);
					pstmt.setString(2, desc);
					pstmt.setLong(3, SesionIdUsuario);
					pstmt.setBinaryStream(4, filePart.getInputStream());
					pstmt.setString(5, ext);
					pstmt.setString(6, type);
					pstmt.setString(7, estatico );
					pstmt.setString(8, template);
					pstmt.setString(9, state);
					pstmt.setString(10, tipoPersona);
					pstmt.setString(11, tipoCliente);
					pstmt.setLong(12, IdTipoClienteCmf);
					pstmt.setString(13, initCode);
					pstmt.setLong(14, SesionIdEmpresa);
					pstmt.execute();
					dbConnection.commit();
				}catch(SQLException ex){
					dbConnection.rollback();
					logger.error(ex);
					throw new Exception(ex);
				}
			}else{
				logger.info("UPDATE ArchivosContrato name null");
				final String updateSql = "UPDATE \"BSCL.Plataforma.BD::CTT.ArchivosContrato\" "
						+ "SET \"CodigoArchivo\"=?, \"Descripcion\"=?, \"IdUsuario\"=?, \"FechaModificacion\"= CURRENT_UTCTIMESTAMP, \"EsEstatico\"=?, \"IdEstado\"=?, \"IdTipoPersona\"=?, \"IdTipoCliente\"=?, \"IdTipoClienteCmf\"=?  WHERE \"CodigoArchivo\"=? and \"IdEmpresa\"=?";
				
				System.out.println(updateSql);
				logger.info("UPDATE ArchivosContrato: " + updateSql);
				try(PreparedStatement pstmt = dbConnection.prepareStatement(updateSql)){
					pstmt.setString(1, code);
					pstmt.setString(2, desc);
					pstmt.setLong(3, SesionIdUsuario);
					pstmt.setString(4, estatico);
					pstmt.setString(5, state);
					pstmt.setString(6, tipoPersona);
					pstmt.setString(7, tipoCliente);
					pstmt.setLong(8, IdTipoClienteCmf);
					pstmt.setString(9, initCode);
					pstmt.setLong(10, SesionIdEmpresa);
					pstmt.execute();
					dbConnection.commit();
				}catch(SQLException ex){
					dbConnection.rollback();
					logger.error(ex);
					throw new Exception(ex);	
				}
			}
			Utils.addLog(dbConnection, Utils.getSchema(), SesionIdEmpresa, SesionIdUsuario, modulo, "MODIFICACION","UI_MODIFICACION_CONTRATO", SesionIdUsuario, 0);
			respuesta = "OK";
						
			System.out.println("Archivo "+code+" almacenado.");
			response.setContentType("application/text");
			
			try(PrintWriter out = response.getWriter()){
				out.print(respuesta);
				out.flush();
			}
			
		} catch (Exception e) {
			response.setStatus(401);
			response.getOutputStream().print("401 - BAD REQUEST");	
			response.getOutputStream().flush();
			return;
		}
	}
	
}