package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ConsultarTotalesDistribucion;
import cl.bcs.apv.api.dto.consultartotalesdistribucion.ConsultarTotalesDistribucionEntrada;
import cl.bcs.apv.api.dto.consultartotalesdistribucion.ConsultarTotalesDistribucionResultado;
import cl.bcs.apv.api.dto.consultartotalesdistribucion.TotalesDR;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ConsultarTotalesDistribucion} (family C): totals of the distribution by state (grids T and R).
 * Legacy {@code cl.bcs.APV.ConsultarTotalesDistribucionRecaudacion}; its call never fills messages nor the error counter (both stay null).
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class ConsultarTotalesDistribucionServicio implements ConsultarTotalesDistribucion {

    private static final String SQL_CONSULTA_TOTALES = SqlLegacy.recurso("ConsultarTotalesDistribucionRecaudacion");

    private final SqlLegacy sql;

    public ConsultarTotalesDistribucionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
    }

    /** Row of the totals query (legacy {@code Totales_DR}). */
    public static class TotalesDRFila {
        public String NombreEstado;
        public BigDecimal RegistrosRecaudado;
        public BigDecimal DisTribuidoTotal;
        public BigDecimal RecTotal;
        public BigDecimal Diferencia;
        public String Grilla;
    }

    @Override
    public ConsultarTotalesDistribucionResultado ejecutar(ConsultarTotalesDistribucionEntrada in) {
        try {
            HashMap<String, Object> param = new HashMap<>();
            param.put("p_FechaDesde", in.fechaDesde());
            param.put("p_FechaHasta", in.fechaHasta());
            param.put("Grilla", 'T');
            List<TotalesDRFila> totales = sql.consultar(SQL_CONSULTA_TOTALES, param, TotalesDRFila.class);

            param = new HashMap<>();
            param.put("p_FechaDesde", in.fechaDesde());
            param.put("p_FechaHasta", in.fechaHasta());
            param.put("Grilla", 'R');
            List<TotalesDRFila> resumen = sql.consultar(SQL_CONSULTA_TOTALES, param, TotalesDRFila.class);

            // the legacy call does not fill P_OUT_MENSAJES nor P_OUT_ERRORESCONTADOR (null, omitted by the plain Gson)
            return new ConsultarTotalesDistribucionResultado(null, null, aTotales(totales), aTotales(resumen));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private static List<TotalesDR> aTotales(List<TotalesDRFila> filas) {
        List<TotalesDR> r = new ArrayList<>();
        for (TotalesDRFila f : filas) {
            r.add(new TotalesDR(f.NombreEstado, f.RegistrosRecaudado, f.DisTribuidoTotal, f.RecTotal, f.Diferencia, f.Grilla));
        }
        return r;
    }
}
