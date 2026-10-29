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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Removes concepts from the commission-charge exclusion list (legacy {@code EliminarCobroComisionExclusionConcepto}).
 * Behaviours CTA-B-22..25.
 *
 * <p>An empty request is an error; every requested id must exist (one error per unknown id, CTA-B-23/24) and, if any is unknown,
 * nothing is deleted; otherwise all are deleted, one confirmation message is answered and each deletion is audited.
 */
@Service
public class EliminarCobroComisionExclusionConceptoService {

    private static final Logger log = LoggerFactory.getLogger(EliminarCobroComisionExclusionConceptoService.class);

    private static final String SQL_DELETE = SqlReportes.recurso("CobroComisionExclusionConcepto_DeleteCobroComisionExclusionConceptos");
    private static final String SQL_SELECT = SqlReportes.recurso("CobroComisionExclusionConcepto_SelectCobroComisionExclusionConceptos");

    private final SqlReportes sql;
    private final AuditoriaReportes auditoria;

    @Autowired
    public EliminarCobroComisionExclusionConceptoService(DataSource dataSource, AuditoriaReportes auditoria) {
        this.sql = new SqlReportes(new NamedParameterJdbcTemplate(dataSource));
        this.auditoria = auditoria;
    }

    /** Without audit trail (characterization tests do not observe it). */
    public EliminarCobroComisionExclusionConceptoService(DataSource dataSource) {
        this(dataSource, AuditoriaReportes.NINGUNA);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(eliminar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida eliminar(Entrada entrada) throws Exception {
        log.info("INICIO::EliminarCobroComisionExclusionConcepto");
        Mensajeria mensajes = new Mensajeria(sql, entrada.p_IdEmpresa);
        Salida salida = new Salida();

        if (entrada.p_Conceptos.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_ENTRADA_SIN_CONCEPTOS"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        List<Long> ids = new ArrayList<>();
        entrada.p_Conceptos.forEach(c -> ids.add(c.IdExclusionConcepto));
        Map<Long, ConceptoExclusion> existentes = sql.consultar(SQL_SELECT, Map.of("idsExclusionConcepto", ids), ConceptoExclusion.class)
                .stream().collect(Collectors.toMap(c -> c.IdExclusionConcepto, c -> c));

        List<ConceptoExclusion> eliminar = new ArrayList<>();
        for (ConceptoExclusion concepto : entrada.p_Conceptos) {
            ConceptoExclusion actual = existentes.get(concepto.IdExclusionConcepto);
            concepto.IdConcepto = actual != null ? actual.IdConcepto : concepto.IdConcepto;
            concepto.Existe = actual != null ? "S" : "N";
            if ("S".equals(concepto.Existe)) {
                eliminar.add(concepto);
            } else {
                Map<String, String> valores = new HashMap<>();
                valores.put("IdExclusionConcepto", concepto.IdExclusionConcepto.toString());
                salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_EXCLUSION_CONCEPTO_NO_EXISTE", valores));
            }
        }
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        if (salida.P_OUT_ERRORESCONTADOR > 0L) {
            return salida;
        }

        if (!eliminar.isEmpty()) {
            sql.lote(SQL_DELETE, eliminar);
            salida.P_OUT_MENSAJES.add(mensajes.ok("UI_EXCLUSION_CONCEPTO_BORRADA"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            auditar(eliminar, entrada.p_IdEmpresa, entrada.p_IdUsuario);
        }
        log.info("FIN::EliminarCobroComisionExclusionConcepto");
        return salida;
    }

    private void auditar(List<ConceptoExclusion> conceptos, Long idEmpresa, Long idUsuario) throws Exception {
        String fechaSistema = new ConfiguracionSistema(sql).fechaSistema();
        AgregarLogEntrada entrada = new AgregarLogEntrada();
        entrada.p_IdTrabajo = 1L;
        for (ConceptoExclusion concepto : conceptos) {
            entrada.p_Log.add(AgregarLogEntrada.Log.cta("ELIMINACION", "EXCLUSION_CONCEPTO_COBRO_COMISION", concepto.IdExclusionConcepto,
                    entrada.p_IdTrabajo, idEmpresa, idUsuario, fechaSistema,
                    "Eliminacion de Concepto " + concepto.IdConcepto + " en Exclusion de Concepto Cobro Comision Patrimonio"));
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
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
