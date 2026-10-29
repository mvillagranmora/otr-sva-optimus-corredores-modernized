package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Bulk load of accounting entry definitions (asientos) for existing vouchers.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.CargarAsientoComprobante}. Implements CONT-G-220..230. Each OK line names a
 * voucher (nemotecnico) and either an account or a metadata ({@code &CODE&}); events are {@code +EVT|-EVT}, conditions
 * {@code Campo#Operador#Valor|...}. A metadata line is delegated to {@link AgregarCuentasPorMetadataService}.
 * Database failures propagate (legacy L320-323).
 */
@Service
public class CargarAsientoComprobanteService {

    private final String SQL_SELECT_COMPROBANTE = SqlResources.load("CargarAsientoComprobante_SelectCombrobante.sql");
    private final String SQL_SELECT_ID_CUENTA = SqlResources.load("CargarAsientoComprobante_SelectIdCuentaContable.sql");
    private final String SQL_SELECT_METADATA = SqlResources.load("CargarAsientoComprobante_SelectMetadata.sql");
    private final String SQL_INSERT_ASIENTO = SqlResources.load("CargarAsientoComprobante_InsertContabilidadDefinicionAsiento.sql");
    private final String SQL_INSERT_TIPO_EVENTO = SqlResources.load("CargarAsientoComprobante_InsertContabilidadDefinicionTipoEvento.sql");
    private final String SQL_INSERT_ASIENTO_COND = SqlResources.load("CargarAsientoComprobante_InsertContabilidadTipoAsientoCondicion.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final AgregarCuentasPorMetadataService agregarCuentasPorMetadata;

    @Autowired
    public CargarAsientoComprobanteService(NamedParameterJdbcTemplate jdbc,
                                           AgregarCuentasPorMetadataService agregarCuentasPorMetadata) {
        this.jdbc = jdbc;
        this.agregarCuentasPorMetadata = agregarCuentasPorMetadata;
    }

    public CargarAsientoComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, new AgregarCuentasPorMetadataService(jdbc));
    }

    record Linea(@SerializedName("CACT_CONDICIONES") String condiciones,
                 @SerializedName("CACT_CUENTAOMETADATA") String cuentaOMetadata,
                 @SerializedName("CACT_DEBEHABER") String debeHaber,
                 @SerializedName("CACT_EVENTOS") String eventos,
                 @SerializedName("CACT_FORMATO") String formato,
                 @SerializedName("CACT_NEMOTECNICO") String nemotecnico,
                 @SerializedName("MENSAJEVALIDACION") String mensajeValidacion) {
        boolean valida() { return "OK".equalsIgnoreCase(mensajeValidacion); }
        boolean esMetadata() { return cuentaOMetadata.startsWith("&"); }
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_Asientos") List<Linea> asientos) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    private record Comprobante(Long idTipoContabilidad, String nemo, Long ejercicio) {}

    /** {@code +EVT_A}: operator is the first character, event the trimmed rest. */
    private record EventoLinea(String operador, String idTipoEvento) {
        static EventoLinea parse(String texto) {
            return new EventoLinea(texto.substring(0, 1), texto.substring(1).trim());
        }
    }

    /** Accumulates the rows of one load so they are written in three batches at the end (legacy L310-318). */
    private static final class Lote {
        final List<MapSqlParameterSource> asientos = new ArrayList<>();
        final List<MapSqlParameterSource> eventos = new ArrayList<>();
        final List<MapSqlParameterSource> condiciones = new ArrayList<>();
        final Map<String, Long> ordenPorNemo = new HashMap<>();
        final List<MensajeSalida> mensajes = new ArrayList<>();

        void advertencia(String codigo, Map<String, String> valores) {
            mensajes.add(MensajeSalida.advertencia(0L, codigo, valores));
        }
    }

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.plain(), cargar(entrada));
    }

    Salida cargar(Entrada entrada) {
        List<Comprobante> comprobantes = comprobantes(entrada);
        Lote lote = new Lote();
        for (Linea linea : entrada.asientos()) {
            if (!linea.valida() || linea.cuentaOMetadata() == null || linea.cuentaOMetadata().isEmpty()) continue;
            if (linea.esMetadata()) cargarMetadata(entrada, linea, comprobantes, lote);
            else cargarCuenta(linea, comprobantes, lote);
        }
        if (!lote.asientos.isEmpty()) {
            jdbc.batchUpdate(SQL_INSERT_ASIENTO, SqlParams.batch(lote.asientos));
            if (!lote.eventos.isEmpty()) jdbc.batchUpdate(SQL_INSERT_TIPO_EVENTO, SqlParams.batch(lote.eventos));
            if (!lote.condiciones.isEmpty()) jdbc.batchUpdate(SQL_INSERT_ASIENTO_COND, SqlParams.batch(lote.condiciones));
        }
        return new Salida("UI_EXITO", 0L, lote.mensajes);
    }

    private List<Comprobante> comprobantes(Entrada entrada) {
        List<String> nemos = entrada.asientos().stream().filter(Linea::valida).map(Linea::nemotecnico).toList();
        if (nemos.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_COMPROBANTE,
                new MapSqlParameterSource().addValue("nemos", nemos).addValue("IdEmpresa", entrada.idEmpresa()),
                SqlRow.mapper(r -> new Comprobante(r.lng("IdTipoContabilidad"), r.str("Nemo"), r.lng("Ejercicio"))));
    }

    private static Optional<Comprobante> comprobante(List<Comprobante> comprobantes, String nemo) {
        return comprobantes.stream().filter(c -> nemo.equalsIgnoreCase(c.nemo())).findFirst();
    }

    private void cargarMetadata(Entrada entrada, Linea linea, List<Comprobante> comprobantes, Lote lote) {
        String texto = linea.cuentaOMetadata();
        if (texto.length() <= 2) return;
        String metadata = texto.substring(1, texto.length() - 1);
        List<Long> ids = jdbc.query(SQL_SELECT_METADATA, new MapSqlParameterSource("CodigoMetadata", metadata),
                SqlRow.mapper(r -> r.lng("Id")));
        if (ids.isEmpty()) {
            lote.advertencia("NO_EXISTE_METADATA", valores("IdMetadata", metadata));
            return;
        }
        Optional<Comprobante> comprobante = comprobante(comprobantes, linea.nemotecnico());
        if (comprobante.isEmpty()) {
            lote.advertencia("NO_EXISTE_CONTABILIDAD", valores("IdTipoContabilidad", linea.nemotecnico()));
            return;
        }
        List<AgregarCuentasPorMetadataService.Evento> eventos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (String parte : linea.eventos().split("\\|")) {
            if (parte.isEmpty()) continue;
            EventoLinea e = EventoLinea.parse(parte);
            if (vistos.add(e.idTipoEvento())) {
                eventos.add(new AgregarCuentasPorMetadataService.Evento(e.idTipoEvento(), e.operador()));
            } else {
                lote.advertencia("YA_EXISTE_EVENTO_ASIENTO", valores("AsientoEventoCuentaContable",
                        linea.nemotecnico() + "-" + metadata + "-" + e.idTipoEvento()));
            }
        }
        // Legacy ignores the delegate's result (L219).
        agregarCuentasPorMetadata.agregar(new AgregarCuentasPorMetadataService.Entrada(entrada.idEmpresa(),
                entrada.idUsuario(), entrada.idModulo(), ids.get(0), eventos, linea.debeHaber(),
                String.valueOf(comprobante.get().ejercicio()), linea.formato(), comprobante.get().idTipoContabilidad(), null));
    }

    private void cargarCuenta(Linea linea, List<Comprobante> comprobantes, Lote lote) {
        // Legacy draws the id before checking the voucher exists (L224 before L233).
        Long idDefinicionAsiento = jdbc.query(SQL_SELECT_ID_CUENTA, new MapSqlParameterSource(),
                SqlRow.mapper(r -> r.lng("Id"))).get(0);
        Optional<Comprobante> encontrado = comprobante(comprobantes, linea.nemotecnico());
        if (encontrado.isEmpty()) {
            lote.advertencia("NO_EXISTE_CONTABILIDAD", valores("IdTipoContabilidad", linea.nemotecnico()));
            return;
        }
        Comprobante comprobante = encontrado.get();
        String cuenta = linea.cuentaOMetadata();
        long orden = lote.ordenPorNemo.merge(linea.nemotecnico(), 1L, Long::sum);
        String[] eventos = linea.eventos() != null ? linea.eventos().split("\\|") : new String[0];

        lote.asientos.add(new MapSqlParameterSource()
                .addValue("IdDefinicionAsiento", idDefinicionAsiento)
                .addValue("IdTipoContabilidad", comprobante.idTipoContabilidad())
                .addValue("IdCuentaContable", cuenta)
                .addValue("Ejercicio", comprobante.ejercicio())
                .addValue("Orden", orden)
                .addValue("DebeHaber", linea.debeHaber())
                .addValue("TipoEvento", eventos.length > 1 ? "C" : "S")
                .addValue("Condicionado", "S")
                .addValue("OmiteCondicionCmbte", "N")
                .addValue("Estado", "V")
                .addValue("Formato", linea.formato())
                .addValue("IdMetadataCuentaContable", null));

        Set<String> vistos = new HashSet<>();
        for (String texto : eventos) {
            if (texto.isEmpty()) continue;
            EventoLinea e = EventoLinea.parse(texto);
            if (vistos.add(e.idTipoEvento())) {
                lote.eventos.add(new MapSqlParameterSource()
                        .addValue("IdTipoEvento", e.idTipoEvento())
                        .addValue("IdDefinicionAsiento", idDefinicionAsiento)
                        .addValue("Operador", e.operador())
                        .addValue("Estado", "V"));
            } else {
                lote.advertencia("YA_EXISTE_EVENTO_ASIENTO", valores("AsientoEventoCuentaContable",
                        linea.nemotecnico() + "-" + cuenta + "-" + e.idTipoEvento()));
            }
        }

        String[] condiciones = linea.condiciones() != null ? linea.condiciones().split("\\|") : new String[0];
        for (String texto : condiciones) {
            String[] partes = texto.split("#");
            if (partes.length != 3) continue;
            String operador = partes[1];
            if (!AgregarMetadataService.CONDICIONES_VALIDAS.contains(operador)) {
                Map<String, String> v = valores("Condicion", operador);
                v.put("IdTipoContabilidad", linea.nemotecnico());
                v.put("Cuenta", cuenta);
                lote.advertencia("CONDICION_NO_VALIDA_DETERMINANTE_ASIENTO", v);
                continue;
            }
            lote.condiciones.add(new MapSqlParameterSource()
                    .addValue("IdDefinicionAsiento", idDefinicionAsiento)
                    .addValue("IdTipoContabilidad", comprobante.idTipoContabilidad())
                    .addValue("IdTipoDatoEvento", partes[0].trim())
                    .addValue("ValorVarchar", partes[2].trim())
                    .addValue("ValorDecimal", decimalOrZero(partes[2]))
                    .addValue("Condicion", operador)
                    .addValue("Conector", "AND")
                    .addValue("TipoCondicion", "A")
                    .addValue("Estado", "V")
                    .addValue("OmiteCondicionCmbte", ""));
        }
    }

    private static Map<String, String> valores(String clave, String valor) {
        Map<String, String> m = new HashMap<>();
        m.put(clave, valor);
        return m;
    }

    private static BigDecimal decimalOrZero(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }
}
