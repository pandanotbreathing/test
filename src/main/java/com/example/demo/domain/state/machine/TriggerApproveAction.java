package com.example.demo.domain.state.machine;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import reactor.core.publisher.Mono;

public class TriggerApproveAction implements Action<EmployeeState, EmployeeEvent> {
    @Override
    public void execute(StateContext<EmployeeState, EmployeeEvent> stateContext) {
        Message<EmployeeEvent> message = MessageBuilder.createMessage(
                EmployeeEvent.APPROVE,
                stateContext.getMessageHeaders()
        );
        stateContext.getStateMachine()
                .sendEvent(Mono.just(message))
                .subscribe();
    }
}
