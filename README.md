# RecipeHub

Save recipes from Instagram Reels / TikTok by sharing them to the RecipeHub Android app.
It's a bookmarking tool: sharing a post saves its thumbnail, source link, and an
automatically-derived name (from the post's own caption — no AI involved) straight to a
local list. Ingredients and steps are optional notes you add yourself from the detail screen.

## Structure

- `android/` — Kotlin + Jetpack Compose app, frontend-only. Registers as a share target for
  `text/plain`. On share, it fetches the post's metadata directly (TikTok's public oEmbed
  endpoint, or Instagram's public page `og:meta` tags) over HTTPS, caches the thumbnail image
  locally, and derives a title heuristically — no backend server, no API keys.

## Running the app

Open `android/` in Android Studio. Copy `android/local.properties.example` to
`android/local.properties` and set `sdk.dir` to your Android SDK path. Build and run — no other
setup needed.
