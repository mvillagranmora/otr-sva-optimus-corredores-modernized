package cl.bcs.apv.jobs.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.mapeo.CargarArchivoPreviredMapeo;
import cl.bcs.apv.api.casouso.CargarArchivoPrevired;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredEntrada;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredResultado;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.jobs.job.archivo.RutaSegura;
import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobContext;

/**
 * Mass load of Previred: reads param p_parametros, calls the typed use case CargarArchivoPrevired, writes
 * APV/LOG_CARGA_MASIVA_PREVIRED_&lt;userId&gt;_&lt;yyyyMMddHHmmss&gt;.txt (legacy BcsJobCargaMasivaPrevired.java:56-135).
 *
 * <p>Reproduced bug for bug (defects listed in README_CARACTERIZACION.md): the {@code FileWriter} is opened before the folder is created (CMP-012,
 * the {@code mkdirs} is dead code); any {@code Exception} of the service is swallowed, rolled back and the job still returns true (CMP-023/024);
 * an {@code Error} propagates (CMP-025). Path safety (review H-3, lenient rule of Tesoreria): the user id is checked as a name part and the
 * target must stay under the base ({@link RutaSegura}); a blank base is not checked (legacy path, open item).
 */
public final class BcsJobCargaMasivaPrevired implements ApvJob {

    /** Legacy class name, the routing key of /internal/job-call. */
    public static final String CLASE = "cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired";

    private static final Logger log = LoggerFactory.getLogger(BcsJobCargaMasivaPrevired.class);
    private static final DateTimeFormatter MARCA = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CargarArchivoPrevired cargarArchivoPrevired;
    private final ConfiguracionSistema configuracion;
    private final Reloj reloj;

    public BcsJobCargaMasivaPrevired(CargarArchivoPrevired cargarArchivoPrevired, ConfiguracionSistema configuracion, Reloj reloj) {
        this.cargarArchivoPrevired = cargarArchivoPrevired;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        log.info("INICIO::BcsJobCargaMasivaPrevired");
        CargarArchivoPreviredEntrada entrada = prepararParametrosEntrada(ctx.getParam("p_parametros"));

        String base = ConfiguracionLegacy.plataforma(configuracion, entrada.idEmpresa(), "PLA.RutaBaseArchivos"); // :55
        String nombre = "APV/LOG_CARGA_MASIVA_PREVIRED_" + RutaSegura.nombre("idUsuario", String.valueOf(entrada.idUsuario())) + "_"
                + MARCA.withZone(ZoneId.systemDefault()).format(reloj.ahora()) + ".txt"; // :56-58
        // H-3 (lenient, as Tesoreria): the target must stay under the base. A blank base keeps the legacy "/APV/..." path unchecked (OPEN: container-root risk)
        if (base != null && !base.isBlank()) {
            RutaSegura.bajoBase(base, base + "/" + nombre);
        }
        // :61-62 the writer is created BEFORE the folder check (CMP-012); the legacy used the platform charset (UTF-8 on Java 18+)
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(base + "/" + nombre), StandardCharsets.UTF_8));
        try {
            File carpeta = new File(base + "/APV");
            if (!carpeta.exists()) {
                carpeta.mkdirs(); // dead in practice, kept as the legacy had it
            }
            log.info("EJECUCION SERVICIO DE CARGA MASIVA PREVIRED*****");
            bw.write("EJECUCION SERVICIO DE CARGA MASIVA PREVIRED*****\n");

            CargarArchivoPreviredResultado salida = cargarArchivoPrevired.ejecutar(entrada);

            bw.write("RESULTADO DE CARGA MASIVA PREVIRED*****\n");
            ctx.updateRutaSalida(nombre, ctx.idPrograma()); // :86-89 BAT.Programa.RutaSalida on the job transaction
            for (Mensaje a : salida.mensajes()) { // a null list fails here, after RutaSalida (CMP-026)
                String linea = "{ id: " + a.id() + ", tipo: " + a.tipo() + ", codigo: " + a.codigo() + ", descripcion: " + a.descripcion() + " }";
                log.info(linea);
                bw.write(linea + "\n");
            }
            bw.write("FIN RESULTADO*****");
            return true;
        } catch (Exception er) {
            // :114-116 CMP-023/024: swallowed; the job-level rollback, then the legacy falls through to return true
            ctx.rollback();
            log.error("Carga masiva Previred failed; rolled back and reported as success as the legacy did", er);
        } finally {
            bw.close();
        }
        return true;
    }

    /** Legacy {@code prepararParametrosEntradaCargaPrevired} (:121-135): the JSON under {@code p_parametros.p_parametros}, mapped with the legacy field names. */
    private static CargarArchivoPreviredEntrada prepararParametrosEntrada(String parametros) {
        JsonObject interno = JsonParser.parseString(parametros).getAsJsonObject().getAsJsonObject("p_parametros");
        return CargarArchivoPreviredMapeo.INSTANCIA.entradaDesdeJson(interno);
    }
}
