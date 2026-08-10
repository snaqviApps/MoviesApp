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
<b>ExoPlayer branch</b> carries the major changes below: 

1. ExoPlayer Integration (VideoPlayer.kt):
- Uses Media3 ExoPlayer to handle video playback.
- Wraps PlayerView in an AndroidView for Compose compatibility.
- Configures a MediaItem from a stream URL and ensures proper resource management using DisposableEffect (releasing the player when the composable leaves the composition).


2. UI Components for Video:
- VideoPlayerDialog.kt: A full-screen or modal dialog that hosts the VideoPlayer. It includes an overlaid "Close" button to dismiss the stream.
- SecurityCameraScreen.kt: A wrapper component that observes the enableSecurityCamera state and triggers the stream overlay.

 
3. ViewModel State Management (MoviesViewModel.kt): 
- Introduces enableSecurityCamera (a mutableStateOf boolean) to toggle the visibility of the security stream.
- Added enableExoPlayerDefaults() to switch the camera state (effectively closing the dialog when triggered by the UI).
  
    
4. UI Components for Video:

6. Key Dependencies Added 
• androidx.media3:media3-exoplayer
• androidx.media3:media3-ui
• androidx.media3:media3-exoplayer-hls
• androidx.media3:media3-exoplayer-rtsp

--------------------------
<b>Branch: alternate-parallel-backend-calls</b>

- The new implementation in MoviesViewModel utilizes a "racing" pattern to fetch movie data from two different endpoints simultaneously, opting for the result that arrives first.
Key Components:

1. async { ... }: Launches two concurrent coroutines.
   popularDeferred: Fetches movies based on the provided endPoint (defaults to "popular"). topRatedDeferred: Fetches movies from the "top_rated" endpoint.

2. select { ... }: Acts as a race coordinator. 
   onAwait: Suspends until the first Deferred completes. Cancellation: Immediately cancels the "loser" coroutine (topRatedDeferred.cancel() or popularDeferred.cancel()) to save resources.

3. Broadcast Integration: Dispatches a MovieBroadcastReceiver.ACTION_RACE_COMPLETE intent, allowing other parts of the app to react to the winner.

Implementation Details:
• Structured Concurrency: Wrapped in coroutineScope to ensure that if the parent viewModelScope is cancelled, all internal async tasks are also cleaned up.
• State Management: Updates _moviesState with the winner's data and sets currentEndPoint to reflect the winning category.
• Error Handling: Catches exceptions and updates the UI state to MovieUIState.Error, while properly re-throwing CancellationException to maintain coroutine hygiene.

Observations & Recommendations:
• Efficiency: This approach effectively reduces perceived latency by not waiting for a specific endpoint if another is faster.
• Resource Usage: While it doubles the initial request count, the immediate cancellation of the slower request mitigates unnecessary data usage.
• Scalability: This pattern can be extended to more than two sources if needed (e.g., racing multiple mirrors or cache vs. network).



<table>
  <!-- Row 1: Titles -->
  <tr>
    <td align="center" valign="bottom">
      <b>ExoPlayer on Display</b>
    </td>
    <td align="center" valign="bottom">
      <b>Winner Movie Notification</b>
    </td>
  </tr>
  <!-- Row 2: Images -->
  <tr>
    <td valign="top">
      <img width="1080" height="2400" alt="Screenshot_20260801_104759" src="https://github.com/user-attachments/assets/ffc7e18f-d8d2-4313-9e60-4bbe427678a3" />
    </td>
    <td valign="top">
      <img width="1080" height="2400" alt="Screenshot_20260801_104858" src="https://github.com/user-attachments/assets/93359dfd-dd61-4a99-bc0a-5e27b424e36f" />
    </td>
  </tr>
</table>
