package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.DTOs.GetReservationDTO;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.Reservations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface reservationsRepository extends JpaRepository<Reservations, Long> {
    @Query("SELECT new org.example.GraduationProject.Common.DTOs.GetReservationDTO(r.id,r.hall.id, r.date, r.chosenServices, r.endDate, r.category, r.totalPrice, r.hall.name) " +
            "FROM Reservations r WHERE r.customer.id = :CustomerId")
    Page<GetReservationDTO> findReservedCustomer(Pageable pageable, @Param("CustomerId") Long CustomerId);

    @Query("SELECT new org.example.GraduationProject.Common.DTOs.GetReservationDTO( r.id,r.hall.id,r.date, r.chosenServices, r.endDate, r.category, r.totalPrice, r.hall.name) " +
            "FROM Reservations r WHERE r.hall.hallOwner.id = :ownerId")
    Page<GetReservationDTO> findReservedHallOwner(Pageable pageable,@Param("ownerId")Long ownerId);

    @Query("SELECT r FROM Reservations r " +
            "WHERE r.isDeleted = false " +
            "AND r.date >= :startOfDay " +
            "AND r.date < :endOfDay " +
            "AND r.isNotificationSent = false")
    List<Reservations> findReservationsForTomorrow(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
    @Query("SELECT MAX(r.lastModified) FROM Reservations r " +
            "WHERE r.hall.id IN :hallIds")
    LocalDateTime findLatestReservationUpdateForOwner(@Param("hallIds") List<Long> hallIds);


}
