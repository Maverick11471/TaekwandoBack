package com.taekwandoback.entity;

import com.taekwandoback.dto.MemberDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberIndex")
    private Long memberIndex;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String secondPassword; // 부모용 2차 비밀번호

    @Column(nullable = false)
    private String username;


    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    private boolean approvedByMaster = false;

    private String role;

    public MemberDto toDto() {
        return MemberDto.builder()
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
