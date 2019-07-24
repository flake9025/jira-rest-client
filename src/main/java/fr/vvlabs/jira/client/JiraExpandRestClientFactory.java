package fr.vvlabs.jira.client;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import java.net.URI;

/**
 * based on original code, this extended class allows to customize "expand" query param with search client
 */
public class JiraExpandRestClientFactory implements JiraRestClientFactory {

  public JiraExpandRestClientFactory() {
  }

  public JiraExpandRestClient create(URI serverUri, AuthenticationHandler authenticationHandler) {
    DisposableHttpClient httpClient = (new AsynchronousHttpClientFactory()).createClient(serverUri, authenticationHandler);
    return new JiraExpandRestClient(serverUri, httpClient);
  }

  public JiraExpandRestClient createWithBasicHttpAuthentication(URI serverUri, String username, String password) {
    return this.create(serverUri, (AuthenticationHandler)(new BasicHttpAuthenticationHandler(username, password)));
  }

  public JiraExpandRestClient createWithAuthenticationHandler(URI serverUri, AuthenticationHandler authenticationHandler) {
    return this.create(serverUri, authenticationHandler);
  }

  public JiraExpandRestClient create(URI serverUri, HttpClient httpClient) {
    DisposableHttpClient disposableHttpClient = (new AsynchronousHttpClientFactory()).createClient(httpClient);
    return new JiraExpandRestClient(serverUri, disposableHttpClient);
  }
}
