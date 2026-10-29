package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * {@link DiccionarioMensajesPort} over the legacy {@code ConsultarDiccionario.sql} (verbatim; parameters {@code IdEmpresa} and
 * {@code CodigoParametro="PLA.LocalizacionSistema"}). The legacy {@code Diccionario} loaded the dictionary once per JVM into statics (and wiped it
 * when another company was loaded). Here the dictionary of a company is loaded ONCE PER UNIT OF WORK and kept in a resource bound to the current
 * transaction, which is released when the transaction completes: no state survives a request, no state is shared between requests (DEV-F2).
 * Outside a transaction (unit tests) every call reads again.
 */
@Component
public class DiccionarioJdbc implements DiccionarioMensajesPort {

    /** Row of the legacy query ({@code Diccionario.RegistroDiccionario}). */
    public static class RegistroDiccionario {
        public String CodLenguaje;
        public String CodDiccionario;
        public String Descripcion;
    }

    private static final String SQL = SqlRecursos.kit("ConsultarDiccionario.sql");
    private static final String SEPARADOR = "::";
    private static final String SIN_TRADUCCION = "sin traduccion";
    private static final String RECURSO = DiccionarioJdbc.class.getName() + ".diccionarios";

    private final ConexionUnidadDeTrabajo conexion;

    public DiccionarioJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public String mensaje(Long idEmpresa, String codigo, Map<String, String> valores) {
        RegistroDiccionario registro = diccionario(idEmpresa).get(codigo);
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

    @SuppressWarnings("unchecked")
    private Map<String, RegistroDiccionario> diccionario(Long idEmpresa) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return cargar(idEmpresa);
        }
        Map<Long, Map<String, RegistroDiccionario>> cache = (Map<Long, Map<String, RegistroDiccionario>>) TransactionSynchronizationManager
                .getResource(RECURSO);
        if (cache == null) {
            cache = new HashMap<>();
            TransactionSynchronizationManager.bindResource(RECURSO, cache);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(RECURSO);
                }
            });
        }
        Map<Long, Map<String, RegistroDiccionario>> local = cache;
        return local.computeIfAbsent(idEmpresa, this::cargar);
    }

    private Map<String, RegistroDiccionario> cargar(Long idEmpresa) {
        Map<String, Object> parms = new HashMap<>();
        parms.put("IdEmpresa", idEmpresa);
        parms.put("CodigoParametro", "PLA.LocalizacionSistema");
        Map<String, RegistroDiccionario> registros = new HashMap<>();
        for (RegistroDiccionario r : conexion.usar(con -> SqlNombrado.consultar(con, SQL, parms, RegistroDiccionario.class))) {
            registros.put(r.CodDiccionario, r);
        }
        return registros;
    }
}
