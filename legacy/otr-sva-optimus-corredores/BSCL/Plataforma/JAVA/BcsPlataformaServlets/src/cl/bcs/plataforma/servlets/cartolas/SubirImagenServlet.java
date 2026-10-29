package cl.bcs.plataforma.servlets.cartolas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;

import cl.bcs.normativa.cartolas.dal.RecursoService;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.utils.PropertiesUtil;
import cl.bcs.utils.ServletUtils;

@WebServlet("/cartolas/subirImagen")
public class SubirImagenServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SubirImagenServlet.class.getName());
    private static final int MAX_FILE_SIZE = 1 * 1024 * 1024;
    private static final int MAX_MEM_SIZE = 1 * 1024 * 1024;
    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/hanaDB")
    private DataSource dataSource;
    private int maxFileSize = MAX_FILE_SIZE;
    private int maxMemSize = MAX_MEM_SIZE;
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
                final String filename = receiveAndSaveFile(conn, request);
                final String reply = getReplyAsJson(filename);
                final PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                out.print(reply);
                LOGGER.log(Level.INFO, "Archivo subido OK: " + filename);
            }
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al procesar upload", ex);
            throw new ServletException("Error al procesar upload", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private String getReplyAsJson(
            final String filename) {
        final JSONObject obj = new JSONObject();
        obj.put("code", "OK");
        obj.put("message", "Archivo recibido");
        obj.put("filename", filename);
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

    private String receiveAndSaveFile(
            final Connection conn,
            final HttpServletRequest request)
                    throws FileUploadException,
                    SQLException,
                    IOException {

        final DiskFileItemFactory factory = new DiskFileItemFactory();

        // maximum size that will be stored in memory.
        factory.setSizeThreshold(maxMemSize);

        // Location to save data that is larger than maxMemSize.
        factory.setRepository(new File(tempFilePath));

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload(factory);

        // maximum file size to be uploaded.
        upload.setSizeMax(maxFileSize);

        // Parse the request to get file items.
        final List<?> fileItems = upload.parseRequest(request);

        // Process the uploaded file items.
        final Iterator<?> i = fileItems.iterator();

        final HashMap<String, String> map = new HashMap<String, String>();

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

        save(conn, map, fileItem);

        return fileItem.getName();
    }

    private void save(
            final Connection conn,
            final HashMap<String, String> map,
            final FileItem fileItem)
                    throws SQLException,
                    IOException {
        try (final InputStream is = fileItem.getInputStream()) {
            final Properties props = PropertiesUtil.newProperties(map);
            final ContextoCartolas contexto = new ContextoCartolas(conn, props, 0);
            final long idRecurso = Long.valueOf(map.get("idRecurso"));
            RecursoService.updateImage(contexto, idRecurso, is);
        }
    }

}
