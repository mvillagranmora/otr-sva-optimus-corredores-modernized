package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.javacall.ActivarMandatosArchivoCargaService;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService.EntradaFormatoArchivo;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService.SalidaFormatoArchivo;
import cl.bcs.tesoreria.javacall.dap.ArchivoFtp.Cabecera;
import cl.bcs.tesoreria.javacall.dap.ArchivoFtp.Detalle;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.jobs.job.archivo.ArchivoCargos;
import cl.bcs.tesoreria.jobs.job.archivo.FormatoCargos;
import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo.ResultadoProcedimiento;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.port.out.Secuencias;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.tesoreria.generarArchivoCargos.GenerarArchivoCargos} (242 lines, with its {@code Archivo},
 * {@code FormatoArchivo} and {@code FormatoArchivoDetalle}): the PAC charge file job. Format id 8: the format comes from
 * {@link ConsultarFormatoArchivoService#consultar}, the data from the stored procedure ({@link ProcedimientoArchivo}), each value
 * is formatted by field type ({@link FormatoCargos}), the file {@code <RutaCompleta><NombreArchivoSalida>_<yyyyMMdd_HHmmss>.<ext>}
 * is written ({@link ArchivoCargos}), its lines are stored in {@code FTP.Cabecera} / {@code FTP.Detalle}, when
 * {@code DiaDescuento > 0} {@link ActivarMandatosArchivoCargaService#logica} runs, and {@code BAT.Programa.RutaSalida} gets
 * {@code <RutaTesoreria><file name>}. Rules: RULE-061 (no {@code running} flag), RULE-076 (debug mode not ported).
 *
 * <p>Legacy defects kept bug for bug (README): TES-J-DEF-02 the procedure runs TWICE; 03 a procedure without result sets is a
 * {@link NullPointerException}; 04 formatter quirks; 08 the file is not transactional.
 */
public final class GenerarArchivoCargos implements TesoreriaJob {

    public static final String CLASE = "cl.bcs.tesoreria.generarArchivoCargos.GenerarArchivoCargos";

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoCargos.class);
    private static final String SQL_INSERT_CABECERA = SqlNombrado.recurso("jobs/FTP_InsertTableCabecera");
    private static final String SQL_INSERT_DETALLE = SqlNombrado.recurso("jobs/FTP_InsertTableDetalle");

    private final ConsultarFormatoArchivoService formatos;
    private final ActivarMandatosArchivoCargaService mandatos;
    private final ProcedimientoArchivo procedimientos;
    private final Secuencias secuencias;
    private final SqlNombrado sql;
    private final Clock clock;

    public GenerarArchivoCargos(ConsultarFormatoArchivoService formatos, ActivarMandatosArchivoCargaService mandatos,
                                ProcedimientoArchivo procedimientos, Secuencias secuencias, JdbcTemplate jdbc,
                                Clock clock) {
        this.formatos = Objects.requireNonNull(formatos, "formatos");
        this.mandatos = Objects.requireNonNull(mandatos, "mandatos");
        this.procedimientos = Objects.requireNonNull(procedimientos, "procedimientos");
        this.secuencias = Objects.requireNonNull(secuencias, "secuencias");
        this.sql = new SqlNombrado(new NamedParameterJdbcTemplate(Objects.requireNonNull(jdbc, "jdbc")));
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    /** The prepared call of the legacy {@code CallableStatement}: each {@link #execute()} is one legacy {@code execute()}. */
    private final class Llamada {
        private final String llamada;
        private final List<Object> parametros;

        Llamada(String llamada, List<Object> parametros) {
            this.llamada = llamada;
            this.parametros = parametros;
        }

        List<ResultadoProcedimiento> execute() throws Exception {
            return procedimientos.ejecutar(llamada, parametros);
        }
    }

    /** Legacy :64-79 {@code getService}: executes once; {@code null} when the procedure returned no result set. */
    private Llamada getService(SalidaFormatoArchivo formato, Long discountDay, String fechaCargo) throws Exception {
        String params = "?,?,?,?";
        for (int i = 0; i < formato.CantidadDeTiposDeRegistro; i++) {
            params = params + ",?";
        }
        String query = String.format("CALL BSCL.\"%s\"(%s)", formato.Servicio, params);
        List<Object> parametros = new ArrayList<>();
        parametros.add((long) formato.IdEmpresa);
        parametros.add((long) formato.IdFormatoArchivo);
        parametros.add((long) discountDay);
        parametros.add(fechaCargo);
        Llamada llamada = new Llamada(query, parametros);
        return llamada.execute().isEmpty() ? null : llamada;
    }

    /** Legacy :81-90. */
    private void upgradeJobManager(JobContext ctx, ArchivoCargos file, SalidaFormatoArchivo formato) {
        String rutaTesoreria = formato.RutaTesoreria + file.getFilename();
        ctx.updateRutaSalida(rutaTesoreria, ctx.idPrograma());
    }

    /** Legacy :92-118 {@code uploadToFTP}: the file lines become FTP.Cabecera / FTP.Detalle. */
    private void uploadToFtp(ArchivoCargos file, SalidaFormatoArchivo formato) throws Exception {
        List<Long> secuenciasDetalle = siguientes("BSCL.Plataforma.BD.Secuencias::Detalle", file.getDetails());
        List<Long> secuenciasCabecera = siguientes("BSCL.Plataforma.BD.Secuencias::Cabecera", 1L);
        List<Cabecera> cabeceras = new ArrayList<>();
        Cabecera cabecera = new Cabecera();
        cabecera.NombreArchivo = file.getFilename();
        cabecera.IdCabecera = secuenciasCabecera.get(0);
        cabecera.IdCodigoTipo = formato.FormatoSalida.equals("CSV") ? "CARCSV" : "CARTXT";
        cabecera.IdEstado = "PEN";
        cabecera.FechaCreacion = Timestamp.from(clock.instant());
        cabecera.IdUsuario = formato.IdUsuario;
        cabeceras.add(cabecera);
        List<Detalle> detalles = new ArrayList<>();
        for (int i = 0; i < file.getLines().size(); i++) {
            Detalle detalle = new Detalle();
            detalle.IdDetalle = secuenciasDetalle.get(i);
            detalle.IdCabecera = cabecera.IdCabecera;
            detalle.Linea = file.getLines().get(i);
            detalles.add(detalle);
        }
        sql.lote(SQL_INSERT_CABECERA, cabeceras);
        sql.lote(SQL_INSERT_DETALLE, detalles);
    }

    private List<Long> siguientes(String nombre, Long cantidad) {
        List<Long> valores = new ArrayList<>();
        for (long i = 0; i < cantidad; i++) {
            valores.add(secuencias.siguiente(nombre));
        }
        return valores;
    }

    /** Legacy :120-146 {@code generateFile}. */
    private ArchivoCargos generateFile(List<ResultadoProcedimiento> resultados, SalidaFormatoArchivo formato,
                                       FormatoCargos formateador) throws Exception {
        ArchivoCargos file = new ArchivoCargos(formato, clock);
        Integer typeRecordOptimus = 0;
        file.open();
        try {
            for (ResultadoProcedimiento resultado : resultados) {
                for (List<String> fila : resultado.filas()) {
                    List<String> valuesFormatted = new ArrayList<>();
                    for (int i = 0; i < resultado.columnas().size(); i++) {
                        String columna = resultado.columnas().get(i);
                        if (columna.equals("TipoRegistroOptimus")) {
                            typeRecordOptimus = Integer.parseInt(fila.get(i));
                        } else {
                            valuesFormatted.add(formateador.formatear(typeRecordOptimus, columna, fila.get(i),
                                    formato.RellenarCampo.equals("S")));
                        }
                    }
                    file.addLine(valuesFormatted);
                }
            }
            file.close();
        } catch (Exception | Error e) {
            file.abandonar();
            throw e;
        }
        return file;
    }

    /** Legacy :148-153 {@code transaction} and :155-215 {@code logica(false, conn)}. */
    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        log.info("== BEGIN JOB ==");
        EntradaFormatoArchivo entrada = new EntradaFormatoArchivo();
        entrada.IdFormatoArchivo = 8L;
        entrada.IdEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        entrada.IdUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        Long discountDay = Long.parseLong(ctx.getParam("DiaDescuento"));
        String fechaCargo = ctx.getParam("FechaCargo");

        List<SalidaFormatoArchivo> lista = new ArrayList<>();
        for (JsonElement formato : formatos.consultar(JsonLegacy.aJson(entrada))) {
            lista.add(JsonLegacy.de(formato, SalidaFormatoArchivo.class));
        }
        SalidaFormatoArchivo formato = lista.get(0);
        FormatoCargos formateador = new FormatoCargos(formato);
        if (formato.CantidadDeTiposDeRegistro == 0) {
            log.info("NO EXISTE FORMATO CONFIGURADO PARA GENERAR EL ARCHIVO");
            return true;
        }
        Llamada storedProcedure = getService(formato, discountDay, fechaCargo);
        List<ResultadoProcedimiento> resultados = storedProcedure.execute(); // TES-J-DEF-02 / 03: second execution, NPE when null
        if (resultados.isEmpty()) {
            log.info("NO EXISTE DATA PARA GENERAR EL ARCHIVO");
            return true;
        }
        ArchivoCargos file = generateFile(resultados, formato, formateador);
        uploadToFtp(file, formato);
        log.info("ARCHIVO CREADO EXITOSAMENTE EN > {}", file.getFullpath());

        if (discountDay > 0) {
            mandatos.logica(discountDay);
        }
        upgradeJobManager(ctx, file, formato);
        ctx.commit();
        log.info("== END JOB ==");
        return true;
    }
}
