@import gg.jte.support.ForSupport
@import io.firebolt.EventType
@import io.firebolt.TemplateHelpers
@import java.util.Collections
@import java.util.List
@import java.util.Map
@import java.util.Optional
@import java.util.stream.Collectors
@import static io.firebolt.TemplateHelpers.reverse
@import static io.firebolt.TemplateHelpers.stringify

@param org.pac4j.oauth.profile.github.GitHubProfile profile
@param Map<EventType, Integer> eventsPerType
@param Map<String, Integer> eventsPerYear
@param Integer totalEvents
@param Optional<String> mostActiveYear
@param Optional<EventType> mostActiveType
@param Optional<String> firstYear
@param Optional<String> repoWithMostActivity
@param List<EventType> unusedEventTypes 
@param long totalCount
@param long totalCountReposInteractedWith

@template.header(profile = Optional.of(profile))

    <div style="display: flex">
        <div style="width: 350px; padding-right: 3em; flex: 0 0 auto;">
            <h5 class="red">By Event Type</h5>
            <ul>
                @for(var entry : eventsPerType.entrySet())
                    <li>
                        ${entry.getKey().displayName()}
                        <span class="red">(${entry.getValue()})</span></li>
                @endfor
            </ul>
            <h5 class="red">By Year
                @if(eventsPerYear.size() > 4)
                    <span style="padding-left: 5px;"></span>
                    <span class="line" data-peity='{ "fill": ["#f82a2f" ], "height": 12, "width": 60, "stroke": "black" }'>0, ${String.join(", ", stringify(reverse(eventsPerYear.values())))}</span>
                @endif
            </h5>
            <ul>
                @for(var entry : eventsPerYear.entrySet())
                    <li>${entry.getKey().replaceFirst("Event$", "")} <span class="red">(${entry.getValue()})</span></li>

                @endfor
            </ul>
        </div>
        <div style="flex-grow: 1">
            <h2>Welcome ${profile.getDisplayName()}!</h2>
            <p>
                Are you ready to take a look at your GitHub history? Let's see where you excel at! We took a look at <b class="red" style="font-size: larger">${java.text.NumberFormat.getInstance().format(totalCount)}</b> events in total!
            </p>

            @if(mostActiveYear.isPresent() && firstYear.isPresent())
                <p>Your most active year was <span class="red">${mostActiveYear.get()}</span> (starting in <span class="red">${firstYear.get()}</span>).</p>
            @endif

            @if(mostActiveType.isPresent())
                <p>
                    Over the course of your GitHub career about <span class="red">${totalEvents}</span> events have been found, most of them have been <span class="red">${mostActiveType.get().displayName()}</span>.
                    You interacted across <span class="red">${totalCountReposInteractedWith}</span> repositories in total, including forks and your own repositories!
                </p>
            @endif

            @if (repoWithMostActivity.isPresent())
                <p>The repository you interacted with the most is <a href="https://github.com/${repoWithMostActivity.get()}">${repoWithMostActivity.get()}</a></p>
            @endif

            @if(unusedEventTypes.isEmpty())
                <p>Wow, you also used all the event types. You seem to be quite the GitHub expert!</p>
            @else
                <p>
                You have never interacted with GitHub to trigger the following events:
                @for(var entryLoop : ForSupport.of(unusedEventTypes))
                    <span class="red">${entryLoop.get().displayName()}</span>@if(!entryLoop.isLast()), @else.@endif
                @endfor
                </p>
            @endif

            <p>
            Soon we'll talk about your top10 most commented issues and PRs!
            </p>


        </div>
    </div>

@template.footer()