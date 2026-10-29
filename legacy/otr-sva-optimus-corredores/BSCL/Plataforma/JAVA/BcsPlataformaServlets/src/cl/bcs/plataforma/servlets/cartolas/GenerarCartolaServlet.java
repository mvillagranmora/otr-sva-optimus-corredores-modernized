package cl.bcs.plataforma.servlets.cartolas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.Map.Entry;
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

import cl.bcs.normativa.cartolas.master.CartolaMaster;
import cl.bcs.normativa.cartolas.master.GeneradorMaster;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.utils.SqlUtil;

@WebServlet("/cartolas/generar")
public class GenerarCartolaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(GenerarCartolaServlet.class.getName());
    private static final long serialVersionUID = 1L;

    public static Properties getProperties(
            final HttpServletRequest request) {
        Properties props = new Properties();
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            props.put(key, value);
        }

        return props;
    }

    @Resource(name = "jdbc/hanaDB")
    private DataSource dataSource;

    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                    throws ServletException,
                    IOException {

        try {
            final Properties props = getProperties(request);
            final ContextoCartolas contexto = new ContextoCartolas(dataSource.getConnection(),
                    props, 0);
            final GeneradorMaster generador = new GeneradorMaster(contexto, dataSource);
            CartolaMaster cartolaMaster;

            final String tipo = request.getParameter("tipo");
            if (tipo.equals("online")) {
                final long idCliente = Long.valueOf(request.getParameter("idCliente"));
                final long idPortafolio = Long.valueOf(request.getParameter("idPortafolio"));
                final Date fechaDesde = SqlUtil.getDate(request.getParameter("fechaDesde"));
                final Date fechaHasta = SqlUtil.getDate(request.getParameter("fechaHasta"));
                cartolaMaster = generador.generarOnline(idCliente, idPortafolio, fechaDesde,
                        fechaHasta);
            } else {
                final long idCartola = Long.valueOf(request.getParameter("idCartola"));
                cartolaMaster = generador.generar(idCartola);
            }

            final ServletContext context = getServletContext();
            final String filename = cartolaMaster.getNombreArchivo();
            final String mimeType = context.getMimeType(filename);

            response.setContentType(mimeType);
            response.setContentLength(cartolaMaster.getBytes().length);

            final String headerValue = String.format("attachment; filename=\"%s\"", filename);
            response.setHeader("Content-Disposition", headerValue);

            // Cookie para que el cliente sepa que hubo una descarga.
            response.setHeader("Set-Cookie", "fileDownload=true; path=/");

            try (InputStream is = new ByteArrayInputStream(cartolaMaster.getBytes());
                    OutputStream os = response.getOutputStream()) {
                final byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            LOGGER.log(Level.INFO, "Descarga de cartola OK: " + filename);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error en descarga de cartola", ex);
            throw new ServletException("Error en descarga de cartola", ex);
        }
    }
}
