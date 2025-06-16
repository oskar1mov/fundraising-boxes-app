package com.fundraising.entity;

import com.fundraising.enums.Currency;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fundraising_events",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class FundraisingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    public FundraisingEvent() {}

    public FundraisingEvent(String name, Currency currency) {
        this.name = name;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
}