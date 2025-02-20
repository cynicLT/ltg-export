package org.cynic.ltg_export.arhitecture;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.cynic.ltg_export.Constants;
import org.junit.jupiter.api.Tag;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Tag("unit")
@AnalyzeClasses(importOptions = ImportOption.DoNotIncludeTests.class, packagesOf = Constants.class)
public class ApplicationTest {

    @ArchTest
    public static final ArchRule ALL_CLASSES_NOT_USING_PAIR =
        ArchRuleDefinition.noClasses()
            .should().accessClassesThat()
            .resideInAPackage("org.apache.commons.lang3.tuple..");


    @ArchTest
    public static final ArchRule CONSTANTS_ARE_FINAL =
        ArchRuleDefinition.theClass(Constants.class)
            .should().haveOnlyFinalFields()
            .andShould().bePublic()
            .andShould().notBeAnnotatedWith(Component.class)
            .andShould().notBeAnnotatedWith(Service.class)
            .because("Constants are not Business Logic");

    @ArchTest
    public static final ArchRule CONSTANTS_HAS_PRIVATE_CONSTRUCTOR =
        ArchRuleDefinition.constructors()
            .that().areDeclaredIn(Constants.class)
            .should().haveModifier(JavaModifier.PRIVATE)
            .because("Constants are singletons");

}
