package com.taekwandoback.repository;

import com.taekwandoback.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    long countByEmail(String email);
}
