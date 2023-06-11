package org.okcoder.sample.spring_boot_app_logging.config;

import jakarta.annotation.Priority;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//@Priority(Ordered.HIGHEST_PRECEDENCE)
@Priority(-10000)
public class ContentCachedFilter extends OncePerRequestFilter {


    private final MultipartResolver multipartResolver;

    public ContentCachedFilter(@Qualifier(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME) MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (multipartResolver.isMultipart(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachedRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachedRequestWrapper.class);
        if (requestWrapper == null) {
            requestWrapper = new ContentCachedRequestWrapper(request);
        }

        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,ContentCachingResponseWrapper.class);
        if (responseWrapper == null) {
            responseWrapper = new ContentCachingResponseWrapper(response);
        }
        filterChain.doFilter(requestWrapper, responseWrapper);

        responseWrapper.copyBodyToResponse();
    }
}
