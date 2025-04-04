package com.taekwandoback.controller;

import com.taekwandoback.dto.EmailDto;
import com.taekwandoback.dto.MailCheckRequestDto;
import com.taekwandoback.dto.MemberDto;
import com.taekwandoback.dto.ResponseDto;
import com.taekwandoback.jwt.JwtProvider;
import com.taekwandoback.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final Map<String, Integer> emailCodeMap = new ConcurrentHashMap<>();
    private final JwtProvider jwtProvider;

    @PostMapping("/emails/email-duplicate-check")
    public ResponseEntity<?> emailCheck(@RequestBody MemberDto memberDto) {
        ResponseDto<Map<String, String>> responseDto = new ResponseDto<>();
        Map<String, String> map = memberService.emailCheck(memberDto.getEmail());

        try {
            log.info("email:{}", memberDto.getEmail());

            responseDto.setItem(map);
            responseDto.setStatusCode(200);
            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok().body(responseDto);

        } catch (Exception e) {
            log.error("email-check error:{}", e.getMessage());

            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setErrorMessage(e.getMessage());

            return ResponseEntity.internalServerError().body(responseDto);
        }
    }

    @PostMapping("/emails/send-email")
    public ResponseEntity<?> sendEmail(@RequestBody EmailDto emailDto) {

        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>();

        try {
            Map<String, Object> sendEmailMsg = memberService.sendEmail(emailDto.getEmail());

            if ("SendEmail Success".equals(sendEmailMsg.get("sendEmailMsg"))) {
                String email = emailDto.getEmail();
                int verificationCode = (int) sendEmailMsg.get("verificationCode");
                emailCodeMap.put(email, verificationCode);

                log.info("verificationCode: {}", verificationCode);
            }

            responseDto.setStatusCode(200);
            responseDto.setItem(sendEmailMsg);
            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok().body(responseDto);
        } catch (Exception e) {

            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setErrorMessage(e.getMessage());

            return ResponseEntity.internalServerError().body(responseDto);
        }
    }

    @PostMapping("/emails/email-verification-code-check")
    public ResponseEntity<?> emailVerificationCodeCheck(
        @RequestBody MailCheckRequestDto mailCheckRequestDto) {

        try {
            String email = mailCheckRequestDto.getEmail();
            String enteredCode = mailCheckRequestDto.getEnteredCode();

            if (email == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이메일 입력이 필요합니다.");
            }

            if (enteredCode == null || email == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("인증코드와 이메일을 확인해주세요.");
            }

            Integer savedCode = emailCodeMap.get(email);

            if (savedCode == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("인증코드가 만료되었습니다.");
            }

            boolean isMatch = String.valueOf(savedCode).equals(enteredCode);
            log.info("인증 시도: {} -> {}", enteredCode, savedCode);
            log.info("isMatch: {}", isMatch);

            if (isMatch) {
                emailCodeMap.remove(email);
            }

            return ResponseEntity.ok(isMatch);
        } catch (Exception e) {

            log.error("verificationCodeError: {}", e.getMessage());

            return ResponseEntity.internalServerError().body("인증코드에 오류가 있습니다.");

        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody MemberDto memberDto) {

        ResponseDto<MemberDto> responseDto = new ResponseDto<>();

        try {
            log.info(memberDto.toString());
            MemberDto joinMemberDto = memberService.join(memberDto);

            responseDto.setStatusCode(200);
            responseDto.setStatusMessage("created");
            responseDto.setItem(joinMemberDto);

            return ResponseEntity.ok().body(responseDto);

        } catch (Exception e) {
            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setErrorMessage(e.getMessage());
            log.error("Join error: {}", e.getMessage());

            return ResponseEntity.internalServerError().body(responseDto);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberDto memberDto, HttpServletResponse response) {
        ResponseDto<MemberDto> responseDto = new ResponseDto<>();

        try {

            MemberDto loginMember = memberService.login(memberDto);
            log.info("loginMember: {}", loginMember);

            String jwtToken = jwtProvider.createJwt(loginMember.toEntity());
            log.info("jwtToken: {}, jwtToken");

            StringBuilder cookieHeader = new StringBuilder(
                "ACCESS_TOKEN=" + jwtToken + "; Path=/; HttpOnly; ");
            cookieHeader.append("Secure; SameSite=None");
            log.info("Cookie header: {}", cookieHeader.toString());
            response.addHeader("Set-Cookie", cookieHeader.toString());

            responseDto.setStatusCode(HttpStatus.OK.value());
            responseDto.setStatusMessage("ok");
            responseDto.setItem(loginMember);

            return ResponseEntity.ok().body(responseDto);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setErrorMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(responseDto);
        }

    }

    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@RequestBody MemberDto memberDto) {

        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>();

        try {
            MemberDto findMember = memberService.findPassword(memberDto);
            Map<String, Object> sendEmailMsg = memberService.sendEmail(findMember.getEmail());

            if ("SendEmail Success".equals(sendEmailMsg.get("sendEmailMsg"))) {
                String email = findMember.getEmail();
                int verificationCode = (int) sendEmailMsg.get("verificationCode");
                emailCodeMap.put(email, verificationCode);

                log.info("verificationCode: {}", verificationCode);
            }

            responseDto.setStatusCode(200);

            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok().body(responseDto);
        } catch (Exception e) {

            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setErrorMessage(e.getMessage());

            return ResponseEntity.internalServerError().body(responseDto);
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody MemberDto memberDto) {

        ResponseDto<MemberDto> responseDto = new ResponseDto<>();

        try {
            MemberDto updateMember = memberService.updatePassword(memberDto);

            responseDto.setItem(updateMember);
            responseDto.setStatusMessage("update");
            responseDto.setStatusCode(200);

            log.info("updateMember:{}", updateMember);

            return ResponseEntity.ok().body(responseDto);
        } catch (Exception e) {

            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setErrorMessage(e.getMessage());

            log.error(e.getMessage());

            return ResponseEntity.internalServerError().body(responseDto);
        }
    }

}
