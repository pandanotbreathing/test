package com.example.demo.domain.repository;

import com.example.demo.domain.model.EmployeeCheck;
import com.example.demo.domain.model.EmployeeCheckId;
import com.example.demo.domain.model.EmployeeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeCheckRepository extends JpaRepository<EmployeeCheck, EmployeeCheckId> {
    @Query(value = "SELECT DISTINCT ON (check_region) employee_id, check_name, check_region, created " +
                    "FROM employee_check " +
                    "WHERE employee_id = ?1 " +
                    "GROUP BY check_region, employee_id, check_name " +
                    "ORDER BY check_region, created DESC",
            nativeQuery = true)
    List<EmployeeCheck> findLatestCheckStates(Long employeeId);

    EmployeeCheck findByEmployeeIdAndCheckName(Long employeeId, EmployeeState checkName);
}
