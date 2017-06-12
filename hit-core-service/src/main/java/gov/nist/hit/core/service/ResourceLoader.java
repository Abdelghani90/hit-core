package gov.nist.hit.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import gov.nist.hit.core.domain.CFTestStep;
import gov.nist.hit.core.domain.ResourceType;
import gov.nist.hit.core.domain.ResourceUploadAction;
import gov.nist.hit.core.domain.ResourceUploadResult;
import gov.nist.hit.core.domain.ResourceUploadStatus;
import gov.nist.hit.core.domain.TestCase;
import gov.nist.hit.core.domain.TestCaseGroup;
import gov.nist.hit.core.domain.TestPlan;
import gov.nist.hit.core.domain.TestStep;
import gov.nist.hit.core.domain.TestingStage;
import gov.nist.hit.core.repo.TestCaseGroupRepository;
import gov.nist.hit.core.repo.TestCaseRepository;
import gov.nist.hit.core.repo.TestStepRepository;
import gov.nist.hit.core.service.util.ResourcebundleHelper;

public abstract class ResourceLoader extends ResourcebundleLoader {

  @Autowired
  private TestCaseRepository testCaseRepository;

  @Autowired
  private TestCaseGroupRepository testCaseGroupRepository;

  @Autowired
  private TestStepRepository testStepRepository;

  public List<Resource> getApiDirectories(String pattern) throws IOException {
    String lookup = this.getDirectory() + pattern;
    return ResourcebundleHelper.getDirectoriesFile(lookup);
  }

  public Resource getApiResource(String pattern) throws IOException {
    String lookup = this.getDirectory() + pattern;
    return ResourcebundleHelper.getResourceFile(lookup);
  }

  public List<Resource> getApiResources(String pattern) throws IOException {
    String lookup = this.getDirectory() + pattern;
    return ResourcebundleHelper.getResourcesFile(lookup);
  }

  // ------ Context-Free test Case

  public ResourceUploadStatus handleCFTC(Long testCaseId, CFTestStep tc) {

    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTSTEP);
    result.setId(tc.getPersistentId());

    CFTestStep existing = this.cfTestStepRepository.getByPersistentId(tc.getPersistentId());

