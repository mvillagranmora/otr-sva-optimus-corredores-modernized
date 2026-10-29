package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ConfiguracionFechaFFMM;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.DiaFeriado;
import cl.bcs.fondomutuo.fechas.FechasPort;

/**
 * {@link FechasPort} over the two legacy SQL of {@code CalcularFechaSolicitudAporteRescate} (verbatim; the three {@code CallCalcularFecha...} /
 * temporary-table files of the same prefix are never loaded, as in the legacy). SELECT only. The holiday rows are mapped by column label as the legacy
 * did ({@code java.sql.Date} + {@code Long}); the {@code Long} object of the driver is passed on untouched (the legacy compares ids with {@code ==}, D-FEC-3).
 */
@Component
public class FechasJdbc implements FechasPort {

    /** Row of {@code SelectDiaEsFeriado.sql} (legacy {@code DiaFeriado}). */
    public static class FilaDiaFeriado {
        public java.sql.Date Fecha;
        public Long IdPais;
    }

    private static final String SQL_SELECT_CONFIGURACION_FECHAS_FFMM = SqlRecursos.modulo("CalcularFechaSolicitudAporteRescate_SelectConfiguracionFechasFFMM.sql");
    private static final String SQL_SELECT_DIAESFERIADO = SqlRecursos.modulo("CalcularFechaSolicitudAporteRescate_SelectDiaEsFeriado.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public FechasJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConfiguracionFechaFFMM> configuracionFechas(Long idEmpresa) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_IdEmpresa", idEmpresa);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_CONFIGURACION_FECHAS_FFMM, params, ConfiguracionFechaFFMM.class));
    }

    @Override
    public List<DiaFeriado> feriados(List<LocalDate> fechas, List<Long> idPaises) {
        Map<String, Object> params = new HashMap<>(2);
        List<Object> fechasSql = new ArrayList<>();
        for (LocalDate f : fechas) {
            fechasSql.add(java.sql.Date.valueOf(f));
        }
        params.put("Fechas", fechasSql);
        params.put("IdPais", idPaises);
        List<FilaDiaFeriado> filas = conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_DIAESFERIADO, params, FilaDiaFeriado.class));
        List<DiaFeriado> salida = new ArrayList<>();
        for (FilaDiaFeriado fila : filas) {
            DiaFeriado d = new DiaFeriado();
            d.Fecha = fila.Fecha == null ? null : fila.Fecha.toLocalDate();
            d.IdPais = fila.IdPais;
            salida.add(d);
        }
        return salida;
    }
}
