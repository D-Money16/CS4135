package elib.elib_gateway;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component 
public class JWTFilter extends OncePerRequestFilter{

    private JwtUtil jwtUtil;
    private final GatewayErrorResponseWriter errorResponseWriter;

    public JWTFilter(JwtUtil jwtUtil, GatewayErrorResponseWriter errorResponseWriter) {

        this.jwtUtil = jwtUtil;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal (HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String header = httpServletRequest.getHeader("Authorization");

        String path = httpServletRequest.getRequestURI();

        if (isPathPublic(path)) {

            filterChain.doFilter(httpServletRequest, httpServletResponse);

            return;
        }

        if (header != null && !header.startsWith("Bearer ")) {
            errorResponseWriter.write(httpServletRequest, httpServletResponse, HttpStatus.BAD_REQUEST, "Authorization header must use the Bearer scheme");
            return;
        }

        String token = extractToken(httpServletRequest);

        if (token == null){

            errorResponseWriter.write(httpServletRequest, httpServletResponse, HttpStatus.UNAUTHORIZED, "Missing token");

            return;
        }

        Claims claims;

        try {
            claims = jwtUtil.getClaims(token);
        } catch (RuntimeException ex) {
            errorResponseWriter.write(httpServletRequest, httpServletResponse, HttpStatus.UNAUTHORIZED, "Invalid token");
            return;
        }

        String role = claims.get("role", String.class);

        if (role == null || (!role.equals("STUDENT") && !role.equals("STAFF") && !role.equals("ADMIN"))) {

            errorResponseWriter.write(httpServletRequest, httpServletResponse, HttpStatus.UNAUTHORIZED, "Invalid role");

            return;
        }

        httpServletRequest.setAttribute("claims", claims);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

        private boolean isPathPublic(String path) {

            return path.startsWith("/api/user/auth");
        }
            private String extractToken(HttpServletRequest httpServletRequest) {

                String tokenHeader = httpServletRequest.getHeader("Authorization");

                if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                    
                    return tokenHeader.substring(7);
                }

                return null;
            }


    }