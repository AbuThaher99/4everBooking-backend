package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Entities.UserHallRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface UserHallRecommendationRepository extends JpaRepository<UserHallRecommendation, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM UserHallRecommendation r WHERE r.user = :user")
    void deleteByUser(@Param("user") User user);

}
