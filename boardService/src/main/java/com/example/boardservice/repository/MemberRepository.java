package com.example.boardservice.repository;

import com.example.boardservice.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);
    Member findByNickname(String nickname);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

}
