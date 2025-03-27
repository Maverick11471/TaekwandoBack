package com.taekwandoback.dto;

import com.taekwandoback.entity.Member;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MemberDto {
    private Long memberIndex;
    private String email;
    private String password;
    private String secondPassword;
    private String username;
    private LocalDate birthday;

    private boolean approvedByMaster;
    private boolean emailVerified; // 이메일 인증 여부 추가

    public Member toEntity(){
        return Member.builder()
            .memberIndex(this.memberIndex)
            .email(this.email)
            .password(this.password)
            .secondPassword(this.secondPassword)
            .username(this.username)
            .birthday(this.birthday)
            .approvedByMaster(this.approvedByMaster)

            .emailVerified(this.emailVerified)
            .build();
    }
}
