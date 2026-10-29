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
 * Legacy {@code cl.bcs.tesoreria.RequisitosGirarComprobanteIngreso}, behaviours TES-A-500..525: checks that the comprobantes of ingreso can be
 * girados together and informs the concept, currency, liquidation date, box and payment media they share. It only reads (the list of comprobantes
 * is staged in a session temporary table that is dropped at the end).
 */
@Service
public class RequisitosGirarComprobanteIngresoService {

    private static final String TABLA_TEMPORAL = "#RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar";
    private static final String SQL_CREAR_TABLA = "CREATE LOCAL TEMPORARY TABLE " + TABLA_TEMPORAL + " (\"IdComprobante\" BIGINT)";
    private static final String SQL_INSERTAR = "INSERT INTO " + TABLA_TEMPORAL + " (\"IdComprobante\") VALUES (:IdComprobante)";
    private static final String SQL_VACIAR = "DELETE FROM " + TABLA_TEMPORAL + " WHERE 1=1";
    private static final String SQL_ELIMINAR_TABLA = "DROP TABLE " + TABLA_TEMPORAL;

    private static final String SQL_COMPROBANTE_TESORERIA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectComprobanteTes.sql");
    private static final String SQL_CUENTA_CORREDORA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectCuentaCorredora.sql");
    private static final String SQL_FOLIOS = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectFoliosComprobantes.sql");
    private static final String SQL_ESTADO = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectComprobanteEstado.sql");
    private static final String SQL_INGRESO = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectComprobanteIngreso.sql");
    private static final String SQL_FECHA_LIQUIDACION = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectComprobanteFechaLiq.sql");
    private static final String SQL_MONEDA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectComprobanteMoneda.sql");
    private static final String SQL_FORMA_DE_PAGO = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectComprobanteFpago.sql");
    private static final String SQL_CAJA_VALIDA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectValidaCaja.sql");
    private static final String SQL_ORIGEN = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectValidaOrigen.sql");
    private static final String SQL_MEDIOS_DE_PAGO = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectMediosPago.sql");
    private static final String SQL_CONCEPTOS = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectConceptos.sql");
    private static final String SQL_MONEDA_FECHA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectMonedaFecha.sql");
    private static final String SQL_CAJA = loadSql("sql/RequisitosGirarComprobanteIngreso_SelectCaja.sql");

    public static class EntradaRequisitoGirarComprobanteIngreso extends RequisitosGirarComun.Entrada {
    }

    public static class SalidaRequisitoGirarComprobanteIngreso extends RequisitosGirarComun.Salida {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;

    public RequisitosGirarComprobanteIngresoService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaRequisitoGirarComprobanteIngreso in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaRequisitoGirarComprobanteIngreso.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaRequisitoGirarComprobanteIngreso logica(EntradaRequisitoGirarComprobanteIngreso entrada) throws Exception {
        SalidaRequisitoGirarComprobanteIngreso salida = new SalidaRequisitoGirarComprobanteIngreso();
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
        validar(entrada, salida, SQL_INGRESO, Map.of());
        validar(entrada, salida, SQL_FECHA_LIQUIDACION, Map.of());
        validar(entrada, salida, SQL_MONEDA, Map.of());
        validar(entrada, salida, SQL_FORMA_DE_PAGO, Map.of());
        validar(entrada, salida, SQL_CAJA_VALIDA, Map.of());
        validar(entrada, salida, SQL_ORIGEN, Map.of());

        salida.p_out_MediosDePagoAGirar = jdbc.query(SQL_MEDIOS_DE_PAGO, Map.of(), Filas.de(RequisitosGirarComun.MediosPago.class));
        RequisitosGirarComun.resumir(jdbc, salida, SQL_CONCEPTOS, SQL_MONEDA_FECHA, SQL_CAJA);

        jdbc.getJdbcOperations().execute(SQL_VACIAR);
        RequisitosGirarComun.ignorandoErrores(jdbc.getJdbcOperations()::execute, SQL_ELIMINAR_TABLA);
        return salida;
    }

    private void validar(EntradaRequisitoGirarComprobanteIngreso entrada, SalidaRequisitoGirarComprobanteIngreso salida, String sql,
            Map<String, ?> parametros) {
        RequisitosGirarComun.validar(jdbc, diccionario, entrada, salida, sql, parametros);
    }
}
