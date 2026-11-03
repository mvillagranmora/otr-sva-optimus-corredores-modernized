package cl.bcs.plataforma.login.dialecto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * Loads the production statement layer (SqlDialecto, HanaDialecto, PostgresDialecto, SentenciasSql) a second time, in a child-first class loader whose
 * RESOURCES the test controls: a resource can be hidden (the file does not exist) or replaced (the file has other text). That is how a test proves that
 * {@code SentenciasSql.texto(id)} really reads {@code /sql/postgres/<id>.sql} and what it does when the file is missing, WITHOUT touching the production
 * resources and without a test double that would replace the class under test. The isolated classes are used by reflection only (they are not the classes
 * the test was compiled against).
 */
final class LoginPg3Aislado implements AutoCloseable {

    private final Cargador cargador;

    LoginPg3Aislado(Set<String> ocultos, Map<String, String> reemplazos) {
        this.cargador = new Cargador(ocultos, reemplazos);
    }

    static final class Cargador extends URLClassLoader {
        private final Set<String> ocultos;
        private final Map<String, String> reemplazos;

        Cargador(Set<String> ocultos, Map<String, String> reemplazos) {
            super(new URL[] { clasesPrincipales() }, ClassLoader.getPlatformClassLoader());
            this.ocultos = ocultos;
            this.reemplazos = reemplazos;
        }

        @Override
        public InputStream getResourceAsStream(String nombre) {
            if (ocultos.contains(nombre)) return null;
            String r = reemplazos.get(nombre);
            if (r != null) return new ByteArrayInputStream(r.getBytes(StandardCharsets.UTF_8));
            return super.getResourceAsStream(nombre);
        }

        @Override
        public URL getResource(String nombre) {
            if (ocultos.contains(nombre) || reemplazos.containsKey(nombre)) return null;
            return super.getResource(nombre);
        }
    }

    /** the directory (or jar) the production classes of the module are loaded from */
    static URL clasesPrincipales() {
        return SqlDialecto.class.getProtectionDomain().getCodeSource().getLocation();
    }

    /** {@code SentenciasSql} of the given dialect ({@code hana} or {@code postgres}) built over the isolated classes, through the public no-arg constructors */
    Object sentencias(String dialecto) {
        try {
            Class<?> clase = cargador.loadClass(ApiLoginPg3.PAQUETE + (dialecto.equals("hana") ? "HanaDialecto" : "PostgresDialecto"));
            Object d = clase.getConstructor().newInstance();
            Class<?> tipo = cargador.loadClass(ApiLoginPg3.PAQUETE + "SqlDialecto");
            return cargador.loadClass(ApiLoginPg3.PAQUETE + "SentenciasSql").getConstructor(tipo).newInstance(d);
        } catch (NoSuchMethodException e) {
            throw ApiLoginPg3.pendiente("the production dialect classes have no public no-arg constructor / SentenciasSql(SqlDialecto): " + e.getMessage());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException r) throw r;
            throw new AssertionError(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /** calls a public method of an isolated object; the exception the method throws reaches the caller unchanged */
    static Object invocar(Object objeto, String nombre, Class<?>[] tipos, Object... args) {
        try {
            Method m = objeto.getClass().getMethod(nombre, tipos);
            return m.invoke(objeto, args);
        } catch (NoSuchMethodException e) {
            throw ApiLoginPg3.pendiente("SentenciasSql has no public method " + nombre);
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof RuntimeException r) throw r;
            if (c instanceof Error er) throw er;
            throw new AssertionError(c);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static String texto(Object sentencias, String id) {
        return (String) invocar(sentencias, "texto", new Class<?>[] { String.class }, id);
    }

    static void exigirDisponibles(Object sentencias, java.util.Collection<String> ids) {
        invocar(sentencias, "exigirDisponibles", new Class<?>[] { java.util.Collection.class }, ids);
    }

    @Override
    public void close() throws java.io.IOException {
        cargador.close();
    }
}
