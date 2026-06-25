package com.example.incluapp.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.incluapp.R
import com.example.incluapp.ui.theme.PrimaryBackground
import com.example.incluapp.ui.theme.PrimaryYellow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3_000)
        onNavigateToHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.puce_logo),
                contentDescription = "Logo PUCE",
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(74.dp),
                contentScale = ContentScale.Fit
            )

            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = null,
                tint = PrimaryYellow,
                modifier = Modifier.size(58.dp)
            )

            Text(
                text = "LexiEdu",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryYellow,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Emprendimiento Tecnologico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Pontificia Universidad Catolica del Ecuador",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Grupo 3",
                style = MaterialTheme.typography.titleSmall,
                color = PrimaryYellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Juan C. Cevallos\nKevin Daniel Cepeda Lema\nSteven Ariel Rosero\nVictoria Yulieth Galarza Taco",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}
