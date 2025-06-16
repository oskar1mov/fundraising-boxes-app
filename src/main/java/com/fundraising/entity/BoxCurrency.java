package com.fundraising.entity;

import com.fundraising.enums.Currency;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "box_currencies")
public class BoxCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    public BoxCurrency() {}

    public BoxCurrency(Box box, Currency currency, BigDecimal amount) {
        this.box = box;
        this.currency = currency;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Box getBox() { return box; }
    public void setBox(Box box) { this.box = box; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}