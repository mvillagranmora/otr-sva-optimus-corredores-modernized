package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.RelojSistemaPort;
import cl.bcs.contabilidad.domain.port.out.SecuenciaPort;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Reverses pre-vouchers: copies each one with debit and credit swapped (the swap is done by
 * ReversaPreComprobante_SelectPrecomprobanteAsiento.sql), inserts header, lines and events, then stages the new
 * pre-vouchers with {@link EnvioContabilidadService} ({@code p_UsaReglasNegocio = S}, {@code p_Destino = B}) and
 * merges its counter and messages.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.ReversaPreComprobante.logica} (ReversaPreComprobante.java:134-258),
 * CONT-B-30..35. An already-reversed id answers ERROR PRECOMPROBANTE_YA_ESTA_REVERSADO without raising the counter
 * (legacy keeps only EnvioContabilidad's counter). {@code IdPreComprobante = 0} draws a new id from the sequence.
 *
 * <p>Approved deviation: where legacy threw "key field is not unique" ({@code Utils.List2Hash}, :151 and :181), this
 * service answers a controlled ERROR with counter 1 and writes nothing: a repeated IdPreComprobanteAnt in the input
 * (PRECOMPROBANTE_ANTERIOR_REPETIDO) or a pre-voucher already reversed more than once (PRECOMPROBANTE_REVERSADO_MAS_DE_UNA_VEZ).
 */
@Service
public class ReversaPreComprobanteService {

    static final String SEQ_PRECOMPROBANTE = "BSCL.Contabilidad.BD.Secuencias::IdPreComprobante";
    static final String SEQ_PRECOMPROBANTE_ASIENTO = "BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento";
    static final String ANTERIOR_REPETIDO = "PRECOMPROBANTE_ANTERIOR_REPETIDO";
    static final String REVERSADO_MAS_DE_UNA_VEZ = "PRECOMPROBANTE_REVERSADO_MAS_DE_UNA_VEZ";
    private static final String GLOSA = "REVERSA PRE-COMPROBANTE N° ";

    private final String SQL_SELECT_REVERSA = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectReversaPrecomprobante.sql");
    private final String SQL_SELECT_ANTERIOR = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectPrecomprobanteAnterior.sql");
    private final String SQL_SELECT_ASIENTO = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectPrecomprobanteAsiento.sql");
    private final String SQL_SELECT_EVENTO = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectEventoPrecomprobanteAsiento.sql");
    private final String SQL_INSERT_PRECOMPROBANTE = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_InsertPrecomprobante.sql");
    private final String SQL_INSERT_ASIENTO = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_InsertPrecomprobanteAsiento.sql");
    private final String SQL_INSERT_EVENTO = SqlResources.load("cl/bcs/contabilidad/sql/ReversaPreComprobante_InsertEventoPrecomprobanteAsiento.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final SecuenciaPort secuencias;
    private final RelojSistemaPort reloj;
    private final DiccionarioPort diccionario;
    private final EnvioContabilidadService envio;

    public ReversaPreComprobanteService(NamedParameterJdbcTemplate jdbc, SecuenciaPort secuencias, RelojSistemaPort reloj,
                                        DiccionarioPort diccionario, EnvioContabilidadService envio) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
        this.reloj = reloj;
        this.diccionario = diccionario;
        this.envio = envio;
    }

    // ------------------------------------------------------------------ contract

    /** One pre-voucher to reverse; {@code idPreComprobante = 0} asks for a new id. */
    public record Reversion(@SerializedName("IdPreComprobanteAnt") Long idPreComprobanteAnt,
                            @SerializedName("IdPreComprobante") Long idPreComprobante) {
        boolean nuevoId() { return Long.valueOf(0L).equals(idPreComprobante); }
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdPreComprobantes") List<Reversion> reversiones,
                   @SerializedName("p_FechaContable") String fechaContable,
                   @SerializedName("p_CodigoUsuario") String codigoUsuario) {}

    public record Resultado(@SerializedName("P_OUT_ERRORESCONTADOR") long erroresContador,
                            @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    // ------------------------------------------------------------------ rows

    private record PreComprobante(Long idPreComprobante, Long idTipoContabilidad, Long idEmpresa, Long ejercicio, String tipo,
                                  Long folioContabilidad, Long idReversa) {}

    private record Asiento(Long idPreComprobanteAsiento, Long idEmpresa, Long idPreComprobante, String idCuentaContable,
                           Long ejercicio, Long linea, BigDecimal montoDebe, BigDecimal montoHaber, String estado) {}

    private record Evento(Long idPreComprobanteAsiento, Long idEvento, String operador, Long linea, String estado,
                          String glosaDetalle) {}

    private static final org.springframework.jdbc.core.RowMapper<PreComprobante> PRECOMPROBANTE = SqlRow.mapper(r -> new PreComprobante(
            r.lng("IdPreComprobante"), r.lng("IdTipoContabilidad"), r.lng("IdEmpresa"), r.lng("Ejercicio"), r.str("Tipo"),
            r.lng("FolioContabilidad"), r.lng("IdReversa")));

    // ------------------------------------------------------------------ entry points

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada e = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), reversar(e.idEmpresa(), e.codigoUsuario(), e.fechaContable(),
                e.reversiones() == null ? List.of() : e.reversiones()));
    }

    @Transactional
    public Resultado reversar(Long idEmpresa, String codigoUsuario, String fechaContable, List<Reversion> reversiones) {
        Set<Long> vistos = new HashSet<>();
        for (Reversion r : reversiones) {
            if (!vistos.add(r.idPreComprobanteAnt())) {
                return error(idEmpresa, r.idPreComprobanteAnt(), ANTERIOR_REPETIDO);
            }
        }
        String fechaProceso = reloj.fechaProceso(idEmpresa);
        String horaProceso = reloj.horaProceso(idEmpresa);

        List<Long> anteriores = reversiones.stream().map(Reversion::idPreComprobanteAnt).distinct().toList();
        Map<Long, PreComprobante> reversados = new HashMap<>();
        for (PreComprobante p : consultar(SQL_SELECT_REVERSA, anteriores, PRECOMPROBANTE)) {
            if (reversados.putIfAbsent(p.idReversa(), p) != null) {
                return error(idEmpresa, p.idReversa(), REVERSADO_MAS_DE_UNA_VEZ);
            }
        }

        List<MensajeSalida> mensajes = new ArrayList<>();
        List<Long> aReversar = new ArrayList<>();
        long idsNuevos = 0;
        for (Reversion r : reversiones) {
            if (reversados.containsKey(r.idPreComprobanteAnt())) {
                Map<String, String> valores = Map.of("IdPreComprobanteAnt", String.valueOf(r.idPreComprobanteAnt()));
                mensajes.add(new MensajeSalida(r.idPreComprobanteAnt(), MensajeSalida.ERROR, "PRECOMPROBANTE_YA_ESTA_REVERSADO",
                        diccionario.mensaje(idEmpresa, "PRECOMPROBANTE_YA_ESTA_REVERSADO", valores)));
            } else if (!aReversar.contains(r.idPreComprobanteAnt())) {
                aReversar.add(r.idPreComprobanteAnt());
                if (r.nuevoId()) idsNuevos++; // drawn even when the id does not exist (legacy :160)
            }
        }

        List<PreComprobante> originales = consultar(SQL_SELECT_ANTERIOR, aReversar, PRECOMPROBANTE);
        List<Asiento> asientos = consultar(SQL_SELECT_ASIENTO, aReversar, SqlRow.mapper(r -> new Asiento(
                r.lng("IdPreComprobanteAsiento"), r.lng("IdEmpresa"), r.lng("IdPreComprobante"), r.str("IdCuentaContable"),
                r.lng("Ejercicio"), r.lng("Linea"), r.dec("MontoDebe"), r.dec("MontoHaber"), r.str("Estado"))));
        List<Evento> eventos = consultar(SQL_SELECT_EVENTO, aReversar, SqlRow.mapper(r -> new Evento(
                r.lng("IdPreComprobanteAsiento"), r.lng("IdEvento"), r.str("Operador"), r.lng("Linea"), r.str("Estado"),
                r.str("GlosaDetalle"))));

        Map<Long, Reversion> porAnterior = new LinkedHashMap<>();
        reversiones.forEach(r -> porAnterior.put(r.idPreComprobanteAnt(), r));
        Iterator<Long> nuevosPre = secuencias.siguientes(SEQ_PRECOMPROBANTE, idsNuevos).iterator();
        Iterator<Long> nuevosAsiento = secuencias.siguientes(SEQ_PRECOMPROBANTE_ASIENTO, asientos.size()).iterator();

        List<MapSqlParameterSource> insertPre = new ArrayList<>();
        List<MapSqlParameterSource> insertAsiento = new ArrayList<>();
        List<MapSqlParameterSource> insertEvento = new ArrayList<>();
        List<Long> creados = new ArrayList<>();
        for (PreComprobante original : originales) {
            Reversion r = porAnterior.get(original.idPreComprobante());
            Long idNuevo = r.nuevoId() ? nuevosPre.next() : r.idPreComprobante();
            String glosa = GLOSA + r.idPreComprobanteAnt();
            creados.add(idNuevo);
            insertPre.add(new MapSqlParameterSource()
                    .addValue("IdPreComprobante", idNuevo)
                    .addValue("IdTipoContabilidad", original.idTipoContabilidad())
                    .addValue("IdEmpresa", original.idEmpresa())
                    .addValue("Ejercicio", original.ejercicio())
                    .addValue("Tipo", original.tipo())
                    .addValue("FechaContable", fechaContable)
                    .addValue("Glosa", glosa)
                    .addValue("FechaProceso", fechaProceso)
                    .addValue("HoraProceso", horaProceso)
                    .addValue("CodigoUsuario", codigoUsuario)
                    .addValue("FolioContabilidad", original.folioContabilidad())
                    .addValue("IdReversa", r.idPreComprobanteAnt())
                    .addValue("Estado", "V"));
            for (Asiento a : asientos) {
                if (!Objects.equals(a.idPreComprobante(), r.idPreComprobanteAnt())) continue;
                Long idAsientoNuevo = nuevosAsiento.next();
                insertAsiento.add(new MapSqlParameterSource()
                        .addValue("IdPreComprobanteAsiento", idAsientoNuevo)
                        .addValue("IdEmpresa", a.idEmpresa())
                        .addValue("IdPreComprobante", idNuevo)
                        .addValue("IdCuentaContable", a.idCuentaContable())
                        .addValue("Ejercicio", a.ejercicio())
                        .addValue("Linea", a.linea())
                        .addValue("MontoDebe", a.montoDebe())
                        .addValue("MontoHaber", a.montoHaber())
                        .addValue("FechaContable", fechaContable)
                        .addValue("Glosa", glosa + " CUENTA N° " + a.idCuentaContable())
                        .addValue("Estado", a.estado()));
                for (Evento ev : eventos) {
                    if (!Objects.equals(ev.idPreComprobanteAsiento(), a.idPreComprobanteAsiento())) continue;
                    insertEvento.add(new MapSqlParameterSource() // the operator is copied, not flipped (legacy :221-228)
                            .addValue("IdPreComprobanteAsiento", idAsientoNuevo)
                            .addValue("IdEvento", ev.idEvento())
                            .addValue("Operador", ev.operador())
                            .addValue("Linea", ev.linea())
                            .addValue("Estado", ev.estado())
                            .addValue("GlosaDetalle", ev.glosaDetalle()));
                }
            }
        }
        jdbc.batchUpdate(SQL_INSERT_PRECOMPROBANTE, SqlParams.batch(insertPre));
        jdbc.batchUpdate(SQL_INSERT_ASIENTO, SqlParams.batch(insertAsiento));
        jdbc.batchUpdate(SQL_INSERT_EVENTO, SqlParams.batch(insertEvento));

        long errores = 0;
        if (!creados.isEmpty()) {
            EnvioContabilidadService.Resultado enviado = envio.enviar(idEmpresa, null, null, "B", "S", creados);
            errores += enviado.erroresContador();
            mensajes.addAll(enviado.mensajes());
        }
        return new Resultado(errores, mensajes);
    }

    private <T> List<T> consultar(String sql, List<Long> ids, org.springframework.jdbc.core.RowMapper<T> mapper) {
        List<Long> conValor = ids.stream().filter(Objects::nonNull).toList();
        if (conValor.isEmpty()) return List.of(); // legacy IN (NULL): nothing matches
        return jdbc.query(sql, new MapSqlParameterSource("IdPreComprobanteAnt", conValor), mapper);
    }

    private Resultado error(Long idEmpresa, Long id, String codigo) {
        Map<String, String> valores = Map.of("IdPreComprobanteAnt", String.valueOf(id));
        return new Resultado(1L, List.of(new MensajeSalida(id, MensajeSalida.ERROR, codigo, diccionario.mensaje(idEmpresa, codigo, valores))));
    }
}
