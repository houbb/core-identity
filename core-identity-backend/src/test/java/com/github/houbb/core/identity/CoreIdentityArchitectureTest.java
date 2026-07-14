package com.github.houbb.core.identity;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture boundary tests.
 * Ensures layered architecture and module boundaries are enforced.
 */
@DisplayName("Architecture Boundary Tests")
class CoreIdentityArchitectureTest {

    private static final String BASE_PACKAGE = "com.github.houbb.core.identity";

    private static JavaClasses identityBackendClasses;

    @BeforeAll
    static void setUp() {
        identityBackendClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("API layer should not depend on infrastructure repositories")
    void apiShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence..");
        rule.check(identityBackendClasses);
    }

    @Test
    @DisplayName("Application layer should not depend on Spring Web")
    void applicationShouldNotDependOnSpringWeb() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.springframework.web..")
                .orShould().dependOnClassesThat()
                .resideInAPackage("jakarta.servlet..");
        rule.check(identityBackendClasses);
    }

    @Test
    @DisplayName("Application layer should not depend on JdbcTemplate")
    void applicationShouldNotDependOnJdbc() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.springframework.jdbc..");
        rule.check(identityBackendClasses);
    }

    @Test
    @DisplayName("Identity Backend should not depend on Admin Backend")
    void identityBackendShouldNotDependOnAdminBackend() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + "..")
                .and().resideOutsideOfPackage(BASE_PACKAGE + ".admin..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".admin..");
        rule.check(identityBackendClasses);
    }
}