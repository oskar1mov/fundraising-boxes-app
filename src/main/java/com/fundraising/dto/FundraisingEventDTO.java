package com.fundraising.dto;

import com.fundraising.enums.Currency;
import java.math.BigDecimal;

public class FundraisingEventDTO {
    private Long id;
    private String name;
    private BigDecimal balance;
    private Currency currency;

    public FundraisingEventDTO() {}

    public FundraisingEventDTO(Long id, String name, BigDecimal balance, Currency currency) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.currency = currency;
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
