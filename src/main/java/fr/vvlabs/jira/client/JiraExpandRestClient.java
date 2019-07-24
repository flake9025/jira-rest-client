package fr.vvlabs.jira.client;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * based on original code, this extended class allows to customize "expand" query param with search client
 */
@Slf4j
public class JiraExpandRestClient extends AsynchronousJiraRestClient {

  // ===========================================================
  // Constants
  // ===========================================================

  private static final String API_URI = "/rest/api/latest";

  // ===========================================================
  // Fields
  // ===========================================================

  private final ExpandSearchRestClient searchRestClient;

  // ===========================================================
  // Constructors
  // ===========================================================

  /**
   * Build a new Jira Rest Client
   */
  public JiraExpandRestClient(final URI serverUri, final DisposableHttpClient httpClient) {
    super(serverUri, httpClient);
    URI baseUri = UriBuilder.fromUri(serverUri).path(API_URI).build(new Object[0]);
    this.searchRestClient = new JiraExpandSearchRestClient(baseUri, httpClient);
  }

  // ===========================================================
  // Methods for/from SuperClass/Interfaces
  // ===========================================================

  @Override
  public ExpandSearchRestClient getSearchClient() {
    return this.searchRestClient;
  }

  // ===========================================================
  // Methods
  // ===========================================================

  /**
   * Get All Projects
   * @return all projects
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public Set<Project> getAllProjects() {
    final Set<Project> projectsResult = new HashSet<>();
    Iterable<BasicProject> jiraProjects = executeWithRetry(() -> getProjectClient().getAllProjects().claim());
    for (BasicProject basicProject : jiraProjects) {
      Project jiraProject = executeWithRetry(() -> getProjectClient().getProject(basicProject.getKey()).claim());
      if(jiraProject != null) {
        projectsResult.add(jiraProject);
      }
    }
    return projectsResult;
  }

  /**
   * Get Projects by Keys
   * @param projectKeys
   * @return projects by keys
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public Set<Project> getProjectsByKeys(Set<String> projectKeys)  {
    final Set<Project> projectsResult = new HashSet<>();
    for (String projectKey : projectKeys) {
      Project jiraProject = (executeWithRetry(() -> getProjectClient().getProject(projectKey).claim()));
      if(jiraProject != null) {
        projectsResult.add(jiraProject);
      }
    }
    return projectsResult;
  }

  /**
   * Get Issues paginated
   * @param customJQL
   * @param start
   * @param maxResults
   * @return
   */
  public List<Issue> getIssues(String customJQL, int start, int maxResults){
    return getIssues(customJQL, start, maxResults, null, null, null, null, null);
  }

  /**
   * Get Issues peginated, with projects filter, custom fields and expandable properties
   * @param customJQL
   * @param start
   * @param maxResults
   * @param projects
   * @param fields
   * @param expandos
   * @return
   */
  public List<Issue> getIssues(String customJQL, int start, int maxResults, Set<String> projects, Set<String> fields, Set<String> expandos){
    return getIssues(customJQL, start, maxResults, projects, fields, expandos, null, null);
  }

  /**
   * Get Issues peginated, with projects filter, custom fields and expandable properties, from / to dates
   * @param customJQL
   * @param start
   * @param maxResults
   * @param projects
   * @param fields
   * @param expandos
   * @param startDate
   * @param endDate
   * @return
   */
  public List<Issue> getIssues(String customJQL, int start, int maxResults, Set<String> projects, Set<String> fields, Set<String> expandos, LocalDateTime startDate, LocalDateTime endDate) {
    List<Issue> listOfIssues = new ArrayList<>();
    // Custom jql filter
    customJQL = customJQL != null ? customJQL : "";
    // projects filter
    if(projects != null && !projects.isEmpty()) {
      // REST api needs projects separated with quotes
      projects = projects.stream().map(p -> "\"" + p + "\"").collect(Collectors.toSet());
      String projectFilter = "project IN (" + String.join(",", projects) + ")";
      customJQL += StringUtils.isNotBlank(customJQL) ? " AND "  + projectFilter : projectFilter;
    }
    // incremental sync
    if (startDate != null) {
      String lastSyncFilter = "updated > " + startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
      customJQL += StringUtils.isNotBlank(customJQL) ? " AND "  + lastSyncFilter : lastSyncFilter;
    }
    if (endDate != null) {
      String startSyncFilter = "updated < " + endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
      customJQL += StringUtils.isNotBlank(customJQL) ? " AND "  + startSyncFilter : startSyncFilter;
    }
    final String jqlQuery = customJQL;
    log.debug("JQL Query : {}", jqlQuery);

    SearchResult results = executeWithRetry(() -> getSearchClient().searchJql(jqlQuery, maxResults, start, fields, expandos).claim());
    if (results != null && results.getIssues() != null) {
      results.getIssues().forEach(listOfIssues::add);
    }
    return listOfIssues;
  }

  /**
   * Get Issue By Key
   * @param issueKey
   * @return
   */
  public Issue getIssueByKey(String issueKey) {
   return executeWithRetry(() -> getIssueClient().getIssue(issueKey).claim());
  }

  /**
   * execute call with retry
   * @param s
   * @param <T>
   * @return
   */
  @Retryable(maxAttempts = 2, backoff = @Backoff(delay = 500))
  public <T> T executeWithRetry(Supplier<T> s) {
    try {
      return s.get();
    } catch (Exception e) {
      log.warn("executeWithRetry(...) got exception, retry ...", e.getMessage(), e);
      throw e;
    }
  }
}
