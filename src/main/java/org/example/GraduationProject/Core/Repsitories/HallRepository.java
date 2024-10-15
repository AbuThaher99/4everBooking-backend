package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.Reservations;
import org.example.GraduationProject.Common.Enums.HallCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {


    @Query(value = "SELECT * FROM halls h " +
            "WHERE h.isDeleted = 0 " +
            "AND h.isProcessed = 1 " +
            "AND (:search IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:location IS NULL OR LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:category IS NULL OR " +
            "     (JSON_VALUE(h.categories, CONCAT('$.', :category)) IS NOT NULL AND " +
            "      JSON_VALUE(h.categories, CONCAT('$.', :category)) BETWEEN :minPrice AND :maxPrice)) " +
            "AND h.capacity BETWEEN :minCapacity AND :maxCapacity",
            nativeQuery = true)
    Page<Hall> findAll(Pageable pageable,
                       @Param("search") String search,
                       @Param("location") String location,
                       @Param("minPrice") Double minPrice,
                       @Param("maxPrice") Double maxPrice,
                       @Param("minCapacity") Integer minCapacity,
                       @Param("maxCapacity") Integer maxCapacity,
                       @Param("category") String category);

    @Query("SELECT h FROM Hall h WHERE h.hallOwner.id = :ownerId and h.isDeleted = false and h.isProcessed =true ")
    Page<Hall> findByOwnerId(Pageable pageable ,@Param("ownerId") Long ownerId);
    @Query("SELECT h FROM Hall h WHERE h.hallOwner.id = :ownerId and h.isDeleted = false and h.isProcessed =true ")
    List<Hall> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT r FROM Reservations r WHERE r.hall.id = :hallId")
    List<Reservations> findByHallId(@Param("hallId") Long hallId);
    @Query("SELECT r FROM Reservations r WHERE r.hall.hallOwner.id = :hallOwnerId")
    List<Reservations> findAllbyHallOwner(@Param("hallOwnerId") Long hallOwnerId);

    @Query("SELECT r FROM Reservations r " +
            "WHERE r.hall.id = :hallId " +
            "AND (YEAR(r.date) = :year AND MONTH(r.date) = :month " +
            "OR YEAR(r.endDate) = :year AND MONTH(r.endDate) = :month)")
    List<Reservations> findReservationsInMonth(@Param("hallId") Long hallId,
                                               @Param("year") int year,
                                               @Param("month") int month);
    @Query("select h from Hall h where h.isProcessed = false")
    Page<Hall> findAllHallsIsProcessed(Pageable pageable);
}
