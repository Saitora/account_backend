package ru.sberbank.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "accounts", schema = "public", catalog = "accounting")
public class AccountsEntity {
    private int id;
    private String accountNumber;
    private BigDecimal cash = new BigDecimal(0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, insertable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "account_number", updatable = false, nullable = false, insertable = false)
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Basic
    @Column(name = "cash")
    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountsEntity that = (AccountsEntity) o;
        return id == that.id &&
                Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(cash, that.cash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, cash);
    }
}
