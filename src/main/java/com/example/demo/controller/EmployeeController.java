package com.example.demo.controller;

import com.example.demo.domain.model.Employee;
import com.example.demo.domain.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping(value = "/{id}", produces = "application/json")
    public Employee get(@PathVariable Long id) {
        return employeeService.get(id);
    }
}
