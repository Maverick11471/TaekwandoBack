package com.taekwandoback.dto;

import com.taekwandoback.entity.Member;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class MemberDto {

    private Long memberIndex;
    private String email;
    private String password;
    private String secondPassword;
    private String username;
    private LocalDate birthday;
    private String role;

    private boolean approvedByMaster;


    public Member toEntity() {
        return Member.builder()
            .memberIndex(this.memberIndex)
            .email(this.email)
            .password(this.password)
            .secondPassword(this.secondPassword)
            .username(this.username)
            .birthday(this.birthday)
            .approvedByMaster(this.approvedByMaster)
            .role(this.role)

            .build();
    }
}
