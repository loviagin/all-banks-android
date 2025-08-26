package com.lovigin.android.allbanks.ui.welcome

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.prefs.Prefs
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    pages: List<WelcomePage> = welcomePages,
    onFinish: () -> Unit // куда уйти после «Start»
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    var buttonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        if (buttonPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val bg by animateColorAsState(
        targetValue = pages[pagerState.currentPage].color.copy(alpha = 0.1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bgColor"
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(bg)) {
        // Пейджер
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val p = pages[page]
            WelcomePageContent(
                title = p.title,
                subtitle = p.subtitle,
                color = p.color,
                icon = p.icon
            )
        }

        // Индикаторы + Кнопка
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicators(
                total = pages.size,
                current = pagerState.currentPage,
                activeColor = pages[pagerState.currentPage].color
            )
            Spacer(Modifier.height(20.dp))

            val isLast = pagerState.currentPage == pages.lastIndex
            val buttonText = if (isLast) "Start" else "Continue"

            Button(
                onClick = {
                    buttonPressed = true
                    scope.launch {
                        // маленький «пых» и обратно
                        kotlinx.coroutines.delay(100)
                        buttonPressed = false
                        if (!isLast) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            // В твоём Swift в конце ставится hasSeenWelcome = false.
                            // Повторю точно так же (если надо «true» — поменяем).
                            setHasSeenWelcome(context, false)
                            onFinish()
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .scale(buttonScale),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomePageContent(
    title: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(30.dp))
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        )
    }
}

@Composable
private fun PageIndicators(total: Int, current: Int, activeColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { idx ->
            val selected = idx == current
            Box(
                modifier = Modifier
                    .size(if (selected) 10.dp else 8.dp)
                    .background(
                        color = if (selected) activeColor else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        pages = welcomePages,
        onFinish = {}
    )
}

// повторяем поведение твоего @AppStorage
private fun setHasSeenWelcome(context: Context, value: Boolean) {
    // fire-and-forget
    kotlinx.coroutines.GlobalScope.launch {
        Prefs.setHasSeenWelcome(context, value)
    }
}