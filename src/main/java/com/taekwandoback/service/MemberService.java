package com.taekwandoback.service;

import com.taekwandoback.dto.MemberDto;
import com.taekwandoback.entity.Member;
import com.taekwandoback.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    private static int number;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String username;

    public Map<String, String> emailCheck(String email) {

        Map<String, String> emailCheckMsgMap = new HashMap<>();

        long emailCheckNum = memberRepository.countByEmail(email);
        if (emailCheckNum == 0) {
            emailCheckMsgMap.put("memberEmailCheckMsg", "available email");
        } else {
            emailCheckMsgMap.put("memberEmailCheckMsg", "invalid email");
        }

        return emailCheckMsgMap;

    }

    public static void createNumber() {
        number = (int) (Math.random() * (90000)) + 100000;
    }

    public MimeMessage createMail(String email) {
        createNumber();

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(username);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[경희최강태권도] 요청하신 통합회원 인증번호를 안내해드립니다.");

            String body = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                "h1 { color: #333; }" +
                "h3 { color: #555; }" +
                ".footer { margin-top: 20px; font-size: 12px; color: #888; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h3>이메일 인증코드</h3>" +
                "<p>경희대 최강 태권도에 입력한 이메일 주소가 올바른지 확인하기 위한 인증번호입니다.\n</p>" +
                "<p>아래의 인증번호를 복사하여 이메일 인증을 완료해주세요.</p>" +
                "<h1>" + number + "</h1>" +
                "<p>감사합니다.</p>" +
                "<div class='footer'>" +
                "</div>" +
                "</body>" +
                "</html>";
            message.setText(body, "UTF-8", "html");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public Map<String, Object> sendEmail(String mail) {

        Map<String, Object> sendEmailMsg = new HashMap<>();

        try {
            MimeMessage message = createMail(mail);
            javaMailSender.send(message);

            sendEmailMsg.put("sendEmailMsg", "SendEmail Success");
            sendEmailMsg.put("verificationCode", number);
        } catch (MailException e) {
            sendEmailMsg.put("sendEmailMsg", "SendEmail Fail");

            throw new RuntimeException(e);
        }

        return sendEmailMsg;
    }

    public MemberDto join(MemberDto joinDto) {

        joinDto.setRole("ROLE_USER");
        joinDto.setApprovedByMaster(false);

        joinDto.setPassword(passwordEncoder.encode(joinDto.getPassword()));
        joinDto.setSecondPassword(passwordEncoder.encode(joinDto.getSecondPassword()));

        Member member = joinDto.toEntity();

        Member joinMember = memberRepository.save(member);

        MemberDto joinMemberDto = joinMember.toDto();
        joinMemberDto.setPassword("");
        joinMemberDto.setSecondPassword("");

        return joinMemberDto;

    }

    public MemberDto login(MemberDto memberDto) {

        Member member = memberRepository.findByEmail(memberDto.getEmail())
            .orElseThrow(() -> new RuntimeException("email not found"));

        if (!passwordEncoder.matches(memberDto.getPassword(), member.getPassword())) {
            throw new RuntimeException("wrong password");
        }

        MemberDto loginMember = member.toDto();

        loginMember.setPassword("");
        loginMember.setSecondPassword("");

        return loginMember;

    }

    public MemberDto findPassword(MemberDto memberDto) {

        Member member = memberRepository.findByEmail(memberDto.getEmail())
            .orElseThrow(() -> new RuntimeException("email not found"));

        if (!member.getUsername().equals(memberDto.getUsername())) {
            throw new RuntimeException("The name associated with the email does not match");
        }

        MemberDto findMember = member.toDto();

        findMember.setPassword("");
        findMember.setSecondPassword("");

        return findMember;

    }

    public MemberDto updatePassword(MemberDto memberDto) {

        Member member = memberRepository.findByEmail(memberDto.getEmail())
            .orElseThrow(() -> new RuntimeException("email not found"));

        member.setPassword(passwordEncoder.encode(memberDto.getPassword()));

        MemberDto updateMember = member.toDto();

        updateMember.setPassword("");
        updateMember.setSecondPassword("");

        return updateMember;
    }


}
