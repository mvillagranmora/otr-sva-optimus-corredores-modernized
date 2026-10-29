package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.infrastructure.interfaz.InterfazBitacora;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Run log of the SIGA interface jobs (CONT-J-24, CONT-J-25, CONT-J-45).
 *
 * <p>Legacy: {@code cl.bcs.contabilidad.Log} (BcsContabilidadServicios Log.java): the file
 * {@code <path>logs/LOG_ServiceBROAD_<yyyyMMdd>.log} (:25, path joined by plain concatenation), lines
 * {@code "TIPO - mensaje\r\n"} appended (:33-34), and every write failure swallowed (:36-39). A null message prints
 * as {@code null}. The job and the {@code Crea*} generators share it, so the generator lines land in the same file.
 * UTF-8: the legacy used the platform default charset, which is UTF-8 on Java 18+.
 */
final class LogServiceBroad implements InterfazBitacora {

    private static final Logger log = LoggerFactory.getLogger(LogServiceBroad.class);
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    private final Path archivo;

    LogServiceBroad(String path, LocalDate hoy) {
        this.archivo = Path.of(path + "logs/LOG_ServiceBROAD_" + hoy.format(YYYYMMDD) + ".log");
    }

    @Override
    public void escribir(String tipo, String mensaje) {
        try {
            Files.createDirectories(archivo.toAbsolutePath().getParent());
            Files.writeString(archivo, tipo + " - " + mensaje + "\r\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException e) {
            log.warn("Cannot write the interface run log {}: {}", archivo, e.toString());
        }
    }
}
