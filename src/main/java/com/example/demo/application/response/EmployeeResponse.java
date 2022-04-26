package com.example.demo.application.response;

import com.example.demo.domain.model.Employee;
import com.example.demo.domain.model.EmployeeCheck;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class EmployeeResponse {
    private final Employee employee;
    private final List<EmployeeCheck> employeeChecks;
}
