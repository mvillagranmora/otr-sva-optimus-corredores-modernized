package cl.bcs.fondomutuo.certificacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;

/**
 * The bridge between the T4 tests and the production units of the fronts F-G (Facturacion) and F-I (reinversion and switch), which DO NOT EXIST yet ("red before code"): the tests compile
 * and run without them and fail with a clear {@code AssertionError} ("T4 not implemented: the production class ... does not exist yet") until they are written. Everything the tests
 * assume about the NEW production classes is in this file and in {@code API_ASSUMED_T4.md}: change the names HERE (never the goldens) if production needs another shape. The classes of
 * T1..T3 (which exist) are reached through {@link ApiT2} and {@link ApiT3}.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.certificacion}):
 * <pre>
 * public interface FacturacionPort { JsonObject crearMovimientoComprobante(JsonObject e); JsonObject crearComprobante(JsonObject e); }   // legacy Facturacion (G6, not ported)
 * public interface CrearAporteReinversionPort { JsonObject crear(JsonObject entrada); }              // legacy CrearSolicitudAporteReinversion.logica (AR, not ported in T4-B)
 * public interface CertificacionHijaPort { Salida certificar(Entrada entrada) throws Exception; }      // the recursion of the legacy (new CertificarSolicitudAporteRescate().logica)
 * public final class CertificarAporteRescateFacturacion {                                              // F-G
 *     public CertificarAporteRescateFacturacion(FacturacionPort, CertificarAporteRescateDatos)
 *     public void facturar(CertificarAporteRescateContexto) throws Exception                           // C:724-759   FM-CER-48..51
 * }
 * public final class CertificarAporteRescateReinversion {                                              // F-I (1)
 *     public CertificarAporteRescateReinversion(CrearAporteReinversionPort, CertificarAporteRescateDatos, FormatoMoneda)
 *     public void reinvertir(CertificarAporteRescateContexto) throws Exception                         // C:762-787, 2343-2557   FM-CER-52..58
 * }
 * public interface CertificarAporteRescateEscrituraMonto { ... }                                      // the ONE write of F-C..F-I (FM-CER-64); JDBC adapter CertificarAporteRescateEscrituraMontoJdbc(ConexionUnidadDeTrabajo)
 * public final class CertificarAporteRescateSwitch {                                                   // F-I (2)
 *     public CertificarAporteRescateSwitch(CertificacionHijaPort, CertificarAporteRescateDatos, CertificarAporteRescateEscrituraMonto)
 *     public void certificarAportesSwitch(CertificarAporteRescateContexto) throws Exception            // C:838-843, 2764-2775   FM-CER-63
 *     public void cambiarMontoMonedaSistema(CertificarAporteRescateContexto) throws Exception          // C:845, 2820-2835       FM-CER-64 (the ONLY write of F-C..F-I)
 * }
 * </pre>
 * Additive changes: {@code CertificarAporteRescateContexto.carterasRetiradas} (the movements of custody that {@code retirarCustodiaYCostear} retired: the reinversion reads them),
 * {@code CertificarAporteRescateDatos} (reads of the destinations, of the origin series, of the IVA and of the sums); the UPDATE of FM-CER-64 has its OWN write port (the existing
 * {@code CertificarAporteRescateEscritura} is not touched: T1 implements it with a lambda). The harness builds the REAL JDBC adapters, so the names of those methods are not part of the contract. The legacy order of the steps is {@link Pasos#todos}.
 */
final class ApiT4 {

    static final String PAQUETE = ApiT2.PAQUETE;
    static final String PUERTO_FACTURACION = PAQUETE + "FacturacionPort";
    static final String PUERTO_REINVERSION = PAQUETE + "CrearAporteReinversionPort";
    static final String PUERTO_HIJO = PAQUETE + "CertificacionHijaPort";
    static final String FACTURACION = PAQUETE + "CertificarAporteRescateFacturacion";
    static final String REINVERSION = PAQUETE + "CertificarAporteRescateReinversion";
    static final String SWITCH = PAQUETE + "CertificarAporteRescateSwitch";
    static final String ESCRITURA_MONTO = PAQUETE + "CertificarAporteRescateEscrituraMonto";
    static final String ESCRITURA_MONTO_JDBC = PAQUETE + "CertificarAporteRescateEscrituraMontoJdbc";

