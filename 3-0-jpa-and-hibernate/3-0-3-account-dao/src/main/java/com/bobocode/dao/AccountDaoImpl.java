package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;


    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        performDbActionInsideTransactionWithoutReturnedVal(em -> em.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return performDbActionInsideTransaction(em -> em.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
        return (Account) performDbActionInsideTransaction(em -> {
            Query query = em.createQuery("select a from Account a where a.email=:email", Account.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        });
    }

    @Override
    public List<Account> findAll() {
        return performDbActionInsideTransaction(em -> {
            var selectAllFromAccount = em.createQuery("select a from Account a", Account.class);
            return selectAllFromAccount.getResultList();
        });
    }

    @Override
    public void update(Account account) {
        performDbActionInsideTransactionWithoutReturnedVal(em -> em.merge(account));
    }

    @Override
    public void remove(Account account) {
        performDbActionInsideTransactionWithoutReturnedVal(em -> {
            var foundAcc = em.find(Account.class, account.getId());
            em.remove(foundAcc);
        });
    }

    private void performDbActionInsideTransactionWithoutReturnedVal(Consumer<EntityManager> entityManagerConsumer) {
        performDbActionInsideTransaction(em -> {
            entityManagerConsumer.accept(em);
            return null;
        });
    }

    private <T> T performDbActionInsideTransaction(Function<EntityManager, T> persistenceFunction) {
        T result;
        var entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            result = persistenceFunction.apply(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Transaction was rolled back: ", e);
        } finally {
            entityManager.close();
        }

        return result;
    }
}

