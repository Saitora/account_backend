package ru.sberbank.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "operations", schema = "public", catalog = "accounting")
public class OperationsEntity {
    private int id;
    private Integer fromAccountId = null;
    private Integer toAccountId = null;
    private String operationType;
    private BigDecimal value;
    private Timestamp created;
    private AccountsEntity fromAccount;
    private AccountsEntity toAccount;

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
    @Column(name = "from_account_id", updatable = false)
    public Integer getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Integer accountId) {
        this.fromAccountId = accountId;
    }

    @Basic
    @Column(name = "to_account_id", updatable = false)
    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer accountId) {
        this.toAccountId = accountId;
    }

    @Basic
    @Column(name = "operation_type")
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    @Basic
    @Column(name = "value")
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Basic
    @Column(name = "created", updatable = false, nullable = false, insertable = false)
    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @ManyToOne
    @JoinColumn(name = "from_account_id", nullable = false, insertable = false, updatable = false)
    public AccountsEntity getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(AccountsEntity account) {
        this.fromAccount = account;
    }

    @ManyToOne
    @JoinColumn(name = "to_account_id", nullable = false, insertable = false, updatable = false)
    public AccountsEntity getToAccount() {
        return toAccount;
    }

    public void setToAccount(AccountsEntity account) {
        this.toAccount = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationsEntity that = (OperationsEntity) o;
        return id == that.id &&
                fromAccountId == that.fromAccountId &&
                toAccountId == that.toAccountId &&
                Objects.equals(operationType, that.operationType) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromAccountId, toAccountId, operationType, value);
    }
}
