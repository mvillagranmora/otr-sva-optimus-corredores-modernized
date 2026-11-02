package cl.bcs.fondomutuo.aportereinversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/**
 * The ONLY bridge between the FMM-CSR tests and the production classes of {@code CrearSolicitudAporteReinversion}, which DO NOT EXIST yet ("red before code"): the tests COMPILE and RUN
 * without them and fail with {@code AssertionError: CSR not implemented: the production class ... does not exist yet} until they are written. Everything the tests assume about the NEW
 * production classes is in this file and in {@code API_ASSUMED_CSR.md}: change the names HERE (never the goldens) if production needs another shape.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.aportereinversion}, main source set):
 * <pre>
 * public final class AporteReinversionContrato {                      // the nested classes of the legacy, same public field names, order and defaults (FM-REI-41)
 *     public static class Entrada { ... }                             // EntradaCrearSolicitudAporteReinversion
 *     public static class Salida { ... }                              // SalidaCrearSolicitudAporteReinversion
 *     public static class InSolicitudesAporte { ... }
 *     public static class ConsultaRegimenReinversion { ... }
 * }
 * public interface AporteReinversionPort { AporteReinversionContrato.Salida logica(AporteReinversionContrato.Entrada entrada) throws Exception; }   // legacy logica(con, entrada)
 * public interface AporteReinversionDatos { ... }                     // reads / writes of the class that are its own (SQL verbatim)
 * public class AporteReinversionJdbc implements AporteReinversionDatos { public AporteReinversionJdbc(ConexionUnidadDeTrabajo) }
 * public final class AporteReinversionFabrica {
 *     public static final class Dependencias { public RevisarRequisitosBasicosPort basicos; ConfiguracionSistemaPort configuracion; EmpresaPort empresas; DiccionarioMensajesPort diccionario;
 *         CalendarioPort calendario; SeriesFondoPort series; RequisitosDatos requisitosDatos; ComisionNnDatos comisionNnDatos; AporteReinversionDatos datos; PerfilFondoMutuoPort perfilPort;
 *         FechasPort fechasPort; CuotasPort cuotasPort; PatrimonioEmisorPort patrimonioPort; FlujoCustodiaFmDatos flujoCustodiaDatos; AgfDatos agfDatos; SecuenciaPort secuencias;
 *         ParidadPort paridad; PreciosTasasPort precios; CustodiaPort custodia; FlujoCajaPort flujoCaja; LogPort log; ProductosPortafolioPort productos;
 *         ProcesosExternos.TesoreriaPort tesoreria; ProcesosExternos.SpotPort spot; Compuertas compuertas; }
 *     public static AporteReinversionPort crear(Dependencias d)
 * }
 * </pre>
 * The input is read and the output written with {@link JsonLegacy#plano()} (the legacy {@code call}: plain {@code new Gson()} both ways). Not assumed (not exercised by these tests): the
 * JavaCall handler, the dispatcher registration and the Manejador destination (Q-T4A-6).
 */
final class ApiCsr {

    static final String PAQUETE = "cl.bcs.fondomutuo.aportereinversion.";
    static final String CONTRATO = PAQUETE + "AporteReinversionContrato";
    static final String ENTRADA = CONTRATO + "$Entrada";
    static final String SALIDA = CONTRATO + "$Salida";
    static final String IN_APORTE = CONTRATO + "$InSolicitudesAporte";
    static final String CONSULTA_REGIMEN = CONTRATO + "$ConsultaRegimenReinversion";
    static final String PUERTO = PAQUETE + "AporteReinversionPort";
    static final String JDBC = PAQUETE + "AporteReinversionJdbc";
    static final String FABRICA = PAQUETE + "AporteReinversionFabrica";
    static final String DEPENDENCIAS = FABRICA + "$Dependencias";

    private ApiCsr() {
    }

    static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("CSR not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_CSR.md)", e);
        }
    }

    /** A new {@code AporteReinversionFabrica.Dependencias} (every field null). */
    static Object dependencias() {
        try {
            Constructor<?> c = cargar(DEPENDENCIAS).getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("CSR: AporteReinversionFabrica.Dependencias must have a public no-arg constructor", e);
        }
    }

    static void poner(Object dependencias, String campo, Object valor) {
        try {
            Field f = dependencias.getClass().getField(campo);
            f.set(dependencias, valor);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("CSR: AporteReinversionFabrica.Dependencias must have a public field '" + campo + "' (API_ASSUMED_CSR.md)", e);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError("CSR: cannot set Dependencias." + campo + " with a " + (valor == null ? "null" : valor.getClass().getName()) + ": " + e, e);
        }
    }

    /** The real JDBC adapter of the reads and writes of the class over the unit of work of the case. */
    static Object nuevoDatos(ConexionUnidadDeTrabajo conexion) {
        Class<?> c = cargar(JDBC);
        try {
            return c.getConstructor(ConexionUnidadDeTrabajo.class).newInstance(conexion);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("CSR: " + JDBC + " must have a public constructor (ConexionUnidadDeTrabajo)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("CSR: the constructor of " + JDBC + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("CSR: cannot build " + JDBC + ": " + e, e);
        }
    }

    static Object crear(Object dependencias) {
        Class<?> f = cargar(FABRICA);
        try {
            Method m = f.getMethod("crear", cargar(DEPENDENCIAS));
            return m.invoke(null, dependencias);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("CSR: " + FABRICA + " must have a public static crear(Dependencias)", e);
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof Error er) throw er;
            if (c instanceof RuntimeException re) throw re;
            throw new AssertionError("CSR: " + c, c);
        } catch (IllegalAccessException e) {
            throw new AssertionError("CSR: " + FABRICA + ".crear must be public", e);
        }
    }

    /** The typed input as an in-process caller (Certificar, the Manejador) builds it: the legacy JSON read with the plain Gson. */
    static Object entrada(JsonObject json) {
        return JsonLegacy.plano().fromJson(json.toString(), cargar(ENTRADA));
    }

    /** {@code port.logica(entrada)}: whatever the class throws comes out UNCHANGED (the legacy has no try/catch around the work). */
    static Object logica(Object puerto, Object entrada) throws Exception {
        Class<?> p = cargar(PUERTO);
        if (!p.isInstance(puerto)) throw new AssertionError("CSR: " + puerto.getClass().getName() + " must implement " + PUERTO);
        try {
            Method m = p.getMethod("logica", cargar(ENTRADA));
            return m.invoke(puerto, entrada);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("CSR: " + PUERTO + " must declare Salida logica(Entrada) throws Exception", e);
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause();
            if (causa instanceof Exception ex) throw ex;
            if (causa instanceof Error er) throw er;
            throw e;
        } catch (IllegalAccessException e) {
            throw new AssertionError("CSR: " + PUERTO + ".logica must be public", e);
        }
    }

    /** The legacy {@code call}: JSON in, {@code logica}, JSON out (plain Gson both ways). */
    static JsonObject llamar(Object puerto, JsonObject entrada) throws Exception {
        Object salida = logica(puerto, entrada(entrada));
        return JsonParser.parseString(JsonLegacy.plano().toJson(salida)).getAsJsonObject();
    }
}
