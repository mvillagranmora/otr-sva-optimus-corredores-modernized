package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.AuditoriaReportes;
import cl.bcs.cuentainversion.reportes.AgregarLogEntrada;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates commission-charge options (legacy {@code CrearCobroComisionOpcionesCobro}). Behaviours CTA-B-01..07.
 *
 * <p>Every option is validated first (percentage in [0, 1], CTA-B-02/06/07; any invalid option blocks the whole request), the
 * options are staged in a session temporary table, the ones that already exist (same id, or same option / APV flag / market) are
 * reported as duplicates (CTA-B-03) and only if there is no duplicate the new ones are inserted with the next sequence value.
 */
@Service
public class CrearCobroComisionOpcionesCobroService {

    private static final Logger log = LoggerFactory.getLogger(CrearCobroComisionOpcionesCobroService.class);

    private static final String SQL_INSERT = SqlReportes.recurso("OpcionesCobroComisiones_InsertRegistro");
    private static final String SQL_CREAR_PASO = SqlReportes.recurso("OpcionesCobroComisiones_CrearTablaPaso");
    private static final String SQL_INSERT_PASO = SqlReportes.recurso("OpcionesCobroComisiones_InsertTablaPaso");
    private static final String SQL_SELECT_PASO = SqlReportes.recurso("OpcionesCobroComisiones_SelectOpcionesCobroTablaPaso");
    private static final String SQL_DROP_PASO = SqlReportes.recurso("OpcionesCobroComisiones_DropTablaPaso");

    private final SqlReportes sql;
    private final AuditoriaReportes auditoria;

    @Autowired
    public CrearCobroComisionOpcionesCobroService(NamedParameterJdbcTemplate jdbc, AuditoriaReportes auditoria) {
        this.sql = new SqlReportes(jdbc);
        this.auditoria = auditoria;
    }

    /** Without audit trail (characterization tests do not observe it). */
    public CrearCobroComisionOpcionesCobroService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, AuditoriaReportes.NINGUNA);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(crear(JsonReportes.leer(params, Entrada.class)));
    }

    Salida crear(Entrada in) throws Exception {
        log.info("INICIO::CrearCobroComisionOpcionesCobro");
        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        String fechaSistema = config.fechaSistema();
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

            for (OpcionCobro existente : preparadas) {
                if (existente.IdOpcionCobro > 0) {
                    salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_OPCION_DUPLICADA", Map.of(
                            "Opcion", existente.OpcionCobro.toString(),
                            "PortafolioAPV", existente.PortafolioAPV,
                            "Mercado", existente.Mercado)));
                }
            }
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            }

            List<OpcionCobro> nuevas = preparadas.stream().filter(o -> o.IdOpcionCobro == 0L).toList();
            if (!nuevas.isEmpty()) {
                sql.lote(SQL_INSERT, nuevas);
                for (OpcionCobro nueva : nuevas) {
                    salida.P_OUT_MENSAJES.add(mensajes.ok("UI_OPCION_COBRO_INGRESADA", Map.of(
                            "Opcion", nueva.OpcionCobro.toString(),
                            "PortafolioAPV", nueva.PortafolioAPV,
                            "Mercado", nueva.Mercado,
                            "PorcentajeCobro", nueva.PorcentajeCobro.toString())));
                }
            }
        } finally {
            sql.ejecutar(SQL_DROP_PASO);
        }

        // LEGACY DEFECT kept (see README CTA-B-01): the audit list is never filled, so the audit service receives nothing.
        auditar(new ArrayList<>(), in.p_IdEmpresa, in.p_IdUsuario, fechaSistema);
        log.info("FIN::CrearCobroComisionOpcionesCobro");
        return salida;
    }

    private void auditar(List<OpcionCobro> opciones, Long idEmpresa, Long idUsuario, String fechaSistema) {
        AgregarLogEntrada entrada = new AgregarLogEntrada();
        entrada.p_IdTrabajo = 1L;
        for (OpcionCobro opcion : opciones) {
            entrada.p_Log.add(AgregarLogEntrada.Log.cta("INGRESO", "INGRESO_OPCION_COBRO_COMISION", opcion.OpcionCobro, entrada.p_IdTrabajo,
                    idEmpresa, idUsuario, fechaSistema,
                    "Ingreso de opción de cobro " + opcion.OpcionCobro + " en menú de opciones de Cobro Comision Patrimonio"));
        }
        if (!entrada.p_Log.isEmpty()) {
            auditoria.agregarLog(entrada);
        }
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "CTA";
        public String p_UsaReglasNegocio = "N";
        public List<OpcionCobro> p_Opciones = new ArrayList<>();
    }

    /** Option row; also the shape of the staging table and of the INSERT parameters (public fields, defaults of the legacy POJO). */
    public static class OpcionCobro {
        public Long IdOpcionCobro = 0L;
        public Long OpcionCobro = 0L;
        public String PortafolioAPV = "N";
        public String Mercado = "";
        public BigDecimal PorcentajeCobro = BigDecimal.ZERO;
        public String Estado = "N";
        public String FechaIngreso = "0001-01-01";
        /** {@code java.util.Date}: accepts the ISO instant with T/Z too (CTA-B-05). */
        public Date FechaVigencia = java.sql.Date.valueOf("0001-01-01");
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
