package com.pathiful.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SepaMandateRepository extends JpaRepository<SepaMandate, Long> {

    Optional<SepaMandate> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