    if (existing != null) {
      result.setAction(ResourceUploadAction.UPDATE);
      Long exId = existing.getId();
      tc.setId(exId);
      List<CFTestStep> merged = this.mergeCFTC(tc.getChildren(), existing.getChildren());
      tc.setChildren(merged);
      this.cfTestStepRepository.saveAndFlush(tc);
      result.setStatus(ResourceUploadResult.SUCCESS);
      return result;
    } else {
      result.setAction(ResourceUploadAction.ADD);
      if (testCaseId != null && testCaseId != -1) {
        CFTestStep parent = this.cfTestStepRepository.getByPersistentId(testCaseId);
        if (parent == null) {
          result.setStatus(ResourceUploadResult.FAILURE);
          result.setMessage("CF TestCase(" + testCaseId + ") not found");
          return result;
        } else {
          parent.getChildren().add(tc);
          this.cfTestStepRepository.saveAndFlush(parent);
          result.setStatus(ResourceUploadResult.SUCCESS);
          return result;
        }
      } else {
        // tc.setRoot(true);
        this.cfTestStepRepository.saveAndFlush(tc);
        result.setStatus(ResourceUploadResult.SUCCESS);
        return result;
      }
    }

  }

  // ------ Context-Based test Case

  public ResourceUploadStatus handleTS(Long testCaseId, TestStep ts) {

    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTSTEP);
    result.setId(ts.getPersistentId());

    TestStep existing = this.testStepRepository.getByPersistentId(ts.getPersistentId());

    if (existing != null) {
      result.setAction(ResourceUploadAction.UPDATE);
      Long exId = existing.getId();
      ts.setId(exId);
      ts.setTestCase(existing.getTestCase());
      this.testStepRepository.saveAndFlush(ts);
      result.setStatus(ResourceUploadResult.SUCCESS);
      return result;
    } else {
      result.setAction(ResourceUploadAction.ADD);
      TestCase tc = this.testCaseRepository.getByPersistentId(testCaseId);
      if (tc == null) {
        result.setStatus(ResourceUploadResult.FAILURE);
        result.setMessage("TestCase(" + testCaseId + ") not found");
        return result;
      } else {
        tc.addTestStep(ts);
        this.testCaseRepository.saveAndFlush(tc);
        result.setStatus(ResourceUploadResult.SUCCESS);
        return result;
      }
    }

  }

  public ResourceUploadStatus addTC(Long parentId, TestCase tc, String where) {

    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTCASE);
    result.setAction(ResourceUploadAction.ADD);
    result.setId(tc.getPersistentId());

    if (where.toLowerCase().equals("group")) {
      TestCaseGroup tcg = this.testCaseGroupRepository.getByPersistentId(parentId);
      if (tcg == null) {
        result.setStatus(ResourceUploadResult.FAILURE);
        result.setMessage("TestCaseGroup(" + parentId + ") not found");
        return result;
      } else {
        tcg.getTestCases().add(tc);
        this.testCaseGroupRepository.saveAndFlush(tcg);
        result.setStatus(ResourceUploadResult.SUCCESS);
        return result;
      }

    } else if (where.toLowerCase().equals("plan")) {
      TestPlan tp = this.testPlanRepository.getByPersistentId(parentId);
      if (tp == null) {
        result.setStatus(ResourceUploadResult.FAILURE);
        result.setMessage("TestPlan(" + parentId + ") not found");
        return result;
      } else {
        tp.getTestCases().add(tc);
        this.testPlanRepository.saveAndFlush(tp);
        result.setStatus(ResourceUploadResult.SUCCESS);
        return result;
      }
    } else {
      return null;
    }
  }

  public ResourceUploadStatus updateTC(TestCase tc) {
    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTCASE);
    result.setAction(ResourceUploadAction.UPDATE);
    result.setId(tc.getPersistentId());

    TestCase existing = this.testCaseRepository.getByPersistentId(tc.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      Set<TestStep> merged = this.mergeTS(tc.getTestSteps(), existing.getTestSteps());
      tc.setId(exId);
      tc.setDataMappings(existing.getDataMappings());
      tc.setTestSteps(merged);
      this.testCaseRepository.saveAndFlush(tc);
      result.setStatus(ResourceUploadResult.SUCCESS);
      return result;
    } else {
      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("TestCase(" + tc.getPersistentId() + ") not found");
      return result;
    }

  }

  public ResourceUploadStatus addTCG(Long parentId, TestCaseGroup tcg, String where) {

    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTCASEGROUP);
    result.setAction(ResourceUploadAction.ADD);
    result.setId(tcg.getPersistentId());

    if (where.toLowerCase().equals("plan")) {
      TestPlan tp = this.testPlanRepository.getByPersistentId(parentId);
      if (tp == null) {
        result.setStatus(ResourceUploadResult.FAILURE);
        result.setMessage("TestPlan(" + parentId + ") not found");
        return result;
      } else {
        tp.getTestCaseGroups().add(tcg);
        this.testPlanRepository.saveAndFlush(tp);
        result.setStatus(ResourceUploadResult.SUCCESS);
        return result;
      }
    } else if (where.toLowerCase().equals("group")) {
      TestCaseGroup tcgg = this.testCaseGroupRepository.getByPersistentId(parentId);
      if (tcgg == null) {
        result.setStatus(ResourceUploadResult.FAILURE);
        result.setMessage("TestCaseGroup(" + parentId + ") not found");
        return result;
      } else {
        tcgg.getTestCaseGroups().add(tcg);
        this.testCaseGroupRepository.saveAndFlush(tcgg);
        result.setStatus(ResourceUploadResult.SUCCESS);
        return result;
      }

    } else {
      return null;
    }
  }

  public ResourceUploadStatus updateTCG(TestCaseGroup tcg) {

    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTCASEGROUP);
    result.setAction(ResourceUploadAction.UPDATE);
    result.setId(tcg.getPersistentId());

    TestCaseGroup existing = this.testCaseGroupRepository.getByPersistentId(tcg.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      Set<TestCase> mergedTc = this.mergeTC(tcg.getTestCases(), existing.getTestCases());
      Set<TestCaseGroup> mergedTcg =
          this.mergeTCG(tcg.getTestCaseGroups(), existing.getTestCaseGroups());
      tcg.setId(exId);
      tcg.setTestCases(mergedTc);
      tcg.setTestCaseGroups(mergedTcg);
      this.testCaseGroupRepository.saveAndFlush(tcg);
      result.setStatus(ResourceUploadResult.SUCCESS);
      return result;
    } else {

      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("TestCaseGroup(" + tcg.getPersistentId() + ") not found");
      return result;

    }
  }

  public ResourceUploadStatus handleTP(TestPlan tp) {

    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTPLAN);
    result.setId(tp.getPersistentId());
    TestPlan existing = this.testPlanRepository.getByPersistentId(tp.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      Set<TestCase> mergedTc = this.mergeTC(tp.getTestCases(), existing.getTestCases());
      Set<TestCaseGroup> mergedTcg =
          this.mergeTCG(tp.getTestCaseGroups(), existing.getTestCaseGroups());
      tp.setId(exId);
      tp.setTestCases(mergedTc);
      tp.setTestCaseGroups(mergedTcg);
      result.setAction(ResourceUploadAction.UPDATE);
    } else {
      result.setAction(ResourceUploadAction.ADD);
    }

    this.testPlanRepository.saveAndFlush(tp);
    result.setStatus(ResourceUploadResult.SUCCESS);
    return result;
  }

  // ---- Helper Functions
  // Creation Methods

  public List<TestStep> createTS() throws IOException {

    List<TestStep> tmp = new ArrayList<TestStep>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      TestStep testStep = testStep(fileName + "/", null, false);
      if (testStep != null) {
        tmp.add(testStep);
      }
    }
    return tmp;
  }

  public List<TestCase> createTC() throws IOException {
    List<TestCase> tmp = new ArrayList<TestCase>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      TestCase testCase = testCase(fileName + "/", null, false);
      if (testCase != null) {
        tmp.add(testCase);
      }
    }
    return tmp;
  }

  public List<TestCaseGroup> createTCG() throws IOException {
    List<TestCaseGroup> tmp = new ArrayList<TestCaseGroup>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      TestCaseGroup testCaseGroup = testCaseGroup(fileName + "/", null, false);
      if (testCaseGroup != null) {
        tmp.add(testCaseGroup);
      }
    }
    return tmp;
  }

  public List<TestPlan> createTP() {
    List<TestPlan> tmp = new ArrayList<TestPlan>();
    List<Resource> resources;
    try {
      resources = getApiDirectories("*");
      for (Resource resource : resources) {
        String fileName = resource.getFilename();
        TestPlan testPlan = testPlan(fileName + "/", TestingStage.CB);
        if (testPlan != null) {
          tmp.add(testPlan);
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return tmp;
  }

  public List<CFTestStep> createCFTC() throws IOException {

    List<CFTestStep> tmp = new ArrayList<CFTestStep>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      CFTestStep testObject = cfTestStep(fileName + "/");
      if (testObject != null) {
        tmp.add(testObject);
      }
    }
    return tmp;
  }

  // Merge Methods

  public Set<TestStep> mergeTS(Set<TestStep> newL, Set<TestStep> oldL) {
    int index = -1;
    List<TestStep> tmp = new ArrayList<TestStep>();
    tmp.addAll(oldL);

    for (TestStep tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        tcs.setId(tmp.get(index).getId());
        if (tmp.get(index).getTestContext() != null) {
          tcs.getTestContext().setId(tmp.get(index).getTestContext().getId());
        }
        tmp.set(index, tcs);
      } else {
        tmp.add(tcs);
      }

    }

    return new HashSet<TestStep>(tmp);
  }

  public Set<TestCase> mergeTC(Set<TestCase> newL, Set<TestCase> oldL) {
    int index = -1;
    List<TestCase> tmp = new ArrayList<TestCase>();
    tmp.addAll(oldL);

    for (TestCase tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        Set<TestStep> newLs = mergeTS(tcs.getTestSteps(), tmp.get(index).getTestSteps());
        tcs.setTestSteps(newLs);
        TestCase existing = tmp.get(index);
        tcs.setDataMappings(existing.getDataMappings());
        tcs.setId(existing.getId());
        tmp.set(index, tcs);
      } else {
        tmp.add(tcs);
      }

    }
    return new HashSet<TestCase>(tmp);
  }

  public Set<TestCaseGroup> mergeTCG(Set<TestCaseGroup> newL, Set<TestCaseGroup> oldL) {
    int index = -1;
    List<TestCaseGroup> tmp = new ArrayList<TestCaseGroup>();
    tmp.addAll(oldL);

    for (TestCaseGroup tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        Set<TestCase> newLs = mergeTC(tcs.getTestCases(), tmp.get(index).getTestCases());
        tcs.setTestCases(newLs);
        if (tcs.getTestCaseGroups() != null && tcs.getTestCaseGroups().size() > 0) {
          Set<TestCaseGroup> newLsg =
              mergeTCG(tcs.getTestCaseGroups(), tmp.get(index).getTestCaseGroups());
          tcs.setTestCaseGroups(newLsg);
        }
        tcs.setId(tmp.get(index).getId());
        tmp.set(index, tcs);
      } else {
        tmp.add(tcs);
      }

    }
    return new HashSet<TestCaseGroup>(tmp);
  }

  public List<CFTestStep> mergeCFTC(List<CFTestStep> newL, List<CFTestStep> oldL) {
    int index = -1;
    List<CFTestStep> tmp = new ArrayList<CFTestStep>();
    tmp.addAll(oldL);

    for (CFTestStep tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        CFTestStep existing = tmp.get(index);
        if (existing.getChildren() != null && existing.getChildren().size() > 0) {
          if (tcs.getChildren() != null && tcs.getChildren().size() > 0) {
            List<CFTestStep> children = mergeCFTC(tcs.getChildren(), existing.getChildren());
            tcs.setChildren(children);
          } else {
            tcs.setChildren(existing.getChildren());
          }
        }
        tcs.setId(tmp.get(index).getId());
        tmp.set(index, tcs);
      } else
        tmp.add(tcs);
    }
    return tmp;
  }

  // Delete
  public ResourceUploadStatus deleteTS(Long id) {
    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTSTEP);
    result.setAction(ResourceUploadAction.DELETE);
    result.setId(id);
    TestStep s = this.testStepRepository.getByPersistentId(id);
    if (s == null) {
      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("TestStep(" + id + ") not found");
    } else {
      this.testStepRepository.delete(s.getId());
      result.setStatus(ResourceUploadResult.SUCCESS);
    }
    return result;
  }

  public ResourceUploadStatus deleteTC(Long id) {
    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTCASE);
    result.setAction(ResourceUploadAction.DELETE);
    result.setId(id);
    TestCase s = this.testCaseRepository.getByPersistentId(id);
    if (s == null) {
      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("TestCase(" + id + ") not found");
    } else {
      this.testCaseRepository.delete(s.getId());
      result.setStatus(ResourceUploadResult.SUCCESS);
    }
    return result;
  }

  public ResourceUploadStatus deleteTCG(Long id) {
    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTCASEGROUP);
    result.setAction(ResourceUploadAction.DELETE);
    result.setId(id);
    TestCaseGroup s = this.testCaseGroupRepository.getByPersistentId(id);
    if (s == null) {
      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("TestCaseGroup(" + id + ") not found");
    } else {
      this.testCaseGroupRepository.delete(s.getId());
      result.setStatus(ResourceUploadResult.SUCCESS);
    }
    return result;
  }

  public ResourceUploadStatus deleteTP(Long id) {
    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.TESTPLAN);
    result.setAction(ResourceUploadAction.DELETE);
    result.setId(id);
    TestPlan s = this.testPlanRepository.getByPersistentId(id);
    if (s == null) {
      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("TestPlan(" + id + ") not found");
    } else {
      this.testPlanRepository.delete(s.getId());
      result.setStatus(ResourceUploadResult.SUCCESS);
    }
    return result;
  }

  public ResourceUploadStatus deleteCFTC(Long id) {
    ResourceUploadStatus result = new ResourceUploadStatus();
    result.setType(ResourceType.CFTESTCASE);
    result.setAction(ResourceUploadAction.DELETE);
    result.setId(id);
    CFTestStep s = this.cfTestStepRepository.getByPersistentId(id);
    if (s == null) {
      result.setStatus(ResourceUploadResult.FAILURE);
      result.setMessage("CF TestCase(" + id + ") not found");
    } else {
      this.cfTestStepRepository.delete(s.getId());
      result.setStatus(ResourceUploadResult.SUCCESS);
    }
    return result;
  }

  public void flush() {
    this.testStepRepository.flush();
    this.testCaseRepository.flush();
    this.testCaseGroupRepository.flush();
    this.testPlanRepository.flush();
  }

  public abstract List<ResourceUploadStatus> addOrReplaceValueSet() throws IOException;

  public abstract List<ResourceUploadStatus> addOrReplaceConstraints() throws IOException;

  public abstract List<ResourceUploadStatus> addOrReplaceIntegrationProfile() throws IOException;

}
