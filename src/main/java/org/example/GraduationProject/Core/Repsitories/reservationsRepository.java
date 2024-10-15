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

import java.util.List;

@Repository
public interface reservationsRepository extends JpaRepository<Reservations, Long> {
    @Query("SELECT new org.example.GraduationProject.Common.DTOs.GetReservationDTO( r.date, r.chosenServices, r.endDate, r.category, r.totalPrice, r.hall.name) " +
            "FROM Reservations r WHERE r.customer.id = :CustomerId")
    Page<GetReservationDTO> findReservedCustomer(Pageable pageable, @Param("CustomerId") Long CustomerId);

    @Query("SELECT new org.example.GraduationProject.Common.DTOs.GetReservationDTO( r.date, r.chosenServices, r.endDate, r.category, r.totalPrice, r.hall.name) " +
            "FROM Reservations r WHERE r.hall.hallOwner.id = :ownerId")
    Page<GetReservationDTO> findReservedHallOwner(Pageable pageable,@Param("ownerId")Long ownerId);
}
