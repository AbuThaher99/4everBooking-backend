package org.example.GraduationProject.Core.Repsitories;

import org.example.GraduationProject.Common.Entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c.id FROM Customer c WHERE c.user.id = :userId")
    Long getCustomerByUserId(@Param("userId") Long userId);

    Customer findByUserId(Long userId);
}
