package com.example.demo.domain.services;

import com.example.demo.domain.exception.EmployeeNotFoundException;
import com.example.demo.domain.model.Employee;
import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import com.example.demo.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class EmployeeService {
    private final EmployeeRepository repository;
    private final StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory;

    public void newEmployee(Employee employee) {
        employee.setState(EmployeeState.ADDED);
        repository.save(employee);
    }

    public void processEvent(Long employeeId, EmployeeEvent event) {
        getStateMachine(employeeId).sendEvent(Mono.just(MessageBuilder.withPayload(event).build()));
    }

    public Employee get(Long employeeId) {
        return repository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private StateMachine<EmployeeState, EmployeeEvent> getStateMachine(Long employeeId) {
        Employee employee = get(employeeId);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = stateMachineFactory.getStateMachine(
                Long.toString(employee.getId())
        );

        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(machineAccess -> machineAccess.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(
                                employee.getState(),
                                null,
                                null,
                                null
                        )
                ).block());
        stateMachine.startReactively().block();
        return stateMachine;
    }
}
