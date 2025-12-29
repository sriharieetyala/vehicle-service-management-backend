package com.vsms.authservice.repository;

import com.vsms.authservice.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByUserId(Integer userId);

    Optional<Admin> findByUserEmail(String email);

    List<Admin> findByIsSuperAdminTrue();

    long count();
}
