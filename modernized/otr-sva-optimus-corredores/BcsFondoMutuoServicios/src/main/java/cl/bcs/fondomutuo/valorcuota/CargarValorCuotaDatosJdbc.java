package cl.bcs.fondomutuo.valorcuota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** The three SELECTs of {@code CargarValorCuota}, verbatim SQL, on the connection of the unit of work. */
@Component
public class CargarValorCuotaDatosJdbc implements CargarValorCuotaDatos {

    private static final String SQL_SELECT_SERIE_FONDO_ISIN = SqlRecursos.modulo("CargarValorCuota_SelectSerieFondoISIN.sql");
    private static final String SQL_SELECT_VALOR_CUOTA_EXISTENTE_FECHA = SqlRecursos.modulo("CargarValorCuota_SelectValorCuotaExistenteFecha.sql");
    private static final String SQL_SELECT_VALOR_CUOTA_DIA_ANTERIOR = SqlRecursos.modulo("CargarValorCuota_SelectValorCuotaDiaAnteriorFechaCarga.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public CargarValorCuotaDatosJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    /** L-2: largest IN list sent in one statement (the values are DISTINCT, so a row belongs to exactly one chunk; up to this size there is ONE statement, as before). */
    static final int TRAMO_IN = 1000;

    /** The list in chunks of {@link #TRAMO_IN}; an empty list is ONE empty chunk (the SQL then reads {@code IN (NULL)}, as before). */
    static <T> List<List<T>> tramos(List<T> lista) {
        List<List<T>> tramos = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += TRAMO_IN) tramos.add(lista.subList(i, Math.min(lista.size(), i + TRAMO_IN)));
        if (tramos.isEmpty()) tramos.add(lista);
        return tramos;
    }

    @Override
    public List<CargarValorCuotaContrato.SerieFondoIsin> seriesPorIsin(List<String> isin) {
        List<CargarValorCuotaContrato.SerieFondoIsin> todas = new ArrayList<>();
        for (List<String> tramo : tramos(isin)) {
            Map<String, Object> params = new HashMap<>();
            params.put("ISIN", tramo);
            todas.addAll(conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIE_FONDO_ISIN, params, CargarValorCuotaContrato.SerieFondoIsin.class)));
        }
        return todas;
    }

    /** The series are chunked; the dates list (distinct closing dates of one load) is sent whole. */
    @Override
    public List<CargarValorCuotaContrato.ValorCuotaVigente> valoresVigentesEnFechas(List<String> idSerieFondo, List<String> fechasVigencia) {
        List<CargarValorCuotaContrato.ValorCuotaVigente> todas = new ArrayList<>();
        for (List<String> tramo : tramos(idSerieFondo)) {
            Map<String, Object> params = new HashMap<>();
            params.put("IdSerieFondo", tramo);
            params.put("FechaVigencia", fechasVigencia);
            todas.addAll(conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_VALOR_CUOTA_EXISTENTE_FECHA, params, CargarValorCuotaContrato.ValorCuotaVigente.class)));
        }
        return todas;
    }

    @Override
    public List<CargarValorCuotaContrato.ValorCuotaVigente> historicoDiaAnterior(List<String> idSerieFondo, List<String> fechasCierre) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idSerieFondo);
        params.put("FechaCierre", fechasCierre);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_VALOR_CUOTA_DIA_ANTERIOR, params, CargarValorCuotaContrato.ValorCuotaVigente.class));
    }
}
