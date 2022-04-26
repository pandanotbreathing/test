package com.example.demo.application.controller;

import com.example.demo.application.request.ProcessEventRequest;
import com.example.demo.domain.model.Employee;
import com.example.demo.domain.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping(value = "/{id}")
    public Employee get(@PathVariable Long id) {
//        Employee employee = new Employee();
//        employee.setName("first");
//        employee.setAge(34);
//        employeeService.newEmployee(employee);
//


        return employeeService.get(id);
    }

    @PostMapping(value = "/{id}/event")
    public void processEvent(@PathVariable Long id, @RequestBody ProcessEventRequest processEventRequest) {
        employeeService.processEvent(id, processEventRequest.getEvent());
    }
}