    private ApiT4() {
    }

    /** The checked-exception lambda of the recursion (the double of the child). */
    @FunctionalInterface
    interface Hijo {
        Salida certificar(Entrada entrada) throws Exception;
    }

    /** What the units are built with (every port is answered by the scripted / replayed collaborators of the case). */
    static final class Dependencias {
        ApiT3.Dependencias t3;
        UnaryOperator<JsonObject> crearMovimientoComprobante;
        UnaryOperator<JsonObject> crearComprobante;
        UnaryOperator<JsonObject> crearAporteReinversion;
        Hijo hijo;
        CertificarAporteRescateDatos datos;
        cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo conexion;
        FormatoMoneda formato;
    }

    /** The steps of F-C..F-I in the order of the legacy {@code logica}; the J-lite hook runs between Spot and the recursion (see {@link #todos}). */
    static final class Pasos {
        private final ApiT2.Pasos t2;
        private final ApiT3.Pasos t3;
        private final Object facturacion;
        private final Object reinversion;
        private final Object conmutador;

        private Pasos(ApiT2.Pasos t2, ApiT3.Pasos t3, Object facturacion, Object reinversion, Object conmutador) {
            this.t2 = t2;
            this.t3 = t3;
            this.facturacion = facturacion;
            this.reinversion = reinversion;
            this.conmutador = conmutador;
        }

        void facturar(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(facturacion, "facturar", ctx);
        }

        void reinvertir(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(reinversion, "reinvertir", ctx);
        }

        void certificarAportesSwitch(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(conmutador, "certificarAportesSwitch", ctx);
        }

        void cambiarMontoMonedaSistema(CertificarAporteRescateContexto ctx) throws Exception {
            invocar(conmutador, "cambiarMontoMonedaSistema", ctx);
        }

        /**
         * C:575-640 aportes, 582-596 and 644-694 rescates, 698-721 Tesoreria, 724-759 Facturacion, 762-787 reinversion, 790-830 Spot; then (after front J, which the harness does not
         * run, see {@code entreTanto}) the recursion C:838-843 and FM-CER-64 C:845.
         */
        void todos(CertificarAporteRescateContexto ctx, Runnable frenteJ) throws Exception {
            t2.validarSaldo(ctx);
            t2.prepararRescates(ctx);
            t2.recalcularCostoUF(ctx);
            t2.ingresarCustodia(ctx);
            t3.moverCuentaAportes(ctx);
            t2.retirarCustodiaYCostear(ctx);
            t3.moverCuentaRescates(ctx);
            t3.moverTesoreria(ctx);
            facturar(ctx);
            reinvertir(ctx);
            t3.moverSpot(ctx);
            frenteJ.run();
            certificarAportesSwitch(ctx);
            cambiarMontoMonedaSistema(ctx);
        }
    }

    static Pasos crear(Dependencias d) {
        ApiT2.Pasos t2 = ApiT2.crear(d.t3.t2);
        ApiT3.Pasos t3 = ApiT3.crear(d.t3);
        Class<?> cFact = cargar(FACTURACION);
        Class<?> cRein = cargar(REINVERSION);
        Class<?> cSwitch = cargar(SWITCH);
        Class<?> pFact = cargar(PUERTO_FACTURACION);
        Class<?> pRein = cargar(PUERTO_REINVERSION);
        Class<?> pHijo = cargar(PUERTO_HIJO);
        Object puertoFact = proxy(pFact, Map.of("crearMovimientoComprobante", d.crearMovimientoComprobante, "crearComprobante", d.crearComprobante));
        Object puertoRein = proxy(pRein, Map.of("crear", d.crearAporteReinversion));
        Object puertoHijo = proxyHijo(pHijo, d.hijo);
        Object facturacion = construir(cFact, new Class<?>[] { pFact, CertificarAporteRescateDatos.class }, puertoFact, d.datos);
        Object reinversion = construir(cRein, new Class<?>[] { pRein, CertificarAporteRescateDatos.class, FormatoMoneda.class }, puertoRein, d.datos, d.formato);
        Class<?> pEscritura = cargar(ESCRITURA_MONTO);
        Object escritura = construir(cargar(ESCRITURA_MONTO_JDBC), new Class<?>[] { cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo.class }, d.conexion);
        Object conmutador = construir(cSwitch, new Class<?>[] { pHijo, CertificarAporteRescateDatos.class, pEscritura }, puertoHijo, d.datos, escritura);
        return new Pasos(t2, t3, facturacion, reinversion, conmutador);
    }

