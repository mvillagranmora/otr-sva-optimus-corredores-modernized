package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds the homologated accounts whose metadata satisfies every requested filter.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.FiltrarCuentas}. Implements CONT-G-420..422. Only the first filter per
 * IdTipoDatoEvento counts; condition {@code T} accepts any value, otherwise condition and value must match
 * (case-insensitive). Every failure is reported as UI_ERROR with empty lists.
 */
@Service
public class FiltrarCuentasService {

    private static final Logger log = LoggerFactory.getLogger(FiltrarCuentasService.class);

    private final String SQL_SELECT_CUENTAS = SqlResources.load("FiltrarCuentas_SelectCuentaContable.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public FiltrarCuentasService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Filtro(@SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                  @SerializedName("Condicion") String condicion,
                  @SerializedName("Valor") String valor) {
        boolean esTodos() { return "T".equalsIgnoreCase(condicion); }

        boolean acepta(FilaCuenta f) {
            return esTodos() || (condicion.equalsIgnoreCase(f.condicion()) && valor.equalsIgnoreCase(f.valor()));
        }
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_Filtros") List<Filtro> filtros) {}

    record CuentaContable(@SerializedName("CuentaOptimus") String cuentaOptimus,
                          @SerializedName("CuentaContabilidad") String cuentaContabilidad,
                          @SerializedName("NombreCuenta") String nombreCuenta,
                          @SerializedName("DetalleCuenta") String detalleCuenta) {}

    record CampoFiltro(@SerializedName("CuentaOptimus") String cuentaOptimus,
                       @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                       @SerializedName("Condicion") String condicion,
                       @SerializedName("Valor") String valor,
                       @SerializedName("EsTodos") String esTodos) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_CUENTASCONTABLES") List<CuentaContable> cuentasContables,
                  @SerializedName("P_OUT_CAMPOSFILTROS") List<CampoFiltro> camposFiltros) {}

    private record FilaCuenta(CuentaContable cuenta, String idTipoDatoEvento, String condicion, String valor) {}

    /** Matches collected for one account. */
    private static final class Coincidencias {
        final CuentaContable cuenta;
        final Set<String> tipos = new LinkedHashSet<>();
        final List<CampoFiltro> campos = new ArrayList<>();

        Coincidencias(CuentaContable cuenta) { this.cuenta = cuenta; }
    }

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        Salida salida;
        try {
            salida = filtrar(entrada);
        } catch (RuntimeException e) {
            log.error("FiltrarCuentas error", e);
            salida = new Salida("UI_ERROR", 1L, List.of(), List.of());
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }

    private Salida filtrar(Entrada entrada) {
        Map<String, Filtro> porTipo = new LinkedHashMap<>();
        for (Filtro f : entrada.filtros()) porTipo.putIfAbsent(f.idTipoDatoEvento(), f);

        List<FilaCuenta> filas = jdbc.query(SQL_SELECT_CUENTAS,
                new MapSqlParameterSource("IdTipoDatoEvento", new ArrayList<>(porTipo.keySet())),
                SqlRow.mapper(r -> new FilaCuenta(new CuentaContable(r.str("CuentaOptimus"), r.str("CuentaContabilidad"),
                        r.str("NombreCuenta"), r.str("DetalleCuenta")), r.str("IdTipoDatoEvento"), r.str("Condicion"), r.str("Valor"))));

        Map<String, Coincidencias> porCuenta = new LinkedHashMap<>();
        for (FilaCuenta fila : filas) {
            Coincidencias c = porCuenta.computeIfAbsent(fila.cuenta().cuentaOptimus(), k -> new Coincidencias(fila.cuenta()));
            Filtro filtro = porTipo.get(fila.idTipoDatoEvento());
            if (filtro != null && filtro.acepta(fila)) {
                c.tipos.add(fila.idTipoDatoEvento());
                c.campos.add(new CampoFiltro(fila.cuenta().cuentaOptimus(), fila.idTipoDatoEvento(), fila.condicion(),
                        fila.valor(), filtro.esTodos() ? "S" : "N"));
            }
        }

        List<CuentaContable> cuentas = new ArrayList<>();
        List<CampoFiltro> campos = new ArrayList<>();
        for (Coincidencias c : porCuenta.values()) {
            if (c.tipos.size() == porTipo.size()) {
                cuentas.add(c.cuenta);
                campos.addAll(c.campos);
            }
        }
        return new Salida("UI_EXITO", 0L, cuentas, campos);
    }
}
