package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Jdbc;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Rebuilds the local account homologation table (ITZ.HomologaCuenta) from the SAP B1 chart of accounts (OACT).
 *
 * <p>Legacy {@code cl.bcs.contabilidad.HomologarCuentasContables}. Implements CONT-G-180..185. The SAP B1 HANA database
 * is read through the module's shared SAP B1 pool ({@link SapB1Jdbc}, {@code SAP_B1_DB_*}); the company schema replaces {@code :EMPRESA} in the legacy statement
 * and comes from {@code sap.b1.company} / env {@code SAP_B1_COMPANY} (legacy B1_COMPANY in interfazSAP.properties).
 * Without any SAP wiring the legacy "configuration not found" error is answered; missing SAP_B1_DB_* settings and
 * database failures propagate, so the JavaCall unit of work rolls back.
 * Not ported: the InsertarLogServicio audit row (legacy L177-196, platform module).
 */
@Service
public class HomologarCuentasContablesService {

    /** Used when no company is configured: an invalid schema that makes the query fail loudly, never a real one. */
    static final String COMPANIA_NO_CONFIGURADA = "SAP_B1_COMPANY_NO_CONFIGURADO";
    private static final Pattern IDENTIFICADOR = Pattern.compile("[A-Za-z0-9_]+");
    private static final Set<Integer> GRUPOS_ACTIVO = Set.of(1, 5, 6, 8);
    private static final Set<Integer> GRUPOS_PASIVO = Set.of(2, 3, 4, 7);
    private static final int LARGO_NOMBRE = 70;

    private final String SQL_SELECT_SAP = SqlResources.load("HomologarCuentasContables_SelectCuentasContables.sql");
    private final String SQL_DELETE_LOCAL = SqlResources.load("HomologarCuentasContables_DeleteCuentasContables.sql");
    private final String SQL_INSERT_LOCAL = SqlResources.load("HomologarCuentasContables_InsertCuentasContables.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private Supplier<NamedParameterJdbcTemplate> sapJdbcProvider;
    private String compania;

    public HomologarCuentasContablesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        String env = System.getenv("SAP_B1_COMPANY");
        this.compania = env == null || env.isBlank() ? COMPANIA_NO_CONFIGURADA : env;
    }

    /** Production wiring: the module's single SAP B1 pool, opened lazily on the first homologation. */
    @Autowired
    void setSapB1Jdbc(SapB1Jdbc sapB1Jdbc) {
        this.sapJdbcProvider = sapB1Jdbc;
    }

    /** Tests and manual wiring: a ready SAP B1 template. */
    public void setSapJdbc(NamedParameterJdbcTemplate sapJdbc) {
        this.sapJdbcProvider = () -> sapJdbc;
    }

    @Autowired
    void setCompania(@Value("${sap.b1.company:${SAP_B1_COMPANY:" + COMPANIA_NO_CONFIGURADA + "}}") String compania) {
        this.compania = compania;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_FechaContable") String fechaContable) {}

    record Salida(@SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    /** One OACT row of SAP B1. */
    private record CuentaSap(String acctCode, String acctName, String segment0, Integer groupMask, String fatherNum) {}

    private record CuentaHomologada(String cuentaOptimus, String codigoContabilidad, String nombre, String detalle,
                                    String tipoCuenta, String imputable) {
        MapSqlParameterSource params() {
            return new MapSqlParameterSource()
                    .addValue("CuentaOptimus", cuentaOptimus)
                    .addValue("CuentaContabilidad", cuentaOptimus)
                    .addValue("CodigoContabilidad", codigoContabilidad)
                    .addValue("CuentaProveedor", "")
                    .addValue("CuentaEEFF", "")
                    .addValue("CentroCosto", "")
                    .addValue("NombreCuenta", nombre)
                    .addValue("DetalleCuenta", detalle)
                    .addValue("Imputable", imputable)
                    .addValue("TipoCuenta", tipoCuenta);
        }
    }

    public JsonObject execute(JsonObject params) {
        JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        Salida salida = homologar();
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }

    Salida homologar() {
        if (sapJdbcProvider == null) {
            return new Salida(1L, List.of(new MensajeSalida(0L, MensajeSalida.ERROR, "ARCHIVO_CONFIGURACION_NO_ENCONTRADO",
                    "Archivo de configuracion no encontrado")));
        }
        if (!IDENTIFICADOR.matcher(compania).matches()) {
            throw new IllegalStateException("Invalid SAP B1 company schema: " + compania);
        }
        NamedParameterJdbcTemplate sapJdbc = sapJdbcProvider.get();
        List<CuentaSap> cuentas = sapJdbc.query(SQL_SELECT_SAP.replace(":EMPRESA", compania), new MapSqlParameterSource(),
                SqlRow.mapper(r -> new CuentaSap(r.str("AcctCode"), r.str("AcctName"), r.str("Segment_0"),
                        r.integer("GroupMask"), r.str("FatherNum"))));
        Set<String> padres = cuentas.stream().map(CuentaSap::fatherNum).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        List<CuentaHomologada> homologadas = cuentas.stream().map(c -> homologar(c, padres)).toList();

        // Legacy wipes the table with a parameterless DELETE executed once even for an empty chart (SQL.java:599-601).
        jdbc.update(SQL_DELETE_LOCAL, new MapSqlParameterSource());
        jdbc.batchUpdate(SQL_INSERT_LOCAL, SqlParams.batch(homologadas, CuentaHomologada::params));

        return new Salida(0L, List.of(new MensajeSalida(0L, MensajeSalida.INFORMACION, "CUENTAS_CONTABLES_ACTUALIZADAS",
                "Cuentas contables actualizadas")));
    }

    private static CuentaHomologada homologar(CuentaSap c, Set<String> padres) {
        String cuenta = c.segment0() == null || c.segment0().trim().isEmpty() ? c.acctCode() : c.segment0();
        return new CuentaHomologada(cuenta, c.acctCode(), recortar(c.acctName(), "CUENTA SIN NOMBRE"),
                recortar(c.acctName(), "CUENTA SIN DETALLE"), tipoCuenta(c.groupMask()),
                padres.contains(c.acctCode()) ? "N" : "S");
    }

    /** SAP group: activo (1, 5, 6, 8) -> A, pasivo (2, 3, 4, 7) -> P, otherwise empty (legacy L133-161). */
    private static String tipoCuenta(Integer groupMask) {
        if (groupMask == null) return "";
        if (GRUPOS_ACTIVO.contains(groupMask)) return "A";
        if (GRUPOS_PASIVO.contains(groupMask)) return "P";
        return "";
    }

    private static String recortar(String texto, String siVacio) {
        if (texto == null) return siVacio;
        return texto.length() > LARGO_NOMBRE ? texto.substring(0, LARGO_NOMBRE) : texto;
    }
}
