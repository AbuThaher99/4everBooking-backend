package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.HallOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HallOwnerRepository extends JpaRepository<HallOwner, Long> {
    @Query("SELECT c FROM HallOwner c WHERE c.user.id = :userId")
    HallOwner getHallOwnerByUserId(@Param("userId") Long userId);

    @Query("SELECT h FROM Hall h WHERE h.hallOwner.id = :hallOwnerId AND h.isDeleted = true ")
    Page<Hall> getDeletedHallsByHallOwner( Pageable pageable,Long hallOwnerId);
}
