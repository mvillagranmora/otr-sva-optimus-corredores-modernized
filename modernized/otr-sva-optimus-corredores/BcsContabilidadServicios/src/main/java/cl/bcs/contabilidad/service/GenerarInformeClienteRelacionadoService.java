package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.ClienteRelacionadoRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.informe.InformeClienteRelacionadoWriter;
import cl.bcs.contabilidad.service.ConsultarOperacionesClienteRelacionadoService.Consulta;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes the related-client report to disk: legacy {@code cl.bcs.contabilidad.GenerarInformeClienteRelacionado.logica()}
 * (GenerarInformeClienteRelacionado.java:139-240), rules CONT-D-50..54. Production reaches it through the batch with
 * {@code debug=false} (BcsContabilidadJobs GenerarInformeClienteRelacionadoBatch.java:53).
 *
 * <p>Flow: the file name comes from the injected clock; the folder is PLATAFORMA/PLA.RutaBaseArchivos +
 * CONTABILIDAD/RutaInformeClienteRel, and the returned RutaArchivo is RutaInformeClienteRel + name. An empty path gives
 * FALTA_CONFIGURACION_RUTA_ARCHIVO and an end date before the start date gives UI_FECHAS_INVALIDAS. A query error is
 * reported with the query's P_OUT_MENSAJE as the message code. Any other failure (a missing parameter, a malformed
 * date, the logo, the write) gives UI_ERROR_GENERAR_INFORME. Output through a plain Gson: null RutaArchivo is omitted.
 *
 * <p>Legacy {@code call()} hard-coded {@code debug=true}, which sent both the file and RutaArchivo to
 * {@code C:\__ARCHIVO_PRUEBA\}. Here that override is the optional property
 * {@code contabilidad.informe-cliente-relacionado.ruta-depuracion}; when it is not set, the configured paths are used.
 */
@Service
public class GenerarInformeClienteRelacionadoService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeClienteRelacionadoService.class);

    static final String FALTA_CONFIGURACION_RUTA_ARCHIVO = "FALTA_CONFIGURACION_RUTA_ARCHIVO";
    static final String UI_FECHAS_INVALIDAS = "UI_FECHAS_INVALIDAS";
    static final String UI_ERROR_GENERAR_INFORME = "UI_ERROR_GENERAR_INFORME";

    private final ConsultarOperacionesClienteRelacionadoService consulta;
    private final ClienteRelacionadoRepository repository;
    private final PlataformaPort plataforma;
    private final InformeClienteRelacionadoWriter writer;
    private final Clock clock;
    /** Replaces both the physical folder and the returned folder when set (legacy debug=true). */
    private final String rutaDepuracion;

    public GenerarInformeClienteRelacionadoService(ConsultarOperacionesClienteRelacionadoService consulta,
                                                   ClienteRelacionadoRepository repository, PlataformaPort plataforma,
                                                   InformeClienteRelacionadoWriter writer, Clock clock) {
        this(consulta, repository, plataforma, writer, clock, "");
    }

    @Autowired
    public GenerarInformeClienteRelacionadoService(ConsultarOperacionesClienteRelacionadoService consulta,
                                                   ClienteRelacionadoRepository repository, PlataformaPort plataforma,
                                                   InformeClienteRelacionadoWriter writer, Clock clock,
                                                   @Value("${contabilidad.informe-cliente-relacionado.ruta-depuracion:}")
                                                   String rutaDepuracion) {
        this.consulta = consulta;
        this.repository = repository;
        this.plataforma = plataforma;
        this.writer = writer;
        this.clock = clock;
        this.rutaDepuracion = rutaDepuracion == null ? "" : rutaDepuracion;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_FechaInicio") String fechaInicio,
                   @SerializedName("p_FechaFin") String fechaFin,
                   @SerializedName("p_IdModulo") String idModulo) {}

    record Salida(@SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("RutaArchivo") String rutaArchivo) {

        static Salida error(String codigo) {
            List<MensajeSalida> mensajes = new ArrayList<>(List.of(MensajeSalida.error(0L, codigo, Map.of())));
            return new Salida(mensajes, MensajeSalida.contarErrores(mensajes), null);
        }
    }

    public JsonObject execute(JsonObject params) {
        Entrada in = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.plain(), generar(in));
    }

    Salida generar(Entrada in) {
        try {
            String nombre = InformeClienteRelacionadoWriter.nombreArchivo(LocalDateTime.now(clock));
            String rutaBase = parametro(in.idEmpresa(), "PLATAFORMA", "PLA.RutaBaseArchivos");
            String rutaInforme = parametro(in.idEmpresa(), "CONTABILIDAD", "RutaInformeClienteRel");

            boolean depuracion = !rutaDepuracion.isEmpty();
            String rutaFisica = depuracion ? rutaDepuracion : rutaBase + rutaInforme;
            String rutaFinal = depuracion ? rutaDepuracion : rutaInforme;
            if (rutaFisica.isEmpty() || rutaFinal.isEmpty()) return Salida.error(FALTA_CONFIGURACION_RUTA_ARCHIVO);

            if (LocalDate.parse(in.fechaFin()).isBefore(LocalDate.parse(in.fechaInicio()))) {
                return Salida.error(UI_FECHAS_INVALIDAS);
            }

            Files.createDirectories(Path.of(rutaFisica));

            Consulta resultado = consulta.consultar(in.idEmpresa(), in.fechaInicio(), in.fechaFin());
            if (resultado.errores() > 0) return Salida.error(resultado.mensaje());

            byte[] logo = repository.consultarLogoCorredora(in.idEmpresa());
            byte[] informe = writer.escribir(in.fechaInicio(), in.fechaFin(), logo, resultado.resultado());
            // GICR:192 rutaFisica.concat(nombre): plain concatenation, as configured
            Files.write(Path.of(rutaFisica + nombre), informe);

            return new Salida(new ArrayList<>(), 0L, rutaFinal + nombre);
        } catch (RuntimeException | IOException e) {
            log.error("GenerarInformeClienteRelacionado empresa {}: {}", in.idEmpresa(), e.getMessage(), e);
            return Salida.error(UI_ERROR_GENERAR_INFORME);
        }
    }

    /** Legacy ConfiguracionSistema.getParametroConfiguracionString threw when the parameter did not exist. */
    private String parametro(Long idEmpresa, String modulo, String nombre) {
        String valor = plataforma.obtenerParametroConfiguracion(idEmpresa, modulo, nombre, null);
        if (valor == null) {
            throw new IllegalStateException("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return valor;
    }
}
