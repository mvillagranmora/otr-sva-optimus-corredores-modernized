package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Commission blotter query (legacy {@code ConsultarCobroComisionRegistros}). Behaviours CTA-B-26..31.
 *
 * <p>One row per client, portfolio, product, state and APV flag: the custody and the cash patrimony commissions of the date range are
 * added (an edited commission replaces the calculated amount, once per branch: custody and cash each add it, README defect 5 /
 * CTA-B-28). Filters: client, portfolio, state (blank = everything but INA), product group (9999 = non APV) and period.
 */
@Service
public class ConsultarCobroComisionRegistrosService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCobroComisionRegistrosService.class);

    private static final String SQL_CONSULTAR = SqlReportes.recurso("CobroComision_ConsultarRegistros");

    private final SqlReportes sql;

    public ConsultarCobroComisionRegistrosService(DataSource dataSource) {
        this.sql = new SqlReportes(new NamedParameterJdbcTemplate(dataSource));
    }

    @Transactional(readOnly = true)
    public JsonObject execute(JsonObject params) {
        return JsonReportes.escribir(consultar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida consultar(Entrada in) {
        log.info("INICIO::ConsultarCobroComisionRegistros");
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("p_Cliente", in.p_Cliente);
        parametros.put("p_Portafolio", in.p_Portafolio);
        parametros.put("p_FechaDesde", in.p_FechaDesde);
        parametros.put("p_FechaHasta", in.p_FechaHasta);
        parametros.put("p_Periodo", in.p_Periodo);
        parametros.put("p_EstadoComision", in.p_IdEstado);
        parametros.put("p_ProductoAgrupador", in.p_IdProductoAgrupador);

        Salida salida = new Salida();
        salida.P_OUT_TABLA = sql.consultar(SQL_CONSULTAR, parametros, Registro.class);
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        log.info("FIN::ConsultarCobroComisionRegistros");
        return salida;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "CTA";
        public String p_UsaReglasNegocio = "N";
        public Long p_Cliente = 0L;
        public Long p_Portafolio = 0L;
        public String p_FechaDesde = "0001-01-01";
        public String p_FechaHasta = "0001-01-01";
        public Long p_Periodo = 0L;
        public String p_IdEstado = "0";
        public Long p_IdProductoAgrupador = 0L;
    }

    /** Result row (and element of {@code P_OUT_TABLA}). */
    public static class Registro {
        public String RutCliente = "";
        public String Nombre = "";
        public Long IdCliente = 0L;
        public Long IdPortafolio = 0L;
        public String Portafolio = "";
        public String Producto = "";
        public BigDecimal MontoComision = BigDecimal.ZERO;
        public String EstadoComision = "";
        public String NombreEstado = "";
        public String PortafolioAPV = "N";
    }

    public static class Salida {
        public List<Registro> P_OUT_TABLA = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
