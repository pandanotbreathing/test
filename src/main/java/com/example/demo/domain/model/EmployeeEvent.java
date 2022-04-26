package com.example.demo.domain.model;

public enum EmployeeEvent {
    BEGIN_CHECK,
    COMPLETE_INITIAL_WORK_PERMIT_CHECK,
    FINISH_WORK_PERMIT_CHECK,
    FINISH_SECURITY_CHECK,
    APPROVE, //internal automatic transition state
    ACTIVATE
}
