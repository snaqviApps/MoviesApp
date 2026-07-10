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
- below are two images

<table>
  <tr>
<td>


<b>Populr Movies Screen</b>
<img width="1080" height="2232" alt="popular" src="https://github.com/user-attachments/assets/46ba5644-9ca6-4173-b0e1-ec832ead9b26" />
" />
</td>

##
<td> 
  <b>Top rated movies Screen</b>
  <img width="1080" height="2232" alt="Top_Rated" src="https://github.com/user-attachments/assets/4e4098e6-022c-47db-8bb0-53a9e1c58e43" />

  </td>
  </tr>
</table>
