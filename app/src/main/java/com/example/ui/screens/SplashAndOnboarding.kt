package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        delay(2600)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(DarkBloodRed.copy(alpha = 0.35f), VoidBlack, VoidBlack),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.caduceus_logo),
                contentDescription = "CADUCEUS 2026 Logo",
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .border(2.dp, AntiqueGold.copy(alpha = 0.7f), CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CADUCEUS 2026",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = TextWhite,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "HORROR × HOROSCOPE",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = PrimaryCrimson,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "WHERE STARS ALIGN WITH DARKNESS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AntiqueGold,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var stepIndex by remember { mutableStateOf(0) }

    val steps = listOf(
        Triple(
            "DISCOVER CADUCEUS 2026",
            "Enter the darkest, most cinematic cultural festival of the year where ancient horoscope mysticism intertwines with gothic horror.",
            "♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓"
        ),
        Triple(
            "EXPLORE COMPETITIONS",
            "Compete across football tournaments, battle of the bands, gothic fashion runway, dance battles, and occult esports arena.",
            "⚽ 🎸 🎭 💃 🎮 🎨"
        ),
        Triple(
            "INSTANT EVENT CALL-OUTS",
            "Never miss a match or performance. Receive immediate real-time notifications with venue instructions when events are about to start.",
            "⚡ 📢 📍 🏆"
        ),
        Triple(
            "YOUR DIGITAL FEST PASS",
            "Unlock an encrypted digital festival pass with a unique QR code for seamless venue entry, attendance verification, and privileges.",
            "🎫 🛡 ⚜ ⚔"
        )
    )

    val current = steps[stepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.caduceus_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "CADUCEUS 2026",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                        color = AntiqueGold
                    )
                )
            }

            // Middle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(AntiqueGold, DarkBloodRed)),
                    width = 1.5.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = current.third,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = current.first,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = current.second,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextLightGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            // Bottom Navigation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (idx == stepIndex) 20.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (idx == stepIndex) PrimaryCrimson else BorderDark)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (stepIndex < steps.size - 1) {
                            stepIndex++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson)
                ) {
                    Text(
                        text = if (stepIndex == steps.size - 1) "ENTER THE FESTIVAL" else "CONTINUE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
