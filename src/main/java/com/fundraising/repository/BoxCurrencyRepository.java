package com.fundraising.repository;

import com.fundraising.entity.Box;
import com.fundraising.entity.BoxCurrency;
import com.fundraising.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoxCurrencyRepository extends JpaRepository<BoxCurrency, Long> {
    List<BoxCurrency> findByBox(Box box);
    Optional<BoxCurrency> findByBoxAndCurrency(Box box, Currency currency);

    @Query("SELECT COALESCE(SUM(bc.amount), 0) FROM BoxCurrency bc WHERE bc.box = :box")
    BigDecimal getTotalAmountInBox(@Param("box") Box box);

    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN false ELSE true END FROM BoxCurrency bc WHERE bc.box = :box AND bc.amount > 0")
    boolean isBoxEmpty(@Param("box") Box box);
}