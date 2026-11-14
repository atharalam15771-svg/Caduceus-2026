package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaduceusUser
import com.example.data.model.ZodiacSignInfo
import com.example.data.repository.CaduceusRepository
import com.example.data.util.CaduceusAudioManager
import com.example.data.util.ZodiacCatalog
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.launch

@Composable
fun HoroscopeScreen(
    user: CaduceusUser,
    repository: CaduceusRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedSign by remember { mutableStateOf(ZodiacCatalog.getForSign(user.zodiacSign)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "THE HOROSCOPE CHAMBER",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = TextWhite,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "WHERE STARS ALIGN WITH DARKNESS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = AntiqueGold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active Zodiac Feature Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(AntiqueGold, DarkBloodRed)),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DarkObsidian)
                        .border(1.5.dp, AntiqueGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedSign.symbol,
                        fontSize = 42.sp,
                        color = AntiqueGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = selectedSign.sign.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )

                Text(
                    text = "${selectedSign.dates} • ${selectedSign.element} Element",
                    style = MaterialTheme.typography.labelMedium.copy(color = AntiqueGold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "“${selectedSign.darkPersonality}”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextWhite,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = DarkObsidian,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DAILY FESTIVAL FORTUNE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AntiqueGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedSign.dailyHoroscope,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextLightGrey,
                                lineHeight = 20.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lucky Colour: ${selectedSign.luckyColor}",
                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold)
                            )
                            Text(
                                text = "Lucky Number: ${selectedSign.luckyNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!user.zodiacSign.equals(selectedSign.sign, ignoreCase = true)) {
                    Button(
                        onClick = {
                            CaduceusAudioManager.playReveal(context)
                            scope.launch {
                                repository.updateUserZodiac(user.userId, selectedSign.sign)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "LINK TO MY FESTIVAL PROFILE",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextWhite)
                        )
                    }
                } else {
                    Surface(
                        color = PrimaryCrimson.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "YOUR ACTIVE ZODIAC SIGN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AntiqueGold,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SELECT ZODIAC ARCHETYPE",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = AntiqueGold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of 12 signs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ZodiacCatalog.signs.chunked(3).forEach { rowSigns ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSigns.forEach { signInfo ->
                        val isSelected = signInfo.sign == selectedSign.sign
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    CaduceusAudioManager.playTap(context)
                                    selectedSign = signInfo
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkBloodRed else SurfaceCard
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.verticalGradient(
                                    if (isSelected) listOf(AntiqueGold, PrimaryCrimson) else listOf(BorderDark, BorderDark)
                                ),
                                width = 1.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = signInfo.symbol, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = signInfo.sign,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) TextWhite else TextLightGrey,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
