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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Cancels commission records of a list of portfolios in the commission blotter (legacy {@code EliminarCobroComisionRegistros}).
 * Behaviours CTA-B-40..44.
 *
 * <p>{@code p_IdPortafolios} is a ";"-separated list; the non-numeric items are ignored and an empty result is an error. The
 * edited-commission rows of those portfolios and period are set to INA, the VIGENTE custody and cash patrimony rows of each portfolio
 * in the date range are set to INA, and each portfolio is audited.
 */
@Service
public class EliminarCobroComisionRegistrosService {

    private static final Logger log = LoggerFactory.getLogger(EliminarCobroComisionRegistrosService.class);

    private static final String SQL_CUSTODIA = SqlReportes.recurso("CobroComision_EliminarRegistrosBlotterCus");
    private static final String SQL_CAJA = SqlReportes.recurso("CobroComision_EliminarRegistrosBlotterCaja");
    private static final String SQL_EDITADO_INA = SqlReportes.recurso("CobroComision_EliminarRegistroEditado");
    private static final String SQL_EDITADOS = SqlReportes.recurso("CobroComision_ConsultarEditados");

    /** What the legacy {@code NumberUtils.isNumber} accepted: signed decimals, exponents, hex and a type suffix. */
    private static final Pattern NUMERO = Pattern.compile("[+-]?(0[xX][0-9a-fA-F]+|(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?)[lLfFdD]?");

    private final SqlReportes sql;
    private final AuditoriaReportes auditoria;

    @Autowired
    public EliminarCobroComisionRegistrosService(NamedParameterJdbcTemplate jdbc, AuditoriaReportes auditoria) {
        this.sql = new SqlReportes(jdbc);
        this.auditoria = auditoria;
    }

    /** Without audit trail (characterization tests do not observe it). */
    public EliminarCobroComisionRegistrosService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, AuditoriaReportes.NINGUNA);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(eliminar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida eliminar(Entrada in) throws Exception {
        log.info("INICIO::EliminarCobroComision");
        Salida salida = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);

        // LEGACY DEFECT kept (see README CTA-B-44): a null p_IdPortafolios fails with a NullPointerException.
        List<Long> portafolios = Arrays.stream(in.p_IdPortafolios.split(";")).filter(s -> NUMERO.matcher(s).matches()).map(Long::parseLong).toList();
        if (portafolios.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_ENTRADA_SIN_DATA"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        List<Editado> editados = sql.consultar(SQL_EDITADOS, Map.of("Periodo", in.p_Periodo, "IdPortafolio", portafolios), Editado.class);
        if (!editados.isEmpty()) {
            sql.lote(SQL_EDITADO_INA, editados.stream().map(e -> new ActualizarEditado(in.p_Periodo, e.IdPortafolio)).toList());
        }

        List<ActualizarRegistro> registros = new ArrayList<>();
        for (long portafolio : portafolios) {
            registros.add(new ActualizarRegistro(portafolio, in.p_FechaDesde, in.p_FechaHasta));
            auditar(portafolio, in.p_IdEmpresa, in.p_IdUsuario);
        }
        sql.lote(SQL_CUSTODIA, registros);
        sql.lote(SQL_CAJA, registros);

        salida.P_OUT_MENSAJES.add(mensajes.ok("UI_REGISTROS_ANULADOS_CORRECTAMENTE"));
        return salida;
    }

    private void auditar(Long idPortafolio, Long idEmpresa, Long idUsuario) throws Exception {
        AgregarLogEntrada entrada = new AgregarLogEntrada();
        entrada.p_IdTrabajo = 1L;
        AgregarLogEntrada.Log l = AgregarLogEntrada.Log.cta("ELIMINACION", "ELIMINACION_REGISTRO_COBRO_COMISION", entrada.p_IdTrabajo,
                entrada.p_IdTrabajo, idEmpresa, idUsuario, new ConfiguracionSistema(sql).fechaSistema(),
                "Eliminación del portafolio " + idPortafolio + " en Blotter de Comisiones ");
        l.IdPortafolio = idPortafolio;
        entrada.p_Log.add(l);
        auditoria.agregarLog(entrada);
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdPortafolios;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long p_Periodo;
    }

    public static class Editado {
        public Long IdRegistro;
        public String EstadoComision;
        public Long IdPortafolio;
    }

    /** Parameters of the UPDATE of the edited-commission rows. */
    public record ActualizarEditado(Long Periodo, Long IdPortafolio) {
    }

    /** Parameters of the UPDATE of the custody / cash patrimony rows. */
    public record ActualizarRegistro(Long IdPortafolio, String FechaDesde, String FechaHasta) {
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
