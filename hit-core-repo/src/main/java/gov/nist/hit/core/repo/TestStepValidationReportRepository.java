/**
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 */

package gov.nist.hit.core.repo;

import gov.nist.hit.core.domain.TestStepValidationReport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestStepValidationReportRepository extends
    JpaRepository<TestStepValidationReport, Long> {

  @Query("select report from TestStepValidationReport report where report.user.id = :userId and report.testStep.id = :testStepId")
  TestStepValidationReport findOneByTestStepAndUser(@Param("userId") Long userId,
      @Param("testStepId") Long testStepId);

  @Query("select report from TestStepValidationReport report where report.user.id = :userId and report.testStep.id = :testStepId")
  List<TestStepValidationReport> findAllByTestStepAndUser(@Param("userId") Long userId,
      @Param("testStepId") Long testStepId);

  @Query("select report from TestStepValidationReport report where report.user.id = :userId and report.testStep.id IN (:testStepIds)")
  List<TestStepValidationReport> findAllByTestSteps(@Param("userId") Long userId,
      @Param("testStepIds") List<Long> testStepIds);

  @Query("select report from TestStepValidationReport report where report.user.id = :userId and report.testStep.testCase.id = :testCaseId")
  List<TestStepValidationReport> findAllByTestCaseAndUser(@Param("userId") Long userId,
      @Param("testCaseId") Long testCaseId);

  @Query("select report from TestStepValidationReport report where report.user.id = :userId")
  List<TestStepValidationReport> findAllByUser(@Param("userId") Long userId);

  @Query("select report from TestStepValidationReport report where report.id = :reportId and report.user.id = :userId")
  TestStepValidationReport findOneByIdAndUser(@Param("reportId") Long reportId,
      @Param("userId") Long userId);

}
