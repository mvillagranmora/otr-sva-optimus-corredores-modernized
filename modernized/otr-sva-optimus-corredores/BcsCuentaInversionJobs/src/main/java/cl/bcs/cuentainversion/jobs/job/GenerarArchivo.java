package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.job.archivo.Archivo;
import cl.bcs.cuentainversion.jobs.job.archivo.FormatoArchivo;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import java.time.Clock;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.generarArchivo.GenerarArchivo} (299 lines): generic file generator driven by CTA.FormatoArchivo (with Archivo, FormatoArchivo, FormatoArchivoDetalle).
 */
public final class GenerarArchivo implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.generarArchivo.GenerarArchivo";

    private final ProcedimientoArchivo procedimientos;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    private static final Pattern SERVICIO_VALIDO = Pattern.compile("^[A-Za-z0-9_.:]+$");

    private final boolean validarServicio;

    public GenerarArchivo(ProcedimientoArchivo procedimientos, JdbcTemplate jdbc, Clock clock) {
        this(procedimientos, jdbc, clock, true);
    }

    /**
     * {@code validarServicio=false} is ONLY for replaying golden GA-29, recorded from the legacy with a Servicio that carries a double
     * quote ({@code SP"X}, which the legacy interpolates unchecked); production wiring always validates.
     */
    public GenerarArchivo(ProcedimientoArchivo procedimientos, JdbcTemplate jdbc, Clock clock, boolean validarServicio) {
        this.validarServicio = validarServicio;
        this.procedimientos = Objects.requireNonNull(procedimientos, "procedimientos");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarArchivo.java:57-265 (transaction -> logica(false)); always answers true (CIJ-DEF-09)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String codigoFormatoArchivo = ctx.getParam("CodigoFormatoArchivo");
        Long idPrograma = ctx.idPrograma();
        FormatoArchivo formatoArchivo = new FormatoArchivo(jdbc, idEmpresa, codigoFormatoArchivo); // :103 never throws
        if (formatoArchivo.getCantidadDeTiposDeRegistro() == 0) { // :111 CIJ-DEF-08: a missing format is a NullPointerException
            log.warn("NO EXISTE FORMATO CONFIGURADO PARA GENERAR EL ARCHIVO");
            return true;
        }
        Archivo archivo = new Archivo(formatoArchivo, clock, idPrograma); // review M-2: unsafe name / path -> job error (outside the swallowing try)
        validarServicio(formatoArchivo.getServicio()); // review M-3: job error too, nothing was called
        try {
            // :130-137 four fixed parameters plus one output table per record type
            StringBuilder paramSp = new StringBuilder("?,?,?,?");
            for (int i = 0; i < formatoArchivo.getCantidadDeTiposDeRegistro(); i++) {
                paramSp.append(",?");
            }
            String callSp = String.format("CALL \"%s\"(%s)", formatoArchivo.getServicio(), paramSp);
            // :142-148 p_IdEmpresa, p_IdUsuario, p_CodigoFormatoArchivo, p_IdPrograma
            List<ProcedimientoArchivo.ResultadoProcedimiento> resultados =
                    procedimientos.ejecutar(callSp, Arrays.<Object>asList(idEmpresa, idUsuario, codigoFormatoArchivo, idPrograma));
            if (resultados.isEmpty()) { // :211-216 no data
                log.warn("NO EXISTE DATA PARA GENERAR EL ARCHIVO");
                return true;
            }
            archivo.crear();
            for (ProcedimientoArchivo.ResultadoProcedimiento resultado : resultados) {
                escribirResultado(formatoArchivo, archivo, resultado);
            }
            archivo.cerrar();
            // :221-230 only the module path: the Job Manager adds the base path when the file is downloaded
            Llamada.actualizar(jdbc, SQL_UPDATE_PROGRAMA_JOBMANAGER, "RutaSalida", formatoArchivo.getRutaModulo() + archivo.getNombreArchivo(),
                    "IdPrograma", ctx.idPrograma());
            ctx.commit();
        } catch (Exception ex) { // :234-240 CIJ-DEF-09: swallowed, the file stays open / partial and the job still commits
            log.error("ERROR AL GENERAR ARCHIVO (IdPrograma={}): {}", idPrograma, ex.toString(), ex);
            archivo.liberar(); // review H-2: the descriptor is released; the partial file and the missing RutaSalida stay as pinned
        }
        return true;
    }

    /** Review M-3: the {@code Servicio} of CTA.FormatoArchivo is interpolated into {@code CALL "%s"}; only procedure-name characters. */
    private void validarServicio(String servicio) {
        if (validarServicio && (servicio == null || !SERVICIO_VALIDO.matcher(servicio).matches())) {
            throw new IllegalArgumentException("Invalid Servicio in the file format (only letters, digits, '_', '.' and ':' are allowed)");
        }
    }

    private static final String SQL_UPDATE_PROGRAMA_JOBMANAGER =
            "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma";

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivo.class);

    /** Legacy :156-204 for one result set (record type). A CSV header REPLACES the first data row (CIJ-DEF-08). */
    private static void escribirResultado(FormatoArchivo formatoArchivo, Archivo archivo,
                                          ProcedimientoArchivo.ResultadoProcedimiento resultado) {
        int numcols = resultado.columnas().size();
        int contSet = 0;
        for (List<String> fila : resultado.filas()) {
            String[] lineaCSV = new String[numcols];
            String lineaTXT = "";
            int tipoRegistroOptimus = 0;
            for (int i = 1; i <= numcols; ++i) {
                String nombreColumna = resultado.columnas().get(i - 1);
                if (contSet == 0 && nombreColumna.compareTo("TipoRegistroOptimus") != 0
                        && formatoArchivo.getAgregoNombreColumna().compareTo("S") == 0
                        && formatoArchivo.getFormatoSalida().compareTo("CSV") == 0) {
                    lineaCSV[i - 2] = nombreColumna;
                } else if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus")) {
                    tipoRegistroOptimus = Integer.parseInt(fila.get(i - 1));
                } else if (formatoArchivo.getFormatoSalida().compareTo("TXT") == 0) {
                    lineaTXT = lineaTXT + formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, fila.get(i - 1))
                            + formatoArchivo.getSeparadorCampos();
                } else if (formatoArchivo.getFormatoSalida().compareTo("CSV") == 0) {
                    lineaCSV[i - 2] = formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, fila.get(i - 1));
                }
            }
            if (formatoArchivo.getFormatoSalida().compareTo("TXT") == 0) {
                archivo.escribir(lineaTXT);
            } else if (formatoArchivo.getFormatoSalida().compareTo("CSV") == 0) {
                archivo.escribir(lineaCSV);
            }
            contSet++;
        }
    }
}
