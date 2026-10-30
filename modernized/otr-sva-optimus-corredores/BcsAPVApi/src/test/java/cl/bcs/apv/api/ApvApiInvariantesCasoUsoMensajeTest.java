package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.casouso.CasosUso;

/**
 * Invariants of the two hand written pieces every operation depends on, and of the default factory of every record.
 *
 * <p>Rule ids: B-APVAPI-15 ({@code CasoUso}: one abstract method, generic E/R, nothing else), B-APVAPI-16 ({@code Mensaje}: record of the four legacy
 * {@code MensajeSalida} fields, defaults and list factory), B-APVAPI-17 (every record has the {@code porDefecto()} factory and the legacy list defaults
 * are mutable and not shared), B-APVAPI-18 ({@code CasosUso} is a non instantiable constant holder whose list cannot be modified).
 */
class ApvApiInvariantesCasoUsoMensajeTest {

    // B-APVAPI-15
    @Test
    void b_apvapi_15_CasoUso_es_una_interfaz_publica_con_un_unico_metodo_ejecutar() throws Exception {
        Class<?> c = CasoUso.class;
        assertTrue(c.isInterface() && Modifier.isPublic(c.getModifiers()));
        assertEquals(2, c.getTypeParameters().length, "CasoUso<E, R>");
        assertEquals("E", c.getTypeParameters()[0].getName());
        assertEquals("R", c.getTypeParameters()[1].getName());
        assertEquals(0, c.getInterfaces().length, "CasoUso extends nothing");
        assertEquals(0, c.getDeclaredFields().length, "CasoUso holds no constants/state");
        Method[] ms = c.getDeclaredMethods();
        assertEquals(1, ms.length, "only ejecutar");
        Method m = ms[0];
        assertEquals("ejecutar", m.getName());
        assertTrue(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()), "ejecutar is abstract (no default body)");
        assertEquals(1, m.getParameterCount());
        assertEquals("R", m.getGenericReturnType().getTypeName());
        assertEquals("E", m.getGenericParameterTypes()[0].getTypeName());
        assertEquals(0, m.getExceptionTypes().length, "ejecutar declares no checked exception (runtime exceptions propagate)");
    }

    // B-APVAPI-16
    @Test
    void b_apvapi_16_Mensaje_es_el_MensajeSalida_legacy_de_cuatro_campos() throws Exception {
        assertTrue(Mensaje.class.isRecord() && Modifier.isPublic(Mensaje.class.getModifiers()));
        RecordComponent[] cs = Mensaje.class.getRecordComponents();
        assertEquals(List.of("id", "tipo", "codigo", "descripcion"), java.util.Arrays.stream(cs).map(RecordComponent::getName).toList());
        assertEquals(List.of(Long.class, String.class, String.class, String.class), java.util.Arrays.stream(cs).map(RecordComponent::getType).toList());
        Mensaje d = Mensaje.porDefecto();
        assertEquals(new Mensaje(null, null, null, null), d);
        assertNull(d.id());
        assertNull(d.tipo());
        assertNull(d.codigo());
        assertNull(d.descripcion());
        // lista(): empty, mutable, a new instance every time (the legacy default is "new ArrayList<MensajeSalida>()" per object)
        List<Mensaje> a = Mensaje.lista();
        List<Mensaje> b = Mensaje.lista();
        assertTrue(a.isEmpty() && b.isEmpty());
        assertNotSame(a, b);
        a.add(d);
        assertTrue(b.isEmpty(), "lists must not be shared");
        // value semantics
        assertEquals(new Mensaje(1L, "ERROR", "E1", "x"), new Mensaje(1L, "ERROR", "E1", "x"));
        assertEquals(2, Mensaje.class.getDeclaredMethods().length - extra(Mensaje.class), "porDefecto and lista besides the record methods");
    }

    private static int extra(Class<?> c) {
        int n = 0;
        for (Method m : c.getDeclaredMethods()) {
            String s = m.getName();
            if (List.of("id", "tipo", "codigo", "descripcion", "equals", "hashCode", "toString").contains(s)) {
                n++;
            }
        }
        return n;
    }

    // B-APVAPI-17
    @Test
    void b_apvapi_17_todos_los_records_tienen_porDefecto_y_las_listas_por_defecto_no_se_comparten() throws Exception {
        GrafoApv g = GrafoApv.get();
        assertEquals(234, g.pares.size());
        int listasMutables = 0;
        for (GrafoApv.Par p : g.pares) {
            Class<?> r = p.record();
            Method f = r.getMethod("porDefecto");
            assertTrue(Modifier.isStatic(f.getModifiers()) && f.getParameterCount() == 0 && f.getReturnType() == r, r.getName() + ": static R porDefecto()");
            Object a = f.invoke(null);
            Object b = f.invoke(null);
            assertEquals(a, b, r.getName() + ": porDefecto is deterministic");
            for (RecordComponent c : r.getRecordComponents()) {
                Object va = c.getAccessor().invoke(a);
                Object vb = c.getAccessor().invoke(b);
                if (va instanceof List<?> la) {
                    @SuppressWarnings("unchecked")
                    List<Object> mutable = (List<Object>) la;
                    assertNotSame(la, vb, r.getName() + "." + c.getName() + ": default list shared between instances");
                    if (la.isEmpty()) {
                        mutable.add(null);
                        mutable.remove(null);
                        listasMutables++;
                    }
                } else if (va != null && !(va instanceof Number) && !(va instanceof String) && !(va instanceof Boolean)) {
                    // nested default objects are fresh too
                    assertNotSame(va, vb, r.getName() + "." + c.getName() + ": default object shared");
                }
            }
        }
        System.out.println("[B-APVAPI-17] records with porDefecto: " + g.pares.size() + "; mutable default lists: " + listasMutables);
        assertTrue(listasMutables > 50, "expected many legacy 'new ArrayList' defaults, found " + listasMutables);
    }

    // B-APVAPI-18
    @Test
    void b_apvapi_18_CasosUso_es_un_contenedor_final_no_instanciable_e_inmodificable() throws Exception {
        Class<?> c = CasosUso.class;
        assertTrue(Modifier.isFinal(c.getModifiers()) && Modifier.isPublic(c.getModifiers()));
        assertEquals(1, c.getDeclaredConstructors().length);
        assertTrue(Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()), "private constructor");
        assertEquals(1, c.getDeclaredFields().length, "only TODOS");
        assertTrue(Modifier.isStatic(c.getDeclaredField("TODOS").getModifiers()) && Modifier.isFinal(c.getDeclaredField("TODOS").getModifiers()));
        assertNotNull(CasosUso.TODOS);
        assertThrows(UnsupportedOperationException.class, () -> CasosUso.TODOS.add(null), "TODOS must be immutable");
        assertThrows(UnsupportedOperationException.class, () -> CasosUso.TODOS.remove(0), "TODOS must be immutable");
        List<Class<?>> copia = new ArrayList<>(CasosUso.TODOS);
        assertEquals(copia, new ArrayList<>(CasosUso.TODOS), "stable order");
        // alphabetical order by simple name (the generator sorts by use case name): keeps diffs reviewable
        List<String> nombres = CasosUso.TODOS.stream().map(Class::getSimpleName).toList();
        assertEquals(nombres.stream().sorted().toList(), nombres, "CasosUso.TODOS is sorted by name");
    }
}
