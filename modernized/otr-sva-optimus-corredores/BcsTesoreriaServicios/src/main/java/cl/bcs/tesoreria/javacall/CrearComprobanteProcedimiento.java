package cl.bcs.tesoreria.javacall;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * The generation of comprobantes FROM other comprobantes, which the Java implementation does not cover yet and the legacy sends to the HANA stored
 * procedure {@code BSCL.Tesoreria.BD.Servicios::CrearComprobante} (used by {@link CrearComprobanteJavaService} and by {@link CrearComprobanteService}
 * with {@code USARCREARCOMPROBANTEJAVA = N}; TES-A-233, TES-A-253..256). The procedure is OUTSIDE the equivalence proof: the Java side pinned here is
 * the staging of the inputs in the global temporary tables, the call through the {@code StoredProcedure} collaborator, the reading of its output
 * tables into messages and results, and the clean-up of the tables.
 */
@Component
public class CrearComprobanteProcedimiento {

    static final String PROCEDIMIENTO = "BSCL.Tesoreria.BD.Servicios::CrearComprobante";

    private static final String SQL_INSERT_INGRESO_EGRESO_GENERAR = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_InsertIngresoEgresoGenerar.sql");
    private static final String SQL_INSERT_COMPROBANTE_GENERAR = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_InsertComprobanteGenerar.sql");
    private static final String SQL_INSERT_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_GBL_InsertFormaDePago.sql");
    private static final String SQL_SELECT_RESULTADO = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectResultadoProceso.sql");
    private static final String SQL_SELECT_INFORMACION = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectInformacionDetalle.sql");
    private static final String SQL_SELECT_ADVERTENCIAS = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectAdvertenciasDetalle.sql");
    private static final String SQL_SELECT_ERRORES = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectErroresDetalle.sql");
    private static final List<String> LIMPIAR = CrearComprobanteSql.sentencias("sql/CrearComprobante_Gbl_LimpiarProcedimiento.sql");

    /** Scalar inputs of the procedure (all the legacy Entrada fields but the three lists). */
    record Escalares(Long idEmpresa, Long idUsuario, String idModulo, String simularOperacion, String usaReglasNegocio, String fechaMovimiento,
            Long idCaja, String desdeMovimientos, String desdeComprobantes, String esIngresoAutomatico) {
    }

    /** One generated result row of the procedure (legacy {@code ResultadoProceso_Salida}). */
    public static class Resultado {
        public String Campo;
        public Long Id;
        public String Servicio;
        public String Resultado;
        public Long NumeroReferencia = 0L;
    }

    record Salida(List<Resultado> resultados, List<MensajeSalida> mensajes) {
    }

    public static class InformacionDetalle {
        public String Codigo;
        public String Descripcion;
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final Colaboradores colaboradores;

    public CrearComprobanteProcedimiento(NamedParameterJdbcTemplate jdbc, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.colaboradores = colaboradores;
    }

    /** The three lists are the ones of the legacy Entrada; a null list is a NullPointerException, as in the legacy. */
    Salida ejecutar(Escalares in, List<?> ingresoEgresoGenerar, List<?> comprobanteGenerar, List<?> formaDePago) throws SQLException {
        if (ingresoEgresoGenerar.size() > 0) {
            CrearComprobanteSql.lote(jdbc, SQL_INSERT_INGRESO_EGRESO_GENERAR, ingresoEgresoGenerar);
        }
        if (comprobanteGenerar.size() > 0) {
            CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_GENERAR, comprobanteGenerar);
        }
        if (formaDePago.size() > 0) {
            CrearComprobanteSql.lote(jdbc, SQL_INSERT_FORMA_DE_PAGO, formaDePago);
        }

        JsonObject llamada = new JsonObject();
        llamada.addProperty("procedimiento", PROCEDIMIENTO);
        llamada.add("entrada", escalares(in));
        colaboradores.invocar("StoredProcedure", llamada);

        List<Resultado> resultados = jdbc.getJdbcOperations().query(SQL_SELECT_RESULTADO, Filas.de(Resultado.class));
        List<MensajeSalida> mensajes = new ArrayList<>();
        for (InformacionDetalle i : jdbc.getJdbcOperations().query(SQL_SELECT_INFORMACION, Filas.de(InformacionDetalle.class))) {
            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, i.Codigo, i.Descripcion));
        }
        for (AdvertenciaDetalle a : jdbc.getJdbcOperations().query(SQL_SELECT_ADVERTENCIAS, Filas.de(AdvertenciaDetalle.class))) {
            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, a.Codigo, a.Descripcion));
        }
        for (ErrorDetalle e : jdbc.getJdbcOperations().query(SQL_SELECT_ERRORES, Filas.de(ErrorDetalle.class))) {
            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, e.Codigo, e.Descripcion));
        }
        CrearComprobanteSql.ejecutar(jdbc, LIMPIAR);
        return new Salida(resultados, mensajes);
    }

    private static JsonObject escalares(Escalares in) {
        JsonObject o = new JsonObject();
        o.addProperty("p_IdEmpresa", in.idEmpresa());
        o.addProperty("p_IdUsuario", in.idUsuario());
        o.addProperty("p_IdModulo", in.idModulo());
        o.addProperty("p_SimularOperacion", in.simularOperacion());
        o.addProperty("p_UsaReglasNegocio", in.usaReglasNegocio());
        o.addProperty("p_FechaMovimiento", in.fechaMovimiento());
        o.addProperty("p_IdCaja", in.idCaja());
        o.addProperty("p_GenerarComprobanteDesdeMovimientos", in.desdeMovimientos());
        o.addProperty("p_GenerarComprobanteDesdeComprobantes", in.desdeComprobantes());
        o.addProperty("p_EsIngresoAutomatico", in.esIngresoAutomatico());
        return o;
    }
}
