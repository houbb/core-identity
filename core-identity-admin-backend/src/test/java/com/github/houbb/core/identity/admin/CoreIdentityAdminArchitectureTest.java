package com.github.houbb.core.identity.admin;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Admin Backend architecture boundary tests.
 */
@DisplayName("Admin Backend Architecture Tests")
class CoreIdentityAdminArchitectureTest {

    private static final String ADMIN_PACKAGE = "com.github.houbb.core.identity.admin";

    @Test
    @DisplayName("Admin Backend should not contain Identity domain objects")
    void adminShouldNotContainIdentityDomain() {
        // Admin backend should not have any classes under identity.application.domain
        // This is enforced by package structure — we verify no classes match this pattern
        var classes = new ClassFileImporter()
                .importPackages(ADMIN_PACKAGE);

        ArchRule rule = noClasses()
                .should().resideInAPackage("..application.domain..");
        rule.check(classes);
    }
}