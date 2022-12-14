@import org.pac4j.oauth.profile.github.GitHubProfile
@import java.util.Optional

@param Optional<GitHubProfile> profile

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My GitHub activity</title>
    <script type="text/javascript" src="/peity-vanilla.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/mvp.css/1.12.0/mvp.min.css" integrity="sha512-uGSbf4PlkW85oi5DOO69hz9QOSwg6aCglJy1eVj7bCgZ85sNx1UuiXqHv8pTDcRp94luyTRjFJ6WKfy1+WkCwA==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <style type="text/css">
@font-face {
  font-family: 'Poppins';
  src: url('https://assets.website-files.com/5e8a264ceaf4870394477fc7/5efa68696b50751bf6fe1032_poppins-v9-latin-600.woff2') format('woff2');
  font-weight: 600;
  font-style: normal;
  font-display: auto;
}
@font-face {
  font-family: 'Poppins';
  src: url('https://assets.website-files.com/5e8a264ceaf4870394477fc7/5efa6869e1b0eb32dd770a20_poppins-v9-latin-900.woff2') format('woff2');
  font-weight: 900;
  font-style: normal;
  font-display: auto;
}
@font-face {
  font-family: 'Poppins';
  src: url('https://assets.website-files.com/5e8a264ceaf4870394477fc7/5efa68693e93733a4509e008_poppins-v9-latin-700.woff2') format('woff2');
  font-weight: 700;
  font-style: normal;
  font-display: auto;
}
@font-face {
  font-family: 'Poppins';
  src: url('https://assets.website-files.com/5e8a264ceaf4870394477fc7/5efa6869bf02b433f86ceb5d_poppins-v9-latin-regular.woff2') format('woff2');
  font-weight: 400;
  font-style: normal;
  font-display: auto;
}
:root {
  --font-family: Poppins, sans-serif;
}
.red { color: #f82a2f; }
a { color: #f82a2f; }
button {
  padding: 4px 8px 4px 8px;
  background-color: #f82a2f;
  border-color: #f82a2f;
  color: white;
  font-weight: 600;
}
header, footer {
  padding: 1em 0;
}
</style>
</head>
<body>

<header>
    <nav style="margin-bottom: 0">
        <a href="http://firebolt.io">
            <img src="https://assets.website-files.com/5e8a264ceaf4870394477fc7/5e8a264ceaf4879f75477fdd_logo_website.svg"/>
        </a>
        <h1>My GitHub Activity</h1>
        @if(profile.isPresent() && profile.get().getPictureUrl() != null)
            <a href="${profile.get().getProfileUrl().toASCIIString()}"><img src="${profile.get().getPictureUrl().toString()}" width="40"></a>
        @endif
        <a><img src="https://github.githubassets.com/images/modules/logos_page/Octocat.png" width="40" /> </a>
        @if(profile.isPresent())
            <a href="/auth/logout"><button>Logout</button></a>
        @else
            <a href="/stats/my-github-stats"><button>Login</button></a>
        @endif
    </nav>
</header>

<main style="padding-top: 0">
