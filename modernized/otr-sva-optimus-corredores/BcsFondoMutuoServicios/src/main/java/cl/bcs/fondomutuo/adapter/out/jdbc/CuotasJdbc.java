package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.ValorCuotaTipoParidad;
import cl.bcs.fondomutuo.cuotas.CuotasPort;

/** {@link CuotasPort} over {@code CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql} (verbatim legacy SQL, SELECT only, step 3A). */
@Component
public class CuotasJdbc implements CuotasPort {

    private static final String SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO = SqlRecursos.modulo("CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public CuotasJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ValorCuotaTipoParidad> valorCuotaTipoCambio(Long idEmpresa, List<String> idSerieFondo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSerieFondo", idSerieFondo);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ValorCuotaTipoParidad.class));
    }
}
