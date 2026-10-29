package cl.bcs.cuentainversion.reportes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read access to the per-company configuration parameters of the platform modules (legacy {@code ConfiguracionSistema}): one
 * table per module, rows {@code (IdEmpresa, CodigoParametro, Valor)}. Not cached (the legacy cache was disabled too).
 */
public final class ConfiguracionSistema {

    public static final String PLATAFORMA = "PLATAFORMA";
    public static final String CUENTAINVERSION = "CUENTAINVERSION";
    public static final String CUSTODIA = "CUSTODIA";
    public static final String SOCIODENEGOCIO = "SOCIODENEGOCIO";
    public static final String FACTURACION = "FACTURACION";

    private static final Map<String, String> CONSULTAS = Map.ofEntries(
            Map.entry("PLATAFORMA", "plataforma/ConsultarConfiguracionPlataforma"),
            Map.entry("ORDENES", "plataforma/ConsultarConfiguracionOrdenes"),
            Map.entry("CUENTAINVERSION", "plataforma/ConsultarConfiguracionCuentaInversion"),
            Map.entry("TESORERIA", "plataforma/ConsultarConfiguracionTesoreria"),
            Map.entry("FACTURACION", "plataforma/ConsultarConfiguracionFacturacion"),
            Map.entry("SOCIODENEGOCIO", "plataforma/ConsultarConfiguracionSocioDeNegocio"),
            Map.entry("CUSTODIA", "plataforma/ConsultarConfiguracionCustodia"),
            Map.entry("APV", "plataforma/ConsultarConfiguracionAPV"),
            Map.entry("CONTABILIDAD", "plataforma/ConsultarConfiguracionContabilidad"),
            Map.entry("FONDOMUTUO", "plataforma/ConsultarConfiguracionFondoMutuo"),
            Map.entry("NORMATIVA", "plataforma/ConsultarConfiguracionNormativa"),
            Map.entry("MESA", "plataforma/ConsultarConfiguracionMesa"),
            Map.entry("PRODUCTO", "plataforma/ConsultarConfiguracionProducto"),
            Map.entry("LIMITE", "plataforma/ConsultarConfiguracionLimite"));

    /** The module or the parameter does not exist for the company (legacy: a plain {@code Exception}). */
    public static final class NoEncontradaException extends Exception {
        public NoEncontradaException(String mensaje) {
            super(mensaje);
        }
    }

    public static final class Registro {
        public Long IdEmpresa;
        public String CodigoParametro;
        public String Valor;
    }

    private final SqlReportes sql;

    public ConfiguracionSistema(SqlReportes sql) {
        this.sql = sql;
    }

    public String cadena(Long idEmpresa, String modulo, String parametro) throws NoEncontradaException {
        String consulta = CONSULTAS.get(modulo);
        if (consulta == null) {
            throw new NoEncontradaException("Configuracion modulo " + modulo + " no encontrada");
        }
        Map<String, String> valores = new HashMap<>();
        for (Registro r : sql.consultar(SqlReportes.recurso(consulta), Registro.class)) {
            if (Objects.equals(r.IdEmpresa, idEmpresa)) {
                valores.put(r.CodigoParametro, r.Valor);
            }
        }
        if (!valores.containsKey(parametro)) {
            throw new NoEncontradaException("Parametro " + parametro + " en modulo " + modulo + " no encontrado");
        }
        return valores.get(parametro);
    }

    public long entero(Long idEmpresa, String modulo, String parametro) throws NoEncontradaException {
        return Long.parseLong(cadena(idEmpresa, modulo, parametro));
    }

    public BigDecimal decimal(Long idEmpresa, String modulo, String parametro) throws NoEncontradaException {
        return new BigDecimal(cadena(idEmpresa, modulo, parametro));
    }

    public Set<String> conjunto(Long idEmpresa, String modulo, String parametro, String delimitador) throws NoEncontradaException {
        return Arrays.stream(cadena(idEmpresa, modulo, parametro).split(delimitador)).collect(Collectors.toSet());
    }

    /** {@code PLA.FechaSistema} of company 1 (the legacy services read the system date of company 1 only). */
    public String fechaSistema() throws NoEncontradaException {
        return cadena(1L, PLATAFORMA, "PLA.FechaSistema");
    }
}
