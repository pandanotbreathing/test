package com.example.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@IdClass(EmployeeCheckId.class)
public class EmployeeCheck implements Serializable {
    @Id
    private Long employeeId;

    @Id
    @Enumerated(EnumType.STRING)
    private EmployeeState checkName;

    @Enumerated(EnumType.STRING)
    private EmployeeStateRegion checkRegion;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}
