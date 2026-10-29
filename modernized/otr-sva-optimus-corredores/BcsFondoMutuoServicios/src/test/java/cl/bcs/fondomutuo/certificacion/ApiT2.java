package cl.bcs.fondomutuo.certificacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.UnaryOperator;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * The bridge between the T2 tests and the production units of the fronts F-C and F-D, which DO NOT EXIST yet ("red before code"): the tests are compiled and run without them, and fail
 * with a clear {@code AssertionError} ("T2 not implemented: the class ... is missing") until they are written. Everything the tests assume about the production API is in this file and in
 * {@code API_ASSUMED_T2.md}: change the names HERE (never the goldens) if production needs another shape.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.certificacion}):
 * <pre>
 * public interface ProcesarCustodiaFondoMutuoPort { JsonObject procesar(JsonObject entrada); }       // legacy ProcesarCustodiaFondoMutuo.logica (not ported: closed gate G-CUS)
 * public interface CalculaCustodiaRescatePort     { JsonObject calcular(JsonObject entrada); }       // legacy CalculaCustodiaRescate.logica (not ported: closed gate G-CUS)
 * public final class CertificarAporteRescateAportes {                                                // F-C
 *     public CertificarAporteRescateAportes(FlujoCajaPort, ParidadPort, ProcesarCustodiaFondoMutuoPort, CertificarAporteRescateDatos, FormatoMoneda)
 *     public void validarSaldo(CertificarAporteRescateContexto)          throws Exception   // C:575-579  FM-CER-21, 22 and the format of the aportes (FM-CER-23)
 *     public void recalcularCostoUF(CertificarAporteRescateContexto)                         // C:600-601  FM-CER-24..26 (never throws: the legacy swallows every error)
 *     public void ingresarCustodia(CertificarAporteRescateContexto)      throws Exception   // C:602-620  FM-CER-27, 28
 * }
 * public final class CertificarAporteRescateRescates {                                               // F-D
 *     public CertificarAporteRescateRescates(CalculaCustodiaRescatePort, ProcesarCustodiaFondoMutuoPort, CalcularComisiones, ParidadPort, FormatoMoneda, DiccionarioMensajesPort)
 *     public void prepararRescates(CertificarAporteRescateContexto)      throws Exception   // C:582-596  FM-CER-31..38 (and the format of the rescates, FM-CER-23)
 *     public void retirarCustodiaYCostear(CertificarAporteRescateContexto) throws Exception // C:644-676  FM-CER-39..41
 * }
 * </pre>
 * Each method is a NO-OP when its list (aportes / rescates) is empty (the legacy guards with {@code if(list.size() > 0)}), reads and writes ONLY the {@link CertificarAporteRescateContexto} and
 * its ports (no JDBC write), and adds its counters and messages to {@code contexto.salida}. The legacy order of the five steps, which interleaves the two fronts and leaves the cuenta de
 * inversion of the aportes (front E) between {@code ingresarCustodia} and {@code retirarCustodiaYCostear}, is the order of {@link Pasos#todos}.
 */
final class ApiT2 {

    static final String PAQUETE = "cl.bcs.fondomutuo.certificacion.";
    static final String APORTES = PAQUETE + "CertificarAporteRescateAportes";
    static final String RESCATES = PAQUETE + "CertificarAporteRescateRescates";
    static final String PUERTO_CUSTODIA = PAQUETE + "ProcesarCustodiaFondoMutuoPort";
    static final String PUERTO_CALCULO_CUSTODIA = PAQUETE + "CalculaCustodiaRescatePort";

    private ApiT2() {
    }

    /** What the units are built with (every port is answered by the scripted collaborators of the case, as the oracle's registered doubles). */
    static final class Dependencias {
        FlujoCajaPort flujoCaja;
        ParidadPort paridad;
        UnaryOperator<JsonObject> procesarCustodia;
        UnaryOperator<JsonObject> calculaCustodia;
        CertificarAporteRescateDatos datos;
        FormatoMoneda formato;
        CalcularComisiones comisiones;
        DiccionarioMensajesPort diccionario;
    }

    /** The five steps of F-C and F-D in the order of the legacy {@code logica} (front E, the cuenta de inversion, runs between the third and the fourth). */
    static final class Pasos {
        private final Object aportes;
        private final Object rescates;

        private Pasos(Object aportes, Object rescates) {
            this.aportes = aportes;
            this.rescates = rescates;
        }

        void validarSaldo(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(aportes, "validarSaldo", ctx);
        }

        void prepararRescates(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(rescates, "prepararRescates", ctx);
        }

        void recalcularCostoUF(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(aportes, "recalcularCostoUF", ctx);
        }

        void ingresarCustodia(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(aportes, "ingresarCustodia", ctx);
        }

        void retirarCustodiaYCostear(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(rescates, "retirarCustodiaYCostear", ctx);
        }

        /** C:575-579, 582-596, 600-620, 644-676 (the code that is not F-C / F-D is not run). */
        void todos(CertificarAporteRescateContexto ctx) throws Exception {
            validarSaldo(ctx);
            prepararRescates(ctx);
            recalcularCostoUF(ctx);
            ingresarCustodia(ctx);
            retirarCustodiaYCostear(ctx);
        }
    }

    static Pasos crear(Dependencias d) {
        Class<?> cAportes = cargar(APORTES);
        Class<?> cRescates = cargar(RESCATES);
        Class<?> pCustodia = cargar(PUERTO_CUSTODIA);
        Class<?> pCalculo = cargar(PUERTO_CALCULO_CUSTODIA);
        Object custodia = puerto(pCustodia, "procesar", d.procesarCustodia);
        Object calculo = puerto(pCalculo, "calcular", d.calculaCustodia);
        Object aportes = construir(cAportes, new Class<?>[] { FlujoCajaPort.class, ParidadPort.class, pCustodia, CertificarAporteRescateDatos.class, FormatoMoneda.class }, d.flujoCaja, d.paridad,
                custodia, d.datos, d.formato);
        Object rescates = construir(cRescates, new Class<?>[] { pCalculo, pCustodia, CalcularComisiones.class, ParidadPort.class, FormatoMoneda.class, DiccionarioMensajesPort.class }, calculo,
                custodia, d.comisiones, d.paridad, d.formato, d.diccionario);
        return new Pasos(aportes, rescates);
    }

    static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T2 not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T2.md)", e);
        }
    }

    private static Object puerto(Class<?> interfaz, String metodo, UnaryOperator<JsonObject> destino) {
        assertInterfaz(interfaz, metodo);
        return Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[] { interfaz }, (proxy, m, args) -> {
            if (m.getName().equals(metodo)) return destino.apply((JsonObject) args[0]);
            if (m.getDeclaringClass() == Object.class) return m.invoke(destino, args);
            throw new AssertionError("T2: the port " + interfaz.getSimpleName() + " must have only the method " + metodo + "(JsonObject): " + m);
        });
    }

    private static void assertInterfaz(Class<?> interfaz, String metodo) {
        if (!interfaz.isInterface()) throw new AssertionError("T2: " + interfaz.getName() + " must be an interface (a port)");
        try {
            Method m = interfaz.getMethod(metodo, JsonObject.class);
            if (m.getReturnType() != JsonObject.class) throw new AssertionError("T2: " + interfaz.getName() + "." + metodo + " must return JsonObject");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T2: " + interfaz.getName() + " must declare JsonObject " + metodo + "(JsonObject)", e);
        }
    }

    private static Object construir(Class<?> clase, Class<?>[] tipos, Object... args) {
        try {
            return clase.getConstructor(tipos).newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T2: " + clase.getName() + " must have a public constructor " + java.util.Arrays.toString(tipos) + " (API_ASSUMED_T2.md)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T2: the constructor of " + clase.getName() + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T2: cannot build " + clase.getName() + ": " + e, e);
        }
    }

    /** Calls {@code objeto.metodo(ctx)}; whatever the unit throws comes out UNCHANGED (the legacy has no try/catch around these steps). */
    private static void invocar(Object objeto, String metodo, CertificarAporteRescateContexto ctx) throws Exception {
        Method m;
        try {
            m = objeto.getClass().getMethod(metodo, CertificarAporteRescateContexto.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T2: " + objeto.getClass().getName() + " must have a public method " + metodo + "(CertificarAporteRescateContexto) (API_ASSUMED_T2.md)", e);
        }
        try {
            m.invoke(objeto, ctx);
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause();
            if (causa instanceof Exception ex) throw ex;
            if (causa instanceof Error er) throw er;
            throw e;
        } catch (IllegalAccessException e) {
            throw new AssertionError("T2: " + objeto.getClass().getName() + "." + metodo + " must be public", e);
        }
    }
}
