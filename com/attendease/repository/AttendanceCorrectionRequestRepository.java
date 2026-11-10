package com.attendease.repository;

import com.attendease.model.entity.AttendanceCorrectionRequest;
import com.attendease.model.enums.CorrectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceCorrectionRequestRepository extends JpaRepository<AttendanceCorrectionRequest, Long> {

    @Query("SELECT r FROM AttendanceCorrectionRequest r " +
           "JOIN FETCH r.student s JOIN FETCH s.user " +
           "JOIN FETCH r.subject " +
           "LEFT JOIN FETCH r.attendance " +
           "LEFT JOIN FETCH r.reviewedBy " +
           "WHERE s.id = :studentId ORDER BY r.createdAt DESC")
    List<AttendanceCorrectionRequest> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("SELECT r FROM AttendanceCorrectionRequest r " +
           "JOIN FETCH r.student s JOIN FETCH s.user " +
           "JOIN FETCH r.subject sub " +
           "LEFT JOIN FETCH r.attendance " +
           "LEFT JOIN FETCH r.reviewedBy " +
           "WHERE sub.id IN :subjectIds " +
           "ORDER BY r.createdAt DESC")
    List<AttendanceCorrectionRequest> findBySubjectIdsWithDetails(@Param("subjectIds") List<Long> subjectIds);

    @Query("SELECT r FROM AttendanceCorrectionRequest r " +
           "JOIN FETCH r.student s JOIN FETCH s.user " +
           "JOIN FETCH r.subject sub " +
           "LEFT JOIN FETCH r.attendance " +
           "LEFT JOIN FETCH r.reviewedBy " +
           "ORDER BY r.createdAt DESC")
    List<AttendanceCorrectionRequest> findAllWithDetails();

    @Query("SELECT r FROM AttendanceCorrectionRequest r " +
           "JOIN FETCH r.student s JOIN FETCH s.user " +
           "JOIN FETCH r.subject sub " +
           "LEFT JOIN FETCH r.attendance " +
           "LEFT JOIN FETCH r.reviewedBy " +
           "WHERE r.id = :id")
    Optional<AttendanceCorrectionRequest> findByIdWithDetails(@Param("id") Long id);

    long countByStudentIdAndStatus(Long studentId, CorrectionStatus status);

    @Query("SELECT COUNT(r) FROM AttendanceCorrectionRequest r " +
           "WHERE r.subject.id IN :subjectIds AND r.status IN ('PENDING', 'UNDER_REVIEW')")
    long countPendingBySubjectIds(@Param("subjectIds") List<Long> subjectIds);

    long countByStatus(CorrectionStatus status);
}

