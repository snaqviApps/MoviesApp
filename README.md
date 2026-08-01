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
- Add a `MoviesRepository` and `MoviesViewModel` to handle data operations and UI state management via `StateFlow`.
- Create a reactive UI using Compose and Material 3, including `MoviesScreen` for state handling and `MovieCard` for displaying results with Coil image loading.
- Configure app permissions and theme settings, including dynamic color support and custom typography.
- below are two images

--------------------------
<b>Branch: additional-advanced</b>
  [commit: bfe9cd6]
- displays the winner-job, and in turn cancels the other one

  [commit: ab9128b2]
- MainActivity.kt - Verified permission request logic.
- MoviesViewModel.kt - Verified explicit intent and action setting.
- MovieBroadcastReceiver.kt - Verified channel reset and high priority settings.

--------------------------



<table>
  <!-- Row 1: Titles -->
  <tr>
    <td align="center" valign="bottom">
      <b>Popular Movies Screen</b>
    </td>
    <td align="center" valign="bottom">
      <b>Top rated movies Screen</b>
    </td>
  </tr>
  <!-- Row 2: Images -->
  <tr>
    <td valign="top">
      <img width="1080" height="2232" alt="popular" src="https://github.com/user-attachments/assets/46ba5644-9ca6-4173-b0e1-ec832ead9b26" />
    </td>
    <td valign="top">
      <img width="1080" height="2232" alt="Top_Rated" src="https://github.com/user-attachments/assets/4e4098e6-022c-47db-8bb0-53a9e1c58e43" />
    </td>
  </tr>
</table>
