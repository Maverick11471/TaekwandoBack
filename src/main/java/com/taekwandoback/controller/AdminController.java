package com.taekwandoback.controller;

import com.taekwandoback.entity.Member;
import com.taekwandoback.repository.MemberRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/batch-register")
    public ResponseEntity<?> registerFromCsv() throws IOException {
        Path path = Paths.get("src/main/resources/test.csv");
        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 6) {
                continue;
            }

            String email = parts[0].trim();
            String rawPassword = parts[1].trim();
            String username = parts[2].trim();
            String birthdayStr = parts[3].trim(); // e.g. 2000-01-01
            String role = parts[4].trim();
            String secondPassword = parts[5].trim();

            Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .username(username)
                .birthday(LocalDate.parse(birthdayStr)) // <-- 중요
                .role(role)
                .secondPassword(secondPassword)
                .approvedByMaster(true)
                .build();

            memberRepository.save(member);
        }

        return ResponseEntity.ok("회원 등록 완료!");
    }


}
