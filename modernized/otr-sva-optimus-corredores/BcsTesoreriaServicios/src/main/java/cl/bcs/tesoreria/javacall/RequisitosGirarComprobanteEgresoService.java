package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.RequisitosGirarComprobanteEgreso}, behaviours TES-A-530..551: checks that the comprobantes of egreso can be
 * girados together (including the PAREGR, cuenta de inversion and socio account rules) and informs the concept, currency, liquidation date, box and
 * payment media they share. It only reads (the comprobantes are staged in a session temporary table that is dropped at the end).
 */
@Service
public class RequisitosGirarComprobanteEgresoService {

    private static final String TABLA_TEMPORAL = "#RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar";
    private static final String SQL_CREAR_TABLA = "CREATE LOCAL TEMPORARY TABLE " + TABLA_TEMPORAL + " (\"IdComprobante\" BIGINT)";
    private static final String SQL_INSERTAR = "INSERT INTO " + TABLA_TEMPORAL + " (\"IdComprobante\") VALUES (:IdComprobante)";
    private static final String SQL_VACIAR = "DELETE FROM " + TABLA_TEMPORAL + " WHERE 1=1";
    private static final String SQL_ELIMINAR_TABLA = "DROP TABLE " + TABLA_TEMPORAL;

    private static final String SQL_COMPROBANTE_TESORERIA = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobanteTes.sql");
    /** The legacy Egreso reads the account rule from the Ingreso resource. */
    private static final String SQL_CUENTA_CORREDORA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectCuentaCorredora.sql");
    private static final String SQL_FOLIOS = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectFoliosComprobantes.sql");
    private static final String SQL_ESTADO = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobanteEstado.sql");
    private static final String SQL_EGRESO = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobanteEgreso.sql");
    private static final String SQL_PAREGR = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobantePAREGR.sql");
    private static final String SQL_FECHA_LIQUIDACION = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobanteFechaLiq.sql");
    private static final String SQL_MUEVE_CUENTA_INVERSION = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectMueveCuentaInversion.sql");
    private static final String SQL_FORMA_DE_PAGO_PAREGR = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectFormaPagoPAREGR.sql");
    private static final String SQL_CUENTA_SOCIO = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectValidaCuentaSocio.sql");
    private static final String SQL_MONEDA = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobanteMoneda.sql");
    private static final String SQL_FORMA_DE_PAGO = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectComprobanteFpago.sql");
    private static final String SQL_CAJA_VALIDA = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectValidaCaja.sql");
    private static final String SQL_MEDIOS_DE_PAGO = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectMediosPago.sql");
    private static final String SQL_CONCEPTOS = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectConceptos.sql");
    private static final String SQL_MONEDA_FECHA = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectMonedaFecha.sql");
    private static final String SQL_CAJA = loadSql("sql/RequisitosGirarComprobanteEgreso_SelectCaja.sql");

    public static class EntradaRequisitoGirarComprobanteEgreso extends RequisitosGirarComun.Entrada {
    }

    public static class SalidaRequisitoGirarComprobanteEgreso extends RequisitosGirarComun.Salida {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;

    public RequisitosGirarComprobanteEgresoService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaRequisitoGirarComprobanteEgreso in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaRequisitoGirarComprobanteEgreso.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaRequisitoGirarComprobanteEgreso logica(EntradaRequisitoGirarComprobanteEgreso entrada) throws Exception {
        SalidaRequisitoGirarComprobanteEgreso salida = new SalidaRequisitoGirarComprobanteEgreso();
        jdbc.getJdbcOperations().execute(SQL_CREAR_TABLA);
        RequisitosGirarComun.insertarComprobantes(jdbc, SQL_INSERTAR, entrada.p_ComprobantesAGirar);

        // basic validations
        validar(entrada, salida, SQL_COMPROBANTE_TESORERIA, Map.of());
        Map<String, Object> cuenta = new HashMap<>();
        cuenta.put("p_IdCuentaBancariaCorredora", entrada.p_IdCuentaBancariaCorredora);
        // LEGACY DEFECT kept (see README TES-A-518): p_simularOperacion is ignored, the account check always runs as 'N'
        cuenta.put("p_simularOperacion", "N");
        validar(entrada, salida, SQL_CUENTA_CORREDORA, cuenta);
        validar(entrada, salida, SQL_FOLIOS, Map.of());

        // business validations
        validar(entrada, salida, SQL_ESTADO, Map.of());
        validar(entrada, salida, SQL_EGRESO, Map.of());
        validar(entrada, salida, SQL_PAREGR, Map.of());
        validar(entrada, salida, SQL_FECHA_LIQUIDACION, Map.of());
        validar(entrada, salida, SQL_MUEVE_CUENTA_INVERSION, Map.of());
        RequisitosGirarComun.validarSiAlguno(jdbc, diccionario, entrada, salida, SQL_FORMA_DE_PAGO_PAREGR);
        RequisitosGirarComun.validarSiAlguno(jdbc, diccionario, entrada, salida, SQL_CUENTA_SOCIO);
        validar(entrada, salida, SQL_MONEDA, Map.of());
        validar(entrada, salida, SQL_FORMA_DE_PAGO, Map.of());
        validar(entrada, salida, SQL_CAJA_VALIDA, Map.of());

        salida.p_out_MediosDePagoAGirar = jdbc.query(SQL_MEDIOS_DE_PAGO, Map.of(), Filas.de(RequisitosGirarComun.MediosPago.class));
        RequisitosGirarComun.resumir(jdbc, salida, SQL_CONCEPTOS, SQL_MONEDA_FECHA, SQL_CAJA);

        jdbc.getJdbcOperations().execute(SQL_VACIAR);
        RequisitosGirarComun.ignorandoErrores(jdbc.getJdbcOperations()::execute, SQL_ELIMINAR_TABLA);
        return salida;
    }

    private void validar(EntradaRequisitoGirarComprobanteEgreso entrada, SalidaRequisitoGirarComprobanteEgreso salida, String sql,
            Map<String, ?> parametros) {
        RequisitosGirarComun.validar(jdbc, diccionario, entrada, salida, sql, parametros);
    }
}
