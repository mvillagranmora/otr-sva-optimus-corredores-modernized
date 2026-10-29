package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * {@link ConfiguracionSistemaPort} over the legacy SQL (the 14 {@code ConsultarConfiguracion<MODULO>.sql}, verbatim): each call reads the WHOLE table of
 * the module, as the legacy did with its cache switched off, and picks {@code (IdEmpresa, CodigoParametro)}. Nothing is kept between calls (DEV-F2:
 * the legacy kept a JVM-wide static map). The SQL files are loaded once at class initialisation and fail the start-up when one is missing.
 */
@Component
public class ConfiguracionSistemaJdbc implements ConfiguracionSistemaPort {

    /** Row of the legacy query ({@code ConfiguracionSistema.RegistroConfiguracion}). */
    public static class RegistroConfiguracion {
        public Long IdEmpresa;
        public String CodigoParametro;
        public String Valor;
    }

    private static final Map<String, String> SQLS = cargar();

    private final ConexionUnidadDeTrabajo conexion;

    public ConfiguracionSistemaJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    private static Map<String, String> cargar() {
        Map<String, String> m = new HashMap<>();
        m.put("PLATAFORMA", SqlRecursos.kit("ConsultarConfiguracionPlataforma.sql"));
        m.put("ORDENES", SqlRecursos.kit("ConsultarConfiguracionOrdenes.sql"));
        m.put("CUENTAINVERSION", SqlRecursos.kit("ConsultarConfiguracionCuentaInversion.sql"));
        m.put("TESORERIA", SqlRecursos.kit("ConsultarConfiguracionTesoreria.sql"));
        m.put("FACTURACION", SqlRecursos.kit("ConsultarConfiguracionFacturacion.sql"));
        m.put("SOCIODENEGOCIO", SqlRecursos.kit("ConsultarConfiguracionSocioDeNegocio.sql"));
        m.put("CUSTODIA", SqlRecursos.kit("ConsultarConfiguracionCustodia.sql"));
        m.put("APV", SqlRecursos.kit("ConsultarConfiguracionAPV.sql"));
        m.put("CONTABILIDAD", SqlRecursos.kit("ConsultarConfiguracionContabilidad.sql"));
        m.put("FONDOMUTUO", SqlRecursos.kit("ConsultarConfiguracionFondoMutuo.sql"));
        m.put("NORMATIVA", SqlRecursos.kit("ConsultarConfiguracionNormativa.sql"));
        m.put("MESA", SqlRecursos.kit("ConsultarConfiguracionMesa.sql"));
        m.put("PRODUCTO", SqlRecursos.kit("ConsultarConfiguracionProducto.sql"));
        m.put("LIMITE", SqlRecursos.kit("ConsultarConfiguracionLimite.sql"));
        return Map.copyOf(m);
    }

    @Override
    public String texto(Long idEmpresa, String modulo, String nombreParametro) {
        return leer(idEmpresa, modulo, nombreParametro);
    }

    @Override
    public long entero(Long idEmpresa, String modulo, String nombreParametro) {
        return Long.parseLong(leer(idEmpresa, modulo, nombreParametro));
    }

    @Override
    public BigDecimal decimal(Long idEmpresa, String modulo, String nombreParametro) {
        return new BigDecimal(leer(idEmpresa, modulo, nombreParametro));
    }

    @Override
    public Set<String> conjunto(Long idEmpresa, String modulo, String nombreParametro, String delimitador) {
        return Arrays.stream(leer(idEmpresa, modulo, nombreParametro).split(delimitador)).collect(Collectors.toSet());
    }

    private String leer(Long idEmpresa, String modulo, String nombre) {
        String sql = SQLS.get(modulo);
        if (sql == null) {
            throw new IllegalArgumentException("Configuracion modulo " + modulo + " no encontrada");
        }
        String[] hallado = conexion.usar(con -> {
            String[] r = null;
            for (RegistroConfiguracion reg : SqlNombrado.consultar(con, sql, new HashMap<>(), RegistroConfiguracion.class)) {
                if (idEmpresa != null && idEmpresa.equals(reg.IdEmpresa) && nombre.equals(reg.CodigoParametro)) {
                    r = new String[] {reg.Valor};
                }
            }
            return r;
        });
        if (hallado == null) {
            throw new ParametroNoEncontradoException(nombre, modulo);
        }
        return hallado[0];
    }
}
