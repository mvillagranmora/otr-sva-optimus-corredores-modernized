package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.model.sap.HomologaRespuestaSap;
import cl.bcs.contabilidad.domain.model.sap.ResultadoInterfazContable;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.SapGateway;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.service.GenerarJsonAsientoSapService;
import cl.bcs.contabilidad.service.InterfazContableService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Legacy {@code cl.bcs.contabilidad.jobs.InterfazContable}: the BcsJob shell around
 * {@link InterfazContableService#procesar} (CONT-J-60..79c).
 *
 * <p>Legacy: InterfazContable.java:46-186.
 * <ol>
 *   <li>IdEmpresa and IdUsuario through {@code Long.parseLong}; Destino must be present (:48-51).</li>
 *   <li>The run log {@code ITZ/<yyyyMMddHHmmss>_InterfazContable.log} under {@code PLA.RutaBaseArchivos} of the
 *       IdEmpresa param (:84-85; the procedure reads {@code PLA.Configuracion}, module PLATAFORMA). A lookup failure or
 *       a missing value is an error (the legacy procedure signalled 10007, CONT-J-70/71).</li>
 *   <li>SecCmbte = base64 of the ids joined by ';' (:89-90).</li>
 *   <li>The service posts each pre-voucher; each accepted one is committed at once (:243-244, CONT-J-73). Its lines
 *       are appended to the log as they are produced, each followed by "\n" (:108, :123, :160, :164, :169), so a
 *       failure later in the run leaves the lines written so far (CONT-J-79..79c).</li>
 *   <li>RutaSalida = the RELATIVE file name (:178), then true, whatever SAP answered (:185, CONT-J-75, legacy defect
 *       kept: the ids marked 'E' are not reported as a failure).</li>
 * </ol>
 * Any exception propagates (:179-181): the runner rolls back what was not committed and reports the error. The debug
 * switch, the SAP credentials file read from the working directory and the {@code main()} of the legacy class are
 * not ported; the SAP connection settings come from BcsContabilidadServicios ({@code SapB1Gateway}).
 */
public final class InterfazContable implements ContabilidadJob {

    public static final String CLASE = "cl.bcs.contabilidad.jobs.InterfazContable";

    static final String MODULO = "PLATAFORMA";
    static final String PARAMETRO_RUTA_BASE = "PLA.RutaBaseArchivos";
    private static final DateTimeFormatter MARCA = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PlataformaPort plataforma;
    private final Function<InterfazContableRepository, InterfazContableService> servicio;
    private final InterfazContableRepository repositorio;
    private final Clock clock;

    /**
     * @param servicio builds the service of one run over the repository it receives: the job's repository,
     *                 decorated with the per-voucher commit
     */
    public InterfazContable(PlataformaPort plataforma,
                            Function<InterfazContableRepository, InterfazContableService> servicio,
                            InterfazContableRepository repositorio, Clock clock) {
        this.plataforma = Objects.requireNonNull(plataforma, "plataforma");
        this.servicio = Objects.requireNonNull(servicio, "servicio");
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * The production factory: a service that also streams its lines to the run log of the job that created the
     * repository it receives (decision 2026-10-15, point iii). Any other repository gets a service without a run log.
     */
    public static Function<InterfazContableRepository, InterfazContableService> servicioConBitacora(
            GenerarJsonAsientoSapService generador, SapGateway sap, DiccionarioPort diccionario) {
        Objects.requireNonNull(generador, "generador");
        Objects.requireNonNull(sap, "sap");
        Objects.requireNonNull(diccionario, "diccionario");
        return repo -> repo instanceof RepositorioDeCorrida corrida
                ? new InterfazContableService(generador, sap, corrida, diccionario, corrida.bitacora()::escribir)
                : new InterfazContableService(generador, sap, repo, diccionario);
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws IOException {
        long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String secCmbte = ctx.getParam("SecCmbte");
        String destino = Objects.requireNonNull(ctx.getParam("Destino"), "Destino");

        String archivo = "ITZ/" + LocalDateTime.now(clock).format(MARCA) + "_InterfazContable.log";
        BitacoraInterfazContable bitacora = new BitacoraInterfazContable(Path.of(rutaBase(idEmpresa) + archivo));
        List<Long> secuencias = InterfazContableService.decodificarSecuencias(secCmbte);

        RepositorioDeCorrida corrida = new RepositorioDeCorrida(repositorio, ctx, bitacora);
        ResultadoInterfazContable resultado =
                servicio.apply(corrida).procesar(idEmpresa, idUsuario, destino, secuencias);
        bitacora.completar(resultado.lineasLog());

        ctx.updateRutaSalida(archivo, ctx.idPrograma());
        return true;
    }

    private String rutaBase(long idEmpresa) {
        String ruta = plataforma.obtenerParametroConfiguracion(idEmpresa, MODULO, PARAMETRO_RUTA_BASE, null);
        if (ruta == null) {
            throw new IllegalStateException(PARAMETRO_RUTA_BASE + " is not configured for empresa " + idEmpresa);
        }
        return ruta;
    }

    /** The job's repository for one run: commits the job transaction after each accepted pre-voucher (:244). */
    private record RepositorioDeCorrida(InterfazContableRepository repositorio, JobContext ctx,
                                        BitacoraInterfazContable bitacora) implements InterfazContableRepository {

        @Override
        public List<HomologaRespuestaSap> consultarHomologaRespuestaSap() {
            return repositorio.consultarHomologaRespuestaSap();
        }

        @Override
        public void actualizarEstadoContabilizado(long idEmpresa, long idUsuario, long idPreComprobante) {
            repositorio.actualizarEstadoContabilizado(idEmpresa, idUsuario, idPreComprobante);
            ctx.commit();
        }

        @Override
        public void marcarConError(List<Long> idsPreComprobante) {
            repositorio.marcarConError(idsPreComprobante);
        }
    }

    /**
     * The run log file: created on its first line (the legacy {@code FileUtils.writeStringToFile(file, ..., true)}
     * created it), UTF-8, appended to, each line followed by "\n". It is not transactional: what was written stays
     * when the run fails (CONT-J-78, CONT-J-79).
     *
     * <p>It never throws while the service runs (the service's listener contract): the first write failure stops
     * further writes and is thrown by {@link #completar}, so the run ends in ERROR, as the legacy IOException did.
     */
    private static final class BitacoraInterfazContable {

        private final Path archivo;
        private int recibidas;
        private IOException fallo;

        BitacoraInterfazContable(Path archivo) {
            this.archivo = archivo;
        }

        /** One line, as the service produces it. */
        void escribir(String linea) {
            recibidas++;
            if (fallo != null) {
                return;
            }
            try {
                Files.createDirectories(archivo.toAbsolutePath().getParent());
                Files.writeString(archivo, linea + "\n", StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                fallo = e;
            }
        }

        /** Writes the lines of the finished run that were not streamed (a service without a run log streams none). */
        void completar(List<String> lineas) throws IOException {
            lineas.subList(Math.min(recibidas, lineas.size()), lineas.size()).forEach(this::escribir);
            if (fallo != null) {
                throw fallo;
            }
        }
    }
}
