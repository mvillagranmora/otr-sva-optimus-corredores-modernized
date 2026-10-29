package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazContext;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerators;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.jobs.runtime.JobContext;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Legacy {@code cl.bcs.contabilidad.jobs.CreaInterfazDiaria} (CONT-J-20..39b).
 *
 * <p>Legacy {@code logica} (CreaInterfazDiaria.java:64-238), in order: the run log
 * {@code <base>logs/LOG_ServiceBROAD_<today>.log}; fechaEjecucion = today - 1 day in the clock's zone (:79-90; the
 * FechaEjecucion parameter is NOT read, :82, CONT-J-21 kept); the contador (:93-103); "Estableciendo conexion a SAP"
 * and the SAP connection (:109-113, a failure aborts everything, CONT-J-38); "Iniciando generacion de archivos"
 * (:117); the six generators of BcsContabilidadServicios, each in its own try that logs {@code e.getMessage()}
 * (:118-193), and which write their own "Creando Archivo" / "Fin de creacion de Archivo" lines to the same log
 * (CONT-J-39); then the SFTP publication of the whole base path (:198-226). Any other failure is logged by the outer
 * catch with its message (:229-232) and the run still commits.
 */
public final class CreaInterfazDiaria extends InterfazSigaJob {

    public static final String CLASE = "cl.bcs.contabilidad.jobs.CreaInterfazDiaria";

    /** For a SAP source that connects on each query ({@link ConexionSap#IMPLICITA}). */
    public CreaInterfazDiaria(PlataformaPort plataforma, InterfazContadorSource contador, InterfazSapSource sap,
                              InterfazFileTransfer transferencia, Supplier<SftpSettings> sftpSettings, Clock clock) {
        this(plataforma, contador, sap, ConexionSap.IMPLICITA, transferencia, sftpSettings, clock);
    }

    public CreaInterfazDiaria(PlataformaPort plataforma, InterfazContadorSource contador, InterfazSapSource sap,
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
        LogServiceBroad bitacora = new LogServiceBroad(rutaBase, hoy);
        try {
            LocalDate fechaEjecucion = hoy.minusDays(1);
            long contador = contador(fechaEjecucion);
            conectarSap(bitacora);
            InterfazContext contexto = new InterfazContext(fechaEjecucion, contador,
                    Path.of(rutaBase + SUBDIRECTORIO_FI), Path.of(rutaBase + SUBDIRECTORIO_BPC), bitacora);
            bitacora.info("Iniciando generacion de archivos");
            for (InterfazGenerator generador : InterfazGenerators.enOrdenLegacy(sap, clock)) {
                try {
                    generador.generar(contexto);
                } catch (Exception e) {
                    bitacora.error(e.getMessage());
                }
            }
            publicar(Path.of(rutaBase), bitacora);
        } catch (RuntimeException e) {
            bitacora.error(e.getMessage());
        }
    }
}
