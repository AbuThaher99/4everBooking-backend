package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.Reservations;
import org.example.GraduationProject.Common.Enums.HallCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {


    @Query(value = "SELECT * FROM halls h " +
            "WHERE h.isDeleted = false " +
            "AND h.isProcessed = true " +
            "AND (:search IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:location IS NULL OR LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:category IS NULL OR " +
            "     (JSON_UNQUOTE(JSON_EXTRACT(h.categories, CONCAT('$.', :category))) IS NOT NULL AND " +
            "      CAST(JSON_UNQUOTE(JSON_EXTRACT(h.categories, CONCAT('$.', :category))) AS DECIMAL) BETWEEN :minPrice AND :maxPrice)) " +
            "AND h.capacity BETWEEN :minCapacity AND :maxCapacity " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM reservations r " +
            "    WHERE r.hallId = h.id " +
            "    AND (:startDate IS NOT NULL AND :endDate IS NOT NULL AND " +
            "         (r.date BETWEEN :startDate AND :endDate OR " +
            "          r.endDate BETWEEN :startDate AND :endDate OR " +
            "          :startDate BETWEEN r.date AND r.endDate OR " +
            "          :endDate BETWEEN r.date AND r.endDate)) " +
            ") " +
            "ORDER BY h.averageRating DESC",
            nativeQuery = true)
    Page<Hall> findAll(Pageable pageable,
                       @Param("search") String search,
                       @Param("location") String location,
                       @Param("minPrice") Double minPrice,
                       @Param("maxPrice") Double maxPrice,
                       @Param("minCapacity") Integer minCapacity,
                       @Param("maxCapacity") Integer maxCapacity,
                       @Param("category") String category,
                       @Param("startDate") LocalDateTime startDate,  // Ensure LocalDateTime here
                       @Param("endDate") LocalDateTime endDate);     // Ensure LocalDateTime here


    @Query("SELECT r.hall FROM UserHallRecommendation r " +
            "WHERE r.hall.isDeleted = false " +
            "AND r.hall.isProcessed = true " +
            "AND r.user.id = :userId " +
            "ORDER BY r.score DESC")
    Page<Hall> findRecommendedHalls(Pageable pageable, @Param("userId") Long userId);

    @Query("SELECT h FROM Hall h WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(h.latitude)) * " +
            "cos(radians(h.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(h.latitude)))) < :radius " +
            "AND h.isDeleted = false AND h.isProcessed = true")
    Page<Hall> findHallsNearLocation(Pageable pageable, double latitude, double longitude, double radius);

    @Query(value = "SELECT * FROM halls h " +
            "WHERE JSON_EXTRACT(h.categories, CONCAT('$.\"', :category, '\"')) IS NOT NULL " +
            "AND h.isDeleted = false AND h.isProcessed = true " +
            "ORDER BY CAST(JSON_EXTRACT(h.categories, CONCAT('$.\"', :category, '\"')) AS DECIMAL) ASC",
            nativeQuery = true)
    Page<Hall> findAllSortedByCategoryPrice(Pageable pageable, @Param("category") String category);





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

    @Query("select h from Hall h where h.capacity = :capacity")
    List<Hall> findByCapacity(@Param("capacity") Integer capacity);

    @Query("SELECT h.id FROM Hall h WHERE h.hallOwner.id = :ownerId")
    List<Long> findHallIdsByOwnerId(@Param("ownerId") Long ownerId);
}
