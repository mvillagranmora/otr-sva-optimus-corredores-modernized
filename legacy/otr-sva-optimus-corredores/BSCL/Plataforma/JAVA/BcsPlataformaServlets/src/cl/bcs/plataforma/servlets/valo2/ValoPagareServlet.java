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

import cl.bcs.utils.Fecha;
import cl.bcs.utils.ServletUtils;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.ValorizadorPagares;
import cl.bcs.valo2.ValorizadorPagaresParams;

@WebServlet("/valo2/valorizarPagare")
public class ValoPagareServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ValoBonoServlet.class.getName());
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static ValorizadorPagaresParams getParams(
            final HttpServletRequest request) {
        final int diasAno = ServletUtils.getInteger(request, "diasAno", 360);
        final int diasVencimiento = ServletUtils.getInteger(request, "diasVencimiento", 0);
        final Date fechaCaptacion = ServletUtils.getDate(request, "fechaCaptacion");
        final Date fechaVencimiento = ServletUtils.getDate(request, "fechaVencimiento");
        final String nemo = request.getParameter("nemo");
        final BigDecimal tasa = ServletUtils.getBigDecimal(request, "tasa");
        final int tipoInteres = ServletUtils.getInteger(request, "tipoInteres", 0);
        final BigDecimal valorCaptacion = ServletUtils.getBigDecimal(request, "valorCaptacion");
        final BigDecimal valorRescate = ServletUtils.getBigDecimal(request, "valorRescate");
        final String moneda = request.getParameter("moneda");
        final String monedaPago = request.getParameter("monedaPago");

        final ValorizadorPagaresParams params = new ValorizadorPagaresParams();
        params.setDiasAno(diasAno);
        params.setDiasVencimiento(diasVencimiento);
        params.setFechaCaptacion(fechaCaptacion);
        params.setFechaVencimiento(fechaVencimiento);
        params.setNemo(nemo);
        params.setTasa(tasa);
        params.setTipoInteres(tipoInteres);
        params.setValorCaptacion(valorCaptacion);
        params.setValorRescate(valorRescate);
        params.setMoneda(moneda);
        params.setMonedaPago(monedaPago);
        return params;
    }

    private static String getReplyAsJson(
            final ValorizadorPagaresParams params) {
        final JSONObject obj = new JSONObject();
        setValue(obj, "codigoRetorno", 0);
        setValue(obj, "diasAno", params.getDiasAno());
        setValue(obj, "diasVencimiento", params.getDiasVencimiento());
        setValue(obj, "fechaCaptacion", Fecha.stringToSqlDate(params.getFechaCaptacion()));
        setValue(obj, "fechaVencimiento", Fecha.stringToSqlDate(params.getFechaVencimiento()));
        setValue(obj, "nemo", params.getNemo());
        setValue(obj, "tasa", params.getTasa());
        setValue(obj, "tipoInteres", params.getTipoInteres());
        setValue(obj, "valorCaptacion", params.getValorCaptacion());
        setValue(obj, "valorRescate", params.getValorRescate());
        setValue(obj, "valorCaptacionPago", params.getValorCaptacionPago());
        setValue(obj, "valorRescatePago", params.getValorRescatePago());
        setValue(obj, "moneda", params.getMoneda());
        setValue(obj, "monedaPago", params.getMonedaPago());
        return obj.toJSONString();
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
        final ValorizadorPagaresParams params = getParams(request);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Requerimiento de valorizacion: " + params);
        }

        try {
            final ValorizadorContexto contexto = new ValorizadorContexto(dataSource);
            final ValorizadorPagares valorizador = new ValorizadorPagares(contexto);

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
