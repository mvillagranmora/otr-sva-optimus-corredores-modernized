package cl.bcs.plataforma.servlets.valo2;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.google.gson.Gson;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.ServletUtils;
import cl.bcs.utils.Utils;
import cl.bcs.valo2.ValorizadorBonos;
import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;

@WebServlet("/valo2/valorizar")
public class ValoBonoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ValoBonoServlet.class.getName());
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static ValorizadorBonosParams getParams(
            final HttpServletRequest request) {
        final String nemo = request.getParameter("nemo");
        final BigDecimal valorNominal = ServletUtils.getBigDecimal(request, "valorNominal");
        final Date fechaValorizacion = ServletUtils.getDate(request, "fechaValorizacion");
        final BigDecimal precio = ServletUtils.getBigDecimal(request, "precio");
        final BigDecimal tasa = ServletUtils.getBigDecimal(request, "tasa");
        final BigDecimal montoTotal = ServletUtils.getBigDecimal(request, "montoTotal");

        final ValorizadorBonosParams params = new ValorizadorBonosParams();
        params.setNemo(nemo);
        params.setFechaValorizacion(fechaValorizacion);
        params.setPrecio(precio);
        params.setTasa(tasa);
        params.setValorNominal(valorNominal);
        params.setMontoTotal(montoTotal);
        return params;
    }

    private static String getReplyAsJson(
            final ValorizadorBonosParams params) {
    	Gson gson = Utils.getGsonBuilder();
        final JSONObject obj = new JSONObject();
        setValue(obj, "codigoRetorno", params.getCodigoRetorno());
        setValue(obj, "mensaje", params.getMensaje());
        setValue(obj, "convexidad", params.getConvexidad());
        setValue(obj, "duration", params.getDuration());
        setValue(obj, "fechaValorizacion", DATE_FORMAT.format(params.getFechaValorizacion()));
        setValue(obj, "montoCapital", params.getMontoCapital());
        setValue(obj, "montoIntereses", params.getMontoIntereses());
        setValue(obj, "montoReajuste", params.getMontoReajuste());
        setValue(obj, "montoTotal", params.getMontoTotal());
        setValue(obj, "nemo", params.getNemo());
        setValue(obj, "precio", params.getPrecio());
        setValue(obj, "tasa", params.getTasa());
        setValue(obj, "tasa360", params.getTasa360());
        setValue(obj, "valorNominal", params.getValorNominal());
        setValue(obj, "monedaPago", params.getMonedaPago());
        setValue(obj, "montoTotalPago", params.getMontoTotalPago());
        setValue(obj, "cupones",params.getCupones());
        return gson.toJson(obj);
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
        final ValorizadorBonosParams params = getParams(request);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Requerimiento de valorizacion: " + params);
        }

        try {
            final ValorizadorContexto contexto = new ValorizadorContexto(dataSource);
            final ValorizadorBonos valorizador = new ValorizadorBonos(contexto);

            valorizador.valorizar(params);
            params.setCodigoRetorno(0);
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Valorizacion OK: " + params);
            }

        } catch (final RuntimeException | SQLException ex) {
            params.setCodigoRetorno(-1);
            params.setMensaje(ex.getMessage());
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Error al valorizar: " + params, ex);
                ex.printStackTrace(System.out);
            }
        }

        final String reply = getReplyAsJson(params);
        final PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.print(reply);
    }

    
}
