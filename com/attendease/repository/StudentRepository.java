package com.attendease.repository;

import com.attendease.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNo(String rollNo);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByClassEntityIdAndSectionId(Long classId, Long sectionId);
    List<Student> findBySectionId(Long sectionId);
    List<Student> findByClassEntityId(Long classId);
    
    @Query("SELECT s FROM Student s JOIN FETCH s.user u JOIN FETCH s.classEntity c JOIN FETCH s.section sec WHERE u.active = true")
    List<Student> findAllActiveWithDetails();

    @Query("SELECT s FROM Student s JOIN FETCH s.user u JOIN FETCH s.classEntity c JOIN FETCH s.section sec WHERE s.id = :id")
    Optional<Student> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Student s JOIN FETCH s.user u JOIN FETCH s.classEntity c JOIN FETCH s.section sec WHERE u.id = :userId")
    Optional<Student> findByUserIdWithDetails(@Param("userId") Long userId);
}

