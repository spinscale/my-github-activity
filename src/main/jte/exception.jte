@import java.util.Optional
@import org.pac4j.oauth.profile.github.GitHubProfile

@param Optional<GitHubProfile> profile
@param boolean databaseConnectionIssue

@template.header(profile = profile)

@if(databaseConnectionIssue)
    <p>This little app is currently in maintenance mode and will return shortly!</p>
@else
    <p>There was an error trying to fulfill your request. Please check back later.</p>
@endif

@template.footer()
