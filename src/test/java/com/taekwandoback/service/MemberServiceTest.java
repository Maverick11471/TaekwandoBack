package com.taekwandoback.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taekwandoback.dto.MemberDto;
import com.taekwandoback.entity.Member;
import com.taekwandoback.repository.MemberRepository;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MemberService memberService;

    private MemberDto testMemberDto;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testMemberDto = new MemberDto();
        testMemberDto.setEmail("test@example.com");
        testMemberDto.setPassword("password123");
        testMemberDto.setSecondPassword("password123");
        testMemberDto.setUsername("홍길동");

        testMember = testMemberDto.toEntity();
        testMember.setMemberIndex(1L);
    }

    @Test
    void emailCheck_AvailableEmail() {
        // Given
        when(memberRepository.countByEmail(anyString())).thenReturn(0L);

        // When
        Map<String, String> result = memberService.emailCheck("new@example.com");

        // Then
        assertThat(result.get("memberEmailCheckMsg")).isEqualTo("available email");
        verify(memberRepository).countByEmail("new@example.com");
    }

    @Test
    void sendEmail_Success() throws Exception {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        Map<String, Object> result = memberService.sendEmail("test@example.com");

        // Then
        assertThat(result.get("sendEmailMsg")).isEqualTo("SendEmail Success");
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void join_Success() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // When
        MemberDto result = memberService.join(testMemberDto);

        // Then
        assertThat(result.getEmail()).isEqualTo(testMemberDto.getEmail());
        assertThat(result.getPassword()).isEmpty();
        verify(passwordEncoder, times(2)).encode(anyString());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void login_Success() {
        // Given
        when(memberRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When
        MemberDto result = memberService.login(testMemberDto);

        // Then
        assertThat(result.getEmail()).isEqualTo(testMemberDto.getEmail());
        assertThat(result.getPassword()).isEmpty();
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    void findPassword_Success() {
        // Given
        when(memberRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(testMember));

        // When
        MemberDto result = memberService.findPassword(testMemberDto);

        // Then
        assertThat(result.getEmail()).isEqualTo(testMemberDto.getEmail());
        assertThat(result.getPassword()).isEmpty();
        verify(memberRepository).findByEmail(anyString());
    }

    @Test
    void updatePassword_Success() {
        // Given
        when(memberRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(testMember));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // When
        MemberDto result = memberService.updatePassword(testMemberDto);

        // Then
        assertThat(result.getEmail()).isEqualTo(testMemberDto.getEmail());
        verify(passwordEncoder).encode(anyString());
        verify(memberRepository).findByEmail(anyString());
    }

    // 예외 케이스 테스트 (assertThrows 사용)
    @Test
    void login_WrongPassword() {
        // Given
        when(memberRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            memberService.login(testMemberDto);
        });
    }

    @Test
    void findPassword_InvalidUser() {
        // Given
        when(memberRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(testMember));
        testMemberDto.setUsername("잘못된이름");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            memberService.findPassword(testMemberDto);
        });
    }
}
