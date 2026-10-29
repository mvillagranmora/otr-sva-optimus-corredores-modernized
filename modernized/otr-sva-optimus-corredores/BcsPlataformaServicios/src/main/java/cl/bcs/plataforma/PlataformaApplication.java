package cl.bcs.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import cl.bcs.plataforma.service.JavaCallService;

/**
 * D-4 / DEV-A1 (plan step 2): the 326 skeleton classes {@code service/**}{@code /*Service implements JavaCallService} answered 200 with
 * invented data; they are taken OUT of the Spring context (not deleted: they stay in the source tree for wave B) by excluding every
 * {@link JavaCallService} from the component scan. The classes this module really serves are {@code JavaCallHandler} beans
 * ({@code adapter.in.javacall}). The first two filters are the standard ones of {@code @SpringBootApplication}, which an explicit
 * {@code @ComponentScan} replaces.
 */
@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JavaCallService.class)
})
public class PlataformaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaApplication.class, args);
    }
}
