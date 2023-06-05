package org.okcoder.sample.spring_boot_app_logging.config;

import org.okcoder.sample.spring_boot_app_logging.logging.HttpRequestLoggingInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {

    private final  HttpRequestLoggingInterceptor httpRequestLoggingInterceptor;

    public WebConfig(HttpRequestLoggingInterceptor httpRequestLoggingInterceptor) {
        this.httpRequestLoggingInterceptor = httpRequestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpRequestLoggingInterceptor).addPathPatterns("/**").order(1);
    }
}
