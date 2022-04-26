package com.example.demo.domain.state.machine;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import java.util.List;

public class ApproveTransitionGuard implements Guard<EmployeeState, EmployeeEvent> {
    @Override
    public boolean evaluate(StateContext<EmployeeState, EmployeeEvent> stateContext) {
        return stateContext.getStateMachine()
                .getState()
                .getIds()
                .containsAll(
                        List.of(EmployeeState.SECURITY_CHECK_FINISHED, EmployeeState.WORK_PERMIT_CHECK_FINISHED)
                );
    }
}
