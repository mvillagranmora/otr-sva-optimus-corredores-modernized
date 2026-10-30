package cl.bcs.fondomutuo.certificacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.UnaryOperator;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * The bridge between the T3 tests and the production units of the fronts F-E (cuenta de inversion), F-F (Tesoreria) and F-H (Spot), which DO NOT EXIST yet ("red before code"): the tests
 * are compiled and run without them, and fail with a clear {@code AssertionError} ("T3 not implemented: the class ... is missing") until they are written. Everything the tests assume about
 * the NEW production classes is in this file and in {@code API_ASSUMED_T3.md}: change the names HERE (never the goldens) if production needs another shape. The classes of T1 and T2
 * (which exist) are reached through {@link ApiT2}; the typed ports of the module that already exist ({@link ProcesosExternos.TesoreriaPort}, {@link ProcesosExternos.SpotPort},
 * {@link ConfiguracionSistemaPort}, {@link CertificarAporteRescateDatos}) are used directly.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.certificacion}):
 * <pre>
 * public interface CrearCargoAbonoPort { JsonObject crear(JsonObject entrada); }                     // legacy CrearCargoAbono.logica (CuentaInversion, not reachable from this module)
 * public final class CertificarAporteRescateCuentaInversion {                                        // F-E
 *     public CertificarAporteRescateCuentaInversion(CrearCargoAbonoPort, CertificarAporteRescateDatos)
 *     public void moverAportes(CertificarAporteRescateContexto)   throws Exception                   // C:622-640   FM-CER-29, 30
 *     public void moverRescates(CertificarAporteRescateContexto)  throws Exception                   // C:679-694   FM-CER-42, 43
 * }
 * public final class CertificarAporteRescateTesoreria {                                              // F-F
 *     public CertificarAporteRescateTesoreria(ProcesosExternos.TesoreriaPort, ConfiguracionSistemaPort)
 *     public void moverTesoreria(CertificarAporteRescateContexto) throws Exception                   // C:698-721   FM-CER-44..47
 * }
 * public final class CertificarAporteRescateSpot {                                                   // F-H
 *     public CertificarAporteRescateSpot(ProcesosExternos.SpotPort)
 *     public void moverSpot(CertificarAporteRescateContexto)      throws Exception                   // C:790-830   FM-CER-59, 60
 * }
 * </pre>
 * Each method is a NO-OP when its list (aportes / rescates) is empty (the legacy guards with {@code if(list.size() > 0)}), reads and writes ONLY the {@link CertificarAporteRescateContexto}
 * and its ports (no JDBC write), and adds its counters and messages to {@code contexto.salida}. {@link CertificarAporteRescateDatos} gains ONE read: the {@code GeneraCargoAbono} of a
 * movement type (the same SELECT of the legacy), which the harness gets from the real {@code CertificarAporteRescateDatosJdbc}. The legacy order of the nine steps, which interleaves the
 * fronts (the cuenta de inversion of the aportes between the third and the fourth step of T2, Facturacion and the reinversion between Tesoreria and Spot), is the order of {@link Pasos#todos}.
 */
final class ApiT3 {

    static final String PAQUETE = ApiT2.PAQUETE;
    static final String CUENTA = PAQUETE + "CertificarAporteRescateCuentaInversion";
    static final String TESORERIA = PAQUETE + "CertificarAporteRescateTesoreria";
    static final String SPOT = PAQUETE + "CertificarAporteRescateSpot";
    static final String PUERTO_CUENTA = PAQUETE + "CrearCargoAbonoPort";

    private ApiT3() {
    }

    /** What the units are built with (every port is answered by the scripted collaborators of the case, as the oracle's registered doubles). */
    static final class Dependencias {
        ApiT2.Dependencias t2;
        UnaryOperator<JsonObject> crearCargoAbono;
        ProcesosExternos.TesoreriaPort tesoreria;
        ProcesosExternos.SpotPort spot;
        CertificarAporteRescateDatos datos;
        ConfiguracionSistemaPort configuracion;
    }

    /** The nine steps of F-C..F-H in the order of the legacy {@code logica}. */
    static final class Pasos {
        private final ApiT2.Pasos t2;
        private final Object cuenta;
        private final Object tesoreria;
        private final Object spot;

        private Pasos(ApiT2.Pasos t2, Object cuenta, Object tesoreria, Object spot) {
            this.t2 = t2;
            this.cuenta = cuenta;
            this.tesoreria = tesoreria;
            this.spot = spot;
        }

        void moverCuentaAportes(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(cuenta, "moverAportes", ctx);
        }

        void moverCuentaRescates(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(cuenta, "moverRescates", ctx);
        }

        void moverTesoreria(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(tesoreria, "moverTesoreria", ctx);
        }

        void moverSpot(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(spot, "moverSpot", ctx);
        }

        /** C:575-640 (aportes), 582-596, 644-694 (rescates), 698-721 (Tesoreria), 790-830 (Spot); the code that is not F-C..F-H (facturacion, reinversion) is not run. */
        void todos(CertificarAporteRescateContexto ctx) throws Exception {
            t2.validarSaldo(ctx);
            t2.prepararRescates(ctx);
            t2.recalcularCostoUF(ctx);
            t2.ingresarCustodia(ctx);
            moverCuentaAportes(ctx);
            t2.retirarCustodiaYCostear(ctx);
            moverCuentaRescates(ctx);
            moverTesoreria(ctx);
            moverSpot(ctx);
        }
    }

    static Pasos crear(Dependencias d) {
        ApiT2.Pasos t2 = ApiT2.crear(d.t2);
        Class<?> cCuenta = cargar(CUENTA);
        Class<?> cTesoreria = cargar(TESORERIA);
        Class<?> cSpot = cargar(SPOT);
        Class<?> pCuenta = cargar(PUERTO_CUENTA);
        Object puertoCuenta = puerto(pCuenta, "crear", d.crearCargoAbono);
        Object cuenta = construir(cCuenta, new Class<?>[] { pCuenta, CertificarAporteRescateDatos.class }, puertoCuenta, d.datos);
        Object tesoreria = construir(cTesoreria, new Class<?>[] { ProcesosExternos.TesoreriaPort.class, ConfiguracionSistemaPort.class }, d.tesoreria, d.configuracion);
        Object spot = construir(cSpot, new Class<?>[] { ProcesosExternos.SpotPort.class }, d.spot);
        return new Pasos(t2, cuenta, tesoreria, spot);
    }

    static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T3 not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T3.md)", e);
        }
    }

    private static Object puerto(Class<?> interfaz, String metodo, UnaryOperator<JsonObject> destino) {
        assertInterfaz(interfaz, metodo);
        return Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[] { interfaz }, (proxy, m, args) -> {
            if (m.getName().equals(metodo)) return destino.apply((JsonObject) args[0]);
            if (m.getDeclaringClass() == Object.class) return m.invoke(destino, args);
            throw new AssertionError("T3: the port " + interfaz.getSimpleName() + " must have only the method " + metodo + "(JsonObject): " + m);
        });
    }

    static void assertInterfaz(Class<?> interfaz, String metodo) {
        if (!interfaz.isInterface()) throw new AssertionError("T3: " + interfaz.getName() + " must be an interface (a port)");
        try {
            Method m = interfaz.getMethod(metodo, JsonObject.class);
            if (m.getReturnType() != JsonObject.class) throw new AssertionError("T3: " + interfaz.getName() + "." + metodo + " must return JsonObject");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T3: " + interfaz.getName() + " must declare JsonObject " + metodo + "(JsonObject)", e);
        }
    }

    private static Object construir(Class<?> clase, Class<?>[] tipos, Object... args) {
        try {
            return clase.getConstructor(tipos).newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T3: " + clase.getName() + " must have a public constructor " + java.util.Arrays.toString(tipos) + " (API_ASSUMED_T3.md)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T3: the constructor of " + clase.getName() + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T3: cannot build " + clase.getName() + ": " + e, e);
        }
    }

    /** Calls {@code objeto.metodo(ctx)}; whatever the unit throws comes out UNCHANGED (the legacy has no try/catch around these steps). */
    private static void invocar(Object objeto, String metodo, CertificarAporteRescateContexto ctx) throws Exception {
        Method m;
        try {
            m = objeto.getClass().getMethod(metodo, CertificarAporteRescateContexto.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T3: " + objeto.getClass().getName() + " must have a public method " + metodo + "(CertificarAporteRescateContexto) (API_ASSUMED_T3.md)", e);
        }
        try {
            m.invoke(objeto, ctx);
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause();
            if (causa instanceof Exception ex) throw ex;
            if (causa instanceof Error er) throw er;
            throw e;
        } catch (IllegalAccessException e) {
            throw new AssertionError("T3: " + objeto.getClass().getName() + "." + metodo + " must be public", e);
        }
    }
}
