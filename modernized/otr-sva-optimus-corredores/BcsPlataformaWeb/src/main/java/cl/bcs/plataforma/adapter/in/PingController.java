package cl.bcs.plataforma.adapter.in;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health-check / throughput probe.
 * Source: PingServlet — @WebServlet("/Ping")
 */
@RestController
public class PingController {

    /**
     * GET /Ping — optionally sleeps {@code sleep} seconds before responding.
     * Source: PingServlet.doGet — Thread.sleep(sleep_seconds * 1000)
     */
    @GetMapping("/Ping")
    public String ping(@RequestParam(value = "sleep", defaultValue = "0") long sleepSeconds)
            throws InterruptedException {
        if (sleepSeconds > 0) {
            Thread.sleep(sleepSeconds * 1_000L);
        }
        return "Served at: /";
    }

    @PostMapping("/Ping")
    public String pingPost(@RequestParam(value = "sleep", defaultValue = "0") long sleepSeconds)
            throws InterruptedException {
        return ping(sleepSeconds);
    }
}
