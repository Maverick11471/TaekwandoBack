package com.taekwandoback.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taekwandoback.dto.EmailDto;
import com.taekwandoback.dto.MailCheckRequestDto;
import com.taekwandoback.dto.MemberDto;
import com.taekwandoback.jwt.JwtProvider;
import com.taekwandoback.service.MemberService;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock  // @MockBean 대신 @Mock 사용
    private MemberService memberService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private MemberController memberController;

    private final Map<String, Integer> emailCodeMap = new ConcurrentHashMap<>();

    // BeforeEach 메서드 수정
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // final 필드만 주입
        memberController = new MemberController(memberService, jwtProvider);

        // emailCodeMap 강제 주입 (리플렉션)
        Field emailCodeMapField = MemberController.class.getDeclaredField("emailCodeMap");
        emailCodeMapField.setAccessible(true);
        emailCodeMapField.set(memberController, new ConcurrentHashMap<>());

        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @Test
    void emailCheck() throws Exception {
        // given
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail("test@example.com");

        Mockito.when(memberService.emailCheck("test@example.com"))
            .thenReturn(Map.of("duplicate", "false"));

        // when & then
        mockMvc.perform(post("/members/emails/email-duplicate-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberDto)))
            .andExpect(status().isOk());
    }

    @Test
    void sendEmail() throws Exception {
        // Given
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("test@example.com");
        Map<String, Object> mockResponse = Map.of("sendEmailMsg", "SendEmail Success",
            "verificationCode", 123456);

        Mockito.when(memberService.sendEmail("test@example.com"))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/members/emails/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.item.sendEmailMsg").value("SendEmail Success"));
    }

    @Test
    void emailVerificationCodeCheck() throws Exception {
        // Given
        String email = "test@example.com";
        String validCode = "123456";

        // 리플렉션으로 emailCodeMap 직접 주입
        Field emailCodeMapField = MemberController.class.getDeclaredField("emailCodeMap");
        emailCodeMapField.setAccessible(true);
        Map<String, Integer> emailCodeMap = (Map<String, Integer>) emailCodeMapField.get(
            memberController);
        emailCodeMap.put(email, 123456); // Integer로 저장

        MailCheckRequestDto requestDto = new MailCheckRequestDto();
        requestDto.setEmail(email);
        requestDto.setEnteredCode(validCode);

        // When & Then
        mockMvc.perform(post("/members/emails/email-verification-code-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print()) // 응답 전체 출력
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void join() throws Exception {
        // Given
        MemberDto requestDto = new MemberDto();
        requestDto.setEmail("new@example.com");
        requestDto.setPassword("password123");

        MemberDto mockResponse = new MemberDto();
        mockResponse.setEmail(requestDto.getEmail());

        // any()로 변경
        Mockito.when(memberService.join(Mockito.any(MemberDto.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.item.email").value("new@example.com"));
    }

    @Test
    void login() throws Exception {
        // Given
        MemberDto loginDto = new MemberDto();
        loginDto.setEmail("user@example.com");
        loginDto.setPassword("password123");

        MemberDto mockMember = new MemberDto();
        mockMember.setEmail(loginDto.getEmail());
        String mockToken = "mock.jwt.token";

        Mockito.when(memberService.login(loginDto)).thenReturn(mockMember);
        Mockito.when(jwtProvider.createJwt(Mockito.any())).thenReturn(mockToken);

        // When & Then
        MockHttpServletResponse response = mockMvc.perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"))
            .andExpect(jsonPath("$.item.email").value("user@example.com"))
            .andReturn().getResponse();

        // Cookie 검증
        String cookieHeader = response.getHeader("Set-Cookie");
        assert cookieHeader != null;
        assert cookieHeader.contains("ACCESS_TOKEN=mock.jwt.token");
    }

    @Test
    void findPassword() throws Exception {
        // Given
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail("user@example.com");

        MemberDto mockMember = new MemberDto();
        mockMember.setEmail(memberDto.getEmail());

        Mockito.when(memberService.findPassword(memberDto)).thenReturn(mockMember);
        Mockito.when(memberService.sendEmail(memberDto.getEmail()))
            .thenReturn(Map.of("sendEmailMsg", "SendEmail Success", "verificationCode", 654321));

        // When & Then
        mockMvc.perform(post("/members/find-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void updatePassword() throws Exception {
        // Given
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail("user@example.com");
        memberDto.setPassword("newPassword123");

        MemberDto mockResponse = new MemberDto();
        mockResponse.setEmail(memberDto.getEmail());

        Mockito.when(memberService.updatePassword(memberDto)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/members/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.item.email").value("user@example.com"));
    }
}