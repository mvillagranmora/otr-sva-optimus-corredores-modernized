package cl.bcs.plataforma.servlets.normativo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
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

import org.apache.commons.io.FilenameUtils;

import cl.bcs.normativa.dal.DocumentoDAO;
import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.utils.ZipUtil;

@WebServlet("/files/download")
public class FileDownloadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileDownloadServlet.class.getName());
    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/hanaDB")
    DataSource dataSource;

    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                    throws ServletException,
                    IOException {

        try {
            // obtains ServletContext.
            final ServletContext context = getServletContext();

            try (final Connection conn = dataSource.getConnection()) {
                // reads input file from an absolute path.
                String filePath = getFilePath(conn, request);

                // manages zip file.
                final String zipped = request.getParameter("zipped");
                if (zipped != null) {
                    final boolean isZipped = Boolean.valueOf(zipped);
                    if (isZipped) {
                        final String zipPath = FilenameUtils.removeExtension(filePath) + ".zip";
                        ZipUtil.generateZip(filePath, zipPath);
                        filePath = zipPath;
                    }
                }

                // gets MIME type of the file.
                String mimeType = context.getMimeType(filePath);
                if (mimeType == null) {
                    // set to binary type if MIME mapping not found.
                    mimeType = "text/plain";
                }

                final File file = new File(filePath);

                // modifies response.
                response.setContentType(mimeType);
                response.setContentLength((int) file.length());

                // forces download.
                final String headerValue = String.format("attachment; filename=\"%s\"",
                        file.getName());
                response.setHeader("Content-Disposition", headerValue);

                // Cookie para que el cliente sepa que hubo una descarga.
                response.setHeader("Set-Cookie", "fileDownload=true; path=/");

                try (FileInputStream is = new FileInputStream(file);
                        OutputStream os = response.getOutputStream()) {
                    final byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            }

            LOGGER.log(Level.INFO, "Archivo bajado OK");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al procesar download", ex);
            throw new ServletException("Error al procesar download", ex);
        }
    }

    private String getFilePath(
            final Connection conn,
            final HttpServletRequest request)
                    throws RecordNotFoundException,
                    SQLException {
        final String schema = request.getParameter("schema");
        final long idDocumento = Long.valueOf(request.getParameter("idDocumento"));
        final Documento documento = DocumentoDAO.buscarPorId(conn, schema, idDocumento);
        final String nombreArchivo = request.getParameter("nombreArchivo");
        if (nombreArchivo == null) {
            final String tipo = request.getParameter("tipo");
            final long idProceso = Long.valueOf(request.getParameter("idProceso"));
            return LocalizadorArchivo.buscarRutaArchivo(conn, schema, tipo, documento, idProceso);
        } else {
            return LocalizadorArchivo.buscarRutaRaiz(conn, schema, documento.getIdEmpresa())
                    + File.separator + nombreArchivo;
        }

    }

}
