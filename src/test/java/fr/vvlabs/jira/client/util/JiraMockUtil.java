package fr.vvlabs.jira.client.util;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

public class JiraMockUtil {

  public static final String JIRA_FIELD_ACTIVITY_DOMAIN_ID = "customfield_10300";
  public static final String JIRA_FIELD_ACTIVITY_DOMAIN_NAME = "DomaineActivite";
  public static final String JIRA_FIELD_ESTIMATION_ID = "customfield_10301";
  public static final String JIRA_FIELD_ESTIMATION_NAME = "Estimation Loom";
  public static final String JIRA_FIELD_VISIBLITY_ID = "customfield_10302";
  public static final String JIRA_FIELD_VISIBLITY_NAME = "Visibility";
  public static final String JIRA_FIELD_TIME_SPENT_ID = "customfield_10620";
  public static final String JIRA_FIELD_TIME_SPENT_NAME = "Réalisé Loom";
  public static final String JIRA_FIELD_ESTIMATION_REVALUED_ID = "customfield_10650";
  public static final String JIRA_FIELD_ESTIMATION_REVALUED_NAME = "Estimation Loom Réévaluée";
  public static final String JIRA_FIELD_TECHNOLOGY_ID = "customfield_10700";
  public static final String JIRA_FIELD_TECHNOLOGY_NAME = "TechnologiePrincipale";
  public static final String JIRA_FIELD_DELIVERY_ID = "customfield_11612";
  public static final String JIRA_FIELD_DELIVERY_NAME = "Date de livraison";
  public static final String JIRA_FIELD_BILLING_ID = "customfield_11632";
  public static final String JIRA_FIELD_BILLING_NAME = "Date de facturation";
  public static final String JIRA_FIELD_DUE_DATE_ID = "duedate";
  public static final String JIRA_FIELD_DUE_DATE_NAME = "Date d'échéance";

  public static final String[] JIRA_ACTIVITY_DOMAINS = {"Développement", "Design", "Fonctionnel", "Admin sys", "Tests Auto"};
  public static final String[] JIRA_VISIBILITY = {"Aucun(e)", "Mon projet", "Les loomers", "Le monde"};
  public static final String[] JIRA_STATUS = {"Aucun(e)", "Mon projet", "Les loomers", "Le monde"};

  public static final Map<Long, String> JIRA_STATUS_MAP = new HashMap<>();

  static {
    JIRA_STATUS_MAP.put(1L, "TODO");
    JIRA_STATUS_MAP.put(3L, "IN_PROGRESS");
    JIRA_STATUS_MAP.put(10300L, "IN_REVIEW");
    JIRA_STATUS_MAP.put(6L, "DONE");
    JIRA_STATUS_MAP.put(10500L, "CANCELLED");
  }

  public static final String LOOM_RECETTE_B = "Loom  Recette B";
  public static final String LOOM_RECETTE_A = "Loom Recette A";

