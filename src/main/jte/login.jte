@param long totalCount

@template.header(profile = java.util.Optional.<org.pac4j.oauth.profile.github.GitHubProfile>empty())

Welcome to check your GitHub contributions! Click on Login and we will take a look at more than a decade of your GitHub contributions across <b style="text-decoration: underline; font-size: larger">${java.text.NumberFormat.getInstance().format(totalCount)}</b> events.

This service is powered by <a href="https://firebolt.io">Firebolt</a> and uses the publicly available <a href="https://gharchive.org">Github Archive</a> data.

@template.footer()
