package cl.bcs.plataforma.servlets.valo2;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.simple.JSONObject;

import com.google.gson.GsonBuilder;

import cl.bcs.utils.ServletUtils;
import cl.bcs.valo2.Cuponera;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Cupon;

@WebServlet("/valo2/cupones")
public class CuponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CuponServlet.class.getName());

    private static String getErrorReplyAsJson(
            final Exception ex) {
        final JSONObject obj = new JSONObject();
        setValue(obj, "codigoRetorno", -1);
        setValue(obj, "mensaje", ex.getMessage());
        return obj.toJSONString();
    }

    private static String getReplyAsJson(
            final List<Cupon> cupones) {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(cupones);
    }

    @SuppressWarnings("unchecked")
    public static void setValue(
            final JSONObject obj,
            final String name,
            final Object val) {
        if (val != null) {
            obj.put(name, val);
        }
    }

    @Resource(name = "jdbc/hanaDB")
    private DataSource dataSource;

    @Override
    public void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                    throws ServletException,
                    IOException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Requerimiento de cupones");
        }

        String reply;
        try {
            final ValorizadorContexto contexto = new ValorizadorContexto(dataSource);
            final Cuponera cuponera = new Cuponera(contexto);

            final String nemo = request.getParameter("nemo");
            final Date fecha = ServletUtils.getDate(request, "fecha");
            final BigDecimal valorNominal = ServletUtils.getBigDecimal(request, "valorNominal");

            final List<Cupon> cupones = cuponera.obtenerCupones(nemo, fecha, valorNominal);
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Consulta cupones OK: " + nemo);
            }

            reply = getReplyAsJson(cupones);

        } catch (final RuntimeException | SQLException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Error al consultar cupones", ex);
            }

            reply = getErrorReplyAsJson(ex);
        }

        final PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.print(reply);
    }

}
