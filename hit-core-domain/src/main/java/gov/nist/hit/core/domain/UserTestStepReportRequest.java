package gov.nist.hit.core.domain;

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
 * <p/>
 * Created by Maxence Lefort on 9/13/16.
 */
public class UserTestStepReportRequest {
    private Long testStepId;

    private Long accountId;

    public UserTestStepReportRequest(Long testStepId, Long accountId) {
        this.testStepId = testStepId;
        this.accountId = accountId;
    }

    public Long getTestStepId() {
        return testStepId;
    }

    public void setTestStepId(Long testStepId) {
        this.testStepId = testStepId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}