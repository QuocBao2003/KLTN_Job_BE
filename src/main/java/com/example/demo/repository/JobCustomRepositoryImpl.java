package com.example.demo.repository;

import com.example.demo.domain.Job;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public class JobCustomRepositoryImpl implements JobCustomRepository{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Job> findAllWithPackagePriority(Specification<Job> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Job> root = query.from(Job.class);

        // Join
        Join<Object, Object> userPackageJoin = root.join("userPackage", JoinType.LEFT);
        Join<Object, Object> servicePackageJoin = userPackageJoin.join("servicePackage", JoinType.LEFT);

        // Apply Specification
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // Package priority ordering
        Expression<Integer> packagePriority = cb.<Integer>selectCase()
                .when(cb.equal(servicePackageJoin.get("packageType"), "FEATURED_JOB"), 1)
                .when(cb.equal(servicePackageJoin.get("packageType"), "PRIORITY_BOLD_TITLE"), 2)
                .when(cb.equal(servicePackageJoin.get("packageType"), "PRIORITY_DISPLAY"), 3)
                .otherwise(4);

        query.orderBy(
                cb.asc(packagePriority),
                cb.desc(root.get("updatedAt"))
        );

        // Execute with pagination
        List<Job> jobs = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Job> countRoot = countQuery.from(Job.class);
        countQuery.select(cb.count(countRoot));

        if (spec != null) {
            Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
            if (countPredicate != null) {
                countQuery.where(countPredicate);
            }
        }

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(jobs, pageable, total);
    }
}
