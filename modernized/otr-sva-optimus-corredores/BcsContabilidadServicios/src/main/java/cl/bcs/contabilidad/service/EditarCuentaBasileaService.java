package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Changes the account of a Basilea interface concept, after checking the account is homologated with that name.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.EditarCuentaBasilea}. Implements CONT-G-400..406. Only {@code p_Accion = GUARDAR}
 * does anything; any other action answers counter 1 with no message. {@code FechaSistema} is the platform's
 * PLA.FechaSistema (through {@link PlataformaPort}; today's date when no platform is wired). Not ported: the AgregarLog
 * audit entry (legacy L122-130, platform module).
 */
@Service
public class EditarCuentaBasileaService {

    private final String SQL_SELECT_CUENTA = SqlResources.load("EditarCuentaBasilea_SelectCuentaContableCliente.sql");
    private final String SQL_SELECT_CUENTA_ACTUAL = SqlResources.load("EditarCuentaBasilea_SelectCuentaContableClienteActual.sql");
    private final String SQL_UPDATE_CUENTA = SqlResources.load("EditarCuentaBasilea_UpdateCuentaBasilea.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final PlataformaPort plataforma;
    private final Clock clock;

    @Autowired
    public EditarCuentaBasileaService(NamedParameterJdbcTemplate jdbc, ObjectProvider<PlataformaPort> plataforma) {
        this(jdbc, plataforma.getIfAvailable(), Clock.systemDefaultZone());
    }

    public EditarCuentaBasileaService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, null, Clock.systemDefaultZone());
    }

    EditarCuentaBasileaService(NamedParameterJdbcTemplate jdbc, PlataformaPort plataforma, Clock clock) {
        this.jdbc = jdbc;
        this.plataforma = plataforma;
        this.clock = clock;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_Accion") String accion,
                   @SerializedName("p_Concepto") String concepto,
                   @SerializedName("p_TipoCuentaContable") String tipoCuentaContable,
                   @SerializedName("p_CuentaContable") String cuentaContable,
                   @SerializedName("p_NombreCuentaContable") String nombreCuentaContable) {}

    record CuentaContable(@SerializedName("IdUsuario") Long idUsuario,
                          @SerializedName("CuentaContable") String cuentaContable,
                          @SerializedName("NombreCuentaContable") String nombreCuentaContable,
                          @SerializedName("Concepto") String concepto,
                          @SerializedName("TipoCuentaContable") String tipoCuentaContable,
                          @SerializedName("FechaSistema") String fechaSistema) {
        MapSqlParameterSource params() {
            return new MapSqlParameterSource()
                    .addValue("CuentaContable", cuentaContable)
                    .addValue("NombreCuentaContable", nombreCuentaContable)
                    .addValue("IdUsuario", idUsuario)
                    .addValue("FechaSistema", fechaSistema)
                    .addValue("Concepto", concepto)
                    .addValue("TipoCuentaContable", tipoCuentaContable);
        }
    }

    record Salida(@SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes,
                  @SerializedName("CuentaContable") CuentaContable cuentaContable) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        Salida salida = "GUARDAR".equals(entrada.accion()) ? guardar(entrada) : new Salida(1L, new ArrayList<>(), null);
        return JavaCallJson.write(JavaCallJson.legacy(), salida);
    }

    private Salida guardar(Entrada entrada) {
        String fechaSistema = fechaSistema(entrada.idEmpresa());
        List<CuentaContable> cuentas = jdbc.query(SQL_SELECT_CUENTA,
                new MapSqlParameterSource("CuentaContabilidad", entrada.cuentaContable()),
                SqlRow.mapper(r -> new CuentaContable(entrada.idUsuario(), r.str("CuentaContable"), r.str("NombreCuentaContable"),
                        entrada.concepto(), entrada.tipoCuentaContable(), fechaSistema)));
        // Current values, read for the audit entry (legacy L105-107).
        jdbc.queryForList(SQL_SELECT_CUENTA_ACTUAL, new MapSqlParameterSource()
                .addValue("IdConcepto", entrada.concepto())
                .addValue("TipoCuentaContable", entrada.tipoCuentaContable()));

        String error = validar(entrada, cuentas);
        if (error != null) {
            Map<String, String> v = new HashMap<>();
            v.put("CuentaContable", entrada.cuentaContable());
            return new Salida(1L, List.of(MensajeSalida.error(0L, error, v)), null);
        }
        CuentaContable cuenta = cuentas.get(0);
        jdbc.update(SQL_UPDATE_CUENTA, cuenta.params());
        return new Salida(0L, new ArrayList<>(), cuenta);
    }

    /** Legacy {@code validaCuentaContable} (L134-170): the error code, or null when the account can be saved. */
    private static String validar(Entrada entrada, List<CuentaContable> cuentas) {
        if (cuentas.isEmpty()) return "NO_EXISTE_CUENTA_CONTABLE";
        if (cuentas.size() > 1) return "UI_ERROR_EDITAR_CUENTA_CONTABLE_BASILEA";
        String nombre = cuentas.get(0).nombreCuentaContable();
        if ("NO HOMOLOGADO".equals(nombre)) return "NO_EXISTE_CUENTA_CONTABLE_HOMOLOGADA";
        if (!entrada.nombreCuentaContable().equals(nombre)) return "CUENTA_CONTABLE_ERROR_NOMBRE";
        return null;
    }

    private String fechaSistema(Long idEmpresa) {
        String fecha = plataforma == null ? null : plataforma.obtenerFechaSistema(idEmpresa);
        return fecha != null ? fecha : LocalDate.now(clock).toString();
    }
}
