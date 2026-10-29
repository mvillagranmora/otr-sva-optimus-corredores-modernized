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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edits commission-charge options (legacy {@code EditarCobroComisionOpcionesCobro}). Behaviours CTA-B-08..12.
 *
 * <p>Same staging approach as the creation: the percentage must be in [0, 1] (CTA-B-12), an id that does not exist is an error
 * (CTA-B-09), the existing rows are updated and a unique-key violation (another option with the same option / APV flag / market)
 * is reported as a WARNING, not as an error (CTA-B-10, README defect 2): the update is not counted as failed.
 */
@Service
public class EditarCobroComisionOpcionesCobroService {

    private static final Logger log = LoggerFactory.getLogger(EditarCobroComisionOpcionesCobroService.class);

    private static final String SQL_UPDATE = SqlReportes.recurso("OpcionesCobroComisiones_UpdateRegistro");
    private static final String SQL_CREAR_PASO = SqlReportes.recurso("OpcionesCobroComisiones_CrearTablaPaso");
    private static final String SQL_INSERT_PASO = SqlReportes.recurso("OpcionesCobroComisiones_InsertTablaPaso");
    private static final String SQL_SELECT_PASO = SqlReportes.recurso("OpcionesCobroComisiones_SelectOpcionesCobroTablaPaso");
    private static final String SQL_DROP_PASO = SqlReportes.recurso("OpcionesCobroComisiones_DropTablaPaso");

    private final SqlReportes sql;

    public EditarCobroComisionOpcionesCobroService(DataSource dataSource) {
        this.sql = new SqlReportes(new NamedParameterJdbcTemplate(dataSource));
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(editar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida editar(Entrada in) throws Exception {
        log.info("INICIO::EditarCobroComisionOpcionesCobro");
        String fechaSistema = new ConfiguracionSistema(sql).fechaSistema();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        Salida salida = new Salida();

        if (in.p_Opciones.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_ENTRADA_SIN_CONCEPTOS"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        for (OpcionCobro opcion : in.p_Opciones) {
            opcion.FechaIngreso = fechaSistema;
            if (opcion.PorcentajeCobro.compareTo(BigDecimal.ZERO) < 0 || opcion.PorcentajeCobro.compareTo(BigDecimal.ONE) > 0) {
                Map<String, String> valores = new HashMap<>();
                valores.put("Opcion", opcion.OpcionCobro.toString());
                valores.put("PortafolioAPV", opcion.PortafolioAPV);
                valores.put("IdClasificacionTipoInstrumento", opcion.Mercado);
                salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_PORCENTAJE_NO_VALIDO", valores));
            }
        }
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            return salida;
        }

        try {
            sql.ejecutar(SQL_CREAR_PASO);
            sql.lote(SQL_INSERT_PASO, in.p_Opciones);
            List<OpcionCobro> preparadas = sql.consultar(SQL_SELECT_PASO, Map.of(), OpcionCobro.class);

            for (OpcionCobro inexistente : preparadas) {
                if (inexistente.IdOpcionCobro == 0L) {
                    salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_OPCION_NO_ENCONTRADA",
                            Map.of("IdOpcion", inexistente.IdOpcionCobro.toString())));
                }
            }
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            }

            List<OpcionCobro> existentes = preparadas.stream().filter(o -> o.IdOpcionCobro > 0L).toList();
            if (!existentes.isEmpty()) {
                try {
                    sql.lote(SQL_UPDATE, existentes);
                } catch (Exception e) {
                    // The whole update is reported as a duplicated option (unique key violated), whatever the real cause was (CTA-B-10).
                    salida.P_OUT_MENSAJES.add(mensajes.advertencia("UI_ADVERTENCIA_OPCION_DUPLICADA", Map.of()));
                    salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
                    salida.P_OUT_ADVERTENCIASCONTADOR++;
                }
            }
        } finally {
            sql.ejecutar(SQL_DROP_PASO);
        }

        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        log.info("FIN::EditarCobroComisionOpcionesCobro");
        return salida;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "CTA";
        public String p_UsaReglasNegocio = "N";
        public List<OpcionCobro> p_Opciones = new ArrayList<>();
    }

    public static class OpcionCobro {
        public Long IdOpcionCobro = 0L;
        public Long OpcionCobro = 0L;
        public String PortafolioAPV = "N";
        public String Mercado = "";
        public BigDecimal PorcentajeCobro = BigDecimal.ZERO;
        public String Estado = "";
        public Date FechaVigencia = java.sql.Date.valueOf("0001-01-01");
        public String FechaModificacion = "0001-01-01";
        public String FechaIngreso = "0001-01-01";
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
