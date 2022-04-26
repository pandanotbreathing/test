package com.example.demo.application.request;

import com.example.demo.domain.model.EmployeeEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class ProcessEventRequest {
    @NonNull
    private EmployeeEvent event;
}
