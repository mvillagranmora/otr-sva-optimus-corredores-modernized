package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;
import cl.bcs.tesoreria.javacall.distribucion.EntradaRecaudacionTraspaso;
import cl.bcs.tesoreria.javacall.distribucion.SalidaRecaudacionTraspaso;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.jobs.job.archivo.CsvLegacyWriter;
import cl.bcs.tesoreria.jobs.job.archivo.RutaSegura;
import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.Reloj;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.tesoreria.BcsJobProcesarDistribucion} (BcsTesoreriaJobs, 169 lines): the PAC rendition job. Runs the
 * distribution ({@link ProcesarDistribucionService#execute}), stores the processed and unprocessed mandates
 * ({@code ValidarMandatosPAC}), writes {@code <PLA.RutaBaseArchivos>/tes/RendicionPAC<yyyyMMddHHmmss>.csv} (UTF-8 BOM, ';',
 * one header row and one row per output message), sets {@code BAT.Programa.RutaSalida}, commits, then clears the "rendition
 * running" flag ({@code ValidaEjecucionProcesoRecPAC}) and commits again. Rules: RULE-061 (no daemon, no
 * {@code AtomicBoolean running}), RULE-076 (the debug {@code main()} is not ported).
 *
 * <p>Legacy defects kept bug for bug (README, TES-J-DEF-14 and 15): the flag reset always uses {@code p_IdEmpresa = 1} and
 * runs AFTER the first commit; a message with null {@code tipo} or {@code id} aborts the CSV half written.
 */
public final class BcsJobProcesarDistribucion implements TesoreriaJob {

    public static final String CLASE = "cl.bcs.tesoreria.BcsJobProcesarDistribucion";

    private static final Logger log = LoggerFactory.getLogger(BcsJobProcesarDistribucion.class);

    private final ProcesarDistribucionService distribucion;
    private final Colaboradores colaboradores;
    private final ConfiguracionSistema configuracion;
    private final Reloj reloj;

    public BcsJobProcesarDistribucion(ProcesarDistribucionService distribucion, Colaboradores colaboradores,
                                      ConfiguracionSistema configuracion, Reloj reloj) {
        this.distribucion = Objects.requireNonNull(distribucion, "distribucion");
        this.colaboradores = Objects.requireNonNull(colaboradores, "colaboradores");
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    /** BcsJobProcesarDistribucion.java:51-125. */
    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        log.info("*RENDICIONPAC* Inicio de Job");
        try {
            Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
            Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
            String simular = ctx.getParam("Simular");

            EntradaRecaudacionTraspaso entrada = new EntradaRecaudacionTraspaso();
            entrada.p_IdEmpresa = idEmpresa;
            entrada.p_IdUsuario = idUsuario;
            entrada.p_IdModulo = "TES";
            entrada.p_simular = simular;
            JsonObject salidaJson = distribucion.execute(JsonLegacy.aJson(entrada));
            SalidaRecaudacionTraspaso salida = JsonLegacy.de(salidaJson, SalidaRecaudacionTraspaso.class);

            // the legacy handed its typed output to ValidarMandatosPAC.logica(conn, salida, idEmpresa, idUsuario) and let any exception reach the catch
            // below (rollback + rethrow); the service answers the complete object, passed on as it is. With colaboradores-en-proceso=true this call is
            // the typed in-process operation that updates LogMandatoPAC in this transaction (an exception must NOT be swallowed here: the shared
            // transaction would be marked rollback-only)
            JsonObject mandatos = new JsonObject();
            mandatos.add("salida", salidaJson);
            mandatos.addProperty("idEmpresa", idEmpresa);
            mandatos.addProperty("idUsuario", idUsuario);
            colaboradores.invocar(ServiciosExternos.VALIDAR_MANDATOS_PAC, mandatos);

            String rutaBase = ConfiguracionLegacy.plataforma(configuracion, idEmpresa, "PLA.RutaBaseArchivos");
            ConfiguracionLegacy.parametro(configuracion, idEmpresa, ConfiguracionLegacy.PLATAFORMA, "PLA.FechaSistema");
            String rutaRelativa = "tes/RendicionPAC" + ConfiguracionLegacy.formato(reloj.ahora(), "yyyyMMddHHmmss") + ".csv";
            String rutaAbsoluta = (rutaBase + "/" + rutaRelativa).replace("//", "/");

            RutaSegura.bajoBase(rutaBase, rutaAbsoluta); // review M1
            escribirCsv(rutaAbsoluta, salida);
            ctx.updateRutaSalida(rutaRelativa, ctx.idPrograma());
            ctx.commit();
        } catch (Exception e) {
            log.error("Error", e);
            ctx.rollback();
            throw e;
        }

        JsonObject reinicio = new JsonObject();
        reinicio.addProperty("p_IdEmpresa", 1L); // legacy defect TES-J-DEF-14: always empresa 1
        reinicio.addProperty("p_ValorNuevo", "N");
        colaboradores.invocar(ServiciosExternos.VALIDA_EJECUCION_PROCESO_REC_PAC, reinicio);
        ctx.commit();

        log.info("*RENDICIONPAC* Fin de Job");
        return true;
    }

    /** BOM, header, one row per message; a failure to close is swallowed (:93-118). */
    private static void escribirCsv(String ruta, SalidaRecaudacionTraspaso salida) throws Exception {
        FileOutputStream os = new FileOutputStream(ruta);
        os.write(0xef);
        os.write(0xbb);
        os.write(0xbf);
        CsvLegacyWriter csv = new CsvLegacyWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), ';');
        try {
            csv.escribirLinea(new String[]{"id", "tipo", "codigo", "descripcion"});
            for (MensajeSalida mensaje : salida.P_OUT_MENSAJES) {
                // legacy error counter: computed and never used; it throws on a null tipo (TES-J-DEF-15)
                mensaje.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR);
                csv.escribirLinea(new String[]{mensaje.id.toString(), mensaje.tipo, mensaje.codigo, mensaje.descripcion});
            }
        } finally {
            try {
                csv.close();
            } catch (Exception ignorada) {
                // legacy: swallowed
            }
        }
    }
}
