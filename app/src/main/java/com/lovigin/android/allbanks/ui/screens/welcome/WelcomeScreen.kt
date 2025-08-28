package com.lovigin.android.allbanks.ui.screens.welcome

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.prefs.setHasSeenWelcome
import kotlinx.coroutines.launch

data class WelcomePage(
    val title: String,
    val subtitle: String,
    val color: Color,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val pages = remember {
        listOf(
            WelcomePage(
                title = "💰 Your finances in one place",
                subtitle = "Combine all accounts, cards and crypto wallets in one app. Control your finances now in your hands.",
                color = Color(red = 0.2f, green = 0.5f, blue = 0.8f),
                icon = { Icon(Icons.Default.Savings, contentDescription = null, tint = Color(red = 0.2f, green = 0.5f, blue = 0.8f), modifier = Modifier.size(80.dp)) }
            ),
            WelcomePage(
                title = "🔒 Maximum privacy",
                subtitle = "Your data is stored only on your device. No servers, no analytics — only your privacy.",
                color = Color(red = 0.2f, green = 0.7f, blue = 0.3f),
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(red = 0.2f, green = 0.7f, blue = 0.3f), modifier = Modifier.size(80.dp)) }
            ),
            WelcomePage(
                title = "🌍 Multi-currency control",
                subtitle = "Instant conversion of balances to any currency. Track your assets in the format you prefer.",
                color = Color(red = 0.6f, green = 0.2f, blue = 0.8f),
                icon = { Icon(Icons.Default.Public, contentDescription = null, tint = Color(red = 0.6f, green = 0.2f, blue = 0.8f), modifier = Modifier.size(80.dp)) }
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.95f else 1f, label = "btnScale")

    val bg = pages[pagerState.currentPage].color.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val p = pages[page]
            WelcomePageView(p)
        }

        // Bottom controls
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            // Indicators
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { idx ->
                    val isCurrent = idx == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 10.dp else 8.dp)
                            .background(
                                color = if (isCurrent) pages[pagerState.currentPage].color else Color.Gray.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }

            // Continue / Start button
            val isLast = pagerState.currentPage == pages.lastIndex
            Button(
                onClick = {
                    pressed = true
                    scope.launch {
                        // маленький "пульс"
                        kotlinx.coroutines.delay(100)
                        pressed = false
                        if (!isLast) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            setHasSeenWelcome(context, true)
                            onFinish()
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .scale(scale)
            ) {
                Text(if (isLast) "Start" else "Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WelcomePageView(p: WelcomePage) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        p.icon()
        Spacer(Modifier.height(24.dp))
        Text(
            p.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = p.color,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            p.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        Spacer(Modifier.weight(1f))
    }
}