package com.example.demo.domain.state.machine;

import com.example.demo.domain.exception.EmployeeNotFoundException;
import com.example.demo.domain.model.*;
import com.example.demo.domain.repository.EmployeeCheckRepository;
import com.example.demo.domain.repository.EmployeeRepository;
import com.example.demo.domain.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Component
public class EmployeeStateChangeListener extends StateMachineInterceptorAdapter<EmployeeState, EmployeeEvent> {
    private final EmployeeRepository employeeRepository;
    private final EmployeeCheckRepository employeeCheckRepository;

    @Override
    public void preStateChange(
            State<EmployeeState, EmployeeEvent> state,
            Message<EmployeeEvent> message,
            Transition<EmployeeState, EmployeeEvent> transition,
            StateMachine<EmployeeState, EmployeeEvent> stateMachine,
            StateMachine<EmployeeState, EmployeeEvent> rootStateMachine
    ) {
        Long employeeId = (Long) message.getHeaders().get(EmployeeService.EMPLOYEE_ID_HEADER);
        if (employeeId == null || employeeId <= 0) {
            log.warn("triggered preStateChange action w/o EMPLOYEE_ID_HEADER {}", message);
            return;
        }

        Collection<EmployeeState> ids = transition.getTarget().getIds();
        ids.parallelStream()
                .forEach(id -> processEmployeeState(employeeId, id));
    }

    private void processEmployeeState(Long employeeId, EmployeeState state) {
        log.info("processing employee {} state {}", employeeId, state);
        if (EmployeeStateRegion.MAIN.equals(state.getRegion())) {
            saveEmployeeState(employeeId, state);
            return;
        }

        saveEmployeeCheckState(employeeId, state);
    }

    private void saveEmployeeCheckState(Long employeeId, EmployeeState state) {
        EmployeeCheck check = employeeCheckRepository.findByEmployeeIdAndCheckName(employeeId, state);
        if (check == null) {
            employeeCheckRepository.save(
                    EmployeeCheck.builder()
                            .employeeId(employeeId)
                            .checkName(state)
                            .checkRegion(state.getRegion())
                            .build()
            );
            log.info("employee {} check state {} has been set", employeeId, state);
        }
    }

    private void saveEmployeeState(Long employeeId, EmployeeState state) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
        employee.setState(state);
        employeeRepository.save(employee);
        log.info("employee {} state {} has been set", employeeId, state);
    }
}
