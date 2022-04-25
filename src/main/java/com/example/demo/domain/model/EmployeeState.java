package com.example.demo.domain.model;

public enum EmployeeState {
    ADDED(EmployeeStateRegion.MAIN),
    IN_CHECK(EmployeeStateRegion.MAIN),
    SECURITY_CHECK_STARTED(EmployeeStateRegion.SECURITY_CHECK),
    SECURITY_CHECK_FINISHED(EmployeeStateRegion.SECURITY_CHECK),
    WORK_PERMIT_CHECK_STARTED(EmployeeStateRegion.WORK_PERMIT),
    WORK_PERMIT_CHECK_PENDING_VERIFICATION(EmployeeStateRegion.WORK_PERMIT),
    WORK_PERMIT_CHECK_FINISHED(EmployeeStateRegion.WORK_PERMIT),
    APPROVED(EmployeeStateRegion.MAIN),
    ACTIVE(EmployeeStateRegion.MAIN);

    private final EmployeeStateRegion region;

    EmployeeState(EmployeeStateRegion region) {
        this.region = region;
    }

    public EmployeeStateRegion getRegion() {
        return region;
    }
}
