package com.example.demo.config;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.UUID;
import java.util.function.Consumer;

@SpringBootTest
public class StateMachineConfigTest {
    @Autowired
    private StateMachineFactory<EmployeeState, EmployeeEvent> factory;

    @Test
    public void testAddedStateMachine() {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.start();

//        StateMachine<EmployeeState, EmployeeEvent> stateMachine1 = factory.getStateMachine(UUID.randomUUID());
//        stateMachine1.getStateMachineAccessor().doWithAllRegions(employeeStateEmployeeEventStateMachineAccess -> {
////            employeeStateEmployeeEventStateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(EmployeeState.IN_CHECK, null, null, null));
//            employeeStateEmployeeEventStateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION, null, null, null));
//            employeeStateEmployeeEventStateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(EmployeeState.SECURITY_CHECK_FINISHED, null, null, null));
//        });
//
//        System.out.println(stateMachine1.getState().toString());

        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.BEGIN_CHECK);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
//        stateMachine.sendEvent(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.FINISH_SECURITY_CHECK);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.ACTIVATE);
        System.out.println(stateMachine.getState().toString());
    }
}
