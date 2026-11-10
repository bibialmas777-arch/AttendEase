package com.attendease.repository;

import com.attendease.model.entity.FacultySubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultySubjectAssignmentRepository extends JpaRepository<FacultySubjectAssignment, Long> {

    @Query("SELECT a FROM FacultySubjectAssignment a " +
           "JOIN FETCH a.faculty f JOIN FETCH f.user " +
           "JOIN FETCH a.subject sub JOIN FETCH sub.classEntity " +
           "JOIN FETCH a.section sec " +
           "WHERE f.id = :facultyId")
    List<FacultySubjectAssignment> findByFacultyIdWithDetails(@Param("facultyId") Long facultyId);

    @Query("SELECT a FROM FacultySubjectAssignment a " +
           "JOIN FETCH a.faculty f JOIN FETCH f.user " +
           "JOIN FETCH a.subject sub JOIN FETCH sub.classEntity " +
           "JOIN FETCH a.section sec")
    List<FacultySubjectAssignment> findAllWithDetails();

    boolean existsByFacultyIdAndSubjectIdAndSectionId(Long facultyId, Long subjectId, Long sectionId);

    Optional<FacultySubjectAssignment> findByFacultyIdAndSubjectIdAndSectionId(Long facultyId, Long subjectId, Long sectionId);

    @Query("SELECT COUNT(a) > 0 FROM FacultySubjectAssignment a " +
           "WHERE a.faculty.user.id = :userId AND a.subject.id = :subjectId AND a.section.id = :sectionId")
    boolean isFacultyAssignedToSubjectAndSection(@Param("userId") Long userId,
                                                @Param("subjectId") Long subjectId,
                                                @Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(a) > 0 FROM FacultySubjectAssignment a " +
           "WHERE a.faculty.user.id = :userId AND a.subject.id = :subjectId")
    boolean isFacultyAssignedToSubject(@Param("userId") Long userId,
                                       @Param("subjectId") Long subjectId);
}

