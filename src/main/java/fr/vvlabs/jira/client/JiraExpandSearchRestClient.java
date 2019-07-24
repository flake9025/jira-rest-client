package fr.vvlabs.jira.client;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSearchRestClient;
import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.atlassian.util.concurrent.Promise;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * based on original code, this extended class allows to customize "expand" query param
 */
public class JiraExpandSearchRestClient extends AsynchronousSearchRestClient implements ExpandSearchRestClient {

  // ===========================================================
  // Constants
  // ===========================================================

  private static final String START_AT_ATTRIBUTE = "startAt";
  private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
  private static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 500;
  private static final String JQL_ATTRIBUTE = "jql";
  private static final String SEARCH_URI_PREFIX = "search";
  private static final String EXPAND_ATTRIBUTE = "expand";
  private static final String FIELDS_ATTRIBUTE = "fields";

  // ===========================================================
  // Fields
  // ===========================================================

  private final SearchResultJsonParser searchResultJsonParser = new SearchResultJsonParser();
  private final List<String> defaultExpand = Arrays.asList(Expandos.SCHEMA.getValue(), Expandos.NAMES.getValue());
  private final URI searchUri;

  // ===========================================================
  // Constructors
  // ===========================================================

  /**
   * Build a new Jira Search Rest Client
   * @param baseUri
   * @param asyncHttpClient
   */
  public JiraExpandSearchRestClient(final URI baseUri, final HttpClient asyncHttpClient) {
    super(baseUri, asyncHttpClient);
    this.searchUri = UriBuilder.fromUri(baseUri).path(SEARCH_URI_PREFIX).build(new Object[0]);
  }

  // ===========================================================
  // Methods for/from SuperClass/Interfaces
  // ===========================================================

  @Override
  public Promise<SearchResult> searchJql(@Nullable String jql, @Nullable Integer maxResults, @Nullable Integer startAt,
      @Nullable Set<String> fields, @Nullable Set<String> expandos) {

    Set<String> expandosValues = new HashSet<>(defaultExpand);
    if(expandos != null){
      expandosValues.addAll(expandos);
    }

    String notNullJql = StringUtils.defaultString(jql);
    return notNullJql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET ? this.searchJqlImplPost(maxResults, startAt, expandosValues, notNullJql, fields)
        : this.searchJqlImplGet(maxResults, startAt, expandosValues, notNullJql, fields);
  }

  // ===========================================================
  // Methods
  // ===========================================================

  private Promise<SearchResult> searchJqlImplGet(@Nullable Integer maxResults, @Nullable Integer startAt, Iterable<String> expandosValues,
      String jql, @Nullable Set<String> fields) {
    UriBuilder uriBuilder = UriBuilder.fromUri(this.searchUri).queryParam(JQL_ATTRIBUTE, new Object[]{jql})
        .queryParam(EXPAND_ATTRIBUTE, new Object[]{Joiner.on(",").join(expandosValues)});
    if (fields != null) {
      uriBuilder.queryParam(FIELDS_ATTRIBUTE, new Object[]{Joiner.on(",").join(fields)});
    }
    this.addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
    this.addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
    return this.getAndParse(uriBuilder.build(new Object[0]), this.searchResultJsonParser);
  }

  private void addOptionalQueryParam(UriBuilder uriBuilder, String key, Object... values) {
    if (values != null && values.length > 0 && values[0] != null) {
      uriBuilder.queryParam(key, values);
    }
  }

  private Promise<SearchResult> searchJqlImplPost(@Nullable Integer maxResults, @Nullable Integer startAt, Iterable<String> expandosValues,
      String jql, @Nullable Set<String> fields) {
    JSONObject postEntity = new JSONObject();
    try {
      postEntity.put(JQL_ATTRIBUTE, jql).put(EXPAND_ATTRIBUTE, ImmutableList.copyOf(expandosValues)).putOpt(START_AT_ATTRIBUTE, startAt)
          .putOpt(MAX_RESULTS_ATTRIBUTE, maxResults);
      if (fields != null) {
        postEntity.put(FIELDS_ATTRIBUTE, fields);
      }
    } catch (JSONException var8) {
      throw new RestClientException(var8);
    }
    return this.postAndParse(this.searchUri, postEntity, this.searchResultJsonParser);
  }
}
