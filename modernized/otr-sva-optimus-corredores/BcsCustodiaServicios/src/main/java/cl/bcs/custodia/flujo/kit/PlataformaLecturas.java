package cl.bcs.custodia.flujo.kit;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.custodia.flujo.jdbc.SqlNombrado;
import cl.bcs.custodia.flujo.jdbc.SqlRecursos;

/**
 * The Plataforma reads ConsultarFlujoCustodia depends on, over the legacy SQL verbatim and the connection of the caller (SELECT only, no state kept
 * between calls): {@code ConsultarEmpresa}, {@code ConfiguracionSistema.getParametroConfiguracionString}, {@code Diccionario.getMensaje} and the
 * holiday table of {@code ObtenerDiaSiguiente}. CUS-FLU-02, CUS-FLU-04, CUS-FLU-05, CUS-FLU-06.
 */
public final class PlataformaLecturas {

    /** Row of the legacy configuration query ({@code ConfiguracionSistema.RegistroConfiguracion}). */
    public static class RegistroConfiguracion {
        public Long IdEmpresa;
        public String CodigoParametro;
        public String Valor;
    }

    /** Row of the legacy dictionary query ({@code Diccionario.RegistroDiccionario}). */
    public static class RegistroDiccionario {
        public String CodLenguaje;
        public String CodDiccionario;
        public String Descripcion;
    }

    private static final String SQL_EMPRESA = SqlRecursos.kit("ConsultarEmpresa_Select.sql");
    private static final String SQL_CONFIG_PLATAFORMA = SqlRecursos.kit("ConsultarConfiguracionPlataforma.sql");
    private static final String SQL_CONFIG_CUSTODIA = SqlRecursos.kit("ConsultarConfiguracionCustodia.sql");
    private static final String SQL_DICCIONARIO = SqlRecursos.kit("ConsultarDiccionario.sql");
    private static final String SQL_FERIADOS = SqlRecursos.kit("ConsultarFeriados.sql");
    private static final String SEPARADOR = "::";
    private static final String SIN_TRADUCCION = "sin traduccion";

    private PlataformaLecturas() {
    }

    /** Legacy {@code ConsultarEmpresa.logica}: no row is {@code Exception("Empresa no existe")}. */
    public static InformacionEmpresa empresa(Connection con, Long idEmpresa) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        List<InformacionEmpresa> empresas = SqlNombrado.consultar(con, SQL_EMPRESA, params, InformacionEmpresa.class);
        if (empresas.isEmpty()) {
            throw new Exception("Empresa no existe");
        }
        return empresas.get(0);
    }

    /**
     * Legacy {@code ConfiguracionSistema.getParametroConfiguracionString}, cache off: the whole table of the module is read; a module with no row for the
     * company is a {@link NullPointerException} (the legacy dereferenced a missing map), a missing parameter {@code Exception("Parametro ... no encontrado")}.
     */
    public static String parametro(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        String sql = switch (modulo) {
            case "PLATAFORMA" -> SQL_CONFIG_PLATAFORMA;
            case "CUSTODIA" -> SQL_CONFIG_CUSTODIA;
            default -> throw new Exception("Configuracion modulo " + modulo + " no encontrada");
        };
        Map<String, Map<String, String>> registros = new HashMap<>();
        for (RegistroConfiguracion reg : SqlNombrado.consultar(con, sql, new HashMap<>(), RegistroConfiguracion.class)) {
            registros.computeIfAbsent(modulo + "-" + reg.IdEmpresa, k -> new HashMap<>()).put(reg.CodigoParametro, reg.Valor);
        }
        Map<String, String> deLaEmpresa = registros.get(modulo + "-" + idEmpresa);
        if (!deLaEmpresa.containsKey(nombreParametro)) {
            throw new Exception("Parametro " + nombreParametro + " en modulo " + modulo + " no encontrado");
        }
        return deLaEmpresa.get(nombreParametro);
    }

    /** Legacy {@code Diccionario.getMensaje(con, idEmpresa, codigo, valores)}: no entry, or no values map, is "{codigo} sin traduccion". */
    public static String mensaje(Connection con, Long idEmpresa, String codigo, Map<String, String> valores) throws Exception {
        Map<String, Object> parms = new HashMap<>();
        parms.put("IdEmpresa", idEmpresa);
        parms.put("CodigoParametro", "PLA.LocalizacionSistema");
        RegistroDiccionario registro = null;
        for (RegistroDiccionario r : SqlNombrado.consultar(con, SQL_DICCIONARIO, parms, RegistroDiccionario.class)) {
            if (codigo != null && codigo.equals(r.CodDiccionario)) {
                registro = r;
            }
        }
        if (registro == null || valores == null) {
            return codigo + " " + SIN_TRADUCCION;
        }
        String mensaje = registro.Descripcion;
        for (Map.Entry<String, String> par : valores.entrySet()) {
            String valor = par.getValue() == null ? "" : par.getValue();
            mensaje = mensaje.replace(SEPARADOR + par.getKey() + SEPARADOR, valor);
        }
        return mensaje;
    }

    /** Rows of {@code ConsultarFeriados.sql} (column label to value) for the given countries. */
    public static List<Map<String, Object>> feriados(Connection con, List<Long> idPaises) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPais", new ArrayList<>(idPaises));
        return SqlNombrado.filas(con, SQL_FERIADOS, params);
    }
}
