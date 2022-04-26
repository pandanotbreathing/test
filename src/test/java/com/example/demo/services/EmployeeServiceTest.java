package com.example.demo.services;

import com.example.demo.domain.exception.EmployeeNotFoundException;
import com.example.demo.domain.model.Employee;
import com.example.demo.domain.model.EmployeeCheck;
import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import com.example.demo.domain.repository.EmployeeCheckRepository;
import com.example.demo.domain.repository.EmployeeRepository;
import com.example.demo.domain.services.EmployeeService;
import com.example.demo.domain.state.machine.EmployeeStateChangeListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository repository;
    @Mock
    private EmployeeCheckRepository checkRepository;
    @Mock
    private StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory;
    @Mock
    private EmployeeStateChangeListener employeeStateChangeListener;
    @Mock
    private StateMachine<EmployeeState, EmployeeEvent> stateMachine;
    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void newEmployee() {
        Employee employee = Employee.builder()
                .name("testName")
                .age(34)
                .build();

        Long newEmployeeId = 1L;

        when(repository.save(employee)).thenAnswer((Answer<Employee>) invocationOnMock -> {
            Employee employeeParameter = invocationOnMock.getArgument(0);
            employeeParameter.setId(newEmployeeId);
            return employeeParameter;
        });

        Employee result = employeeService.newEmployee(employee);
        Assert.assertEquals(EmployeeState.ADDED, result.getState());
        Assert.assertEquals(newEmployeeId, result.getId());
        verify(repository).save(employee);
    }

    @Test
    public void get_success() {
        Long employeeId = 1L;
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("testEmployee")
                .age(41)
                .build();
        when(repository.findById(employeeId)).thenReturn(Optional.of(employee));

        Employee result = employeeService.get(employeeId);
        Assert.assertEquals(employee, result);

        verify(repository).findById(employeeId);
    }

    @Test(expected = EmployeeNotFoundException.class)
    public void get_notFoundException() {
        Long employeeId = 1L;
        when(repository.findById(employeeId)).thenReturn(Optional.empty());

        employeeService.get(employeeId);
        fail("employeeService.get should've failed with EmployeeNotFoundException");
    }

    @Test
    public void getEmployeeChecksIfApplicable_withChecks() {
        Long employeeId = 1L;
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("testEmployee")
                .age(41)
                .state(EmployeeState.IN_CHECK)
                .build();

        EmployeeCheck check1 = EmployeeCheck.builder()
                .employeeId(employeeId)
                .checkName(EmployeeState.SECURITY_CHECK_FINISHED)
                .checkRegion(EmployeeState.SECURITY_CHECK_FINISHED.getRegion())
                .build();
        EmployeeCheck check2 = EmployeeCheck.builder()
                .employeeId(employeeId)
                .checkName(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION)
                .checkRegion(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION.getRegion())
                .build();
        List<EmployeeCheck> employeeChecks = List.of(check1, check2);

        when(checkRepository.findLatestCheckStates(employeeId)).thenReturn(employeeChecks);

        List<EmployeeCheck> result = employeeService.getEmployeeChecksIfApplicable(employee);
        Assert.assertEquals(employeeChecks, result);
        verify(checkRepository).findLatestCheckStates(employeeId);
    }

    @Test
    public void getEmployeeChecksIfApplicable_withoutChecks() {
        Long employeeId = 1L;
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("testEmployee")
                .age(41)
                .state(EmployeeState.ACTIVE)
                .build();

        List<EmployeeCheck> result = employeeService.getEmployeeChecksIfApplicable(employee);
        Assert.assertEquals(Collections.emptyList(), result);
        verify(checkRepository, never()).findLatestCheckStates(employeeId);
    }

    @Test
    public void processEvent() {
        Long employeeId = 1L;
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("testEmployee")
                .age(41)
                .state(EmployeeState.IN_CHECK)
                .build();

        when(repository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(stateMachineFactory.getStateMachine()).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(Mono.empty());
        StateMachineAccessor<EmployeeState, EmployeeEvent> accessor = mock(StateMachineAccessor.class);
        StateMachineAccess<EmployeeState, EmployeeEvent> machineAccess = mock(StateMachineAccess.class);
        doNothing().when(machineAccess).addStateMachineInterceptor(any());
        when(machineAccess.resetStateMachineReactively(any())).thenReturn(Mono.empty());
        doAnswer(invocationOnMock -> {
            Consumer<StateMachineAccess<EmployeeState, EmployeeEvent>> consumer = invocationOnMock.getArgument(0);
            consumer.accept(machineAccess);
            return null;
        }).when(accessor).doWithRegion(any());
        when(stateMachine.getStateMachineAccessor()).thenReturn(accessor);
        EmployeeCheck employeeCheck = EmployeeCheck.builder()
                .employeeId(employeeId)
                .checkName(EmployeeState.SECURITY_CHECK_FINISHED)
                .checkRegion(EmployeeState.SECURITY_CHECK_FINISHED.getRegion())
                .build();
        when(checkRepository.findLatestCheckStates(employeeId)).thenReturn(Collections.singletonList(employeeCheck));
        when(stateMachine.startReactively()).thenReturn(Mono.empty());
        when(stateMachine.sendEvent(any(Mono.class))).thenReturn(Flux.just("Test"));

        employeeService.processEvent(employeeId, EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);

        verify(checkRepository).findLatestCheckStates(employeeId);
    }
}
