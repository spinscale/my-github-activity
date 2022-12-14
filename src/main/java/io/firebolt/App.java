package io.firebolt;

import com.firebolt.jdbc.exception.FireboltException;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.javalin.CallbackHandler;
import org.pac4j.javalin.JavalinWebContext;
import org.pac4j.javalin.LogoutHandler;
import org.pac4j.javalin.SecurityHandler;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.profile.github.GitHubProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        boolean isProduction = !Files.exists(Paths.get("src", "main", "jte"));
        App javalinApp = new App(isProduction);
        javalinApp.run();

        // proper shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(javalinApp::stop));
    }

    private final Javalin app;
    private Connection connection;
    private final GithubProfileDao githubProfileDao;

    App(boolean isProduction) throws Exception {
        Jdbi jdbi = setupJDBI();
        this.githubProfileDao = jdbi.onDemand(GithubProfileDao.class);

        if (isProduction) {
            JavalinJte.init(TemplateEngine.createPrecompiled(ContentType.Html));
        } else {
            JavalinJte.init();
        }

        // required for JS files, all packages properly as it is within resources?
        app = Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
            config.showJavalinBanner = false;
            config.routing.ignoreTrailingSlashes = true;
        });

        app.exception(Exception.class, (exception, ctx) -> {
            // this might be a DB connection issue, let's call it maintenance mode to the outside
            boolean databaseConnectionIssue = exception instanceof FireboltException;
            Optional<GitHubProfile> profile = retrieveProfile(ctx);
            ctx.render("exception.jte", Map.of("profile", profile, "databaseConnectionIssue", databaseConnectionIssue));
        });

        GitHubClient gitHubClient = new GitHubClient(getenv("GITHUB_OAUTH_KEY"), getenv("GITHUB_OAUTH_SECRET"));
        gitHubClient.setScope("read:user"); // no need for anything else than getting the username
        final Config config = new Config(getenv("GITHUB_CALLBACK_URL"), gitHubClient);

        CallbackHandler callback = new CallbackHandler(config, null, true);
        app.get("/auth/login/github", callback);
        app.post("/auth/login/github", callback);
        app.get("/auth/logout", new LogoutHandler(config, "/"));

        // redirect to your favourite page if logged in
        app.get("/", ctx -> {
            Optional<GitHubProfile> profile = retrieveProfile(ctx);
            if (profile.isPresent()) {
                ctx.redirect("/stats/" + profile.get().getUsername());
            } else {
                ctx.render("login.jte", Map.of("totalCount", githubProfileDao.totalCount()));
            }
        });

        app.before("/stats/{githubLogin}", new SecurityHandler(config, "GitHubClient"));
        // special redirect
        app.get("/stats/my-github-stats", ctx -> {
            Optional<GitHubProfile> profile = retrieveProfile(ctx);
            ctx.redirect("/stats/" + profile.get().getUsername());
        });
        app.get("/stats/{githubLogin}", ctx -> {
            GitHubProfile profile = retrieveProfile(ctx).get();
            String login = profile.getUsername();

            // redirect if this is not for the user...
            if (!login.equals(ctx.pathParam("githubLogin"))) {
                ctx.redirect("/stats/" + login);
            }

            Map<EventType, Integer> eventsPerType = githubProfileDao.fetchTypesAndCount(login);
            LinkedHashMap<String, Integer> eventsPerYear = githubProfileDao.activityByYear(login);

            Optional<String> mostActiveYear = eventsPerYear.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
            Optional<String> firstYear = eventsPerYear.keySet().stream().min(Comparator.comparing(String::toString));
            Optional<EventType> mostActiveType = eventsPerType.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
            long totalCount = githubProfileDao.totalCount();
            Optional<String> repoWithMostActivity = githubProfileDao.repoWithMostActivity(login);
            long totalCountReposInteractedWith = githubProfileDao.totalRepoCountInteractedWith(login);

            // figure out which event types this user is not doing
            List<EventType> unusedEventTypes = new ArrayList<>(githubProfileDao.fetchAllEventTypes());
            unusedEventTypes.removeAll(eventsPerType.keySet());

            int totalEvents = eventsPerType.values().stream().reduce(Integer::sum).orElse(0);

            Map<String, Object> renderMap = new HashMap<>();
            renderMap.put("profile", profile);
            renderMap.put("eventsPerType", eventsPerType);
            renderMap.put("eventsPerYear", eventsPerYear);
            renderMap.put("mostActiveYear", mostActiveYear);
            renderMap.put("mostActiveType", mostActiveType);
            renderMap.put("firstYear", firstYear);
            renderMap.put("totalEvents", totalEvents);
            renderMap.put("repoWithMostActivity", repoWithMostActivity);
            renderMap.put("totalCountReposInteractedWith", totalCountReposInteractedWith);
            renderMap.put("unusedEventTypes", unusedEventTypes);
            renderMap.put("totalCount", totalCount);
            ctx.render("activity.jte", renderMap);
        });
    }

    private Jdbi setupJDBI() throws SQLException {
        String user = getenv("FIREBOLT_USER");
        String password = getenv("FIREBOLT_PASSWORD");
        String engine = getenv("FIREBOLT_ENGINE");
        String database = getenv("FIREBOLT_DATABASE");

        String urlEncodedEngine = URLEncoder.encode(engine, StandardCharsets.UTF_8);
        String urlEncodedUser = URLEncoder.encode(user, StandardCharsets.UTF_8);
        String urlEncodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
        String url = String.format(Locale.ROOT, "jdbc:firebolt://api.app.firebolt.io/%s?engine=%s&user=%s&password=%s",
                database, urlEncodedEngine, urlEncodedUser, urlEncodedPassword);

        logger.info("JDBC URL is [jdbc:firebolt://api.app.firebolt.io/{}?engine={}&user={}&password=XXX]", database,
                urlEncodedEngine, urlEncodedUser);

        this.connection = DriverManager.getConnection(url);
        Jdbi jdbi = Jdbi.create(connection);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    public void run() {
        app.start("0.0.0.0", 7000);
    }

    public void stop() {
        // shutdown web server
        app.stop();
        // shut down JDBC
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Optional<GitHubProfile> retrieveProfile(Context ctx) {
        return new ProfileManager(new JavalinWebContext(ctx), JEESessionStore.INSTANCE).getProfile(GitHubProfile.class);
    }

    private static String getenv(String name) {
        String value = System.getenv(name);
        requireNonNull(value, name + " must be set");
        return value;
    }
}
