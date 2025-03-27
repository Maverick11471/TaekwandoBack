package com.taekwandoback.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import com.taekwandoback.entity.Member;

@Component  // 🅰️ Spring Bean으로 등록
public class JwtProvider {

    private static final String SECRET_KEY = "Yml0Y2FtcGRldm9wczEydG9kb2Jvb3RhcHA1MDJyZWFjdHNwcmluZ2Jvb3Q=";  // 🔐 JWT 서명에 사용할 비밀 키

    SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));  // 🔧 비밀 키를 바이트 배열로 변환하여 생성

    // [1] JWT 토큰 생성 메서드 (회원 정보를 기반으로 JWT를 생성)
    public String createJwt(Member member) {

        Date expireDate = Date.from(Instant.now().plus(100, ChronoUnit.DAYS));  // 🔧 만료일을 현재 시간에서 100일 후로 설정

        // 🔧 JWT 생성 (HS256 알고리즘 사용)
        return Jwts.builder()
            .signWith(key, SignatureAlgorithm.HS256)  // 🔐 비밀 키로 서명
            .setSubject(String.valueOf(member.getMemberIndex())) // 🔧 JWT에 담을 회원 정보 (여기선 memberId)
            .issuer("bestTaekwando")  // 🔧 발급자 정보
            .issuedAt(new Date())  // 🔧 발급 일자
            .expiration(expireDate)  // 🔧 만료 일자
            .compact();  // 🔧 JWT 문자열로 반환
    }

   // [2] JWT 유효성 검사 및 주인(subject) 정보 반환
    public String validateAndGetSubject(String token) {
        Claims claims = Jwts.parser()  // 🔧 JWT 파서 생성
            .verifyWith(key)  // 🔧 비밀 키로 서명 검증
            .build()
            .parseSignedClaims(token)  // 🔧 JWT 토큰의 서명 및 유효성 검증
            .getPayload();  // 🔧 JWT의 페이로드 추출 (토큰 내용)

        return claims.getSubject();  // 🔧 페이로드에서 주인(subject) 정보 반환 (회원 ID)
    }
}
