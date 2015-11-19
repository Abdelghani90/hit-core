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

package gov.nist.hit.core.api;

import gov.nist.hit.core.domain.AppInfo;
import gov.nist.hit.core.repo.AppInfoRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Harold Affo (NIST)
 * 
 */
@RestController
@RequestMapping("/appInfo")
public class AppInfoController {


  @Autowired
  private AppInfoRepository appInfoRepository;

  @RequestMapping(method = RequestMethod.GET)
  public AppInfo info(HttpServletRequest request) {
    List<AppInfo> infos = appInfoRepository.findAll();
    if (infos != null && !infos.isEmpty()) {
      AppInfo appInfo = infos.get(0);
      appInfo.setDate(request.getServletContext().getInitParameter("dTime"));
      appInfo.setCsrfToken(request.getServletContext().getInitParameter("csrfToken"));
      appInfo.setUrl(getUrl(request));
      return appInfo;
    }
    return new AppInfo();
  }


  private String getUrl(HttpServletRequest request) {
    String scheme = request.getScheme();
    String host = request.getHeader("Host");
    // return scheme + "://" + host + "/hit-tool";
    String url = scheme + "://" + host + request.getContextPath();
    return url;
  }
}