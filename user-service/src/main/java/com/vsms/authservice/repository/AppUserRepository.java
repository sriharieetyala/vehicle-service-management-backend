package com.vsms.authservice.repository;

import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByRole(Role role);

    List<AppUser> findByRoleAndStatus(Role role, UserStatus status);

    long countByRole(Role role);

    long countByRoleAndStatus(Role role, UserStatus status);
}
