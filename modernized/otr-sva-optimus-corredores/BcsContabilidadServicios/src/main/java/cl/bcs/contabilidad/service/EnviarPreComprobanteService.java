package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Schedules the sending of pre-vouchers to SAP: registers the platform batch program ASIENTO_SAP and marks the
 * pre-vouchers with it (Estado P).
 *
 * <p>Legacy {@code cl.bcs.contabilidad.EnviarPreComprobante}. Implements CONT-G-160..166. Only pre-vouchers in Estado
 * V, D or E can be sent; any other estado is an ERROR per pre-voucher and nothing is sent. The program is registered
 * through {@link ProcesoBatchPort} (legacy {@code InscribirProcesoBatch}, platform module); without that port the
 * request is refused before anything is written ({@link FuncionalidadNoMigradaException}). Database failures propagate.
 */
@Service
public class EnviarPreComprobanteService {

    private static final Set<String> ESTADOS_ENVIABLES = Set.of("V", "D", "E");
    /** Legacy keeps the tokens NumberUtils.isNumber accepts and parses them with Long.parseLong (L93). */
    private static final Pattern ENTERO = Pattern.compile("-?\\d+");

    private final String SQL_SELECT = SqlResources.load("EnviarPreComprobante_SelectPreComprobantes.sql");
    private final String SQL_UPDATE = SqlResources.load("EnviarPreComprobante_UpdatePreComprobantes.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final ProcesoBatchPort procesoBatch;

    @Autowired
    public EnviarPreComprobanteService(NamedParameterJdbcTemplate jdbc, ObjectProvider<ProcesoBatchPort> procesoBatch) {
        this(jdbc, procesoBatch.getIfAvailable());
    }

    public EnviarPreComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, (ProcesoBatchPort) null);
    }

    public EnviarPreComprobanteService(NamedParameterJdbcTemplate jdbc, ProcesoBatchPort procesoBatch) {
        this.jdbc = jdbc;
        this.procesoBatch = procesoBatch;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_UsaReglasNegocio") String usaReglasNegocio,
                   @SerializedName("p_SecCmbte") String secCmbte,
                   @SerializedName("p_Destino") String destino) {
        List<Long> secuencias() {
            if (secCmbte == null) return List.of();
            return Arrays.stream(secCmbte.split(";")).filter(s -> ENTERO.matcher(s).matches()).map(Long::parseLong).toList();
        }
    }

    record Salida(@SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador) {}

    private record PreComprobante(Long idPreComprobante, String estado, String nombreEstado) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), enviar(entrada));
    }

    Salida enviar(Entrada entrada) {
        List<Long> secuencias = entrada.secuencias();
        if (secuencias.isEmpty()) {
            // Legacy returns before counting errors: the counter stays 0 (L94-97).
            return new Salida(List.of(MensajeSalida.error(0L, "ERROR_PRECOMPROBANTE_VACIO", Map.of())), 0L);
        }
        List<PreComprobante> precomprobantes = jdbc.query(SQL_SELECT, new MapSqlParameterSource("IdPreComprobantes", secuencias),
                SqlRow.mapper(r -> new PreComprobante(r.lng("IdPreComprobante"), r.str("Estado"), r.str("NombreEstado"))));

        List<MensajeSalida> mensajes = new ArrayList<>();
        List<PreComprobante> validos = new ArrayList<>();
        long id = 0;
        for (PreComprobante p : precomprobantes) {
            if (ESTADOS_ENVIABLES.contains(p.estado())) {
                validos.add(p);
            } else {
                Map<String, String> v = new HashMap<>();
                v.put("IdComprobante", String.valueOf(p.idPreComprobante()));
                mensajes.add(MensajeSalida.error(id++, "ERROR_PROCESO_PRECOMPROBANTE", v));
            }
        }
        if (!mensajes.isEmpty()) return new Salida(mensajes, MensajeSalida.contarErrores(mensajes));
        if (validos.isEmpty()) {
            // None of the ids exists: legacy registered an ASIENTO_SAP program with nothing to send; no program here.
            return new Salida(List.of(MensajeSalida.informacion(0L, "PRECOMPROBANTES_EN_ENVIO_SAP", Map.of())), 0L);
        }

        ProcesoBatchPort.Inscripcion inscripcion = inscribirAsientoSap(entrada, validos);
        if (inscripcion.erroresContador() > 0) return new Salida(inscripcion.mensajes(), inscripcion.erroresContador());

        jdbc.batchUpdate(SQL_UPDATE, SqlParams.batch(validos, p -> new MapSqlParameterSource()
                .addValue("IdPrograma", inscripcion.idPrograma())
                .addValue("IdPreComprobante", p.idPreComprobante())));
        boolean hayDescuadrados = validos.stream().anyMatch(p -> "D".equals(p.estado()));
        MensajeSalida resultado = hayDescuadrados
                ? MensajeSalida.advertencia(0L, "PRECOMPROBANTES_EN_ENVIO_SAP_DESC", Map.of())
                : MensajeSalida.informacion(0L, "PRECOMPROBANTES_EN_ENVIO_SAP", Map.of());
        return new Salida(List.of(resultado), 0L);
    }

    /** Legacy L136-155: program ASIENTO_SAP, 1 s delay, the valid ids joined by ';' and base64-encoded. */
    private ProcesoBatchPort.Inscripcion inscribirAsientoSap(Entrada entrada, List<PreComprobante> validos) {
        if (procesoBatch == null) {
            throw new FuncionalidadNoMigradaException("EnviarPreComprobante: batch program registration (InscribirProcesoBatch) not available");
        }
        String secuencias = validos.stream().map(p -> String.valueOf(p.idPreComprobante())).collect(Collectors.joining(";"));
        String secCmbteB64 = Base64.getEncoder().encodeToString(secuencias.getBytes(StandardCharsets.UTF_8));
        return procesoBatch.inscribir(entrada.idEmpresa(), entrada.idModulo(), entrada.idUsuario(), "ASIENTO_SAP", 1L, List.of(
                new ProcesoBatchPort.Parametro("IdEmpresa", String.valueOf(entrada.idEmpresa()), null),
                new ProcesoBatchPort.Parametro("IdUsuario", String.valueOf(entrada.idUsuario()), null),
                new ProcesoBatchPort.Parametro("Destino", String.valueOf(entrada.destino()), null),
                new ProcesoBatchPort.Parametro("SecCmbte", null, secCmbteB64)));
    }
}
