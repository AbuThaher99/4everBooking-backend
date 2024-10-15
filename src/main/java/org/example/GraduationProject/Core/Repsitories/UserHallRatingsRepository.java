package org.example.GraduationProject.Core.Repsitories;



import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Entities.UserHallRatings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHallRatingsRepository extends JpaRepository<UserHallRatings, Long> {
    List<UserHallRatings> findByUser(User user);
    List<UserHallRatings> findByHall(Hall hall);
    List<UserHallRatings> findByUserAndHall(User user, Hall hall);
    @Query("SELECT r FROM UserHallRatings r WHERE r.user.id = :userId AND r.hall.id = :hallId")
    UserHallRatings findByUserIdAndHallId(@Param("userId") Long userId,@Param("hallId") Long hallId);
    @Query("SELECT AVG(uhr.rating) FROM UserHallRatings uhr WHERE uhr.hall.id = :hallId")
    Double findAverageRatingByHallId(@Param("hallId") Long hallId);
}
