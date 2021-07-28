package com.jamesdpeters.minecraft.database.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Database<T> {

    private final Class<T> clazz;

    public Database(Class<T> clazz) {
        this.clazz = clazz;
    }

    //** MEMBER METHODS **/
    public void saveEntity(Object object) {
        runTransaction(entityManager -> entityManager.persist(object));
    }

    public Optional<T> findEntity(Object id) {
        return Optional.ofNullable(HibernateUtil.getEntityManager().find(clazz, id));
    }

    public List<T> findAll() {
        var q = HibernateUtil.getCriteriaQuery(clazz);
        q.select(q.from(clazz));
        return HibernateUtil.getEntityManager().createQuery(q).getResultList();
    }

    public List<T> findAll(String sql) {
        var q = HibernateUtil.getEntityManager().createQuery(sql, clazz);
        return q.getResultList();
    }

    public TypedQuery<T> getQuery(String sql) {
        return HibernateUtil.getEntityManager().createQuery(sql, clazz);
    }

    public void runTransaction(Consumer<EntityManager> sessionConsumer) {
        var entityManager = HibernateUtil.getEntityManager();
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            sessionConsumer.accept(entityManager);
            transaction.commit();
        } catch (RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }

    public void refresh(T entity) {
        HibernateUtil.getEntityManager().refresh(entity);
    }

    public Class<T> clazz() {
        return clazz;
    }

}
