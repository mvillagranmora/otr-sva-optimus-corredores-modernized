package cl.bcs.fondomutuo.consultas.dolar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** The one read of {@code ConsultarValorDolarFondoMutuo}: {@code ..._SelectValorCuotaTipoCambio.sql}, verbatim, on the connection of the unit of work. */
@Component
public class ConsultarDolarJdbc implements ConsultarDolarDatos {

    private static final String SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO = SqlRecursos.modulo("ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarDolarJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarDolarContrato.ValorCuotaTipoParidad> valoresTipoCambio(List<String> idsSerieFondo, Long idEmpresa) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idsSerieFondo);
        params.put("IdEmpresa", idEmpresa);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ConsultarDolarContrato.ValorCuotaTipoParidad.class));
    }
}