  public static Issue createIssue(long jiraId, String jiraIssueKey) {

    IssueField fieldActivityDomain = createIssueField(JiraMockUtil.JIRA_FIELD_ACTIVITY_DOMAIN_ID, JiraMockUtil.JIRA_FIELD_ACTIVITY_DOMAIN_NAME, JIRA_ACTIVITY_DOMAINS[1]);
    IssueField fieldEstimationLoom = createIssueField(JiraMockUtil.JIRA_FIELD_ESTIMATION_ID, JiraMockUtil.JIRA_FIELD_ESTIMATION_NAME, "0.5");
    IssueField fieldVisibility = createIssueField(JiraMockUtil.JIRA_FIELD_VISIBLITY_ID, JiraMockUtil.JIRA_FIELD_VISIBLITY_NAME, JIRA_VISIBILITY[1]);
    IssueField fieldTimeSpent = createIssueField(JiraMockUtil.JIRA_FIELD_TIME_SPENT_ID, JiraMockUtil.JIRA_FIELD_TIME_SPENT_NAME, "1");
    IssueField fieldReevaluate = createIssueField(JiraMockUtil.JIRA_FIELD_ESTIMATION_REVALUED_ID, JiraMockUtil.JIRA_FIELD_ESTIMATION_REVALUED_NAME, "1");
    IssueField fieldTechnologiePrincipale = createIssueField(JiraMockUtil.JIRA_FIELD_TECHNOLOGY_ID, JiraMockUtil.JIRA_FIELD_TECHNOLOGY_NAME, "Java");

    java.util.List<IssueField> fields = new ArrayList<>();
    fields.add(fieldActivityDomain);
    fields.add(fieldEstimationLoom);
    fields.add(fieldVisibility);
    fields.add(fieldTimeSpent);
    fields.add(fieldReevaluate);
    fields.add(fieldTechnologiePrincipale);

    URI uri = URI.create("http://uri.com/self");
    BasicProject project = new BasicProject(uri, "key", 1L, LOOM_RECETTE_A);

    Long jiraResolutionId = 7654L;
    String resolutionDescription = "a resolution";
    Resolution resolution = new Resolution(uri, jiraResolutionId, resolutionDescription, resolutionDescription);

    Map<String, URI> avatars = new HashMap<>();
    avatars.put(User.S48_48, uri);
    User user = createUser("test@jira.com");

    ChangelogItem item1 = createJiraChangelogItem(IssueFieldId.STATUS_FIELD, "11448", "A chiffrer", "10800", "SELECTED FOR LOOM DEVELOPMENT");
    ChangelogItem item2 = createJiraChangelogItem(IssueFieldId.STATUS_FIELD, "3", "In Progress", "10300", "IN REVIEW");
    ChangelogGroup group1 = createChangelogGroup(2018,6,15, user, Arrays.asList(item1, item2));

    ChangelogItem item3 = createJiraChangelogItem(IssueFieldId.STATUS_FIELD,  "1001", "Task", "10103", "Bug");
    ChangelogItem item4 = createCustomChangelogItem("Estimation Loom", null, null, null, "1");
    ChangelogGroup group2 = createChangelogGroup(2018,7,10, user, Arrays.asList(item3, item4));

    ChangelogItem item5 = createJiraChangelogItem(IssueFieldId.ISSUE_TYPE_FIELD, "1001", "Task", "10103", "Bug");
    ChangelogItem item6 = createJiraChangelogItem(IssueFieldId.STATUS_FIELD, "10300", "In Review", "11426", "A déployer");
    ChangelogGroup group3 = createChangelogGroup(2018,5,20, user, Arrays.asList(item5, item6));

    List<ChangelogGroup> changelog = Arrays.asList(group1, group2, group3);
    return new Issue("summary", uri, jiraIssueKey, jiraId, project, null, createIssueStatus(1L), "description", null, resolution, null, null,
        user, null, null, null, null, null, null, null, fields, null, null, null, null, null, null, null, null, changelog, null, null);
  }

  public static Status createIssueStatus(long statusId) {
    URI uri = URI.create("http://uri.com/self");
    String statusName = JIRA_STATUS_MAP.containsKey(statusId) ? JIRA_STATUS_MAP.get(statusId) : "";
    return new com.atlassian.jira.rest.client.api.domain.Status(uri, statusId, statusName, "", null);
  }

  public static IssueField createIssueField(String fieldId, String fieldName, String fieldValue) {
    JSONObject jsonValueVisibility = new JSONObject();
    try {
      jsonValueVisibility.put("value", fieldValue);
    }catch (JSONException e){ }
    IssueField fieldVisibility = new IssueField(fieldId, fieldName, "", jsonValueVisibility);
    return fieldVisibility;
  }

  public static Project createProject(Long idProject, String keyProject) {
    URI uri = URI.create("http://uri.com/self");
    Project project = new Project(null, uri, keyProject, idProject, "TEST", "TEST", null, null,
        null, null, null, null);
    return project;
  }

  public static User createUser(String token) {
    URI uri = URI.create("http://uri.com/self");
    Map<String, URI> avatars = new HashMap<>();
    avatars.put(User.S48_48, uri);
    User user = new User(uri, "test user", "M Test user", token, true, null, avatars, null);
    return user;
  }


  public static ChangelogGroup createChangelogGroup(int year, int month, int day, User user, List<ChangelogItem> items){
    DateTime date = (new DateTime()).withDate(year, month, day).withTime(0, 0, 0, 0);
    return new ChangelogGroup(user, date, items);
  }

  public static ChangelogItem createJiraChangelogItem(IssueFieldId field, String fromId, String fromString, String toId, String toString){
    return new ChangelogItem(FieldType.JIRA, field.id, fromId, fromString, toId, toString);
  }

  public static ChangelogItem createCustomChangelogItem(String field,  String fromId, String fromString, String toId, String toString){
    return new ChangelogItem(FieldType.CUSTOM, field, fromId, fromString, toId, toString);
  }

}
