package com.halvern.tollgate.platform.config;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Keeps springfox 3.0.0 starting now that actuator is on the classpath.
 *
 * <p>Actuator's endpoint mapping always uses {@code PathPatternParser}, which springfox 3.0.0 cannot read, so
 * startup fails in {@code documentationPluginsBootstrapper} even with the ant path matcher configured. This
 * removes the pattern-parser mappings from springfox's view. Springfox is no longer maintained; remove this
 * together with springfox when the API docs move to springdoc.
 *
 * @see <a href="https://github.com/springfox/springfox/issues/3462">springfox#3462</a>
 */
@Configuration
public class SpringfoxWorkaroundConfig {

    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    List<RequestMappingInfoHandlerMapping> mappings = handlerMappings(bean);
                    mappings.removeIf(mapping -> mapping.getPatternParser() != null);
                }
                return bean;
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> handlerMappings(Object bean) {
                Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                if (field == null) {
                    throw new IllegalStateException("springfox internals changed: no handlerMappings field");
                }
                ReflectionUtils.makeAccessible(field);
                return (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(field, bean);
            }
        };
    }
}
