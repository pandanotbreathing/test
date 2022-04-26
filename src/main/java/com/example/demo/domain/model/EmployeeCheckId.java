package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class EmployeeCheckId implements Serializable {
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    private EmployeeState checkName;
}
