package cl.bcs.fondomutuo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * D-4 / DEV-F1 (plan step 2, paso 0 de higiene): the legacy skeleton of this module answered 200 with invented data. Its classes are taken OUT of the
 * Spring context, not deleted (the files stay in the tree for the later waves, to be removed in F2): the 132 {@code service.javacall.*Service}, the other
 * {@code service.*} stubs, the old controllers {@code adapter.in.web.*} (typed REST with invented logic and the old {@code InternalJavaCallController}
 * that injected the 132), the old HANA adapters {@code adapter.out.hana.*} and the stub {@code GlobalExceptionHandler} (it echoed exception messages).
 * What the module really serves is registered explicitly: {@code adapter.in.javacall.*} (allow-list of 2 FQN), the kit ports and their adapters.
 * The first two filters are the standard ones of {@code @SpringBootApplication}, which an explicit {@code @ComponentScan} replaces. The test
 * {@code StubsFueraDelContextoTest} fails if any excluded class is registered.
 *
 * <p>Tesoreria's library (F2, C2) will be imported by an explicit configuration naming only the beans it needs; {@code cl.bcs.tesoreria} is never
 * component-scanned here.
 */
@SpringBootApplication
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = BcsFondoMutuoApplication.STUBS)
})
public class BcsFondoMutuoApplication {

    /** Class-name regex of everything that stays out of the Spring context (also used by the tests). */
    public static final String STUBS = "cl\\.bcs\\.fondomutuo\\.(service|adapter\\.in\\.web|adapter\\.out\\.hana)\\..*"
            + "|cl\\.bcs\\.fondomutuo\\.config\\.GlobalExceptionHandler";

    public static void main(String[] args) {
        SpringApplication.run(BcsFondoMutuoApplication.class, args);
    }
}
