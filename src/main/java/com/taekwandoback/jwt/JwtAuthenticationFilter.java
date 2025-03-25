package bibid.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 요청마다 JWT를 검증하고, 인증이 성공하면 SecurityContext에 등록한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    /**
     * [1] 요청에서 JWT 토큰을 추출하는 메서드
     * - HttpOnly 쿠키에서 ACCESS_TOKEN을 찾아 반환한다.
     */
    private String parseBearerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) { // 쿠키 이름이 "ACCESS_TOKEN"일 경우
                    return cookie.getValue(); // 토큰 값 반환
                }
            }
        }
        return null; // 토큰이 없으면 null 반환
    }

    /**
     * [2] 요청마다 실행되는 JWT 검증 및 Security Context 설정 메서드
     * - JWT 토큰을 파싱하고 검증한 후, SecurityContext에 인증 정보를 저장한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // [2-1] 요청에서 JWT 토큰을 가져온다.
            String token = parseBearerToken(request);

            // [2-2] 토큰이 존재하고 유효할 경우, 인증 절차를 진행한다.
            if (token != null && !token.equalsIgnoreCase("null")) {
                // [2-3] 토큰의 유효성 검사 및 사용자명 추출
                String username = jwtProvider.validateAndGetSubject(token);
                log.info("Username from token: {}", username);

                // [2-4] 데이터베이스에서 사용자 정보를 가져옴
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.info("Loaded user details: {}", userDetails);

                // [2-5] 인증 객체 생성 (비밀번호는 null)
                AbstractAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // [2-6] SecurityContext에 인증 객체 등록
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authenticationToken);
                SecurityContextHolder.setContext(securityContext);
            }
        } catch (Exception e) {
            log.error("set security context error: {}", e.getMessage());
        }

        // [2-7] 필터 체인을 통해 요청을 다음 필터로 전달
        filterChain.doFilter(request, response);
    }
}