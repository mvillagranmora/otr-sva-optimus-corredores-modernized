package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.sap.BusinessPartner;
import cl.bcs.contabilidad.domain.model.sap.HomologaRespuestaSap;
import cl.bcs.contabilidad.domain.model.sap.ResultadoAsientoSap;
import cl.bcs.contabilidad.domain.model.sap.ResultadoInterfazContable;
import cl.bcs.contabilidad.domain.model.sap.SapException;
import cl.bcs.contabilidad.domain.model.sap.SapRespuesta;
import cl.bcs.contabilidad.domain.model.sap.SapSesion;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.domain.port.out.SapGateway;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Posts staged pre-vouchers to SAP B1, one by one: ensures every partner exists (GET, then POST when 404), posts the
 * JournalVoucher, and on HTTP 204 calls ITZ.ActualizarEstadoPreComprobante (state CON). Every failed id is set to
 * Estado 'E' in one batch at the end; a failure does not stop the batch.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.jobs.InterfazContable.logica / procesarPreComprobante}
 * (BcsContabilidadJobs InterfazContable.java:80-249), CONT-B-40..50. The run-log lines are returned
 * ({@link ResultadoInterfazContable#lineasLog()}) with the legacy texts, including the legacy typo "EXISTOSAMENTE".
 * The SAP error message of a failed id is the HomologaRespuestaSap text for (code, value fragment), with
 * {nombreCampo} / {cuentaContable} replaced by the first quoted text of SAP's message (codes -1000 / -5002); when no
 * row matches it is the raw SAP message, and when the failure is not a SAP answer the line has no message.
 *
 * <p>Not migrated here: the BcsJob shell (scheduler parameters, writing the log file under PLA.RutaBaseArchivos,
 * updateRutaSalida). Not transactional on purpose: each successful id is committed by its own procedure call.
 *
 * <p>Legacy wrote each line to the file as soon as it was known (InterfazContable.java:108, :123, :160, :164), so a
 * failure later in the run (the HomologaRespuestaSap query at :116, the 'E' batch at :175) left the lines written up
 * to that point. Those lines are therefore also reported, in order and as they are produced, to an optional
 * listener ({@code lineaEscrita}); the returned result still carries all of them.
 */
@Service
public class InterfazContableService {

    private static final Logger log = LoggerFactory.getLogger(InterfazContableService.class);

    static final String LINEA_OK = "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: ";
    static final String LINEA_ERROR = "ERROR AL ENVIAR PRECOMPROBANTE A SAP: ";
    static final String LINEA_ERROR_CONEXION = "ERROR AL ENVIAR PRECOMPROBANTE A SAP. MENSAJE: ";
    private static final Pattern ENTRE_COMILLAS = Pattern.compile("'(.*?)'");

    private final GenerarJsonAsientoSapService generador;
    private final SapGateway sap;
    private final InterfazContableRepository repositorio;
    private final DiccionarioPort diccionario;
    private final Consumer<String> lineaEscrita;

    @Autowired
    public InterfazContableService(GenerarJsonAsientoSapService generador, SapGateway sap,
                                   InterfazContableRepository repositorio, DiccionarioPort diccionario) {
        this(generador, sap, repositorio, diccionario, linea -> { });
    }

    /**
     * @param lineaEscrita receives every run-log line when it is produced, before {@link #procesar} returns or
     *                     fails; it must not throw
     */
    public InterfazContableService(GenerarJsonAsientoSapService generador, SapGateway sap,
                                   InterfazContableRepository repositorio, DiccionarioPort diccionario,
                                   Consumer<String> lineaEscrita) {
        this.generador = generador;
        this.sap = sap;
        this.repositorio = repositorio;
        this.diccionario = diccionario;
        this.lineaEscrita = Objects.requireNonNull(lineaEscrita, "lineaEscrita");
    }

    /** Job parameter SecCmbte: base64 (UTF-8) of the ids joined by ';' (InterfazContable.java:89-90). */
    public static List<Long> decodificarSecuencias(String secCmbteBase64) {
        String texto = new String(Base64.getDecoder().decode(secCmbteBase64), StandardCharsets.UTF_8);
        return Arrays.stream(texto.split(";")).map(Long::parseLong).toList();
    }

    public ResultadoInterfazContable procesar(long idEmpresa, long idUsuario, String destino, List<Long> secuencias) {
        List<String> lineas = new ArrayList<>();
        Consumer<String> registrar = linea -> {
            lineas.add(linea);
            lineaEscrita.accept(linea);
        };
        List<Long> enviados = new ArrayList<>();
        List<Long> conError = new ArrayList<>();

        SapSesion sesion = null;
        try {
            sesion = sap.login();
        } catch (RuntimeException e) {
            log.warn("InterfazContable: SAP B1 login failed: {}", e.getMessage());
            registrar.accept(LINEA_ERROR_CONEXION
                    + diccionario.mensaje(idEmpresa, "UI_ERROR_CONEXION_SAP", Map.of()));
            conError.addAll(secuencias);
        }
        List<HomologaRespuestaSap> homologacion = repositorio.consultarHomologaRespuestaSap();

        if (sesion != null) {
            for (Long id : secuencias) {
                try {
                    procesarPreComprobante(idEmpresa, idUsuario, id, sesion);
                    enviados.add(id);
                    registrar.accept(LINEA_OK + id);
                } catch (SapException e) {
                    log.warn("InterfazContable: pre-voucher {} rejected: {}", id, e.getMessage());
                    conError.add(id);
                    registrar.accept(mensajeHomologado(e.respuesta(), homologacion)
                            .map(m -> LINEA_ERROR + id + ", Mensaje: " + m).orElse(LINEA_ERROR + id));
                } catch (RuntimeException e) {
                    log.warn("InterfazContable: pre-voucher {} failed", id, e);
                    conError.add(id);
                    registrar.accept(LINEA_ERROR + id);
                }
            }
        }
        if (!conError.isEmpty()) repositorio.marcarConError(conError);
        return new ResultadoInterfazContable(enviados, conError, lineas);
    }

    /** InterfazContable.java:188-249. */
    private void procesarPreComprobante(long idEmpresa, long idUsuario, long id, SapSesion sesion) {
        ResultadoAsientoSap resultado = generador.generar(idEmpresa, List.of(id));
        for (BusinessPartner socio : resultado.socios()) {
            SapRespuesta existe = sap.consultarSocio(sesion, socio.CardCode());
            if (existe.Codigo() == 404) {
                SapRespuesta creado = sap.crearSocio(sesion, socio);
                if (creado.Codigo() != 201) {
                    // legacy reports the status of the CHECK (404), not of the creation (CONT-B-46)
                    throw new SapException(new SapRespuesta(existe.Codigo(), "Error al crear socio " + creado));
                }
            } else if (existe.Codigo() != 200) {
                throw new SapException(new SapRespuesta(existe.Codigo(),
                        "Error al verificar si existe socio " + socio.CardCode() + " - " + existe));
            }
        }
        if (resultado.vouchers().isEmpty()) {
            throw new IllegalStateException("pre-voucher " + id + " is not in the SAP staging view");
        }
        SapRespuesta respuesta = sap.enviarAsiento(sesion, resultado.vouchers().get(0));
        if (respuesta.Codigo() != 204) throw new SapException(respuesta);
        repositorio.actualizarEstadoContabilizado(idEmpresa, idUsuario, id);
    }

    /**
     * InterfazContable.java:131-157. Empty when the answer cannot be interpreted (legacy inner catch, :162-166): for
     * HTTP 400 the body must be {@code {"error":{"code":n,"message":{"value":"..."}}}}.
     */
    static Optional<String> mensajeHomologado(SapRespuesta r, List<HomologaRespuestaSap> homologacion) {
        try {
            boolean errorSap = r.Codigo() == 400;
            JsonObject error = errorSap ? JsonParser.parseString(r.Mensaje()).getAsJsonObject().getAsJsonObject("error") : null;
            int codigo = errorSap ? error.get("code").getAsInt() : r.Codigo();
            String valor = errorSap ? error.getAsJsonObject("message").get("value").getAsString() : "";
            String mensaje = homologacion.stream()
                    .filter(h -> h.Codigo() == codigo && valor.contains(h.Valor()))
                    .map(HomologaRespuestaSap::Mensaje).findFirst().orElse(r.Mensaje());
            if (codigo == -1000) mensaje = reemplazarPrimerCitado(mensaje, valor, "{nombreCampo}");
            if (codigo == -5002) mensaje = reemplazarPrimerCitado(mensaje, valor, "{cuentaContable}");
            return Optional.of(String.valueOf(mensaje));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static String reemplazarPrimerCitado(String mensaje, String valor, String marcador) {
        Matcher m = ENTRE_COMILLAS.matcher(valor);
        return m.find() ? mensaje.replace(marcador, m.group(1)) : mensaje;
    }
}
