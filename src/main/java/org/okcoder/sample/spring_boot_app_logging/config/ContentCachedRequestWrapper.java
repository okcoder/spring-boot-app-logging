package org.okcoder.sample.spring_boot_app_logging.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.WebUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @see <a href="https://qiita.com/kazuki43zoo/items/757b557c05f548c6c5db">Spring MVC(+Spring Boot)上でのリクエスト共通処理の実装方法を理解する</a>
 * @see <a href="https://qiita.com/asachan/items/2e3b64b0fcb9d2dabe7c">Spring Boot + Spring SecurityでRequestのBodyを複数回読み取る方法</a>
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
    public BufferedReader getReader() {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
        }
        return this.reader;
    }

    @Override
    public ServletInputStream getInputStream() {
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

    private static class BufferedServletInputStream extends ServletInputStream {
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
        public int read() {
            return this.byteArrayInputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return byteArrayInputStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return byteArrayInputStream.read(b, off, len);
        }
    }

}
