package dev.koenv.roadassist.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.koenv.roadassist.app.data.storage.ActivityHolder

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
        ActivityHolder.locationPermissionDeferred?.complete(granted)
        ActivityHolder.locationPermissionDeferred = null
    }

    private val takePictureLauncher = registerForActivityResult(TakePicture()) { success ->
        ActivityHolder.cameraDeferred?.complete(success)
        ActivityHolder.cameraDeferred = null
    }

    private val pickGalleryLauncher = registerForActivityResult(GetContent()) { uri: Uri? ->
        ActivityHolder.galleryDeferred?.complete(uri)
        ActivityHolder.galleryDeferred = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)
        ActivityHolder.activity = this
        ActivityHolder.launchPermissionRequest = { permission -> permissionLauncher.launch(permission) }
        ActivityHolder.launchCamera = { uri -> takePictureLauncher.launch(uri) }
        ActivityHolder.launchGallery = { mimeType -> pickGalleryLauncher.launch(mimeType) }
        setContent { App() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityHolder.activity === this) {
            ActivityHolder.activity = null
            ActivityHolder.launchPermissionRequest = null
            ActivityHolder.launchCamera = null
            ActivityHolder.launchGallery = null
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
