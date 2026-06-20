package dev.koenv.roadassist.app.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.koenv.roadassist.app.theme.RoadAssistColors
import dev.koenv.roadassist.app.ui.auth.login.RoadAssistAppIcon

@Composable
internal fun AuthBrandingPanel(modifier: Modifier = Modifier) {
    // full-height dark sidebar
    Box(modifier = modifier.background(RoadAssistColors.BrandDark), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(40.dp)) {
            RoadAssistAppIcon(size = 56.dp, lightScheme = false) // app icon
            Spacer(Modifier.height(40.dp))
            Box(modifier = Modifier.height(2.dp).width(40.dp).background(RoadAssistColors.Primary)) // accent bar
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Help on the way, faster.", // tagline
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Report a breakdown with your location and a photo. Dispatchers triage every report and keep you updated.", // description
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA2B1), lineHeight = 20.sp),
            )
        }
    }
}
