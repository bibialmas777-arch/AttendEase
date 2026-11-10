package com.attendease.repository;

import com.attendease.model.entity.Attendance;
import com.attendease.model.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentIdAndSubjectIdAndDate(Long studentId, Long subjectId, LocalDate date);

    boolean existsByStudentIdAndSubjectIdAndDate(Long studentId, Long subjectId, LocalDate date);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.student s JOIN FETCH s.user " +
           "JOIN FETCH a.subject " +
           "WHERE a.student.id = :studentId ORDER BY a.date DESC")
    List<Attendance> findByStudentIdOrderByDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.student s JOIN FETCH s.user " +
           "JOIN FETCH a.subject " +
           "WHERE a.student.id = :studentId AND a.subject.id = :subjectId ORDER BY a.date DESC")
    List<Attendance> findByStudentIdAndSubjectIdOrderByDateDesc(@Param("studentId") Long studentId,
                                                               @Param("subjectId") Long subjectId);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.student s JOIN FETCH s.user " +
           "JOIN FETCH a.subject sub " +
           "WHERE a.subject.id = :subjectId AND s.section.id = :sectionId AND a.date = :date")
    List<Attendance> findBySubjectIdAndSectionIdAndDate(@Param("subjectId") Long subjectId,
                                                       @Param("sectionId") Long sectionId,
                                                       @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.student s JOIN FETCH s.user " +
           "JOIN FETCH a.subject sub " +
           "WHERE (:classId IS NULL OR s.classEntity.id = :classId) " +
           "AND (:sectionId IS NULL OR s.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR a.subject.id = :subjectId) " +
           "AND (:startDate IS NULL OR a.date >= :startDate) " +
           "AND (:endDate IS NULL OR a.date <= :endDate) " +
           "ORDER BY a.date DESC, s.rollNo ASC")
    List<Attendance> findFiltered(@Param("classId") Long classId,
                                 @Param("sectionId") Long sectionId,
                                 @Param("subjectId") Long subjectId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId")
    long countTotalByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.subject.id = :subjectId")
    long countTotalByStudentIdAndSubjectId(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.subject.id = :subjectId AND a.status = :status")
    long countByStudentIdAndSubjectIdAndStatus(@Param("studentId") Long studentId,
                                              @Param("subjectId") Long subjectId,
                                              @Param("status") AttendanceStatus status);

    @Query("SELECT a.subject.id, a.subject.name, a.subject.code, " +
           "COUNT(a), " +
           "SUM(CASE WHEN a.status = com.attendease.model.enums.AttendanceStatus.PRESENT THEN 1 ELSE 0 END) " +
           "FROM Attendance a " +
           "WHERE a.student.id = :studentId " +
           "GROUP BY a.subject.id, a.subject.name, a.subject.code")
    List<Object[]> getSubjectWiseStatsForStudent(@Param("studentId") Long studentId);

    @Query("SELECT a.subject.name, " +
           "COUNT(a), " +
           "SUM(CASE WHEN a.status = com.attendease.model.enums.AttendanceStatus.PRESENT THEN 1 ELSE 0 END) " +
           "FROM Attendance a " +
           "WHERE (:classId IS NULL OR a.student.classEntity.id = :classId) " +
           "AND (:sectionId IS NULL OR a.student.section.id = :sectionId) " +
           "GROUP BY a.subject.id, a.subject.name")
    List<Object[]> getSubjectAttendanceAverages(@Param("classId") Long classId,
                                               @Param("sectionId") Long sectionId);

    @Query("SELECT FUNCTION('DATE_FORMAT', a.date, '%Y-%m') as m, " +
           "COUNT(a), " +
           "SUM(CASE WHEN a.status = com.attendease.model.enums.AttendanceStatus.PRESENT THEN 1 ELSE 0 END) " +
           "FROM Attendance a " +
           "WHERE (:classId IS NULL OR a.student.classEntity.id = :classId) " +
           "AND (:sectionId IS NULL OR a.student.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR a.subject.id = :subjectId) " +
           "GROUP BY FUNCTION('DATE_FORMAT', a.date, '%Y-%m') " +
           "ORDER BY m ASC")
    List<Object[]> getMonthlyTrend(@Param("classId") Long classId,
                                  @Param("sectionId") Long sectionId,
                                  @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(a), " +
           "SUM(CASE WHEN a.status = com.attendease.model.enums.AttendanceStatus.PRESENT THEN 1 ELSE 0 END) " +
           "FROM Attendance a " +
           "WHERE a.date = :date")
    List<Object[]> getDailyAttendanceStats(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a), " +
           "SUM(CASE WHEN a.status = com.attendease.model.enums.AttendanceStatus.PRESENT THEN 1 ELSE 0 END) " +
           "FROM Attendance a " +
           "WHERE a.markedBy.id = :facultyUserId AND a.date = :date")
    List<Object[]> getDailyAttendanceStatsForFaculty(@Param("facultyUserId") Long facultyUserId,
                                                    @Param("date") LocalDate date);
}

