package edu.review.moviesappreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import edu.review.moviesappreview.presentation.screen.MoviesScreen
import edu.review.moviesappreview.ui.theme.MoviesAppReviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoviesAppReviewTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.LightGray
                ) { innerPadding ->
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