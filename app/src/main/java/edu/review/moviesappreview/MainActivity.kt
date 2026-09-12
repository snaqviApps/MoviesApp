package edu.review.moviesappreview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import edu.review.moviesappreview.presentation.screen.MoviesScreen
import edu.review.moviesappreview.presentation.screen.system.PowerStatusScreen
import edu.review.moviesappreview.ui.theme.MoviesAppReviewTheme
import edu.review.moviesappreview.util.checkAndRequestNotificationPermission

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 👈 ADD THIS DECLARATION
    private external fun doNotAnyThing()

    // Sample JNI C++ Bridge test
    init {
        System.loadLibrary("moviesappreview")
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("Permission: ", "Granted")
        } else {
            Log.i("Permission: ", "Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        doNotAnyThing()

        // PHASE 1: Run OS/Activity Level Setup
        checkAndRequestNotificationPermission(
            this,
            requestPermissionLauncher = requestPermissionLauncher
        )

        // PHASE 2: Run Compose UI Level Setup
        setContent {
            MoviesAppReviewTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.LightGray
                ) { innerPadding ->

                    // PowerStatusScreen lives here safely inside the Compose tree.
                    // Because it has no visible layout (it only contains the DisposableEffect),
                    // it sits here invisibly acting as your background listener.
                    PowerStatusScreen()

                    MoviesScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoviesAppReviewTheme {
        MoviesScreen(
            modifier = Modifier
        )
    }
}