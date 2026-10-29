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

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Deletes commission-charge options by id (legacy {@code EliminarCobroComisionOpcionesCobro}). Behaviours CTA-B-13..15.
 *
 * <p>An empty request is an error. The DELETE runs once per id and an unknown id is silently ignored (CTA-B-14); nothing is
 * counted as a warning.
 */
@Service
public class EliminarCobroComisionOpcionesCobroService {

    private static final Logger log = LoggerFactory.getLogger(EliminarCobroComisionOpcionesCobroService.class);

    private static final String SQL_DELETE = SqlReportes.recurso("OpcionesCobroComisiones_DeleteRegistro");

    private final SqlReportes sql;
    private final AuditoriaReportes auditoria;

    @Autowired
    public EliminarCobroComisionOpcionesCobroService(DataSource dataSource, AuditoriaReportes auditoria) {
        this.sql = new SqlReportes(new NamedParameterJdbcTemplate(dataSource));
        this.auditoria = auditoria;
    }

    /** Without audit trail (characterization tests do not observe it). */
    public EliminarCobroComisionOpcionesCobroService(DataSource dataSource) {
        this(dataSource, AuditoriaReportes.NINGUNA);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(eliminar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida eliminar(Entrada in) throws Exception {
        log.info("INICIO::EliminarCobroComisionOpcionesCobro");
        Salida salida = new Salida();

        if (in.p_Opciones.isEmpty()) {
            salida.P_OUT_MENSAJES.add(new Mensajeria(sql, in.p_IdEmpresa).error("UI_ERROR_ENTRADA_SIN_CONCEPTOS"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        sql.lote(SQL_DELETE, in.p_Opciones);

        // LEGACY DEFECT kept (see README CTA-B-15, defect 1): the list handed to the audit is a different, empty list, so nothing is audited.
        auditar(new ArrayList<>(), in.p_IdEmpresa, in.p_IdUsuario);
        log.info("FIN::EliminarCobroComisionOpcionesCobro");
        return salida;
    }

    private void auditar(List<OpcionCobro> opciones, Long idEmpresa, Long idUsuario) throws Exception {
        String fechaSistema = new ConfiguracionSistema(sql).fechaSistema();
        AgregarLogEntrada entrada = new AgregarLogEntrada();
        entrada.p_IdTrabajo = 1L;
        for (OpcionCobro opcion : opciones) {
            entrada.p_Log.add(AgregarLogEntrada.Log.cta("ELIMINACION", "ELIMINACION_OPCION_COBRO_COMISION", opcion.IdOpcionCobro,
                    entrada.p_IdTrabajo, idEmpresa, idUsuario, fechaSistema,
                    "Eliminación de opción de cobro " + opcion.IdOpcionCobro + " en menú de opciones de Cobro Comision Patrimonio"));
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

    public static class OpcionCobro {
        public Long IdOpcionCobro = 0L;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
