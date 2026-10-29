package cl.bcs.plataforma.adapter.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Background-job dispatcher stub.
 * Source: CallBatch — @WebServlet("/CallBatch") + @WebListener
 *
 * The CallBatch servlet queued JavaCall plugin jobs (loaded from the filesystem
 * via PluginLoader) in a single-threaded scheduled executor.  The plugin JAR
 * ("PLUGIN_OPTIMUS.jar") is not available in the modernized environment.
 *
 * The endpoint is preserved for API compatibility; invocations throw
 * UnsupportedOperationException (→ 503 via GlobalExceptionHandler) until
 * the plugin system is replaced with a proper task queue.
 */
@RestController
public class CallBatchController {

    private static final Logger log = LoggerFactory.getLogger(CallBatchController.class);

    /**
     * POST /CallBatch — queues a background JavaCall job (stub).
     *
     * @throws UnsupportedOperationException always — triggers 503
     */
    @PostMapping("/CallBatch")
    public ResponseEntity<String> callBatch(HttpServletRequest request,
                                             @RequestBody(required = false) String body) {
        throw new UnsupportedOperationException(
                "requires PLUGIN_OPTIMUS.jar or replacement task queue");
    }
}
