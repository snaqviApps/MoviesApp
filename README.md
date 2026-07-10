This App displays a list of movies from the TMDB API, with 03 endpoints
 - Popular
 - Top Rated
 - Now Playing

App flows by Initializing with TMDB integration, and gives user options using CTAs to select the end points
as mentioned above

ChangeList:
- Implement the initial project structure using modern Android development patterns, including Jetpack Compose, and Retrofit.
- Set up project configuration with Version Catalogs (`libs.versions.toml`), KSP, and Kotlin Serialization.
- Implement the data layer using Retrofit and Gson to fetch movie data from TMDB endpoints (Popular, Top Rated, and Now Playing).
- Add a `MoviesRepository` and `MovieViewModel` to handle data operations and UI state management via `StateFlow`.
- Create a reactive UI using Compose and Material 3, including `MoviesScreen` for state handling and `MovieCard` for displaying results with Coil image loading.
- Configure app permissions and theme settings, including dynamic color support and custom typography.