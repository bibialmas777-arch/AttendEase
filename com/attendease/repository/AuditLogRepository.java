package com.attendease.repository;

import com.attendease.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.changedBy ORDER BY a.changedAt DESC")
    List<AuditLog> findAllWithUser();

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.changedBy WHERE a.entity = :entity ORDER BY a.changedAt DESC")
    List<AuditLog> findByEntityWithUser(@Param("entity") String entity);

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.changedBy WHERE a.entity = :entity AND a.entityId = :entityId ORDER BY a.changedAt DESC")
    List<AuditLog> findByEntityAndEntityIdWithUser(@Param("entity") String entity, @Param("entityId") Long entityId);
}

