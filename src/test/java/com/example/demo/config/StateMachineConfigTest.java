package com.example.demo.config;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import com.example.demo.domain.state.machine.InMemoryStateMachinePersist;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import reactor.core.publisher.Mono;

import java.util.List;
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
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());
        stateMachine.sendEvent(EmployeeEvent.ACTIVATE);
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());
        System.out.println(stateMachine.getState().toString());
//        stateMachine.sendEvent(EmployeeEvent.FINISH_SECURITY_CHECK);
//        Assertions.assertNotEquals(EmployeeState.APPROVED, stateMachine.getState().getId());

        StateMachinePersister<EmployeeState, EmployeeEvent, String> stateMachinePersister = new DefaultStateMachinePersister<EmployeeState, EmployeeEvent, String>(new InMemoryStateMachinePersist());
        try {
            stateMachinePersister.persist(stateMachine, "tests");
            StateMachine<EmployeeState, EmployeeEvent> stateMachine1 = factory.getStateMachine(UUID.randomUUID());
            stateMachinePersister.restore(stateMachine1, "tests");
            System.out.println("RESTORED" + stateMachine1.getState());
            stateMachine1.sendEvent(Mono.just(MessageBuilder.withPayload(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK).build())).blockLast();
            System.out.println("RESTORED FIRED " + stateMachine1.getState());
            Assertions.assertTrue(stateMachine1.getState().getIds().contains(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION));
            stateMachine1.sendEvent(Mono.just(MessageBuilder.withPayload(EmployeeEvent.FINISH_SECURITY_CHECK).build())).blockLast();
            Assertions.assertTrue(stateMachine1.getState().getIds().containsAll(List.of(EmployeeState.SECURITY_CHECK_FINISHED, EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION)));
            Assertions.assertFalse(stateMachine1.getState().getIds().contains(EmployeeState.APPROVED));
            stateMachine1.sendEvent(Mono.just(MessageBuilder.withPayload(EmployeeEvent.FINISH_WORK_PERMIT_CHECK).build())).blockLast();
            Assertions.assertEquals(EmployeeState.APPROVED, stateMachine1.getState().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        stateMachine.sendEvent(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        Message<EmployeeEvent> eventMessage = MessageBuilder.withPayload(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK)
//                .setHeader(EMPLOYEE_ID_HEADER, employeeId)
                .build();
        stateMachine.sendEvent(Mono.just(eventMessage)).blockLast();


        stateMachine.sendEvent(Mono.just(eventMessage)).blockLast();
        Assertions.assertTrue(stateMachine.getState().getIds().contains(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION));
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
        Assertions.assertTrue(stateMachine.getState().getIds().contains(EmployeeState.WORK_PERMIT_CHECK_FINISHED));
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));
//        stateMachine.sendEvent(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.FINISH_SECURITY_CHECK);
        Assertions.assertEquals(EmployeeState.APPROVED, stateMachine.getState().getId());

        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(EmployeeEvent.ACTIVATE);
        Assertions.assertEquals(EmployeeState.ACTIVE, stateMachine.getState().getId());
        System.out.println(stateMachine.getState().toString());
    }
}
