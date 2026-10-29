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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profitability of the benchmark indices of the asset classification tree (legacy {@code IndiceRentabilidadAtributtionAnalysis.procesar},
 * a method called by {@code InformeAtributtionAnalisys}; the legacy {@code call} answered {@code null}). Behaviours CTA-B-175..181.
 *
 * <p>For each classification node: first and last index value of the date range; the profitability is
 * {@code (last / first, rounded to 2 decimals HALF_UP) - 1}, times 100. Nodes without index ({@code ND}) are 0; a missing first
 * or last value counts as 1.
 */
@Service
public class IndiceRentabilidadAttributionAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(IndiceRentabilidadAttributionAnalysisService.class);

    private static final String SQL_INDICES = SqlReportes.recurso("InformeAtributtionAnalysis_Select_Indice");
    private static final String SQL_CREAR_PASO = SqlReportes.recurso("IndiceRentabilidadAtributtionAnalysis_CreateTablaPaso");
    private static final String SQL_INSERT_PASO = SqlReportes.recurso("IndiceRentabilidadAtributtionAnalysis_InsertTablaPaso");
    private static final String SQL_DROP_PASO = SqlReportes.recurso("IndiceRentabilidadAtributtionAnalysis_DropTablaPaso");

    private static final BigDecimal CIEN = BigDecimal.valueOf(100L);

    private final SqlReportes sql;

    public IndiceRentabilidadAttributionAnalysisService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) {
        return JsonReportes.escribir(procesar(JsonReportes.leer(params, Entrada.class)));
    }

    /** Same entry point the report service uses in the legacy (procesar). */
    public Salida procesar(Entrada in) {
        log.info("INICIO::IndiceRentabilidadAttributionAnalysis");
        Salida out = new Salida();
        sql.ejecutar(SQL_CREAR_PASO);
        sql.lote(SQL_INSERT_PASO, in.p_clasificacion);
        Map<String, Object> params = new HashMap<>();
        params.put("FechaDesde", in.p_FechaDesde);
        params.put("FechaHasta", in.p_FechaHasta);
        List<Nodo> nodos = sql.consultar(SQL_INDICES, params, Nodo.class);

        List<Indice> indices = new ArrayList<>();
        for (Nodo nodo : nodos) {
            Indice indice = new Indice();
            indice.Indice = nodo.Indice;
            indice.Descripcion = nodo.Descripcion;
            if (nodo.Indice.equals("ND")) {
                indice.rentabilidadIndice = BigDecimal.ZERO;
            } else {
                BigDecimal precioFinal = nodo.PrecioFinal != null ? nodo.PrecioFinal : BigDecimal.ONE;
                BigDecimal precioInicial = nodo.PrecioInicial != null ? nodo.PrecioInicial : BigDecimal.ONE;
                // The ratio is rounded to 2 decimals before the percentage is taken (1.105 -> 1.11 -> 11).
                BigDecimal rentabilidad = precioFinal.divide(precioInicial, 2, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
                indice.rentabilidadIndice = rentabilidad.multiply(CIEN);
            }
            indices.add(indice);
        }
        out.P_OUT_SALIDA.put("rentabilidadindice", indices);
        sql.ejecutar(SQL_DROP_PASO);
        return out;
    }

    /**
     * Not used by {@link #procesar}: kept only because the pre-existing {@code ServiceFormulasTest} (B-CTA-12) pins it. It rounds the
     * ratio to 4 decimals, the legacy ({@code procesar}, pinned by the CTA-B-175..181 goldens) rounds it to 2: flagged for SME review.
     */
    BigDecimal calcularIndice(BigDecimal precioFinal, BigDecimal precioInicial) {
        return precioFinal.divide(precioInicial, 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(CIEN)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public String p_FechaDesde;
        public String p_FechaHasta;
        public List<Clasificacion> p_clasificacion = new ArrayList<>();
    }

    /** Node of the classification tree to evaluate (also the staging-table row). */
    public static class Clasificacion {
        public Long IdArbolClasificacionActivo;
    }

    /** Row of the index query. */
    public static class Nodo {
        public String Indice;
        public BigDecimal PrecioFinal;
        public BigDecimal PrecioInicial;
        public Long IdClasificacionActivo;
        public Long Padre;
        public String Descripcion;
    }

    /** Element of the answer: index, its profitability in percent and the node description. */
    public static class Indice {
        public String Indice;
        public BigDecimal rentabilidadIndice;
        public String Descripcion;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Map<String, List<Indice>> P_OUT_SALIDA = new HashMap<>();
    }
}
