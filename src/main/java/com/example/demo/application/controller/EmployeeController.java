package com.example.demo.application.controller;

import com.example.demo.application.request.ProcessEventRequest;
import com.example.demo.application.response.EmployeeResponse;
import com.example.demo.domain.model.Employee;
import com.example.demo.domain.model.EmployeeCheck;
import com.example.demo.domain.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        Employee employee = employeeService.get(id);
        List<EmployeeCheck> employeeChecks = employeeService.getEmployeeChecksIfApplicable(employee);
        return new EmployeeResponse(employee, employeeChecks);
    }

    @PostMapping
    public Employee create(@Valid @RequestBody Employee employee) {
        return employeeService.newEmployee(employee);
    }

    @PostMapping(value = "/{id}/event")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processEvent(@PathVariable Long id, @RequestBody ProcessEventRequest processEventRequest) {
        employeeService.processEvent(id, processEventRequest.getEvent());
    }
}
