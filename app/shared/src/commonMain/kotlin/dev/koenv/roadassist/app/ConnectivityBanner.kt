package dev.koenv.roadassist.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun ConnectivityBanner(visible: Boolean) {
    if (!visible) return
    val bg = RoadAssistColors.Accent
    val fg = RoadAssistColors.AccentForeground
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ServerOfflineIcon(color = fg)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Server unreachable. Check your connection.",
                style = MaterialTheme.typography.bodySmall,
                color = fg,
            )
        }
}

@Composable
private fun ServerOfflineIcon(color: androidx.compose.ui.graphics.Color) {
    val tint = color
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(16.dp)
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.width * 0.42f
                val stroke = size.width * 0.12f
                drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))
                val gap = r * 0.32f
                drawLine(
                    color = tint,
                    start = Offset(cx, cy - r * 0.55f),
                    end = Offset(cx, cy + gap),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawCircle(color = tint, radius = stroke * 0.6f, center = Offset(cx, cy + r * 0.52f))
            },
    )
}
