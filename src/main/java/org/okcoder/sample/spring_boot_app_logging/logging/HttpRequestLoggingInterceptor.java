package org.okcoder.sample.spring_boot_app_logging.logging;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);
    private final MultipartResolver multipartResolver;
    private final HttpRequestLoggingProperties properties;

    public HttpRequestLoggingInterceptor(@Qualifier(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME) MultipartResolver multipartResolver, HttpRequestLoggingProperties properties) {
        this.multipartResolver = multipartResolver;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpServletRequest requestToUse = request;

        if (!(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        logHttpHeaders(requestToUse);

        if (requestToUse.getContentLength() > 0) {
            logHttpBody(requestToUse);
        }

        return true;
    }

    private void logHttpBody(HttpServletRequest request) throws ServletException, IOException {
        if (multipartResolver.isMultipart(request)) {
            for (Part part : request.getParts()) {
                logPart(part.getContentType(), part.getName(), part.getInputStream(),request.getCharacterEncoding());
            }
        } else {
            logPart(request.getContentType(), "-", request.getInputStream(),request.getCharacterEncoding());
        }
    }

    private void logPart(String contentType, String name, InputStream inputStream, String characterEncoding) {
        //MimeType.valueOf(contentType).includes()
        if (Objects.equals(MediaType.valueOf(contentType).getType(),MediaType.IMAGE_JPEG.getType())){

        }else{

        }
    }

    private void logHttpHeaders(HttpServletRequest request) {
        final var headers = Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !properties.getExcludes().contains(name))
                .collect(Collectors.toMap(name -> name, name -> Collections.list(request.getHeaders(name))));
        logger.info("logHttpHeaders {}", headers);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
