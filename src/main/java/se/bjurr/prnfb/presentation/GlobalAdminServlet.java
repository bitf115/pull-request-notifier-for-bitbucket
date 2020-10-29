package se.bjurr.prnfb.presentation;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import java.net.URI;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import se.bjurr.prnfb.service.UserCheckService;

@BitbucketComponent
public class GlobalAdminServlet extends HttpServlet {
  private static final long serialVersionUID = 3846987953228399693L;
  private final LoginUriProvider loginUriProvider;
  private final TemplateRenderer renderer;
  private final RepositoryService repositoryService;
  private final ProjectService projectService;
  private final UserCheckService userCheckService;
  private final UserManager userManager;
  private final PageBuilderService pageBuilderService;

  @Autowired
  public GlobalAdminServlet(
      @ComponentImport final UserManager userManager,
      @ComponentImport final LoginUriProvider loginUriProvider,
      @ComponentImport final TemplateRenderer renderer,
      @ComponentImport final RepositoryService repositoryService,
      @ComponentImport final PageBuilderService pageBuilderService,
      UserCheckService userCheckService,
      ProjectService projectService) {
    this.userManager = userManager;
    this.loginUriProvider = loginUriProvider;
    this.renderer = renderer;
    this.repositoryService = repositoryService;
    this.pageBuilderService = pageBuilderService;
    this.userCheckService = userCheckService;
    this.projectService = projectService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      UserProfile user = this.userManager.getRemoteUser(request);
      if (user == null) {
        response.sendRedirect(this.loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
        return;
      }

      String projectKey = null;
      String repositorySlug = null;

      final Optional<Repository> repository = getRepository(request.getPathInfo());
      if (repository.isPresent()) {
        projectKey = repository.get().getProject().getKey();
        repositorySlug = repository.get().getSlug();
      }

      final Optional<Project> project = getProject(request.getPathInfo());
      if (project.isPresent()) {
        projectKey = project.get().getKey();
        repositorySlug = null;
      }

      boolean isAdmin =
          this.userCheckService.isAdmin(user.getUserKey(), projectKey, repositorySlug);
      boolean isSystemAdmin = this.userCheckService.isSystemAdmin(user.getUserKey());

      Map<String, Object> context = newHashMap();
      if (repository.isPresent()) {
        context =
            of( //
                "repository",
                repository.get(), //
                "isAdmin",
                isAdmin, //
                "isSystemAdmin",
                isSystemAdmin);
      } else if (project.isPresent()) {
        context =
            of( //
                "project",
                project.get(), //
                "isAdmin",
                isAdmin, //
                "isSystemAdmin",
                isSystemAdmin);
      } else {
        context =
            of( //
                "isAdmin", isAdmin, //
                "isSystemAdmin", isSystemAdmin);
      }

      response.setContentType("text/html;charset=UTF-8");
      pageBuilderService
          .assembler()
          .resources()
          .requireWebResource("se.bjurr.prnfb.pull-request-notifier-for-bitbucket:resources");
      this.renderer.render( //
          "admin.vm", //
          context, //
          response.getWriter());
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
    }
  }

  private URI getUri(HttpServletRequest request) {
    StringBuffer builder = request.getRequestURL();
    if (request.getQueryString() != null) {
      builder.append("?");
      builder.append(request.getQueryString());
    }
    return URI.create(builder.toString());
  }

  @VisibleForTesting
  Optional<Project> getProject(String pathInfo) {
    java.util.Optional<String[]> componentsOpt = getComponents(pathInfo);
    if (!componentsOpt.isPresent() || componentsOpt.get().length != 1) {
      return absent();
    }
    String[] components = componentsOpt.get();
    String projectKey = components[0];
    Project project = projectService.getByKey(projectKey);
    return Optional.of(project);
  }

  @VisibleForTesting
  Optional<Repository> getRepository(String pathInfo) {
    java.util.Optional<String[]> componentsOpt = getComponents(pathInfo);
    if (!componentsOpt.isPresent() || componentsOpt.get().length != 2) {
      return absent();
    }
    String[] components = componentsOpt.get();
    String project = components[0];
    String repoSlug = components[1];
    final Repository repository =
        checkNotNull(
            this.repositoryService.getBySlug(project, repoSlug), //
            "Did not find " + project + " " + repoSlug);
    return Optional.of(repository);
  }

  private java.util.Optional<String[]> getComponents(String pathInfo) {
    if (pathInfo == null || pathInfo.isEmpty()) {
      return java.util.Optional.empty();
    }
    int indexOf = pathInfo.indexOf("prnfb/admin/");
    if (indexOf == -1) {
      return java.util.Optional.empty();
    }
    String root = pathInfo.substring(indexOf + "prnfb/admin/".length());
    if (root.isEmpty()) {
      return java.util.Optional.empty();
    }
    String[] split = root.split("/");
    return java.util.Optional.of(split);
  }
}
