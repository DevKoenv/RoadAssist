package dev.koenv.roadassist.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import roadassist.app.shared.generated.resources.Res
import roadassist.app.shared.generated.resources.ibm_plex_mono_medium
import roadassist.app.shared.generated.resources.ibm_plex_mono_regular
import roadassist.app.shared.generated.resources.ibm_plex_mono_semibold
import roadassist.app.shared.generated.resources.ibm_plex_sans_bold
import roadassist.app.shared.generated.resources.ibm_plex_sans_italic
import roadassist.app.shared.generated.resources.ibm_plex_sans_medium
import roadassist.app.shared.generated.resources.ibm_plex_sans_regular
import roadassist.app.shared.generated.resources.ibm_plex_sans_semibold
import roadassist.app.shared.generated.resources.space_grotesk_bold
import roadassist.app.shared.generated.resources.space_grotesk_medium
import roadassist.app.shared.generated.resources.space_grotesk_regular
import roadassist.app.shared.generated.resources.space_grotesk_semibold

object RoadAssistColors {
    val Primary = Color(0xFFE0590B)
    val PrimaryForeground = Color(0xFFFFFFFF)
    val BrandDark = Color(0xFF1A1D23)
    val BrandDarkSurface = Color(0xFF2A2E38)
    val Background = Color(0xFFFFFFFF)
    val Foreground = Color(0xFF15171C)
    val Card = Color(0xFFFFFFFF)
    val CardForeground = Color(0xFF15171C)
    val Secondary = Color(0xFFF1F3F6)
    val SecondaryForeground = Color(0xFF15171C)
    val Muted = Color(0xFFF7F8FA)
    val MutedForeground = Color(0xFF646B7A)
    val Accent = Color(0xFFFBEDE2)
    val AccentForeground = Color(0xFF9C3C05)
    val Destructive = Color(0xFFD92D20)
    val DestructiveForeground = Color(0xFFFFFFFF)
    val Border = Color(0xFFE4E7EC)
    val Input = Color(0xFFE4E7EC)
    val Ink2 = Color(0xFF3A3F4B)
    val Ink3 = Color(0xFF646B7A)
    val Ink4 = Color(0xFF8B93A3)

    val StatusNew = Color(0xFF2563EB)
    val StatusNewBg = Color(0xFFE8EFFE)
    val StatusInProgress = Color(0xFFB7791F)
    val StatusInProgressBg = Color(0xFFFBF0D9)
    val StatusEnRoute = Color(0xFF7C3AED)
    val StatusEnRouteBg = Color(0xFFF0E9FD)
    val StatusResolved = Color(0xFF0F8A5F)
    val StatusResolvedBg = Color(0xFFE2F4EC)
}

data class RoadAssistExtendedColors(
    val statusNew: Color,
    val statusNewBg: Color,
    val statusInProgress: Color,
    val statusInProgressBg: Color,
    val statusEnRoute: Color,
    val statusEnRouteBg: Color,
    val statusResolved: Color,
    val statusResolvedBg: Color,
    val accent: Color,
    val accentForeground: Color,
    val muted: Color,
    val mutedForeground: Color,
    val border: Color,
    val input: Color,
    val ink2: Color,
    val ink3: Color,
    val ink4: Color,
)

val LocalRoadAssistColors = staticCompositionLocalOf {
    RoadAssistExtendedColors(
        statusNew = RoadAssistColors.StatusNew,
        statusNewBg = RoadAssistColors.StatusNewBg,
        statusInProgress = RoadAssistColors.StatusInProgress,
        statusInProgressBg = RoadAssistColors.StatusInProgressBg,
        statusEnRoute = RoadAssistColors.StatusEnRoute,
        statusEnRouteBg = RoadAssistColors.StatusEnRouteBg,
        statusResolved = RoadAssistColors.StatusResolved,
        statusResolvedBg = RoadAssistColors.StatusResolvedBg,
        accent = RoadAssistColors.Accent,
        accentForeground = RoadAssistColors.AccentForeground,
        muted = RoadAssistColors.Muted,
        mutedForeground = RoadAssistColors.MutedForeground,
        border = RoadAssistColors.Border,
        input = RoadAssistColors.Input,
        ink2 = RoadAssistColors.Ink2,
        ink3 = RoadAssistColors.Ink3,
        ink4 = RoadAssistColors.Ink4,
    )
}

