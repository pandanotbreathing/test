package com.example.demo.domain.config;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import com.example.demo.domain.model.EmployeeStateRegion;
import com.example.demo.domain.state.machine.ApproveTransitionGuard;
import com.example.demo.domain.state.machine.TriggerApproveAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<EmployeeState, EmployeeEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<EmployeeState, EmployeeEvent> states) throws Exception {
        states.withStates()
                .initial(EmployeeState.ADDED)
                .region(EmployeeStateRegion.MAIN.name())
                .state(EmployeeState.ADDED)
                .state(EmployeeState.IN_CHECK)
                .state(EmployeeState.APPROVED)
                .end(EmployeeState.ACTIVE)
                .and()
                .withStates()
                    .parent(EmployeeState.IN_CHECK)
                    .region(EmployeeStateRegion.SECURITY_CHECK.name())
                    .initial(EmployeeState.SECURITY_CHECK_STARTED)
                    .state(EmployeeState.SECURITY_CHECK_STARTED)
                    .state(EmployeeState.SECURITY_CHECK_FINISHED)
                    .end(EmployeeState.SECURITY_CHECK_FINISHED)
                .and()
                .withStates()
                    .parent(EmployeeState.IN_CHECK)
                    .region(EmployeeStateRegion.WORK_PERMIT.name())
                    .initial(EmployeeState.WORK_PERMIT_CHECK_STARTED)
                    .state(EmployeeState.WORK_PERMIT_CHECK_STARTED)
                    .state(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION)
                    .state(EmployeeState.WORK_PERMIT_CHECK_FINISHED)
                    .end(EmployeeState.WORK_PERMIT_CHECK_FINISHED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<EmployeeState, EmployeeEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(EmployeeState.ADDED)
                .target(EmployeeState.IN_CHECK)
                .event(EmployeeEvent.BEGIN_CHECK)
                    .and()
                    .withExternal()
                    .source(EmployeeState.WORK_PERMIT_CHECK_STARTED)
                    .target(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION)
                    .event(EmployeeEvent.COMPLETE_INITIAL_WORK_PERMIT_CHECK)
                    .and()
                    .withExternal()
                    .source(EmployeeState.WORK_PERMIT_CHECK_PENDING_VERIFICATION)
                    .target(EmployeeState.WORK_PERMIT_CHECK_FINISHED)
                    .action(new TriggerApproveAction())
                    .event(EmployeeEvent.FINISH_WORK_PERMIT_CHECK)
                    .and()
                    .withExternal()
                    .source(EmployeeState.SECURITY_CHECK_STARTED)
                    .target(EmployeeState.SECURITY_CHECK_FINISHED)
                    .event(EmployeeEvent.FINISH_SECURITY_CHECK)
                    .action(new TriggerApproveAction())
                .and()
                .withExternal()
                .source(EmployeeState.IN_CHECK)
                .target(EmployeeState.APPROVED)
                .event(EmployeeEvent.APPROVE)
                .guard(new ApproveTransitionGuard())
                .and()
                .withExternal()
                .source(EmployeeState.APPROVED)
                .target(EmployeeState.ACTIVE)
                .event(EmployeeEvent.ACTIVATE);
    }
}
