package cl.bcs.cuentainversion.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cl.bcs.cuentainversion.jobs.job.archivo.Archivo;
import cl.bcs.cuentainversion.jobs.job.archivo.FormatoArchivo;
import cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo.ResultadoProcedimiento;
import cl.bcs.cuentainversion.jobs.runtime.JobResultado;
import cl.bcs.cuentainversion.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.cuentainversion.jobs.support.H2Jobs;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

/**
 * Architecture review of step 5 on the generic file generator: M-3 (the Servicio is validated before it is interpolated into CALL),
 * M-2 (the file name / path stay under the configured folder) and H-2 partial (descriptor released, ERROR log with the program id;
 * the swallow of CIJ-DEF-09 and the partial file are unchanged). The legacy-recorded behaviour is pinned by the goldens (GA-nn).
 */
class GenerarArchivoRevisionPaso5Test {

    static final Clock RELOJ = Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC);
    static final String NOMBRE = "ARCHIVO_X_20260102_030405.TXT";

    @TempDir
    Path base;

    final List<String> llamadas = new ArrayList<>();

    ProcedimientoArchivo procedimiento(List<List<String>> filas) {
        return (llamada, parametros) -> {
            llamadas.add(llamada);
            return List.of(new ResultadoProcedimiento(List.of("TipoRegistroOptimus", "Cuenta"), filas));
        };
    }

    H2Jobs formato(String servicio, String nombreSalida, String carpeta) {
        H2Jobs db = H2Jobs.nuevaLegacy();
        db.jdbc().execute("CREATE SCHEMA BSCL");
        db.jdbc().execute("CREATE TABLE BSCL.\"BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo\" (\"IdEmpresa\" BIGINT, \"IdEstado\" VARCHAR(3), "
                + "\"IdFormatoArchivo\" BIGINT, \"CodigoFormatoArchivo\" VARCHAR(100), \"FormatoSalida\" VARCHAR(10), \"Servicio\" VARCHAR(200), "
                + "\"SeparadorCampos\" VARCHAR(5), \"Descripcion\" VARCHAR(200), \"RutaModulo\" VARCHAR(200), \"RutaCompleta\" VARCHAR(500), "
                + "\"NombreArchivoSalida\" VARCHAR(100), \"CantidadDeTiposDeRegistro\" INT, \"RellenarCampo\" VARCHAR(5), "
                + "\"SeparadorDecimales\" VARCHAR(5), \"AgregoNombreColumna\" VARCHAR(5))");
        db.jdbc().execute("CREATE TABLE BSCL.\"BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle\" (\"IdFormatoArchivo\" BIGINT, "
                + "\"IdFormatoArchivoDetalle\" BIGINT NOT NULL PRIMARY KEY, \"TipoRegistro\" INT, \"Campo\" VARCHAR(100), \"TipoDato\" VARCHAR(10), "
                + "\"Largo\" INT, \"Decimales\" INT, \"ValorDefecto\" VARCHAR(100), \"Orden\" INT, \"DescripcionFormatoDetalle\" VARCHAR(200), "
                + "\"Formato\" VARCHAR(50))");
        db.jdbc().update("INSERT INTO BSCL.\"BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo\" VALUES (1, 'ACT', 1, 'ARCHIVO_X', 'TXT', ?, '|', "
                + "'d', 'cta/', ?, ?, 1, 'S', '.', 'N')", servicio, carpeta, nombreSalida);
        db.jdbc().update("INSERT INTO BSCL.\"BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle\" VALUES (1, 101, 1, 'Cuenta', 'NUM', 6, 0, '', 1, 'c', NULL)");
        db.programa(900L, null);
        return db;
    }

    JobResultado correr(H2Jobs db, GenerarArchivo job) {
        return db.runner(job).ejecutar(GenerarArchivo.CLASE, 900L,
                Map.of("IdEmpresa", "1", "IdUsuario", "21", "CodigoFormatoArchivo", "ARCHIVO_X"));
    }

    String carpeta() {
        return base.toString().replace('\\', '/') + "/cta/";
    }

    @Test
    @DisplayName("M-3: a Servicio with a double quote is a job error and the procedure is never called")
    void servicioConComillaEsErrorDeJob() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        H2Jobs db = formato("SP\"X\"(1); DROP --", "ARCHIVO_X", carpeta());
        JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"))), db.jdbc(), RELOJ));
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Servicio");
        assertThat(llamadas).isEmpty();
        assertThat(db.rutaSalida(900L)).isNull();
    }

    @Test
    @DisplayName("M-3: a repository-style procedure name (letters, digits, '_', '.', ':') is interpolated unchanged")
    void servicioValidoSeInterpola() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        H2Jobs db = formato("BSCL.CuentaInversion.BD.Servicios::SP_Archivo_1", "ARCHIVO_X", carpeta());
        JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"))), db.jdbc(), RELOJ));
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(llamadas).containsExactly("CALL \"BSCL.CuentaInversion.BD.Servicios::SP_Archivo_1\"(?,?,?,?,?)");
        assertThat(base.resolve("cta").resolve(NOMBRE)).exists();
    }

    @Test
    @DisplayName("M-3: the compatibility switch used only by golden GA-29 keeps the legacy interpolation")
    void interruptorDeCompatibilidadSoloParaElGolden() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        H2Jobs db = formato("SP\"X", "ARCHIVO_X", carpeta());
        JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"))), db.jdbc(), RELOJ, false));
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(llamadas).containsExactly("CALL \"SP\"X\"(?,?,?,?,?)");
    }

    @Test
    @DisplayName("M-2: a file name with '..' or a path separator is a job error and nothing is written outside the folder")
    void nombreConSeparadoresEsErrorDeJob() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        for (String malo : List.of("../evil", "a/b", "a\\b", "..")) {
            llamadas.clear();
            H2Jobs db = formato("SP_X", malo, carpeta());
            JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"))), db.jdbc(), RELOJ));
            assertThat(r.desenlace()).as(malo).isEqualTo(Desenlace.ERROR);
            assertThat(r.error()).as(malo).isInstanceOf(IllegalArgumentException.class);
            assertThat(db.rutaSalida(900L)).as(malo).isNull();
        }
        try (Stream<Path> s = Files.walk(base)) {
            assertThat(s.filter(Files::isRegularFile)).isEmpty();
        }
    }

    @Test
    @DisplayName("M-2: spaces and accents in the name (a golden uses them) and a normal folder still work")
    void nombreConEspaciosYAcentos() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        H2Jobs db = formato("SP_X", "Saldos de Cuentas Ñandú", carpeta());
        JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"))), db.jdbc(), RELOJ));
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(base.resolve("cta").resolve("Saldos de Cuentas Ñandú_20260102_030405.TXT")).exists();
    }

    @Test
    @DisplayName("H-2: a failure midway releases the file (it can be deleted), keeps the partial file and the missing RutaSalida (CIJ-DEF-09)")
    void falloAMitadLiberaElArchivoSinCambiarElComportamiento() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        H2Jobs db = formato("SP_X", "ARCHIVO_X", carpeta());
        // the second row has a TipoRegistroOptimus that is not a number: NumberFormatException after the first line was written
        JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"), List.of("x", "6"))), db.jdbc(), RELOJ));
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.rutaSalida(900L)).isNull();
        Path archivo = base.resolve("cta").resolve(NOMBRE);
        assertThat(archivo).exists();
        Files.delete(archivo); // on Windows this fails when the descriptor is still open
    }

    @Test
    @DisplayName("H-2: when crear() fails the ERROR log names the program")
    void crearQueFallaSeLogueaConElPrograma() throws Exception {
        Logger log = (Logger) LoggerFactory.getLogger(Archivo.class);
        ListAppender<ILoggingEvent> eventos = new ListAppender<>();
        eventos.start();
        log.addAppender(eventos);
        try {
            // two missing levels: the one-level mkdir of the legacy cannot create them, FileWriter fails with an IOException (swallowed)
            H2Jobs db = formato("SP_X", "ARCHIVO_X", base.toString().replace('\\', '/') + "/no/hay/");
            JobResultado r = correr(db, new GenerarArchivo(procedimiento(List.of(List.of("1", "5"))), db.jdbc(), RELOJ));
            assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
            assertThat(eventos.list).anySatisfy(e -> {
                assertThat(e.getLevel()).isEqualTo(Level.ERROR);
                assertThat(e.getFormattedMessage()).contains("IdPrograma=900").contains("crear");
            });
        } finally {
            log.detachAppender(eventos);
        }
    }

    @Test
    @DisplayName("H-2: cerrar() closes the descriptor even when the flush fails (the failure stays swallowed)")
    void cerrarCierraAunqueFalleElFlush() throws Exception {
        Files.createDirectories(base.resolve("cta"));
        H2Jobs db = formato("SP_X", "ARCHIVO_X", carpeta());
        Archivo archivo = new Archivo(new FormatoArchivo(db.jdbc(), 1L, "ARCHIVO_X"), RELOJ, 900L);
        AtomicBoolean cerrado = new AtomicBoolean();
        Field campo = Archivo.class.getDeclaredField("fw");
        campo.setAccessible(true);
        campo.set(archivo, new java.io.FileWriter(base.resolve("cta").resolve("x.txt").toFile()) {
            @Override
            public void flush() throws IOException {
                throw new IOException("flush failed (test)");
            }

            @Override
            public void close() throws IOException {
                cerrado.set(true);
                super.close();
            }
        });
        archivo.cerrar();
        assertThat(cerrado).isTrue();
    }
}
