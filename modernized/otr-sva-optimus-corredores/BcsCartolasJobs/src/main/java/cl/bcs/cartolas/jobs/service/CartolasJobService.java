package cl.bcs.cartolas.jobs.service;

import cl.bcs.cartolas.jobs.domain.CartolasJobRequest;
import cl.bcs.cartolas.jobs.domain.PasosEjecucion;
import cl.bcs.cartolas.jobs.port.in.CartolasJobUseCase;
import cl.bcs.cartolas.jobs.port.out.CargaDatosPort;
import cl.bcs.cartolas.jobs.port.out.EmisionPort;
import cl.bcs.cartolas.jobs.port.out.NominaPort;
import cl.bcs.cartolas.jobs.port.out.ParametroPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Servicio de orquestación del proceso de cartolas.
 *
 * Modernización de: cl.bcs.normativa.cartolas.tools.ProcesoCartolasApp.procesar()
 *                   cl.bcs.normativa.cartolas.jobs.BcsJobCartolas.transaction()
 *
 * B-CAR-01: Routing de pasos por TipoProceso
 * B-CAR-02: Validación — nómina ya certificada → IllegalStateException
 * B-CAR-03: Nomina.abrir() / Nomina.cerrar() enmarcan el proceso
 *
 * Los métodos de lógica pura son {@code public static} para testear sin Spring.
 */
@Service
public class CartolasJobService implements CartolasJobUseCase {

    private static final Logger log = LoggerFactory.getLogger(CartolasJobService.class);

    private final NominaPort nominaPort;
    private final ParametroPort parametroPort;
    private final CargaDatosPort cargaDatosPort;
    private final EmisionPort emisionPort;

    public CartolasJobService(NominaPort nominaPort,
                              ParametroPort parametroPort,
                              CargaDatosPort cargaDatosPort,
                              EmisionPort emisionPort) {
        this.nominaPort    = nominaPort;
        this.parametroPort = parametroPort;
        this.cargaDatosPort = cargaDatosPort;
        this.emisionPort   = emisionPort;
    }

    /**
     * B-CAR-01 / B-CAR-02 / B-CAR-03: Ejecuta el proceso de cartolas.
     *
     * Oracle: ProcesoCartolasApp.procesar() — flujo completo incluyendo
     *         validación de nómina, apertura, pasos por tipoProceso y cierre.
     */
    @Override
    public void ejecutar(CartolasJobRequest request) {
        Objects.requireNonNull(request, "request no puede ser null");

        final long jobId     = request.jobId();
        final long idEmpresa = request.idEmpresa();
        final String tipo    = request.tipoProceso();

        log.info("INICIO::CartolasJob jobId={} tipoProceso={}", jobId, tipo);
        final long inicio = System.currentTimeMillis();

        // B-CAR-02: validar que la nómina no esté certificada
        final boolean certificada = nominaPort.esCertificada(idEmpresa, jobId);
        validarNominaNoEstaCertificada(certificada, jobId);

        // B-CAR-03: abrir nómina
        nominaPort.abrir(idEmpresa, jobId, tipo);

        try {
            // B-CAR-01: determinar pasos según tipoProceso
            final PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(tipo);

            if (pasos.guardarParametros()) {
                Objects.requireNonNull(parametroPort, "parametroPort no puede ser null");
                parametroPort.guardar(idEmpresa, jobId, Map.of("tipoProceso", tipo));
            }

            if (pasos.poblarNomina()) {
                Objects.requireNonNull(nominaPort, "nominaPort no puede ser null");
                final long idNomina = nominaPort.obtenerIdNomina(idEmpresa, jobId);
                nominaPort.poblar(idEmpresa, jobId, idNomina);
            }

            if (pasos.cargarDatos()) {
                Objects.requireNonNull(cargaDatosPort, "cargaDatosPort no puede ser null");
                cargaDatosPort.cargar(idEmpresa, jobId);
            }

            if (pasos.emitir()) {
                Objects.requireNonNull(emisionPort, "emisionPort no puede ser null");
                emisionPort.emitir(idEmpresa, jobId);
            }

            // B-CAR-03: cerrar nómina en éxito
            nominaPort.cerrar(idEmpresa, jobId);

        } catch (Exception ex) {
            // B-CAR-03: cerrar nómina con error
            nominaPort.cerrarConError(idEmpresa, jobId, ex);
            throw ex;
        }

        log.info("FIN::CartolasJob jobId={} tipoProceso={} {}ms",
                jobId, tipo, System.currentTimeMillis() - inicio);
    }

    /**
     * B-CAR-02: Valida que la nómina no esté ya certificada.
     *
     * Lógica pura, testeable sin Spring.
     * Oracle: ProcesoCartolasApp.procesar() — if (nomina.isCertificada()) throw NominaException
     *
     * @param certificada estado de certificación de la nómina
     * @param jobId       identificador del job (para el mensaje de error)
     * @throws IllegalStateException si la nómina ya está certificada
     */
    public static void validarNominaNoEstaCertificada(boolean certificada, long jobId) {
        if (certificada) {
            throw new IllegalStateException(
                    "La nomina ya esta certificada: jobId=" + jobId);
        }
    }

    /**
     * B-CAR-01: Determina el número de pasos que se ejecutarán para un tipoProceso.
     *
     * Lógica pura, testeable sin Spring.
     * Cuenta cuántos pasos son {@code true} en {@link PasosEjecucion}.
     *
     * @param tipoProceso tipo de proceso
     * @return número de pasos activos (0-4)
     */
    public static int contarPasosActivos(String tipoProceso) {
        Objects.requireNonNull(tipoProceso, "tipoProceso no puede ser null");
        final PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(tipoProceso);
        int count = 0;
        if (pasos.guardarParametros()) count++;
        if (pasos.poblarNomina())      count++;
        if (pasos.cargarDatos())       count++;
        if (pasos.emitir())            count++;
        return count;
    }

    /**
     * B-CAR-01: Describe los pasos del flujo para un tipoProceso como string.
     *
     * Lógica pura, testeable sin Spring.
     * Útil para logging y diagnóstico.
     *
     * @param tipoProceso tipo de proceso
     * @return descripción textual de los pasos activos
     */
    public static String describirFlujo(String tipoProceso) {
        Objects.requireNonNull(tipoProceso, "tipoProceso no puede ser null");
        final PasosEjecucion pasos = PasosEjecucion.paraTipoProceso(tipoProceso);
        final var sb = new StringBuilder();
        if (pasos.guardarParametros()) sb.append("PARAMETROS ");
        if (pasos.poblarNomina())      sb.append("POBLAR_NOMINA ");
        if (pasos.cargarDatos())       sb.append("CARGA_DATOS ");
        if (pasos.emitir())            sb.append("EMISION");
        return sb.toString().trim();
    }
}
