package cz.csas.startup.FBPoc.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class Account implements Serializable {

    private Long id;
    private Long prefix;
    private Long number;
    private String type;
    private BigDecimal balance;
    private String currency;

    public Long getPrefix() {
        return prefix;
    }

    public void setPrefix(Long prefix) {
        this.prefix = prefix;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder a = new StringBuilder();
        a.append(getType()).append(" ");
        if (getPrefix() != null) a.append(getPrefix()).append("-");
        a.append(getNumber());
        return a.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!id.equals(account.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
