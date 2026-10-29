package cl.bcs.fondomutuo.adapter.in.javacall;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SEC-004 (CWE-770/400): input limits of the {@code /internal/} requests, applied BEFORE the unit of work opens its transaction (order between the token
 * filter and the unit of work), so a rejected request never touches the database. (1) body size: larger than {@code services.javacall.max-body-bytes}
 * (default 2 MB) answers 413 with a fixed body, checking Content-Length first and otherwise reading at most the cap plus one byte; (2) JSON nesting depth
 * larger than {@code services.javacall.max-json-depth} (default 64) and (3) any JSON array longer than {@code services.javacall.max-list-size} (default
 * 5000; the known input lists are arrays) answer 400 with a fixed body. A body that is not parseable is NOT judged here: it goes on and the controller
 * answers its usual 400. The defaults are far above every case of the oracle (the suite proves it); the bytes read are replayed to the controller unchanged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class JavaCallLimitesFilter extends OncePerRequestFilter {

    public static final int MAX_BODY_POR_DEFECTO = 2 * 1024 * 1024;
    public static final int MAX_PROFUNDIDAD_POR_DEFECTO = 64;
    public static final int MAX_LISTA_POR_DEFECTO = 5000;
    /** Request attribute with the top-level {@code "class"} string of the body (last occurrence, as the controller reads it); absent when there is none. */
    public static final String ATRIBUTO_CLASE = JavaCallLimitesFilter.class.getName() + ".CLASE";
    static final String CUERPO_413 = "{\"error\":\"request body too large\"}";
    static final String CUERPO_400 = "{\"error\":\"request structure exceeds the allowed limits\"}";

    private static final Logger log = LoggerFactory.getLogger(JavaCallLimitesFilter.class);

    private final int maxBody;
    private final int maxProfundidad;
    private final int maxLista;

    public JavaCallLimitesFilter(
            @Value("${services.javacall.max-body-bytes:2097152}") int maxBody,
            @Value("${services.javacall.max-json-depth:64}") int maxProfundidad,
            @Value("${services.javacall.max-list-size:5000}") int maxLista) {
        this.maxBody = maxBody;
        this.maxProfundidad = maxProfundidad;
        this.maxLista = maxLista;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !TokenInternoFilter.normalizar(request.getRequestURI()).startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getContentLengthLong() > maxBody) {
            rechazar(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, CUERPO_413);
            return;
        }
        byte[] cuerpo = leerConTope(request.getInputStream());
        if (cuerpo == null) {
            rechazar(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, CUERPO_413);
            return;
        }
        String motivo = excede(new String(cuerpo, StandardCharsets.UTF_8));
        if (motivo != null) {
            log.warn("JavaCall request rejected: {} (bytes={})", motivo, cuerpo.length);
            rechazar(response, HttpServletResponse.SC_BAD_REQUEST, CUERPO_400);
            return;
        }
        String clase = claseDe(cuerpo);
        if (clase != null) request.setAttribute(ATRIBUTO_CLASE, clase);
        chain.doFilter(new CuerpoRepetible(request, cuerpo), response);
    }

    /** The top-level {@code "class"} string of the JSON object (the last one if repeated, like the controller); null if absent, not a string or unparseable. */
    static String claseDe(byte[] cuerpo) {
        try (JsonReader r = new JsonReader(new StringReader(new String(cuerpo, StandardCharsets.UTF_8)))) {
            r.setStrictness(com.google.gson.Strictness.LENIENT);
            if (r.peek() != JsonToken.BEGIN_OBJECT) return null;
            r.beginObject();
            String clase = null;
            while (r.hasNext()) {
                String nombre = r.nextName();
                if ("class".equals(nombre) && r.peek() == JsonToken.STRING) {
                    clase = r.nextString();
                } else {
                    if ("class".equals(nombre)) clase = null;
                    r.skipValue();
                }
            }
            return clase;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** The bytes, or null when they exceed the cap. */
    private byte[] leerConTope(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) >= 0) {
            out.write(buf, 0, n);
            if (out.size() > maxBody) return null;
        }
        return out.toByteArray();
    }

    /** Reason (fixed text) when the depth or a list length exceeds the cap; null otherwise or when the body cannot be parsed (the controller answers that). */
    String excede(String json) {
        try (JsonReader r = new JsonReader(new StringReader(json))) {
            r.setStrictness(com.google.gson.Strictness.LENIENT);
            int profundidad = 0;
            int[] largo = new int[maxProfundidad + 2];
            boolean[] lista = new boolean[maxProfundidad + 2];
            while (r.peek() != JsonToken.END_DOCUMENT) {
                JsonToken t = r.peek();
                switch (t) {
                    case BEGIN_ARRAY, BEGIN_OBJECT -> {
                        if (profundidad > 0 && lista[profundidad] && ++largo[profundidad] > maxLista) return "list longer than " + maxLista;
                        if (t == JsonToken.BEGIN_ARRAY) r.beginArray(); else r.beginObject();
                        if (++profundidad > maxProfundidad) return "nesting deeper than " + maxProfundidad;
                        lista[profundidad] = t == JsonToken.BEGIN_ARRAY;
                        largo[profundidad] = 0;
                    }
                    case END_ARRAY -> {
                        r.endArray();
                        profundidad--;
                    }
                    case END_OBJECT -> {
                        r.endObject();
                        profundidad--;
                    }
                    case NAME -> r.nextName();
                    default -> {
                        r.skipValue();
                        if (profundidad > 0 && lista[profundidad] && ++largo[profundidad] > maxLista) return "list longer than " + maxLista;
                    }
                }
            }
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static void rechazar(HttpServletResponse response, int status, String cuerpo) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(cuerpo.getBytes(StandardCharsets.UTF_8));
    }

    /** The request with the body already read, replayable (the controller reads it as if nothing had happened). */
    private static final class CuerpoRepetible extends HttpServletRequestWrapper {
        private final byte[] bytes;

        CuerpoRepetible(HttpServletRequest request, byte[] bytes) {
            super(request);
            this.bytes = bytes;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            return new ServletInputStream() {
                public int read() {
                    return in.read();
                }

                public int read(byte[] b, int off, int len) {
                    return in.read(b, off, len);
                }

                public boolean isFinished() {
                    return in.available() == 0;
                }

                public boolean isReady() {
                    return true;
                }

                public void setReadListener(ReadListener l) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            String enc = getCharacterEncoding();
            Charset cs = enc == null ? StandardCharsets.UTF_8 : Charset.forName(enc);
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), cs));
        }

        @Override
        public int getContentLength() {
            return bytes.length;
        }

        @Override
        public long getContentLengthLong() {
            return bytes.length;
        }
    }
}
