package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.domain.ConceptoExclusion;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excludes account-statement concepts from the commission charge (legacy {@code CrearCobroComisionExclusionConcepto}).
 * Behaviours CTA-B-16..21 and CTA-B-46.
 *
 * <p>Rules: an empty request is an error; the validity date must not be before the system date; each concept is classified as
 * unknown (warning), already excluded (warning) or excludable; the excludable ones get the next sequence value, the requested date
 * as validity start and are inserted, with one audit entry each.
 * {@code p_FechaVigenciaDesde} is a {@link java.sql.Date}: an ISO instant with T/Z is read as {@code null} and the comparison fails with a
 * NullPointerException (CTA-B-46, README defect 3).
 */
@Service
public class CrearCobroComisionExclusionConceptoService {

    private static final Logger log = LoggerFactory.getLogger(CrearCobroComisionExclusionConceptoService.class);

    private static final String SQL_CREATE_PASO = SqlReportes.recurso("CobroComisionExclusionConcepto_CreateTablaPaso");
    private static final String SQL_INSERT_PASO = SqlReportes.recurso("CobroComisionExclusionConcepto_InsertTablaPaso");
    private static final String SQL_VALIDOS = SqlReportes.recurso("CobroComisionExclusionConcepto_SelectConceptosCuentaInversionValidos");
    private static final String SQL_INSERT = SqlReportes.recurso("CobroComisionExclusionConcepto_InsertConceptosCuentaInversion");
    private static final String SQL_DROP_PASO = SqlReportes.recurso("CobroComisionExclusionConcepto_DropTablaPaso");
    private static final String SECUENCIA = "BSCL.CuentaInversion.BD.Secuencias::CobroComisionExclusionConcepto";

    private final SqlReportes sql;
    private final AuditoriaReportes auditoria;

    @Autowired
    public CrearCobroComisionExclusionConceptoService(NamedParameterJdbcTemplate jdbc, AuditoriaReportes auditoria) {
        this.sql = new SqlReportes(jdbc);
        this.auditoria = auditoria;
    }

    /** Without audit trail (characterization tests do not observe it). */
    public CrearCobroComisionExclusionConceptoService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, AuditoriaReportes.NINGUNA);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(crear(JsonReportes.leer(params, Entrada.class)));
    }

    Salida crear(Entrada entrada) throws Exception {
        log.info("INICIO::CrearCobroComisionExclusionConcepto");
        Mensajeria mensajes = new Mensajeria(sql, entrada.p_IdEmpresa);
        Salida salida = new Salida();

        if (entrada.p_Conceptos.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_ENTRADA_SIN_CONCEPTOS"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        String fechaSistema = config.fechaSistema();
        // LEGACY DEFECT kept (see README CTA-B-46): a null date (unreadable ISO instant) fails here with a NullPointerException.
        if (entrada.p_FechaVigenciaDesde.before(Date.valueOf(fechaSistema))) {
            salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_FECHA_VIGENCIA_DESDE_MENOR_FECHA_SISTEMA"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        List<ConceptoExclusion> excluibles = new ArrayList<>();
        for (ConceptoExclusion concepto : validarExistencia(entrada.p_Conceptos)) {
            Map<String, String> valores = Map.of("IdConcepto", concepto.IdConcepto);
            if ("N".equals(concepto.Existe)) {
                salida.P_OUT_MENSAJES.add(mensajes.advertencia("UI_ADVERTENCIA_CONCEPTO_NO_EXISTE", valores));
                salida.P_OUT_ADVERTENCIASCONTADOR++;
            } else if ("N".equals(concepto.Excluido)) {
                excluibles.add(concepto);
            } else {
                salida.P_OUT_MENSAJES.add(mensajes.advertencia("UI_ADVERTENCIA_CONCEPTO_YA_EXCLUIDO", valores));
                salida.P_OUT_ADVERTENCIASCONTADOR++;
            }
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        }

        if (!excluibles.isEmpty()) {
            asignarIdentificadores(excluibles, entrada.p_FechaVigenciaDesde.toString());
            sql.lote(SQL_INSERT, excluibles);
            for (ConceptoExclusion concepto : excluibles) {
                salida.P_OUT_MENSAJES.add(mensajes.ok("UI_CONCEPTO_EXCLUIDO", Map.of("IdConcepto", concepto.IdConcepto)));
            }
        }

        auditar(excluibles, entrada.p_IdEmpresa, entrada.p_IdUsuario, config.fechaSistema());
        log.info("FIN::CrearCobroComisionExclusionConcepto");
        return salida;
    }

    /**
     * Classifies each requested concept (known? already excluded?) through a session temporary table. A SQL failure is logged and
     * answers an empty list (nothing excluded, no message), as the legacy did.
     */
    private List<ConceptoExclusion> validarExistencia(List<ConceptoExclusion> pedidos) {
        List<ConceptoExclusion> validados = new ArrayList<>();
        try {
            sql.ejecutar(SQL_CREATE_PASO);
            List<ConceptoPaso> paso = pedidos.stream().map(c -> new ConceptoPaso(c.IdConcepto)).toList();
            sql.lote(SQL_INSERT_PASO, paso);
            validados = sql.consultar(SQL_VALIDOS, Map.of(), ConceptoExclusion.class);
        } catch (DataAccessException e) {
            log.info("CrearCobroComisionExclusionConcepto.validarExistencia: error validating concepts", e);
        } finally {
            try {
                sql.ejecutar(SQL_DROP_PASO);
            } catch (DataAccessException e) {
                log.info("CrearCobroComisionExclusionConcepto.validarExistencia: error dropping the temporary table", e);
            }
        }
        return validados;
    }

    private void asignarIdentificadores(List<ConceptoExclusion> conceptos, String fechaVigencia) {
        List<Long> ids = sql.secuencia(SECUENCIA, (long) conceptos.size());
        for (int i = 0; i < conceptos.size(); i++) {
            conceptos.get(i).IdExclusionConcepto = ids.get(i);
            conceptos.get(i).FechaVigenciaDesde = Date.valueOf(fechaVigencia);
        }
    }

    private void auditar(List<ConceptoExclusion> conceptos, Long idEmpresa, Long idUsuario, String fechaSistema) {
        AgregarLogEntrada entrada = new AgregarLogEntrada();
        entrada.p_IdTrabajo = 1L;
        for (ConceptoExclusion concepto : conceptos) {
            entrada.p_Log.add(AgregarLogEntrada.Log.cta("INGRESO", "EXCLUSION_CONCEPTO_COBRO_COMISION", concepto.IdExclusionConcepto,
                    entrada.p_IdTrabajo, idEmpresa, idUsuario, fechaSistema,
                    "Ingreso de Concepto " + concepto.IdConcepto + " a Exclusion de Concepto Cobro Comision Patrimonio"));
        }
        if (!entrada.p_Log.isEmpty()) {
            auditoria.agregarLog(entrada);
        }
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa = 1L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "CTA";
        public String p_UsaReglasNegocio = "N";
        public List<ConceptoExclusion> p_Conceptos = new ArrayList<>();
        public Date p_FechaVigenciaDesde = Date.valueOf("0001-01-01");
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }

    /** Row of the staging table (the legacy always wrote IdTrabajo 0). */
    public static class ConceptoPaso {
        public Long IdTrabajo = 0L;
        public String IdConcepto;

        ConceptoPaso(String idConcepto) {
            this.IdConcepto = idConcepto;
        }
    }
}
