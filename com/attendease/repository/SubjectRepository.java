package com.attendease.repository;

import com.attendease.model.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
    boolean existsByCode(String code);
    List<Subject> findByClassEntityId(Long classId);

    @Query("SELECT s FROM Subject s JOIN FETCH s.classEntity")
    List<Subject> findAllWithClass();

    @Query("SELECT s FROM Subject s JOIN FETCH s.classEntity WHERE s.classEntity.id = :classId")
    List<Subject> findByClassIdWithClass(@Param("classId") Long classId);
}