private val RoadAssistColorScheme = lightColorScheme(
    primary = RoadAssistColors.Primary,
    onPrimary = RoadAssistColors.PrimaryForeground,
    primaryContainer = RoadAssistColors.Accent,
    onPrimaryContainer = RoadAssistColors.AccentForeground,
    secondary = RoadAssistColors.Secondary,
    onSecondary = RoadAssistColors.SecondaryForeground,
    background = RoadAssistColors.Background,
    onBackground = RoadAssistColors.Foreground,
    surface = RoadAssistColors.Card,
    onSurface = RoadAssistColors.CardForeground,
    surfaceVariant = RoadAssistColors.Muted,
    onSurfaceVariant = RoadAssistColors.MutedForeground,
    error = RoadAssistColors.Destructive,
    onError = RoadAssistColors.DestructiveForeground,
    outline = RoadAssistColors.Border,
    outlineVariant = RoadAssistColors.Input,
)

@Composable
fun spaceGroteskFamily() = FontFamily(
    Font(Res.font.space_grotesk_regular, weight = FontWeight.Normal),
    Font(Res.font.space_grotesk_medium, weight = FontWeight.Medium),
    Font(Res.font.space_grotesk_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.space_grotesk_bold, weight = FontWeight.Bold),
)

@Composable
fun ibmPlexSansFamily() = FontFamily(
    Font(Res.font.ibm_plex_sans_regular, weight = FontWeight.Normal),
    Font(Res.font.ibm_plex_sans_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(Res.font.ibm_plex_sans_medium, weight = FontWeight.Medium),
    Font(Res.font.ibm_plex_sans_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.ibm_plex_sans_bold, weight = FontWeight.Bold),
)

@Composable
fun ibmPlexMonoFamily() = FontFamily(
    Font(Res.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
    Font(Res.font.ibm_plex_mono_medium, weight = FontWeight.Medium),
    Font(Res.font.ibm_plex_mono_semibold, weight = FontWeight.SemiBold),
)

@Composable
fun roadAssistTypography(): Typography {
    val display = spaceGroteskFamily()
    val sans = ibmPlexSansFamily()
    val mono = ibmPlexMonoFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 46.sp, lineHeight = (46 * 1.04f).sp, letterSpacing = (-0.025 * 46).sp),
        headlineLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, letterSpacing = (-0.02 * 30).sp),
        headlineMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.01 * 24).sp),
        headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, letterSpacing = (-0.01 * 19).sp),
        titleMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = (-0.01 * 15).sp),
        bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.5.sp, lineHeight = (15.5f * 1.62f).sp),
        bodyMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.5.sp),
        bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp),
        labelLarge = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 11.sp),
        labelSmall = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    )
}

@Composable
fun RoadAssistTheme(content: @Composable () -> Unit) {
    val extendedColors = RoadAssistExtendedColors(
        statusNew = RoadAssistColors.StatusNew,
        statusNewBg = RoadAssistColors.StatusNewBg,
        statusInProgress = RoadAssistColors.StatusInProgress,
        statusInProgressBg = RoadAssistColors.StatusInProgressBg,
        statusEnRoute = RoadAssistColors.StatusEnRoute,
        statusEnRouteBg = RoadAssistColors.StatusEnRouteBg,
        statusResolved = RoadAssistColors.StatusResolved,
        statusResolvedBg = RoadAssistColors.StatusResolvedBg,
        accent = RoadAssistColors.Accent,
        accentForeground = RoadAssistColors.AccentForeground,
        muted = RoadAssistColors.Muted,
        mutedForeground = RoadAssistColors.MutedForeground,
        border = RoadAssistColors.Border,
        input = RoadAssistColors.Input,
        ink2 = RoadAssistColors.Ink2,
        ink3 = RoadAssistColors.Ink3,
        ink4 = RoadAssistColors.Ink4,
    )
    CompositionLocalProvider(LocalRoadAssistColors provides extendedColors) {
        MaterialTheme(
            colorScheme = RoadAssistColorScheme,
            typography = roadAssistTypography(),
            content = content,
        )
    }
}
