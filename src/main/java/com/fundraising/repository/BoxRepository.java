package com.fundraising.repository;

import com.fundraising.entity.Box;
import com.fundraising.enums.BoxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
    Optional<Box> findByBoxIdentifier(String boxIdentifier);
    List<Box> findByStatus(BoxStatus status);
    boolean existsByBoxIdentifier(String boxIdentifier);
}