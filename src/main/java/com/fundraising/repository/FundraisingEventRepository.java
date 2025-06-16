package com.fundraising.repository;

import com.fundraising.entity.FundraisingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundraisingEventRepository extends JpaRepository<FundraisingEvent, Long> {
    boolean existsByNameIgnoreCase(String name);
}