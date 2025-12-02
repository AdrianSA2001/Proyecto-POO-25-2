package pe.edu.uni.app.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String fullUrl = queryString == null ? uri : uri + "?" + queryString;
        
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.info("📥 REQUEST INCOMING");
        logger.info("   Method: {}", method);
        logger.info("   URI: {}", fullUrl);
        logger.info("   Origin: {}", httpRequest.getHeader("Origin"));
        logger.info("   Referer: {}", httpRequest.getHeader("Referer"));
        logger.info("   User-Agent: {}", httpRequest.getHeader("User-Agent"));
        
        // Log headers importantes
        Collections.list(httpRequest.getHeaderNames()).forEach(headerName -> {
            if (headerName.toLowerCase().contains("origin") || 
                headerName.toLowerCase().contains("access-control") ||
                headerName.toLowerCase().contains("content-type")) {
                logger.info("   Header {}: {}", headerName, httpRequest.getHeader(headerName));
            }
        });
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("📤 RESPONSE SENT");
            logger.info("   Status: {}", httpResponse.getStatus());
            logger.info("   Duration: {} ms", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ ERROR in filter chain");
            logger.error("   Exception: {}", e.getMessage());
            logger.error("   Duration: {} ms", duration);
            throw e;
        }
        
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}


