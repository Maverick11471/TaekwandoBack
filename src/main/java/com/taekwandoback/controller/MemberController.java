package com.taekwandoback.controller;

import com.taekwandoback.dto.MemberDto;
import com.taekwandoback.dto.ResponseDto;
import com.taekwandoback.repository.MemberRepository;
import com.taekwandoback.service.MemberService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/email-check")
    public ResponseEntity<?> emailCheck(@RequestBody MemberDto memberDto){
        ResponseDto<Map<String,String>> responseDto = new ResponseDto<>();
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

}
