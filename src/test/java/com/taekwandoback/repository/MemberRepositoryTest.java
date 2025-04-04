package com.taekwandoback.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.taekwandoback.entity.Member;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private Member createTestMember(String email) {
        return Member.builder()
            .email(email)
            .password("ValidPass123!")
            .secondPassword("ValidPass123!")
            .birthday(LocalDate.of(2000, 1, 1))
            .username("홍길동")
            .role("ROLE_USER")
            .approvedByMaster(false)
            .build();
    }

    @BeforeEach
    void setUp() {
        Member member = createTestMember("test@example.com");
        entityManager.persistAndFlush(member);
    }

    @Test
    void countByEmail_WhenEmailExists_ShouldReturn1() {
        // When
        long count = memberRepository.countByEmail("test@example.com");
        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findByEmail_WhenEmailExists_ShouldReturnMember() {
        // Given
        Member member = createTestMember("test2@example.com");
        entityManager.persistAndFlush(member);

        // When
        Optional<Member> found = memberRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void saveMember_ShouldPersistCorrectly() {
        // Given
        Member member = createTestMember("new@example.com");

        // When
        Member saved = memberRepository.save(member);

        // Then
        assertThat(saved.getMemberIndex()).isNotNull();
        assertThat(saved.getBirthday()).isEqualTo(member.getBirthday());
    }
}