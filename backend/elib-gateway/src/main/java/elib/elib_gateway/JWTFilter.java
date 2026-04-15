package elib.elib_gateway;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component 
public class JWTFilter extends OncePerRequestFilter{

    private JwtUtil jwtUtil;

    public JWTFilter(JwtUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal (HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String header = httpServletRequest.getHeader("Authorization");

        String path = httpServletRequest.getRequestURI();

        if (isPathPublic(path)) {

            filterChain.doFilter(httpServletRequest, httpServletResponse);

            return;
        }

        String token = extractToken(httpServletRequest);

        if (token == null){

            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            httpServletResponse.getWriter().write("Missing token");

            return;
        }

        if (!jwtUtil.validateToken(token)) {

            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            httpServletResponse.getWriter().write("Invalid token");

            return;
        }

        Claims claims = jwtUtil.getClaims(token);
        String role = claims.get("role", String.class);

        if (role == null || (!role.equals("STUDENT") && !role.equals("STAFF") && !role.equals("ADMIN"))) {

            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            httpServletResponse.getWriter().write("Invalid role");

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