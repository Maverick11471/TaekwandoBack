package com.taekwandoback.service;

import com.taekwandoback.repository.MemberRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Map<String, String> emailCheck(String email){

        Map<String, String> emailCheckMsgMap = new HashMap<>();

        long emailCheckNum = memberRepository.countByEmail(email);
        if(emailCheckNum == 0){
            emailCheckMsgMap.put("memberEmailCheckMsg", "available email");
        } else {
            emailCheckMsgMap.put("memberEmailCheckMsg", "invalid email");
        }

        return emailCheckMsgMap;

    }
}
