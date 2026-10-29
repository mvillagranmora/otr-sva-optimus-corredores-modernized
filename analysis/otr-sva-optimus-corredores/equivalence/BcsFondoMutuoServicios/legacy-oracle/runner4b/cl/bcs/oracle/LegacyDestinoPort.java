package cl.bcs.oracle;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import com.google.gson.Gson;

import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;

/**
 * Adapter of {@link TipoSolicitudPort} over a legacy destination class of the oracle (a registered DOUBLE for the seven not served in F1): builds the
 * legacy {@code EntradaCrearSolicitudX} once (ONE object for both passes, as the legacy Manejador does), assigns the common fields by reflection (null
 * included: a null channel overwrites the legacy default "OPT"), converts only the lists the facade sent, and calls {@code logica(con, entrada)}.
 */
class LegacyDestinoPort implements TipoSolicitudPort {
    private static final Gson GSON = new Gson();
    private final Connection con;
    private final Object servicio;
    private final Class<?> entradaCls;
    private final Method logica;
    private Object entrada;

    LegacyDestinoPort(Connection con, String simple) throws Exception {
        this.con = con;
        Class<?> c = Class.forName("cl.bcs.fondomutuo." + simple);
        this.servicio = c.getDeclaredConstructor().newInstance();
        this.entradaCls = Class.forName("cl.bcs.fondomutuo." + simple + "$Entrada" + simple);
        this.logica = c.getMethod("logica", Connection.class, entradaCls);
    }

    private void set(String campo, Object valor) throws Exception {
        Field f = entradaCls.getField(campo);
        f.set(entrada, valor);
    }

    private void lista(String campo, java.util.List<?> valor) throws Exception {
        if (valor == null) return;
        Field f = entradaCls.getField(campo);
        f.set(entrada, GSON.fromJson(GSON.toJsonTree(valor), f.getGenericType()));
    }

    @Override
    public Respuesta logica(Pedido p) throws Exception {
        if (entrada == null) entrada = entradaCls.getDeclaredConstructor().newInstance();
        set("p_IdEmpresa", p.p_IdEmpresa);
        set("p_IdUsuario", p.p_IdUsuario);
        set("p_IdModulo", p.p_IdModulo);
        set("p_SimularOperacion", p.p_SimularOperacion);
        set("p_UsaReglasNegocio", p.p_UsaReglasNegocio);
        set("p_IdTipoCanalIngreso", p.p_IdTipoCanalIngreso);
        lista("p_SolicitudesAporte", p.p_SolicitudesAporte);
        lista("p_SolicitudesRescate", p.p_SolicitudesRescate);
        try {
            Object salida = logica.invoke(servicio, con, entrada);
            return GSON.fromJson(GSON.toJson(salida), Respuesta.class);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }
}
