package cl.bcs.plataforma.servlets.normativo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;

import cl.bcs.normativa.batch.Batch;
import cl.bcs.normativa.dal.DocumentoDAO;
import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.utils.ServletUtils;
 
@WebServlet("/files/upload")
public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName());
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_MEM_SIZE = 4 * 1024;
    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/hanaDB")
    DataSource dataSource;

    private Documento documento;
    private HashMap<String, String> map;
    private int maxFileSize = MAX_FILE_SIZE;
    private int maxMemSize = MAX_MEM_SIZE;
    private String schema;
    private String tempFilePath;

    @Override
    public void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                    throws ServletException {

        throw new ServletException(
                "GET method used with " + getClass().getName() + ": POST method required.");
    }

    @Override
    public void doPost(
            final HttpServletRequest request,
            final HttpServletResponse response)
                    throws IOException,
                    ServletException {

        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Falta archivo en los parametros");
            return;
        }

        try {
            try (final Connection conn = dataSource.getConnection()) {

                receiveFile(conn, request);
                scheduleBatch(conn);

                final String filename = map.get("rutaArchivo");
                final String reply = getReplyAsJson(filename);
                final PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                out.print(reply);
            }

            LOGGER.log(Level.INFO, "Archivo subido OK");
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al procesar upload", ex);
            throw new ServletException("Error al procesar upload", ex);
        }
    }

    private String getFilePath(
            final Connection conn,
            final FileItem item)
                    throws SQLException {
        return LocalizadorArchivo.buscarRutaArchivoEntrada(conn, schema, documento,
                FilenameUtils.getName(item.getName()));
    }

    @SuppressWarnings("unchecked")
    private String getReplyAsJson(
            final String filename) {
        final JSONObject obj = new JSONObject();
        obj.put("code", "OK");
        obj.put("message", "Archivo recibido");
        obj.put("filename", FilenameUtils.getName(filename));
        return obj.toJSONString();
    }

    @Override
    public void init() {
        final ServletContext context = getServletContext();
        this.tempFilePath = context.getInitParameter("tempFilePath");
        this.maxFileSize = ServletUtils.getInteger(context.getInitParameter("maxFileSize"),
                MAX_FILE_SIZE);
        this.maxMemSize = ServletUtils.getInteger(context.getInitParameter("maxMemSize"),
                MAX_MEM_SIZE);
    }

    private void receiveFile(
            final Connection conn,
            final HttpServletRequest request)
                    throws Exception {

        final DiskFileItemFactory factory = new DiskFileItemFactory();

        // maximum size that will be stored in memory.
        factory.setSizeThreshold(maxMemSize);

        // Location to save data that is larger than maxMemSize.
        //factory.setRepository(new File(tempFilePath));
        factory.setRepository(new File("/tmp/"));

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload(factory);

        // maximum file size to be uploaded.
        upload.setSizeMax(maxFileSize);

        // Parse the request to get file items.
        final List<?> fileItems = upload.parseRequest(request);

        // Process the uploaded file items.
        final Iterator<?> i = fileItems.iterator();

        map = new HashMap<String, String>();

        FileItem fileItem = null;
        while (i.hasNext()) {
            final FileItem fi = (FileItem) i.next();
            if (fi.isFormField()) {
                final String fieldName = fi.getFieldName();
                final String fieldValue = fi.getString();
                map.put(fieldName, fieldValue);
            } else {
                fileItem = fi;
            }
        }

        schema = map.get("schema");
        final long idDocumento = Long.valueOf(map.get("idDocumento"));
        documento = DocumentoDAO.buscarPorId(conn, schema, idDocumento);

        final String path = getFilePath(conn, fileItem);
        map.put("rutaArchivo", path);
        fileItem.write(new File(path));
    }

    private void scheduleBatch(
            final Connection conn)
                    throws SQLException {
        final long idUsuario = Long.valueOf(map.get("idUsuario"));
        Batch.scheduleProcess(conn, documento.getIdEmpresa(), idUsuario, schema, map);
    }

}