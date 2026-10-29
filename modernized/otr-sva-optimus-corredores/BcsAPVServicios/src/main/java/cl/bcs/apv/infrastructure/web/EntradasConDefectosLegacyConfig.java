package cl.bcs.apv.infrastructure.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tools.jackson.databind.json.JsonMapper;

/** Wires {@link EntradasConDefectosLegacy} into Spring MVC: body deserializers on the JSON converter and the resolver of the GET query string. */
@Configuration
public class EntradasConDefectosLegacyConfig implements WebMvcConfigurer {

    private volatile JsonMapper mapper;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> convertidores) {
        mapper = EntradasConDefectosLegacy.instalar(convertidores);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new EntradasConDefectosLegacy.ResolverQuery(() -> mapper));
    }
}
