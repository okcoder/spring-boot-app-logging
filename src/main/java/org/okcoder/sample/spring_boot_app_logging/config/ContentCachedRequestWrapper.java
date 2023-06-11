package org.okcoder.sample.spring_boot_app_logging.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.WebUtils;

import java.io.*;

/**
 * @refs https://qiita.com/kazuki43zoo/items/757b557c05f548c6c5db
 * @see org.springframework.web.util.ContentCachingRequestWrapper
 */
public class ContentCachedRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cache;
    private BufferedServletInputStream inputStream;
    private BufferedReader reader;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public ContentCachedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        cache = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
        }
        return this.reader;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new BufferedServletInputStream(new ByteArrayInputStream(this.cache));
        }
        return inputStream;
    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return (enc != null ? enc : WebUtils.DEFAULT_CHARACTER_ENCODING);
    }

    public byte[] getCachedContent() {
        return this.cache;
    }

    private class BufferedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        public BufferedServletInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.byteArrayInputStream = byteArrayInputStream;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {

        }

        @Override
        public int read() throws IOException {
            return this.byteArrayInputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return byteArrayInputStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return byteArrayInputStream.read(b, off, len);
        }
    }

}
