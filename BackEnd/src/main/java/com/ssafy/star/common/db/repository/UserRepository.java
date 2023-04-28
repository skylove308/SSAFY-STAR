package com.ssafy.star.common.db.repository;

import com.ssafy.star.common.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserId(String userId);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByAccountId(String accountId);
}
