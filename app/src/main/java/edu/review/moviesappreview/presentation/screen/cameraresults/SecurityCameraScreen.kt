package edu.review.moviesappreview.presentation.screen.cameraresults

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.review.moviesappreview.presentation.viewmodel.MoviesViewModel

@Composable
fun SecurityCameraScreen(
    modifier: Modifier = Modifier,
    streamUrl: String,
    viewModel: MoviesViewModel = viewModel(),
    onSecurityStream: @Composable (isSecurityCamera: Boolean, streamUrl: String) -> Unit

) {
    onSecurityStream(
        viewModel.enableSecurityCamera,
        streamUrl
    )
}