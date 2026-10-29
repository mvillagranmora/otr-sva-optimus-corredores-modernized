package cl.bcs.reports;

import cl.bcs.reports.report.TempFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Startup housekeeping: (1) an ERROR in the log when INTERNAL_TOKEN is empty (the service still starts, fail closed:
 * every /report call answers 401, but the operator must see why); not logged under the "test" profile.
 * (2) sweep of stale temp files (*.rpt, *.png older than 1 hour) left by a process that died mid-render.
 */
@Component
public class StartupChecks {

    private static final Logger log = LoggerFactory.getLogger(StartupChecks.class);

    static final long STALE_MILLIS = 60L * 60L * 1000L;

    private final String token;
    private final Environment env;

    public StartupChecks(@Value("${services.internal-token:}") String token, Environment env) {
        this.token = token;
        this.env = env;
    }

    @PostConstruct
    void run() {
        if ((token == null || token.isEmpty()) && !env.acceptsProfiles(Profiles.of("test"))) {
            log.error("INTERNAL_TOKEN is empty: every protected call (/report) will be rejected with 401. "
                    + "Set INTERNAL_TOKEN to the shared secret of the callers.");
        }
        TempFiles.sweep(STALE_MILLIS);
    }
}
