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
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {
    private final MultipartResolver multipartResolver;
    private final HttpRequestLoggingProperties properties;

    public HttpRequestLoggingInterceptor(@Qualifier(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME) MultipartResolver multipartResolver, HttpRequestLoggingProperties properties) {
        this.multipartResolver = multipartResolver;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod method = (HandlerMethod) handler;
        final Logger logger = LoggerFactory.getLogger(method.getBeanType().getName() + "." + method.getMethod().getName());

        logHttpHeaders(logger, request);

        logHttpBody(logger, request);

        return true;
    }

    private void logHttpBody(Logger logger, HttpServletRequest request) throws ServletException, IOException {
        if (request.getContentLength() <= 0) {
            return;
        }
        if (multipartResolver.isMultipart(request)) {
            for (Part part : request.getParts()) {
                logPart(logger, part.getContentType(), part.getName(), part.getInputStream(), request.getCharacterEncoding());
            }
        } else {
            ContentCachedRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachedRequestWrapper.class);
            logger.info("{},{}", wrapper.getContentType(), new String(wrapper.getCachedContent(), Charset.forName(wrapper.getCharacterEncoding())));
        }
    }

    private void logPart(Logger logger, String contentType, String name, InputStream inputStream, String characterEncoding) throws IOException {
        if (Objects.equals(MediaType.valueOf(contentType).getType(), MediaType.IMAGE_JPEG.getType())) {
            logger.info("{},{},{}", name, contentType, StreamUtils.copyToByteArray(inputStream));
        } else {
            logger.info("{},{},{}", name, contentType, StreamUtils.copyToString(inputStream, Charset.forName(characterEncoding)));
        }
    }

    private void logHttpHeaders(Logger logger, HttpServletRequest request) {
        final var headers = Collections.list(request.getHeaderNames())
                .stream()
                .filter(name -> !properties.getExcludes().contains(name))
                .collect(Collectors.toMap(name -> name, name -> getHeaderValue(request,name)));
        logger.info("logHttpHeaders {}", headers);
    }

    private String getHeaderValue(HttpServletRequest request, String name){
        return Collections.list(request.getHeaders(name))
                .stream()
                .collect(Collectors.joining(","));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
