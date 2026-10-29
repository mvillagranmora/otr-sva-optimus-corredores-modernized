package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bulk load of voucher-level conditions (determinantes, TipoCondicion C).
 *
 * <p>Legacy {@code cl.bcs.contabilidad.CargarDeterminanteComprobante}. Implements CONT-G-240..246. Database failures
 * propagate (legacy L162-165).
 */
@Service
public class CargarDeterminanteComprobanteService {

    private final String SQL_INSERT_DETERMINANTE = SqlResources.load("CargarDeterminanteComprobante_InsertDeterminante.sql");
    private final String SQL_SELECT_COMPROBANTE = SqlResources.load("CargarDeterminanteComprobante_SelectCombrobante.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public CargarDeterminanteComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Linea(@SerializedName("CDCT_NEMOTECNICO") String nemotecnico,
                 @SerializedName("CDCT_TIPODATO") String tipoDato,
                 @SerializedName("CDCT_CONDICION") String condicion,
                 @SerializedName("CDCT_VALOR") String valor,
                 @SerializedName("MENSAJEVALIDACION") String mensajeValidacion) {
        boolean valida() { return "OK".equalsIgnoreCase(mensajeValidacion); }
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_Determinantes") List<Linea> determinantes) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    private record Comprobante(Long idTipoContabilidad, String nemo) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.plain(), cargar(entrada));
    }

    Salida cargar(Entrada entrada) {
        List<Comprobante> comprobantes = comprobantes(entrada);
        List<MapSqlParameterSource> filas = new ArrayList<>();
        List<MensajeSalida> mensajes = new ArrayList<>();

        for (Linea linea : entrada.determinantes()) {
            if (!linea.valida()) {
                mensajes.add(MensajeSalida.error(0L, "ERROR_FORMATO_LINEA", Map.of()));
                continue;
            }
            Optional<Comprobante> comprobante = comprobantes.stream()
                    .filter(c -> linea.nemotecnico().equalsIgnoreCase(c.nemo())).findFirst();
            if (comprobante.isEmpty()) {
                Map<String, String> v = new HashMap<>();
                v.put("IdTipoContabilidad", linea.nemotecnico());
                mensajes.add(MensajeSalida.advertencia(0L, "NO_EXISTE_CONTABILIDAD", v));
                continue;
            }
            if (!AgregarMetadataService.CONDICIONES_VALIDAS.contains(linea.condicion())) {
                Map<String, String> v = new HashMap<>();
                v.put("Condicion", linea.condicion());
                v.put("IdTipoContabilidad", linea.nemotecnico());
                mensajes.add(MensajeSalida.advertencia(0L, "CONDICION_NO_VALIDA_DETERMINANTE", v));
                continue;
            }
            filas.add(new MapSqlParameterSource()
                    .addValue("IdDefinicionAsiento", 0L)
                    .addValue("IdTipoContabilidad", comprobante.get().idTipoContabilidad())
                    .addValue("IdTipoDatoEvento", linea.tipoDato().trim())
                    .addValue("ValorVarchar", linea.valor().trim())
                    .addValue("ValorDecimal", decimalOrZero(linea.valor()))
                    .addValue("Condicion", linea.condicion())
                    .addValue("Conector", "AND")
                    .addValue("TipoCondicion", "C")
                    .addValue("Estado", "V"));
        }
        jdbc.batchUpdate(SQL_INSERT_DETERMINANTE, SqlParams.batch(filas));
        // Legacy resets counter and message after the loop (L159-160).
        return new Salida("UI_EXITO", 0L, mensajes);
    }

    private List<Comprobante> comprobantes(Entrada entrada) {
        List<String> nemos = entrada.determinantes().stream().filter(Linea::valida).map(Linea::nemotecnico).toList();
        if (nemos.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_COMPROBANTE,
                new MapSqlParameterSource().addValue("nemos", nemos).addValue("IdEmpresa", entrada.idEmpresa()),
                SqlRow.mapper(r -> new Comprobante(r.lng("IdTipoContabilidad"), r.str("Nemo"))));
    }

    /** Legacy parses the untrimmed value (L131-137). */
    private static BigDecimal decimalOrZero(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }
}
