package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.DTOs.PaginationDTO;
import org.example.GraduationProject.Common.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmail(@Param("email") String email);
    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    Page<User> findAllDeletedUsers(Pageable pageable);
}
