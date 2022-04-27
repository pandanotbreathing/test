package com.example.demo.domain.services;

import com.example.demo.domain.exception.EmployeeNotFoundException;
import com.example.demo.domain.model.Employee;
import com.example.demo.domain.model.EmployeeCheck;
import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import com.example.demo.domain.repository.EmployeeCheckRepository;
import com.example.demo.domain.repository.EmployeeRepository;
import com.example.demo.domain.state.machine.EmployeeStateChangeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeService {
    public static final String EMPLOYEE_ID_HEADER = "employeeId";
    private final EmployeeRepository repository;
    private final EmployeeCheckRepository checkRepository;
    private final StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory;
    private final EmployeeStateChangeListener employeeStateChangeListener;

    public Employee newEmployee(Employee employee) {
        employee.setState(EmployeeState.ADDED);
        return repository.save(employee);
    }

    public void processEvent(Long employeeId, EmployeeEvent event) {
        log.info("processing {} event for employee {}", event, employeeId);
        Message<EmployeeEvent> eventMessage = MessageBuilder.withPayload(event)
                .setHeader(EMPLOYEE_ID_HEADER, employeeId)
                .build();
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = getStateMachine(employeeId);
        log.info("sending {} event to state machine {}", event, eventMessage);
        stateMachine.sendEvent(Mono.just(eventMessage)).blockLast();
    }

    public Employee get(Long employeeId) {
        return repository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    public List<EmployeeCheck> getEmployeeChecksIfApplicable(Employee employee) {
        if (EmployeeState.IN_CHECK.equals(employee.getState())) {
            return checkRepository.findLatestCheckStates(employee.getId());
        }

        return Collections.emptyList();
    }

    private StateMachine<EmployeeState, EmployeeEvent> getStateMachine(Long employeeId) {
        Employee employee = get(employeeId);
        log.info("employee {} is found {}. resetting state machine", employeeId, employee);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = stateMachineFactory.getStateMachine();

        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                .doWithRegion(machineAccess -> {
                    machineAccess.addStateMachineInterceptor(employeeStateChangeListener);
                    List<StateMachineContext<EmployeeState, EmployeeEvent>> contexts = new ArrayList<>();
                    if (EmployeeState.IN_CHECK.equals(employee.getState())) {
                        List<EmployeeCheck> latestCheckStates = checkRepository.findLatestCheckStates(employeeId);
                        log.info("employee with status IN_CHECK, setting check states to state machine {}", latestCheckStates);
                        latestCheckStates.forEach(item -> contexts.add(
                                new DefaultStateMachineContext<>(
                                        item.getCheckName(),
                                        null,
                                        null,
                                        null
                                )
                        ));
                    }
                    DefaultStateMachineContext<EmployeeState, EmployeeEvent> context= new DefaultStateMachineContext<EmployeeState, EmployeeEvent>(
                            contexts,
                            employee.getState(),
                            null,
                            null,
                            null
                    );
                    machineAccess.resetStateMachineReactively(context)
                            .block();
                });
        stateMachine.startReactively().block();
        log.info("state machine started successfully");
        return stateMachine;
    }
}
