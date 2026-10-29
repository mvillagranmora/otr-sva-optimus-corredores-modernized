package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintenance of the financial indices used by the attribution analysis (legacy {@code ProcesaIndicesAtributtionAnalisys}).
 * Behaviours CTA-B-165..174.
 *
 * <p>{@code p_Tarea}: {@code C} creates the indices, {@code M} modifies them (null fields keep their value), {@code E} deletes
 * them with their values. Creating an index that is a system parameter, or deleting one used in the classification tree, is
 * refused with {@code UI_PROCESO_EJECUTADO_ERROR} whose text is the specific dictionary message; any other failure answers the same
 * code with the generic text of the task. Every failure is an answered message, never an exception, except the null task
 * (CTA-B-174, README defect 16).
 */
@Service
public class ProcesaIndicesAtributtionAnalisysService {

    private static final Logger log = LoggerFactory.getLogger(ProcesaIndicesAtributtionAnalisysService.class);

    private static final String SQL_INSERTAR = SqlReportes.recurso("ProcesaIndicesAtributtionAnalisys_InsertaIndice");
    private static final String SQL_ES_PARAMETRO = SqlReportes.recurso("ProcesaIndicesAtributtionAnalisys_ExisteIndiceSistema");
    private static final String SQL_EN_ARBOL = SqlReportes.recurso("ProcesaIndicesAtributtionAnalisys_ExisteRelacionMercadoSubmercado");
    private static final String SQL_MODIFICAR = SqlReportes.recurso("ProcesaIndicesAtributtionAnalisys_ModificaIndice");
    private static final String SQL_ELIMINAR = SqlReportes.recurso("ProcesaIndicesAtributtionAnalisys_EliminaIndice");
    private static final String SQL_ELIMINAR_VALORES = SqlReportes.recurso("ProcesaIndicesAtributtionAnalisys_EliminaValoresIndice");

    private static final String CREAR = "C";
    private static final String MODIFICAR = "M";
    private static final String ELIMINAR = "E";
    private static final String CODIGO_ERROR = "UI_PROCESO_EJECUTADO_ERROR";

    private final SqlReportes sql;

    public ProcesaIndicesAtributtionAnalisysService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        Salida salida = procesar(JsonReportes.leer(params, Entrada.class));
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        return JsonReportes.escribir(salida);
    }

    Salida procesar(Entrada entrada) throws Exception {
        log.info("INICIO::ProcesaIndicesAtributtionAnalisys");
        Salida salida = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, entrada.p_IdEmpresa);
        try {
            new ConfiguracionSistema(sql).fechaSistema();
            // LEGACY DEFECT kept (see README CTA-B-174): a null p_Tarea fails here and again in the handler below (NullPointerException).
            if (entrada.p_Tarea.equals(CREAR)) {
                try {
                    String id = entrada.p_Registros.get(0).p_IdIndiceFinanciero;
                    if (cuenta(SQL_ES_PARAMETRO, id) > 0) {
                        return rechazar(salida, mensajes.texto("UI_EXISTE_INDICE_SISTEMA", valores("IdIndiceFinanciero", id)));
                    }
                    sql.lote(SQL_INSERTAR, entrada.p_Registros);
                } catch (Exception e) {
                    // e.g. the index already exists (primary key): answered as "could not identify the index".
                    log.info("ProcesaIndices: error creating the index", e);
                    return rechazar(salida, mensajes.texto("UI_PROCESO_EJECUTADO_ERROR_INDENTIFICAR_INDICE", new HashMap<>()));
                }
            } else if (entrada.p_Tarea.equals(MODIFICAR)) {
                sql.lote(SQL_MODIFICAR, entrada.p_Registros);
            } else if (entrada.p_Tarea.equals(ELIMINAR)) {
                String id = entrada.p_Registros.get(0).p_IdIndiceFinanciero;
                if (cuenta(SQL_EN_ARBOL, id) > 0) {
                    return rechazar(salida, mensajes.texto("UI_EXISTE_INDICE_RELACIONADO", valores("IdIndiceFinanciero", id)));
                }
                sql.lote(SQL_ELIMINAR_VALORES, entrada.p_Registros);
                sql.lote(SQL_ELIMINAR, entrada.p_Registros);
            }
        } catch (Exception e) {
            log.info("ProcesaIndices: error processing the indices", e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
            String texto = switch (entrada.p_Tarea) {
                case CREAR -> mensajes.texto("UI_ERROR_INGRESAR_INDICE", new HashMap<>());
                case MODIFICAR -> mensajes.texto("UI_ERROR_MODIFICAR_INDICE", new HashMap<>());
                case ELIMINAR -> mensajes.texto("UI_ERROR_ELIMINAR_INDICE", new HashMap<>());
                default -> null;
            };
            if (texto != null) {
                salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.ERROR, CODIGO_ERROR, texto));
            }
        }
        return salida;
    }

    private long cuenta(String consulta, String idIndice) {
        return sql.consultar(consulta, Map.of("p_IdIndiceFinanciero", idIndice), Conteo.class).get(0).Existe;
    }

    private static Map<String, String> valores(String clave, String valor) {
        Map<String, String> m = new HashMap<>();
        if (valor != null) {
            m.put(clave, valor);
        }
        return m;
    }

    private static Salida rechazar(Salida salida, String texto) {
        salida.P_OUT_ERRORESCONTADOR = 1L;
        salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.ERROR, CODIGO_ERROR, texto));
        return salida;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_Tarea;
        public List<DatosIndice> p_Registros;
    }

    /** Index definition; also the parameter set of the INSERT / UPDATE / DELETE (public fields named like the SQL parameters). */
    public static class DatosIndice {
        public String p_IdIndiceFinanciero;
        public String p_Periodo;
        public String p_Nombre;
        public String p_Descripcion;
        public String p_IdMonedaParametro;
        public String p_IdMonedaValor;
    }

    public static class Conteo {
        public Long Existe;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }
}
