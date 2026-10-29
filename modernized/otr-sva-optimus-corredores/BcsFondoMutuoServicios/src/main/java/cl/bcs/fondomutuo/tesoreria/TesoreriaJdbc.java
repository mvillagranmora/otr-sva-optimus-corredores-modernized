package cl.bcs.fondomutuo.tesoreria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * {@link TesoreriaDatos} over the legacy SQL, verbatim: the file {@code ComunesSolicitudes_SelectModalidadPago.sql} (byte-identical to the legacy,
 * {@code RecursosSqlTest}) and the three in-line SQL of {@code ProcesarIngresoEgresoTesoreria.java:40,42,45} copied character for character (their SHA-256
 * is pinned by {@code ProcesarIngresoEgresoTesoreriaTest}). Read only; no cache; the connection of the unit of work.
 */
public class TesoreriaJdbc implements TesoreriaDatos {

    static final String SQL_SELECT_MODALIDAD_PAGO = SqlRecursos.modulo("ComunesSolicitudes_SelectModalidadPago.sql");
    static final String SQL_SELECT_EMPLEADO = "SELECT COALESCE(\"IdEntidad\",0) AS \"IdEmpleado\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"TipoEntidad\" = 'EMP' AND \"IdUsuario\" = :IdUsuario";
    static final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = "SELECT \"IdPreferenciaPagoPortafolio\", \"IdCuentaBancaria\", \"IdBancoCuentaBancaria\", \"NumeroCuentaBancaria\", \"IdTipoCuentaBancaria\", \"NombreTipoCuentaBancaria\" FROM \"BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio\" WHERE \"IdPreferenciaPagoPortafolio\" IN (:IdPreferenciaPagoPortafolio)";
    static final String SQL_SELECT_PREFERENCIA_PAGO_CORREDOR = "SELECT \"IdTipoMovimiento\", \"IdConcepto\", \"IdMedioDePago\", \"IdCuentaBanco\", \"IdMoneda\" FROM \"BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor\" AS PRE WHERE \"IdConcepto\" = 'RESCFM' AND \"NombreEntidad\" = 'FFM.RescatePorTraspaso' AND \"IdEstado\" = 'ACT' AND \"IdEmpresa\" = :IdEmpresa";

    private final ConexionUnidadDeTrabajo conexion;

    public TesoreriaJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    private <T> List<T> consultar(String sql, Map<String, Object> params, Class<T> tipo) {
        return conexion.usar(con -> SqlNombrado.consultar(con, sql, params, tipo));
    }

    @Override
    public List<Empleado> empleados(Long idUsuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdUsuario", idUsuario);
        return consultar(SQL_SELECT_EMPLEADO, params, Empleado.class);
    }

    @Override
    public List<ModalidadPago> modalidadesPago() {
        return consultar(SQL_SELECT_MODALIDAD_PAGO, new HashMap<>(), ModalidadPago.class);
    }

    @Override
    public List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio(List<Long> idsPreferencia) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPreferenciaPagoPortafolio", idsPreferencia);
        return consultar(SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, params, PreferenciaPagoPortafolio.class);
    }

    @Override
    public List<PreferenciaPagoCorredor> preferenciasPagoCorredor(Long idEmpresa) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        return consultar(SQL_SELECT_PREFERENCIA_PAGO_CORREDOR, params, PreferenciaPagoCorredor.class);
    }
}
