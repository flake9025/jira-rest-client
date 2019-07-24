package fr.vvlabs.jira.client;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import io.atlassian.util.concurrent.Promise;
import java.util.Set;
import javax.annotation.Nullable;

public interface ExpandSearchRestClient extends SearchRestClient {

  Promise<SearchResult> searchJql(@Nullable String jql, @Nullable Integer maxResults, @Nullable Integer startAt,
      @Nullable Set<String> fields, @Nullable Set<String> expandos);
}
