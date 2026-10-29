package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.interfaz.ArchivosInterfaz;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadCuentasGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadMovimientoGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadSaldoGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.CuentasBpcGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazContext;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.MovimientoBpcGenerator;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Legacy {@code cl.bcs.contabilidad.jobs.CreaInterfazDemanda} (CONT-J-40..57).
 *
 * <p>Differences with {@link CreaInterfazDiaria}, from CreaInterfazDemanda.java:64-278:
 * <ul>
 *   <li>everything lives under {@code <base>APPS/GAB1/} (:72-75), log included; its previous contents are deleted
 *       first, the folder itself is kept (:80-84, BorrarDirectorio :266-278);</li>
 *   <li>the date is the FechaEjecucion parameter (:88); a missing or invalid one is one {@code ERR - } line and no
 *       generation (DEV-J-03; the legacy passed the raw text on);</li>
 *   <li>five steps without Descriptivas, with their own names: {@code B1_BROAD_CTAS.txt} /
 *       {@code B1_CTRL_BROAD_CTAS.txt} and {@code *_BPC_DM.TXT} (:120-211), which the control files also carry;</li>
 *   <li>a failing step logs "&lt;label&gt;: " plus the message for an IllegalArgumentException or for "Broad Cuenta",
 *       and plus the first stack frame otherwise; "Movimiento BCP" keeps its typo (CONT-J-46/47);</li>
 *   <li>"Fin de generacion de archivos" (:213) and "Subiendo archivos al directorio destino" (:217) are logged;</li>
 *   <li>the outer catch logs the message of an IllegalArgumentException and the first stack frame of anything else
 *       (:248-257).</li>
 * </ul>
 * As in Diaria, a failure to obtain the SAP connection aborts the generation and the upload (:112-114, CONT-J-56).
 */
public final class CreaInterfazDemanda extends InterfazSigaJob {

    public static final String CLASE = "cl.bcs.contabilidad.jobs.CreaInterfazDemanda";

    static final String SUBDIRECTORIO_GAB1 = "APPS/GAB1/";
    static final ArchivosInterfaz ARCHIVOS_CUENTAS =
            new ArchivosInterfaz("B1_BROAD_CTAS.txt", "B1_CTRL_BROAD_CTAS.txt");
    static final ArchivosInterfaz ARCHIVOS_CUENTAS_BPC =
            new ArchivosInterfaz("B1_CUENTAS_BPC_DM.TXT", "B1_CTRL_CUENTAS_BPC_DM.TXT");
    static final ArchivosInterfaz ARCHIVOS_MOVIMIENTO_BPC =
            new ArchivosInterfaz("B1_BROAD_BPC_DM.TXT", "B1_CTRL_BROAD_BPC_DM.TXT");

    private static final Logger log = LoggerFactory.getLogger(CreaInterfazDemanda.class);

    /**
     * One step: its log label and whether a non-IllegalArgumentException failure is logged with its message (only
     * "Broad Cuenta", :132-136) or with its first stack frame (the others).
     */
    private record Paso(String etiqueta, boolean conMensaje, InterfazGenerator generador) {
    }

    /** For a SAP source that connects on each query ({@link ConexionSap#IMPLICITA}). */
    public CreaInterfazDemanda(PlataformaPort plataforma, InterfazContadorSource contador, InterfazSapSource sap,
                               InterfazFileTransfer transferencia, Supplier<SftpSettings> sftpSettings, Clock clock) {
        this(plataforma, contador, sap, ConexionSap.IMPLICITA, transferencia, sftpSettings, clock);
    }

    public CreaInterfazDemanda(PlataformaPort plataforma, InterfazContadorSource contador, InterfazSapSource sap,
                               ConexionSap conexionSap, InterfazFileTransfer transferencia,
                               Supplier<SftpSettings> sftpSettings, Clock clock) {
        super(plataforma, contador, sap, conexionSap, transferencia, sftpSettings, clock);
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    void generar(JobContext ctx, String rutaBase, LocalDate hoy) {
        String gab1 = rutaBase + SUBDIRECTORIO_GAB1;
        LogServiceBroad bitacora = new LogServiceBroad(gab1, hoy);
        try {
            vaciar(Path.of(gab1));
            LocalDate fechaEjecucion = fechaEjecucion(ctx.getParam("FechaEjecucion"));
            long contador = contador(fechaEjecucion);
            conectarSap(bitacora);
            InterfazContext contexto = new InterfazContext(fechaEjecucion, contador,
                    Path.of(gab1 + SUBDIRECTORIO_FI), Path.of(gab1 + SUBDIRECTORIO_BPC), bitacora);
            bitacora.info("Iniciando generacion de archivos");
            for (Paso paso : pasos()) {
                ejecutar(paso, contexto, bitacora);
            }
            bitacora.info("Fin de generacion de archivos");
            bitacora.info("Subiendo archivos al directorio destino");
            publicar(Path.of(gab1), bitacora);
        } catch (IllegalArgumentException e) {
            bitacora.error(e.getMessage());
        } catch (RuntimeException e) {
            bitacora.error(primerMarco(e));
        }
    }

    private List<Paso> pasos() {
        return List.of(
                new Paso("Broad Cuenta", true, new BroadCuentasGenerator(sap, clock, ARCHIVOS_CUENTAS)),
                new Paso("Broad Movimiento Contable", false, new BroadMovimientoGenerator(sap, clock)),
                new Paso("Broad Saldo Contable", false, new BroadSaldoGenerator(sap, clock)),
                new Paso("Cuenta BPC", false, new CuentasBpcGenerator(sap, clock, ARCHIVOS_CUENTAS_BPC)),
                new Paso("Movimiento BCP", false, new MovimientoBpcGenerator(sap, clock, ARCHIVOS_MOVIMIENTO_BPC)));
    }

    private static void ejecutar(Paso paso, InterfazContext contexto, LogServiceBroad bitacora) {
        try {
            paso.generador().generar(contexto);
        } catch (IllegalArgumentException e) {
            bitacora.error(paso.etiqueta() + ": " + e.getMessage());
        } catch (Exception e) {
            bitacora.error(paso.etiqueta() + ": " + (paso.conMensaje() ? e.getMessage() : primerMarco(e)));
        }
    }

    /** DEV-J-03: the legacy passed the raw text to the SQL and the generators; here it must be a yyyy-MM-dd date. */
    private static LocalDate fechaEjecucion(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("FechaEjecucion is required (yyyy-MM-dd)");
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("FechaEjecucion '" + valor + "' is not a valid yyyy-MM-dd date", e);
        }
    }

    /** Legacy {@code e.getStackTrace()[0].toString()}; an exception without stack trace prints itself. */
    private static String primerMarco(Throwable e) {
        StackTraceElement[] marcos = e.getStackTrace();
        return marcos.length > 0 ? marcos[0].toString() : e.toString();
    }

    /**
     * Legacy BorrarDirectorio: deletes everything below {@code directorio}, depth first, and keeps the folder itself.
     * A file that cannot be deleted is skipped, as {@code File.delete()} returning false was.
     */
    private static void vaciar(Path directorio) {
        if (!Files.isDirectory(directorio)) {
            return;
        }
        try (Stream<Path> arbol = Files.walk(directorio)) {
            arbol.sorted(Comparator.reverseOrder()).filter(p -> !p.equals(directorio)).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("Cannot delete {}: {}", p, e.toString());
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list " + directorio + ": " + e.getMessage(), e);
        }
    }
}