    static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T4 not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T4.md)", e);
        }
    }

    private static Object proxy(Class<?> interfaz, Map<String, UnaryOperator<JsonObject>> destinos) {
        if (!interfaz.isInterface()) throw new AssertionError("T4: " + interfaz.getName() + " must be an interface (a port)");
        for (String m : destinos.keySet()) {
            try {
                Method met = interfaz.getMethod(m, JsonObject.class);
                if (met.getReturnType() != JsonObject.class) throw new AssertionError("T4: " + interfaz.getName() + "." + m + " must return JsonObject");
            } catch (NoSuchMethodException e) {
                throw new AssertionError("T4: " + interfaz.getName() + " must declare JsonObject " + m + "(JsonObject)", e);
            }
        }
        return Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[] { interfaz }, (p, m, args) -> {
            UnaryOperator<JsonObject> destino = destinos.get(m.getName());
            if (destino != null) return destino.apply((JsonObject) args[0]);
            if (m.getDeclaringClass() == Object.class) return m.invoke(destinos, args);
            throw new AssertionError("T4: the port " + interfaz.getSimpleName() + " must have only the methods " + destinos.keySet() + ": " + m);
        });
    }

    private static Object proxyHijo(Class<?> interfaz, Hijo hijo) {
        if (!interfaz.isInterface()) throw new AssertionError("T4: " + interfaz.getName() + " must be an interface (a port)");
        try {
            Method met = interfaz.getMethod("certificar", Entrada.class);
            if (met.getReturnType() != Salida.class) throw new AssertionError("T4: " + interfaz.getName() + ".certificar must return CertificarAporteRescateContrato.Salida");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T4: " + interfaz.getName() + " must declare Salida certificar(Entrada) throws Exception", e);
        }
        return Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[] { interfaz }, (p, m, args) -> {
            if (m.getName().equals("certificar")) return hijo.certificar((Entrada) args[0]);
            if (m.getDeclaringClass() == Object.class) return m.invoke(hijo, args);
            throw new AssertionError("T4: the port " + interfaz.getSimpleName() + " must have only the method certificar(Entrada): " + m);
        });
    }

    private static Object construir(Class<?> clase, Class<?>[] tipos, Object... args) {
        try {
            return clase.getConstructor(tipos).newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T4: " + clase.getName() + " must have a public constructor " + Arrays.toString(tipos) + " (API_ASSUMED_T4.md)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T4: the constructor of " + clase.getName() + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T4: cannot build " + clase.getName() + ": " + e, e);
        }
    }

    /** Calls {@code objeto.metodo(ctx)}; whatever the unit throws comes out UNCHANGED (the legacy has no try/catch around these steps). */
    private static void invocar(Object objeto, String metodo, CertificarAporteRescateContexto ctx) throws Exception {
        Method m;
        try {
            m = objeto.getClass().getMethod(metodo, CertificarAporteRescateContexto.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T4: " + objeto.getClass().getName() + " must have a public method " + metodo + "(CertificarAporteRescateContexto) (API_ASSUMED_T4.md)", e);
        }
        try {
            m.invoke(objeto, ctx);
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause();
            if (causa instanceof Exception ex) throw ex;
            if (causa instanceof Error er) throw er;
            throw e;
        } catch (IllegalAccessException e) {
            throw new AssertionError("T4: " + objeto.getClass().getName() + "." + metodo + " must be public", e);
        }
    }
}
