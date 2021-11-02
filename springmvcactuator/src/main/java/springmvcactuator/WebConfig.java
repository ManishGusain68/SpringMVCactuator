package springmvcactuator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "springmvcactuator.controller")
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Configuration
	@Import({ EndpointAutoConfiguration.class, HealthIndicatorAutoConfiguration.class,

			InfoEndpointAutoConfiguration.class, HealthEndpointAutoConfiguration.class,

			WebEndpointAutoConfiguration.class, ServletManagementContextAutoConfiguration.class,
			ManagementContextAutoConfiguration.class, })
	@EnableConfigurationProperties(CorsEndpointProperties.class)
	class ActuatorConfiguration {

		@Bean // taken from WebMvcEndpointManagementContextConfiguration.class
		public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
				ServletEndpointsSupplier servletEndpointsSupplier,
				ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes,
				CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties) {
			List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
			Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
			allEndpoints.addAll(webEndpoints);
			allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
			allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
			EndpointMapping endpointMapping = new EndpointMapping(webEndpointProperties.getBasePath());
			return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
					corsProperties.toCorsConfiguration(),
					new EndpointLinksResolver(allEndpoints, webEndpointProperties.getBasePath()));
		}

		@Bean
		DispatcherServletPath dispatcherServletPath() {
			return () -> "/";
		}

	}

	/**
	 * Configure a handler to delegate unhandled requests by forwarding to the
	 * Servlet container's "default" servlet. A common use case for this is when
	 * the {@link DispatcherServlet} is mapped to "/" thus overriding the
	 * Servlet container's default handling of static resources.
	 */
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
}