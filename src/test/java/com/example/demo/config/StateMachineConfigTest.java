package com.example.demo.config;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.example.demo.domain.services.EmployeeService.EMPLOYEE_ID_HEADER;

@SpringBootTest
public class StateMachineConfigTest {
    @Autowired
    private StateMachineFactory<EmployeeState, EmployeeEvent> factory;

    //Happy path #Scenario 1
    @Test
    public void securityCheckWorkPermitToActive() {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.startReactively().block();

        sendEvent(stateMachine, EmployeeEvent.BEGIN_CHECK);
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.FINISH_SECURITY_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_FINISHED,
                EmployeeState.WORK_PERMIT_CHECK_STARTED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_FINISHED,
                EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
        Assertions.assertEquals(EmployeeState.APPROVED, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.ACTIVATE);
        Assertions.assertEquals(EmployeeState.ACTIVE, stateMachine.getState().getId());
    }

    //Happy path #Scenario 2
    @Test
    public void workPermitSecurityCheckToActive() {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.startReactively().block();

        sendEvent(stateMachine, EmployeeEvent.BEGIN_CHECK);
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_STARTED,
                EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_STARTED,
                EmployeeState.WORK_PERMIT_CHECK_FINISHED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.FINISH_SECURITY_CHECK);
        Assertions.assertEquals(EmployeeState.APPROVED, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.ACTIVATE);
        Assertions.assertEquals(EmployeeState.ACTIVE, stateMachine.getState().getId());
    }

    //Happy path #Scenario 3
    @Test
    public void workPermitSecurityWorkPermitToActive() {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.startReactively().block();

        sendEvent(stateMachine, EmployeeEvent.BEGIN_CHECK);
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION,
                EmployeeState.SECURITY_CHECK_STARTED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.FINISH_SECURITY_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION,
                EmployeeState.SECURITY_CHECK_FINISHED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
        Assertions.assertEquals(EmployeeState.APPROVED, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.ACTIVATE);
        Assertions.assertEquals(EmployeeState.ACTIVE, stateMachine.getState().getId());
    }

    //Unhappy path scenario #Scenario 1
    @Test
    public void securityCheckFinishedNotActive() {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.startReactively().block();

        sendEvent(stateMachine, EmployeeEvent.BEGIN_CHECK);
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.FINISH_SECURITY_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_FINISHED,
                EmployeeState.WORK_PERMIT_CHECK_STARTED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.ACTIVATE);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_FINISHED,
                EmployeeState.WORK_PERMIT_CHECK_STARTED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));
    }

    //Unhappy path scenario #Scenario 2
    @Test
    public void workPermitStartedToFinishedNotAllowed() {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.startReactively().block();

        sendEvent(stateMachine, EmployeeEvent.BEGIN_CHECK);
        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());

        sendEvent(stateMachine, EmployeeEvent.FINISH_SECURITY_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_FINISHED,
                EmployeeState.WORK_PERMIT_CHECK_STARTED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));

        sendEvent(stateMachine, EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
        assertStates(
                stateMachine,
                EmployeeState.IN_CHECK,
                EmployeeState.SECURITY_CHECK_FINISHED,
                EmployeeState.WORK_PERMIT_CHECK_STARTED
        );
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));
        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.WORK_PERMIT_CHECK_FINISHED));
    }


//    public void test() {
//        sendEvent(stateMachine, EmployeeEvent.BEGIN_CHECK);
////        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(EmployeeEvent.BEGIN_CHECK).build()));
//        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());
//        sendEvent(stateMachine, EmployeeEvent.ACTIVATE);
////        stateMachine.sendEvent(EmployeeEvent.ACTIVATE);
//        Assertions.assertEquals(EmployeeState.IN_CHECK, stateMachine.getState().getId());
////        System.out.println(stateMachine.getState().toString());
////        Message<EmployeeEvent> eventMessage = MessageBuilder.withPayload(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK)
////                .setHeader(EMPLOYEE_ID_HEADER, "1")
////                .build();
////        stateMachine.sendEvent(Mono.just(eventMessage)).blockLast();
//        sendEvent(stateMachine, EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK);
////        stateMachine.sendEvent(Mono.just(eventMessage)).blockLast();
//        Assertions.assertTrue(stateMachine.getState().getIds().contains(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION));
////        System.out.println(stateMachine.getState().toString());
////        stateMachine.sendEvent(EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
//        sendEvent(stateMachine, EmployeeEvent.FINISH_WORK_PERMIT_CHECK);
//        Assertions.assertTrue(stateMachine.getState().getIds().contains(EmployeeState.WORK_PERMIT_CHECK_FINISHED));
//        Assertions.assertFalse(stateMachine.getState().getIds().contains(EmployeeState.APPROVED));
////        System.out.println(stateMachine.getState().toString());
//        sendEvent(stateMachine, EmployeeEvent.FINISH_SECURITY_CHECK);
////        stateMachine.sendEvent(EmployeeEvent.FINISH_SECURITY_CHECK);
//        Assertions.assertEquals(EmployeeState.APPROVED, stateMachine.getState().getId());
//
////        System.out.println(stateMachine.getState().toString());
////        stateMachine.sendEvent(EmployeeEvent.ACTIVATE);
//        sendEvent(stateMachine, EmployeeEvent.ACTIVATE);
//        Assertions.assertEquals(EmployeeState.ACTIVE, stateMachine.getState().getId());
////        System.out.println(stateMachine.getState().toString());
//    }
    private void sendEvent(StateMachine<EmployeeState, EmployeeEvent> stateMachine, EmployeeEvent event) {
        Message<EmployeeEvent> message = MessageBuilder.withPayload(event)
                .setHeader(EMPLOYEE_ID_HEADER, "1")
                .build();
        stateMachine.sendEvent(Mono.just(message))
                .subscribe();
    }

    private void assertStates(
            StateMachine<EmployeeState, EmployeeEvent> stateMachine,
            EmployeeState... employeeStates
    ) {
        Assertions.assertTrue(
                stateMachine.getState()
                        .getIds()
                        .containsAll(List.of(employeeStates))
        );
    }
}
