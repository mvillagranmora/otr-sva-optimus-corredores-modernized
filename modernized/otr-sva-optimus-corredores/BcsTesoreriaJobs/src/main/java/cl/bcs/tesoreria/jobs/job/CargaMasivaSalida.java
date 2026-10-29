package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.javacall.kit.colaborador.AgregarLog;
import cl.bcs.tesoreria.jobs.job.archivo.CsvLegacyWriter;
import cl.bcs.tesoreria.jobs.job.archivo.RutaSegura;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * The outputs of the mass-load job that are shared by all its guards: the one-line error CSV, the run output
 * ({@code BAT.Programa.RutaSalida}) and the AgregarLog record. Legacy: BcsJobCargaMasivaIngresoEgresoDAPFFMM.java:291-415.
 */
final class CargaMasivaSalida {

    private static final String BASE_NOMBRE_ARCHIVO_SALIDA_ERRORES = "CDB_Errores_IngresoEgreso_";

    private final Colaboradores colaboradores;
    private final ConfiguracionSistema configuracion;

    CargaMasivaSalida(Colaboradores colaboradores, ConfiguracionSistema configuracion) {
        this.colaboradores = colaboradores;
        this.configuracion = configuracion;
    }

    /** Legacy :349-383 {@code generarArchivoSalidaErrores}; "" when the folder does not exist or the write failed (TES-J-DEF-16). */
    String generarArchivoSalidaErrores(String fechaActual, String horaProceso, String mensaje, String tipoArchivoCarga,
                                       Long idEmpresa) throws Exception {
        RutaSegura.nombre("TipoArchivoCarga", tipoArchivoCarga); // review M1
        String[] linea = {"0", "0", "Rechazado", mensaje};
        String fechaHora = fechaActual.replace("-", "") + "_" + horaProceso.replace(":", "");
        String nombreArchivoSalida = BASE_NOMBRE_ARCHIVO_SALIDA_ERRORES + tipoArchivoCarga + "_" + fechaHora + ".csv";

        String rutaAbsolutaBase = ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
        String rutaRelativaCarga = ConfiguracionLegacy.parametro(configuracion, idEmpresa, "TESORERIA",
                "TES.RutaSalidaArchivosCargaMasiva");
        String rutaRelativaArchivo = rutaRelativaCarga + nombreArchivoSalida;
        if (!new File(rutaAbsolutaBase + rutaRelativaCarga).exists()) {
            return "";
        }
        RutaSegura.bajoCarpeta(rutaAbsolutaBase, rutaAbsolutaBase + rutaRelativaArchivo);
        return generarReporteCsv(rutaAbsolutaBase, rutaRelativaArchivo, linea) ? rutaRelativaArchivo : "";
    }

    /** Legacy :385-415 {@code generarReporteCSV}: opencsv without quote character, ';' separated. */
    private static boolean generarReporteCsv(String rutaAbsolutaBase, String rutaRelativaArchivo, String[] linea)
            throws Exception {
        FileOutputStream fos = new FileOutputStream(rutaAbsolutaBase + rutaRelativaArchivo);
        CsvLegacyWriter csv = new CsvLegacyWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8), ';',
                CsvLegacyWriter.SIN_COMILLA);
        boolean correcto = true;
        try {
            csv.escribirLinea(linea);
        } catch (Exception e) {
            correcto = false;
        }
        try {
            csv.close();
        } catch (Exception e) {
            correcto = false;
        }
        return correcto;
    }

    /** Legacy :291-333 {@code agregarLog}: estadoProcesado "S" (processed) or "N" (rejected). */
    void agregarLog(Long idEmpresa, Long idUsuario, String estadoProcesado, String fechaSistema,
                    String tipoArchivoCarga, String mensajes) {
        AgregarLog.InAgregarLog entrada = new AgregarLog.InAgregarLog();
        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdCabeceraLog = idUsuario;
        log.IdTipoLog = "INFORMATIVO";
        log.IdModulo = "TES";
        log.IdUsuario = idUsuario;
        log.IdEmpresa = idEmpresa;
        log.FechaSistemaLog = fechaSistema;
        log.IdUsuarioAtendido = idUsuario;
        log.IdCliente = 0L;
        log.EstadoCliente = "";
        log.NombreCliente = "";
        log.IdTipoDocumentoIdentidad = "";
        log.IdDocumentoIdentidad = "";
        log.IdPortafolio = 0L;
        log.EstadoPortafolio = "";
        log.IdTipoRegistro = "CARGA_MASIVA_INGRESO_EGRESO_" + tipoArchivoCarga;
        log.EstadoRegistro = "S".equals(estadoProcesado) ? "PRO" : "RZD";
        log.IdTipoOperacion = "";
        log.Folio = 0L;
        log.Cantidad = BigDecimal.ZERO;
        log.PrecioTasa = BigDecimal.ZERO;
        log.Monto = BigDecimal.ZERO;
        log.IdMonedaOrigen = "";
        log.IdMonedaLiquidacion = "";
        log.TipoCambio = "";
        String conErrores = "Proceso de Carga Masiva ha terminado con errores: " + mensajes;
        log.Nemo = "N".equals(estadoProcesado) ? conErrores : mensajes;
        log.IdProceso = 0L;
        log.EstadoProceso = "S".equals(estadoProcesado) ? "ING" : "RZD";
        log.NombreProceso = "N".equals(estadoProcesado) ? conErrores : mensajes;
        entrada.p_Log.add(log);
        AgregarLog.invocar(colaboradores, entrada);
    }

    /** Legacy :335-347 {@code actualizarRutaSalidaPrograma}. */
    static void actualizarRutaSalida(JobContext ctx, String rutaArchivoSalida, Long idPrograma) {
        ctx.updateRutaSalida(rutaArchivoSalida, idPrograma);
    }
}
