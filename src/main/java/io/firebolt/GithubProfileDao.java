package io.firebolt;

import io.firebolt.jdbi.Cached;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public interface GithubProfileDao {

    @Cached
    @SqlQuery("SELECT distinct(event_type) FROM gharchive")
    List<EventType> fetchAllEventTypes();

    @Cached
    @SqlQuery("SELECT count(distinct(repo_name)) FROM gharchive WHERE actor_login = ? ;")
    long totalRepoCountInteractedWith(String login);

    @Cached
    @SqlQuery("SELECT count(*) FROM gharchive")
    long totalCount();

    @Cached
    @SqlQuery("""
                SELECT event_type, count(*) AS event_count
                FROM gharchive
                WHERE actor_login = ?
                GROUP BY event_type
                ORDER BY event_count DESC
            """)
    @KeyColumn("event_type")
    @ValueColumn("event_count")
    LinkedHashMap<EventType, Integer> fetchTypesAndCount(String login);

    @Cached
    @SqlQuery("""
                SELECT EXTRACT(YEAR FROM created_at) as year, count(*) AS event_count
                FROM gharchive
                WHERE actor_login = ?
                GROUP BY year
                ORDER BY year DESC;
            """)
    @KeyColumn("year")
    @ValueColumn("event_count")
    LinkedHashMap<String, Integer> activityByYear(String login);

    @Cached
    @SqlQuery("""
                SELECT repo_name, count(*) as cnt
                FROM gharchive
                WHERE actor_login =  ?
                GROUP BY repo_name
                ORDER BY cnt DESC
                LIMIT 1;
            """)
    Optional<String> repoWithMostActivity(String Login);

    // TODO FIXME THIS IS SLOW
    @Cached
    @SqlQuery("""
            SELECT html_url, max(issue_comment_event_issue_comments) as comment_count
            FROM gharchive
            WHERE actor_login = ? and event_type = 'IssueCommentEvent'
            GROUP BY html_url
            ORDER BY comment_count DESC
            LIMIT 10;
            """)
    @KeyColumn("html_url")
    @ValueColumn("commented_count")
    LinkedHashMap<String, Integer> mostActiveIssuesWithComments(String login);
}
