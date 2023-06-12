package org.okcoder.sample.spring_boot_app_logging.logging;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.okcoder.sample.spring_boot_app_logging.config.ContentCachedRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {
    private final MultipartResolver multipartResolver;
    private final HttpLoggingProperties properties;

    public HttpLoggingInterceptor(@Qualifier(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME) MultipartResolver multipartResolver, HttpLoggingProperties properties) {
        this.multipartResolver = multipartResolver;
        this.properties = properties;
    }

    private Logger getLogger(Object handler) {
        HandlerMethod method = (HandlerMethod) handler;
        return LoggerFactory.getLogger(method.getBeanType().getName() + "." + method.getMethod().getName());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        final Logger logger = getLogger(handler);

        logResponseHeaders(logger, request);
        logRequestBody(logger, request);

        return true;
    }

    private void logRequestBody(Logger logger, HttpServletRequest request) throws ServletException, IOException {
        if (request.getContentLength() <= 0) {
            return;
        }
        if (multipartResolver.isMultipart(request)) {
            for (Part part : request.getParts()) {
                logPart(logger, part.getContentType(), part.getName(), part.getInputStream(), request.getCharacterEncoding());
            }
        } else {
            ContentCachedRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachedRequestWrapper.class);
            logger.info("RequestBody:{}", new String(wrapper.getCachedContent(), Charset.forName(wrapper.getCharacterEncoding())));
        }
    }

    private void logPart(Logger logger, String contentType, String name, InputStream inputStream, String characterEncoding) throws IOException {
        if (Objects.equals(MediaType.valueOf(contentType).getType(), MediaType.IMAGE_JPEG.getType())) {
            logger.info("RequestPart: {},{},{}", name, contentType, StreamUtils.copyToByteArray(inputStream));
        } else {
            logger.info("RequestPart: {},{},{}", name, contentType, StreamUtils.copyToString(inputStream, Charset.forName(characterEncoding)));
        }
    }

    private void logResponseHeaders(Logger logger, HttpServletRequest request) {
        final var headers = Collections.list(request.getHeaderNames())
                .stream()
                .filter(name -> !properties.getRequest().getExcludes().contains(name))
                .collect(Collectors.toMap(name -> name, name -> getHeaderValue(request, name)));
        logger.info("RequestHeaders {}", headers);
    }

    private String getHeaderValue(HttpServletRequest request, String name) {
        return String.join(",", Collections.list(request.getHeaders(name)));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

        final Logger logger = getLogger(handler);

        logResponseHeaders(logger, response);
        logResponseBody(logger, response);

    }

    private void logResponseHeaders(Logger logger, HttpServletResponse response) {
        final var headers = response.getHeaderNames()
                .stream()
                .filter(name -> !properties.getRequest().getExcludes().contains(name))
                .collect(Collectors.toMap(name -> name, name -> String.join(",", response.getHeaders(name))));
        logger.info("ResponseHeaders {}", headers);
    }

    private void logResponseBody(Logger logger, HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        logger.info("ResponseBody:{},{}", wrapper.getContentType(), new String(wrapper.getContentAsByteArray(), Charset.forName(wrapper.getCharacterEncoding())));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    }
}
