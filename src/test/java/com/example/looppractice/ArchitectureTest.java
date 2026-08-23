package com.example.looppractice;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

/** 層の向きを機械的に固定する。エージェントが「動くけれど構造を壊す」変更を入れたときに、 レビューではなくビルドで落ちるようにするのが目的。 */
@AnalyzeClasses(
    packages = "com.example.looppractice",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_WEB =
      ArchRuleDefinition.noClasses()
          .that()
          .resideInAPackage("..text..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..web..");

  @ArchTest
  static final ArchRule CONTROLLERS_MUST_LIVE_IN_WEB_PACKAGE =
      ArchRuleDefinition.classes()
          .that()
          .haveSimpleNameEndingWith("Controller")
          .should()
          .resideInAPackage("..web..");
}
