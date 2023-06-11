package org.okcoder.sample.spring_boot_app_logging.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "org.okcoder.logging.http")
public class HttpLoggingProperties {
    private HeaderProperties request = new HeaderProperties();
    private HeaderProperties response = new HeaderProperties();

    public HeaderProperties getRequest() {
        return request;
    }

    public void setRequest(HeaderProperties request) {
        this.request = request;
    }

    public HeaderProperties getResponse() {
        return response;
    }

    public void setResponse(HeaderProperties response) {
        this.response = response;
    }

    static class HeaderProperties {
        public Set<String> excludes = new HashSet<>();

        public Set<String> getExcludes() {
            return excludes;
        }

        public void setExcludes(Set<String> excludes) {
            this.excludes = excludes;
        }

    }
}
