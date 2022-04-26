package com.example.demo.domain.services;

import com.example.demo.domain.exception.EmployeeNotFoundException;
import com.example.demo.domain.model.Employee;
import com.example.demo.domain.model.EmployeeCheck;
import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import com.example.demo.domain.repository.EmployeeCheckRepository;
import com.example.demo.domain.repository.EmployeeRepository;
import com.example.demo.domain.state.machine.EmployeeStateChangeListener;
import com.example.demo.domain.state.machine.InMemoryStateMachinePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmployeeService {
    public static final String EMPLOYEE_ID_HEADER = "employeeId";
    private final EmployeeRepository repository;
    private final EmployeeCheckRepository checkRepository;
    private final StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory;
    private final EmployeeStateChangeListener employeeStateChangeListener;
//    private final InMemoryStateMachinePersist inMemoryStateMachinePersist;
    private final StateMachinePersister<EmployeeState, EmployeeEvent, String> stateMachinePersister = new DefaultStateMachinePersister<EmployeeState, EmployeeEvent, String>(new InMemoryStateMachinePersist());

    public void newEmployee(Employee employee) {
        employee.setState(EmployeeState.ADDED);
        repository.save(employee);
    }

    public void processEvent(Long employeeId, EmployeeEvent event) {
        Message<EmployeeEvent> eventMessage = MessageBuilder.withPayload(event)
                .setHeader(EMPLOYEE_ID_HEADER, employeeId)
                .build();
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = getStateMachine(employeeId);
        stateMachine.sendEvent(Mono.just(eventMessage)).blockLast();
//        try {
//            stateMachinePersister.persist(stateMachine, Long.toString(employeeId));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        System.out.println(stateMachine.getState());
    }

    public Employee get(Long employeeId) {
        return repository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private StateMachine<EmployeeState, EmployeeEvent> getStateMachine(Long employeeId) {
        Employee employee = get(employeeId);
//
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = stateMachineFactory.getStateMachine();
//        try {
//            return stateMachinePersister.restore(stateMachine, Long.toString(employeeId));
////            return stateMachinePersister;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor().doWithRegion(
                machineAccess -> {
                    machineAccess.addStateMachineInterceptor(employeeStateChangeListener);
                    List<StateMachineContext<EmployeeState, EmployeeEvent>> contexts = new ArrayList<>();
                    if (EmployeeState.IN_CHECK.equals(employee.getState())) {
                        List<EmployeeCheck> latestCheckStates = checkRepository.findLatestCheckStates(employeeId);
                        latestCheckStates.forEach(item -> {
                            contexts.add(new DefaultStateMachineContext<>(item.getCheckName(), null, null, null));
                        });
                    }
//                    StateMachineContext
                    machineAccess.resetStateMachineReactively(new DefaultStateMachineContext<EmployeeState, EmployeeEvent>(contexts, employee.getState(), null, null, null)).block();
                }
        );
//        stateMachine.getStateMachineAccessor()
//                .doWithAllRegions(machineAccess -> {
//                    machineAccess.addStateMachineInterceptor(employeeStateChangeListener);
//                    machineAccess.resetStateMachineReactively(
//                            new DefaultStateMachineContext<>(
//                                    employee.getState(),
//                                    null,
//                                    null,
//                                    null
//                            )
//                    ).block();
//                    latestCheckStates.forEach(latestState -> machineAccess.resetStateMachineReactively(
//                            new DefaultStateMachineContext<>(
//                                    latestState.getCheckName(),
//                                    null,
//                                    null,
//                                    null
//                            )
//                    ).block());
//                });
        stateMachine.startReactively().block();
        return stateMachine;
    }
}
