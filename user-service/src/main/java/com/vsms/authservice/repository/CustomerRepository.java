package com.vsms.authservice.repository;

import com.vsms.authservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByUserId(Integer userId);

    Optional<Customer> findByUserEmail(String email);

    long count();
}
