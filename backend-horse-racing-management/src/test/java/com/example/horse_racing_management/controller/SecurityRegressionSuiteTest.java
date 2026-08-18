package com.example.horse_racing_management.controller;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;



@Suite
@SelectClasses({
        AdminSecurityRegressionTest.class,
        RefereeControllerSecurityTest.class,
        ControllerSecurityBroadRegressionTest.class,
        AuthControllerSecurityRegressionTest.class,
        AdminApprovalSecurityRegressionTest.class,
        UserRoleSecurityRegressionTest.class
})
public class SecurityRegressionSuiteTest {
    /**
     * Regression suite cho bảo mật backend.
     * Mục tiêu là đảm bảo các quy tắc phân quyền
     *
     * Suite này gom các test liên quan đến:
     * - admin-only access
     * - referee access
     * - auth/profile endpoints
     * - admin approval workflow
     * - user/role management permissions
     */// Run test: ./mvnw -Dtest=SecurityRegressionSuiteTest test
}
