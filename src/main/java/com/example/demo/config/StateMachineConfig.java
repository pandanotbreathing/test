package com.example.demo.config;

import com.example.demo.domain.model.EmployeeEvent;
import com.example.demo.domain.model.EmployeeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<EmployeeState, EmployeeEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<EmployeeState, EmployeeEvent> states) throws Exception {
        states.withStates()
                .initial(EmployeeState.ADDED)
                .region("Main")
                .state(EmployeeState.ADDED)
                .state(EmployeeState.IN_CHECK)
                .state(EmployeeState.APPROVED)
                .end(EmployeeState.ACTIVE)
                .and()
                .withStates()
                    .parent(EmployeeState.IN_CHECK)
                    .region("SecurityCheck")
                    .initial(EmployeeState.SECURITY_CHECK_STARTED)
                    .state(EmployeeState.SECURITY_CHECK_STARTED)
                    .state(EmployeeState.SECURITY_CHECK_FINISHED)
                    .end(EmployeeState.SECURITY_CHECK_FINISHED)
                .and()
                .withStates()
                    .parent(EmployeeState.IN_CHECK)
                    .region("WorkPermitCheck")
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
                    .event(EmployeeEvent.FINISH_WORK_PERMIT_CHECK)
                    .and()
                    .withExternal()
                    .source(EmployeeState.SECURITY_CHECK_STARTED)
                    .target(EmployeeState.SECURITY_CHECK_FINISHED)
                    .event(EmployeeEvent.FINISH_SECURITY_CHECK)
                .and()
                .withExternal()
                .source(EmployeeState.IN_CHECK)
                .target(EmployeeState.APPROVED)
                .and()
                .withExternal()
                .source(EmployeeState.APPROVED)
                .target(EmployeeState.ACTIVE)
                .event(EmployeeEvent.ACTIVATE);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<EmployeeState, EmployeeEvent> config) throws Exception {
        StateMachineListenerAdapter<EmployeeState, EmployeeEvent> adapter = new StateMachineListenerAdapter<>(){
//            @Override
//            public void stateChanged(State<EmployeeState, EmployeeEvent> from, State<EmployeeState, EmployeeEvent> to) {
//                log.info("state changed from: {}, to: {}", from, to);
//            }

            @Override
            public void transitionEnded(Transition<EmployeeState, EmployeeEvent> transition) {
                log.info("transition ended {}", transition);
            }
        };

        config.withConfiguration().listener(adapter);
    }
}
