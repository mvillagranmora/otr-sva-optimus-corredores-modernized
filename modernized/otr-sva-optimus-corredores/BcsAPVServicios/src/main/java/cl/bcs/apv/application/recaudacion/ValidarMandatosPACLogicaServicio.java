package cl.bcs.apv.application.recaudacion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.logica.RegistroMandatoPAC;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogica;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogicaEntrada;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;

/**
 * TYPED entry point {@code logica} of the legacy {@code cl.bcs.APV.ValidarMandatosPAC} (the JavaCall {@code call()} of the same class returns null and is
 * not this): the Tesoreria job {@code BcsJobProcesarDistribucion} hands it the outcome of the PAC distribution and it stores the result per mandate in
 * {@code BSCL.Tesoreria.BD::PAC.LogMandatoPAC}. It runs on the {@link NamedParameterJdbcTemplate} it is given, i.e. on the connection and in the transaction
 * of the caller (no commit, no rollback of its own: an exception leaves the decision to the caller, as the legacy did with the job's connection).
 *
 * <p>Reproduced bug for bug (goldens APV-C-480..509, recorded from the unmodified legacy):
 * <ul>
 *   <li>the SQL file is called {@code ..._INSERT_LogMandatoPAC.sql} but is an UPDATE ... WHERE IdMandatoPAC AND FechaSistema: it only rewrites rows that
 *       ALREADY exist (written by the distribution) for the system date; nothing is ever inserted;</li>
 *   <li>the system date is read first ({@code PLATAFORMA/PLA.FechaSistema}; absent: {@code Exception}; not a date: {@code DateTimeParseException});</li>
 *   <li>requests of A_PROCESAR whose IdMandatoPAC is also in NO_PROCESADAS are not processed (the rejected list wins); the rest become
 *       {@code PRO / PreRendicion S / Glosa "Procesado."}, written FIRST; every request of NO_PROCESADAS becomes {@code PEN / S} with the
 *       {@code EstadoRendicion} as Glosa; {@code Usuario} is the text of the user id; {@code FechaProceso} is the database CURRENT_TIMESTAMP;</li>
 *   <li>the list P_OUT_SOLICITUDES_PROCESADAS is never read; a null list, a null element, a rejected request without IdMandatoPAC or a null user id with
 *       something to write throw NullPointerException; a request to process without IdMandatoPAC is written with a NULL key (matches nothing);</li>
 *   <li>a statement failure is a plain {@code SQLException}; the PRO batch has already run when the PEN batch fails.</li>
 * </ul>
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ValidarMandatosPACLogicaServicio implements ValidarMandatosPACLogica {

    private static final String SQL_UPDATE_LOGMANDATOS = SqlLegacy.recurso("ValidarMandatosPAC_INSERT_LogMandatoPAC");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;

    public ValidarMandatosPACLogicaServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
    }

    /** Parameter row of the update statement (legacy {@code ValidarMandatosPAC.LogValidarMandatosPAC}; FechaProceso and IdentServicio are never bound). */
    public static class LogValidarMandatosPAC {
        public Long IdMandatoPAC;
        public String IdentServicio;
        public Date FechaProceso;
        public Date FechaSistema;
        public String EstadoProcesamiento;
        public String PreRendicion;
        public String Usuario;
        public String Glosa;
    }

    @Override
    public void ejecutar(ValidarMandatosPACLogicaEntrada entrada) {
        try {
            Date fechaSistema = fechaDe(Plataforma.fechaSistema(configuracion, entrada.idEmpresa()));

            Map<Object, List<RegistroMandatoPAC>> hashNoPro = entrada.solicitudesNoProcesadas().stream()
                    .collect(Collectors.groupingBy(tra -> tra.idMandatoPAC()));
            List<RegistroMandatoPAC> solAGuardarOK = entrada.solicitudesAProcesar().stream()
                    .filter(aP -> !hashNoPro.containsKey(aP.idMandatoPAC())).collect(Collectors.toList());
            List<RegistroMandatoPAC> solAGuardarNOK = entrada.solicitudesNoProcesadas().stream().collect(Collectors.toList());

            if (solAGuardarOK.size() > 0) {
                guardar(solAGuardarOK, fechaSistema, entrada.idUsuario(), "PRO", r -> "Procesado.");
            }
            if (solAGuardarNOK.size() > 0) {
                guardar(solAGuardarNOK, fechaSistema, entrada.idUsuario(), "PEN", RegistroMandatoPAC::estadoRendicion);
            }
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    /** Legacy {@code Fecha.toDate}: 19 characters are read as date-time, anything else as a date. */
    private static Date fechaDe(String fecha) {
        return java.sql.Date.valueOf(fecha.length() == 19 ? LocalDate.parse(fecha, FORMATO_FECHA_HORA) : LocalDate.parse(fecha, FORMATO_FECHA));
    }

    /** Legacy {@code InsertarMandatosProcesados} / {@code InsertarMandatosNoProcesados}: one batch row per request, in list order. */
    private void guardar(List<RegistroMandatoPAC> solicitudes, Date fechaSistema, Long idUsuario, String estado, Function<RegistroMandatoPAC, String> glosa)
            throws Exception {
        List<LogValidarMandatosPAC> log = new ArrayList<>();
        for (RegistroMandatoPAC s : solicitudes) {
            LogValidarMandatosPAC np = new LogValidarMandatosPAC();
            np.IdentServicio = s.identServicio();
            np.IdMandatoPAC = s.idMandatoPAC();
            np.FechaSistema = fechaSistema;
            np.Usuario = idUsuario.toString();
            np.EstadoProcesamiento = estado;
            np.PreRendicion = "S";
            np.Glosa = glosa.apply(s);
            log.add(np);
        }
        if (log.size() > 0) {
            LoteConteo.ejecutar(jdbc, SQL_UPDATE_LOGMANDATOS, log);
        }
    }
}
