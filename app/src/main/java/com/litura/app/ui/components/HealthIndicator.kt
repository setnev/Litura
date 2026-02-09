package com.litura.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.litura.app.ui.theme.HealthEmpty
import com.litura.app.ui.theme.HealthGreen
import com.litura.app.ui.theme.HealthRed
import com.litura.app.ui.theme.HealthYellow

@Composable
fun HealthIndicator(
    currentHealth: Int,
    maxHealth: Int = 10,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxHealth) { index ->
            val filled = index < currentHealth
            val color by animateColorAsState(
                targetValue = when {
                    !filled -> HealthEmpty
                    currentHealth >= 8 -> HealthGreen
                    currentHealth >= 4 -> HealthYellow
                    else -> HealthRed
                },
                label = "health_segment"
            )
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
        Text(
            text = "$currentHealth",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
